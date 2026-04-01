

select pg_terminate_backend(pid)
from pg_stat_activity
where pid in (select pid
              FROM pg_stat_activity
              where usename like '%psp_prl_app%');




 select count(*) from ibobadm.psp_qbdt_request_info  ;
 select count(*) from ibobadm.psp_hcm401k_company_policy; 
 select count(*) from ibobadm.psp_hcm401k_company_qbdt_pitem; 
 select count(*) from ibobadm.psp_hcm401k_employee_deduction ;
 select count(*) from ibobadm.psp_hcm401k_policy ;


 select count(*) from pspadm.psp_qbdt_request_info  ;
 select count(*) from pspadm.psp_hcm401k_company_policy; 
 select count(*) from pspadm.psp_hcm401k_company_qbdt_pitem; 
 select count(*) from pspadm.psp_hcm401k_employee_deduction ;
 select count(*) from pspadm.psp_hcm401k_policy ;



 --audit

revoke SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_qbdt_request_info  from ibob_prl_pspapp;
revoke SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_company_policy  from ibob_prl_pspapp;
revoke SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_company_qbdt_pitem  from ibob_prl_pspapp;
revoke SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_employee_deduction  from ibob_prl_pspapp;
revoke SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_policy  from ibob_prl_pspapp;

GRANT SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_qbdt_request_info  TO ibob_prl_pspapp;
GRANT SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_company_policy  TO ibob_prl_pspapp;
GRANT SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_company_qbdt_pitem  TO ibob_prl_pspapp;
GRANT SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_employee_deduction  TO ibob_prl_pspapp;
GRANT SELECT, INSERT, DELETE, UPDATE ON ibobadm.psp_hcm401k_policy  TO ibob_prl_pspapp;
