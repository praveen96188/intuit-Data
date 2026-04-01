--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

/* Prompt COLUMN EVENT_REASON;
ALTER TABLE PSP_ENTITLEMENT_MESSAGE
 ADD (EVENT_REASON  VARCHAR2(50 CHAR));

Prompt COLUMN FAILURE_COUNT;
ALTER TABLE PSP_ENTITLEMENT_MESSAGE
 ADD (FAILURE_COUNT  NUMBER(19));

Prompt COLUMN LAST_FAILURE_MESSAGE;
ALTER TABLE PSP_ENTITLEMENT_MESSAGE
 ADD (LAST_FAILURE_MESSAGE  VARCHAR2(1000 CHAR)); */

PROMPT finishedDBUpgrade_002.001.000.010.sql