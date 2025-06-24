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

package com.uber.cadence.internal.external;

import com.uber.cadence.entities.QueryWorkflowResponse;
import com.uber.cadence.entities.WorkflowExecution;
import com.uber.cadence.entities.WorkflowExecutionAlreadyStartedError;
import com.uber.cadence.internal.common.SignalWithStartWorkflowExecutionParameters;
import com.uber.cadence.internal.common.StartWorkflowExecutionParameters;
import com.uber.cadence.internal.common.TerminateWorkflowExecutionParameters;
import com.uber.cadence.internal.replay.QueryWorkflowParameters;
import com.uber.cadence.internal.replay.SignalExternalWorkflowParameters;
import com.uber.cadence.serviceclient.IWorkflowServiceV4;
import java.util.concurrent.CompletableFuture;

public interface GenericWorkflowClientExternal {

  WorkflowExecution startWorkflow(StartWorkflowExecutionParameters startParameters)
      throws WorkflowExecutionAlreadyStartedError;

  CompletableFuture<WorkflowExecution> startWorkflowAsync(
      StartWorkflowExecutionParameters startParameters);

  CompletableFuture<WorkflowExecution> startWorkflowAsync(
      StartWorkflowExecutionParameters startParameters, Long timeoutInMillis);

  void enqueueStartWorkflow(StartWorkflowExecutionParameters startParameters)
      throws WorkflowExecutionAlreadyStartedError;

  CompletableFuture<Void> enqueueStartWorkflowAsync(
      StartWorkflowExecutionParameters startParameters, Long timeoutInMillis);

  void signalWorkflowExecution(SignalExternalWorkflowParameters signalParameters);

  CompletableFuture<Void> signalWorkflowExecutionAsync(
      SignalExternalWorkflowParameters signalParameters);

  CompletableFuture<Void> signalWorkflowExecutionAsync(
      SignalExternalWorkflowParameters signalParameters, Long timeoutInMillis);

  WorkflowExecution signalWithStartWorkflowExecution(
      SignalWithStartWorkflowExecutionParameters parameters);

  WorkflowExecution enqueueSignalWithStartWorkflowExecution(
      SignalWithStartWorkflowExecutionParameters parameters);

  void requestCancelWorkflowExecution(WorkflowExecution execution);

  QueryWorkflowResponse queryWorkflow(QueryWorkflowParameters queryParameters);

  void terminateWorkflowExecution(TerminateWorkflowExecutionParameters terminateParameters);

  String generateUniqueId();

  IWorkflowServiceV4 getService();

  String getDomain();
}
