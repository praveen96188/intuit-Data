--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\rel-1.10\PSE\Domain\src\main\model\DBUpgrade_002.000.000.070.sql
--
-- Developers can hand code logic here for data migration purposes
--
select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;

UPDATE PSP_QBDT_TRANSACTION_INFO SET TOKEN = 0; 

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;