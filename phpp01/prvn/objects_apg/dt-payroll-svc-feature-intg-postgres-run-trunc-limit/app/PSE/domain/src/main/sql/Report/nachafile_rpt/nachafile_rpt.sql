set linesize 200
set pages 80
set heading on
set feedback off
 
COL "File Type" FORMAT  a10 
COL "Offload Date" FORMAT  a10 
COL "Credit Amount" FORMAT $999,999,999.00
COL "Debit Amount"  FORMAT $999,999,999.00
spool nachafile_rpt.log
select sysdate from dual;

SELECT   file_type "File Type",
         SUBSTR (created_date - 1, 1, 30) "Offload Date",
         credit_txn_total_amount "Credit Amount",
         debit_txn_total_amount "Debit Amount",
         SUBSTR ((transmission_date - created_date), INSTR ((transmission_date - created_date), ' ') + 1, 2) ||':'||
         SUBSTR ((transmission_date - created_date), INSTR ((transmission_date - created_date), ' ') + 4, 2) ||':'||
         SUBSTR ((transmission_date - created_date), INSTR ((transmission_date - created_date), ' ') + 7, 2) "Elapsed Time"
    FROM pspadm.psp_nachafile
   WHERE created_date BETWEEN SYS_EXTRACT_UTC(to_timestamp(TRUNC(sysdate-10, 'MM')))
                          AND SYS_EXTRACT_UTC(to_timestamp(last_day(sysdate-10)))
        AND file_type='PPD'
ORDER BY created_date, file_type;

spool off
