from ._helpers import sample_request
from quant_engine.app.research import parameter_sweep, walk_forward

def test_parameter_sweep_returns_every_combination():
    request = sample_request()
    results = parameter_sweep(request, {"fee_bps": [0.0, 4.0], "slippage_bps": [0.0, 1.0]})
    assert len(results) == 4
    assert {tuple(sorted(x.parameters.items())) for x in results} == {
        (("fee_bps", 0.0), ("slippage_bps", 0.0)),
        (("fee_bps", 0.0), ("slippage_bps", 1.0)),
        (("fee_bps", 4.0), ("slippage_bps", 0.0)),
        (("fee_bps", 4.0), ("slippage_bps", 1.0)),
    }

def test_walk_forward_creates_out_of_sample_windows():
    request = sample_request()
    windows = walk_forward(request, train_bars=30, test_bars=20, step_bars=20)
    assert len(windows) == 2
    assert all(window.test_end > window.test_start for window in windows)
