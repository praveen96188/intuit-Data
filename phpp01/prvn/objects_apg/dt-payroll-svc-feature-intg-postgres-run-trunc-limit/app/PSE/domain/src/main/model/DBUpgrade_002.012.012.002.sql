--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column OFFERING_SERVICE_CHARGE_TYPE;
ALTER TABLE PSP_SERV_STAT_TXN_SKU_TYPE
 ADD (OFFERING_SERVICE_CHARGE_TYPE  VARCHAR2(255 CHAR));

PROMPT finishedDBUpgrade_002.012.012.002.sql