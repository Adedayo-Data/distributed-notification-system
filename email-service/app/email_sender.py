import asyncio
import os
import time
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail
import dotenv
dotenv.load_dotenv()
MAX_RETRIES = 5
INITIAL_DELAY = 1  # seconds

async def send_email(to_email, subject, content):
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
            response = sg.send(message)
            # If the response indicates success (2xx), exit the loop
            if 200 <= response.status_code < 300:
                print(f"Email sent to {to_email}")
                return True
            else:
                raise Exception(f"Failed with status code {response.status_code}")
        except Exception as e:
            retry_count += 1
            print(f"Attempt {retry_count} failed: {e}")
            if retry_count == MAX_RETRIES:
                print(f"Email permanently failed for {to_email}")
                return False
            await asyncio.sleep(delay)
            delay *= 2  # exponential backoff
