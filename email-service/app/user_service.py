import os
import logging
from typing import Optional

import httpx

from .models import ApiResponse, UserResponse

logger = logging.getLogger(__name__)

USER_SERVICE_URL = os.getenv("USER_SERVICE_URL", "http://user-service:3001").rstrip("/")
USER_ENDPOINT = f"{USER_SERVICE_URL}/api/v1/users"


async def fetch_user(user_id: str, timeout: float = 10.0) -> Optional[UserResponse]:
    url = f"{USER_ENDPOINT}/{user_id}"
    logger.info("Fetching user details from %s", url)
    async with httpx.AsyncClient(timeout=timeout) as client:
        response = await client.get(url)
    response.raise_for_status()

    payload = ApiResponse(**response.json())

    if not payload.success or payload.data is None:
        raise ValueError(payload.error or "User lookup failed")

    return UserResponse(**payload.data)
