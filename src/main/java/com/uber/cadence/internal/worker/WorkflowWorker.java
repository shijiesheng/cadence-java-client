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

package com.uber.cadence.internal.worker;

import com.google.common.base.Strings;
import com.uber.cadence.ActivityLocalDispatchInfo;
import com.uber.cadence.Decision;
import com.uber.cadence.GetWorkflowExecutionHistoryResponse;
import com.uber.cadence.History;
import com.uber.cadence.HistoryEvent;
import com.uber.cadence.PollForDecisionTaskResponse;
import com.uber.cadence.RespondDecisionTaskCompletedRequest;
import com.uber.cadence.RespondDecisionTaskCompletedResponse;
import com.uber.cadence.RespondDecisionTaskFailedRequest;
import com.uber.cadence.RespondQueryTaskCompletedRequest;
import com.uber.cadence.ScheduleActivityTaskDecisionAttributes;
import com.uber.cadence.WorkflowExecution;
import com.uber.cadence.WorkflowExecutionStartedEventAttributes;
import com.uber.cadence.WorkflowQuery;
import com.uber.cadence.WorkflowType;
import com.uber.cadence.common.BinaryChecksum;
import com.uber.cadence.common.WorkflowExecutionHistory;
import com.uber.cadence.internal.common.RpcRetryer;
import com.uber.cadence.internal.common.WorkflowExecutionUtils;
import com.uber.cadence.internal.logging.LoggerTag;
import com.uber.cadence.internal.metrics.MetricsTag;
import com.uber.cadence.internal.metrics.MetricsType;
import com.uber.cadence.internal.worker.LocallyDispatchedActivityWorker.Task;
import com.uber.cadence.serviceclient.IWorkflowService;
import com.uber.m3.tally.Scope;
import com.uber.m3.tally.Stopwatch;
import com.uber.m3.util.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.slf4j.MDC;

public final class WorkflowWorker extends SuspendableWorkerBase
    implements Consumer<PollForDecisionTaskResponse> {

  private static final String POLL_THREAD_NAME_PREFIX = "Workflow Poller taskList=";
  private final DecisionTaskHandler handler;
  private final IWorkflowService service;
  private final String domain;
  private final String taskList;
  private final SingleWorkerOptions options;
  private final String stickyTaskListName;
  private final WorkflowRunLockManager runLocks = new WorkflowRunLockManager();
  private final Function<Task, Boolean> ldaTaskPoller;
  private PollTaskExecutor<PollForDecisionTaskResponse> pollTaskExecutor;

  public WorkflowWorker(
      IWorkflowService service,
      String domain,
      String taskList,
      SingleWorkerOptions options,
      DecisionTaskHandler handler,
      Function<Task, Boolean> ldaTaskPoller,
      String stickyTaskListName) {
    this.service = Objects.requireNonNull(service);
    this.domain = Objects.requireNonNull(domain);
    this.taskList = Objects.requireNonNull(taskList);
    this.handler = handler;
    this.ldaTaskPoller = ldaTaskPoller;
    this.stickyTaskListName = stickyTaskListName;

    PollerOptions pollerOptions = options.getPollerOptions();
    if (pollerOptions.getPollThreadNamePrefix() == null) {
      pollerOptions =
          PollerOptions.newBuilder(pollerOptions)
              .setPollThreadNamePrefix(
                  POLL_THREAD_NAME_PREFIX + "\"" + taskList + "\", domain=\"" + domain + "\"")
              .build();
    }
    this.options = SingleWorkerOptions.newBuilder(options).setPollerOptions(pollerOptions).build();
  }

  @Override
  public void start() {
    if (handler.isAnyTypeSupported()) {
      pollTaskExecutor =
          new PollTaskExecutor<>(domain, taskList, options, new TaskHandlerImpl(handler));
      SuspendableWorker poller =
          new Poller<>(
              options.getIdentity(),
              new WorkflowPollTask(
                  service,
                  domain,
                  taskList,
                  TaskListKind.TASK_LIST_KIND_NORMAL,
                  options.getMetricsScope(),
                  options.getIdentity()),
              pollTaskExecutor,
              options.getPollerOptions(),
              options.getMetricsScope(),
              options.getExecutorWrapper());
      poller.start();
      setPoller(poller);
      options.getMetricsScope().counter(MetricsType.WORKER_START_COUNTER).inc(1);
    }
  }

  public byte[] queryWorkflowExecution(WorkflowExecution exec, String queryType, byte[] args)
      throws Exception {
    GetWorkflowExecutionHistoryResponse historyResponse =
        WorkflowExecutionUtils.getHistoryPage(null, service, domain, exec);
    History history = historyResponse.getHistory();
    WorkflowExecutionHistory workflowExecutionHistory =
        new WorkflowExecutionHistory(history.getEvents());
    return queryWorkflowExecution(
        queryType, args, workflowExecutionHistory, historyResponse.getNextPageToken());
  }

  public byte[] queryWorkflowExecution(String jsonSerializedHistory, String queryType, byte[] args)
      throws Exception {
    WorkflowExecutionHistory history = WorkflowExecutionHistory.fromJson(jsonSerializedHistory);
    return queryWorkflowExecution(queryType, args, history, null);
  }

  public byte[] queryWorkflowExecution(
      WorkflowExecutionHistory history, String queryType, byte[] args) throws Exception {
    return queryWorkflowExecution(queryType, args, history, null);
  }

  private byte[] queryWorkflowExecution(
      String queryType, byte[] args, WorkflowExecutionHistory history, byte[] nextPageToken)
      throws Exception {
    PollForDecisionTaskResponse task = new PollForDecisionTaskResponse();
    task.setWorkflowExecution(history.getWorkflowExecution());
    task.setStartedEventId(Long.MAX_VALUE);
    task.setPreviousStartedEventId(Long.MAX_VALUE);
    task.setNextPageToken(nextPageToken);
    WorkflowQuery query = new WorkflowQuery();
    query.setQueryType(queryType).setQueryArgs(args);
    task.setQuery(query);
    List<HistoryEvent> events = history.getEvents();
    HistoryEvent startedEvent = events.get(0);
    WorkflowExecutionStartedEventAttributes started =
        startedEvent.getWorkflowExecutionStartedEventAttributes();
    if (started == null) {
      throw new IllegalStateException(
          "First event of the history is not WorkflowExecutionStarted: " + startedEvent);
    }
    WorkflowType workflowType = started.getWorkflowType();
    task.setWorkflowType(workflowType);
    task.setHistory(new History().setEvents(events));
    DecisionTaskHandler.Result result = handler.handleDecisionTask(task);
    if (result.getQueryCompleted() != null) {
      RespondQueryTaskCompletedRequest r = result.getQueryCompleted();
      if (r.getErrorMessage() != null) {
        throw new RuntimeException(
            "query failure for "
                + history.getWorkflowExecution()
                + ", queryType="
                + queryType
                + ", args="
                + Arrays.toString(args)
                + ", error="
                + r.getErrorMessage());
      }
      return r.getQueryResult();
    }
    throw new RuntimeException("Query returned wrong response: " + result);
  }

  @Override
  public void accept(PollForDecisionTaskResponse pollForDecisionTaskResponse) {
    pollTaskExecutor.process(pollForDecisionTaskResponse);
  }

  private class TaskHandlerImpl
      implements PollTaskExecutor.TaskHandler<PollForDecisionTaskResponse> {

    final DecisionTaskHandler handler;

    private TaskHandlerImpl(DecisionTaskHandler handler) {
      this.handler = handler;
    }

    @Override
    public void handle(PollForDecisionTaskResponse task) throws Exception {
      Scope metricsScope =
          options
              .getMetricsScope()
              .tagged(ImmutableMap.of(MetricsTag.WORKFLOW_TYPE, task.getWorkflowType().getName()));

      MDC.put(LoggerTag.WORKFLOW_ID, task.getWorkflowExecution().getWorkflowId());
      MDC.put(LoggerTag.WORKFLOW_TYPE, task.getWorkflowType().getName());
      MDC.put(LoggerTag.RUN_ID, task.getWorkflowExecution().getRunId());

      Lock runLock = null;
      if (!Strings.isNullOrEmpty(stickyTaskListName)) {
        runLock = runLocks.getLockForLocking(task.getWorkflowExecution().getRunId());
        runLock.lock();
      }

      try {
        Stopwatch sw = metricsScope.timer(MetricsType.DECISION_EXECUTION_LATENCY).start();
        DecisionTaskHandler.Result response = handler.handleDecisionTask(task);
        sw.stop();

        sw = metricsScope.timer(MetricsType.DECISION_RESPONSE_LATENCY).start();
        sendReply(service, task, response);
        sw.stop();

        metricsScope.counter(MetricsType.DECISION_TASK_COMPLETED_COUNTER).inc(1);
      } finally {
        MDC.remove(LoggerTag.WORKFLOW_ID);
        MDC.remove(LoggerTag.WORKFLOW_TYPE);
        MDC.remove(LoggerTag.RUN_ID);

        if (runLock != null) {
          runLocks.unlock(task.getWorkflowExecution().getRunId());
        }
      }
    }

    @Override
    public Throwable wrapFailure(PollForDecisionTaskResponse task, Throwable failure) {
      WorkflowExecution execution = task.getWorkflowExecution();
      return new RuntimeException(
          "Failure processing decision task. WorkflowID="
              + execution.getWorkflowId()
              + ", RunID="
              + execution.getRunId(),
          failure);
    }

    private void sendReply(
        IWorkflowService service,
        PollForDecisionTaskResponse task,
        DecisionTaskHandler.Result response)
        throws TException {
      RespondDecisionTaskCompletedRequest taskCompleted = response.getTaskCompleted();
      if (taskCompleted != null) {
        taskCompleted.setIdentity(options.getIdentity());
        taskCompleted.setTaskToken(task.getTaskToken());
        taskCompleted.setBinaryChecksum(BinaryChecksum.getBinaryChecksum());
        RpcRetryer.retry(
            () -> {
              RespondDecisionTaskCompletedResponse taskCompletedResponse = null;
              List<Task> activityTasks = new ArrayList<>();
              try {
                if (ldaTaskPoller != null) {
                  for (Decision decision : taskCompleted.getDecisions()) {
                    ScheduleActivityTaskDecisionAttributes attr =
                        decision.getScheduleActivityTaskDecisionAttributes();
                    if (attr != null && taskList.equals(attr.getTaskList().getName())) {
                      // assume the activity type is in registry otherwise the activity would be
                      // failed and retried from server
                      Task activityTask =
                          new Task(
                              attr.getActivityId(),
                              attr.getActivityType(),
                              attr.bufferForInput(),
                              attr.getScheduleToCloseTimeoutSeconds(),
                              attr.getStartToCloseTimeoutSeconds(),
                              attr.getHeartbeatTimeoutSeconds(),
                              task.getWorkflowType(),
                              domain,
                              attr.getHeader(),
                              task.getWorkflowExecution());
                      if (ldaTaskPoller.apply(activityTask)) {
                        options
                            .getMetricsScope()
                            .counter(MetricsType.ACTIVITY_LOCAL_DISPATCH_SUCCEED_COUNTER)
                            .inc(1);
                        decision
                            .getScheduleActivityTaskDecisionAttributes()
                            .setRequestLocalDispatch(true);
                        activityTasks.add(activityTask);
                      } else {
                        // all pollers are busy - no room to optimize
                        options
                            .getMetricsScope()
                            .counter(MetricsType.ACTIVITY_LOCAL_DISPATCH_FAILED_COUNTER)
                            .inc(1);
                      }
                    }
                  }
                }
                taskCompletedResponse = service.RespondDecisionTaskCompleted(taskCompleted);
              } finally {
                for (Task activityTask : activityTasks) {
                  boolean started = false;
                  if (taskCompletedResponse != null
                      && taskCompletedResponse.getActivitiesToDispatchLocally() != null) {
                    ActivityLocalDispatchInfo activityLocalDispatchInfo =
                        taskCompletedResponse
                            .getActivitiesToDispatchLocally()
                            .getOrDefault(activityTask.activityId, null);
                    if (activityLocalDispatchInfo != null) {
                      activityTask.scheduledTimestamp =
                          activityLocalDispatchInfo.getScheduledTimestamp();
                      activityTask.startedTimestamp =
                          activityLocalDispatchInfo.getStartedTimestamp();
                      activityTask.scheduledTimestampOfThisAttempt =
                          activityLocalDispatchInfo.getScheduledTimestampOfThisAttempt();
                      activityTask.taskToken = activityLocalDispatchInfo.bufferForTaskToken();
                      started = true;
                    }
                  }
                  activityTask.notify(started);
                }
              }
            });
      } else {
        RespondDecisionTaskFailedRequest taskFailed = response.getTaskFailed();
        if (taskFailed != null) {
          taskFailed.setIdentity(options.getIdentity());
          taskFailed.setTaskToken(task.getTaskToken());
          taskFailed.setBinaryChecksum(BinaryChecksum.getBinaryChecksum());
          RpcRetryer.retry(() -> service.RespondDecisionTaskFailed(taskFailed));
        } else {
          RespondQueryTaskCompletedRequest queryCompleted = response.getQueryCompleted();
          if (queryCompleted != null) {
            queryCompleted.setTaskToken(task.getTaskToken());
            // Do not retry query response.
            service.RespondQueryTaskCompleted(queryCompleted);
          }
        }
      }
    }
  }
}
