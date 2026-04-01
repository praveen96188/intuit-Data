SET SERVEROUTPUT ON
DECLARE
  l_plans_dropped  PLS_INTEGER;
BEGIN
l_plans_dropped := DBMS_SPM.drop_sql_plan_baseline (
    sql_handle => NULL,
    plan_name  => 'SQL_PLAN_4ddtk1zr99k02b79c6f0a');
DBMS_OUTPUT.put_line(l_plans_dropped);
END;
/
DECLARE
   name   VARCHAR2 (50);
BEGIN
   SELECT address || ',' || hash_value
     INTO name
     FROM v$sqlarea
    WHERE sql_id LIKE 'd74hb8y3x1uhu';
   sys.DBMS_SHARED_POOL.purge (name, 'C', 1);
END;
/
exit
