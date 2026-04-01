select username,machine,count(*) 
from gv$session 
where username is not NULL and username not in ('SYS','SYSTEM','INTUADMIN','GGT','GGS','RDSADMIN','PUBLIC') and type = 'USER'  
group by service_name,username,machine order by machine;

