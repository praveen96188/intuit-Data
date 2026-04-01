spool chk_session_count
set pagesize 1000
set echo off
set feed on
col spid format 999999 heading 'UNIX|ID'
col osuser heading 'NETWORK|USER ID' format a10
col username heading 'ORACLE|USER ID' format a11
col machine format a22
col t3user format a10
col sid  format 9999
col serial#  format 99999
col process format 999999
col command format a8 
break on report;
compute sum label TOTAL of num_connections on report;
select 
   s.osuser,
   substr(s.username,1,11) username,
   substr(s.machine,1,22) machine,
   count(*) num_connections
from v$session s
where s.type != 'BACKGROUND'
group by s.osuser, username, machine
order by s.osuser, username, machine
/

