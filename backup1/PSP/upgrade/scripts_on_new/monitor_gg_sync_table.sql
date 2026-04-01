spool monitor_gg_sync_table_monolith

--declare
--db_name varchar2(10) := &1 ;
--begin
--sql_stmt := 'insert into pspadm.gg_heartbeat values (:1, sysdate)';
--execute immediate sql_stmt using db_name;
--commit;
--end;
--/

insert into pspadm.gg_heartbeat values ('&1', sysdate);
commit;
grant all on pspadm.gg_heartbeat to ops_user;

grant create job to ops_user;
grant create procedure to ops_user;

alter session set current_schema=ops_user;

exec dbms_scheduler.drop_job('MONITOR_DB_SYNC');
BEGIN
dbms_scheduler.create_job(
job_name => '"MONITOR_DB_SYNC"',
job_type => 'PLSQL_BLOCK',
job_action => 'declare
   row number;
begin
  update pspadm.gg_heartbeat set last_update=sysdate where source = "&1";
  commit;
end;',
repeat_interval => 'FREQ=MINUTELY; BYSECOND=0,10,20,30,40,50',
start_date => systimestamp at time zone 'America/Los_Angeles',
job_class => '"DEFAULT_JOB_CLASS"',
comments => 'Monitor db sync',
auto_drop => FALSE,
enabled => TRUE);
END;
/

spool off


