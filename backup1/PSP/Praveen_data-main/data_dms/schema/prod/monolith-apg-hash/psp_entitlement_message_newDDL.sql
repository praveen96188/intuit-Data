

DMS Non-prod repartitioning;

1.stop replication from B-C3 and C3-D3(ensure lag 0) task6
2.rename existing table to _old  (psp_entitlement_message  to  psp_entitlement_message_old)
3.create new tables with actual names (psp_entitlement_message)
4.copy data from old to new tables
5.rename existing indexes into _old
6.create new indexes on actual tables 
7.no change in B-C3 replication task6
8.C3-D3, remove paycheck_usage and pem from task6 and resumed
9.C3-D3, create new task6a for paycheck_usage and pem task6a
10.start task C3-D3(successfully started no issue)
11.resumed B-C3(failed) task6


--staging

Copy data from old table new table in postgres hash estimations
Psp_paycheck_usage: 4hrs
Index creation on paycheck_usage: 2:30hrs
Psp_entitlement_message: 8-9hrs
Index creation on pse: 30min

--Rename 
--rename table to _old
set search_path=pspadm;
alter table pspadm.psp_entitlement_message RENAME to psp_entitlement_message_old;
alter table pspadm.psp_entitlement_message_2012 RENAME to psp_entitlement_message_2012_old;
alter table pspadm.psp_entitlement_message_2013 RENAME to psp_entitlement_message_2013_old;
alter table pspadm.psp_entitlement_message_2014 RENAME to psp_entitlement_message_2014_old;
alter table pspadm.psp_entitlement_message_2015 RENAME to psp_entitlement_message_2015_old;
alter table pspadm.psp_entitlement_message_2016 RENAME to psp_entitlement_message_2016_old;
alter table pspadm.psp_entitlement_message_2017 RENAME to psp_entitlement_message_2017_old;
alter table pspadm.psp_entitlement_message_2018 RENAME to psp_entitlement_message_2018_old;
alter table pspadm.psp_entitlement_message_2019 RENAME to psp_entitlement_message_2019_old;
alter table pspadm.psp_entitlement_message_2020 RENAME to psp_entitlement_message_2020_old;
alter table pspadm.psp_entitlement_message_2021 RENAME to psp_entitlement_message_2021_old;
alter table pspadm.psp_entitlement_message_2022 RENAME to psp_entitlement_message_2022_old;
alter table pspadm.psp_entitlement_message_2023 RENAME to psp_entitlement_message_2023_old;
alter table pspadm.psp_entitlement_message_2024 RENAME to psp_entitlement_message_2024_old;


--create table 

CREATE TABLE pspadm.psp_entitlement_message(
    entitlement_message_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    entitlement_offering_code CHARACTER VARYING(20),
    order_number CHARACTER VARYING(20),
    message_bkp TEXT,
    license_number CHARACTER VARYING(20),
    status CHARACTER VARYING(255),
    token NUMERIC(19,0),
    event_reason CHARACTER VARYING(50),
    failure_count integer,
    last_failure_message CHARACTER VARYING(1000),
    message_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    message TEXT,
    message_enc TEXT
) 
   PARTITION BY Hash (license_number);


CREATE TABLE pspadm.psp_entitlement_message_p0 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 0);
CREATE TABLE pspadm.psp_entitlement_message_p1 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 1);
CREATE TABLE pspadm.psp_entitlement_message_p2 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 2);
CREATE TABLE pspadm.psp_entitlement_message_p3 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 3);
CREATE TABLE pspadm.psp_entitlement_message_p4 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 4);
CREATE TABLE pspadm.psp_entitlement_message_p5 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 5);
CREATE TABLE pspadm.psp_entitlement_message_p6 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 6);
CREATE TABLE pspadm.psp_entitlement_message_p7 PARTITION OF pspadm.psp_entitlement_message FOR VALUES WITH (MODULUS 8, REMAINDER 7);

ALTER TABLE pspadm.psp_entitlement_message ADD CONSTRAINT psp_entitlement_message_pk PRIMARY KEY (license_number, entitlement_message_seq);


--copy data from _old to new

set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2012_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2013_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2014_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2015_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2016_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2017_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2018_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2019_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2020_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2021_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2022_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2023_old;
Insert into pspadm.psp_entitlement_message select * from pspadm.psp_entitlement_message_2024_old;
SELECT CURRENT_TIMESTAMP;




--rename index to old

Alter index psp_entitlement_message_u1 RENAME TO psp_entitlement_message_u1_old;
Alter index psp_entitlement_msg_lcno_eofcd RENAME TO psp_entitlement_msg_lcno_eofcd_old;
Alter index psp_entitlement_msg_msgts_er RENAME TO psp_entitlement_msg_msgts_er_old;
Alter index psp_entitlement_msg_orno_lcno  RENAME TO psp_entitlement_msg_orno_lcno_old;
Alter index idx_ent_msg_mod_date  RENAME TO idx_ent_msg_mod_date_old; 

\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

select 'creating index on psp_entitlement_message' as status ;

create index concurrently psp_entitlement_message_u1_p0 on pspadm.psp_entitlement_message_p0  USING BTREE (token );
create index concurrently psp_entitlement_message_u1_p1 on pspadm.psp_entitlement_message_p1  USING BTREE (token );
create index concurrently psp_entitlement_message_u1_p2 on pspadm.psp_entitlement_message_p2  USING BTREE (token );
create index concurrently psp_entitlement_message_u1_p3 on pspadm.psp_entitlement_message_p3  USING BTREE (token );
create index concurrently psp_entitlement_message_u1_p4 on pspadm.psp_entitlement_message_p4  USING BTREE (token );
create index concurrently psp_entitlement_message_u1_p5 on pspadm.psp_entitlement_message_p5  USING BTREE (token );
create index concurrently psp_entitlement_message_u1_p6 on pspadm.psp_entitlement_message_p6  USING BTREE (token );
create index concurrently psp_entitlement_message_u1_p7 on pspadm.psp_entitlement_message_p7  USING BTREE (token );

create index concurrently psp_entitlement_msg_lcno_eofcd_p0 on pspadm.psp_entitlement_message_p0  USING BTREE (license_number ,entitlement_offering_code );
create index concurrently psp_entitlement_msg_lcno_eofcd_p1 on pspadm.psp_entitlement_message_p1  USING BTREE (license_number ,entitlement_offering_code );
create index concurrently psp_entitlement_msg_lcno_eofcd_p2 on pspadm.psp_entitlement_message_p2  USING BTREE (license_number ,entitlement_offering_code );
create index concurrently psp_entitlement_msg_lcno_eofcd_p3 on pspadm.psp_entitlement_message_p3  USING BTREE (license_number ,entitlement_offering_code );
create index concurrently psp_entitlement_msg_lcno_eofcd_p4 on pspadm.psp_entitlement_message_p4  USING BTREE (license_number ,entitlement_offering_code );
create index concurrently psp_entitlement_msg_lcno_eofcd_p5 on pspadm.psp_entitlement_message_p5  USING BTREE (license_number ,entitlement_offering_code );
create index concurrently psp_entitlement_msg_lcno_eofcd_p6 on pspadm.psp_entitlement_message_p6  USING BTREE (license_number ,entitlement_offering_code );
create index concurrently psp_entitlement_msg_lcno_eofcd_p7 on pspadm.psp_entitlement_message_p7  USING BTREE (license_number ,entitlement_offering_code );

create index concurrently psp_entitlement_msg_msgts_er_p0 on pspadm.psp_entitlement_message_p0  USING BTREE (message_timestamp ,event_reason );
create index concurrently psp_entitlement_msg_msgts_er_p1 on pspadm.psp_entitlement_message_p1  USING BTREE (message_timestamp ,event_reason );
create index concurrently psp_entitlement_msg_msgts_er_p2 on pspadm.psp_entitlement_message_p2  USING BTREE (message_timestamp ,event_reason );
create index concurrently psp_entitlement_msg_msgts_er_p3 on pspadm.psp_entitlement_message_p3  USING BTREE (message_timestamp ,event_reason );
create index concurrently psp_entitlement_msg_msgts_er_p4 on pspadm.psp_entitlement_message_p4  USING BTREE (message_timestamp ,event_reason );
create index concurrently psp_entitlement_msg_msgts_er_p5 on pspadm.psp_entitlement_message_p5  USING BTREE (message_timestamp ,event_reason );
create index concurrently psp_entitlement_msg_msgts_er_p6 on pspadm.psp_entitlement_message_p6  USING BTREE (message_timestamp ,event_reason );
create index concurrently psp_entitlement_msg_msgts_er_p7 on pspadm.psp_entitlement_message_p7  USING BTREE (message_timestamp ,event_reason );

create index concurrently psp_entitlement_msg_orno_lcno_p0 on pspadm.psp_entitlement_message_p0  USING BTREE (order_number ,license_number );
create index concurrently psp_entitlement_msg_orno_lcno_p1 on pspadm.psp_entitlement_message_p1  USING BTREE (order_number ,license_number );
create index concurrently psp_entitlement_msg_orno_lcno_p2 on pspadm.psp_entitlement_message_p2  USING BTREE (order_number ,license_number );
create index concurrently psp_entitlement_msg_orno_lcno_p3 on pspadm.psp_entitlement_message_p3  USING BTREE (order_number ,license_number );
create index concurrently psp_entitlement_msg_orno_lcno_p4 on pspadm.psp_entitlement_message_p4  USING BTREE (order_number ,license_number );
create index concurrently psp_entitlement_msg_orno_lcno_p5 on pspadm.psp_entitlement_message_p5  USING BTREE (order_number ,license_number );
create index concurrently psp_entitlement_msg_orno_lcno_p6 on pspadm.psp_entitlement_message_p6  USING BTREE (order_number ,license_number );
create index concurrently psp_entitlement_msg_orno_lcno_p7 on pspadm.psp_entitlement_message_p7  USING BTREE (order_number ,license_number );

create index concurrently idx_ent_msg_mod_date_p0 on pspadm.psp_entitlement_message_p0  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_p1 on pspadm.psp_entitlement_message_p1  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_p2 on pspadm.psp_entitlement_message_p2  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_p3 on pspadm.psp_entitlement_message_p3  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_p4 on pspadm.psp_entitlement_message_p4  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_p5 on pspadm.psp_entitlement_message_p5  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_p6 on pspadm.psp_entitlement_message_p6  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_p7 on pspadm.psp_entitlement_message_p7  USING BTREE (modified_date);

--psp_entitlement_message
create index  psp_entitlement_message_u1 ON ONLY  pspadm.psp_entitlement_message USING BTREE (token );
create index  psp_entitlement_msg_lcno_eofcd ON ONLY  pspadm.psp_entitlement_message USING BTREE (license_number ,entitlement_offering_code );
create index  psp_entitlement_msg_msgts_er ON ONLY  pspadm.psp_entitlement_message USING BTREE (message_timestamp ,event_reason  );
create index  psp_entitlement_msg_orno_lcno ON ONLY  pspadm.psp_entitlement_message USING BTREE (order_number ,license_number );
create index  idx_ent_msg_mod_date ON ONLY  pspadm.psp_entitlement_message USING BTREE (modified_date);

alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p0 ;
alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p1 ;
alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p2 ;
alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p3 ;
alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p4 ;
alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p5 ;
alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p6 ;
alter index psp_entitlement_message_u1 attach partition  pspadm.psp_entitlement_message_u1_p7 ;

alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p0 ;
alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p1 ;
alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p2 ;
alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p3 ;
alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p4 ;
alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p5 ;
alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p6 ;
alter index psp_entitlement_msg_lcno_eofcd attach partition  pspadm.psp_entitlement_msg_lcno_eofcd_p7 ;

alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p0 ;
alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p1 ;
alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p2 ;
alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p3 ;
alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p4 ;
alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p5 ;
alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p6 ;
alter index psp_entitlement_msg_msgts_er attach partition  pspadm.psp_entitlement_msg_msgts_er_p7 ;

alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p0 ;
alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p1 ;
alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p2 ;
alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p3 ;
alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p4 ;
alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p5 ;
alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p6 ;
alter index psp_entitlement_msg_orno_lcno attach partition  pspadm.psp_entitlement_msg_orno_lcno_p7 ;

alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p0 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p1 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p2 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p3 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p4 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p5 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p6 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_p7 ;


SELECT CURRENT_TIMESTAMP;



--not required
--rename index to old

Alter index idx_ent_msg_mod_date_2012 RENAME TO idx_ent_msg_mod_date_2012_old;
Alter index idx_ent_msg_mod_date_2013 RENAME TO idx_ent_msg_mod_date_2013_old;
Alter index idx_ent_msg_mod_date_2014 RENAME TO idx_ent_msg_mod_date_2014_old;
Alter index idx_ent_msg_mod_date_2015 RENAME TO idx_ent_msg_mod_date_2015_old;
Alter index idx_ent_msg_mod_date_2016 RENAME TO idx_ent_msg_mod_date_2016_old;
Alter index idx_ent_msg_mod_date_2017 RENAME TO idx_ent_msg_mod_date_2017_old;
Alter index idx_ent_msg_mod_date_2018 RENAME TO idx_ent_msg_mod_date_2018_old;
Alter index idx_ent_msg_mod_date_2019 RENAME TO idx_ent_msg_mod_date_2019_old;
Alter index idx_ent_msg_mod_date_2020 RENAME TO idx_ent_msg_mod_date_2020_old;
Alter index idx_ent_msg_mod_date_2021 RENAME TO idx_ent_msg_mod_date_2021_old;
Alter index idx_ent_msg_mod_date_2022 RENAME TO idx_ent_msg_mod_date_2022_old;
Alter index idx_ent_msg_mod_date_2023 RENAME TO idx_ent_msg_mod_date_2023_old;
Alter index idx_ent_msg_mod_date_2024 RENAME TO idx_ent_msg_mod_date_2024_old;
            
            
Alter index psp_entitlement_message_u1_2012 RENAME TO psp_entitlement_message_u1_2012_old;
Alter index psp_entitlement_message_u1_2013 RENAME TO psp_entitlement_message_u1_2013_old;
Alter index psp_entitlement_message_u1_2014 RENAME TO psp_entitlement_message_u1_2014_old;
Alter index psp_entitlement_message_u1_2015 RENAME TO psp_entitlement_message_u1_2015_old;
Alter index psp_entitlement_message_u1_2016 RENAME TO psp_entitlement_message_u1_2016_old;
Alter index psp_entitlement_message_u1_2017 RENAME TO psp_entitlement_message_u1_2017_old;
Alter index psp_entitlement_message_u1_2018 RENAME TO psp_entitlement_message_u1_2018_old;
Alter index psp_entitlement_message_u1_2019 RENAME TO psp_entitlement_message_u1_2019_old;
Alter index psp_entitlement_message_u1_2020 RENAME TO psp_entitlement_message_u1_2020_old;
Alter index psp_entitlement_message_u1_2021 RENAME TO psp_entitlement_message_u1_2021_old;
Alter index psp_entitlement_message_u1_2022 RENAME TO psp_entitlement_message_u1_2022_old;
Alter index psp_entitlement_message_u1_2023 RENAME TO psp_entitlement_message_u1_2023_old;
Alter index psp_entitlement_message_u1_2024 RENAME TO psp_entitlement_message_u1_2024_old;
            
Alter index psp_entitlement_msg_lcno_eofcd_2012 RENAME TO psp_entitlement_msg_lcno_eofcd_2012_old;
Alter index psp_entitlement_msg_lcno_eofcd_2013 RENAME TO psp_entitlement_msg_lcno_eofcd_2013_old;
Alter index psp_entitlement_msg_lcno_eofcd_2014 RENAME TO psp_entitlement_msg_lcno_eofcd_2014_old;
Alter index psp_entitlement_msg_lcno_eofcd_2015 RENAME TO psp_entitlement_msg_lcno_eofcd_2015_old;
Alter index psp_entitlement_msg_lcno_eofcd_2016 RENAME TO psp_entitlement_msg_lcno_eofcd_2016_old;
Alter index psp_entitlement_msg_lcno_eofcd_2017 RENAME TO psp_entitlement_msg_lcno_eofcd_2017_old;
Alter index psp_entitlement_msg_lcno_eofcd_2018 RENAME TO psp_entitlement_msg_lcno_eofcd_2018_old;
Alter index psp_entitlement_msg_lcno_eofcd_2019 RENAME TO psp_entitlement_msg_lcno_eofcd_2019_old;
Alter index psp_entitlement_msg_lcno_eofcd_2020 RENAME TO psp_entitlement_msg_lcno_eofcd_2020_old;
Alter index psp_entitlement_msg_lcno_eofcd_2021 RENAME TO psp_entitlement_msg_lcno_eofcd_2021_old;
Alter index psp_entitlement_msg_lcno_eofcd_2022 RENAME TO psp_entitlement_msg_lcno_eofcd_2022_old;
Alter index psp_entitlement_msg_lcno_eofcd_2023 RENAME TO psp_entitlement_msg_lcno_eofcd_2023_old;
Alter index psp_entitlement_msg_lcno_eofcd_2024 RENAME TO psp_entitlement_msg_lcno_eofcd_2024_old;
            
            
Alter index psp_entitlement_msg_msgts_er_2012 RENAME TO psp_entitlement_msg_msgts_er_2012_old;
Alter index psp_entitlement_msg_msgts_er_2013 RENAME TO psp_entitlement_msg_msgts_er_2013_old;
Alter index psp_entitlement_msg_msgts_er_2014 RENAME TO psp_entitlement_msg_msgts_er_2014_old;
Alter index psp_entitlement_msg_msgts_er_2015 RENAME TO psp_entitlement_msg_msgts_er_2015_old;
Alter index psp_entitlement_msg_msgts_er_2016 RENAME TO psp_entitlement_msg_msgts_er_2016_old;
Alter index psp_entitlement_msg_msgts_er_2017 RENAME TO psp_entitlement_msg_msgts_er_2017_old;
Alter index psp_entitlement_msg_msgts_er_2018 RENAME TO psp_entitlement_msg_msgts_er_2018_old;
Alter index psp_entitlement_msg_msgts_er_2019 RENAME TO psp_entitlement_msg_msgts_er_2019_old;
Alter index psp_entitlement_msg_msgts_er_2020 RENAME TO psp_entitlement_msg_msgts_er_2020_old;
Alter index psp_entitlement_msg_msgts_er_2021 RENAME TO psp_entitlement_msg_msgts_er_2021_old;
Alter index psp_entitlement_msg_msgts_er_2022 RENAME TO psp_entitlement_msg_msgts_er_2022_old;
Alter index psp_entitlement_msg_msgts_er_2023 RENAME TO psp_entitlement_msg_msgts_er_2023_old;
Alter index psp_entitlement_msg_msgts_er_2024 RENAME TO psp_entitlement_msg_msgts_er_2024_old;
         
Alter index psp_entitlement_msg_orno_lcno_2012 RENAME TO psp_entitlement_msg_orno_lcno_2012_old;
Alter index psp_entitlement_msg_orno_lcno_2013 RENAME TO psp_entitlement_msg_orno_lcno_2013_old;
Alter index psp_entitlement_msg_orno_lcno_2014 RENAME TO psp_entitlement_msg_orno_lcno_2014_old;
Alter index psp_entitlement_msg_orno_lcno_2015 RENAME TO psp_entitlement_msg_orno_lcno_2015_old;
Alter index psp_entitlement_msg_orno_lcno_2016 RENAME TO psp_entitlement_msg_orno_lcno_2016_old;
Alter index psp_entitlement_msg_orno_lcno_2017 RENAME TO psp_entitlement_msg_orno_lcno_2017_old;
Alter index psp_entitlement_msg_orno_lcno_2018 RENAME TO psp_entitlement_msg_orno_lcno_2018_old;
Alter index psp_entitlement_msg_orno_lcno_2019 RENAME TO psp_entitlement_msg_orno_lcno_2019_old;
Alter index psp_entitlement_msg_orno_lcno_2020 RENAME TO psp_entitlement_msg_orno_lcno_2020_old;
Alter index psp_entitlement_msg_orno_lcno_2021 RENAME TO psp_entitlement_msg_orno_lcno_2021_old;
Alter index psp_entitlement_msg_orno_lcno_2022 RENAME TO psp_entitlement_msg_orno_lcno_2022_old;
Alter index psp_entitlement_msg_orno_lcno_2023 RENAME TO psp_entitlement_msg_orno_lcno_2023_old;
Alter index psp_entitlement_msg_orno_lcno_2024 RENAME TO psp_entitlement_msg_orno_lcno_2024_old;


--Check old and new table count
select count(*) from pspadm.psp_entitlement_message;
select count(*) from pspadm.psp_entitlement_message_old;
