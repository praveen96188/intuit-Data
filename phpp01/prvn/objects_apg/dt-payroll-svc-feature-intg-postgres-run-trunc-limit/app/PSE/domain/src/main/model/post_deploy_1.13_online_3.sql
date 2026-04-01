set serveroutput on;
spool atf_payment_migration.log
set timing on;
select systimestamp as start_time from dual;

declare
cnt number:='0';
v_sqlstr varchar2(5000);
begin
select count(*) into cnt from user_tables where table_name='TEST_MMT_ATF';
IF (cnt ='1') THEN
    BEGIN
    
        v_sqlstr := 'delete from test_mmt_atf mmt where  exists  
                  (SELECT ''t'' from  
                      PSP_ATFPAYMENTS_TO_PROCESS ATF where ATF.money_movement_transaction_FK=mmt.money_movement_transaction_seq )';
       
        EXECUTE IMMEDIATE v_sqlstr;
        commit;
     
    END;
    else 
    begin
    v_sqlstr:= 'create table test_mmt_atf as select mmt.*
    from psp_money_movement_transaction mmt
    where MMT.MONEY_MOVEMENT_PAYMENT_METHOD in (''EFTPS'', ''EFTPSDirectDebit'', ''HPDE'', ''HPDERefund'', ''CheckPayment'', ''ACHCredit'', ''ACHDebit'', ''EDI'', ''SuperCheck'')
        AND MMT.TAX_PAYMENT_STATUS in (''AcknowledgedByAgency'', ''ReturnedTaxPaid'', ''RejectedByAgency'', ''None'')
        AND MMT.PAYMENT_PERIOD_END > trunc(sysdate,''Y'') and mmt.initiation_date>trunc(sysdate,''Y'')-30 AND MMT.money_movement_transaction_seq NOT IN  
                  (SELECT  ATF.money_movement_transaction_FK
                     FROM PSP_ATFPAYMENTS_TO_PROCESS ATF)';
          EXECUTE IMMEDIATE v_sqlstr;     
          commit;                  
    end;
  END IF;
  end;
/
  
DECLARE
    v_count number := 0;
BEGIN
dbms_output.enable(10000);
FOR mmtRec in (select mmt.*
    from test_mmt_atf mmt) -- Only for the current year.
LOOP
    prc_recalculate_atf_payments(mmtRec.modifier_id, mmtRec.modified_date, mmtRec.money_movement_transaction_seq, mmtRec.payment_template_fk,
        mmtRec.money_movement_payment_method, mmtRec.tax_payment_status, mmtRec.payment_period_end, mmtRec.initiation_date, mmtRec.company_fk, TRUE);
        
    v_count := v_count + 1;
    if (mod(v_count,1000) = 0) then
     dbms_output.put_line('Committing at ' || to_char(v_count));  
     dbms_application_info.set_action( 'Commiting on ->' || v_count || ' as of->' || sysdate);
     Commit;
   end if;

END LOOP;
END;
/

commit;
  
select systimestamp as end_time from dual;
spool off;

-- Duplicate checking.

-- Missing MMT checking.
-- select * from test_mmt_atf tmp
--    where not exists (select 1 from PSP_MONEY_MOVEMENT_TRANSACTION mmt where TMP.MONEY_MOVEMENT_TRANSACTION_SEQ = MMT.MONEY_MOVEMENT_TRANSACTION_SEQ)

-- Drop the temporary table.
-- drop table test_mmt_atf;

