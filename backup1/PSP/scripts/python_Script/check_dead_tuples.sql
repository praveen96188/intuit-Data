SELECT
    relname AS table_name,
    n_dead_tup AS dead_tuples
FROM
    pg_stat_user_tables
WHERE
    n_dead_tup > 500000
ORDER BY
    n_dead_tup DESC;
