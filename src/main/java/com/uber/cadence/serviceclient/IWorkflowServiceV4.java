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

import com.uber.cadence.api.v1.*;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

public interface IWorkflowServiceV4 {
  CompletableFuture<StartWorkflowExecutionResponse> startWorkflowExecution(
      StartWorkflowExecutionRequest request, @Nullable CallMetaData meta);

  CompletableFuture<StartWorkflowExecutionAsyncResponse> startWorkflowExecutionAsync(
      StartWorkflowExecutionAsyncRequest request, @Nullable CallMetaData meta);

  CompletableFuture<SignalWorkflowExecutionResponse> signalWorkflowExecution(
      SignalWorkflowExecutionRequest request, @Nullable CallMetaData meta);

  CompletableFuture<SignalWithStartWorkflowExecutionResponse> signalWithStartWorkflowExecution(
      SignalWithStartWorkflowExecutionRequest request, @Nullable CallMetaData meta);

  CompletableFuture<SignalWithStartWorkflowExecutionAsyncResponse>
      signalWithStartWorkflowExecutionAsync(
          SignalWithStartWorkflowExecutionAsyncRequest request, @Nullable CallMetaData meta);

  CompletableFuture<GetWorkflowExecutionHistoryResponse> getWorkflowExecutionHistory(
      GetWorkflowExecutionHistoryRequest request, @Nullable CallMetaData meta);

  CompletableFuture<QueryWorkflowResponse> queryWorkflow(
      QueryWorkflowRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RequestCancelWorkflowExecutionResponse> requestCancelWorkflowExecution(
      RequestCancelWorkflowExecutionRequest request, @Nullable CallMetaData meta);

  CompletableFuture<TerminateWorkflowExecutionResponse> terminateWorkflowExecution(
      TerminateWorkflowExecutionRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RestartWorkflowExecutionResponse> restartWorkflowExecution(
      RestartWorkflowExecutionRequest request, @Nullable CallMetaData meta);

  CompletableFuture<ListWorkflowExecutionsResponse> listWorkflowExecutions(
      ListWorkflowExecutionsRequest request, @Nullable CallMetaData meta);

  CompletableFuture<ScanWorkflowExecutionsResponse> scanWorkflowExecutions(
      ScanWorkflowExecutionsRequest request, @Nullable CallMetaData meta);

  CompletableFuture<ListOpenWorkflowExecutionsResponse> listOpenWorkflowExecutions(
      ListOpenWorkflowExecutionsRequest request, @Nullable CallMetaData meta);

  CompletableFuture<ListClosedWorkflowExecutionsResponse> listClosedWorkflowExecutions(
      ListClosedWorkflowExecutionsRequest request, @Nullable CallMetaData meta);

  CompletableFuture<CountWorkflowExecutionsResponse> countWorkflowExecutions(
      CountWorkflowExecutionsRequest request, @Nullable CallMetaData meta);

  CompletableFuture<PollForActivityTaskResponse> pollForActivityTask(
      PollForActivityTaskRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RecordActivityTaskHeartbeatResponse> recordActivityTaskHeartbeat(
      RecordActivityTaskHeartbeatRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondActivityTaskCanceledResponse> respondActivityTaskCanceled(
      RespondActivityTaskCanceledRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondActivityTaskCanceledByIDResponse> respondActivityTaskCanceledByID(
      RespondActivityTaskCanceledByIDRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondActivityTaskFailedResponse> respondActivityTaskFailed(
      RespondActivityTaskFailedRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondActivityTaskFailedByIDResponse> respondActivityTaskFailedByID(
      RespondActivityTaskFailedByIDRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondActivityTaskCompletedResponse> respondActivityTaskCompleted(
      RespondActivityTaskCompletedRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondActivityTaskCompletedByIDResponse> respondActivityTaskCompletedByID(
      RespondActivityTaskCompletedByIDRequest request, @Nullable CallMetaData meta);

  CompletableFuture<PollForDecisionTaskResponse> pollForDecisionTask(
      PollForDecisionTaskRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondDecisionTaskFailedResponse> respondDecisionTaskFailed(
      RespondDecisionTaskFailedRequest request, @Nullable CallMetaData meta);

  CompletableFuture<RespondDecisionTaskCompletedResponse> respondDecisionTaskCompleted(
      RespondDecisionTaskCompletedRequest request, @Nullable CallMetaData meta);
}
