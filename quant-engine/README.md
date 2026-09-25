QUANT-TRADER Quant Engine

This is the first production migration boundary: Android remains the cockpit while research/backtesting becomes a server-side source of truth.

API:
- GET /health
- POST /v1/research/backtests
- GET /v1/research/backtests/{run_id}

The baseline engine is deterministic and cost-aware. It consumes explicit OHLCV bars and records fees/slippage in every trade. UI code must not manufacture Sharpe, profit factor, drawdown, or return values.

Next phases:
1. PostgreSQL metadata/results and S3/Parquet market data.
2. Strategy adapters for the existing catalogue.
3. Walk-forward, Monte Carlo, parameter sweeps and provenance hashes.
4. L2/order-flow ingestion and event-driven execution simulation.
5. SQS plus ECS/Batch for long research jobs.
6. Authentication and production observability.
