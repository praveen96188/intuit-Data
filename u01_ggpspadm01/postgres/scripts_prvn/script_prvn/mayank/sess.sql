set lines 300
col service_name for a30
col machine form a70;
select username,machine,count(*) from gv$session where username is not NULL and username not in ('SYS','SYSTEM') and type = 'USER'  group by service_name,username,machine;

