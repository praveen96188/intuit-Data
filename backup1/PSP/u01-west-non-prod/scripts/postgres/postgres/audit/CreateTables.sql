DROP TABLE IF EXISTS psp_sap_method_call CASCADE;

CREATE TABLE psp_sap_method_call
(
    PRIMARY KEY(sap_method_call_seq,realm_id),
    sap_method_call_seq CHARACTER VARYING(255) NOT NULL,
    version SMALLINT,
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

DROP TABLE IF EXISTS psp_source_system_transmission CASCADE;

CREATE TABLE psp_source_system_transmission
(
    PRIMARY KEY(source_system_transmission_seq,realm_id),
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
);

DROP TABLE IF EXISTS psp_qbdt_request_info CASCADE;

CREATE TABLE psp_qbdt_request_info
(
    PRIMARY KEY(qbdt_request_info_seq,realm_id),
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
    paycheck_delete_count BIGINT
);

DROP TABLE IF EXISTS psp_hcm401k_company_policy CASCADE;

CREATE TABLE psp_hcm401k_company_policy
(
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
);

DROP TABLE IF EXISTS psp_hcm401k_company_qbdt_pitem CASCADE;

CREATE TABLE psp_hcm401k_company_qbdt_pitem
(
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
);

DROP TABLE IF EXISTS psp_hcm401k_employee_deduction CASCADE;

CREATE TABLE psp_hcm401k_employee_deduction
(
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
);

DROP TABLE IF EXISTS psp_hcm401k_policy CASCADE;

CREATE TABLE psp_hcm401k_policy
(
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
);

ALTER TABLE psp_qbdt_request_info
ADD CONSTRAINT psp_qbdt_request_info_fk1
FOREIGN KEY (source_system_transmission_fk,realm_id)
REFERENCES psp_source_system_transmission;

DO
$$
BEGIN
   IF NOT EXISTS
     (select
         COUNT(attr.attname)
     from
         pg_class clss,
         pg_class cls,
         pg_index ind,
         pg_attribute attr
     where
         clss.oid = ind.indrelid
         and cls.oid = ind.indexrelid
         and attr.attrelid = clss.oid
         and attr.attnum = ANY(ind.indkey)
         and clss.relkind = 'r'
         and cls.relname in
         (select
             index_rel.relname as index_name
         from
             pg_class table_rel,
             pg_class index_rel,
             pg_index index,
             pg_attribute col
         where
             table_rel.oid = index.indrelid
             and index_rel.oid = index.indexrelid
             and col.attrelid = table_rel.oid
             and col.attnum = ANY(index.indkey)
             and table_rel.relname = 'psp_qbdt_request_info'
             and col.attname = 'source_system_transmission_fk')
     GROUP BY
     cls.relname HAVING COUNT(attr.attname) = 1)
   THEN
CREATE INDEX psp_qbdt_request_info_fk1 ON psp_qbdt_request_info(source_system_transmission_fk, realm_id);
END IF;
END
$$
;

DO
$$
BEGIN
   IF NOT EXISTS
     (select
         COUNT(attr.attname)
     from
         pg_class clss,
         pg_class cls,
         pg_index ind,
         pg_attribute attr
     where
         clss.oid = ind.indrelid
         and cls.oid = ind.indexrelid
         and attr.attrelid = clss.oid
         and attr.attnum = ANY(ind.indkey)
         and clss.relkind = 'r'
         and cls.relname in
         (select
             index_rel.relname as index_name
         from
             pg_class table_rel,
             pg_class index_rel,
             pg_index index,
             pg_attribute col
         where
             table_rel.oid = index.indrelid
             and index_rel.oid = index.indexrelid
             and col.attrelid = table_rel.oid
             and col.attnum = ANY(index.indkey)
             and table_rel.relname = 'psp_hcm401k_company_policy'
             and col.attname = 'hcm401k_policy_fk')
     GROUP BY
     cls.relname HAVING COUNT(attr.attname) = 1)
   THEN
CREATE INDEX psp_hcm401k_company_policy_fk1 ON psp_hcm401k_company_policy(hcm401k_policy_fk, realm_id);
END IF;
END
$$
;

DO
$$
BEGIN
   IF NOT EXISTS
     (select
         COUNT(attr.attname)
     from
         pg_class clss,
         pg_class cls,
         pg_index ind,
         pg_attribute attr
     where
         clss.oid = ind.indrelid
         and cls.oid = ind.indexrelid
         and attr.attrelid = clss.oid
         and attr.attnum = ANY(ind.indkey)
         and clss.relkind = 'r'
         and cls.relname in
         (select
             index_rel.relname as index_name
         from
             pg_class table_rel,
             pg_class index_rel,
             pg_index index,
             pg_attribute col
         where
             table_rel.oid = index.indrelid
             and index_rel.oid = index.indexrelid
             and col.attrelid = table_rel.oid
             and col.attnum = ANY(index.indkey)
             and table_rel.relname = 'psp_hcm401k_company_qbdt_pitem'
             and col.attname = 'hcm401k_company_policy_fk')
     GROUP BY
     cls.relname HAVING COUNT(attr.attname) = 1)
   THEN
CREATE INDEX psp_hcm401k_company_qbdt_p_fk1 ON psp_hcm401k_company_qbdt_pitem(hcm401k_company_policy_fk, realm_id);
END IF;
END
$$
;

DO
$$
BEGIN
   IF NOT EXISTS
     (select
         COUNT(attr.attname)
     from
         pg_class clss,
         pg_class cls,
         pg_index ind,
         pg_attribute attr
     where
         clss.oid = ind.indrelid
         and cls.oid = ind.indexrelid
         and attr.attrelid = clss.oid
         and attr.attnum = ANY(ind.indkey)
         and clss.relkind = 'r'
         and cls.relname in
         (select
             index_rel.relname as index_name
         from
             pg_class table_rel,
             pg_class index_rel,
             pg_index index,
             pg_attribute col
         where
             table_rel.oid = index.indrelid
             and index_rel.oid = index.indexrelid
             and col.attrelid = table_rel.oid
             and col.attnum = ANY(index.indkey)
             and table_rel.relname = 'psp_hcm401k_employee_deduction'
             and col.attname = 'hcm401k_company_policy_fk')
     GROUP BY
     cls.relname HAVING COUNT(attr.attname) = 1)
   THEN
CREATE INDEX psp_hcm401k_employee_deduc_fk1 ON psp_hcm401k_employee_deduction(hcm401k_company_policy_fk, realm_id);
END IF;
END
$$
;
