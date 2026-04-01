\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

--psp_pstub_pay_item

create index concurrently idx_pstub_pay_item_mod_date_p0 on pspadm.psp_pstub_pay_item_p0 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p1 on pspadm.psp_pstub_pay_item_p1 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p10 on pspadm.psp_pstub_pay_item_p10 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p11 on pspadm.psp_pstub_pay_item_p11 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p12 on pspadm.psp_pstub_pay_item_p12 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p13 on pspadm.psp_pstub_pay_item_p13 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p14 on pspadm.psp_pstub_pay_item_p14 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p15 on pspadm.psp_pstub_pay_item_p15 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p2 on pspadm.psp_pstub_pay_item_p2 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p3 on pspadm.psp_pstub_pay_item_p3 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p4 on pspadm.psp_pstub_pay_item_p4 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p5 on pspadm.psp_pstub_pay_item_p5 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p6 on pspadm.psp_pstub_pay_item_p6 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p7 on pspadm.psp_pstub_pay_item_p7 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p8 on pspadm.psp_pstub_pay_item_p8 USING BTREE  (modified_date);
create index concurrently idx_pstub_pay_item_mod_date_p9 on pspadm.psp_pstub_pay_item_p9 USING BTREE  (modified_date);

create index idx_pstub_pay_item_mod_date ON ONLY  pspadm.psp_pstub_pay_item USING BTREE (modified_date);

alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p0 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p1 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p10 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p11 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p12 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p13 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p14 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p15 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p2 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p3 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p4 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p5 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p6 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p7 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p8 ;
alter index idx_pstub_pay_item_mod_date attach partition  pspadm.idx_pstub_pay_item_mod_date_p9 ;


--psp_money_movement_transaction

create index concurrently idx_mmt_mod_date_p0 on pspadm.psp_money_movement_transaction_p0 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p1 on pspadm.psp_money_movement_transaction_p1 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p2 on pspadm.psp_money_movement_transaction_p2 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p3 on pspadm.psp_money_movement_transaction_p3 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p4 on pspadm.psp_money_movement_transaction_p4 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p5 on pspadm.psp_money_movement_transaction_p5 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p6 on pspadm.psp_money_movement_transaction_p6 USING BTREE  (modified_date) ;
create index concurrently idx_mmt_mod_date_p7 on pspadm.psp_money_movement_transaction_p7 USING BTREE  (modified_date) ;

create index idx_mmt_mod_date ON ONLY  pspadm.psp_money_movement_transaction USING BTREE (modified_date);
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p0  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p1  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p2  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p3  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p4  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p5  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p6  ;
alter index idx_mmt_mod_date attach partition  pspadm.idx_mmt_mod_date_p7  ;

--psp_paystub
create index concurrently idx_paystub_mod_date_p0 on pspadm.psp_paystub_p0 USING BTREE  (modified_date);
create index concurrently idx_paystub_mod_date_p1 on pspadm.psp_paystub_p1 USING BTREE  (modified_date);
create index concurrently idx_paystub_mod_date_p2 on pspadm.psp_paystub_p2 USING BTREE  (modified_date);
create index concurrently idx_paystub_mod_date_p3 on pspadm.psp_paystub_p3 USING BTREE  (modified_date);

create index idx_paystub_mod_date ON ONLY  pspadm.psp_paystub USING BTREE (modified_date);
alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p0 ;
alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p1 ;
alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p2 ;
alter index idx_paystub_mod_date attach partition  pspadm.idx_paystub_mod_date_p3 ;

--psp_qbdt_transaction_info
create index concurrently idx_qti_mod_date_p0 on pspadm.psp_qbdt_transaction_info_p0 USING BTREE  (modified_date);
create index concurrently idx_qti_mod_date_p1 on pspadm.psp_qbdt_transaction_info_p1 USING BTREE  (modified_date);
create index concurrently idx_qti_mod_date_p2 on pspadm.psp_qbdt_transaction_info_p2 USING BTREE  (modified_date);
create index concurrently idx_qti_mod_date_p3 on pspadm.psp_qbdt_transaction_info_p3 USING BTREE  (modified_date);

create index idx_qti_mod_date ON ONLY  pspadm.psp_qbdt_transaction_info USING BTREE (modified_date);
alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p0;
alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p1;
alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p2;
alter index idx_qti_mod_date attach partition  pspadm.idx_qti_mod_date_p3;

SELECT CURRENT_TIMESTAMP;
