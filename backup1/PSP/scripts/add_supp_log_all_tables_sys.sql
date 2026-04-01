spool ADD_SUPP_LOG_ALL_TABLES_SYS.SQL
set echo off
set feed off
set head off
set linesize 160
set pagesize 0

select 'alter table ' || owner || '.' || TABLE_NAME || ' add supplemental log data (all) columns;'
from DBA_TABLES
where owner = 'PSPADM'
order by TABLE_NAME;

spool off

spool add_supp_log_all_tables_sys

set echo on
set feed on
set head on

@ADD_SUPP_LOG_ALL_TABLES_SYS.SQL

spool off
exit

