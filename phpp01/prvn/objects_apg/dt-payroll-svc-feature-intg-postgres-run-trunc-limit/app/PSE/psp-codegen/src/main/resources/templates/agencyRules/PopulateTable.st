DECLARE

	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TMP_$AgencyRulesTable.tableName$';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE TMP_$AgencyRulesTable.tableName$ CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TMP_$AgencyRulesTable.tableName$
(
	 $AgencyRulesTable.tableColumns:CreateTableColumnDef();  separator="\n"$
	 PRIMARY KEY($AgencyRulesTable.primaryKey$)
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

$AgencyRulesTable.insertRowValues:{k|
INSERT INTO TMP_$AgencyRulesTable.tableName$ ($AgencyRulesTable.columnSelectList$ ) VALUES (
$k$)
/
}; separator="\n"$


--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO $AgencyRulesTable.tableName$
   ($AgencyRulesTable.columnSelectList$, VERSION)
SELECT
    $AgencyRulesTable.columnSelectList$, 0 
FROM
   TMP_$AgencyRulesTable.tableName$ tt
WHERE
   tt.$AgencyRulesTable.PrimaryKey$ NOT IN (SELECT $AgencyRulesTable.PrimaryKey$ FROM $AgencyRulesTable.tableName$)

/

UPDATE $AgencyRulesTable.tableName$ rt
SET ($AgencyRulesTable.ColumnSelectListWithRTPrefix$) =
(SELECT  $AgencyRulesTable.ColumnSelectListWithTTPrefix$
 FROM TMP_$AgencyRulesTable.tableName$ tt WHERE tt.$AgencyRulesTable.PrimaryKey$ = rt.$AgencyRulesTable.PrimaryKey$)
 WHERE rt.$AgencyRulesTable.PrimaryKey$ IN (SELECT $AgencyRulesTable.PrimaryKey$ FROM TMP_$AgencyRulesTable.tableName$)
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TMP_$AgencyRulesTable.tableName$

/
COMMIT