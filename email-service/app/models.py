from pydantic import BaseModel

class NotificationMessage(BaseModel):
    notification_id: str
    email: str
    template_id: str
    variables: dict
