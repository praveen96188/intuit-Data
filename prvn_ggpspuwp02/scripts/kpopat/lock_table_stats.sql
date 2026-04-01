col spoolname new_value spoolname
select 'lock_table_stats_'||SYS_CONTEXT('USERENV', 'DB_NAME')||'_'||to_char(sysdate,'YYMONDD_HH24.MI.SS') spoolname from dual;
spool '&spoolname' 

col table_name format a40
col stattype_locked format a20

PROMPT === Before change ===

SELECT table_name, stattype_locked
FROM dba_tab_statistics 
WHERE owner = 'QBO_DATA'
and table_name IN ('TXHEADERS_1', 'TXDETAILS_1') 
and partition_name is null;

exec DBMS_STATS.LOCK_TABLE_STATS('QBO_DATA', 'TXHEADERS_1');
exec DBMS_STATS.LOCK_TABLE_STATS('QBO_DATA', 'TXDETAILS_1');

PROMPT === After change ===

SELECT table_name, stattype_locked
FROM dba_tab_statistics 
WHERE owner = 'QBO_DATA'
and table_name IN ('TXHEADERS_1', 'TXDETAILS_1') 
and partition_name is null;

-- rollback
-- exec DBMS_STATS.UNLOCK_TABLE_STATS('QBO_DATA', 'TXHEADERS_1');
-- exec DBMS_STATS.UNLOCK_TABLE_STATS('QBO_DATA', 'TXDETAILS_1');

spool off
