spool 05_DISABLE_TRIGGERS.SQL
set echo off
set feed off
set head off

select 'alter trigger ' || owner || '.' || trigger_name || ' disable;'
  from dba_triggers
  where owner in ('PSPADM')
  and status = 'ENABLED'
  order by 1;

spool off

spool 05_disable_triggers

set echo on
set feed on
set head on

@05_DISABLE_TRIGGERS.SQL

spool off
exit


