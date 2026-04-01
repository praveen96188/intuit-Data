\timing

set search_path to pspadm;

select current_timestamp;

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

select current_timestamp;
