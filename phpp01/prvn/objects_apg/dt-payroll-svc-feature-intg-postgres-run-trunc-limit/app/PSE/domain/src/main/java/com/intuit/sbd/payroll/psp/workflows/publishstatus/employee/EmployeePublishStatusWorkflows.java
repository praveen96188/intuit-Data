package com.intuit.sbd.payroll.psp.workflows.publishstatus.employee;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashSet;
import java.util.Set;

public enum EmployeePublishStatusWorkflows {
    EMS(0),
    SANCTIONS_EMPLOYEE(1),
    WALLET_EMPLOYEE(2),
    PERSONA_VALIDATED(3),
    EMS_PUBLISHED(4);

    private int bit;

    EmployeePublishStatusWorkflows(int bit) {
        this.bit = bit;
    }

    public int getValue() {
        return this.bit;
    }

    public static SpcfLogger logger = SpcfLogManager.getLogger(EmployeePublishStatusWorkflows.class);

    static {
        checkForDuplicateWorkflowsValue();
    }

    /**
     * throws Exception if there is any duplicate value for the workflow
     *
     * @return
     */
    public static void checkForDuplicateWorkflowsValue() {
        final Set<Integer> bits = new HashSet<>();

        for (final EmployeePublishStatusWorkflows value : EmployeePublishStatusWorkflows.values()) {
            boolean added = bits.add(value.getValue());
            if (!added) {
                logger.info("Duplicate Workflow found " + value);
                throw new RuntimeException("Duplicate Workflows Enum values are not allowed");
            }
        }
    }
}
