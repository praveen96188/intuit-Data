set pages 0
DECLARE
   name   VARCHAR2 (50);
BEGIN
   SELECT address || ',' || hash_value
     INTO name
     FROM v$sqlarea
    WHERE sql_id LIKE 'acgvwu0ryuu57';
   sys.DBMS_SHARED_POOL.purge (name, 'C', 1);
END;
/
exit
