\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

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

SELECT CURRENT_TIMESTAMP;
