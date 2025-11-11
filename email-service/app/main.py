from fastapi import FastAPI
import asyncio
from .status_store import get_status
from .queue import consume

app = FastAPI()

@app.on_event("startup")
async def startup_event():
    loop = asyncio.get_event_loop()
    loop.create_task(consume())

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/status/{notification_id}")
def status(notification_id: str):
    return {"notification_id": notification_id, "status": get_status(notification_id)}
