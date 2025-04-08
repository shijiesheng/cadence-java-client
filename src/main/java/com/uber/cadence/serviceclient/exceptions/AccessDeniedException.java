package com.uber.cadence.serviceclient.exceptions;

public class AccessDeniedException extends ServiceClientException {

    public AccessDeniedException(Throwable cause) {
        super(cause);
    }
}
