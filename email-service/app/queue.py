import asyncio
import logging
import os
from typing import Dict

import aio_pika

from .email_sender import send_email_with_retries
from .models import JobRequest, NotificationStatus
from .status_reporter import report_status
from .status_store import is_duplicate, mark_status
from .template_service import render_template
from .user_service import fetch_user

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

RABBITMQ_URL = os.getenv("RABBITMQ_URL")


def _stringify_variables(variables: Dict[str, object]) -> Dict[str, str]:
    return {str(key): "" if value is None else str(value) for key, value in (variables or {}).items()}


async def process_message(message: aio_pika.IncomingMessage):
    async with message.process(requeue=True):
        job = JobRequest.parse_raw(message.body)
        notification_id = job.notification_id

        if is_duplicate(notification_id):
            logger.warning("Duplicate or completed notification %s detected. Skipping.", notification_id)
            mark_status(notification_id, "SKIPPED")
            await report_status(notification_id, NotificationStatus.pending, "Duplicate notification")
            return

        try:
            user = await fetch_user(job.user_id)

            if not user or not user.email:
                logger.warning("User %s missing email address. Skipping notification %s.", job.user_id, notification_id)
                await report_status(notification_id, NotificationStatus.failed, "No email address on file")
                mark_status(notification_id, "SKIPPED")
                return

            preferences = getattr(user, "preferences", None)
            if preferences and preferences.email_notifications is False:
                logger.warning("User %s has email notifications disabled. Skipping notification %s.", user.id, notification_id)
                await report_status(notification_id, NotificationStatus.failed, "Email notifications disabled")
                mark_status(notification_id, "SKIPPED")
                return

            rendered_template = await render_template(
                template_code=job.template_code,
                variables=_stringify_variables(job.variables),
                notification_type="email",
            )

            subject = rendered_template.rendered_subject or "Notification"
            body = rendered_template.rendered_body or ""

            sent = await send_email_with_retries(
                to_email=user.email,
                subject=subject,
                content=body,
                original_data={"job_request": job.dict(by_alias=True), "user_id": user.id},
                rabbitmq_url=RABBITMQ_URL,
            )

            if sent:
                mark_status(notification_id, "DELIVERED")
                await report_status(notification_id, NotificationStatus.delivered)
                logger.info("Successfully processed notification %s", notification_id)
                return

            logger.error("Email delivery failed for %s; message moved to DLQ", notification_id)
            mark_status(notification_id, "FAILED")
            await report_status(notification_id, NotificationStatus.failed, "Email delivery failed")

        except Exception as exc:  # noqa: BLE001
            logger.exception("Failed to process notification %s: %s", notification_id, exc)
            mark_status(notification_id, "FAILED")
            await report_status(notification_id, NotificationStatus.failed, str(exc))
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
