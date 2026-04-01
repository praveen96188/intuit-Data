--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
Prompt Column PSP_PROVIDES_CUSTOMER_SERVICE;
ALTER TABLE PSP_SERVICE
 ADD (PSP_PROVIDES_CUSTOMER_SERVICE  NUMBER(1));

PROMPT finishedDBUpgrade_002.000.000.059.sql