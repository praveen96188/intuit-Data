set pagesize 10000
set linesize 132
--set underline off
--set heading off
--set wrap off

spool ft_mmt_check.log
SELECT SUM ("FTSum"), SUM ("MMTAmount")
  FROM (  SELECT SUM (
                    ft.financial_transaction_amount
                    * (  DECODE (ft.transaction_type_fk, 'AgencyTaxCredit', 1, 0)
                       + DECODE (ft.transaction_type_fk, 'AgencyDirectCredit', 1, 0)
                       + DECODE (ft.transaction_type_fk, 'AgencyTaxDebit', -1, 0)
                       + DECODE (ft.transaction_type_fk, 'AgencyTaxOverpaymentApplied', -1, 0))) AS "FTSum",
                 mmt.mm_transaction_amount AS "MMTAmount",
                 mmt.money_movement_transaction_seq
            FROM pspadm.psp_financial_transaction ft, pspadm.psp_money_movement_transaction mmt
           WHERE ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
             AND ft.transaction_type_fk IN ('AgencyTaxCredit', 'AgencyDirectCredit', 'AgencyTaxDebit', 'AgencyTaxOverpaymentApplied')
             AND mmt.status = 'Created'
             AND mmt.tax_payment_status = 'ReadyToSend'
             AND mmt.money_movement_payment_method IN ('EFTPS', 'EFTPSDirectDebit')
             AND mmt.initiation_date BETWEEN TO_TIMESTAMP (TO_CHAR (SYSDATE, 'yyyymmdd') || '000000', 'yyyymmddhh24miss')
                                         AND TO_TIMESTAMP (TO_CHAR (SYSDATE, 'yyyymmdd') || '235959', 'yyyymmddhh24miss')
        GROUP BY money_movement_transaction_seq, mm_transaction_amount);
spool off
