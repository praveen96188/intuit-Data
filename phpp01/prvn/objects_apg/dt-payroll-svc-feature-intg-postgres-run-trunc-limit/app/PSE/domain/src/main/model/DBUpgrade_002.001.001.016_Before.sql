--
-- This script will be executed BEFORE the automatically generated
-- D:\Dev\PSP\dev\PSE\Domain\src\main\model\DBUpgrade_002.001.001.016.sql
--
-- Developers can hand code logic here for data migration purposes
--
DECLARE
   
   	index_exists PLS_INTEGER;
   
BEGIN
   	SELECT COUNT(*) INTO index_exists
   	FROM "USER_INDEXES"
   	WHERE INDEX_NAME = 'PSP_COMPANY_USAGE_U1';
   
   	IF index_exists = 1 THEN
   		EXECUTE IMMEDIATE 'DROP INDEX "PSP_COMPANY_USAGE_U1"';
   	END IF;
   
END;
/
COMMIT;
/