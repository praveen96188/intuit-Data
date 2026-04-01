set pages 5000
set lines 152
set trimspool on
set colsep '|'
--set feedback off
set underline off
col username for a15
col profile for a29
--col account_type for a20
col db_link for a30
col host for a30
col privilege for a30
col OWNER.TABLE_NAME for a40
col ACCOUNT_TYPE for a29
col ACCOUNT_STATUS for a16
col GRANTED_ROLE for a25
col role for a24

alter session set nls_date_format='mm/dd/yyyy hh24:mi:ss';

--Query 1 - 
spool pspprd_upd_user_&&date\.log
select u.username from intuit_users G, dba_users u, (select username,max(timestamp) last_logon from dba_audit_session group by username) l where u.username = g.user_name (+) and u.username = l.username (+) and g.group_name is null and u.profile='PROD_SOX_PROFILE' order by u.username;
spool off
    
--Query 2 - 
spool pspprd_upd_app_&&date\.log
select u.username from intuit_users G, dba_users u, (select username,max(timestamp) last_logon from dba_audit_session group by username) l where u.username = g.user_name (+) and u.username = l.username (+) and g.group_name is null and u.profile!='PROD_SOX_PROFILE' order by u.username;
spool off

--Query 3 - 
spool pspprd_deleteusers_&&date\.log
Delete intuit_users where user_name not in (select username from dba_users);
spool off

--Query 4 - 
spool pspprd_intuitusers_&&date\.log
select a.instance_name , b.* from v$instance a, intuit_users b order by group_name, sub_group_name;
spool off
    
--Query 5 - 
spool pspprd_users_&&date\.log
select 'Intuit' source, u.username,
'Profiles' Grant_type,g.group_name||decode(sub_group_name,null,null,'/'||sub_group_name) account_type,
account_status,profile,last_logon,sysdate -1 extract_date
from intuit_users G, dba_users u, (select username,max(timestamp) last_logon from dba_audit_session group by username) l 
where  u.username = g.user_name (+) and u.username = l.username (+) 
Union 
select 'Intuit' source, u.username,
'Roles' Grant_type,g.group_name||decode(sub_group_name,null,null,'/'||sub_group_name) account_type, 
account_status,rp.granted_role,last_logon,sysdate -1 extract_date 
from intuit_users G,dba_users u,dba_role_privs rp,
(select username,max(timestamp) last_logon from dba_audit_session group by username) l 
where u.username = g.user_name (+) and g.group_name like 'U%' and u.username = l.username (+) and u.username = rp.grantee 
Union 
select 'Intuit' source,u.username,
'Table Privs' Grant_type,g.group_name||decode(sub_group_name,null,null,'/'||sub_group_name) account_type, 
account_status,tp.privilege||' on '||owner||'.'||table_name,last_logon,sysdate -1 extract_date from intuit_users G,
dba_users u,dba_tab_privs tp,(select username,max(timestamp) last_logon from dba_audit_session group by username) l 
where u.username = g.user_name (+) and g.group_name like 'U%' and u.username = l.username (+) and u.username = tp.grantee 
Union 
select 'Intuit' source,u.username,
'Sys Privs' Grant_type,g.group_name||decode(sub_group_name,null,null,'/'||sub_group_name) account_type, 
account_status,sp.privilege,last_logon,sysdate -1 extract_date 
from intuit_users G,dba_users u,dba_sys_privs sp,
(select username,max(timestamp) last_logon from dba_audit_session group by username) l 
where  u.username = g.user_name (+) and u.username = l.username (+) 
and g.group_name like 'U%' and u.username = sp.grantee order by 2;
spool off
    
--Query 6 - 
spool pspprd_roles_&&date\.log
select role, null"OWNER.TABLE_NAME", null privilege, null GRANTED_ROLE from dba_roles 
Union 
select grantee role, owner||'.'||table_name "OWNER.TABLE_NAME", privilege, null GRANTED_ROLE from dba_tab_privs where grantee in (select role from dba_roles) 
Union 
select grantee role, null "OWNER.TABLE_NAME", privilege, null GRANTED_ROLE from dba_sys_privs where grantee in (select role from dba_roles) 
Union 
select grantee role, null "OWNER.TABLE_NAME", null privilege, granted_role from dba_role_privs where grantee in (select role from dba_roles) 
order by 1;
spool off

--Query 7 - 
spool pspprd_dblink_&&date\.log
select * from dba_db_links order by owner;
spool off

--set feedback on
