--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\rel-1.10\PSE\Domain\src\main\model\DBUpgrade_002.000.010.077.sql
--
-- Developers can hand code logic here for data migration purposes
--

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;


-- initialize new boolean columns
UPDATE PSP_QBDT_TRANSACTION_INFO SET IS_DIRECT_DEPOSIT = 0;
UPDATE PSP_QBDT_TRANSACTION_INFO SET SYSTEM_GENERATED = 0;
UPDATE PSP_LIABILITY_ADJUSTMENT SET IS_RECONCILING_ADJUSTMENT = 0;

commit;

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;


PROMPT finishedDBUpgrade_002.000.010.077_After.sql

