--Audit

UPDATE psp_qbdt_request_info B
SET company_fk = A.company_id
FROM (
         SELECT /*+ Set(max_parallel_workers_per_gather 4)*/ source_system_transmission_seq, company_id
         FROM psp_source_system_transmission ai, psp_qbdt_request_info bi
         WHERE ai.CREATED_DATE between '2023-01-07 00:00:00' and '2023-01-07 03:00:00'
           and ai.source_system_transmission_seq = bi.source_system_transmission_fk
           AND ai.from_source_system = 'QBDT'
           and ai.type in ('PayrollSubmission', 'UsageSend','BalanceFile')
         and bi.company_fk is null
         LIMIT 10000
     ) AS A
WHERE B.source_system_transmission_fk = A.source_system_transmission_seq
  AND B.company_fk IS NULL;


--Monolith table

CREATE TABLE ibobadm.psp_qbdt_request_info(
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


--NO FK

--Index
--create_date
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

--New Index (company_fk, realm_id)
create index concurrently psp_qbdt_request_info_fk2_p0 on pspadm.psp_qbdt_request_info_p0 USING BTREE (company_fk, realm_id);
create index concurrently psp_qbdt_request_info_fk2_p1 on pspadm.psp_qbdt_request_info_p1 USING BTREE (company_fk, realm_id);
create index concurrently psp_qbdt_request_info_fk2_p2 on pspadm.psp_qbdt_request_info_p2 USING BTREE (company_fk, realm_id);
create index concurrently psp_qbdt_request_info_fk2_p3 on pspadm.psp_qbdt_request_info_p3 USING BTREE (company_fk, realm_id);

CREATE INDEX  psp_qbdt_request_info_fk2 ON ONLY pspadm.psp_qbdt_request_info USING BTREE (company_fk, realm_id);

alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p0 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p1 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p2 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p3 ;


--DMS validation

aws dms create-replication-task --replication-task-identifier validation-only-task --replication-task-settings '{"FullLoadSettings":{"TargetTablePrepMode":"DO_NOTHING"},"ValidationSettings":{"EnableValidation":true,"ValidationOnly":true}}' --replication-instance-arn
arn:aws:dms:us-east-1:xxxxxxxxxxx:rep:ABCDEFGH12346 --source-endpoint-arn arn:aws:dms:us-east-1:xxxxxxxxxxxx:endpoint:KSXGO6KATGOXBDZXKRV3QNIZV4 --target-endpoint-arn arn:aws:dms:us-east-1:xxxxxxxxxxxxxxx:endpoint:7SIYPBZTE2X3CZ7FPN7KKOAV6Q --migration-type
cdc --cdc-start-time "2022-06-08T 00:12:12" --table-mappings file://Table-mappings.json


--DMS validation

aws dms create-replication-task --replication-task-identifier validation-only-task --replication-task-settings '{"FullLoadSettings":{"TargetTablePrepMode":"DO_NOTHING"},"ValidationSettings":{"EnableValidation":true,"ValidationOnly":true}}' --replication-instance-arn
arn:aws:dms:us-east-1:xxxxxxxxxxx:rep:ABCDEFGH12346 --source-endpoint-arn arn:aws:dms:us-east-1:xxxxxxxxxxxx:endpoint:KSXGO6KATGOXBDZXKRV3QNIZV4 --target-endpoint-arn arn:aws:dms:us-east-1:xxxxxxxxxxxxxxx:endpoint:7SIYPBZTE2X3CZ7FPN7KKOAV6Q --migration-type
cdc --cdc-start-time "2022-06-08T 00:12:12" --table-mappings file://Table-mappings.json
