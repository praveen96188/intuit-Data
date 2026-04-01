-- TEAMTRACK NUM: PSRV002075
-- CREATED  DATE: 01.21.2011
-- AUTHOR       : IHANNUR
-- MODIFIER     :
--PURPOSE       :  PSP is storing OtherAdditionPreTax in psp_deduction instead of psp_compensation, Moving data from psp_deduction to psp_compensation
-- LOGON AS : 


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     1000
SET PAGESIZE     0
SET DEFINE       OFF


SPOOL dbupgrade_PSRV002075.log

INSERT INTO psp_compensation
(COMPENSATION_SEQ,
 VERSION,
 CREATOR_ID ,
 CREATED_DATE ,
 MODIFIER_ID ,
 MODIFIED_DATE ,
  REALM_ID ,
  COMPENSATION_AMOUNT,
  HOURS_WORKED ,
  COMPENSATION_Y_T_D_AMOUNT,
  PAY_STUB_ORDER    ,
  COMPANY_PAYROLL_ITEM_FK ,
  QBDT_PAYLINE_INFO_FK ,
  PAYCHECK_FK )
SELECT
DEDUCTION_SEQ,
  VERSION ,
  CREATOR_ID  ,
  CREATED_DATE   ,
  MODIFIER_ID   ,
  MODIFIED_DATE   ,
  REALM_ID   ,
  DEDUCTION_AMOUNT   ,
  0,
  DEDUCTION_Y_T_D_AMOUNT ,
  PAY_STUB_ORDER           ,
  COMPANY_PAYROLL_ITEM_FK ,
  QBDT_PAYLINE_INFO_FK    ,
  PAYCHECK_FK
FROM
   psp_deduction d
WHERE d.company_payroll_item_fk in (select company_payroll_item_seq from psp_company_payroll_item where payroll_item_fk='OtherAdditionPreTax')
/

DELETE FROM psp_deduction d WHERE d.company_payroll_item_fk in (select company_payroll_item_seq from psp_company_payroll_item where payroll_item_fk='OtherAdditionPreTax')
/

COMMIT;

spool off;

