set echo off
set feedback off
set trimspool on
set sqlcase mixed
set arraysize 1
set verify off
set serveroutput on


DEFINE p_appuser   = '&&1' ;
DEFINE p_privileges = &&2  ;

spool InstallDB.log

DECLARE user_exists PLS_INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_exists FROM ALL_USERS WHERE USERNAME='&&p_appuser';
	
	IF user_exists = 1 THEN
       EXECUTE IMMEDIATE 'drop user &&p_appuser cascade';
	END IF;
END;
/
BEGIN

EXECUTE IMMEDIATE 'CREATE USER &&p_appuser IDENTIFIED BY &&p_appuser DEFAULT TABLESPACE users TEMPORARY TABLESPACE temp';
EXECUTE IMMEDIATE 'GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE to &&p_appuser';
EXECUTE IMMEDIATE 'Grant create session to &&p_appuser' ;
EXECUTE IMMEDIATE 'Grant create table to &&p_appuser' ;
EXECUTE IMMEDIATE 'Grant create view to &&p_appuser' ;
EXECUTE IMMEDIATE 'Grant create trigger to &&p_appuser' ;
EXECUTE IMMEDIATE 'Grant create procedure to &&p_appuser' ;
EXECUTE IMMEDIATE 'Grant create sequence to &&p_appuser' ;
EXECUTE IMMEDIATE 'grant create synonym to &&p_appuser' ;
EXECUTE IMMEDIATE 'grant create role to &&p_appuser' ;
EXECUTE IMMEDIATE 'grant connect, resource to &&p_appuser' ;
EXECUTE IMMEDIATE 'grant create any context to &&p_appuser' ;

if '&&p_privileges' = 'DBA' THEN
     EXECUTE IMMEDIATE 'GRANT DBA TO &&p_appuser WITH ADMIN OPTION';
END IF;

END;


/
exit
