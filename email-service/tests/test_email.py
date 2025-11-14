import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from unittest.mock import patch
from app.email_sender import send_email_with_retries

import os

import pytest

@pytest.mark.asyncio
@patch("app.email_sender.SendGridAPIClient.send")
async def test_send_email_success(mock_send):
    os.environ.setdefault("FROM_EMAIL", "noreply@example.com")
    os.environ.setdefault("SENDGRID_API_KEY", "dummy-key")
    mock_send.return_value.status_code = 202
    result = await send_email_with_retries(
        "valid@email.com",
        "subject",
        "content",
        {"to": "valid@email.com", "subject": "subject", "content": "content"},
        os.getenv("RABBITMQ_URL")
    )
    assert result is True

@pytest.mark.asyncio
@patch("app.email_sender.SendGridAPIClient.send")
async def test_send_email_fail_and_dlq(mock_send):
    os.environ.setdefault("FROM_EMAIL", "noreply@example.com")
    os.environ.setdefault("SENDGRID_API_KEY", "dummy-key")
    mock_send.side_effect = Exception("send error")
    result = await send_email_with_retries(
        "fail@email.com",
        "subject",
        "content",
        {"to": "fail@email.com", "subject": "subject", "content": "content"},
        os.getenv("RABBITMQ_URL")
    )
    assert result is False
