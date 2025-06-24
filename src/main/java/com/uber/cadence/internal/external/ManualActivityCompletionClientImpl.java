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

import com.uber.cadence.client.ActivityCancelledException;
import com.uber.cadence.client.ActivityCompletionFailureException;
import com.uber.cadence.client.ActivityNotExistsException;
import com.uber.cadence.converter.DataConverter;
import com.uber.cadence.entities.BaseError;
import com.uber.cadence.entities.EntityNotExistsError;
import com.uber.cadence.entities.RecordActivityTaskHeartbeatRequest;
import com.uber.cadence.entities.RecordActivityTaskHeartbeatResponse;
import com.uber.cadence.entities.RespondActivityTaskCanceledByIDRequest;
import com.uber.cadence.entities.RespondActivityTaskCanceledRequest;
import com.uber.cadence.entities.RespondActivityTaskCompletedByIDRequest;
import com.uber.cadence.entities.RespondActivityTaskCompletedRequest;
import com.uber.cadence.entities.RespondActivityTaskFailedByIDRequest;
import com.uber.cadence.entities.RespondActivityTaskFailedRequest;
import com.uber.cadence.entities.WorkflowExecution;
import com.uber.cadence.entities.WorkflowExecutionAlreadyCompletedError;
import com.uber.cadence.internal.common.RpcRetryer;
import com.uber.cadence.internal.metrics.MetricsType;
import com.uber.cadence.serviceclient.IWorkflowServiceV4;
import com.uber.m3.tally.Scope;
import java.util.concurrent.CancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: service call retries
class ManualActivityCompletionClientImpl extends ManualActivityCompletionClient {

  private static final Logger log =
      LoggerFactory.getLogger(ManualActivityCompletionClientImpl.class);

  private final IWorkflowServiceV4 service;

  private final byte[] taskToken;

  private final DataConverter dataConverter;
  private final String domain;
  private final WorkflowExecution execution;
  private final String activityId;
  private final Scope metricsScope;

  ManualActivityCompletionClientImpl(
      IWorkflowServiceV4 service,
      String domain,
      byte[] taskToken,
      DataConverter dataConverter,
      Scope metricsScope) {
    this.service = service;
    this.taskToken = taskToken;
    this.dataConverter = dataConverter;
    this.domain = domain;
    this.execution = null;
    this.activityId = null;
    this.metricsScope = metricsScope;
  }

  ManualActivityCompletionClientImpl(
      IWorkflowServiceV4 service,
      String domain,
      WorkflowExecution execution,
      String activityId,
      DataConverter dataConverter,
      Scope metricsScope) {
    this.service = service;
    this.taskToken = null;
    this.domain = domain;
    this.execution = execution;
    this.activityId = activityId;
    this.dataConverter = dataConverter;
    this.metricsScope = metricsScope;
  }

  @Override
  public void complete(Object result) {
    if (taskToken != null) {
      RespondActivityTaskCompletedRequest request = new RespondActivityTaskCompletedRequest();
      byte[] convertedResult = dataConverter.toData(result);
      request.setResult(convertedResult);
      request.setTaskToken(taskToken);
      try {
        RpcRetryer.retry(() -> service.RespondActivityTaskCompleted(request));
        metricsScope.counter(MetricsType.ACTIVITY_TASK_COMPLETED_COUNTER).inc(1);
      } catch (EntityNotExistsError e) {
        throw new ActivityNotExistsException(e);
      } catch (WorkflowExecutionAlreadyCompletedError e) {
        throw new ActivityNotExistsException(e);
      } catch (BaseError e) {
        throw new ActivityCompletionFailureException(e);
      }
    } else {
      if (activityId == null) {
        throw new IllegalArgumentException("Either activity id or task token are required");
      }
      RespondActivityTaskCompletedByIDRequest request =
          new RespondActivityTaskCompletedByIDRequest();
      request.setActivityID(activityId);
      byte[] convertedResult = dataConverter.toData(result);
      request.setResult(convertedResult);
      request.setDomain(domain);
      request.setWorkflowID(execution.getWorkflowId());
      request.setRunID(execution.getRunId());
      try {
        service.RespondActivityTaskCompletedByID(request);
        metricsScope.counter(MetricsType.ACTIVITY_TASK_COMPLETED_BY_ID_COUNTER).inc(1);
      } catch (EntityNotExistsError e) {
        throw new ActivityNotExistsException(e);
      } catch (WorkflowExecutionAlreadyCompletedError e) {
        throw new ActivityNotExistsException(e);
      } catch (BaseError e) {
        throw new ActivityCompletionFailureException(activityId, e);
      }
    }
  }

  @Override
  public void fail(Throwable failure) {
    if (failure == null) {
      throw new IllegalArgumentException("null failure");
    }
    // When converting failures reason is class name, details are serialized exception.
    if (taskToken != null) {
      RespondActivityTaskFailedRequest request = new RespondActivityTaskFailedRequest();
      request.setReason(failure.getClass().getName());
      request.setDetails(dataConverter.toData(failure));
      request.setTaskToken(taskToken);
      try {
        RpcRetryer.retry(() -> service.RespondActivityTaskFailed(request));
        metricsScope.counter(MetricsType.ACTIVITY_TASK_FAILED_COUNTER).inc(1);
      } catch (EntityNotExistsError e) {
        throw new ActivityNotExistsException(e);
      } catch (WorkflowExecutionAlreadyCompletedError e) {
        throw new ActivityNotExistsException(e);
      } catch (BaseError e) {
        throw new ActivityCompletionFailureException(e);
      }
    } else {
      RespondActivityTaskFailedByIDRequest request = new RespondActivityTaskFailedByIDRequest();
      request.setActivityID(activityId);
      request.setReason(failure.getClass().getName());
      request.setDetails(dataConverter.toData(failure));
      request.setDomain(domain);
      request.setWorkflowID(execution.getWorkflowId());
      request.setRunID(execution.getRunId());
      try {
        RpcRetryer.retry(() -> service.RespondActivityTaskFailedByID(request));
        metricsScope.counter(MetricsType.ACTIVITY_TASK_FAILED_BY_ID_COUNTER).inc(1);
      } catch (EntityNotExistsError e) {
        throw new ActivityNotExistsException(e);
      } catch (WorkflowExecutionAlreadyCompletedError e) {
        throw new ActivityNotExistsException(e);
      } catch (BaseError e) {
        throw new ActivityCompletionFailureException(activityId, e);
      }
    }
  }

  @Override
  public void recordHeartbeat(Object details) throws CancellationException {
    if (taskToken != null) {
      RecordActivityTaskHeartbeatRequest request = new RecordActivityTaskHeartbeatRequest();
      request.setDetails(dataConverter.toData(details));
      request.setTaskToken(taskToken);
      RecordActivityTaskHeartbeatResponse status = null;
      try {
        status = service.RecordActivityTaskHeartbeat(request);
        if (status.isCancelRequested()) {
          throw new ActivityCancelledException();
        }
      } catch (EntityNotExistsError e) {
        throw new ActivityNotExistsException(e);
      } catch (WorkflowExecutionAlreadyCompletedError e) {
        throw new ActivityNotExistsException(e);
      } catch (BaseError e) {
        throw new ActivityCompletionFailureException(e);
      }
    } else {
      throw new UnsupportedOperationException(
          "Heartbeating by id is not implemented by Cadence service yet.");
    }
  }

  @Override
  public void reportCancellation(Object details) {
    if (taskToken != null) {
      RespondActivityTaskCanceledRequest request = new RespondActivityTaskCanceledRequest();
      request.setDetails(dataConverter.toData(details));
      request.setTaskToken(taskToken);
      try {
        service.RespondActivityTaskCanceled(request);
        metricsScope.counter(MetricsType.ACTIVITY_TASK_CANCELED_COUNTER).inc(1);
      } catch (BaseError e) {
        // There is nothing that can be done at this point.
        // so let's just ignore.
        log.info("reportCancellation", e);
      }
    } else {
      RespondActivityTaskCanceledByIDRequest request = new RespondActivityTaskCanceledByIDRequest();
      request.setActivityID(activityId);
      request.setDetails(dataConverter.toData(details));
      request.setDomain(domain);
      request.setWorkflowID(execution.getWorkflowId());
      request.setRunID(execution.getRunId());
      try {
        service.RespondActivityTaskCanceledByID(request);
        metricsScope.counter(MetricsType.ACTIVITY_TASK_CANCELED_BY_ID_COUNTER).inc(1);
      } catch (BaseError e) {
        // There is nothing that can be done at this point.
        // so let's just ignore.
        log.info("reportCancellation", e);
      }
    }
  }
}
