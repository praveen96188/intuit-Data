set pages 500
col username for a25
col machine for a70
select username,machine,count(*) 
from gv$session 
where username is not NULL and username not in ('SYS','SYSTEM','INTUADMIN','GGT','GGS','RDSADMIN','PUBLIC','OPS_USER') and type = 'USER'  
group by service_name,username,machine order by machine;

