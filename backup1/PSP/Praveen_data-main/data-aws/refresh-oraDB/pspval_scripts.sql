



create table oracle_range_cdc_event (
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
);

create index idx_oracle_range_cdc_event_entity_id on oracle_range_cdc_event(entity_id);
create index idx_oracle_range_cdc_event_entity_id_status on oracle_range_cdc_event(entity_id, status);
create index idx_oracle_range_cdc_event_transaction_id_status on oracle_range_cdc_event(transaction_id, status);

create table oracle_hash_cdc_event (
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
);


create index idx_oracle_hash_cdc_event_entity_id on oracle_hash_cdc_event(entity_id);
create index idx_oracle_hash_cdc_event_entity_id_status on oracle_hash_cdc_event(entity_id, status);
create index idx_oracle_hash_cdc_event_transaction_id_status on oracle_hash_cdc_event(transaction_id, status);

create table postgres_hash_cdc_event (
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
);


create index idx_postgres_hash_cdc_event_entity_id on postgres_hash_cdc_event(entity_id);
create index idx_postgres_hash_cdc_event_entity_id_status on postgres_hash_cdc_event(entity_id, status);
create index idx_postgres_hash_cdc_event_transaction_id_status on postgres_hash_cdc_event(transaction_id, status);

create table compare_info_cdc_event (
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

create index status_initial_ix on pspval.compare_info_cdc_event (status) where status='initial';