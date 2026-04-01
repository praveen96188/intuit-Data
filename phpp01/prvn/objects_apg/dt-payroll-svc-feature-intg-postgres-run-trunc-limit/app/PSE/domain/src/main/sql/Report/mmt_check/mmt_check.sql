set pagesize 10000
set linesize 132
--set underline off
--set heading off
--set wrap off
col Type for a20

spool mmt_check.log
SELECT TRUNC (initiation_date) AS "Init Date",
         COUNT (initiation_date) AS "Txn Count",
         money_movement_payment_method AS "Type"
    FROM pspadm.psp_money_movement_transaction
   WHERE     tax_payment_status = 'ReadyToSend'
         AND status = 'Created'
         AND initiation_date >= TRUNC (SYSDATE)
         AND money_movement_payment_method IN ('EFTPS', 'EFTPSDirectDebit')
GROUP BY TRUNC (initiation_date), money_movement_payment_method
ORDER BY TRUNC (initiation_date), money_movement_payment_method;
spool off
