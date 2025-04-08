package com.uber.cadence.internal.compatibility.proto;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.rpc.Status;
import com.uber.cadence.api.v1.*;
import com.uber.cadence.serviceclient.exceptions.*;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;

public class ErrorMapper {
    public static ServiceClientException Error(StatusRuntimeException e) {

        Status status = StatusProto.fromThrowable(e);
        if (status == null || status.getDetailsCount() == 0) {
            return new ServiceClientException("empty status or status with empty details", e);
        }

        Any detail = status.getDetails(0);

        try {
            switch (e.getStatus().getCode()) {
                case PERMISSION_DENIED:
                    return new AccessDeniedException(e);
                case INTERNAL:
                    return new InternalServiceException(e);
                case NOT_FOUND:
                    if (detail.is(WorkflowExecutionAlreadyCompletedError.class)) {
                        return new WorkflowExecutionAlreadyCompletedException(e);
                    } else {
                        return new EntityNotExistsException(e);
                    }
                case ALREADY_EXISTS:
                    if (detail.is(CancellationAlreadyRequestedError.class)) {
                        return new CancellationAlreadyRequestedException(e);
                    } else if (detail.is(DomainAlreadyExistsError.class)) {
                        return new DomainAlreadyExistsException(e);
                    } else if (detail.is(WorkflowExecutionAlreadyStartedError.class)) {
                        WorkflowExecutionAlreadyStartedError error = detail.unpack(WorkflowExecutionAlreadyStartedError.class);
                        return new WorkflowExecutionAlreadyStartedException(error.getStartRequestId(), error.getRunId());
                    }
                case DATA_LOSS:
                    return new InternalDataInconsistencyException(e);
                case FAILED_PRECONDITION:
                    if (detail.is(ClientVersionNotSupportedError.class)) {
                        ClientVersionNotSupportedError error = detail.unpack(ClientVersionNotSupportedError.class);
                        return new ClientVersionNotSupportedException(error.getFeatureVersion(), error.getClientImpl(), error.getSupportedVersions());
                    }else if (detail.is(FeatureNotEnabledError.class)) {
                        FeatureNotEnabledError error = detail.unpack(FeatureNotEnabledError.class);
                        return new FeatureNotEnabledException(error.getFeatureFlag());
                    } else if (detail.is(DomainNotActiveError.class)) {
                        DomainNotActiveError error = detail.unpack(DomainNotActiveError.class);
                        return new DomainNotActiveException(error.getDomain(), error.getCurrentCluster(), error.getActiveCluster());
                    }
                case RESOURCE_EXHAUSTED:
                    if (detail.is(LimitExceededError.class)) {
                        return new LimitExceededException(e);
                    } else {
                        return new ServiceBusyException(e);
                    }
                case UNKNOWN:
                default:
                    return new ServiceClientException(e);
            }
        } catch (InvalidProtocolBufferException ex) {
            return new ServiceClientException(ex);
        }
    }
}
