--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.42_To_1.9.9.43.sql
--
-- Developers can hand code logic here for data migration purposes
--

-- MIGR 62 script will be used  ratherthan running the below statements.

-- PROMPT Before update 

-- SELECT COUNT(*) FROM PSP_COMPANY
-- where MIGRATION_STATUS='MigratedFromAS400'  and AGREE_INFO_AGREE_SUB_TYPE is null;


-- update psp_company set AGREE_INFO_AGREE_SUB_TYPE='Assisted' 
-- where MIGRATION_STATUS='MigratedFromAS400'  and AGREE_INFO_AGREE_SUB_TYPE is null;


-- PROMPT AFTER update 

-- SELECT COUNT(*) FROM PSP_COMPANY
-- where MIGRATION_STATUS='MigratedFromAS400'  and AGREE_INFO_AGREE_SUB_TYPE is null;

-- commit;
