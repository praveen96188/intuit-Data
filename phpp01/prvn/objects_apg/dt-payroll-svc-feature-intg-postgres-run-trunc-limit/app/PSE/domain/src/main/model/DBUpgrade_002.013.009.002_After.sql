--
-- This script will be executed AFTER the automatically generated
-- D:\Dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.009.002.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt populating new table PSP_EMP_TOTALS_PAYROLL_RUN;
declare 
	v_db_name varchar2(100);
	v_ee_system_token number;
begin
	select name into v_db_name from v$database;
	if(v_db_name in ('PSPQA01', 'PSPQA06','PSPTRN01','PSPDV03','PSPDEV01','PSPPP01','XE','ORCL'))
	then
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
		commit;	
	ELSE 
		dbms_output.put_line( 'This is not a QA/DEV env - post_deploy_2013R9.sql needs to be run manually after the automatic deploy');
	END IF;
END;
/
	