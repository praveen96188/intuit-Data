package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.xml.sax.SAXParseException;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * 27-Jan-2008 10:27:27
 */
public class QBDTRequestProcessor {

    private static SpcfLogger logger = PayrollServices.getLogger(QBDTRequestProcessor.class);

    public static final String ESCAPED_PASSWORD_STR = "*****";
    private static final String ACCT_TYPE_ENUM_ERROR = "is not facet-valid with respect to enumeration '[UNKNOWN, CHECKING, SAVINGS]'";
    private static final String ACCT_TYPE_NULL_ERROR = "One of '{ACCTTYPE}' is expected";
    private static final String ACCT_ID_MIN_ERROR = "is not facet-valid with respect to minLength '1' for type '#AnonType_ACCTID";
    private static final String ACCT_ID_NULL_ERROR = "One of '{ACCTID}' is expected";
    private static final String BANK_ID_MIN_ERROR = "is not facet-valid with respect to minLength '1' for type '#AnonType_BANKID";
    private static final String BANK_ID_NULL_ERROR = "One of '{BANKID}' is expected";
    private static final String INVALID_BANK_ACCOUNT = "Error: Invalid bank account found";

    private String mSrcTransmissionId;
    private SourceSystemTransmissionDTO mSrcTransmissionDTO = null;
    private TransmissionType mTransmissionType = null;
    private String mCompanyPSID = null;
    private OFXAPPVERObject mAppVerion = null;
    private PSPRequestContextManager pspRequestContextManager;

    private AssistedRequestProcessor mAssistedRequestProcessor;
    public static final Pattern USER_ID_PATTERN = Pattern.compile("^\\s*<USERID>(.*)$", Pattern.MULTILINE | Pattern.UNIX_LINES);

    /**
     * QBDTRequestProcessor
     */
    public QBDTRequestProcessor() {
        mAssistedRequestProcessor = new AssistedRequestProcessor();
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * @param pISocketManager - Socket Manager
     */
    public QBDTRequestProcessor(ISocketManager pISocketManager) {
        mAssistedRequestProcessor = new AssistedRequestProcessor(pISocketManager);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * Process the OFX string passed in from QB.
     *
     * @param requestStr - OFX request string.
     * @param userId     - psid
     * @return - OFX response string.
     */
    public String processRequest(String requestStr, String userId, String clientIP) {
        try {
            String responseStr;

            try {
                if (userId == null) {
                    QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();
                    qbdtProcessResult.setMessage(ErrorMessages.BadOFXError(ErrorMessages.BadOFXErrorMessages.PSID_MISSING));
                    return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, qbdtProcessResult, clientIP);
                }

                mCompanyPSID = userId;
                responseStr = handleRequest(requestStr, clientIP);
            } catch (Throwable t) {
                logger.error("Error for company PSID " + mCompanyPSID + ".", t);
                responseStr = ProcessingErrorHandler.getUnrecoverableProcessingErrorString();
            }

            if (responseStr == null) {
                QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();
                qbdtProcessResult.setMessage(ErrorMessages.UnexpectedError("Response missing"));
                return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, qbdtProcessResult, clientIP);
            }

            return responseStr;
        }finally {
            pspRequestContextManager.clearRequestContext();
        }
    }

    /**
     * Process the OFX specified for the company passed in.
     *
     * @param requestStr - OFX Request String
     * @return OFX Response String
     * @throws Throwable any unhandled exception
     */
    private String handleRequest(String requestStr, String clientIP) throws Throwable {
        logger.info("Request for PSID=" + mCompanyPSID + " being handled.");

        String responseStr = null;

        Company company;
        try {
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(mCompanyPSID, SourceSystemCode.QBDT);
            PayrollServices.rollbackUnitOfWork();
            if (company == null) {
                // creates its own UOW
                return createTermResponse(clientIP);
            }
            mAssistedRequestProcessor.setPSID(mCompanyPSID);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        try {

            pspRequestContextManager.clearRequestContext();
            pspRequestContextManager.setRequestContext(company, RequestType.OFX,"QBDT");
        } catch (Exception e) {
            logger.error("Event=SetRequestContext Type=OFX Status=Error", e);
        }

        OFX requestOfx;
        try {
            requestOfx = OFXManager.ofxRequestToJava(requestStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            mAppVerion = new OFXAPPVERObject(requestOfx.getSIGNONMSGSRQV1().getSONRQ().getAPPVER());
        } catch (Exception e) {
            return handleExceptionBeforeOFXLoaded(requestStr, clientIP, e);
        }

        // creates its own UOW
        QBDTProcessResult<SourceSystemTransmission> sourceSystemTransmissionProcessResult = initializeTransmissionSecondary(requestStr, requestOfx, clientIP);
        if (!sourceSystemTransmissionProcessResult.isSuccess()) {
            return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, sourceSystemTransmissionProcessResult, clientIP);
        }
        mAssistedRequestProcessor.setSourceSystemTransmissionDTO(mSrcTransmissionDTO);
        mAssistedRequestProcessor.setSourceTransmissionId(mSrcTransmissionId);
        mAssistedRequestProcessor.setAppVerion(mAppVerion);

        if (QBOFX.tokenVal(requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN()) == 0) {
            // if the token is zero or blank the file must be new, record the QBFileID
            try {
                PayrollServices.beginUnitOfWork();
                company = Company.findCompany(mCompanyPSID, SourceSystemCode.QBDT);
                CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

                if (sourceSystemTransmissionProcessResult.getResult().getType().in(TransmissionType.Sync, TransmissionType.UsageSync)
                        && mAppVerion.isExplicitWatermarkRequired()) {
                    companyDTO.getQuickBooksInfo().setWatermarkDate(company.getWatermarkDate());
                }
                companyDTO.getQuickBooksInfo().setFileId(QBOFX.getQBFileId(requestOfx.getSIGNONMSGSRQV1().getSONRQ().getIQBFILEID()));
                ProcessResult fileIdUpdate = PayrollServices.companyManager.updateQBCompanyInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
                if (fileIdUpdate.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                } else {
                    PayrollServices.rollbackUnitOfWork();
                    QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();
                    qbdtProcessResult.setMessage(ErrorMessages.UnexpectedError("Failed to update file id on company."));
                    return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, qbdtProcessResult, clientIP, false);
                }
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }

        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemTransmissionProcessResult.getResult().getType().in(TransmissionType.Sync, TransmissionType.UsageSync)) {
                company = Company.findCompany(mCompanyPSID, SourceSystemCode.QBDT);
                long syncToken = QBOFX.tokenVal(requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN());
                if (syncToken < company.getCurrentToken()) {
                    enforceServiceStartTokenBoundary(company, requestOfx, syncToken);
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }


        QBDTProcessResult<SIGNONMSGSRSV1> signOnPR;
        try {
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(mCompanyPSID, SourceSystemCode.QBDT);
            // validate sign on
            signOnPR = validateSignOn(company, requestOfx, mSrcTransmissionId);
            // commit so that the invalid sign on counter is saved
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        if (!signOnPR.isSuccess()) {
            // this creates its own UOW
            return createInvalidSignOnResponse(signOnPR, clientIP);
        }

        CredentialType credentialType = signOnPR.getCredentialType();

        // validate token: token cannot be greater than the current company token, if the token is less the request must be a sync request
        QBDTProcessResult inSyncResult = validateRequestInSync(company, requestOfx);
        if (!inSyncResult.isSuccess()) {
            return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, inSyncResult, clientIP);
        }

        // reject invalid account information
        if (QBOFX.hasInvalidBankAccount(requestStr)) {
            return handleExceptionBeforeOFXLoaded(requestStr, clientIP, new SAXParseException(INVALID_BANK_ACCOUNT, null));
        }

        // validate company has active service
        try {
            PayrollServices.beginUnitOfWork();
            QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();
            company = Company.findCompany(mCompanyPSID, SourceSystemCode.QBDT);
            if (mTransmissionType == TransmissionType.BalanceFile) {
                if (!company.isCompanyOnService(ServiceCode.Tax)) {
                    // dd submitted balance file
                    qbdtProcessResult.setMessage(ErrorMessages.DDCustomerBalanceFileError());
                } else if (company.getCompanyService(ServiceCode.Tax) != null && company.getService(ServiceCode.Tax).getStatusCd() == ServiceSubStatusCode.PendingSetup) {
                    // balance file not allowed while in pending setup status
                    qbdtProcessResult.setMessage(ErrorMessages.BalanceFileRejectPendingSetup());
                } else if (company.getOnHoldReasonCollection().find(OnHoldReason.ExpirationDate().isNull()).isNotEmpty()) {
                    // balance file while on hold
                    DomainEntitySet<OnHoldReason> onHoldReasons = company.getOnHoldReasonCollection().find(OnHoldReason.ExpirationDate().isNull());
                    qbdtProcessResult.setMessage(ErrorMessages.BalanceFileReceivedForOnHoldClient(onHoldReasons.get(0).getOnHoldReasonCd().toString()));
                } else {
                    DomainEntitySet<TransactionReturn> transactionReturnCollection =
                            TransactionReturn.findTransactionReturnsExcludedStatus(company, TransactionReturnStatusCode.Resolved);
                    if (!transactionReturnCollection.isEmpty() && !LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERReturnReceivable).equals(SpcfMoney.ZERO)) {
                        qbdtProcessResult.setMessage(ErrorMessages.BalanceFileRejectUnresolvedBankReturns());
                    }
                }
            } else if (mTransmissionType == TransmissionType.PayrollSubmission) {
                CompanyService taxService = company.getCompanyService(ServiceCode.Tax);
                CompanyService ddService = company.getCompanyService(ServiceCode.DirectDeposit);

                if ((taxService != null && taxService.getStatusCd().in(ServiceSubStatusCode.PendingBalanceFile, ServiceSubStatusCode.PendingSetup)) && (ddService != null && ddService.isPending())) {
                    // balance file not allowed while in pending setup status
                    qbdtProcessResult.setMessage(ErrorMessages.BalanceFileRejectPendingSetup());
                } else if (company.isCompanyOnActiveService(ServiceCode.Tax) && isCompanyOnDDLimitHold(company)) {
                    qbdtProcessResult.setMessage(ErrorMessages.PayrollRejectDDLimit());
                }
            }
            PayrollServices.rollbackUnitOfWork();

            if (!qbdtProcessResult.isSuccess()) {
                // creates its own UOW
                responseStr = QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, qbdtProcessResult, clientIP);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        // process the request
        if (responseStr == null) {
            responseStr = mAssistedRequestProcessor.processAssistedRequest(requestStr, requestOfx, mTransmissionType, credentialType, clientIP);
        }

        logger.info("Request for PSID=" + mCompanyPSID + " is done.");
        return responseStr;
    }

    private Boolean isCompanyOnDDLimitHold(Company pCompany) {
        DomainEntitySet<OnHoldReason> companyOnHoldReasonCollection = pCompany.getOnHoldReasonCollection();
        if (companyOnHoldReasonCollection.find(OnHoldReason.ExpirationDate().isNull().And(OnHoldReason.OnHoldReasonCd().in(ServiceSubStatusCode.DirectDepositLimit))).isNotEmpty()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private QBDTProcessResult validateRequestInSync(Company pCompany, OFX pRequestOfx) {
        QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();

        long ofxToken = QBOFX.tokenVal(pRequestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN());
        long serverToken = pCompany.getCurrentToken();

        if ((ofxToken < serverToken && TransmissionType.Sync != mTransmissionType && TransmissionType.UsageSync != mTransmissionType) || ofxToken > serverToken) {
            ErrorMessage errorMessage = ErrorMessages.ClientOutOfSyncMessage(Long.toString(ofxToken), Long.toString(serverToken));
            qbdtProcessResult.setMessage(errorMessage);
            QBDTCompanyEventDTO eventDTO = new QBDTCompanyEventDTO();
            eventDTO.setCompany(pCompany);
            eventDTO.setEventTypeCode(EventTypeCode.PayrollRejected);
            eventDTO.setTransmissionId(mSrcTransmissionId);
            eventDTO.setErrMsg(errorMessage.getErrorDescription());
            qbdtProcessResult.addCompanyEvent(eventDTO);
        }
        return qbdtProcessResult;
    }

    private String createTermResponse(String clientIP) {
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX termResponse = new com.intuit.sbd.payroll.psp.common.ofx.response.OFX();

        SIGNONMSGSRSV1 signonmsgsrsv1 = new SIGNONMSGSRSV1();
        SONRS sonrs = new SONRS();
        STATUS status = new STATUS();
        status.setCODE(QBOFX.SUCCESS_STATUS_CODE);
        status.setSEVERITY(QBOFX.MESSAGE_SEVERITY.INFO);
        sonrs.setSTATUS(status);
        sonrs.setDTSERVER(QBOFX.getOFXServerDTTM(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())));
        sonrs.setLANGUAGE(QBOFX.LANGUAGE);
        signonmsgsrsv1.setSONRS(sonrs);
        termResponse.setSIGNONMSGSRSV1(signonmsgsrsv1);

        Company company = Company.findCompany(mCompanyPSID, SourceSystemCode.QBDT);

        IPAYROLLMSGSRSV1 ipayrollmsgsrsv1 = new IPAYROLLMSGSRSV1();
        IPAYROLLUPDATERS ipayrollupdaters = new IPAYROLLUPDATERS();
        if (company != null) {
            ipayrollupdaters.setTOKEN(company.getCurrentToken() + "");
            ipayrollupdaters.setIEMPNEXTID(company.getNextEmployeeId());
            ipayrollupdaters.setIPAYCHKNEXTID(company.getNextPaycheckId());
            ipayrollupdaters.setIPAYROLLTXNEXTID(company.getNextPayrollTransactionId());
            ipayrollupdaters.setIPITEMNEXTID(company.getNextPayrollItemId());
            mAssistedRequestProcessor.addServiceStatus(ipayrollupdaters, company);
        } else {
            ipayrollupdaters.setTOKEN("1");
            ipayrollupdaters.setIPAYROLLTXNEXTID("1");
            ipayrollupdaters.setIPAYCHKNEXTID("1");
            ipayrollupdaters.setIEMPNEXTID("1");
            ipayrollupdaters.setIPITEMNEXTID("1");

            // dd
            IDDSTATUS iddstatus = new IDDSTATUS();
            iddstatus.setIDDMODE(QBOFX.DD_MODES.TERMINATED);
            ipayrollupdaters.setIDDSTATUS(iddstatus);

            // tax
            ITAXSERVSTATUS itaxservstatus = new ITAXSERVSTATUS();
            itaxservstatus.setITAXSERVMODE(QBOFX.TAX_MODES.TERMINATED);
            ipayrollupdaters.setITAXSERVSTATUS(itaxservstatus);
        }

        ipayrollmsgsrsv1.setIPAYROLLUPDATERS(ipayrollupdaters);
        termResponse.setIPAYROLLMSGSRSV1(ipayrollmsgsrsv1);

        String response = OFXManager.javaResponseToOFX(termResponse, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        if (company != null) {
            QBDTRequestProcessorHelper.finalizeTransmission(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, response, "Term Response", null, clientIP);
        }

        return response;
    }

    private void enforceServiceStartTokenBoundary(Company pCompany, OFX pRequestOfx, long pSyncToken) {// validate that sync token >= service started token
        long startingToken = pCompany.getServiceStartToken();
        if (pSyncToken < startingToken) {
            pRequestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(Long.toString(startingToken));
            logger.info(pCompany + " - using OFXServiceActivated event to convert token from: " + pSyncToken + " to: " + startingToken);
        }
    }

    private QBDTProcessResult<SIGNONMSGSRSV1> validateSignOn(Company pCompany, OFX pRequestOfx, String pTransmissionId) {
        SignOnProcessor signOnProcessor = new SignOnProcessor(pTransmissionId, pCompany);
        return signOnProcessor.processSignOnRequest(pRequestOfx.getSIGNONMSGSRQV1(), pRequestOfx);
    }

    /**
     * Return the PSID from the OFX string passed in.  If not found then a null value is returned.
     *
     * @param requestStr - OFX request string.
     * @return - QBDTProcessResult true and contains result if found.
     * QBDTProcessResult false an error message if not found.
     */
    public static QBDTProcessResult<String> retrieveCompanyPSIDFromRequestString(String requestStr) {
        QBDTProcessResult<String> processResult = new QBDTProcessResult<String>();

        // PSRV001827
        if (requestStr == null) {
            processResult.setMessage(ErrorMessages.BadOFXError(ErrorMessages.BadOFXErrorMessages.PSID_MISSING));
            logger.warn("OFX request is null.", null, "OFX Missing PSID");
        } else {
            Matcher m = USER_ID_PATTERN.matcher(requestStr);

            if (m.find()) {
                processResult.setResult(m.group(1).trim());
            } else {
                processResult.setMessage(ErrorMessages.BadOFXError(ErrorMessages.BadOFXErrorMessages.PSID_MISSING));
                logger.warn("OFX Missing PSID: " + requestStr, null, "OFX Missing PSID");
            }
        }

        return processResult;
    }

    public static QBDTProcessResult<String> retrieveStringFromRequestString(String requestStr, String searchStr) {
        QBDTProcessResult<String> processResult = new QBDTProcessResult<String>();

        if (requestStr == null) {
            processResult.setMessage(ErrorMessages.BadOFXError(ErrorMessages.BadOFXErrorMessages.PSID_MISSING));
            logger.warn("OFX request is null.", null, "OFX Missing PSID");
        } else {
            Pattern p = Pattern.compile("^\\s*" + searchStr + "(.*)$", Pattern.MULTILINE | Pattern.UNIX_LINES);
            Matcher m = p.matcher(requestStr);

            if (m.find()) {
                processResult.setResult(m.group(1).trim());
            } else {
                processResult.setMessage(ErrorMessages.BadOFXError(ErrorMessages.BadOFXErrorMessages.PSID_MISSING));
                logger.warn("OFX Missing PSID: " + requestStr, null, "OFX Missing PSID");
            }
        }

        return processResult;
    }

    private String createInvalidSignOnResponse(QBDTProcessResult<SIGNONMSGSRSV1> pSignOnPR, String clientIP) {
        ErrorMessage errMessage = pSignOnPR.getMessage();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX rtnOFX = ProcessingErrorHandler.handleSignOnError(errMessage);
        String resposneOFXStr = OFXManager.javaToOFX(rtnOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        try {
            QBDTRequestProcessorHelper.finalizeTransmissionError(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, resposneOFXStr, errMessage, pSignOnPR.getCompanyEventList(), clientIP);
        } catch (Throwable t) {
            // Log error and return response str.
        }
        return resposneOFXStr;
    }


    /**
     * Handle an exception that has occurred before the OFX is successfully loaded
     * into JAXB objects.
     *
     * @param requestOFXStr - Request OFX String
     * @return - OFX Response String
     */
    private String handleExceptionBeforeOFXLoaded(String requestOFXStr, String clientIP, Exception e) {
        ErrorMessage errMsg;
        if (e.getMessage().contains(ACCT_TYPE_ENUM_ERROR) ||
                e.getMessage().contains(ACCT_TYPE_NULL_ERROR) ||
                e.getMessage().contains(ACCT_ID_MIN_ERROR) ||
                e.getMessage().contains(ACCT_ID_NULL_ERROR) ||
                e.getMessage().contains(BANK_ID_MIN_ERROR) ||
                e.getMessage().contains(BANK_ID_NULL_ERROR) ||
                e.getMessage().contains(INVALID_BANK_ACCOUNT)) {
            errMsg = ErrorMessages.SaxParsingError(e.getMessage());
        } else {
            errMsg = ErrorMessages.BadOFXError(e.getMessage());
        }

        logger.error("Error for company PSID " + mCompanyPSID + ".  Error: " + e.toString(), null, ProcessingErrorHandler.SPCFErrorCodes.OFXError);

        requestOFXStr = stripOutPassword(requestOFXStr);

        if (mSrcTransmissionId == null) {
            mSrcTransmissionId = SpcfUniqueId.createInstance(true).toString();
        }
        if (mSrcTransmissionDTO == null) {
            mSrcTransmissionDTO = new SourceSystemTransmissionDTO();
            mSrcTransmissionDTO.setRequestDocument(requestOFXStr);
            mSrcTransmissionDTO.setRequestToken(0L);
            mSrcTransmissionDTO.setTransmissionType(com.intuit.sbd.payroll.psp.domain.TransmissionType.Unknown);
            mSrcTransmissionDTO.setFromSourceSystem(SourceSystemCode.QBDT);
            mSrcTransmissionDTO.setIPAddress(clientIP);
            PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT, mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO);
        }

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX rtnOFX;

        rtnOFX = ProcessingErrorHandler.handleOfxParsingError(errMsg, requestOFXStr, mSrcTransmissionDTO.getTransmissionType());
        if (rtnOFX != null && rtnOFX.getIPAYROLLMSGSRSV1() != null && rtnOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS() != null) {
            try {
                mSrcTransmissionDTO.setResponseToken(QBOFX.tokenVal(rtnOFX.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getTOKEN()));
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        String responseStr = OFXManager.javaResponseToOFX(rtnOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        try {
            QBDTRequestProcessorHelper.finalizeTransmissionError(mCompanyPSID, mSrcTransmissionId, mSrcTransmissionDTO, responseStr, errMsg, null, clientIP);
        } catch (Throwable t) {
            // Log error and return response str.
        }
        return responseStr;
    }

    /**
     * Escape password in ofx string.
     *
     * @param requestOFX - Request OFX String
     * @return - Request OFX String with password stripped.
     */
    static String stripOutPassword(String requestOFX) {
        Pattern p1 = Pattern.compile("((^\\s*<USERPASS>)(.*?)($))", Pattern.MULTILINE | Pattern.UNIX_LINES);
        Matcher m1 = p1.matcher(requestOFX);
        StringBuffer sb1 = new StringBuffer();
        while (m1.find()) {
            m1.appendReplacement(sb1, "$2" + ESCAPED_PASSWORD_STR + "$4");
        }
        m1.appendTail(sb1);
        return sb1.toString();
    }


    /**
     * Store the OFX request tranmission piece.
     *
     * @param requestStr - Request OFX String
     * @param requestOfx - Request OFX JAXB Object
     * @return QBDTProcessResult
     */
    private QBDTProcessResult<SourceSystemTransmission> initializeTransmissionSecondary(String requestStr, OFX requestOfx, String clientIP) {
        QBDTProcessResult<SourceSystemTransmission> processResult = new QBDTProcessResult<SourceSystemTransmission>();
        requestStr = stripOutPassword(requestStr);

        mSrcTransmissionId = SpcfUniqueId.createInstance(true).toString();
        mSrcTransmissionDTO = new SourceSystemTransmissionDTO();
        if (requestOfx.getIPAYROLLMSGSRQV1() != null && requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ() != null &&
                requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ() != null) {
            mSrcTransmissionDTO.setTRNUID(requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID());
        }

        if (requestOfx.getSIGNONMSGSRQV1() != null && requestOfx.getSIGNONMSGSRQV1().getSONRQ() != null) {
            OFXAPPVERObject ofxAPPVERObject = new OFXAPPVERObject(requestOfx.getSIGNONMSGSRQV1().getSONRQ().getAPPVER());
            mSrcTransmissionDTO.setApplicationVersion(ofxAPPVERObject.getQBVersionStr());
            mSrcTransmissionDTO.setTaxTableId(ofxAPPVERObject.getTaxTableId());
            mSrcTransmissionDTO.setApplicationId(ofxAPPVERObject.getFlavorId());
        }

        mSrcTransmissionDTO.setRequestDocument(requestStr);
        mSrcTransmissionDTO.setIPAddress(clientIP);
        mSrcTransmissionDTO.setFromSourceSystem(SourceSystemCode.QBDT);

        String token;
        String rejectIfMissing = null;
        if (requestOfx != null) {
            if (requestOfx.getIPAYROLLMSGSRQV1() != null && requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ() != null) {
                rejectIfMissing = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getREJECTIFMISSING();
            }

            if (rejectIfMissing == null || rejectIfMissing.length() == 0) {
                processResult.setMessage(ErrorMessages.BadOFXError("OFX REJECTIFMISSING missing."));
                logger.error("Error for company PSID " + mCompanyPSID + ". OFX Missing REJECTIFMISSING: " + requestStr, null, "OFX Missing REJECTIFMISSING");
                return processResult;
            }

            // PSRV004023: introduce new types for Symphony silent sync
            boolean pinProvided = false;
            if (requestOfx.getSIGNONMSGSRQV1() != null && requestOfx.getSIGNONMSGSRQV1().getSONRQ() != null) {
                String pin = requestOfx.getSIGNONMSGSRQV1().getSONRQ().getUSERPASS();
                if (pin != null && !pin.equals("")) {
                    pinProvided = true;
                }
            }

            if (rejectIfMissing.compareToIgnoreCase("Y") == 0) {
                if (pinProvided) {
                    mTransmissionType = TransmissionType.PayrollSubmission;
                } else {
                    mTransmissionType = TransmissionType.UsageSend;
                }
                String ofxTransmissionId = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID();
                if (ofxTransmissionId == null || ofxTransmissionId.length() == 0) {
                    processResult.setMessage(ErrorMessages.BadOFXError("OFX TRNUID missing."));
                    logger.error("Error for company PSID " + mCompanyPSID + ". OFX Missing TRNUID: " + requestStr, null, "OFX Missing TRNUID");
                    return processResult;
                }
            } else {
                if (pinProvided) {
                    mTransmissionType = TransmissionType.Sync;
                } else {
                    mTransmissionType = TransmissionType.UsageSync;
                }
            }

            token = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN();

            if (token == null) {
                processResult.setMessage(ErrorMessages.BadOFXError("OFX TOKEN missing."));
                logger.error("Error for company PSID " + mCompanyPSID + ". OFX Missing TOKEN: " + requestStr, null, "OFX Missing TOKEN");
                return processResult;
            }
        } else {
            // If we the OFX object is null than that means we couldn't parse the OFX.
            //   If we can't parse the OFX, then we don't know what kind it was.
            token = "0";
            mTransmissionType = TransmissionType.Unknown;
        }

        if (QBOFX.isOFXBalanceFile(requestStr)) {
            mTransmissionType = TransmissionType.BalanceFile;
        }

        mSrcTransmissionDTO.setRequestToken(QBOFX.tokenVal(token));
        mSrcTransmissionDTO.setTransmissionType(mTransmissionType);

        // do not use beginTransmission, we need to know that the transmission was saved be fore we continue
        ProcessResult<SourceSystemTransmission> transmissionProcessResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT, mCompanyPSID, this.mSrcTransmissionId, this.mSrcTransmissionDTO);
        processResult.setResult(transmissionProcessResult.getResult());
        return processResult;
    }
}
