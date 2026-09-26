FROM node:22-bookworm-slim AS node-deps
WORKDIR /gateway
COPY gateway/package.json ./
RUN npm install --omit=dev

FROM python:3.12-slim
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1 PIP_NO_CACHE_DIR=1 NODE_PATH=/gateway/node_modules
WORKDIR /app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app
COPY tests ./tests
COPY gateway ./gateway

COPY --from=node-deps /usr/local/bin/node /usr/local/bin/node
COPY --from=node-deps /usr/local/lib/node_modules /usr/local/lib/node_modules
COPY --from=node-deps /gateway/node_modules ./gateway/node_modules

ENV PYTHONPATH=/app
EXPOSE 8080

CMD ["sh", "-c", "uvicorn app.main:app --host 127.0.0.1 --port 8081 & exec node gateway/server.mjs"]
