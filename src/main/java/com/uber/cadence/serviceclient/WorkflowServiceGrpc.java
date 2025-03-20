/*
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.uber.cadence.serviceclient;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.uber.cadence.api.v1.*;
import com.uber.cadence.internal.compatibility.proto.serviceclient.IGrpcServiceStubs;
import io.grpc.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public class WorkflowServiceGrpc implements IWorkflowServiceV4 {

  private final IGrpcServiceStubs grpcServiceStubs;

  WorkflowServiceGrpc(ClientOptions options) {
    this.grpcServiceStubs = IGrpcServiceStubs.newInstance(options);
  }

  @Override
  public CompletableFuture<StartWorkflowExecutionResponse> startWorkflowExecution(
      StartWorkflowExecutionRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .startWorkflowExecution(request));
  }

  @Override
  public CompletableFuture<StartWorkflowExecutionAsyncResponse> startWorkflowExecutionAsync(
      StartWorkflowExecutionAsyncRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .startWorkflowExecutionAsync(request));
  }

  @Override
  public CompletableFuture<SignalWorkflowExecutionResponse> signalWorkflowExecution(
      SignalWorkflowExecutionRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .signalWorkflowExecution(request));
  }

  @Override
  public CompletableFuture<SignalWithStartWorkflowExecutionResponse>
      signalWithStartWorkflowExecution(
          SignalWithStartWorkflowExecutionRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .signalWithStartWorkflowExecution(request));
  }

  @Override
  public CompletableFuture<SignalWithStartWorkflowExecutionAsyncResponse>
      signalWithStartWorkflowExecutionAsync(
          SignalWithStartWorkflowExecutionAsyncRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .signalWithStartWorkflowExecutionAsync(request));
  }

  @Override
  public CompletableFuture<GetWorkflowExecutionHistoryResponse> getWorkflowExecutionHistory(
      GetWorkflowExecutionHistoryRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .getWorkflowExecutionHistory(request));
  }

  @Override
  public CompletableFuture<QueryWorkflowResponse> queryWorkflow(
      QueryWorkflowRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .queryWorkflow(request));
  }

  @Override
  public CompletableFuture<RequestCancelWorkflowExecutionResponse> requestCancelWorkflowExecution(
      RequestCancelWorkflowExecutionRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .requestCancelWorkflowExecution(request));
  }

  @Override
  public CompletableFuture<TerminateWorkflowExecutionResponse> terminateWorkflowExecution(
      TerminateWorkflowExecutionRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .terminateWorkflowExecution(request));
  }

  @Override
  public CompletableFuture<RestartWorkflowExecutionResponse> restartWorkflowExecution(
      RestartWorkflowExecutionRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workflowFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .restartWorkflowExecution(request));
  }

  @Override
  public CompletableFuture<ListWorkflowExecutionsResponse> listWorkflowExecutions(
      ListWorkflowExecutionsRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .visibilityFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .listWorkflowExecutions(request));
  }

  @Override
  public CompletableFuture<ScanWorkflowExecutionsResponse> scanWorkflowExecutions(
      ScanWorkflowExecutionsRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .visibilityFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .scanWorkflowExecutions(request));
  }

  @Override
  public CompletableFuture<ListOpenWorkflowExecutionsResponse> listOpenWorkflowExecutions(
      ListOpenWorkflowExecutionsRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .visibilityFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .listOpenWorkflowExecutions(request));
  }

  @Override
  public CompletableFuture<ListClosedWorkflowExecutionsResponse> listClosedWorkflowExecutions(
      ListClosedWorkflowExecutionsRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .visibilityFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .listClosedWorkflowExecutions(request));
  }

  @Override
  public CompletableFuture<CountWorkflowExecutionsResponse> countWorkflowExecutions(
      CountWorkflowExecutionsRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .visibilityFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .countWorkflowExecutions(request));
  }

  @Override
  public CompletableFuture<PollForActivityTaskResponse> pollForActivityTask(
      PollForActivityTaskRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .pollForActivityTask(request));
  }

  @Override
  public CompletableFuture<RecordActivityTaskHeartbeatResponse> recordActivityTaskHeartbeat(
      RecordActivityTaskHeartbeatRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .recordActivityTaskHeartbeat(request));
  }

  @Override
  public CompletableFuture<RespondActivityTaskCanceledResponse> respondActivityTaskCanceled(
      RespondActivityTaskCanceledRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondActivityTaskCanceled(request));
  }

  @Override
  public CompletableFuture<RespondActivityTaskCanceledByIDResponse> respondActivityTaskCanceledByID(
      RespondActivityTaskCanceledByIDRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondActivityTaskCanceledByID(request));
  }

  @Override
  public CompletableFuture<RespondActivityTaskFailedResponse> respondActivityTaskFailed(
      RespondActivityTaskFailedRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondActivityTaskFailed(request));
  }

  @Override
  public CompletableFuture<RespondActivityTaskFailedByIDResponse> respondActivityTaskFailedByID(
      RespondActivityTaskFailedByIDRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondActivityTaskFailedByID(request));
  }

  @Override
  public CompletableFuture<RespondActivityTaskCompletedResponse> respondActivityTaskCompleted(
      RespondActivityTaskCompletedRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondActivityTaskCompleted(request));
  }

  @Override
  public CompletableFuture<RespondActivityTaskCompletedByIDResponse>
      respondActivityTaskCompletedByID(
          RespondActivityTaskCompletedByIDRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondActivityTaskCompletedByID(request));
  }

  @Override
  public CompletableFuture<PollForDecisionTaskResponse> pollForDecisionTask(
      PollForDecisionTaskRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .pollForDecisionTask(request));
  }

  @Override
  public CompletableFuture<RespondDecisionTaskFailedResponse> respondDecisionTaskFailed(
      RespondDecisionTaskFailedRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondDecisionTaskFailed(request));
  }

  @Override
  public CompletableFuture<RespondDecisionTaskCompletedResponse> respondDecisionTaskCompleted(
      RespondDecisionTaskCompletedRequest request, @Nullable CallMetaData meta) {
    return toCompletableFuture(
        grpcServiceStubs
            .workerFutureStub()
            .withInterceptors(new CallMetadataClientInterceptor(meta))
            .respondDecisionTaskCompleted(request));
  }

  private static class CallMetadataClientInterceptor implements ClientInterceptor {
    private final CallMetaData meta;

    CallMetadataClientInterceptor(CallMetaData meta) {
      this.meta = meta;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
      if (meta != null && meta.getTimeout().isPresent()) {
        Duration timeout = meta.getTimeout().get();
        callOptions = callOptions.withDeadlineAfter(timeout.toMillis(), TimeUnit.MILLISECONDS);
      }
      return next.newCall(method, callOptions);
    }
  }

  private <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenable) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Futures.addCallback(
        listenable,
        new FutureCallback<T>() {
          @Override
          public void onSuccess(T t) {
            completableFuture.complete(t);
          }

          @Override
          public void onFailure(Throwable throwable) {
            completableFuture.completeExceptionally(throwable);
          }
        },
        MoreExecutors.directExecutor());
    return completableFuture;
  }
}
