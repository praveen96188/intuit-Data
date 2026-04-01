set linesize 200
col psid format a10
col type format a25
col created_pst format a25
col modifier_id format a20
col modified_pst format a25
col status format a15


-- 1/24 = 1 hour, 1/48 = 30 mins, 1/19 = 15 mins
select 
	c.source_company_id as psid, 
	sst.type, 
	to_char(new_time(ur.created_date,'GMT','PST'),'YYYY-MM-DD HH12:MI:SS AM') as created_pst, 
	ur.modifier_id, 
	to_char(new_time(ur.modified_date,'GMT','PST'),'YYYY-MM-DD HH12:MI:SS AM') as modified_pst,
	ur.status, 
	ur.error_message
from (
  select /*+ INDEX(uri PSP_QBDT_UNPROCESSED_REQUE_I1) */ created_date, modifier_id, modified_date, status, error_message, source_system_transmission_fk, company_fk
  from pspadm.psp_qbdt_unprocessed_request uri
  where status in ('Processing','Error')
  and created_date <= new_time((systimestamp - 1/96),'PST','GMT')
  and created_date >= trunc(new_time((systimestamp - 5),'PST','GMT'))
) ur
  join pspadm.psp_company c on c.company_seq = ur.company_fk
  join pspadm.psp_source_system_transmission sst on sst.source_system_transmission_seq = ur.source_system_transmission_fk
/