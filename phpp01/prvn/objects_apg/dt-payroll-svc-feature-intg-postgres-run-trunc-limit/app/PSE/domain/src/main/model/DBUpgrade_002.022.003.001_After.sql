--
-- This script will be executed AFTER the automatically generated
-- C:\Users\rtbteam\dt-payroll-svc\app\PSE\domain\src\main\model\DBUpgrade_002.022.003.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
INSERT INTO PSPADM.PSP_SERVICE (SERVICE_CD, VERSION, REALM_ID, DESCRIPTION, NAME, CAN_BE_MANUALLY_CANCELLED, PSP_PROVIDES_CUSTOMER_SERVICE) VALUES ('Guideline401k', 0, -1, 'A METHOD TO TRANSFER 401K DATA TO GUIDELINE', 'Guideline 401k Service', 0, 0);