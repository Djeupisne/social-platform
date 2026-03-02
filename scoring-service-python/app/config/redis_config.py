import json
import os
from typing import Optional, Any
import redis

REDIS_HOST = os.getenv("REDIS_SERVICE_HOST", "redis")
REDIS_SERVICE_PORT = os.getenv("REDIS_SERVICE_PORT", "6379")
# Extraire juste le numéro de port au cas où
try:
    REDIS_PORT = int(REDIS_SERVICE_PORT)
except ValueError:
    REDIS_PORT = 6379

CACHE_TTL = 60 * 30  # 30 minutes

_redis_client: Optional[redis.Redis] = None

def get_redis() -> redis.Redis:
    global _redis_client
    if _redis_client is None:
        _redis_client = redis.Redis(
            host=REDIS_HOST,
            port=REDIS_PORT,
            decode_responses=True
        )
    return _redis_client

def cache_get(key: str) -> Optional[Any]:
    try:
        client = get_redis()
        value = client.get(key)
        if value:
            return json.loads(value)
    except Exception:
        pass
    return None

def cache_set(key: str, value: Any, ttl: int = CACHE_TTL) -> None:
    try:
        client = get_redis()
        client.setex(key, ttl, json.dumps(value, default=str))
    except Exception:
        pass

def cache_delete(key: str) -> None:
    try:
        client = get_redis()
        client.delete(key)
    except Exception:
        pass
