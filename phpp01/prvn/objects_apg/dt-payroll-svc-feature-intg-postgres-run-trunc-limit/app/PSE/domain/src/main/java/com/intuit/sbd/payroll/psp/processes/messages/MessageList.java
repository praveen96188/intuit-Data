package com.intuit.sbd.payroll.psp.processes.messages;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * A collection of MessageInfos.  Add an item to the list by calling
 * the method matching the name of the message to be added,
 * <p/>
 * <code>
 * String company = "Intuit Inc."
 * String ein = "99-1234567";
 * MessageList messageList = new MessageList();
 * messageList.CompanyNotFound(company, ein);
 * </code>
 * <p/>
 * <p/>
 * To create a new message:
 * <p/>
 * 1.  Open the file messageDefinitions.properties
 * <p/>
 * 2.  Avoid adding duplicate message defintions -- search the file for strings that are similar to what
 * you are about to enter.
 * <p/>
 * 3.  Add entries according to the following format:
 * # note: the value for level must match exactly (capitalization included) the MessageLevel enum values.
 * message.<message#>.level
 * message.<message#>.sourceid
 * message.<message#>.messageFormat
 * <p/>
 * The message format should conform to the String.format(String format, Object ... args) API.
 * i.e.
 * # the service is unavailable
 * message.4.level=ERROR
 * message.4.messageFormat="The service is temporarily unavailable."
 * <p/>
 * # company already exists
 * message.5.level=ERROR
 * message.5.messageFormat="A company with EIN ({0}) already exists."
 * <p/>
 * 4.  Edit the MessageList class.  Add a method name that symbolically represents the message.
 * <p/>
 * In general, the method name is what you would have named an exception (if you were throwing this
 * as an exception) without the "Exception" suffix.
 * <p/>
 * For each substitution argument in the message format, add a strongly-typed argument to the message
 * method.
 * <p/>
 * Following the previous example from (3) above:
 * <p/>
 * i.e.
 * class MessageList {
 * ...
 * public void ServiceTemporarilyUnavailable() {
 * addMessage(4);
 * }
 * <p/>
 * public void CompanyAlreadyExists(String ein) {
 * addMessage(5, ein);
 * }
 * ...
 * }
 * <p/>
 * <p/>
 * Compile and use.
 */
public class MessageList extends ArrayList<Message> {

    private boolean bContainsErrors;
    private boolean bContainsWarnings;
    private MessageInfo.MessageLevel defaultMessageLevel;
    public static final String companyOperationNotAllowedMessageCode = "1101";

    public MessageList() {
        super();
        bContainsErrors = false;
    }

    public MessageInfo.MessageLevel getDefaultMessageLevel() {
        return defaultMessageLevel;
    }

    public void setDefaultMessageLevel(MessageInfo.MessageLevel defaultMessageLevel) {
        this.defaultMessageLevel = defaultMessageLevel;
    }

    public void InvalidArgument(EntityName pEntityName, String pSourceId, String pArgument) {
        addMessage(pEntityName, pSourceId, 11, pArgument);
    }

    public void BadProcessArgument(String pArgument) {
        // get process name from stack trace
        String processName = getCallerClassName("BadProcessArgument");
        addMessage(EntityName.PSPProcess, processName, 11, pArgument);
    }

    public void EntityDoesNotExist(EntityName pEntityName, String pSourceId, String pEntityNameString, String pId) {
        addMessage(pEntityName, pSourceId, 12, pEntityNameString, pId);
    }

    public void EmployeeNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 101);
    }

    public void PaycheckNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 103);
    }

    public void PayCheckDateTooFarInTheFuture(EntityName pEntityName, String pSourceId, String pNumberOfDays) {
        addMessage(pEntityName, pSourceId, 109, pNumberOfDays);
    }

    public void PayCheckDateOnHolidayOrWeekend(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 110);
    }

    public void SoftwareVersionUnsupported() {
        addMessage(EntityName.QuickbooksInfo, "1", 111);
    }

    public void CompanyServiceNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 117);
    }

    public void ServiceCodeNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 118);
    }

    public void CompanyAddressNull(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 120);
    }

    public void CompanyBankAccountNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 121);
    }

    public void InvalidSourceSystemCdSpecified(EntityName pEntityName, String pSourceId, String pSourceSystemCd) {
        addMessage(pEntityName, pSourceId, 125, pSourceSystemCd);
    }

    public void CompanyContactNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 127);
    }

    public void SourcePayrollRunIdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 130);
    }

    public void CompanyNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 141);
    }

    public void SourceSystemCdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 137);
    }

    public void CompanyIdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 138);
    }

    public void CompanyBankAccountIdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 139);
    }

    public void BankAccountNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 142);
    }

    public void EmployeeIdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 145);
    }

    public void CompanyAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 161, pSourceSystemCd, pSourceCompanyId);
    }

    public void InvalidNameControlValue(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pNameControl) {
        addMessage(pEntityName, pSourceId, 191, pSourceSystemCd, pSourceCompanyId, pNameControl);
    }

    public void CompanyBankAccountAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 162, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void EmployeeAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 163, pSourceEmployeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void EmployeeBankAccountAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 164, pSourceBankAccountId, pSourceEmployeeId);
    }

    public void InvalidSettlementTypeCode(EntityName pEntityName, String pSourceId, String pSettlementTypeCd) {
        addMessage(pEntityName, pSourceId, 165, pSettlementTypeCd);
    }

    public void EmployeeBankAccountNotFound(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 166, pSourceBankAccountId, pSourceEmployeeId);
    }

    public void EmployeeDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 168, pSourceEmployeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 169, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 170, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyDoesNotMatchPreviousCompany(EntityName pEntityName, String pSourceId, String pSourceSystemCd1, String pSourceCompanyId1, String pSourceSystemCd2, String pSourceCompanyId2) {
        addMessage(pEntityName, pSourceId, 171, pSourceSystemCd1, pSourceCompanyId1, pSourceSystemCd2, pSourceCompanyId2);
    }

    public void CompanyHasPendingFinancialTransactions(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 175, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyNotActive(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 177, pSourceSystemCd, pSourceCompanyId);
    }

    public void EmployeeNotActive(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 178, pSourceEmployeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyHasRecentBankTransactions(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 182, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayrollRunAlreadyExists(EntityName pEntityName, String pSourceId, String pSourcePayrollRunId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 183, pSourcePayrollRunId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayCheckAlreadyExists(EntityName pEntityName, String pSourceId, String pSourcePaycheckId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 184, pSourcePaycheckId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayCheckSplitAlreadyExists(EntityName pEntityName, String pSourceId, String pSourcePaycheckSplitId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 185, pSourcePaycheckSplitId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountNotActive(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 186, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void EmployeeBankAccountNotActive(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 187, pSourceBankAccountId, pSourceEmployeeId);
    }

    public void CompanyBankAccountStatusNotPendingVerification(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId, String pStatus) {
        addMessage(pEntityName, pSourceId, 188, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId, pStatus);
    }

    public void CompanyBankAccountGreaterVerifyRetryCount(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId, String pVerifyRetryCount) {
        addMessage(pEntityName, pSourceId, 189, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId, pVerifyRetryCount);
    }

    public void CompanyBankAccountFailedVerification(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 190, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayrollRunDoesNotExist(EntityName pEntityName, String pSourceId, String pSourcePayrollRunId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 194, pSourcePayrollRunId, pSourceSystemCd, pSourceCompanyId);
    }

    public void TransactionDoesNotExist(EntityName pEntityName, String pSourceId, String pSourcePayrollRunId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 195, pSourcePayrollRunId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PaycheckHasNoDetails(EntityName pEntityName, String pSourceId, String pSourcePaycheckId) {
        addMessage(pEntityName, pSourceId, 198, pSourcePaycheckId);
    }

    public void PaycheckAlreadyCanceled(EntityName pEntityName, String pSourceId, String pSourcePaycheckId) {
        addMessage(pEntityName, pSourceId, 196, pSourcePaycheckId);
    }

    public void CompanyBankAccountNoRecentVerificationTxns(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 197, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void InvalidPaycheckDate(EntityName pEntityName, String pSourceId, String payrollRunDate) {
        addMessage(pEntityName, pSourceId, 199, payrollRunDate);
    }

    public void CompanyBankAccountUpdateFailed(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 202);
    }

    public void CompanyBankAccountVerificationTxnsReturned(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 204, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountVerificationTxnsNotIssuedToBank(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 205, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void NoAccountSignatory(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 207, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountVerificationTxnsCanceled(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 208, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void EmployeeAlreadyInactive(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 215, pSourceEmployeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void EmployeeBankAccountAlreadyInactive(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 216, pSourceBankAccountId, pSourceEmployeeId);
    }

    public void CompanyBankAccountAlreadyInactive(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 217, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountHasPendingFinancialTransactions(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 218, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountHasRecentFinancialTransactions(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 219, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountHasUnresolvedBankReturns(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 226, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyHasUnresolvedBankReturns(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 227, pSourceSystemCd, pSourceCompanyId);
    }

    public void BackdateFeeChargedWarning(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 228, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyBankAccountAlreadyVerified(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 250, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PaycheckDepositDateNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 253);
    }

    public void BankAccountRoutingNumberInvalid(EntityName pEntityName, String pSourceId, String pRoutingNumber) {
        addMessage(pEntityName, pSourceId, 255, pRoutingNumber);
    }

    public void IllegalEventCancel(EntityName pEntityName, String pSourceId, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 257, pSourceCompanyId);
    }

    public void TransactionNoLongerPendingCannotCancel(EntityName pEntityName, String pSourceId, String pTransId) {
        addMessage(pEntityName, pSourceId, 258, pTransId);
    }

    public void TransactionPendingCannotReverse(EntityName pEntityName, String pSourceId, String pTransId) {
        addMessage(pEntityName, pSourceId, 260, pTransId);
    }

    public void TransactionCancelledCannotReverse(EntityName pEntityName, String pSourceId, String pTransId) {
        addMessage(pEntityName, pSourceId, 261, pTransId);
    }

    public void TransactionReturnedCannotReverse(EntityName pEntityName, String pSourceId, String pTransId) {
        addMessage(pEntityName, pSourceId, 262, pTransId);
    }

    public void TransactionReversalAlreadyAttemptedCannotReverse(EntityName pEntityName, String pSourceId, String pTransId) {
        addMessage(pEntityName, pSourceId, 263, pTransId);
    }

    public void FinancialTransactionDoesNotExist(EntityName pEntityName, String pSourceId, String pFinTxId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 264, pFinTxId, pSourceSystemCd, pSourceCompanyId);
    }

    public void TransactionAlreadyRefunded(EntityName pEntityName, String pSourceId, String pSourceTranasctionId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 265, pSourceTranasctionId);
    }

    public void SettlementDateTooFarInFuture(EntityName pEntityName, String pSourceId, String pSettlementTypeDate,
                                             String pSettlementTypeCd) {
        addMessage(pEntityName, pSourceId, 266, pSettlementTypeCd, pSettlementTypeDate);
    }

    public void AmountPositiveForNonACHTransactions(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 267);
    }

    public void SettlementDateNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 269);
    }

    public void SourcePayrollParameterDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCode, String pParameterCode) {
        addMessage(pEntityName, pSourceId, 270, pSourceSystemCode, pParameterCode);
    }

    public void SettlementDateTooFarInPast(EntityName pEntityName, String pSourceId, String pSettlementTypeDate,
                                           String pSettlementTypeCd) {
        addMessage(pEntityName, pSourceId, 271, pSettlementTypeCd, pSettlementTypeDate);
    }

    public void InvalidPaycheckDateRange(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 272);
    }

    public void BadDebtRecoveryAmountTooLarge(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 279);
    }

    public void PendingTransactionAlreadyExists(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 280);
    }

    public void RecoverBadDebtInvalidTxnSettlementType(EntityName pEntityName, String pSourceId, String pSettlementTypeCd) {
        addMessage(pEntityName, pSourceId, 281, pSettlementTypeCd);
    }

    public void CreateTransactionFailurePendingLedgerActivity(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 282);
    }

    public void AmountNotPositive(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 283);
    }

    public void FeeTransferExceedsLedgerBalance(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 284);
    }

    public void MultipleReissuanceOfSameFee(EntityName pEntityName, String pSourceId, String pSrcSystemCd, String pCompanyId, String pTxnId) {
        addMessage(pEntityName, pSourceId, 285, pSrcSystemCd, pCompanyId, pTxnId);
    }

    public void TransactionTypeInvalidForRefunded(EntityName pEntityName, String pSourceId, String pTxType) {
        addMessage(pEntityName, pSourceId, 286, pTxType);
    }

    public void BankReturnDateRangeInvalid(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 287);
    }

    public void CannotCancelPartialPaychecks(EntityName pEntityName, String pSourceId, String pEmployeeName, String pAmount, String pOtherAmounts) {
        addMessage(pEntityName, pSourceId, 289, pEmployeeName, pAmount, pOtherAmounts);
    }

    public void CreateTxnFailureReversalTxnIncomplete(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 290);
    }

    public void FundingModelUpdateNotAllowed(EntityName pEntityName, String pSourceId, String pSourceSystemCd) {
        addMessage(pEntityName, pSourceId, 291, pSourceSystemCd);
    }

    public void PinNotRecognized(EntityName pEntityName, String pSourceId, String pLockAccountDuration, String pMaxNumberOfFailedLoginAttempts) {
        addMessage(pEntityName, pSourceId, 292, pLockAccountDuration, pMaxNumberOfFailedLoginAttempts);
    }

    public void EinNotRecognized(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 12006);
    }

    public void SubscriptionNumberNotRecognized(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 12007);
    }

    public void OperationDeniedForAuthentication(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 12008);
    }

    public void AccountLocked(EntityName pEntityName, String pSourceId, String pLockAccountDuration) {
        addMessage(pEntityName, pSourceId, 293, pLockAccountDuration);
    }

    public void InvalidPINFormat(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 294);
    }

    public void InvalidSourceSystemTransmission(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 295);
    }

    public void SourceSystemTransmissionDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourceSystemTransmissionId) {
        addMessage(pEntityName, pSourceId, 296, pSourceSystemTransmissionId, pSourceSystemCd, pSourceCompanyId);
    }

    public void SourceSystemTransmissionDoesNotBelongToCompany(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourceSystemTransmissionId) {
        addMessage(pEntityName, pSourceId, 297, pSourceSystemTransmissionId, pSourceSystemCd, pSourceCompanyId);
    }

    public void ResponseTokenMustBeGreaterOrEqualRequestToken(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 298);
    }

    public void PaycheckDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourcePaycheckId) {
        addMessage(pEntityName, pSourceId, 299, pSourcePaycheckId, pSourceSystemCd, pSourceCompanyId);
    }

    public void FraudFlagIsNotSet(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 300, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayrollSubmitedWithPendingNOC(EntityName pEntityName, String pSourceId, String pNumberOfAttempts, String pEmployeeName) {
        addMessage(pEntityName, pSourceId, 301, pNumberOfAttempts, pEmployeeName);
    }

    public void AuthenticationFailed(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 302, pSourceSystemCd, pSourceCompanyId);
    }

    public void AmountMustBeGreaterThan(EntityName pEntityName, String pSourceId, String pAmountToBeGreaterThan, String pAmount) {
        addMessage(pEntityName, pSourceId, 303, pAmountToBeGreaterThan, pAmount);
    }

    public void ActiveBankAccountWarning(EntityName pEntityName, String pSourceId, String pTransactionTypeCode) {
        addMessage(pEntityName, pSourceId, 304, pTransactionTypeCode);
    }

    public void PayrollRunAlreadyOffloadedPrefunding(EntityName pEntityName, String pSourceId, String pSourcePayrollId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 305, pSourcePayrollId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CancelEmployeeTransactionsWithPrefundingRecorded(EntityName pEntityName, String pSourceId, String pSourcePayrollId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 306, pSourcePayrollId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CancelAllEmployeeTransactionsWithPrefundingRecorded(EntityName pEntityName, String pSourceId, String pSourcePayrollId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 307, pSourcePayrollId, pSourceSystemCd, pSourceCompanyId);
    }

    public void RecallTransactionsWithPrefundingRecorded(EntityName pEntityName, String pSourceId, String pSourcePayrollId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 308, pSourcePayrollId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CannotCancelPartialPayment(EntityName pEntityName, String pSourceId, String pPayeeName, String pAmount, String pOtherAmounts) {
        addMessage(pEntityName, pSourceId, 309, pPayeeName, pAmount, pOtherAmounts);
    }

    public void InvalidCheckPrintImageType(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 310);
    }

    public void IncorrectCheckPrintImageSize(EntityName pEntityName, String pSourceId, long pHeight, long pWidth) {
        addMessage(pEntityName, pSourceId, 311, pHeight, pWidth);
    }

    public void IncorrectCheckPrintImageResolution(EntityName pEntityName, String pSourceId, long pResolution) {
        addMessage(pEntityName, pSourceId, 312, pResolution);
    }

    public void ReadCheckPrintImageError(EntityName pEntityName, String pSourceId, Throwable t) {
        addMessage(pEntityName, pSourceId, 313, t.getMessage());
    }

    public void CheckPrintBatchDoesNotExist(EntityName pEntityName, String pSourceId, String pBatchId) {
        addMessage(pEntityName, pSourceId, 314, pBatchId);
    }

    public void CompanyDoesNotHaveSignature(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 315, pSourceSystemCd, pSourceCompanyId);
    }

    public void EntitlementCodeDoesNotExist(EntityName pEntityName, String pSourceId, String pAssertItemNumber, EditionType pEdition, NumberOfEmployeesType pNumberOfEmployees) {
        addMessage(pEntityName, pSourceId, 316, pAssertItemNumber, pEdition, pNumberOfEmployees);
    }

    public void EntitlementDoesNotExist(EntityName pEntityName, String pSourceId, String pLicenseNumber, String pEntitlementOfferingCode) {
        addMessage(pEntityName, pSourceId, 317, pLicenseNumber, pEntitlementOfferingCode);
    }

    public void EntitlementMessageDoesNotExist(EntityName pEntityName, String pSourceId, String pMessageId) {
        addMessage(pEntityName, pSourceId, 318, pMessageId);
    }

    public void EINAlreadyInUse(EntityName pEntityName, String pSourceId, String pFEIN, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 319, pFEIN, pSourceSystemCd, pSourceCompanyId);
    }

    public void ActiveAssistedEntitlementAlreadyExists(EntityName pEntityName, String pSourceId, String pFEIN) {
        addMessage(pEntityName, pSourceId, 320, pFEIN);
    }

    public void ActivePrimaryEntitlementAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceCompanyID, String pEntitlmentType) {
        addMessage(pEntityName, pSourceId, 321, pSourceCompanyID, pEntitlmentType);
    }

    public void EntitlementAlreadyExists(EntityName pEntityName, String pSourceId, String pLicenseNumber, String pEntitlementOfferingCode) {
        addMessage(pEntityName, pSourceId, 322, pLicenseNumber, pEntitlementOfferingCode);
    }

    public void EntitlementUnitDoesNotExist(EntityName pEntityName, String pSourceId, String pFEIN, String pLicenseNumber, String pEntitlementOfferingCode) {
        addMessage(pEntityName, pSourceId, 323, pFEIN, pLicenseNumber, pEntitlementOfferingCode);
    }

    public void EntitlementUnitExistsWithSameFEIN(EntityName pEntityName, String pSourceId, String pLicenseNumber, String pEntitlementOfferingCode, String pFEIN) {
        addMessage(pEntityName, pSourceId, 324, pLicenseNumber, pEntitlementOfferingCode, pFEIN);
    }

    public void EntitlementDisabled(EntityName pEntityName, String pSourceId, String pLicenseNumber, String pEntitlementOfferingCode, String pFEIN) {
        addMessage(pEntityName, pSourceId, 325, pLicenseNumber, pEntitlementOfferingCode, pFEIN);
    }

    public void ActivePrimaryEntitlementDoesNotExists(EntityName pEntityName, String pSourceId, String pCompanyId) {
        addMessage(pEntityName, pSourceId, 326, pCompanyId);
    }

    public void OfferingCanNotAssignToCompany(EntityName pEntityName, String pSourceId, String pOfferingCd, String pCompanyId) {
        addMessage(pEntityName, pSourceId, 327, pOfferingCd, pCompanyId);
    }

    public void OfferingEffectiveDateInFuture(EntityName pEntityName, String pSourceId, String pOfferingCd) {
        addMessage(pEntityName, pSourceId, 328, pOfferingCd);
    }

    public void MaxDiamondAssistedEntitlementUnitsAlreadyExist(EntityName pEntityName, String pSourceId, String pFEIN) {
        addMessage(pEntityName, pSourceId, 329, pFEIN);
    }

    public void CannotDeleteCurrentPrice(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 400);
    }

    public void CorpIdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 410);
    }

    public void UserAlreadyExists(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 411, pSourceId);
    }

    public void RoleIdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 412);
    }

    public void RoleDoesNotExist(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 413, pSourceId);
    }

    public void UserDoesNotExist(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 414, pSourceId);
    }

    public void OperationIdNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 415);
    }

    public void DomainDoesNotExist(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 416, pSourceId);
    }

    public void RoleAlreadyExists(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 417, pSourceId);
    }

    public void FLARequiresQuarterLaw(EntityName pEntityName, String  pDebitAccountCode, String pCreditAccountCode) {
        addMessage(pEntityName, "", 418, pDebitAccountCode, pCreditAccountCode);
    }

    public void FLACanNotAddedToPendingPayroll(EntityName pEntityName, String  pPayrollRunId) {
        addMessage(pEntityName, "", 420, pPayrollRunId);
    }

    public void CanNotCreateFLATransaction(EntityName pEntityName, String  pDebitAccountCode, String pCreditAccountCode) {
        addMessage(pEntityName, "", 419, pDebitAccountCode, pCreditAccountCode);
    }

    public void SettlementTypeInvalidForIntuitReversal(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 501);
    }

    public void ReversalFeeCanNotChargedForIntuitReversals(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 502);
    }

    public void CanNotReversePartialPayrollsForIntuitReversals(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 503);
    }

    public void RedebitAmountExceedsUncollectedAmount(EntityName pEntityName, String pSourceId, String pAmount, String pTransactionType, String pFeeType) {
        if (null != pFeeType) {
            addMessage(pEntityName, pSourceId, 504, pAmount, pTransactionType + ":" + pFeeType);
        } else {
            addMessage(pEntityName, pSourceId, 504, pAmount, pTransactionType);
        }
    }

    public void PayeeAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourcePayeeId) {
        addMessage(pEntityName, pSourceId, 600, pSourcePayeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void DDStatusNotValid(EntityName pEntityName, String pSourceId, String pStatus) {
        addMessage(pEntityName, pSourceId, 601, pStatus);
    }

    public void CompanyNotSignedForDD(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 602);
    }

    public void DepositDateOnHolidayOrWeekend(EntityName pEntityName, String pSourceId, String pDepositDate) {
        addMessage(pEntityName, pSourceId, 603, pDepositDate);
    }

    public void InvalidDepositDate(EntityName pEntityName, String pPayrollTXBatchId, String pCutoffTime, String pFundingModelDays,
                                   String pPaycheckDepositDate) {
        addMessage(pEntityName, pPayrollTXBatchId, 604, pCutoffTime, pFundingModelDays, pPaycheckDepositDate);
    }

    public void PaymentSubmittedWithPendingNOC(EntityName pEntityName, String pSourceId, String pNumberOfAttempts, String pPayeeName) {
        addMessage(pEntityName, pSourceId, 605, pNumberOfAttempts, pPayeeName);
    }

    public void DepositDateTooFarInTheFuture(EntityName pEntityName, String pSourceId, String pNumberOfDays) {
        addMessage(pEntityName, pSourceId, 606, pNumberOfDays);
    }

    public void BillPaymentDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourcePaymentId) {
        addMessage(pEntityName, pSourceId, 607, pSourcePaymentId, pSourceSystemCd, pSourceCompanyId);
    }

    public void BillPaymentAlreadyCanceled(EntityName pEntityName, String pSourceId, String pSourcePaymentId) {
        addMessage(pEntityName, pSourceId, 608, pSourcePaymentId);
    }

    public void DuplicatePaymentSplitId(EntityName pEntityName, String pSourceId, String pSplitId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 609, pSplitId, pSourceSystemCd, pSourceCompanyId);
    }

    public void DuplicatePaymentId(EntityName pEntityName, String pSourceId, String pPaymentId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 610, pPaymentId, pSourceSystemCd, pSourceCompanyId);
    }

    public void BillPaymentAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceBillPaymentId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 611, pSourceBillPaymentId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PaymentSubmissionExceedsLimits(EntityName pEntityName, String pSourceId, String pPaymentDate, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 612, pSourceSystemCd, pSourceCompanyId, pPaymentDate);
    }

    public void LimitExceededForPayee(EntityName pEntityName, String pSourceId, String pPayeeName, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 613, pPayeeName, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayeeDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourcePayeeId) {
        addMessage(pEntityName, pSourceId, 614, pSourcePayeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void NotAnAS400Company(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 615);
    }

    public void InvalidPaymentAmount(EntityName pEntityName, String pSourceId, String pPayeeName, String pAmount) {
        addMessage(pEntityName, pSourceId, 616, pPayeeName, pAmount);
    }

    public void TotalRefundAmountNotPositive(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 617);
    }

    public void AlreadyRefundedOneTimeCourtesyRefund(EntityName pEntityName, String pSourceId, String pFeeType) {
        addMessage(pEntityName, pSourceId, 618, pFeeType);
    }

    public void InvalidPaycheckDate(EntityName pEntityName, String pPayrollTXBatchId, String pCutoffTime, String pFundingModelDays,
                                    String pPaycheckDepositDate) {
        addMessage(pEntityName, pPayrollTXBatchId, 1006, pCutoffTime, pFundingModelDays, pPaycheckDepositDate);
    }

    public void RequestIdAlreadyExists(EntityName pEntityName, String pSourceId, String pRequestId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1008, pRequestId, pSourceSystemCd, pSourceCompanyId);
    }

    //This error message should specify that the DD company # service agreement was previously terminated and cannot be added.
    //Cannot change this now due to backward compatibility requirement

    public void CompanyNotAssociatedWithService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1010, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    //This error message should specify that the DD company # service agreement was previously terminated and cannot be added.
    //Cannot change this now due to backward compatibility requirement

    public void CompanyNotActiveOnService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1011, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    //This error message should specify that the DD company # service agreement was previously terminated and cannot be added.
    //Cannot change this now due to backward compatibility requirement

    public void CompanyAlreadyTerminatedForService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1012, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyAlreadyAssociatedWithService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1013, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    public void PayrollRunAlreadyOffloaded(EntityName pEntityName, String pSourceId, String pSourcePayrollId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1015, pSourcePayrollId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayrollRunAlreadyCanceled(EntityName pEntityName, String pSourceId, String pSourcePayrollId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1017, pSourcePayrollId, pSourceSystemCd, pSourceCompanyId);
    }

    public void MorethanOneHandlerMatchesCriteria(EntityName pEntityName, String pBankReturnCd,
                                                  String pTransactionTypeCd,
                                                  String pFinancialTxnId) {
        addMessage(pEntityName, pBankReturnCd, 1021, pTransactionTypeCd, pFinancialTxnId);
    }

    public void CompanyAlreadyCancelledOnService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1022, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    public void CompanyPreviouslyTerminatedOnService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1023, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    public void CompanyOnHoldForService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1024, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    public void CompanySuspendedForService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1025, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    public void CompanyPendingTerminationForService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1026, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    public void CompanyPendingActivationForService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1027, pSourceSystemCd, pSourceCompanyId, pServiceCd);
    }

    public void FinancialTransactionNotFound(EntityName pEntityName, String pSourceId, String pTxnId, String pSrcSystemCd, String pCompanyId) {
        addMessage(pEntityName, pSourceId, 1031, pTxnId);
    }

    public void EinInUse(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pFedTaxId) {
        addMessage(pEntityName, pSourceId, 1038, pSourceSystemCd, pSourceCompanyId, pFedTaxId);
    }

    //This error message should specify that the given service agreement was previously terminated and cannot be added.
    //Cannot change this now due to backward compatibility requirement

    public void EINInUseByTerminatedCompany(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pFedTaxId) {
        addMessage(pEntityName, pSourceId, 1039, pSourceSystemCd, pSourceCompanyId, pFedTaxId);
    }

    public void CompanyPendingActivation(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1040, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayrollRunExceedsDDLimits(EntityName pEntityName, String pSourceId, String pSourcePayrollRunId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1043, pSourcePayrollRunId, pSourceSystemCd, pSourceCompanyId);
    }

    public void EinInUseUpdate(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pFedTaxId) {
        addMessage(pEntityName, pSourceId, 1044, pSourceSystemCd, pSourceCompanyId, pFedTaxId);
    }

    //This error message should specify that the given service agreement was previously terminated and cannot be added.
    //Cannot change this now due to backward compatibility requirement

    public void EINInUseByTerminatedCompanyUpdate(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pFedTaxId) {
        addMessage(pEntityName, pSourceId, 1045, pSourceSystemCd, pSourceCompanyId, pFedTaxId);
    }

    public void PayrollRunTransactionsAlreadySent(EntityName pEntityName, String pSourceId, String pPayrollRunId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1046, pPayrollRunId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PayrollRunCannotBeCancelled(EntityName pEntityName, String pSourceId, String pPayrollRunId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1047, pPayrollRunId, pSourceSystemCd, pSourceCompanyId);
    }

    public void ActionNotValidForPayrollRun(EntityName pEntityName, String pSourceId, String pActionCd, String pPayrollRunId, String pPayrollStatusCd) {
        addMessage(pEntityName, pSourceId, 1048, pActionCd, pPayrollRunId, pPayrollStatusCd);
    }

    public void RequestTooBig(EntityName pEntityName, String pSourceId, String pSectionName, int pNumberInRequest) {
        addMessage(pEntityName, pSourceId, 12009, pSectionName, pNumberInRequest);
    }
    public void PayrollRejectFFCRAOutdatedItems(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 12010);
    }

    public void ActionNotValidForFinancialTransaction(EntityName pEntityName, String pSourceId, String pAction,
                                                      String pTxnId, String pTxnTypeCd, String pTxnStateCd) {
        addMessage(pEntityName, pSourceId, 1051, pAction, pTxnId, pTxnTypeCd, pTxnStateCd);
    }

    public void ActionNotValidForPayrollRunLedgerAccount(EntityName pEntityName, String pSourceId, String pActionCd, String pPayrollRunId) {
        addMessage(pEntityName, pSourceId, 1055, pActionCd, pPayrollRunId);
    }

    public void TransactionReturnIndexRangeInvalid(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1057);
    }

    public void PayrollRunMissingTxnType(EntityName pEntityName, String pSourceId, String pPayrollTXBatchId,
                                         String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1060, pPayrollTXBatchId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyHasAnotherActiveBankAccount(EntityName pEntityName, String pSourceId, String pSourceCompanyBankAccountId,
                                                   String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1061, pSourceSystemCd, pSourceCompanyId, pSourceCompanyBankAccountId);
    }

    public void CompanyDoesNotHaveActiveBankAccount(EntityName pEntityName, String pSourceId,
                                                    String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1062, pSourceSystemCd, pSourceCompanyId);
    }

    public void ServiceNotAvailableForCompanyOffering(EntityName pEntityName, String pSourceId,
                                                      String pSourceSystemCd, String pSourceCompanyId, String pOffering, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1063, pSourceSystemCd, pSourceCompanyId, pOffering, pServiceCd);
    }

    public void CannotUnassignNullAgentFromPayment(EntityName pEntityName, String pSourceId, String pMMTId) {
        addMessage(pEntityName, pSourceId, 1064, pMMTId);
    }

    public void CannotTerminateNonMoneyMovementService(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10085);
    }

    public void CannotUpdateManualPaymentMethodForNonManualPayment(EntityName pEntityName, String pMMTId) {
        addMessage(pEntityName, pMMTId, 1065, pMMTId);
    }

    public void CannotUpdatePaymentMethodToManualForPaymentsSentToEFE(EntityName pEntityName, String pMMTId) {
        addMessage(pEntityName, pMMTId, 1066, pMMTId);
    }

    public void CannotChangePaymentMethod(EntityName pEntityName, String pMMTId, String pPaymentMethod) {
        addMessage(pEntityName, pMMTId, 1067, pMMTId, pPaymentMethod);
    }

    public void CannotFindDefaultOffering(EntityName pEntityName, String pSourceId, String pSourceSystemId, String pServiceCd) {
        addMessage(pEntityName, pSourceId, 1068, pSourceSystemId, pServiceCd);
    }

    public void LawDoesNotExist(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1500);
    }

    public void EmployeeAccrualNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pSourceEmployeeId, String pAccrualType) {
        addMessage(pEntityName, pSourceId, 1501, pSourceEmployeeId, pSourceSystemCd, pSourceCompanyId, pAccrualType);
    }

    public void CompanyLawDoesNotExist(EntityName pEntityName, String pSourceSystemCd, String pSourceCompanyId, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1502, pSourceSystemCd, pSourceCompanyId, pSourceId);
    }

    public void EffectiveDateNotOnQuarterStart(EntityName pEntityName, String pSourceSystemCd, String pSourceCompanyId, String pSourceId, String pEffectiveDate) {
        addMessage(pEntityName, pSourceId, 1506, pSourceSystemCd, pSourceCompanyId, pSourceId, pEffectiveDate);
    }

    public void InvalidCOBRALiabilityAmount(EntityName pEntityName, String pSourceSystemCd, String pSourceCompanyId, String pSourceId, String pAmount) {
        addMessage(pEntityName, pSourceId, 1508, pSourceSystemCd, pSourceCompanyId, pSourceId, pAmount);
    }

    public void LiabilityAdjustmentNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1510, pSourceId, pSourceSystemCd, pSourceCompanyId);
    }

    public void ActiveServiceExistsForEin(EntityName pEntityName, String pSourceId, String pServiceCode1, String pFedTaxId, String pServiceCode2) {
        addMessage(pEntityName, pSourceId, 1511, pServiceCode1, pFedTaxId, pServiceCode2);
    }

    public void GenericError(EntityName pEntityName, String pSourceId, String pErrorText) {
        addMessage(pEntityName, pSourceId, 5000, pErrorText);
    }

    public void GenericWarning(EntityName pEntityName, String pSourceId, String pErrorText) {
        addMessage(pEntityName, pSourceId, 7000, pErrorText);
    }

    public void InvalidValue(EntityName pEntityName, String pSourceId, String pInvalidValueName) {
        addMessage(pEntityName, pSourceId, 5001, pInvalidValueName);
    }

    public void InvalidValue(EntityName pEntityName, String pSourceId, String pInvalidValueName, Object pInvalidValue) {
        addMessage(pEntityName, pSourceId, 5006, pInvalidValueName, pInvalidValue != null ? pInvalidValue : "null");
    }

    public void InvalidValueWarning(EntityName pEntityName, String pSourceId, String pInvalidValueName) {
        addMessage(pEntityName, pSourceId, 5005, pInvalidValueName);
    }

    public void RequiredInputMissingOrBlank(EntityName pEntityName, String pSourceId, String pInputName) {
        addMessage(pEntityName, pSourceId, 5002, pInputName);
    }

    public void NoEntityWithGivenId(String pEntityName, String pEntityId) {
        addMessage(null, null, 5003, pEntityName, pEntityId);
    }

    public void PropertyValueNotUnique(String pPropertyName, String pPropertyId) {
        addMessage(null, null, 5004, pPropertyName, pPropertyId);
    }

    public void PaycheckEmployeeNull(EntityName pEntityName, String pSourceId, String domainEntity) {
        addMessage(pEntityName, null, 5007, pEntityName.toString(), domainEntity, pSourceId);
    }

    public void EntitiesDontMatch(EntityName pEntityName, String pIQBUNIQUEID, String sourceEmployeeId) {
        addMessage(pEntityName, null, 5008, pEntityName.toString(), pIQBUNIQUEID, sourceEmployeeId);
    }


    public void InvalidPaymentTemplateCategory(EntityName pEntityName, String pSourceId, String pPaymentTemplateCd, String pCategory) {
        addMessage(pEntityName, pSourceId, 6000, pPaymentTemplateCd, pCategory);
    }

    public void InvalidMMTQuarter(EntityName pEntityName, String pSourceId, String pMMTId, String pQuarter) {
        addMessage(pEntityName, pSourceId, 6001, pMMTId, pQuarter);
    }

    public void InvalidYearQuarter( String pYear, String pQuarter) {
        addMessage(null, null, 6002, pYear, pQuarter);
    }

    public void InvalidTaxPaymentStatus( EntityName pEntityName, String pMMTId, String pCurrentTaxPaymentStatus, String pExpectedTaxPaymentStatus) {
        addMessage(pEntityName, pMMTId, 6003, pMMTId, pCurrentTaxPaymentStatus,pExpectedTaxPaymentStatus);
    }

    public void AllFinancialTransactionsSameMMT() {
        addMessage(null, null, 6004);
    }
    public void NoTransactionResponsesFound(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pToken) {
        addMessage(pEntityName, pSourceId, 10000, pSourceSystemCd, pSourceCompanyId, pToken);
    }

    public void DuplicateDDTransactionId(EntityName pEntityName, String pSourceId, String pTxnId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10001, pTxnId, pSourceSystemCd, pSourceCompanyId);
    }

    public void DuplicatePaycheckId(EntityName pEntityName, String pSourceId, String pSourcePayCheckId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10002, pSourcePayCheckId, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyDoesNotExistSourceSystemCdSourceSystemId(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10003, pSourceSystemCd, pSourceCompanyId);
    }

    public void CompanyDoesNotExistOnService(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pServiceName) {
        addMessage(pEntityName, pSourceId, 10004, pSourceSystemCd, pSourceCompanyId, pServiceName);
    }

    public void ServiceOperationNotAllowed(String pSourceSystemCd, String pSourceCompanyId, String pServiceCd, String pOperationName) {
        addMessage(EntityName.Company, pSourceCompanyId, 1100, pSourceSystemCd, pSourceCompanyId, pServiceCd, pOperationName);
    }

    public void CompanyOperationNotAllowed(String pSourceSystemCd, String pSourceCompanyId, String pOperationName) {
        addMessage(EntityName.Company, pSourceCompanyId, 1101, pSourceSystemCd, pSourceCompanyId, pOperationName);
    }

    public void CompanyBankAccountCannotHaveRandomDebitsReissued(EntityName pEntityName, String pSourceId, String pSourceBankAccountId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1102, pSourceBankAccountId, pSourceSystemCd, pSourceCompanyId);
    }

    public void ServiceStatusNotAllowedToChange(EntityName pEntityName, String pSourceId, String pServiceCode, String pServiceStatusCode) {
        addMessage(pEntityName, pSourceId, 1103, pServiceCode, pServiceStatusCode);
    }

    public void ServiceStatusNotAllowedToSetManually(EntityName pEntityName, String pSourceId, String pServiceStatusCode) {
        addMessage(pEntityName, pSourceId, 1104, pServiceStatusCode);
    }

    public void ServiceStatusNotAllowed(EntityName pEntityName, String pSourceId, String pServiceStatusCode, String pServiceCode) {
        addMessage(pEntityName, pSourceId, 1105, pServiceStatusCode, pServiceCode);
    }

    public void ServiceStatusNotAllowedForSourceSystem(EntityName pEntityName, String pSourceId, String pServiceStatusCode, String pSourceSystemCd) {
        addMessage(pEntityName, pSourceId, 1106, pServiceStatusCode, pSourceSystemCd);
    }

    public void CompanyIsInDebtToIntuit(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 1107, pSourceSystemCd, pSourceCompanyId);
    }

    public void OnHoldNotAllowedToSetManually(EntityName pEntityName, String pSourceId, String pServiceStatusCode) {
        addMessage(pEntityName, pSourceId, 1108, pServiceStatusCode);
    }

    public void CompanyAlreadyInOnHoldStatus(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pOnHoldStatusCode) {
        addMessage(pEntityName, pSourceId, 1109, pSourceSystemCd, pSourceCompanyId, pOnHoldStatusCode);
    }

    public void OnHoldNotAllowedToRemoveManually(EntityName pEntityName, String pSourceId, String pServiceStatusCode) {
        addMessage(pEntityName, pSourceId, 1110, pServiceStatusCode);
    }

    public void CompanyNotInOnHoldStatus(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pOnHoldStatusCode) {
        addMessage(pEntityName, pSourceId, 1111, pSourceSystemCd, pSourceCompanyId, pOnHoldStatusCode);
    }

    public void ServiceStatusCannotBeChangedManually(EntityName pEntityName, String pSourceId, String pServiceStatusCode) {
        addMessage(pEntityName, pSourceId, 1112, pServiceStatusCode);
    }

    public void CompanyAgencyAlreadyInOnHoldStatus(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pAgencyId, String pOnHoldStatusCode) {
        addMessage(pEntityName, pSourceId, 1113, pSourceSystemCd, pSourceCompanyId, pOnHoldStatusCode, pAgencyId);
    }

    public void CompanyAgencyNotInOnHoldStatus(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pAgencyId, String pOnHoldStatusCode) {
        addMessage(pEntityName, pSourceId, 1114, pSourceSystemCd, pSourceCompanyId, pOnHoldStatusCode, pAgencyId);
    }

    public void InvalidHoldSubStatus(EntityName pEntityName, String pSourceId, ServiceSubStatusCode pOnHoldStatusCode) {
        addMessage(pEntityName, pSourceId, 1114, pOnHoldStatusCode == null ? "null" : pOnHoldStatusCode.name());
    }

    public void DestinationBankAccountNotActive(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1200);
    }

    public void CannotDeactivateWithPendingTransactions(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1201);
    }

    public void CannotAddMuiltipleSubStatuses(EntityName pEntityName, String pSourceId, String pServiceStatusCode) {
        addMessage(pEntityName, pSourceId, 1202, pServiceStatusCode);
    }

    public void ServiceStatusNotAllowedForRole(EntityName pEntityName, String pSourceId, String pServiceStatusCode, String pRoleId) {
        addMessage(pEntityName, pSourceId, 1203, pServiceStatusCode, pRoleId);
    }

    public void ServiceCannotMoveToServiceSubStatus(EntityName pEntityName, String pSourceId, String pServiceStatusCode, String pRoleId) {
        addMessage(pEntityName, pSourceId, 1204, pServiceStatusCode, pRoleId);
    }

    public void ServiceCannotMoveFromServiceSubStatus(EntityName pEntityName, String pSourceId, String pServiceStatusCode, String pRoleId) {
        addMessage(pEntityName, pSourceId, 1205, pServiceStatusCode, pRoleId);
    }

    public void CannotOverrideAmountForPayrollSkuType(EntityName pEntityName, String pSourceId, String pSourceSystemCode, String pOfferingServiceChargeType) {
        addMessage(pEntityName, pSourceId, 1301, pSourceSystemCode, pSourceId, pOfferingServiceChargeType);
    }

    public void AllRedebitsMustBelongToTheSamePayrollRun(EntityName pEntityName, String pSourceId, String pSourceSystemCode) {
        addMessage(pEntityName, pSourceId, 1302, pSourceSystemCode, pSourceId);
    }

    public void NACHAFileDoesNotExist(EntityName pEntityName, String pNACHAFileId) {
        addMessage(pEntityName, pNACHAFileId, 1303, pNACHAFileId);
    }

    public void CompanyDoesNotHaveCurrentActivationCheckList(EntityName pEntityName, String pSourceId, String pSourceSystemCode) {
        addMessage(pEntityName, pSourceId, 1304, pSourceSystemCode, pSourceId);
    }

    public void CheckListItemDoesNotExist(EntityName pEntityName, String pSourceId, String pCheckListItem) {
        addMessage(pEntityName, pSourceId, 1305, pCheckListItem);
    }

    public void CheckListItemStatusNotApplicable(EntityName pEntityName, String pSourceId, String pStatus, String pCheckListItem) {
        addMessage(pEntityName, pSourceId, 1306, pStatus, pCheckListItem);
    }

    public void CheckListItemStatusUpdateTypeNotValid(EntityName pEntityName, String pSourceId, String pUpdateType, String pCheckListItem, String pStatus) {
        addMessage(pEntityName, pSourceId, 1307, pUpdateType, pCheckListItem, pStatus);
    }

    public void ActivationCheckListNotReadyForFinalReview(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1308);
    }


    public void CannotAssignAgentToAPaymentWithoutEFEExceptions(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1309, pSourceId);
    }

    public void CannotResolveAPaymentWithoutUnresolvedEFEExceptions(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 1310, pSourceId);
    }

    public void UnresolvedNOCExists(EntityName pEntityName, String pSourceId, String pName) {
        addMessage(pEntityName, pSourceId, 2301, pName);
    }

    public void UnresolvedEECreditReturnExists(EntityName pEntityName, String pSourceId, String pName) {
        addMessage(pEntityName, pSourceId, 2501, pName);
    }

    public void InvalidOffloadGroup(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10005);
    }

    public void InvalidOffloadDateTime(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10006);
    }

    public void InvalidOffloadDateTime(EntityName pEntityName, String pSourceId, String pDateTime) {
        addMessage(pEntityName, pSourceId, 10007, pDateTime);
    }

    public void SecondOffloadAlreadyScheduled(EntityName pEntityName, String pSourceId, String pDateTime) {
        addMessage(pEntityName, pSourceId, 10008, pDateTime);
    }

    public void TooLateToScheduleSecondOffload(EntityName pEntityName, String pSourceId, String pDateTime) {
        addMessage(pEntityName, pSourceId, 10009, pDateTime);
    }

    public void FailedToScheduleSecondOffload(EntityName pEntityName, String pSourceId, String pDateTime) {
        addMessage(pEntityName, pSourceId, 10010, pDateTime);
    }

    public void SettlementDateNotFutureBankingDay(EntityName pEntityName, String pSourceId, String pSettlementTypeDate,
                                                  String pSettlementTypeCd) {
        addMessage(pEntityName, pSourceId, 10011, pSettlementTypeCd, pSettlementTypeDate);
    }

    public void CompanyOperationNotAllowedForCurrentStatus(String pSourceSystemCd, String pSourceCompanyId, String pCurrentStatus) {
        addMessage(EntityName.Company, pSourceCompanyId, 10012, pSourceSystemCd, pSourceCompanyId, pCurrentStatus);
    }

    public void FailedToExecuteMonthlyGemsUploadProcess(EntityName pEntityName, String pSourceId, String pReportingPeriod) {
        addMessage(pEntityName, pSourceId, 10013, pReportingPeriod);
    }

    public void MonthlyGemsUploadDataNotGenerated(EntityName pEntityName, String pSourceId, String pReportingPeriod) {
        addMessage(pEntityName, pSourceId, 10014, pReportingPeriod);
    }

    public void InvalidUploadStatus(EntityName pEntityName, String pSourceId, String pBatchId, String pUploadStatus, String pReportingPeriod) {
        addMessage(pEntityName, pSourceId, 10015, pBatchId, pUploadStatus, pReportingPeriod);
    }

    public void AgencyCodeNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10016, pSourceId);
    }

    public void EnrollmentStatusNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10017, pSourceId);
    }

    public void EnrollmentNotFound(EntityName pEntityName, String pSourceId, String pAgencyId, String pType) {
        addMessage(pEntityName, pSourceId, 10018, pAgencyId, pType);
    }

    public void FormNotFound(EntityName pEntityName, String pSourceId, String pFormId) {
        addMessage(pEntityName, pSourceId, 10019, pFormId);
    }

    public void DocumentInfoNotSpecified(EntityName pEntityName, String pSourceId, String pSourceSystemCode, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10020, pSourceSystemCode, pSourceCompanyId);
    }

    public void JurisdictionNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10021);
    }

    public void JurisdictionNotFound(EntityName pEntityName, String pSourceId, String pJurisdictionId) {
        addMessage(pEntityName, pSourceId, 10022, pJurisdictionId);
    }

    public void IntuitResponsibilityStartDateNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10023);
    }

    public void CompanyDoesNotSubscribeToTaxService(EntityName pEntityName, String pSourceId, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10024, pSourceId, pSourceCompanyId);
    }

    public void ServiceInfoNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10025);
    }

    public void InvalidAgencyCode(EntityName pEntityName, String pSourceId, String pAgencyCd) {
        addMessage(pEntityName, pSourceId, 10026, pAgencyCd);
    }

    public void CompanyAgencyDataNotSpecified(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pAgencyCd) {
        addMessage(pEntityName, pSourceId, 10027, pSourceSystemCd, pSourceCompanyId, pAgencyCd);
    }

    public void InvalidPaymentTemplateForAgency(EntityName pEntityName, String pSourceId, String pAgencyCd, String pPaymentTemplateCd) {
        addMessage(pEntityName, pSourceId, 10028, pAgencyCd, pPaymentTemplateCd);
    }

    public void CompanyAgencyNotFound(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pAgencyCd) {
        addMessage(pEntityName, pSourceId, 10029, pSourceSystemCd, pSourceCompanyId, pAgencyCd);
    }

    public void EnrollmentStateTransitionNotAllowed(EntityName pEntityName, String pSourceId,
                                                    String pEnrollmentType, String pCurrentValue, String pNewValue) {
        addMessage(pEntityName, pSourceId, 10030, pEnrollmentType, pCurrentValue, pNewValue);
    }

    public void AgencyAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceCompanyId, String pAgencyId) {
        addMessage(pEntityName, pSourceId, 10031, pSourceCompanyId, pAgencyId);
    }

    public void InvalidAgencyIdForACHEnrollments(EntityName pEntityName, String pSourceId, String pAgencyId) {
        addMessage(pEntityName, pSourceId, 10032, pAgencyId);
    }

    public void AgencyIdAndFedTaxIdDoesNotMatch(EntityName pEntityName, String pSourceId, String pSourceCompanyId,
                                                String pFedTaxId, String pAgcyId) {
        addMessage(pEntityName, pSourceId, 10033, pSourceCompanyId, pFedTaxId, pAgcyId);
    }

    public void DocumentCannotBeChanged(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId,
                                        String pCurrentDocId) {
        addMessage(pEntityName, pSourceId, 10034, pSourceSystemCd, pSourceCompanyId, pCurrentDocId);
    }

    public void NoPayrollReceivedWhileSubmittingForRAF(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10035, pSourceSystemCd, pSourceCompanyId);
    }

    public void RAAEnrollmentNotFound(EntityName pEntityName, String pSourceId, String pSourceCompanyId, String pAgencyId) {
        addMessage(pEntityName, pSourceId, 10036, pSourceId, pSourceCompanyId, pAgencyId);
    }

    public void AttemptToStartRAFEnrollmentWhileRAAEnrollmentNotComplete(EntityName pEntityName, String pSourceId,
                                                                         String pSourceCompanyId, String pAgencyId) {
        addMessage(pEntityName, pSourceId, 10037, pSourceId, pSourceCompanyId, pAgencyId);
    }

    public void DocumentNotSpecified(EntityName pEntityName, String pSourceId, String pSourceCompanyId, String pAgencyId) {
        addMessage(pEntityName, pSourceId, 10038, pSourceId, pSourceCompanyId, pAgencyId);
    }

    public void RAFEnrollmentNotFound(EntityName pEntityName, String pSourceId, String pSourceCompanyId, String pAgencyId) {
        addMessage(pEntityName, pSourceId, 10039, pSourceId, pSourceCompanyId, pAgencyId);
    }

    public void DocumentCannotBeDeleted(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId,
                                        String pCurrentDocId) {
        addMessage(pEntityName, pSourceId, 10040, pSourceSystemCd, pSourceCompanyId, pCurrentDocId);
    }

    public void PaymentTemplateNotAssignedToCompany(EntityName pEntityName, String pSourceId, String pSourceSystemCd,
                                                    String pSourceCompanyId, String pPaymentTemplateCd, String pAgencyId) {
        addMessage(pEntityName, pSourceId, 10041, pSourceSystemCd, pSourceCompanyId, pPaymentTemplateCd, pAgencyId);
    }

    public void PaymentFrequencyNotSupportedForThePaymentTemplate(EntityName pEntityName, String pSourceId,
                                                                  String pPaymentFrequencyId, String pPaymentTemplateCd) {
        addMessage(pEntityName, pSourceId, 10042, pPaymentFrequencyId, pPaymentTemplateCd);
    }

    public void EffectiveDepositFrequencyNotFound(EntityName pEntityName, String pSourceId, String pEffectiveDate) {
        addMessage(pEntityName, pSourceId, 10043, pEffectiveDate);
    }

    public void EffectiveDepositFrequencyDoesNotExistForMMT(EntityName pEntityName, String pSourceId, String pMMTId,
                                                            String pSourcePayrollRunId) {
        addMessage(pEntityName, pSourceId, 10044, pMMTId, pSourcePayrollRunId);
    }

    public void EffectiveDateShouldBeGreaterThanLatestDepositFrequency(EntityName pEntityName, String pSourceId, String pEffectiveDate,
                                                                       String pEffectiveDepositFrequency) {
        addMessage(pEntityName, pSourceId, 10045, pEffectiveDate, pEffectiveDepositFrequency);
    }

    public void EmployeeHasPaychecks(EntityName pEntityName, String pSourceId, String pSourceSystemCd,
                                     String pSourceCompanyId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 10046, pSourceEmployeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void LiabilityBalancesDiffer(EntityName pEntityName, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceCompanyId, 10047, pSourceSystemCd, pSourceCompanyId);
    }

    public void InvalidHPDEDateValue(EntityName pEntityName, String pSourceId, String pDateName, String pCurrentDateValue, String pOnOrAfterDate) {
        addMessage(pEntityName, pSourceId, 10048, pDateName, pCurrentDateValue, pOnOrAfterDate);
    }

    public void CompanyActiveBankAccountNotFound(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pAdditionalText) {
        addMessage(pEntityName, pSourceId, 10049, pSourceSystemCd, pSourceCompanyId, pAdditionalText);
    }

    public void PayrollRunIsNotHistorical(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10050, pSourceSystemCd, pSourceCompanyId);
    }

    public void PaycheckIsNotHistorical(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId) {
        addMessage(pEntityName, pSourceId, 10051, pSourceSystemCd, pSourceCompanyId, pPayrollRunId);
    }

    public void InvalidFinancialTransactionTypeForHistoricalPayroll(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, String pTransactionTypeCode) {
        addMessage(pEntityName, pSourceId, 10052, pSourceSystemCd, pSourceCompanyId, pPayrollRunId, pTransactionTypeCode);
    }

    public void InvalidFinancialTransactionStateForHistoricalPayroll(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pPayrollRunId, String pTransactionTypeCode, String pTransactionStateCode) {
        addMessage(pEntityName, pSourceId, 10053, pSourceSystemCd, pSourceCompanyId, pPayrollRunId, pTransactionTypeCode, pTransactionStateCode);
    }

    public void CheckNumberNotAllowedForPaymentMethod(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10054);
    }

    public void TaxPenaltyInterestDoesNotExist(EntityName pEntityName, String pSourceId, String pPenaltyInterestId) {
        addMessage(pEntityName, pSourceId, 10055, pPenaltyInterestId);
    }

    public void AmountOrPaymentMethodCannotBeModified(EntityName pEntityName, String pSourceId, String pType, String pTransactionState) {
        addMessage(pEntityName, pSourceId, 10056, pType, pTransactionState);
    }

    public void FieldCannotUpdateDuringEnrollment(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pFieldName) {
        addMessage(pEntityName, pSourceId, 10057, pSourceSystemCd, pSourceCompanyId, pFieldName);
    }

    public void RecallPayrollRequired(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10058);
    }

    public void PayrollItemAlreadyExists(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pPayrollItemId) {
        addMessage(pEntityName, pSourceId, 10059, pSourceSystemCd, pSourceCompanyId, pPayrollItemId);
    }

    public void PayrollItemDoesNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pPayrollItemId) {
        addMessage(pEntityName, pSourceId, 10060, pSourceSystemCd, pSourceCompanyId, pPayrollItemId);
    }

    public void ProviderDoesNotAcceptPayrollItem(EntityName pEntityName, String pSourceId, String pPaycheckId, String pPayrollItemCode) {
        addMessage(pEntityName, pSourceId, 10061, pPaycheckId, pPayrollItemCode);
    }

    public void NegativeAmountNotAllowedForPayrollItem(EntityName pEntityName, String pSourceId, String pPaycheckId, String pPayrollItemCode) {
        addMessage(pEntityName, pSourceId, 10062, pPaycheckId, pPayrollItemCode);
    }

    public void PaycheckAlreadyDeleted(EntityName pEntityName, String pSourceId, String pSourcePaycheckId) {
        addMessage(pEntityName, pSourceId, 10063, pSourcePaycheckId);
    }

    public void Voided401kPaycheckAlreadyOffloaded(EntityName pEntityName, String pSourceId, String pSourcePaycheckId) {
        addMessage(pEntityName, pSourceId, 10064, pSourcePaycheckId);
    }

    public void Deleted401kPaycheckAlreadyOffloaded(EntityName pEntityName, String pSourceId, String pSourcePaycheckId) {
        addMessage(pEntityName, pSourceId, 10065, pSourcePaycheckId);
    }

    public void Updated401kPaycheckAlreadyOffloaded(EntityName pEntityName, String pSourceId, String pSourcePaycheckId) {
        addMessage(pEntityName, pSourceId, 10066, pSourcePaycheckId);
    }

    public void CannotDeleteDDEmployees(EntityName pEntityName, String pSourceId, String pSourceSystemCd,
                                        String pSourceCompanyId, String pSourceEmployeeId) {
        addMessage(pEntityName, pSourceId, 10067, pSourceEmployeeId, pSourceSystemCd, pSourceCompanyId);
    }

    public void PaycheckPayrollItemDoesNotExistInTransmission(EntityName pEntityName, String pSourceId, String pPayrollItemId) {
        addMessage(pEntityName, pSourceId, 10068, pSourceId, pPayrollItemId);
    }

    public void PaycheckDateAfter401kTransmittalDate(EntityName pEntityName, String pSourceId, String pPaycheckDate) {
        addMessage(pEntityName, pSourceId, 10069, pPaycheckDate);
    }

    public void PaycheckHasInvalidPayPeriodStartDate(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10070, pSourceId, pSourceId);
    }

    public void PaycheckHasInvalidPayPeriodEndDate(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10071, pSourceId, pSourceId);
    }

    public void DuplicateEmployeeSSN(EntityName pEntityName, String pSourceId, String employee1Name, String ssn, String employee2Name) {
        if (employee1Name == null) {
            employee1Name = "";
        }
        if (ssn == null) {
            ssn = "";
        }
        if (employee2Name == null) {
            employee2Name = "";
        }
        addMessage(pEntityName, pSourceId, 10072, employee1Name, ssn, employee2Name);
    }

    public void OutOfSyncEmployeeSSNRequired(EntityName pEntityName, String pSourceId, String employeeFullName) {
        addMessage(pEntityName, pSourceId, 10073, pSourceId, employeeFullName);
    }

    public void DirectDepositPaycheckModificationBeforeOffload(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10074, pSourceId);
    }

    public void ServiceNotAllowedToCancelManually(EntityName pEntityName, String pSourceId, String pServiceCode) {
        addMessage(pEntityName, pSourceId, 10075, pServiceCode);
    }

    public void No401KPayrollItems(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10076);
    }

    public void DirectDepositPaycheckModificationAfterOffload(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10077);
    }

    public void PaychecksWith401KDefferalsMustHavePositiveCompensation(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10078);
    }

    public void DirectDepositPaychecksVoidedAfterOffload(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10079);
    }

    public void QBDT401kIntegrationNotAvailable(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10081);
    }

    public void QBDT401kIntegrationRequiresMoreRecentQBVersion(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10082);
    }

    public void QBDT401kIntegrationRequiresMoreRecentQBDotNetVersion(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10083);
    }

    public void QBDT401kIntegrationAssistedQueued(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10084);
    }

    public void QBDT401kAssistedInvalidCheckDateCompanyMsg(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10086);
    }

    public void QBDT401kAssistedInvalidCheckDateCheckMsg(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10087);
    }

    public void TaxPaycheckModified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10088);
    }

    public void EmployeeNotExist(EntityName pEntityName, String pSourceId, String pSourceSystemCd, String pSourceCompanyId, String pEmployeeId) {
        addMessage(pEntityName, pSourceId, 10089, pSourceSystemCd, pSourceCompanyId, pEmployeeId);
    }

    public void DuplicatePaycheckUniqueIdMatchesPreviousPaycheck(EntityName pEntityName, String pSourceId, String pSourcePayCheckId, String pSourceSystemCd, String pSourceCompanyId) {
        addMessage(pEntityName, pSourceId, 10090, pSourcePayCheckId, pSourceSystemCd, pSourceCompanyId);
    }

    public void AgentCannotTerminateNonMoneyMovementCompany(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 11002);
    }

    public void CompanyOperationNotAllowed401k(EntityName pEntityName, String pSourceSystemCd, String pSourceId, String pOperationName) {
        addMessage(pEntityName, pSourceId, 11101, pOperationName);
    }

    public void InvalidArgumentType(EntityName pEntityName, String pSourceId, String pArgument, String pExpectedType) {
        addMessage(pEntityName, pSourceId, 11000, pArgument, pExpectedType);
    }

    public void PriorPaymentAmountChangeNotAllowed(Company c, String type, String sourceTxnId) {
        addMessage(EntityName.Company, c.getSourceCompanyId(), 10100, type, sourceTxnId);
    }

    public void PriorPaymentDateChangeNotAllowed(Company c, String date, String value) {
        addMessage(EntityName.Company, c.getSourceCompanyId(), 10101, date, value);
    }

    public void CompanyLawDoesNotMatchLaw(String companyLawId, String lawIdFromCompanyLaw, String lawId) {
        addMessage(EntityName.CompanyLaw, companyLawId, 10102, lawIdFromCompanyLaw, lawId);
    }

    public void PriorPaymentCannotBeUnvoided(Company c, String sourceTxnId) {
        addMessage(EntityName.Company, c.getSourceCompanyId(), 10103, sourceTxnId);
    }

    public void PriorPaymentTaxLineCannotBeAddedOrDeleted(Company c, String sourceTxnId) {
        addMessage(EntityName.Company, c.getSourceCompanyId(), 10104, sourceTxnId);
    }

    public void PriorPaymentCannotChangeIsRefund(Company c, String sourceTxnId) {
        addMessage(EntityName.Company, c.getSourceCompanyId(), 10105, sourceTxnId);
    }

    public void PriorPaymentAmountsDoNotMatch(Company c, String sourceTxnId) {
        addMessage(EntityName.Company, c.getSourceCompanyId(), 10106, sourceTxnId);
    }

    public void PriorPaymentTemplateDoesNotMatch(Company c, String sourceTxnId) {
        addMessage(EntityName.Company, c.getSourceCompanyId(), 20107, sourceTxnId);
    }

    public void NewInitiationDateBeforeOffloadDate(EntityName pEntityName, String pSourceId, SpcfCalendar pNewInitDate, SpcfCalendar pOffloadDate) {
        addMessage(pEntityName, pSourceId, 10107, pSourceId, pNewInitDate, pOffloadDate);
    }

    public void PaymentMethodDoesNotMatch(EntityName pEntityName, String pSourceId, String pPaymentMethods) {
        addMessage(pEntityName, pSourceId, 10108, pSourceId, pPaymentMethods);
    }

    public void PaymentStatusDoesNotMatch(EntityName pEntityName, String pSourceId, String pPaymentStatus) {
        addMessage(pEntityName, pSourceId, 10109, pSourceId, pPaymentStatus);
    }

    public void StatusDoesNotMatch(EntityName pEntityName, String pSourceId, String pStatus) {
        addMessage(pEntityName, pSourceId, 10110, pSourceId, pStatus);
    }

    public void NewInitiationDateIsSame(EntityName pEntityName, String pSourceId, SpcfCalendar pNewInitDate, SpcfCalendar pOffloadDate) {
        addMessage(pEntityName, pSourceId, 10111, pSourceId, pNewInitDate, pOffloadDate);
    }

    public void PaymentTemplateDoesNotExist(EntityName pEntityName, String pSourceId, String pPaymentTemplateId) {
        addMessage(pEntityName, pSourceId, 10112, pPaymentTemplateId);
    }

    public void ACHEnrollmentNotSupported(EntityName pEntityName, String pSourceId, String pPaymentTemplateId) {
        addMessage(pEntityName, pSourceId, 10113, pPaymentTemplateId);
    }

    public void ACHEnrollmentDoesNotExist(EntityName pEntityName, String pSourceId, String pCompanyId, String pPaymentTemplateId) {
        addMessage(pEntityName, pSourceId, 10114, pCompanyId, pPaymentTemplateId);
    }

    public void NewACHEnrollmentStatusNotAllowed(EntityName pEntityName, String pSourceId, String pCompanyId, String pPaymentTemplateId, String pFromStatus, String pToStatus) {
        addMessage(pEntityName, pSourceId, 10115, pCompanyId, pPaymentTemplateId, pFromStatus, pToStatus);
    }

    public void ACHEnrollmentStatusNotInEnrolledToDelete(EntityName pEntityName, String pSourceId, String pCompanyId, String pPaymentTemplateId) {
        addMessage(pEntityName, pSourceId, 10116, pCompanyId, pPaymentTemplateId);
    }

    public void LawNotSpecified(EntityName pEntityName, String pSourceId) {
        addMessage(pEntityName, pSourceId, 10117);
    }

    public void LawRateNotFound(EntityName pEntityName, String pSourceId, Company pCompany, Law pLaw, SpcfCalendar pQuarterStart) {
        addMessage(pEntityName, pSourceId, 10118, pCompany.getSourceCompanyId(), pLaw.getLawId(), pQuarterStart.toString());
    }

    public void LawRateNotUnique(EntityName pEntityName, String pSourceId, Company pCompany, Law pLaw, SpcfCalendar pQuarterStart) {
        addMessage(pEntityName, pSourceId, 10119, pCompany.getSourceCompanyId(), pLaw.getLawId(), pQuarterStart.toString());
    }

    public void TronCompanyDoesNotExistOnRealmId(EntityName pEntityName, String realmId) {
        addMessage(pEntityName, realmId, 10120);
    }

    public void MoneymovementAccountDoesNotExistOnRealmId(EntityName pEntityName, String realmId) {
        addMessage(pEntityName, realmId, 10121);
    }

    public void AccountsServiceAccessError(String realmId, String reason) {
        addMessage(EntityName.AccountService, realmId, 10122, "accessing" ,realmId, reason);
    }

    public void AccountsServiceUpdateError(String realmId, String reason) {
        addMessage(EntityName.AccountService, realmId, 10122, "updating", realmId, reason);
    }

    public void AccountsServiceValidateError(String realmId, String reason) {
        addMessage(EntityName.AccountService, realmId, 10122, "validating", realmId, reason);
    }

    public void DuplicateActiveCompaniesFoundForRealm(EntityName pEntityName, String realmId) {
        addMessage(pEntityName, realmId, 10123, realmId);
    }

    public void ActiveCompanyFoundForRealm(EntityName pEntityName, String realmId) {
        addMessage(pEntityName, realmId, 10124, realmId);
    }

    public void RealmUpdateNotAllowed(EntityName pEntityName, String realmId) {
        addMessage(pEntityName, realmId, 10125, realmId);
    }

    public void IUSGrantGenericError(String realmId, String reason) {
        addMessage(EntityName.IUS, realmId, 10126, realmId, reason);
    }

    public void AStoPSPSyncError(String realmId, String reason) {
        addMessage(EntityName.AccountService, realmId, 10127, "syncing", realmId, reason);
    }

    public void ASValidationException(String sourceCompanyId, String reason) {
        addMessage(EntityName.AccountService, sourceCompanyId, 10128,  reason);
    }

    //Message for VMPGrantFailure
    public void VMPGrantAdditionError(String realmId, String reason) {
        addMessage(EntityName.IUS, realmId, 10129, reason);
    }

    public void PSPMigrateRequestError(String realmId, String reason) {
        addMessage(EntityName.Company, realmId, 10130, reason);
    }

    public void AddressComparisonError(String realmId, String reason) {
        addMessage(EntityName.Company, realmId, 12011, reason);
    }

    public void ComplianceAddressNullError(String realmId, String reason) {
        addMessage(EntityName.Company, realmId, 12012, reason);
    }

    public void UpdateUnsupportedForEmployerEntity(String pArgument) {
        // get process name from stack trace
        String processName = getCallerClassName("UpdateUnsupportedForEmployerEntity");
        addMessage(EntityName.Company, processName, 10130, pArgument);
    }

    public void NonDDPayroll(String sourceCompanyId, String payrollRunSeq) {
        addMessage(EntityName.PayrollRun, sourceCompanyId, 12013, sourceCompanyId, payrollRunSeq);
    }

    public void NotTwoDayPayroll(String sourceCompanyId, String payrollRunSeq) {
        addMessage(EntityName.PayrollRun, sourceCompanyId, 12014, sourceCompanyId, payrollRunSeq);
    }

    public void NoPendingPayroll(String sourceCompanyId, String payrollRunSeq) {
        addMessage(EntityName.PayrollRun, sourceCompanyId, 12015, sourceCompanyId, payrollRunSeq);
    }

    public void NoPendingEmployerEmployeeTransaction(String sourceCompanyId, String payrollRunSeq) {
        addMessage(EntityName.PayrollRun, sourceCompanyId, 12016, sourceCompanyId, payrollRunSeq);
    }

    public void UnexpectedErEeSettlementDates(String sourceCompanyId, String payrollRunSeq) {
        addMessage(EntityName.PayrollRun, sourceCompanyId, 12017, sourceCompanyId, payrollRunSeq);
    }

    public void TaxAmountNotCollected( EntityName pEntityName, String sourceId, MoneyMovementTransaction mmt) {
        addMessage(pEntityName, sourceId, 12018, mmt);
    }

    public static ProcessResult replaceCompanyOperationNotAllowedMessages(ProcessResult pValidationResult, SystemCapabilityCode pOperationName, String pSourceSystemCode, String pSourceCompanyId) {
        MessageList messages = pValidationResult.getMessages(companyOperationNotAllowedMessageCode);
        for (Message currentMessage : messages) {
            pValidationResult.getMessages().CompanyOperationNotAllowed401k(EntityName.Company, pSourceSystemCode, pSourceCompanyId, pOperationName.toString());
        }

        pValidationResult.getMessages().removeMessage(companyOperationNotAllowedMessageCode);

        return pValidationResult;
    }

    /**
     * {entity} {attribute} is required
     */
    public void RequiredAttribute(EntityName pEntityName, String pSourceId, String pAttribute) {
        RequiredAttribute(pEntityName, pSourceId, pAttribute, MessageInfo.MessageLevel.ERROR);
    }

    /**
     * {entity} {attribute} is required
     */
    public void RequiredAttribute(EntityName pEntityName, String pSourceId, String pAttribute, MessageInfo.MessageLevel pMessageLevel) {
        addMessage(pEntityName, pSourceId, pMessageLevel, 12000, pEntityName.name(), pAttribute, pEntityName.name().toLowerCase());
    }

    /**
     * {entity} {attribute} is required
     */
    public void InvalidLength(EntityName pEntityName, String pSourceId, String pAttribute, int maxLength) {
        InvalidLength(pEntityName, pSourceId, pAttribute, maxLength, MessageInfo.MessageLevel.ERROR);
    }

    /**
     * {entity} {attribute} must be less than {n} characters
     */
    public void InvalidLength(EntityName pEntityName, String pSourceId, String pAttribute, int maxLength, MessageInfo.MessageLevel pMessageLevel) {
        addMessage(pEntityName, pSourceId, pMessageLevel, 12001, pEntityName.name(), pAttribute, maxLength);
    }

    /**
     * {entity} {attribute} must be less than {n} characters
     */
    public void PatternValidationFailure(EntityName pEntityName, String pSourceId, String pAttribute, String pPatternTemplate) {
        PatternValidationFailure(pEntityName, pSourceId, pAttribute, pPatternTemplate, MessageInfo.MessageLevel.ERROR);
    }

    /**
     * {entity} {attribute} is not a valid value.  It must be entered as {human-pattern}.
     */
    public void PatternValidationFailure(EntityName pEntityName, String pSourceId, String pAttribute, String pPatternTemplate, MessageInfo.MessageLevel pMessageLevel) {
        addMessage(pEntityName, pSourceId, pMessageLevel, 12002, pEntityName.name(), pAttribute, pPatternTemplate);
    }

    /**
     * {entity} {attribute} is not a valid value. {range-message}
     */
    public void RangeValidationFailure(EntityName pEntityName, String pSourceId, String pAttribute, Number min) {
        RangeValidationFailure(pEntityName, pSourceId, pAttribute, min, MessageInfo.MessageLevel.ERROR);
    }

    /**
     * {entity} {attribute} is not a valid value. {range-message}
     */
    public void RangeValidationFailure(EntityName pEntityName, String pSourceId, String pAttribute, Number min, MessageInfo.MessageLevel pMessageLevel) {
        RangeValidationFailure(pEntityName, pSourceId, pAttribute, min, null, pMessageLevel);
    }

    /**
     * {entity} {attribute} is not a valid value. {range-message}
     */
    public void RangeValidationFailure(EntityName pEntityName, String pSourceId, String pAttribute, Number min, Number max) {
        RangeValidationFailure(pEntityName, pSourceId, pAttribute, min, max, MessageInfo.MessageLevel.ERROR);
    }

    /**
     * {entity} {attribute} is not a valid value. {range-message}
     */
    public void RangeValidationFailure(EntityName pEntityName, String pSourceId, String pAttribute, Number min, Number max, MessageInfo.MessageLevel pMessageLevel) {
        String rangeMessage = "";
        if (min != null && max != null) {
            rangeMessage = "The value must be between " + min.toString() + " and " + max.toString() + ".";
        } else if (min != null && max == null) {
            rangeMessage = "The minimum valid value is " + min.toString();
        } else if (min == null && max != null) {
            rangeMessage = "The maximum valid value is " + max.toString();
        }

        addMessage(pEntityName, pSourceId, pMessageLevel, 12003, pEntityName.name(), pAttribute, rangeMessage);
    }

    public void ExceptionOccurred(Throwable t) {
        ExceptionOccurred(null, t, true);
    }

    public void ExceptionOccurred(String errorText, Throwable t) {
        ExceptionOccurred(errorText, t, true);
    }

    public void ExceptionOccurred(String errorText, Throwable t, boolean includeStackTrace) {
        String message = errorText != null ? errorText + " - " : "";
        if (includeStackTrace) {
            StringWriter sw = new StringWriter(1024);
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            message = message + "\n" + sw;
        }

        ExceptionOccurred(message);
    }

    public void ExceptionOccurred(String errorText) {
        addMessage(EntityName.TransactionResponse, "", 12004, errorText);
    }

    public void FormTemplateError(EntityName pEntityName, String pSourceId, String formTemplate) {
        addMessage(pEntityName, pSourceId, 12005, formTemplate);
    }

    /**
     * ******************************************************
     * ALL MESSAGES SHOULD BE ADDED ABOVE THE FOLLOWING
     * SUPPORT METHODS
     * *******************************************************
     */
    public String toString() {
        return toDebugString();
    }

    private String toDebugString() {
        StringBuffer buffer = new StringBuffer();
        for (Message messageInfo : this) {
            buffer.append(messageInfo).append("\n");
        }
        return buffer.toString();
    }

    public boolean containsErrorMessage() {
        return bContainsErrors;
    }

    public boolean containsWarningMessage() {
        return bContainsWarnings;
    }

    private Message addMessage(EntityName pEntityName, String pSourceId, int messageNumber, Object... args) {
        Message message = createPSPMessage(pEntityName, pSourceId, messageNumber, args);
        this.add(message);
        return message;
    }

    private Message addMessage(EntityName pEntityName, String pSourceId, MessageInfo.MessageLevel pMessageLevel, int messageNumber, Object... args) {
        Message message = createPSPMessage(pEntityName, pSourceId, pMessageLevel, messageNumber, args);
        this.add(message);
        return message;
    }

    private Message createPSPMessage(EntityName pEntityName, String pSourceId, int messageNumber, Object... args) {
        MessageDefinition definition = MessageDefinition.getMessageDefinition(messageNumber);
        MessageInfo.MessageLevel messageLevel = defaultMessageLevel != null ? defaultMessageLevel : definition.getLevel();
        return createPSPMessage(pEntityName, pSourceId, messageLevel, definition, args);
    }

    private Message createPSPMessage(EntityName pEntityName, String pSourceId, MessageInfo.MessageLevel messageLevel, int messageNumber, Object... args) {
        MessageDefinition defintion = MessageDefinition.getMessageDefinition(messageNumber);
        return createPSPMessage(pEntityName, pSourceId, messageLevel, defintion, args);
    }

    private Message createPSPMessage(EntityName pEntityName, String pSourceId, MessageInfo.MessageLevel messageLevel, MessageDefinition messageDefinition, Object... args) {
        Message message = new Message();
        message.setEntityName(pEntityName);
        message.setSourceId(pSourceId);
        message.setMessageCode(Integer.toString(messageDefinition.getNumber()));
        message.setLevel(messageLevel);
        message.setStackTrace(Thread.currentThread().getStackTrace());

        if (!bContainsErrors && message.getLevel() == MessageInfo.MessageLevel.ERROR) {
            this.bContainsErrors = true;
        }

        if (!bContainsWarnings && message.getLevel() == MessageInfo.MessageLevel.WARNING) {
            bContainsWarnings = true;
        }

        MessageFormat format = new MessageFormat(messageDefinition.getMessageFormat());
        message.setMessage(format.format(args));

        return message;
    }

    public boolean addAll(MessageList messageList) {
        this.bContainsErrors = this.bContainsErrors || messageList.bContainsErrors;
        bContainsWarnings = bContainsWarnings || messageList.bContainsWarnings;
        return super.addAll(messageList);
    }

    public boolean containsMessage(String messageCode) {
        for (Message message : this) {
            if (message.getMessageCode().equals(messageCode)) {
                return true;
            }
        }
        //
        return false;
    }

    public void removeMessage(String messageCode) {
        boolean found = false;
        bContainsErrors = false;
        bContainsWarnings = false;
        int idx = 0;
        for (Message message : this) {
            if (message.getMessageCode().equals(messageCode)) {
                found = true;
            } else {
                bContainsErrors = (bContainsErrors || message.getLevel() == MessageInfo.MessageLevel.ERROR);
                bContainsWarnings = (bContainsWarnings || message.getLevel() == MessageInfo.MessageLevel.WARNING);
            }
            if (!found) {
                idx++;
            }
        }
        if (found) {
            this.remove(idx);
        }
    }

    private static String getCallerClassName(String callingThis) {
        String callerClass = "";
        boolean nextMethodIsACaller = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement st : stackTrace) {
            if (st.toString().contains(callingThis)) {
                nextMethodIsACaller = true;
            } else {
                if (nextMethodIsACaller) {
                    callerClass = st.getClassName();
                    if (callerClass.indexOf(".") != -1) {
                        callerClass = callerClass.substring(callerClass.lastIndexOf(".") + 1);
                    }
                    break;
                }
            }
        }
        return callerClass;
    }


}
