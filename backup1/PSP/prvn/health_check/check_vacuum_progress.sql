SELECT
    relid,
    phase,
    heap_blks_total,
    heap_blks_scanned,
    heap_blks_vacuumed
FROM
    pg_stat_progress_vacuum
ORDER BY
    relid;
