COLUMN sql_id FORMAT A13
COLUMN bind_name FORMAT A10
COLUMN bind_value FORMAT A26
 
SELECT 
  sql_id,
  b.name bind_name,
  b.value_string bind_value 
FROM
  v$sql t 
JOIN
  v$sql_bind_capture b  using (sql_id)
WHERE
  b.value_string is not null  
AND
  sql_id='&sqlid'
/
