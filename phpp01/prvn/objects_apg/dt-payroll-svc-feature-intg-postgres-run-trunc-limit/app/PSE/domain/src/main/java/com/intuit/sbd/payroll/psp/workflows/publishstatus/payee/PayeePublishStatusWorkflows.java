package com.intuit.sbd.payroll.psp.workflows.publishstatus.payee;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashSet;
import java.util.Set;

public enum PayeePublishStatusWorkflows {
    SANCTIONS_CONTRACTOR(0),
    WALLET_CONTRACTOR(1);

    private int bit;

    PayeePublishStatusWorkflows(int bit) {
        this.bit = bit;
    }

    public int getValue() {
        return this.bit;
    }

    public static SpcfLogger logger = SpcfLogManager.getLogger(PayeePublishStatusWorkflows.class);

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

        for (final PayeePublishStatusWorkflows value : PayeePublishStatusWorkflows.values()) {
            boolean added = bits.add(value.getValue());
            if (!added) {
                logger.info("Duplicate Workflow found " + value);
                throw new RuntimeException("Duplicate Workflows Enum values are not allowed");
            }
        }
    }
}
