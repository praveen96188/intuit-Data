WITH bloat AS (
    SELECT
        n.nspname || '.' || c.relname AS table_name,
        pg_total_relation_size(c.oid) AS total_size,
        pg_table_size(c.oid) AS data_size,
        pg_total_relation_size(c.oid) - pg_table_size(c.oid) AS index_size,
        st.n_live_tup AS live_tuples,
        st.n_dead_tup AS dead_tuples,
        pg_total_relation_size(c.oid) - pg_table_size(c.oid) AS bloat_size
    FROM
        pg_class c
    JOIN
        pg_namespace n ON n.oid = c.relnamespace
    JOIN
        pg_stat_user_tables st ON st.relid = c.oid  -- Using pg_stat_user_tables to get live and dead tuples
    WHERE
        c.relkind = 'r'  -- Only regular tables
        AND pg_total_relation_size(c.oid) > 0  -- Exclude empty tables
),
bloat_summary AS (
    SELECT
        table_name,
        total_size,
        ROUND(bloat_size::numeric / (1024 * 1024 * 1024), 2) AS bloat_size_gb,  -- Convert bloat size to GB
        ROUND(bloat_size::numeric / total_size * 100, 2) AS bloat_percentage
    FROM
        bloat
    WHERE
        bloat_size > 0  -- Only tables with bloat
)
SELECT
    table_name,
    ROUND(total_size::numeric / (1024 * 1024 * 1024), 2) AS total_size_gb,
    bloat_size_gb,
    bloat_percentage 
FROM
    bloat_summary
ORDER BY
    bloat_size_gb DESC
LIMIT 10; 
