from quant_engine.app.orderflow import Aggressor, BookLevel, OrderBookSnapshot, OrderFlowCalculator, TradeTick
from quant_engine.app.orderflow_strategy import Signal, evaluate

def test_orderflow_strategy_detects_sell_absorption():
    calc = OrderFlowCalculator()
    calc.snapshot(OrderBookSnapshot(
        timestamp_ms=1,
        bids=(BookLevel(99.0, 2.0),),
        asks=(BookLevel(100.0, 10.0),),
    ))
    calc.snapshot(OrderBookSnapshot(\n        timestamp_ms=2,\n        bids=(BookLevel(99.0, 2.0),),\n        asks=(BookLevel(101.0, 10.0),),\n    ))\n    # Positive delta, but price stalls while offer depth dominates.\n    result = calc.trade(TradeTick(3, 100.0, 5.0, Aggressor.BUY))
    signal = evaluate(result, imbalance_threshold=0.2)
    assert signal.signal == Signal.SHORT

def test_orderflow_strategy_stays_flat_without_confirmation():
    calc = OrderFlowCalculator()
    calc.snapshot(OrderBookSnapshot(
        timestamp_ms=1,
        bids=(BookLevel(99.0, 10.0),),
        asks=(BookLevel(100.0, 2.0),),
    ))
    result = calc.trade(TradeTick(2, 101.0, 1.0, Aggressor.BUY))
    assert evaluate(result).signal == Signal.FLAT
