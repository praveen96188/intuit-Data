--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
Prompt Column IS_OFFLDTXN_EVT_COMPLETE;

Prompt Column IS_FEE_EVENT_CREATION_COMPLETE;
ALTER TABLE PSP_OFFLOAD_BATCH RENAME COLUMN IS_FEE_EVENT_CREATION_COMPLETE TO IS_OFFLDTXN_EVT_COMPLETE;

 
PROMPT finishedDBUpgrade_002.000.000.051.sql