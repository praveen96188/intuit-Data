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
