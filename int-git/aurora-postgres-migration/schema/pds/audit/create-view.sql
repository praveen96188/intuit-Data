-- ------------ Write CREATE-VIEW-stage scripts -----------

CREATE OR REPLACE  VIEW ibobadm_pds.recalculatetotalsview (emp_totals_payroll_run_seq, rn) AS
SELECT
    et.emp_totals_payroll_run_seq, ROW_NUMBER() OVER (PARTITION BY et.company_fk, et.quarter_start_date ORDER BY et.modified_date DESC) AS rn
    FROM pspadm.psp_emp_totals_payroll_run AS et
    WHERE et.quarter_start_date = (SELECT
        aws_oracle_ext.TO_DATE('20160101 08:00:00', 'YYYYmmdd HH:MI:SS')) AND et.company_fk IN (SELECT DISTINCT
        c.company_seq
        FROM pspadm.psp_company AS c
        JOIN pspadm.psp_company_service AS cs
            ON cs.company_fk = c.company_seq
        JOIN pspadm.psp_tax_company_service_info AS tcs
            ON tcs.tax_company_service_info_seq = cs.company_service_seq
        JOIN pspadm.psp_company_agency AS ca
            ON ca.company_fk = c.company_seq
        JOIN pspadm.psp_company_law AS cl
            ON cl.company_agency_fk = ca.company_agency_seq AND cl.law_fk = 101 AND cl.status = 'Active' AND cl.is_archived = 0
        WHERE ((cs.status_cd = 'ActiveCurrent' AND cs.service_start_date IS NOT NULL) OR (cs.status_cd IN ('Terminated', 'Cancelled') AND tcs.last_quarter_to_file != 0)));

