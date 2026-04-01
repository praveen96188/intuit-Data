\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

CREATE INDEX  concurrently psp_hcm401k_company_policy_fk1
ON pspadm.psp_hcm401k_company_policy
USING BTREE (hcm401k_policy_fk , realm_id );

CREATE INDEX concurrently psp_hcm401k_company_qbdt_p_fk1
ON pspadm.psp_hcm401k_company_qbdt_pitem
USING BTREE (hcm401k_company_policy_fk , realm_id );

CREATE INDEX concurrently psp_hcm401k_employee_deduc_fk1
ON pspadm.psp_hcm401k_employee_deduction
USING BTREE (hcm401k_company_policy_fk , realm_id );


SELECT CURRENT_TIMESTAMP;
