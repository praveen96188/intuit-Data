\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

alter table pspadm.psp_paycheck_split add constraint psp_paycheck_split_fk3 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck_split add constraint psp_paycheck_split_fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_paycheck_split add constraint psp_paycheck_split_fk1 foreign key (employee_bank_account_fk) references pspadm.psp_employee_bank_account (employee_bank_account_seq) ON DELETE NO ACTION;

SELECT CURRENT_TIMESTAMP;
