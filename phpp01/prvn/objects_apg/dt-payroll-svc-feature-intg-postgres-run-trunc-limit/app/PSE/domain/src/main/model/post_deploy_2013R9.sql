--
-- This script will be executed AFTER the automatically generated
-- D:\Dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.009.002.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt populating new table PSP_EMP_TOTALS_PAYROLL_RUN;
declare 
	v_ee_system_token number;
begin
	select to_number(SYSTEM_PARAMETER_VALUE) into v_ee_system_token from psp_system_parameter where system_parameter_cd='EMPLOYEE_CALCULATION_TOKEN';

	insert into PSP_EMP_TOTALS_PAYROLL_RUN 
	(EMP_TOTALS_PAYROLL_RUN_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, 
		QUARTER_START_DATE, 
		STATUS, 
		PAYROLL_RUN_FK, COMPANY_FK)
	(select FN_FORMAT_SYSGUID(sys_guid()), 1, 'InitialInsert', FN_GET_PSP_TIMESTAMP(), 'InitialInsert', FN_GET_PSP_TIMESTAMP(), 
		TRUNC (pr.paycheck_date, 'Q') + decode (to_char(pr.paycheck_date, 'Q'), '1', 8/24,  7/24), 
		case when (e_e_calculation_token > v_ee_system_token ) then 'Pending'
			else 'Processed' end,
		PR.PAYROLL_RUN_SEQ, PR.COMPANY_FK
		from psp_payroll_run pr
	where to_char(PR.PAYCHECK_DATE, 'YYYY') = '2013'
			and e_e_calculation_token > -1 );	
	
	Prompt Index PSP_EMP_TOTALS_PAYROLL_RUN_FK1;
	CREATE INDEX PSP_EMP_TOTALS_PAYROLL_RUN_FK1 ON PSP_EMP_TOTALS_PAYROLL_RUN
	(PAYROLL_RUN_FK, REALM_ID)
	NOPARALLEL;

	Prompt Index PSP_EMP_TOTALS_PAYROLL_RUN_FK2;
	CREATE INDEX PSP_EMP_TOTALS_PAYROLL_RUN_FK2 ON PSP_EMP_TOTALS_PAYROLL_RUN
	(COMPANY_FK, REALM_ID)
	NOPARALLEL;

	ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
	 ADD CONSTRAINT C_PSP_EMP_TOTALS_PAYROLL_R0
	  CHECK (STATUS IN('Pending', 'Processed')) NOValidate;

	ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
	 ADD PRIMARY KEY
	  (EMP_TOTALS_PAYROLL_RUN_SEQ, REALM_ID)
	  USING INDEX;

	ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
	 ADD CONSTRAINT PSP_EMP_TOTALS_PAYROLL_RUN_FK1 
	  FOREIGN KEY (PAYROLL_RUN_FK, REALM_ID) 
	  REFERENCES PSP_PAYROLL_RUN (PAYROLL_RUN_SEQ,REALM_ID);

	ALTER TABLE PSP_EMP_TOTALS_PAYROLL_RUN
	 ADD CONSTRAINT PSP_EMP_TOTALS_PAYROLL_RUN_FK2 
	  FOREIGN KEY (COMPANY_FK, REALM_ID) 
	  REFERENCES PSP_COMPANY (COMPANY_SEQ,REALM_ID);
	  
	commit;	
END;
/
	