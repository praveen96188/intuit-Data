set lines 300
col username for a30
col machine form a70;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('SYS','SYSTEM','INTUADMIN','RDSADMIN') and type = 'USER'  group by service_name,username,machine order by username,machine;

