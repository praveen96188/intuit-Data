--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.20_To_1.9.9.21.sql
--
-- Developers can hand code logic here for data migration purposes
--
-- Removing Column PIN from the Company Table and creating a Company_PIN table
-- Values stored in the previous PIN column have to be copied to the new table

-- Create temporary table to store existing PINs
CREATE TABLE "TEMP_PIN"
(
	PRIMARY KEY(COMPANY_SEQ)
	, "COMPANY_SEQ" VARCHAR2(255 CHAR)  NOT NULL 
	, "PIN" VARCHAR2(100 CHAR) NOT NULL   
)
/
INSERT INTO "TEMP_PIN" SELECT COMPANY_SEQ, P_I_N FROM PSP_COMPANY WHERE P_I_N IS NOT NULL
/
