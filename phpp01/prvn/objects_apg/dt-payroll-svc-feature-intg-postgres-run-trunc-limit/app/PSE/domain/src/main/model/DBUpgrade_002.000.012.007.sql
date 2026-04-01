--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column EMPLOYEE_NAME;
ALTER TABLE PSP_QBDT_PAYROLL_TRANSACTION
 ADD (EMPLOYEE_NAME  VARCHAR2(255 CHAR));

PROMPT finishedDBUpgrade_002.000.012.007.sql