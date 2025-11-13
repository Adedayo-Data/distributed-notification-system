from fastapi import APIRouter, HTTPException
import httpx
import logging
from app.config import settings
from app.models.user import UserRegistrationRequest # <-- New model
from app.models.responses import StandardResponse

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/api/v1/users", # <-- Sets the base path for all routes in this file
    tags=["User Management"]
)

@router.post(
    "/", # The full path will be /api/v1/users/
    response_model=StandardResponse,
    summary="User Registration"
)
async def register_user(request: UserRegistrationRequest):
    """Proxies user registration request to the User Service."""
    
    # The full URL is built from the env variable (http://user-service:3001)
    user_service_url = f"{settings.USER_SERVICE_URL}/api/v1/users/" 
    
    try:
        # We use httpx to forward the request
        async with httpx.AsyncClient() as client:
            response = await client.post(
                user_service_url,
                json=request.dict()
            )
        
        response.raise_for_status() # Raise exception for 4xx/5xx responses

        user_service_data = response.json()
        
        # We re-wrap the User Service response to match our StandardResponse
        return StandardResponse(
            success=True,
            data=user_service_data.get("data"),
            message=user_service_data.get("message", "User created successfully"),
            error=None,
            meta=None
        )

    except httpx.HTTPStatusError as exc:
        error_detail = exc.response.json().get("message", "Registration failed: User service error")
        raise HTTPException(status_code=exc.response.status_code, detail=error_detail)
    except Exception as e:
        logger.error(f"Error during registration proxy: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")