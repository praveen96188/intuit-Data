--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\rel-1.8\PSE\Domain\src\main\model\DBUpgrade_002.000.000.060.sql
--
-- Developers can hand code logic here for data migration purposes
--
DELETE FROM PSP_FRAUD_BANK_ACCOUNT;

INSERT INTO PSP_FRAUD_BANK_ACCOUNT 
       (FRAUD_BANK_ACCOUNT_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, ROUTING_NUMBER, ACCOUNT_NUMBER, ACCOUNT_TYPE_CD, 
	    BANK_NAME, BANK_ACCOUNT_OWNER_NAME, FRAUD_BANK_ACCOUNT_REASON, COMPANY_FK)
SELECT 
	   employeeba2_.employee_bank_account_seq,
       employeeba2_.VERSION,
       employeeba2_.creator_id,
       employeeba2_.created_date,
       employeeba2_.modifier_id,
       employeeba2_.modified_date,
       employeeba2_.realm_id,
       bankaccoun3_.routing_number,
       bankaccoun3_.account_number,
       bankaccoun3_.account_type_cd,
	   bankaccoun3_.bank_name,
	   TRIM(NVL(employee1_1_.last_name, '') || ', ' || NVL(employee1_1_.first_name,'') || ' ' || NVL(employee1_1_.middle_name, '')),
	   'EmployeeBankAccountOfTerminatedCompany',
	   employee1_.company_fk
FROM psp_company_service companyser0_
       inner join psp_employee employee1_ on employee1_.company_fk = companyser0_.company_fk 
       INNER JOIN psp_individual employee1_1_ ON employee1_.employee_seq = employee1_1_.individual_seq
       INNER JOIN psp_employee_bank_account employeeba2_ ON employee1_.employee_seq = employeeba2_.employee_fk
       INNER JOIN psp_bank_account bankaccoun3_ ON employeeba2_.bank_account_fk = bankaccoun3_.bank_account_seq
 WHERE companyser0_.service_fk = 'DirectDeposit'
       AND companyser0_.status_cd = 'Terminated'; 

commit;
	   
select count(*) from psp_fraud_bank_account;