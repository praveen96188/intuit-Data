--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.000.000.057.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE PSP_PAYEE SET IS1099=0;