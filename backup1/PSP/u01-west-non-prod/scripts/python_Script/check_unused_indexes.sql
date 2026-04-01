SELECT 
    t.relname AS table_name,
    i.relname AS index_name,
    CASE 
        WHEN NOT EXISTS (  -- Unused Indexes
            SELECT 1
            FROM pg_stat_user_indexes si
            WHERE si.indexrelid = ix.indexrelid AND si.idx_scan > 0
        ) THEN 'Unused'
        WHEN ix.indisvalid = false THEN 'Invalid'  -- Invalid Indexes
        ELSE 'Valid'
    END AS index_status,
    CASE
        WHEN ix.indisvalid = false THEN 'Invalid'
        ELSE 'Valid'
    END AS index_validity
FROM 
    pg_index ix
    JOIN pg_class i ON i.oid = ix.indexrelid
    JOIN pg_class t ON t.oid = ix.indrelid
WHERE 
    (NOT EXISTS (  -- Only unused indexes
        SELECT 1
        FROM pg_stat_user_indexes si
        WHERE si.indexrelid = ix.indexrelid AND si.idx_scan > 0
    ) OR ix.indisvalid = false)  -- OR invalid indexes
    AND t.relkind = 'r'  -- Regular tables only (not partitions)
    AND t.relispartition = false  -- Exclude partitions
ORDER BY t.relname, i.relname;
