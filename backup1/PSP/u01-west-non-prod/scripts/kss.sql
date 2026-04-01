select  'exec rdsadmin.rdsadmin_util.kill('||s.sid||','||s.serial#||','||'''PROCESS'''||');' from gv$session s
where sid='&sid';

