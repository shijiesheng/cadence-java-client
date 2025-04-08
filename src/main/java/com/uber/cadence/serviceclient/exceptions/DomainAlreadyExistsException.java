package com.uber.cadence.serviceclient.exceptions;

public class DomainAlreadyExistsException extends ServiceClientException {
    public DomainAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}