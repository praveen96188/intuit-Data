compute sum of busy on report
compute sum of avail on report
compute sum of idle on report
break on report
select inst_id i#, 32/*PIE*/-max(decode(trim(statistic),'Servers Busy',value)) avail, max(decode(trim(statistic),'Servers Busy',value)) Busy, max(decode(trim(statistic),'Servers Idle',value)) Idle
from gv$pq_sysstat
group by inst_id
/

