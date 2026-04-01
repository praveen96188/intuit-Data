SELECT pid,usename, state, query_start, now() - query_start AS execution_time, query  FROM pg_stat_activity WHERE state = 'active' and usename not in('postgres');
