\timing
set search_path to pspadm;

set work_mem="10240MB";
set max_parallel_workers_per_gather=16;
select count(*) from PSP_MONEY_MOVEMENT_TRANSACTION;
select count(*) from PSP_FINANCIAL_TRANSACTION;
select count(*) from PSP_PAYCHECK;
select count(*) from PSP_PAYCHECK_SPLIT;
select count(*) from PSP_PSTUB_PAY_ITEM;
select count(*) from PSP_TAX;
