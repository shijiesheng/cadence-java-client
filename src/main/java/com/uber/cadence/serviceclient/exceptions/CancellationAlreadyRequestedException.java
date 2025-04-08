package com.uber.cadence.serviceclient.exceptions;

public class CancellationAlreadyRequestedException extends ServiceClientException {
    public CancellationAlreadyRequestedException(Throwable cause) {
        super(cause);
    }
}