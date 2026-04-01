--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;

Prompt Column ACCOUNT_NUMBER;
ALTER TABLE PSP_PAYEE
 ADD (ACCOUNT_NUMBER  VARCHAR2(150 CHAR));

Prompt Column REFERENCE_NUMBER;
ALTER TABLE PSP_BILL_PAYMENT
 ADD (REFERENCE_NUMBER  VARCHAR2(50 CHAR));

Prompt Column MEMO;
ALTER TABLE PSP_BILL_PAYMENT
 ADD (MEMO  VARCHAR2(4000 CHAR));

Prompt Column TRANSACTION_TYPE;
ALTER TABLE PSP_BILL_PAYMENT
 ADD (TRANSACTION_TYPE  VARCHAR2(255 CHAR));

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;


 
PROMPT finishedDBUpgrade_002.000.010.079.sql