 SELECT d.owner,  d.job_name,d.job_subname jobsubname,
         to_char(d.log_date,
                 'Dy DD-MON-YY:HH24:MI:SS') logdate,
         d.additional_info
FROM     dba_scheduler_jobs s,
         dba_scheduler_job_run_details d
WHERE    d.log_date >= s.LAST_START_DATE
         AND s.job_name = d.job_name
ORDER BY 2
;
