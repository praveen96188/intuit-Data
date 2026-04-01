--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\rel-1.11\PSE\Domain\src\main\model\DBUpgrade_002.000.011.009.sql
--
-- Developers can hand code logic here for data migration purposes
--
-- Insert one row for each company tax service into PSP_TAX_COMPANY_SERVICE_INFO with a last tax quarter = 0
INSERT INTO PSP_TAX_COMPANY_SERVICE_INFO (
   TAX_COMPANY_SERVICE_INFO_SEQ, REALM_ID, LAST_TAX_QUARTER) 
SELECT CS.COMPANY_SERVICE_SEQ, CS.REALM_ID, 0
FROM PSP_COMPANY_SERVICE cs
WHERE CS.SERVICE_FK = 'Tax'
/
COMMIT
/