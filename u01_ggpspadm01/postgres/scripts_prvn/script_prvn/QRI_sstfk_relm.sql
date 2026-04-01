\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


create index concurrently psp_qbdt_request_info_fk1_p0 on pspadm.psp_qbdt_request_info_p0 USING BTREE (source_system_transmission_fk , realm_id);
create index concurrently psp_qbdt_request_info_fk1_p1 on pspadm.psp_qbdt_request_info_p1 USING BTREE (source_system_transmission_fk , realm_id);
create index concurrently psp_qbdt_request_info_fk1_p2 on pspadm.psp_qbdt_request_info_p2 USING BTREE (source_system_transmission_fk , realm_id);
create index concurrently psp_qbdt_request_info_fk1_p3 on pspadm.psp_qbdt_request_info_p3 USING BTREE (source_system_transmission_fk , realm_id);

CREATE INDEX psp_qbdt_request_info_fk1 ON ONLY pspadm.psp_qbdt_request_info USING BTREE (source_system_transmission_fk , realm_id );

alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p0 ;
alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p1 ;
alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p2 ;
alter index psp_qbdt_request_info_fk1 attach partition pspadm.psp_qbdt_request_info_fk1_p3 ;


SELECT CURRENT_TIMESTAMP;
