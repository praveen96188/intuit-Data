--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.001.000.002.sql
--
-- Developers can hand code logic here for data migration purposes
--

-- INIT NUMBER COLUMN
/* 

UPDATE PSP_ENTITLEMENT_MESSAGE SET TOKEN = 0
/

COMMIT
/

 */
 