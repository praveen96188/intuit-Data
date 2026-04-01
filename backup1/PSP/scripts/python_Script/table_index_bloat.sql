WITH table_bloat AS (
    SELECT 
        relname AS table_name,
        pg_size_pretty(pg_table_size(relid)) AS data_size,
        pg_size_pretty(pg_indexes_size(relid)) AS index_size,
        pg_size_pretty(pg_total_relation_size(relid)) AS total_size,
        pg_table_size(relid) AS table_size,
        pg_indexes_size(relid) AS index_size_raw,
        pg_total_relation_size(relid) - pg_table_size(relid) - pg_indexes_size(relid) AS bloat_size
    FROM pg_stat_user_tables
),
index_bloat AS (
    SELECT 
        indexrelname AS index_name,
        pg_size_pretty(pg_relation_size(indexrelid)) AS index_size,
        pg_size_pretty(pg_total_relation_size(indexrelid)) AS total_index_size,
        pg_relation_size(indexrelid) AS index_size_raw,
        pg_total_relation_size(indexrelid) - pg_relation_size(indexrelid) AS index_bloat_size
    FROM pg_stat_user_indexes
)
SELECT 
    t.table_name,
    t.data_size,
    t.index_size,
    t.total_size,
    t.bloat_size AS table_bloat,
    i.index_name,
    i.index_size,
    i.index_bloat_size AS index_bloat
FROM 
    table_bloat t
JOIN 
    index_bloat i ON t.table_name = i.index_name
ORDER BY 
    t.table_name;
