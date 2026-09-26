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


## Phase 2 capabilities

- Canonical OHLCV validation and Parquet read/write.
- Local and S3 dataset-store boundaries using the same interface.
- Dataset/request SHA-256 fingerprints on every backtest result.
- Explicit strategy registry for the eight catalogue strategy IDs. The registry is a migration contract; it does not claim that all eight strategies are fully implemented yet.
- Parameter sweeps and walk-forward out-of-sample windows.
- PostgreSQL schema for durable research-run metadata.
- JSON-safe metrics; infinite profit factor is represented as null when there are wins but no losses.
