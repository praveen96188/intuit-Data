--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

Prompt Column PAYCHECK_DATE;
ALTER TABLE PSP_CHECK_PRINT_BATCH
 ADD (PAYCHECK_DATE  TIMESTAMP(6));


PROMPT finishedDBUpgrade_002.000.000.045.sql