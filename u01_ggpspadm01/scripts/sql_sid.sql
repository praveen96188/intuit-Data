col sql_text format a1000 heading "Current SQL"
select q.sql_text
from v$session s
, v$sql q
WHERE s.sql_address = q.address
and s.sql_hash_value + DECODE
(SIGN(s.sql_hash_value), -1, POWER( 2, 32), 0) = q.hash_value
AND s.sid=&sid;

