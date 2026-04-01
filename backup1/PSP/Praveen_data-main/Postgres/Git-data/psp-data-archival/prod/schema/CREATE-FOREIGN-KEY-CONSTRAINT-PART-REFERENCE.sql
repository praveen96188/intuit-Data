alter table pspadm.psp_financial_transaction
add constraint psp_financial_transaction_fk6 foreign key (paycheck_split_fk, realm_id) 
references pspadm.psp_paycheck_split (paycheck_split_seq, realm_id);

alter table pspadm.psp_financial_transaction
add constraint psp_financial_transaction_fk10 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_financial_transaction
add constraint psp_financialtransaction_fk3 foreign key (original_transaction_fk, realm_id) 
references pspadm.psp_financial_transaction (financial_transaction_seq, realm_id);

alter table pspadm.psp_financial_transaction
add constraint psp_financialtransaction_fk2 foreign key (relatable_transaction_fk, realm_id) 
references pspadm.psp_financial_transaction (financial_transaction_seq, realm_id);


alter table pspadm.psp_qbdt_transaction_info
add constraint psp_qbdt_transaction_info_fk3 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_qbdt_transaction_info
add constraint psp_qbdt_transaction_info_fk4 foreign key (financial_transaction_fk, realm_id) 
references pspadm.psp_financial_transaction (financial_transaction_seq, realm_id);


alter table pspadm.psp_tax
add constraint psp_tax_fk2 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);

alter table pspadm.psp_tax_payment_on_hold_reason
add constraint psp_taxpaymentonholdreason_fk1 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_tp401k_batch_paycheck
add constraint psp_third_party401k_batch__fk2 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);

alter table pspadm.psp_tp401k_paycheck
add constraint psp_tp401k_pchk_pchk_fk foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);

alter table pspadm.psp_transaction_offload_batch
add constraint psp_transaction_offload_ba_fk1 foreign key (financial_transaction_fk, realm_id) 
references pspadm.psp_financial_transaction (financial_transaction_seq, realm_id);

alter table pspadm.psp_transaction_return
add constraint psp_transaction_return_fk1 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_voided_check
add constraint psp_voided_check_fk2 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_wc_paycheck
add constraint psp_wc_pchk_pchk_fk foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);






alter table pspadm.psp_atfpayments_to_process
add constraint psp_atfpayments_to_process_fk1 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_compensation
add constraint psp_compensation_fk1 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);

alter table pspadm.psp_deduction
add constraint psp_deduction_fk1 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);


alter table pspadm.psp_edi_payment_detail
add constraint psp_edi_payment_detail_fk3 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_eftps_payment_detail
add constraint psp_eftps_payment_detail_fk4 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_employer_contribution
add constraint psp_employercontribution_fk2 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);


alter table pspadm.psp_entry_detail_record
add constraint psp_entry_detail_record_fk3 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_financial_trans_state
add constraint psp_financial_transaction__fk5 foreign key (financial_transaction_fk, realm_id) 
references pspadm.psp_financial_transaction (financial_transaction_seq, realm_id);

alter table pspadm.psp_fintxn_onholdreason_assoc
add constraint psp_fin_txn_hold_fk_fin_txn foreign key (financial_transaction_fk, realm_id) 
references pspadm.psp_financial_transaction (financial_transaction_seq, realm_id);

alter table pspadm.psp_fset_filing_detail
add constraint psp_fset_filing_detail_fk3 foreign key (money_movement_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_money_movement_transaction
add constraint psp_moneymovementtransacti_fk1 foreign key (original_transaction_fk, realm_id) 
references pspadm.psp_money_movement_transaction (money_movement_transaction_seq, realm_id);

alter table pspadm.psp_paycheck_split
add constraint psp_paycheck_split_fk2 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);

alter table pspadm.psp_paystub
add constraint psp_paystub_fk3 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);

alter table pspadm.psp_qbdt_paycheck_info
add constraint psp_qbdt_paycheck_info_fk1 foreign key (paycheck_fk, realm_id) 
references pspadm.psp_paycheck (paycheck_seq, realm_id);





























