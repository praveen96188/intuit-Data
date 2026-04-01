--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 

Prompt COLUMN ITEM_ORDER;
ALTER TABLE PSP_EMPLOYEE_PAYROLL_ITEM
 ADD (ITEM_ORDER  NUMBER(10));

Prompt COLUMN FIELD_ORDER;
ALTER TABLE PSP_EMPLOYEE_CUSTOM_FIELD
 ADD (FIELD_ORDER  NUMBER(10));

Prompt COLUMN ACCOUNT_ORDER;
ALTER TABLE PSP_EMPLOYEE_BANK_ACCOUNT
 ADD (ACCOUNT_ORDER  NUMBER(10));

Prompt COLUMN TAX_ORDER;
ALTER TABLE PSP_EMPLOYEE_TAX
 ADD (TAX_ORDER  NUMBER(10));


PROMPT finishedDBUpgrade_002.000.012.003.sql