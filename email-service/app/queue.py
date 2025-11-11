import asyncio
import os
import aio_pika
import httpx
from .email_sender import send_email
from .models import NotificationMessage
from .status_store import set_status
import dotenv
dotenv.load_dotenv()
async def fetch_template(template_id, variables):
    # Dummy endpoint, replace with real API endpoint
    async with httpx.AsyncClient() as client:
        # resp = await client.get(f"http://template-service/templates/{template_id}")
        # template_str = resp.json().get("template", "{{name}} - default email")
        template_str = "{{name}} - default email"

        for k, v in variables.items():
            template_str = template_str.replace(f"{{{{{k}}}}}", v)
        return {
            "subject": "Notification",
            "content": template_str
        }

async def process_message(message: aio_pika.IncomingMessage):
    async with message.process():
        data = NotificationMessage.parse_raw(message.body)
        template = await fetch_template(data.template_id, data.variables)
        try:
            await send_email(data.email, template["subject"], template["content"])
            set_status(data.notification_id, "sent")
        except Exception as e:
            set_status(data.notification_id, f"failed: {e}")

async def consume():
    connection = await aio_pika.connect_robust(os.getenv("RABBITMQ_URL"))
    queue_name = "email.queue"
    async with connection:
        channel = await connection.channel()
        queue = await channel.declare_queue(queue_name, durable=True)
        await queue.consume(process_message)
        print("Consuming email.queue...")
        stop_event = asyncio.Event()
        await stop_event.wait() 