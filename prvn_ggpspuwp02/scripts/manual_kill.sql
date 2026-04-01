select 'exec rdsadmin.rdsadmin_util.kill('||sid||','||serial#||','||'''IMMEDIATE'''||');'
from v$session s
     where s.username like 'QBO%' or s.username like '%UGT%';
