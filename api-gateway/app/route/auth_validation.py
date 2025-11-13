# from fastapi import APIRouter, HTTPException, Header
# import logging

# from app.utils import auth_handler
# logging.basicConfig(level=logging.INFO)
# logger = logging.getLogger(__name__)

# from app.models.responses import StandardResponse


# router = APIRouter(
#     tags=["Authentication"]
# )

# @router.post(
#     "/api/v1/auth/verify",
#     response_model=StandardResponse
# )
# async def verify_token(token: str = Header(..., alias="Authorization")):
#     """
#     Verify JWT token validity
#     Note: Tokens are issued by User Service, Gateway only validates them
#     """
#     try:
#         if not token.startswith("Bearer "):
#             raise HTTPException(status_code=401, detail="Invalid token format")
        
#         token = token.replace("Bearer ", "")
#         payload = auth_handler.verify_token(token)
        
#         return StandardResponse(
#             success=True,
#             data={"valid": True, "user_id": payload.get("user_id")},
#             message="Token is valid",
#             error=None,
#             meta=None
#         )
#     except HTTPException:
#         raise
#     except Exception as e:
#         raise HTTPException(status_code=401, detail="Invalid or expired token")


from fastapi import APIRouter, HTTPException, Header, Request
import logging
import httpx
from pydantic import BaseModel
from app.config import settings
from app.utils import auth_handler
from app.models.responses import StandardResponse  # We will use this to build our response

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class LoginRequest(BaseModel):
    email: str
    password: str

router = APIRouter(
    tags=["Authentication"]
)

# --- THIS IS THE NEW/FIXED ENDPOINT ---

@router.post(
    "/api/v1/auth/login",
    response_model=StandardResponse,  # It MUST return this model
    summary="User Login"
)
async def login(request: LoginRequest):
    """
    Handles user login by proxying credentials to the User Service
    and returning the JWT token.
    """
    
    # This URL is correct
    user_service_url = f"{settings.USER_SERVICE_URL}/api/v1/auth/login"
    
    try:
        # Make a client call to the User Service
        async with httpx.AsyncClient() as client:
            response = await client.post(
                user_service_url,
                json=request.dict()
            )
        
        response.raise_for_status() 
        
        user_service_data = response.json()
        
        # Check if the User Service login was successful
        if not user_service_data.get("success"):
            raise HTTPException(status_code=401, detail=user_service_data.get("message", "Invalid credentials"))

        # --- THIS IS THE FIX ---
        # Don't just 'return user_service_data'.
        # We MUST re-wrap it in our StandardResponse model
        # to add the 'meta: null' field.
        return StandardResponse(
            success=True,
            data=user_service_data.get("data"),
            message=user_service_data.get("message", "Login successful"),
            error=None,
            meta=None  # This is the field that was missing
        )
        # --- END OF FIX ---

    except httpx.HTTPStatusError as exc:
        logger.error(f"HTTP error calling User Service: {exc}")
        # Pass the error from the user service, if possible
        error_detail = exc.response.json().get("message", "Login failed: User service error")
        raise HTTPException(status_code=exc.response.status_code, detail=error_detail)
    except Exception as e:
        logger.error(f"Error during login proxy: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

# --- END OF NEW/FIXED ENDPOINT ---


@router.post(
    "/api/v1/auth/verify",
    response_model=StandardResponse
)
async def verify_token(token: str = Header(..., alias="Authorization")):
    """
    Verify JWT token validity
    Note: Tokens are issued by User Service, Gateway only validates them
    """
    try:
        if not token.startswith("Bearer "):
            raise HTTPException(status_code=401, detail="Invalid token format")
        
        token = token.replace("Bearer ", "")
        payload = auth_handler.verify_token(token)
        
        return StandardResponse(
            success=True,
            data={"valid": True, "user_id": payload.get("user_id")},
            message="Token is valid",
            error=None,
            meta=None
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=401, detail="Invalid or expired token")