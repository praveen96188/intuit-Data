--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

Prompt Column CAN_BE_MANUALLY_CANCELLED;
ALTER TABLE PSP_SERVICE
 ADD (CAN_BE_MANUALLY_CANCELLED  NUMBER(1));

Prompt Column IS_CLOUD_EMPLOYEE;
ALTER TABLE PSP_EMPLOYEE
 ADD (IS_CLOUD_EMPLOYEE  NUMBER(1));

PROMPT finishedDBUpgrade_002.000.000.053.sql