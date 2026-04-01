--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\rel-1.0.1\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.36_To_1.9.9.37.sql
--
-- Developers can hand code logic here for data migration purposes
--
DECLARE

	index_exists PLS_INTEGER;

BEGIN
	SELECT COUNT(*) INTO index_exists
	FROM "USER_INDEXES"
	WHERE INDEX_NAME = 'PSP_MAX_TOKEN_TEMP';

	IF index_exists = 1 THEN
		null;
	ELSE 
		EXECUTE IMMEDIATE 'CREATE INDEX PSP_MAX_TOKEN_TEMP ON PSP_PAYROLL_RUN (PAYROLL_FRAUD_BATCH_TOKEN)';
	END IF;
END;
/

alter sequence SEQ_PAYROLL_FRAUD_BATCH_TOKEN NOCACHE

/

alter sequence SEQ_TXN_TOKEN_NBR NOCACHE

/