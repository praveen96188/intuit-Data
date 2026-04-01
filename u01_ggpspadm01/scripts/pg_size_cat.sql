 SELECT  nspname , -- relkind,
    pg_size_pretty(sum(pg_relation_size(c.oid))) AS size
  FROM pg_class c
  LEFT JOIN pg_namespace n ON (n.oid = c.relnamespace)
  WHERE nspname IN ('pg_catalog', 'information_schema')
  GROUP by nspname
  ORDER BY nspname DESC
;
SELECT relname, nspname ,
   (sum(pg_relation_size(c.oid))/1024/1024/1024) AS size
 FROM pg_class c
 LEFT JOIN pg_namespace n ON (n.oid = c.relnamespace)
WHERE nspname IN ('pg_catalog', 'information_schema')
--  AND relname like 'txdetails_%p9'
 GROUP by nspname, relname
 ORDER BY size DESC
;

