import asyncio
import os
import logging
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail


import aio_pika
import json


MAX_RETRIES = 5
INITIAL_DELAY = 1  # seconds

async def push_to_dead_letter_queue(message_data, error_message, rabbitmq_url, dlq_name="failed.queue"):
    connection = await aio_pika.connect_robust(rabbitmq_url)
    async with connection:
        channel = await connection.channel()
        dlq = await channel.declare_queue(dlq_name, durable=True)
        # Bundle the original message and the error for debugging.
        body = {
            "original_message": message_data,
            "error": error_message,
        }
        await channel.default_exchange.publish(
            aio_pika.Message(body=json.dumps(body).encode()),
            routing_key=dlq_name,
        )


async def send_email_with_retries(to_email, subject, content, original_data, rabbitmq_url):
    message = Mail(
        from_email=os.getenv('FROM_EMAIL'),
        to_emails=to_email,
        subject=subject,
        html_content=content
    )
    sg = SendGridAPIClient(os.getenv('SENDGRID_API_KEY'))

    retry_count = 0
    delay = INITIAL_DELAY

    while retry_count < MAX_RETRIES:
        try:
            # Run synchronous call in thread for async compatibility
            response = await asyncio.to_thread(sg.send, message)
            if 200 <= response.status_code < 300:
                logging.info(f"Email sent to {to_email}")
                return True
            else:
                raise Exception(f"Failed with status code {response.status_code}")
        except Exception as e:
            retry_count += 1
            logging.error(f"Attempt {retry_count} failed: {e}")
            if retry_count == MAX_RETRIES:
                logging.error(f"Email permanently failed for {to_email}")
                # Push to DLQ on final failure
                await push_to_dead_letter_queue(
                    message_data=original_data,
                    error_message=str(e),
                    rabbitmq_url=rabbitmq_url,
                    dlq_name="failed.queue"
                )
                return False
            await asyncio.sleep(delay)
            delay *= 2  # exponential backoff
