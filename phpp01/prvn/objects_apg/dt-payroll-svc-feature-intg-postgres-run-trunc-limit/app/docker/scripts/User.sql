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

alter session set "_ORACLE_SCRIPT"=true;

@CreateUser.sql &&p_appuser &&p_privileges