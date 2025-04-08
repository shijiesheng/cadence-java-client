package com.uber.cadence.serviceclient.exceptions;

/**
 * Base class for all exceptions thrown by the service client.
 *
 * <p>This is a catchall for all other errors.
 */
public class ServiceClientException extends RuntimeException{
    ServiceClientException() {
        super();
    }

    public ServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceClientException(Throwable cause) {
        super(cause);
    }
}
