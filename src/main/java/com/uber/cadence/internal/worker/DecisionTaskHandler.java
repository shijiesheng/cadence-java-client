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

import com.uber.cadence.entities.PollForDecisionTaskResponse;
import com.uber.cadence.entities.RespondDecisionTaskCompletedRequest;
import com.uber.cadence.entities.RespondDecisionTaskFailedRequest;
import com.uber.cadence.entities.RespondQueryTaskCompletedRequest;

/**
 * Interface of workflow task handlers.
 *
 * @author fateev, suskin
 */
public interface DecisionTaskHandler {

  final class Result {
    private final RespondDecisionTaskCompletedRequest taskCompleted;
    private final RespondDecisionTaskFailedRequest taskFailed;
    private final RespondQueryTaskCompletedRequest queryCompleted;

    public Result(
        RespondDecisionTaskCompletedRequest taskCompleted,
        RespondDecisionTaskFailedRequest taskFailed,
        RespondQueryTaskCompletedRequest queryCompleted) {
      this.taskCompleted = taskCompleted;
      this.taskFailed = taskFailed;
      this.queryCompleted = queryCompleted;
    }

    public RespondDecisionTaskCompletedRequest getTaskCompleted() {
      return taskCompleted;
    }

    public RespondDecisionTaskFailedRequest getTaskFailed() {
      return taskFailed;
    }

    public RespondQueryTaskCompletedRequest getQueryCompleted() {
      return queryCompleted;
    }

    @Override
    public String toString() {
      return "Result{"
          + "taskCompleted="
          + taskCompleted
          + ", taskFailed="
          + taskFailed
          + ", queryCompleted="
          + queryCompleted
          + '}';
    }
  }

  /**
   * Handles a single workflow task. Shouldn't throw any exceptions. A compliant implementation
   * should return any unexpected errors as RespondDecisionTaskFailedRequest.
   *
   * @param decisionTask The decision task to handle.
   * @return One of the possible decision task replies: RespondDecisionTaskCompletedRequest,
   *     RespondQueryTaskCompletedRequest, RespondDecisionTaskFailedRequest
   */
  Result handleDecisionTask(PollForDecisionTaskResponse decisionTask) throws Exception;

  /** True if this handler handles at least one workflow type. */
  boolean isAnyTypeSupported();
}
