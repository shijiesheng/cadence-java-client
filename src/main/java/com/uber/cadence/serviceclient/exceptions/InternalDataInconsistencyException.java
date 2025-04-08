package com.uber.cadence.serviceclient.exceptions;

public class InternalDataInconsistencyException extends ServiceClientException{
    public InternalDataInconsistencyException(Throwable cause) {
        super(cause);
    }
}
