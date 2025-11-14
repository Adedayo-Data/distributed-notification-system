import os
from typing import Optional

import dotenv
import redis

dotenv.load_dotenv()
REDIS_URL = os.getenv("REDIS_URL")

# Connect to Redis
redis_client = redis.Redis.from_url(REDIS_URL, decode_responses=True)

STATUS_KEY_PREFIX = "status:"


def _status_key(notification_id: str) -> str:
    return f"{STATUS_KEY_PREFIX}{notification_id}"


def set_status(notification_id: str, status: str) -> None:
    redis_client.set(_status_key(notification_id), status.upper())


def mark_status(notification_id: str, status: str) -> None:
    set_status(notification_id, status)


def get_status(notification_id: str) -> str:
    value = redis_client.get(_status_key(notification_id))
    if value is None:
        # legacy fallback without prefix
        value = redis_client.get(notification_id)
    return value or "unknown"


def get_raw_status(notification_id: str) -> Optional[str]:
    return redis_client.get(_status_key(notification_id))


def is_duplicate(notification_id: str) -> bool:
    current_status = (get_raw_status(notification_id) or "").upper()
    return current_status in {"DELIVERED", "SKIPPED"}
