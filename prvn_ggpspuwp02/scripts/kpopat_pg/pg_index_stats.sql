SELECT * FROM pg_class, pg_index 
 WHERE pg_index.indexrelid = pg_class.oid ;

