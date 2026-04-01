--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
Prompt Column AUTHORIZATION_TOKEN;
ALTER TABLE PSP_AUTH_USER
 ADD (AUTHORIZATION_TOKEN  VARCHAR2(50 CHAR));

Prompt Column LAST_REMOTE_CALL_TIMESTAMP;
ALTER TABLE PSP_AUTH_USER
 ADD (LAST_REMOTE_CALL_TIMESTAMP  TIMESTAMP(6));

select 'finished DBUpgradeFrom_1.2.0.0_To_1.3.0.0.sql ' || to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') from dual