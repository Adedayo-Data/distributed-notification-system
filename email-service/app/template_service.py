import httpx

import logging
logger = logging.getLogger(__name__)

async def fetch_template(template_id: str, variables: dict):
    # Replace with real template service API call
    try:
        response = await httpx.get(f"http://template-service/templates/{template_id}")
        response.raise_for_status()
        template_str = response.json().get("template", "")
        template_str = template_str.format(**variables)
        return {
            "subject": "Notification",
            "content": template_str
        }
    except httpx.HTTPError as e:
        logger.error(f"Error fetching template {template_id}: {e}")
        raise Exception(f"Failed to fetch template: {e}")
