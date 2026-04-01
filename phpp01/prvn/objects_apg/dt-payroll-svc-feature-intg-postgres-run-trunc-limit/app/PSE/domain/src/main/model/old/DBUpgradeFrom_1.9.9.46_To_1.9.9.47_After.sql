--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\v1-maint\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.46_To_1.9.9.47.sql
--
-- Developers can hand code logic here for data migration purposes
--

Update PSP_COMPANY
   Set DEBUG_LOGGING = 0
 Where DEBUG_LOGGING is Null; 