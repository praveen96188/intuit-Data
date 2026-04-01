--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.001.000.007.sql
--
-- Developers can hand code logic here for data migration purposes
--
/* 

--migrate current user single role to first item in collection of roles
INSERT INTO PSP_AUTH_USER_AUTH_ROLE__ASSOC (AUTH_USER_FK, AUTH_ROLE_FK)
SELECT AUTH_USER_SEQ, AUTH_ROLE_FK from PSP_AUTH_USER;


--now delete the redundant column
ALTER TABLE PSP_AUTH_USER
 DROP CONSTRAINT PSP_AUTH_USER_FK1;
 
Prompt drop Index PSP_AUTH_USER_FK1;
DROP INDEX PSP_AUTH_USER_FK1; 

Prompt Column AUTH_ROLE_FK;
ALTER TABLE PSP_AUTH_USER DROP COLUMN AUTH_ROLE_FK;

*/
