# Assuming your fields are snake_case in the JSON payload
from pydantic import BaseModel, Field
from typing import Optional

# You need to define this DTO based on the JSON templates we made before
class TemplateCreationRequest(BaseModel):
    template_key: str = Field(..., alias="templateKey") # Maps JSON snake to internal camel
    subject_template: str = Field(..., alias="subjectTemplate")
    body_template: str = Field(..., alias="bodyTemplate")
    type: str
    version: int
    # Ensure any optional fields (like rendered_image_url) are added here if the underlying service uses them.