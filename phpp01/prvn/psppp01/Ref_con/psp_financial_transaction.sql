\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk16 foreign key (company_law_fk) references pspadm.psp_company_law (company_law_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk13 foreign key (bill_payment_split_fk) references pspadm.psp_bill_payment_split (bill_payment_split_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk14 foreign key (tax_penalty_interest_fk) references pspadm.psp_tax_penalty_interest (tax_penalty_interest_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financialtransaction_fk3 foreign key (company_fk,original_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk8 foreign key (law_fk) references pspadm.psp_law (law_id) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk11 foreign key (comp_adjust_submission_fk) references pspadm.psp_comp_adjust_submission (comp_adjust_submission_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk6 foreign key (company_fk,paycheck_split_fk) references pspadm.psp_paycheck_split (company_fk,paycheck_split_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financialtransaction_fk2 foreign key (company_fk,relatable_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk9 foreign key (current_transaction_state_fk) references pspadm.psp_transaction_state (transaction_state_cd) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk7 foreign key (transaction_type_fk) references pspadm.psp_transaction_type (transaction_type_cd) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk5 foreign key (payroll_run_fk) references pspadm.psp_payroll_run (payroll_run_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk4 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk3 foreign key (debit_bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk2 foreign key (credit_bank_account_fk) references pspadm.psp_bank_account (bank_account_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financial_transaction_fk10 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_transaction add constraint psp_financialtransaction_fk1 foreign key (billing_detail_fk) references pspadm.psp_billing_detail (billing_detail_seq) ON DELETE NO ACTION ;

alter table pspadm.psp_transaction_offload_batch add constraint psp_transaction_offload_ba_fk1 foreign key (company_fk,financial_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_financial_trans_state add constraint psp_financial_transaction__fk5 foreign key (company_fk,financial_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk4 foreign key (company_fk,financial_transaction_fk) references pspadm.psp_financial_transaction (company_fk,financial_transaction_seq) ON DELETE NO ACTION ;

SELECT CURRENT_TIMESTAMP;
