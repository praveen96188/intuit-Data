set lines 300 head off echo off feedback off
spool chk_cnt.sql
select 'select count(*) from IBOBADM_PDS_APG.'||table_name||';' from dba_tables where owner='IBOBADM_PDS_APG' order by table_name;
spool off
set echo on feed on
@chk_cnt


