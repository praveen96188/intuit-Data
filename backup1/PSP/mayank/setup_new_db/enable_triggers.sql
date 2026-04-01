spool ENABLE_TRIGGERS.SQL
set echo off
set feed off
set head off

select 'alter trigger ' || owner || '.' || trigger_name || ' enable;'
  from dba_triggers
  where owner in ('PSPADM')
  and status = 'DISABLED'
  order by 1;

spool off

spool enable_triggers

set echo on
set feed on
set head on

@ENABLE_TRIGGERS.SQL

spool off
exit


