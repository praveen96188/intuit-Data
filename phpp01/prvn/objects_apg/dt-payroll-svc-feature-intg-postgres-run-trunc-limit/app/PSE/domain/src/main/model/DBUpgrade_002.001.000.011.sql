--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

/* Prompt COLUMN MESSAGE_TIMESTAMP;
ALTER TABLE PSP_ENTITLEMENT_MESSAGE
 ADD (MESSAGE_TIMESTAMP  TIMESTAMP(6));

Prompt COLUMN EXPIRATION_TIMESTAMP;
ALTER TABLE PSP_ENTITLEMENT_MESSAGE
 ADD (EXPIRATION_TIMESTAMP  TIMESTAMP(6)); */


PROMPT finishedDBUpgrade_002.001.000.011.sql