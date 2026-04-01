SET LONG 1000000
SET LONGCHUNKSIZE 1000000
SET LONG 1000000
SET LONGCHUNKSIZE 1000000
SET LINESIZE 1000
SET PAGESIZE 0
SET TRIM ON
SET TRIMSPOOL ON
SET ECHO OFF
SET FEEDBACK OFF
SET TIMING OFF

spool hs.csv
select 'sql_id|sql_text|parsing_schema_name|MODULE|ACTION|MACHINE|SAMPLE_TIME' from dual;
select sub1.sql_id || ' | ' || sub1.sql_text || ' | ' || sub1.parsing_schema_name || ' | ' || sub1.module || ' | ' || sub1.action || ' | ' ||  ash.MACHINE || ' | ' || ash.SAMPLE_TIME
       FROM DBA_HIST_ACTIVE_SESS_HISTORY ash,
(SELECT sub.sql_id, st.sql_text, sub.parsing_schema_name, sub.MODULE, sub.ACTION
FROM DBA_HIST_SQLTEXT st,
     (SELECT /*+ parallel(8) */distinct t.sql_id, t.PARSING_SCHEMA_NAME, t.MODULE, t.ACTION
      FROM dba_hist_sqlstat t,
           dba_hist_snapshot s,
           DBA_HIST_SQLTEXT st
      WHERE t.snap_id = s.snap_id
        AND t.dbid = s.dbid
        AND t.instance_number = s.instance_number
        AND t.executions_delta > 0
        AND t.PARSING_SCHEMA_NAME in ('PSPAPP', 'PSPADM')
        AND sql_text not like 'SELECT /* DS_SVC%'
        AND sql_text not like '%FROM sys.%'
        AND SQL_TEXT not like '%SQL Analyze(1)%'
        AND s.BEGIN_INTERVAL_TIME >
            TO_DATE('09/21/2021 00:00:00',
                    'mm/dd/yyyy hh24:mi:ss')
        AND END_INTERVAL_TIME <
            TO_DATE('09/22/2021 00:00:00',
                    'mm/dd/yyyy hh24:mi:ss')
     ) sub
WHERE sub.sql_id = st.sql_id) sub1
where sub1.SQL_ID = ash.SQL_ID;
--ORDER BY 3 DESC;

spool off
