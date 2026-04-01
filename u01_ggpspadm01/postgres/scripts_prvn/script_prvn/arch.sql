SELECT to_date(first_time) DAY,
round(sum(BLOCKS*BLOCK_SIZE)/1024/1024/1024) sz
from
v$archived_log
where dest_id=1 
group by
to_char(first_time,'YYYY-MON-DD'), to_date(first_time)
order by to_date(first_time);


