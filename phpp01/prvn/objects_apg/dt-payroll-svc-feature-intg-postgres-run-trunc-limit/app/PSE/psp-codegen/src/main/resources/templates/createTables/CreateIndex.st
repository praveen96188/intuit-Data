DECLARE
	index_exists PLS_INTEGER;
BEGIN
-- Want to make sure that there is no index on the FK
-- column alone, composite indexes where FK is part of the indexed
-- column is not counted
SELECT COUNT(COLUMN_NAME) into index_exists FROM USER_IND_COLUMNS WHERE INDEX_NAME IN
	(SELECT INDEX_NAME FROM USER_IND_COLUMNS WHERE COLUMN_NAME = '$it.foreignKeyColumnName$' AND
	TABLE_NAME = '$it.tableName$')
	GROUP BY INDEX_NAME HAVING COUNT(COLUMN_NAME) = 1;

EXCEPTION
	WHEN NO_DATA_FOUND THEN
		EXECUTE IMMEDIATE 'CREATE INDEX "$it.foreignKeyConstraintName$" ON "$it.tableName$"("$it.foreignKeyColumnName$", "REALM_ID")';

end;
/
