--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_2.0.0.11_To_2.0.0.12.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt Constraint C_PSP_EVENT_DETAIL_TYPE0;
ALTER TABLE PSP_EVENT_DETAIL_TYPE
 DROP CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0;

Prompt Constraint C_PSP_COMPANY_EVENT_DETAIL0;
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 DROP CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0;

Prompt Constraint PSP_EVTDTLTP_EVTTP_FK_EVTDTTP;
ALTER TABLE PSP_EVTDTLTP_EVTTP_ASSOC
 DROP CONSTRAINT PSP_EVTDTLTP_EVTTP_FK_EVTDTTP; 

PROMPT Before DELETE 
SELECT COUNT(*) FROM PSP_EVENT_DETAIL_TYPE where EVENT_DETAIL_TYPE_CD ='OldDDServiceStatus';

DELETE FROM PSP_EVENT_DETAIL_TYPE where EVENT_DETAIL_TYPE_CD ='OldDDServiceStatus';

PROMPT After DELETE

INSERT INTO PSP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'OldServiceStatus', 0, 'Old Service Status', '', 'com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode');
PROMPT After INSERT

PROMPT Before DELETE 
SELECT COUNT(*) FROM PSP_EVTDTLTP_EVTTP_ASSOC where EVENT_DETAIL_TYPE_FK ='OldDDServiceStatus';

DELETE FROM PSP_EVTDTLTP_EVTTP_ASSOC where EVENT_DETAIL_TYPE_FK ='OldDDServiceStatus';
PROMPT After DELETE 

INSERT INTO PSP_EVTDTLTP_EVTTP_ASSOC ( EVENT_TYPE_FK, EVENT_DETAIL_TYPE_FK ) VALUES ( 'ServiceStatusChange', 'OldServiceStatus' );
PROMPT After INSERT

PROMPT Before DELETE 
SELECT COUNT(*) FROM PSP_EVENT_DETAIL_TYPE where EVENT_DETAIL_TYPE_CD ='NewDDServiceStatus';

DELETE FROM PSP_EVENT_DETAIL_TYPE where EVENT_DETAIL_TYPE_CD ='NewDDServiceStatus';
PROMPT After DELETE 

INSERT INTO PSP_EVENT_DETAIL_TYPE ( EVENT_DETAIL_TYPE_CD, VERSION, NAME, DESCRIPTION, VALUE_CLASS_NAME ) VALUES (
'NewServiceStatus', 0, 'New Service Status', '', 'com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode');
PROMPT After Insert

PROMPT Before DELETE 
SELECT COUNT(*) FROM PSP_EVTDTLTP_EVTTP_ASSOC where EVENT_DETAIL_TYPE_FK ='NewDDServiceStatus';

DELETE FROM PSP_EVTDTLTP_EVTTP_ASSOC where EVENT_DETAIL_TYPE_FK ='NewDDServiceStatus';
PROMPT After DELETE 

INSERT INTO PSP_EVTDTLTP_EVTTP_ASSOC ( EVENT_TYPE_FK, EVENT_DETAIL_TYPE_FK ) VALUES ( 'ServiceStatusChange', 'NewServiceStatus' );
PROMPT After Insert




PROMPT Before update PSP_COMPANY_EVENT_DETAIL - OldDDServiceStatus

select count(*) from psp_company_event_detail where EVENT_DETAIL_TYPE_CD ='OldDDServiceStatus';

update psp_company_event_detail set EVENT_DETAIL_TYPE_CD ='OldServiceStatus' 
where EVENT_DETAIL_TYPE_CD ='OldDDServiceStatus';

PROMPT After update PSP_COMPANY_EVENT_DETAIL - OldDDServiceStatus

PROMPT Before update PSP_COMPANY_EVENT_DETAIL - NewDDServiceStatus

select count(*) from psp_company_event_detail where EVENT_DETAIL_TYPE_CD ='NewDDServiceStatus';

update psp_company_event_detail set EVENT_DETAIL_TYPE_CD ='NewServiceStatus' 
where EVENT_DETAIL_TYPE_CD ='NewDDServiceStatus';

PROMPT After update PSP_COMPANY_EVENT_DETAIL - NewDDServiceStatus

ALTER TABLE PSP_EVTDTLTP_EVTTP_ASSOC ADD (
  CONSTRAINT PSP_EVTDTLTP_EVTTP_FK_EVTDTTP 
 FOREIGN KEY (EVENT_DETAIL_TYPE_FK, REALM_ID) 
 REFERENCES PSP_EVENT_DETAIL_TYPE (EVENT_DETAIL_TYPE_CD,REALM_ID));

 ALTER TABLE PSP_EVENT_DETAIL_TYPE
 ADD CONSTRAINT C_PSP_EVENT_DETAIL_TYPE0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode'));

 
ALTER TABLE PSP_COMPANY_EVENT_DETAIL
 ADD CONSTRAINT C_PSP_COMPANY_EVENT_DETAIL0
 CHECK (EVENT_DETAIL_TYPE_CD IN('ACHEventCd', 'BankAccountNumber', 'BankAccountRoutingNumber', 'EmployeeName', 'SourcePayrollRunId', 'PayrollCancellationReason', 'CallerApplication', 'CompanyBankAccountId', 'ACHReturnReasonCode', 'BankAccountType', 'CancellationDateTime', 'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'PayrollRunId', 'PayrollCancellationScope', 'EmployeeId', 'LimitAmount', 'LimitType', 'ViolationAmount', 'OldCoaName', 'NewCoaName', 'CoaNameChangeByAgent', 'OldServiceStatus', 'NewServiceStatus', 'OldLimitAmount', 'NewLimitAmount', 'OldBAStatus', 'NewBAStatus', 'ManualStrikeReasonDescription', 'FeeType', 'RefundStatus', 'RefundStatusReason', 'ReturnType', 'CompanyServiceId', 'FinancialTransactionId', 'TransmissionId', 'ErrorCode', 'ErrorMessage', 'ErrorType', 'OldStringValue', 'NewStringValue', 'PaycheckDate', 'TransmissionDateTime', 'OffloadDate', 'PaycheckId', 'PaycheckAmount', 'PendingAutoRedebit', 'FeeAmount', 'OriginalTransactionDateTime', 'RefundedFeeBillingDetailId', 'ReversalAmount', 'StrikeReason', 'ReasonDescription', 'NoteText', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 'FailureReason', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'NSFSubType', 'PayrollStatus', 'OldAccountNumber', 'NewAccountNumber', 'OldRoutingNumber', 'NewRoutingNumber', 'OldAccountType', 'NewAccountType', 'EmployeeBankAccountId', 'VerificationStatus', 'PayrollRejectedReason', 'CompanyEventId', 'ServiceStatus', 'OldOnHoldReason', 'NewOnHoldReason', 'CollectionStage', 'AS400EventName', 'AS400EventToken', 'AS400EventOFX', 'WireExpectedDate', 'IntuitInitiated', 'Details', 'FraudEventCategory', 'ContactId', 'NewAmount', 'OldAmount', 'OldDate', 'NewDate', 'OldPayrollStatus', 'NewPayrollStatus', 'SourceBankAccountId', 'BankAccountId', 'SourcePaycheckId', 'FeeBillingDetailId', 'RefundAmount', 'ExpectedToken', 'ReceivedToken', 'Timestamp', 'UniqueIdentifier', 'UserId', 'ServiceCode'));

PROMPT Before update PSP_TRANSACTION_TYPE - Association_Type

update PSP_TRANSACTION_TYPE set ASSOCIATION_TYPE='Impound' 
where TRANSACTION_TYPE_CD in ('EmployerDdDebit','EmployerTaxDebit') ;

PROMPT After update PSP_TRANSACTION_TYPE - Association_Type