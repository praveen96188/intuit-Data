
---- This is a post deploy script that needs to run after completion of  migratioin 

INSERT INTO PSP_COMPANYAGENCY_FRMTEMPLATE 
(COMPANYAGENCY_FRMTEMPLATE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, REALM_ID, EFFECTIVE_DATE, COMPANY_AGENCY_FK, FORM_TEMPLATE_FK)
(SELECT        
Fn_Format_Sysguid(sys_guid()), 0, SYSDATE, SYSDATE, -1, SYSDATE, ca.company_agency_seq, 'IRS-940-FILING'    
FROM PSP_COMPANY_AGENCY ca 
 WHERE ca.agency_fk = 'IRS' 
 AND (SELECT COUNT(*) 
        FROM PSP_COMPANYAGENCY_FRMTEMPLATE
        WHERE company_agency_fk = ca.company_agency_seq
        AND form_template_fk = 'IRS-940-FILING') = 0);

commit;