package com.uber.cadence.serviceclient.exceptions;

public class WorkflowExecutionAlreadyCompletedException extends ServiceClientException {
    public WorkflowExecutionAlreadyCompletedException(Throwable cause) {
        super(cause);
    }
}