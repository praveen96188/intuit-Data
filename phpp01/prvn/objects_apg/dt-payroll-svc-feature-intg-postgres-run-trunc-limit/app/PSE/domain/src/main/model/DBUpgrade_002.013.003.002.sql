--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column APPLICATION_VERSION;
ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 ADD (APPLICATION_VERSION  VARCHAR2(100 CHAR));

Prompt Column APPLICATION_ID;
ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 ADD (APPLICATION_ID  VARCHAR2(100 CHAR));

Prompt Column TAX_TABLE_ID;
ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 ADD (TAX_TABLE_ID  VARCHAR2(100 CHAR));

PROMPT finishedDBUpgrade_002.013.003.002.sql