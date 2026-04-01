set head off
set pagesize 0
set feed off
set verify off
set time off
set timi off

col uname for a35
spool user_status_before
select username uname, account_status
  from dba_users
 where 
 	ACCOUNT_STATUS ='OPEN' and PROFILE in ('INDIVIDUAL_PROFILE', 'APPLICATION_HIGH_RISK_PROFILE','GENERIC_APP_PROFILE','SYSTEM_MONTORING_PROFILE')
-- username like 'QBO%_UW%'
order by 2;
spool off

spool LOCK_OPEN_ACCOUNT.SQL
select 'spool APP_ACCOUNT_LOCK' from dual;
select 'set feed on' from dual;
select 'set echo on' from dual;
select 'alter user ' || username || ' account lock;'
  from dba_users
  where 
  ACCOUNT_STATUS ='OPEN' and PROFILE in ('INDIVIDUAL_PROFILE', 'APPLICATION_HIGH_RISK_PROFILE','GENERIC_APP_PROFILE','SYSTEM_MONTORING_PROFILE')
--  username like 'PSP%'
  order by 1;

select 'spool off' from dual;
spool off

@LOCK_OPEN_ACCOUNT.SQL

spool user_status_after
select username uname, account_status
  from dba_users
 where 
 ACCOUNT_STATUS ='OPEN' and PROFILE in ('INDIVIDUAL_PROFILE', 'APPLICATION_HIGH_RISK_PROFILE','GENERIC_APP_PROFILE','SYSTEM_MONTORING_PROFILE')
-- username like 'PSP%'
order by 2;
spool off
exit

