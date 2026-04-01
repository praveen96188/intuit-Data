select to_char(first_time,'DD-MON-YY HH24') , count(1), round(sum((BLOCKS*BLOCK_SIZE))/1024/1024/1024,1) "Redo MB" 
  from v$archived_log
 where first_time between sysdate-2 and sysdate 
 group by to_char(first_time,'DD-MON-YY HH24')  
 order by to_date(to_char(first_time,'DD-MON-YY HH24') , 'DD-MON-YY HH24')
/
