\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

--psp_paycheck_split
create index concurrently idx_paycheck_split_mod_date_p0 on pspadm.psp_paycheck_split_p0 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p1 on pspadm.psp_paycheck_split_p1 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p10 on pspadm.psp_paycheck_split_p10 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p11 on pspadm.psp_paycheck_split_p11 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p12 on pspadm.psp_paycheck_split_p12 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p13 on pspadm.psp_paycheck_split_p13 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p14 on pspadm.psp_paycheck_split_p14 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p15 on pspadm.psp_paycheck_split_p15 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p2 on pspadm.psp_paycheck_split_p2 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p3 on pspadm.psp_paycheck_split_p3 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p4 on pspadm.psp_paycheck_split_p4 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p5 on pspadm.psp_paycheck_split_p5 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p6 on pspadm.psp_paycheck_split_p6 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p7 on pspadm.psp_paycheck_split_p7 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p8 on pspadm.psp_paycheck_split_p8 USING BTREE  (modified_date);
create index concurrently idx_paycheck_split_mod_date_p9 on pspadm.psp_paycheck_split_p9 USING BTREE  (modified_date);

create index idx_paycheck_split_mod_date ON ONLY  pspadm.psp_paycheck_split USING BTREE (modified_date);
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p0 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p1 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p10 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p11 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p12 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p13 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p14 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p15 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p2 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p3 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p4 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p5 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p6 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p7 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p8 ;
alter index idx_paycheck_split_mod_date attach partition  pspadm.idx_paycheck_split_mod_date_p9 ;


--psp_compensation

create index concurrently idx_compensation_mod_date_p0 on pspadm.psp_compensation_p0 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p1 on pspadm.psp_compensation_p1 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p2 on pspadm.psp_compensation_p2 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p3 on pspadm.psp_compensation_p3 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p4 on pspadm.psp_compensation_p4 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p5 on pspadm.psp_compensation_p5 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p6 on pspadm.psp_compensation_p6 USING BTREE  (modified_date);
create index concurrently idx_compensation_mod_date_p7 on pspadm.psp_compensation_p7 USING BTREE  (modified_date);


create index idx_compensation_mod_date ON ONLY  pspadm.psp_compensation USING BTREE (modified_date);

alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p0 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p1 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p2 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p3 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p4 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p5 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p6 ;
alter index idx_compensation_mod_date attach partition  pspadm.idx_compensation_mod_date_p7 ;



--psp_ledger_balance

create index concurrently idx_lb_mod_date_p0 on pspadm.psp_ledger_balance_p0 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p1 on pspadm.psp_ledger_balance_p1 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p2 on pspadm.psp_ledger_balance_p2 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p3 on pspadm.psp_ledger_balance_p3 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p4 on pspadm.psp_ledger_balance_p4 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p5 on pspadm.psp_ledger_balance_p5 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p6 on pspadm.psp_ledger_balance_p6 USING BTREE  (modified_date) ;
create index concurrently idx_lb_mod_date_p7 on pspadm.psp_ledger_balance_p7 USING BTREE  (modified_date) ;

create index idx_lb_mod_date ON ONLY  pspadm.psp_ledger_balance USING BTREE (modified_date);

alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p0  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p1  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p2  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p3  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p4  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p5  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p6  ;
alter index idx_lb_mod_date attach partition  pspadm.idx_lb_mod_date_p7  ;





SELECT CURRENT_TIMESTAMP;


