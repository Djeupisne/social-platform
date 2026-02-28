from typing import List, Optional
from uuid import UUID

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.enums.categorie_sociale import CategorieSociale
from app.models.scoring_history import ScoringHistory


class ScoringHistoryRepository:

    def save(self, db: Session, history: ScoringHistory) -> ScoringHistory:
        db.add(history)
        db.commit()
        db.refresh(history)
        return history

    def find_by_menage_id_order_by_calcule_le_desc(
        self, db: Session, menage_id: UUID
    ) -> List[ScoringHistory]:
        return (
            db.query(ScoringHistory)
            .filter(ScoringHistory.menage_id == menage_id)
            .order_by(ScoringHistory.calcule_le.desc())
            .all()
        )

    def find_first_by_menage_id_order_by_calcule_le_desc(
        self, db: Session, menage_id: UUID
    ) -> Optional[ScoringHistory]:
        return (
            db.query(ScoringHistory)
            .filter(ScoringHistory.menage_id == menage_id)
            .order_by(ScoringHistory.calcule_le.desc())
            .first()
        )

    def count_by_nouvelle_categorie(
        self, db: Session, categorie: CategorieSociale
    ) -> int:
        return (
            db.query(func.count(ScoringHistory.id))
            .filter(
                ScoringHistory.nouvelle_categorie == categorie,
                ScoringHistory.motif == "CREATION",
            )
            .scalar()
        )

    def find_distinct_latest_by_categorie(
        self, db: Session, categorie: CategorieSociale
    ) -> List[ScoringHistory]:
        """
        Récupère le dernier score distinct de chaque ménage pour une catégorie donnée.
        Équivalent JPQL: SELECT sh FROM ScoringHistory sh WHERE sh.nouvelleCategorie = :categorie
                         AND sh.calculeLe = (SELECT MAX(...) WHERE menageId = sh.menageId)
        """
        subquery = (
            db.query(func.max(ScoringHistory.calcule_le))
            .filter(ScoringHistory.menage_id == ScoringHistory.menage_id)
            .correlate(ScoringHistory)
            .scalar_subquery()
        )
        return (
            db.query(ScoringHistory)
            .filter(
                ScoringHistory.nouvelle_categorie == categorie,
                ScoringHistory.calcule_le == subquery,
            )
            .all()
        )

    def find_latest_scores_with_max_score(
        self, db: Session, max_score: int
    ) -> List[ScoringHistory]:
        """
        Récupère le dernier score de tous les ménages dont le score <= max_score.
        """
        # Sous-requête : dernier calcule_le par menage_id
        latest_subq = (
            db.query(
                ScoringHistory.menage_id,
                func.max(ScoringHistory.calcule_le).label("max_date"),
            )
            .group_by(ScoringHistory.menage_id)
            .subquery()
        )

        return (
            db.query(ScoringHistory)
            .join(
                latest_subq,
                (ScoringHistory.menage_id == latest_subq.c.menage_id)
                & (ScoringHistory.calcule_le == latest_subq.c.max_date),
            )
            .filter(ScoringHistory.nouveau_score <= max_score)
            .all()
        )


# Instance singleton (comme le @Repository Spring)
scoring_history_repository = ScoringHistoryRepository()
