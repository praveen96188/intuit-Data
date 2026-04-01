--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.20_To_1.9.9.21.sql
--
-- Developers can hand code logic here for data migration purposes
--
-- Removing Column PIN from the Company Table and creating a Company_PIN table
-- Values stored in the previous PIN column have to be copied to the new table
INSERT INTO PSP_COMPANY_PIN (
   COMPANY_PIN_SEQ, VERSION,
   CREATED_DATE,  MODIFIED_DATE, 
   P_I_N_VALUE, COMPANY_FK) 
SELECT COMPANY_SEQ, 0, SYSDATE, SYSDATE, PIN, COMPANY_SEQ FROM TEMP_PIN   
/
DROP TABLE TEMP_PIN
/