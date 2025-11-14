# Assuming your fields are snake_case in the JSON payload
from pydantic import BaseModel, Field
from typing import Optional

# You need to define this DTO based on the JSON templates we made before
class TemplateCreationRequest(BaseModel):
    # CRITICAL FIX: Maps incoming camelCase keys to internal snake_case attributes
    # The 'alias' is what the client sends (templateKey)
    # The internal name is what the Java service expects (template_code)
    template_code: str = Field(..., alias="templateKey") 
    subject_template: str = Field(..., alias="subjectTemplate")
    body_template: str = Field(..., alias="bodyTemplate")
    type: str
    version: int

    class Config:
        populate_by_name = True
        # NEW: Ensure Pydantic uses the ALIASES/OUTPUT NAMES when serializing (model_dump)
        by_alias = True