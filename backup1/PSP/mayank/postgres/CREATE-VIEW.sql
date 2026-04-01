-- ------------ Write CREATE-VIEW-stage scripts -----------

CREATE OR REPLACE VIEW pspadm.ht_psp_agency_check_batch (agency_check_batch_seq) AS
SELECT * FROM pspadm.ht_psp_agency_check_batch();



CREATE OR REPLACE VIEW pspadm.ht_psp_agency_id_requirement (agency_id_requirement_seq) AS
SELECT * FROM pspadm.ht_psp_agency_id_requirement();



CREATE OR REPLACE VIEW pspadm.ht_psp_bpcompany_service_info (bpcompany_service_info_seq) AS
SELECT * FROM pspadm.ht_psp_bpcompany_service_info();



CREATE OR REPLACE VIEW pspadm.ht_psp_cdcompany_service_info (cdcompany_service_info_seq) AS
SELECT * FROM pspadm.ht_psp_cdcompany_service_info();



CREATE OR REPLACE VIEW pspadm.ht_psp_check_print_batch (check_print_batch_seq) AS
SELECT * FROM pspadm.ht_psp_check_print_batch();



CREATE OR REPLACE VIEW pspadm.ht_psp_company_paycheck_batch (company_paycheck_batch_seq) AS
SELECT * FROM pspadm.ht_psp_company_paycheck_batch();



CREATE OR REPLACE VIEW pspadm.ht_psp_company_service (company_service_seq) AS
SELECT * FROM pspadm.ht_psp_company_service();



CREATE OR REPLACE VIEW pspadm.ht_psp_contact (contact_seq) AS
SELECT * FROM pspadm.ht_psp_contact();



CREATE OR REPLACE VIEW pspadm.ht_psp_ddcompany_service_info (ddcompany_service_info_seq) AS
SELECT * FROM pspadm.ht_psp_ddcompany_service_info();



CREATE OR REPLACE VIEW pspadm.ht_psp_dep_freq_ledger_operati (dep_freq_ledger_operation_seq) AS
SELECT * FROM pspadm.ht_psp_dep_freq_ledger_operati();



CREATE OR REPLACE VIEW pspadm.ht_psp_deposit_frequency_req (deposit_frequency_req_seq) AS
SELECT * FROM pspadm.ht_psp_deposit_frequency_req();



CREATE OR REPLACE VIEW pspadm.ht_psp_edi_tax_file (edi_tax_file_seq) AS
SELECT * FROM pspadm.ht_psp_edi_tax_file();



CREATE OR REPLACE VIEW pspadm.ht_psp_eftps_file (eftps_file_seq) AS
SELECT * FROM pspadm.ht_psp_eftps_file();



CREATE OR REPLACE VIEW pspadm.ht_psp_employee (employee_seq) AS
SELECT * FROM pspadm.ht_psp_employee();



CREATE OR REPLACE VIEW pspadm.ht_psp_individual (individual_seq) AS
SELECT * FROM pspadm.ht_psp_individual();



CREATE OR REPLACE VIEW pspadm.ht_psp_ledger_operation (ledger_operation_seq) AS
SELECT * FROM pspadm.ht_psp_ledger_operation();



CREATE OR REPLACE VIEW pspadm.ht_psp_manual_requirement (manual_requirement_seq) AS
SELECT * FROM pspadm.ht_psp_manual_requirement();



CREATE OR REPLACE VIEW pspadm.ht_psp_payment_method_requirem (payment_method_requirement_seq) AS
SELECT * FROM pspadm.ht_psp_payment_method_requirem();



CREATE OR REPLACE VIEW pspadm.ht_psp_payment_requirement (payment_requirement_seq) AS
SELECT * FROM pspadm.ht_psp_payment_requirement();



CREATE OR REPLACE VIEW pspadm.ht_psp_racompany_service_info (racompany_service_info_seq) AS
SELECT * FROM pspadm.ht_psp_racompany_service_info();



CREATE OR REPLACE VIEW pspadm.ht_psp_rate_ledger_operation (rate_ledger_operation_seq) AS
SELECT * FROM pspadm.ht_psp_rate_ledger_operation();



CREATE OR REPLACE VIEW pspadm.ht_psp_state_edi_tax_file (state_edi_tax_file_seq) AS
SELECT * FROM pspadm.ht_psp_state_edi_tax_file();



CREATE OR REPLACE VIEW pspadm.ht_psp_system_payment_requirem (system_payment_requirement_seq) AS
SELECT * FROM pspadm.ht_psp_system_payment_requirem();



CREATE OR REPLACE VIEW pspadm.ht_psp_system_requirement (system_requirement_seq) AS
SELECT * FROM pspadm.ht_psp_system_requirement();



CREATE OR REPLACE VIEW pspadm.ht_psp_tax_company_service_inf (tax_company_service_info_seq) AS
SELECT * FROM pspadm.ht_psp_tax_company_service_inf();



CREATE OR REPLACE VIEW pspadm.ht_psp_threshold_requirement (threshold_requirement_seq) AS
SELECT * FROM pspadm.ht_psp_threshold_requirement();



CREATE OR REPLACE VIEW pspadm.ht_psp_tp401kcompany_service_i (tp401kcompany_service_info_seq) AS
SELECT * FROM pspadm.ht_psp_tp401kcompany_service_i();



CREATE OR REPLACE VIEW pspadm.myview (emp_totals_payroll_run_seq, rn) AS
SELECT
    et.emp_totals_payroll_run_seq, ROW_NUMBER() OVER (PARTITION BY et.company_fk, et.quarter_start_date ORDER BY et.modified_date DESC) AS rn
    FROM pspadm.psp_emp_totals_payroll_run AS et
    WHERE et.quarter_start_date = (SELECT
        aws_oracle_ext.TO_DATE('20150701 07:00:00', 'YYYYmmdd HH:MI:SS')) AND et.company_fk IN (SELECT DISTINCT
        c.company_seq
        FROM pspadm.psp_company AS c
        JOIN pspadm.psp_company_service AS cs
            ON cs.company_fk = c.company_seq AND cs.service_fk = 'Tax' AND cs.status_cd NOT IN ('Terminated', 'Cancelled')
        JOIN pspadm.psp_company_agency AS ca
            ON ca.company_fk = c.company_seq
        JOIN pspadm.psp_company_law AS cl
            ON cl.company_agency_fk = ca.company_agency_seq AND cl.law_fk = 90::TEXT AND cl.status = 'Active' AND cl.is_archived = 0);



CREATE OR REPLACE VIEW pspadm.quest_sl_temp_explain1 (statement_id, plan_id, timestamp, remarks, operation, options, object_node, object_owner, object_name, object_alias, object_instance, object_type, optimizer, search_columns, id, parent_id, depth, position, cost, cardinality, bytes, other_tag, partition_start, partition_stop, partition_id, other, distribution, cpu_cost, io_cost, temp_space, access_predicates, filter_predicates, projection, time, qblock_name) AS
SELECT * FROM pspadm.quest_sl_temp_explain1();



CREATE OR REPLACE VIEW pspadm.quest_sl_temp_explain2 (statement_id, plan_id, timestamp, remarks, operation, options, object_node, object_owner, object_name, object_alias, object_instance, object_type, optimizer, search_columns, id, parent_id, depth, position, cost, cardinality, bytes, other_tag, partition_start, partition_stop, partition_id, other, other_xml, distribution, cpu_cost, io_cost, temp_space, access_predicates, filter_predicates, projection, time, qblock_name) AS
SELECT * FROM pspadm.quest_sl_temp_explain2();



CREATE OR REPLACE VIEW pspadm.recalculatetotalsview (emp_totals_payroll_run_seq, rn) AS
SELECT
    et.emp_totals_payroll_run_seq, ROW_NUMBER() OVER (PARTITION BY et.company_fk, et.quarter_start_date ORDER BY et.modified_date DESC) AS rn
    FROM pspadm.psp_emp_totals_payroll_run AS et
    WHERE et.quarter_start_date = (SELECT
        aws_oracle_ext.TO_DATE('20150701 07:00:00', 'YYYYmmdd HH:MI:SS')) AND et.company_fk IN (SELECT DISTINCT
        c.company_seq
        FROM pspadm.psp_company AS c
        JOIN pspadm.psp_company_service AS cs
            ON cs.company_fk = c.company_seq AND cs.service_fk = 'Tax' AND cs.status_cd NOT IN ('Terminated', 'Cancelled')
        JOIN pspadm.psp_company_agency AS ca
            ON ca.company_fk = c.company_seq
        JOIN pspadm.psp_company_law AS cl
            ON cl.company_agency_fk = ca.company_agency_seq AND cl.law_fk = 90::TEXT AND cl.status = 'Active' AND cl.is_archived = 0);



CREATE OR REPLACE VIEW pspadm.sys_temp_fbt (schema, object_name, "OBJECT#", rid, action) AS
SELECT * FROM pspadm.sys_temp_fbt();



CREATE OR REPLACE VIEW pspadm.temp_tax_token (company_seq, tax_token) AS
SELECT
    c.company_seq AS company_seq, MAX(ced2.value) AS tax_token
    FROM pspadm.psp_company_event AS ce
    JOIN pspadm.psp_company AS c
        ON c.company_seq = ce.company_fk
    JOIN pspadm.psp_company_event_detail AS ced
        ON ced.company_fk = c.company_seq
    JOIN pspadm.psp_company_event_detail AS ced2
        ON ced.company_fk = ced2.company_fk
    WHERE ce.event_type_cd = 'OFXServiceActivated' AND ce.status_cd = 'Active' AND ced.event_detail_type_cd = 'ServiceCode' AND ced.value = 'Tax' AND ced2.event_detail_type_cd = 'OFXToken'
    GROUP BY c.company_seq;



CREATE OR REPLACE VIEW pspadm.v_psp_contact (contact_seq, auth_signer_yn_ind, contact_role_cd, source_contact_id, title, title_suffix, job_title, fax, second_phone, company_fk, i_a_m_authentication_id, creator_id, created_date, modifier_id, modified_date) AS
SELECT
    contact_seq, auth_signer_yn_ind, contact_role_cd, source_contact_id, title, title_suffix, job_title, fax, second_phone, company_fk, i_a_m_authentication_id, creator_id, created_date, modifier_id, modified_date
    FROM pspadm.psp_individual AS i
    JOIN pspadm.psp_contact AS c
        ON i.individual_seq = c.contact_seq;



