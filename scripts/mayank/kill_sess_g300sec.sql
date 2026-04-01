 select  'exec rdsadmin.rdsadmin_util.kill('||s.sid||','||s.serial#||','||'''IMMEDIATE'''||');' from gv$session s
 where status = 'ACTIVE' and (sysdate-SQL_EXEC_START)*24*60*60 > 300 and sql_id is not null and USERNAME like 'QBO%'
/
