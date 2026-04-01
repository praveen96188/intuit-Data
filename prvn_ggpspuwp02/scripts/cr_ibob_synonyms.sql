set echo on feedback on
create or replace synonym &app_user_name.PSP_QBDT_REQUEST_INFO for IBOBADM.PSP_QBDT_REQUEST_INFO;
create or replace synonym &&app_user_name.PSP_SAP_METHOD_CALL for IBOBADM.PSP_SAP_METHOD_CALL;
create or replace synonym &&app_user_name.PSP_SOURCE_SYSTEM_TRANSMISSION for IBOBADM.PSP_SOURCE_SYSTEM_TRANSMISSION;
create or replace synonym &&app_user_name.PSP_HCM401K_COMPANY_POLICY for IBOBADM.PSP_HCM401K_COMPANY_POLICY;
create or replace synonym &&app_user_name.PSP_HCM401K_COMPANY_QBDT_PITEM for IBOBADM.PSP_HCM401K_COMPANY_QBDT_PITEM;
create or replace synonym &&app_user_name.PSP_HCM401K_EMPLOYEE_DEDUCTION for IBOBADM.PSP_HCM401K_EMPLOYEE_DEDUCTION;
create or replace synonym &&app_user_name.PSP_HCM401K_POLICY for IBOBADM.PSP_HCM401K_POLICY;

