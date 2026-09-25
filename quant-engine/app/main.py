from datetime import datetime, timezone
from fastapi import FastAPI, HTTPException
from .engine import run_backtest
from .models import BacktestRequest, BacktestResult

app = FastAPI(title="QUANT-TRADER Quant Engine", version="0.1.0")
_RESULTS = {}

@app.get("/health")
def health():
    return {"status": "ok", "service": "quant-engine", "time": datetime.now(timezone.utc).isoformat()}

@app.post("/v1/research/backtests", response_model=BacktestResult, status_code=201)
def create_backtest(request: BacktestRequest):
    try:
        result = run_backtest(request)
        _RESULTS[result.run_id] = result
        return result
    except (ValueError, OverflowError, ZeroDivisionError) as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc

@app.get("/v1/research/backtests/{run_id}", response_model=BacktestResult)
def get_backtest(run_id: str):
    result = _RESULTS.get(run_id)
    if result is None:
        raise HTTPException(status_code=404, detail="Backtest run not found")
    return result
