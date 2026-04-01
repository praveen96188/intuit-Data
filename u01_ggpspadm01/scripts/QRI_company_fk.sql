\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


create index concurrently psp_qbdt_request_info_fk2_p0 on pspadm.psp_qbdt_request_info_p0 USING BTREE (company_fk);
create index concurrently psp_qbdt_request_info_fk2_p1 on pspadm.psp_qbdt_request_info_p1 USING BTREE (company_fk);
create index concurrently psp_qbdt_request_info_fk2_p2 on pspadm.psp_qbdt_request_info_p2 USING BTREE (company_fk);
create index concurrently psp_qbdt_request_info_fk2_p3 on pspadm.psp_qbdt_request_info_p3 USING BTREE (company_fk);

CREATE INDEX  psp_qbdt_request_info_fk2 ON ONLY pspadm.psp_qbdt_request_info USING BTREE (company_fk);

alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p0 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p1 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p2 ;
alter index psp_qbdt_request_info_fk2 attach partition pspadm.psp_qbdt_request_info_fk2_p3 ;

SELECT CURRENT_TIMESTAMP;
