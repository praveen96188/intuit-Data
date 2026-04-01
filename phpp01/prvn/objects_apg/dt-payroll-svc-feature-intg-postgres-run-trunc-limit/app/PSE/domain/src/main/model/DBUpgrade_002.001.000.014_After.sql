--
-- This script will be executed AFTER the automatically generated
-- D:\Dev\PSP\dev\PSE\Domain\src\main\model\DBUpgrade_002.001.000.014.sql
--
-- Developers can hand code logic here for data migration purposes
--

update PSP_TAX_COMPANY_SERVICE_INFO set LAST_QUARTER_TO_FILE = 0 where LAST_QUARTER_TO_FILE is null;

update PSP_TAX_COMPANY_SERVICE_INFO set FILE_ANNUAL_RETURNS = 0 where FILE_ANNUAL_RETURNS is null;

update PSP_TAX_COMPANY_SERVICE_INFO set FINAL_ANNUAL_RETURNS = 0 where FINAL_ANNUAL_RETURNS is null;

                
