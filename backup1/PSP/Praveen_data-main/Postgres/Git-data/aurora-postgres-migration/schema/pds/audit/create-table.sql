-- ------------ Write CREATE-TABLE-stage scripts -----------

CREATE TABLE ibobadm_pds.psp_hcm401k_company_policy(
    hcm401k_company_policy_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    company_id CHARACTER VARYING(4000),
    active SMALLINT,
    hcm401k_policy_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );

CREATE TABLE ibobadm_pds.psp_hcm401k_company_qbdt_pitem(
    hcm401k_company_qbdt_pitem_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    qbdt_pitem_id CHARACTER VARYING(4000),
    company_payroll_item_id CHARACTER VARYING(4000),
    hcm401k_contributor CHARACTER VARYING(255),
    hcm401k_company_policy_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );

CREATE TABLE ibobadm_pds.psp_hcm401k_employee_deduction(
    hcm401k_employee_deduction_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    employee_id CHARACTER VARYING(4000),
    amount NUMERIC(19,7),
    hcm401k_amount_type CHARACTER VARYING(255),
    max_amount NUMERIC(19,7),
    hcm401k_deduction_contributor CHARACTER VARYING(255),
    active SMALLINT,
    hcm401k_company_policy_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );

CREATE TABLE ibobadm_pds.psp_hcm401k_policy(
    hcm401k_policy_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    deduction_item_policy CHARACTER VARYING(255),
    description CHARACTER VARYING(4000),
    deduction_item_provider CHARACTER VARYING(255)
)
        WITH (
        OIDS=FALSE
        );

CREATE TABLE ibobadm_pds.psp_qbdt_request_info(
    qbdt_request_info_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    employee_add_count BIGINT,
    employee_update_count BIGINT,
    employee_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_add_count BIGINT,
    paycheck_update_count BIGINT,
    payroll_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_count BIGINT,
    payroll_item_update_count BIGINT,
    payroll_item_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_delete_count BIGINT,
    payroll_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_trans_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_delete_count BIGINT,
    payroll_trans_delete_count BIGINT,
    delete_processing_start TIMESTAMP(6) WITHOUT TIME ZONE,
    delete_processing_end TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_item_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_count BIGINT,
    payroll_trans_update_count BIGINT,
    payroll_trans_update_start TIMESTAMP(6) WITHOUT TIME ZONE,
    payroll_transaction_add_start TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_add_end TIMESTAMP(6) WITHOUT TIME ZONE,
    employee_update_end TIMESTAMP(6) WITHOUT TIME ZONE,
    paycheck_delete_count BIGINT,
    source_system_transmission_fk CHARACTER VARYING(255) NOT NULL
)
        WITH (
        OIDS=FALSE
        );

CREATE TABLE ibobadm_pds.psp_sap_method_call(
    sap_method_call_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    screen_path CHARACTER VARYING(400),
    service_name CHARACTER VARYING(4000),
    method_name CHARACTER VARYING(4000),
    parameters CHARACTER VARYING(4000),
    result_size NUMERIC(19,0),
    elapsed_millis NUMERIC(19,0),
    exception_trace CHARACTER VARYING(4000),
    security_principal CHARACTER VARYING(4000),
    session_id CHARACTER VARYING(4000),
    host CHARACTER VARYING(200)
)
        WITH (
        OIDS=FALSE
        );

CREATE TABLE ibobadm_pds.psp_source_system_transmission(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    host CHARACTER VARYING(200),
    from_source_system CHARACTER VARYING(255),
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document TEXT,
    response_document TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    company_id CHARACTER VARYING(4000)
)
        WITH (
        OIDS=FALSE
        );
