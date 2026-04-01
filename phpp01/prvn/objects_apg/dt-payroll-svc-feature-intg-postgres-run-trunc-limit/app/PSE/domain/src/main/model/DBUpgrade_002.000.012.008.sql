--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column EXTRA_WITHHOLDING;
ALTER TABLE PSP_EMPLOYEE_TAX 
 RENAME COLUMN EXTRA_WITHHOLDING to EXTRA_WITHHOLDING_OLD;

ALTER TABLE PSP_EMPLOYEE_TAX 
 SET UNUSED (EXTRA_WITHHOLDING_OLD);

ALTER TABLE PSP_EMPLOYEE_TAX
 ADD(EXTRA_WITHHOLDING NUMBER(19,7));

Prompt Column EXTRA_WITHHOLDING_TYPE;
ALTER TABLE PSP_EMPLOYEE_TAX
 ADD (EXTRA_WITHHOLDING_TYPE  VARCHAR2(255 CHAR));

Prompt Column MISC_DATA_ORDER;
ALTER TABLE PSP_TAX_TABLE_MISC_DATA
 ADD (MISC_DATA_ORDER  NUMBER(10));

PROMPT finishedDBUpgrade_002.000.012.008.sql