-- ------------ Write CREATE-VIEW-stage scripts -----------

CREATE OR REPLACE  VIEW pspadm.psp_employee_view (employee_seq, realm_id, source_employee_id, status_cd, status_effective_date, company_fk, hire_date, re_hire_date, fed_filing_status, work_state, termination_date, fed_allowances, is_statutory, has_retirement_plan, has_third_party_sick_pay, tp_401k_info_is_hce, tp_401k_info_owner_percent, tp_401k_info_is_family_member, tp_401k_info_last_upload_date, is_cloud_employee, is_deceased, qualifies_for_aeic, fed_extra_withholding, live_state, pay_period, is_archived, consumer_realm_id, is_viewing_paystub_disabled, birth_date_enc, tax_id_enc, persona_id, birth_date, tax_id, fed_claim_dependents, fed_other_income, fed_deductions, fed_multiple_jobs, fed_w4_employee_pref, modified_date, created_date) AS
SELECT
    e.employee_seq, e.realm_id, e.source_employee_id, e.status_cd, e.status_effective_date, e.company_fk, e.hire_date, e.re_hire_date, e.fed_filing_status, e.work_state, e.termination_date, e.fed_allowances, e.is_statutory, e.has_retirement_plan, e.has_third_party_sick_pay, e.tp_401k_info_is_hce, e.tp_401k_info_owner_percent, e.tp_401k_info_is_family_member, e.tp_401k_info_last_upload_date, e.is_cloud_employee, e.is_deceased, e.qualifies_for_aeic, e.fed_extra_withholding, e.live_state, e.pay_period, e.is_archived, e.consumer_realm_id, e.is_viewing_paystub_disabled, e.birth_date_enc, e.tax_id_enc, e.persona_id, e.birth_date, e.tax_id, e.fed_claim_dependents, e.fed_other_income, e.fed_deductions, e.fed_multiple_jobs, e.fed_w4_employee_pref, i.modified_date, i.created_date
    FROM pspadm.psp_employee AS e, pspadm.psp_individual AS i
    WHERE e.employee_seq = i.individual_seq;



CREATE OR REPLACE  VIEW pspadm.recalculatetotalsview (emp_totals_payroll_run_seq, rn) AS
SELECT
    et.emp_totals_payroll_run_seq, ROW_NUMBER() OVER (PARTITION BY et.company_fk, et.quarter_start_date ORDER BY et.modified_date DESC) AS rn
    FROM pspadm.psp_emp_totals_payroll_run AS et
    WHERE et.quarter_start_date = (SELECT
        aws_oracle_ext.TO_DATE('20160401 07:00:00', 'YYYYmmdd HH:MI:SS')) AND et.company_fk IN (SELECT DISTINCT
        c.company_seq
        FROM pspadm.psp_company AS c
        JOIN pspadm.psp_company_service AS cs
            ON cs.company_fk = c.company_seq
        JOIN pspadm.psp_tax_company_service_info AS tcs
            ON tcs.tax_company_service_info_seq = cs.company_service_seq
        JOIN pspadm.psp_company_agency AS ca
            ON ca.company_fk = c.company_seq
        JOIN pspadm.psp_company_law AS cl
            ON cl.company_agency_fk = ca.company_agency_seq AND cl.law_fk = 130::TEXT AND cl.status = 'Active' AND cl.is_archived = 0
        WHERE ((cs.status_cd = 'ActiveCurrent' AND cs.service_start_date IS NOT NULL) OR (cs.status_cd IN ('Terminated', 'Cancelled') AND tcs.last_quarter_to_file != 0)));



CREATE OR REPLACE  VIEW pspadm.v_psp_bank_account (bank_account_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, account_type_cd, bank_name, effective_date, expiration_date, routing_number, a_c_h_entry_class, paycard_flag) AS
SELECT
    bank_account_seq, version, creator_id, created_date, modifier_id, modified_date, realm_id, account_type_cd, bank_name, effective_date, expiration_date, routing_number, a_c_h_entry_class,
    CASE aws_oracle_ext.substr(account_number, 1, 3)
        WHEN '968' THEN 1
        ELSE 0
    END *
    CASE routing_number
        WHEN '031101169' THEN 1
        ELSE 0
    END AS paycard_flag
    FROM pspadm.psp_bank_account;



CREATE OR REPLACE  VIEW pspadm.v_psp_contact (contact_seq, realm_id, auth_signer_yn_ind, contact_role_cd, source_contact_id, title, title_suffix, job_title, fax, second_phone, company_fk, i_a_m_authentication_id, creator_id, created_date, modifier_id, modified_date) AS
SELECT
    contact_seq, c.realm_id, auth_signer_yn_ind, contact_role_cd, source_contact_id, title, title_suffix, job_title, fax, second_phone, company_fk, i_a_m_authentication_id, creator_id, created_date, modifier_id, modified_date
    FROM pspadm.psp_individual AS i
    JOIN pspadm.psp_contact AS c
        ON i.individual_seq = c.contact_seq;



CREATE OR REPLACE  VIEW pspadm.v_psp_employee (employee_seq, realm_id, source_employee_id, status_cd, status_effective_date, tax_id, company_fk, hire_date, re_hire_date, fed_filing_status, work_state, termination_date, fed_allowances, is_statutory, has_retirement_plan, has_third_party_sick_pay, birth_date, tp_401k_info_is_hce, tp_401k_info_owner_percent, tp_401k_info_is_family_member, tp_401k_info_last_upload_date, is_cloud_employee, is_deceased, qualifies_for_aeic, fed_extra_withholding, live_state, pay_period, is_archived, consumer_realm_id, creator_id, created_date, modifier_id, modified_date, is_viewing_paystub_disabled) AS
SELECT
    employee_seq, emp.realm_id, source_employee_id, status_cd, status_effective_date, tax_id, company_fk, hire_date, re_hire_date, fed_filing_status, work_state, termination_date, fed_allowances, is_statutory, has_retirement_plan, has_third_party_sick_pay, birth_date, tp_401k_info_is_hce, tp_401k_info_owner_percent, tp_401k_info_is_family_member, tp_401k_info_last_upload_date, is_cloud_employee, is_deceased, qualifies_for_aeic, fed_extra_withholding, live_state, pay_period, is_archived, consumer_realm_id, is_viewing_paystub_disabled AS creator_id, created_date, modifier_id, modified_date, is_viewing_paystub_disabled
    FROM pspadm.psp_employee AS emp
    JOIN pspadm.psp_individual AS ind
        ON emp.employee_seq = ind.individual_seq;



