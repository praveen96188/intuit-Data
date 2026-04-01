package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Contains each of the error messages that can be returned in the
 * response OFX returned to a QB client.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Feb 12, 2008
 * Time: 5:47:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ErrorMessages {

    public static enum ErrorEnum {
        MaxPinRetryError,
        AuthenticationFailedError,
        UnexpectedError,
        BadOFXError,
        DDCustomerBalanceFileError,
        PayrollUpdateNotInSyncError,
        BankAccountNotActiveError,
        FutureDatedPayrollTooFarInfutureError,
        ClientOutOfSyncMessage,
        QBFileIdChanged,
        DuplicatePaycheckSubmitted,
        DuplicateIPaycheckModSubmitted,
        AS400Unavailable,
        QBVersionSunsetted,
        QBReleaseNotSupported,
        QBTaxTableNotSupported,
        QBReleaseToBeSunset,
        QBReleaseWithChecksAfterSunset,
        PayrollRejectACHReturnR01ThruR09,
        PayrollRejectNOC,
        PayrollRejectACHReturnNonR01ThruR09,
        PayrollRejectRiskAssessment,
        PayrollRejectIntuitCollections,
        PayrollRejectRiskCollections,
        PayrollRejectFraud,
        PayrollRejectFraudReview,
        PayrollRejectSuspendedDD,
        PayrollRejectPendingTermination,
        PayrollRejectMissingPaperwork,
        PayrollRejectAuditCorrections,
        PayrollRejectDDLimit,
        PayrollRejectThreeEmployeesSameBankAcct,
        InvalidEERoutingNumber,
        PSPInternalErrorMessage,
        DataMigrationInProgress,
        PayrollRejectNOCMultipleEEBankAccounts,
        PayrollRejectNOCEEBankAccountNumber,
        PayrollRejectNOCEEBankRoutingNumber,
        PayrollRejectNOCEEBankAccountType,
        PayrollRejectNOCEEBankAccountNumberRoutingNumber,
        PayrollRejectNOCEEBankAccountNumberAccountType,
        PayrollRejectNOCEEBankRoutingNumberAccountType,
        PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType,
        PayrollWarnNOCEEBankAccountName,
        PayrollWarnNOCNoChangesDetected,
        PayrollErrorNOCNoChangesDetected,
        BalanceFileReceivedForOnHoldClient,
        BalanceFileRejectUnresolvedBankReturns,
        ErrorUncompressingHTTPRequest,
        DuplicatePayrollRunId,
        SaxParsingError,
        AssistedProcessingDataError,
        EngineeringHold,
        BalanceFileRejectPendingSetup,
        InvalidEINError,
        InvalidSubscriptionNumberError,
        DuplicatePaycheckUniqueId,
        PayrollRejectBankReturnMultipleEEBankAccounts,
        PayrollRejectBankReturnEEBankAccount,
        InvalidEmployeeName,
        TransmissionsDisabled,
        MaxLimitExceededError,
        PayrollRejectFFCRAOutdatedItems,
        MTLComplianceHold
    }

    private static SpcfLogger logger = SpcfLogManager.getLogger(ErrorMessages.class);

    public static final String DEFAULT_BUNDLE_NAME = "resources/qbdtMessageDefinitions";

    /**
     * all messages are defined in a single file that must be placed on the classpath
     */
    private static ResourceBundle messageDefinitions;

    /**
     * Load the error message property file.
     *
     * @param bundleName
     */
    public static void loadBundle(String bundleName) {
        try {
            messageDefinitions = ResourceBundle.getBundle(bundleName);
        } catch (Exception e) {
            String message = "Unable to load MessageDefinition bundle: " + bundleName;
            logger.fatal(message, e);
            throw new RuntimeException(message, e);
        }
    }

    static {
        loadBundle(DEFAULT_BUNDLE_NAME);
    }

    public static class BadOFXErrorMessages {
        public static final String PSID_MISSING = "Could not find PSID in OFX.";
    }

    public static String getMessageStr(String messageDescriptor, String... strList) {
        MessageFormat format = new MessageFormat(messageDefinitions.getString(messageDescriptor + ".messageString"));
        StringBuilder rtnStr = new StringBuilder(format.format(strList));
        rtnStr.append("\n[Message Code ");
        rtnStr.append(getMessageCode(messageDescriptor));
        rtnStr.append("]");
        return rtnStr.toString();
    }

    public static String getErrorCodeStr(String messageDescriptor) {
        return messageDefinitions.getString(messageDescriptor + ".messageErrorCode");
    }

    private static String getTransmissionErrorString(String messageDescriptor, String... strList) {
        MessageFormat format = new MessageFormat(messageDefinitions.getString(messageDescriptor + ".transmissionErrorMessageString"));
        return format.format(strList);
    }

    private static String getMessageCode(String messageDescriptor) {
        return messageDefinitions.getString(messageDescriptor + ".messageCode");
    }

    public static ErrorMessage MaxPinRetryError() {
        return new ErrorMessage(ErrorEnum.MaxPinRetryError, getErrorCodeStr("MaxPinRetryError"), getMessageStr("MaxPinRetryError"), getTransmissionErrorString("MaxPinRetryError"), getMessageCode("MaxPinRetryError"));
    }

    public static ErrorMessage AuthenticationFailedError() {
        return new ErrorMessage(ErrorEnum.AuthenticationFailedError, getErrorCodeStr("AuthenticationFailedError"), getMessageStr("AuthenticationFailedError"), getTransmissionErrorString("AuthenticationFailedError"), getMessageCode("AuthenticationFailedError"));
    }

    public static ErrorMessage InvalidEINError() {
        return new ErrorMessage(ErrorEnum.InvalidEINError, getErrorCodeStr("InvalidEINError"), getMessageStr("InvalidEINError"), getTransmissionErrorString("InvalidEINError"), getMessageCode("InvalidEINError"));
    }

    public static ErrorMessage InvalidSubscriptionNumberError() {
        return new ErrorMessage(ErrorEnum.InvalidSubscriptionNumberError, getErrorCodeStr("InvalidSubscriptionNumberError"), getMessageStr("InvalidSubscriptionNumberError"), getTransmissionErrorString("InvalidSubscriptionNumberError"), getMessageCode("InvalidSubscriptionNumberError"));
    }

    public static ErrorMessage UnexpectedError(String errDesc) {
        return new ErrorMessage(ErrorEnum.UnexpectedError, getErrorCodeStr("UnexpectedError"), getMessageStr("UnexpectedError"), getTransmissionErrorString("UnexpectedError", errDesc), getMessageCode("UnexpectedError"));
    }

    public static ErrorMessage BadOFXError(String ofxErrorDescription) {
        return new ErrorMessage(ErrorEnum.BadOFXError, getErrorCodeStr("BadOFXError"), getMessageStr("BadOFXError"), getTransmissionErrorString("BadOFXError", ofxErrorDescription), getMessageCode("BadOFXError"));
    }

    public static ErrorMessage SaxParsingError(String ofxErrorDescription) {
        return new ErrorMessage(ErrorEnum.SaxParsingError, getErrorCodeStr("SaxParsingError"), getMessageStr("SaxParsingError"), getTransmissionErrorString("SaxParsingError", ofxErrorDescription), getMessageCode("SaxParsingError"));
    }

    public static ErrorMessage DDCustomerBalanceFileError() {
        return new ErrorMessage(ErrorEnum.DDCustomerBalanceFileError, getErrorCodeStr("DDCustomerBalanceFileError"), getMessageStr("DDCustomerBalanceFileError"), getTransmissionErrorString("DDCustomerBalanceFileError"), getMessageCode("DDCustomerBalanceFileError"));
    }

    public static ErrorMessage BalanceFileRejectPendingSetup() {
        return new ErrorMessage(ErrorEnum.BalanceFileRejectPendingSetup, getErrorCodeStr("BalanceFileRejectPendingSetup"), getMessageStr("BalanceFileRejectPendingSetup"), getTransmissionErrorString("BalanceFileRejectPendingSetup"), getMessageCode("BalanceFileRejectPendingSetup"));
    }


    public static ErrorMessage BankAccountNotActiveError() {
        return new ErrorMessage(ErrorEnum.BankAccountNotActiveError, getErrorCodeStr("BankAccountNotActiveError"), getMessageStr("BankAccountNotActiveError"), getTransmissionErrorString("BankAccountNotActiveError"), getMessageCode("BankAccountNotActiveError"));
    }

    public static ErrorMessage FutureDatedPayrollTooFarInfutureError() {
        return new ErrorMessage(ErrorEnum.FutureDatedPayrollTooFarInfutureError, getErrorCodeStr("FutureDatedPayrollTooFarInfutureError"), getMessageStr("FutureDatedPayrollTooFarInfutureError"), getTransmissionErrorString("FutureDatedPayrollTooFarInfutureError"), getMessageCode("FutureDatedPayrollTooFarInfutureError"));
    }

    public static ErrorMessage ClientOutOfSyncMessage(String clientToken, String serverToken) {
        return new ErrorMessage(ErrorEnum.ClientOutOfSyncMessage, getErrorCodeStr("ClientOutOfSyncMessage"), getMessageStr("ClientOutOfSyncMessage"), getTransmissionErrorString("ClientOutOfSyncMessage", serverToken, clientToken), getMessageCode("ClientOutOfSyncMessage"));
    }

    public static ErrorMessage QBFileIdChanged(String serverFileId, String clientFileId) {
        return new ErrorMessage(ErrorEnum.QBFileIdChanged, getErrorCodeStr("QBFileIdChanged"), getMessageStr("QBFileIdChanged"), getTransmissionErrorString("QBFileIdChanged", serverFileId, clientFileId), getMessageCode("QBFileIdChanged"));
    }

    public static ErrorMessage DuplicatePaycheckSubmitted() {
        return new ErrorMessage(ErrorEnum.DuplicatePaycheckSubmitted, getErrorCodeStr("DuplicatePaycheckSubmitted"), getMessageStr("DuplicatePaycheckSubmitted"), getTransmissionErrorString("DuplicatePaycheckSubmitted"), getMessageCode("DuplicatePaycheckSubmitted"));
    }

    public static ErrorMessage DuplicateIPaycheckModSubmitted(String errDesc) {
        return new ErrorMessage(ErrorEnum.DuplicateIPaycheckModSubmitted, getErrorCodeStr("DuplicateIPaycheckModSubmitted"), getMessageStr("DuplicateIPaycheckModSubmitted"), getTransmissionErrorString("DuplicateIPaycheckModSubmitted", errDesc), getMessageCode("DuplicateIPaycheckModSubmitted"));
    }

    public static ErrorMessage QBVersionSunsetted(String appVersion, String quickBooksVersionString, String firstDate, String secondDate) {
        return new ErrorMessage(ErrorEnum.QBVersionSunsetted, getErrorCodeStr("QBVersionSunsetted"), getMessageStr("QBVersionSunsetted", quickBooksVersionString, firstDate, secondDate), getTransmissionErrorString("QBVersionSunsetted", appVersion), getMessageCode("QBVersionSunsetted"));
    }

    public static ErrorMessage QBReleaseWithChecksAfterSunset(String appVersion, String quickBooksVersionString, String firstDate, String secondDate) {
        return new ErrorMessage(ErrorEnum.QBReleaseWithChecksAfterSunset, getErrorCodeStr("QBReleaseWithChecksAfterSunset"), getMessageStr("QBReleaseWithChecksAfterSunset", quickBooksVersionString, firstDate, secondDate), getTransmissionErrorString("QBReleaseWithChecksAfterSunset", appVersion), getMessageCode("QBReleaseWithChecksAfterSunset"));
    }

    public static ErrorMessage QBReleaseNotSupported(String appVersion) {
        return new ErrorMessage(ErrorEnum.QBReleaseNotSupported, getErrorCodeStr("QBReleaseNotSupported"), getMessageStr("QBReleaseNotSupported"), getTransmissionErrorString("QBReleaseNotSupported", appVersion), getMessageCode("QBReleaseNotSupported"));
    }

    public static ErrorMessage QBTaxTableNotSupported(String taxTableVersion) {
        return new ErrorMessage(ErrorEnum.QBTaxTableNotSupported, getErrorCodeStr("QBTaxTableNotSupported"), getMessageStr("QBTaxTableNotSupported"), getTransmissionErrorString("QBTaxTableNotSupported", taxTableVersion), getMessageCode("QBTaxTableNotSupported"));
    }

    public static ErrorMessage PayrollRejectACHReturnR01ThruR09() {
        return new ErrorMessage(ErrorEnum.PayrollRejectACHReturnR01ThruR09, getErrorCodeStr("PayrollRejectACHReturnR01ThruR09"), getMessageStr("PayrollRejectACHReturnR01ThruR09"), getTransmissionErrorString("PayrollRejectACHReturnR01ThruR09"), getMessageCode("PayrollRejectACHReturnR01ThruR09"));
    }

    public static ErrorMessage PayrollRejectNOC(String employeeName) {
        String errMsg = getMessageStr("PayrollRejectNOC", employeeName, getMessageCode("());"));
        return new ErrorMessage(ErrorEnum.PayrollRejectNOC, getErrorCodeStr("PayrollRejectNOC"), errMsg, getTransmissionErrorString("PayrollRejectNOC", employeeName), getMessageCode("PayrollRejectNOC"));
    }

    public static ErrorMessage PayrollRejectACHReturnNonR01ThruR09() {
        return new ErrorMessage(ErrorEnum.PayrollRejectACHReturnNonR01ThruR09, getErrorCodeStr("PayrollRejectACHReturnNonR01ThruR09"), getMessageStr("PayrollRejectACHReturnNonR01ThruR09"), getTransmissionErrorString("PayrollRejectACHReturnNonR01ThruR09"), getMessageCode("PayrollRejectACHReturnNonR01ThruR09"));
    }

    public static ErrorMessage PayrollRejectRiskAssessment() {
        return new ErrorMessage(ErrorEnum.PayrollRejectRiskAssessment, getErrorCodeStr("PayrollRejectRiskAssessment"), getMessageStr("PayrollRejectRiskAssessment"), getTransmissionErrorString("PayrollRejectRiskAssessment"), getMessageCode("PayrollRejectRiskAssessment"));
    }

    public static ErrorMessage PayrollRejectIntuitCollections() {
        return new ErrorMessage(ErrorEnum.PayrollRejectIntuitCollections, getErrorCodeStr("PayrollRejectIntuitCollections"), getMessageStr("PayrollRejectIntuitCollections"), getTransmissionErrorString("PayrollRejectIntuitCollections"), getMessageCode("PayrollRejectIntuitCollections"));
    }

    public static ErrorMessage PayrollRejectRiskCollections() {
        return new ErrorMessage(ErrorEnum.PayrollRejectRiskCollections, getErrorCodeStr("PayrollRejectRiskCollections"), getMessageStr("PayrollRejectRiskCollections"), getTransmissionErrorString("PayrollRejectRiskCollections"), getMessageCode("PayrollRejectRiskCollections"));
    }

    public static ErrorMessage PayrollRejectFraud() {
        return new ErrorMessage(ErrorEnum.PayrollRejectFraud, getErrorCodeStr("PayrollRejectFraud"), getMessageStr("PayrollRejectFraud"), getTransmissionErrorString("PayrollRejectFraud"), getMessageCode("PayrollRejectFraud"));
    }

    public static ErrorMessage MTLComplianceHold() {
        return new ErrorMessage(ErrorEnum.MTLComplianceHold, getErrorCodeStr("MTLComplianceHold"), getMessageStr("MTLComplianceHold"), getTransmissionErrorString("MTLComplianceHold"), getMessageCode("MTLComplianceHold"));
    }

    public static ErrorMessage PayrollRejectFraudReview() {
        return new ErrorMessage(ErrorEnum.PayrollRejectFraudReview, getErrorCodeStr("PayrollRejectFraudReview"), getMessageStr("PayrollRejectFraudReview"), getTransmissionErrorString("PayrollRejectFraudReview"), getMessageCode("PayrollRejectFraudReview"));
    }

    public static ErrorMessage PayrollRejectSuspendedDD() {
        return new ErrorMessage(ErrorEnum.PayrollRejectSuspendedDD, getErrorCodeStr("PayrollRejectSuspendedDD"), getMessageStr("PayrollRejectSuspendedDD"), getTransmissionErrorString("PayrollRejectSuspendedDD"), getMessageCode("PayrollRejectSuspendedDD"));
    }

    public static ErrorMessage PayrollRejectPendingTermination() {
        return new ErrorMessage(ErrorEnum.PayrollRejectPendingTermination, getErrorCodeStr("PayrollRejectPendingTermination"), getMessageStr("PayrollRejectPendingTermination"), getTransmissionErrorString("PayrollRejectPendingTermination"), getMessageCode("PayrollRejectPendingTermination"));
    }

    public static ErrorMessage PayrollRejectMissingPaperwork() {
        return new ErrorMessage(ErrorEnum.PayrollRejectMissingPaperwork, getErrorCodeStr("PayrollRejectMissingPaperwork"), getMessageStr("PayrollRejectMissingPaperwork"), getTransmissionErrorString("PayrollRejectMissingPaperwork"), getMessageCode("PayrollRejectMissingPaperwork"));
    }

    public static ErrorMessage PayrollRejectAuditCorrections() {
        return new ErrorMessage(ErrorEnum.PayrollRejectAuditCorrections, getErrorCodeStr("PayrollRejectAuditCorrections"), getMessageStr("PayrollRejectAuditCorrections"), getTransmissionErrorString("PayrollRejectAuditCorrections"), getMessageCode("PayrollRejectAuditCorrections"));
    }

    public static ErrorMessage PayrollRejectDDLimit() {
        return new ErrorMessage(ErrorEnum.PayrollRejectDDLimit, getErrorCodeStr("PayrollRejectDDLimit"), getMessageStr("PayrollRejectDDLimit"), getTransmissionErrorString("PayrollRejectDDLimit"), getMessageCode("PayrollRejectDDLimit"));
    }

    public static ErrorMessage PSPInternalErrorMessage(String internalPSPErrorCode) {
        return new ErrorMessage(ErrorEnum.PSPInternalErrorMessage, null, internalPSPErrorCode, null, null);
    }

    public static ErrorMessage DataMigrationInProgress() {
        return new ErrorMessage(ErrorEnum.DataMigrationInProgress, getErrorCodeStr("DataMigrationInProgress"), getMessageStr("DataMigrationInProgress"), getTransmissionErrorString("DataMigrationInProgress"), getMessageCode("DataMigrationInProgress"));
    }

    public static ErrorMessage PayrollRejectNOCMultipleEEBankAccounts() {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCMultipleEEBankAccounts, getErrorCodeStr("PayrollRejectNOCMultipleEEBankAccounts"), getMessageStr("PayrollRejectNOCMultipleEEBankAccounts"), getTransmissionErrorString("PayrollRejectNOCMultipleEEBankAccounts"), getMessageCode("PayrollRejectNOCMultipleEEBankAccounts"));
    }

    public static ErrorMessage PayrollRejectNOCEEBankAccountNumber(String empName, String origAccountNumber, String newAccountNumber) {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCEEBankAccountNumber, getErrorCodeStr("PayrollRejectNOCEEBankAccountNumber"), getMessageStr("PayrollRejectNOCEEBankAccountNumber", empName, origAccountNumber, newAccountNumber), getTransmissionErrorString("PayrollRejectNOCEEBankAccountNumber", empName), getMessageCode("PayrollRejectNOCEEBankAccountNumber"));
    }

    public static ErrorMessage PayrollRejectNOCEEBankRoutingNumber(String employeeName, String origRoutingNumber, String changedRoutingNumber) {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCEEBankRoutingNumber, getErrorCodeStr("PayrollRejectNOCEEBankRoutingNumber"), getMessageStr("PayrollRejectNOCEEBankRoutingNumber", employeeName, origRoutingNumber, changedRoutingNumber), getTransmissionErrorString("PayrollRejectNOCEEBankRoutingNumber", employeeName), getMessageCode("PayrollRejectNOCEEBankRoutingNumber"));
    }

    public static ErrorMessage PayrollRejectNOCEEBankAccountType(String employeeName, String origAccountType, String changedAccountType) {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCEEBankAccountType, getErrorCodeStr("PayrollRejectNOCEEBankAccountType"), getMessageStr("PayrollRejectNOCEEBankAccountType", employeeName, origAccountType, changedAccountType), getTransmissionErrorString("PayrollRejectNOCEEBankAccountType", employeeName), getMessageCode("PayrollRejectNOCEEBankAccountType"));
    }

    public static ErrorMessage PayrollRejectNOCEEBankAccountNumberRoutingNumber(String employeeName, String origAccountNumber, String origRoutingNumber, String changedAccountNumber, String changedRoutingNumber) {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCEEBankAccountNumberRoutingNumber, getErrorCodeStr("PayrollRejectNOCEEBankAccountNumberRoutingNumber"), getMessageStr("PayrollRejectNOCEEBankAccountNumberRoutingNumber", employeeName, origAccountNumber, origRoutingNumber, changedAccountNumber, changedRoutingNumber), getTransmissionErrorString("PayrollRejectNOCEEBankAccountNumberRoutingNumber", employeeName), getMessageCode("PayrollRejectNOCEEBankAccountNumberRoutingNumber"));
    }

    public static ErrorMessage PayrollRejectNOCEEBankAccountNumberAccountType(String employeeName, String origAccountNumber, String origAccountType, String changedAccountNumber, String changedAccountType) {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCEEBankAccountNumberAccountType, getErrorCodeStr("PayrollRejectNOCEEBankAccountNumberAccountType"), getMessageStr("PayrollRejectNOCEEBankAccountNumberAccountType", employeeName, origAccountNumber, origAccountType, changedAccountNumber, changedAccountType), getTransmissionErrorString("PayrollRejectNOCEEBankAccountNumberAccountType", employeeName), getMessageCode("PayrollRejectNOCEEBankAccountNumberAccountType"));
    }

    public static ErrorMessage PayrollRejectNOCEEBankRoutingNumberAccountType(String employeeName, String origRoutingNumber, String origAccountType, String changedRoutingNumber, String changedAccountType) {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCEEBankRoutingNumberAccountType, getErrorCodeStr("PayrollRejectNOCEEBankRoutingNumberAccountType"), getMessageStr("PayrollRejectNOCEEBankRoutingNumberAccountType", employeeName, origRoutingNumber, origAccountType, changedRoutingNumber, changedAccountType), getTransmissionErrorString("PayrollRejectNOCEEBankRoutingNumberAccountType", employeeName), getMessageCode("PayrollRejectNOCEEBankRoutingNumberAccountType"));
    }

    public static ErrorMessage PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType(String employeeName, String origAccountNumber, String origRoutingNumber, String origAccountType, String changedAccountNumber, String changedRoutingNumber, String changedAccountType) {
        return new ErrorMessage(ErrorEnum.PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType, getErrorCodeStr("PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType"), getMessageStr("PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType", employeeName, origAccountNumber, origRoutingNumber, origAccountType, changedAccountNumber, changedRoutingNumber, changedAccountType), getTransmissionErrorString("PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType", employeeName), getMessageCode("PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType"));
    }

    public static ErrorMessage PayrollWarnNOCEEBankAccountName(String employeeName) {
        return new ErrorMessage(ErrorEnum.PayrollWarnNOCEEBankAccountName, getErrorCodeStr("PayrollWarnNOCEEBankAccountName"), getMessageStr("PayrollWarnNOCEEBankAccountName", employeeName), getTransmissionErrorString("PayrollWarnNOCEEBankAccountName", employeeName), getMessageCode("PayrollWarnNOCEEBankAccountName"));
    }

    public static ErrorMessage PayrollWarnNOCNoChangesDetected() {
        return new ErrorMessage(ErrorEnum.PayrollWarnNOCNoChangesDetected, getErrorCodeStr("PayrollWarnNOCNoChangesDetected"), getMessageStr("PayrollWarnNOCNoChangesDetected"), getTransmissionErrorString("PayrollWarnNOCNoChangesDetected"), getMessageCode("PayrollWarnNOCNoChangesDetected"));
    }

    public static ErrorMessage PayrollErrorNOCNoChangesDetected() {
        return new ErrorMessage(ErrorEnum.PayrollErrorNOCNoChangesDetected, getErrorCodeStr("PayrollErrorNOCNoChangesDetected"), getMessageStr("PayrollErrorNOCNoChangesDetected"), getTransmissionErrorString("PayrollErrorNOCNoChangesDetected"), getMessageCode("PayrollErrorNOCNoChangesDetected"));
    }

    public static ErrorMessage PayrollRejectBankReturnMultipleEEBankAccounts() {
        return new ErrorMessage(ErrorEnum.PayrollRejectBankReturnMultipleEEBankAccounts, getErrorCodeStr("PayrollRejectBankReturnMultipleEEBankAccounts"), getMessageStr("PayrollRejectBankReturnMultipleEEBankAccounts"), getTransmissionErrorString("PayrollRejectBankReturnMultipleEEBankAccounts"), getMessageCode("PayrollRejectBankReturnMultipleEEBankAccounts"));
    }

    public static ErrorMessage PayrollRejectBankReturnEEBankAccount(String employeeName) {
        return new ErrorMessage(ErrorEnum.PayrollRejectBankReturnEEBankAccount, getErrorCodeStr("PayrollRejectBankReturnEEBankAccount"), getMessageStr("PayrollRejectBankReturnEEBankAccount", employeeName), getTransmissionErrorString("PayrollRejectBankReturnMultipleEEBankAccounts", employeeName), getMessageCode("PayrollRejectBankReturnEEBankAccount"));
    }

    public static ErrorMessage BalanceFileReceivedForOnHoldClient(String onHoldState) {
        return new ErrorMessage(ErrorEnum.BalanceFileReceivedForOnHoldClient, getErrorCodeStr("BalanceFileReceivedForOnHoldClient"), getMessageStr("BalanceFileReceivedForOnHoldClient"), getTransmissionErrorString("BalanceFileReceivedForOnHoldClient", onHoldState), getMessageCode("BalanceFileReceivedForOnHoldClient"));
    }

    public static ErrorMessage BalanceFileRejectUnresolvedBankReturns() {
        return new ErrorMessage(ErrorEnum.BalanceFileRejectUnresolvedBankReturns, getErrorCodeStr("BalanceFileRejectUnresolvedBankReturns"), getMessageStr("BalanceFileRejectUnresolvedBankReturns"), getTransmissionErrorString("BalanceFileRejectUnresolvedBankReturns"), getMessageCode("BalanceFileRejectUnresolvedBankReturns"));
    }

    public static ErrorMessage ErrorUncompressingHTTPRequest() {
        return new ErrorMessage(ErrorEnum.ErrorUncompressingHTTPRequest, getErrorCodeStr("ErrorUncompressingHTTPRequest"), getMessageStr("ErrorUncompressingHTTPRequest"), getTransmissionErrorString("ErrorUncompressingHTTPRequest"), getMessageCode("ErrorUncompressingHTTPRequest"));
    }

    public static ErrorMessage QBReleaseToBeSunset(String quickBooksVersionString, String firstDate, String secondDate) {
        return new ErrorMessage(ErrorEnum.QBReleaseToBeSunset, getErrorCodeStr("QBReleaseToBeSunset"), getMessageStr("QBReleaseToBeSunset", quickBooksVersionString, firstDate, secondDate), getTransmissionErrorString("QBReleaseToBeSunset"), getMessageCode("QBReleaseToBeSunset"));
    }

    public static ErrorMessage InvalidEERoutingNumber(String pRouting, String pEmpName) {
        return new ErrorMessage(ErrorEnum.InvalidEERoutingNumber, getErrorCodeStr("InvalidRoutingNumber"), getMessageStr("InvalidRoutingNumber", pRouting, pEmpName), getTransmissionErrorString("InvalidRoutingNumber"), getMessageCode("InvalidRoutingNumber"));
    }

    public static ErrorMessage DuplicatePayrollRunId() {
        return new ErrorMessage(ErrorEnum.DuplicatePayrollRunId, getErrorCodeStr("DuplicatePayrollRunId"), getMessageStr("DuplicatePayrollRunId"), getTransmissionErrorString("DuplicatePayrollRunId"), getMessageCode("DuplicatePayrollRunId"));
    }

    public static ErrorMessage AssistedProcessingDataError(String pErrorDescription) {
        return new ErrorMessage(ErrorEnum.AssistedProcessingDataError, getErrorCodeStr("AssistedProcessingDataError"), getMessageStr("AssistedProcessingDataError"), getTransmissionErrorString("AssistedProcessingDataError", pErrorDescription), getMessageCode("AssistedProcessingDataError"));
    }

    public static ErrorMessage EngineeringHold(String pErrorDescription) {
        return new ErrorMessage(ErrorEnum.EngineeringHold, getErrorCodeStr("EngineeringHold"), getMessageStr("EngineeringHold"), getTransmissionErrorString("EngineeringHold", pErrorDescription), getMessageCode("EngineeringHold"));
    }

    public static ErrorMessage DuplicatePaycheckUniqueId() {
        return new ErrorMessage(ErrorEnum.DuplicatePaycheckUniqueId, getErrorCodeStr("DuplicatePaycheckUniqueId"), getMessageStr("DuplicatePaycheckUniqueId"), getTransmissionErrorString("DuplicatePaycheckUniqueId"), getMessageCode("DuplicatePaycheckUniqueId"));
    }

    public static ErrorMessage InvalidEmployeeName() {
        return new ErrorMessage(ErrorEnum.InvalidEmployeeName, getErrorCodeStr("InvalidEmployeeName"), getMessageStr("InvalidEmployeeName"), getTransmissionErrorString("InvalidEmployeeName"), getMessageCode("InvalidEmployeeName"));
    }

    public static ErrorMessage TransmissionsDisabled() {
        return new ErrorMessage(ErrorEnum.TransmissionsDisabled, getErrorCodeStr("TransmissionsDisabled"), getMessageStr("TransmissionsDisabled"), getTransmissionErrorString("TransmissionsDisabled"), getMessageCode("TransmissionsDisabled"));
    }

    public static ErrorMessage MaxLimitExceededError(String pErrorDescription) {
        return new ErrorMessage(ErrorEnum.MaxLimitExceededError, getErrorCodeStr("MaxLimitExceededError"), getMessageStr("MaxLimitExceededError"), getTransmissionErrorString("MaxLimitExceededError", pErrorDescription), getMessageCode("MaxLimitExceededError"));
    }
    public static ErrorMessage PayrollRejectFFCRAOutdatedItems() {
        return new ErrorMessage(ErrorEnum.PayrollRejectFFCRAOutdatedItems,
                getErrorCodeStr("PayrollRejectFFCRAOutdatedItems"), getMessageStr("PayrollRejectFFCRAOutdatedItems"), getTransmissionErrorString("PayrollRejectFFCRAOutdatedItems"), getMessageCode("PayrollRejectFFCRAOutdatedItems"));
    }
}
