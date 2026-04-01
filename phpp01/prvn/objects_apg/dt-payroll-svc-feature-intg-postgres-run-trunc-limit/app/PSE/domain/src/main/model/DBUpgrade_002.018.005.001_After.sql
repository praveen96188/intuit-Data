--
-- This script will be executed AFTER the automatically generated
-- C:\Users\snasim\Documents\GitHub\Payroll-Services\PSE\domain\src\main\model\DBUpgrade_002.018.005.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE PSP_COMPANY SET D_D_PUBLISH_FLAG = 0;
COMMIT;