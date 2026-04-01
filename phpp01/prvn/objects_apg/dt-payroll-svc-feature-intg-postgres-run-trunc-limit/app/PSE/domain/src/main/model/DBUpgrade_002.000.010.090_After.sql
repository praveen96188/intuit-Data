--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\rel-1.10\PSE\Domain\src\main\model\DBUpgrade_002.000.010.090.sql
--
-- Developers can hand code logic here for data migration purposes
--
DECLARE
    
    index_exists PLS_INTEGER;

BEGIN
    SELECT COUNT(*) INTO index_exists
    FROM "USER_INDEXES"
    WHERE INDEX_NAME = 'PSP_FRAUD_EVENT_I1';

    IF index_exists = 1 THEN
        EXECUTE IMMEDIATE 'DROP INDEX "PSP_FRAUD_EVENT_I1"';
    END IF;

END;
/

CREATE INDEX PSP_FRAUD_EVENT_I1 ON PSP_FRAUD_EVENT (EVENT_STATUS_CD, EVENT_TIME_STAMP)
/