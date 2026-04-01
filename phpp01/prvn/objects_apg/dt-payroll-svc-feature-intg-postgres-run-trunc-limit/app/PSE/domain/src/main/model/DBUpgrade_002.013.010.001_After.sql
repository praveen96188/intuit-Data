--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.010.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
Update PSP_COMPANY_LAW_RATE
Set RATE_TYPE = 'Percentage'
Where RATE_TYPE is null;
Commit;