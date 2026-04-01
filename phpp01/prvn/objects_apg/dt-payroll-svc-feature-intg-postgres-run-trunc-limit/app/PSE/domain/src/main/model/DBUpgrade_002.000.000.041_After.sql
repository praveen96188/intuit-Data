--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.000.000.041.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE PSP_DEDUCTION SET DEDUCTION_Y_T_D_AMOUNT = 0, PAY_STUB_ORDER = 0;
UPDATE PSP_COMPENSATION SET COMPENSATION_Y_T_D_AMOUNT = 0, PAY_STUB_ORDER = 0;
UPDATE PSP_TAX SET TAX_LIABILITY_Y_T_D_AMOUNT = 0, PAY_STUB_ORDER = 0;

