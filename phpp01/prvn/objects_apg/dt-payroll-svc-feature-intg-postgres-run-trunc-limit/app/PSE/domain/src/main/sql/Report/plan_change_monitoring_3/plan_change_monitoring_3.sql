set head off;
set linesize 205;
set pagesize 100;
col "Last Old Plan Exec" format a20;
col "SQL Id" format a15;

PROMPT SQL_ID                Plan   Old Plan Elapsed   OldAvgEla  TimeDelta      Execs  Execs Old BufGets   OldAvgBuf DskReads  OldAvgDsk Last Old Plan Exec;
PROMPT --------------- ---------- ---------- --------- --------- ---------- ---------- ---------- --------- --------- --------- --------- --------------------;

SELECT 
   S.INST_ID || '-' || S.SQL_ID as "SQL Id", 																			 		-- 13  6
   S.PLAN_HASH_VALUE as "Plan", 																 		-- 10  4
   stat.PLAN_HASH_VALUE as "Old Plan",															 		-- 10  8
   to_char(max(S.ELAPSED_TIME/S.EXECUTIONS/1000000), '9990.000') as "Elapsed",    								 		--  8  7
   to_char(avg(STAT.ELAPSED_TIME_DELTA/STAT.EXECUTIONS_DELTA/1000000), '9990.000') as "OldAvgElapsed",				 		--  8 13
   avg(STAT.ELAPSED_TIME_DELTA/STAT.EXECUTIONS_DELTA/1000000) - max(S.ELAPSED_TIME/S.EXECUTIONS/1000000) as "TimeDelta",       		-- 10  9  (shrink to thousandth of sec? rename to delta?)
   max(S.EXECUTIONS) as "Execs",																 		--  9  5
   sum(stat.EXECUTIONS_DELTA) as "Execs Old",													       		--  9  8
   to_char(max(S.BUFFER_GETS/S.EXECUTIONS), '99999990') as "BufGets",    									 		--  8  7
   to_char(avg(STAT.BUFFER_GETS_DELTA/STAT.EXECUTIONS_DELTA), '99999990') as "OldAvgBufGets",           				 		--  8 13
   to_char(max(S.DISK_READS/S.EXECUTIONS), '99999990') as "DskReads",    									 		--  8  8
   to_char(avg(STAT.DISK_READS_DELTA/STAT.EXECUTIONS_DELTA), '99999990') as "OldAvgDskReads",						 		-- 08 14
   to_char(max(SS.BEGIN_INTERVAL_TIME), 'YY-MM-DD HH12:MI:SS AM') as "Last Old Plan Exec",                                     		-- 20 18
   SUBSTR(SQL_TEXT, CASE WHEN (INSTR(UPPER(SQL_TEXT), 'FROM') > 0) THEN INSTR(UPPER(SQL_TEXT), 'FROM') ELSE 1 END, 205) as "Sql Text" 	-- 80  8
FROM GV$SQL s
     INNER JOIN DBA_HIST_SQLSTAT stat ON STAT.DBID = 2864657972 and stat.sql_id = s.sql_id AND STAT.INSTANCE_NUMBER = s.inst_id AND stat.plan_hash_value <> s.plan_hash_value
     INNER JOIN DBA_HIST_SNAPSHOT ss ON SS.DBID = STAT.DBID and SS.INSTANCE_NUMBER = STAT.INSTANCE_NUMBER AND STAT.SNAP_ID = SS.SNAP_ID AND SS.BEGIN_INTERVAL_TIME >= sysdate-14 
WHERE 
   S.parsing_schema_name = 'PSPAPP' AND
   S.PLAN_HASH_VALUE <> stat.PLAN_HASH_VALUE AND
   ELAPSED_TIME_DELTA > 0 AND
   S.EXECUTIONS > 0 AND
   STAT.EXECUTIONS_DELTA > 0
GROUP BY
   S.INST_ID || '-' || S.SQL_ID, SUBSTR(SQL_TEXT, CASE WHEN (INSTR(UPPER(SQL_TEXT), 'FROM') > 0) THEN INSTR(UPPER(SQL_TEXT), 'FROM') ELSE 1 END, 205), S.PLAN_HASH_VALUE, stat.PLAN_HASH_VALUE
ORDER BY 
   6;

