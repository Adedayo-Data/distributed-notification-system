# Use the official Python base image
FROM python:3.11-slim

# Set a working directory inside the container
WORKDIR /app

# Install system dependencies (if your email libs need them)
RUN apt-get update && apt-get install -y \
    build-essential \
    gcc \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy Python dependencies first (for caching)
COPY requirements.txt .

# Install dependencies
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# Copy the app code
COPY app/ app/

# Expose the FastAPI port (optional, for local reference)
EXPOSE 8000

# Command to run the FastAPI app with Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
