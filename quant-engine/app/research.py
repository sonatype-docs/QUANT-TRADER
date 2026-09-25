from __future__ import annotations
from dataclasses import dataclass
from itertools import product
from .engine import run_backtest
from .models import BacktestRequest, BacktestResult

@dataclass(frozen=True)
class SweepResult:
    parameters: dict[str, float]
    result: BacktestResult

@dataclass(frozen=True)
class WalkForwardWindow:
    train_start: int
    train_end: int
    test_start: int
    test_end: int
    result: BacktestResult

def parameter_sweep(request: BacktestRequest, parameter_grid: dict[str, list[float]]) -> list[SweepResult]:
    if not parameter_grid:
        return [SweepResult({}, run_backtest(request))]
    keys = sorted(parameter_grid)
    values = [parameter_grid[key] for key in keys]
    output: list[SweepResult] = []
    for combo in product(*values):
        params = dict(zip(keys, combo))
        candidate = request.model_copy(update=params)
        output.append(SweepResult(params, run_backtest(candidate)))
    return output

def walk_forward(
    request: BacktestRequest,
    train_bars: int,
    test_bars: int,
    step_bars: int | None = None,
) -> list[WalkForwardWindow]:
    if train_bars < 30 or test_bars < 1:
        raise ValueError("train_bars must be >= 30 and test_bars must be >= 1")
    step = step_bars or test_bars
    if step < 1:
        raise ValueError("step_bars must be >= 1")
    windows: list[WalkForwardWindow] = []
    start = 0
    while start + train_bars + test_bars <= len(request.bars):
        test_start = start + train_bars
        test_end = test_start + test_bars
        test_request = request.model_copy(update={"bars": request.bars[test_start:test_end]})
        # The current baseline has no trainable parameters. This still creates
        # a deterministic out-of-sample evaluation boundary for future optimizers.
        result = run_backtest(test_request)
        windows.append(WalkForwardWindow(start, test_start, test_start, test_end, result))
        start += step
    return windows
