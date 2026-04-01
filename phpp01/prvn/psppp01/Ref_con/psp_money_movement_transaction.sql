\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


--alter table pspadm.psp_money_movement_transaction add constraint psp_moneymovementtransacti_fk1 foreign key (company_fk,original_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
--alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk6 foreign key (payment_frequency_fk) references pspadm.psp_pmt_template_frequency (payment_template_frequency_id) ON DELETE NO ACTION;
--alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk3 foreign key (offload_batch_fk) references pspadm.psp_offload_batch (offload_batch_seq) ON DELETE NO ACTION;
----alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk2 foreign key (company_fk) references pspadm.psp_company (company_seq) ON DELETE NO ACTION;
alter table pspadm.psp_money_movement_transaction add constraint psp_money_movement_transac_fk5 foreign key (payment_template_fk) references pspadm.psp_payment_template (payment_template_cd) ON DELETE NO ACTION;

alter table pspadm.psp_atfpayments_to_process add constraint psp_atfpayments_to_process_fk1 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_edi_payment_detail add constraint psp_edi_payment_detail_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_eftps_payment_detail add constraint psp_eftps_payment_detail_fk4 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_entry_detail_record add constraint psp_entry_detail_record_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_fset_filing_detail add constraint psp_fset_filing_detail_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_payment_batch_assoc add constraint psp_payment_batch_assoc_fk2 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_qbdt_transaction_info add constraint psp_qbdt_transaction_info_fk3 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_tax_payment_on_hold_reason add constraint psp_taxpaymentonholdreason_fk1 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_transaction_return add constraint psp_transaction_return_fk1 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;
alter table pspadm.psp_voided_check add constraint psp_voided_check_fk2 foreign key (company_fk,money_movement_transaction_fk) references pspadm.psp_money_movement_transaction (company_fk,money_movement_transaction_seq) ON DELETE NO ACTION ;


SELECT CURRENT_TIMESTAMP;
