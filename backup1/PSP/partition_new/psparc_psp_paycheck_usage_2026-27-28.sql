----------------------------------------psp_paycheck_usage-------------------------------------------

--DDL--

\timing
set search_path to psparc;

ALTER TABLE psparc.psp_paycheck_usage DETACH PARTITION psparc.psp_paycheck_usage_2026;

ALTER TABLE psparc.psp_paycheck_usage ATTACH PARTITION psparc.psp_paycheck_usage_2026 FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2027-01-01 00:00:00');


CREATE TABLE psparc.psp_paycheck_usage_2027 PARTITION OF psparc.psp_paycheck_usage FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2028-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2028 PARTITION OF psparc.psp_paycheck_usage FOR VALUES FROM ('2028-01-01 00:00:00') TO (MAXVALUE);

--CONSTRAINTS--

ALTER TABLE psparc.psp_paycheck_usage_2027 ADD CONSTRAINT psparc.psp_paycheck_usage_2027_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2028 ADD CONSTRAINT psparc.psp_paycheck_usage_2028_pk PRIMARY KEY (paycheck_usage_seq, realm_id);

--Index--

ALTER INDEX psparc.psp_paycheck_usage_2027_bill_fk_idx RENAME TO psparc.psp_paycheckusage_fk1_2027;
ALTER INDEX psparc.psp_paycheck_usage_2027_company_fk_idx RENAME TO psparc.psp_paycheck_usage_fk1_2027;
ALTER INDEX psparc.psp_paycheck_usage_2027_employee_usage_fk_idx RENAME TO psparc.psp_paycheckusage_fk2_2027;
ALTER INDEX psparc.psp_paycheck_usage_2027_employee_usage_fk_source_paycheck_i_idx RENAME TO psparc.psp_pa.ycheck_usage_u1_2027;
ALTER INDEX psparc.psp_paycheck_usage_2027_source_paycheck_id_idx RENAME TO psparc.psparcpsp_paycheck_usage_nu1_2027;
ALTER INDEX psparc.psp_paycheck_usage_2027_modified_date_idx RENAME TO psparc.idx_pchk_usg_mod_date_2027;

ALTER INDEX psparc.psp_paycheck_usage_2028_bill_fk_idx RENAME TO psparc.psp_paycheckusage_fk1_2028;
ALTER INDEX psparc.psp_paycheck_usage_2028_company_fk_idx RENAME TO psparc.psp_paycheck_usage_fk1_2028;
ALTER INDEX psparc.psp_paycheck_usage_2028_employee_usage_fk_idx RENAME TO psparc.psp_paycheckusage_fk2_2028;
ALTER INDEX psparc.psp_paycheck_usage_2028_employee_usage_fk_source_paycheck_i_idx RENAME TO psparc.psp_paycheck_usage_u1_2028;
ALTER INDEX psparc.psp_paycheck_usage_2028_source_paycheck_id_idx RENAME TO psparc.psp_paycheck_usage_nu1_2028;
ALTER INDEX psparc.psp_paycheck_usage_2028_modified_date_idx RENAME TO psparc.idx_pchk_usg_mod_date_2028;

--attach--

alter index psparc.psp_paycheckusage_fk1 attach partition psparc.psp_paycheckusage_fk1_2027;
alter index psparc.psp_paycheck_usage_fk1 attach partition psparc.psp_paycheck_usage_fk1_2027;
alter index psparc.psp_paycheckusage_fk2 attach partition psparc.psp_paycheckusage_fk2_2027;
alter index psparc.psp_paycheck_usage_u1 attach partition psparc.psp_paycheck_usage_u1_2027;
alter index psparc.psp_paycheck_usage_nu1 attach partition psparc.psp_paycheck_usage_nu1_2027;
ALTER INDEX psparc.idx_pchk_usg_mod_date attach  partition idx_pchk_usg_mod_date_2027;

alter index psparc.psp_paycheckusage_fk1 attach partition psparc.psp_paycheckusage_fk1_2028;
alter index psparc.psp_paycheck_usage_fk1 attach partition psparc.psp_paycheck_usage_fk1_2028;
alter index psparc.psp_paycheckusage_fk2 attach partition psparc.psp_paycheckusage_fk2_2028;
alter index psparc.psp_paycheck_usage_u1 attach partition psparc.psp_paycheck_usage_u1_2028;
alter index psparc.psp_paycheck_usage_nu1 attach partition psparc.psp_paycheck_usage_nu1_2028;
ALTER INDEX psparc.idx_pchk_usg_mod_date attach  partition psparc.idx_pchk_usg_mod_date_2028;

_____________________________________________________________________________________________________________