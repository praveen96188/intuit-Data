spool chk_account
set echo on
set feed on
col username format a40
select username, account_status from dba_users where (username like 'QBO%' or username like '%UGT%')
and username not in ('QBO','QBO_DATA') order by 1;

spool off

