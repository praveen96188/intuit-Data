--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column HAS_INVALID_EMAIL;
ALTER TABLE PSP_INDIVIDUAL
 ADD (HAS_INVALID_EMAIL  NUMBER(1) default 0 NOT NULL);

Prompt Column HAS_INVALID_EMAIL;
ALTER TABLE PSP_PAYEE
 ADD (HAS_INVALID_EMAIL  NUMBER(1) default 0 NOT NULL);

PROMPT finishedDBUpgrade_002.013.006.002.sql