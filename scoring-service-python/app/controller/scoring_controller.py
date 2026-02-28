import logging
from typing import List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.config.database import get_db
from app.enums.categorie_sociale import CategorieSociale
from app.schemas.scoring_schemas import ScoringRequest, ScoringResponse
from app.service.scoring_service import scoring_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/scoring", tags=["Scoring"])


@router.post("/calculate", response_model=ScoringResponse, status_code=201)
def calculate_score(request: ScoringRequest, db: Session = Depends(get_db)):
    """Calcule et enregistre le score d'un ménage"""
    return scoring_service.calculate_score(db, request)


@router.get("/menages/{menage_id}/latest", response_model=ScoringResponse)
def get_latest_score(menage_id: UUID, db: Session = Depends(get_db)):
    """Récupère le dernier score d'un ménage"""
    try:
        return scoring_service.get_latest_score(db, menage_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.get("/menages/{menage_id}/history", response_model=List[ScoringResponse])
def get_scoring_history(menage_id: UUID, db: Session = Depends(get_db)):
    """Récupère l'historique des scores d'un ménage"""
    return scoring_service.get_scoring_history(db, menage_id)


@router.get("/menages/{menage_id}/eligible", response_model=bool)
def is_eligible_for_program(
    menage_id: UUID,
    max_score: int = Query(..., description="Score maximum requis pour le programme"),
    db: Session = Depends(get_db),
):
    """Vérifie si un ménage est éligible à un programme social"""
    try:
        return scoring_service.is_eligible_for_program(db, menage_id, max_score)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.get("/categories/{categorie}/menages", response_model=List[ScoringResponse])
def get_menages_by_category(categorie: CategorieSociale, db: Session = Depends(get_db)):
    """Récupère tous les ménages d'une catégorie sociale"""
    return scoring_service.get_menages_by_category(db, categorie)


@router.get("/eligible", response_model=List[ScoringResponse])
def get_eligible_menages(
    max_score: int = Query(..., description="Score maximum pour l'éligibilité"),
    db: Session = Depends(get_db),
):
    """Récupère tous les ménages éligibles pour un programme (score <= maxScore)"""
    return scoring_service.get_eligible_menages(db, max_score)
