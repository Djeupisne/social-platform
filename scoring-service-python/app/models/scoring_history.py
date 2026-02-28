import uuid
from datetime import datetime

from sqlalchemy import Column, String, Integer, Enum as SAEnum, DateTime
from sqlalchemy.dialects.postgresql import UUID

from app.config.database import Base
from app.enums.categorie_sociale import CategorieSociale


class ScoringHistory(Base):
    __tablename__ = "scoring_history"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    menage_id = Column(UUID(as_uuid=True), nullable=False, index=True)
    menage_code = Column(String, nullable=True)
    ancien_score = Column(Integer, default=0)
    nouveau_score = Column(Integer, nullable=False)
    ancienne_categorie = Column(SAEnum(CategorieSociale), nullable=True)
    nouvelle_categorie = Column(SAEnum(CategorieSociale), nullable=True)
    motif = Column(String, nullable=True)  # CREATION, MISE_A_JOUR, RECALCUL, CALCUL_MENSUEL
    calcule_le = Column(DateTime, default=datetime.utcnow)
