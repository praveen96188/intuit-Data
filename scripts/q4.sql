exec qbo_spm.CREATE_DBL('C29');
create public database link qbowrpt using '(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = qbowrpt.sbg-qbo-prod.a.intuit.com)(PORT = 1521)) (CONNECT_DATA = (SERVER = DEDICATED) (SERVICE_NAME = qbowrpt)))';
set pages 0
set lines 3000   
   SELECT  P.COMPANY_ID ||','||
        P.PURCHASE_DATE ||','||
        P.EXPIRY_DATE ||','||
        R.CANCEL_DATE ||','||
        R.REGION_ID ||','||
        CASE WHEN R.CANCEL_DATE is NULL THEN 'ACTIVE' ELSE 'CANCELLED' END ||','||
        P.COMPANY_ID
FROM qbo_data.partnerbillingreceipt_1 P, qborpt.rptcompanyids_1@qbowrpt R 
 WHERE P.company_id(+)=R.company_id  AND P.is_active(+)=1
   AND R.qbo_partner in (5,6,7,8,9)
   AND R.company_id in (select company_id from kpopat.billing_issue@spm_c29_las where cluster_id = &c)
/
