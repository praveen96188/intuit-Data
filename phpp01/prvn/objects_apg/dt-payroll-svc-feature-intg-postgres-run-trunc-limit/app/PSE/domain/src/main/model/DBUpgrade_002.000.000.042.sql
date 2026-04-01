--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
Prompt Column TP401K_IS_TOK_ACCEPTED;
ALTER TABLE PSP_PAYROLL_ITEM
 ADD (TP401K_IS_TOK_ACCEPTED  NUMBER(1));

Prompt Column TP401K_ALLOWS_NEGATIVE_AMOUNTS;
ALTER TABLE PSP_PAYROLL_ITEM
 ADD (TP401K_ALLOWS_NEGATIVE_AMOUNTS  NUMBER(1));

Prompt Column HAS_SAFE_HARBOR;
ALTER TABLE PSP_TP401KCOMPANY_SERVICE_INFO
 ADD (HAS_SAFE_HARBOR  NUMBER(1));

PROMPT finishedDBUpgrade_002.000.000.042.sql