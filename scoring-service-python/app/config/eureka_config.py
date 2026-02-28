import logging
import os
import socket

import py_eureka_client.eureka_client as eureka_client

logger = logging.getLogger(__name__)

EUREKA_URL = os.getenv(
    "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE",
    "http://discovery-service:8761/eureka/"
)
APP_NAME = os.getenv("SPRING_APPLICATION_NAME", "scoring-service")
PORT = int(os.getenv("SERVER_PORT", "8083"))
HOSTNAME = os.getenv("HOSTNAME", socket.gethostname())


async def register_with_eureka():
    """
    Enregistre le service Python dans Eureka.
    Équivalent de @EnableDiscoveryClient + spring.application.name en Spring Boot.
    """
    try:
        await eureka_client.init_async(
            eureka_server=EUREKA_URL,
            app_name=APP_NAME,
            instance_port=PORT,
            instance_host=HOSTNAME,
            health_check_url=f"http://{HOSTNAME}:{PORT}/actuator/health",
            status_page_url=f"http://{HOSTNAME}:{PORT}/swagger-ui.html",
            home_page_url=f"http://{HOSTNAME}:{PORT}/",
        )
        logger.info("Service '%s' enregistré dans Eureka (%s)", APP_NAME, EUREKA_URL)
    except Exception as e:
        logger.warning("Impossible de s'enregistrer dans Eureka: %s", str(e))


async def deregister_from_eureka():
    """Désenregistre le service au moment de l'arrêt."""
    try:
        await eureka_client.stop_async()
        logger.info("Service '%s' désenregistré d'Eureka", APP_NAME)
    except Exception as e:
        logger.warning("Erreur lors du désenregistrement Eureka: %s", str(e))
