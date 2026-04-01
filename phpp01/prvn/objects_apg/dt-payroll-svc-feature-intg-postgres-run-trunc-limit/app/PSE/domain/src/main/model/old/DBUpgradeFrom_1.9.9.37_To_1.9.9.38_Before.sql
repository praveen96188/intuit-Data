--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.37_To_1.9.9.38.sql
--
-- Developers can hand code logic here for data migration purposes
-- Script to remove NoticeOfChange onhold reasons
SET SERVEROUTPUT ON;

DECLARE
v_other_holds_exist BOOLEAN;
v_transaction_type VARCHAR2(100);
v_onhold_reason VARCHAR2(100);
v_otherHoldsCount NUMBER;

v_noc_onhold_cnt NUMBER := 0;
v_noc_ftx_onhold_asc_cnt NUMBER := 0;
v_noc_ftx_onhold_cnt NUMBER := 0;

v_noc_onhold_sel_cnt NUMBER;
v_noc_ftx_onhold_asc_sel_cnt NUMBER;
--v_noc_fintxn_onhold_sel_count NUMBER;

BEGIN
 
 SELECT COUNT(*) INTO v_noc_onhold_sel_cnt FROM PSP_ON_HOLD_REASON WHERE ON_HOLD_REASON_CD='NoticeOfChange';
 SELECT COUNT(*) INTO v_noc_ftx_onhold_asc_sel_cnt FROM PSP_FINTXN_ONHOLDREASON_ASSOC 
		WHERE ON_HOLD_REASON_FK IN (SELECT ON_HOLD_REASON_SEQ FROM PSP_ON_HOLD_REASON WHERE ON_HOLD_REASON_CD='NoticeOfChange');
 
 DBMS_OUTPUT.PUT_LINE('NOC Onhold Count:' ||v_noc_onhold_sel_cnt);
 DBMS_OUTPUT.PUT_LINE('NOC Onhold Financial Transaction Association Count:' ||v_noc_ftx_onhold_asc_sel_cnt);
 
 FOR ONHOLD_REC IN (
                SELECT ON_HOLD_REASON_SEQ FROM PSP_ON_HOLD_REASON WHERE ON_HOLD_REASON_CD='NoticeOfChange'
            )
  LOOP
    FOR FIN_TXN_REC IN (
                            SELECT FINANCIAL_TRANSACTION_FK FROM PSP_FINTXN_ONHOLDREASON_ASSOC WHERE ON_HOLD_REASON_FK = ONHOLD_REC.ON_HOLD_REASON_SEQ
                       )
    LOOP
        -- identify whether any other OnholdReason holds this transaction; remove onhold for this transaction only when there are no other Onhold (that has association
        -- with this transaction type) exists for the company
        SELECT TRANSACTION_TYPE_FK INTO v_transaction_type FROM PSP_FINANCIAL_TRANSACTION WHERE FINANCIAL_TRANSACTION_SEQ=FIN_TXN_REC.FINANCIAL_TRANSACTION_FK;
        v_other_holds_exist:=false;
        FOR REC IN (
            SELECT ON_HOLD_REASON_FK FROM PSP_FINTXN_ONHOLDREASON_ASSOC WHERE FINANCIAL_TRANSACTION_FK=FIN_TXN_REC.FINANCIAL_TRANSACTION_FK AND ON_HOLD_REASON_FK<>ONHOLD_REC.ON_HOLD_REASON_SEQ 
        ) LOOP 
            SELECT ON_HOLD_REASON_CD into v_onhold_reason FROM PSP_ON_HOLD_REASON WHERE ON_HOLD_REASON_SEQ=REC.ON_HOLD_REASON_FK;
            SELECT COUNT(*) INTO v_otherHoldsCount FROM PSP_SERV_STAT_TXN_SKU_TYPE WHERE SERVICE_SUB_STATUS_FK=v_onhold_reason AND TRANSACTION_TYPE_FK=v_transaction_type;
            
            IF (v_otherHoldsCount = 1) THEN
                v_other_holds_exist:=true;
                EXIT;
            END IF;
            
        END LOOP;
        
        IF (v_other_holds_exist = false) THEN
			DBMS_OUTPUT.PUT_LINE('Removing Financial Transaction: '|| FIN_TXN_REC.FINANCIAL_TRANSACTION_FK ||' from OnHold for NoticeOfChange.');
            UPDATE PSP_FINANCIAL_TRANSACTION SET ON_HOLD=0 WHERE FINANCIAL_TRANSACTION_SEQ=FIN_TXN_REC.FINANCIAL_TRANSACTION_FK;
			v_noc_ftx_onhold_cnt:=v_noc_ftx_onhold_cnt+1;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Deleting Financial Transaction: '||FIN_TXN_REC.FINANCIAL_TRANSACTION_FK || ' Onhold Reason: ' ||ONHOLD_REC.ON_HOLD_REASON_SEQ|| ' association.');    
        DELETE FROM PSP_FINTXN_ONHOLDREASON_ASSOC WHERE ON_HOLD_REASON_FK = ONHOLD_REC.ON_HOLD_REASON_SEQ AND FINANCIAL_TRANSACTION_FK=FIN_TXN_REC.FINANCIAL_TRANSACTION_FK;
		v_noc_ftx_onhold_asc_cnt:=v_noc_ftx_onhold_asc_cnt+1;
    END LOOP;
    
	DBMS_OUTPUT.PUT_LINE('Deleting Onhold Reason: ' ||ONHOLD_REC.ON_HOLD_REASON_SEQ); 
    DELETE FROM PSP_ON_HOLD_REASON WHERE ON_HOLD_REASON_CD='NoticeOfChange' AND ON_HOLD_REASON_SEQ=ONHOLD_REC.ON_HOLD_REASON_SEQ;
	v_noc_onhold_cnt:=v_noc_onhold_cnt + 1;
  END LOOP;
  
  IF (v_noc_onhold_cnt = v_noc_onhold_sel_cnt AND v_noc_ftx_onhold_asc_cnt = v_noc_ftx_onhold_asc_sel_cnt AND v_noc_ftx_onhold_cnt <= v_noc_ftx_onhold_asc_sel_cnt) THEN
	DBMS_OUTPUT.PUT_LINE('Executing COMMIT');
	COMMIT;
  ELSE 
	DBMS_OUTPUT.PUT_LINE('Executing ROLLBACK');
	ROLLBACK;
  END IF;
  
 SELECT COUNT(*) INTO v_noc_onhold_sel_cnt FROM PSP_ON_HOLD_REASON WHERE ON_HOLD_REASON_CD='NoticeOfChange';
 SELECT COUNT(*) INTO v_noc_ftx_onhold_asc_sel_cnt FROM PSP_FINTXN_ONHOLDREASON_ASSOC 
		WHERE ON_HOLD_REASON_FK IN (SELECT ON_HOLD_REASON_SEQ FROM PSP_ON_HOLD_REASON WHERE ON_HOLD_REASON_CD='NoticeOfChange');
 
 DBMS_OUTPUT.PUT_LINE('NOC Onhold Count:' ||v_noc_onhold_sel_cnt);
 DBMS_OUTPUT.PUT_LINE('NOC Onhold Financial Transaction Association Count:' ||v_noc_ftx_onhold_asc_sel_cnt);
 
 DBMS_OUTPUT.PUT_LINE('Deleting NoticeOfChange Service Sub Status data from the static data tables');
 DELETE FROM PSP_ROLE_SUB_STATUS WHERE SERVICE_SUB_STATUS_FK='NoticeOfChange';
 DELETE FROM PSP_SVCSTAT_SYSCAP_ASSOC WHERE SERVICE_SUB_STATUS_FK='NoticeOfChange';
 DELETE FROM PSP_SVCSTAT_SVC_ASSOC WHERE SERVICE_SUB_STATUS_FK='NoticeOfChange';
 DELETE FROM PSP_SVCSTAT_SRCSYS_ASSOC WHERE SERVICE_SUB_STATUS_FK='NoticeOfChange';
 DELETE FROM PSP_SERV_STAT_TXN_SKU_TYPE WHERE SERVICE_SUB_STATUS_FK='NoticeOfChange';
 DELETE FROM PSP_SERVICE_SUB_STATUS WHERE SERVICE_SUB_STATUS_CD='NoticeOfChange';
 COMMIT;
                
END;

/

SET SERVEROUTPUT OFF;
SHOW ERRORS;
