# QUANT-TRADER

QUANT-TRADER is a single-repository Android trading cockpit plus AWS-backed quant research platform.

## Repository architecture

- `app/` — existing Jetpack Compose Android cockpit and trading/research UI.
- `quant-engine/` — Python/FastAPI research engine, executable strategy rules, order-flow, pair research, walk-forward/sweeps, durable jobs, Bedrock analyst pipeline, and risk boundary.
- `infra/` — AWS CloudFormation for Cognito, ECS/Fargate, S3, SQS, DynamoDB, ECR, ALB, IAM, and supporting infrastructure.
- `.github/workflows/` — Android CI, quant-engine CI, infrastructure validation, and AWS deployment workflow.

The Android application remains the user-facing product. The Python service is an in-repository backend, not a separate product or replacement repository.

## Production flow

Android → Cognito → ALB/API → quant-engine → DynamoDB/SQS → ECS worker → S3 results.

Research results can be retrieved by the Android cockpit after the asynchronous worker completes.

## Local development

Backend:

```bash
cd quant-engine
pip install -r requirements.txt
PYTHONPATH=. pytest -q
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

Android backend configuration is supplied at build time with `QUANT_ENGINE_BASE_URL`, `COGNITO_ISSUER`, and `COGNITO_APP_CLIENT_ID`.

## AWS deployment

The canonical deployment is `.github/workflows/quant-engine-deploy.yml`. It builds the `quant-engine/` directory into the ECR image and deploys the infrastructure and ECS services defined in `infra/quant-engine-stack.yml`.

No second repository is required.
