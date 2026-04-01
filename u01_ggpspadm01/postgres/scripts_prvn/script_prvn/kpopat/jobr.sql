SELECT j.job_name JOBNAME,
       j.JOB_SUBNAME STEPNAME,
       g.SESSION_ID SID,
       g.SESSION_SERIAL_NUM SERIAL#,
       j.SLAVE_OS_PROCESS_ID OSID,
       g.INST_ID,
       s.instance_name,
substr(ELAPSED_TIME,6,2) * 60 + substr(ELAPSED_TIME,9,2) "ELPSED(Mins)"
FROM   dba_SCHEDULER_RUNNING_JOBS j,
       GV$SCHEDULER_RUNNING_JOBS g,
       gv$instance s
WHERE  s.inst_id = g.inst_id
       AND g.SESSION_ID = j.SESSION_ID
       AND j.SLAVE_OS_PROCESS_ID = g.OS_PROCESS_ID
      order by g.SESSION_ID
;
