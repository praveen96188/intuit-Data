spool TABLE_SANITY_COUNT.SQL
set echo off
set feed off
set head off
set linesize 160
set pagesize 0

select 'select /*+ parallel(8) */count(*) from ' || a.owner || '.' || a.table_name || ';'
from DBA_TABLES a
where  owner in ('PSPADM')
order by a.table_name;

spool off

spool table_sanity_count

set echo on
set feed off
set head off

@TABLE_SANITY_COUNT.SQL

spool off
exit


