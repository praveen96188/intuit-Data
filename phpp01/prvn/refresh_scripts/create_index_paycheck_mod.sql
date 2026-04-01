\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

create index concurrently  idx_pchk_usg_mod_date_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (modified_date);


SELECT CURRENT_TIMESTAMP;
