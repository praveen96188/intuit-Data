--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column OFFER_RESTRICTION;
ALTER TABLE PSP_OFFER
 ADD (OFFER_RESTRICTION  VARCHAR2(255 CHAR));

-- added to pre deploy script
/* Prompt Column PROCESS_TRANSMISSIONS;
ALTER TABLE PSP_QUICKBOOKS_INFO
 ADD (PROCESS_TRANSMISSIONS  NUMBER(1)); */

PROMPT finishedDBUpgrade_002.001.001.011.sql