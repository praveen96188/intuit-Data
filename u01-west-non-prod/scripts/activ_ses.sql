SELECT pid, query, state, query_start, now() - query_start AS execution_time FROM pg_stat_activity WHERE state = 'active';
