--
-- This script will be executed AFTER the automatically generated
-- C:\Users\snasim\Documents\GitHub\Payroll-Services\PSE\domain\src\main\model\DBUpgrade_002.019.006.001.sql
--
-- Developers can hand code logic here for data migration purposes
--

-- Below Index was created manually before the deployment to avoid cutting down the time taken during deployment

-- CREATE INDEX PSP_PAYCHECK_USAGE_NU1 ON PSP_PAYCHECK_USAGE (SOURCE_PAYCHECK_ID) PARALLEL 32 ONLINE;