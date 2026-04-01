---- This is a post deploy script that needs to run after deploying rel-1.13 (online)

Update PSP_PROPERTY_AUDIT pa 
set pa.class_name='CompanyAgencyPaymentTemplate',
	pa.MODIFIED_DATE = SYS_EXTRACT_UTC(SYSTIMESTAMP), pa.MODIFIER_ID='POST-DEPLOY-rel-1.13', 
    object_identifier = (select t.COMPANYAGENY_PMTTEMPLATE_FK 
                         from TEMP_POPULATE_AGENCY_ID t 
						 where t.company_agency_fk = pa.object_identifier 
                         and source_id = (select max(source_id) from TEMP_POPULATE_AGENCY_ID tt where tt.company_agency_fk = t.company_agency_fk)) 
    where  property_name='AgencyTaxpayerId' and class_name='CompanyAgency';

DROP TABLE TEMP_POPULATE_AGENCY_ID;

commit;
