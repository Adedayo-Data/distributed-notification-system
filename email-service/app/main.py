import os
import asyncio
import logging
from contextlib import suppress
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import aio_pika
import dotenv

from .status_store import get_status
from .queue import consume, close_connection

dotenv.load_dotenv()
rabbitmq_url = os.getenv("RABBITMQ_URL")

logging.basicConfig(level=logging.INFO)

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Replace with production origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
async def startup_event():
    logging.info("Starting up: connecting to RabbitMQ...")

    # ADD THIS RETRY LOGIC
    retries = 20
    delay = 3
    for i in range(retries):
        try:
            app.state.rmq_connection = await aio_pika.connect_robust(rabbitmq_url)
            break
        except Exception as e:
            logging.warning(f"RabbitMQ not ready ({type(e).__name__}: {e}), retrying in {delay} seconds... ({i+1}/{retries})")
            await asyncio.sleep(delay)
    else:
        raise RuntimeError("Could not connect to RabbitMQ after retries.")
    
    app.state.consumer_stop_event = asyncio.Event()
    app.state.consumer_task = asyncio.create_task(
        consume(app.state.rmq_connection, app.state.consumer_stop_event)
    )
    logging.info("RabbitMQ connected and consumer started.")

@app.on_event("shutdown")
async def shutdown_event():
    logging.info("Shutting down...")

    if hasattr(app.state, "consumer_stop_event"):
        app.state.consumer_stop_event.set()

    if hasattr(app.state, "consumer_task") and app.state.consumer_task:
        app.state.consumer_task.cancel()
        with suppress(asyncio.CancelledError):
            await app.state.consumer_task

    if hasattr(app.state, "rmq_connection") and app.state.rmq_connection:
        await close_connection(app.state.rmq_connection)

    logging.info("Shutdown complete.")

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.get("/status/{notification_id}")
async def status(notification_id: str):
    return {"notification_id": notification_id, "status": get_status(notification_id)}
