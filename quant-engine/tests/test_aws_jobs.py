def test_aws_job_module_imports_without_creating_clients():
    from quant_engine.app.aws_jobs import DynamoJobStore
    assert DynamoJobStore is not None
