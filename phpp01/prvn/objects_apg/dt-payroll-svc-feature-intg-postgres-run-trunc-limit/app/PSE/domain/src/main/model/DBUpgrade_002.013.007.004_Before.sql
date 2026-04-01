--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.007.004.sql
--
-- Developers can hand code logic here for data migration purposes
--

-- DROP SYNONYM PSPAPP.SEQ_PAYROLL_FRAUD_BATCH_TOKEN; 

DROP SEQUENCE SEQ_PAYROLL_FRAUD_BATCH_TOKEN; 

DROP TRIGGER TR_INS_PAYROLL_FRAUD_TOKEN;

DROP TRIGGER TR_UPD_PAYROLL_FRAUD_TOKEN;

COMMIT;