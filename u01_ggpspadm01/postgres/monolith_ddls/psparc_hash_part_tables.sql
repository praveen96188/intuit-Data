\timing
set search_path=psparc;
SELECT CURRENT_TIMESTAMP;
--psparc.psp_ledger_balance

CREATE TABLE psparc.psp_ledger_balance(
    ledger_balance_seq CHARACTER VARYING(255) ,
    version int,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    balance_amount NUMERIC(19,4),
    balance_date TIMESTAMP(6) WITHOUT TIME ZONE,
    ledger_account_fk CHARACTER VARYING(255) ,
    company_fk CHARACTER VARYING(255) ,
    reporting_type CHARACTER VARYING(100) DEFAULT 'DirectDeposit'
)
    PARTITION BY hash (company_fk) ;

-- create partitions
CREATE TABLE psparc.psp_ledger_balance_p0 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE psparc.psp_ledger_balance_p1 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE psparc.psp_ledger_balance_p2 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE psparc.psp_ledger_balance_p3 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE psparc.psp_ledger_balance_p4 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE psparc.psp_ledger_balance_p5 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE psparc.psp_ledger_balance_p6 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE psparc.psp_ledger_balance_p7 PARTITION OF psparc.psp_ledger_balance FOR VALUES WITH (MODULUS 8, REMAINDER 7);


--PSP_MONEY_MOVEMENT_TRANSACTION

CREATE TABLE psparc.psp_money_movement_transaction(
    money_movement_transaction_seq CHARACTER VARYING(255) ,
    version int ,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    due_date TIMESTAMP(6) WITHOUT TIME ZONE,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    mm_transaction_amount NUMERIC(19,4),
    status CHARACTER VARYING(255),
    money_movement_payment_method CHARACTER VARYING(255),
    original_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    deposit_frequency_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    offload_batch_fk CHARACTER VARYING(255),
    tax_payment_status CHARACTER VARYING(255),
    tax_pmtstatus_effectivedate TIMESTAMP(6) WITHOUT TIME ZONE,
    reference_number CHARACTER VARYING(4000),
    manual_payment_status CHARACTER VARYING(255),
    payment_frequency_fk CHARACTER VARYING(255),
    original_transaction_fk CHARACTER VARYING(255),
    payment_period_begin TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_period_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_template_fk CHARACTER VARYING(255),
    agency_taxpayer_id_enc CHARACTER VARYING(4000),
    agency_taxpayer_id CHARACTER VARYING(80),
    transaction_number CHARACTER VARYING(30),
    record_metadata CHARACTER VARYING(512)
)
    PARTITION BY hash (company_fk);

-- create partitions
CREATE TABLE psparc.psp_money_movement_transaction_p0 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE psparc.psp_money_movement_transaction_p1 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE psparc.psp_money_movement_transaction_p2 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE psparc.psp_money_movement_transaction_p3 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE psparc.psp_money_movement_transaction_p4 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE psparc.psp_money_movement_transaction_p5 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE psparc.psp_money_movement_transaction_p6 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE psparc.psp_money_movement_transaction_p7 PARTITION OF psparc.psp_money_movement_transaction FOR VALUES WITH (MODULUS 8, REMAINDER 7);



--PSP_ENTRY_DETAIL_RECORD

CREATE TABLE psparc.psp_entry_detail_record(
    entry_detail_record_seq CHARACTER VARYING(255) ,
    version int ,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    trace_number CHARACTER VARYING(20),
    credit_debit_indicator CHARACTER VARYING(255),
    intuit_bank_account_fk CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255) ,
    company_fk CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    n_a_c_h_a_file_type CHARACTER VARYING(255),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    legal_name CHARACTER VARYING(100),
    standard_entry_description CHARACTER VARYING(15),
    txp_record_data_enc CHARACTER VARYING(4000),
    record_data_enc CHARACTER VARYING(4000),
    record_data CHARACTER VARYING(250),
    txp_record_data CHARACTER VARYING(90),
    j_p_m_c_trace_number CHARACTER VARYING(50),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_funding_model CHARACTER VARYING(4000),
    record_metadata CHARACTER VARYING(512)
)
    PARTITION BY hash (company_fk);

-- create partitions
CREATE TABLE psparc.psp_entry_detail_record_p0 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_entry_detail_record_p1 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_entry_detail_record_p2 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_entry_detail_record_p3 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_entry_detail_record_p4 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_entry_detail_record_p5 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_entry_detail_record_p6 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_entry_detail_record_p7 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_entry_detail_record_p8 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_entry_detail_record_p9 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_entry_detail_record_p10 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_entry_detail_record_p11 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_entry_detail_record_p12 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_entry_detail_record_p13 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_entry_detail_record_p14 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_entry_detail_record_p15 PARTITION OF psparc.psp_entry_detail_record FOR VALUES WITH (MODULUS 16, REMAINDER 15);


--psp_financial_trans_state

CREATE TABLE psparc.psp_financial_trans_state(
    financial_trans_state_seq CHARACTER VARYING(255) ,
    version int ,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    transaction_state_eff_date TIMESTAMP(6) WITHOUT TIME ZONE,
    insert_user_id CHARACTER VARYING(30),
    gems_upload_batch_fk CHARACTER VARYING(255),
    financial_transaction_fk CHARACTER VARYING(255) ,
    transaction_state_fk CHARACTER VARYING(255) ,
    transaction_response_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255)
)
    PARTITION BY hash (company_fk) ;

-- create partitions
CREATE TABLE psparc.psp_financial_trans_state_p0 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_financial_trans_state_p1 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_financial_trans_state_p2 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_financial_trans_state_p3 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_financial_trans_state_p4 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_financial_trans_state_p5 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_financial_trans_state_p6 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_financial_trans_state_p7 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_financial_trans_state_p8 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_financial_trans_state_p9 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_financial_trans_state_p10 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_financial_trans_state_p11 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_financial_trans_state_p12 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_financial_trans_state_p13 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_financial_trans_state_p14 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_financial_trans_state_p15 PARTITION OF psparc.psp_financial_trans_state FOR VALUES WITH (MODULUS 16, REMAINDER 15);


--PSP_FINANCIAL_TRANSACTION

CREATE TABLE psparc.psp_financial_transaction(
    financial_transaction_seq CHARACTER VARYING(255) ,
    version int ,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    financial_transaction_amount NUMERIC(19,4),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    settlement_type_cd CHARACTER VARYING(255),
    credit_bank_account_type CHARACTER VARYING(255),
    debit_bank_account_type CHARACTER VARYING(255),
    on_hold SMALLINT,
    sku CHARACTER VARYING(40),
    sku_quantity BIGINT,
    billing_detail_fk CHARACTER VARYING(255),
    credit_bank_account_fk CHARACTER VARYING(255),
    debit_bank_account_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    paycheck_split_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255) ,
    law_fk CHARACTER VARYING(255),
    current_transaction_state_fk CHARACTER VARYING(255),
    original_transaction_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    original_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    refund_type CHARACTER VARYING(255),
    comp_adjust_submission_fk CHARACTER VARYING(255),
    tax_penalty_interest_fk CHARACTER VARYING(255),
    relatable_transaction_fk CHARACTER VARYING(255),
    bill_payment_split_fk CHARACTER VARYING(255),
    company_law_fk CHARACTER VARYING(255)
)
    PARTITION BY hash (company_fk) ;
    
-- create partitions
CREATE TABLE psparc.psp_financial_transaction_p0 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_financial_transaction_p1 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_financial_transaction_p2 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_financial_transaction_p3 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_financial_transaction_p4 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_financial_transaction_p5 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_financial_transaction_p6 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_financial_transaction_p7 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_financial_transaction_p8 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_financial_transaction_p9 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_financial_transaction_p10 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_financial_transaction_p11 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_financial_transaction_p12 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_financial_transaction_p13 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_financial_transaction_p14 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_financial_transaction_p15 PARTITION OF psparc.psp_financial_transaction FOR VALUES WITH (MODULUS 16, REMAINDER 15);


--PSP_PAYCHECK

CREATE TABLE psparc.psp_paycheck(
    paycheck_seq CHARACTER VARYING(255) ,
    version int ,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_paycheck_id CHARACTER VARYING(50),
    pay_period_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    voided_after_offload SMALLINT,
    pay_period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    d_d_employee_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255) ,
    net_amount NUMERIC(19,4),
    comp_adjust_submission_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255),
    gross_amount NUMERIC(19,4),
    y_t_d_gross_amount NUMERIC(19,4),
    y_t_d_net_amount NUMERIC(19,4),
    source_employee_fk CHARACTER VARYING(255),
    is_y_t_d_adjustment SMALLINT,
    company_fk CHARACTER VARYING(255),
    approval_date_time_end TIMESTAMP(6) WITHOUT TIME ZONE,
    d_d_message_status CHARACTER VARYING(255)  DEFAULT 'None',
    session_id CHARACTER VARYING(100)
)
    PARTITION BY hash (company_fk);

-- create partitions
CREATE TABLE psparc.psp_paycheck_p0 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_paycheck_p1 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_paycheck_p2 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_paycheck_p3 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_paycheck_p4 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_paycheck_p5 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_paycheck_p6 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_paycheck_p7 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_paycheck_p8 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_paycheck_p9 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_paycheck_p10 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_paycheck_p11 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_paycheck_p12 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_paycheck_p13 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_paycheck_p14 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_paycheck_p15 PARTITION OF psparc.psp_paycheck FOR VALUES WITH (MODULUS 16, REMAINDER 15);


--PSP_PAYCHECK_SPLIT

CREATE TABLE psparc.psp_paycheck_split(
    paycheck_split_seq CHARACTER VARYING(255) ,
    version int ,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE ,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    paycheck_split_amount NUMERIC(19,4),
    source_dd_txn_id CHARACTER VARYING(50),
    employee_bank_account_fk CHARACTER VARYING(255) ,
    paycheck_fk CHARACTER VARYING(255) ,
    company_fk CHARACTER VARYING(255),
    pay_stub_order integer DEFAULT 0
)
    PARTITION BY hash (company_fk);

-- create partitions
CREATE TABLE psparc.psp_paycheck_split_p0 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_paycheck_split_p1 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_paycheck_split_p2 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_paycheck_split_p3 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_paycheck_split_p4 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_paycheck_split_p5 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_paycheck_split_p6 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_paycheck_split_p7 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_paycheck_split_p8 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_paycheck_split_p9 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_paycheck_split_p10 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_paycheck_split_p11 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_paycheck_split_p12 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_paycheck_split_p13 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_paycheck_split_p14 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_paycheck_split_p15 PARTITION OF psparc.psp_paycheck_split FOR VALUES WITH (MODULUS 16, REMAINDER 15);



--psp_company_event

CREATE TABLE psparc.psp_company_event(
    company_event_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    event_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_cd CHARACTER VARYING(255),
    event_type_cd CHARACTER VARYING(255),
    event_token NUMERIC(19,0),
    source_id CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255) ,
    note_last_updated_date TIMESTAMP(6) WITHOUT TIME ZONE
)
    PARTITION BY HASH (company_fk);


-- create partitions
CREATE TABLE psparc.psp_company_event_p0 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE psparc.psp_company_event_p1 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE psparc.psp_company_event_p2 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE psparc.psp_company_event_p3 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE psparc.psp_company_event_p4 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE psparc.psp_company_event_p5 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE psparc.psp_company_event_p6 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE psparc.psp_company_event_p7 PARTITION OF psparc.psp_company_event FOR VALUES WITH (MODULUS 8, REMAINDER 7);



--PSP_COMPANY_EVENT_DETAIL


CREATE TABLE psparc.psp_company_event_detail(
    company_event_detail_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    value CHARACTER VARYING(4000),
    event_detail_type_cd CHARACTER VARYING(255),
    company_event_fk CHARACTER VARYING(255) NOT NULL,
    event_detail_subtype CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);

-- create partitions
CREATE TABLE psparc.psp_company_event_detail_p0 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE psparc.psp_company_event_detail_p1 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE psparc.psp_company_event_detail_p2 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE psparc.psp_company_event_detail_p3 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE psparc.psp_company_event_detail_p4 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE psparc.psp_company_event_detail_p5 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE psparc.psp_company_event_detail_p6 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE psparc.psp_company_event_detail_p7 PARTITION OF psparc.psp_company_event_detail FOR VALUES WITH (MODULUS 8, REMAINDER 7);



--PSP_COMPANY_EVENT_EMAIL_PARAM

CREATE TABLE psparc.psp_company_event_email_param(
    company_event_email_param_seq CHARACTER VARYING(255) NOT NULL,
    version integer,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    param_type_cd CHARACTER VARYING(255),
    value CHARACTER VARYING(4000),
    company_event_email_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);

-- create partitions
CREATE TABLE psparc.psp_company_event_email_param_p0 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE psparc.psp_company_event_email_param_p1 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE psparc.psp_company_event_email_param_p2 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE psparc.psp_company_event_email_param_p3 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE psparc.psp_company_event_email_param_p4 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE psparc.psp_company_event_email_param_p5 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE psparc.psp_company_event_email_param_p6 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE psparc.psp_company_event_email_param_p7 PARTITION OF psparc.psp_company_event_email_param FOR VALUES WITH (MODULUS 8, REMAINDER 7);




--Task 3

--psp_compensation

CREATE TABLE psparc.psp_compensation(
    compensation_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    compensation_amount NUMERIC(19,4),
    hours_worked NUMERIC(19,7),
    paycheck_fk CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255),
    compensation_y_t_d_amount NUMERIC(19,4),
    pay_stub_order integer,
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);


--create partition 

CREATE TABLE psparc.psp_compensation_p0 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE psparc.psp_compensation_p1 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE psparc.psp_compensation_p2 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE psparc.psp_compensation_p3 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE psparc.psp_compensation_p4 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE psparc.psp_compensation_p5 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE psparc.psp_compensation_p6 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE psparc.psp_compensation_p7 PARTITION OF psparc.psp_compensation FOR VALUES WITH (MODULUS 8, REMAINDER 7);



--PSP_PROPERTY_AUDIT

CREATE TABLE psparc.psp_property_audit(
    property_audit_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    class_name CHARACTER VARYING(100),
    property_name CHARACTER VARYING(100),
    old_property_value CHARACTER VARYING(4000),
    new_property_value CHARACTER VARYING(4000),
    object_identifier CHARACTER VARYING(40),
    user_id CHARACTER VARYING(100),
    audit_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);


--create partition
CREATE TABLE psparc.psp_property_audit_p0 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE psparc.psp_property_audit_p1 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE psparc.psp_property_audit_p2 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE psparc.psp_property_audit_p3 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE psparc.psp_property_audit_p4 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE psparc.psp_property_audit_p5 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE psparc.psp_property_audit_p6 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE psparc.psp_property_audit_p7 PARTITION OF psparc.psp_property_audit FOR VALUES WITH (MODULUS 8, REMAINDER 7);


--psp_qbdt_paycheck_info

CREATE TABLE psparc.psp_qbdt_paycheck_info(
    qbdt_paycheck_info_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    prorate smallint,
    check_number CHARACTER VARYING(11),
    memo CHARACTER VARYING(4000),
    cleared CHARACTER VARYING(1),
    on_service smallint,
    tracking_class CHARACTER VARYING(128),
    account_name CHARACTER VARYING(128),
    paycheck_fk CHARACTER VARYING(255),
    list_id CHARACTER VARYING(38),
    company_fk CHARACTER VARYING(255) ,
    token NUMERIC(19,0) DEFAULT - 2,
    vacation_hours_accrued NUMERIC(19,7) DEFAULT 0,
    sick_hours_accrued NUMERIC(19,7) DEFAULT 0,
    void_token NUMERIC(19,0) DEFAULT - 1,
    is_assisted smallint NOT NULL DEFAULT 0
)
    PARTITION BY HASH (company_fk);

--create partition

CREATE TABLE psparc.psp_qbdt_paycheck_info_p0 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p1 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p2 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p3 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p4 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p5 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p6 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p7 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p8 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p9 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p10 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p11 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p12 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p13 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p14 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_qbdt_paycheck_info_p15 PARTITION OF psparc.psp_qbdt_paycheck_info FOR VALUES WITH (MODULUS 16, REMAINDER 15);

--psp_qbdt_payline_info

CREATE TABLE psparc.psp_qbdt_payline_info(
    qbdt_payline_info_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    rate NUMERIC(19,7),
    tracking_class CHARACTER VARYING(128),
    job CHARACTER VARYING(128),
    item CHARACTER VARYING(128),
    wc_code CHARACTER VARYING(20),
    quantity NUMERIC(19,7),
    expense_by_job smallint,
    rate_type CHARACTER VARYING(255),
    quantity_type CHARACTER VARYING(255),
    employer_contribution_fk CHARACTER VARYING(255),
    compensation_fk CHARACTER VARYING(255),
    deduction_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);

--createn partition

CREATE TABLE psparc.psp_qbdt_payline_info_p0 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_qbdt_payline_info_p1 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_qbdt_payline_info_p2 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_qbdt_payline_info_p3 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_qbdt_payline_info_p4 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_qbdt_payline_info_p5 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_qbdt_payline_info_p6 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_qbdt_payline_info_p7 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_qbdt_payline_info_p8 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_qbdt_payline_info_p9 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_qbdt_payline_info_p10 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_qbdt_payline_info_p11 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_qbdt_payline_info_p12 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_qbdt_payline_info_p13 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_qbdt_payline_info_p14 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_qbdt_payline_info_p15 PARTITION OF psparc.psp_qbdt_payline_info FOR VALUES WITH (MODULUS 16, REMAINDER 15);


--PSP_PSTUB_PAY_ITEM

CREATE TABLE psparc.psp_pstub_pay_item(
    pstub_pay_item_seq CHARACTER VARYING(255) NOT NULL,
    version integer,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    acct_name CHARACTER VARYING(51),
    employee_paid smallint,
    income_subject_to_tax NUMERIC(19,4),
    name CHARACTER VARYING(31),
    payroll_item_list_id CHARACTER VARYING(51),
    rate CHARACTER VARYING(100),
    type CHARACTER VARYING(255),
    wage_base NUMERIC(19,4),
    y_t_d NUMERIC(19,4),
    paystub_fk CHARACTER VARYING(255),
    cur_amt NUMERIC(19,4),
    qty_amt CHARACTER VARYING(20),
    qty_time CHARACTER VARYING(20),
    company_fk CHARACTER VARYING(255) NOT NULL
) 
     PARTITION BY HASH (company_fk);


-- create partitions
CREATE TABLE psparc.psp_pstub_pay_item_p0 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_pstub_pay_item_p1 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_pstub_pay_item_p2 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_pstub_pay_item_p3 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_pstub_pay_item_p4 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_pstub_pay_item_p5 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_pstub_pay_item_p6 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_pstub_pay_item_p7 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_pstub_pay_item_p8 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_pstub_pay_item_p9 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_pstub_pay_item_p10 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_pstub_pay_item_p11 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_pstub_pay_item_p12 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_pstub_pay_item_p13 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_pstub_pay_item_p14 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_pstub_pay_item_p15 PARTITION OF psparc.psp_pstub_pay_item FOR VALUES WITH (MODULUS 16, REMAINDER 15);


--psp_tax

CREATE TABLE psparc.psp_tax(
    tax_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    taxable_wages_amount NUMERIC(19,4),
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    total_wages_amount NUMERIC(19,4),
    tax_liability_amount NUMERIC(19,4),
    paycheck_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(50) NOT NULL,
    tax_liability_y_t_d_amount NUMERIC(19,4),
    pay_stub_order integer,
    tips_taxable_wage_amount NUMERIC(19,4),
    company_law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);

-- create partitions
CREATE TABLE psparc.psp_tax_p0 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 0);
CREATE TABLE psparc.psp_tax_p1 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 1);
CREATE TABLE psparc.psp_tax_p2 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 2);
CREATE TABLE psparc.psp_tax_p3 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 3);
CREATE TABLE psparc.psp_tax_p4 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 4);
CREATE TABLE psparc.psp_tax_p5 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 5);
CREATE TABLE psparc.psp_tax_p6 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 6);
CREATE TABLE psparc.psp_tax_p7 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 7);
CREATE TABLE psparc.psp_tax_p8 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 8);
CREATE TABLE psparc.psp_tax_p9 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 9);
CREATE TABLE psparc.psp_tax_p10 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 10);
CREATE TABLE psparc.psp_tax_p11 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 11);
CREATE TABLE psparc.psp_tax_p12 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 12);
CREATE TABLE psparc.psp_tax_p13 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 13);
CREATE TABLE psparc.psp_tax_p14 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 14);
CREATE TABLE psparc.psp_tax_p15 PARTITION OF psparc.psp_tax FOR VALUES WITH (MODULUS 16, REMAINDER 15);

--PSP_DISBURSE_ADVICE_TAX_LIAB

CREATE TABLE psparc.psp_disburse_advice_tax_liab(
    disburse_advice_tax_liab_seq CHARACTER VARYING(255) NOT NULL,
    version integer,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payroll_item_id CHARACTER VARYING(4000),
    current_amount NUMERIC(19,4),
    quarter_amount NUMERIC(19,4),
    y_t_d_amount NUMERIC(19,4),
    current_taxable_amount NUMERIC(19,4),
    quarter_taxable_amount NUMERIC(19,4),
    y_t_d_taxable_amount NUMERIC(19,4),
    state CHARACTER VARYING(4000),
    state_tax_desc CHARACTER VARYING(4000),
    fed_tax_desc CHARACTER VARYING(4000),
    other_tax_desc CHARACTER VARYING(4000),
    disburse_advice_fk CHARACTER VARYING(255),
    tips_liability_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk) ;

--create partition 

CREATE TABLE psparc.psp_disburse_advice_tax_liab_p0 PARTITION OF psparc.psp_disburse_advice_tax_liab FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE psparc.psp_disburse_advice_tax_liab_p1 PARTITION OF psparc.psp_disburse_advice_tax_liab FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE psparc.psp_disburse_advice_tax_liab_p2 PARTITION OF psparc.psp_disburse_advice_tax_liab FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE psparc.psp_disburse_advice_tax_liab_p3 PARTITION OF psparc.psp_disburse_advice_tax_liab FOR VALUES WITH (MODULUS 4, REMAINDER 3);


--psp_qbdt_transaction_info

CREATE TABLE psparc.psp_qbdt_transaction_info(
    qbdt_transaction_info_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    agency_name CHARACTER VARYING(128),
    reference_number CHARACTER VARYING(11),
    account_name CHARACTER VARYING(128),
    memo CHARACTER VARYING(4000),
    on_service smallint,
    cleared CHARACTER VARYING(1),
    tracking_class CHARACTER VARYING(128),
    is_deleted smallint,
    token NUMERIC(19,0) DEFAULT - 2,
    liability_check_fk CHARACTER VARYING(255),
    liability_check_line_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    is_direct_deposit smallint DEFAULT 0,
    system_generated smallint DEFAULT 0,
    comp_adjust_submission_fk CHARACTER VARYING(255),
    liability_adjustment_fk CHARACTER VARYING(255),
    financial_transaction_fk CHARACTER VARYING(255),
    prior_payment_submission_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) ,
    qbdt_payroll_transaction_fk CHARACTER VARYING(255),
    qbdt_payroll_trans_line_fk CHARACTER VARYING(255)
)
    PARTITION BY HASH (company_fk);

--create partition 

CREATE TABLE psparc.psp_qbdt_transaction_info_p0 PARTITION OF psparc.psp_qbdt_transaction_info FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE psparc.psp_qbdt_transaction_info_p1 PARTITION OF psparc.psp_qbdt_transaction_info FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE psparc.psp_qbdt_transaction_info_p2 PARTITION OF psparc.psp_qbdt_transaction_info FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE psparc.psp_qbdt_transaction_info_p3 PARTITION OF psparc.psp_qbdt_transaction_info FOR VALUES WITH (MODULUS 4, REMAINDER 3);

--PSP_PAYSTUB

CREATE TABLE psparc.psp_paystub(
    paystub_seq CHARACTER VARYING(255) NOT NULL,
    version integer,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    adj_net_pay NUMERIC(19,4),
    gross_pay NUMERIC(19,4),
    net_pay NUMERIC(19,4),
    pay_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pre_tax_deductions NUMERIC(19,4),
    taxes NUMERIC(19,4),
    y_t_d_adj_net_pay NUMERIC(19,4),
    y_t_d_gross_pay NUMERIC(19,4),
    y_t_d_net_pay NUMERIC(19,4),
    y_t_d_pre_tax_deductions NUMERIC(19,4),
    y_t_d_taxes NUMERIC(19,4),
    pstub_employer_info_fk CHARACTER VARYING(255),
    pstub_employee_info_fk CHARACTER VARYING(255),
    paycheck_fk CHARACTER VARYING(255),
    check_number CHARACTER VARYING(30),
    source_mod_time BIGINT,
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);
--create partition 

CREATE TABLE psparc.psp_paystub_p0 PARTITION OF psparc.psp_paystub FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE psparc.psp_paystub_p1 PARTITION OF psparc.psp_paystub FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE psparc.psp_paystub_p2 PARTITION OF psparc.psp_paystub FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE psparc.psp_paystub_p3 PARTITION OF psparc.psp_paystub FOR VALUES WITH (MODULUS 4, REMAINDER 3);
--PSP_DEDUCTION

CREATE TABLE psparc.psp_deduction(
    deduction_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deduction_amount NUMERIC(19,4),
    paycheck_fk CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255),
    deduction_y_t_d_amount NUMERIC(19,4),
    pay_stub_order integer,
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);

--create partition 

CREATE TABLE psparc.psp_deduction_p0 PARTITION OF psparc.psp_deduction FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE psparc.psp_deduction_p1 PARTITION OF psparc.psp_deduction FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE psparc.psp_deduction_p2 PARTITION OF psparc.psp_deduction FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE psparc.psp_deduction_p3 PARTITION OF psparc.psp_deduction FOR VALUES WITH (MODULUS 4, REMAINDER 3);


--psp_pstub_paid_timeoff_item

CREATE TABLE psparc.psp_pstub_paid_timeoff_item(
    pstub_paid_timeoff_item_seq CHARACTER VARYING(255) NOT NULL,
    version integer,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    acct_name CHARACTER VARYING(51),
    available CHARACTER VARYING(30),
    name CHARACTER VARYING(31),
    payroll_item_list_id CHARACTER VARYING(51),
    y_t_d_used CHARACTER VARYING(30),
    paystub_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);

--create partition 

CREATE TABLE psparc.psp_pstub_paid_timeoff_item_p0 PARTITION OF psparc.psp_pstub_paid_timeoff_item FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE psparc.psp_pstub_paid_timeoff_item_p1 PARTITION OF psparc.psp_pstub_paid_timeoff_item FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE psparc.psp_pstub_paid_timeoff_item_p2 PARTITION OF psparc.psp_pstub_paid_timeoff_item FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE psparc.psp_pstub_paid_timeoff_item_p3 PARTITION OF psparc.psp_pstub_paid_timeoff_item FOR VALUES WITH (MODULUS 4, REMAINDER 3);

--psp_pstub_employee_info

CREATE TABLE psparc.psp_pstub_employee_info(
    pstub_employee_info_seq CHARACTER VARYING(255) NOT NULL,
    version integer,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    fed_allowances INTEGER,
    fed_extra NUMERIC(19,4),
    fed_tax_filing_status CHARACTER VARYING(50),
    fed_tax_filing_status_code INTEGER,
    first_name CHARACTER VARYING(30),
    last_name CHARACTER VARYING(30),
    s_s_n CHARACTER VARYING(15),
    state_allowances INTEGER,
    state_extra NUMERIC(19,4),
    state_tax_filing_status CHARACTER VARYING(63),
    state_tax_filing_status_code INTEGER,
    tax_filing_state CHARACTER VARYING(2),
    employee_fk CHARACTER VARYING(255),
    source_mod_time BIGINT,
    pstub_address_fk CHARACTER VARYING(255),
    middle_name CHARACTER VARYING(30),
    fed_claim_dependents NUMERIC(19,4),
    fed_other_income NUMERIC(19,4),
    fed_deductions NUMERIC(19,4),
    fed_multiple_jobs CHARACTER VARYING(4000),
    fed_w4_employee_pref CHARACTER VARYING(4000),
    company_fk CHARACTER VARYING(255) 
)
    PARTITION BY HASH (company_fk);


--create partition 

CREATE TABLE psparc.psp_pstub_employee_info_p0 PARTITION OF psparc.psp_pstub_employee_info FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE psparc.psp_pstub_employee_info_p1 PARTITION OF psparc.psp_pstub_employee_info FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE psparc.psp_pstub_employee_info_p2 PARTITION OF psparc.psp_pstub_employee_info FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE psparc.psp_pstub_employee_info_p3 PARTITION OF psparc.psp_pstub_employee_info FOR VALUES WITH (MODULUS 4, REMAINDER 3);

--tables moved from audit db to monolith
CREATE TABLE psparc.psp_qbdt_request_info(
    qbdt_request_info_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    employee_add_count BIGINT,
    employee_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_update_count BIGINT,
    employee_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_add_count BIGINT,
    paycheck_update_count BIGINT,
    payroll_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_count BIGINT,
    payroll_item_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_update_count BIGINT,
    payroll_item_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_count BIGINT,
    payroll_transaction_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_trans_update_count BIGINT,
    payroll_trans_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_trans_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_delete_count BIGINT,
    payroll_item_delete_count BIGINT,
    payroll_trans_delete_count BIGINT,
    delete_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    delete_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    source_system_transmission_fk CHARACTER VARYING(255),
    paycheck_delete_count BIGINT,
    company_fk  CHARACTER VARYING(255)
)
   PARTITION BY hash (company_fk) ;

--create partition 
CREATE TABLE psparc.psp_qbdt_request_info_p0 PARTITION OF psparc.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE psparc.psp_qbdt_request_info_p1 PARTITION OF psparc.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE psparc.psp_qbdt_request_info_p2 PARTITION OF psparc.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE psparc.psp_qbdt_request_info_p3 PARTITION OF psparc.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 3);


SELECT CURRENT_TIMESTAMP;
