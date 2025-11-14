from enum import Enum
from typing import Any, Dict, Optional

from pydantic import BaseModel, EmailStr, Field


class JobRequest(BaseModel):
    notification_id: str = Field(..., alias="notification_id")
    user_id: str = Field(..., alias="user_id")
    template_code: str = Field(..., alias="template_code")
    variables: Dict[str, Any] = Field(default_factory=dict)

    class Config:
        allow_population_by_field_name = True


class UserPreferences(BaseModel):
    email_notifications: Optional[bool] = Field(default=True, alias="email_notifications")
    push_notifications: Optional[bool] = Field(default=True, alias="push_notifications")

    class Config:
        allow_population_by_field_name = True


class UserResponse(BaseModel):
    id: str
    email: Optional[EmailStr] = None
    name: Optional[str] = None
    preferences: Optional[UserPreferences] = None


class ApiResponse(BaseModel):
    success: bool
    message: Optional[str] = None
    data: Optional[Any] = None
    error: Optional[str] = None
    meta: Optional[Any] = None


class TemplateRenderResult(BaseModel):
    rendered_subject: Optional[str] = None
    rendered_body: Optional[str] = None


class TemplateRenderRequest(BaseModel):
    template_code: str = Field(..., alias="template_code")
    notification_type: str = Field(..., alias="notification_type")
    variables: Dict[str, Any] = Field(default_factory=dict)

    class Config:
        allow_population_by_field_name = True


class NotificationStatus(str, Enum):
    delivered = "delivered"
    failed = "failed"
    pending = "pending"
    skipped = "skipped"


class StatusUpdateRequest(BaseModel):
    notification_id: str
    status: NotificationStatus
    error: Optional[str] = None
