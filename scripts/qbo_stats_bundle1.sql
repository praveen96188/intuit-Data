
col spoolname new_value spoolname
select 'qbo_stats_bundle1_'||SYS_CONTEXT('USERENV', 'DB_NAME')||'_'||to_char(sysdate,'YYMONDD_HH24.MI.SS') spoolname from dual;
spool '&spoolname'

set timing on time on echo on
set serveroutput on 

PROMPT === Disable partition level stats ===
@disable_partition_stats.sql

prompt === Before fixing stats ===
@show_tx_col_stats

prompt === Unlocking table stats ===

exec DBMS_STATS.UNLOCK_TABLE_STATS('QBO_DATA', 'TXHEADERS_1');
exec DBMS_STATS.UNLOCK_TABLE_STATS('QBO_DATA', 'TXDETAILS_1');

prompt === Setting table prefs ===

exec DBMS_STATS.SET_TABLE_PREFS('QBO_DATA', 'TXHEADERS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id for columns size 1 TX_DATE');
exec DBMS_STATS.SET_TABLE_PREFS('QBO_DATA', 'TXDETAILS_1', 'METHOD_OPT', 'for all columns size auto for columns size 10 invoice_id deposit_id for columns size 1 TX_DATE');

prompt === Gathering stats ===

execute dbms_stats.gather_table_stats('QBO_DATA','TXHEADERS_1', degree=>4);
execute dbms_stats.gather_table_stats('QBO_DATA','TXDETAILS_1', degree=>4);

prompt === Fixing stats for tx_date ====

exec QBO_STATS_PKG.SET_DATE_RANGE('QBO_DATA', 'TXHEADERS_1', 'TX_DATE', to_date('2014-01-01', 'yyyy-mm-dd'), trunc(sysdate+30));
exec QBO_STATS_PKG.SET_DATE_RANGE('QBO_DATA', 'TXDETAILS_1', 'TX_DATE', to_date('2014-01-01', 'yyyy-mm-dd'), trunc(sysdate+30));

prompt === After fixing stats ===

@show_tx_col_stats

prompt === Locking stats ===

exec DBMS_STATS.LOCK_TABLE_STATS('QBO_DATA', 'TXHEADERS_1');
exec DBMS_STATS.LOCK_TABLE_STATS('QBO_DATA', 'TXDETAILS_1');

SELECT table_name, stattype_locked
FROM dba_tab_statistics 
WHERE owner = 'QBO_DATA'
and table_name IN ('TXHEADERS_1', 'TXDETAILS_1') 
and partition_name is null;

PROMPT === Lock stats on additional tables ===

@lock_table_stats_set2.sql

PROMPT === Delete partition level stats on tx tables ===
@delete_partition_stats.sql

spool off

exit
