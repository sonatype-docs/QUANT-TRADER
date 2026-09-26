def test_dynamo_job_store_imports():
    from quant_engine.app.aws_jobs import DynamoJobStore
    assert DynamoJobStore
