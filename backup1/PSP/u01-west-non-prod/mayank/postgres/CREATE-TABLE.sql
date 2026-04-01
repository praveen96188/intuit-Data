-- ------------ Write CREATE-TABLE-stage scripts -----------

CREATE TABLE pspadm.a(
    a DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.alert_tempfts(
    source_company_id CHARACTER VARYING(50),
    financial_transaction_amount NUMERIC(19,4),
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.at_widget(
    id CHARACTER VARYING(255) NOT NULL,
    display_text CHARACTER VARYING(255) NOT NULL,
    widget_configuration TEXT,
    acl CHARACTER VARYING(255),
    max_open_widgets NUMERIC(38,0),
    display_order NUMERIC(38,0),
    parent_widget_id CHARACTER VARYING(255),
    modifier_id CHARACTER VARYING(255),
    date_updated TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.cfr_backup(
    consumer_realm_id CHARACTER VARYING(50),
    employee_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.cfr_backup_temp(
    consumer_realm_id CHARACTER VARYING(50),
    employee_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.cfr_backup_temp1(
    consumer_realm_id CHARACTER VARYING(50),
    employee_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.dbexpert_plan1(
    statement_id CHARACTER VARYING(30),
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    remarks CHARACTER VARYING(80),
    operation CHARACTER VARYING(30),
    options CHARACTER VARYING(255),
    object_node CHARACTER VARYING(128),
    object_owner CHARACTER VARYING(30),
    object_name CHARACTER VARYING(30),
    object_instance DOUBLE PRECISION,
    object_type CHARACTER VARYING(30),
    optimizer CHARACTER VARYING(255),
    search_columns DOUBLE PRECISION,
    id DOUBLE PRECISION,
    parent_id DOUBLE PRECISION,
    position DOUBLE PRECISION,
    cost DOUBLE PRECISION,
    cardinality DOUBLE PRECISION,
    bytes DOUBLE PRECISION,
    other_tag CHARACTER VARYING(255),
    partition_start CHARACTER VARYING(255),
    partition_stop CHARACTER VARYING(255),
    partition_id DOUBLE PRECISION,
    other TEXT,
    distribution CHARACTER VARYING(30),
    cpu_cost NUMERIC(38,0),
    io_cost NUMERIC(38,0),
    temp_space NUMERIC(38,0),
    access_predicates CHARACTER VARYING(4000),
    filter_predicates CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.dms_test(
    batch_job_audit_log_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    engine_instance_name CHARACTER VARYING(500),
    job_action CHARACTER VARYING(500),
    job_namespace CHARACTER VARYING(500),
    message CHARACTER VARYING(4000),
    message_detail CHARACTER VARYING(4000),
    is_verified INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.gg_heartbeat(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.gg_heartbeat_ib(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.gg_heartbeat_smc(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.gg_heartbeat_sst(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.mysales(
    id DOUBLE PRECISION,
    flag DOUBLE PRECISION,
    product CHARACTER VARYING(20),
    channel_id DOUBLE PRECISION,
    cust_id DOUBLE PRECISION,
    amount_sold DOUBLE PRECISION,
    order_date TIMESTAMP(0) WITHOUT TIME ZONE,
    ship_date TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.payroll_hist(
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    end_time TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_submit_cnt DOUBLE PRECISION,
    tot_err_cnt DOUBLE PRECISION,
    distinct_err_type_cnt DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_accounting_report_file(
    accounting_report_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    type CHARACTER VARYING(255),
    transmission_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ach_transaction_code(
    transaction_code CHARACTER VARYING(2) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    ach_account_type_cd CHARACTER VARYING(255),
    credit_debit_indicator CHARACTER VARYING(255),
    description CHARACTER VARYING(150),
    is_return_code INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_achenrollment(
    achenrollment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    status_reason CHARACTER VARYING(1000),
    status CHARACTER VARYING(255),
    confirmation_number CHARACTER VARYING(4000),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_achenrollment_det_test(
    achenrollment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    agency_id CHARACTER VARYING(4000),
    legal_name CHARACTER VARYING(4000),
    a_c_h_enrollment_fk CHARACTER VARYING(255),
    response_file_fk CHARACTER VARYING(255),
    request_file_fk CHARACTER VARYING(255),
    agency_id_enc CHARACTER VARYING(4000),
    fein_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_achenrollment_detail(
    achenrollment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    agency_id_pt_bk CHARACTER VARYING(4000),
    f_e_i_n_pt_bk CHARACTER VARYING(4000),
    legal_name CHARACTER VARYING(4000),
    a_c_h_enrollment_fk CHARACTER VARYING(255),
    response_file_fk CHARACTER VARYING(255),
    request_file_fk CHARACTER VARYING(255),
    agency_id_enc CHARACTER VARYING(4000),
    fein_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_achenrollment_file(
    achenrollment_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    type CHARACTER VARYING(255),
    file_content TEXT,
    file_content_enc TEXT
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_action_event(
    code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(100),
    type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_additional_filing_amount(
    name CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    a_t_f_law_id CHARACTER VARYING(4000),
    description CHARACTER VARYING(4000),
    rate INTEGER,
    payment_template_fk CHARACTER VARYING(255),
    is_system_applied_credit INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_address(
    address_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    address_line3 CHARACTER VARYING(80),
    address_line2 CHARACTER VARYING(80),
    address_line1 CHARACTER VARYING(80),
    city CHARACTER VARYING(256),
    country CHARACTER VARYING(256),
    state CHARACTER VARYING(21),
    zip_code CHARACTER VARYING(13),
    zip_code_extension CHARACTER VARYING(10)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ade_law_map(
    ade_law_map_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    ade_name CHARACTER VARYING(50),
    law_fk CHARACTER VARYING(255),
    ade_law_map_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_agency(
    agency_id CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(300),
    default_r_a_a_form CHARACTER VARYING(255),
    a_c_h_enrollment_required INTEGER,
    r_a_a_enrollment_required INTEGER,
    r_a_f_enrollment_required INTEGER,
    agency_supported INTEGER,
    agency_abbrev CHARACTER VARYING(4000),
    rfnds_intuit_for_returned_pmt INTEGER,
    no_calculation INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_agency_check_batch(
    agency_check_batch_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    super_check INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_agency_id_requirement(
    agency_id_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    pattern CHARACTER VARYING(4000),
    example CHARACTER VARYING(50),
    required INTEGER,
    payment_template_agency_id_fk CHARACTER VARYING(255),
    custom_requirement CHARACTER VARYING(255),
    prohibit_default_ids INTEGER NOT NULL DEFAULT 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_agency_rate_request(
    agency_rate_request_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    record_count INTEGER,
    status CHARACTER VARYING(255),
    year_quarter CHARACTER VARYING(4000),
    agency_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_annual_billing_batch(
    annual_billing_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    form_type_cd CHARACTER VARYING(255),
    form_year INTEGER,
    annual_billing_batch_status_cd CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_annual_billing_item(
    annual_billing_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    form_count INTEGER,
    error_message CHARACTER VARYING(4000),
    annual_billing_item_status_cd CHARACTER VARYING(255),
    annual_billing_batch_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_applied_database_patch(
    applied_database_patch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    database_patch_version CHARACTER VARYING(15),
    database_patch_type_cd CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_assisted_bundle_bill(
    assisted_bundle_bill_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    bill_date TIMESTAMP(6) WITHOUT TIME ZONE,
    total_count INTEGER,
    total_amount NUMERIC(19,4),
    asst_status CHARACTER VARYING(255),
    asst_bundle_comp_usage_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_asst_bundle_bill_detail(
    asst_bundle_bill_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    billing_detail_id CHARACTER VARYING(4000),
    assisted_bundle_bill_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_asst_bundle_comp_usage(
    asst_bundle_comp_usage_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    source_system_cd CHARACTER VARYING(255),
    entitlement_id CHARACTER VARYING(20),
    license_id CHARACTER VARYING(20)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_atfdata_extract_batch(
    atfdata_extract_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    run_type CHARACTER VARYING(255),
    batch_id NUMERIC(19,0),
    start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    batch_status CHARACTER VARYING(255),
    year INTEGER,
    quarter INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_atfdata_extract_file(
    atfdata_extract_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    file_type CHARACTER VARYING(255),
    file_status CHARACTER VARYING(255),
    file_name CHARACTER VARYING(150),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    a_t_f_data_extract_batch_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_atfpayments_to_process(
    atfpayments_to_process_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    payment_date TIMESTAMP(6) WITHOUT TIME ZONE,
    quarter_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    money_movement_transaction_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_atfpayrolls_to_process(
    atfpayrolls_to_process_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payroll_run_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_auth_domain(
    domain_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256),
    name CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_auth_operation(
    operation_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256),
    name CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_auth_role(
    auth_role_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    role_id CHARACTER VARYING(40),
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    auth_domain_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_auth_user(
    auth_user_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    corp_id CHARACTER VARYING(50),
    first_name CHARACTER VARYING(80),
    last_name CHARACTER VARYING(80),
    authorization_token CHARACTER VARYING(50),
    last_remote_call_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    nbr_of_failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_auth_user_auth_role__assoc(
    auth_user_fk CHARACTER VARYING(255) NOT NULL,
    auth_role_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_authrole_operation_assoc(
    auth_role_fk CHARACTER VARYING(255) NOT NULL,
    auth_operation_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_bank_account(
    bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    account_number_pt_bk CHARACTER VARYING(80),
    account_type_cd CHARACTER VARYING(255),
    bank_name CHARACTER VARYING(256),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    routing_number CHARACTER VARYING(20),
    a_c_h_entry_class CHARACTER VARYING(255),
    a_c_h_account_type_cd CHARACTER VARYING(255),
    account_number_enc CHARACTER VARYING(4000),
    session_id CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_bank_holiday(
    bank_holiday_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    bank_holiday_name CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_batch_job_audit_log(
    batch_job_audit_log_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    engine_instance_name CHARACTER VARYING(500),
    job_action CHARACTER VARYING(500),
    job_namespace CHARACTER VARYING(500),
    message CHARACTER VARYING(4000),
    message_detail CHARACTER VARYING(4000),
    is_verified INTEGER
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_batch_job_parameter(
    id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    job_step CHARACTER VARYING(300),
    param_name CHARACTER VARYING(300),
    param_value CHARACTER VARYING(300),
    batch_job_setup_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_batch_job_setup(
    job_type CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    dly_bw_retries_timer_expr CHARACTER VARYING(100),
    is_automatically_scheduled INTEGER,
    job_processor_class_name CHARACTER VARYING(100),
    max_retries INTEGER,
    job_timer_expression CHARACTER VARYING(100),
    job_namespace CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_batch_job_status(
    batch_job_status_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    job_type CHARACTER VARYING(255),
    is_running INTEGER DEFAULT 0,
    last_started_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    last_ended_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_bill(
    bill_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    bill_date TIMESTAMP(6) WITHOUT TIME ZONE,
    usage_count INTEGER,
    synched_count INTEGER,
    company_usage_fk CHARACTER VARYING(255),
    closed INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_bill_payment(
    bill_payment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    source_id CHARACTER VARYING(50),
    payee_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255),
    memo CHARACTER VARYING(4000),
    transaction_type CHARACTER VARYING(255),
    session_id CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_bill_payment_split(
    bill_payment_split_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    source_id CHARACTER VARYING(50),
    bill_payment_fk CHARACTER VARYING(255),
    payee_bank_account_fk CHARACTER VARYING(255),
    reference_number CHARACTER VARYING(50)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_billing_detail(
    billing_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    service_date TIMESTAMP(6) WITHOUT TIME ZONE,
    service_cd CHARACTER VARYING(255),
    item_name CHARACTER VARYING(100),
    item_sku CHARACTER VARYING(40),
    quantity INTEGER,
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
    tax_exception_ind INTEGER,
    payroll_run_fk CHARACTER VARYING(255) NOT NULL,
    billing_period TIMESTAMP(6) WITHOUT TIME ZONE,
    memo CHARACTER VARYING(100),
    base_price NUMERIC(19,4),
    offering_svcchg_price_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_bpcompany_service_info(
    bpcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    override_company_limit_amount NUMERIC(19,4),
    cons_limit_violation_cnt NUMERIC(19,0),
    override_payee_limit_amount NUMERIC(19,4)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_cdcompany_service_info(
    cdcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    last_paycheck_id NUMERIC(19,0)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_check_print_batch(
    check_print_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    number_of_checks NUMERIC(19,0),
    sent_to_printer TIMESTAMP(6) WITHOUT TIME ZONE,
    check_print_batch_status_code CHARACTER VARYING(255),
    check_print_batch_message CHARACTER VARYING(250),
    recon_plus_file_fk CHARACTER VARYING(255),
    positive_pay_file_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_check_print_paycheck(
    check_print_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_paycheck_id CHARACTER VARYING(50),
    check_number CHARACTER VARYING(10),
    cp_paycheck_status_code CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) NOT NULL,
    employee_print_name CHARACTER VARYING(50),
    company_paycheck_batch_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_check_print_signature(
    check_print_signature_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    signature TEXT,
    company_fk CHARACTER VARYING(255),
    sourcesys_printedchk_info_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_collection_stage(
    collection_stage_code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_comp_adjust_submission(
    comp_adjust_submission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    submission_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    source_id CHARACTER VARYING(9),
    amount NUMERIC(19,4),
    void_submission_fk CHARACTER VARYING(255),
    original_submission_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_comp_pmt_template_agencyid(
    comp_pmt_template_agencyid_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(50),
    agency_taxpayer_id_pt_bk CHARACTER VARYING(50),
    company_agency_pmt_template_fk CHARACTER VARYING(255) NOT NULL,
    agency_taxpayer_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_comp_pmttemplate_pmtmethod(
    comp_pmttemplate_pmtmethod_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    agent_enabled INTEGER,
    enabled INTEGER,
    payment_method CHARACTER VARYING(255),
    company_agency_pmt_template_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company(
    company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    phone CHARACTER VARYING(100),
    dba_name CHARACTER VARYING(100),
    fed_tax_id_pt_bk CHARACTER VARYING(80),
    legal_name CHARACTER VARYING(100),
    source_company_id CHARACTER VARYING(50),
    notification_email CHARACTER VARYING(100),
    next_payroll_transaction_id CHARACTER VARYING(50),
    next_paycheck_id CHARACTER VARYING(50),
    next_employee_id CHARACTER VARYING(50),
    next_payroll_item_id CHARACTER VARYING(50),
    account_locked_until TIMESTAMP(6) WITHOUT TIME ZONE,
    nbr_of_failed_login_attempts INTEGER,
    current_token NUMERIC(19,0),
    source_system_cd CHARACTER VARYING(255),
    tax_exempt_expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_flagged_for_fraud INTEGER,
    sign_up_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_frequency_fk CHARACTER VARYING(255),
    mailing_address_fk CHARACTER VARYING(255),
    legal_address_fk CHARACTER VARYING(255),
    funding_model_fk CHARACTER VARYING(255) NOT NULL,
    private_key CHARACTER VARYING(1000),
    public_key CHARACTER VARYING(1000),
    nbr_failed_authentications INTEGER,
    debug_logging INTEGER,
    p_s_id CHARACTER VARYING(50),
    offload_group_fk CHARACTER VARYING(255) NOT NULL,
    cloud_current_token NUMERIC(19,0),
    i_a_m_authentication_id CHARACTER VARYING(50),
    i_a_m_realm_id CHARACTER VARYING(50),
    price_type CHARACTER VARYING(50),
    tax_exempt_status CHARACTER VARYING(255),
    annual_billing_batch_fk CHARACTER VARYING(255),
    name_control CHARACTER VARYING(10),
    fed_tax_id_enc CHARACTER VARYING(4000),
    d_d_publish_flag INTEGER NOT NULL DEFAULT 0,
    private_key_enc CHARACTER VARYING(4000),
    fed_tax_id CHARACTER VARYING(80) DEFAULT NULL,
    o_i_i_flag CHARACTER VARYING(20) DEFAULT '0000000000000000',
    compliance_address_fk CHARACTER VARYING(255),
    is_dg_disassociated INTEGER NOT NULL DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_additional_info(
    company_additional_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    company_fk CHARACTER VARYING(255) NOT NULL,
    industry_type_fk CHARACTER VARYING(255),
    ownership_type_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_agency(
    company_agency_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    resp_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    resp_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    first_filings_quarter CHARACTER VARYING(5),
    last_filings_quarter CHARACTER VARYING(5),
    is_final_return INTEGER,
    generate_annual_form INTEGER,
    final_payroll_date TIMESTAMP(6) WITHOUT TIME ZONE,
    agency_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    er_fica_deferral_enabled INTEGER DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_aud(
    company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    private_key_enc CHARACTER VARYING(4000),
    phone CHARACTER VARYING(100),
    dba_name CHARACTER VARYING(100),
    legal_name CHARACTER VARYING(100),
    source_company_id CHARACTER VARYING(50),
    debug_logging INTEGER,
    notification_email CHARACTER VARYING(100),
    next_payroll_transaction_id CHARACTER VARYING(50),
    next_paycheck_id CHARACTER VARYING(50),
    next_employee_id CHARACTER VARYING(50),
    next_payroll_item_id CHARACTER VARYING(50),
    account_locked_until TIMESTAMP(6) WITHOUT TIME ZONE,
    nbr_of_failed_login_attempts INTEGER,
    current_token NUMERIC(19,0),
    source_system_cd CHARACTER VARYING(255),
    tax_exempt_expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    name_control CHARACTER VARYING(10),
    is_flagged_for_fraud INTEGER,
    sign_up_date TIMESTAMP(6) WITHOUT TIME ZONE,
    public_key CHARACTER VARYING(1000),
    nbr_failed_authentications INTEGER,
    cloud_current_token NUMERIC(19,0),
    i_a_m_realm_id CHARACTER VARYING(50),
    tax_exempt_status CHARACTER VARYING(255),
    price_type CHARACTER VARYING(50),
    fed_tax_id_enc CHARACTER VARYING(4000),
    d_d_publish_flag INTEGER,
    payroll_frequency_fk CHARACTER VARYING(255),
    mailing_address_fk CHARACTER VARYING(255),
    legal_address_fk CHARACTER VARYING(255),
    offload_group_fk CHARACTER VARYING(255) NOT NULL,
    funding_model_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_bank_account(
    company_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    session_id CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_consent(
    company_consent_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    fein_pt_bk CHARACTER VARYING(4000),
    signup_date TIMESTAMP(6) WITHOUT TIME ZONE,
    signed INTEGER,
    app_id CHARACTER VARYING(4000),
    app_name CHARACTER VARYING(4000),
    fein_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_daily_liability(
    company_daily_liability_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    liability_date TIMESTAMP(6) WITHOUT TIME ZONE,
    taxable_wages NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    total_tips_amount NUMERIC(19,4),
    tax_amount NUMERIC(19,4),
    law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_event(
    company_event_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    event_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_cd CHARACTER VARYING(255),
    event_type_cd CHARACTER VARYING(255),
    event_token NUMERIC(19,0),
    source_id CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255) NOT NULL,
    note_last_updated_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_event_detail(
    company_event_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    value CHARACTER VARYING(4000),
    event_detail_type_cd CHARACTER VARYING(255),
    company_event_fk CHARACTER VARYING(255) NOT NULL,
    event_detail_subtype CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_event_email(
    company_event_email_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    retry_count INTEGER,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    email_template_type_cd CHARACTER VARYING(255),
    company_event_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_event_email_bk(
    company_event_email_seq CHARACTER VARYING(255),
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0),
    retry_count INTEGER,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    email_template_type_cd CHARACTER VARYING(255),
    company_event_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_event_email_bk1(
    company_event_email_seq CHARACTER VARYING(255),
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0),
    retry_count INTEGER,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    email_template_type_cd CHARACTER VARYING(255),
    company_event_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_event_email_param(
    company_event_email_param_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    param_type_cd CHARACTER VARYING(255),
    value CHARACTER VARYING(4000),
    company_event_email_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_filing_amount(
    company_filing_amount_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,7),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE,
    name CHARACTER VARYING(4000),
    company_agency_pmt_template_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_law(
    company_law_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    law_fk CHARACTER VARYING(255) NOT NULL,
    exemption_status CHARACTER VARYING(255),
    tax_form_line CHARACTER VARYING(255),
    source_description CHARACTER VARYING(50),
    source_id CHARACTER VARYING(5),
    qbdt_payroll_item_info_fk CHARACTER VARYING(255),
    is_archived INTEGER DEFAULT 0,
    filing_status CHARACTER VARYING(255),
    reimbursable_status CHARACTER VARYING(255),
    additional_company_law_fk CHARACTER VARYING(255),
    w2_code INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_law_rate(
    company_law_rate_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    rate NUMERIC(19,7),
    company_law_fk CHARACTER VARYING(255) NOT NULL,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE,
    rate_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_note(
    company_note_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    notes CHARACTER VARYING(4000),
    insert_user_id CHARACTER VARYING(30),
    company_fk CHARACTER VARYING(255) NOT NULL,
    company_event_fk CHARACTER VARYING(255) NOT NULL,
    alert INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_offer(
    company_offer_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    usages_remaining INTEGER,
    offer_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_offering(
    company_offering_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    offering_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_paycheck_batch(
    company_paycheck_batch_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_payroll_item(
    company_payroll_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_payroll_item_id CHARACTER VARYING(4000),
    company_fk CHARACTER VARYING(255),
    payroll_item_fk CHARACTER VARYING(255),
    source_description CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    tax_form_line CHARACTER VARYING(255),
    qbdt_payroll_item_info_fk CHARACTER VARYING(255),
    is_archived INTEGER DEFAULT 0,
    additional_payroll_item_fk CHARACTER VARYING(255),
    w2_code INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_pin(
    company_pin_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    p_i_n_value CHARACTER VARYING(256),
    company_fk CHARACTER VARYING(255) NOT NULL,
    hash_type CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_rate_request(
    company_rate_request_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_message CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    agency_rate_request_fk CHARACTER VARYING(255),
    company_agency_fk CHARACTER VARYING(255),
    old_rate NUMERIC(19,7),
    new_rate NUMERIC(19,7)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_service(
    company_service_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    service_fk CHARACTER VARYING(255) NOT NULL,
    service_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    funding_model_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_service_bank_acct(
    company_service_bank_acct_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    company_service_fk CHARACTER VARYING(255) NOT NULL,
    company_bank_account_fk CHARACTER VARYING(255) NOT NULL,
    payroll_run_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_tfssubmission(
    company_tfssubmission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    submission_status CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255),
    year INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_company_usage(
    company_usage_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    source_system_cd CHARACTER VARYING(255),
    entitlement_id CHARACTER VARYING(20),
    license_id CHARACTER VARYING(20),
    billing_day_of_month INTEGER,
    start_day_of_usage_month INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_companyagency_frmtemplate(
    companyagency_frmtemplate_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255),
    form_template_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_companyagency_pmttemplate(
    companyagency_pmttemplate_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    agency_taxpayer_id_pt_bk CHARACTER VARYING(80),
    agency_taxpayer_id_enc CHARACTER VARYING(4000),
    agency_taxpayer_id CHARACTER VARYING(80)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_compensation(
    compensation_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    compensation_amount NUMERIC(19,4),
    hours_worked NUMERIC(19,7),
    paycheck_fk CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255),
    compensation_y_t_d_amount NUMERIC(19,4),
    pay_stub_order NUMERIC(19,0),
    qbdt_payline_info_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_contact(
    contact_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    auth_signer_yn_ind INTEGER,
    contact_role_cd CHARACTER VARYING(255),
    source_contact_id CHARACTER VARYING(300),
    title CHARACTER VARYING(20),
    title_suffix CHARACTER VARYING(20),
    job_title CHARACTER VARYING(80),
    fax CHARACTER VARYING(20),
    second_phone CHARACTER VARYING(20),
    company_fk CHARACTER VARYING(255) NOT NULL,
    i_a_m_authentication_id CHARACTER VARYING(50),
    social_security_number_pt_bk CHARACTER VARYING(4000),
    date_of_birth_pt_bk TIMESTAMP(6) WITHOUT TIME ZONE,
    date_of_birth_enc CHARACTER VARYING(4000),
    social_security_number_enc CHARACTER VARYING(4000),
    date_of_birth TIMESTAMP(6) WITHOUT TIME ZONE,
    social_security_number CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_contact_temp(
    contact_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    auth_signer_yn_ind INTEGER,
    contact_role_cd CHARACTER VARYING(255),
    source_contact_id CHARACTER VARYING(300),
    title CHARACTER VARYING(20),
    title_suffix CHARACTER VARYING(20),
    job_title CHARACTER VARYING(80),
    fax CHARACTER VARYING(20),
    second_phone CHARACTER VARYING(20),
    company_fk CHARACTER VARYING(255) NOT NULL,
    i_a_m_authentication_id CHARACTER VARYING(50),
    social_security_number CHARACTER VARYING(4000),
    date_of_birth TIMESTAMP(6) WITHOUT TIME ZONE,
    date_of_birth_enc CHARACTER VARYING(4000),
    social_security_number_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ddcompany_service_info(
    ddcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    average_pay_run_amount NUMERIC(19,4),
    cons_limit_violation_cnt NUMERIC(19,0),
    high_annual_pay_amount NUMERIC(19,4),
    override_company_limit_amount NUMERIC(19,4),
    override_employee_limit_amount NUMERIC(19,4),
    offload_group_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_deduction(
    deduction_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deduction_amount NUMERIC(19,4),
    paycheck_fk CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255),
    deduction_y_t_d_amount NUMERIC(19,4),
    pay_stub_order NUMERIC(19,0),
    qbdt_payline_info_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_deleted_record(
    deleted_record_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    record_identifier CHARACTER VARYING(4000),
    table_name CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_dep_freq_ledger_operation(
    dep_freq_ledger_operation_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    deposit_frequency CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_deposit_frequency(
    deposit_frequency_code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    a_t_f_deposit_freq_code CHARACTER VARYING(50),
    description CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_deposit_frequency_file(
    deposit_frequency_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(150),
    status CHARACTER VARYING(255),
    is_archived INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_deposit_frequency_file_rec(
    deposit_frequency_file_rec_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    company_name CHARACTER VARYING(35),
    current_year CHARACTER VARYING(4),
    deposit_frequency CHARACTER VARYING(1),
    e_i_n_pt_bk CHARACTER VARYING(9),
    form_filed CHARACTER VARYING(3),
    last_period_base_code CHARACTER VARYING(1),
    error_message CHARACTER VARYING(4000),
    deposit_frequency_file_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    ein_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_deposit_frequency_req(
    deposit_frequency_req_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    prohibited_deposit_frequency CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_dicrfile(
    dicrfile_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    credit_txn_total_amount NUMERIC(19,4),
    debit_txn_total_amount NUMERIC(19,4),
    file_name CHARACTER VARYING(1000),
    status CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_disburse_advice(
    disburse_advice_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    tax_liability_amount NUMERIC(19,4),
    tax_quarter INTEGER,
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_disburse_advice_tax_liab(
    disburse_advice_tax_liab_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_edi_payment_detail(
    edi_payment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    transaction_set_id INTEGER,
    status_cd CHARACTER VARYING(255),
    transaction_id CHARACTER VARYING(4000),
    error_cd CHARACTER VARYING(4000),
    error_message CHARACTER VARYING(4000),
    fed_tax_id_pt_bk CHARACTER VARYING(4000),
    group_id INTEGER,
    group_transaction_time CHARACTER VARYING(4000),
    payment_amount NUMERIC(19,4),
    parent_file_fk CHARACTER VARYING(255) NOT NULL,
    response_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_edi_tax_file(
    edi_tax_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    file_code INTEGER,
    file_id INTEGER,
    file_name CHARACTER VARYING(4000),
    file_type CHARACTER VARYING(255),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    submit_date TIMESTAMP(6) WITHOUT TIME ZONE,
    system_owner CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ee_count_report(
    emp_count NUMERIC(5,0),
    co_count NUMERIC(15,0),
    service CHARACTER VARYING(255),
    created_date TIMESTAMP(0) WITHOUT TIME ZONE DEFAULT (aws_oracle_ext.SYSDATE())
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ee_payrollitem_qtrtotals(
    ee_payrollitem_qtrtotals_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    quarter INTEGER,
    year INTEGER,
    taxable_wages NUMERIC(19,4),
    amount NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    tips_taxable_wages_amount NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_effective_deposit_freq(
    effective_deposit_freq_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_pmt_template_fk CHARACTER VARYING(255) NOT NULL,
    payment_template_frequency_fk CHARACTER VARYING(255) NOT NULL,
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_eftps_enrollment(
    eftps_enrollment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255) NOT NULL,
    eftps_enrollment_id CHARACTER VARYING(100),
    secondary INTEGER DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_eftps_enrollment_detail(
    eftps_enrollment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    group_id INTEGER,
    transaction_set_id INTEGER,
    transaction_id INTEGER,
    fed_tax_id_pt_bk CHARACTER VARYING(80),
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
    fed_tax_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_eftps_file(
    eftps_file_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    ack_file_fk CHARACTER VARYING(255),
    file_subtype CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_eftps_payment_detail(
    eftps_payment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    transaction_set_id INTEGER,
    transaction_id INTEGER,
    return_cd CHARACTER VARYING(255),
    payment_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_type_code CHARACTER VARYING(4000),
    eft_transaction_id CHARACTER VARYING(4000),
    agency_payment_id CHARACTER VARYING(4000),
    fed_tax_id_pt_bk CHARACTER VARYING(4000),
    payment_due_date TIMESTAMP(6) WITHOUT TIME ZONE,
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_amount NUMERIC(19,4),
    payment_details CHARACTER VARYING(4000),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    group_id INTEGER,
    reject_cd CHARACTER VARYING(4000),
    reason CHARACTER VARYING(4000),
    response_date TIMESTAMP(6) WITHOUT TIME ZONE,
    parent_file_fk CHARACTER VARYING(255) NOT NULL,
    return_file_fk CHARACTER VARYING(255),
    response_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    same_day_ack_number CHARACTER VARYING(4000),
    payment_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_tax_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_emp_totals_payroll_run(
    emp_totals_payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    quarter_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee(
    employee_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_employee_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_id_pt_bk CHARACTER VARYING(20),
    company_fk CHARACTER VARYING(255),
    hire_date TIMESTAMP(6) WITHOUT TIME ZONE,
    re_hire_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_filing_status CHARACTER VARYING(50),
    work_state CHARACTER VARYING(21),
    termination_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_allowances INTEGER,
    is_statutory INTEGER,
    has_retirement_plan INTEGER,
    has_third_party_sick_pay INTEGER,
    birth_date_pt_bk TIMESTAMP(6) WITHOUT TIME ZONE,
    tp_401k_info_is_hce INTEGER,
    tp_401k_info_owner_percent NUMERIC(19,7),
    tp_401k_info_is_family_member INTEGER,
    tp_401k_info_last_upload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_cloud_employee INTEGER,
    is_deceased INTEGER,
    qualifies_for_aeic INTEGER,
    fed_extra_withholding NUMERIC(19,4),
    live_state CHARACTER VARYING(21),
    pay_period CHARACTER VARYING(255),
    is_archived INTEGER DEFAULT 0,
    consumer_realm_id CHARACTER VARYING(50),
    is_viewing_paystub_disabled INTEGER DEFAULT 0,
    birth_date_enc CHARACTER VARYING(4000),
    tax_id_enc CHARACTER VARYING(4000),
    rec_num CHARACTER VARYING(255),
    persona_id CHARACTER VARYING(4000),
    fed_claim_dependents NUMERIC(19,4),
    fed_other_income NUMERIC(19,4),
    fed_deductions NUMERIC(19,4),
    fed_w4_employee_pref CHARACTER VARYING(4000),
    fed_multiple_jobs INTEGER NOT NULL DEFAULT 0,
    tax_id CHARACTER VARYING(80) DEFAULT NULL,
    birth_date CHARACTER VARYING(80) DEFAULT NULL,
    is_dg_disassociated INTEGER NOT NULL DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_accrual(
    employee_accrual_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    accrual_period CHARACTER VARYING(255),
    hours_per_period NUMERIC(19,7),
    hours NUMERIC(19,7),
    max_hours NUMERIC(19,7),
    new_year_reset INTEGER,
    accrual_type CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_bank_account(
    employee_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    bank_account_fk CHARACTER VARYING(255) NOT NULL,
    employee_fk CHARACTER VARYING(255) NOT NULL,
    amount NUMERIC(19,7),
    amount_type CHARACTER VARYING(255),
    account_order INTEGER,
    session_id CHARACTER VARYING(4000),
    source_bank_account_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_custom_field(
    employee_custom_field_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(128),
    value CHARACTER VARYING(128),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    field_order INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_law_qtr_totals(
    employee_law_qtr_totals_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    hours_worked NUMERIC(19,7),
    month_one_worked_indicator INTEGER,
    month_two_worked_indicator INTEGER,
    month_three_worked_indicator INTEGER,
    quarter INTEGER,
    taxable_wages NUMERIC(19,4),
    tax_amount NUMERIC(19,4),
    tips_taxable_wages_amount NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    weeks_worked INTEGER,
    year INTEGER,
    law_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    hourly_rate NUMERIC(19,7),
    company_law_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_payroll_item(
    employee_payroll_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,7),
    "LIMIT" NUMERIC(19,7),
    type CHARACTER VARYING(255),
    amount_type CHARACTER VARYING(255),
    limit_type CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255) NOT NULL,
    employee_fk CHARACTER VARYING(255) NOT NULL,
    item_order INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_tax(
    employee_tax_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    tax_law_version CHARACTER VARYING(20),
    w2_name CHARACTER VARYING(41),
    state CHARACTER VARYING(2),
    subject_to INTEGER,
    tax_type CHARACTER VARYING(255),
    filing_status CHARACTER VARYING(50),
    allowances INTEGER,
    company_law_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    tax_order INTEGER,
    extra_withholding NUMERIC(19,7),
    extra_withholding_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_to_process_totals(
    employee_to_process_totals_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    employee_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_usage(
    employee_usage_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    employee_name CHARACTER VARYING(240),
    employee_record_number CHARACTER VARYING(50),
    source_employee_id CHARACTER VARYING(50),
    usage_count INTEGER,
    usage_period_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_w2_totals(
    employee_w2_totals_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    year INTEGER,
    taxable_wages NUMERIC(19,4),
    amount NUMERIC(19,4),
    tips_taxable_wages_amount NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    company_law_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employee_wage_plan(
    employee_wage_plan_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(255),
    state CHARACTER VARYING(15),
    wage_plan_value CHARACTER VARYING(15),
    wage_plan_domain CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255) NOT NULL,
    description CHARACTER VARYING(100),
    rules_version CHARACTER VARYING(10),
    invalid_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employer_contribution(
    employer_contribution_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    contribution_amount NUMERIC(19,4),
    contribution_y_t_d_amount NUMERIC(19,4),
    taxable_wages_amount NUMERIC(19,4),
    total_wages_amount NUMERIC(19,4),
    pay_stub_order NUMERIC(19,0),
    company_payroll_item_fk CHARACTER VARYING(255) NOT NULL,
    paycheck_fk CHARACTER VARYING(255),
    qbdt_payline_info_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_employer_preference(
    employer_preference_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    app_name CHARACTER VARYING(30),
    preference_name CHARACTER VARYING(30),
    preference_value CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entitlement(
    entitlement_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    billing_day_of_month INTEGER,
    billing_profile_id CHARACTER VARYING(40),
    trial_associated INTEGER DEFAULT 0,
    retail INTEGER NOT NULL DEFAULT 0,
    billing_realm_id CHARACTER VARYING(30),
    o_i_i_billing_flag CHARACTER VARYING(20)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entitlement_code(
    entitlement_code_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    edition_type CHARACTER VARYING(255),
    number_of_employees_type CHARACTER VARYING(255),
    quick_books_subtype NUMERIC(19,0),
    subtype_description CHARACTER VARYING(50),
    asset_item_cd CHARACTER VARYING(255),
    asset_item_number CHARACTER VARYING(10),
    is_primary INTEGER,
    is_usage_billing INTEGER,
    is_first_usage_free INTEGER,
    billing_frequency_type CHARACTER VARYING(255),
    asset_type_cd CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entitlement_code_offering(
    entitlement_code_offering_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    service_cd CHARACTER VARYING(255),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    entitlement_code_fk CHARACTER VARYING(255) NOT NULL,
    offering_fk CHARACTER VARYING(255) NOT NULL,
    price_type CHARACTER VARYING(4000),
    is_default INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entitlement_message(
    entitlement_message_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    message_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    event_reason CHARACTER VARYING(50),
    expiration_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    entitlement_offering_code CHARACTER VARYING(20),
    order_number CHARACTER VARYING(20),
    message TEXT,
    license_number CHARACTER VARYING(20),
    status CHARACTER VARYING(255),
    token NUMERIC(19,0),
    failure_count NUMERIC(19,0),
    last_failure_message CHARACTER VARYING(1000),
    message_enc TEXT
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entitlement_unit(
    entitlement_unit_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    entitlement_unit_status CHARACTER VARYING(255),
    service_key CHARACTER VARYING(19),
    entitlement_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    extension_key CHARACTER VARYING(14),
    error_count NUMERIC(19,0),
    fed_tax_id_pt_bk CHARACTER VARYING(9),
    last_validation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    fed_tax_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entity_change(
    entity_change_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(4000),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    old_e_i_n_pt_bk CHARACTER VARYING(80),
    new_e_i_n_pt_bk CHARACTER VARYING(80),
    agent_id CHARACTER VARYING(4000),
    is_successor INTEGER,
    has_new_data_file INTEGER,
    is_error INTEGER,
    old_ein_enc CHARACTER VARYING(4000),
    new_ein_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entity_update(
    entity_update_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    entity_name CHARACTER VARYING(4000),
    retry_count CHARACTER VARYING(4000),
    changed_attributes TEXT,
    status CHARACTER VARYING(255),
    transaction_id CHARACTER VARYING(4000),
    event_type CHARACTER VARYING(255),
    entity_id CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entry_detail_record(
    entry_detail_record_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    trace_number CHARACTER VARYING(20),
    credit_debit_indicator CHARACTER VARYING(255),
    record_data_pt_bk CHARACTER VARYING(250),
    intuit_bank_account_fk CHARACTER VARYING(255),
    n_a_c_h_a_file_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    n_a_c_h_a_file_type CHARACTER VARYING(255),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    legal_name CHARACTER VARYING(100),
    txp_record_data_pt_bk CHARACTER VARYING(90),
    standard_entry_description CHARACTER VARYING(15),
    txp_record_data_enc CHARACTER VARYING(4000),
    record_data_enc CHARACTER VARYING(4000),
    j_p_m_c_trace_number CHARACTER VARYING(50),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    tax_funding_model CHARACTER VARYING(4000)
)
    PARTITION BY RANGE (initiation_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_event_as400_sync(
    event_as400_sync_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    retry_count INTEGER,
    company_event_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_event_detail_type(
    event_detail_type_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    value_class_name CHARACTER VARYING(150)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_event_log(
    event_log_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_event_type(
    event_type_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    event_group_cd CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_evttp_srcsys_assoc(
    interesting_event_types_fk CHARACTER VARYING(255) NOT NULL,
    source_system_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_failed_payroll_run(
    failed_payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_token CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fee(
    fee_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    description CHARACTER VARYING(160),
    fee_cd CHARACTER VARYING(255),
    name CHARACTER VARYING(80),
    source_system_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_financial_trans_state(
    financial_trans_state_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    transaction_state_eff_date TIMESTAMP(6) WITHOUT TIME ZONE,
    insert_user_id CHARACTER VARYING(30),
    gems_upload_batch_fk CHARACTER VARYING(255),
    financial_transaction_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL,
    transaction_response_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255),
    comp_funding_model CHARACTER VARYING(4000)
)
    PARTITION BY RANGE (transaction_state_eff_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_financial_transaction(
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    financial_transaction_amount NUMERIC(19,4),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    settlement_type_cd CHARACTER VARYING(255),
    credit_bank_account_type CHARACTER VARYING(255),
    debit_bank_account_type CHARACTER VARYING(255),
    on_hold INTEGER,
    sku CHARACTER VARYING(40),
    sku_quantity INTEGER,
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
    company_law_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255)
)
    PARTITION BY RANGE (settlement_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_financial_txn_action(
    financial_txn_action_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    action_event_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fintxn_onholdreason_assoc(
    financial_transaction_fk CHARACTER VARYING(255) NOT NULL,
    on_hold_reason_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_forecast(
    forecast_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    estimated_transaction_count INTEGER,
    status CHARACTER VARYING(255),
    run_date TIMESTAMP(6) WITHOUT TIME ZONE,
    actual_transaction_count INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_forecast_detail(
    forecast_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    job_action CHARACTER VARYING(4000),
    estimated_run_time NUMERIC(19,0),
    actual_run_time NUMERIC(19,0),
    forecast_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_form_template(
    form_template_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(4000),
    default_form_template CHARACTER VARYING(50),
    agency_fk CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fraud_address(
    fraud_address_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    address_line1 CHARACTER VARYING(80),
    address_line2 CHARACTER VARYING(80),
    address_line3 CHARACTER VARYING(80),
    city CHARACTER VARYING(256),
    state CHARACTER VARYING(21),
    zip_code CHARACTER VARYING(13),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fraud_bank_account(
    fraud_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    routing_number CHARACTER VARYING(20),
    account_number_pt_bk CHARACTER VARYING(80),
    account_type_cd CHARACTER VARYING(255),
    bank_name CHARACTER VARYING(256),
    bank_account_owner_name CHARACTER VARYING(256),
    fraud_bank_account_reason CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    account_number_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fraud_company(
    fraud_company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    dba_name CHARACTER VARYING(100),
    fed_tax_id_pt_bk CHARACTER VARYING(80),
    legal_name CHARACTER VARYING(100),
    license_number CHARACTER VARYING(100),
    notification_email CHARACTER VARYING(100),
    source_agreement_id CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fraud_contact(
    fraud_contact_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    email CHARACTER VARYING(100),
    first_name CHARACTER VARYING(80),
    last_name CHARACTER VARYING(80),
    phone CHARACTER VARYING(100),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fraud_event(
    fraud_event_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    company_e_i_n_pt_bk CHARACTER VARYING(80),
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
    company_ein_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fraud_rule(
    fraud_rule_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(4000),
    source_system_cd CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fraud_value(
    fraud_value_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(255),
    value CHARACTER VARYING(4000),
    fraud_rule_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fset_file(
    fset_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    file_name CHARACTER VARYING(250),
    file_type CHARACTER VARYING(255),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    transmission_id CHARACTER VARYING(50),
    submit_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_fset_filing_detail(
    fset_filing_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    fed_tax_id_pt_bk CHARACTER VARYING(80),
    agency_id_pt_bk CHARACTER VARYING(80),
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
    agency_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_funding_model(
    funding_model_cd CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    number_of_funding_days INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_gems_ledger_posting_rule(
    gems_ledger_posting_rule_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    account CHARACTER VARYING(80),
    company CHARACTER VARYING(16),
    department CHARACTER VARYING(16),
    group_code CHARACTER VARYING(16),
    inter_company CHARACTER VARYING(16),
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    reporting_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_gems_monthly_balance(
    gems_monthly_balance_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    period_balance NUMERIC(19,4),
    reported_balance NUMERIC(19,4),
    reporting_period CHARACTER VARYING(40),
    to_date_balance NUMERIC(19,4),
    gems_ledger_posting_rule_fk CHARACTER VARYING(255) NOT NULL,
    gems_upload_batch_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_gems_upload_batch(
    gems_upload_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    batch_id INTEGER,
    batch_type CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_status CHARACTER VARYING(255),
    file_name CHARACTER VARYING(1000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_hours_worked_exception(
    hours_worked_exception_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    pay_type CHARACTER VARYING(255),
    payroll_item_cd CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_individual(
    individual_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    first_name CHARACTER VARYING(80),
    gender_cd CHARACTER VARYING(255),
    last_name CHARACTER VARYING(80),
    middle_name CHARACTER VARYING(80),
    communication_type_preference CHARACTER VARYING(255),
    email CHARACTER VARYING(100),
    phone CHARACTER VARYING(100),
    mailing_address_fk CHARACTER VARYING(255),
    suffix CHARACTER VARYING(20),
    has_invalid_email INTEGER DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_industry_type(
    industry_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    industry CHARACTER VARYING(4000),
    standard_industry_code CHARACTER VARYING(10)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_intuit_ba_bt_ft(
    intuit_ba_bt_ft_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    file_type CHARACTER VARYING(255),
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    intuit_bank_account_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_intuit_bank_acc_txn_type(
    intuit_bank_acc_txn_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    credit_debit_ind CHARACTER VARYING(255),
    intuit_bank_account_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_intuit_bank_account(
    intuit_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    eff_dttm TIMESTAMP(6) WITHOUT TIME ZONE,
    exp_dttm TIMESTAMP(6) WITHOUT TIME ZONE,
    bank_account_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_intuit_shipper_info(
    intuit_shipper_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    shipper_name CHARACTER VARYING(50),
    shipper_address_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_iopsync_company(
    iopsync_company_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    company_id INTEGER,
    has_employee_payroll INTEGER,
    has_contractor_payment INTEGER,
    notes CHARACTER VARYING(4000),
    retry_count INTEGER,
    status CHARACTER VARYING(255),
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    end_time TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_law(
    law_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(4000),
    law_category_code CHARACTER VARYING(255),
    law_abbrev CHARACTER VARYING(50),
    is_employer_tax INTEGER,
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    law_type_cd CHARACTER VARYING(15),
    requires_month_counts INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_law_rate_range(
    law_rate_range_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    min_rate NUMERIC(19,7),
    max_rate NUMERIC(19,7),
    precision INTEGER,
    law_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_law_rate_value(
    law_rate_value_id CHARACTER VARYING(50) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    rate NUMERIC(19,7),
    law_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ledger_account(
    ledger_account_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    balance_calculation_rule CHARACTER VARYING(255),
    ledger_account_type CHARACTER VARYING(255),
    reporting_frequency CHARACTER VARYING(255),
    requires_quarter_law INTEGER,
    account_abbreviation CHARACTER VARYING(20)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ledger_account_action(
    ledger_account_action_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    credit_debit_indicator CHARACTER VARYING(255),
    action_event_fk CHARACTER VARYING(255) NOT NULL,
    ledger_account_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ledger_balance(
    ledger_balance_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    balance_amount NUMERIC(19,4),
    balance_date TIMESTAMP(6) WITHOUT TIME ZONE,
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    reporting_type CHARACTER VARYING(255)
)
    PARTITION BY RANGE (balance_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ledger_balance_backup(
    ledger_balance_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    balance_amount NUMERIC(19,4),
    balance_date TIMESTAMP(6) WITHOUT TIME ZONE,
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    reporting_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ledger_operation(
    ledger_operation_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_system_code CHARACTER VARYING(255),
    source_company_id CHARACTER VARYING(4000),
    amount NUMERIC(19,4),
    memo CHARACTER VARYING(4000),
    check_date TIMESTAMP(6) WITHOUT TIME ZONE,
    original_legal_name CHARACTER VARYING(4000),
    status CHARACTER VARYING(255),
    messages CHARACTER VARYING(4000),
    original_index INTEGER,
    law_fk CHARACTER VARYING(255),
    ledger_operation_job_fk CHARACTER VARYING(255),
    wage_amount NUMERIC(19,4)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ledger_operation_job(
    ledger_operation_job_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finish_time TIMESTAMP(6) WITHOUT TIME ZONE,
    job_type CHARACTER VARYING(255),
    original_file TEXT,
    processed_file TEXT,
    description CHARACTER VARYING(256)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_liab_check_billing_assoc(
    liab_check_billing_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    billing_detail_fk CHARACTER VARYING(255),
    liability_check_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_liability_adjustment(
    liability_adjustment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    comp_adjust_submission_fk CHARACTER VARYING(255),
    law_fk CHARACTER VARYING(255) NOT NULL,
    employee_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255) NOT NULL,
    taxable_wages NUMERIC(19,4),
    total_wages NUMERIC(19,4),
    company_law_fk CHARACTER VARYING(255),
    is_reconciling_adjustment INTEGER,
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_liability_check(
    liability_check_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_void INTEGER,
    source_id CHARACTER VARYING(9),
    transaction_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255) NOT NULL,
    type CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255),
    system_modified_token NUMERIC(19,0)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_liability_check_line(
    liability_check_line_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    liability_check_fk CHARACTER VARYING(255) NOT NULL,
    company_law_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_limit_rule(
    limit_rule_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_system_cd CHARACTER VARYING(255),
    description CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_limit_value(
    limit_value_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(255),
    value CHARACTER VARYING(255),
    tier INTEGER,
    limit_rule_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_manual_requirement(
    manual_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_message_log(
    message_log_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    request_log TEXT,
    transaction_id CHARACTER VARYING(4000),
    response_log TEXT,
    flow_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_money_movement_transaction(
    money_movement_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    due_date TIMESTAMP(6) WITHOUT TIME ZONE,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    mm_transaction_amount NUMERIC(19,4),
    status CHARACTER VARYING(255),
    money_movement_payment_method CHARACTER VARYING(255),
    original_initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    deposit_frequency_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    offload_batch_fk CHARACTER VARYING(255),
    reference_number CHARACTER VARYING(4000),
    manual_payment_status CHARACTER VARYING(255),
    payment_frequency_fk CHARACTER VARYING(255),
    original_transaction_fk CHARACTER VARYING(255),
    payment_period_begin TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_period_end TIMESTAMP(6) WITHOUT TIME ZONE,
    agency_taxpayer_id_pt_bk CHARACTER VARYING(80),
    payment_template_fk CHARACTER VARYING(255),
    tax_payment_status CHARACTER VARYING(255),
    tax_pmtstatus_effectivedate TIMESTAMP(6) WITHOUT TIME ZONE,
    agency_taxpayer_id_enc CHARACTER VARYING(4000),
    transaction_number CHARACTER VARYING(4000)
)
    PARTITION BY RANGE (initiation_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_nachafile(
    nachafile_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    offload_batch_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offer(
    offer_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    offer_cd CHARACTER VARYING(40),
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    is_approved INTEGER,
    discount_type CHARACTER VARYING(255),
    discount_amount NUMERIC(19,4),
    discount_percent NUMERIC(19,7),
    begin_event CHARACTER VARYING(255),
    end_event CHARACTER VARYING(255),
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    duration_days INTEGER,
    usages_allowed INTEGER,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    promotion_id CHARACTER VARYING(50),
    offer_restriction CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offer_price(
    offer_price_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    fee_type CHARACTER VARYING(255),
    offer_fk CHARACTER VARYING(255) NOT NULL,
    alt_unit_price NUMERIC(19,4),
    alt_base_price NUMERIC(19,4)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offer_svcchg_assoc(
    offer_fk CHARACTER VARYING(255) NOT NULL,
    offering_service_charge_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offering(
    offering_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    s_k_u CHARACTER VARYING(40),
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    is_approved INTEGER,
    offering_code CHARACTER VARYING(255),
    service_code CHARACTER VARYING(255),
    limit_rule_fk CHARACTER VARYING(255),
    reporting_type CHARACTER VARYING(255),
    fraud_rule_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offering_svcchg(
    offering_svcchg_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    s_k_u CHARACTER VARYING(40),
    is_tier INTEGER,
    tier_number INTEGER,
    tier_units INTEGER,
    sku_type CHARACTER VARYING(255),
    offering_svcchg_grp_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offering_svcchg_grp(
    offering_svcchg_grp_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256),
    applies_to CHARACTER VARYING(255),
    offering_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offload_batch(
    offload_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_cd CHARACTER VARYING(255),
    status_effecive_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offload_group_fk CHARACTER VARYING(255) NOT NULL,
    is_offldtxn_evt_complete INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_offload_group(
    offload_group_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    cutoff_time CHARACTER VARYING(20),
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    offload_group_cd CHARACTER VARYING(10)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_on_hold_reason(
    on_hold_reason_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    on_hold_reason_cd CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_ownership_type(
    ownership_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    ownership CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pay_item(
    pay_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_item_cd CHARACTER VARYING(255),
    liability_adjustment_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_paycheck(
    paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_paycheck_id CHARACTER VARYING(50),
    pay_period_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    voided_after_offload INTEGER,
    pay_period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    d_d_employee_fk CHARACTER VARYING(255),
    payroll_run_fk CHARACTER VARYING(255) NOT NULL,
    net_amount NUMERIC(19,4),
    comp_adjust_submission_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255),
    gross_amount NUMERIC(19,4),
    y_t_d_gross_amount NUMERIC(19,4),
    y_t_d_net_amount NUMERIC(19,4),
    source_employee_fk CHARACTER VARYING(255),
    is_y_t_d_adjustment INTEGER,
    company_fk CHARACTER VARYING(255),
    approval_date_time_end TIMESTAMP(6) WITHOUT TIME ZONE,
    d_d_message_status CHARACTER VARYING(255) NOT NULL DEFAULT 'None',
    session_i_d CHARACTER VARYING(4000),
    session_id CHARACTER VARYING(4000)
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_paycheck_split(
    paycheck_split_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    paycheck_split_amount NUMERIC(19,4),
    source_dd_txn_id CHARACTER VARYING(50),
    employee_bank_account_fk CHARACTER VARYING(255) NOT NULL,
    paycheck_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    pay_stub_order NUMERIC(19,0)
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_paycheck_usage(
    paycheck_usage_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    check_number CHARACTER VARYING(11),
    source_paycheck_id CHARACTER VARYING(50),
    transaction_id CHARACTER VARYING(50),
    paycheck_status_code CHARACTER VARYING(255),
    bill_fk CHARACTER VARYING(255),
    employee_usage_fk CHARACTER VARYING(255),
    reason_for_free_charge CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_paycheck_usage_hist(
    paycheck_usage_hist_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    notes CHARACTER VARYING(4000),
    employee_usage_fk CHARACTER VARYING(255) NOT NULL,
    paycheck_usage_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payee(
    payee_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(300),
    email CHARACTER VARYING(100),
    phone CHARACTER VARYING(50),
    source_payee_id CHARACTER VARYING(50),
    tax_id_pt_bk CHARACTER VARYING(20),
    company_fk CHARACTER VARYING(255),
    mailing_address_fk CHARACTER VARYING(255),
    is1099 INTEGER,
    account_number_pt_bk CHARACTER VARYING(150),
    has_invalid_email INTEGER DEFAULT 0,
    account_number_enc CHARACTER VARYING(4000),
    tax_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payee_bank_account(
    payee_bank_account_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(50),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payee_fk CHARACTER VARYING(255),
    bank_account_fk CHARACTER VARYING(255),
    session_id CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payment_batch_assoc(
    payment_batch_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    agency_check_batch_fk CHARACTER VARYING(255) NOT NULL,
    money_movement_transaction_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payment_method_requirement(
    payment_method_requirement_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    pmt_template_pmt_method_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payment_requirement(
    payment_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payment_template(
    payment_template_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    default_deposit_frequency CHARACTER VARYING(50),
    non_modifiable_frequency INTEGER,
    payment_template_abbrev CHARACTER VARYING(4000),
    prior_qtr_adj_req_amendment INTEGER,
    agency_refunds_quarterly INTEGER,
    agency_fk CHARACTER VARYING(255),
    no_calculation INTEGER,
    support_start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    txp_record_class CHARACTER VARYING(4000),
    category CHARACTER VARYING(255),
    processing_start_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payment_template_agency_id(
    payment_template_agency_id_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(50),
    payment_template_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payroll_fraud_batch(
    payroll_fraud_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    start_time TIMESTAMP(6) WITHOUT TIME ZONE,
    end_time TIMESTAMP(6) WITHOUT TIME ZONE,
    number_of_payrolls_processed NUMERIC(19,0)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payroll_frequency(
    payroll_freq_cd CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payroll_item(
    payroll_item_code CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payroll_item_description CHARACTER VARYING(4000),
    payroll_item_type CHARACTER VARYING(255),
    tp401k_is_tok_accepted INTEGER,
    tp401k_allows_negative_amounts INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payroll_item_taxable_to(
    payroll_item_taxable_to_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    company_law_fk CHARACTER VARYING(255) NOT NULL,
    company_payroll_item_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payroll_run(
    payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    processed_by_fraud_batch_job INTEGER NOT NULL DEFAULT 0,
    count CHARACTER VARYING(30),
    debit_settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    d_d_status CHARACTER VARYING(255) NOT NULL DEFAULT 'None',
    tax_and_fees_status CHARACTER VARYING(255) NOT NULL DEFAULT 'None',
    d_d_message_status CHARACTER VARYING(255) NOT NULL DEFAULT 'None',
    offload_group_fk CHARACTER VARYING(255) NOT NULL DEFAULT '3b67b658-dc4e-012a-fc4f-005056c02727',
    assisted_usage_billing_token NUMERIC(19,0) NOT NULL DEFAULT 0,
    comp_funding_model CHARACTER VARYING(4000),
    funding_model CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payroll_run_action(
    payroll_run_action_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    action_event_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_payroll_subtype(
    payroll_subtype_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payroll_subtype_cd CHARACTER VARYING(255),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offering_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_paystub(
    paystub_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    source_mod_time INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pmt_template_bankaccount(
    pmt_template_bankaccount_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_bank_account_id CHARACTER VARYING(20),
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    payment_template_fk CHARACTER VARYING(255),
    bank_account_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pmt_template_frequency(
    payment_template_frequency_id CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payment_frequency_id CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    tax_code_id CHARACTER VARYING(8),
    obsolete INTEGER,
    agent_disallowed INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pmt_template_paymentmethod(
    pmt_template_paymentmethod_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payment_method CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255),
    payment_method_order INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pmttemplate_chkinfo_assoc(
    pmttemplate_chkinfo_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payment_template_fk CHARACTER VARYING(255) NOT NULL,
    pmttemplate_printedchkinfo_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pmttemplate_printedchkinfo(
    pmttemplate_printedchkinfo_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name_line2 CHARACTER VARYING(40),
    name_line1 CHARACTER VARYING(40),
    address_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_posting_rule(
    posting_rule_cd CHARACTER VARYING(40) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    credit_debit_ind CHARACTER VARYING(10),
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_prior_payment_submission(
    prior_payment_submission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_id CHARACTER VARYING(9),
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_property_audit(
    property_audit_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    class_name CHARACTER VARYING(100),
    property_name CHARACTER VARYING(100),
    old_property_value CHARACTER VARYING(4000),
    new_property_value CHARACTER VARYING(4000),
    object_identifier CHARACTER VARYING(40),
    user_id CHARACTER VARYING(100),
    audit_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_address(
    pstub_address_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    line1 CHARACTER VARYING(51),
    line2 CHARACTER VARYING(51),
    line3 CHARACTER VARYING(51),
    line4 CHARACTER VARYING(51),
    line5 CHARACTER VARYING(51)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_dditem(
    pstub_dditem_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    acct_name CHARACTER VARYING(51),
    acct_number CHARACTER VARYING(30),
    acct_type CHARACTER VARYING(10),
    bank_name CHARACTER VARYING(51),
    name CHARACTER VARYING(31),
    payroll_item_list_id CHARACTER VARYING(51),
    routing_number CHARACTER VARYING(20),
    paystub_fk CHARACTER VARYING(255),
    cur_amt NUMERIC(19,4)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_employee_info(
    pstub_employee_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    source_mod_time INTEGER,
    pstub_address_fk CHARACTER VARYING(255),
    middle_name CHARACTER VARYING(30),
    fed_claim_dependents NUMERIC(19,4),
    fed_other_income NUMERIC(19,4),
    fed_deductions NUMERIC(19,4),
    fed_multiple_jobs CHARACTER VARYING(4000),
    fed_w4_employee_pref CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_employee_info_bk(
    pstub_employee_info_seq CHARACTER VARYING(255),
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0),
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
    source_mod_time INTEGER,
    pstub_address_fk CHARACTER VARYING(255),
    middle_name CHARACTER VARYING(30),
    fed_claim_dependents NUMERIC(19,4),
    fed_other_income NUMERIC(19,4),
    fed_deductions NUMERIC(19,4),
    fed_multiple_jobs CHARACTER VARYING(4000),
    fed_w4_employee_pref CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_employee_preference(
    pstub_employee_preference_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    employee_fk CHARACTER VARYING(255),
    app_name CHARACTER VARYING(30),
    preference_name CHARACTER VARYING(30),
    preference_value CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_employer_info(
    pstub_employer_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(60),
    name_addr_fed_id CHARACTER VARYING(200),
    object_hash CHARACTER VARYING(200),
    pstub_address_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_msg(
    pstub_msg_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    sequence INTEGER,
    type CHARACTER VARYING(255),
    text CHARACTER VARYING(200),
    paystub_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_paid_timeoff_item(
    pstub_paid_timeoff_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    acct_name CHARACTER VARYING(51),
    available CHARACTER VARYING(30),
    name CHARACTER VARYING(31),
    payroll_item_list_id CHARACTER VARYING(51),
    y_t_d_used CHARACTER VARYING(30),
    paystub_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_pay_item(
    pstub_pay_item_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    acct_name CHARACTER VARYING(51),
    employee_paid INTEGER,
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
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_pstub_state_tax_info(
    pstub_state_tax_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    agency_id_pt_bk CHARACTER VARYING(80),
    agency_name CHARACTER VARYING(300),
    pstub_employer_info_fk CHARACTER VARYING(255) NOT NULL,
    agency_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_employee_info(
    qbdt_employee_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    bill_pay_account CHARACTER VARYING(64),
    initials CHARACTER VARYING(12),
    tracking_class CHARACTER VARYING(128),
    use_time INTEGER,
    enforce_subject_to INTEGER,
    employee_type CHARACTER VARYING(255),
    is_deleted INTEGER,
    title CHARACTER VARYING(20),
    alt_phone CHARACTER VARYING(21),
    employee_fk CHARACTER VARYING(255),
    list_id CHARACTER VARYING(4000),
    token NUMERIC(19,0),
    company_fk CHARACTER VARYING(255),
    use_d_d INTEGER,
    print_as_name CHARACTER VARYING(50),
    is_recoverable INTEGER DEFAULT 0,
    is_assisted INTEGER NOT NULL DEFAULT 0,
    employee_seasonal CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_paycheck_info(
    qbdt_paycheck_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    prorate INTEGER,
    check_number CHARACTER VARYING(11),
    memo CHARACTER VARYING(4000),
    cleared CHARACTER VARYING(1),
    on_service INTEGER,
    tracking_class CHARACTER VARYING(128),
    account_name CHARACTER VARYING(128),
    paycheck_fk CHARACTER VARYING(255),
    list_id CHARACTER VARYING(4000),
    company_fk CHARACTER VARYING(255),
    token NUMERIC(19,0),
    vacation_hours_accrued NUMERIC(19,7),
    sick_hours_accrued NUMERIC(19,7),
    void_token NUMERIC(19,0),
    is_assisted INTEGER NOT NULL DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_payline_info(
    qbdt_payline_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    rate NUMERIC(19,7),
    tracking_class CHARACTER VARYING(128),
    job CHARACTER VARYING(128),
    item CHARACTER VARYING(128),
    wc_code CHARACTER VARYING(20),
    quantity NUMERIC(19,7),
    expense_by_job INTEGER,
    rate_type CHARACTER VARYING(255),
    quantity_type CHARACTER VARYING(255),
    employer_contribution_fk CHARACTER VARYING(255),
    compensation_fk CHARACTER VARYING(255),
    deduction_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_payroll_item_info(
    qbdt_payroll_item_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    earnings_table INTEGER,
    is_employee_paid INTEGER,
    liability_account CHARACTER VARYING(255),
    liability_agency CHARACTER VARYING(255),
    agency_id_pt_bk CHARACTER VARYING(24),
    adjusts_gross INTEGER,
    based_on_quantity INTEGER,
    expense_account CHARACTER VARYING(255),
    default_rate NUMERIC(19,7),
    default_limit NUMERIC(19,4),
    expense_by_job INTEGER,
    pay_type CHARACTER VARYING(255),
    special_type CHARACTER VARYING(255),
    is_deleted INTEGER,
    on_service INTEGER,
    default_rate_type CHARACTER VARYING(255),
    company_law_fk CHARACTER VARYING(255),
    company_payroll_item_fk CHARACTER VARYING(255),
    list_id CHARACTER VARYING(4000),
    token NUMERIC(19,0),
    company_fk CHARACTER VARYING(255),
    rate_push_token NUMERIC(19,0),
    overtime_multiplier NUMERIC(19,7),
    detail_type NUMERIC(19,0),
    agency_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_payroll_trans_line(
    qbdt_payroll_trans_line_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    amount NUMERIC(19,4),
    wage_base_amount NUMERIC(19,4),
    taxable_wage_amount NUMERIC(19,4),
    company_payroll_item_fk CHARACTER VARYING(255),
    qbdt_payroll_transaction_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_payroll_transaction(
    qbdt_payroll_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    transaction_type CHARACTER VARYING(255),
    amount NUMERIC(19,4),
    period_end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    is_voided INTEGER,
    source_id CHARACTER VARYING(9),
    transaction_date TIMESTAMP(6) WITHOUT TIME ZONE,
    comp_adjust_submission_fk CHARACTER VARYING(255),
    prior_payment_submission_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    employee_name CHARACTER VARYING(255),
    e_e_calculation_token NUMERIC(19,0)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_transaction_info(
    qbdt_transaction_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    agency_name CHARACTER VARYING(128),
    reference_number CHARACTER VARYING(11),
    account_name CHARACTER VARYING(128),
    memo CHARACTER VARYING(4000),
    on_service INTEGER,
    cleared CHARACTER VARYING(1),
    tracking_class CHARACTER VARYING(128),
    is_deleted INTEGER,
    token NUMERIC(19,0),
    liability_check_fk CHARACTER VARYING(255),
    liability_check_line_fk CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    is_direct_deposit INTEGER DEFAULT 0,
    system_generated INTEGER DEFAULT 0,
    comp_adjust_submission_fk CHARACTER VARYING(255),
    liability_adjustment_fk CHARACTER VARYING(255),
    financial_transaction_fk CHARACTER VARYING(255),
    prior_payment_submission_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    qbdt_payroll_transaction_fk CHARACTER VARYING(255),
    qbdt_payroll_trans_line_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_qbdt_unprocessed_request(
    qbdt_unprocessed_request_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    error_message CHARACTER VARYING(4000),
    source_system_transmission_id CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    host CHARACTER VARYING(200)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_quickbooks_info(
    quickbooks_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL DEFAULT 0,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
    process_transmissions INTEGER,
    symphony_on_board_version CHARACTER VARYING(100),
    i_a_m_realm_id CHARACTER VARYING(50),
    allow_transmissions INTEGER,
    watermark_date TIMESTAMP(6) WITHOUT TIME ZONE,
    quickbooks_sku CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_racompany_service_info(
    racompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    override_company_limit_amount NUMERIC(19,4)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_rafenrollment(
    rafenrollment_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_agency_fk CHARACTER VARYING(255),
    status_reason CHARACTER VARYING(1000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_rafenrollment_detail(
    rafenrollment_detail_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    fed_taxid_pt_bk CHARACTER VARYING(4000),
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
    fed_taxid_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_rafenrollment_file(
    rafenrollment_file_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status CHARACTER VARYING(255),
    r_a_f_action_code CHARACTER VARYING(255),
    file_name CHARACTER VARYING(4000),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    email_file_name CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_rate_ledger_operation(
    rate_ledger_operation_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    additional_filing_amount_name CHARACTER VARYING(4000),
    push_to_quick_books INTEGER,
    rate NUMERIC(19,7)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_report_job_setup(
    report_name CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    report_schedule CHARACTER VARYING(4000),
    report_mailing_list CHARACTER VARYING(4000),
    query_filename CHARACTER VARYING(4000),
    is_automatically_scheduled INTEGER,
    report_namespace CHARACTER VARYING(4000),
    encrypted_fields CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_reporting_agent(
    reporting_agent_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    fax CHARACTER VARYING(100),
    contact CHARACTER VARYING(300),
    fed_id_pt_bk CHARACTER VARYING(80),
    fed_tax_id_pt_bk CHARACTER VARYING(80),
    legal_name CHARACTER VARYING(100),
    phone CHARACTER VARYING(100),
    address_fk CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000),
    fed_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_return_reason_desc(
    reason_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_role_sub_status(
    role_sub_status_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    allowed_change_type CHARACTER VARYING(255),
    auth_role_fk CHARACTER VARYING(255) NOT NULL,
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_rtb_customer_issue(
    rtb_customer_issue_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    error_log CHARACTER VARYING(4000),
    status CHARACTER VARYING(4000),
    company_id CHARACTER VARYING(4000),
    resolution_type CHARACTER VARYING(4000),
    jira_id CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_rtbautomationbackup(
    rtbautomationbackup_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    rtb_backup TEXT,
    company_id CHARACTER VARYING(4000),
    event_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_saved_reports(
    saved_reports_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    report_id CHARACTER VARYING(256),
    display_name CHARACTER VARYING(256),
    input_param CHARACTER VARYING(4000),
    query TEXT,
    description CHARACTER VARYING(512)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_second_offload(
    second_offload_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    override_cutoff_time CHARACTER VARYING(16),
    offload_group_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_serv_stat_txn_sku_type(
    serv_stat_txn_sku_type_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    sku_type CHARACTER VARYING(255),
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    offering_service_charge_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_service(
    service_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    can_be_manually_cancelled INTEGER,
    psp_provides_customer_service INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_service_status(
    service_status_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name CHARACTER VARYING(100),
    description CHARACTER VARYING(256)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_service_sub_status(
    service_sub_status_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(500),
    name CHARACTER VARYING(100),
    is_set_manually INTEGER,
    is_removed_manually INTEGER,
    service_status_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_smsmigration(
    smsmigration_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    migration_status CHARACTER VARYING(255),
    validation_error_result TEXT,
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_smssync_failure(
    smssync_failure_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_company_id CHARACTER VARYING(50),
    company_realm_id NUMERIC(19,0),
    sync_direction CHARACTER VARYING(255),
    last_retry_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    status CHARACTER VARYING(255),
    count INTEGER,
    failure_reason CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_source_payroll_parameter(
    source_payroll_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    parameter_cd CHARACTER VARYING(255),
    parameter_value CHARACTER VARYING(100),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    source_system_cd CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_source_system(
    source_system_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_source_system_law_assoc(
    source_system_law_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_law_code CHARACTER VARYING(80),
    law_fk CHARACTER VARYING(255) NOT NULL,
    source_system_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_sourcesys_printedchk_info(
    sourcesys_printedchk_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    name_line1 CHARACTER VARYING(40),
    name_line2 CHARACTER VARYING(40),
    next_check_number NUMERIC(19,0),
    source_system_code CHARACTER VARYING(255),
    source_system_logo TEXT,
    bank_logo TEXT,
    address_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_sql_execution_log_entry(
    sql_execution_log_entry_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    row_count INTEGER,
    s_q_l TEXT,
    reason CHARACTER VARYING(2000),
    execution_time NUMERIC(19,0),
    error_message CHARACTER VARYING(4000),
    user_name CHARACTER VARYING(60),
    committed INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_state_edi_tax_file(
    state_edi_tax_file_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    ack_file_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_state_report_assoc(
    state_report_output_fk CHARACTER VARYING(255),
    payment_template_frequency_fk CHARACTER VARYING(255),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    state_report_assoc_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_state_report_output(
    state_report_output_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    report_output TEXT,
    report_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_suicredits_job(
    suicredits_job_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    year INTEGER,
    quarter INTEGER,
    status CHARACTER VARYING(255),
    payment_template_fk CHARACTER VARYING(255),
    processed_file TEXT
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_svcchgprice(
    svcchgprice_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offering_service_charge_fk CHARACTER VARYING(255) NOT NULL,
    unit_price NUMERIC(19,4),
    base_price NUMERIC(19,4)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_svcstat_srcsys_assoc(
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    source_system_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_svcstat_svc_assoc(
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    service_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_svcstat_syscap_assoc(
    service_sub_status_fk CHARACTER VARYING(255) NOT NULL,
    system_capability_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_capability(
    system_capability_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(256)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_parameter(
    system_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    system_parameter_cd CHARACTER VARYING(256),
    system_parameter_description CHARACTER VARYING(160),
    system_parameter_org CHARACTER VARYING(80),
    system_parameter_value CHARACTER VARYING(500),
    is_secured INTEGER NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_parameter_apr21(
    system_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    system_parameter_cd CHARACTER VARYING(256),
    system_parameter_description CHARACTER VARYING(160),
    system_parameter_org CHARACTER VARYING(80),
    system_parameter_value CHARACTER VARYING(500),
    is_secured INTEGER NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_parameter_april15(
    system_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    system_parameter_cd CHARACTER VARYING(256),
    system_parameter_description CHARACTER VARYING(160),
    system_parameter_org CHARACTER VARYING(80),
    system_parameter_value CHARACTER VARYING(500),
    is_secured INTEGER NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_parameter_april17(
    system_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    system_parameter_cd CHARACTER VARYING(256),
    system_parameter_description CHARACTER VARYING(160),
    system_parameter_org CHARACTER VARYING(80),
    system_parameter_value CHARACTER VARYING(500),
    is_secured INTEGER NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_parameter_from_old(
    system_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    system_parameter_cd CHARACTER VARYING(256),
    system_parameter_description CHARACTER VARYING(160),
    system_parameter_org CHARACTER VARYING(80),
    system_parameter_value CHARACTER VARYING(500),
    is_secured INTEGER NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_parameter_test2(
    system_parameter_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    system_parameter_cd CHARACTER VARYING(256),
    system_parameter_description CHARACTER VARYING(160),
    system_parameter_org CHARACTER VARYING(80),
    system_parameter_value CHARACTER VARYING(500),
    is_secured INTEGER NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_payment_requirement(
    system_payment_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    system_requirement_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_system_requirement(
    system_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    system_requirement_type CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tax(
    tax_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tax_company_service_info(
    tax_company_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    last_quarter_to_file INTEGER,
    file_annual_returns INTEGER,
    final_annual_returns INTEGER,
    last_payroll_date TIMESTAMP(6) WITHOUT TIME ZONE,
    w2_delivery_preference_cd CHARACTER VARYING(255),
    client_packet_delivery_pref_cd CHARACTER VARYING(255),
    last_tax_year INTEGER,
    in_house_w2 INTEGER DEFAULT 0,
    include_on_s_s_a_file INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tax_credits9061(
    tax_credits9061_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    form9061 TEXT,
    fed_tax_id_pt_bk CHARACTER VARYING(9),
    employee_name CHARACTER VARYING(242),
    s_s_n_pt_bk CHARACTER VARYING(9),
    tax_credits_application_fk CHARACTER VARYING(255) NOT NULL,
    fed_tax_id_enc CHARACTER VARYING(4000),
    s_s_n_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tax_credits_application(
    tax_credits_application_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    unsigned_document TEXT,
    signed_document TEXT,
    document_key CHARACTER VARYING(4000),
    signers_remaining CHARACTER VARYING(4000),
    document_password CHARACTER VARYING(4000),
    employer_email CHARACTER VARYING(4000),
    employee_email CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tax_payment_on_hold_reason(
    tax_payment_on_hold_reason_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_date TIMESTAMP(6) WITHOUT TIME ZONE,
    on_hold_reason_cd CHARACTER VARYING(255),
    money_movement_transaction_fk CHARACTER VARYING(255),
    note CHARACTER VARYING(500)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tax_penalty_interest(
    tax_penalty_interest_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    type CHARACTER VARYING(255),
    transaction_id CHARACTER VARYING(100),
    penalty_interest_date TIMESTAMP(6) WITHOUT TIME ZONE,
    note CHARACTER VARYING(4000),
    payment_method CHARACTER VARYING(255),
    period_type CHARACTER VARYING(255),
    year INTEGER,
    amount NUMERIC(19,4),
    period_number INTEGER,
    check_number CHARACTER VARYING(30),
    company_agency_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tax_table_misc_data(
    tax_table_misc_data_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    value CHARACTER VARYING(56),
    employee_tax_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255),
    misc_data_order INTEGER DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_third_party401k_batch(
    third_party401k_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    batch_id INTEGER,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_date TIMESTAMP(6) WITHOUT TIME ZONE,
    upload_status_cd CHARACTER VARYING(255),
    file_name CHARACTER VARYING(1000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_threshold_requirement(
    threshold_requirement_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    maximum_payment_amount NUMERIC(19,4)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401k_batch_employee(
    tp401k_batch_employee_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    third_party401k_batch_fk CHARACTER VARYING(255),
    employee_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401k_batch_paycheck(
    tp401k_batch_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    third_party401k_batch_fk CHARACTER VARYING(255),
    paycheck_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401k_paycheck(
    tp401k_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    current_state_cd CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401k_paycheck_pending(
    tp401k_paycheck_pending_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    state_cd CHARACTER VARYING(255),
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    third_party401k_paycheck_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401k_paycheck_state(
    tp401k_paycheck_state_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    state_cd CHARACTER VARYING(255),
    state_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    third_party401k_paycheck_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401k_signup_batch(
    tp401k_signup_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    file_name CHARACTER VARYING(1000),
    batch_id INTEGER,
    download_date TIMESTAMP(6) WITHOUT TIME ZONE,
    download_status_cd CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401k_signup_queue(
    tp401k_signup_queue_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    fed_tax_id_pt_bk CHARACTER VARYING(80),
    custodial_id CHARACTER VARYING(100),
    effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    legal_name CHARACTER VARYING(100),
    has_safe_harbor INTEGER,
    status CHARACTER VARYING(255),
    fed_tax_id_enc CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_tp401kcompany_service_info(
    tp401kcompany_service_info_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    custodial_id CHARACTER VARYING(4000),
    has_safe_harbor INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_transaction_offload_batch(
    transaction_offload_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    offload_status_cd CHARACTER VARYING(10),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    financial_transaction_fk CHARACTER VARYING(255) NOT NULL,
    offload_batch_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_transaction_response(
    transaction_response_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    source_request_id CHARACTER VARYING(50),
    transaction_token_number NUMERIC(19,0),
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_transaction_return(
    transaction_return_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    bank_return_cd CHARACTER VARYING(10),
    bank_return_description CHARACTER VARYING(160),
    bank_return_trace_number NUMERIC(19,0),
    return_status_cd CHARACTER VARYING(255),
    return_status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    money_movement_transaction_fk CHARACTER VARYING(255),
    return_batch_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_transaction_return_batch(
    transaction_return_batch_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    a_c_h_return_file_name CHARACTER VARYING(1000),
    return_date TIMESTAMP(6) WITHOUT TIME ZONE,
    status_cd CHARACTER VARYING(255),
    status_effective_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_transaction_state(
    transaction_state_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_transaction_type(
    transaction_type_cd CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    description CHARACTER VARYING(160),
    name CHARACTER VARYING(80),
    transaction_category CHARACTER VARYING(255),
    association_type CHARACTER VARYING(255),
    fee_ind INTEGER,
    n_a_c_h_a_batch_type CHARACTER VARYING(255),
    transaction_type_group_cd CHARACTER VARYING(255),
    include_in_txn_response INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_transmission_payroll_run(
    transmission_payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    payroll_process CHARACTER VARYING(255),
    source_system_transmission_id CHARACTER VARYING(255) NOT NULL,
    payroll_run_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_txntype_service_assoc(
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    service_fk CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_usage_period(
    usage_period_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    start_date TIMESTAMP(6) WITHOUT TIME ZONE,
    end_date TIMESTAMP(6) WITHOUT TIME ZONE,
    company_usage_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_user_preference(
    key CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    default_value CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_user_setting(
    user_setting_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    value CHARACTER VARYING(4000),
    user_preference_fk CHARACTER VARYING(255),
    auth_user_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_vmp_employee_info(
    vmp_employee_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    consumer_realm_id CHARACTER VARYING(4000),
    employee_recnum CHARACTER VARYING(4000),
    email CHARACTER VARYING(4000),
    persona_id CHARACTER VARYING(4000),
    company_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_voided_check(
    voided_check_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    reason CHARACTER VARYING(4000),
    money_movement_transaction_fk CHARACTER VARYING(255),
    accounting_report_file_fk CHARACTER VARYING(255),
    agency_check_batch_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_wage_limit(
    wage_limit_id CHARACTER VARYING(10) NOT NULL,
    version NUMERIC(19,0),
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    effective_year_quarter CHARACTER VARYING(5),
    amount NUMERIC(19,4),
    law_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_wc_paycheck(
    wc_paycheck_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    current_state_cd CHARACTER VARYING(255),
    paycheck_fk CHARACTER VARYING(255),
    paycheck_version NUMERIC(19,0) NOT NULL DEFAULT 1
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_wc_paycheck_pending(
    wc_paycheck_pending_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    state_cd CHARACTER VARYING(255),
    workers_comp_paycheck_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_wc_paycheck_state(
    wc_paycheck_state_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    state_cd CHARACTER VARYING(255),
    state_effective_date TIMESTAMP(6) WITHOUT TIME ZONE,
    workers_comp_paycheck_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.qboe_purging_metadata_new(
    table_name CHARACTER VARYING(100) NOT NULL,
    bkp_table_name CHARACTER VARYING(100) NOT NULL,
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT aws_oracle_ext.systimestamp(),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT aws_oracle_ext.systimestamp(),
    table_order NUMERIC(3,0) NOT NULL,
    table_query CHARACTER VARYING(1000),
    record_count NUMERIC(12,0) DEFAULT 0,
    is_backup_created INTEGER DEFAULT 0,
    is_qboe_data_purged INTEGER DEFAULT 0,
    is_company_based INTEGER DEFAULT 1,
    primary_key_column CHARACTER VARYING(100),
    is_revert_required INTEGER DEFAULT 0,
    is_revert_completed INTEGER DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.repl_issue(
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    financial_transaction_amount NUMERIC(19,4),
    settlement_date TIMESTAMP(6) WITHOUT TIME ZONE,
    settlement_type_cd CHARACTER VARYING(255),
    credit_bank_account_type CHARACTER VARYING(255),
    debit_bank_account_type CHARACTER VARYING(255),
    on_hold INTEGER,
    sku CHARACTER VARYING(40),
    sku_quantity INTEGER,
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
    company_law_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255)
)
    PARTITION BY RANGE (settlement_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.sept17_9amist(
    id DOUBLE PRECISION,
    flag DOUBLE PRECISION,
    product CHARACTER VARYING(20),
    channel_id DOUBLE PRECISION,
    cust_id DOUBLE PRECISION,
    amount_sold DOUBLE PRECISION,
    order_date TIMESTAMP(0) WITHOUT TIME ZONE,
    ship_date TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.sql_tracker(
    sql_id CHARACTER VARYING(13),
    plan_hash_value DOUBLE PRECISION,
    sql_text CHARACTER VARYING(1000),
    object_type CHARACTER VARYING(19),
    object_name CHARACTER VARYING(128),
    created TIMESTAMP(0) WITHOUT TIME ZONE,
    num_rows DOUBLE PRECISION,
    operation CHARACTER VARYING(120),
    options CHARACTER VARYING(120),
    module CHARACTER VARYING(64),
    created_date CHARACTER(12)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.sys_export_schema_01(
    process_order DOUBLE PRECISION,
    duplicate DOUBLE PRECISION,
    dump_fileid DOUBLE PRECISION,
    dump_position DOUBLE PRECISION,
    dump_length DOUBLE PRECISION,
    dump_orig_length DOUBLE PRECISION,
    dump_allocation DOUBLE PRECISION,
    completed_rows DOUBLE PRECISION,
    error_count DOUBLE PRECISION,
    elapsed_time DOUBLE PRECISION,
    object_type_path CHARACTER VARYING(200),
    object_path_seqno DOUBLE PRECISION,
    object_type CHARACTER VARYING(30),
    in_progress CHARACTER(1),
    object_name CHARACTER VARYING(500),
    object_long_name CHARACTER VARYING(4000),
    object_schema CHARACTER VARYING(30),
    original_object_schema CHARACTER VARYING(30),
    original_object_name CHARACTER VARYING(4000),
    partition_name CHARACTER VARYING(30),
    subpartition_name CHARACTER VARYING(30),
    dataobj_num DOUBLE PRECISION,
    flags DOUBLE PRECISION,
    property DOUBLE PRECISION,
    trigflag DOUBLE PRECISION,
    creation_level DOUBLE PRECISION,
    completion_time TIMESTAMP(0) WITHOUT TIME ZONE,
    object_tablespace CHARACTER VARYING(30),
    size_estimate DOUBLE PRECISION,
    object_row DOUBLE PRECISION,
    processing_state CHARACTER(1),
    processing_status CHARACTER(1),
    base_process_order DOUBLE PRECISION,
    base_object_type CHARACTER VARYING(30),
    base_object_name CHARACTER VARYING(30),
    base_object_schema CHARACTER VARYING(30),
    ancestor_process_order DOUBLE PRECISION,
    domain_process_order DOUBLE PRECISION,
    parallelization DOUBLE PRECISION,
    unload_method DOUBLE PRECISION,
    load_method DOUBLE PRECISION,
    granules DOUBLE PRECISION,
    scn DOUBLE PRECISION,
    grantor CHARACTER VARYING(30),
    xml_clob TEXT,
    parent_process_order DOUBLE PRECISION,
    name CHARACTER VARYING(30),
    value_t CHARACTER VARYING(4000),
    value_n DOUBLE PRECISION,
    is_default DOUBLE PRECISION,
    file_type DOUBLE PRECISION,
    user_directory CHARACTER VARYING(4000),
    user_file_name CHARACTER VARYING(4000),
    file_name CHARACTER VARYING(4000),
    extend_size DOUBLE PRECISION,
    file_max_size DOUBLE PRECISION,
    process_name CHARACTER VARYING(30),
    last_update TIMESTAMP(0) WITHOUT TIME ZONE,
    work_item CHARACTER VARYING(30),
    object_number DOUBLE PRECISION,
    completed_bytes DOUBLE PRECISION,
    total_bytes DOUBLE PRECISION,
    metadata_io DOUBLE PRECISION,
    data_io DOUBLE PRECISION,
    cumulative_time DOUBLE PRECISION,
    packet_number DOUBLE PRECISION,
    instance_id DOUBLE PRECISION,
    old_value CHARACTER VARYING(4000),
    seed DOUBLE PRECISION,
    last_file DOUBLE PRECISION,
    user_name CHARACTER VARYING(30),
    operation CHARACTER VARYING(30),
    job_mode CHARACTER VARYING(30),
    queue_tabnum DOUBLE PRECISION,
    control_queue CHARACTER VARYING(30),
    status_queue CHARACTER VARYING(30),
    remote_link CHARACTER VARYING(4000),
    version DOUBLE PRECISION,
    job_version CHARACTER VARYING(30),
    db_version CHARACTER VARYING(30),
    timezone CHARACTER VARYING(64),
    state CHARACTER VARYING(30),
    phase DOUBLE PRECISION,
    guid BYTEA,
    start_time TIMESTAMP(0) WITHOUT TIME ZONE,
    block_size DOUBLE PRECISION,
    metadata_buffer_size DOUBLE PRECISION,
    data_buffer_size DOUBLE PRECISION,
    degree DOUBLE PRECISION,
    platform CHARACTER VARYING(101),
    abort_step DOUBLE PRECISION,
    instance CHARACTER VARYING(60),
    cluster_ok DOUBLE PRECISION,
    service_name CHARACTER VARYING(100),
    object_int_oid CHARACTER VARYING(32)
)
        WITH (
        OIDS=FALSE
        );


COMMENT ON TABLE pspadm.sys_export_schema_01
     IS 'Data Pump Master Table EXPORT                         SCHEMA                        ';



CREATE TABLE pspadm.sys_export_schema_02(
    process_order DOUBLE PRECISION,
    duplicate DOUBLE PRECISION,
    dump_fileid DOUBLE PRECISION,
    dump_position DOUBLE PRECISION,
    dump_length DOUBLE PRECISION,
    dump_orig_length DOUBLE PRECISION,
    dump_allocation DOUBLE PRECISION,
    completed_rows DOUBLE PRECISION,
    error_count DOUBLE PRECISION,
    elapsed_time DOUBLE PRECISION,
    object_type_path CHARACTER VARYING(200),
    object_path_seqno DOUBLE PRECISION,
    object_type CHARACTER VARYING(30),
    in_progress CHARACTER(1),
    object_name CHARACTER VARYING(500),
    object_long_name CHARACTER VARYING(4000),
    object_schema CHARACTER VARYING(30),
    original_object_schema CHARACTER VARYING(30),
    original_object_name CHARACTER VARYING(4000),
    partition_name CHARACTER VARYING(30),
    subpartition_name CHARACTER VARYING(30),
    dataobj_num DOUBLE PRECISION,
    flags DOUBLE PRECISION,
    property DOUBLE PRECISION,
    trigflag DOUBLE PRECISION,
    creation_level DOUBLE PRECISION,
    completion_time TIMESTAMP(0) WITHOUT TIME ZONE,
    object_tablespace CHARACTER VARYING(30),
    size_estimate DOUBLE PRECISION,
    object_row DOUBLE PRECISION,
    processing_state CHARACTER(1),
    processing_status CHARACTER(1),
    base_process_order DOUBLE PRECISION,
    base_object_type CHARACTER VARYING(30),
    base_object_name CHARACTER VARYING(30),
    base_object_schema CHARACTER VARYING(30),
    ancestor_process_order DOUBLE PRECISION,
    domain_process_order DOUBLE PRECISION,
    parallelization DOUBLE PRECISION,
    unload_method DOUBLE PRECISION,
    load_method DOUBLE PRECISION,
    granules DOUBLE PRECISION,
    scn DOUBLE PRECISION,
    grantor CHARACTER VARYING(30),
    xml_clob TEXT,
    parent_process_order DOUBLE PRECISION,
    name CHARACTER VARYING(30),
    value_t CHARACTER VARYING(4000),
    value_n DOUBLE PRECISION,
    is_default DOUBLE PRECISION,
    file_type DOUBLE PRECISION,
    user_directory CHARACTER VARYING(4000),
    user_file_name CHARACTER VARYING(4000),
    file_name CHARACTER VARYING(4000),
    extend_size DOUBLE PRECISION,
    file_max_size DOUBLE PRECISION,
    process_name CHARACTER VARYING(30),
    last_update TIMESTAMP(0) WITHOUT TIME ZONE,
    work_item CHARACTER VARYING(30),
    object_number DOUBLE PRECISION,
    completed_bytes DOUBLE PRECISION,
    total_bytes DOUBLE PRECISION,
    metadata_io DOUBLE PRECISION,
    data_io DOUBLE PRECISION,
    cumulative_time DOUBLE PRECISION,
    packet_number DOUBLE PRECISION,
    instance_id DOUBLE PRECISION,
    old_value CHARACTER VARYING(4000),
    seed DOUBLE PRECISION,
    last_file DOUBLE PRECISION,
    user_name CHARACTER VARYING(30),
    operation CHARACTER VARYING(30),
    job_mode CHARACTER VARYING(30),
    queue_tabnum DOUBLE PRECISION,
    control_queue CHARACTER VARYING(30),
    status_queue CHARACTER VARYING(30),
    remote_link CHARACTER VARYING(4000),
    version DOUBLE PRECISION,
    job_version CHARACTER VARYING(30),
    db_version CHARACTER VARYING(30),
    timezone CHARACTER VARYING(64),
    state CHARACTER VARYING(30),
    phase DOUBLE PRECISION,
    guid BYTEA,
    start_time TIMESTAMP(0) WITHOUT TIME ZONE,
    block_size DOUBLE PRECISION,
    metadata_buffer_size DOUBLE PRECISION,
    data_buffer_size DOUBLE PRECISION,
    degree DOUBLE PRECISION,
    platform CHARACTER VARYING(101),
    abort_step DOUBLE PRECISION,
    instance CHARACTER VARYING(60),
    cluster_ok DOUBLE PRECISION,
    service_name CHARACTER VARYING(100),
    object_int_oid CHARACTER VARYING(32)
)
        WITH (
        OIDS=FALSE
        );


COMMENT ON TABLE pspadm.sys_export_schema_02
     IS 'Data Pump Master Table EXPORT                         SCHEMA                        ';



CREATE TABLE pspadm.t10(
    id DOUBLE PRECISION NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_a_prc_inc(
    psid CHARACTER VARYING(100),
    offering CHARACTER VARYING(100),
    new_offering CHARACTER VARYING(100),
    change_date TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_as400_w2_counts(
    source_company_id CHARACTER VARYING(50),
    form_count INTEGER
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_atr_ato_data(
    company_fk CHARACTER VARYING(255),
    financial_transaction_amount NUMERIC(19,4),
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_cancelled_entitlement(
    license_number CHARACTER VARYING(40),
    eoc CHARACTER VARYING(20),
    cancel_date TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_ddactivation(
    company_seq CHARACTER VARYING(255) NOT NULL,
    event_type_cd CHARACTER VARYING(255),
    event_detail_type_cd CHARACTER VARYING(255),
    value CHARACTER VARYING(4000),
    event_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    newvalue CHARACTER VARYING(4000),
    oldvalue CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_ddactivation_v2(
    company_seq CHARACTER VARYING(255) NOT NULL,
    event_type_cd CHARACTER VARYING(255),
    event_detail_type_cd CHARACTER VARYING(255),
    value CHARACTER VARYING(4000),
    event_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    newvalue CHARACTER VARYING(4000),
    oldvalue CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_ddactivation_v3(
    company_seq CHARACTER VARYING(255) NOT NULL,
    event_type_cd CHARACTER VARYING(255),
    event_detail_type_cd CHARACTER VARYING(255),
    value CHARACTER VARYING(4000),
    event_time_stamp TIMESTAMP(6) WITHOUT TIME ZONE,
    newvalue CHARACTER VARYING(4000),
    oldvalue CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_ft(
    ft_seq CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_fts(
    financial_trans_state_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    transaction_state_eff_date TIMESTAMP(6) WITHOUT TIME ZONE,
    insert_user_id CHARACTER VARYING(30),
    gems_upload_batch_fk CHARACTER VARYING(255),
    financial_transaction_fk CHARACTER VARYING(255) NOT NULL,
    transaction_state_fk CHARACTER VARYING(255) NOT NULL,
    transaction_response_fk CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255),
    transaction_type_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_future_payroll_data(
    company_seq CHARACTER VARYING(255) NOT NULL,
    payment_template_cd CHARACTER VARYING(255) NOT NULL,
    paycheck_month DOUBLE PRECISION,
    cnt DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_hiwh_agency_ids(
    old_agency_id CHARACTER VARYING(255),
    new_agency_id CHARACTER VARYING(255),
    source_company_id CHARACTER VARYING(50)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_mmt(
    money_movement_transaction_seq CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_orstt_agency_ids(
    source_company_id CHARACTER VARYING(50),
    company_seq CHARACTER VARYING(255),
    old_agency_id CHARACTER VARYING(255),
    new_agency_id CHARACTER VARYING(255),
    company_agency_fk CHARACTER VARYING(255),
    agency_taxpayer_id_enc CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_psp9932(
    company_fk CHARACTER VARYING(255),
    source_employee_fk CHARACTER VARYING(255),
    rate NUMERIC(19,7),
    source_description CHARACTER VARYING(4000),
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    rn DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_wc_all_paychecks(
    companytxnid CHARACTER VARYING(4000),
    sourcecompanyid CHARACTER VARYING(4000),
    sourcepaycheckid CHARACTER VARYING(4000),
    checkdate CHARACTER VARYING(4000),
    payperiodbegindate CHARACTER VARYING(4000),
    payperiodenddate CHARACTER VARYING(4000),
    processed CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_wc_paycheck_details(
    cid CHARACTER VARYING(4000),
    sourcecompanyid CHARACTER VARYING(4000),
    sourcepaycheckid CHARACTER VARYING(4000),
    payperiodbegindate CHARACTER VARYING(4000),
    payperiodenddate CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_wc_pck1(
    cid CHARACTER VARYING(4000),
    sourcecompanyid CHARACTER VARYING(4000),
    sourcepaycheckid CHARACTER VARYING(4000),
    payperiodbegindate CHARACTER VARYING(4000),
    payperiodenddate CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_wc_psp_active_pchk_dtls(
    sourcecompanyid CHARACTER VARYING(4000),
    sourcepaycheckid CHARACTER VARYING(4000),
    payperiodbegindate CHARACTER VARYING(4000),
    payperiodenddate CHARACTER VARYING(4000),
    legalname CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_wc_psp_paycheck_details(
    sourcecompanyid CHARACTER VARYING(4000),
    sourcepaycheckid CHARACTER VARYING(4000),
    payperiodbegindate CHARACTER VARYING(4000),
    payperiodenddate CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.temp_wc_psp_paycheck_dtls(
    sourcecompanyid CHARACTER VARYING(4000),
    sourcepaycheckid CHARACTER VARYING(4000),
    payperiodbegindate CHARACTER VARYING(4000),
    payperiodenddate CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.tempfts(
    source_company_id CHARACTER VARYING(50),
    financial_transaction_amount NUMERIC(19,4),
    transaction_type_fk CHARACTER VARYING(255) NOT NULL,
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    financial_transaction_seq CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.test(
    a NUMERIC(20,0)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.test1(
    a NUMERIC(38,0),
    b CHARACTER VARYING(10)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.test2_backup(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.test_scn(
    id DOUBLE PRECISION,
    dt TIMESTAMP(6) WITHOUT TIME ZONE,
    id_1 CHARACTER VARYING(20)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.test_table2(
    testcolumn1 CHARACTER VARYING(120),
    testcolumn1_enc CHARACTER VARYING(120)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.test_timezoneabc(
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_end_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.test_timezoneabc_old(
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_begin_date TIMESTAMP(6) WITHOUT TIME ZONE,
    pay_end_date TIMESTAMP(6) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.tmp_psp_edr(
    entry_detail_record_seq CHARACTER VARYING(255),
    modifier_id CHARACTER VARYING(255),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    n_a_c_h_a_file_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.tmp_psp_mmt(
    money_movement_transaction_seq CHARACTER VARYING(255) NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    initiation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    offload_batch_fk CHARACTER VARYING(255),
    status CHARACTER VARYING(255),
    tax_payment_status CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.tmp_psp_nachafile(
    nachafile_seq CHARACTER VARYING(255),
    modifier_id CHARACTER VARYING(255),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    transmission_date TIMESTAMP(6) WITHOUT TIME ZONE,
    file_name CHARACTER VARYING(255),
    status CHARACTER VARYING(255),
    offload_batch_fk CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.tmp_rpt(
    ledger_balance_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL,
    balance_amount NUMERIC(19,4),
    balance_date TIMESTAMP(6) WITHOUT TIME ZONE,
    ledger_account_fk CHARACTER VARYING(255) NOT NULL,
    company_fk CHARACTER VARYING(255) NOT NULL,
    reporting_type CHARACTER VARYING(255),
    report DOUBLE PRECISION,
    legal_name CHARACTER VARYING(100),
    fed_tax_id CHARACTER VARYING(100)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.toad_plan_sql(
    username CHARACTER VARYING(30),
    statement_id CHARACTER VARYING(32),
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    statement CHARACTER VARYING(2000)
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.toad_plan_table(
    statement_id CHARACTER VARYING(30),
    plan_id DOUBLE PRECISION,
    timestamp TIMESTAMP(0) WITHOUT TIME ZONE,
    remarks CHARACTER VARYING(4000),
    operation CHARACTER VARYING(30),
    options CHARACTER VARYING(255),
    object_node CHARACTER VARYING(128),
    object_owner CHARACTER VARYING(30),
    object_name CHARACTER VARYING(30),
    object_alias CHARACTER VARYING(65),
    object_instance NUMERIC(38,0),
    object_type CHARACTER VARYING(30),
    optimizer CHARACTER VARYING(255),
    search_columns DOUBLE PRECISION,
    id NUMERIC(38,0),
    parent_id NUMERIC(38,0),
    depth NUMERIC(38,0),
    position NUMERIC(38,0),
    cost NUMERIC(38,0),
    cardinality NUMERIC(38,0),
    bytes NUMERIC(38,0),
    other_tag CHARACTER VARYING(255),
    partition_start CHARACTER VARYING(255),
    partition_stop CHARACTER VARYING(255),
    partition_id NUMERIC(38,0),
    other TEXT,
    distribution CHARACTER VARYING(30),
    cpu_cost NUMERIC(38,0),
    io_cost NUMERIC(38,0),
    temp_space NUMERIC(38,0),
    access_predicates CHARACTER VARYING(4000),
    filter_predicates CHARACTER VARYING(4000),
    projection CHARACTER VARYING(4000),
    time NUMERIC(38,0),
    qblock_name CHARACTER VARYING(30),
    other_xml TEXT
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.tpr_metadata(
    transmission_payroll_run_seq CHARACTER VARYING(255) NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL
)
    PARTITION BY HASH (transmission_payroll_run_seq)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.track_progress(
    company_seq CHARACTER VARYING(255) NOT NULL,
    table_name CHARACTER VARYING(100) NOT NULL,
    is_backup_required INTEGER DEFAULT 1,
    is_backup_completed INTEGER DEFAULT 0,
    is_delete_required INTEGER DEFAULT 1,
    is_delete_completed INTEGER DEFAULT 0,
    is_revert_required INTEGER DEFAULT 1,
    is_revert_completed INTEGER DEFAULT 0,
    no_of_records DOUBLE PRECISION DEFAULT 0
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.tt(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.ttt(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.ttttt(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



