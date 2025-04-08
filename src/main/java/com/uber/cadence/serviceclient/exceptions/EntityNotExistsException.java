package com.uber.cadence.serviceclient.exceptions;

public class EntityNotExistsException extends ServiceClientException {

    public EntityNotExistsException(Throwable cause) {
        super(cause);
    }
}
