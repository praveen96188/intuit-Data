--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column CLOSED;
ALTER TABLE PSP_BILL
 ADD (CLOSED  NUMBER(1));

Prompt Column SUBSCRIPTION_START_DATE;
ALTER TABLE PSP_ENTITLEMENT
 ADD (SUBSCRIPTION_START_DATE  TIMESTAMP(6));

Prompt Column BILLING_DAY_OF_MONTH;
ALTER TABLE PSP_ENTITLEMENT
 ADD (BILLING_DAY_OF_MONTH  NUMBER(10));

PROMPT finishedDBUpgrade_002.001.001.008.sql