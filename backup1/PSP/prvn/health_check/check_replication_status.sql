SELECT 
    slot_name,
    plugin,
    slot_type,
    active,
    restart_lsn,
    confirmed_flush_lsn,
    pg_catalog.pg_size_pretty(pg_catalog.pg_total_relation_size('pg_replication_slots')) AS total_size
FROM 
    pg_replication_slots
WHERE
    (active = true OR active = false)  -- Include both active and inactive (invalid) slots
ORDER BY active DESC, slot_name;
