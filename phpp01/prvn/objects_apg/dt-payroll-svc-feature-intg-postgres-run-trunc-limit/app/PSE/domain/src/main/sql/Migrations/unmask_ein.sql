/* Formatted on 9/16/2011 2:19:51 PM (QP5 v5.163.1008.3004) */

/* Formatted on 9/16/2011 2:19:47 PM (QP5 v5.163.1008.3004) */
UPDATE PSP_COMPANY_SERVICE PCE
   SET STATUS_CD = 'ActiveCurrent'
 WHERE SERVICE_FK = 'Cloud'
       AND EXISTS
              (SELECT 't'
                 FROM psp_company pc
                WHERE PC.COMPANY_SEQ = PCE.COMPANY_FK
                      AND PC.FED_TAX_ID LIKE 'CRIS-%'); 

UPDATE PSP_COMPANY PC
   SET PC.FED_TAX_ID = SUBSTR (PC.FED_TAX_ID, -9)
 WHERE FED_TAX_ID LIKE 'CRIS-%'; 

commit;  
