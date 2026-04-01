spool kill_session_all.sql
SET LINESIZE 32000;
SET PAGESIZE 40000;
SET LONG 50000;
select 'exec rdsadmin.rdsadmin_util.kill('||sid||','||serial#||');' from gv$session where username is not NULL and username not in ('RDSADMIN','INTUADMIN','SYS','SYSTEM') and type = 'USER' ;

spool off;

@kill_session_all.sql
