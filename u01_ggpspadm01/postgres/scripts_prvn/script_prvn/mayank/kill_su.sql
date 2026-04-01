select 'exec rdsadmin.rdsadmin_util.kill('||sid||','||serial#||','||'''IMMEDIATE'''||');'
from v$session where sid='&sid';
