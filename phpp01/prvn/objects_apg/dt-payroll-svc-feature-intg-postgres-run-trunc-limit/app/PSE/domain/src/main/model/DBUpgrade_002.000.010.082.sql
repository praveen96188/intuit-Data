--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

Prompt COLUMN F941_TAX_PERIOD;
ALTER TABLE PSP_RAFENROLLMENT_DETAIL
 ADD (F941_TAX_PERIOD  VARCHAR2(100 CHAR));

Prompt COLUMN F940_TAX_PERIOD;
ALTER TABLE PSP_RAFENROLLMENT_DETAIL
 ADD (F940_TAX_PERIOD  VARCHAR2(100 CHAR));

Prompt COLUMN F94X_F_T_D_PERIOD;
ALTER TABLE PSP_RAFENROLLMENT_DETAIL
 ADD (F94X_F_T_D_PERIOD  VARCHAR2(100 CHAR));

Prompt COLUMN YEAR;
ALTER TABLE PSP_RAFENROLLMENT_DETAIL DROP COLUMN YEAR;

Prompt COLUMN QUARTER;
ALTER TABLE PSP_RAFENROLLMENT_DETAIL DROP COLUMN QUARTER;


PROMPT finishedDBUpgrade_002.000.010.082.sql