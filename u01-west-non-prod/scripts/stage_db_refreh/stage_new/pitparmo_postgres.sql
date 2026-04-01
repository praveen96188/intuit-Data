grant pspadm_readwrite_role to psp_prl_app;
alter user psp_prl_app set search_path to pspadm;
ALTER role psp_prl_app SET work_mem TO '16MB';
ALTER role psp_prl_app SET max_parallel_workers_per_gather TO 2;
--grant read only role
grant pspadm_readonly_role to psprjf;
--set search path
alter user psprjf set search_path to pspadm;
--set resource usage
ALTER role psprjf SET work_mem TO '64MB';
ALTER role psprjf SET max_parallel_workers_per_gather TO 4;
--grant read only role
grant pspadm_readwrite_role to pspbatch_rw_user;
--set search path
alter user pspbatch_rw_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_rw_user SET work_mem TO '64MB';
ALTER role pspbatch_rw_user SET max_parallel_workers_per_gather TO 4;
--grant read only role
grant pspadm_readonly_role to pspbatch_ro_user;
--set search path
alter user pspbatch_ro_user set search_path to pspadm;
--set resource usage
ALTER role pspbatch_ro_user SET work_mem TO '64MB';
ALTER role pspbatch_ro_user SET max_parallel_workers_per_gather TO 4;
