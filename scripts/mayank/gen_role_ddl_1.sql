set long 100000
set longchunksize 200
set heading off
set feedback off
set echo off
set verify off
undefine role

select (case
when ((select count(*)
from dba_roles
where role = '&1') > 0)
then dbms_metadata.get_ddl ('ROLE', '&1')
else to_clob ('Role does not exist')
end ) Extracted_DDL from dual
UNION ALL
select (case
when ((select count(*)
from dba_role_privs
where grantee = '&1') > 0)
then dbms_metadata.get_granted_ddl ('ROLE_GRANT', '&1')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from dba_role_privs
where grantee = '&1') > 0)
then dbms_metadata.get_granted_ddl ('DEFAULT_ROLE', '&1')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from dba_sys_privs
where grantee = '&1') > 0)
then dbms_metadata.get_granted_ddl ('SYSTEM_GRANT', '&1')
end ) from dual
UNION ALL
select (case
when ((select count(*)
from dba_tab_privs
where grantee = '&1') > 0)
then dbms_metadata.get_granted_ddl ('OBJECT_GRANT', '&1')
end ) from dual;

