\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

create index concurrently psp_qbdt_request_info_crdt_p0 on pspadm.psp_qbdt_request_info_p0 USING BTREE (created_date);
create index concurrently psp_qbdt_request_info_crdt_p1 on pspadm.psp_qbdt_request_info_p1 USING BTREE (created_date);
create index concurrently psp_qbdt_request_info_crdt_p2 on pspadm.psp_qbdt_request_info_p2 USING BTREE (created_date);
create index concurrently psp_qbdt_request_info_crdt_p3 on pspadm.psp_qbdt_request_info_p3 USING BTREE (created_date);


CREATE INDEX psp_qbdt_request_info_crdt ON ONLY pspadm.psp_qbdt_request_info USING BTREE (created_date );

alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p0 ;
alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p1 ;
alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p2 ;
alter index psp_qbdt_request_info_crdt attach partition pspadm.psp_qbdt_request_info_crdt_p3 ;



SELECT CURRENT_TIMESTAMP;
