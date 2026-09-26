from __future__ import annotations
import os
from .sqs_jobs import SQSResearchQueue
from .s3_results import S3ResultStore
from .worker import run_worker

def main():
    queue_url = os.environ["RESEARCH_QUEUE_URL"]
    bucket = os.environ["RESEARCH_RESULTS_BUCKET"]
    run_worker(SQSResearchQueue(queue_url), S3ResultStore(bucket))

if __name__ == "__main__":
    main()
