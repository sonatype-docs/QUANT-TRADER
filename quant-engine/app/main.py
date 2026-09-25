from datetime import datetime, timezone
import os
from fastapi import FastAPI, HTTPException, Header
from .engine import run_backtest
from .models import BacktestRequest, BacktestResult

app = FastAPI(title="QUANT-TRADER Quant Engine", version="0.1.0")
_RESULTS = {}

def _require_api_key(api_key: str | None):
    expected = os.getenv("QUANT_ENGINE_API_KEY")
    if not expected:
        raise HTTPException(status_code=503, detail="Quant engine API key is not configured")
    if api_key != expected:
        raise HTTPException(status_code=401, detail="Invalid API key")

@app.get("/health")
def health():
    return {"status": "ok", "service": "quant-engine", "time": datetime.now(timezone.utc).isoformat()}

@app.post("/v1/research/backtests", response_model=BacktestResult, status_code=201)
def create_backtest(request: BacktestRequest, x_api_key: str | None = Header(default=None)):
    _require_api_key(x_api_key)
    try:
        result = run_backtest(request)
        _RESULTS[result.run_id] = result
        return result
    except (ValueError, OverflowError, ZeroDivisionError) as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc

@app.get("/v1/research/backtests/{run_id}", response_model=BacktestResult)
def get_backtest(run_id: str, x_api_key: str | None = Header(default=None)):
    _require_api_key(x_api_key)
    result = _RESULTS.get(run_id)
    if result is None:
        raise HTTPException(status_code=404, detail="Backtest run not found")
    return result
