--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column RATE_TYPE;
ALTER TABLE PSP_COMPANY_LAW_RATE
 ADD (RATE_TYPE  VARCHAR2(255 CHAR));

ALTER TABLE PSP_COMPANY_LAW_RATE
 ADD CONSTRAINT C_PSP_COMPANY_LAW_RATE0
  CHECK (RATE_TYPE IN('MoneyType', 'Percentage'));

PROMPT finished DBUpgrade_002.013.010.001.sql