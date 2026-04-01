--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.007.003.sql
--
-- Developers can hand code logic here for data migration purposes
--

Update PSP_PAYROLL_RUN
   Set PROCESSED_BY_FRAUD_BATCH_JOB = 0
 Where PAYROLL_FRAUD_BATCH_TOKEN > (Select Max(MAX_PROCESSED_TOKEN)
                                      From PSP_PAYROLL_FRAUD_BATCH);

Update PSP_PAYROLL_RUN
   Set PROCESSED_BY_FRAUD_BATCH_JOB = 1
 Where PAYROLL_FRAUD_BATCH_TOKEN <= (Select Max(MAX_PROCESSED_TOKEN)
                                       From PSP_PAYROLL_FRAUD_BATCH);

Commit;