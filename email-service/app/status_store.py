import os
import redis
import dotenv
dotenv.load_dotenv()
REDIS_URL = os.getenv("REDIS_URL")

# Connect to Redis
redis_client = redis.Redis.from_url(REDIS_URL, decode_responses=True)

def set_status(notification_id, status):
    redis_client.set(notification_id, status)

def get_status(notification_id):
    return redis_client.get(notification_id) or "unknown"
