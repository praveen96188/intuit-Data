select slot_name, slot_type, active, active_pid, pg_size_pretty(pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn)) from pg_replication_slots where active_pid is null;

