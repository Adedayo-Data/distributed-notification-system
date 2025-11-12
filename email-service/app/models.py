from pydantic import BaseModel, EmailStr
from typing import Dict

class NotificationMessage(BaseModel):
    notification_id: str
    user_id: str
    email: EmailStr
    template_id: str
    variables: Dict[str, str]
