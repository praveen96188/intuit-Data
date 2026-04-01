set serveroutput on
set feedback off
set pagesize 5000
set linesize 120

prompt Enter directory to spool output to:
accept spooldir

prompt Enter Schema to be analyzed
accept schema

set heading on
set verify off
set serveroutput on

spool &spooldir/aws_ma-&schema-schema_summary.out

prompt
prompt Scanning &schema schema now. This make take a few minutes.....
prompt


select object_type as "OBJECT TYPE", count(*) as COUNT from all_objects where owner=upper('&schema') group by object_type;

--get num lines of code in each pl/sql object

col name format a50
 select name,type,count(line) as "# lines of code" from all_source where owner=upper('&schema') group by name,type order by type;

select sum(count(line)) as "Total # Lines of code" from all_source where owner =upper('&schema') group by name;

--general checks

