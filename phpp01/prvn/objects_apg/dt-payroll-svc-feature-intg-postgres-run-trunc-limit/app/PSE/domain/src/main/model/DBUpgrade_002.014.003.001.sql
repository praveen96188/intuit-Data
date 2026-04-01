--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column STATE_TAX_FILING_STATUS;
ALTER TABLE PSP_PSTUB_EMPLOYEE_INFO
MODIFY(STATE_TAX_FILING_STATUS VARCHAR2(63 CHAR));

PROMPT finished DBUpgrade_002.014.003.001.sql