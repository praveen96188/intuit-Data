\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


--psp_entry_detail_record
create index concurrently idx_edr_mod_date_p0 on pspadm.psp_entry_detail_record_p0 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p1 on pspadm.psp_entry_detail_record_p1 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p10 on pspadm.psp_entry_detail_record_p10 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p11 on pspadm.psp_entry_detail_record_p11 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p12 on pspadm.psp_entry_detail_record_p12 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p13 on pspadm.psp_entry_detail_record_p13 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p14 on pspadm.psp_entry_detail_record_p14 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p15 on pspadm.psp_entry_detail_record_p15 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p2 on pspadm.psp_entry_detail_record_p2 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p3 on pspadm.psp_entry_detail_record_p3 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p4 on pspadm.psp_entry_detail_record_p4 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p5 on pspadm.psp_entry_detail_record_p5 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p6 on pspadm.psp_entry_detail_record_p6 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p7 on pspadm.psp_entry_detail_record_p7 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p8 on pspadm.psp_entry_detail_record_p8 USING BTREE  (modified_date);
create index concurrently idx_edr_mod_date_p9 on pspadm.psp_entry_detail_record_p9 USING BTREE  (modified_date);

create index idx_edr_mod_date ON ONLY  pspadm.psp_entry_detail_record USING BTREE (modified_date);
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p0 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p1 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p10 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p11 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p12 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p13 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p14 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p15 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p2 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p3 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p4 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p5 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p6 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p7 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p8 ;
alter index idx_edr_mod_date attach partition  pspadm.idx_edr_mod_date_p9 ;


--psp_disburse_advice_tax_liab
create index concurrently idx_datl_mod_date_p0 on pspadm.psp_disburse_advice_tax_liab_p0 USING BTREE  (modified_date);
create index concurrently idx_datl_mod_date_p1 on pspadm.psp_disburse_advice_tax_liab_p1 USING BTREE  (modified_date);
create index concurrently idx_datl_mod_date_p2 on pspadm.psp_disburse_advice_tax_liab_p2 USING BTREE  (modified_date);
create index concurrently idx_datl_mod_date_p3 on pspadm.psp_disburse_advice_tax_liab_p3 USING BTREE  (modified_date);

create index idx_datl_mod_date ON ONLY  pspadm.psp_disburse_advice_tax_liab USING BTREE (modified_date);
alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p0 ;
alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p1 ;
alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p2 ;
alter index idx_datl_mod_date attach partition  pspadm.idx_datl_mod_date_p3 ;




SELECT CURRENT_TIMESTAMP;

