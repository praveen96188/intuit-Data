set search_path to pspadm;

create index concurrently  idx_ft_settlement_date_p0 on pspadm.psp_financial_transaction_p0 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p1 on pspadm.psp_financial_transaction_p1 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p2 on pspadm.psp_financial_transaction_p2 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p3 on pspadm.psp_financial_transaction_p3 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p4 on pspadm.psp_financial_transaction_p4 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p5 on pspadm.psp_financial_transaction_p5 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p6 on pspadm.psp_financial_transaction_p6 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p7 on pspadm.psp_financial_transaction_p7 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p8 on pspadm.psp_financial_transaction_p8 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p9 on pspadm.psp_financial_transaction_p9 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p10 on pspadm.psp_financial_transaction_p10 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p11 on pspadm.psp_financial_transaction_p11 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p12 on pspadm.psp_financial_transaction_p12 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p13 on pspadm.psp_financial_transaction_p13 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p14 on pspadm.psp_financial_transaction_p14 USING BTREE (settlement_date); 
create index concurrently  idx_ft_settlement_date_p15 on pspadm.psp_financial_transaction_p15 USING BTREE (settlement_date); 

create index concurrently  idx_ft_settlement_date ON ONLY  pspadm.psp_financial_transaction USING BTREE (settlement_date); 

alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p0;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p1;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p2;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p3;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p4;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p5;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p6;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p7;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p8;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p9;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p10;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p11;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p12;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p13;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p14;
alter index idx_ft_settlement_date attach partition idx_ft_settlement_date_p15;


