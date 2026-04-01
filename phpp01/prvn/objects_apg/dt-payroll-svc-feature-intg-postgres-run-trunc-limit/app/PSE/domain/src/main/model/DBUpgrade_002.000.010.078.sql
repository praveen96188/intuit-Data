--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;


Prompt Column OLD_EIN;
ALTER TABLE PSP_ENTITY_CHANGE
 ADD (OLD_EIN  VARCHAR2(80 CHAR));

Prompt Column NEW_EIN;
ALTER TABLE PSP_ENTITY_CHANGE
 ADD (NEW_EIN  VARCHAR2(80 CHAR));

Prompt Column AGENT_ID;
ALTER TABLE PSP_ENTITY_CHANGE
 ADD (AGENT_ID  VARCHAR2(4000 CHAR));

Prompt Column E_I_N;
ALTER TABLE PSP_ENTITY_CHANGE DROP COLUMN E_I_N;

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;


 
PROMPT finishedDBUpgrade_002.000.010.078.sql