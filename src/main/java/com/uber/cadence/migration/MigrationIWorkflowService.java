package com.uber.cadence.migration;

import com.uber.cadence.*;
import com.uber.cadence.serviceclient.DummyIWorkflowService;
import com.uber.cadence.serviceclient.IWorkflowService;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import java.util.concurrent.CompletableFuture;

public class MigrationIWorkflowService extends DummyIWorkflowService {

    private IWorkflowService serviceOld;
    private IWorkflowService serviceNew;
    private String domainOld;
    private String domainNew;

    // TODO to implement
    MigrationIWorkflowService(IWorkflowService serviceOld, String domainOld, IWorkflowService serviceNew, String domainNew) {
    }

    @Override
    public StartWorkflowExecutionResponse StartWorkflowExecution(StartWorkflowExecutionRequest startRequest) throws TException {

    }


    // TODO implement in parallel
    private boolean shouldStartInNew(String workflowID)  {
        try {
            serviceNew.DescribeWorkflowExecution(new DescribeWorkflowExecutionRequest()
                    .setDomain(domainNew)
                    .setExecution(new WorkflowExecution().setWorkflowId(workflowID))
            )
        } catch (EntityNotExistsError) {

        } finally {
            try {
                serviceOld.DescribeWorkflowExecution(new DescribeWorkflowExecutionRequest()
                        .setDomain(domainOld)
                        .setExecution(new WorkflowExecution().setWorkflowId(workflowID))
                )
            } catch (EntityNotExistsError) {

            } finally {

            }
        }

    }
}
