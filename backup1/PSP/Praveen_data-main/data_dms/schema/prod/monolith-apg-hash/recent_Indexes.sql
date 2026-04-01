\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

--psp_financial_transaction
create index psp_financial_transaction_i6_p0 on  pspadm.psp_financial_transaction_p0  using BTREE (settlement_date );
create index psp_financial_transaction_i6_p1 on  pspadm.psp_financial_transaction_p1 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p2 on  pspadm.psp_financial_transaction_p2 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p3 on  pspadm.psp_financial_transaction_p3 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p4 on  pspadm.psp_financial_transaction_p4 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p5 on  pspadm.psp_financial_transaction_p5 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p6 on  pspadm.psp_financial_transaction_p6 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p7 on  pspadm.psp_financial_transaction_p7 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p8 on  pspadm.psp_financial_transaction_p8 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p9 on  pspadm.psp_financial_transaction_p9 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p10 on  pspadm.psp_financial_transaction_p10 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p11 on  pspadm.psp_financial_transaction_p11 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p12 on  pspadm.psp_financial_transaction_p12 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p13 on  pspadm.psp_financial_transaction_p13 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p14 on  pspadm.psp_financial_transaction_p14 using BTREE (settlement_date );
create index psp_financial_transaction_i6_p15 on  pspadm.psp_financial_transaction_p15 using BTREE (settlement_date );


create index  psp_financial_transaction_i6 ON ONLY pspadm.psp_financial_transaction USING BTREE (settlement_date );

alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p0 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p1 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p10 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p11 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p12 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p13 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p14 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p15 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p2 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p3 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p4 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p5 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p6 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p7 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p8 ;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p9 ;


SELECT CURRENT_TIMESTAMP;

--psp_paycheck_split
create index  idx_paycheck_split_cr_date ON ONLY pspadm.psp_paycheck_split USING BTREE (created_date );

create index idx_paycheck_split_cr_date_p0 on pspadm.psp_paycheck_split_p0 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p1 on pspadm.psp_paycheck_split_p1 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p10 on pspadm.psp_paycheck_split_p10 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p11 on pspadm.psp_paycheck_split_p11 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p12 on pspadm.psp_paycheck_split_p12 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p13 on pspadm.psp_paycheck_split_p13 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p14 on pspadm.psp_paycheck_split_p14 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p15 on pspadm.psp_paycheck_split_p15 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p2 on pspadm.psp_paycheck_split_p2 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p3 on pspadm.psp_paycheck_split_p3 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p4 on pspadm.psp_paycheck_split_p4 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p5 on pspadm.psp_paycheck_split_p5 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p6 on pspadm.psp_paycheck_split_p6 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p7 on pspadm.psp_paycheck_split_p7 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p8 on pspadm.psp_paycheck_split_p8 using BTREE (created_date );
create index idx_paycheck_split_cr_date_p9 on pspadm.psp_paycheck_split_p9 using BTREE (created_date );



alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p0 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p1 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p10 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p11 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p12 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p13 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p14 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p15 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p2 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p3 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p4 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p5 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p6 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p7 ;
alter index idx_paycheck_split_cr_date attach partition pspadm.idx_paycheck_split_cr_date_p8 ;

SELECT CURRENT_TIMESTAMP;

 \timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;
 analyze pspadm.psp_financial_transaction_p0;
 analyze pspadm.psp_financial_transaction_p1 ;
 analyze pspadm.psp_financial_transaction_p2 ;
 analyze pspadm.psp_financial_transaction_p3 ;
 analyze pspadm.psp_financial_transaction_p4 ;
 analyze pspadm.psp_financial_transaction_p5 ;
 analyze pspadm.psp_financial_transaction_p6 ;
 analyze pspadm.psp_financial_transaction_p7 ;
 analyze pspadm.psp_financial_transaction_p8 ;
 analyze pspadm.psp_financial_transaction_p9 ;
 analyze pspadm.psp_financial_transaction_p10 ;
 analyze pspadm.psp_financial_transaction_p11 ;
 analyze pspadm.psp_financial_transaction_p12 ;
 analyze pspadm.psp_financial_transaction_p13 ;
 analyze pspadm.psp_financial_transaction_p14 ;
 analyze pspadm.psp_financial_transaction_p15 ;

 SELECT CURRENT_TIMESTAMP;


 
