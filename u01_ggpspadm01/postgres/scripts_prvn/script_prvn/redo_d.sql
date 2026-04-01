select to_char(FIRST_TIME, 'YYYY-MM-DD') time, count(*) NUM_LOGS, sum(BLOCKS)/2097152 size_gb
from  v$archived_log
where 
--CREATOR='LGWR' and  
dest_id=1 and 
FIRST_TIME >= sysdate - 30
group by to_char(FIRST_TIME, 'YYYY-MM-DD')
order by 1;

