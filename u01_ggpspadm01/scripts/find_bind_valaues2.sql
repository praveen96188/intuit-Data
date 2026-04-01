 SELECT
  sql_id,
  b.name bind_name,
  count(distinct b.value_string) bind_value_cnt
FROM
  v$sql t
JOIN
  v$sql_bind_capture b  using (sql_id)
WHERE
  b.value_string is not null
AND
  sql_id='&sqlid'
GROUP BY sql_id,
  b.name   
/
