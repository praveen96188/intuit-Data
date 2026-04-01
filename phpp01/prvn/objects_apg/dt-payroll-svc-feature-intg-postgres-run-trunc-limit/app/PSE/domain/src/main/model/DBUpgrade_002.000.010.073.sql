--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;

Prompt Column VERSION;
ALTER TABLE PSP_COMPANY
MODIFY(VERSION  DEFAULT 0);

Prompt Column VERSION;
ALTER TABLE PSP_AUTH_USER
MODIFY(VERSION  DEFAULT 0);

Prompt Column SAME_DAY_ACK_NUMBER;
ALTER TABLE PSP_EFTPS_PAYMENT_DETAIL
 ADD (SAME_DAY_ACK_NUMBER  VARCHAR2(4000 CHAR));

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;

 
PROMPT finishedDBUpgrade_002.000.010.073.sql