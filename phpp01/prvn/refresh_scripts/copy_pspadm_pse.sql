\timing
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

set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;
--create drop old indexes and create new indexes on table
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

