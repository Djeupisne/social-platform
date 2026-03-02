import os

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", os.getenv("RABBIT_HOST", "rabbitmq"))
RABBIT_PORT_STR = os.getenv("RABBITMQ_PORT", os.getenv("RABBIT_PORT", "5672"))
try:
    RABBITMQ_PORT = int(RABBIT_PORT_STR)
except ValueError:
    RABBITMQ_PORT = 5672

RABBITMQ_USER = os.getenv("RABBITMQ_USER", os.getenv("RABBIT_USER", "social_rabbit"))
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", os.getenv("RABBIT_PASSWORD", "Rabbit@Togo2024!"))

RABBITMQ_URL = (
    f"amqp://{RABBITMQ_USER}:{RABBITMQ_PASSWORD}"
    f"@{RABBITMQ_HOST}:{RABBITMQ_PORT}/"
)

# Exchange & Queues
MENAGE_EXCHANGE = "menage.exchange"
MENAGE_CREATED_QUEUE = "menage.created.queue"
MENAGE_UPDATED_QUEUE = "menage.updated.queue"
MENAGE_CREATED_ROUTING_KEY = "menage.created"
MENAGE_UPDATED_ROUTING_KEY = "menage.updated"