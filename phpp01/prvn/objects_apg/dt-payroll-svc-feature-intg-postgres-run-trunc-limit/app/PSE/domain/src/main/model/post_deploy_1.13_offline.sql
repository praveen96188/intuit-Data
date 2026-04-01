---- This is a post deploy script that needs to run after deploying rel-1.13 (offline)

update PSP_COMPANYAGENCY_PMTTEMPLATE capt 
set capt.MODIFIED_DATE = SYS_EXTRACT_UTC(SYSTIMESTAMP), capt.MODIFIER_ID='POST-DEPLOY-rel-1.13',
	capt.agency_taxpayer_id = (select AGENCY_TAXPAYER_ID 
                               from TEMP_POPULATE_AGENCY_ID t 
                               where t.COMPANYAGENY_PMTTEMPLATE_FK = capt.COMPANYAGENCY_PMTTEMPLATE_SEQ 
                               and source_id = (select max(source_id) from TEMP_POPULATE_AGENCY_ID tt where tt.COMPANYAGENY_PMTTEMPLATE_FK = T.COMPANYAGENY_PMTTEMPLATE_FK));

commit;							   
