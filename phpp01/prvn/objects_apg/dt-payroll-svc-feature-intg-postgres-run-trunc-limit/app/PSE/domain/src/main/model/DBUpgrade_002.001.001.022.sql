--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column SYSTEM_PARAMETER_VALUE;
ALTER TABLE PSP_SYSTEM_PARAMETER
MODIFY(SYSTEM_PARAMETER_VALUE VARCHAR2(500 CHAR));

Prompt Column E_E_CALCULATION_TOKEN;
ALTER TABLE PSP_QBDT_PAYROLL_TRANSACTION
 ADD (E_E_CALCULATION_TOKEN  NUMBER(19));

PROMPT finishedDBUpgrade_002.001.001.022.sql