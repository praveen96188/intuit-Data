\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

--psp_tax
create index concurrently idx_tax_mod_date_p0 on pspadm.psp_tax_p0 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p1 on pspadm.psp_tax_p1 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p10 on pspadm.psp_tax_p10 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p11 on pspadm.psp_tax_p11 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p12 on pspadm.psp_tax_p12 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p13 on pspadm.psp_tax_p13 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p14 on pspadm.psp_tax_p14 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p15 on pspadm.psp_tax_p15 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p2 on pspadm.psp_tax_p2 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p3 on pspadm.psp_tax_p3 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p4 on pspadm.psp_tax_p4 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p5 on pspadm.psp_tax_p5 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p6 on pspadm.psp_tax_p6 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p7 on pspadm.psp_tax_p7 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p8 on pspadm.psp_tax_p8 USING BTREE  (modified_date);
create index concurrently idx_tax_mod_date_p9 on pspadm.psp_tax_p9 USING BTREE  (modified_date);

create index idx_tax_mod_date ON ONLY  pspadm.psp_tax  USING BTREE (modified_date);

alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p0 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p1 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p10 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p11 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p12 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p13 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p14 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p15 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p2 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p3 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p4 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p5 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p6 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p7 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p8 ;
alter index idx_tax_mod_date attach partition  pspadm.idx_tax_mod_date_p9 ;

--psp_pstub_employee_info
create index concurrently idx_pei_mod_date_p0 on pspadm.psp_pstub_employee_info_p0 USING BTREE  (modified_date);
create index concurrently idx_pei_mod_date_p1 on pspadm.psp_pstub_employee_info_p1 USING BTREE  (modified_date);
create index concurrently idx_pei_mod_date_p2 on pspadm.psp_pstub_employee_info_p2 USING BTREE  (modified_date);
create index concurrently idx_pei_mod_date_p3 on pspadm.psp_pstub_employee_info_p3 USING BTREE  (modified_date);

create index idx_pei_mod_date ON ONLY  pspadm.psp_pstub_employee_info USING BTREE (modified_date);
alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p0 ;
alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p1 ;
alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p2 ;
alter index idx_pei_mod_date attach partition  pspadm.idx_pei_mod_date_p3 ;

--psp_pstub_paid_timeoff_item
create index concurrently idx_ppti_mod_date_p0 on pspadm.psp_pstub_paid_timeoff_item_p0 USING BTREE  (modified_date);
create index concurrently idx_ppti_mod_date_p1 on pspadm.psp_pstub_paid_timeoff_item_p1 USING BTREE  (modified_date);
create index concurrently idx_ppti_mod_date_p2 on pspadm.psp_pstub_paid_timeoff_item_p2 USING BTREE  (modified_date);
create index concurrently idx_ppti_mod_date_p3 on pspadm.psp_pstub_paid_timeoff_item_p3 USING BTREE  (modified_date);

create index idx_ppti_mod_date ON ONLY  pspadm.psp_pstub_paid_timeoff_item USING BTREE (modified_date);
alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p0 ;
alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p1 ;
alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p2 ;
alter index idx_ppti_mod_date attach partition  pspadm.idx_ppti_mod_date_p3 ;

--psp_deduction
create index concurrently idx_deduction_mod_date_p0 on pspadm.psp_deduction_p0 USING BTREE  (modified_date);
create index concurrently idx_deduction_mod_date_p1 on pspadm.psp_deduction_p1 USING BTREE  (modified_date);
create index concurrently idx_deduction_mod_date_p2 on pspadm.psp_deduction_p2 USING BTREE  (modified_date);
create index concurrently idx_deduction_mod_date_p3 on pspadm.psp_deduction_p3 USING BTREE  (modified_date);

create index idx_deduction_mod_date ON ONLY  pspadm.psp_deduction USING BTREE (modified_date);
alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p0 ;
alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p1 ;
alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p2 ;
alter index idx_deduction_mod_date attach partition  pspadm.idx_deduction_mod_date_p3 ;

SELECT CURRENT_TIMESTAMP;
