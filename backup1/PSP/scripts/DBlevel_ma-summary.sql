set serveroutput on
set feedback off
set pagesize 5000
set linesize 120

prompt Enter directory to spool output to:
accept spooldir

prompt Enter database name to scan:
accept dbname

set heading on
set verify off
set serveroutput on

spool &spooldir/aws-ma-&dbname-results.out

prompt
prompt Scanning &dbname database now. This make take a few minutes.....
prompt


--general checks

—db size
select sum(bytes)/1024/1024 size_in_mb from dba_segments;

--v$option 
set pagesize 250
col parameter format a50
col value format a10
select * from v$option;

--dba_feature_usage_statistics

col name format a60
set linesize 100
 select name, detected_usages, currently_used,last_usage_date from dba_feature_usage_Statistics;

--v$os_stats
col value format 9999999
select stat_name,value, comments from v$osstat;


