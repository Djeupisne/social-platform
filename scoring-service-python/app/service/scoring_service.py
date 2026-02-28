import logging
from datetime import datetime
from typing import List, Optional
from uuid import UUID

from sqlalchemy.orm import Session

from app.config.redis_config import cache_get, cache_set, cache_delete
from app.enums.categorie_sociale import CategorieSociale
from app.models.scoring_history import ScoringHistory
from app.repository.scoring_history_repository import scoring_history_repository
from app.schemas.scoring_schemas import ScoringRequest, ScoringResponse

logger = logging.getLogger(__name__)


class ScoringService:

    # ------------------------------------------------------------------
    # Calcul du score d'un ménage
    # ------------------------------------------------------------------
    def calculate_score(self, db: Session, request: ScoringRequest) -> ScoringResponse:
        logger.info("Calcul du score pour le ménage: %s", request.menage_id)

        score = 0

        # 1. Équipements du ménage
        if request.has_tv:         score += 5   # Télévision
        if request.has_radio:      score += 5   # Radio
        if request.has_motorcycle: score += 5   # Moto
        if request.has_car:        score += 10  # Voiture
        if request.is_owner:       score += 20  # Propriétaire

        logger.debug("Score après équipements: %d", score)

        # 2. Tranche salariale la plus élevée du ménage
        score += self._get_salary_points(request.max_salary)

        logger.debug("Score après salaire: %d", score)

        # 3. Détermination de la catégorie sociale
        categorie = self._determine_category(score)

        # 4. Sauvegarde de l'historique
        ancien_score = self._get_current_score(db, request.menage_id)
        ancienne_categorie = self._get_current_category(db, request.menage_id)

        history = ScoringHistory(
            menage_id=request.menage_id,
            menage_code=request.menage_code,
            ancien_score=ancien_score,
            ancienne_categorie=ancienne_categorie,
            nouveau_score=score,
            nouvelle_categorie=categorie,
            motif=self._generate_description(request, score, categorie),
            calcule_le=datetime.utcnow(),
        )
        scoring_history_repository.save(db, history)

        # Invalider le cache pour ce ménage
        cache_delete(f"scoring:{request.menage_id}")

        return self._build_response(request.menage_id, request.menage_code, score, categorie)

    # ------------------------------------------------------------------
    # Récupère le dernier score (avec cache Redis)
    # ------------------------------------------------------------------
    def get_latest_score(self, db: Session, menage_id: UUID) -> ScoringResponse:
        cache_key = f"scoring:{menage_id}"

        # Lecture depuis Redis
        cached = cache_get(cache_key)
        if cached:
            logger.debug("Cache hit pour ménage: %s", menage_id)
            return ScoringResponse(**cached)

        history = scoring_history_repository.find_first_by_menage_id_order_by_calcule_le_desc(
            db, menage_id
        )
        if not history:
            raise ValueError(f"Aucun score trouvé pour le ménage: {menage_id}")

        response = self._to_response(history)

        # Mise en cache
        cache_set(cache_key, response.model_dump())

        return response

    # ------------------------------------------------------------------
    # Historique des scores d'un ménage
    # ------------------------------------------------------------------
    def get_scoring_history(self, db: Session, menage_id: UUID) -> List[ScoringResponse]:
        histories = scoring_history_repository.find_by_menage_id_order_by_calcule_le_desc(
            db, menage_id
        )
        return [self._to_response(h) for h in histories]

    # ------------------------------------------------------------------
    # Tous les ménages par catégorie sociale
    # ------------------------------------------------------------------
    def get_menages_by_category(
        self, db: Session, categorie: CategorieSociale
    ) -> List[ScoringResponse]:
        histories = scoring_history_repository.find_distinct_latest_by_categorie(db, categorie)
        return [self._to_response(h) for h in histories]

    # ------------------------------------------------------------------
    # Éligibilité à un programme social
    # ------------------------------------------------------------------
    def is_eligible_for_program(
        self, db: Session, menage_id: UUID, required_max_score: int
    ) -> bool:
        latest = self.get_latest_score(db, menage_id)
        return latest.score <= required_max_score

    # ------------------------------------------------------------------
    # Ménages éligibles pour un programme (score <= maxScore)
    # ------------------------------------------------------------------
    def get_eligible_menages(self, db: Session, max_score: int) -> List[ScoringResponse]:
        histories = scoring_history_repository.find_latest_scores_with_max_score(db, max_score)
        return [self._to_response(h) for h in histories]

    # ------------------------------------------------------------------
    # Méthodes privées utilitaires
    # ------------------------------------------------------------------
    def _get_salary_points(self, salary: float) -> int:
        """Points par tranche salariale selon le TP"""
        if salary < 30_000:   return 10   # [0 ; 30.000[
        if salary < 100_000:  return 20   # [30.000 ; 100.000[
        if salary < 200_000:  return 30   # [100.000 ; 200.000[
        if salary < 700_000:  return 40   # [200.000 ; 700.000[
        if salary < 1_000_000: return 45  # [700.000 ; 1.000.000[
        return 55                          # [1.000.000 ; plus[

    def _determine_category(self, score: int) -> CategorieSociale:
        """Détermine la catégorie sociale selon le score (exactement selon le TP)"""
        if score < 20:       return CategorieSociale.TRES_VULNERABLE
        elif score < 45:     return CategorieSociale.VULNERABLE
        elif score <= 55:    return CategorieSociale.A_RISQUE
        elif score <= 70:    return CategorieSociale.NON_VULNERABLE
        elif score <= 85:    return CategorieSociale.RICHE
        else:                return CategorieSociale.TRES_RICHE

    def _get_current_score(self, db: Session, menage_id: UUID) -> int:
        try:
            return self.get_latest_score(db, menage_id).score
        except (ValueError, Exception):
            return 0

    def _get_current_category(self, db: Session, menage_id: UUID) -> Optional[CategorieSociale]:
        try:
            return self.get_latest_score(db, menage_id).categorie
        except (ValueError, Exception):
            return None

    def _generate_description(
        self, request: ScoringRequest, score: int, categorie: CategorieSociale
    ) -> str:
        return (
            f"Score calculé: {score} points - {categorie.label} "
            f"(TV:{5 if request.has_tv else 0}, "
            f"Radio:{5 if request.has_radio else 0}, "
            f"Moto:{5 if request.has_motorcycle else 0}, "
            f"Voiture:{10 if request.has_car else 0}, "
            f"Propriétaire:{20 if request.is_owner else 0}, "
            f"Salaire max: {request.max_salary:.0f} FCFA)"
        )

    def _build_response(
        self, menage_id: UUID, menage_code: str, score: int, categorie: CategorieSociale
    ) -> ScoringResponse:
        return ScoringResponse(
            menage_id=menage_id,
            menage_code=menage_code,
            score=score,
            categorie=categorie,
            categorie_label=categorie.label,
            calcule_le=datetime.utcnow(),
            description="Score calculé avec succès",
        )

    def _to_response(self, h: ScoringHistory) -> ScoringResponse:
        return ScoringResponse(
            menage_id=h.menage_id,
            menage_code=h.menage_code or "",
            score=h.nouveau_score,
            categorie=h.nouvelle_categorie,
            categorie_label=h.nouvelle_categorie.label if h.nouvelle_categorie else "",
            calcule_le=h.calcule_le,
            description=self._build_description(h),
        )

    def _build_description(self, h: ScoringHistory) -> str:
        if h.motif == "CREATION":
            return "Score initial à la création du ménage"
        ancien_label = h.ancienne_categorie.label if h.ancienne_categorie else "N/A"
        nouvelle_label = h.nouvelle_categorie.label if h.nouvelle_categorie else "N/A"
        return (
            f"Score mis à jour: {h.ancien_score} → {h.nouveau_score} "
            f"({ancien_label} → {nouvelle_label})"
        )


# Instance singleton
scoring_service = ScoringService()
