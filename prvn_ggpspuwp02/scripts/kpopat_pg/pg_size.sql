-- table wise size
SELECT table_schema, table_name,                                                                                        
    pg_size_pretty(total_bytes) AS total_size
    , pg_size_pretty(table_bytes) AS table_size
    , pg_size_pretty(index_bytes) AS index_size
    , pg_size_pretty(toast_bytes) AS toast_size
 -- , 'VACUUM FULL VERBOSE '||table_schema||'.'||table_name||';' vac_ddl
  FROM (
  SELECT *, total_bytes-index_bytes-COALESCE(toast_bytes,0) AS table_bytes FROM (
      SELECT nspname AS table_schema, regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(relname,'_m\d+',''),'_from_psp',''),'_from_qbdt',''),'_from_ews',''),'_from_null',''),'_from_cris',''),'_from_as400',''),'_from_dflt','') AS TABLE_NAME
              , sum(c.reltuples) AS row_estimate
              , sum(pg_total_relation_size(c.oid)) AS total_bytes
              , sum(pg_indexes_size(c.oid)) AS index_bytes
              , sum(pg_total_relation_size(reltoastrelid)) AS toast_bytes
          FROM pg_class c
          LEFT JOIN pg_namespace n ON n.oid = c.relnamespace
          WHERE relkind = 'r'
  GROUP by nspname, regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(relname,'_m\d+',''),'_from_psp',''),'_from_qbdt',''),'_from_ews',''),'_from_null',''),'_from_cris',''),'_from_as400',''),'_from_dflt','')
  ) a
) a order by total_bytes desc;

