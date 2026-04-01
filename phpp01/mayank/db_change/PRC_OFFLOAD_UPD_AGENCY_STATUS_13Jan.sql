set echo on timing on
set serveroutput on
spool PRC_OFFLOAD_UPD_AGENCY_STATUS_13Jan.log
BEGIN
	pspadm.PRC_OFFLOAD_UPD_AGENCY_STATUS(p_user_id=>'PSP-22133',
	p_app_server_date =>TO_TIMESTAMP('2022-01-12 08:00:00.000000', 'YYYY-MM-DD HH24:MI:SS.FF6'),
	p_offload_batch_id =>'60989a90-df5e-5eff-e053-5a048f0a4ea8',
	p_offload_date =>TO_TIMESTAMP('2021-10-26 07:00:00.000000', 'YYYY-MM-DD HH24:MI:SS.FF6'),
	p_file_type =>'Tax');
END;
/


spool off
