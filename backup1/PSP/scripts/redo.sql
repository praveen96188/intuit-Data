set pages 10000
set lines 3000
SELECT to_date(first_time) DAY,
to_char(sum(decode(to_char(first_time,'HH24'),'00',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "00",
to_char(sum(decode(to_char(first_time,'HH24'),'01',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "01",
to_char(sum(decode(to_char(first_time,'HH24'),'02',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "02",
to_char(sum(decode(to_char(first_time,'HH24'),'03',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "03",
to_char(sum(decode(to_char(first_time,'HH24'),'04',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "04",
to_char(sum(decode(to_char(first_time,'HH24'),'05',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "05",
to_char(sum(decode(to_char(first_time,'HH24'),'06',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "06",
to_char(sum(decode(to_char(first_time,'HH24'),'07',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "07",
to_char(sum(decode(to_char(first_time,'HH24'),'08',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "08",
to_char(sum(decode(to_char(first_time,'HH24'),'09',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "09",
to_char(sum(decode(to_char(first_time,'HH24'),'10',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "10",
to_char(sum(decode(to_char(first_time,'HH24'),'11',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "11",
to_char(sum(decode(to_char(first_time,'HH24'),'12',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "12",
to_char(sum(decode(to_char(first_time,'HH24'),'13',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "13",
to_char(sum(decode(to_char(first_time,'HH24'),'14',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "14",
to_char(sum(decode(to_char(first_time,'HH24'),'15',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "15",
to_char(sum(decode(to_char(first_time,'HH24'),'16',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "16",
to_char(sum(decode(to_char(first_time,'HH24'),'17',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "17",
to_char(sum(decode(to_char(first_time,'HH24'),'18',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "18",
to_char(sum(decode(to_char(first_time,'HH24'),'19',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "19",
to_char(sum(decode(to_char(first_time,'HH24'),'20',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "20",
to_char(sum(decode(to_char(first_time,'HH24'),'21',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "21",
to_char(sum(decode(to_char(first_time,'HH24'),'22',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "22",
to_char(sum(decode(to_char(first_time,'HH24'),'23',(BLOCKS*BLOCK_SIZE)/1024/1024/1024,0)),'999.9') "23"
from
v$archived_log
where dest_id=1 and to_date(first_time) > sysdate -15
group by
to_char(first_time,'YYYY-MON-DD'), to_date(first_time)
order by to_date(first_time);

