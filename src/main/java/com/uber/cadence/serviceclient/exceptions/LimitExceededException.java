package com.uber.cadence.serviceclient.exceptions;

public class LimitExceededException extends ServiceClientException {
    public LimitExceededException(Throwable cause) {
        super(cause);
    }
}