\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


--psp_company_event_detail
create index concurrently idx_ced_mod_date_p0 on pspadm.psp_company_event_detail_p0 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p1 on pspadm.psp_company_event_detail_p1 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p2 on pspadm.psp_company_event_detail_p2 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p3 on pspadm.psp_company_event_detail_p3 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p4 on pspadm.psp_company_event_detail_p4 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p5 on pspadm.psp_company_event_detail_p5 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p6 on pspadm.psp_company_event_detail_p6 USING BTREE  (modified_date );
create index concurrently idx_ced_mod_date_p7 on pspadm.psp_company_event_detail_p7 USING BTREE  (modified_date );

create index idx_ced_mod_date ON ONLY  pspadm.psp_company_event_detail USING BTREE (modified_date);

alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p0 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p1 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p2 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p3 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p4 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p5 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p6 ;
alter index idx_ced_mod_date attach partition  pspadm.idx_ced_mod_date_p7 ;

--psp_qbdt_paycheck_info
create index concurrently idx_qpi_mod_date_p0 on pspadm.psp_qbdt_paycheck_info_p0 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p1 on pspadm.psp_qbdt_paycheck_info_p1 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p10 on pspadm.psp_qbdt_paycheck_info_p10 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p11 on pspadm.psp_qbdt_paycheck_info_p11 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p12 on pspadm.psp_qbdt_paycheck_info_p12 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p13 on pspadm.psp_qbdt_paycheck_info_p13 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p14 on pspadm.psp_qbdt_paycheck_info_p14 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p15 on pspadm.psp_qbdt_paycheck_info_p15 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p2 on pspadm.psp_qbdt_paycheck_info_p2 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p3 on pspadm.psp_qbdt_paycheck_info_p3 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p4 on pspadm.psp_qbdt_paycheck_info_p4 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p5 on pspadm.psp_qbdt_paycheck_info_p5 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p6 on pspadm.psp_qbdt_paycheck_info_p6 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p7 on pspadm.psp_qbdt_paycheck_info_p7 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p8 on pspadm.psp_qbdt_paycheck_info_p8 USING BTREE  (modified_date);
create index concurrently idx_qpi_mod_date_p9 on pspadm.psp_qbdt_paycheck_info_p9 USING BTREE  (modified_date);

create index idx_qpi_mod_date ON ONLY  pspadm.psp_qbdt_paycheck_info USING BTREE (modified_date);
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p0 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p1 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p10 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p11 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p12 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p13 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p14 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p15 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p2 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p3 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p4 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p5 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p6 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p7 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p8 ;
alter index idx_qpi_mod_date attach partition  pspadm.idx_qpi_mod_date_p9 ;


--psp_paycheck
create index concurrently idx_paycheck_mod_date_p0 on pspadm.psp_paycheck_p0 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p1 on pspadm.psp_paycheck_p1 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p10 on pspadm.psp_paycheck_p10 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p11 on pspadm.psp_paycheck_p11 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p12 on pspadm.psp_paycheck_p12 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p13 on pspadm.psp_paycheck_p13 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p14 on pspadm.psp_paycheck_p14 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p15 on pspadm.psp_paycheck_p15 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p2 on pspadm.psp_paycheck_p2 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p3 on pspadm.psp_paycheck_p3 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p4 on pspadm.psp_paycheck_p4 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p5 on pspadm.psp_paycheck_p5 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p6 on pspadm.psp_paycheck_p6 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p7 on pspadm.psp_paycheck_p7 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p8 on pspadm.psp_paycheck_p8 USING BTREE  (modified_date);
create index concurrently idx_paycheck_mod_date_p9 on pspadm.psp_paycheck_p9 USING BTREE  (modified_date);

create index idx_paycheck_mod_date ON ONLY  pspadm.psp_paycheck USING BTREE (modified_date);
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p0 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p1 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p10 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p11 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p12 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p13 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p14 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p15 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p2 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p3 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p4 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p5 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p6 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p7 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p8 ;
alter index idx_paycheck_mod_date attach partition  pspadm.idx_paycheck_mod_date_p9 ;


SELECT CURRENT_TIMESTAMP;

