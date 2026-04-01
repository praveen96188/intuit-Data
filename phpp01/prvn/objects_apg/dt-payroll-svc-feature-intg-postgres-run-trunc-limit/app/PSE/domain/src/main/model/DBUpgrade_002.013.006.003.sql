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

Prompt Column IS_ASSISTED;
ALTER TABLE PSP_QBDT_EMPLOYEE_INFO
 ADD (IS_ASSISTED  NUMBER(1) DEFAULT 0 NOT NULL);

Prompt Column IS_ASSISTED;
ALTER TABLE PSP_QBDT_PAYCHECK_INFO
 ADD (IS_ASSISTED  NUMBER(1) DEFAULT 0 NOT NULL);

PROMPT finished DBUpgrade_002.013.006.003.sql