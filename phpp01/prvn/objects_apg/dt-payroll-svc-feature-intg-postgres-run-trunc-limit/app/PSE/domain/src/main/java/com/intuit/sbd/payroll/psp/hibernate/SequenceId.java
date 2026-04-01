package com.intuit.sbd.payroll.psp.hibernate;

/**
 * PSID_AK is not here because
 */
public enum SequenceId {
    SEQ_ATF_BATCH_ID_NBR,
    SEQ_GEMS_UPLOAD_BATCH_ID,
    SEQ_QBDT_SOURCE_COMPANY_ID,
    SEQ_EFTPS_FILE_SEQUENCE,
    SEQ_EFTPS_PAYMENT_SEQUENCE,
    SEQ_EFTPS_SEGMENT_SEQUENCE,
    SEQ_SUBSCRIPTION_NUMBER,
    SEQ_TRANSACTION_NUMBER,
    SEQ_ACH_FILE_CTR,
    SEQ_ASST_USAGE_BILLING_TOKEN,
    SEQ_EE_CALCULATION_TOKEN,
    SEQ_USAGE_BILLING_TOKEN,
    SEQ_401K_SIGNUP_BATCH_ID,
    SEQ_401K_UPLOAD_BATCH_ID,
    SEQ_TXN_TOKEN_NBR,
    SEQ_TRACE_NBR;

    public String getName(){
        return this.toString().toLowerCase();
    }
}
