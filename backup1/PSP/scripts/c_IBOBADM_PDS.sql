set lines 300 head off echo off feedback off
spool chk_cnt.sql
select 'select count(*) from IBOBADM_PDS.'||table_name||';' from dba_tables where owner='IBOBADM_PDS' order by table_name;
spool off
set echo on feed on
@chk_cnt


