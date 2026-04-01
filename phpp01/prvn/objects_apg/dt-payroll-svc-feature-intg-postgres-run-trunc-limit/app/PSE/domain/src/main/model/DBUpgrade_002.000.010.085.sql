--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

Prompt Column REFERENCE_NUMBER;
ALTER TABLE PSP_BILL_PAYMENT DROP COLUMN REFERENCE_NUMBER;

Prompt Column REFERENCE_NUMBER;
ALTER TABLE PSP_BILL_PAYMENT_SPLIT
 ADD (REFERENCE_NUMBER  VARCHAR2(50 CHAR));

 
PROMPT finishedDBUpgrade_002.000.010.085.sql