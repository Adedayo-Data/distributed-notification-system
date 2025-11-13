import httpx

import logging
from dotenv import load_dotenv
load_dotenv()
logger = logging.getLogger(__name__)

TEMPLATE_SERVICE_URL = os.getenv("TEMPLATE_SERVICE_URL", "http://template-service:8085")

async def fetch_template(template_id: str, variables: dict):
    # Replace with real template service API call
    try:
        response = await httpx.get(f"{TEMPLATE_SERVICE_URL}/templates/{template_id}")
        response.raise_for_status()
        data = response.json()
        template_str = data.get("template")
        if not template_str:
            logger.error(f"Template {template_id} not found")
            raise Exception("Template not found")
        # template_str = response.json().get("template", "")
        template_str = template_str.format(**variables)
        return {
            "subject": "Notification",
            "content": template_str
        }
    except httpx.HTTPError as e:
        logger.error(f"Error fetching template {template_id}: {e}")
        raise Exception(f"Failed to fetch template: {e}")
