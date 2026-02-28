from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel, Field

from app.enums.categorie_sociale import CategorieSociale


class ScoringRequest(BaseModel):
    menage_id: UUID
    menage_code: str
    has_tv: bool = False
    has_radio: bool = False
    has_motorcycle: bool = False
    has_car: bool = False
    is_owner: bool = False
    max_salary: float = Field(..., ge=0)

    class Config:
        populate_by_name = True


class ScoringResponse(BaseModel):
    menage_id: UUID
    menage_code: str
    score: int
    categorie: CategorieSociale
    categorie_label: str
    calcule_le: datetime
    description: str

    class Config:
        from_attributes = True
