SELECT 
sql_exec_start, 
sql_exec_id, 
sql_plan_hash_value, 
inst_id 
      FROM gv$sql_monitor
      WHERE sql_id = '&sql_id' 
      ORDER BY 1, 2;
