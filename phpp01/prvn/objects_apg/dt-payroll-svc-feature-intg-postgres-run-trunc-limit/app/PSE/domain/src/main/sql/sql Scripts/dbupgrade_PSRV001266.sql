-- TEAMTRACK NUM: PSRV001266
-- CREATED  DATE: 07.02.2009
-- MODIFIED DATE: 07.14.2009
-- AUTHOR       : KP
--
-- PURPOSE: 
--   Populating the new OriginalSettlementDate field in the FinancialTransaction table with SettlementDate.
--   (the main goal here is to ensure OriginalSettlementDate is never null by backfilling it with SettlementDate)
--
-- LOGON AS : PSPADM


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF


SPOOL dbupgrade_PSRV001266.log

SELECT 'User = ' || USER
  FROM DUAL;
  
SELECT 'Start Time = ' || TO_CHAR (SYSDATE, 'MM.DD.YYYY HH24:MI')
  FROM DUAL;

PROMPT .
PROMPT Total number of records in PSP_FINANCIAL_TRANSACTION with null ORIGINAL_SETTLEMENT_DATE (before update)

SELECT COUNT (*)
  FROM psp_financial_transaction
 WHERE original_settlement_date IS NULL;

--

PROMPT .
PROMPT Update PSP_FINANCIAL_TRANSACTION.original_settlement_date 20k at a time ...

DECLARE
   v_count           PLS_INTEGER := 0;
   v_iteration_cnt   PLS_INTEGER := 0;
BEGIN
   -- update field 20k at a time.

   LOOP
UPDATE psp_financial_transaction
   SET original_settlement_date = settlement_date
       WHERE original_settlement_date IS NULL AND ROWNUM < 20001;

      v_count := v_count + SQL%ROWCOUNT;

      EXIT WHEN SQL%ROWCOUNT = 0;

      COMMIT;

      v_iteration_cnt := v_iteration_cnt + 1;
   END LOOP;

   COMMIT;

   DBMS_OUTPUT.put_line ('Total rows updated : ' || v_count);
   DBMS_OUTPUT.put_line ('Total update iterations : ' || v_iteration_cnt);
EXCEPTION
   WHEN OTHERS THEN DBMS_OUTPUT.put_line (SQLERRM);
END;
/

SHOW ERRORS

--

PROMPT .
PROMPT Total number of records in PSP_FINANCIAL_TRANSACTION with null ORIGINAL_SETTLEMENT_DATE (after update)

SELECT COUNT (*)
  FROM psp_financial_transaction
 WHERE original_settlement_date IS NULL;

SELECT 'End Time = ' || TO_CHAR (SYSDATE, 'MM.DD.YYYY HH24:MI')
  FROM DUAL;

PROMPT .
PROMPT Done.

SPOOL OFF