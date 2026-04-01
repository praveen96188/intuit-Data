set serverout on size unl
begin
delete KPOPAT.COMPANY_ID_NIKHIL_OUTPUT ;
for i in (select * from sbg_vdba.spm_qbo_clusters where cluster_id not in ('C60')) loop
-- create table kpopat.company_id_nikhil_output as
execute immediate ( '
INSERT INTO KPOPAT.COMPANY_ID_NIKHIL_OUTPUT 
SELECT '''||i.cluster_id||''' cluster_id, c.company_id,
       a.postal_code
  FROM QBO_Data.CompanyInfo_1@spm_'||i.cluster_id||'_uw2 c, 
       QBO_Data.Addresses_1@spm_'||i.cluster_id||'_uw2 a 
 WHERE c.company_id IN (select company_id from KPOPAT.COMPANY_ID_NIKHIL)
   AND c.legal_address_id = a.address_id
   AND c.company_id = a.company_id
   AND EXISTS (select 1 
                from QBO_Data.CompanyInfo_1@spm_'||i.cluster_id||'_uw2 c1, 
                     QBO_Data.Addresses_1@spm_'||i.cluster_id||'_uw2 a1
               WHERE c1.company_id= a1.company_id 
                 AND c1.company_id = c.company_id
                 AND c1.address_id = a1.address_id)');
commit;                 
end loop;
end;
/
