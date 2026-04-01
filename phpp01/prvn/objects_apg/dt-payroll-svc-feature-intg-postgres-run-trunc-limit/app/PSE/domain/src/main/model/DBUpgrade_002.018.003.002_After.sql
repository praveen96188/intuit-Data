--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\Payroll-Services\PSE\domain\src\main\model\DBUpgrade_002.018.003.002.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt Index PSP_COMPANY_FedTaxidEnc_I1;
CREATE INDEX PSP_COMPANY_FedTaxidEnc_I1 ON PSP_COMPANY
(FED_TAX_ID_ENC )
NOPARALLEL;