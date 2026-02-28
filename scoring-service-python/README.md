# Scoring Service — Python (FastAPI)

Port-à-port du microservice Java Spring Boot en **Python / FastAPI**.

## Correspondance Java → Python

| Java (Spring Boot)               | Python (FastAPI)                     |
|----------------------------------|--------------------------------------|
| `@SpringBootApplication`         | `FastAPI()` + `lifespan`             |
| `@RestController` / `@GetMapping`| `APIRouter` + `@router.get/post`     |
| `@Service`                       | Classe Python classique (singleton)  |
| `@Repository` / JPA              | SQLAlchemy ORM + Repository pattern  |
| `@Entity` / Hibernate            | `Base` + `Column` SQLAlchemy         |
| `@Cacheable` (Redis)             | `cache_get/cache_set` (redis-py)     |
| `@RabbitListener`                | `aio_pika` consumer asynchrone       |
| `Lombok @Builder / @Data`        | Pydantic `BaseModel`                 |
| `application.yml`                | Variables d'environnement / `.env`   |
| `ddl-auto: update`               | `Base.metadata.create_all()`         |
| `/actuator/health`               | `GET /actuator/health`               |
| Swagger (SpringDoc)              | `GET /swagger-ui.html` (natif)       |

## Structure du projet

```
app/
├── main.py                          # Point d'entrée (équiv. ScoringServiceApplication)
├── config/
│   ├── database.py                  # SQLAlchemy engine & session
│   ├── redis_config.py              # Cache Redis (TTL 30 min)
│   └── rabbitmq_config.py           # Constantes RabbitMQ
├── enums/
│   └── categorie_sociale.py         # Enum CategorieSociale + from_score()
├── models/
│   └── scoring_history.py           # Table scoring_history (ORM)
├── schemas/
│   └── scoring_schemas.py           # ScoringRequest / ScoringResponse (Pydantic)
├── repository/
│   └── scoring_history_repository.py # Requêtes DB (équiv. JpaRepository)
├── service/
│   └── scoring_service.py           # Logique métier principale
├── controller/
│   └── scoring_controller.py        # Endpoints REST
└── listener/
    └── menage_event_listener.py     # Consommateurs RabbitMQ
```

## Démarrage rapide

```bash
# 1. Copier les variables d'environnement
cp .env.example .env

# 2. Lancer tous les services avec Docker Compose
docker-compose up --build

# 3. Swagger disponible sur
http://localhost:8083/swagger-ui.html
```

## Endpoints REST

| Méthode | URL                                        | Description                          |
|---------|--------------------------------------------|--------------------------------------|
| POST    | `/api/v1/scoring/calculate`               | Calcule le score d'un ménage         |
| GET     | `/api/v1/scoring/menages/{id}/latest`     | Dernier score (avec cache Redis)     |
| GET     | `/api/v1/scoring/menages/{id}/history`    | Historique des scores                |
| GET     | `/api/v1/scoring/menages/{id}/eligible`   | Éligibilité à un programme           |
| GET     | `/api/v1/scoring/categories/{cat}/menages`| Ménages par catégorie sociale        |
| GET     | `/api/v1/scoring/eligible?max_score=X`    | Tous les ménages éligibles           |
| GET     | `/actuator/health`                         | Health check                         |

## Algorithme de scoring (identique au TP)

| Critère           | Points |
|-------------------|--------|
| Télévision        | 5      |
| Radio             | 5      |
| Moto              | 5      |
| Voiture           | 10     |
| Propriétaire      | 20     |
| Salaire < 30k     | 10     |
| Salaire 30k-100k  | 20     |
| Salaire 100k-200k | 30     |
| Salaire 200k-700k | 40     |
| Salaire 700k-1M   | 45     |
| Salaire > 1M      | 55     |
