\timing
set search_path=pspval;


create table pspval.postgres_hash_cdc_event (
   id SERIAL not null,
   created_date TIMESTAMP not null,
   modified_date TIMESTAMP not null,
   transaction_id varchar(255) not null,
   table_name varchar(255) not null,
   operation varchar(20) not null,
   initial_data text ,
   initial_data_hash varchar(4000) ,
   change_data text ,
   change_data_hash varchar(4000) ,
   event_modifier_id varchar(255),
   event_modified_date TIMESTAMP,
   status varchar(60) not null,
   company_id varchar(255),
   entity_id varchar(255) not null,
   primary key (id)
)   
      PARTITION BY Hash (id);

-- create partitions
CREATE TABLE pspadm.postgres_hash_cdc_event_p0 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE pspadm.postgres_hash_cdc_event_p1 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE pspadm.postgres_hash_cdc_event_p2 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE pspadm.postgres_hash_cdc_event_p3 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE pspadm.postgres_hash_cdc_event_p4 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE pspadm.postgres_hash_cdc_event_p5 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE pspadm.postgres_hash_cdc_event_p6 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE pspadm.postgres_hash_cdc_event_p7 PARTITION OF pspadm.postgres_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 7);



create table pspval.oracle_hash_cdc_event (
   id serial not null,
   created_date timestamp not null,
   modified_date timestamp not null,
   transaction_id varchar(255) not null,
   table_name varchar(255) not null,
   operation varchar(20) not null,
   initial_data text ,
   initial_data_hash varchar(4000) ,
   change_data text ,
   change_data_hash varchar(4000) ,
   event_modifier_id varchar(255),
   event_modified_date timestamp,
   status varchar(60) not null,
   company_id varchar(255),
   entity_id varchar(255) not null,
   primary key (id)
)  
     PARTITION BY Hash (id);

CREATE TABLE pspadm.oracle_hash_cdc_event_p0 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE pspadm.oracle_hash_cdc_event_p1 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE pspadm.oracle_hash_cdc_event_p2 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE pspadm.oracle_hash_cdc_event_p3 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE pspadm.oracle_hash_cdc_event_p4 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE pspadm.oracle_hash_cdc_event_p5 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE pspadm.oracle_hash_cdc_event_p6 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE pspadm.oracle_hash_cdc_event_p7 PARTITION OF pspadm.oracle_hash_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 7);


create table pspval.oracle_range_cdc_event (
   id serial not null,
   created_date timestamp not null,
   modified_date timestamp not null,
   transaction_id varchar(255) not null,
   table_name varchar(255) not null,
   operation varchar(20) not null,
   initial_data text ,
   initial_data_hash varchar(4000) ,
   change_data text ,
   change_data_hash varchar(4000) ,
   event_modifier_id varchar(255),
   event_modified_date timestamp,
   status varchar(60) not null,
   company_id varchar(255),
   entity_id varchar(255) not null,
   primary key (id)
)  
      PARTITION BY Hash (id);

CREATE TABLE pspadm.oracle_range_cdc_event_p0 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE pspadm.oracle_range_cdc_event_p1 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE pspadm.oracle_range_cdc_event_p2 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE pspadm.oracle_range_cdc_event_p3 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE pspadm.oracle_range_cdc_event_p4 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE pspadm.oracle_range_cdc_event_p5 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE pspadm.oracle_range_cdc_event_p6 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE pspadm.oracle_range_cdc_event_p7 PARTITION OF pspadm.oracle_range_cdc_event FOR VALUES WITH (MODULUS 8, REMAINDER 7);



create table pspval.compare_info_cdc_event (
    id serial not null,
    batch_job_name varchar(255) not null,
    source_database_type varchar(255),
    source_tx_ids text ,
    source_modified_from timestamp,
    source_modified_to timestamp,
    source_modifier_id varchar(255),
    target_database_type varchar(255),
    target_tx_ids text ,
    target_modified_from timestamp,
    target_modified_to timestamp,
    target_modifier_id varchar(255),
    status varchar(60) not null,
    workflow varchar(255),
    created_date timestamp,
    modified_date timestamp,
    retry_count integer,
    primary key (id)
);





