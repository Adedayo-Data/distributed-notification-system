import logging
import os
from typing import Optional

import httpx

from .models import NotificationStatus, StatusUpdateRequest

logger = logging.getLogger(__name__)

STATUS_UPDATE_URL = os.getenv(
    "STATUS_UPDATE_URL", "http://api-gateway:8000/api/v1/email/status/"
)


async def report_status(
    notification_id: str,
    status: NotificationStatus,
    error: Optional[str] = None,
    timeout: float = 10.0,
) -> None:
    payload = StatusUpdateRequest(
        notification_id=notification_id,
        status=status,
        error=error,
    )

    try:
        async with httpx.AsyncClient(timeout=timeout) as client:
            await client.post(
                STATUS_UPDATE_URL,
                json=payload.dict(exclude_none=True),
            )
    except httpx.HTTPStatusError as exc:
        logger.warning(
            "Status update rejected for %s with status %s: %s",
            notification_id,
            exc.response.status_code,
            exc.response.text,
        )
    except Exception as exc:  # noqa: BLE001
        logger.error(
            "Failed to report status for %s: %s",
            notification_id,
            exc,
        )
