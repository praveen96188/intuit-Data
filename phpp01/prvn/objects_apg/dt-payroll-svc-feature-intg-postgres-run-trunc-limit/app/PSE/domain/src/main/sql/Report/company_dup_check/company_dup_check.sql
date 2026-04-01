set lines 120
set pages 100
set feedback off
col FED_TAX_ID for a30
col SOURCE_SYSTEM_CD for a30

spool company_dup_check.log
SELECT   fed_tax_id, source_system_cd, COUNT (1)as CNT, max(created_date) AS maxcd
    FROM pspadm.psp_company c
   WHERE EXISTS (
            SELECT c.company_seq
              FROM pspadm.psp_company_service s
             WHERE c.company_seq = s.company_fk
               AND s.status_cd NOT IN ('Terminated', 'Cancelled'))
    AND source_system_cd <> 'IOP'
GROUP BY c.fed_tax_id, c.source_system_cd
  HAVING COUNT (1) > 1
  order by maxcd desc;
spool off
