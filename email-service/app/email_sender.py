import os
import json
import asyncio
import logging
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail
import aio_pika

MAX_RETRIES = 5
INITIAL_DELAY = 1  # seconds

FROM_EMAIL = os.getenv("FROM_EMAIL")
SENDGRID_API_KEY = os.getenv("SENDGRID_API_KEY")

logging.basicConfig(level=logging.INFO)


async def push_to_dead_letter_queue(message_data, error_message, rabbitmq_url, dlq_name="failed.queue"):
    """Push a failed message to RabbitMQ DLQ."""
    try:
        connection = await aio_pika.connect_robust(rabbitmq_url)
        async with connection:
            channel = await connection.channel()
            dlq = await channel.declare_queue(dlq_name, durable=True)
            body = json.dumps({
                "original_message": message_data,
                "error": error_message
            }).encode()
            await channel.default_exchange.publish(
                aio_pika.Message(body=body),
                routing_key=dlq_name
            )
            logging.info("Message pushed to DLQ")
    except Exception:
        logging.exception("Failed to push message to DLQ")


async def send_email_async(sg, message):
    """Run SendGrid synchronous send in a thread for async compatibility."""
    loop = asyncio.get_running_loop()
    return await loop.run_in_executor(None, sg.send, message)


async def send_email_with_retries(to_email, subject, content, original_data, rabbitmq_url):
    """Send email with retry logic and DLQ fallback."""
    if not FROM_EMAIL or not SENDGRID_API_KEY:
        raise RuntimeError("SendGrid configuration missing")

    sg = SendGridAPIClient(SENDGRID_API_KEY)
    message = Mail(
        from_email=FROM_EMAIL,
        to_emails=to_email,
        subject=subject,
        html_content=content
    )

    retry_count = 0
    delay = INITIAL_DELAY

    while retry_count < MAX_RETRIES:
        try:
            response = await send_email_async(sg, message)
            if 200 <= response.status_code < 300:
                logging.info(f"Email sent")
                return True
            else:
                raise Exception(f"SendGrid status code: {response.status_code}")
        except Exception as e:
            retry_count += 1
            logging.warning(f"Attempt {retry_count} failed for {to_email}: {e}")
            if retry_count == MAX_RETRIES:
                logging.error(f"Email permanently failed for {to_email}, sending to DLQ")
                await push_to_dead_letter_queue(
                    message_data=original_data,
                    error_message=str(e),
                    rabbitmq_url=rabbitmq_url
                )
                return False
            await asyncio.sleep(delay)
            delay = min(delay * 2, 60)  # exponential backoff with cap
