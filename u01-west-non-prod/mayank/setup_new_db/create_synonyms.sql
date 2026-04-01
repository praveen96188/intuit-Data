spool CREATE_SYNONYMS.SQL
set echo off
set feed off
set head off
set linesize 160
set pagesize 0

select 'CREATE OR REPLACE SYNONYM PSPAPP.'||table_name ||' for '|| OWNER||'.'||TABLE_NAME ||';' from DBA_TABLES where OWNER='PSPADM' order by TABLE_NAME;

spool off

spool create_synonyms

set echo on
set feed on
set head on

@CREATE_SYNONYMS.SQL

spool off
exit


