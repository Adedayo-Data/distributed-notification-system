import httpx

async def fetch_template(template_id: str, variables: dict):
    # Replace with real template service API call
    template_str = "{name} - default email"
    
    template_str = template_str.format(**variables)
    return {
        "subject": "Notification",
        "content": template_str
    }
