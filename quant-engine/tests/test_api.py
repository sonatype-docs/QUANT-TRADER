from fastapi.testclient import TestClient
from quant_engine.app.main import app
from ._helpers import sample_request

def test_health():
    assert TestClient(app).get("/health").status_code == 200

def test_create_and_fetch_backtest():
    client = TestClient(app)
    created = client.post("/v1/research/backtests", json=sample_request().model_dump(mode="json"))
    assert created.status_code == 201
    run_id = created.json()["run_id"]
    fetched = client.get(f"/v1/research/backtests/{run_id}")
    assert fetched.status_code == 200
    assert fetched.json()["run_id"] == run_id
