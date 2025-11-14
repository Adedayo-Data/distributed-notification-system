import logging
import os
from typing import Dict

import httpx
from dotenv import load_dotenv

from .models import ApiResponse, TemplateRenderRequest, TemplateRenderResult

load_dotenv()
logger = logging.getLogger(__name__)

TEMPLATE_SERVICE_URL = os.getenv("TEMPLATE_SERVICE_URL", "http://template-service:8085").rstrip("/")


async def render_template(
    template_code: str,
    variables: Dict[str, str],
    notification_type: str,
) -> TemplateRenderResult:
    """Call the template service to render a template for email notifications."""
    payload = TemplateRenderRequest(
        template_code=template_code,
        notification_type=notification_type,
        variables=variables,
    )

    url = f"{TEMPLATE_SERVICE_URL}/api/v1/templates/render"
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(url, json=payload.dict(by_alias=True))
        response.raise_for_status()
    except httpx.HTTPError as exc:
        logger.error("Failed to reach template service: %s", exc)
        raise

    api_response = ApiResponse(**response.json())
    if not api_response.success or api_response.data is None:
        error_message = api_response.error or "Template rendering failed"
        logger.error("Template %s rendering failed: %s", template_code, error_message)
        raise RuntimeError(error_message)

    return TemplateRenderResult(**api_response.data)
