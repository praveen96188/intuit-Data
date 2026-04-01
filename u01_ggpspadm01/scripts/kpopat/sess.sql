set lines 300
col service_name for a30
col machine form a90;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('INTUADMIN','GGS','OPS_USER','PUBLIC','SYS','SYSTEM', 'RDSADMIN') and type = 'USER'  group by username,machine order by machine;

