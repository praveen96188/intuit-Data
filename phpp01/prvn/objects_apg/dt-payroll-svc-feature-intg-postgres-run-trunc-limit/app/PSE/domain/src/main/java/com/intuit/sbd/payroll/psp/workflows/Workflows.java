package com.intuit.sbd.payroll.psp.workflows;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines the Workflows available and its bit position in the OII flag
 * The workflow bit postion in the OII flag should be unique
 * do not change the bit position
 */
public enum Workflows {
    // This flag should be enabled always as a pre-requisite to use OII flag
    OII(0),
    // Placeholder never used
    ADD_EIN(1),
    // Placeholder never used
    PURCHASE_PAYROLL(2),
    // Not reliable flag & never used, use SERVICE_FK ='DirectDeposit' and STATUS_CD ='ActiveCurrent' from PSP_COMPANY_SERVICE
    ACTIVATE_DIRECT_DEPOSIT(3),
    // This flag is set when is company is DD enabled via TRON only OR migrated to SMS as part of AccountProfileMigration
    MONEY_MOVEMENT_ONBOARDING(4),
    // Used for companies auto enabled through OII;
    ENABLE_VMP(5);

    private int bit;

    Workflows(int bit) {
        this.bit = bit;
    }

    public int getValue() {
        return this.bit;
    }

    public static SpcfLogger logger = SpcfLogManager.getLogger(Workflows.class);


    static {
        checkForDuplicateWorkflowsValue();
    }

    /**
     * throws Exception if there is any duplicate value for the workflow
     * @return
     */
    public static void checkForDuplicateWorkflowsValue() {
        final Set<Integer> bits = new HashSet<>();

        for (final Workflows value : Workflows.values()) {
            boolean added=bits.add(value.getValue());
            if(!added){
                logger.info("Duplicate Workflow forund "+value);
                throw new RuntimeException("Duplicate Workflows Enum values are not allowed");
            }
        }
    }
}