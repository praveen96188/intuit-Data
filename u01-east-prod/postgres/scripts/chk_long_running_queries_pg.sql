\set ECHO none
\x
SELECT datname,
       usename,
       application_name,
       pid,
       wait_event_type,wait_event,state,
       round((extract(epoch from now()) - extract(epoch from xact_start))::numeric, 3) as trans_duration_secs,
       round((extract(epoch from now()) - extract(epoch from query_start))::numeric, 3) as query_duration_secs,
--       (now() - pg_stat_activity.query_start) AS duration,
       query
FROM pg_stat_activity
WHERE usename not in ('ggs', 'ggt', 'dms_apg_src', 'datalake_debezium', 'data_capture_role')
  and xact_start is not null
  and (now() - pg_stat_activity.query_start) > interval '5 minutes';

