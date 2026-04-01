set timing on
set serveroutput on
set lines 5000 
select  /*+parallel(8)*/  company.source_company_id,replace(company.legal_name,',',null) legal_name,pca.agency_taxpayer_id_enc,pca.payment_template_fk,cl.source_description, (case when (companyagency.agency_fk)='IRS' then 'FEDERAL'   when   companyagency.agency_fk='NOCAL' then 'Null'  else companyagency.agency_fk end) tax_type,
primaryprincipal.first_name ||' '|| primaryprincipal.last_name primary_principal_name,  primaryprincipal.email primary_principal_email, PRIMARYPRINCIPAL.PHONE primary_principal_phone,
adminprincipal.first_name || ' '|| adminprincipal.last_name payroll_admin_name ,adminprincipal.email payroll_admin_email, legaladdress.address_line1 || ' ' || legaladdress.address_line2 || ' ' || legaladdress.address_line3  legal_address,legaladdress.city City,legaladdress.state State, legaladdress.zip_code || legaladdress.zip_code_extension ZipCode , to_char(eventtype.event_time_stamp,'MM-DD-YYYY') event_date
from pspadm.psp_entitlement_unit eu ,
pspadm.psp_company company,
pspadm.psp_company_service compservices,
pspadm.psp_address legaladdress,
pspadm.psp_contact contact,
pspadm.psp_individual primaryprincipal,
pspadm.psp_contact adminncontact,
pspadm.psp_individual adminprincipal,
pspadm.psp_company_event eventtype,
pspadm.psp_company_agency companyagency,
pspadm.psp_company_law cl,
pspadm.psp_companyagency_pmttemplate pca
where eu.entitlement_unit_status ='Activated'
and eu.company_fk = company.company_seq
and companyagency.company_fk=company.company_seq
and eu.company_fk =compservices.company_fk
and legaladdress.address_seq=company.legal_address_fk
and compservices.status_cd='ActiveCurrent'
and compservices.service_fk = 'Tax'
and contact.company_fk=company.company_seq and contact.contact_role_cd='PrimaryPrincipal'
and primaryprincipal.individual_seq = contact.contact_seq
and adminncontact.company_fk=company.company_seq and adminncontact.contact_role_cd='PayrollAdmin'
and adminprincipal.individual_seq = adminncontact.contact_seq
and companyagency.company_agency_seq=cl.company_agency_fk
and eventtype.company_fk=company.company_seq
and companyagency.company_agency_seq=pca.company_agency_fk
and cl.reimbursable_status='NotReimbursable'
and cl.filing_status='Active'
and cl.exemption_status='NonExempt'
and eventtype.company_event_seq in (select  pce.company_event_seq from pspadm.psp_company_event pce, pspadm.psp_company_event_detail  oldpced ,pspadm.psp_company_event_detail  newpced, pspadm.psp_company_event_detail servicecd
where  ( oldpced.event_detail_type_cd='OldServiceStatus' and oldpced.value like 'Pending%') and  pce.company_event_seq=oldpced.company_event_fk
and ( newpced.event_detail_type_cd='NewServiceStatus' and newpced.value='Active Current' ) and  pce.company_event_seq=newpced.company_event_fk
and (servicecd.event_detail_type_cd='ServiceCode' and servicecd.value='Assisted Service') and  pce.company_event_seq=servicecd.company_event_fk
and pce.event_type_cd='ServiceStatusChange' and pce.created_date>=trunc(sysdate-1));

