package com.uber.cadence.migration;

import com.uber.cadence.StartWorkflowExecutionRequest;
import com.uber.cadence.StartWorkflowExecutionResponse;
import com.uber.cadence.activity.ActivityMethod;

// TODO add implementation
public interface MigrationActivities  {
    final class StartNewWorkflowRequest{
        StartWorkflowExecutionRequest request;

        com.uber.cadence.StartWorkflowExecutionResponse
    }

    final class StartWorkflowExecutionResponse{
        StartWorkflowExecutionResponse response;
    }

    StartWorkflowExecutionResponse StartWorkflowInNewDomain(StartNewWorkflowRequest);
}
