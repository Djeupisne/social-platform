import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from prometheus_fastapi_instrumentator import Instrumentator

from app.config.database import engine, Base
from app.config.eureka_config import register_with_eureka, deregister_from_eureka
from app.controller.scoring_controller import router as scoring_router
from app.listener.menage_event_listener import start_listeners

# Import des modèles pour créer les tables
import app.models.scoring_history  # noqa: F401

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)

_rabbitmq_connection = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Gestion du cycle de vie de l'application (équivalent @PostConstruct / @PreDestroy)"""
    global _rabbitmq_connection

    # ── Démarrage ──────────────────────────────────────────────────────
    logger.info("Démarrage du Scoring Service...")

    # Création des tables (équivalent ddl-auto: update)
    Base.metadata.create_all(bind=engine)
    logger.info("Tables créées / vérifiées.")

    # Connexion RabbitMQ et démarrage des listeners
    _rabbitmq_connection = await start_listeners()

    # Enregistrement dans Eureka (équivalent @EnableDiscoveryClient)
    await register_with_eureka()

    logger.info("Scoring Service démarré sur le port 8083")

    yield

    # ── Arrêt ──────────────────────────────────────────────────────────
    if _rabbitmq_connection:
        await _rabbitmq_connection.close()
    await deregister_from_eureka()
    logger.info("Scoring Service arrêté.")


app = FastAPI(
    title="Scoring Service",
    description="Microservice de calcul du score social des ménages",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/swagger-ui.html",  # compatible avec les habitudes Spring
    redoc_url="/api-docs",
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Routeur
app.include_router(scoring_router)

# ── Métriques Prometheus ────────────────────────────────────────────────
# Expose /actuator/prometheus — équivalent de micrometer + Spring Boot Actuator
Instrumentator().instrument(app).expose(app, endpoint="/actuator/prometheus")


@app.get("/actuator/health", tags=["Actuator"])
def health():
    """Health check — équivalent de l'actuator Spring Boot"""
    return {"status": "UP", "service": "scoring-service"}


@app.get("/actuator/info", tags=["Actuator"])
def info():
    """Info — équivalent de l'actuator Spring Boot"""
    return {
        "app": {"name": "scoring-service", "version": "1.0.0", "language": "Python / FastAPI"}
    }
