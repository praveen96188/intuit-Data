select 'exec rdsadmin.rdsadmin_util.kill('||s.sid||','||s.serial#||','||'''IMMEDIATE'''||');'
from v$session s, v$lock l 
where s.username like 'QBO%' and s.status = 'INACTIVE' and 
s.event like 'SQL*Net%' and s.sid = l.sid and 
s.seconds_in_wait >= 60 and l.block <> 0 
order by l.block desc, s.seconds_in_wait desc;
