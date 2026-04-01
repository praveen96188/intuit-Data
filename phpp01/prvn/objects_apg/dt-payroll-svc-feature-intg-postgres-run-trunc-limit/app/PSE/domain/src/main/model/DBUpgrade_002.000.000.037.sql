--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

Prompt Column TP_401K_INFO_IS_HCE;
ALTER TABLE PSP_EMPLOYEE
 ADD (TP_401K_INFO_IS_HCE  NUMBER(1));

Prompt Column TP_401K_INFO_OWNER_PERCENT;
ALTER TABLE PSP_EMPLOYEE
 ADD (TP_401K_INFO_OWNER_PERCENT  NUMBER(19,7));

Prompt Column TP_401K_INFO_IS_FAMILY_MEMBER;
ALTER TABLE PSP_EMPLOYEE
 ADD (TP_401K_INFO_IS_FAMILY_MEMBER  NUMBER(1));




 
PROMPT finishedDBUpgrade_002.000.000.037.sql