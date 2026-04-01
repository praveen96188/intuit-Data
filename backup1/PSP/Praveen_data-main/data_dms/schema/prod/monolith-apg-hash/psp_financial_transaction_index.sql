


\timing

set session work_mem='64MB';
set session maintenance_work_mem='25098MB';
set search_path to pspadm;
SELECT CURRENT_TIMESTAMP;

create index concurrently  psp_financial_transaction_i6_p0 on pspadm.psp_financial_transaction_p0 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p1 on pspadm.psp_financial_transaction_p1 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p2 on pspadm.psp_financial_transaction_p2 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p3 on pspadm.psp_financial_transaction_p3 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p4 on pspadm.psp_financial_transaction_p4 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p5 on pspadm.psp_financial_transaction_p5 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p6 on pspadm.psp_financial_transaction_p6 USING BTREE (settlement_date); 

create index concurrently  psp_financial_transaction_i6_p7 on pspadm.psp_financial_transaction_p7 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p8 on pspadm.psp_financial_transaction_p8 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p9 on pspadm.psp_financial_transaction_p9 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p10 on pspadm.psp_financial_transaction_p10 USING BTREE (settlement_date); 

create index concurrently  psp_financial_transaction_i6_p11 on pspadm.psp_financial_transaction_p11 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p12 on pspadm.psp_financial_transaction_p12 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p13 on pspadm.psp_financial_transaction_p13 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p14 on pspadm.psp_financial_transaction_p14 USING BTREE (settlement_date); 
create index concurrently  psp_financial_transaction_i6_p15 on pspadm.psp_financial_transaction_p15 USING BTREE (settlement_date); 

SELECT CURRENT_TIMESTAMP;

set search_path to pspadm;

create index   psp_financial_transaction_i6 ON ONLY  pspadm.psp_financial_transaction USING BTREE (settlement_date); 

alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p0;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p1;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p2;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p3;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p4;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p5;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p6;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p7;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p8;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p9;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p10;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p11;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p12;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p13;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p14;
alter index psp_financial_transaction_i6 attach partition pspadm.psp_financial_transaction_i6_p15;

SELECT CURRENT_TIMESTAMP;

\timing
set search_path to pspadm;
set max_parallel_workers_per_gather=0;
SELECT CURRENT_TIMESTAMP;

create index concurrently psp_paycheck_split_i1_p0 on pspadm.psp_paycheck_split_p0 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p1 on pspadm.psp_paycheck_split_p1 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p2 on pspadm.psp_paycheck_split_p2 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p3 on pspadm.psp_paycheck_split_p3 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p4 on pspadm.psp_paycheck_split_p4 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p5 on pspadm.psp_paycheck_split_p5 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p6 on pspadm.psp_paycheck_split_p6 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p7 on pspadm.psp_paycheck_split_p7 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p8 on pspadm.psp_paycheck_split_p8 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p9 on pspadm.psp_paycheck_split_p9 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p10 on pspadm.psp_paycheck_split_p10 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p11 on pspadm.psp_paycheck_split_p11 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p12 on pspadm.psp_paycheck_split_p12 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p13 on pspadm.psp_paycheck_split_p13 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p14 on pspadm.psp_paycheck_split_p14 USING BTREE (created_date);
create index concurrently psp_paycheck_split_i1_p15 on pspadm.psp_paycheck_split_p15 USING BTREE (created_date);
SELECT CURRENT_TIMESTAMP;

create index psp_paycheck_split_i1 ON ONLY pspadm.psp_paycheck_split USING BTREE (created_date);

alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p0 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p1 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p2 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p3 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p4 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p5 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p6 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p7 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p8 ;
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p9 ;   
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p10; 
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p11; 
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p12; 
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p13; 
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p14; 
alter index psp_paycheck_split_i1 attach partition pspadm.psp_paycheck_split_i1_p15;
