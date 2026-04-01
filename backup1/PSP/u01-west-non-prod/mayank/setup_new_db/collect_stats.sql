spool collect_stats
set echo on
set feed on
set time on
set timi on

EXEC DBMS_STATS.gather_schema_stats (ownname => 'PSPADM', estimate_percent => dbms_stats.auto_sample_size, degree => 16);

spool off
exit

