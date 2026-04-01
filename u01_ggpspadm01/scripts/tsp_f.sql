comp sum of nfrags totsiz avasiz on report 
break on report  
col tsname  format         a16 justify c heading 'Tablespace' 
col nfrags  format     999,990 justify c heading 'Free|Frags' 
col mxfrag  format 999,999,990 justify c heading 'Largest|Frag (KB)' 
col totsiz  format 999,999,999,999 justify c heading 'Total|(KB)' 
col avasiz  format 999,999,999,999 justify c heading 'Available|(KB)' 
col pctusd  format         990 justify c heading 'Pct|Used'  

select   
total.tablespace_name                       tsname,   
count(free.bytes)                           nfrags,   
nvl(max(free.bytes)/1024,0)                 mxfrag,   
total.bytes/1024                            totsiz,   
nvl(sum(free.bytes)/1024,0)                 avasiz,   
(1-nvl(sum(free.bytes),0)/total.bytes)*100  pctusd 
from   
dba_data_files  total,   
dba_free_space  free 
where   
total.tablespace_name = free.tablespace_name(+)   
and total.file_id=free.file_id(+)group by   total.tablespace_name,   total.bytes 
order by total.tablespace_name
/ 

