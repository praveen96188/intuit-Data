--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

Prompt Column EMPLOYEE_PRINT_NAME;
ALTER TABLE PSP_CHECK_PRINT_PAYCHECK
 ADD (EMPLOYEE_PRINT_NAME  VARCHAR2(50 CHAR));

Prompt Column EMPLOYEE_FIRST_NAME;
ALTER TABLE PSP_CHECK_PRINT_PAYCHECK DROP COLUMN EMPLOYEE_FIRST_NAME;

Prompt Column EMPLOYEE_LAST_NAME;
ALTER TABLE PSP_CHECK_PRINT_PAYCHECK DROP COLUMN EMPLOYEE_LAST_NAME;


PROMPT finishedDBUpgrade_002.000.000.047.sql