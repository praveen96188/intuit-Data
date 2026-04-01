SELECT   'exec rdsadmin.rdsadmin_util.kill('|| s.sid|| ','|| s.serial#||','''||'PROCESS'||''')'
FROM     gv$session s,
         gv$process p,
     gv$sqlarea a
WHERE    p.addr = s.paddr
  AND    s.sql_id = a.sql_id 
  AND      s.username IS NOT NULL
  and p.inst_id = s.inst_id
and s.sql_id= 'gzrjn47dq54g7'
and status ='ACTIVE'
ORDER BY s.status,
         s.program
/
