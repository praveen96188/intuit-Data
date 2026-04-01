--
-- This script will be executed BEFORE the automatically generated
-- D:\Dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.008.001.sql
--
-- Developers can hand code logic here for data migration purposes
--

--manually remove static data since the code gen will update the constraints before the initial data scripts remove the entries
delete from psp_event_detail_type edt where edt.event_detail_type_cd in 
('CallerApplication', 'BankAccountType', 'TransmissionDateTime', 'PendingAutoRedebit', 
'OriginalTransactionDateTime', 'ReversalAmount', 'NumberOfBackdatedDays', 'StrikeRemovalReason', 
'BankAccountName', 'AdjustedERSettlementDate', 'AdjustedEESettlementDate', 'TerminationDateTime', 'QBToken', 'PSPToken', 'UnsupportedVersionNumber', 'UnsupportedReleaseNumber', 'BankAccountId',
'CheckListItemType', 'OldAgencyStatus', 'NewAgencyStatus', 'OldPaymentMethod', 'NewPaymentMethod', 'OriginalMMT', 'NewMMT', 'ReferenceNumber', 'PaymentStatus', 'EFERejectionReason', 'BillPaymentSplitId')
/

delete from PSP_EVTTP_SRCSYS_ASSOC ass where ass.interesting_event_types_fk in 
 ('AgentNote', 'ErrorInfo', 'PayrollSubmissionFailed', 'PayrollOffloaded', 'FraudDetected', 'OffloadReportPrinted', 
'CompanyInformationChanged', 'CBAVerifyExpired', 'AgentAssignedToPayment', 'CompanyContactChanged', 'AgentUnassignedFromPayment', 'PaycheckCancelled',
'PayrollRecalledAfterOffload', 'ActivationPending', 'AgentAssignedToCheckList', 'ActivationCheckListStatusChanged', 'ActivationCheckListItemStatusChanged', 'PostActivationCheckListStatusChanged',
'AgencyStatusChange', 'PaymentExceptionsResolved', 'ManualPaymentStatusChanged', 'ReplacementCheckOrWireIssued', 'TaxPaymentSuccessful', 'TaxPaymentRejected',  
'AllowSingleUntimelyPayroll', 'TaxFilingUploaded', 'UntimelyPayrollReceived', 'PenaltyAndInterestCreated', 'NoToTermsOfAgreement', 'HPDECalculatedValues', 'PenaltyAndInterestUpdated', 'BillPaymentProcessedTooSoon', 
'CustomerTaxPaymentCreated', 'NegativeLiability')
/

delete from psp_event_type et where et.event_type_cd in
('AgentNote', 'ErrorInfo', 'PayrollSubmissionFailed', 'PayrollOffloaded', 'FraudDetected', 'OffloadReportPrinted', 
'CompanyInformationChanged', 'CBAVerifyExpired', 'AgentAssignedToPayment', 'CompanyContactChanged', 'AgentUnassignedFromPayment', 'PaycheckCancelled',
'PayrollRecalledAfterOffload', 'ActivationPending', 'AgentAssignedToCheckList', 'ActivationCheckListStatusChanged', 'ActivationCheckListItemStatusChanged', 'PostActivationCheckListStatusChanged',
'AgencyStatusChange', 'PaymentExceptionsResolved', 'ManualPaymentStatusChanged', 'ReplacementCheckOrWireIssued', 'TaxPaymentSuccessful', 'TaxPaymentRejected',  
'AllowSingleUntimelyPayroll', 'TaxFilingUploaded', 'UntimelyPayrollReceived', 'PenaltyAndInterestCreated', 'NoToTermsOfAgreement', 'HPDECalculatedValues', 'PenaltyAndInterestUpdated', 'BillPaymentProcessedTooSoon', 
'CustomerTaxPaymentCreated', 'NegativeLiability')
/

