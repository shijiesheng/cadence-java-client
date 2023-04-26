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

package com.uber.cadence.migration;

import com.uber.cadence.*;
import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.common.RetryOptions;
import com.uber.cadence.internal.common.RpcRetryer;
import com.uber.cadence.internal.sync.SyncWorkflowDefinition;
import com.uber.cadence.serviceclient.IWorkflowService;
import com.uber.cadence.workflow.*;
import org.apache.thrift.protocol.TField;
import com.uber.cadence.activity.ActivityOptions;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;

public class MigrationInterceptor extends WorkflowInterceptorBase {

  // TODO add newDomain override
  private final WorkflowInterceptor next;
  private static final String versionChangeID = "cadenceMigrationInterceptor";
  private static final int versionV1 = 1;

  private final ActivityOptions options =
          new ActivityOptions
                  .Builder()
                  .setScheduleToCloseTimeout(Duration.ofSeconds(10))
                  .setRetryOptions(new RetryOptions.Builder().build())
                  .build();
  private final MigrationActivities activities =
          Workflow.newActivityStub(MigrationActivities.class, options);

  private class MigrationDecision {
    boolean shouldMigrate;
    String reason;

    public MigrationDecision(boolean shouldMigrate, String reason) {
      this.shouldMigrate = shouldMigrate;
      this.reason = reason;
    }
  }

  public MigrationInterceptor(
    WorkflowInterceptor next,
    WorkflowClient clientInNewDomain) {
    super(next);
    this.next = next;
  }

  @Override
  public byte[] executeWorkflow(SyncWorkflowDefinition workflowDefinition, WorkflowExecuteInput input) {
    WorkflowInfo info = Workflow.getWorkflowInfo();

    int version = getVersion(versionChangeID, Workflow.DEFAULT_VERSION, versionV1);
    switch (version) {
      case versionV1:
        // skip migration on non-cron and child workflows
        if(input.getWorkflowExecutionStartedEventAttributes().cronSchedule == "" ||
                input.getWorkflowExecutionStartedEventAttributes().getParentWorkflowExecution().getWorkflowId() != ""){
          return next.executeWorkflow(workflowDefinition, input);
        }

        MigrationDecision decision = Workflow.sideEffect(MigrationDecision.class, () -> shouldMigrate(workflowDefinition, input));
        if (decision.shouldMigrate)
        {
          StartWorkflowExecutionRequest request =
                  new StartWorkflowExecutionRequest()
                          // add as much as possible from the attributes to request, including header, memo,
                          // refer to https://sourcegraph.uberinternal.com/github.com/uber/cadence@master/-/blob/service/frontend/workflowHandler.go#L4623:6
                          .setDomain(newDomainName) //from where to get new domain name
                          .setWorkflowId(input.getWorkflowExecutionStartedEventAttributes ().execution)
                          .setTaskList(new TaskList().setName(input.getWorkflowExecutionStartedEventAttributes().taskList.getName()))
                          .setInput(input.getInput())
                          .setWorkflowType(new WorkflowType().setName(input.getWorkflowType().getName()))
                          .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.TerminateIfRunning)
                          .setRetryPolicy(input.getWorkflowExecutionStartedEventAttributes().getRetryPolicy())
                          .setRequestId(UUID.randomUUID().toString())
                          .setExecutionStartToCloseTimeoutSeconds(864000)
                          .setTaskStartToCloseTimeoutSeconds(60);
          try {
            MigrationActivities.StartWorkflowExecutionResponse response = activities.StartWorkflowInNewDomain(new MigrationActivities.StartNewWorkflowRequest());
            // TODO add logging, metrics
            throw new CancellationException("cancel due to migration: "+ response.response.toString());
          } catch (ActivityException e) { // fallback if start workflow in new domain failed
            return next.executeWorkflow(workflowDefinition, input);
          }
        }
      default:
        return next.executeWorkflow(workflowDefinition, input);
    }
  }

//  @Override
//  public void continueAsNew(Optional<String> workflowType, Optional<ContinueAsNewOptions> options, Object[] args) {
//    int version = getVersion(versionChangeID, Workflow.DEFAULT_VERSION, versionV1);
//    switch (version) {
//      case versionV1:
//        next.
//        if(input.getWorkflowExecutionStartedEventAttributes().cronSchedule == "" ||
//                input.getWorkflowExecutionStartedEventAttributes().getParentWorkflowExecution().getWorkflowId() != ""){
//          return next.executeWorkflow(workflowDefinition, input);
//        }
//      default:
//        next.continueAsNew(workflowType, options, args);
//    }

  }

  //TODO reserved for dynamic configuration support
  private MigrationDecision shouldMigrate(SyncWorkflowDefinition workflowDefinition, WorkflowExecuteInput input)  {
    return new MigrationDecision(true, "");
  }
}
