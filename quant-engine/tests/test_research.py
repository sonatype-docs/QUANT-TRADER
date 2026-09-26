from ._helpers import sample_request
from quant_engine.app.research import parameter_sweep, walk_forward

def test_parameter_sweep_returns_every_combination():
    request = sample_request().model_copy(update={"strategy_id": "STRAT-02-TURTLE-DONCHIAN", "bars": sample_request().bars * 2})
    results = parameter_sweep(request, {"fee_bps": [0.0, 4.0], "slippage_bps": [0.0, 1.0]})
    assert len(results) == 4
    assert all(item.result.strategy_id == "STRAT-02-TURTLE-DONCHIAN" for item in results)

def test_parameter_sweep_rejects_unknown_fields():
    try:
        parameter_sweep(sample_request(), {"donchian_period": [20]})
        assert False
    except ValueError as exc:
        assert "unsupported sweep fields" in str(exc)

def test_walk_forward_creates_out_of_sample_windows_with_strategy_engine():
    request = sample_request().model_copy(update={"strategy_id": "STRAT-02-TURTLE-DONCHIAN", "bars": sample_request().bars * 4})
    windows = walk_forward(request, train_bars=30, test_bars=30, step_bars=30)
    assert windows
    assert all(window.test_end > window.test_start for window in windows)
    assert all(window.result.strategy_id == "STRAT-02-TURTLE-DONCHIAN" for window in windows)
