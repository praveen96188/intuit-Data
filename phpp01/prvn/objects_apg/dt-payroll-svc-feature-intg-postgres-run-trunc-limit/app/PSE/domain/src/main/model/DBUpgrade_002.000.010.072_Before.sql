--
-- This script will be executed BEFORE the automatically generated
-- D:\dev\PSP\rel-1.10\PSE\Domain\src\main\model\DBUpgrade_002.000.010.072.sql
--
-- Developers can hand code logic here for data migration purposes
--

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;

update psp_company_offering set offering_fk='f3dfab37-0002-4e03-b6e4-996bf320804d' where offering_fk='bb56f2c0-159b-437f-b30d-5e12243bb22a'
/
update psp_company_service set service_fk='Tax' where service_fk='Assisted'
/
commit
/

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual;

PROMPT finishedDBUpgrade_002.000.010.072_Before.sql