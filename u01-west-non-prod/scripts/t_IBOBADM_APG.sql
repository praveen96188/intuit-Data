set lines 300 head off echo off feedback off
spool trunc_ibobadm_apg_tab.sql
select 'truncate table IBOBADM_APG.'||table_name||';' from dba_tables where owner='IBOBADM_APG' order by table_name;
spool off
set echo on feed on
@trunc_ibobadm_apg_tab

