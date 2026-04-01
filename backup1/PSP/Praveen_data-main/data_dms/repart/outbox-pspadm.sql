CREATE TABLE IF NOT EXISTS pspadm.outbox
(
    event_id                 uuid                        NOT NULL,
    topic                    VARCHAR(255)                NOT NULL,
    partition_key            VARCHAR(255)                NOT NULL,
    version                  INTEGER                     NOT NULL,
    utc_time                 TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    intuit_entity_type       VARCHAR(255)                NOT NULL,
    event_recorded_timestamp VARCHAR(32)                 NOT NULL,
    region                   VARCHAR(255)                NOT NULL,
    headers                  JSONB,
    payload                  JSONB,
    PRIMARY KEY (event_id, utc_time)
) PARTITION BY range(utc_time);

CREATE INDEX IX_intuit_entity_type ON pspadm.outbox(intuit_entity_type);
CREATE INDEX IX_event_recorded_timestamp ON pspadm.outbox(event_recorded_timestamp);

CREATE TABLE IF NOT EXISTS pspadm.outbox_heartbeat
(
    utc_time        TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC') PRIMARY KEY,
    is_realtime     BOOLEAN                     NOT NULL
);


CREATE ROLE data_capture_role WITH PASSWORD 'XXXX' LOGIN;
GRANT rds_replication TO data_capture_role;
GRANT CONNECT ON DATABASE ppdspg02 TO data_capture_role;
GRANT USAGE ON SCHEMA pspadm TO data_capture_role;
GRANT SELECT ON pspadm.outbox TO data_capture_role;
GRANT SELECT, INSERT, DELETE ON pspadm.outbox_heartbeat TO data_capture_role;




SELECT cron.schedule('outbox_partitioned_job', '20 22 * * *', $$SELECT partman.run_maintenance('pspadm.outbox')$$);
UPDATE cron.job SET database = 'ppdspg02' WHERE jobid = 34;


CREATE PUBLICATION data_capture_outbox_publication_e2e
    WITH (publish = 'insert');

SELECT partman.create_parent(
    p_parent_table => 'pspadm.outbox',
    p_control => 'utc_time',
    p_type => 'native',
    p_publications => ARRAY['data_capture_outbox_publication_e2e'],
    p_interval=> 'daily',
    p_start_partition=> (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')::text,
    p_premake => 2);

UPDATE partman.part_config
    SET infinite_time_partitions = true,
    retention = '7 days',
    retention_keep_table=false
    WHERE parent_table = 'pspadm.outbox';

ALTER PUBLICATION data_capture_outbox_publication_e2e
ADD TABLE pspadm.outbox_heartbeat;




SELECT * FROM cron.job_run_details WHERE jobid = '34';
SELECT * FROM cron.job;
select * from pg_publication_tables  where pubname='data_capture_outbox_publication_e2e';



alter table pspadm.psp_outbox 
add column outbox_seq CHARACTER VARYING(255) NOT NULL,
add column creator_id CHARACTER VARYING(30),
add column created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
add column modifier_id CHARACTER VARYING(30),
add column modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
add column realm_id SMALLINT NOT NULL DEFAULT - 1;


CREATE TABLE IF NOT EXISTS pspadm.psp_outbox
(
    event_id                 uuid                        NOT NULL,
    topic                    VARCHAR(255)                NOT NULL,
    partition_key            VARCHAR(255)                NOT NULL,
    version                  INTEGER                     NOT NULL,
    utc_time                 TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    intuit_entity_type       VARCHAR(255)                NOT NULL,
    event_recorded_timestamp VARCHAR(32)                 NOT NULL,
    region                   VARCHAR(255)                NOT NULL,
    headers                  JSONB,
    payload                  JSONB,
    outbox_seq CHARACTER VARYING(255) NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    PRIMARY KEY (event_id, utc_time)
) PARTITION BY range(utc_time);


CREATE INDEX IX_intuit_entity_type ON pspadm.psp_outbox(intuit_entity_type);
CREATE INDEX IX_event_recorded_timestamp ON pspadm.psp_outbox(event_recorded_timestamp);

SELECT cron.schedule('outbox_partitioned_job', '20 22 * * *', $$SELECT partman.run_maintenance('pspadm.psp_outbox')$$);
UPDATE cron.job SET database = 'ppdspg02' WHERE jobid = 68;--> SELECT * FROM cron.job;

GRANT CONNECT ON DATABASE postgres to pspadm_owner;
GRANT CREATE ON DATABASE postgres   TO pspadm_owner;
GRANT USAGE ON SCHEMA cron TO pspadm_owner;
grant select, update,delete, insert on cron.job to pspadm_owner;
GRANT CREATE ON SCHEMA partman TO pspadm_owner;
GRANT USAGE ON SCHEMA partman TO pspadm_owner;


GRANT SELECT ON pspadm.psp_outbox TO data_capture_role;
GRANT SELECT ON pspadm.pspadm_outbox TO data_capture_role;
GRANT SELECT, INSERT, DELETE ON pspadm.outbox_heartbeat TO data_capture_role;


ALTER PUBLICATION data_capture_outbox_publication_e2e
    ADD TABLE pspadm.psp_outbox;
    


alter default privileges in schema partman grant select, insert, update, delete on  tables to pspadm_owner;
GRANT UPDATE, INSERT, SELECT, DELETE ON ALL TABLES IN SCHEMA partman TO pspadm_owner;

select
  t.relname as table_name,
  i.relname as index_name,
  a.attname as column_name
from
  pg_class t,
  pg_class i,
  pg_index ix,
  pg_attribute a
where
    t.oid = ix.indrelid
  and i.oid = ix.indexrelid
  and a.attrelid = t.oid
  and a.attnum = ANY(ix.indkey)
  and t.relkind = 'r'
  and t.relname like 'psp_financial_transaction%'
order by
  t.relname,
  i.relname;


select  /*+ IndexScan(ft psp_financial_transaction_fk5) */  count(distinct(ft.payroll_run_fk)) as count, ft.company_fk  from PSP_FINANCIAL_TRANSACTION ft where ft.current_transaction_state_fk in ('Executed', 'Completed', 'Returned') and
ft.transaction_type_fk in ('EmployeeDdCredit', 'EmployerDdDebit', 'EmployerFeeDebit')   and created_date >= timestamp '2023-01-01 00:00:00.000000' and created_date <= timestamp '2023-12-31 00:00:00.000000'
group by ft.company_fk
order by count desc;
