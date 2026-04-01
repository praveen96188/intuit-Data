--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 


Prompt Column ACCOUNT_LOCKED_UNTIL;
ALTER TABLE PSP_AUTH_USER
 ADD (ACCOUNT_LOCKED_UNTIL  TIMESTAMP(6));

Prompt Column NBR_OF_FAILED_LOGIN_ATTEMPTS;
ALTER TABLE PSP_AUTH_USER
ADD (NBR_OF_FAILED_LOGIN_ATTEMPTS  NUMBER(10) default 0 not null);


PROMPT finished DBUpgrade_002.013.007.001.sql