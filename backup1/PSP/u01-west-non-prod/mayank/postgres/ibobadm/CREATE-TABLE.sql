-- ------------ Write CREATE-TABLE-stage scripts -----------

CREATE TABLE ibobadm.gg_heartbeat(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.gg_heartbeat_smc(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.gg_heartbeat_sst(
    source CHARACTER VARYING(20) NOT NULL,
    last_update TIMESTAMP(0) WITHOUT TIME ZONE
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.psp_qbdt_request_info(
    qbdt_request_info_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.psp_sap_method_call(
    sap_method_call_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0),
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
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
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.psp_source_system_transmission(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version NUMERIC(19,0) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id NUMERIC(19,0) NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.test1(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE ibobadm.test2(
    id DOUBLE PRECISION
)
        WITH (
        OIDS=FALSE
        );



