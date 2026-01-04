# This image is used by the PlotService in backend to generate plots
# in an isolated environment (AI-generated Python scripts).

FROM python:3.12.12-alpine

# Environment variables for safer and cleaner Python behavior
# Python runtime configuration:
# - PYTHONDONTWRITEBYTECODE: prevents creation of .pyc files and __pycache__ directories
#   (keeps the container filesystem clean)
# - PYTHONUNBUFFERED: forces stdout/stderr to be flushed immediately
#   (ensures logs from AI-generated scripts appear instantly)
# - PIP_NO_CACHE_DIR: disables pip's package cache
#   (reduces image size and avoids unnecessary files in Docker layers)
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

# Install system dependencies
RUN apk add --no-cache build-base libpq-dev

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create plots folder and set ownership
RUN mkdir -p /app/plotService/plots \
    && chown -R appuser:appgroup /app/plotService

WORKDIR /app/plotService

# Install Python packages
RUN pip install --upgrade pip && \
    pip install \
        matplotlib==3.9.2 \
        pandas==2.2.3 \
        numpy==2.1.3 \
        psycopg2-binary==2.9.9 \
        sqlalchemy==2.0.36 \
        mysql-connector-python==9.5.0

USER appuser

# Backend overrides the default command
CMD ["sh"]
