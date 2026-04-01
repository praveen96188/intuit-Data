\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;


--psp_financial_trans_state
create index concurrently idx_fts_mod_date_p0 on pspadm.psp_financial_trans_state_p0 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p1 on pspadm.psp_financial_trans_state_p1 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p10 on pspadm.psp_financial_trans_state_p10 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p11 on pspadm.psp_financial_trans_state_p11 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p12 on pspadm.psp_financial_trans_state_p12 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p13 on pspadm.psp_financial_trans_state_p13 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p14 on pspadm.psp_financial_trans_state_p14 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p15 on pspadm.psp_financial_trans_state_p15 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p2 on pspadm.psp_financial_trans_state_p2 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p3 on pspadm.psp_financial_trans_state_p3 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p4 on pspadm.psp_financial_trans_state_p4 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p5 on pspadm.psp_financial_trans_state_p5 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p6 on pspadm.psp_financial_trans_state_p6 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p7 on pspadm.psp_financial_trans_state_p7 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p8 on pspadm.psp_financial_trans_state_p8 USING BTREE  (modified_date);
create index concurrently idx_fts_mod_date_p9 on pspadm.psp_financial_trans_state_p9 USING BTREE  (modified_date);


create index idx_fts_mod_date ON ONLY  pspadm.psp_financial_trans_state USING BTREE (modified_date);
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p0 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p1 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p10 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p11 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p12 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p13 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p14 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p15 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p2 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p3 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p4 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p5 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p6 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p7 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p8 ;
alter index idx_fts_mod_date attach partition  pspadm.idx_fts_mod_date_p9 ;


--psp_property_audit
create index concurrently idx_prop_aud_mod_date_p0 on pspadm.psp_property_audit_p0 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p1 on pspadm.psp_property_audit_p1 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p2 on pspadm.psp_property_audit_p2 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p3 on pspadm.psp_property_audit_p3 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p4 on pspadm.psp_property_audit_p4 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p5 on pspadm.psp_property_audit_p5 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p6 on pspadm.psp_property_audit_p6 USING BTREE  (modified_date);
create index concurrently idx_prop_aud_mod_date_p7 on pspadm.psp_property_audit_p7 USING BTREE  (modified_date);

create index idx_prop_aud_mod_date ON ONLY  pspadm.psp_property_audit USING BTREE (modified_date);
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p0 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p1 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p2 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p3 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p4 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p5 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p6 ;
alter index idx_prop_aud_mod_date attach partition  pspadm.idx_prop_aud_mod_date_p7 ;


--psp_company_event
create index concurrently idx_ce_mod_date_p0 on pspadm.psp_company_event_p0 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p1 on pspadm.psp_company_event_p1 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p2 on pspadm.psp_company_event_p2 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p3 on pspadm.psp_company_event_p3 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p4 on pspadm.psp_company_event_p4 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p5 on pspadm.psp_company_event_p5 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p6 on pspadm.psp_company_event_p6 USING BTREE  (modified_date);
create index concurrently idx_ce_mod_date_p7 on pspadm.psp_company_event_p7 USING BTREE  (modified_date);

create index idx_ce_mod_date ON ONLY  pspadm.psp_company_event USING BTREE (modified_date);
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p0 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p1 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p2 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p3 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p4 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p5 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p6 ;
alter index idx_ce_mod_date attach partition  pspadm.idx_ce_mod_date_p7 ;

SELECT CURRENT_TIMESTAMP;
