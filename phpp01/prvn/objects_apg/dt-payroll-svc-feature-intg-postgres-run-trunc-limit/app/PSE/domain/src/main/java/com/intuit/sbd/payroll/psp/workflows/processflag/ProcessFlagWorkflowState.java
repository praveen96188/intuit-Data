package com.intuit.sbd.payroll.psp.workflows.processflag;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashSet;
import java.util.Set;

public enum ProcessFlagWorkflowState {
    INITIAL(0),
    DONE(2),
    ERROR(3);

    private int bit;

    ProcessFlagWorkflowState(int bit) {
        this.bit = bit;
    }

    public int getValue() {
        return this.bit;
    }

    public static SpcfLogger logger = SpcfLogManager.getLogger(ProcessFlagWorkflowState.class);

    static {
        checkForDuplicateWorkflowStateValue();
    }

    public static ProcessFlagWorkflowState workflowState(int value) {
        for (ProcessFlagWorkflowState workflowState : ProcessFlagWorkflowState.values()) {
            if (workflowState.getValue() == value) {
                return workflowState;
            }
        }
        throw new IllegalArgumentException("No workflow found for given index");
    }

    /**
     * throws Exception if there is any duplicate value for the workflow state
     *
     * @return
     */
    public static void checkForDuplicateWorkflowStateValue() {
        final Set<Integer> bits = new HashSet<>();

        for (final ProcessFlagWorkflowState value : ProcessFlagWorkflowState.values()) {
            boolean added = bits.add(value.getValue());
            if (!added) {
                logger.info("Duplicate WorkflowState found " + value);
                throw new RuntimeException("Duplicate WorkflowState Enum values are not allowed");
            }
        }
    }
}
