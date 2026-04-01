--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column CONSUMER_REALM_ID;
ALTER TABLE PSP_EMPLOYEE
 ADD (CONSUMER_REALM_ID  VARCHAR2(50 CHAR));

PROMPT finishedDBUpgrade_002.013.001.002.sql