package com.uber.cadence.serviceclient.exceptions;

public class InternalServiceException extends ServiceClientException{
    public InternalServiceException(Throwable cause) {
        super(cause);
    }
}
