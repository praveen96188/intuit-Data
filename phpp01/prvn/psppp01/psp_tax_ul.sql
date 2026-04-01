\timing
set search_path to pspadm;

set work_mem=10240M;
set max_parallel_workers_per_gather=16;
select count(*) from pspadm.psp_tax where created_date between '2023-01-01 00:00:00' and '2023-07-24 00:00:00';
