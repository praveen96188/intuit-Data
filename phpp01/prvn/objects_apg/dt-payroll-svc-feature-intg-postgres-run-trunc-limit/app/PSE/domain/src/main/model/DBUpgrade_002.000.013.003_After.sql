--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\PSP\rel-1.13\PSE\Domain\src\main\model\DBUpgrade_002.000.013.003.sql
--
-- Developers can hand code logic here for data migration purposes
--
-- Iowa SUI Payment Template Agency ID records were pointing at the WH templates.
update PSP_COMP_PMT_TEMPLATE_AGENCYID cptaid set CPTAID.COMPANY_AGENCY_PMT_TEMPLATE_FK =      
    (select SUICAPT.COMPANYAGENCY_PMTTEMPLATE_SEQ
        from PSP_COMPANYAGENCY_PMTTEMPLATE suicapt
            join psp_company_agency suica on suica.COMPANY_AGENCY_SEQ = SUICAPT.COMPANY_AGENCY_FK
            join psp_company_agency whca on WHCA.COMPANY_FK = SUICA.COMPANY_FK
            join PSP_COMPANYAGENCY_PMTTEMPLATE whcapt on WHCAPT.COMPANY_AGENCY_FK = WHCA.COMPANY_AGENCY_SEQ
        where SUICAPT.PAYMENT_TEMPLATE_FK = 'IA-600103-PAYMENT'
            and whcapt.PAYMENT_TEMPLATE_FK = 'IA-44105-PAYMENT'
            and cptaid.COMPANY_AGENCY_PMT_TEMPLATE_FK = WHCAPT.COMPANYAGENCY_PMTTEMPLATE_SEQ)
    where CPTAID.NAME = 'Client Bank Acct'
/
