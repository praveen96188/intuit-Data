--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column S_S_N_ENC;
ALTER TABLE PSP_TAX_CREDITS9061
 ADD (S_S_N_ENC  VARCHAR2(4000 CHAR));

Prompt Column TAX_ID_ENC;
ALTER TABLE PSP_PAYEE
 ADD (TAX_ID_ENC  VARCHAR2(4000 CHAR));

PROMPT finished DBUpgrade_002.018.009.001.sql