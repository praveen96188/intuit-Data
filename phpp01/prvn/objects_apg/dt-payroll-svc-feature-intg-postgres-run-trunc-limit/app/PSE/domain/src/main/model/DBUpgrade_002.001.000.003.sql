--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

/* 
Prompt COLUMN SUBSCRIPTION_END_DATE;
ALTER TABLE PSP_ENTITLEMENT
 ADD (SUBSCRIPTION_END_DATE  TIMESTAMP(6));

Prompt COLUMN VERSION;
ALTER TABLE PSP_AUTH_USER
MODIFY(VERSION  DEFAULT 0);

Prompt COLUMN VERSION;
ALTER TABLE PSP_COMPANY
MODIFY(VERSION  DEFAULT 0);

Prompt COLUMN ERROR_COUNT;
ALTER TABLE PSP_ENTITLEMENT_UNIT
 ADD (ERROR_COUNT  NUMBER(19));

UPDATE PSP_ENTITLEMENT_UNIT
SET ERROR_COUNT = 0;
*/

PROMPT finishedDBUpgrade_002.001.000.003.sql