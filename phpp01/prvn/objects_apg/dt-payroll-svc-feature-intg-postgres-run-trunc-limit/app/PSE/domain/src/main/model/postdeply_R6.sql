spool ATFPaymentsUpdates.log

set serveroutput on size unlimited;

DECLARE
   v_user_id    VARCHAR2 (25) := 'AgencyTaxPaymentsFix';
   v_psp_date   TIMESTAMP;                                         -- UTC Date
   v_upd_cnt number;
v_start_time TIMESTAMP(6);
BEGIN
   
v_upd_cnt:=0;
select systimestamp into v_start_time from dual;

   -- Update Payment Status of ACHCRedit MMTs to AcknowledgedByAgency
   FOR mmtRec IN (SELECT mmt.* FROM psp_money_movement_transaction mmt
           WHERE     tax_payment_status = 'SentToAgency'
                 AND money_movement_payment_method = 'ACHCredit'
                 AND status = 'Executed'
                 AND mmt.initiation_date BETWEEN SYSDATE - 60 AND SYSDATE)
   LOOP
	SELECT SYS_EXTRACT_UTC (FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;
      prc_recalculate_atf_payments (v_user_id,  v_psp_date,
                                    mmtRec.money_movement_transaction_seq,
                                    mmtRec.payment_template_fk,
                                    mmtRec.money_movement_payment_method,
                                    'AcknowledgedByAgency',
                                    mmtRec.payment_period_end,
                                    mmtRec.initiation_date,
                                    mmtRec.company_fk);

      -- Set the Payment Status to AcknowledgedByAgency
      UPDATE psp_money_movement_transaction mmt
         SET tax_payment_status = 'AcknowledgedByAgency',
             VERSION = VERSION + 1,
             modifier_id = v_user_id,
             modified_date = v_psp_date
       WHERE mmt.money_movement_transaction_seq = mmtRec.money_movement_transaction_seq;

      v_upd_cnt:=v_upd_cnt+sql%rowcount;

    IF MOD(v_upd_cnt, 10000) = 0 THEN

      dbms_output.put_line('processed 10000 in last' || (systimestamp - v_start_time) || '---> Total Finished ' || v_upd_cnt );
   
    END IF;

	commit;
       
   END LOOP;
  dbms_output.put_line('finished ' || v_upd_cnt || ' in ' || (systimestamp - v_start_time));
END;
/

spool off;

-- FLA changes
Update psp_financial_transaction set transaction_type_fk=(select name from psp_transaction_type where transaction_type_cd=transaction_type_fk)
where financial_transaction_seq in (select financial_transaction_fk from PSP_FINANCIAL_TRANS_STATE where transaction_type_fk in ('FLATemp1', 'FLATemp2', 'FLATemp3', 'FLATemp4', 'FLATemp5' )  
and trunc(TRANSACTION_STATE_EFF_DATE) > date '2012-04-09'); 

Update PSP_FINANCIAL_TRANS_STATE set transaction_type_fk=(select name from psp_transaction_type where transaction_type_cd=transaction_type_fk)
where transaction_type_fk in ('FLATemp1', 'FLATemp2', 'FLATemp3', 'FLATemp4', 'FLATemp5' )  and trunc(TRANSACTION_STATE_EFF_DATE) > date '2012-04-09';

update PSP_TRANSACTION_TYPE set name =transaction_type_cd where transaction_type_cd like 'FLATemp%';

delete psp_posting_rule where transaction_type_fk like 'FLATemp%';

commit;
