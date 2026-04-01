--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\rel-1.10\PSE\Domain\src\main\model\DBUpgrade_002.000.010.074.sql
--
-- Developers can hand code logic here for data migration purposes
--

-- populate temp table
select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;



select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual;

PROMPT finishedDBUpgrade_002.000.000.074_After.sql

