package com.intuit.sbd.payroll.psp.workflows.publishstatus.company;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashSet;
import java.util.Set;

public enum CompanyPublishStatusWorkflows {
    EMS(0),
    EVS(1),
    EVS_HOURLY(2),
    SANCTIONS_EMPLOYER(3),
    SANCTIONS_EMPLOYEE(4),
    SANCTIONS_CONTRACTOR(5),
    WORKFORCE_INVITE(6),
    WALLET_EMPLOYEE(7),
    WALLET_CONTRACTOR(8);

    private int bit;

    CompanyPublishStatusWorkflows(int bit) {
        this.bit = bit;
    }

    public int getValue() {
        return this.bit;
    }

    public static SpcfLogger logger = SpcfLogManager.getLogger(CompanyPublishStatusWorkflows.class);

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

        for (final CompanyPublishStatusWorkflows value : CompanyPublishStatusWorkflows.values()) {
            boolean added = bits.add(value.getValue());
            if (!added) {
                logger.info("Duplicate Workflow found " + value);
                throw new RuntimeException("Duplicate Workflows Enum values are not allowed");
            }
        }
    }
}
