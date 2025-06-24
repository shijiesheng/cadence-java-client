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

package com.uber.cadence.internal.testservice;

import com.uber.cadence.entities.BadRequestError;
import com.uber.cadence.entities.BaseError;
import com.uber.cadence.entities.ChildWorkflowExecutionCanceledEventAttributes;
import com.uber.cadence.entities.ChildWorkflowExecutionCompletedEventAttributes;
import com.uber.cadence.entities.ChildWorkflowExecutionFailedEventAttributes;
import com.uber.cadence.entities.ChildWorkflowExecutionStartedEventAttributes;
import com.uber.cadence.entities.ChildWorkflowExecutionTimedOutEventAttributes;
import com.uber.cadence.entities.EntityNotExistsError;
import com.uber.cadence.entities.InternalServiceError;
import com.uber.cadence.entities.PollForActivityTaskRequest;
import com.uber.cadence.entities.PollForActivityTaskResponse;
import com.uber.cadence.entities.PollForDecisionTaskRequest;
import com.uber.cadence.entities.PollForDecisionTaskResponse;
import com.uber.cadence.entities.QueryWorkflowRequest;
import com.uber.cadence.entities.QueryWorkflowResponse;
import com.uber.cadence.entities.RecordActivityTaskHeartbeatResponse;
import com.uber.cadence.entities.RequestCancelWorkflowExecutionRequest;
import com.uber.cadence.entities.RespondActivityTaskCanceledByIDRequest;
import com.uber.cadence.entities.RespondActivityTaskCanceledRequest;
import com.uber.cadence.entities.RespondActivityTaskCompletedByIDRequest;
import com.uber.cadence.entities.RespondActivityTaskCompletedRequest;
import com.uber.cadence.entities.RespondActivityTaskFailedByIDRequest;
import com.uber.cadence.entities.RespondActivityTaskFailedRequest;
import com.uber.cadence.entities.RespondDecisionTaskCompletedRequest;
import com.uber.cadence.entities.RespondDecisionTaskFailedRequest;
import com.uber.cadence.entities.RespondQueryTaskCompletedRequest;
import com.uber.cadence.entities.SignalExternalWorkflowExecutionDecisionAttributes;
import com.uber.cadence.entities.SignalExternalWorkflowExecutionFailedCause;
import com.uber.cadence.entities.SignalWorkflowExecutionRequest;
import com.uber.cadence.entities.StartChildWorkflowExecutionFailedEventAttributes;
import com.uber.cadence.entities.StartWorkflowExecutionRequest;
import com.uber.cadence.entities.StickyExecutionAttributes;
import com.uber.cadence.entities.WorkflowExecutionAlreadyCompletedError;
import com.uber.cadence.entities.WorkflowExecutionCloseStatus;
import com.uber.cadence.internal.testservice.TestWorkflowMutableStateImpl.QueryId;
import java.util.Optional;

interface TestWorkflowMutableState {

  ExecutionId getExecutionId();

  /** @return close status of the workflow or empty if still open */
  Optional<WorkflowExecutionCloseStatus> getCloseStatus();

  StartWorkflowExecutionRequest getStartRequest();

  void startDecisionTask(PollForDecisionTaskResponse task, PollForDecisionTaskRequest pollRequest)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void completeDecisionTask(int historySize, RespondDecisionTaskCompletedRequest request)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void completeSignalExternalWorkflowExecution(String signalId, String runId)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void failSignalExternalWorkflowExecution(
      String signalId, SignalExternalWorkflowExecutionFailedCause cause)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void failDecisionTask(RespondDecisionTaskFailedRequest request)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void childWorkflowStarted(ChildWorkflowExecutionStartedEventAttributes a)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void childWorkflowFailed(String workflowId, ChildWorkflowExecutionFailedEventAttributes a)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void childWorkflowTimedOut(String activityId, ChildWorkflowExecutionTimedOutEventAttributes a)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void failStartChildWorkflow(String workflowId, StartChildWorkflowExecutionFailedEventAttributes a)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void childWorkflowCompleted(String workflowId, ChildWorkflowExecutionCompletedEventAttributes a)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void childWorkflowCanceled(String workflowId, ChildWorkflowExecutionCanceledEventAttributes a)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void startWorkflow(
      boolean continuedAsNew, Optional<SignalWorkflowExecutionRequest> signalWithStartSignal)
      throws InternalServiceError, BadRequestError;

  void startActivityTask(PollForActivityTaskResponse task, PollForActivityTaskRequest pollRequest)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void completeActivityTask(String activityId, RespondActivityTaskCompletedRequest request)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void completeActivityTaskById(String activityId, RespondActivityTaskCompletedByIDRequest request)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void failActivityTask(String activityId, RespondActivityTaskFailedRequest request)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void failActivityTaskById(String id, RespondActivityTaskFailedByIDRequest failRequest)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  RecordActivityTaskHeartbeatResponse heartbeatActivityTask(String activityId, byte[] details)
      throws InternalServiceError, EntityNotExistsError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void signal(SignalWorkflowExecutionRequest signalRequest)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void signalFromWorkflow(SignalExternalWorkflowExecutionDecisionAttributes a)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void requestCancelWorkflowExecution(RequestCancelWorkflowExecutionRequest cancelRequest)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void cancelActivityTask(String id, RespondActivityTaskCanceledRequest canceledRequest)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  void cancelActivityTaskById(String id, RespondActivityTaskCanceledByIDRequest canceledRequest)
      throws EntityNotExistsError, InternalServiceError, WorkflowExecutionAlreadyCompletedError,
          BadRequestError;

  QueryWorkflowResponse query(QueryWorkflowRequest queryRequest) throws BaseError;

  void completeQuery(QueryId queryId, RespondQueryTaskCompletedRequest completeRequest)
      throws EntityNotExistsError;

  StickyExecutionAttributes getStickyExecutionAttributes();

  Optional<TestWorkflowMutableState> getParent();
}
