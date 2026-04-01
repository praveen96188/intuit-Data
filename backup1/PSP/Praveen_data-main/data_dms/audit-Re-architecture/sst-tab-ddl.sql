set search_path=ibobadm;

CREATE TABLE ibobadm.psp_source_system_transmission(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
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