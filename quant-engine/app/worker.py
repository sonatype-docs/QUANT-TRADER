from __future__ import annotations
import logging
from .strategy_backtest import run_strategy_backtest
from .models import BacktestRequest
from .s3_results import S3ResultStore

logger = logging.getLogger(__name__)

def execute_job(message: dict, result_store: S3ResultStore) -> str:
    job_type = message["job_type"]
    if job_type != "backtest":
        raise ValueError(f"unsupported job_type: {job_type}")
    request = BacktestRequest.model_validate(message["payload"])
    result = run_strategy_backtest(request)
    return result_store.put(result)

def run_worker(queue, result_store, poll_seconds: int = 10) -> None:
    while True:
        messages = queue.receive(10)
        if not messages:
            continue
        for message in messages:
            try:
                import json
                body = json.loads(message["Body"])
                execute_job(body, result_store)
                queue.delete(message["ReceiptHandle"])
            except Exception:
                logger.exception("research job failed; leaving message for retry/DLQ")
