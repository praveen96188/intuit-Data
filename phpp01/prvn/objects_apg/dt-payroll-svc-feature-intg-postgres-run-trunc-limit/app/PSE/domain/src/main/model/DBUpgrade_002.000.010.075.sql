--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;

Prompt COLUMN INVALID_DATE;
ALTER TABLE PSP_EFFECTIVE_DEPOSIT_FREQ
 ADD (INVALID_DATE  TIMESTAMP(6));

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;


PROMPT finishedDBUpgrade_002.000.010.075.sql