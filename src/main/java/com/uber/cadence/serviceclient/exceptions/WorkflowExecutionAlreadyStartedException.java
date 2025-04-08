package com.uber.cadence.serviceclient.exceptions;

public class WorkflowExecutionAlreadyStartedException extends ServiceClientException {
    private final String startRequestId;
    private final String runId;

    public WorkflowExecutionAlreadyStartedException(String startRequestId, String runId) {
        this.startRequestId = startRequestId;
        this.runId = runId;
    }

    public String getStartRequestId() {
        return startRequestId;
    }

    public String getRunId() {
        return runId;
    }
}
