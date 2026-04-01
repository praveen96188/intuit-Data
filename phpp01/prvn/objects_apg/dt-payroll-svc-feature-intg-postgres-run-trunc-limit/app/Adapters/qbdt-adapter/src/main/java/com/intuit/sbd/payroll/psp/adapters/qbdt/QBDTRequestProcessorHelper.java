package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdt.translators.EmployeeTranslator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyEventDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyEventDetailDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.utils.ServiceSubStatusFactory;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 27, 2010
 * Time: 9:07:17 AM
 */
public class QBDTRequestProcessorHelper {
    private static SpcfLogger logger = PayrollServices.getLogger(QBDTRequestProcessorHelper.class);

    public static String createErrorResponseStringAndFinalizeTransmission(String pPSID, String pSourceTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, QBDTProcessResult pQBDTProcessResult, String clientIP) {
        return createErrorResponseStringAndFinalizeTransmission(pPSID, pSourceTransmissionId, pSourceSystemTransmissionDTO, pQBDTProcessResult, clientIP, false);
    }

    public static String createErrorResponseStringAndFinalizeTransmission(String pPSID, String pSourceTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, QBDTProcessResult pQBDTProcessResult, String clientIP, boolean isRetry) {
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX rtnOFX;
        if (pSourceSystemTransmissionDTO == null ||
                pSourceSystemTransmissionDTO.getTransmissionType() == null ||
                pSourceSystemTransmissionDTO.getTransmissionType() == TransmissionType.Sync ||
                pSourceSystemTransmissionDTO.getTransmissionType() == TransmissionType.UsageSync) {
            rtnOFX = ProcessingErrorHandler.handleSignOnError(pQBDTProcessResult.getMessage());
        } else {
            rtnOFX = ProcessingErrorHandler.handleUpdateError(pPSID, pSourceSystemTransmissionDTO.getTRNUID(), pQBDTProcessResult.getMessage());
        }
        String responseStr = OFXManager.javaResponseToOFX(rtnOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        try {
            finalizeTransmissionError(pPSID, pSourceTransmissionId, pSourceSystemTransmissionDTO, responseStr, pQBDTProcessResult.getMessage(), pQBDTProcessResult.getCompanyEventList(), clientIP, isRetry);
        } catch (Throwable t) {
            // Nothing to do, just log the error.
            logger.error("Error for company PSID " + pPSID + ".", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return responseStr;
    }

    public static void finalizeTransmissionError(String pPSID, String pSourceTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, String responseOFXStr, ErrorMessage transmissionFailureMessage, List<QBDTCompanyEventDTO> eventList, String clientIP) {
        finalizeTransmissionError(pPSID, pSourceTransmissionId, pSourceSystemTransmissionDTO, responseOFXStr, transmissionFailureMessage, eventList, clientIP, false);
    }

    public static void finalizeTransmissionError(String pPSID, String pSourceTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, String responseOFXStr, ErrorMessage transmissionFailureMessage, List<QBDTCompanyEventDTO> eventList, String clientIP, boolean isRetry) {
        if (isRetry) {
            return;
        }

        try {
            PayrollServices.beginUnitOfWork();
            CompanyEventDTO coEventDTO = new CompanyEventDTO();
            Collection<CompanyEventDetailDTO> eventDTOCollection = new ArrayList<CompanyEventDetailDTO>();
            coEventDTO.setEventDetails(eventDTOCollection);
            coEventDTO.setEventTypeCode(EventTypeCode.TransmissionError);
            CompanyEventDetailDTO coEventDetailErrMsgDTO = new CompanyEventDetailDTO();
            coEventDetailErrMsgDTO.setEventDetailTypeCode(EventDetailTypeCode.ErrorMessage);
            coEventDetailErrMsgDTO.setEventDetailValue(transmissionFailureMessage.getTransmissionErrorDescription());
            eventDTOCollection.add(coEventDetailErrMsgDTO);

            CompanyEventDetailDTO coEventDetailTranmissionIdDTO = new CompanyEventDetailDTO();
            coEventDetailTranmissionIdDTO.setEventDetailTypeCode(EventDetailTypeCode.TransmissionId);
            coEventDetailTranmissionIdDTO.setEventDetailValue(pSourceTransmissionId);
            eventDTOCollection.add(coEventDetailTranmissionIdDTO);

            CompanyEventDetailDTO coEventDetailTransmissionIdDTO = new CompanyEventDetailDTO();
            coEventDetailTransmissionIdDTO.setEventDetailTypeCode(EventDetailTypeCode.ReasonDescription);
            coEventDetailTransmissionIdDTO.setEventDetailValue(transmissionFailureMessage.getErrorDescription());
            eventDTOCollection.add(coEventDetailTransmissionIdDTO);

            ProcessResult<CompanyEvent> coEventAddPR = PayrollServices.companyManager.addCompanyEvent(SourceSystemCode.QBDT, pPSID, coEventDTO);
            if (!coEventAddPR.isSuccess()) {
                // Log error but don't stop processing.
                logger.error("Error for company PSID " + pPSID + ". Could not add transmission error event with transmissionFailureMessage: " + transmissionFailureMessage);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error("Error for company PSID " + pPSID + ".", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        String errDescription;
        if (transmissionFailureMessage.getUniqueErrorIdentifier() == ErrorMessages.ErrorEnum.AuthenticationFailedError
                || transmissionFailureMessage.getUniqueErrorIdentifier() == ErrorMessages.ErrorEnum.MaxPinRetryError) {
            errDescription = QBDTTransmissionMessageDescription.getErrorBadPIN();
        } else {
            errDescription = QBDTTransmissionMessageDescription.getErrorDescriptor();
        }
        finalizeTransmission(pPSID, pSourceTransmissionId, pSourceSystemTransmissionDTO, responseOFXStr, errDescription, eventList, clientIP);
    }

    public static void finalizeTransmission(String pPSID, String pSourceTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, String responseOFXStr, String transmissionDescription, List<QBDTCompanyEventDTO> eventList, String clientIP) {
        finalizeTransmission(pPSID, pSourceTransmissionId, pSourceSystemTransmissionDTO, responseOFXStr, transmissionDescription, eventList, clientIP, false);
    }

    public static void finalizeTransmission(String pPSID, String pSourceTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, String responseOFXStr, String transmissionDescription, List<QBDTCompanyEventDTO> eventList, String clientIP, boolean isRetry) {
        if (pPSID == null || pSourceSystemTransmissionDTO == null) {
            return;
        }

        if (isRetry) {
            if (pSourceSystemTransmissionDTO.getDescription() != null) {
                if (pSourceSystemTransmissionDTO.getDescription().equals(AssistedConnectionInformation.getSkippedProcessingMessage())) {
                    pSourceSystemTransmissionDTO.setDescription(transmissionDescription);
                } else {
                    pSourceSystemTransmissionDTO.setDescription("Reprocessed - " + transmissionDescription);
                }
                PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBDT, pPSID, pSourceTransmissionId, pSourceSystemTransmissionDTO);
            }

            return;
        }

        Company company = Company.findCompany(pPSID, SourceSystemCode.QBDT);
        pSourceSystemTransmissionDTO.setDescription(transmissionDescription);
        pSourceSystemTransmissionDTO.setResponseDocument(responseOFXStr);
        pSourceSystemTransmissionDTO.setIPAddress(clientIP);

        if (company != null) {
            try {
                PayrollServices.beginUnitOfWork();
                company = Company.findCompany(pPSID, SourceSystemCode.QBDT);
                pSourceSystemTransmissionDTO.setResponseToken(company.getCurrentToken());
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } else {
            pSourceSystemTransmissionDTO.setResponseToken(0L);
        }

        if (eventList != null) {
            try {
                PayrollServices.beginUnitOfWork();
                for (QBDTCompanyEventDTO event : eventList) {
                    createCompanyEvent(event);
                }
                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
        PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBDT, pPSID, pSourceTransmissionId, pSourceSystemTransmissionDTO);
    }

    private static QBDTProcessResult createCompanyEvent(QBDTCompanyEventDTO eventDTO) {

        QBDTProcessResult processResult = new QBDTProcessResult();
        CompanyEventDTO coEventDTO = new CompanyEventDTO();
        coEventDTO.setEventTypeCode(eventDTO.getEventTypeCode());

        Collection<CompanyEventDetailDTO> eventDTOCollection = new ArrayList<CompanyEventDetailDTO>();

        CompanyEventDetailDTO coEventDetailTranmissionIdDTO = new CompanyEventDetailDTO();
        coEventDetailTranmissionIdDTO.setEventDetailTypeCode(EventDetailTypeCode.TransmissionId);
        coEventDetailTranmissionIdDTO.setEventDetailValue(eventDTO.getTransmissionId());
        eventDTOCollection.add(coEventDetailTranmissionIdDTO);

        if (eventDTO.getErrMsg() != null) {
            CompanyEventDetailDTO coEventDetailDescriptionDTO = new CompanyEventDetailDTO();
            coEventDetailDescriptionDTO.setEventDetailTypeCode(EventDetailTypeCode.PayrollRejectedReason);
            coEventDetailDescriptionDTO.setEventDetailValue(eventDTO.getErrMsg());
            eventDTOCollection.add(coEventDetailDescriptionDTO);
        }

        coEventDTO.setEventDetails(eventDTOCollection);

        ProcessResult<CompanyEvent> coEventAddPR = PayrollServices.companyManager.addCompanyEvent(eventDTO.getCompany().getSourceSystemCd(), eventDTO.getCompany().getSourceCompanyId(), coEventDTO);
        if (!coEventAddPR.isSuccess()) {
            String errMsgStr = "PayrollServices.companyManager.addCompanyEvent('" + eventDTO.getCompany().getSourceSystemCd() + "','" + eventDTO.getCompany().getSourceCompanyId() + "','" + coEventDTO.getEventTypeCode() + ") failed with error code " + coEventAddPR.getMessages().get(0).getMessageCode() + ".";
            processResult.setMessage(ErrorMessages.UnexpectedError(errMsgStr));
            return processResult;
        }
        return processResult;

    }

    /**
     * Returns the corresponding error message for when a company is in the specified status.
     *
     * @return Result of getting the on hold message
     */
    public static QBDTProcessResult<ErrorMessage> getOnHoldErrorMessageForCompanyStatus(String pPsid) {
        QBDTProcessResult<ErrorMessage> processResult = new QBDTProcessResult<ErrorMessage>();
        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
        ServiceSubStatusCode firstOnHoldReasonCd = ServiceSubStatusFactory.getServiceSubStatusCodeBySeverity(company);

        if (firstOnHoldReasonCd != null) {
            switch (firstOnHoldReasonCd) {
                case AchRejectR1R9:
                    processResult.setResult(ErrorMessages.PayrollRejectACHReturnR01ThruR09());
                    return processResult;

                case DirectDepositLimit:
                    processResult.setResult(ErrorMessages.PayrollRejectDDLimit());
                    return processResult;

                case AchRejectOther:
                    processResult.setResult(ErrorMessages.PayrollRejectACHReturnNonR01ThruR09());
                    return processResult;

                case Fraud:
                case AMLHold:
                    processResult.setResult(ErrorMessages.PayrollRejectFraud());
                    return processResult;

                case FraudReview:
                    processResult.setResult(ErrorMessages.PayrollRejectFraudReview());
                    return processResult;

                case IntuitCollections:
                    processResult.setResult(ErrorMessages.PayrollRejectIntuitCollections());
                    return processResult;

                case MissingPaperwork:
                    processResult.setResult(ErrorMessages.PayrollRejectMissingPaperwork());
                    return processResult;

                case RiskCollections:
                    processResult.setResult(ErrorMessages.PayrollRejectRiskCollections());
                    return processResult;

                case SuspendedDirectDeposit:
                    processResult.setResult(ErrorMessages.PayrollRejectSuspendedDD());
                    return processResult;

                case PendingTermination:
                    processResult.setResult(ErrorMessages.PayrollRejectPendingTermination());
                    return processResult;

                case AuditCorrections:
                    processResult.setResult(ErrorMessages.PayrollRejectAuditCorrections());
                    return processResult;

                case MTLHold:
                    processResult.setResult(ErrorMessages.MTLComplianceHold());
                    return processResult;
            }
        }
        processResult.setMessage(ErrorMessages.UnexpectedError(""));
        return processResult;
    }

    /**
     * C01 - Incorrect bank account number
     * C02 - Incorrect transit/routing number
     * C03 - Incorrect transit/routing number and bank account number
     * C04 - Bank account name change
     * C05 - Incorrect payment code
     * C06 - Incorrect bank account number and transit code
     * C07 - Incorrect transit/routing number, bank account number and payment code
     * C09 - Incorrect individual ID number (applies only to consumer initiated transactions).
     * C10 - Incorrect company name
     * C11 - Incorrect company identification
     * C12 - Incorrect company name and company ID
     * C13 - Addenda Format Error - The Entry Detail Record is correct, but
     * information in its Addenda Record is unclear
     * or formatted incorrectly, i.e. not formatted in
     * ANSI or NACHA endorsed banking conventions.
     *
     * @param pCompany the company
     * @return result of getting the noc message
     */
    public static QBDTProcessResult<ErrorMessage> getNOCMessage(Company pCompany) {
        QBDTProcessResult<ErrorMessage> processResult = new QBDTProcessResult<ErrorMessage>();

        DomainEntitySet<CompanyEvent> payrollSubmittedWithPendingNOCList = CompanyEvent.findCompanyEvents
                (pCompany, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, true);

        String empBAId = payrollSubmittedWithPendingNOCList.get(0).getCompanyEventDetails(EventDetailTypeCode.EmployeeBankAccountId).get(0).getValue();

        DomainEntitySet<CompanyEventDetail> nocEventDetails = CompanyEvent.findCompanyEventDetails(pCompany, EventTypeCode.NOC,
                EventDetailTypeCode.EmployeeBankAccountId, empBAId);

        EmployeeBankAccount empBA = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(empBAId));
        String employeeName = getEmployeeNameFromBankAccount(empBA);

        boolean bankNameNOC = false;
        boolean bankAccountNumberNOC = false;
        boolean bankRoutingNumberNOC = false;
        boolean bankAccountTypeNOC = false;
        String origAccountNumber = null;
        String changedAccountNumber = null;
        String origRoutingNumber = null;
        String changedRoutingNumber = null;
        String origAccountType = null;
        String changedAccountType = null;

        for (CompanyEventDetail eventDetail : nocEventDetails) {
            CompanyEvent coNOCEvent = eventDetail.getCompanyEvent();
            String returnType = coNOCEvent.getCompanyEventDetails(EventDetailTypeCode.ACHEventCd).get(0).getValue();

            if (returnType.compareTo("C04") == 0) {
                bankNameNOC = true;
                continue;
            }
            String oldAccountNumber = coNOCEvent.getCompanyEventDetails(EventDetailTypeCode.OldAccountNumber).get(0).getValue();
            String newAccountNumber = coNOCEvent.getCompanyEventDetails(EventDetailTypeCode.NewAccountNumber).get(0).getValue();
            if (oldAccountNumber.compareTo(newAccountNumber) != 0) {
                bankAccountNumberNOC = true;
                origAccountNumber = oldAccountNumber;
                changedAccountNumber = newAccountNumber;
            }

            String oldRoutingNumber = coNOCEvent.getCompanyEventDetails(EventDetailTypeCode.OldRoutingNumber).get(0).getValue();
            String newRoutingNumber = coNOCEvent.getCompanyEventDetails(EventDetailTypeCode.NewRoutingNumber).get(0).getValue();
            if (oldRoutingNumber.compareTo(newRoutingNumber) != 0) {
                bankRoutingNumberNOC = true;
                origRoutingNumber = oldRoutingNumber;
                changedRoutingNumber = newRoutingNumber;
            }

            String oldAccountType = coNOCEvent.getCompanyEventDetails(EventDetailTypeCode.OldAccountType).get(0).getValue();
            String newAccountType = coNOCEvent.getCompanyEventDetails(EventDetailTypeCode.NewAccountType).get(0).getValue();
            if (oldAccountType.compareTo(newAccountType) != 0) {
                bankAccountTypeNOC = true;
                origAccountType = oldAccountType;
                changedAccountType = newAccountType;
            }
        }

        // Three NOC reasons
        if (bankAccountNumberNOC && bankRoutingNumberNOC && bankAccountTypeNOC) {
            processResult.setResult(ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType(employeeName, origAccountNumber, origRoutingNumber, origAccountType, changedAccountNumber, changedRoutingNumber, changedAccountType));
            return processResult;
        }

        // Two NOC reasons
        if (bankAccountNumberNOC && bankRoutingNumberNOC) {
            processResult.setResult(ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumber(employeeName, origAccountNumber, origRoutingNumber, changedAccountNumber, changedRoutingNumber));
            return processResult;
        }
        if (bankAccountNumberNOC && bankAccountTypeNOC) {
            processResult.setResult(ErrorMessages.PayrollRejectNOCEEBankAccountNumberAccountType(employeeName, origAccountNumber, origAccountType, changedAccountNumber, changedAccountType));
            return processResult;
        }
        if (bankRoutingNumberNOC && bankAccountTypeNOC) {
            processResult.setResult(ErrorMessages.PayrollRejectNOCEEBankRoutingNumberAccountType(employeeName, origRoutingNumber, origAccountType, changedRoutingNumber, changedAccountType));
            return processResult;
        }
        // One NOC reason
        if (bankAccountNumberNOC) {
            processResult.setResult(ErrorMessages.PayrollRejectNOCEEBankAccountNumber(employeeName, origAccountNumber, changedAccountNumber));
            return processResult;
        }
        if (bankRoutingNumberNOC) {
            processResult.setResult(ErrorMessages.PayrollRejectNOCEEBankRoutingNumber(employeeName, origRoutingNumber, changedRoutingNumber));
            return processResult;
        }
        if (bankAccountTypeNOC) {
            processResult.setResult(ErrorMessages.PayrollRejectNOCEEBankAccountType(employeeName, origAccountType, changedAccountType));
            return processResult;
        }
        if (bankNameNOC) {
            processResult.setResult(ErrorMessages.PayrollWarnNOCEEBankAccountName(employeeName));
            return processResult;
        }

        processResult.setResult(ErrorMessages.PayrollErrorNOCNoChangesDetected());
        return processResult;
    }

    public static QBDTProcessResult<ErrorMessage> getBankReturnEEBankAccountMessage(Company company) {
        QBDTProcessResult<ErrorMessage> processResult = new QBDTProcessResult<ErrorMessage>();

        // Find the Employee Bank Account from the return event so we can get the employee's name.
        DomainEntitySet<CompanyEvent> payrollSubmittedWithEEReturnList = CompanyEvent.findCompanyEvents
                (company, EventTypeCode.PayrollSubmittedWithEmployeeWithPendingReturn, CompanyEventStatus.Active, true);
        String empBAId = payrollSubmittedWithEEReturnList.get(0).getCompanyEventDetails(EventDetailTypeCode.EmployeeBankAccountId).get(0).getValue();
        EmployeeBankAccount empBA = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(empBAId));
        String employeeName = getEmployeeNameFromBankAccount(empBA);

        processResult.setResult(ErrorMessages.PayrollRejectBankReturnEEBankAccount(employeeName));

        return processResult;
    }

    private static String getEmployeeNameFromBankAccount(EmployeeBankAccount empBankAccount) {
        String employeeName;
        if (empBankAccount.getEmployee().getSourceEmployeeId().compareTo("0") == 0) {
            employeeName = empBankAccount.getEmployee().getSourceEmployeeId();
        } else {
            String firstName = empBankAccount.getEmployee().getFirstName();

            if (firstName.compareTo(EmployeeTranslator.NULL_EMP_NAME_STR) != 0) {
                employeeName = empBankAccount.getEmployee().getFirstName() + " " + empBankAccount.getEmployee().getLastName();
            } else {
                employeeName = empBankAccount.getEmployee().getLastName();
            }
        }

        return employeeName;
    }
}
