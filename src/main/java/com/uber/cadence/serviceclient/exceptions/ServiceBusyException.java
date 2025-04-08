package com.uber.cadence.serviceclient.exceptions;

public class ServiceBusyException extends ServiceClientException {
    public ServiceBusyException(Throwable cause) {
        super(cause);
    }
}