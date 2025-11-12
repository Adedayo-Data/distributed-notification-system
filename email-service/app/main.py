from fastapi import FastAPI
import asyncio
# CORS
from fastapi.middleware.cors import CORSMiddleware
from .status_store import get_status
from .queue import consume , close_connection

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

consumer_task = None  # declare globally

@app.on_event("startup")
async def startup_event():
    global consumer_task
    consumer_task = asyncio.create_task(consume())
    print("Consuming email.queue...")

@app.on_event("shutdown")
async def shutdown_event():
    global consumer_task
    if consumer_task:
        consumer_task.cancel()
        try:
            await consumer_task
        except asyncio.CancelledError:
            pass
    await close_connection()
    print("Consumer stopped cleanly.")

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/status/{notification_id}")
def status(notification_id: str):
    return {"notification_id": notification_id, "status": get_status(notification_id)}
