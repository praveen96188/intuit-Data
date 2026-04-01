DECLARE
   name   VARCHAR2 (50);
BEGIN
   SELECT address || ',' || hash_value
     INTO name
     FROM v$sqlarea
    WHERE sql_id LIKE 'asny9bcuq5v9w';
   sys.DBMS_SHARED_POOL.purge (name, 'C', 1);
END;
/
exit
