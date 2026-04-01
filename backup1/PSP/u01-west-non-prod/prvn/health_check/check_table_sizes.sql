SELECT
    relname AS table_name,
    pg_size_pretty(pg_total_relation_size(relid)) AS total_size,
    pg_size_pretty(pg_table_size(relid)) AS data_size,
    pg_size_pretty(pg_indexes_size(relid)) AS index_size
FROM
    pg_stat_user_tables
ORDER BY
    pg_total_relation_size(relid) DESC
LIMIT 10;  -- Limiting to the top 10 largest tables, adjust as needed
