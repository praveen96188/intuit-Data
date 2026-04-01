DECLARE

	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = '$it.tableName$';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "$it.tableName$" CASCADE CONSTRAINTS';
	END IF;

END;

/
CREATE TABLE "$it.tableName$"
(
    PRIMARY KEY($it.primaryKey$,"REALM_ID")
    $it.PropertyTemplateParameters:CreateTableColumnDef();  separator="\n"$
)
/