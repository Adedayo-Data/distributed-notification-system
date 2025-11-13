from pydantic import BaseModel, EmailStr
from typing import Optional

# 1. Define the nested model for preferences
class UserPreference(BaseModel):
    """Notification preferences for a user."""
    email: bool
    push: bool

# 2. Define the main request model
class UserRegistrationRequest(BaseModel):
    """Model for creating a new user account."""
    name: str
    email: EmailStr
    password: str
    push_token: Optional[str] = None
    preferences: UserPreference