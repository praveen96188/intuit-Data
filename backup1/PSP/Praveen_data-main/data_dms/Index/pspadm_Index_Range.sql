\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

--psp_entitlement_message
create index concurrently idx_ent_msg_mod_date_2012 on pspadm.psp_entitlement_message_2012  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2013 on pspadm.psp_entitlement_message_2013  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2014 on pspadm.psp_entitlement_message_2014  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2015 on pspadm.psp_entitlement_message_2015  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2016 on pspadm.psp_entitlement_message_2016  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2017 on pspadm.psp_entitlement_message_2017  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2018 on pspadm.psp_entitlement_message_2018  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2019 on pspadm.psp_entitlement_message_2019  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2020 on pspadm.psp_entitlement_message_2020  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2021 on pspadm.psp_entitlement_message_2021  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2022 on pspadm.psp_entitlement_message_2022  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2023 on pspadm.psp_entitlement_message_2023  USING BTREE (modified_date);
create index concurrently idx_ent_msg_mod_date_2024 on pspadm.psp_entitlement_message_2024  USING BTREE (modified_date);

create index idx_ent_msg_mod_date ON ONLY  pspadm.psp_entitlement_message USING BTREE (modified_date);
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2012 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2013 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2014 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2015 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2016 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2017 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2018 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2019 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2020 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2021 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2022 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2023 ;
alter index idx_ent_msg_mod_date attach partition  pspadm.idx_ent_msg_mod_date_2024 ;


--psp_paycheck_usage
create index concurrently idx_pchk_usg_mod_date_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (modified_date);
create index concurrently idx_pchk_usg_mod_date_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (modified_date);

create index idx_pchk_usg_mod_date ON ONLY  pspadm.psp_paycheck_usage USING BTREE (modified_date);
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2012 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2013 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2014 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2015 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2016 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2017 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2018 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2019 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2020 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2021 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2022 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2023 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2024 ;

--psp_entity_update

create index concurrently idx_ent_upd_mod_date_m112022 on pspadm.psp_entity_update_m112022 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m122022 on pspadm.psp_entity_update_m122022 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m012023 on pspadm.psp_entity_update_m012023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m022023 on pspadm.psp_entity_update_m022023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m032023 on pspadm.psp_entity_update_m032023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m042023 on pspadm.psp_entity_update_m042023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m052023 on pspadm.psp_entity_update_m052023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m062023 on pspadm.psp_entity_update_m062023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m072023 on pspadm.psp_entity_update_m072023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m082023 on pspadm.psp_entity_update_m082023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m092023 on pspadm.psp_entity_update_m092023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m102023 on pspadm.psp_entity_update_m102023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m112023 on pspadm.psp_entity_update_m112023 USING BTREE (modified_date);
create index concurrently idx_ent_upd_mod_date_m122023 on pspadm.psp_entity_update_m122023 USING BTREE (modified_date);

create index idx_ent_upd_mod_date ON ONLY  pspadm.psp_entity_update USING BTREE (modified_date);
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112022;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122022;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112023;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122023;


--Non-partition tables
create index concurrently idx_prl_run_mod_date on pspadm.psp_payroll_run  USING BTREE (modified_date);
create index concurrently idx_bill_det_mod_date on pspadm.psp_billing_detail  USING BTREE (modified_date);
create index concurrently idx_cdl_mod_date on pspadm.psp_company_daily_liability  USING BTREE (modified_date);
create index concurrently idx_csba_mod_date on pspadm.psp_company_service_bank_acct  USING BTREE (modified_date);
create index concurrently idx_elqt_mod_date on pspadm.psp_employee_law_qtr_totals  USING BTREE (modified_date);
create index concurrently idx_epi_mod_date on pspadm.psp_employee_payroll_item  USING BTREE (modified_date);
create index concurrently idx_emp_tax_mod_date on 	pspadm.psp_employee_tax  USING BTREE (modified_date);
create index concurrently idx_emp_cont_mod_date on pspadm.psp_employer_contribution  USING BTREE (modified_date);
create index concurrently idx_individual_mod_date on pspadm.psp_individual  USING BTREE (modified_date);
create index concurrently idx_pdditem_mod_date on pspadm.psp_pstub_dditem  USING BTREE (modified_date);
create index concurrently idx_ttmd_mod_date on pspadm.psp_tax_table_misc_data  USING BTREE (modified_date);
create index concurrently idx_ewt_mod_date on pspadm.psp_employee_w2_totals  USING BTREE (modified_date);

SELECT CURRENT_TIMESTAMP;
