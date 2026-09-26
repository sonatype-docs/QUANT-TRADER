# Alpaca Paper Trading Gateway

Server-side trading gateway for QUANT-TRADER.

The gateway is locked to Alpaca's paper endpoint and never accepts a client-supplied upstream URL, API key, or secret.

Routes:
- GET /health
- GET /v1/trading/account
- GET /v1/trading/orders
- POST /v1/trading/orders
- GET /v1/trading/orders/:order_id
- PATCH /v1/trading/orders/:order_id
- DELETE /v1/trading/orders/:order_id
- DELETE /v1/trading/orders
- GET /v1/trading/positions
- GET /v1/trading/positions/:symbol_or_asset_id
- DELETE /v1/trading/positions
- /v1/research/* proxies to the existing Python research service.

Protected routes require the existing QUANT-TRADER X-API-Key.
