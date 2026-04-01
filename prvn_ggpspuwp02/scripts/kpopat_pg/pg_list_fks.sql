SELECT t.oid::regclass::text AS table_name, count(1) AS total
  FROM pg_constraint c
  JOIN pg_class t ON (t.oid = c.confrelid)
 GROUP BY table_name
 ORDER BY total DESC;
