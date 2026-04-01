-- switch to psppp001 database
\c psppp001

--# create read-only user for Finance reports permission to login and grant needful permissions #--
create user pspqsfinadmin with password 'xxxxxx';
grant pspadm_readonly_role to pspqsfinadmin;
alter user pspqsfinadmin set search_path to pspadm;
--assign resource privilges for reports
alter role pspqsfinadmin set work_mem to '256MB';
alter role pspqsfinadmin set max_parallel_workers_per_gather to 8;
alter role pspqsfinadmin set enable_bitmapscan to off;
