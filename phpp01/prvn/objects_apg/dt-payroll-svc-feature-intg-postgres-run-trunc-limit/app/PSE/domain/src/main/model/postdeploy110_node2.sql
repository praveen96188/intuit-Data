
---this is a post deploy script.




select systimestamp as start_time_postdeploy_2 from dual;

--ALTER TABLE PSP_COMP_ADJUST_SUBMISSION add (QBDT_TRANSACTION_INFO_FK varchar2(255));
--select count(*) from PSP_COMP_ADJUST_SUBMISSION where QBDT_TRANSACTION_INFO_FK is not null;

merge into psp_qbdt_transaction_info
using PSP_COMP_ADJUST_SUBMISSION on (psp_qbdt_transaction_info.QBDT_TRANSACTION_INFO_SEQ = PSP_COMP_ADJUST_SUBMISSION.QBDT_TRANSACTION_INFO_FK)
when matched then update set comp_adjust_submission_fk = comp_adjust_submission_seq;

commit;



--select count(*) from psp_qbdt_transaction_info where comp_adjust_submission_fk is not null;

--ALTER TABLE PSP_PAYCHECK add (QBDT_PAYCHECK_INFO_FK varchar2(255));
--select count(*) from PSP_PAYCHECK where QBDT_PAYCHECK_INFO_FK is not null;

merge /*+index(psp_paycheck psp_paycheck_fk5)  */  into psp_qbdt_paycheck_info
using PSP_PAYCHECK on (psp_qbdt_paycheck_info.QBDT_PAYCHECK_INFO_SEQ = PSP_PAYCHECK.QBDT_PAYCHECK_INFO_FK)
when matched then update set paycheck_fk = paycheck_seq;

commit;



--select count(*) from psp_qbdt_paycheck_info where paycheck_fk is not null;

--ALTER TABLE PSP_FINANCIAL_TRANSACTION add (QBDT_TRANSACTION_INFO_FK varchar2(255));
--select count(*) from PSP_FINANCIAL_TRANSACTION where QBDT_TRANSACTION_INFO_FK is not null;

merge /*+INDEX(PSP_FINANCIAL_TRANSACTION PSP_FINANCIAL_TRANSACTION_FK17 ) index(psp_qbdt_transaction_info SYS_C00138023 ) */ into psp_qbdt_transaction_info
using PSP_FINANCIAL_TRANSACTION on (psp_qbdt_transaction_info.QBDT_TRANSACTION_INFO_SEQ = PSP_FINANCIAL_TRANSACTION.QBDT_TRANSACTION_INFO_FK)
when matched then update set financial_transaction_fk = financial_transaction_seq;

commit;



--select count(*) from psp_qbdt_transaction_info where financial_transaction_fk is not null;

--ALTER TABLE PSP_LIABILITY_ADJUSTMENT add (QBDT_TRANSACTION_INFO_FK varchar2(255));
--select count(*) from PSP_LIABILITY_ADJUSTMENT where QBDT_TRANSACTION_INFO_FK is not null;

merge into psp_qbdt_transaction_info
using PSP_LIABILITY_ADJUSTMENT on (psp_qbdt_transaction_info.QBDT_TRANSACTION_INFO_SEQ = PSP_LIABILITY_ADJUSTMENT.QBDT_TRANSACTION_INFO_FK)
when matched then update set liability_adjustment_fk = liability_adjustment_seq;

commit;




--select count(*) from psp_qbdt_transaction_info where liability_adjustment_fk is not null;



----insert priorpayment data into PSP_PRIOR_PAYMENT_SUBMISSION

--- Add column temporarily and it will  be dropped later.

alter table psp_prior_payment_submission add (MONEY_MOVEMENT_TRANSACTION_FK varchar2(255));
alter table psp_prior_payment_submission add (QBDT_TRANSACTION_INFO_FK varchar2(255));

INSERT INTO PSP_PRIOR_PAYMENT_SUBMISSION (PRIOR_PAYMENT_SUBMISSION_SEQ,
                                          VERSION,
                                          CREATOR_ID,
                                          CREATED_DATE,
                                          MODIFIER_ID,
                                          MODIFIED_DATE,
                                          REALM_ID,
                                          SOURCE_ID,
                                          COMPANY_FK,
                                          MONEY_MOVEMENT_TRANSACTION_FK,
                                          QBDT_TRANSACTION_INFO_FK)                                          
   SELECT /*+index(A PSP_MM_TRANSACTION_I2) */ Fn_Format_Sysguid (SYS_GUID ()),
          '1',
          'QBDTAdapter',
          SYSDATE,
          'QBDTAdapter',
          SYSDATE,
          '-1',
          source_id,
          company_fk,
           MONEY_MOVEMENT_TRANSACTION_SEQ,
           QBDT_TRANSACTION_INFO_FK
          FROM psp_money_movement_transaction A
    WHERE A.MONEY_MOVEMENT_PAYMENT_METHOD LIKE 'HPDE%';

COMMIT;

  ------ to update qbdt_transaction_info with prior payment seq's.  
  
  
   begin 
    for i in (select prior_payment_submission_seq,MONEY_MOVEMENT_TRANSACTION_FK,QBDT_TRANSACTION_INFO_FK  from psp_prior_payment_submission )
    loop
    begin
    update  psp_qbdt_transaction_info  set prior_payment_submission_fk=i.prior_payment_submission_seq,MONEY_MOVEMENT_TRANSACTION_FK=i.MONEY_MOVEMENT_TRANSACTION_FK where QBDT_TRANSACTION_INFO_SEQ=i.QBDT_TRANSACTION_INFO_FK;
    end;
    end loop;
    commit;
    end;
    /
    
    
 
 Prompt COLUMN SOURCE_ID;
 ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION SET UNUSED COLUMN SOURCE_ID;

 
 ALTER TABLE PSP_PRIOR_PAYMENT_SUBMISSION DROP COLUMN MONEY_MOVEMENT_TRANSACTION_FK;
 
 
 ALTER TABLE PSP_PRIOR_PAYMENT_SUBMISSION DROP COLUMN QBDT_TRANSACTION_INFO_FK;

Prompt COLUMN QBDT_TRANSACTION_INFO_FK;
ALTER TABLE PSP_MONEY_MOVEMENT_TRANSACTION SET UNUSED COLUMN QBDT_TRANSACTION_INFO_FK;
 

 SELECT SYSTIMESTAMP AS BENCH_MARK_FOR_FLASHBACK FROM DUAL;
 
 DECLARE
    cur_mmt    VARCHAR2 (255);
    prev_mmt   VARCHAR2 (255):='0';
    sysid      VARCHAR2 (255);
 BEGIN
    FOR i
       IN (  SELECT                       /*+index(mt PSP_MM_TRANSACTION_I2) */
                   MONEY_MOVEMENT_TRANSACTION_FK      mmt_fk,
                    law.PAYMENT_TEMPLATE_FK pt,
                    SUM (FINANCIAL_TRANSACTION_AMOUNT) amt
               FROM psp_financial_transaction ft,
                    psp_law law,
                    psp_money_movement_transaction mt
              WHERE MT.MONEY_MOVEMENT_TRANSACTION_SEQ =
                       ft.MONEY_MOVEMENT_TRANSACTION_FK
                    AND MT.MONEY_MOVEMENT_PAYMENT_METHOD LIKE 'HPDE%'
                    AND ft.law_fk = LAW.LAW_ID
           GROUP BY ft.MONEY_MOVEMENT_TRANSACTION_FK, LAW.PAYMENT_TEMPLATE_FK
           ORDER BY ft.MONEY_MOVEMENT_TRANSACTION_FK)
    LOOP
       BEGIN
          cur_mmt := i.mmt_fk;
          IF cur_mmt != prev_mmt
          THEN
             UPDATE psp_money_movement_transaction
                SET payment_template_fk = i.pt, MM_TRANSACTION_AMOUNT = i.amt where money_movement_transaction_seq=cur_mmt;
 
             COMMIT;
          Else
   sysid:=FN_FORMAT_SYSGUID(SYS_GUID());     
 Insert into PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION
    (MONEY_MOVEMENT_TRANSACTION_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, DUE_DATE, INITIATION_DATE, MM_TRANSACTION_AMOUNT, STATUS, MONEY_MOVEMENT_PAYMENT_METHOD, COMPANY_FK,PAYMENT_TEMPLATE_FK, OFFLOAD_BATCH_FK)
  select
    sysid, '1', 'QBDTAdapter', SYSDATE, 'QBDTAdapter',
    SYSDATE, -1, DUE_DATE,INITIATION_DATE , i.amt,STATUS,
     MONEY_MOVEMENT_PAYMENT_METHOD, COMPANY_FK,i.pt,OFFLOAD_BATCH_FK from psp_money_movement_transaction where money_movement_transaction_seq=cur_mmt;
 commit;
 
 Insert into PSPADM.PSP_QBDT_TRANSACTION_INFO
    (QBDT_TRANSACTION_INFO_SEQ, 
     VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, 
     REALM_ID, AGENCY_NAME, REFERENCE_NUMBER, ACCOUNT_NAME, MEMO, 
     ON_SERVICE, CLEARED, TRACKING_CLASS, IS_DELETED, TOKEN, 
     LIABILITY_CHECK_FK, LIABILITY_CHECK_LINE_FK, MONEY_MOVEMENT_TRANSACTION_FK, IS_DIRECT_DEPOSIT, SYSTEM_GENERATED, 
     COMP_ADJUST_SUBMISSION_FK, LIABILITY_ADJUSTMENT_FK, FINANCIAL_TRANSACTION_FK, PRIOR_PAYMENT_SUBMISSION_FK)
   select FN_FORMAT_SYSGUID(SYS_GUID()),VERSION,CREATOR_ID,SYSDATE,MODIFIER_ID,SYSDATE,REALM_ID, AGENCY_NAME, REFERENCE_NUMBER, ACCOUNT_NAME, MEMO,ON_SERVICE, CLEARED, TRACKING_CLASS, IS_DELETED, TOKEN, 
  LIABILITY_CHECK_FK, LIABILITY_CHECK_LINE_FK, sysid, IS_DIRECT_DEPOSIT, SYSTEM_GENERATED, 
     COMP_ADJUST_SUBMISSION_FK, LIABILITY_ADJUSTMENT_FK, FINANCIAL_TRANSACTION_FK, PRIOR_PAYMENT_SUBMISSION_FK FROM PSPADM.PSP_QBDT_TRANSACTION_INFO  WHERE MONEY_MOVEMENT_TRANSACTION_FK=cur_mmt; 
 commit;
 update psp_financial_transaction set money_movement_transaction_fk=sysid where money_movement_transaction_fk=cur_mmt and law_fk in ( SELECT LAW_ID FROM PSP_LAW WHERE PAYMENT_TEMPLATE_FK=i.pt);
 commit;
  end if;
prev_mmt:=cur_mmt;
 end;
 end loop;
 end;
 /
 
---drop the existing constraint  C_PSP_MONEY_MOVEMENT_TRANS2
 
 ALTER TABLE PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION DROP CONSTRAINT  C_PSP_MONEY_MOVEMENT_TRANS2;
 
---- create constraint C_PSP_MONEY_MOVEMENT_TRANS2

 ALTER TABLE PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION ADD 
    CONSTRAINT C_PSP_MONEY_MOVEMENT_TRANS2
   CHECK (TAX_PAYMENT_STATUS IN('RejectedByAgency', 'SentToAgency', 'AcknowledgedByAgency', 'ReturnedTaxNotPaid', 'ReturnedTaxPaid', 'ReadyToSend', 'OnHold', 'None'));
 

-----update current partition of psp_money_movement_transaction  
 

 begin
 loop
 UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG22011)  SET tax_payment_status = 'None' 
 where tax_payment_status is null and  ROWNUM < 10000;
 if SQL%ROWCOUNT = 0 THEN
  exit;
 else
 commit;
 end if;
 end loop;
 commit;
 end;
 /

----REMOVE DUPLICATE COMPANY AGENCIES FROM PSP_COMPANY_AGENCY AND UPDATE THE AFFECTED TABLES WITH THE ONE WE KEEP.

begin
for i in (select max(company_agency_seq) max, min(company_agency_seq) min from psp_company_agency group by company_fk,agency_fk having count(*) >1)
loop
begin
update psp_company_law set company_agency_fk =i.min where company_agency_fk=i.max;
update psp_companyagency_pmttemplate set company_agency_fk =i.min where company_agency_fk=i.max;
delete from psp_company_agency where company_agency_seq=i.max;
end;
end loop;
commit;
end;
/

----Negate rate in psp_company_law_rate for all the employees that are paid.

UPDATE  PSP_COMPANY_LAW_RATE SET RATE=-RATE WHERE COMPANY_LAW_FK IN (SELECT  COMPANY_LAW_SEQ
  FROM PSP_COMPANY_LAW PCL, PSP_QBDT_PAYROLL_ITEM_INFO PQI
 WHERE PCL.QBDT_PAYROLL_ITEM_INFO_FK = PQI.QBDT_PAYROLL_ITEM_INFO_SEQ
       AND PQI.IS_EMPLOYEE_PAID = '1');
       
   COMMIT;
   

---change status_cd for assisted customers and cloud customers who has assisted service.


update psp_company_service set status_cd='PendingBalanceFile' where SERVICE_FK='Tax' and status_cd in ('PendingBankVerification','PendingPinCreation','PendingEnrollment');

commit;

update psp_company_service set status_cd='PendingBalanceFile' 
             where  service_fk='Cloud' and status_cd in ('PendingBankVerification','PendingPinCreation','PendingEnrollment') 
                          and company_fk in (select company_fk from psp_company_service where service_fk='Tax');


commit;
   
---- drop trigger TRC_LEDGER_BALANCE_CALC_AI


DROP TRIGGER TRC_LEDGER_BALANCE_CALC_AI;

----remove duplicate source_id,company_fk from psp_comp_adjust_submission and update affected tables.

alter table psp_comp_adjust_submission SET UNUSED COLUMN qbdt_transaction_info_fk cascade constraints;
alter table psp_liability_adjustment set unused column qbdt_transaction_info_fk cascade constraints; 
select systimestamp from dual;
begin
for i in (SELECT max(COMP_ADJUST_SUBMISSION_SEQ) max_seq,min(COMP_ADJUST_SUBMISSION_SEQ) min_seq, company_fk cmp_fk, source_id src_id,count(*) cnt
FROM PSP_COMP_ADJUST_SUBMISSION
WHERE source_id IS NOT NULL
GROUP BY company_fk, source_id
HAVING COUNT(*) > 1)
loop
begin
if(i.cnt=2)
then
update psp_liability_adjustment set comp_adjust_submission_fk=null where comp_adjust_submission_fk=i.min_seq;
delete from  psp_qbdt_transaction_info WHERE comp_adjust_submission_fk=i.min_seq;
commit;
delete from psp_comp_adjust_submission where COMP_ADJUST_SUBMISSION_SEQ=i.min_seq;
commit;
else
update psp_liability_adjustment set comp_adjust_submission_fk=null where comp_adjust_submission_fk in (select COMP_ADJUST_SUBMISSION_SEQ from psp_comp_adjust_submission where company_fk=i.cmp_fk and source_id=i.src_id) and comp_adjust_submission_fk!=i.max_seq;
delete from  psp_qbdt_transaction_info WHERE comp_adjust_submission_fk in (select COMP_ADJUST_SUBMISSION_SEQ from psp_comp_adjust_submission where company_fk=i.cmp_fk and source_id=i.src_id) and comp_adjust_submission_fk!=i.max_seq;
commit;
delete from psp_comp_adjust_submission where company_fk=i.cmp_fk and source_id=i.src_id and COMP_ADJUST_SUBMISSION_SEQ!=i.max_seq;
commit;
end if;
commit;
end;
end loop;
end;
/

delete from psp_qbdt_transaction_info where liability_adjustment_fk in (select liability_adjustment_seq from psp_liability_adjustment where comp_adjust_submission_fk is null);


delete from psp_liability_adjustment where comp_adjust_submission_fk is null;



update /*+index(PSP_PAYCHECK PSP_PAYCHECK_FK5)*/ psp_paycheck
set net_amount = net_amount * -1
where QBDT_PAYCHECK_INFO_FK is not null;

commit;





select systimestamp as end_time_postdeploy_2 from dual;


