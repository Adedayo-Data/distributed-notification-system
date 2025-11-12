import asyncio
import logging
import aio_pika
from contextlib import suppress
from .email_sender import send_email_with_retries
from .models import NotificationMessage
from .status_store import set_status
import os

logging.basicConfig(level=logging.INFO)

async def process_message(message: aio_pika.IncomingMessage):
    async with message.process(requeue=True):
        try:
            data = NotificationMessage.parse_raw(message.body)
            from .template_service import fetch_template
            template = await fetch_template(data.template_id, data.variables)
            rabbitmq_url = os.getenv("RABBITMQ_URL")
            sent = await send_email_with_retries(
                to_email=data.email,
                subject=template["subject"],
                content=template["content"],
                original_data=data.dict(),
                rabbitmq_url=rabbitmq_url
            )
            status_msg = "sent" if sent else "failed: see DLQ"
            set_status(data.notification_id, status_msg)
            logging.info(f"Notification {data.notification_id} processed: {status_msg}")
        except Exception:
            logging.exception("Failed to process message")
            raise

async def consume(connection, stop_event: asyncio.Event):
    channel = await connection.channel()
    await channel.set_qos(prefetch_count=10)
    queue = await channel.declare_queue("email.queue", durable=True)
    await queue.consume(process_message)
    logging.info("Consuming email.queue...")
    await stop_event.wait()

async def close_connection(connection):
    if connection and not connection.is_closed:
        await connection.close()
