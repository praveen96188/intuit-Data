--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

Prompt COLUMN USE_D_D;
ALTER TABLE PSP_QBDT_EMPLOYEE_INFO
 ADD (USE_D_D  NUMBER(1));

Prompt COLUMN PRINT_AS_NAME;
ALTER TABLE PSP_QBDT_EMPLOYEE_INFO
 ADD (PRINT_AS_NAME  VARCHAR2(50 CHAR));


PROMPT finishedDBUpgrade_002.000.012.002.sql