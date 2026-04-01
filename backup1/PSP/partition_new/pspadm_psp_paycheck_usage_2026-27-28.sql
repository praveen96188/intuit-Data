----------------------------------------psp_paycheck_usage-------------------------------------------

--DDL--

\timing
set search_path to pspadm;

ALTER TABLE pspadm.psp_paycheck_usage DETACH PARTITION pspadm.psp_paycheck_usage_2026;

ALTER TABLE pspadm.psp_paycheck_usage ATTACH PARTITION pspadm.psp_paycheck_usage_2026 FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2027-01-01 00:00:00');


CREATE TABLE pspadm.psp_paycheck_usage_2027 PARTITION OF pspadm.psp_paycheck_usage FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2028-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2028 PARTITION OF pspadm.psp_paycheck_usage FOR VALUES FROM ('2028-01-01 00:00:00') TO (MAXVALUE);

--CONSTRAINTS--

ALTER TABLE pspadm.psp_paycheck_usage_2027 ADD CONSTRAINT pspadm.psp_paycheck_usage_2027_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2028 ADD CONSTRAINT pspadm.psp_paycheck_usage_2028_pk PRIMARY KEY (paycheck_usage_seq, realm_id);

--Index--

ALTER INDEX pspadm.psp_paycheck_usage_2027_bill_fk_idx RENAME TO pspadmpsp_paycheckusage_fk1_2027;
ALTER INDEX pspadm.psp_paycheck_usage_2027_company_fk_idx RENAME TO pspadm.psp_paycheck_usage_fk1_2027;
ALTER INDEX pspadm.psp_paycheck_usage_2027_employee_usage_fk_idx RENAME TO pspadm.psp_paycheckusage_fk2_2027;
ALTER INDEX pspadm.psp_paycheck_usage_2027_employee_usage_fk_source_paycheck_i_idx RENAME TO pspadm.psp_paycheck_usage_u1_2027;
ALTER INDEX pspadm.psp_paycheck_usage_2027_source_paycheck_id_idx RENAME TO pspadm.psp_paycheck_usage_nu1_2027;
ALTER INDEX pspadm.psp_paycheck_usage_2027_modified_date_idx RENAME TO pspadm.idx_pchk_usg_mod_date_2027;

ALTER INDEX pspadm.psp_paycheck_usage_2028_bill_fk_idx RENAME TO pspadm.psp_paycheckusage_fk1_2028;
ALTER INDEX pspadm.psp_paycheck_usage_2028_company_fk_idx RENAME TO pspadm.psp_paycheck_usage_fk1_2028;
ALTER INDEX pspadm.psp_paycheck_usage_2028_employee_usage_fk_idx RENAME TO pspadm.psp_paycheckusage_fk2_2028;
ALTER INDEX pspadm.psp_paycheck_usage_2028_employee_usage_fk_source_paycheck_i_idx RENAME TO pspadm.psp_paycheck_usage_u1_2028;
ALTER INDEX pspadm.psp_paycheck_usage_2028_source_paycheck_id_idx RENAME TO pspadm.psp_paycheck_usage_nu1_2028;
ALTER INDEX pspadm.psp_paycheck_usage_2028_modified_date_idx RENAME TO pspadm.idx_pchk_usg_mod_date_2028;

--attach--

alter index pspadm.psp_paycheckusage_fk1 attach partition pspadm.psp_paycheckusage_fk1_2027;
alter index pspadm.psp_paycheck_usage_fk1 attach partition pspadm.psp_paycheck_usage_fk1_2027;
alter index pspadm.psp_paycheckusage_fk2 attach partition pspadm.psp_paycheckusage_fk2_2027;
alter index pspadm.psp_paycheck_usage_u1 attach partition pspadm.psp_paycheck_usage_u1_2027;
alter index pspadm.psp_paycheck_usage_nu1 attach partition pspadm.psp_paycheck_usage_nu1_2027;
ALTER INDEX pspadm.idx_pchk_usg_mod_date attach  partition pspadm.idx_pchk_usg_mod_date_2027;

alter index pspadm.psp_paycheckusage_fk1 attach partition pspadm.psp_paycheckusage_fk1_2028;
alter index pspadm.psp_paycheck_usage_fk1 attach partition pspadm.psp_paycheck_usage_fk1_2028;
alter index pspadm.psp_paycheckusage_fk2 attach partition pspadm.psp_paycheckusage_fk2_2028;
alter index pspadm.psp_paycheck_usage_u1 attach partition pspadm.psp_paycheck_usage_u1_2028;
alter index pspadm.psp_paycheck_usage_nu1 attach partition pspadm.psp_paycheck_usage_nu1_2028;
ALTER INDEX pspadm.idx_pchk_usg_mod_date attach  partition pspadm.idx_pchk_usg_mod_date_2028;

_____________________________________________________________________________________________________________