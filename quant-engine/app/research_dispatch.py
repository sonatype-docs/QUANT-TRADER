from __future__ import annotations
import os
from .sqs_jobs import SQSResearchQueue
from .jobs import ResearchJob

class ResearchDispatcher:
    def __init__(self):
        self.queue = SQSResearchQueue(os.environ["RESEARCH_QUEUE_URL"])

    def dispatch(self, job: ResearchJob) -> str:
        return self.queue.enqueue(job.job_id, job.job_type, job.payload)
