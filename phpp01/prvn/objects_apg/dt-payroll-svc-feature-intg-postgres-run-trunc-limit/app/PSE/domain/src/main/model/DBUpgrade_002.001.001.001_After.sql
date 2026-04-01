--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\dev-2012R7\PSE\Domain\src\main\model\DBUpgrade_002.001.001.001.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_LAW SET EDITABLE_RATES = 0;
UPDATE PSP_TAX_COMPANY_SERVICE_INFO SET FILE_ANNUAL_RETURNS = 0;
UPDATE PSP_TAX_COMPANY_SERVICE_INFO SET FINAL_ANNUAL_RETURNS = 0;
UPDATE PSP_TAX_COMPANY_SERVICE_INFO SET LAST_TAX_YEAR = 0;
COMMIT;