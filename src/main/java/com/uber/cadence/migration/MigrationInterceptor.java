package com.uber.cadence.migration;

import com.uber.cadence.internal.sync.SyncWorkflowDefinition;
import com.uber.cadence.workflow.ContinueAsNewOptions;
import com.uber.cadence.workflow.Workflow;
import com.uber.cadence.workflow.WorkflowInterceptor;
import com.uber.cadence.workflow.WorkflowInterceptorBase;

import java.util.Optional;

public class MigrationInterceptor extends WorkflowInterceptorBase {
    private static final String versionChangeID = "cadenceMigrationInterceptor";
    private static final int versionV1 = 1;
    private final WorkflowInterceptor next;


    public MigrationInterceptor(clientToCallNewDomain, WorkflowInterceptor next) {
        super(next);
        this.next = next;

        Workflow.newActivityStub()
        this.activities = c;
    }

    @Override
    public byte[] executeWorkflow(
            SyncWorkflowDefinition workflowDefinition, WorkflowExecuteInput input) {
        int version = getVersion(versionChangeID, Workflow.DEFAULT_VERSION, versionV1);
        switch (version) {
            case versionV1:
                if input.getWorkflowExecutionStartedEventAttributes().cronSchedule == "" {
                }

                // should migration inside sideeffect (reserverd for future usage)
                shouldMigrateDecision = sideEffect();
                if!  shouldMigrate {
                   return next.executeWorkflow()
                }
                // call activity to start new workflow
                // refer to https://cadenceworkflow.io/docs/java-client/implementing-workflows/#calling-activities for activities usage

            }
            default:
                return next.executeWorkflow(workflowDefinition, input);
        }
    }

    @Override
    public void continueAsNew(
            Optional<String> workflowType, Optional<ContinueAsNewOptions> options, Object[] args) {
    //TODO
    }
}

public class MigartionContinueInterceptor extends WorkflowInterceptorBase {

}

MigrateCron => MigartionContinueAsNew =>
