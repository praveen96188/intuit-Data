set lines 300 pages 5000
col service_name for a30
col machine form a70;
col username for a30
select username,machine,count(*) from gv$session where username is not NULL and username not in ('SYS','SYSTEM','INTUADMIN','RDSADMIN') and type = 'USER'  group by service_name,username,machine order by username,machine;

