-- Oracle(A):

select count(*) from IBOBADM.PSP_HCM401K_POLICY;
select count(*) from IBOBADM.PSP_HCM401K_EMPLOYEE_DEDUCTION;
select count(*) from IBOBADM.PSP_HCM401K_COMPANY_QBDT_PITEM;
select count(*) from IBOBADM.PSP_HCM401K_COMPANY_POLICY;
select count(*) from IBOBADM.PSP_QBDT_REQUEST_INFO  WHERE created_date  between  TRUNC(SYSDATE, 'MM') and LAST_DAY(sysdate)+1;
select count(*) from IBOBADM.PSP_SAP_METHOD_CALL  WHERE created_date  between  TRUNC(SYSDATE, 'MM') and LAST_DAY(sysdate)+1;
select count(*) from IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION  WHERE created_date  between  TRUNC(SYSDATE, 'MM') and LAST_DAY(sysdate)+1;


-- Postgres(B):

select count(*) from ibobadm.psp_hcm401k_policy;
select count(*) from ibobadm.psp_hcm401k_employee_deduction;
select count(*) from ibobadm.psp_hcm401k_company_qbdt_pitem;
select count(*) from ibobadm.psp_hcm401k_company_policy;
select count(*) from ibobadm.psp_qbdt_request_info  where created_date  between cast(date_trunc('MONTH', current_date) as date) and ((cast(date_trunc('MONTH', current_date) as date) + INTERVAL '1 MONTH day') ::date);
select count(*) from ibobadm.psp_sap_method_call  where created_date  between cast(date_trunc('MONTH', current_date) as date) and ((cast(date_trunc('MONTH', current_date) as date) + INTERVAL '1 MONTH day') ::date);
select count(*) from ibobadm.psp_source_system_transmission  where created_date  between cast(date_trunc('MONTH', current_date) as date) and ((cast(date_trunc('MONTH', current_date) as date) + INTERVAL '1 MONTH day') ::date);

-- Oracle(C):

select count(*) from IBOBADM_APG.PSP_HCM401K_POLICY;
select count(*) from IBOBADM_APG.PSP_HCM401K_EMPLOYEE_DEDUCTION;
select count(*) from IBOBADM_APG.PSP_HCM401K_COMPANY_QBDT_PITEM;
select count(*) from IBOBADM_APG.PSP_HCM401K_COMPANY_POLICY;
select count(*) from IBOBADM_APG.PSP_QBDT_REQUEST_INFO  WHERE created_date  between  TRUNC(SYSDATE, 'MM') and LAST_DAY(sysdate)+1;
select count(*) from IBOBADM_APG.PSP_SAP_METHOD_CALL  WHERE created_date  between  TRUNC(SYSDATE, 'MM') and LAST_DAY(sysdate)+1;
select count(*) from IBOBADM_APG.PSP_SOURCE_SYSTEM_TRANSMISSION  WHERE created_date  between  LAST_DAY(sysdate)+1;
