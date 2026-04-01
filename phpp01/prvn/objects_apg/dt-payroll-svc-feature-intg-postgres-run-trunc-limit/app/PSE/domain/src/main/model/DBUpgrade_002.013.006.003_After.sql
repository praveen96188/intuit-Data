--
-- This script will be executed AFTER the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.006.003.sql
--
-- Developers can hand code logic here for data migration purposes
--

Prompt sync flags merging started;

update psp_qbdt_paycheck_info
set token = -token-1
where token<-2;

commit;

update psp_qbdt_employee_info
set is_assisted =  1
where is_recoverable = 1;

commit;

UPDATE psp_qbdt_paycheck_info pqi
   SET is_assisted = 1
WHERE EXISTS
          (SELECT 1
             FROM (  SELECT MAX (VALUE) tax_token, ce.company_fk
                       FROM pspadm.psp_company_event ce,
                            pspadm.psp_company_event_detail ced
                      WHERE     ce.company_event_seq = ced.company_event_fk
                            AND ce.event_type_cd = 'OFXServiceActivated'
                            AND ce.status_cd = 'Active'
                            AND ced.event_detail_type_cd = 'OFXToken'
                            AND EXISTS
                                   (SELECT 1
                                      FROM pspadm.psp_company_event_detail ced1
                                     WHERE ced1.company_event_fk =
                                              ced.company_event_fk
                                           AND ced1.event_detail_type_cd =
                                                  'ServiceCode'
                                           AND ced1.VALUE = 'Tax')
                   GROUP BY ce.company_fk) a
            WHERE a.company_fk = pqi.company_fk AND pqi.token > a.tax_token);


commit; 

UPDATE psp_qbdt_paycheck_info pqi
   SET is_assisted = 0
WHERE EXISTS
          (SELECT 1
             FROM psp_paycheck pp
             where pp.PAYCHECK_SEQ = pqi.paycheck_fk
             and pp.source_paycheck_id like '-%');

commit;
       

Prompt sync flags merging done;