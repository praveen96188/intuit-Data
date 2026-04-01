spool all_co_ad.txt
set echo off
set pages 0
set trimspool on
select company_id||','||ASSIGNED_DATE  from kpopat.all_companies a where exists (select 1 from rptcompanyids_1@qbowrpt b where a.cluster_id = b.company_cluster_id and a.company_id=b.company_id)
/
spool off
