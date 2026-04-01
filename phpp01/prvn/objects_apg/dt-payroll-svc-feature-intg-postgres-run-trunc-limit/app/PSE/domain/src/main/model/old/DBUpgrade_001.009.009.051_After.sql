--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\rel-1.3\PSE\Domain\src\main\model\DBUpgrade_001.009.009.051.sql
--
-- Developers can hand code logic here for data migration purposes
--


Prompt Constraint C_PSP_ENTRY_DETAIL_RECORD0;
ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 DROP CONSTRAINT C_PSP_ENTRY_DETAIL_RECORD0;
ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 ADD CONSTRAINT C_PSP_ENTRY_DETAIL_RECORD0
 CHECK (N_A_C_H_A_FILE_TYPE IN('CCD', 'PPD'));

ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 ADD CONSTRAINT C_PSP_ENTRY_DETAIL_RECORD1
 CHECK (N_A_C_H_A_BATCH_TYPE IN('BookTransfer', 'Payroll', 'Reversal'));

ALTER TABLE PSP_ENTRY_DETAIL_RECORD
 ADD CONSTRAINT C_PSP_ENTRY_DETAIL_RECORD2
 CHECK (CREDIT_DEBIT_INDICATOR IN('Credit', 'Debit'));
 

-- CREATing a new sequence from old

DECLARE

v_startval number(38);
v_sql_Str varchar2(4000);

BEGIN

	SELECT SEQ_TRACE_NBR.NEXTVAL+1 into v_startval FROM DUAL;

	v_sql_str := 'CREATE SEQUENCE SEQ_TRACE_NUMBER '||
		     ' START WITH '|| v_startval ||
		     ' MAXVALUE 999999999999999999 '||
                     ' MINVALUE 0 '||
                     ' NOCYCLE '||
                     ' CACHE 10000  '||
                     ' ORDER ';
                     
       execute immediate v_sql_str;
end;
/

