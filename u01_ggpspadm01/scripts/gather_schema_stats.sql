set timing on time on echo on
set serveroutput on 

spool gather_schema_stats_output
exec dbms_stats.gather_schema_stats(ownname=>'PSPADM',degree =>16);
spool off

