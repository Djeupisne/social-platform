import asyncio
import json
import logging
from datetime import datetime
from uuid import UUID

import aio_pika

from app.config.database import SessionLocal
from app.config.rabbitmq_config import (
    RABBITMQ_URL,
    MENAGE_EXCHANGE,
    MENAGE_CREATED_QUEUE,
    MENAGE_UPDATED_QUEUE,
    MENAGE_CREATED_ROUTING_KEY,
    MENAGE_UPDATED_ROUTING_KEY,
)
from app.enums.categorie_sociale import CategorieSociale
from app.models.scoring_history import ScoringHistory
from app.repository.scoring_history_repository import scoring_history_repository

logger = logging.getLogger(__name__)


async def handle_menage_created(message: aio_pika.IncomingMessage):
    """
    Écoute la queue menage.created.queue
    Équivalent de @RabbitListener(queues = "menage.created.queue")
    """
    async with message.process():
        try:
            event = json.loads(message.body.decode())
            logger.info("Événement ménage créé reçu: %s", event)

            menage_id = UUID(event["menageId"])
            code = event.get("code", "")
            score = int(event["score"])
            categorie = CategorieSociale(event["categorie"])

            db = SessionLocal()
            try:
                history = ScoringHistory(
                    menage_id=menage_id,
                    menage_code=code,
                    ancien_score=0,
                    nouveau_score=score,
                    ancienne_categorie=None,
                    nouvelle_categorie=categorie,
                    motif="CREATION",
                    calcule_le=datetime.utcnow(),
                )
                scoring_history_repository.save(db, history)
                logger.info("Historique créé pour ménage %s", menage_id)
            finally:
                db.close()

        except Exception as e:
            logger.error("Erreur traitement événement ménage créé: %s", str(e))


async def handle_menage_updated(message: aio_pika.IncomingMessage):
    """
    Écoute la queue menage.updated.queue
    Équivalent de @RabbitListener(queues = "menage.updated.queue")
    """
    async with message.process():
        try:
            event = json.loads(message.body.decode())
            logger.info("Événement ménage mis à jour reçu: %s", event)

            menage_id = UUID(event["menageId"])
            code = event.get("code", "")
            ancien_score = int(event["ancienScore"])
            nouveau_score = int(event["nouveauScore"])

            db = SessionLocal()
            try:
                history = ScoringHistory(
                    menage_id=menage_id,
                    menage_code=code,
                    ancien_score=ancien_score,
                    nouveau_score=nouveau_score,
                    ancienne_categorie=CategorieSociale.from_score(ancien_score),
                    nouvelle_categorie=CategorieSociale.from_score(nouveau_score),
                    motif="MISE_A_JOUR",
                    calcule_le=datetime.utcnow(),
                )
                scoring_history_repository.save(db, history)
                logger.info("Historique mis à jour pour ménage %s", menage_id)
            finally:
                db.close()

        except Exception as e:
            logger.error("Erreur traitement événement ménage mis à jour: %s", str(e))


async def start_listeners():
    """Démarre les consommateurs RabbitMQ (appelé au démarrage de l'app)"""
    max_retries = 5
    for attempt in range(max_retries):
        try:
            connection = await aio_pika.connect_robust(RABBITMQ_URL)
            channel = await connection.channel()

            # Déclarer l'exchange (topic, comme dans le Java)
            exchange = await channel.declare_exchange(
                MENAGE_EXCHANGE, aio_pika.ExchangeType.TOPIC, durable=True
            )

            # Queue menage.created
            created_queue = await channel.declare_queue(MENAGE_CREATED_QUEUE, durable=True)
            await created_queue.bind(exchange, MENAGE_CREATED_ROUTING_KEY)
            await created_queue.consume(handle_menage_created)

            # Queue menage.updated
            updated_queue = await channel.declare_queue(MENAGE_UPDATED_QUEUE, durable=True)
            await updated_queue.bind(exchange, MENAGE_UPDATED_ROUTING_KEY)
            await updated_queue.consume(handle_menage_updated)

            logger.info("Listeners RabbitMQ démarrés avec succès")
            return connection

        except Exception as e:
            logger.warning(
                "Tentative %d/%d de connexion à RabbitMQ échouée: %s",
                attempt + 1, max_retries, str(e)
            )
            if attempt < max_retries - 1:
                await asyncio.sleep(5)
            else:
                logger.error("Impossible de se connecter à RabbitMQ après %d tentatives", max_retries)
                return None
