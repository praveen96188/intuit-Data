--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column VERSION;
ALTER TABLE PSP_AUTH_USER
MODIFY(VERSION  DEFAULT 0);

Prompt Column BILLING_PROFILE_ID;
ALTER TABLE PSP_ENTITLEMENT
 ADD (BILLING_PROFILE_ID  VARCHAR2(40 CHAR));

Prompt Column VERSION;
ALTER TABLE PSP_COMPANY
MODIFY(VERSION  DEFAULT 0);

Prompt Column VERSION;
ALTER TABLE PSP_ENTITLEMENT_UNIT
MODIFY(VERSION  DEFAULT 0);

PROMPT finishedDBUpgrade_002.001.001.023.sql