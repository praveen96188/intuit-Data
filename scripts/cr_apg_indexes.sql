\timing
set search_path=pspadm;
create index concurrently psp_userrole_fk_role on pspadm.psp_auth_user_auth_role__assoc (auth_role_fk);
create index concurrently psp_userrole_fk_user on pspadm.psp_auth_user_auth_role__assoc (auth_user_fk);
--create index concurrently psp_company_event_email_fk1 on pspadm.psp_company_event_email (company_fk,company_event_fk);
--create index concurrently status_cd_idx on pspadm.psp_company_event_email (status_cd);
--create index concurrently psp_paycheck_usage_hist_fk2 on pspadm.psp_paycheck_usage_hist (company_fk,paycheck_usage_fk);
--create index concurrently psp_paycheck_usage_hist_fk3 on pspadm.psp_paycheck_usage_hist (company_fk);
