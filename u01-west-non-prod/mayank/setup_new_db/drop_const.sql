spool 04_DROP_CONSTRAINTS.SQL
set echo off
set feed off
set head off
set linesize 160
set pagesize 0

select 'alter table ' || a.owner || '.' || a.table_name ||
       ' drop constraint ' || a.constraint_name || ';'
  from dba_constraints a
  where  owner in ('PSPADM')
  and a.constraint_type in ('P', 'R')
  order by a.constraint_type desc, a.table_name, a.constraint_type;

spool off

spool 04_drop_constraints

set echo on
set feed on
set head on

@04_DROP_CONSTRAINTS.SQL

spool off
exit


