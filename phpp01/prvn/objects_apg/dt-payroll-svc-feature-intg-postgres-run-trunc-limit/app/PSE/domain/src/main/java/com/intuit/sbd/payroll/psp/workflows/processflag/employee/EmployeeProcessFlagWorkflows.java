package com.intuit.sbd.payroll.psp.workflows.processflag.employee;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashSet;
import java.util.Set;

public enum EmployeeProcessFlagWorkflows {
    PERSONA_CHECK(0);

    private int bit;

    EmployeeProcessFlagWorkflows(int bit) {
        this.bit = bit;
    }

    public int getValue() {
        return this.bit;
    }

    public static SpcfLogger logger = SpcfLogManager.getLogger(EmployeeProcessFlagWorkflows.class);

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

        for (final EmployeeProcessFlagWorkflows value : EmployeeProcessFlagWorkflows.values()) {
            boolean added = bits.add(value.getValue());
            if (!added) {
                logger.info("Duplicate Workflow found " + value);
                throw new RuntimeException("Duplicate Workflows Enum values are not allowed");
            }
        }
    }
}
