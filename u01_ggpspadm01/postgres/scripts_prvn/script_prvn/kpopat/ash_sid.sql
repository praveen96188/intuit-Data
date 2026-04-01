set pages 1000
col u for a10
col machine for a10 trunc
col event for a30 trunc
col module for a30 trunc
col sql_opname for a10
col client_id for a30
 SELECT (SELECT username
            FROM dba_users u
           WHERE u.user_id = h.user_id)
            u,
         sample_time,
         session_state,
         session_id,
         sql_id,
         machine,
         module,
         event,
         p1,
         blocking_session,
         sql_opname,
         client_id
    FROM dba_hist_active_sess_history h
    where session_id= &sess_id -- 7923<-- 7732, 2077 <-- 3988
   and sample_time BETWEEN TO_DATE ('09-FEB-21 01:00:00',
                                      'DD-MON-YY hh24:mi:ss')
                         AND TO_DATE ('09-FEB-21 23:36:00',
                                      'DD-MON-YY hh24:mi:ss')
ORDER BY sample_time;
