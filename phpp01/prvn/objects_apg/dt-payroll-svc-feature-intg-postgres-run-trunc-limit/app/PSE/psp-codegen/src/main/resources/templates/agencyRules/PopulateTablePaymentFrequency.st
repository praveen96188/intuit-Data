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
   NOT EXISTS (SELECT * FROM $AgencyRulesTable.tableName$ t
		WHERE t.$AgencyRulesTable.ForeignKey1$=tt.$AgencyRulesTable.ForeignKey1$
	   	AND   t.$AgencyRulesTable.ForeignKey2$=tt.$AgencyRulesTable.ForeignKey2$)
/

merge into $AgencyRulesTable.tableName$ rt
using TMP_$AgencyRulesTable.tableName$ tt
on (rt.payment_template_fk  = tt.payment_template_fk
and rt.payment_frequency_id = tt.payment_frequency_id)
when matched then
update set rt.obsolete = tt.obsolete
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TMP_$AgencyRulesTable.tableName$

/
COMMIT