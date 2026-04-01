--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column CATEGORY;
ALTER TABLE PSP_PAYMENT_TEMPLATE
 ADD (CATEGORY  VARCHAR2(255 CHAR));

Prompt Column REQUIRES_QUARTER_LAW;
ALTER TABLE PSP_LEDGER_ACCOUNT
 ADD (REQUIRES_QUARTER_LAW  NUMBER(1));

Prompt Column ACCOUNT_ABBREVIATION;
ALTER TABLE PSP_LEDGER_ACCOUNT
 ADD (ACCOUNT_ABBREVIATION  VARCHAR2(20 CHAR));


PROMPT finishedDBUpgrade_002.000.013.000.sql