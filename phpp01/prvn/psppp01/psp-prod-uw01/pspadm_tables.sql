\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

CREATE TABLE pspadm.arch_purge_main_stats_list(
    tab_l_zero CHARACTER VARYING(100),
    tab_l_one CHARACTER VARYING(100),
    tab_l_two CHARACTER VARYING(100),
    tab_l_three CHARACTER VARYING(100),
    tab_l_four CHARACTER VARYING(100),
    tab_purge_parti_name CHARACTER VARYING(100),
    tab_purge_record_cnt DOUBLE PRECISION,
    tab_archived_status CHARACTER VARYING(20),
    tab_purge_status CHARACTER VARYING(20),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    curr_purge_level DOUBLE PRECISION,
    tab_self_ref_flag CHARACTER VARYING(20),
    multi_parent_tab_flag CHARACTER VARYING(20),
    comments CHARACTER VARYING(300),
    purge_table_name CHARACTER VARYING(100),
    purge_seq_num DOUBLE PRECISION,
    rel_parent_of_pur_tab CHARACTER VARYING(100),
    purge_time_start TIMESTAMP(6) WITHOUT TIME ZONE,
    purge_time_end TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.as400_dropme(
    pref_userid CHARACTER VARYING(20),
    pref_w2_pref CHARACTER VARYING(20),
    pref_packet_pref CHARACTER VARYING(20)
);

CREATE TABLE pspadm.backup_schema_stats_090911(
    statid CHARACTER VARYING(30),
    type CHARACTER(1),
    version DOUBLE PRECISION,
    flags DOUBLE PRECISION,
    c1 CHARACTER VARYING(30),
    c2 CHARACTER VARYING(30),
    c3 CHARACTER VARYING(30),
    c4 CHARACTER VARYING(30),
    c5 CHARACTER VARYING(30),
    n1 DOUBLE PRECISION,
    n2 DOUBLE PRECISION,
    n3 DOUBLE PRECISION,
    n4 DOUBLE PRECISION,
    n5 DOUBLE PRECISION,
    n6 DOUBLE PRECISION,
    n7 DOUBLE PRECISION,
    n8 DOUBLE PRECISION,
    n9 DOUBLE PRECISION,
    n10 DOUBLE PRECISION,
    n11 DOUBLE PRECISION,
    n12 DOUBLE PRECISION,
    d1 TIMESTAMP(0) WITHOUT TIME ZONE,
    r1 BYTEA,
    r2 BYTEA,
    ch1 CHARACTER VARYING(1000)
);

CREATE TABLE pspadm.backup_stats(
    statid CHARACTER VARYING(30),
    type CHARACTER(1),
    version DOUBLE PRECISION,
    flags DOUBLE PRECISION,
    c1 CHARACTER VARYING(30),
    c2 CHARACTER VARYING(30),
    c3 CHARACTER VARYING(30),
    c4 CHARACTER VARYING(30),
    c5 CHARACTER VARYING(30),
    n1 DOUBLE PRECISION,
    n2 DOUBLE PRECISION,
    n3 DOUBLE PRECISION,
    n4 DOUBLE PRECISION,
    n5 DOUBLE PRECISION,
    n6 DOUBLE PRECISION,
    n7 DOUBLE PRECISION,
    n8 DOUBLE PRECISION,
    n9 DOUBLE PRECISION,
    n10 DOUBLE PRECISION,
    n11 DOUBLE PRECISION,
    n12 DOUBLE PRECISION,
    d1 TIMESTAMP(0) WITHOUT TIME ZONE,
    r1 BYTEA,
    r2 BYTEA,
    ch1 CHARACTER VARYING(1000)
);

CREATE TABLE pspadm.backup_stats_032911(
    statid CHARACTER VARYING(30),
    type CHARACTER(1),
    version DOUBLE PRECISION,
    flags DOUBLE PRECISION,
    c1 CHARACTER VARYING(30),
    c2 CHARACTER VARYING(30),
    c3 CHARACTER VARYING(30),
    c4 CHARACTER VARYING(30),
    c5 CHARACTER VARYING(30),
    n1 DOUBLE PRECISION,
    n2 DOUBLE PRECISION,
    n3 DOUBLE PRECISION,
    n4 DOUBLE PRECISION,
    n5 DOUBLE PRECISION,
    n6 DOUBLE PRECISION,
    n7 DOUBLE PRECISION,
    n8 DOUBLE PRECISION,
    n9 DOUBLE PRECISION,
    n10 DOUBLE PRECISION,
    n11 DOUBLE PRECISION,
    n12 DOUBLE PRECISION,
    d1 TIMESTAMP(0) WITHOUT TIME ZONE,
    r1 BYTEA,
    r2 BYTEA,
    ch1 CHARACTER VARYING(1000)
);

CREATE TABLE pspadm.backup_tab_stats_090911(
    statid CHARACTER VARYING(30),
    type CHARACTER(1),
    version DOUBLE PRECISION,
    flags DOUBLE PRECISION,
    c1 CHARACTER VARYING(30),
    c2 CHARACTER VARYING(30),
    c3 CHARACTER VARYING(30),
    c4 CHARACTER VARYING(30),
    c5 CHARACTER VARYING(30),
    n1 DOUBLE PRECISION,
    n2 DOUBLE PRECISION,
    n3 DOUBLE PRECISION,
    n4 DOUBLE PRECISION,
    n5 DOUBLE PRECISION,
    n6 DOUBLE PRECISION,
    n7 DOUBLE PRECISION,
    n8 DOUBLE PRECISION,
    n9 DOUBLE PRECISION,
    n10 DOUBLE PRECISION,
    n11 DOUBLE PRECISION,
    n12 DOUBLE PRECISION,
    d1 TIMESTAMP(0) WITHOUT TIME ZONE,
    r1 BYTEA,
    r2 BYTEA,
    ch1 CHARACTER VARYING(1000)
);

CREATE TABLE pspadm.cmp3$100488(
    employer_contribution_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    contribution_amount NUMERIC(19,4),
    contribution_y_t_d_amount NUMERIC(19,4),
    taxable_wages_amount NUMERIC(19,4),
    total_wages_amount NUMERIC(19,4),
    pay_stub_order NUMERIC(19,0),
    company_payroll_item_fk CHARACTER VARYING(255) NOT NULL,
    paycheck_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.cmp3$15077(
    tax_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    taxable_wages_amount NUMERIC(19,4),
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    total_wages_amount NUMERIC(19,4),
    tax_liability_amount NUMERIC(19,4),
    paycheck_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(50) NOT NULL,
    tax_liability_y_t_d_amount NUMERIC(19,4),
    pay_stub_order NUMERIC(19,0),
    tips_taxable_wage_amount NUMERIC(19,4),
    company_law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.cmp4$15077(
    tax_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    taxable_wages_amount NUMERIC(19,4),
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    total_wages_amount NUMERIC(19,4),
    tax_liability_amount NUMERIC(19,4),
    paycheck_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(50) NOT NULL,
    tax_liability_y_t_d_amount NUMERIC(19,4),
    pay_stub_order NUMERIC(19,0),
    tips_taxable_wage_amount NUMERIC(19,4),
    company_law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.del_test(
    a CHARACTER VARYING(10),
    b NUMERIC(10,0)
);

CREATE TABLE pspadm.distinct_company_fk(
    company_fk CHARACTER VARYING(255),
    status CHARACTER(1)
);

CREATE TABLE pspadm.empty(
    entry_detail_record_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    trace_number CHARACTER VARYING(20),
    credit_debit_indicator CHARACTER VARYING(255),
    record_data CHARACTER VARYING(250),
    intuit_bank_account_fk CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    n_a_c_h_a_file_type CHARACTER VARYING(255),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    legal_name CHARACTER VARYING(100),
    txp_record_data CHARACTER VARYING(80)
);

CREATE TABLE pspadm.gg_heartbeat(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.paycheck_backup_date(
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_end_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.payroll_hist(
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    end_time TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_submit_cnt DOUBLE PRECISION,
    tot_err_cnt DOUBLE PRECISION,
    distinct_err_type_cnt DOUBLE PRECISION
);

CREATE TABLE pspadm.pet_feb27(
    employee_tax_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    tax_law_version CHARACTER VARYING(20),
    w2_name CHARACTER VARYING(41),
    state CHARACTER VARYING(2),
    subject_to NUMERIC(1,0),
    tax_type CHARACTER VARYING(255),
    filing_status CHARACTER VARYING(50),
    allowances NUMERIC(10,0),
    company_law_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    tax_order NUMERIC(10,0),
    extra_withholding NUMERIC(19,7),
    extra_withholding_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_accounting_report_file(
    accounting_report_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    type CHARACTER VARYING(255),
    transmission_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_ach_transaction_code(
    transaction_code CHARACTER VARYING(2) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    ach_account_type_cd CHARACTER VARYING(255),
    credit_debit_indicator CHARACTER VARYING(255),
    description CHARACTER VARYING(150),
    is_return_code NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_achenrollment(
    achenrollment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    status_reason CHARACTER VARYING(1000),
    status CHARACTER VARYING(255),
    confirmation_number CHARACTER VARYING(4000),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_achenrollment_detail(
    achenrollment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    legal_name CHARACTER VARYING(4000),
    a_c_h_enrollment_fk CHARACTER VARYING(255),
    response_file_fk CHARACTER VARYING(255),
    request_file_fk CHARACTER VARYING(255),
    agency_id_enc CHARACTER VARYING(4000),
    fein_enc CHARACTER VARYING(4000),
    f_e_i_n CHARACTER VARYING(4000),
    agency_id CHARACTER VARYING(4000),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_achenrollment_file(
    achenrollment_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    type CHARACTER VARYING(255),
    file_content TEXT,
    file_content_enc TEXT
);

CREATE TABLE pspadm.psp_action_event(
    code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(100),
    type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_additional_filing_amount(
    name CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    a_t_f_law_id CHARACTER VARYING(4000),
    description CHARACTER VARYING(4000),
    rate NUMERIC(1,0),
    payment_template_fk CHARACTER VARYING(255),
    is_system_applied_credit NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_address(
    address_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    address_line3 CHARACTER VARYING(80),
    address_line2 CHARACTER VARYING(80),
    address_line1 CHARACTER VARYING(80),
    city CHARACTER VARYING(256),
    country CHARACTER VARYING(256),
    state CHARACTER VARYING(21),
    zip_code CHARACTER VARYING(13),
    zip_code_extension CHARACTER VARYING(10)
);

CREATE TABLE pspadm.psp_ade_law_map(
    ade_law_map_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    ade_name CHARACTER VARYING(50),
    law_fk CHARACTER VARYING(255),
    ade_law_map_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_agency(
    agency_id CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(300),
    default_r_a_a_form CHARACTER VARYING(255),
    a_c_h_enrollment_required NUMERIC(1,0),
    r_a_a_enrollment_required NUMERIC(1,0),
    r_a_f_enrollment_required NUMERIC(1,0),
    agency_supported NUMERIC(1,0),
    agency_abbrev CHARACTER VARYING(4000),
    rfnds_intuit_for_returned_pmt NUMERIC(1,0),
    no_calculation NUMERIC(1,0),
    agency_id_enc CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_agency_check_batch(
    agency_check_batch_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    super_check NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_agency_id_requirement(
    agency_id_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    pattern CHARACTER VARYING(4000),
    example CHARACTER VARYING(50),
    required NUMERIC(1,0),
    payment_template_agency_id_fk CHARACTER VARYING(255),
    custom_requirement CHARACTER VARYING(255),
    prohibit_default_ids NUMERIC(1,0) NOT NULL DEFAULT 1
);

CREATE TABLE pspadm.psp_agency_rate_request(
    agency_rate_request_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    record_count NUMERIC(10,0),
    status CHARACTER VARYING(255),
    year_quarter CHARACTER VARYING(30),
    agency_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_annual_billing_batch(
    annual_billing_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    form_type_cd CHARACTER VARYING(255),
    form_year NUMERIC(10,0),
    annual_billing_batch_status_cd CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_annual_billing_item(
    annual_billing_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    form_count NUMERIC(10,0),
    error_message CHARACTER VARYING(4000),
    annual_billing_item_status_cd CHARACTER VARYING(255),
    annual_billing_batch_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_applied_database_patch(
    applied_database_patch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    database_patch_version CHARACTER VARYING(15),
    database_patch_type_cd CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_archive_record(
    archive_record_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    record_identifier CHARACTER VARYING(4000),
    table_name CHARACTER VARYING(4000),
    status CHARACTER VARYING(20)
);

CREATE TABLE pspadm.psp_assisted_bundle_bill(
    assisted_bundle_bill_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    bill_date TIMESTAMP(6) WITHOUT TIME ZONE,
    total_count NUMERIC(10,0),
    total_amount NUMERIC(19,4),
    asst_status CHARACTER VARYING(255),
    asst_bundle_comp_usage_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_asst_bundle_bill_detail(
    asst_bundle_bill_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    billing_detail_id CHARACTER VARYING(4000),
    assisted_bundle_bill_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_asst_bundle_comp_usage(
    asst_bundle_comp_usage_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    source_system_cd CHARACTER VARYING(255),
    entitlement_id CHARACTER VARYING(20),
    license_id CHARACTER VARYING(20)
);

CREATE TABLE pspadm.psp_atfdata_extract_batch(
    atfdata_extract_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    run_type CHARACTER VARYING(255),
    batch_id NUMERIC(19,0),
    start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    batch_status CHARACTER VARYING(255),
    year NUMERIC(10,0),
    quarter NUMERIC(10,0)
);

CREATE TABLE pspadm.psp_atfdata_extract_file(
    atfdata_extract_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    file_type CHARACTER VARYING(255),
    file_status CHARACTER VARYING(255),
    file_name CHARACTER VARYING(150),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    a_t_f_data_extract_batch_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_atfpayments_to_process(
    atfpayments_to_process_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    payment_date TIMESTAMP(6) WITHOUT TIME ZONE,
    quarter_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    money_movement_transaction_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_atfpayrolls_to_process(
    atfpayrolls_to_process_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payroll_run_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_auth_domain(
    domain_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256),
    name CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_auth_operation(
    operation_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256),
    name CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_auth_role(
    auth_role_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    role_id CHARACTER VARYING(40),
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    auth_domain_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_auth_user(
    auth_user_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    corp_id CHARACTER VARYING(50),
    first_name CHARACTER VARYING(80),
    last_name CHARACTER VARYING(80),
    authorization_token CHARACTER VARYING(50),
    last_remote_call_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    account_locked_until TIMESTAMP(6) WITHOUT TIME ZONE,
    nbr_of_failed_login_attempts NUMERIC(10,0) NOT NULL DEFAULT 0
);

CREATE TABLE pspadm.psp_auth_user_auth_role__assoc(
    auth_user_fk CHARACTER VARYING(255) NOT NULL,
    auth_role_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) DEFAULT - 1
);

CREATE TABLE pspadm.psp_authrole_operation_assoc(
    auth_role_fk CHARACTER VARYING(255) NOT NULL,
    auth_operation_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_bank_account(
    bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    account_type_cd CHARACTER VARYING(255),
    bank_name CHARACTER VARYING(256),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    routing_number CHARACTER VARYING(20),
    a_c_h_entry_class CHARACTER VARYING(255),
    a_c_h_account_type_cd CHARACTER VARYING(255),
    account_number_enc CHARACTER VARYING(4000),
    account_number CHARACTER VARYING(80),
    session_id CHARACTER VARYING(100),
    record_metadata CHARACTER VARYING(512),
    wallet_id CHARACTER VARYING(64)
);

CREATE TABLE pspadm.psp_bank_holiday(
    bank_holiday_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    bank_holiday_name CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_batch_job_audit_log(
    batch_job_audit_log_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    engine_instance_name CHARACTER VARYING(500),
    job_action CHARACTER VARYING(500),
    job_namespace CHARACTER VARYING(500),
    message CHARACTER VARYING(4000),
    message_detail CHARACTER VARYING(4000),
    is_verified NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_batch_job_parameter(
    id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    job_step CHARACTER VARYING(300),
    param_name CHARACTER VARYING(300),
    param_value CHARACTER VARYING(300),
    batch_job_setup_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_batch_job_setup(
    job_type CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    dly_bw_retries_timer_expr CHARACTER VARYING(100),
    is_automatically_scheduled NUMERIC(1,0),
    job_processor_class_name CHARACTER VARYING(100),
    max_retries NUMERIC(10,0),
    job_timer_expression CHARACTER VARYING(100),
    job_namespace CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_batch_job_status(
    batch_job_status_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    job_type CHARACTER VARYING(255),
    is_running NUMERIC(1,0) DEFAULT 0,
    last_started_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    last_ended_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_bill(
    bill_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    bill_date TIMESTAMP(6) WITHOUT TIME ZONE,
    usage_count NUMERIC(10,0),
    synched_count NUMERIC(10,0),
    company_usage_fk CHARACTER VARYING(255),
    closed NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_bill_payment(
    bill_payment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    source_id CHARACTER VARYING(50),
    payee_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255),
    memo CHARACTER VARYING(4000),
    transaction_type CHARACTER VARYING(255),
    session_id CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_bill_payment_split(
    bill_payment_split_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    source_id CHARACTER VARYING(50),
    bill_payment_fk CHARACTER VARYING(255),
    payee_bank_account_fk CHARACTER VARYING(255),
    reference_number CHARACTER VARYING(50)
);

CREATE TABLE pspadm.psp_billing_detail(
    billing_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    service_date TIMESTAMP(6) WITHOUT TIME ZONE,
    service_cd CHARACTER VARYING(255),
    item_name CHARACTER VARYING(100),
    item_sku CHARACTER VARYING(40),
    quantity NUMERIC(10,0),
    unit_price NUMERIC(19,4),
    offer_cd CHARACTER VARYING(40),
    offer_name CHARACTER VARYING(100),
    discount_amount NUMERIC(19,4),
    tax_amount NUMERIC(19,4),
    tax_computed_date TIMESTAMP(6) WITHOUT TIME ZONE,
    item_total NUMERIC(19,4),
    offload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_amount_when_offloaded NUMERIC(19,4),
    offering_service_charge_type CHARACTER VARYING(255),
    tax_jurisdiction CHARACTER VARYING(16),
    tax_exception_ind NUMERIC(1,0),
    payroll_run_fk CHARACTER VARYING(255) NOT NULL,
    billing_period TIMESTAMP(6) WITHOUT TIME ZONE,
    memo CHARACTER VARYING(100),
    base_price NUMERIC(19,4),
    offering_svcchg_price_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_bpcompany_service_info(
    bpcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    override_company_limit_amount NUMERIC(19,4),
    cons_limit_violation_cnt NUMERIC(19,0),
    override_payee_limit_amount NUMERIC(19,4)
);

CREATE TABLE pspadm.psp_cdcompany_service_info(
    cdcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    last_paycheck_id NUMERIC(19,0)
);

CREATE TABLE pspadm.psp_check_print_batch(
    check_print_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    number_of_checks NUMERIC(19,0),
    sent_to_printer TIMESTAMP(6) WITHOUT TIME ZONE,
    check_print_batch_status_code CHARACTER VARYING(255),
    check_print_batch_message CHARACTER VARYING(250),
    recon_plus_file_fk CHARACTER VARYING(255),
    positive_pay_file_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_check_print_paycheck(
    check_print_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_paycheck_id CHARACTER VARYING(50),
    check_number CHARACTER VARYING(10),
    cp_paycheck_status_code CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) NOT NULL,
    employee_print_name CHARACTER VARYING(50),
    company_paycheck_batch_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_check_print_signature(
    check_print_signature_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    signature TEXT,
    company_fk CHARACTER VARYING(255),
    sourcesys_printedchk_info_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_collection_stage(
    collection_stage_code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_comp_adjust_submission(
    comp_adjust_submission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    submission_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    source_id CHARACTER VARYING(9),
    amount NUMERIC(19,4),
    void_submission_fk CHARACTER VARYING(255),
    original_submission_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_comp_pmt_template_agencyid(
    comp_pmt_template_agencyid_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(50),
    company_agency_pmt_template_fk CHARACTER VARYING(255) NOT NULL,
    agency_taxpayer_id_enc CHARACTER VARYING(4000),
    agency_taxpayer_id CHARACTER VARYING(50),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_comp_pmttemplate_pmtmethod(
    comp_pmttemplate_pmtmethod_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    agent_enabled NUMERIC(1,0),
    enabled NUMERIC(1,0),
    payment_method CHARACTER VARYING(255),
    company_agency_pmt_template_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_company(
    company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    phone CHARACTER VARYING(100),
    dba_name CHARACTER VARYING(100),
    legal_name CHARACTER VARYING(100),
    source_company_id CHARACTER VARYING(50),
    notification_email CHARACTER VARYING(100),
    next_payroll_transaction_id CHARACTER VARYING(50),
    next_paycheck_id CHARACTER VARYING(50),
    next_employee_id CHARACTER VARYING(50),
    next_payroll_item_id CHARACTER VARYING(50),
    account_locked_until TIMESTAMP(6) WITHOUT TIME ZONE,
    nbr_of_failed_login_attempts NUMERIC(10,0),
    current_token NUMERIC(19,0),
    source_system_cd CHARACTER VARYING(255),
    tax_exempt_expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_flagged_for_fraud NUMERIC(1,0),
    sign_up_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_frequency_fk CHARACTER VARYING(255),
    mailing_address_fk CHARACTER VARYING(255),
    legal_address_fk CHARACTER VARYING(255),
    funding_model_fk CHARACTER VARYING(255) NOT NULL,
    public_key CHARACTER VARYING(1000),
    nbr_failed_authentications NUMERIC(10,0),
    debug_logging NUMERIC(1,0),
    offload_group_fk CHARACTER VARYING(255) NOT NULL,
    cloud_current_token NUMERIC(19,0),
    i_a_m_realm_id CHARACTER VARYING(50),
    price_type CHARACTER VARYING(50),
    tax_exempt_status CHARACTER VARYING(255),
    annual_billing_batch_fk CHARACTER VARYING(255),
    name_control CHARACTER VARYING(10),
    fed_tax_id_enc CHARACTER VARYING(4000),
    d_d_publish_flag NUMERIC(1,0) NOT NULL DEFAULT 0,
    fed_tax_id CHARACTER VARYING(80),
    private_key_enc CHARACTER VARYING(4000),
    private_key CHARACTER VARYING(4000),
    o_i_i_flag CHARACTER VARYING(32),
    compliance_address_fk CHARACTER VARYING(255),
    is_dg_disassociated NUMERIC(1,0) NOT NULL DEFAULT 0,
    publish_status CHARACTER VARYING(64) DEFAULT '00000000',
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_company_additional_info(
    company_additional_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    company_fk CHARACTER VARYING(255) NOT NULL,
    industry_type_fk CHARACTER VARYING(255),
    ownership_type_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_company_agency(
    company_agency_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    resp_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    resp_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    first_filings_quarter CHARACTER VARYING(5),
    last_filings_quarter CHARACTER VARYING(5),
    is_final_return NUMERIC(1,0),
    generate_annual_form NUMERIC(1,0),
    final_payroll_date TIMESTAMP(6) WITHOUT TIME ZONE,
    agency_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    er_fica_deferral_enabled NUMERIC(1,0) DEFAULT 0
);

CREATE TABLE pspadm.psp_company_bank_account(
    company_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    insert_user_id CHARACTER VARYING(30),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    total_retry_count NUMERIC(19,0),
    verify_retry_count NUMERIC(19,0),
    source_bank_account_name CHARACTER VARYING(128),
    last_retry_date TIMESTAMP(6) WITHOUT TIME ZONE,
    bank_account_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    session_id CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_company_consent(
    company_consent_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    signup_date TIMESTAMP(6) WITHOUT TIME ZONE,
    signed NUMERIC(1,0),
    app_id CHARACTER VARYING(4000),
    app_name CHARACTER VARYING(4000),
    fein_enc CHARACTER VARYING(4000),
    fein CHARACTER VARYING(4000),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_company_daily_liability(
    company_daily_liability_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    liability_date TIMESTAMP(6) WITHOUT TIME ZONE,
    taxable_wages NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    total_tips_amount NUMERIC(19,4),
    tax_amount NUMERIC(19,4),
    law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_company_event_bkp(
    company_event_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
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
    email_status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    email_retry_count NUMERIC(10,0),
    email_status CHARACTER VARYING(255),
    source_id CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255) NOT NULL,
    note_last_updated_date TIMESTAMP(6) WITHOUT TIME ZONE
);


--added company_fk

CREATE TABLE pspadm.psp_company_event_email(
    company_event_email_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    retry_count NUMERIC(10,0),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    email_template_type_cd CHARACTER VARYING(255),
    company_event_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);


CREATE TABLE pspadm.psp_company_filing_amount(
    company_filing_amount_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,7),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE,
    name CHARACTER VARYING(4000),
    company_agency_pmt_template_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_company_law(
    company_law_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    law_fk CHARACTER VARYING(255) NOT NULL,
    exemption_status CHARACTER VARYING(255),
    tax_form_line CHARACTER VARYING(255),
    source_description CHARACTER VARYING(50),
    source_id CHARACTER VARYING(5),
    is_archived NUMERIC(1,0) DEFAULT 0,
    filing_status CHARACTER VARYING(255),
    reimbursable_status CHARACTER VARYING(255),
    additional_company_law_fk CHARACTER VARYING(255),
    w2_code NUMERIC(10,0) DEFAULT - 1
);

CREATE TABLE pspadm.psp_company_law_rate(
    company_law_rate_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    rate NUMERIC(19,7),
    company_law_fk CHARACTER VARYING(255) NOT NULL,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE,
    rate_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_company_note(
    company_note_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    notes CHARACTER VARYING(4000),
    insert_user_id CHARACTER VARYING(30),
    company_fk CHARACTER VARYING(255) NOT NULL,
    company_event_fk CHARACTER VARYING(255) NOT NULL,
    alert NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_company_note_bkp(
    company_note_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    notes CHARACTER VARYING(4000),
    insert_user_id CHARACTER VARYING(30),
    company_fk CHARACTER VARYING(255) NOT NULL,
    company_event_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_company_offer(
    company_offer_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    usages_remaining NUMERIC(10,0),
    offer_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_company_offering(
    company_offering_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    offering_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_company_paycheck_batch(
    company_paycheck_batch_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_company_payroll_item(
    company_payroll_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_payroll_item_id CHARACTER VARYING(4000),
    company_fk CHARACTER VARYING(255),
    payroll_item_fk CHARACTER VARYING(255),
    source_description CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    tax_form_line CHARACTER VARYING(255),
    is_archived NUMERIC(1,0) DEFAULT 0,
    additional_payroll_item_fk CHARACTER VARYING(255),
    w2_code NUMERIC(10,0) DEFAULT - 1
);

CREATE TABLE pspadm.psp_company_pin(
    company_pin_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    p_i_n_value CHARACTER VARYING(256),
    company_fk CHARACTER VARYING(255) NOT NULL,
    hash_type CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_company_rate_request(
    company_rate_request_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_message CHARACTER VARYING(256),
    status CHARACTER VARYING(255),
    agency_rate_request_fk CHARACTER VARYING(255),
    company_agency_fk CHARACTER VARYING(255),
    old_rate NUMERIC(19,7),
    new_rate NUMERIC(19,7)
);

CREATE TABLE pspadm.psp_company_service(
    company_service_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    service_fk CHARACTER VARYING(255) NOT NULL,
    service_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    funding_model_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_company_service_bank_acct(
    company_service_bank_acct_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    company_service_fk CHARACTER VARYING(255) NOT NULL,
    company_bank_account_fk CHARACTER VARYING(255) NOT NULL,
    payroll_run_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_company_tfssubmission(
    company_tfssubmission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    submission_status CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255),
    year NUMERIC(10,0)
);

CREATE TABLE pspadm.psp_company_usage(
    company_usage_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    source_system_cd CHARACTER VARYING(255),
    entitlement_id CHARACTER VARYING(20),
    license_id CHARACTER VARYING(20),
    billing_day_of_month NUMERIC(10,0),
    start_day_of_usage_month NUMERIC(10,0)
);

CREATE TABLE pspadm.psp_companyagency_frmtemplate(
    companyagency_frmtemplate_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255),
    form_template_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_companyagency_pmttemplate(
    companyagency_pmttemplate_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    agency_taxpayer_id_enc CHARACTER VARYING(4000),
    agency_taxpayer_id CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);


CREATE TABLE pspadm.psp_contact(
    contact_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    auth_signer_yn_ind NUMERIC(1,0),
    contact_role_cd CHARACTER VARYING(255),
    source_contact_id CHARACTER VARYING(300),
    title CHARACTER VARYING(20),
    title_suffix CHARACTER VARYING(20),
    job_title CHARACTER VARYING(80),
    fax CHARACTER VARYING(20),
    second_phone CHARACTER VARYING(20),
    company_fk CHARACTER VARYING(255) NOT NULL,
    i_a_m_authentication_id CHARACTER VARYING(50),
    date_of_birth_enc CHARACTER VARYING(4000),
    social_security_number_enc CHARACTER VARYING(4000),
    social_security_number CHARACTER VARYING(4000),
    date_of_birth TIMESTAMP(6) WITHOUT TIME ZONE,
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_ddcompany_service_info(
    ddcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    average_pay_run_amount NUMERIC(19,4),
    cons_limit_violation_cnt NUMERIC(19,0),
    high_annual_pay_amount NUMERIC(19,4),
    override_company_limit_amount NUMERIC(19,4),
    override_employee_limit_amount NUMERIC(19,4),
    offload_group_fk CHARACTER VARYING(255)
);



CREATE TABLE pspadm.psp_deleted_record(
    deleted_record_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    record_identifier CHARACTER VARYING(4000),
    table_name CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_dep_freq_ledger_operation(
    dep_freq_ledger_operation_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    deposit_frequency CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_deposit_frequency(
    deposit_frequency_code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    a_t_f_deposit_freq_code CHARACTER VARYING(50),
    description CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_deposit_frequency_file(
    deposit_frequency_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(150),
    status CHARACTER VARYING(255),
    is_archived NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_deposit_frequency_file_rec(
    deposit_frequency_file_rec_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    company_name CHARACTER VARYING(35),
    current_year CHARACTER VARYING(4),
    deposit_frequency CHARACTER VARYING(1),
    form_filed CHARACTER VARYING(3),
    last_period_base_code CHARACTER VARYING(1),
    error_message CHARACTER VARYING(4000),
    deposit_frequency_file_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    ein_enc CHARACTER VARYING(4000),
    e_i_n CHARACTER VARYING(9),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_deposit_frequency_req(
    deposit_frequency_req_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    prohibited_deposit_frequency CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_dicrfile(
    dicrfile_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    credit_txn_total_amount NUMERIC(19,4),
    debit_txn_total_amount NUMERIC(19,4),
    file_name CHARACTER VARYING(1000),
    status CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_disburse_advice(
    disburse_advice_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    tax_liability_amount NUMERIC(19,4),
    tax_quarter NUMERIC(10,0),
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255)
);


--added company_fk

CREATE TABLE pspadm.psp_edi_payment_detail(
    edi_payment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payment_details CHARACTER VARYING(4000),
    payment_due_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    period_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    response_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_type_code CHARACTER VARYING(4000),
    confirmation_number CHARACTER VARYING(4000),
    transaction_set_id NUMERIC(10,0),
    status_cd CHARACTER VARYING(255),
    transaction_id CHARACTER VARYING(4000),
    error_cd CHARACTER VARYING(4000),
    error_message CHARACTER VARYING(4000),
    group_id NUMERIC(10,0),
    group_transaction_time CHARACTER VARYING(4000),
    payment_amount NUMERIC(19,4),
    parent_file_fk CHARACTER VARYING(255) NOT NULL,
    response_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(4000),
    record_metadata CHARACTER VARYING(512),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_edi_tax_file(
    edi_tax_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    file_code NUMERIC(10,0),
    file_id NUMERIC(10,0),
    file_name CHARACTER VARYING(4000),
    file_type CHARACTER VARYING(255),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    submit_date TIMESTAMP(6) WITHOUT TIME ZONE,
    system_owner CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_ee_payrollitem_qtrtotals(
    ee_payrollitem_qtrtotals_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    quarter NUMERIC(10,0),
    year NUMERIC(10,0),
    taxable_wages NUMERIC(19,4),
    amount NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    tips_taxable_wages_amount NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_effective_deposit_freq(
    effective_deposit_freq_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_pmt_template_fk CHARACTER VARYING(255) NOT NULL,
    payment_template_frequency_fk CHARACTER VARYING(255) NOT NULL,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_eftps_enrollment(
    eftps_enrollment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    eftps_enrollment_id CHARACTER VARYING(100),
    secondary NUMERIC(1,0) DEFAULT 0
);

CREATE TABLE pspadm.psp_eftps_enrollment_detail(
    eftps_enrollment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    group_id NUMERIC(10,0),
    transaction_set_id NUMERIC(10,0),
    transaction_id NUMERIC(10,0),
    legal_name CHARACTER VARYING(100),
    legal_zip CHARACTER VARYING(13),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    response_date TIMESTAMP(6) WITHOUT TIME ZONE,
    reject_cd CHARACTER VARYING(20),
    reject_reason CHARACTER VARYING(100),
    eftps_enrollment_fk CHARACTER VARYING(255),
    parent_file_fk CHARACTER VARYING(255),
    response_file_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_eftps_file(
    eftps_file_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    ack_file_fk CHARACTER VARYING(255),
    file_subtype CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_eftps_payment_detail(
    eftps_payment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    transaction_set_id NUMERIC(10,0),
    transaction_id NUMERIC(10,0),
    return_cd CHARACTER VARYING(255),
    payment_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_type_code CHARACTER VARYING(4000),
    eft_transaction_id CHARACTER VARYING(4000),
    agency_payment_id CHARACTER VARYING(4000),
    payment_due_date TIMESTAMP(6) WITHOUT TIME ZONE,
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_amount NUMERIC(19,4),
    payment_details CHARACTER VARYING(4000),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    group_id NUMERIC(10,0),
    reject_cd CHARACTER VARYING(4000),
    reason CHARACTER VARYING(4000),
    response_date TIMESTAMP(6) WITHOUT TIME ZONE,
    parent_file_fk CHARACTER VARYING(255) NOT NULL,
    return_file_fk CHARACTER VARYING(255),
    response_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    same_day_ack_number CHARACTER VARYING(4000),
    payment_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(4000),
    record_metadata CHARACTER VARYING(512),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_emp_totals_payroll_run(
    emp_totals_payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    quarter_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_employee(
    employee_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_employee_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255),
    hire_date TIMESTAMP(6) WITHOUT TIME ZONE,
    re_hire_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_filing_status CHARACTER VARYING(50),
    work_state CHARACTER VARYING(21),
    termination_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_allowances NUMERIC(10,0),
    is_statutory NUMERIC(1,0),
    has_retirement_plan NUMERIC(1,0),
    has_third_party_sick_pay NUMERIC(1,0),
    tp_401k_info_is_hce NUMERIC(1,0),
    tp_401k_info_owner_percent NUMERIC(19,7),
    tp_401k_info_is_family_member NUMERIC(1,0),
    tp_401k_info_last_upload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_cloud_employee NUMERIC(1,0),
    is_deceased NUMERIC(1,0),
    qualifies_for_aeic NUMERIC(1,0),
    fed_extra_withholding NUMERIC(19,4),
    live_state CHARACTER VARYING(21),
    pay_period CHARACTER VARYING(255),
    is_archived NUMERIC(1,0) DEFAULT 0,
    consumer_realm_id CHARACTER VARYING(50),
    is_viewing_paystub_disabled NUMERIC(1,0) NOT NULL DEFAULT 0,
    birth_date_enc CHARACTER VARYING(4000),
    tax_id_enc CHARACTER VARYING(4000),
    persona_id CHARACTER VARYING(4000),
    birth_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_id CHARACTER VARYING(20),
    fed_claim_dependents NUMERIC(19,4),
    fed_other_income NUMERIC(19,4),
    fed_deductions NUMERIC(19,4),
    fed_multiple_jobs NUMERIC(1,0) NOT NULL DEFAULT 0,
    fed_w4_employee_pref CHARACTER VARYING(4000),
    is_dg_disassociated NUMERIC(1,0) NOT NULL DEFAULT 0,
    publish_status CHARACTER VARYING(16) DEFAULT '0000',
    record_metadata CHARACTER VARYING(512),
    user_auth_id CHARACTER VARYING(4000),
    process_flag CHARACTER VARYING(16) DEFAULT '0000000000000000'
);

CREATE TABLE pspadm.psp_employee_accrual(
    employee_accrual_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    accrual_period CHARACTER VARYING(255),
    hours_per_period NUMERIC(19,7),
    hours NUMERIC(19,7),
    max_hours NUMERIC(19,7),
    new_year_reset NUMERIC(1,0),
    accrual_type CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_employee_bank_account(
    employee_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    bank_account_fk CHARACTER VARYING(255) NOT NULL,
    employee_fk CHARACTER VARYING(255) NOT NULL,
    amount NUMERIC(19,7),
    amount_type CHARACTER VARYING(255),
    account_order NUMERIC(10,0) DEFAULT - 1,
    session_id CHARACTER VARYING(100),
    source_bank_account_id_enc CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_employee_bank_account_bkp(
    employee_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    bank_account_fk CHARACTER VARYING(255) NOT NULL,
    employee_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_employee_custom_field(
    employee_custom_field_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(128),
    value CHARACTER VARYING(128),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    field_order NUMERIC(10,0) DEFAULT - 1
);

CREATE TABLE pspadm.psp_employee_law_qtr_totals(
    employee_law_qtr_totals_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    hours_worked NUMERIC(19,7),
    month_one_worked_indicator NUMERIC(1,0),
    month_two_worked_indicator NUMERIC(1,0),
    month_three_worked_indicator NUMERIC(1,0),
    quarter NUMERIC(10,0),
    taxable_wages NUMERIC(19,4),
    tax_amount NUMERIC(19,4),
    tips_taxable_wages_amount NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    weeks_worked NUMERIC(10,0),
    year NUMERIC(10,0),
    law_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    hourly_rate NUMERIC(19,7),
    company_law_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_employee_payroll_item(
    employee_payroll_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,7),
    "LIMIT" NUMERIC(19,7),
    type CHARACTER VARYING(255),
    amount_type CHARACTER VARYING(255),
    limit_type CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255) NOT NULL,
    employee_fk CHARACTER VARYING(255) NOT NULL,
    item_order NUMERIC(10,0) DEFAULT - 1
);

CREATE TABLE pspadm.psp_employee_tax(
    employee_tax_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    tax_law_version CHARACTER VARYING(20),
    w2_name CHARACTER VARYING(41),
    state CHARACTER VARYING(2),
    subject_to NUMERIC(1,0),
    tax_type CHARACTER VARYING(255),
    filing_status CHARACTER VARYING(50),
    allowances NUMERIC(10,0),
    company_law_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    tax_order NUMERIC(10,0) DEFAULT - 1,
    extra_withholding NUMERIC(19,7) DEFAULT 0,
    extra_withholding_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_employee_usage(
    employee_usage_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    employee_name CHARACTER VARYING(240),
    employee_record_number CHARACTER VARYING(50),
    source_employee_id CHARACTER VARYING(50),
    usage_count NUMERIC(10,0),
    usage_period_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_employee_w2_totals(
    employee_w2_totals_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    year NUMERIC(10,0),
    taxable_wages NUMERIC(19,4),
    amount NUMERIC(19,4),
    tips_taxable_wages_amount NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    company_law_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_employee_wage_plan(
    employee_wage_plan_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(255),
    state CHARACTER VARYING(15),
    wage_plan_value CHARACTER VARYING(15),
    wage_plan_domain CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    description CHARACTER VARYING(100),
    rules_version CHARACTER VARYING(10),
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_employer_contribution(
    employer_contribution_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    contribution_amount NUMERIC(19,4),
    contribution_y_t_d_amount NUMERIC(19,4),
    taxable_wages_amount NUMERIC(19,4),
    total_wages_amount NUMERIC(19,4),
    pay_stub_order NUMERIC(19,0),
    company_payroll_item_fk CHARACTER VARYING(255) NOT NULL,
    paycheck_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_employer_preference(
    employer_preference_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    app_name CHARACTER VARYING(30),
    preference_name CHARACTER VARYING(30),
    preference_value CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_entitlement(
    entitlement_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    order_number CHARACTER VARYING(20),
    license_number CHARACTER VARYING(20),
    customer_id CHARACTER VARYING(50),
    next_charge_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_method_type CHARACTER VARYING(255),
    credit_card_type CHARACTER VARYING(20),
    credit_card_number CHARACTER VARYING(4),
    credit_card_expiration CHARACTER VARYING(7),
    entitlement_offering_code CHARACTER VARYING(20),
    contact_email CHARACTER VARYING(100),
    entitlement_state CHARACTER VARYING(255),
    contact_name CHARACTER VARYING(200),
    subscription_number CHARACTER VARYING(18),
    entitlement_code_fk CHARACTER VARYING(255) NOT NULL,
    billing_zip_code CHARACTER VARYING(10),
    cancellation_reason CHARACTER VARYING(200),
    subscription_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    last_message_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    order_source_cd CHARACTER VARYING(255),
    subscription_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    billing_day_of_month NUMERIC(10,0),
    billing_profile_id CHARACTER VARYING(40),
    trial_associated NUMERIC(1,0) NOT NULL DEFAULT 0,
    retail NUMERIC(1,0) NOT NULL DEFAULT 0,
    billing_realm_id CHARACTER VARYING(30),
    o_i_i_billing_flag CHARACTER VARYING(32)
);

CREATE TABLE pspadm.psp_entitlement_code(
    entitlement_code_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    edition_type CHARACTER VARYING(255),
    number_of_employees_type CHARACTER VARYING(255),
    quick_books_subtype NUMERIC(19,0),
    subtype_description CHARACTER VARYING(50),
    asset_item_cd CHARACTER VARYING(255),
    asset_item_number CHARACTER VARYING(7),
    is_primary NUMERIC(1,0),
    is_usage_billing NUMERIC(1,0),
    is_first_usage_free NUMERIC(1,0),
    billing_frequency_type CHARACTER VARYING(255),
    asset_type_cd CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_entitlement_code_offering(
    entitlement_code_offering_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    service_cd CHARACTER VARYING(255),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    entitlement_code_fk CHARACTER VARYING(255) NOT NULL,
    offering_fk CHARACTER VARYING(255) NOT NULL,
    price_type CHARACTER VARYING(150),
    is_default NUMERIC(1,0)
);



CREATE TABLE pspadm.psp_entitlement_unit(
    entitlement_unit_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    entitlement_unit_status CHARACTER VARYING(255),
    service_key CHARACTER VARYING(19),
    entitlement_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    extension_key CHARACTER VARYING(14),
    error_count NUMERIC(19,0),
    last_validation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(9),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_entity_change(
    entity_change_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(4000),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    agent_id CHARACTER VARYING(4000),
    is_successor NUMERIC(1,0),
    has_new_data_file NUMERIC(1,0),
    is_error NUMERIC(1,0),
    old_ein_enc CHARACTER VARYING(4000),
    new_ein_enc CHARACTER VARYING(4000),
    old_ein CHARACTER VARYING(80),
    new_ein CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);



CREATE TABLE pspadm.psp_entity_update_hist(
    entity_update_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    entity_name CHARACTER VARYING(4000),
    retry_count NUMERIC(10,0),
    changed_attributes TEXT,
    status CHARACTER VARYING(255),
    transaction_id CHARACTER VARYING(4000),
    event_type CHARACTER VARYING(255),
    entity_id CHARACTER VARYING(4000)
);


CREATE TABLE pspadm.psp_event_as400_sync(
    event_as400_sync_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    retry_count NUMERIC(10,0),
    company_event_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) 
);

CREATE TABLE pspadm.psp_event_detail_type(
    event_detail_type_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    value_class_name CHARACTER VARYING(150)
);

CREATE TABLE pspadm.psp_event_log(
    event_log_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    event_log_type_cd CHARACTER VARYING(255),
    company_id CHARACTER VARYING(4000),
    domain_name CHARACTER VARYING(4000),
    component_name CHARACTER VARYING(4000),
    architecture_name CHARACTER VARYING(4000),
    host_name CHARACTER VARYING(4000),
    application_name CHARACTER VARYING(4000),
    object_name CHARACTER VARYING(4000),
    message CHARACTER VARYING(4000),
    message_dttm TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_event_type(
    event_type_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    event_group_cd CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_evttp_srcsys_assoc(
    interesting_event_types_fk CHARACTER VARYING(255) NOT NULL,
    source_system_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_failed_payroll_run(
    failed_payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_token CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_fee(
    fee_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    description CHARACTER VARYING(160),
    fee_cd CHARACTER VARYING(255),
    name CHARACTER VARYING(80),
    source_system_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL
);


CREATE TABLE pspadm.psp_financial_txn_action(
    financial_txn_action_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    action_event_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_fintxn_onholdreason_assoc(
    financial_transaction_fk CHARACTER VARYING(255) NOT NULL,
    on_hold_reason_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_forecast(
    forecast_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    estimated_transaction_count NUMERIC(10,0),
    status CHARACTER VARYING(255),
    run_date TIMESTAMP(6) WITHOUT TIME ZONE,
    actual_transaction_count NUMERIC(10,0)
);

CREATE TABLE pspadm.psp_forecast_detail(
    forecast_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    job_action CHARACTER VARYING(4000),
    estimated_run_time NUMERIC(19,0),
    actual_run_time NUMERIC(19,0),
    forecast_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_form_template(
    form_template_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(4000),
    default_form_template CHARACTER VARYING(50),
    agency_fk CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_fraud_address(
    fraud_address_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    address_line1 CHARACTER VARYING(80),
    address_line2 CHARACTER VARYING(80),
    address_line3 CHARACTER VARYING(80),
    city CHARACTER VARYING(256),
    state CHARACTER VARYING(21),
    zip_code CHARACTER VARYING(13),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_fraud_bank_account(
    fraud_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    routing_number CHARACTER VARYING(20),
    account_type_cd CHARACTER VARYING(255),
    bank_name CHARACTER VARYING(256),
    bank_account_owner_name CHARACTER VARYING(256),
    fraud_bank_account_reason CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    account_number_enc CHARACTER VARYING(4000),
    account_number CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_fraud_company(
    fraud_company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    dba_name CHARACTER VARYING(100),
    legal_name CHARACTER VARYING(100),
    license_number CHARACTER VARYING(100),
    notification_email CHARACTER VARYING(100),
    source_agreement_id CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_fraud_contact(
    fraud_contact_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    email CHARACTER VARYING(100),
    first_name CHARACTER VARYING(80),
    last_name CHARACTER VARYING(80),
    phone CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_fraud_event(
    fraud_event_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    event_type_cd CHARACTER VARYING(255),
    event_status_cd CHARACTER VARYING(255),
    event_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    company_psid CHARACTER VARYING(50),
    fraud_category CHARACTER VARYING(40),
    fraud_trigger_detail CHARACTER VARYING(4000),
    payroll_direct_deposit_amount NUMERIC(19,4),
    company_event_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255),
    company_ein_enc CHARACTER VARYING(4000),
    fraud_trigger_detail_enc CHARACTER VARYING(4000),
    company_ein CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_fraud_rule(
    fraud_rule_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(255),
    source_system_cd CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_fraud_value(
    fraud_value_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(255),
    value CHARACTER VARYING(255),
    fraud_rule_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_fset_file(
    fset_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(250),
    file_type CHARACTER VARYING(255),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    transmission_id CHARACTER VARYING(50),
    submit_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_fset_filing_detail(
    fset_filing_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    filing_due_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status CHARACTER VARYING(255),
    business_name CHARACTER VARYING(100),
    address_line1 CHARACTER VARYING(80),
    city CHARACTER VARYING(256),
    state CHARACTER VARYING(21),
    zip CHARACTER VARYING(13),
    filing_amount NUMERIC(19,4),
    submission_id CHARACTER VARYING(50),
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    address_line2 CHARACTER VARYING(80),
    filing_status_date TIMESTAMP(6) WITHOUT TIME ZONE,
    error_message CHARACTER VARYING(4000),
    response_file_fk CHARACTER VARYING(255),
    parent_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    agency_id_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(80),
    agency_id CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_funding_model(
    funding_model_cd CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    number_of_funding_days NUMERIC(10,0)
);

CREATE TABLE pspadm.psp_gems_ledger_posting_rule(
    gems_ledger_posting_rule_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    account CHARACTER VARYING(80),
    company CHARACTER VARYING(16),
    department CHARACTER VARYING(16),
    group_code CHARACTER VARYING(16),
    inter_company CHARACTER VARYING(16),
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    reporting_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_gems_monthly_balance(
    gems_monthly_balance_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    period_balance NUMERIC(19,4),
    reported_balance NUMERIC(19,4),
    reporting_period CHARACTER VARYING(40),
    to_date_balance NUMERIC(19,4),
    gems_ledger_posting_rule_fk CHARACTER VARYING(255) NOT NULL,
    gems_upload_batch_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_gems_upload_batch(
    gems_upload_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    batch_id NUMERIC(10,0),
    batch_type CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_status CHARACTER VARYING(255),
    file_name CHARACTER VARYING(1000)
);

CREATE TABLE pspadm.psp_hours_worked_exception(
    hours_worked_exception_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    pay_type CHARACTER VARYING(255),
    payroll_item_cd CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_individual(
    individual_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    first_name CHARACTER VARYING(80),
    gender_cd CHARACTER VARYING(255),
    last_name CHARACTER VARYING(80),
    middle_name CHARACTER VARYING(80),
    communication_type_preference CHARACTER VARYING(255),
    email CHARACTER VARYING(100),
    phone CHARACTER VARYING(100),
    mailing_address_fk CHARACTER VARYING(255),
    suffix CHARACTER VARYING(20),
    has_invalid_email NUMERIC(1,0) NOT NULL DEFAULT 0
);

CREATE TABLE pspadm.psp_industry_type(
    industry_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    industry CHARACTER VARYING(4000),
    standard_industry_code CHARACTER VARYING(10)
);

CREATE TABLE pspadm.psp_intuit_ba_bt_ft(
    intuit_ba_bt_ft_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    file_type CHARACTER VARYING(255),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    intuit_bank_account_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_intuit_bank_acc_txn_type(
    intuit_bank_acc_txn_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    credit_debit_ind CHARACTER VARYING(255),
    intuit_bank_account_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_intuit_bank_account(
    intuit_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    eff_dttm TIMESTAMP(6) WITHOUT TIME ZONE,
    exp_dttm TIMESTAMP(6) WITHOUT TIME ZONE,
    bank_account_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_intuit_shipper_info(
    intuit_shipper_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    shipper_name CHARACTER VARYING(50),
    shipper_address_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_iopsync_company(
    iopsync_company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    company_id NUMERIC(10,0),
    has_employee_payroll NUMERIC(1,0),
    has_contractor_payment NUMERIC(1,0),
    notes CHARACTER VARYING(4000),
    retry_count NUMERIC(10,0),
    status CHARACTER VARYING(255),
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    end_time TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_law(
    law_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(4000),
    law_category_code CHARACTER VARYING(255),
    law_abbrev CHARACTER VARYING(50),
    is_employer_tax NUMERIC(1,0),
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    law_type_cd CHARACTER VARYING(15),
    requires_month_counts NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_law_rate_range(
    law_rate_range_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    min_rate NUMERIC(19,7),
    max_rate NUMERIC(19,7),
    precision NUMERIC(10,0),
    law_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_law_rate_value(
    law_rate_value_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    rate NUMERIC(19,7),
    law_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_lb_tmp(
    company_id CHARACTER VARYING(20)
);

CREATE TABLE pspadm.psp_lb_tmp_comp_fk(
    company_seq CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_lb_tmp_comp_fk_5000(
    company_seq CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_ledger_account(
    ledger_account_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    balance_calculation_rule CHARACTER VARYING(255),
    ledger_account_type CHARACTER VARYING(255),
    reporting_frequency CHARACTER VARYING(255),
    requires_quarter_law NUMERIC(1,0),
    account_abbreviation CHARACTER VARYING(20)
);

CREATE TABLE pspadm.psp_ledger_account_action(
    ledger_account_action_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    credit_debit_indicator CHARACTER VARYING(255),
    action_event_fk CHARACTER VARYING(255) NOT NULL,
    ledger_account_fk CHARACTER VARYING(255) NOT NULL
);


CREATE TABLE pspadm.psp_ledger_balance_bkp(
    ledger_balance_seq CHARACTER VARYING(255),
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    balance_amount NUMERIC(19,4),
    balance_date TIMESTAMP(6) WITHOUT TIME ZONE,
    ledger_account_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    reporting_type CHARACTER VARYING(100) NOT NULL DEFAULT 'DirectDeposit'
);

CREATE TABLE pspadm.psp_ledger_operation(
    ledger_operation_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_system_code CHARACTER VARYING(255),
    source_company_id CHARACTER VARYING(4000),
    amount NUMERIC(19,4),
    memo CHARACTER VARYING(4000),
    check_date TIMESTAMP(6) WITHOUT TIME ZONE,
    original_legal_name CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    messages CHARACTER VARYING(4000),
    original_index NUMERIC(10,0),
    law_fk CHARACTER VARYING(255),
    ledger_operation_job_fk CHARACTER VARYING(255),
    wage_amount NUMERIC(19,4)
);

CREATE TABLE pspadm.psp_ledger_operation_job(
    ledger_operation_job_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finish_time TIMESTAMP(6) WITHOUT TIME ZONE,
    job_type CHARACTER VARYING(255),
    original_file TEXT,
    processed_file TEXT,
    description CHARACTER VARYING(256)
);

CREATE TABLE pspadm.psp_liab_check_billing_assoc(
    liab_check_billing_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    billing_detail_fk CHARACTER VARYING(255),
    liability_check_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_liability_adjustment(
    liability_adjustment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    comp_adjust_submission_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255) NOT NULL,
    employee_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255) NOT NULL,
    taxable_wages NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    company_law_fk CHARACTER VARYING(255),
    is_reconciling_adjustment NUMERIC(1,0),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_liability_check(
    liability_check_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_void NUMERIC(1,0),
    source_id CHARACTER VARYING(9),
    transaction_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    type CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    system_modified_token NUMERIC(19,0)
);

CREATE TABLE pspadm.psp_liability_check_line(
    liability_check_line_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    liability_check_fk CHARACTER VARYING(255) NOT NULL,
    company_law_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_limit_rule(
    limit_rule_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_system_cd CHARACTER VARYING(255),
    description CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_limit_value(
    limit_value_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(255),
    value CHARACTER VARYING(255),
    tier NUMERIC(10,0),
    limit_rule_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_manual_requirement(
    manual_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_message_log(
    message_log_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    request_log TEXT,
    transaction_id CHARACTER VARYING(4000),
    response_log TEXT,
    flow_type CHARACTER VARYING(255)
);


CREATE TABLE pspadm.psp_nachafile(
    nachafile_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    confirmation_code CHARACTER VARYING(100),
    file_name CHARACTER VARYING(1000),
    confirmation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    finalization_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    transmission_date TIMESTAMP(6) WITHOUT TIME ZONE,
    credit_txn_total_amount NUMERIC(19,4),
    debit_txn_total_amount NUMERIC(19,4),
    status CHARACTER VARYING(255),
    file_type CHARACTER VARYING(255),
    file_i_d_modifier CHARACTER VARYING(50),
    offload_batch_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_offer(
    offer_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    offer_cd CHARACTER VARYING(40),
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    is_approved NUMERIC(1,0),
    discount_type CHARACTER VARYING(255),
    discount_amount NUMERIC(19,4),
    discount_percent NUMERIC(19,7),
    begin_event CHARACTER VARYING(255),
    end_event CHARACTER VARYING(255),
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    duration_days NUMERIC(10,0),
    usages_allowed NUMERIC(10,0),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    promotion_id CHARACTER VARYING(50),
    offer_restriction CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_offer_price(
    offer_price_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    fee_type CHARACTER VARYING(255),
    offer_fk CHARACTER VARYING(255) NOT NULL,
    alt_unit_price NUMERIC(19,4),
    alt_base_price NUMERIC(19,4)
);

CREATE TABLE pspadm.psp_offer_svcchg_assoc(
    offer_fk CHARACTER VARYING(255) NOT NULL,
    offering_service_charge_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_offering(
    offering_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    s_k_u CHARACTER VARYING(40),
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    is_approved NUMERIC(1,0),
    offering_code CHARACTER VARYING(255),
    service_code CHARACTER VARYING(255),
    limit_rule_fk CHARACTER VARYING(255),
    reporting_type CHARACTER VARYING(255),
    fraud_rule_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_offering_svcchg(
    offering_svcchg_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    s_k_u CHARACTER VARYING(40),
    is_tier NUMERIC(1,0),
    tier_number NUMERIC(10,0),
    tier_units NUMERIC(10,0),
    sku_type CHARACTER VARYING(255),
    offering_svcchg_grp_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_offering_svcchg_grp(
    offering_svcchg_grp_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    applies_to CHARACTER VARYING(255),
    offering_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_offload_batch(
    offload_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    status_effecive_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offload_group_fk CHARACTER VARYING(255) NOT NULL,
    is_offldtxn_evt_complete NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_offload_group(
    offload_group_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    cutoff_time CHARACTER VARYING(20),
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    offload_group_cd CHARACTER VARYING(10)
);

CREATE TABLE pspadm.psp_on_hold_reason(
    on_hold_reason_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    on_hold_reason_cd CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_ownership_type(
    ownership_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    ownership CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_pay_item(
    pay_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_item_cd CHARACTER VARYING(255),
    liability_adjustment_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255)
);


CREATE TABLE pspadm.psp_paycheck_usage_hist(
    paycheck_usage_hist_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    notes CHARACTER VARYING(4000),
    employee_usage_fk CHARACTER VARYING(255) NOT NULL,
    paycheck_usage_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_payee(
    payee_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(300),
    email CHARACTER VARYING(100),
    phone CHARACTER VARYING(50),
    source_payee_id CHARACTER VARYING(50),
    company_fk CHARACTER VARYING(255),
    mailing_address_fk CHARACTER VARYING(255),
    is1099 NUMERIC(1,0),
    has_invalid_email NUMERIC(1,0) NOT NULL DEFAULT 0,
    account_number_enc CHARACTER VARYING(4000),
    tax_id_enc CHARACTER VARYING(4000),
    tax_id CHARACTER VARYING(4000),
    account_number CHARACTER VARYING(150),
    record_metadata CHARACTER VARYING(512),
    publish_status CHARACTER VARYING(16) DEFAULT '0000000000000000'
);

CREATE TABLE pspadm.psp_payee_bank_account(
    payee_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payee_fk CHARACTER VARYING(255),
    bank_account_fk CHARACTER VARYING(255),
    session_id CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_payment_batch_assoc(
    payment_batch_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    agency_check_batch_fk CHARACTER VARYING(255) NOT NULL,
    money_movement_transaction_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_payment_method_requirement(
    payment_method_requirement_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    pmt_template_pmt_method_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_payment_requirement(
    payment_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_payment_template(
    payment_template_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    default_deposit_frequency CHARACTER VARYING(50),
    non_modifiable_frequency NUMERIC(1,0),
    payment_template_abbrev CHARACTER VARYING(4000),
    prior_qtr_adj_req_amendment NUMERIC(1,0),
    agency_refunds_quarterly NUMERIC(1,0),
    agency_fk CHARACTER VARYING(255),
    no_calculation NUMERIC(1,0),
    support_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    txp_record_class CHARACTER VARYING(4000),
    category CHARACTER VARYING(255),
    processing_start_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_payment_template_agency_id(
    payment_template_agency_id_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(50),
    payment_template_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_payroll_fraud_batch(
    payroll_fraud_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    end_time TIMESTAMP(6) WITHOUT TIME ZONE,
    number_of_payrolls_processed NUMERIC(19,0)
);

CREATE TABLE pspadm.psp_payroll_frequency(
    payroll_freq_cd CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80)
);

CREATE TABLE pspadm.psp_payroll_item(
    payroll_item_code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payroll_item_description CHARACTER VARYING(4000),
    payroll_item_type CHARACTER VARYING(255),
    tp401k_is_tok_accepted NUMERIC(1,0),
    tp401k_allows_negative_amounts NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_payroll_item_taxable_to(
    payroll_item_taxable_to_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    company_law_fk CHARACTER VARYING(255) NOT NULL,
    company_payroll_item_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_payroll_run(
    payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_direct_deposit_amount NUMERIC(19,4),
    payroll_run_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_pay_run_id CHARACTER VARYING(50),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_run_status CHARACTER VARYING(255),
    paycheck_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    wire_expected_date TIMESTAMP(6) WITHOUT TIME ZONE,
    collection_stage_cd CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) NOT NULL,
    payroll_run_type CHARACTER VARYING(255),
    usage_billing_token NUMERIC(19,0),
    e_e_calculation_token NUMERIC(19,0),
    processed_by_fraud_batch_job NUMERIC(1,0) NOT NULL DEFAULT 0,
    debit_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    d_d_status CHARACTER VARYING(255) NOT NULL DEFAULT 'None',
    tax_and_fees_status CHARACTER VARYING(255) NOT NULL DEFAULT 'None',
    d_d_message_status CHARACTER VARYING(255) NOT NULL DEFAULT 'None',
    assisted_usage_billing_token NUMERIC(19,0) NOT NULL DEFAULT 0,
    funding_model CHARACTER VARYING(4000),
    offload_group_fk CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_payroll_run_action(
    payroll_run_action_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    action_event_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_payroll_subtype(
    payroll_subtype_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payroll_subtype_cd CHARACTER VARYING(255),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offering_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_perf_sst(
    time_pacific TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    transmission_type CHARACTER VARYING(255) NOT NULL,
    "< 1 min" NUMERIC(19,0) NOT NULL DEFAULT 0,
    "1 to 2 mins" NUMERIC(19,0) NOT NULL DEFAULT 0,
    "2 to 3 mins" NUMERIC(19,0) NOT NULL DEFAULT 0,
    "3 to 4 mins" NUMERIC(19,0) NOT NULL DEFAULT 0,
    "4 to 5 mins" NUMERIC(19,0) NOT NULL DEFAULT 0,
    "> 5 mins" NUMERIC(19,0) NOT NULL DEFAULT 0,
    total NUMERIC(19,0) NOT NULL DEFAULT 0,
    average NUMERIC(19,2) NOT NULL DEFAULT 0,
    "STD DEV" NUMERIC(19,2) NOT NULL DEFAULT 0
);

CREATE TABLE pspadm.psp_pmt_template_bankaccount(
    pmt_template_bankaccount_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(20),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_template_fk CHARACTER VARYING(255),
    bank_account_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_pmt_template_frequency(
    payment_template_frequency_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payment_frequency_id CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    tax_code_id CHARACTER VARYING(8),
    obsolete NUMERIC(1,0),
    agent_disallowed NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_pmt_template_paymentmethod(
    pmt_template_paymentmethod_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payment_method CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255),
    payment_method_order NUMERIC(10,0)
);

CREATE TABLE pspadm.psp_pmttemplate_chkinfo_assoc(
    pmttemplate_chkinfo_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    pmttemplate_printedchkinfo_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_pmttemplate_printedchkinfo(
    pmttemplate_printedchkinfo_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name_line2 CHARACTER VARYING(40),
    name_line1 CHARACTER VARYING(40),
    address_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_posting_rule(
    posting_rule_cd CHARACTER VARYING(40) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    credit_debit_ind CHARACTER VARYING(10),
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_posting_rule_mar7(
    posting_rule_cd CHARACTER VARYING(40) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    credit_debit_ind CHARACTER VARYING(10),
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_prior_payment_submission(
    prior_payment_submission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_id CHARACTER VARYING(9),
    company_fk CHARACTER VARYING(255) NOT NULL
);



CREATE TABLE pspadm.psp_property_audit_bkp(
    property_audit_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
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
);

CREATE TABLE pspadm.psp_pstub_address(
    pstub_address_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    line1 CHARACTER VARYING(51),
    line2 CHARACTER VARYING(51),
    line3 CHARACTER VARYING(51),
    line4 CHARACTER VARYING(51),
    line5 CHARACTER VARYING(51)
);

CREATE TABLE pspadm.psp_pstub_dditem(
    pstub_dditem_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    acct_name CHARACTER VARYING(51),
    acct_number CHARACTER VARYING(30),
    acct_type CHARACTER VARYING(10),
    bank_name CHARACTER VARYING(51),
    name CHARACTER VARYING(31),
    payroll_item_list_id CHARACTER VARYING(51),
    routing_number CHARACTER VARYING(20),
    paystub_fk CHARACTER VARYING(255),
    cur_amt NUMERIC(19,4),
    company_fk CHARACTER VARYING(255)
);


CREATE TABLE pspadm.psp_pstub_employee_preference(
    pstub_employee_preference_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    employee_fk CHARACTER VARYING(255),
    app_name CHARACTER VARYING(30),
    preference_name CHARACTER VARYING(30),
    preference_value CHARACTER VARYING(100)
);

CREATE TABLE pspadm.psp_pstub_employer_info(
    pstub_employer_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(60),
    name_addr_fed_id CHARACTER VARYING(200),
    object_hash CHARACTER VARYING(200),
    pstub_address_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_pstub_msg(
    pstub_msg_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    sequence NUMERIC(10,0),
    type CHARACTER VARYING(255),
    text CHARACTER VARYING(200),
    paystub_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);




CREATE TABLE pspadm.psp_pstub_state_tax_info(
    pstub_state_tax_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    agency_name CHARACTER VARYING(300),
    pstub_employer_info_fk CHARACTER VARYING(255) NOT NULL,
    agency_id_enc CHARACTER VARYING(4000),
    agency_id CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_qbdt_employee_info(
    qbdt_employee_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    bill_pay_account CHARACTER VARYING(64),
    initials CHARACTER VARYING(12),
    tracking_class CHARACTER VARYING(128),
    use_time NUMERIC(1,0),
    enforce_subject_to NUMERIC(1,0),
    employee_type CHARACTER VARYING(255),
    is_deleted NUMERIC(1,0),
    title CHARACTER VARYING(20),
    alt_phone CHARACTER VARYING(21),
    employee_fk CHARACTER VARYING(255),
    list_id CHARACTER VARYING(38),
    company_fk CHARACTER VARYING(255),
    token NUMERIC(19,0) DEFAULT - 1,
    use_d_d NUMERIC(1,0) DEFAULT 0,
    print_as_name CHARACTER VARYING(50),
    is_recoverable NUMERIC(1,0) DEFAULT 0,
    is_assisted NUMERIC(1,0) NOT NULL DEFAULT 0,
    employee_seasonal CHARACTER VARYING(255)
);



CREATE TABLE pspadm.psp_qbdt_payroll_item_info(
    qbdt_payroll_item_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    earnings_table NUMERIC(1,0),
    is_employee_paid NUMERIC(1,0),
    liability_account CHARACTER VARYING(255),
    liability_agency CHARACTER VARYING(255),
    adjusts_gross NUMERIC(1,0),
    based_on_quantity NUMERIC(1,0),
    expense_account CHARACTER VARYING(255),
    default_rate NUMERIC(19,7),
    default_limit NUMERIC(19,4),
    expense_by_job NUMERIC(1,0),
    pay_type CHARACTER VARYING(255),
    special_type CHARACTER VARYING(255),
    is_deleted NUMERIC(1,0),
    on_service NUMERIC(1,0),
    default_rate_type CHARACTER VARYING(255),
    company_law_fk CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255),
    list_id CHARACTER VARYING(38),
    company_fk CHARACTER VARYING(255),
    token NUMERIC(19,0) DEFAULT - 1,
    rate_push_token NUMERIC(19,0),
    overtime_multiplier NUMERIC(19,7) DEFAULT 1.0,
    detail_type NUMERIC(19,0) DEFAULT - 1,
    agency_id_enc CHARACTER VARYING(4000),
    agency_id CHARACTER VARYING(24),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_qbdt_payroll_trans_line(
    qbdt_payroll_trans_line_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    wage_base_amount NUMERIC(19,4),
    taxable_wage_amount NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    qbdt_payroll_transaction_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_qbdt_payroll_transaction(
    qbdt_payroll_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    transaction_type CHARACTER VARYING(255),
    amount NUMERIC(19,4),
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_voided NUMERIC(1,0),
    source_id CHARACTER VARYING(9),
    transaction_date TIMESTAMP(6) WITHOUT TIME ZONE,
    comp_adjust_submission_fk CHARACTER VARYING(255),
    prior_payment_submission_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    employee_name CHARACTER VARYING(255),
    e_e_calculation_token NUMERIC(19,0) DEFAULT - 1
);

CREATE TABLE pspadm.psp_qbdt_request_info(
    qbdt_request_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    employee_add_count NUMERIC(10,0),
    employee_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_update_count NUMERIC(10,0),
    employee_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_add_count NUMERIC(10,0),
    paycheck_update_count NUMERIC(10,0),
    payroll_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_count NUMERIC(10,0),
    payroll_item_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_update_count NUMERIC(10,0),
    payroll_item_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_count NUMERIC(10,0),
    payroll_transaction_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_trans_update_count NUMERIC(10,0),
    payroll_trans_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_trans_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_delete_count NUMERIC(10,0),
    payroll_item_delete_count NUMERIC(10,0),
    payroll_trans_delete_count NUMERIC(10,0),
    delete_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    delete_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    source_system_transmission_fk CHARACTER VARYING(255),
    paycheck_delete_count NUMERIC(10,0)
);


CREATE TABLE pspadm.psp_qbdt_unprocessed_request(
    qbdt_unprocessed_request_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    error_message CHARACTER VARYING(4000),
    source_system_transmission_id CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_quickbooks_info(
    quickbooks_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    license_number CHARACTER VARYING(100),
    coa_fee_account_name CHARACTER VARYING(100),
    coa_sales_tax_account_name CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    a_s400_payroll_count NUMERIC(19,0),
    token NUMERIC(19,0),
    company_fk CHARACTER VARYING(255) NOT NULL,
    file_id CHARACTER VARYING(50),
    process_transmissions NUMERIC(1,0),
    symphony_on_board_version CHARACTER VARYING(100),
    i_a_m_realm_id CHARACTER VARYING(50),
    allow_transmissions NUMERIC(1,0) DEFAULT 1,
    watermark_date TIMESTAMP(6) WITHOUT TIME ZONE,
    quickbooks_sku CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_racompany_service_info(
    racompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    override_company_limit_amount NUMERIC(19,4)
);

CREATE TABLE pspadm.psp_rafenrollment(
    rafenrollment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255),
    status_reason CHARACTER VARYING(1000)
);

CREATE TABLE pspadm.psp_rafenrollment_detail(
    rafenrollment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    legal_name CHARACTER VARYING(4000),
    legal_street_address CHARACTER VARYING(4000),
    legal_city CHARACTER VARYING(4000),
    legal_state CHARACTER VARYING(4000),
    legal_zip_code CHARACTER VARYING(4000),
    r_a_f_enrollment_fk CHARACTER VARYING(255) NOT NULL,
    enrollment_file_fk CHARACTER VARYING(255) NOT NULL,
    delete_file_fk CHARACTER VARYING(255),
    f941_tax_period CHARACTER VARYING(100),
    f940_tax_period CHARACTER VARYING(100),
    f94x_f_t_d_period CHARACTER VARYING(100),
    fed_taxid_enc CHARACTER VARYING(4000),
    fed_taxid CHARACTER VARYING(4000),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_rafenrollment_file(
    rafenrollment_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    r_a_f_action_code CHARACTER VARYING(255),
    file_name CHARACTER VARYING(4000),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    email_file_name CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_rate_ledger_operation(
    rate_ledger_operation_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    additional_filing_amount_name CHARACTER VARYING(4000),
    push_to_quick_books NUMERIC(1,0),
    rate NUMERIC(19,7)
);

CREATE TABLE pspadm.psp_report_job_setup(
    report_name CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    report_schedule CHARACTER VARYING(4000),
    report_mailing_list CHARACTER VARYING(4000),
    query_filename CHARACTER VARYING(4000),
    is_automatically_scheduled NUMERIC(1,0),
    report_namespace CHARACTER VARYING(4000),
    encrypted_fields CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_reporting_agent(
    reporting_agent_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    fax CHARACTER VARYING(100),
    contact CHARACTER VARYING(300),
    legal_name CHARACTER VARYING(100),
    phone CHARACTER VARYING(100),
    address_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_id_enc CHARACTER VARYING(4000),
    fed_id CHARACTER VARYING(80),
    fed_tax_id CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_return_reason_desc(
    reason_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256)
);

CREATE TABLE pspadm.psp_role_sub_status(
    role_sub_status_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    allowed_change_type CHARACTER VARYING(255),
    auth_role_fk CHARACTER VARYING(255) NOT NULL,
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_rpt_paid_employees(
    service_type CHARACTER VARYING(15) NOT NULL,
    start_date TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    company_seq CHARACTER VARYING(36) NOT NULL,
    employee_count NUMERIC(9,0),
    total_gross_pay NUMERIC(19,4),
    total_net_pay NUMERIC(19,4)
);

CREATE TABLE pspadm.psp_rtbautomationbackup(
    rtbautomationbackup_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    rtb_backup TEXT,
    company_id CHARACTER VARYING(4000),
    event_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_sap_method_call(
    sap_method_call_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    screen_path CHARACTER VARYING(400),
    service_name CHARACTER VARYING(255),
    method_name CHARACTER VARYING(255),
    parameters CHARACTER VARYING(4000),
    result_size NUMERIC(19,0) DEFAULT - 1,
    elapsed_millis NUMERIC(19,0) DEFAULT - 1,
    exception_trace CHARACTER VARYING(4000),
    security_principal CHARACTER VARYING(255),
    session_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200)
);

CREATE TABLE pspadm.psp_sap_method_call_bkp(
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    screen_path CHARACTER VARYING(400),
    service_name CHARACTER VARYING(255),
    method_name CHARACTER VARYING(255),
    parameters CHARACTER VARYING(4000),
    result_size NUMERIC(8,0),
    elapsed_millis NUMERIC(8,0),
    exception_trace CHARACTER VARYING(4000),
    security_principal CHARACTER VARYING(255),
    session_id CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_saved_reports(
    saved_reports_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    report_id CHARACTER VARYING(256),
    display_name CHARACTER VARYING(256),
    input_param CHARACTER VARYING(4000),
    query TEXT,
    description CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_second_offload(
    second_offload_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    override_cutoff_time CHARACTER VARYING(16),
    offload_group_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_serv_stat_txn_sku_type(
    serv_stat_txn_sku_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    sku_type CHARACTER VARYING(255),
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    offering_service_charge_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_service(
    service_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    can_be_manually_cancelled NUMERIC(1,0),
    psp_provides_customer_service NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_service_status(
    service_status_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256)
);

CREATE TABLE pspadm.psp_service_sub_status(
    service_sub_status_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(500),
    name CHARACTER VARYING(100),
    is_set_manually NUMERIC(1,0),
    is_removed_manually NUMERIC(1,0),
    service_status_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_smsmigration(
    smsmigration_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    migration_status CHARACTER VARYING(255),
    validation_error_result TEXT,
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_smssync_failure(
    smssync_failure_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    company_realm_id NUMERIC(19,0),
    sync_direction CHARACTER VARYING(255),
    last_retry_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    status CHARACTER VARYING(255),
    count NUMERIC(10,0),
    failure_reason CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_source_payroll_parameter(
    source_payroll_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    parameter_cd CHARACTER VARYING(255),
    parameter_value CHARACTER VARYING(100),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_system_cd CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_source_system(
    source_system_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80)
);

CREATE TABLE pspadm.psp_source_system_law_assoc(
    source_system_law_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_law_code CHARACTER VARYING(80),
    law_fk CHARACTER VARYING(255) NOT NULL,
    source_system_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_sourcesys_printedchk_info(
    sourcesys_printedchk_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name_line1 CHARACTER VARYING(40),
    name_line2 CHARACTER VARYING(40),
    next_check_number NUMERIC(19,0),
    source_system_code CHARACTER VARYING(255),
    source_system_logo TEXT,
    bank_logo TEXT,
    address_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_sql_execution_log_entry(
    sql_execution_log_entry_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    row_count NUMERIC(10,0),
    s_q_l TEXT,
    reason CHARACTER VARYING(2000),
    execution_time NUMERIC(19,0),
    error_message CHARACTER VARYING(4000),
    user_name CHARACTER VARYING(60),
    committed NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_state_edi_tax_file(
    state_edi_tax_file_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    ack_file_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_state_report_assoc(
    state_report_output_fk CHARACTER VARYING(255),
    payment_template_frequency_fk CHARACTER VARYING(255),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    state_report_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE pspadm.psp_state_report_output(
    state_report_output_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    report_output TEXT,
    report_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_suicredits_job(
    suicredits_job_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    year NUMERIC(10,0),
    quarter NUMERIC(10,0),
    status CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255),
    processed_file TEXT
);

CREATE TABLE pspadm.psp_svcchgprice(
    svcchgprice_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offering_service_charge_fk CHARACTER VARYING(255) NOT NULL,
    unit_price NUMERIC(19,4),
    base_price NUMERIC(19,4)
);

CREATE TABLE pspadm.psp_svcstat_srcsys_assoc(
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    source_system_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_svcstat_svc_assoc(
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    service_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_svcstat_syscap_assoc(
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    system_capability_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_system_capability(
    system_capability_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256)
);

CREATE TABLE pspadm.psp_system_parameter(
    system_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    system_parameter_cd CHARACTER VARYING(256),
    system_parameter_description CHARACTER VARYING(160),
    system_parameter_org CHARACTER VARYING(80),
    system_parameter_value CHARACTER VARYING(150),
    is_secured NUMERIC(1,0) NOT NULL
);

CREATE TABLE pspadm.psp_system_payment_requirement(
    system_payment_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    system_requirement_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_system_requirement(
    system_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    system_requirement_type CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_tax_account_audit(
    audit_id DOUBLE PRECISION NOT NULL,
    table_name CHARACTER VARYING(35),
    mesg_txt CHARACTER VARYING(4000),
    chg_dt TIMESTAMP(6) WITHOUT TIME ZONE,
    row_id CHARACTER(255),
    reported CHARACTER VARYING(3) NOT NULL DEFAULT 'NO'
);

CREATE TABLE pspadm.psp_tax_company_service_info(
    tax_company_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    last_quarter_to_file NUMERIC(10,0),
    file_annual_returns NUMERIC(1,0),
    final_annual_returns NUMERIC(1,0),
    last_payroll_date TIMESTAMP(6) WITHOUT TIME ZONE,
    w2_delivery_preference_cd CHARACTER VARYING(255),
    client_packet_delivery_pref_cd CHARACTER VARYING(255),
    last_tax_year NUMERIC(10,0),
    in_house_w2 NUMERIC(1,0),
    include_on_s_s_a_file NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_tax_credits9061(
    tax_credits9061_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    form9061 TEXT,
    employee_name CHARACTER VARYING(242),
    tax_credits_application_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    s_s_n_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(9),
    s_s_n CHARACTER VARYING(9),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_tax_credits_application(
    tax_credits_application_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    unsigned_document TEXT,
    signed_document TEXT,
    document_key CHARACTER VARYING(4000),
    signers_remaining CHARACTER VARYING(4000),
    document_password CHARACTER VARYING(4000),
    employer_email CHARACTER VARYING(4000),
    employee_email CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_tax_payment_on_hold_reason(
    tax_payment_on_hold_reason_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    on_hold_reason_cd CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    note CHARACTER VARYING(500),
    company_fk CHARACTER VARYING(255) 
);

CREATE TABLE pspadm.psp_tax_penalty_interest(
    tax_penalty_interest_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    type CHARACTER VARYING(255),
    transaction_id CHARACTER VARYING(100),
    penalty_interest_date TIMESTAMP(6) WITHOUT TIME ZONE,
    note CHARACTER VARYING(4000),
    payment_method CHARACTER VARYING(255),
    period_type CHARACTER VARYING(255),
    year NUMERIC(10,0),
    amount NUMERIC(19,4),
    period_number NUMERIC(10,0),
    check_number CHARACTER VARYING(30),
    company_agency_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_tax_table_misc_data(
    tax_table_misc_data_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    value CHARACTER VARYING(56),
    employee_tax_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    misc_data_order NUMERIC(10,0) DEFAULT - 1
);

CREATE TABLE pspadm.psp_third_party401k_batch(
    third_party401k_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    batch_id NUMERIC(10,0),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_status_cd CHARACTER VARYING(255),
    file_name CHARACTER VARYING(1000)
);

CREATE TABLE pspadm.psp_threshold_requirement(
    threshold_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    maximum_payment_amount NUMERIC(19,4)
);

CREATE TABLE pspadm.psp_tp401k_batch_employee(
    tp401k_batch_employee_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    third_party401k_batch_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_tp401k_batch_paycheck(
    tp401k_batch_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    third_party401k_batch_fk CHARACTER VARYING(255),
    paycheck_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_tp401k_paycheck(
    tp401k_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    current_state_cd CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) 
);

CREATE TABLE pspadm.psp_tp401k_paycheck_pending(
    tp401k_paycheck_pending_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    state_cd CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    third_party401k_paycheck_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_tp401k_paycheck_state(
    tp401k_paycheck_state_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    state_cd CHARACTER VARYING(255),
    state_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    third_party401k_paycheck_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_tp401k_signup_batch(
    tp401k_signup_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    file_name CHARACTER VARYING(1000),
    batch_id NUMERIC(10,0),
    download_date TIMESTAMP(6) WITHOUT TIME ZONE,
    download_status_cd CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_tp401k_signup_queue(
    tp401k_signup_queue_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    custodial_id CHARACTER VARYING(100),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    legal_name CHARACTER VARYING(100),
    has_safe_harbor NUMERIC(1,0),
    status CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(80),
    record_metadata CHARACTER VARYING(512)
);

CREATE TABLE pspadm.psp_tp401kcompany_service_info(
    tp401kcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    custodial_id CHARACTER VARYING(4000),
    has_safe_harbor NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_transaction_offload_batch(
    transaction_offload_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    offload_status_cd CHARACTER VARYING(10),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    financial_transaction_fk CHARACTER VARYING(255) NOT NULL,
    offload_batch_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) 

);

CREATE TABLE pspadm.psp_transaction_response(
    transaction_response_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_request_id CHARACTER VARYING(50),
    transaction_token_number NUMERIC(19,0),
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_transaction_return(
    transaction_return_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    bank_return_cd CHARACTER VARYING(10),
    bank_return_description CHARACTER VARYING(160),
    bank_return_trace_number NUMERIC(19,0),
    return_status_cd CHARACTER VARYING(255),
    return_status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    money_movement_transaction_fk CHARACTER VARYING(255),
    return_batch_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_transaction_return_batch(
    transaction_return_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    a_c_h_return_file_name CHARACTER VARYING(1000),
    return_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_transaction_state(
    transaction_state_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80)
);

CREATE TABLE pspadm.psp_transaction_type(
    transaction_type_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    transaction_category CHARACTER VARYING(255),
    association_type CHARACTER VARYING(255),
    fee_ind NUMERIC(1,0),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    transaction_type_group_cd CHARACTER VARYING(255),
    include_in_txn_response NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_transaction_type_mar7(
    transaction_type_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    transaction_category CHARACTER VARYING(255),
    association_type CHARACTER VARYING(255),
    fee_ind NUMERIC(1,0),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    transaction_type_group_cd CHARACTER VARYING(255),
    include_in_txn_response NUMERIC(1,0)
);

CREATE TABLE pspadm.psp_transmission_payroll_run(
    transmission_payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    payroll_process CHARACTER VARYING(255),
    source_system_transmission_id CHARACTER VARYING(255) NOT NULL,
    payroll_run_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_txntype_service_assoc(
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    service_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
);

CREATE TABLE pspadm.psp_usage_period(
    usage_period_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_usage_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_user_preference(
    key CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    default_value CHARACTER VARYING(4000)
);

CREATE TABLE pspadm.psp_user_setting(
    user_setting_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    value CHARACTER VARYING(4000),
    user_preference_fk CHARACTER VARYING(255),
    auth_user_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_vmp_employee_info(
    vmp_employee_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    consumer_realm_id CHARACTER VARYING(4000),
    employee_recnum CHARACTER VARYING(4000),
    email CHARACTER VARYING(4000),
    persona_id CHARACTER VARYING(4000),
    company_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.psp_voided_check(
    voided_check_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    reason CHARACTER VARYING(4000),
    money_movement_transaction_fk CHARACTER VARYING(255),
    accounting_report_file_fk CHARACTER VARYING(255),
    agency_check_batch_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_wage_limit(
    wage_limit_id CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0),
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    effective_year_quarter CHARACTER VARYING(5),
    amount NUMERIC(19,4),
    law_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_wc_company(
    wc_company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    subs_type_cd CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    integration_id CHARACTER VARYING(255),
    subs_end_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.psp_wc_paycheck(
    wc_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    current_state_cd CHARACTER VARYING(255),
    paycheck_fk CHARACTER VARYING(255),
    paycheck_version NUMERIC(19,0) NOT NULL DEFAULT 1,
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_wc_paycheck_pending(
    wc_paycheck_pending_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    state_cd CHARACTER VARYING(255),
    workers_comp_paycheck_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.psp_wc_paycheck_state(
    wc_paycheck_state_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    state_cd CHARACTER VARYING(255),
    state_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    workers_comp_paycheck_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.rep_test(
    name CHARACTER VARYING(10)
);

CREATE TABLE pspadm.stats_11dec(
    statid CHARACTER VARYING(128),
    type CHARACTER(1),
    version DOUBLE PRECISION,
    flags DOUBLE PRECISION,
    c1 CHARACTER VARYING(128),
    c2 CHARACTER VARYING(128),
    c3 CHARACTER VARYING(128),
    c4 CHARACTER VARYING(128),
    c5 CHARACTER VARYING(128),
    c6 CHARACTER VARYING(128),
    n1 DOUBLE PRECISION,
    n2 DOUBLE PRECISION,
    n3 DOUBLE PRECISION,
    n4 DOUBLE PRECISION,
    n5 DOUBLE PRECISION,
    n6 DOUBLE PRECISION,
    n7 DOUBLE PRECISION,
    n8 DOUBLE PRECISION,
    n9 DOUBLE PRECISION,
    n10 DOUBLE PRECISION,
    n11 DOUBLE PRECISION,
    n12 DOUBLE PRECISION,
    n13 DOUBLE PRECISION,
    d1 TIMESTAMP(0) WITHOUT TIME ZONE,
    t1 TIMESTAMP(6) WITH TIME ZONE,
    r1 BYTEA,
    r2 BYTEA,
    r3 BYTEA,
    ch1 CHARACTER VARYING(1000),
    cl1 TEXT
);

CREATE TABLE pspadm.t_backfill_metadata(
    batch_id DOUBLE PRECISION,
    table_name CHARACTER VARYING(40),
    partition_name CHARACTER VARYING(30),
    status CHARACTER VARYING(10)
);

CREATE TABLE pspadm.tab_1(
    dat DOUBLE PRECISION
);

CREATE TABLE pspadm.temp1_paycheck(
    paycheck_fk CHARACTER VARYING(255) NOT NULL,
    net_amt DOUBLE PRECISION
);

CREATE TABLE pspadm.temp_a_prc_inc(
    psid CHARACTER VARYING(100),
    offering CHARACTER VARYING(100),
    new_offering CHARACTER VARYING(100),
    change_date TIMESTAMP(0) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.temp_bad_payroll(
    bad_payroll_company_seq CHARACTER VARYING(100)
);

CREATE TABLE pspadm.temp_bkp_edr_june3rd(
    entry_detail_record_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    trace_number CHARACTER VARYING(20),
    credit_debit_indicator CHARACTER VARYING(255),
    record_data CHARACTER VARYING(250),
    intuit_bank_account_fk CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    n_a_c_h_a_file_type CHARACTER VARYING(255),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    legal_name CHARACTER VARYING(100),
    txp_record_data CHARACTER VARYING(90),
    standard_entry_description CHARACTER VARYING(15)
);

CREATE TABLE pspadm.temp_company_07152010(
    company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    phone CHARACTER VARYING(100),
    dba_name CHARACTER VARYING(100),
    fed_tax_id CHARACTER VARYING(80),
    legal_name CHARACTER VARYING(100),
    source_company_id CHARACTER VARYING(50),
    notification_email CHARACTER VARYING(100),
    next_payroll_transaction_id CHARACTER VARYING(50),
    next_paycheck_id CHARACTER VARYING(50),
    next_employee_id CHARACTER VARYING(50),
    next_payline_transaction_id CHARACTER VARYING(50),
    account_locked_until TIMESTAMP(6) WITHOUT TIME ZONE,
    nbr_of_failed_login_attempts NUMERIC(10,0),
    qb_info_app_version CHARACTER VARYING(100),
    quickbooks_info_application_id CHARACTER VARYING(100),
    quickbooks_info_license_number CHARACTER VARYING(100),
    qb_info_coa_fee_acc_name CHARACTER VARYING(100),
    qb_info_coa_sales_tax_acc_name CHARACTER VARYING(100),
    quickbooks_info_tax_table_id CHARACTER VARYING(100),
    qb_info_as400_payroll_count NUMERIC(19,0),
    agree_info_agree_create_date TIMESTAMP(6) WITHOUT TIME ZONE,
    agreement_info_name CHARACTER VARYING(100),
    agreement_info_service_type CHARACTER VARYING(100),
    agree_info_sub_nbr CHARACTER VARYING(100),
    agree_info_source_id CHARACTER VARYING(100),
    agreement_info_service_key CHARACTER VARYING(100),
    agree_info_agree_sub_type CHARACTER VARYING(40),
    current_token NUMERIC(19,0),
    source_system_cd CHARACTER VARYING(255),
    migration_status CHARACTER VARYING(255),
    tax_exempt_expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_flagged_for_fraud NUMERIC(1,0),
    sign_up_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_frequency_fk CHARACTER VARYING(255),
    mailing_address_fk CHARACTER VARYING(255),
    legal_address_fk CHARACTER VARYING(255),
    funding_model_fk CHARACTER VARYING(255) NOT NULL,
    private_key CHARACTER VARYING(1000),
    public_key CHARACTER VARYING(1000),
    nbr_failed_authentications NUMERIC(10,0),
    debug_logging NUMERIC(1,0),
    p_s_id CHARACTER VARYING(50),
    offload_group_fk CHARACTER VARYING(255) NOT NULL,
    cloud_current_token NUMERIC(19,0)
);

CREATE TABLE pspadm.temp_company_datafile(
    company_seq CHARACTER VARYING(255) NOT NULL,
    offering_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.temp_dd4v_prc_inc(
    psid CHARACTER VARYING(100),
    offering CHARACTER VARYING(100),
    new_offering CHARACTER VARYING(100),
    change_date TIMESTAMP(0) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.temp_dd_prc_inc(
    psid CHARACTER VARYING(100),
    offering CHARACTER VARYING(100),
    new_offering CHARACTER VARYING(100),
    change_date TIMESTAMP(0) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.temp_entitle_pending(
    record_pk CHARACTER VARYING(15),
    created_dttm TIMESTAMP(6) WITH TIME ZONE,
    batch_id DOUBLE PRECISION,
    fed_tax_id CHARACTER VARYING(100),
    agree_info_sub_nbr NUMERIC(22,7),
    license_number CHARACTER VARYING(30),
    entitlement_offering_code CHARACTER VARYING(30),
    ec_edition_type CHARACTER VARYING(250),
    ec_num_empl_type CHARACTER VARYING(250),
    ec_asset_item_num CHARACTER VARYING(50),
    order_number CHARACTER VARYING(30),
    order_source CHARACTER VARYING(50),
    customer_id CHARACTER VARYING(30),
    next_charge_date TIMESTAMP(0) WITHOUT TIME ZONE,
    payment_method_type CHARACTER VARYING(30),
    credit_card_type CHARACTER VARYING(30),
    credit_card_number CHARACTER VARYING(4),
    credit_card_expiration CHARACTER VARYING(61),
    contact_email CHARACTER VARYING(100),
    entitlement_state CHARACTER VARYING(30),
    contact_name CHARACTER VARYING(101),
    subscription_number NUMERIC(22,7),
    subscription_end_date TIMESTAMP(0) WITHOUT TIME ZONE,
    billing_zip_code CHARACTER VARYING(30),
    cancellation_reason CHARACTER VARYING(200),
    entitlement_unit_status CHARACTER VARYING(30),
    service_key CHARACTER VARYING(50),
    extension_key CHARACTER VARYING(100),
    last_validation_date TIMESTAMP(0) WITHOUT TIME ZONE,
    migration_status CHARACTER(12),
    migration_status_dttm TIMESTAMP(6) WITH TIME ZONE,
    error_message CHARACTER(4)
);

CREATE TABLE pspadm.temp_fix_ncd(
    ent_seq CHARACTER VARYING(100),
    created_dt TIMESTAMP(6) WITHOUT TIME ZONE,
    lic CHARACTER VARYING(100),
    eoc CHARACTER VARYING(100),
    from_ncd TIMESTAMP(6) WITHOUT TIME ZONE,
    to_ncd TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.temp_fix_ncd_reactivation(
    ent_seq CHARACTER VARYING(100),
    created_dt TIMESTAMP(6) WITHOUT TIME ZONE,
    lic CHARACTER VARYING(100),
    eoc CHARACTER VARYING(100),
    from_ncd TIMESTAMP(6) WITHOUT TIME ZONE,
    to_ncd TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.temp_ft(
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    financial_transaction_amount NUMERIC(19,4),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    settlement_type_cd CHARACTER VARYING(255),
    credit_bank_account_type CHARACTER VARYING(255),
    debit_bank_account_type CHARACTER VARYING(255),
    on_hold NUMERIC(1,0),
    sku CHARACTER VARYING(40),
    sku_quantity NUMERIC(10,0),
    billing_detail_fk CHARACTER VARYING(255),
    credit_bank_account_fk CHARACTER VARYING(255),
    debit_bank_account_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    paycheck_split_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    law_fk CHARACTER VARYING(255),
    current_transaction_state_fk CHARACTER VARYING(255),
    original_transaction_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    original_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    refund_type CHARACTER VARYING(255),
    comp_adjust_submission_fk CHARACTER VARYING(255),
    tax_penalty_interest_fk CHARACTER VARYING(255),
    relatable_transaction_fk CHARACTER VARYING(255),
    bill_payment_split_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.temp_future_payroll_data(
    company_seq CHARACTER VARYING(255) NOT NULL,
    payment_template_cd CHARACTER VARYING(255) NOT NULL,
    paycheck_month DOUBLE PRECISION,
    cnt DOUBLE PRECISION
);

CREATE TABLE pspadm.temp_guid_company(
    guid CHARACTER VARYING(100),
    company_seq CHARACTER VARYING(100),
    row_count_guid NUMERIC(10,0),
    row_count_company NUMERIC(10,0)
);

CREATE TABLE pspadm.temp_modes_agency_ids(
    old_agency_id CHARACTER VARYING(255),
    new_agency_id CHARACTER VARYING(255),
    source_company_id CHARACTER VARYING(50)
);

CREATE TABLE pspadm.temp_orstt_agency_ids(
    source_company_id CHARACTER VARYING(50),
    company_seq CHARACTER VARYING(255),
    old_agency_id CHARACTER VARYING(255),
    new_agency_id CHARACTER VARYING(255),
    company_agency_fk CHARACTER VARYING(255),
    agency_taxpayer_id_enc CHARACTER VARYING(255)
);

CREATE TABLE pspadm.temp_paycheck(
    paycheck_fk CHARACTER VARYING(255) NOT NULL,
    net_amt DOUBLE PRECISION
);

CREATE TABLE pspadm.temp_pedr_feb23(
    entry_detail_record_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    trace_number CHARACTER VARYING(20),
    credit_debit_indicator CHARACTER VARYING(255),
    record_data CHARACTER VARYING(250),
    intuit_bank_account_fk CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    n_a_c_h_a_file_type CHARACTER VARYING(255),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    legal_name CHARACTER VARYING(100),
    txp_record_data CHARACTER VARYING(90),
    standard_entry_description CHARACTER VARYING(15)
);

CREATE TABLE pspadm.temp_pft_feb23(
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    financial_transaction_amount NUMERIC(19,4),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    settlement_type_cd CHARACTER VARYING(255),
    credit_bank_account_type CHARACTER VARYING(255),
    debit_bank_account_type CHARACTER VARYING(255),
    on_hold NUMERIC(1,0),
    sku CHARACTER VARYING(40),
    sku_quantity NUMERIC(10,0),
    billing_detail_fk CHARACTER VARYING(255),
    credit_bank_account_fk CHARACTER VARYING(255),
    debit_bank_account_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    paycheck_split_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
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
);

CREATE TABLE pspadm.temp_pfts_feb23(
    financial_trans_state_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    transaction_state_eff_date TIMESTAMP(6) WITHOUT TIME ZONE,
    insert_user_id CHARACTER VARYING(30),
    gems_upload_batch_fk CHARACTER VARYING(255),
    financial_transaction_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL,
    transaction_response_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.temp_pmmt_feb23(
    money_movement_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
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
    agency_taxpayer_id CHARACTER VARYING(80),
    payment_template_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.temp_psp_payroll_fraud_batch(
    payroll_fraud_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    max_processed_token NUMERIC(19,0),
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    end_time TIMESTAMP(6) WITHOUT TIME ZONE,
    number_of_payrolls_processed NUMERIC(19,0)
);

CREATE TABLE pspadm.temp_raffi_sql_log(
    log_message CHARACTER VARYING(200),
    create_date TIMESTAMP(0) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.temp_subscriptionnumber(
    subscriptionnumber CHARACTER VARYING(10)
);

CREATE TABLE pspadm.temp_tax_b(
    company_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255) NOT NULL,
    amount NUMERIC(19,4),
    description CHARACTER VARYING(4000),
    qtr CHARACTER VARYING(1),
    yr CHARACTER VARYING(4)
);

CREATE TABLE pspadm.temp_tax_c(
    company_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255),
    ft_amt DOUBLE PRECISION,
    description CHARACTER VARYING(4000),
    qtr CHARACTER VARYING(1),
    yr CHARACTER VARYING(4)
);

CREATE TABLE pspadm.temp_tax_calc_apr12(
    fed_tax_id CHARACTER VARYING(80),
    source_company_id CHARACTER VARYING(50),
    law_fk CHARACTER VARYING(50) NOT NULL,
    description CHARACTER VARYING(4000),
    tax_liability_amt DOUBLE PRECISION,
    adj_amt DOUBLE PRECISION,
    ft_amt DOUBLE PRECISION,
    diff DOUBLE PRECISION,
    qtr CHARACTER VARYING(1),
    yr CHARACTER VARYING(4)
);

CREATE TABLE pspadm.temp_tax_d(
    company_fk CHARACTER VARYING(255) NOT NULL,
    tor_amt DOUBLE PRECISION,
    qtr CHARACTER VARYING(1),
    law_fk CHARACTER VARYING(255),
    yr CHARACTER VARYING(4)
);

CREATE TABLE pspadm.temp_tax_e(
    company_fk CHARACTER VARYING(255) NOT NULL,
    yr CHARACTER VARYING(5),
    law_fk CHARACTER VARYING(50) NOT NULL,
    description CHARACTER VARYING(4000),
    yq DOUBLE PRECISION
);

CREATE TABLE pspadm.test2(
    id DOUBLE PRECISION
);

CREATE TABLE pspadm.test_mmt_atf(
    money_movement_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
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
    efe_pymt_batch_fk CHARACTER VARYING(255),
    payment_frequency_fk CHARACTER VARYING(255),
    auth_user_fk CHARACTER VARYING(255),
    original_transaction_fk CHARACTER VARYING(255),
    payment_period_begin TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_period_end TIMESTAMP(6) WITHOUT TIME ZONE,
    agency_taxpayer_id CHARACTER VARYING(80),
    payment_template_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.tmp_auth_user(
    auth_user_seq CHARACTER VARYING(255) NOT NULL,
    corp_id CHARACTER VARYING(50),
    first_name CHARACTER VARYING(80),
    last_name CHARACTER VARYING(80),
    auth_role_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.tmp_auth_user1(
    auth_user_seq CHARACTER VARYING(255) NOT NULL,
    corp_id CHARACTER VARYING(50),
    first_name CHARACTER VARYING(80),
    last_name CHARACTER VARYING(80),
    auth_role_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.tmp_pewp_feb27(
    employee_wage_plan_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(255),
    state CHARACTER VARYING(15),
    wage_plan_value CHARACTER VARYING(15),
    wage_plan_domain CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    description CHARACTER VARYING(100),
    rules_version CHARACTER VARYING(10),
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE
);

CREATE TABLE pspadm.tmp_pft_feb26(
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.tmp_pmmt_feb25(
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    financial_transaction_amount NUMERIC(19,4),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    settlement_type_cd CHARACTER VARYING(255),
    credit_bank_account_type CHARACTER VARYING(255),
    debit_bank_account_type CHARACTER VARYING(255),
    on_hold NUMERIC(1,0),
    sku CHARACTER VARYING(40),
    sku_quantity NUMERIC(10,0),
    billing_detail_fk CHARACTER VARYING(255),
    credit_bank_account_fk CHARACTER VARYING(255),
    debit_bank_account_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    paycheck_split_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
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
);

CREATE TABLE pspadm.tmp_psp_edr(
    entry_detail_record_seq CHARACTER VARYING(255),
    modifier_id CHARACTER VARYING(255),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    money_movement_transaction_fk CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.tmp_psp_mmt(
    money_movement_transaction_seq CHARACTER VARYING(255),
    modifier_id CHARACTER VARYING(255),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offload_batch_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255),
    tax_payment_status CHARACTER VARYING(255),
    mm_transaction_amount NUMERIC(19,4)
);

CREATE TABLE pspadm.tmp_pttmd_feb27(
    tax_table_misc_data_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    value CHARACTER VARYING(56),
    employee_tax_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    misc_data_order NUMERIC(10,0)
);

CREATE TABLE pspadm.toad_plan_sql(
    username CHARACTER VARYING(30),
    statement_id CHARACTER VARYING(32),
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    statement CHARACTER VARYING(2000)
);

CREATE TABLE pspadm.z_redef_paycheck(
    paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    source_paycheck_id CHARACTER VARYING(50),
    pay_period_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    voided_after_offload NUMERIC(1,0),
    pay_period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.z_redef_paycheck_split(
    paycheck_split_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    paycheck_split_amount NUMERIC(19,4),
    source_dd_txn_id CHARACTER VARYING(50),
    employee_bank_account_fk CHARACTER VARYING(255) NOT NULL,
    paycheck_fk CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE pspadm.z_redef_src_sys_transmission(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document TEXT,
    response_document TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_fk CHARACTER VARYING(255)
);

CREATE TABLE pspadm.z_temp_diy_reset_company(
    source_company_id CHARACTER VARYING(50) NOT NULL,
    updated_yn_ind CHARACTER VARYING(10) DEFAULT 'N',
    modified_dttm TIMESTAMP(0) WITHOUT TIME ZONE
);
COMMENT ON TABLE pspadm.z_temp_diy_reset_company
     IS 'This table contains a list of DIY companies that will have their price codes reset to standard.  This is to address a bug in the adapters.  To run only on Mar 5 2009';

SELECT CURRENT_TIMESTAMP;

