--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL


alter session set current_schema=pspadm;

Prompt Column COMPANY_FK;
ALTER TABLE PSP_PAYCHECK_USAGE_HIST
 ADD (COMPANY_FK  VARCHAR2(255 CHAR));
Prompt Column COMPANY_FK;
ALTER TABLE PSP_PSTUB_MSG
 ADD (COMPANY_FK  VARCHAR2(255 CHAR));
Prompt Column COMPANY_FK;
ALTER TABLE PSP_PSTUB_DDITEM
 ADD (COMPANY_FK  VARCHAR2(255 CHAR));
Prompt Column COMPANY_FK;
ALTER TABLE PSP_PSTUB_PAID_TIMEOFF_ITEM
 ADD (COMPANY_FK  VARCHAR2(255 CHAR));

PROMPT finished DBUpgrade_002.023.002.001.sql
