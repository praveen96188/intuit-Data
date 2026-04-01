--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.014.008.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
ALTER TABLE PSP_BATCH_JOB_STATUS
 ADD CONSTRAINT C_PSP_BATCH_JOB_STATUS1
  UNIQUE (JOB_TYPE);
  
ALTER TABLE PSP_BATCH_JOB_STATUS MODIFY IS_RUNNING DEFAULT 0;

