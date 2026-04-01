--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column PAYCHECK_VERSION;

ALTER TABLE PSP_WC_PAYCHECK
ADD PAYCHECK_VERSION  NUMBER(19) default 1 NOT NULL;
PROMPT finished DBUpgrade_002.013.008.004.sql