spool tmp_log
set lines 3000 echo off timing off feedback on trimspool on numwidth 50 underline off heading on pagesize 0
PROMPT "Segment Size in GB"
select owner,segment_type,sum(bytes)/1024/1024/1024 from dba_segments where owner='PSPADM' group by owner,segment_type;
PROMPT "No Of Objects"
select owner,object_type,count(*) from dba_objects where owner='PSPADM' group by owner,object_type order by object_type;

