package com.intuit.sbd.payroll.psp.workflows;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines the workflow state for each worflow
 */
public enum WorkflowState {
    DISABLED(0),
    ENABLED(1),
    ENABLE_AUTHORIZATION(2);

    private int bit;

    WorkflowState(int bit){
        this.bit = bit;
    }

    public int getValue(){
        return this.bit;
    }

    public static SpcfLogger logger = SpcfLogManager.getLogger(WorkflowState.class);

    static {
        checkForDuplicateWorkflowStateValue();
    }

    public static WorkflowState workflowState(int value)  {
        for (WorkflowState workflowState : WorkflowState.values()){
            if(workflowState.getValue() == value){
                return workflowState;
            }
        }
        throw new IllegalArgumentException("No workflow found for given index");
    }


    /**
     * throws Exception if there is any duplicate value for the workflow state
     * @return
     */
    public static void  checkForDuplicateWorkflowStateValue() {
        final Set<Integer> bits = new HashSet<>();

        for (final WorkflowState value : WorkflowState.values()) {
            boolean added=bits.add(value.getValue());
            if(!added){
                logger.info("Duplicate WorkflowState forund "+value);
                throw new RuntimeException("Duplicate WorkflowState Enum values are not allowed");
            }

        }
    }


}
