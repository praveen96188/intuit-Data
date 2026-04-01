set lines 300
set pages 1000
col Metric for a50
col  "Metric Unit" for a25
select m.metric_name "Metric" ,
       round(m.value,2)    "Current Value",
       round(s.average,2) "Average Value",
       round(s.MAXVAL,2) "Max Value",
       m.metric_unit "Metric Unit"
 FROM v$sysmetric       m,
      v$sysmetric_summary s
WHERE m.group_id=s.group_id
  AND m.METRIC_ID=s.METRIC_ID
  AND s.average > 0
  AND s.metric_name IN (
-- 'Redo Allocation Hit Ratio',
            'Redo Generated Per Sec',
--                        'Logons Per Sec',
                        'Logical Reads Per Sec',
                    --    'Redo Writes Per Sec',
                        'Host CPU Utilization (%)',
                        'Network Traffic Volume Per Sec',
                    --    'Enqueue Timeouts Per Sec',
                    --    'Enqueue Waits Per Sec',
                    --    'Enqueue Requests Per Sec',
--                        'Consistent Read Gets Per Sec',
                        'Physical Read Total IO Requests Per Sec',
                        'Physical Write Total IO Requests Per Sec',
                        'Current Logons Count',
--                        'SQL Service Response Time',
                        'Database Wait Time Ratio',
                        'Database CPU Time Ratio',
--                        'Shared Pool Free %',
--                        'PGA Cache Hit %',
--                        'Session Limit %',
                        'Database Time Per Sec',
                    --    'Physical Write IO Requests Per Sec',
--                        'Physical Write Total Bytes Per Sec',
                    --    'Physical Read IO Requests Per Sec',
--                        'Physical Read Total Bytes Per Sec',
                        'Current OS Load',
                        'Session Count',
                        'Average Synchronous Single-Block Read Latency',
                        'I/O Megabytes per Second',
                        'Average Active Sessions',
                        'Total PGA Allocated')
 ORDER BY 1    ;

