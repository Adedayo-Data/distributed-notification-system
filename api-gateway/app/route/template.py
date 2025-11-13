from fastapi import APIRouter, HTTPException, Path, Header
import httpx
import logging
from app.config import settings
from app.models.responses import StandardResponse
from app.models.templatecreationrequest import TemplateCreationRequest
from app.route.auth_validation import verify_token
# Assume we have a function to validate the JWT for protected routes
from app.utils import auth_handler

logger = logging.getLogger(__name__)

router = APIRouter(
    tags=["Template Retrieval"]
)

# In api-gateway/app/route/template.py, add this function below the imports

@router.post(
    "/api/v1/templates",
    response_model=StandardResponse,
    summary="Create New Template"
)
async def create_template(request: TemplateCreationRequest, token: str = Header(..., alias="Authorization")):
    """
    Handles template creation request, validates admin token, and proxies
    to the Template Service (port 8085).
    """
    
    # 1. Authorize the token (the required protection)
    # The verify_token function is imported from auth_validation
    await verify_token(token) 
    
    # 2. Define the URL to the internal Template Service
    template_service_url = f"{settings.TEMPLATE_SERVICE_URL}/api/v1/templates"
    
    try:
        # 3. Proxy the request using httpx
        async with httpx.AsyncClient() as client:
            response = await client.post(
                template_service_url,
                json=request.dict() # Convert Pydantic model to dict for JSON body
            )
        
        response.raise_for_status() # Raise error for 4xx/5xx responses
        
        # 4. Return the response (which should already be a StandardResponse from Template Service)
        return response.json()

    except httpx.HTTPStatusError as exc:
        logger.error(f"HTTP error calling Template Service: {exc}")
        error_detail = exc.response.json().get("message", "Template creation failed.")
        raise HTTPException(status_code=exc.response.status_code, detail=error_detail)
    except Exception as e:
        logger.error(f"Error during template creation proxy: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

# The existing GET /api/v1/templates function goes here...
# Endpoint the Push/Email services will call to get the *rendered* template
@router.post(
    "/api/v1/templates/render",
    response_model=StandardResponse,
    summary="Proxy: Render Template"
)
async def render_template(request: dict):
    """Proxies template rendering request to the Template Service."""
    
    template_service_url = f"{settings.TEMPLATE_SERVICE_URL}/api/v1/templates/render"
    
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                template_service_url,
                json=request # Pass the entire request body as-is
            )
        
        response.raise_for_status()
        
        # Template Service should return the standardized response, so we just pass it back
        return response.json()

    except httpx.HTTPStatusError as exc:
        logger.error(f"HTTP error calling Template Service: {exc}")
        raise HTTPException(status_code=exc.response.status_code, detail="Template rendering failed")
    except Exception as e:
        logger.error(f"Error during template proxy: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

# Endpoint for admin to get a list of templates
@router.get(
    "/api/v1/templates",
    response_model=StandardResponse,
    summary="Proxy: Get Template List"
)
async def get_templates(token: str = Header(..., alias="Authorization")):
    """Retrieves a list of templates. Requires authentication."""
    
    # 1. Authorize the admin token
    auth_handler(token) 
    
    template_service_url = f"{settings.TEMPLATE_SERVICE_URL}/api/v1/templates"
    
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(template_service_url)
        
        response.raise_for_status()
        return response.json()
        
    except Exception as e:
        logger.error(f"Error during template list proxy: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")