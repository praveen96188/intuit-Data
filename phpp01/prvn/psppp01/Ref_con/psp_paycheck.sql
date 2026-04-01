\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


alter table pspadm.psp_paycheck add constraint psp_paycheck_fk4 foreign key (source_employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk5 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk2 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk1 foreign key (d_d_employee_fk) references pspadm.psp_employee (employee_seq) ON DELETE NO ACTION;
alter table pspadm.psp_paycheck add constraint psp_paycheck_fk3 foreign key (comp_adjust_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION;

alter table pspadm.psp_compensation add constraint psp_compensation_fk1 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_deduction add constraint psp_deduction_fk1 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_employer_contribution add constraint psp_employercontribution_fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_paystub add constraint psp_paystub_fk3 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_qbdt_paycheck_info add constraint psp_qbdt_paycheck_info_fk1 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_tax add constraint psp_tax_fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_tp401k_batch_paycheck add constraint psp_third_party401k_batch__fk2 foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_tp401k_paycheck add constraint psp_tp401k_pchk_pchk_fk foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_wc_paycheck add constraint psp_wc_pchk_pchk_fk foreign key (company_fk,paycheck_fk) references pspadm.psp_paycheck (company_fk,paycheck_seq) ON DELETE NO ACTION ;


SELECT CURRENT_TIMESTAMP;
