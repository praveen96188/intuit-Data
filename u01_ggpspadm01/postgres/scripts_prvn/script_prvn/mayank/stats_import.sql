spool stats_import.log
set timing on
set echo on
EXEC DBMS_APPLICATION_INFO.SET_MODULE('Importing Stats');
EXEC DBMS_STATS.IMPORT_SCHEMA_STATS ('PSPADM','OPT_STATS_FOR_AWS2',statown=>'KPOPAT')
spool off

