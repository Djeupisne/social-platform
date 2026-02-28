import os

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "rabbitmq")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "guest")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")

RABBITMQ_URL = (
    f"amqp://{RABBITMQ_USER}:{RABBITMQ_PASSWORD}"
    f"@{RABBITMQ_HOST}:{RABBITMQ_PORT}/"
)

# Exchange & Queues — mêmes noms que le service Java
MENAGE_EXCHANGE = "menage.exchange"
MENAGE_CREATED_QUEUE = "menage.created.queue"
MENAGE_UPDATED_QUEUE = "menage.updated.queue"
MENAGE_CREATED_ROUTING_KEY = "menage.created"
MENAGE_UPDATED_ROUTING_KEY = "menage.updated"
