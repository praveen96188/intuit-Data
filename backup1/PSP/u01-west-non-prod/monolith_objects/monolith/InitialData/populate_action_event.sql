DROP TABLE IF EXISTS TEMP_ACTION_EVENT CASCADE;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_ACTION_EVENT (LIKE PSP_ACTION_EVENT INCLUDING ALL) ;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'RefundRebillFee',0,'Refund or Rebill this transaction','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'FinancialTransactionVoidTx',0,'Void this transaction','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'FinancialTransactionCancel',0,'Cancel this transaction','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'IssueReissueRefundEr',0,'Issue a refund for this transaction','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'TxStateHistory',0,'View History','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'DDTransactionCancel',0,'Cancel dd credit transactions','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'DDTransactionReverse',0,'Reverse dd credit transactions','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'DDRedebitAdd',0,'Create a payment transaction','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'DDRedebitRecord',0,'Create a payment transaction','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'DDRedebitEdit',0,'Change Expected Resolution Status/Date','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'ERFeeAdd',0,'Create a fee','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'BadDebtWriteOff',0,'Bad Debt Write Off','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'BadDebtRecover',0,'Bad Debt Recover','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'BadDebtWriteOffEEReturn',0,'EE Return Bad Debt Write Off','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'EEReturnTransfer',0,'Employee Return Transfer','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'FeeTransfer',0,'Fee Transfer','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'Intuit5DayReturnTransfer',0,'Intuit 5 Day Return Transfer','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'DDRefund',0,'DD Refund','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'ERReturnRefund',0,'Employer Return Refund','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'EEReturnRefund',0,'Employee Return Refund','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'ERWireExpected',0,'Add or Edit Wire Expected Date','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'ERFraudOrEscalationRefund',0,'Refund Employer for Fraud or Escalation','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'RecordPrefundingWire',0,'Record prefunding wire transaction','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES (
'CancelAdjustment',0,'Cancel adjustment','PayrollRun')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES (
'VoidPayrollTaxPayment',0,'Void payroll tax payment','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES (
'ReissuePayrollTaxPayment',0,'Reissue payroll tax payment','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES (
'ApplyERPayableToBalanceDue',0,'Apply ER Payable to balance due','LedgerAccount')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES (
'RefundDebit',0,'Create Refund Debit','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES ( 
'ERFeeCancel',0,'Cancel this transaction','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES (
'RefundERPayableCancel',0,'Cancel this transaction','FinancialTransaction')
;
INSERT INTO TEMP_ACTION_EVENT (CODE, VERSION, DESCRIPTION, TYPE) VALUES (
'VoidTORTransaction',0,'Void this transaction','FinancialTransaction')
;

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_ACTION_EVENT
   (CODE, VERSION, DESCRIPTION, TYPE) 
SELECT 
   CODE, VERSION, DESCRIPTION, TYPE
FROM 
   TEMP_ACTION_EVENT tae
WHERE 
   tae.CODE NOT IN (SELECT CODE FROM PSP_ACTION_EVENT)

;

DELETE FROM PSP_ACTION_EVENT
WHERE CODE NOT IN (SELECT CODE FROM TEMP_ACTION_EVENT) 

;

UPDATE PSP_ACTION_EVENT ae
SET  (VERSION, DESCRIPTION, TYPE) =
(SELECT tae.VERSION, tae.DESCRIPTION, tae.TYPE
 FROM TEMP_ACTION_EVENT tae WHERE tae.CODE =ae.CODE)
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_ACTION_EVENT

;
COMMIT
 

 
