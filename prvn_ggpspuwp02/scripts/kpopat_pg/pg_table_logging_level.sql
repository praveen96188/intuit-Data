SELECT c.oid,
n.nspname,
c.relname,
CASE relreplident
          WHEN 'd' THEN 'default'
          WHEN 'n' THEN 'nothing'
          WHEN 'f' THEN 'full'
          WHEN 'i' THEN 'index'
       END AS replica_identity
FROM pg_class c
LEFT JOIN pg_namespace n ON n.oid = c.relnamespace 
  AND relkind ='r' order by 2, 3;
