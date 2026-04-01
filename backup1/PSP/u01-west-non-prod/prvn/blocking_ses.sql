SELECT blocked_locks.pid     AS blocked_pid,
       blocked_activity.usename  AS blocked_user,
       blocked_activity.client_addr as blocked_client_addr,
       blocked_activity.client_hostname as blocked_client_hostname,
       blocked_activity.client_port as blocked_client_port,
       blocked_activity.application_name as blocked_application_name,
       blocked_activity.wait_event_type as blocked_wait_event_type,
       blocked_activity.wait_event as blocked_wait_event,
       blocked_activity.query    AS blocked_statement,
       blocking_locks.pid     AS blocking_pid,
       blocking_activity.usename AS blocking_user,
       blocking_activity.client_addr as blocking_user_addr,
       blocking_activity.client_hostname as blocking_client_hostname,
       blocking_activity.client_port as blocking_client_port,
       blocking_activity.application_name as blocking_application_name,
       blocking_activity.wait_event_type as blocking_wait_event_type,
       blocking_activity.wait_event as blocking_wait_event,
       blocking_activity.query   AS current_statement_in_blocking_process,
       now() - blocked_activity.query_start as Blocking_duration
FROM  pg_catalog.pg_locks         blocked_locks
          JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
          JOIN pg_catalog.pg_locks         blocking_locks
               ON blocking_locks.locktype = blocked_locks.locktype
                   AND blocking_locks.DATABASE IS NOT DISTINCT FROM blocked_locks.DATABASE
                   AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation
                   AND blocking_locks.page IS NOT DISTINCT FROM blocked_locks.page
                   AND blocking_locks.tuple IS NOT DISTINCT FROM blocked_locks.tuple
                   AND blocking_locks.virtualxid IS NOT DISTINCT FROM blocked_locks.virtualxid
                   AND blocking_locks.transactionid IS NOT DISTINCT FROM blocked_locks.transactionid
                   AND blocking_locks.classid IS NOT DISTINCT FROM blocked_locks.classid
                   AND blocking_locks.objid IS NOT DISTINCT FROM blocked_locks.objid
                   AND blocking_locks.objsubid IS NOT DISTINCT FROM blocked_locks.objsubid
                   AND blocking_locks.pid != blocked_locks.pid
          JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted ORDER BY blocked_activity.pid;
