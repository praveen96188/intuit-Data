--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column SYMPHONY_ON_BOARD_VERSION;
ALTER TABLE PSP_QUICKBOOKS_INFO
ADD SYMPHONY_ON_BOARD_VERSION VARCHAR2(100 CHAR);

PROMPT finishedDBUpgrade_002.001.001.021.sql