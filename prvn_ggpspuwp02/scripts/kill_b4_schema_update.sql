set lines 400 pages 999 trimspool on feed off
col db_name format a12
col table_name format a40
col column_name format a49
col low_value format a40
col high_value format a40
col method_opt_pref format a100
col company_id format a20
col auth_and_servlet format a30 trunc
col thread_id format a20
col event format a40 trunc
col object_name format a40
alter session set nls_date_format = 'yyyy-mm-dd hh24:mi:ss';
select 'exec rdsadmin.rdsadmin_util.kill('||s.sid||','||s.serial#||','||'''IMMEDIATE'''||');'
from v$session s
where s.username like 'QBO%' 
and s.status = 'INACTIVE' 
-- and s.event like 'SQL*Net%' 
-- and s.seconds_in_wait >= 60 
and s.CLIENT_IDENTIFIER is not null
and taddr is not null
order by s.seconds_in_wait desc;
