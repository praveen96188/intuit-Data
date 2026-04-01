
--DMS surce and target in  Audit DB.


--Create User source Postgres

CREATE USER  dms_apg_src WITH PASSWORD 'dms#123';
GRANT CONNECT ON DATABASE pdsibobdb to dms_apg_src;
GRANT CREATE ON DATABASE pdsibobdb  TO dms_apg_src;
GRANT USAGE ON SCHEMA ibobadm_pds TO dms_apg_src;
Grant  rds_superuser to dms_apg_src;
Grant  rds_replication to dms_apg_src;
--connect dms_apg_user and create schema
Create schema awsdms_ddl_audit;
GRANT USAGE ON SCHEMA awsdms_ddl_audit TO dms_apg_src;
CREATE TABLE awsdms_ddl_audit.awsdms_ddl_audit
(
  c_key    bigserial primary key,
  c_time   timestamp,    -- Informational
  c_user   varchar(64),  -- Informational: current_user
  c_txn    varchar(16),  -- Informational: current transaction
  c_tag    varchar(24),  -- Either 'CREATE TABLE' or 'ALTER TABLE' or 'DROP TABLE'
  c_oid    integer,      -- For future use - TG_OBJECTID
  c_name   varchar(64),  -- For future use - TG_OBJECTNAME
  c_schema varchar(64),  -- For future use - TG_SCHEMANAME. For now - holds current_schema
  c_ddlqry  text         -- The DDL query associated with the current DDL event
);
                                               
CREATE OR REPLACE FUNCTION awsdms_ddl_audit.awsdms_intercept_ddl()
  RETURNS event_trigger
LANGUAGE plpgsql
SECURITY DEFINER
  AS $$
  declare _qry text;
BEGIN
  if (tg_tag='CREATE TABLE' or tg_tag='ALTER TABLE' or tg_tag='DROP TABLE') then
         SELECT current_query() into _qry;
         insert into awsdms_ddl_audit.awsdms_ddl_audit
         values
         (
         default,current_timestamp,current_user,cast(TXID_CURRENT()as varchar(16)),tg_tag,0,'',current_schema,_qry
         );
         delete from awsdms_ddl_audit.awsdms_ddl_audit;
end if;
END;
$$;
                        
CREATE EVENT TRIGGER awsdms_intercept_ddl ON ddl_command_end 
EXECUTE PROCEDURE awsdms_ddl_audit.awsdms_intercept_ddl();

grant all on awsdms_ddl_audit.awsdms_ddl_audit to public;
grant all on awsdms_ddl_audit.awsdms_ddl_audit_c_key_seq to public;


--DMS Target Postgres



CREATE USER  dms_apg_tgt WITH PASSWORD 'dms#123';
GRANT SET ON PARAMETER session_replication_role TO dms_apg_tgt;
GRANT CONNECT ON DATABASE pdsibobdb to dms_apg_tgt;
GRANT CREATE ON DATABASE pdsibobdb TO dms_apg_tgt;
GRANT CREATE ON SCHEMA ibobadm_pds TO dms_apg_tgt;
GRANT USAGE ON SCHEMA ibobadm_pds TO dms_apg_tgt;
alter default privileges in schema ibobadm_pds grant select, insert, update, delete on tables to dms_apg_tgt;
GRANT UPDATE, INSERT, SELECT, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA ibobadm_pds TO dms_apg_tgt;



---DDL in Monolith



select CURRENT_TIMESTAMP;



CREATE TABLE pspadm.psp_hcm401k_company_policy(
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

CREATE TABLE pspadm.psp_hcm401k_company_qbdt_pitem(
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

CREATE TABLE pspadm.psp_hcm401k_employee_deduction(
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

CREATE TABLE pspadm.psp_hcm401k_policy(
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


CREATE TABLE pspadm.psp_qbdt_request_info(
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

--partition tables

CREATE TABLE pspadm.psp_qbdt_request_info_p0 PARTITION OF pspadm.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE pspadm.psp_qbdt_request_info_p1 PARTITION OF pspadm.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 1);
CREATE TABLE pspadm.psp_qbdt_request_info_p2 PARTITION OF pspadm.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 2);
CREATE TABLE pspadm.psp_qbdt_request_info_p3 PARTITION OF pspadm.psp_qbdt_request_info FOR VALUES WITH (MODULUS 4, REMAINDER 3);

--PK

ALTER TABLE pspadm.psp_qbdt_request_info ADD PRIMARY KEY (company_fk,qbdt_request_info_seq);


---Primary-key

ALTER TABLE pspadm.psp_hcm401k_company_policy
ADD PRIMARY KEY (hcm401k_company_policy_seq, realm_id);

ALTER TABLE pspadm.psp_hcm401k_company_qbdt_pitem
ADD PRIMARY KEY (hcm401k_company_qbdt_pitem_seq, realm_id);

ALTER TABLE pspadm.psp_hcm401k_employee_deduction
ADD PRIMARY KEY (hcm401k_employee_deduction_seq, realm_id);

ALTER TABLE pspadm.psp_hcm401k_policy
ADD PRIMARY KEY (hcm401k_policy_seq, realm_id);




--FK

ALTER TABLE pspadm.psp_hcm401k_company_policy
ADD CONSTRAINT psp_hcm401k_company_policy_fk1 FOREIGN KEY (hcm401k_policy_fk, realm_id) 
REFERENCES pspadm.psp_hcm401k_policy (hcm401k_policy_seq, realm_id)
ON DELETE NO ACTION;

ALTER TABLE pspadm.psp_hcm401k_company_qbdt_pitem
ADD CONSTRAINT psp_hcm401k_company_qbdt_p_fk1 FOREIGN KEY (hcm401k_company_policy_fk, realm_id) 
REFERENCES pspadm.psp_hcm401k_company_policy (hcm401k_company_policy_seq, realm_id)
ON DELETE NO ACTION;

ALTER TABLE pspadm.psp_hcm401k_employee_deduction
ADD CONSTRAINT psp_hcm401k_employee_deduc_fk1 FOREIGN KEY (hcm401k_company_policy_fk, realm_id) 
REFERENCES pspadm.psp_hcm401k_company_policy (hcm401k_company_policy_seq, realm_id)
ON DELETE NO ACTION;


--Indexes

CREATE INDEX psp_hcm401k_company_policy_fk1
ON pspadm.psp_hcm401k_company_policy
USING BTREE (hcm401k_policy_fk , realm_id );

CREATE INDEX psp_hcm401k_company_qbdt_p_fk1
ON pspadm.psp_hcm401k_company_qbdt_pitem
USING BTREE (hcm401k_company_policy_fk , realm_id );

CREATE INDEX psp_hcm401k_employee_deduc_fk1
ON pspadm.psp_hcm401k_employee_deduction
USING BTREE (hcm401k_company_policy_fk , realm_id );



--create_date Index
create index concurrently psp_qbdt_request_info_crdt_p0 on pspadm.psp_qbdt_request_info_p0 USING BTREE (created_date);
create index concurrently psp_qbdt_request_info_crdt_p1 on pspadm.psp_qbdt_request_info_p1 USING BTREE (created_date);
create index concurrently psp_qbdt_request_info_crdt_p2 on pspadm.psp_qbdt_request_info_p2 USING BTREE (created_date);
create index concurrently psp_qbdt_request_info_crdt_p3 on pspadm.psp_qbdt_request_info_p3 USING BTREE (created_date);


CREATE INDEX psp_qbdt_request_info_crdt ON ONLY pspadm.psp_qbdt_request_info USING BTREE (created_date );

alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p0 ;
alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p1 ;
alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p2 ;
alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p3 ;

--source_system_transmission_fk , realm_id
create index concurrently psp_qbdt_request_info_fk1_p0 on pspadm.psp_qbdt_request_info_p0 USING BTREE (source_system_transmission_fk , realm_id);
create index concurrently psp_qbdt_request_info_fk1_p1 on pspadm.psp_qbdt_request_info_p1 USING BTREE (source_system_transmission_fk , realm_id);
create index concurrently psp_qbdt_request_info_fk1_p2 on pspadm.psp_qbdt_request_info_p2 USING BTREE (source_system_transmission_fk , realm_id);
create index concurrently psp_qbdt_request_info_fk1_p3 on pspadm.psp_qbdt_request_info_p3 USING BTREE (source_system_transmission_fk , realm_id);

CREATE INDEX psp_qbdt_request_info_fk1 ON ONLY pspadm.psp_qbdt_request_info USING BTREE (source_system_transmission_fk , realm_id );

alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p0 ;
alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p1 ;
alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p2 ;
alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p3 ;

--New Index (company_fk)
create index concurrently psp_qbdt_request_info_fk2_p0 on pspadm.psp_qbdt_request_info_p0 USING BTREE (company_fk);
create index concurrently psp_qbdt_request_info_fk2_p1 on pspadm.psp_qbdt_request_info_p1 USING BTREE (company_fk);
create index concurrently psp_qbdt_request_info_fk2_p2 on pspadm.psp_qbdt_request_info_p2 USING BTREE (company_fk);
create index concurrently psp_qbdt_request_info_fk2_p3 on pspadm.psp_qbdt_request_info_p3 USING BTREE (company_fk);

CREATE INDEX  psp_qbdt_request_info_fk2 ON ONLY pspadm.psp_qbdt_request_info USING BTREE (company_fk);

alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p0 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p1 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p2 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p3 ;




--Reverse replication.
--Audit Database
create schema temp;

alter table ibobadm.gg_heartbeat SET SCHEMA temp ;
alter table ibobadm.psp_qbdt_request_info SET SCHEMA temp;
alter table ibobadm.psp_hcm401k_company_policy SET SCHEMA temp;
alter table ibobadm.psp_hcm401k_company_qbdt_pitem SET SCHEMA temp;
alter table ibobadm.psp_hcm401k_employee_deduction SET SCHEMA temp;
alter table ibobadm.psp_hcm401k_policy SET SCHEMA temp ;





Ensure there are no active connections to the Postgres Hash Database.

SELECT datname,usename, coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,count(*)
from pg_stat_activity
where pid != pg_backend_pid()
group by datname,usename, coalesce(client_hostname, client_addr::text, 'localhost')
order by usename;

(Kill Connections if needed)


select pg_terminate_backend(pid)
from pg_stat_activity
where pid in (select pid
              FROM pg_stat_activity
              where usename like '%psp%');


wait till  audit to monolith DMS replication lag 0


stop and Delete Audit to Monolith DMS replication tasks from console.


--Connect Audit 
--Create new schema.
change schema for 5 tables and gg_heartbeat table
create schema ibobadm_temp;

alter table ibobadm_temp.gg_heartbeat SET SCHEMA temp ;
alter table ibobadm_temp.psp_qbdt_request_info SET SCHEMA temp;
alter table ibobadm_temp.psp_hcm401k_company_policy SET SCHEMA temp;
alter table ibobadm_temp.psp_hcm401k_company_qbdt_pitem SET SCHEMA temp;
alter table ibobadm_temp.psp_hcm401k_employee_deduction SET SCHEMA temp;
alter table ibobadm_temp.psp_hcm401k_policy SET SCHEMA temp ;











