package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.processors.*;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CloudV3ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IDDLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMP;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPITEM;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.DisburseAdvice;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PayrollItem;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.exceptions.MoneyMovementControlException;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.FlushMode;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.GenericJDBCException;

import javax.persistence.OptimisticLockException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 23, 2010
 * Time: 3:28:14 PM
 */
public class AssistedRequestProcessor {
    private static final String SKIPPED_PROCESSING_MESSAGE = "Transmission processing skipped because processing flag is set to false";
    private static String WARNING_MSG_TEMPLATE;
    private static boolean debugLogs=FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_2108_LOGS, false);
    static {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String fileName = Application.findFileOnClassPath("resources/WarningMessageTemplate.html");
            BufferedReader input = new BufferedReader(new FileReader(fileName));
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }

            WARNING_MSG_TEMPLATE = stringBuilder.toString();
        } catch (Exception e) {
            WARNING_MSG_TEMPLATE = null;
        }
    }

    private static SpcfLogger logger = PayrollServices.getLogger(AssistedRequestProcessor.class);

    private SpcfUniqueId mUnprocessedRequestId;
    private long mStartingTransactionId;
    private OFXAPPVERObject mAppVerion = null;

    public OFXAPPVERObject getAppVerion() {
        return mAppVerion;
    }

    public void setAppVerion(OFXAPPVERObject pAppVerion) {
        mAppVerion = pAppVerion;
    }

    ISocketManager mISocketManager;
    private String mPSID;
    private String mSourceTransmissionId;
    private SourceSystemTransmissionDTO mSourceSystemTransmissionDTO;
    private Company mCompany;
    private boolean mIsAssistedRequest = false;

    private CompanyProcessor mCompanyProcessor;
    private EmployeeProcessor mEmployeeProcessor;
    private PayrollItemProcessor mPayrollItemProcessor;
    private PayrollTransactionProcessor mPayrollTransactionProcessor;
    private PayrollProcessor mPayrollProcessor;
    private DisburseAdviceProcessor mDisburseAdviceProcessor;
    private PaystubProcessor mPaystubProcessor;

    private boolean mIsRetry = false;
    private boolean mShouldProcessRequest = true;

    private AssistedConnectionInformation mConnectionInformation;

    public AssistedRequestProcessor() {
        mConnectionInformation = new AssistedConnectionInformation();
     }

    public AssistedRequestProcessor(ISocketManager pISocketManager) {
        this();
        mISocketManager = pISocketManager;
    }

    public void setPSID(String pPSID) {
        mPSID = pPSID;
        mCompany = Company.findCompany(mPSID, SourceSystemCode.QBDT);
        mStartingTransactionId = Long.parseLong(mCompany.getNextPayrollTransactionId());
        setShouldProcessRequest();
    }

    public void setSourceTransmissionId(String pSourceTransmissionId) {
        mSourceTransmissionId = pSourceTransmissionId;
    }

    public void setSourceSystemTransmissionDTO(SourceSystemTransmissionDTO pSourceSystemTransmissionDTO) {
        mSourceSystemTransmissionDTO = pSourceSystemTransmissionDTO;
        mConnectionInformation.setTransmissionType(pSourceSystemTransmissionDTO.getTransmissionType());
        if(mCompany.isCompanyOnService(ServiceCode.Tax) && (!mCompany.isMigratingToAssisted() || pSourceSystemTransmissionDTO.getTransmissionType() == TransmissionType.BalanceFile)) {
            mIsAssistedRequest = true;
            mConnectionInformation.setIsAssistedRequest(mIsAssistedRequest);
        }
    }

    public void setIsRetry(boolean isRetry) {
        mIsRetry = isRetry;
        setShouldProcessRequest();
    }

    private void setShouldProcessRequest() {
        if(mCompany != null && mCompany.getQuickbooksInfo() != null) {
            mShouldProcessRequest = mCompany.getQuickbooksInfo().getProcessTransmissions();
        }

        mShouldProcessRequest = mShouldProcessRequest || mIsRetry;
    }

    public String processAssistedRequest(String request, OFX pOFX, TransmissionType pTransmissionType, CredentialType pCredentialType, String pClientIP) {
        String response = null;

        try {
            ProcessResult<List<PayrollRun>> processResult;
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                mCompany = Company.findCompany(mPSID, SourceSystemCode.QBDT);
                QbdtRequestStatus[] requestStatuses;

                if(mIsRetry || !mShouldProcessRequest) {
                    requestStatuses = new QbdtRequestStatus[]{QbdtRequestStatus.Processing};
                } else {
                    requestStatuses = new QbdtRequestStatus[]{QbdtRequestStatus.Error, QbdtRequestStatus.Queued, QbdtRequestStatus.Processing};
                }
                boolean hasUnprocessedRequests =
                        QbdtUnprocessedRequest.findUnprocessedRequests(mCompany, false, requestStatuses).size() > 0;
                PayrollServices.rollbackUnitOfWork();

                if(hasUnprocessedRequests) {
                    QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();
                    logger.error("Company with psid "+mPSID+"has a pending request that was not saved correctly or has not finished.");
                    qbdtProcessResult.setMessage(ErrorMessages.EngineeringHold("Company has a pending request that was not saved correctly or has not finished. If there was an error engineering will look into the problem."));
                    return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mPSID, mSourceTransmissionId, mSourceSystemTransmissionDTO, qbdtProcessResult,pClientIP, mIsRetry);
                }

                if(pTransmissionType.in(TransmissionType.Sync, TransmissionType.UsageSync)) {

                    processRequestOnTheAS400(request);

                    try {
                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        mCompany = Company.findCompany(mPSID, SourceSystemCode.QBDT);
                        SyncResponse syncResponse;
                        if (mAppVerion.listIdLoopBackSupported()) {
                            syncResponse = createSyncResponseWithListId(pOFX, mCompany);
                        } else {
                            syncResponse = createSyncResponse(pOFX, mCompany);
                        }
                        response = syncResponse.response;
                        PayrollServices.rollbackUnitOfWork();

                        if (!syncResponse.ratePushItems.isEmpty()) {
                            PayrollServices.beginUnitOfWork();
                            for (QbdtPayrollItemInfo ratePushItem : syncResponse.ratePushItems) {
                                Application.refresh(ratePushItem);
                                ratePushItem.setRatePushToken(-1);
                            }
                            PayrollServices.commitUnitOfWork();
                        }

                    } catch (MaxLimitExceededException e) {
                        PayrollServices.rollbackUnitOfWork();

                        QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();
                        qbdtProcessResult.setMessage(ErrorMessages.MaxLimitExceededError(e.getMessage()));
                        return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mPSID, mSourceTransmissionId, mSourceSystemTransmissionDTO, qbdtProcessResult,pClientIP, mIsRetry);
                    }
                } else {
                    if(!mIsRetry && pCredentialType == CredentialType.Pin) {
                        mUnprocessedRequestId = storeUnprocessedAssistedRequest();
                    }

                    PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.MANUAL);

                    processResult = handleOFXSubmissionInPSP(pOFX, pTransmissionType, pCredentialType);

                    if (processResult.isSuccess()) {
                        boolean commitSuccessful = false;
                        try {
                            if(mConnectionInformation.isZeroPayroll()) {
                                CompanyEvent.createZeroPayrollReceivedEvent(mCompany, mSourceTransmissionId);
                            }
                            StopWatch stopWatch = StopWatch.startTimer();
                            logger.info("Started commit for PSID=" +  mCompany.getSourceCompanyId());
                            PayrollServices.commitUnitOfWorkWithSecondary();
                            logger.info("Finished commit for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + stopWatch.getElapsedSeconds());
                            commitSuccessful = true;
                            // if we make it here we have committed everything and we won't need to reprocess the request
                            if(mShouldProcessRequest) {
                                deleteUnprocessedRequest(mUnprocessedRequestId);
                            } else {
                                // mark as an error for now, we'll reprocess later
                                updateUnprocessedRequest(mUnprocessedRequestId, QbdtRequestStatus.Error, SKIPPED_PROCESSING_MESSAGE);
                            }
                            mUnprocessedRequestId = null;
                        } catch (Throwable t) {
                            PayrollServices.rollbackUnitOfWorkWithSecondary();
                            if(!commitSuccessful) {
                                if (mIsAssistedRequest) {
                                    if(t instanceof StaleObjectStateException || t instanceof OptimisticLockException || t instanceof GenericJDBCException) {
                                        // mark for immediate reprocessing
                                        updateUnprocessedRequest(mUnprocessedRequestId, mShouldProcessRequest ? QbdtRequestStatus.Queued : QbdtRequestStatus.Error, mShouldProcessRequest ? null : SKIPPED_PROCESSING_MESSAGE);
                                        logger.error("Error processing assisted request. The request has been queued to be automatically reprocessed", t);
                                    } else if(t instanceof MoneyMovementControlException) {
                                        logger.error("FT Amount Breached The Allowed Threshold Amount for Assisted Company. psid=" + mCompany.getSourceCompanyId(), t);
                                        throw t;
                                    }
                                } else {
                                    if(t instanceof MoneyMovementControlException)
                                        logger.error("FT Amount Breached The Allowed Threshold Amount for Non Assisted Company. psid=" + mCompany.getSourceCompanyId(), t);
                                    // allow normal exception path for non assisted requests
                                    throw t;
                                }
                            } else {
                                // log the error, but continue
                                // the error must have been in updating the unprocessed request or updating the tokens on the AS400
                                logger.error("Error after request commit. No immediate action needed logging for stack trace only. ", t);
                            }
                        }

                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        if (response == null) {
                            // generate the OFX response
                            response = createPayrollResponse(pOFX, pTransmissionType, processResult, false);
                        }
                        PayrollServices.rollbackUnitOfWork();

                    } else {
                        PayrollServices.rollbackUnitOfWorkWithSecondary();

                        deleteUnprocessedRequest(mUnprocessedRequestId);
                        mUnprocessedRequestId = null;

                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        QBDTProcessResult qbdtProcessResult = createClientErrorMessage(processResult, pOFX);
                        PayrollServices.rollbackUnitOfWork();
                        return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mPSID, mSourceTransmissionId, mSourceSystemTransmissionDTO, qbdtProcessResult,pClientIP, mIsRetry);
                    }
                }
            } finally {
                PayrollServices.rollbackUnitOfWorkWithSecondary();
            }

        } catch (Throwable t) {
            logger.warn("Error processing assisted request for PSID=" + mPSID, t);

            // delete the unprocessed record
            if(mUnprocessedRequestId != null) {
                deleteUnprocessedRequest(mUnprocessedRequestId);
                mUnprocessedRequestId = null;
            }

            QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();
            qbdtProcessResult.setMessage(ErrorMessages.UnexpectedError(t.getMessage()));
            return QBDTRequestProcessorHelper.createErrorResponseStringAndFinalizeTransmission(mPSID, mSourceTransmissionId, mSourceSystemTransmissionDTO, qbdtProcessResult,pClientIP, mIsRetry);
        }

        if(!mConnectionInformation.getTransmissionType().in(TransmissionType.Sync, TransmissionType.UsageSync) && !mConnectionInformation.isZeroPayroll()) {
            mSourceSystemTransmissionDTO.setQBDTRequestInfoDTO(mConnectionInformation.createRequestInfo());
        }

        StopWatch stopWatch = StopWatch.startTimer();
        logger.info("Started finalize for PSID=" + mCompany.getSourceCompanyId());
        finalizeTransmission(response,pClientIP);
        logger.info("Finished finalize for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + stopWatch.getElapsedSeconds());
        return response;
    }

    public void finalizeTransmission(String pResponse,String pClientIP) {
        QBDTRequestProcessorHelper.finalizeTransmission(mPSID, mSourceTransmissionId, mSourceSystemTransmissionDTO, pResponse, mConnectionInformation.getConnectionMessage(), null, pClientIP,mIsRetry);
    }

    private ProcessResult saveAS400LiabilityChecks(com.intuit.sbd.payroll.psp.common.ofx.response.OFX pAS400Response, Company pCompany, CredentialType pCredentialType) {
        ProcessResult processResult = new ProcessResult();

        // payroll response
        if(pAS400Response.getIPAYROLLMSGSRSV1() != null &&
                pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS() != null &&
                pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS() != null &&
                pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS() != null) {
            PayrollTransactionProcessor payrollTransactionProcessor = new PayrollTransactionProcessor(pCompany, new AssistedConnectionInformation(), pCredentialType);

            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx : pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX()) {
                processResult.merge(addAddOrUpdateAS400PayrollTransaction(pCompany, payrollTransactionProcessor, ipayrolltx));
            }

            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx : pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTXMOD()) {
                processResult.merge(addAddOrUpdateAS400PayrollTransaction(pCompany, payrollTransactionProcessor, ipayrolltx));
            }
        }
        // sync response
        else if(pAS400Response.getIPAYROLLMSGSRSV1() != null &&
                pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS() != null &&
                pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA() != null) {
            PayrollTransactionProcessor payrollTransactionProcessor = new PayrollTransactionProcessor(pCompany, new AssistedConnectionInformation(), pCredentialType);

            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx : pAS400Response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD()) {
                processResult.merge(addAddOrUpdateAS400PayrollTransaction(pCompany, payrollTransactionProcessor, ipayrolltx));
            }
        }
        return processResult;
    }

    private ProcessResult addAddOrUpdateAS400PayrollTransaction(Company pCompany, PayrollTransactionProcessor pPayrollTransactionProcessor, com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx) {
        ProcessResult processResult = new ProcessResult();
        ResponsePayrollTransaction responsePayrollTransaction = new ResponsePayrollTransaction(ipayrolltx);
        if(responsePayrollTransaction.getTransactionType() == AbstractPayrollTransaction.TransactionType.LiabilityCheck) {
            responsePayrollTransaction.setSystemModified(true);
            LiabilityCheck liabilityCheck = LiabilityCheck.findLiabilityCheckBySourceId(pCompany, responsePayrollTransaction.getSourceId());
            processResult.merge(pPayrollTransactionProcessor.addOrUpdateLiabilityCheck(responsePayrollTransaction, liabilityCheck));
        } else if(responsePayrollTransaction.getTransactionType() == AbstractPayrollTransaction.TransactionType.FundsTransfer ||
                responsePayrollTransaction.getTransactionType() == AbstractPayrollTransaction.TransactionType.DirectDepositReturn) {
            QbdtPayrollTransaction qbdtPayrollTransaction = QbdtPayrollTransaction.findQbdtPayrollTransactionBySourceId(pCompany, responsePayrollTransaction.getSourceId());
            processResult.merge(pPayrollTransactionProcessor.addOrUpdatePayrollTransaction(responsePayrollTransaction, qbdtPayrollTransaction));
        } else if(responsePayrollTransaction.getTransactionType() == AbstractPayrollTransaction.TransactionType.CompanyLiabilityAdjustment ||
                responsePayrollTransaction.getTransactionType() == AbstractPayrollTransaction.TransactionType.EmployeeLiabilityAdjustment) {
            if(CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(pCompany, responsePayrollTransaction.getSourceId()) != null) {
                processResult.merge(pPayrollTransactionProcessor.updateLiabilityAdjustments(responsePayrollTransaction));
            }
        }
        return processResult;
    }

    private QBDTProcessResult createClientErrorMessage(ProcessResult<List<PayrollRun>> pProcessResult, OFX pRequest) {
        QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();

        List<String> messageCodes = new ArrayList<String>();
        for (Message message : pProcessResult.getMessages()) {
            messageCodes.add(message.getMessageCode());
        }

        if(messageCodes.contains(PSPErrorMessageConstants.COMPANY_OPERATION_NOT_ALLOWED)) {
            qbdtProcessResult.setMessage(QBDTRequestProcessorHelper.getOnHoldErrorMessageForCompanyStatus(mPSID).getResult());
        } else if (messageCodes.contains(PSPErrorMessageConstants.PAYCHECK_DATE_TOO_FAR_IN_FUTURE)) {
            qbdtProcessResult.setMessage(ErrorMessages.FutureDatedPayrollTooFarInfutureError());
        } else if (messageCodes.contains(PSPErrorMessageConstants.PAYCHECK_DATE_AFTER_DISCO_DATE)) {
            SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                    SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
            SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();
            String quickbooksVersionString = parameter.getParameterValue();
            SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
            expirationDate.addDays(-1);
            String formattedDate = formatter.format(CalendarUtils.convertToDate(expirationDate));
            qbdtProcessResult.setMessage(ErrorMessages.QBReleaseWithChecksAfterSunset(quickbooksVersionString, quickbooksVersionString, formattedDate, formattedDate));
        } else if(messageCodes.contains(PSPErrorMessageConstants.PAYROLL_SUBMITTED_WITH_PENDING_NOC)) {
            Set<String> nocErrors = new HashSet<String>();
            for (Message message : pProcessResult.getMessages()) {
                if(message.getMessage() != null) {
                    nocErrors.add(message.getMessage());
                }
            }
            if (nocErrors.size() > 1) {
                qbdtProcessResult.setMessage(ErrorMessages.PayrollRejectNOCMultipleEEBankAccounts());
            }  else {
                QBDTProcessResult<ErrorMessage> errMsgPR = QBDTRequestProcessorHelper.getNOCMessage(mCompany);
                qbdtProcessResult.setMessage(errMsgPR.getResult());
            }
        } else if(messageCodes.contains(PSPErrorMessageConstants.PAYROLL_SUBMITTED_WITH_PENDING_EE_RETURN)) {
            Set<String> returnErrors = new HashSet<String>();
            for (Message message : pProcessResult.getMessages()) {
                if(message.getMessage() != null) {
                    returnErrors.add(message.getMessage());
                }
            }
            if (returnErrors.size() > 1) {
                qbdtProcessResult.setMessage(ErrorMessages.PayrollRejectBankReturnMultipleEEBankAccounts());
            }  else {
                QBDTProcessResult<ErrorMessage> errMsgPR = QBDTRequestProcessorHelper.getBankReturnEEBankAccountMessage(mCompany);
                qbdtProcessResult.setMessage(errMsgPR.getResult());
            }
        } else if (messageCodes.contains(PSPErrorMessageConstants.INVALID_ROUTING_NUMBER)) {
            String badRoutingNumber = "";
            for (Message message : pProcessResult.getMessages()) {
                if(PSPErrorMessageConstants.INVALID_ROUTING_NUMBER.equals(message.getMessageCode())) {
                    badRoutingNumber = message.getMessage().replaceAll("[^\\d]", "");
                    break;
                }
            }

            String employeeName = "";
            for (IPAYROLLRUN ipayrollrun : pRequest.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
                for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                    for (IDDLINE iddline : ipaychk.getIDDLINE()) {
                        if (badRoutingNumber.equals(iddline.getIDDACCT().getBANKACCTTO().getBANKID())) {
                            employeeName = ipaychk.getIEMPNAME();
                            break;
                        }
                    }
                }
            }

            qbdtProcessResult.setMessage(ErrorMessages.InvalidEERoutingNumber(badRoutingNumber, employeeName));
        } else if(messageCodes.contains(PSPErrorMessageConstants.COMPANY_BANK_ACCOUNT_NOT_ACTIVE)) {
            qbdtProcessResult.setMessage(ErrorMessages.BankAccountNotActiveError());
        } else if (messageCodes.contains(PSPErrorMessageConstants.PAYROLL_RUN_EXCEEDS_DD_LIMITS)) {
            qbdtProcessResult.setMessage(ErrorMessages.PayrollRejectDDLimit());
        }  else if (messageCodes.contains(PSPErrorMessageConstants.DUPLICATE_PAYCHECK_FOUND)) {
            qbdtProcessResult.setMessage(ErrorMessages.DuplicatePaycheckUniqueId());
        } else if (messageCodes.contains(PSPErrorMessageConstants.OUTDATED_FFCRA_ITEMS)) {
            qbdtProcessResult.setMessage(ErrorMessages.PayrollRejectFFCRAOutdatedItems());
        }
        if(qbdtProcessResult.getMessage() == null) {
            String errorMessage = pProcessResult.toString();
            if(errorMessage != null && errorMessage.length() > 3500) {
                errorMessage = errorMessage.substring(0, 3500);
            }

            qbdtProcessResult.setMessage(ErrorMessages.AssistedProcessingDataError(errorMessage));
        } else if(mConnectionInformation.getTransmissionType().in(TransmissionType.PayrollSubmission, TransmissionType.UsageSend)){
            QBDTCompanyEventDTO eventDTO = new QBDTCompanyEventDTO();
            eventDTO.setCompany(mCompany);
            eventDTO.setErrMsg(qbdtProcessResult.getMessage().getErrorDescription());
            eventDTO.setEventTypeCode(EventTypeCode.PayrollRejected);
            eventDTO.setTransmissionId(mSourceTransmissionId);
            qbdtProcessResult.addCompanyEvent(eventDTO);
        }

        return qbdtProcessResult;
    }

    private QBDTProcessResult createClientWarningMessage(ProcessResult<List<PayrollRun>> pProcessResult) {
        QBDTProcessResult qbdtProcessResult = new QBDTProcessResult();

        List<String> messageCodes = new ArrayList<String>();
        for (Message message : pProcessResult.getMessages()) {
            messageCodes.add(message.getMessageCode());
        }

        if(messageCodes.contains(PSPErrorMessageConstants.WARNING_PENDING_NOC)) {
                QBDTProcessResult<ErrorMessage> errMsgPR = QBDTRequestProcessorHelper.getNOCMessage(mCompany);
                qbdtProcessResult.setMessage(errMsgPR.getResult());
        } else if(messageCodes.contains(PSPErrorMessageConstants.WARNING_BACKDATE_FEE)) {
            ErrorMessage errorMessage = new ErrorMessage(ErrorMessages.ErrorEnum.AssistedProcessingDataError, "0",
                                                         "Your payroll was not transmitted by 5:00 p.m. (Pacific Time) two banking days prior to the paycheck date.  As a result, your direct deposits will be delayed one business day, you are subject to a backdating fee, and you may also receive notices or penalties and interest charges for late payment of payroll taxes.",
                                                         null, null);
            qbdtProcessResult.setMessage(errorMessage);
        }

        return qbdtProcessResult;
    }

    private ProcessResult<String> processRequestOnTheAS400(String pRequest) throws ReadTimeOutException {
        // only forward tax requests to the AS400
        if(!mIsAssistedRequest) {
            return new ProcessResult<String>();
        }

        ProcessResult<String> processResult = new ProcessResult<String>();
        processResult.setResult(processRequest(pRequest));
        if(processResult.getResult() != null) {
            processResult.setSuccess(!QBOFX.ofxStringContainsErrorSeverity(processResult.getResult()));
        }
        mConnectionInformation.setIsAs400Rejection(!processResult.isSuccess());
        return processResult;
    }

    public ProcessResult<List<PayrollRun>> handleOFXSubmissionInPSP(OFX requestOfx, TransmissionType transmissionType, CredentialType pCredentialType) {
        mCompany = Company.findCompany(mPSID, SourceSystemCode.QBDT);

        if(!mShouldProcessRequest) {
            mConnectionInformation.setProcessedRequest(mShouldProcessRequest);
            // do not process the request, only increment the token and next id
            mCompany.getNextToken();
            if(requestOfx.getIPAYROLLMSGSRQV1() != null && requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ() != null &&
                    requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ() != null &&
                    requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ() != null) {
                IPAYROLLRQ ipayrollrq = requestOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
                for (IEMP iemp : ipayrollrq.getIEMP()) {

                	//set session id for kount
                	iemp.setISESSIONID(ipayrollrq.getISESSIONID());
                    mCompany.usedEmployeeId(iemp.getIEMPID());
                }
                for (IPITEM ipitem : ipayrollrq.getIPITEM()) {
                    mCompany.usedPayrollItemId(ipitem.getIPITEMID());
                }
                for (IPAYROLLTX ipayrolltx : ipayrollrq.getIPAYROLLTX()) {
                    mCompany.usedPayrollTransactionId(ipayrolltx.getIPAYROLLTXID());
                }
                for (IPAYROLLRUN ipayrollrun : ipayrollrq.getIPAYROLLRUN()) {
                	ipayrollrun.setISESSIONID(ipayrollrq.getISESSIONID());

                    for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                        mCompany.usedPaycheckId(ipaychk.getIPAYCHKID());
                    }
                }
            }
            Application.save(mCompany);
            return new ProcessResult<List<PayrollRun>>();
        }

        // initialize processors
        mCompanyProcessor = new CompanyProcessor(mCompany, mConnectionInformation, pCredentialType);
        mEmployeeProcessor = new EmployeeProcessor(mCompany, mConnectionInformation, pCredentialType);
        mPayrollItemProcessor = new PayrollItemProcessor(mCompany, mConnectionInformation,pCredentialType);
        mPayrollTransactionProcessor = new PayrollTransactionProcessor(mCompany, mConnectionInformation, pCredentialType);
        mPayrollProcessor = new PayrollProcessor(mCompany, mConnectionInformation, mSourceTransmissionId, transmissionType, new OFXAPPVERObject(requestOfx.getSIGNONMSGSRQV1().getSONRQ().getAPPVER()), pCredentialType, mEmployeeProcessor.getMigratedEmployees());
        mPaystubProcessor = new PaystubProcessor(mCompany, mConnectionInformation);
        mDisburseAdviceProcessor = new DisburseAdviceProcessor(mCompany);

        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();
        switch (transmissionType) {
            case PayrollSubmission:
            case UsageSend:
                processResult = processAssistedPayrollRequest(requestOfx);
                if(FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_SYNC_COMPLETE_INFO, false)) {
                    processResult.merge(processV3PayrollRequest(requestOfx));
                }
                break;
            case BalanceFile:
                Application.getSessionCache().addNonHibernateObject(Employee.ALL_EMPLOYEES_IN_MEMORY_CACHE_KEY + ":" + mCompany.getId(), false);
                processResult = processAssistedBalanceFile(requestOfx);
                break;
            default:
                // psp is not doing any processing for other transmission types
                break;
        }
        return processResult;
    }

    private com.intuit.sbd.payroll.psp.common.ofx.response.OFX initializeSuccessfulResponse(OFX pRequest, TransmissionType pTransmissionType, ProcessResult pProcessResult) {
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = new com.intuit.sbd.payroll.psp.common.ofx.response.OFX();
        response.setSIGNONMSGSRSV1(SignOnProcessor.createSuccessfulSignOn());

        addServiceMessages(pRequest, response);

        IPAYROLLMSGSRSV1 ipayrollmsgsrsv1 = new IPAYROLLMSGSRSV1();
        IPAYROLLUPDATERS ipayrollupdaters = new IPAYROLLUPDATERS();

        Company company = Company.findCompany(mPSID, SourceSystemCode.QBDT);
        ipayrollupdaters.setTOKEN(Long.toString(company.getCurrentToken()));
        ipayrollupdaters.setIEMPNEXTID(company.getNextEmployeeId());
        ipayrollupdaters.setIPITEMNEXTID(company.getNextPayrollItemId());
        ipayrollupdaters.setIPAYROLLTXNEXTID(company.getNextPayrollTransactionId());
        ipayrollupdaters.setIPAYCHKNEXTID(company.getNextPaycheckId());

        if(pTransmissionType == TransmissionType.BalanceFile) {
            addServiceStatus(ipayrollupdaters, company);
        }

        if(!pTransmissionType.in(TransmissionType.Sync, TransmissionType.UsageSync)) {
            IPAYROLLTRNRS ipayrolltrnrs = new IPAYROLLTRNRS();
            STATUS status = new STATUS();
            status.setCODE(QBOFX.SUCCESS_STATUS_CODE);
            status.setSEVERITY(QBOFX.MESSAGE_SEVERITY.INFO);
            ipayrolltrnrs.setSTATUS(status);
            if(pRequest.getIPAYROLLMSGSRQV1() != null && pRequest.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ()!= null &&
                    pRequest.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ() != null) {
                ipayrolltrnrs.setTRNUID(pRequest.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getTRNUID());
            }

            // add warning msg if needed
            if (mAppVerion.warningSupported() && WARNING_MSG_TEMPLATE != null && pProcessResult.hasCloudWarnings()) {
                WARNING warning = new WARNING();
                warning.setTITLE("Payroll Cloud Service Missing Information");
                warning.setMSG(StringEscapeUtils.escapeXml(prepareWarningPage(pProcessResult.getWarnings())));
                ipayrolltrnrs.setWARNING(warning);
            }

            // DIY-DD requires a blank PAYROLLRS tag
            if(!mIsAssistedRequest) {
                ipayrolltrnrs.setIPAYROLLRS(new IPAYROLLRS());
            }

            ipayrollupdaters.setIPAYROLLTRNRS(ipayrolltrnrs);
        }

        ipayrollmsgsrsv1.setIPAYROLLUPDATERS(ipayrollupdaters);
        response.setIPAYROLLMSGSRSV1(ipayrollmsgsrsv1);

        return response;
    }

    private String prepareWarningPage(HashMap<ServiceCode, HashSet<String>> pWarnings) {
        String msg = WARNING_MSG_TEMPLATE;

        // wc
        StringBuffer wc_warning = new StringBuffer();
        TreeSet<String> wc_warning_list = new TreeSet<String>(pWarnings.get(ServiceCode.WorkersComp));
        if (wc_warning_list != null) {
            for (String each : wc_warning_list) {
                wc_warning.append(each).append("\n");
            }
        }

        if (wc_warning.length() > 0) {
            msg = msg.replace("<!--WC_WARNING_PLACE_HOLDER-->", wc_warning);
        }

        return msg;
    }

    private void addServiceMessages(OFX pRequest, com.intuit.sbd.payroll.psp.common.ofx.response.OFX pResponse) {
        // sunset message
        addSunsetMessage(pRequest, pResponse);
    }

    private void addSunsetMessage(OFX pRequest, com.intuit.sbd.payroll.psp.common.ofx.response.OFX pResponse) {
        SpcfCalendar calendar = PSPDate.getPSPTime().toLocal();
        SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar effectiveDate = parameter.getEffectiveDate().toLocal();
        SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();
        String quickbooksVersionString = parameter.getParameterValue();

        OFXAPPVERObject OFXAPPVERObject = new OFXAPPVERObject(pRequest.getSIGNONMSGSRQV1().getSONRQ().getAPPVER());
        if (OFXAPPVERObject.isMinQBVersionSupported()) {
            if (calendar.getTimeInMilliseconds() >= effectiveDate.getTimeInMilliseconds() &&
                    calendar.getTimeInMilliseconds() < expirationDate.getTimeInMilliseconds()) {

                // Set the expiration date to one day before for purposes of message
                // This assumes that the
                expirationDate.addDays(-1);
                SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d,yyyy");
                String formattedDate = formatter.format(CalendarUtils.convertToDate(expirationDate));

                ErrorMessage errMsg = ErrorMessages.QBReleaseToBeSunset(quickbooksVersionString, formattedDate, formattedDate);
                pResponse.getSIGNONMSGSRSV1().getSONRS().getSTATUS().setMESSAGE(errMsg.getErrorDescription());
            }
        }
    }

    public void addServiceStatus(IPAYROLLUPDATERS pIpayrollupdaters, Company pCompany) {
        CompanyService taxService = pCompany.getService(ServiceCode.Tax);
        CompanyService ddService = pCompany.getService(ServiceCode.DirectDeposit);

        boolean hasActiveTaxService = taxService != null && pCompany.isCompanyOnService(ServiceCode.Tax);
        boolean hasActiveDDService = ddService != null && pCompany.isCompanyOnService(ServiceCode.DirectDeposit);

        ITAXSERVSTATUS itaxservstatus = new ITAXSERVSTATUS();
        if(taxService != null) {
            if(hasActiveTaxService && taxService.getStatusCd() != null) {
                switch (taxService.getStatusCd()) {
                    case PendingSetup:
                    case PendingBalanceFile:
                        itaxservstatus.setITAXSERVMODE(QBOFX.TAX_MODES.VERIFIED);
                        break;
                    default:
                        if(taxService.getServiceStartDate() != null) {
                            itaxservstatus.setIDTACTIVE(QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(taxService.getServiceStartDate())));
                        }
                        itaxservstatus.setITAXSERVMODE(QBOFX.TAX_MODES.ACTIVE);
                        break;
                }
            } else if(hasActiveDDService) {
                itaxservstatus.setITAXSERVMODE(QBOFX.TAX_MODES.NO);
            } else {
                itaxservstatus.setITAXSERVMODE(QBOFX.TAX_MODES.TERMINATED);
            }
        } else if(hasActiveDDService) {
            itaxservstatus.setITAXSERVMODE(QBOFX.TAX_MODES.NO);
        }

        if(itaxservstatus.getITAXSERVMODE() != null) {
            pIpayrollupdaters.setITAXSERVSTATUS(itaxservstatus);
        }


        if(ddService != null) {
            IDDSTATUS iddstatus = new IDDSTATUS();
            if(hasActiveDDService) {
                iddstatus.setIDDMODE(QBOFX.DD_MODES.ACTIVE);
            } else {
                iddstatus.setIDDMODE(QBOFX.DD_MODES.TERMINATED);
            }
            iddstatus.setIFUNDINGMODEL(pCompany.getFundingModel().getFundingModelCd());
            pIpayrollupdaters.setIDDSTATUS(iddstatus);
        }

        if(mConnectionInformation.ismIsDataRecovery()) {

            logger.info("Setting guideLine status for Data recovery for PSID=" + pCompany.getSourceCompanyId());

            CompanyService guidelineService = pCompany.getService(ServiceCode.Guideline401k);

            if (guidelineService != null) {
                IGLSERVSTATUS iglservstatus = new IGLSERVSTATUS();

                switch (guidelineService.getStatusCd()) {
                    case ActiveCurrent:
                        iglservstatus.setIGLMODE(QBOFX.GUIDELINE_MODES.ACTIVE);
                        break;
                    case PendingEnrollment:
                        iglservstatus.setIGLMODE(QBOFX.GUIDELINE_MODES.PENDING_ENROLLMENT);
                        break;
                    case Cancelled:
                        iglservstatus.setIGLMODE(QBOFX.GUIDELINE_MODES.CANCELLED);
                        break;
                }
                pIpayrollupdaters.setIGLSERVSTATUS(iglservstatus);
            }
        }
    }

    private String createPayrollResponse(OFX pRequest, TransmissionType pTransmissionType, ProcessResult<List<PayrollRun>> pProcessResult, boolean pForceEmptyResponse) {
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofx = initializeSuccessfulResponse(pRequest, pTransmissionType, pProcessResult);
        IPAYROLLRS ipayrollrs = ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();

        if(!pForceEmptyResponse) {

            // do not include void responses in balf response
            if(pTransmissionType != TransmissionType.BalanceFile) {
                // add voided paychecks to response
                // void acks do not include detail
                DomainEntitySet<Paycheck> voidedPaychecks = Paycheck.findPaychecksVoidedDuringSubmission(mCompany, mCompany.getCurrentToken());
                for (Paycheck voidedPaycheck : voidedPaychecks.sort(Paycheck.SourcePaycheckId())) {
                    if(ipayrollrs == null) {
                        ipayrollrs = new IPAYROLLRS();
                        ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().setIPAYROLLRS(ipayrollrs);
                    }
                    ipayrollrs.getIPAYCHKMOD().add(new PaycheckResponse(voidedPaycheck, mAppVerion).getIPAYCHKMOD());
                }
            }

            addLiabilityChecksToPayrollResponse(mCompany, ofx);

            if(pProcessResult.hasWarnings()) {
                // add warning messages
                if(ofx.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE() == null) {
                    QBDTProcessResult qbdtProcessResult = createClientWarningMessage(pProcessResult);
                    if(qbdtProcessResult.getMessage() != null){
                    ofx.getSIGNONMSGSRSV1().getSONRS().getSTATUS().setMESSAGE(qbdtProcessResult.getMessage().getErrorDescription());
                    }
                }
            }
        }

        return OFXManager.javaResponseToOFX(ofx);
    }

    public void addLiabilityChecksToPayrollResponse(Company pCompany, com.intuit.sbd.payroll.psp.common.ofx.response.OFX pResponse) {
        if(mConnectionInformation.isZeroPayroll()) {
            // the token will match the system modified token if the last request was a payroll and this one is a zero payroll
            return;
        }

        IPAYROLLRS ipayrollrs = pResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();

        DomainEntitySet<LiabilityCheck> liabilityChecks =
                Application.find(LiabilityCheck.class,
                        LiabilityCheck.Company().equalTo(pCompany)
                                .And(LiabilityCheck.SystemModifiedToken().equalTo(pCompany.getCurrentToken()))
                                .And(LiabilityCheck.QbdtTransactionInfo().Token().greaterThan(0L)))
                           .sort(LiabilityCheck.SourceId());

        for (LiabilityCheck liabilityCheck : liabilityChecks) {
            if(ipayrollrs == null) {
                ipayrollrs = new IPAYROLLRS();
                pResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().setIPAYROLLRS(ipayrollrs);
            }

            PayrollTransactionResponse payrollTransactionResponse = new PayrollTransactionResponse(liabilityCheck);
            if (Long.parseLong(liabilityCheck.getSourceId()) >= mStartingTransactionId) {
                ipayrollrs.getIPAYROLLTX().add(payrollTransactionResponse.getIPAYROLLTX());
            } else {
                ipayrollrs.getIPAYROLLTXMOD().add(payrollTransactionResponse.getIPAYROLLTX());
            }
        }
    }

    private class SyncResponse {
        public String response;
        public List<QbdtPayrollItemInfo> ratePushItems = new ArrayList<QbdtPayrollItemInfo>();
    }

	private SyncResponse createSyncResponse(OFX pRequest, Company pCompany) {
        SyncResponse syncResponse = new SyncResponse();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofx = initializeSuccessfulResponse(pRequest, TransmissionType.Sync, null);

        long currentToken = mCompany.getCurrentToken();
        long syncToken = QBOFX.tokenVal(pRequest.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN());
        if(syncToken < currentToken) {
            mConnectionInformation.setIsDataRecovery(true);
            if (!pCompany.onUsageBilling() || pCompany.isCompanyOnService(ServiceCode.DirectDeposit, ServiceCode.Tax)) {
                IPAYROLLUPDATEDATA ipayrollupdatedata = ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
                if (ipayrollupdatedata == null) {
                    ipayrollupdatedata = new IPAYROLLUPDATEDATA();
                    ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().setIPAYROLLUPDATEDATA(ipayrollupdatedata);
                }

                addSyncCompanyInfo(pCompany, syncToken, ipayrollupdatedata);
                addSyncPayrollTransactions(pCompany, syncToken, ipayrollupdatedata);

                addServiceStatus(ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS(), pCompany);

                if (mIsAssistedRequest) {
                    addSyncPaychecks(pCompany, syncToken, ipayrollupdatedata);
                    syncResponse.ratePushItems = addSyncPayrollItems(pCompany, syncToken, ipayrollupdatedata);
                    addSyncEmployees(pCompany, syncToken, ipayrollupdatedata);
                } else if(syncToken > pCompany.getServiceStartToken()){
                    DomainEntitySet<Paycheck> voidedPaychecks = Paycheck.findPaychecksVoidedDuringSubmission(pCompany, syncToken);
                    for (Paycheck voidedPaycheck : voidedPaychecks.sort(Paycheck.SourcePaycheckId())) {
                        ipayrollupdatedata.getIPAYCHKMOD().add(new PaycheckResponse(voidedPaycheck, mAppVerion).getIPAYCHKMOD());
                    }
                }
            }
        } else if(!pCompany.isCompanyOnService(ServiceCode.DirectDeposit, ServiceCode.Tax)){
            addServiceStatus(ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS(), pCompany);
        }

        syncResponse.response = OFXManager.javaResponseToOFX(ofx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
		return syncResponse;
	}

    private SyncResponse createSyncResponseWithListId(OFX pRequest, Company pCompany) {
        SyncResponse syncResponse = new SyncResponse();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofx = initializeSuccessfulResponse(pRequest, TransmissionType.Sync, null);

        long currentToken = mCompany.getCurrentToken();
        long syncToken = QBOFX.tokenVal(pRequest.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN());

        if (syncToken < currentToken) {
            mConnectionInformation.setIsDataRecovery(true);

            IPAYROLLUPDATEDATA ipayrollupdatedata = ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA();
            if (ipayrollupdatedata == null) {
                ipayrollupdatedata = new IPAYROLLUPDATEDATA();
                ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().setIPAYROLLUPDATEDATA(ipayrollupdatedata);
            }

            addSyncCompanyInfo(pCompany, syncToken, ipayrollupdatedata);
            addSyncPayrollTransactions(pCompany, syncToken, ipayrollupdatedata);
            addSyncPaychecks(pCompany, syncToken, ipayrollupdatedata);
            syncResponse.ratePushItems = addSyncPayrollItems(pCompany, syncToken, ipayrollupdatedata);
            addSyncEmployees(pCompany, syncToken, ipayrollupdatedata);
        }

        addServiceStatus(ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS(), pCompany);

        syncResponse.response = OFXManager.javaResponseToOFX(ofx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        return syncResponse;
    }

	private void addSyncCompanyInfo(Company pCompany, long pSyncToken, IPAYROLLUPDATEDATA pIPAYROLLUPDATEDATA) {
        if(pCompany.getQuickbooksInfo() != null && pCompany.getQuickbooksInfo().getToken() > pSyncToken) {
            pIPAYROLLUPDATEDATA.setICOINFOMOD(new CompanyResponse(pCompany).getICOINFOMOD());
        }
    }

    private List<QbdtPayrollItemInfo> addSyncPayrollItems(Company pCompany, long pSyncToken, IPAYROLLUPDATEDATA pIPAYROLLUPDATEDATA) {
        Map<String, PayrollItemResponse> payrollItemResponseMap = new HashMap<String, PayrollItemResponse>();

        // find and add company payroll items
        DomainEntitySet<QbdtPayrollItemInfo> qbdtPayrollItemInfos = QbdtPayrollItemInfo.findPayrollItemsWithGreaterToken(pCompany, pSyncToken);

        // compensation (string sort is good enough)
        DomainEntitySet<QbdtPayrollItemInfo> compensationItems =
                qbdtPayrollItemInfos.find(QbdtPayrollItemInfo.CompanyPayrollItem().PayrollItem().PayrollItemCode().in(PayrollItem.COMPENSATION_CODES))
                        .sort(QbdtPayrollItemInfo.CompanyPayrollItem().SourcePayrollItemId());
        for (QbdtPayrollItemInfo compensationItem : compensationItems) {
            CompanyPayrollItem companyPayrollItem = compensationItem.getCompanyPayrollItem();
            if(compensationItem.getIsDeleted()) {
                payrollItemResponseMap.put(companyPayrollItem.getSourcePayrollItemId(), null);
            } else {
                payrollItemResponseMap.put(companyPayrollItem.getSourcePayrollItemId(), new PayrollItemResponse(compensationItem, companyPayrollItem));
            }
        }

        // tax items (string sort is good enough)
        DomainEntitySet<QbdtPayrollItemInfo> taxItems =
                qbdtPayrollItemInfos.find(QbdtPayrollItemInfo.CompanyLaw().isNotNull())
                        .sort(QbdtPayrollItemInfo.CompanyLaw().SourceId());
        for (QbdtPayrollItemInfo taxItem : taxItems) {
            CompanyLaw companyLaw = taxItem.getCompanyLaw();
            if(taxItem.getIsDeleted()) {
                payrollItemResponseMap.put(companyLaw.getSourceId(), null);
            } else {
                payrollItemResponseMap.put(companyLaw.getSourceId(), new PayrollItemResponse(taxItem, companyLaw));
            }
        }

        // deductions (string sort is good enough)
        DomainEntitySet<QbdtPayrollItemInfo> deductionItems =
                qbdtPayrollItemInfos.find(QbdtPayrollItemInfo.CompanyPayrollItem().PayrollItem().PayrollItemCode().in(PayrollItem.DEDUCTION_CODES))
                        .sort(QbdtPayrollItemInfo.CompanyPayrollItem().SourcePayrollItemId());
        for (QbdtPayrollItemInfo deductionItem : deductionItems) {
            CompanyPayrollItem companyPayrollItem = deductionItem.getCompanyPayrollItem();
            if(deductionItem.getIsDeleted()) {
                payrollItemResponseMap.put(companyPayrollItem.getSourcePayrollItemId(), null);
            } else {
                payrollItemResponseMap.put(companyPayrollItem.getSourcePayrollItemId(), new PayrollItemResponse(deductionItem, companyPayrollItem));
            }
        }

        // contributions (string sort is good enough)
        DomainEntitySet<QbdtPayrollItemInfo> contributionItems =
                qbdtPayrollItemInfos.find(QbdtPayrollItemInfo.CompanyPayrollItem().PayrollItem().PayrollItemCode().in(PayrollItem.CONTRIBUTION_CODES))
                        .sort(QbdtPayrollItemInfo.CompanyPayrollItem().SourcePayrollItemId());
        for (QbdtPayrollItemInfo contributionItem : contributionItems) {
            CompanyPayrollItem companyPayrollItem = contributionItem.getCompanyPayrollItem();
            if(contributionItem.getIsDeleted()) {
                payrollItemResponseMap.put(companyPayrollItem.getSourcePayrollItemId(), null);
            } else {
                payrollItemResponseMap.put(companyPayrollItem.getSourcePayrollItemId(), new PayrollItemResponse(contributionItem, companyPayrollItem));
            }
        }

        DomainEntitySet<PayrollItemTaxableTo> payrollItemTaxableTos = QbdtPayrollItemInfo.findPayrollItemTaxableToWithGreaterToken(pCompany, pSyncToken);
        for (PayrollItemTaxableTo payrollItemTaxableTo : payrollItemTaxableTos) {
            PayrollItemResponse payrollItemResponse = payrollItemResponseMap.get(payrollItemTaxableTo.getCompanyPayrollItem().getSourcePayrollItemId());
            if(payrollItemResponse != null) {
                payrollItemResponse.addTaxableTo(payrollItemTaxableTo.getCompanyLaw().getSourceId());
            }
        }

        // SpcfCalendar currentQuarterStart = CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime());
        DomainEntitySet<CompanyLaw> laws = QbdtPayrollItemInfo.findCompanyLawsWithGreaterToken(pCompany, pSyncToken);
        for (CompanyLaw law : laws) {

            DomainEntitySet<CompanyLawRate> lawRates = QbdtPayrollItemInfo.findValidLawRates(law);
            for (int i = 0 ; i < lawRates.size() ; i++) {
                CompanyLawRate lawRate = lawRates.get(i);

                PayrollItemResponse payrollItemResponse = payrollItemResponseMap.get(lawRate.getCompanyLaw().getSourceId());
                if(payrollItemResponse != null) {
                    // This is a very specific fix for PSP-2476 because QB is weird.
                    // If this is the newest SUI law, it is not for Q1 of following year, and it is unexpired.
                    // Also, only sunset this current rate when the rate value differs from the next (older) in the set.
                    // Not making changes in this block for PSP-3156 Fixed Rates because this rate is specifically for
                    //  SUI rate and that cannot have a fixed value
                    if ( i == 0 && lawRates.size() > 1 &&
                            PaymentTemplateCategory.SUI.equals(lawRate.getCompanyLaw().getLaw().getPaymentTemplate().getCategory()) &&
                            lawRate.calculateExpirationDate() == null &&
                            !(PSPDate.getPSPTime().before(lawRate.getEffectiveDate()) && lawRate.getEffectiveDate() != null && lawRate.getEffectiveDate().getMonth() == 1) &&
                            lawRate.getRate() != lawRates.get(1).getRate()) {
                        // Treat this rate as expired so that it ends up in the sunset list.
                        SpcfCalendar expirationDate = lawRate.getEffectiveDate().copy();
                        expirationDate.addMonths(3);
                        expirationDate.addDays(-1);
                        // Just in case we cross the DST boundary, re-generate the expiration date as a localized date.
                        SpcfCalendar localizedExpirationDate = SpcfCalendar.createInstance(expirationDate.getYear(),
                                                                                           expirationDate.getMonth(),
                                                                                           expirationDate.getDay(),
                                                                                           SpcfTimeZone.getLocalTimeZone());
                        payrollItemResponse.addRate(lawRate, localizedExpirationDate);

                        // Create another unexpired company law rate with the same rate value.
                        payrollItemResponse.addRate(lawRate, null);
                    } else {
                        payrollItemResponse.addRate(lawRate, lawRate.calculateExpirationDate());
                    }
                }
            }
        }

        List<QbdtPayrollItemInfo> ratePushItems = new ArrayList<QbdtPayrollItemInfo>();
        for (String payrollItemId : payrollItemResponseMap.keySet()) {
            PayrollItemResponse payrollItemResponse = payrollItemResponseMap.get(payrollItemId);
            if(payrollItemResponse == null) {
                pIPAYROLLUPDATEDATA.getIPITEMDELID().add(payrollItemId);
            } else {
                pIPAYROLLUPDATEDATA.getIPITEMMOD().add(payrollItemResponse.getIPITEM());
                if (payrollItemResponse.getRatePushItem() != null) {
                    ratePushItems.add(payrollItemResponse.getRatePushItem());
                }
            }
        }
        return ratePushItems;
    }

    private void addSyncEmployees(Company pCompany, long pSyncToken, IPAYROLLUPDATEDATA pIPAYROLLUPDATEDATA) {
        Map<String, EmployeeResponse> employeeResponseMap = new HashMap<String, EmployeeResponse>();

        // find and add employees
        DomainEntitySet<Employee> employees = QbdtEmployeeInfo.findEmployeesWithGreaterToken(pCompany, pSyncToken);
        if(employees.size() == 0) {
            // there are no employees to recover don't try the rest of the queries
            return;
        }

        // limit the number of employees in one go
        int maxLimit = SystemParameter.findIntValue(SystemParameter.Code.QBDT_MAX_EMPLOYEES_PER_DR, 500);
        if(employees.size() > maxLimit && pCompany.isCompanyOnService(ServiceCode.Tax)) {
            throw new MaxLimitExceededException("Employee max limit exceeded - found " + employees.size() + " employees");
        }

        for (Employee employee : employees) {
            QbdtEmployeeInfo qbdtEmployeeInfo = employee.getQbdtEmployeeInfo();
            if(qbdtEmployeeInfo.getIsDeleted()) {
                employeeResponseMap.put(employee.getSourceEmployeeId(), null);
            } else {
                employeeResponseMap.put(employee.getSourceEmployeeId(), new EmployeeResponse(employee, mAppVerion));
            }
        }

        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = QbdtEmployeeInfo.findEmployeeBankAccountsWithGreaterToken(pCompany, pSyncToken);
        for (EmployeeBankAccount employeeBankAccount : employeeBankAccounts) {
            EmployeeResponse employeeResponse = employeeResponseMap.get(employeeBankAccount.getEmployee().getSourceEmployeeId());
            if(employeeResponse != null) {
                employeeResponse.addBankAccount(employeeBankAccount);
            }
        }

        DomainEntitySet<EmployeePayrollItem> employeePayrollItems = QbdtEmployeeInfo.findEmployeePayrollItemsWithGreaterToken(pCompany, pSyncToken);
        for (EmployeePayrollItem employeePayrollItem : employeePayrollItems) {
            EmployeeResponse employeeResponse = employeeResponseMap.get(employeePayrollItem.getEmployee().getSourceEmployeeId());
            if(employeeResponse != null) {
                employeeResponse.addPayrollItem(employeePayrollItem);
            }
        }

        DomainEntitySet<EmployeeCustomField> employeeCustomFields = QbdtEmployeeInfo.findEmployeeCustomFieldsWithGreaterToken(pCompany, pSyncToken);
        for (EmployeeCustomField employeeCustomField : employeeCustomFields) {
            EmployeeResponse employeeResponse = employeeResponseMap.get(employeeCustomField.getEmployee().getSourceEmployeeId());
            if(employeeResponse != null) {
                employeeResponse.addCustomField(employeeCustomField);
            }
        }

        DomainEntitySet<EmployeeTax> employeeTaxes = QbdtEmployeeInfo.findEmployeeTaxesWithGreaterToken(pCompany, pSyncToken);
        for (EmployeeTax employeeTax : employeeTaxes) {
            EmployeeResponse employeeResponse = employeeResponseMap.get(employeeTax.getEmployee().getSourceEmployeeId());
            if(employeeResponse != null) {
                employeeResponse.addTax(employeeTax);
            }
        }

        for (String employeeId : employeeResponseMap.keySet()) {
            EmployeeResponse employeeResponse = employeeResponseMap.get(employeeId);
            if(employeeResponse == null) {
                pIPAYROLLUPDATEDATA.getIEMPDELID().add(employeeId);
            } else {
                pIPAYROLLUPDATEDATA.getIEMPMOD().add(employeeResponse.getIEMP());
            }
        }
    }

    private void addSyncPayrollTransactions(Company pCompany, long pSyncToken, IPAYROLLUPDATEDATA pIPAYROLLUPDATEDATA) {
        DomainEntitySet<PriorPaymentSubmission> priorPaymentSubmissions = new DomainEntitySet<PriorPaymentSubmission>();
        DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = new DomainEntitySet<CompanyAdjustmentSubmission>();
        Set<String> deleteIds = new HashSet<String>();

        // for disaster recovery we only send back the last year or year and a half worth of data
        // the date is not included in the query because there are 4 tables to check and the query would be very messy
        SpcfCalendar transactionDate = null;
        if(pSyncToken <= pCompany.getServiceStartToken()) {
            if(mIsAssistedRequest) {
                SpcfCalendar today = PSPDate.getPSPTime().copy();
                if(today.getMonth() < 6) {
                    transactionDate = SpcfCalendar.createInstance(today.getYear()-1, 1, 1, SpcfTimeZone.getLocalTimeZone());
                } else {
                    transactionDate = SpcfCalendar.createInstance(today.getYear(), 1, 1, SpcfTimeZone.getLocalTimeZone());
                }
            } else if(pCompany.isCompanyOnService(ServiceCode.DirectDeposit) && pCompany.getCompanyService(ServiceCode.DirectDeposit).getStatusCd() == ServiceSubStatusCode.ActiveCurrent) {
                // for DIY DD don't send anything back, after they are active
                return;
            }
        }

        DomainEntitySet<QbdtTransactionInfo> qbdtTransactionInfos = QbdtTransactionInfo.findQbdtTransactionInfosGreaterToken(pCompany, pSyncToken);
        for (QbdtTransactionInfo qbdtTransactionInfo : qbdtTransactionInfos) {
            // prior payment submission
            if(qbdtTransactionInfo.getPriorPaymentSubmission() != null) {
                PriorPaymentSubmission priorPaymentSubmission = qbdtTransactionInfo.getPriorPaymentSubmission();
                if(transactionDate != null &&
                        priorPaymentSubmission.getQbdtTransactionInfoCollection().size() > 0 &&
                        priorPaymentSubmission.getQbdtTransactionInfoCollection().get(0).getMoneyMovementTransaction() != null &&
                        priorPaymentSubmission.getQbdtTransactionInfoCollection().get(0).getMoneyMovementTransaction().getInitiationDate().before(transactionDate)) {
                    continue;
                }

                if(qbdtTransactionInfo.getIsDeleted()) {
                    deleteIds.add(qbdtTransactionInfo.getPriorPaymentSubmission().getSourceId());
                } else {
                    priorPaymentSubmissions.add(qbdtTransactionInfo.getPriorPaymentSubmission());
                }
            }
            // prior payment / company adjustment / dd return / funds transfer
            else if(qbdtTransactionInfo.getQbdtPayrollTransaction() != null &&
                    qbdtTransactionInfo.getQbdtPayrollTransaction().getCompanyAdjustmentSubmission() == null &&
                    qbdtTransactionInfo.getQbdtPayrollTransaction().getPriorPaymentSubmission() == null){
                if(transactionDate != null && qbdtTransactionInfo.getQbdtPayrollTransaction().getTransactionDate().before(transactionDate)) {
                    continue;
                }

                if(qbdtTransactionInfo.getIsDeleted()) {
                    deleteIds.add(qbdtTransactionInfo.getQbdtPayrollTransaction().getSourceId());
                } else if(qbdtTransactionInfo.getQbdtPayrollTransaction().getCompanyAdjustmentSubmission() != null) {
                    companyAdjustmentSubmissions.add(qbdtTransactionInfo.getQbdtPayrollTransaction().getCompanyAdjustmentSubmission());
                } else if(qbdtTransactionInfo.getQbdtPayrollTransaction().getPriorPaymentSubmission() != null) {
                    priorPaymentSubmissions.add(qbdtTransactionInfo.getQbdtPayrollTransaction().getPriorPaymentSubmission());
                } else {
                    pIPAYROLLUPDATEDATA.getIPAYROLLTXMOD().add(new PayrollTransactionResponse(qbdtTransactionInfo.getQbdtPayrollTransaction()).getIPAYROLLTX());
                }
            }
            // company adjustment
            else if(qbdtTransactionInfo.getCompanyAdjustmentSubmission() != null &&
                    qbdtTransactionInfo.getCompanyAdjustmentSubmission().getOriginalSubmission() == null) {
                if(transactionDate != null && qbdtTransactionInfo.getCompanyAdjustmentSubmission().getSubmissionDate().before(transactionDate)) {
                    continue;
                }

                if(qbdtTransactionInfo.getIsDeleted()) {
                    deleteIds.add(qbdtTransactionInfo.getCompanyAdjustmentSubmission().getSourceId());
                } else {
                    companyAdjustmentSubmissions.add(qbdtTransactionInfo.getCompanyAdjustmentSubmission());
                }
            }
            // liability check
            else if(qbdtTransactionInfo.getLiabilityCheck() != null) {
                if(transactionDate != null && qbdtTransactionInfo.getLiabilityCheck().getTransactionDate().before(transactionDate)) {
                    continue;
                }

                if(qbdtTransactionInfo.getIsDeleted()) {
                    pIPAYROLLUPDATEDATA.getIPAYROLLTXDELID().add(qbdtTransactionInfo.getLiabilityCheck().getSourceId());
                } else {
                    pIPAYROLLUPDATEDATA.getIPAYROLLTXMOD().add(new PayrollTransactionResponse(qbdtTransactionInfo.getLiabilityCheck()).getIPAYROLLTX());
                }
            }
        }

        for (String deleteId : deleteIds) {
            pIPAYROLLUPDATEDATA.getIPAYROLLTXDELID().add(deleteId);
        }

        for (PriorPaymentSubmission priorPaymentSubmission : priorPaymentSubmissions) {
            pIPAYROLLUPDATEDATA.getIPAYROLLTXMOD().add(new PayrollTransactionResponse(priorPaymentSubmission).getIPAYROLLTX());
        }

        for (CompanyAdjustmentSubmission companyAdjustmentSubmission : companyAdjustmentSubmissions) {
            pIPAYROLLUPDATEDATA.getIPAYROLLTXMOD().add(new PayrollTransactionResponse(companyAdjustmentSubmission).getIPAYROLLTX());
        }

        Collections.sort(pIPAYROLLUPDATEDATA.getIPAYROLLTXMOD(), new Comparator<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX>() {
            public int compare(com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX o1, com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX o2) {
                if(Integer.parseInt(o1.getIPAYROLLTXID()) > Integer.parseInt(o2.getIPAYROLLTXID())) {
                    return 1;
                } else if(Integer.parseInt(o1.getIPAYROLLTXID()) < Integer.parseInt(o2.getIPAYROLLTXID())) {
                    return -1;
                }
                return 0;
            }
        });
    }

    private void addSyncPaychecks(Company pCompany, long pSyncToken, IPAYROLLUPDATEDATA pIPAYROLLUPDATEDATA) {
        // for disaster recovery we only send back the last year or year and a half worth of data
        SpcfCalendar paycheckDate = null;
        if(pSyncToken <= pCompany.getServiceStartToken() && mIsAssistedRequest) {
            SpcfCalendar today = PSPDate.getPSPTime().copy();
            if (today.getMonth() < 6) {
                paycheckDate = SpcfCalendar.createInstance(today.getYear() - 1, 1, 1, SpcfTimeZone.getLocalTimeZone());
            } else {
                paycheckDate = SpcfCalendar.createInstance(today.getYear(), 1, 1, SpcfTimeZone.getLocalTimeZone());
            }
        }

        // build paycheck response in parts to avoid trips to the DB
        DomainEntitySet<Paycheck> paychecks = Paycheck.findPaychecksWithGreaterToken(pCompany, pSyncToken, paycheckDate, mIsAssistedRequest);
        if(paychecks.size() == 0) {
            // don't try the other queries if no paychecks were found for the first query
            return;
        }

        // limit the number of paychecks in one go but only for Assisted companies
        int maxLimit = SystemParameter.findIntValue(SystemParameter.Code.QBDT_MAX_PAYCHECKS_PER_DR, 1000);
        if(paychecks.size() > maxLimit && pCompany.isCompanyOnService(ServiceCode.Tax)) {
            throw new MaxLimitExceededException("Paycheck max limit exceeded - found " + paychecks.size() + " paychecks");
        }

        // Source paycheck id -> paycheck response
        Map<Long, PaycheckResponse> paycheckResponses = new TreeMap<Long, PaycheckResponse>();

        // paycheck info first
        for (Paycheck paycheck : paychecks) {
            paycheckResponses.put(Long.parseLong(paycheck.getSourcePaycheckId()), new PaycheckResponse(paycheck, mAppVerion));
        }

        // then paycheck detail
        DomainEntitySet<Compensation> compensations = Paycheck.findPaycheckCompensationsWithGreaterToken(pCompany, pSyncToken, paycheckDate, mIsAssistedRequest);
        for (Compensation compensation : compensations) {
            Paycheck paycheck = compensation.getPaycheck();
            PaycheckResponse paycheckResponse = paycheckResponses.get(Long.parseLong(paycheck.getSourcePaycheckId()));
            if(paycheckResponse == null) {
                paycheckResponse = new PaycheckResponse(paycheck, mAppVerion);
                paycheckResponses.put(Long.parseLong(paycheck.getSourcePaycheckId()), paycheckResponse);
            }
            paycheckResponse.getCompensations().add(compensation);
        }

        DomainEntitySet<Deduction> deductions = Paycheck.findPaycheckDeductionsWithGreaterToken(pCompany, pSyncToken, paycheckDate, mIsAssistedRequest);
        for (Deduction deduction : deductions) {
            Paycheck paycheck = deduction.getPaycheck();
            PaycheckResponse paycheckResponse = paycheckResponses.get(Long.parseLong(paycheck.getSourcePaycheckId()));
            if(paycheckResponse == null) {
                paycheckResponse = new PaycheckResponse(paycheck, mAppVerion);
                paycheckResponses.put(Long.parseLong(paycheck.getSourcePaycheckId()), paycheckResponse);
            }
            paycheckResponse.getDeductions().add(deduction);
        }

        DomainEntitySet<EmployerContribution> employerContributions = Paycheck.findPaycheckEmployerContributionsWithGreaterToken(pCompany, pSyncToken, paycheckDate, mIsAssistedRequest);
        for (EmployerContribution employerContribution : employerContributions) {
            Paycheck paycheck = employerContribution.getPaycheck();
            PaycheckResponse paycheckResponse = paycheckResponses.get(Long.parseLong(paycheck.getSourcePaycheckId()));
            if(paycheckResponse == null) {
                paycheckResponse = new PaycheckResponse(paycheck, mAppVerion);
                paycheckResponses.put(Long.parseLong(paycheck.getSourcePaycheckId()), paycheckResponse);
            }
            paycheckResponse.getEmployerContributions().add(employerContribution);
        }

        DomainEntitySet<Tax> taxes = Paycheck.findPaycheckTaxesWithGreaterToken(pCompany, pSyncToken, paycheckDate, mIsAssistedRequest);
        for (Tax tax : taxes) {
            Paycheck paycheck = tax.getPaycheck();
            PaycheckResponse paycheckResponse = paycheckResponses.get(Long.parseLong(paycheck.getSourcePaycheckId()));
            if(paycheckResponse == null) {
                paycheckResponse = new PaycheckResponse(paycheck, mAppVerion);
                paycheckResponses.put(Long.parseLong(paycheck.getSourcePaycheckId()), paycheckResponse);
            }
            paycheckResponse.getTaxes().add(tax);
        }

        DomainEntitySet<PaycheckSplit> paycheckSplits = Paycheck.findPaycheckSplitsWithGreaterToken(pCompany, pSyncToken, paycheckDate, mIsAssistedRequest);
        for (PaycheckSplit paycheckSplit : paycheckSplits) {
            Paycheck paycheck = paycheckSplit.getPaycheck();
            PaycheckResponse paycheckResponse = paycheckResponses.get(Long.parseLong(paycheck.getSourcePaycheckId()));
            if(paycheckResponse == null) {
                paycheckResponse = new PaycheckResponse(paycheck, mAppVerion);
                paycheckResponses.put(Long.parseLong(paycheck.getSourcePaycheckId()), paycheckResponse);
            }
            paycheckResponse.getPaycheckSplits().add(paycheckSplit);
        }

        for (PaycheckResponse paycheckResponse : paycheckResponses.values()) {
            pIPAYROLLUPDATEDATA.getIPAYCHKMOD().add(paycheckResponse.getIPAYCHKMOD());
        }
    }
    private ProcessResult<List<PayrollRun>> processAssistedPayrollRequest(OFX pRequestOFX) {
        IPAYROLLRQ ipayrollrq = pRequestOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();

        logger.info("Started processing payroll request for PSID=" + mCompany.getSourceCompanyId());

        StopWatch timer = StopWatch.startTimer();

        if (debugLogs) {
            logger.info("Started company processing for PSID=" + mCompany.getSourceCompanyId());
        }
        processResult.merge(processCompany(ipayrollrq, pRequestOFX.getSIGNONMSGSRQV1().getSONRQ()));
        if (debugLogs){
            logger.info("Finished company processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        }
        if (!processResult.isSuccess()) {
            return processResult;
        }

        if (debugLogs) {
            timer.start();
            logger.info("Started Payroll Items processing for PSID=" + mCompany.getSourceCompanyId());
        }
        processResult.merge(processPayrollItems(ipayrollrq));
        if (debugLogs) {
            logger.info("Finished payroll Items processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME" + timer.getElapsedSeconds());
        }

        if (!processResult.isSuccess()) {
            return processResult;
        }

        if (debugLogs) {
            timer.start();
            logger.info("Started employees processing for PSID=" + mCompany.getSourceCompanyId());
        }
        processResult.merge(processEmployees(ipayrollrq));
        if (debugLogs){
            logger.info("Finished employee processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        }
        if (!processResult.isSuccess()) {
            return processResult;
        }

        if (debugLogs){
            timer.start();
            logger.info("Started Payroll Transaction Processing for PSID=" + mCompany.getSourceCompanyId());
        }
        processResult.merge(processPayrollTransactions(ipayrollrq));
        if (debugLogs){
            logger.info("Finished Payroll Transaction processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        }
        if (!processResult.isSuccess()) {
            return processResult;
        }

        if (debugLogs) {
            timer.start();
            logger.info("Started payroll processing for PSID=" + mCompany.getSourceCompanyId());
        }
        ProcessResult<List<PayrollRun>> payrollsPR = processPayrolls(ipayrollrq);
        if (debugLogs) {
            logger.info("Finished payroll processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        }
        processResult.merge(payrollsPR);
        if (!payrollsPR.isSuccess()) {
            return processResult;
        }
        processResult.setResult(payrollsPR.getResult());

        if (debugLogs){
            timer.start();
            logger.info("Started paystub for PSID=" + mCompany.getSourceCompanyId());
        }
        processResult.merge(processPaystubs(ipayrollrq, payrollsPR.getResult()));
        if (debugLogs){
            logger.info("Finished paystub processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        }

        if (!processResult.isSuccess()) {
            return processResult;
        }

        if (debugLogs){
            timer.start();
            logger.info("Started delete processing for PSID=" + mCompany.getSourceCompanyId());
        }
        processResult.merge(processDeletes(ipayrollrq));
        if (debugLogs){
            logger.info("Finished delete processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        }
        if (!processResult.isSuccess()) {
            return processResult;
        }

        if (debugLogs) {
            timer.start();
            logger.info("Started disburse advise processing for PSID=" + mCompany.getSourceCompanyId());
        }
        processResult.merge(processDisburseAdvice(ipayrollrq));
        if (debugLogs) {
            logger.info("Finished disburse advice processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        }

        if (!processResult.isSuccess()) {
            return processResult;
        }

        // for specific versions of QB with DIY services revert any employee source ids that were changed
        OFXAPPVERObject ofxappverObject = new OFXAPPVERObject(pRequestOFX.getSIGNONMSGSRQV1().getSONRQ().getAPPVER());
        if(!mCompany.isCompanyOnService(ServiceCode.Tax) && SystemParameter.findStringValue(SystemParameter.Code.QB_EMPLOYEE_ID_SWAPPING_VERSIONS, "").contains(ofxappverObject.getQBVersionStr())) {
            if (debugLogs){
                timer.start();
                logger.info("Started revert Employee ID processing for PSID=" + mCompany.getSourceCompanyId());
            }
            processResult.merge(revertEmployeeSourceIds());
            if (debugLogs){
                logger.info("Finished revert employee sourceID processing for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
            }
        }

        logger.info("Finished processing payroll request for PSID=" + mCompany.getSourceCompanyId() + ". ELAPSED_TIME=" + timer.getElapsedSeconds());
        return processResult;
    }

    private ProcessResult processV3PayrollRequest(OFX pRequestOFX){
        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();
        mCompany = Company.findCompany(mPSID, SourceSystemCode.QBDT);
        boolean isDDCompany = mCompany.isCompanyOnService((ServiceCode.DirectDeposit));

        // Flag from qbdt flows for DIY+DD, non vmp and non assisted companies.
        // CloudV3 is only enabled for DIY DD companies.
        if (isDDCompany) {
            CompanyService companyService = mCompany.getCompanyService(ServiceCode.CloudV3);
            if( companyService != null && companyService.getStatusCd().equals(ServiceSubStatusCode.Terminated)){
                logger.info("Event=CloudV3Service, SubEvent=TerminateCloudV3, PSID="+mCompany.getSourceCompanyId());
                return processResult;
            }
            else if (pRequestOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ() != null &&
                    pRequestOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getSYNCCOMPLETEINFO()==null
            ) // tag is not present in ofx
            {
                if (companyService != null && companyService.isActive()) {
                    logger.info("Event=CloudV3Service, SubEvent=DeactivateCloudV3, PSID="+mCompany.getSourceCompanyId());
                    processResult.merge(PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, mCompany.getSourceCompanyId(), ServiceCode.CloudV3));
                }
            } else {
                CloudV3ServiceInfoDTO serviceInfoDTO = new CloudV3ServiceInfoDTO();
                serviceInfoDTO.setServiceStartDate(PSPDate.getPSPTime());
                if (companyService == null) {
                    logger.info("Event=CloudV3Service, SubEvent=AddCloudV3, PSID="+mCompany.getSourceCompanyId());
                    processResult.merge(PayrollServices.companyManager.addService(SourceSystemCode.QBDT, mCompany.getSourceCompanyId(), serviceInfoDTO));
                } else {
                    //if cloudV3 is active, no action
                    //if cloudV3 is added and is in deactivated status then reactivate service
                    if (!companyService.isActive()) // CloudV3 service is deactivated
                    {
                        logger.info("Event=CloudV3Service, SubEvent=UpdateAndReactivateCloudV3, PSID="+mCompany.getSourceCompanyId());
                        ProcessResult<CompanyService> updateResult =PayrollServices.companyManager.updateService(
                                SourceSystemCode.QBDT,
                                mCompany.getSourceCompanyId(),
                                serviceInfoDTO);
                        processResult.merge(PayrollServices.companyManager.reactivateService(
                                SourceSystemCode.QBDT,
                                mCompany.getSourceCompanyId(),
                                ServiceCode.CloudV3));
                        processResult.merge(updateResult);
                    }
                }
            }
        }
        return processResult;
    }

    private ProcessResult revertEmployeeSourceIds() {
        ProcessResult processResult = new ProcessResult();
        for (EmployeeDTO employeeDTO : mEmployeeProcessor.getEmployeeUpdates()) {
            employeeDTO.setEmployeeId(employeeDTO.getOriginalSourceId());
            processResult.merge(PayrollServices.employeeManager.updateEmployee(mCompany.getSourceSystemCd(),
                                                                               mCompany.getSourceCompanyId(),
                                                                               employeeDTO));
        }

        return processResult;
    }

    private ProcessResult<List<PayrollRun>> processAssistedBalanceFile(OFX pRequestOFX) {
        mCompany = Company.findCompany(mPSID, SourceSystemCode.QBDT);

        if (mCompany.hasSentBalanceFile()) {
            throw new RuntimeException("Second BALF received");
        }

        CompanyService companyService = mCompany.getCompanyService(ServiceCode.Tax);

        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();

        processResult = processAssistedPayrollRequest(pRequestOFX);

        CompanyEvent.createBalanceFileReceivedEvent(mCompany, companyService);

        if (companyService != null) {
            companyService.updateCompanyServiceStatus(ServiceSubStatusCode.ActiveCurrent);
        }

        IPAYROLLRQ ipayrollrq = pRequestOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        Date quarterToStartDate = QBOFX.mapOFXStringToDate(ipayrollrq.getICOINFOMOD().getIDTFILEQTRSTART());
        SpcfCalendar quarterStartDate = CalendarUtils.convertToSpcfCalendar(quarterToStartDate);
        ProcessResult<PayrollRun> overPaymentProcess = PayrollServices.transmissionManagerSecondary.handleBALFOverPaymentTax(mSourceTransmissionId, quarterStartDate);
        if (!overPaymentProcess.isSuccess()) {
            processResult.merge(overPaymentProcess);
            return processResult;
        }
        if(overPaymentProcess.getResult() != null){
            processResult.getResult().add(overPaymentProcess.getResult());
        }

        return processResult;
    }

    private ProcessResult processCompany(IPAYROLLRQ pIPAYROLLRQ, SONRQ pSONRQ) {
        return mCompanyProcessor.processCompany(pIPAYROLLRQ.getICOINFOMOD(), pSONRQ);
    }

    private ProcessResult processPayrollItems(IPAYROLLRQ pIPAYROLLRQ) {
        ProcessResult processResult = new ProcessResult();

        // add new payroll items
        mConnectionInformation.setNewPayrollItemsCount(pIPAYROLLRQ.getIPITEM().size());
        mConnectionInformation.setNewPayrollItemsProcessingStart(PSPDate.getPSPTime());
        processResult.merge(mPayrollItemProcessor.processPayrollItems(pIPAYROLLRQ.getIPITEM()));
        mConnectionInformation.setNewPayrollItemsProcessingEnd(PSPDate.getPSPTime());
        if (!processResult.isSuccess()) {
            return processResult;
        }

        // update payroll items
        mConnectionInformation.setPayrollItemModsCount(pIPAYROLLRQ.getIPITEMMOD().size());
        mConnectionInformation.setPayrollItemModsProcessingStart(PSPDate.getPSPTime());
        processResult.merge(mPayrollItemProcessor.processPayrollItems(pIPAYROLLRQ.getIPITEMMOD()));
        mConnectionInformation.setPayrollItemModsProcessingEnd(PSPDate.getPSPTime());
        if (!processResult.isSuccess()) {
            return processResult;
        }

        return processResult;
    }

    private Set<String> collectEEIdsInPaycheck(IPAYROLLRQ pIPAYROLLRQ) {
        Set<String> eeids = new HashSet<String>();

        for (IPAYROLLRUN payrollRun : pIPAYROLLRQ.getIPAYROLLRUN()) {
            for (IPAYCHK paycheck : payrollRun.getIPAYCHK()) {
                eeids.add(paycheck.getIEMPID());
            }
            for (IPAYCHK paycheck : payrollRun.getIPAYCHKMOD()) {
                eeids.add(paycheck.getIEMPID());
            }
        }

        return eeids;
    }

    private void excludeNonRelevantObjs(List<IEMP> pOriginalEEList, Set<String> pInterestedEEIds) {
        for (Iterator<IEMP> iterator = pOriginalEEList.iterator(); iterator.hasNext(); ) {
            IEMP next = iterator.next();
            if (!pInterestedEEIds.contains(next.getIEMPID())) {
                iterator.remove();
            }
        }
    }

    private ProcessResult processEmployees(IPAYROLLRQ pIPAYROLLRQ) {
        ProcessResult processResult = new ProcessResult();
        List<IEMP> newEEs = pIPAYROLLRQ.getIEMP();
        List<IEMP> modEEs = pIPAYROLLRQ.getIEMPMOD();

        for (IEMP iemp : newEEs) {

	    	iemp.setISESSIONID(pIPAYROLLRQ.getISESSIONID());
        }
        for (IEMP modiemp : modEEs) {

	    	modiemp.setISESSIONID(pIPAYROLLRQ.getISESSIONID());
        }

        if (!mIsAssistedRequest) {
            // PSRV003961: a temp work around to prune EMP/EMPMOD which are not for any PAYCHECK in the same ofx
            // this additional check is for DIY customers only
            // PSP-2504: loosen the throttler for CloudV2
            if (mCompany.isCompanyOnService(ServiceCode.CloudV2)) {
                int alertLevel = SystemParameter.findIntValue(SystemParameter.Code.NUM_EE_PER_OFX_ALERT_LEVEL, 2000);
                if ((pIPAYROLLRQ.getIEMP().size() + pIPAYROLLRQ.getIEMPMOD().size()) > alertLevel) {
                    logger.info("The number of EMP and EMPMOD in the ofx exceeds the alert level of CloudV2. PSID=" + mPSID);
                    Set<String> eeids = collectEEIdsInPaycheck(pIPAYROLLRQ);
                    excludeNonRelevantObjs(newEEs, eeids);
                    excludeNonRelevantObjs(modEEs, eeids);
                }
            } else {
                if ((pIPAYROLLRQ.getIEMP().size() + pIPAYROLLRQ.getIEMPMOD().size()) > 100) {
                    logger.info("The number of EMP and EMPMOD in the ofx exceeds the alert level. PSID=" + mPSID);
                    Set<String> eeids = collectEEIdsInPaycheck(pIPAYROLLRQ);
                    excludeNonRelevantObjs(newEEs, eeids);
                    excludeNonRelevantObjs(modEEs, eeids);
                }
            }
        }

        // add new employees
        mConnectionInformation.setNewEmployeesCount(newEEs.size());
        mConnectionInformation.setNewEmployeesProcessingStart(PSPDate.getPSPTime());
        processResult.merge(mEmployeeProcessor.processEmployees(newEEs));
        mConnectionInformation.setNewEmployeesProcessingEnd(PSPDate.getPSPTime());
        if (!processResult.isSuccess()) {
            return processResult;
        }

        // update employees
        mConnectionInformation.setEmployeeModsCount(modEEs.size());
        mConnectionInformation.setEmployeeModsProcessingStart(PSPDate.getPSPTime());
        processResult.merge(mEmployeeProcessor.processEmployees(modEEs));
        mConnectionInformation.setEmployeeModsProcessingEnd(PSPDate.getPSPTime());
        if (!processResult.isSuccess()) {
            return processResult;
        }

        return processResult;
    }

    private ProcessResult<List<PayrollRun>> processPayrollTransactions(IPAYROLLRQ pIPAYROLLRQ) {
        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();
        processResult.setResult(new ArrayList<PayrollRun>());

        // add new non adjustment payroll transactions
        mConnectionInformation.setNewPayrollTransactionsCount(pIPAYROLLRQ.getIPAYROLLTX().size());
        mConnectionInformation.setNewPayrollTransactionsProcessingStart(PSPDate.getPSPTime());
        moveModToNewForLIABCHKandPRIORPMT(pIPAYROLLRQ, mConnectionInformation.isBalanceFile());
        processResult.merge(mPayrollTransactionProcessor.addNonAdjustmentPayrollTransactions(pIPAYROLLRQ.getIPAYROLLTX()));
        mConnectionInformation.setNewPayrollTransactionsProcessingEnd(PSPDate.getPSPTime());
        if (!processResult.isSuccess()) {
            return processResult;
        }

        // update payroll transactions
        mConnectionInformation.setPayrollTransactionModsCount(pIPAYROLLRQ.getIPAYROLLTXMOD().size());
        mConnectionInformation.setPayrollTransactionModsProcessingStart(PSPDate.getPSPTime());
        ProcessResult<List<PayrollRun>> updatePayrollTransactionsPR =
                mPayrollTransactionProcessor.updatePayrollTransactions(pIPAYROLLRQ.getIPAYROLLTXMOD());
        mConnectionInformation.setPayrollTransactionModsProcessingEnd(PSPDate.getPSPTime());
        if (!updatePayrollTransactionsPR.isSuccess()) {
            processResult.merge(updatePayrollTransactionsPR);
            return processResult;
        }
        processResult.getResult().addAll(updatePayrollTransactionsPR.getResult());

        return processResult;
    }

    private ProcessResult<List<PayrollRun>> processPayrolls(IPAYROLLRQ pIPAYROLLRQ) {
        // process payroll submissions and updates
        mConnectionInformation.setPayrollProcessingStart(PSPDate.getPSPTime());

        List<IPAYROLLRUN> payrollruns=pIPAYROLLRQ.getIPAYROLLRUN();

        for (IPAYROLLRUN ipayrollrun : payrollruns) {
        	ipayrollrun.setISESSIONID(pIPAYROLLRQ.getISESSIONID());
        }

        ProcessResult<List<PayrollRun>> processResult =
                mPayrollProcessor.processPayrolls(payrollruns,
                                                  mPayrollTransactionProcessor.getNewLiabilityAdjustments(pIPAYROLLRQ.getIPAYROLLTX()));
        mConnectionInformation.setPayrollProcessingEnd(PSPDate.getPSPTime());
        return processResult;
    }

    private ProcessResult<List<Paystub>> processPaystubs(IPAYROLLRQ pIPAYROLLRQ, List<PayrollRun> pPayrollRuns) {
        // to-do
        //mConnectionInformation.setPayrollProcessingStart(PSPDate.getPSPTime());
        ProcessResult<List<Paystub>> processResult =
                mPaystubProcessor.processPaystubs(pIPAYROLLRQ.getIPAYROLLRUN(), pPayrollRuns);
        //mConnectionInformation.setPayrollProcessingEnd(PSPDate.getPSPTime());
        return processResult;
    }

    private ProcessResult<List<PayrollRun>> processDeletes(IPAYROLLRQ pIPAYROLLRQ) {
        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();
        processResult.setResult(new ArrayList<PayrollRun>());
        mConnectionInformation.setDeletesProcessingStart(PSPDate.getPSPTime());

        // delete employees
        mConnectionInformation.setEmployeeDeletesCount(pIPAYROLLRQ.getIEMPDELID().size());
        processResult.merge(mEmployeeProcessor.deleteEmployees(pIPAYROLLRQ.getIEMPDELID()));
        if (!processResult.isSuccess()) {
            return processResult;
        }

        // delete payroll items
        mConnectionInformation.setPayrollItemDeletesCount(pIPAYROLLRQ.getIPITEMDELID().size());
        processResult.merge(mPayrollItemProcessor.deletePayrollItems(pIPAYROLLRQ.getIPITEMDELID()));
        if (!processResult.isSuccess()) {
            return processResult;
        }

        // delete paycheck for Symphony
        mConnectionInformation.setPaycheckDeletesCount(pIPAYROLLRQ.getIPITEMDELID().size());
        processResult.merge(mPayrollProcessor.deletePaychecks(pIPAYROLLRQ.getIPAYCHKDELID(),pIPAYROLLRQ.getISESSIONID()));
        if (!processResult.isSuccess()) {
            return processResult;
        }

        // delete payroll transactions
        mConnectionInformation.setPayrollTransactionDeletesCount(pIPAYROLLRQ.getIPAYROLLTXDELID().size());
        ProcessResult<List<PayrollRun>> deletePayrollTransactionsPR =
                mPayrollTransactionProcessor.deletePayrollTransactions(pIPAYROLLRQ.getIPAYROLLTXDELID());
        if (!deletePayrollTransactionsPR.isSuccess()) {
            processResult.merge(deletePayrollTransactionsPR);
            return processResult;
        }
        processResult.getResult().addAll(deletePayrollTransactionsPR.getResult());

        // to-do: delete paystub

        mConnectionInformation.setDeletesProcessingEnd(PSPDate.getPSPTime());
        return processResult;
    }

    private ProcessResult<List<DisburseAdvice>> processDisburseAdvice(IPAYROLLRQ pIPAYROLLRQ) {
        ProcessResult<List<DisburseAdvice>> processResult = new ProcessResult<List<DisburseAdvice>>();

        processResult.merge(mDisburseAdviceProcessor.processDisburseAdvice(pIPAYROLLRQ.getIPAYROLLRUN()));

        return processResult;
    }

    /**
     * Fulfill the request on the server.
     *
     * @param requestStr - OFX Request string.
     * @return - OFX response string.
     * @throws com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedRequestProcessor.ReadTimeOutException
     */
    private String processRequest(String requestStr) throws ReadTimeOutException {

        String responseStr;

        logger.info("Request for PSID=" + mPSID + ".");
        //logger.info("The request is: \r\n" + requestStr);

        if (mISocketManager == null) {
            try {
                mISocketManager = SocketManagerFactory.createISocketManager();
            } catch (Exception e) {
                String errStr = "Error instantiating SocketManagerFactory class: " + e.toString();
                logger.fatal(errStr, e);
                throw new RuntimeException(errStr);
            }
        }

        // Since we are no longer sending requests to the AS/400, this is really just
        // here to allow QB Adapter tests to pass in socket managers to generate QB submission errors.
        try {
            if (mISocketManager != null) {
                responseStr = mISocketManager.processRequest(requestStr);
            } else {
                responseStr = "There is no longer an AS/400";
            }
        } catch (Exception e) {
            String errStr = "Error for company PSID=" + mPSID + ". : " + e.toString();

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);

            if(writer.toString().contains("Read timed out")) {
                throw new ReadTimeOutException(e);
            }

            logger.fatal(errStr, e);
            responseStr = ProcessingErrorHandler.getUnrecoverableProcessingErrorString();
            return responseStr;
        }

        return responseStr;
    }

    public SpcfUniqueId storeUnprocessedAssistedRequest() {
        SpcfUniqueId qbdtUnprocessedRequestId = null;
        try {
            PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.MANUAL);
            Company company = Company.findCompany(mCompany.getSourceCompanyId(), mCompany.getSourceSystemCd());

            QbdtUnprocessedRequest qbdtUnprocessedRequest = new QbdtUnprocessedRequest();
            qbdtUnprocessedRequest.setCompany(company);
            SourceSystemTransmission sourceSystemTransmission = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(mSourceTransmissionId);
            if (Objects.isNull(sourceSystemTransmission)) {
                throw new RuntimeException("Could not find source system transmission with transmission id '" + mSourceTransmissionId + "'");
            }
            qbdtUnprocessedRequest.setSourceSystemTransmissionId(sourceSystemTransmission.getId().toString());
            qbdtUnprocessedRequest.setStatus(QbdtRequestStatus.Processing);
            qbdtUnprocessedRequest = Application.save(qbdtUnprocessedRequest);
            qbdtUnprocessedRequestId = qbdtUnprocessedRequest.getId();
            PayrollServices.commitUnitOfWorkWithSecondary();
        } catch (Throwable t) {
            logger.error("Unable to save unprocessed request transmission (" + mSourceTransmissionId + ") for company " + mCompany.getSourceSystemCompanyId()  + ".", t);
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
        return qbdtUnprocessedRequestId;
    }

    private void updateUnprocessedRequest(SpcfUniqueId pUniqueId, QbdtRequestStatus pQbdtRequestStatus, String pErrorMessage, Throwable pThrowable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        pThrowable.printStackTrace(printWriter);

        pErrorMessage += "\n" + writer.toString();

        updateUnprocessedRequest(pUniqueId, pQbdtRequestStatus, pErrorMessage);
    }

    public void updateUnprocessedRequest(SpcfUniqueId pUniqueId, QbdtRequestStatus pQbdtRequestStatus, String pErrorMessage) {
        if(pUniqueId == null) {
            return;
        }

        if(pQbdtRequestStatus == QbdtRequestStatus.Queued && mConnectionInformation != null) {
            mConnectionInformation.setRequestQueuedToBeReprocessed(true);
        }

        try {
            // truncate error messages to 4000 characters
            String errorMessage;
            if(pErrorMessage != null && pErrorMessage.length() > 4000) {
                errorMessage = pErrorMessage.substring(0, 4000);
            } else {
                errorMessage = pErrorMessage;
            }

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            QbdtUnprocessedRequest qbdtUnprocessedRequest = Application.findById(QbdtUnprocessedRequest.class, pUniqueId);
            qbdtUnprocessedRequest.setStatus(pQbdtRequestStatus);
            qbdtUnprocessedRequest.setErrorMessage(errorMessage);
            Application.save(qbdtUnprocessedRequest);
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error("Unable to save unprocessed request transmission (" + pUniqueId + ") for company PSID=" + mCompany.getSourceSystemCompanyId()  + " with status " + pQbdtRequestStatus + ".", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void deleteUnprocessedRequest(SpcfUniqueId pUniqueId) {
        if(pUniqueId == null) {
            return;
        }

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            QbdtUnprocessedRequest qbdtUnprocessedRequest = Application.findById(QbdtUnprocessedRequest.class, pUniqueId);
            Application.delete(qbdtUnprocessedRequest);
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error("Unable to delete unprocessed request transmission (" + pUniqueId + ") for company PSID=" + mCompany.getSourceSystemCompanyId()  + ".", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private class ReadTimeOutException extends Exception {
        public ReadTimeOutException(Exception e) {
            super(e);
        }
    }

    private class MaxLimitExceededException extends RuntimeException {
        public MaxLimitExceededException(String message) {
            super(message);
        }
    }

    /**
     *
     * @param pIPAYROLLRQ
     */
    public static void moveModToNewForLIABCHKandPRIORPMT(IPAYROLLRQ pIPAYROLLRQ, boolean isBalanceFile) {
        if (isBalanceFile) {
            Iterator ipayrolltxns = pIPAYROLLRQ.getIPAYROLLTXMOD().iterator();
            while (ipayrolltxns.hasNext()) {
                IPAYROLLTX ipayroll = (IPAYROLLTX) ipayrolltxns.next();
                QBOFX.OFXPayrollTransactionTransactionType transactionType = QBOFX.OFXPayrollTransactionTransactionType.valueOf(ipayroll.getIPAYROLLTXTYPE().trim());
                if (QBOFX.OFXPayrollTransactionTransactionType.LIABCHK.equals(transactionType) || QBOFX.OFXPayrollTransactionTransactionType.PRIORPMT.equals(transactionType)) {
                    pIPAYROLLRQ.getIPAYROLLTX().add(ipayroll);
                    ipayrolltxns.remove();
                }
            }
        }
    }
}
