spool add_redos
set echo on
set feed on
set time on
set timi on

select GROUP#, BYTES, STATUS from V$LOG;
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);
exec rdsadmin.rdsadmin_util.add_logfile(bytes => 1073741824);

select GROUP#, BYTES, STATUS from V$LOG;

spool off
exit