from __future__ import annotations
from dataclasses import dataclass
from typing import Callable
from .models import BacktestRequest, BacktestResult
from .engine import run_backtest

@dataclass(frozen=True)
class StrategyDefinition:
    strategy_id: str
    name: str
    description: str
    runner: Callable[[BacktestRequest], BacktestResult]

# These are explicit server-side strategy contracts. They prevent the Android
# catalogue from being treated as an executable source of truth while each
# production strategy is migrated into a real runner.
STRATEGIES: tuple[StrategyDefinition, ...] = (
    StrategyDefinition("STRAT-01-MOMENTUM", "Momentum", "Server-side momentum adapter.", run_backtest),
    StrategyDefinition("STRAT-02-MEAN-REVERSION", "Mean Reversion", "Server-side mean-reversion adapter.", run_backtest),
    StrategyDefinition("STRAT-03-VOLATILITY", "Volatility", "Server-side volatility adapter.", run_backtest),
    StrategyDefinition("STRAT-04-BREAKOUT", "Breakout", "Server-side breakout adapter.", run_backtest),
    StrategyDefinition("STRAT-05-REGIME", "Regime", "Server-side regime adapter.", run_backtest),
    StrategyDefinition("STRAT-06-ORDER-FLOW-DELTA", "Order Flow Delta", "Order-flow contract; live L2 implementation is Phase 3.", run_backtest),
    StrategyDefinition("STRAT-07-MICROSTRUCTURE", "Microstructure", "Server-side microstructure contract.", run_backtest),
    StrategyDefinition("STRAT-08-ENSEMBLE", "Ensemble", "Server-side ensemble contract.", run_backtest),
)

_REGISTRY = {item.strategy_id: item for item in STRATEGIES}

def get_strategy(strategy_id: str) -> StrategyDefinition:
    try:
        return _REGISTRY[strategy_id]
    except KeyError as exc:
        raise ValueError(f"Unknown strategy_id: {strategy_id}") from exc

def list_strategies() -> list[StrategyDefinition]:
    return list(STRATEGIES)
