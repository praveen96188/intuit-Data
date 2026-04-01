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
