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

Prompt Column VERSION;
ALTER TABLE PSP_COMPANY
MODIFY(VERSION  DEFAULT 0);

Prompt Column VERSION;
ALTER TABLE PSP_ENTITLEMENT_UNIT
MODIFY(VERSION  DEFAULT 0);

Prompt Column VERSION;
ALTER TABLE PSP_QUICKBOOKS_INFO
MODIFY(VERSION  DEFAULT 0);

Prompt Column AVAILABLE;
ALTER TABLE PSP_PSTUB_PAID_TIMEOFF_ITEM
MODIFY(AVAILABLE VARCHAR2(30 CHAR));


Prompt Column Y_T_D_USED;
ALTER TABLE PSP_PSTUB_PAID_TIMEOFF_ITEM
MODIFY(Y_T_D_USED VARCHAR2(30 CHAR));

PROMPT finishedDBUpgrade_002.013.004.002.sql