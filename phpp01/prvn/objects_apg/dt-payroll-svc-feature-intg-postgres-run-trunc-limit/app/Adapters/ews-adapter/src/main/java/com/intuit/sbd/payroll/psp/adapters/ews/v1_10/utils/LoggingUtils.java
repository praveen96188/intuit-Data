package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsAdapterConst;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BaseSMSMigration;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domain.SMSMigration;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * @author Jeff Jones
 */
public class LoggingUtils {
    private static final SpcfLogger logger;

    private static final JAXBContext mPSIMessageContext;
    private static final JAXBContext mEwsRequestContext;
    private static final JAXBContext mEwsResponseContext;
    private static final JAXBContext mGetPayrollContext;
    private static final JAXBContext mPayrollInfoContext;

    static {
        logger = PayrollServices.getLogger(LoggingUtils.class);
        try {
            mPSIMessageContext = JAXBContext.newInstance(PSIMessageWSDTO.class);
            mEwsRequestContext = JAXBContext.newInstance(EwsRequest.class);
            mEwsResponseContext = JAXBContext.newInstance(EwsResponse.class);
            mGetPayrollContext = JAXBContext.newInstance(GetPayrollInfoWSDTO.class);
            mPayrollInfoContext = JAXBContext.newInstance(PayrollInfoWSDTO.class);
        } catch (Exception e) {
            logger.fatal(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void logTransmissions(TransmissionsList pTransmissionsList) throws Exception {
        try {
            if (pTransmissionsList != null && pTransmissionsList.getCompanySeq() != null) {

                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                Company company = Application.findById(Company.class, SpcfUniqueId.createInstance(pTransmissionsList.getCompanySeq()));
                PayrollServices.rollbackUnitOfWork();
                
                if (company != null) {
                    String psid = company.getSourceCompanyId();

                    for (PSPTransmission pspTransmission : pTransmissionsList.getmPSPTransmissions()) {
                        logPSPRequest(psid, pspTransmission);
                    }

                    for (AS400Transmission as400Transmission : pTransmissionsList.getmAS400Transmissions()) {
                        logAS400RequestAndResponse(psid, as400Transmission);
                    }

                    for (PSPTransmission pspTransmission : pTransmissionsList.getmPSPTransmissions()) {
                        logPSPResponse(psid, pspTransmission);
                    }
                } else {
                    logger.warn(String.format("Unable to log transmissions, Company Id %s not found.", pTransmissionsList.getCompanySeq()));
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private static void logPSPRequest(String pPSID, PSPTransmission pPSPTransmission) throws Exception {
        cleanPSPTransmission(pPSPTransmission);

        Marshaller ewsRequestMarshaller = mEwsRequestContext.createMarshaller();
        ewsRequestMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        ewsRequestMarshaller.marshal(pPSPTransmission.getRequest(), stringWriter);
        String requestXML = stringWriter.toString();

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO(pPSPTransmission.getTransmissionType(), requestXML);

        sourceSystemTransmissionDTO.setDescription(pPSPTransmission.getTransmissionType().toString());
        sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.EWS);
        sourceSystemTransmissionDTO.setToSourceSystem(SourceSystemCode.PSP);
        sourceSystemTransmissionDTO.setRequestToken((long) 0);
        sourceSystemTransmissionDTO.setIPAddress(pPSPTransmission.getRequest().getIpAddress());

        EwsQuickBooks ewsQuickBooks = null;
        if (pPSPTransmission.getRequest() instanceof EwsCreateAccount) {
            EwsCreateAccount ewsCreateAccount = (EwsCreateAccount) pPSPTransmission.getRequest();
            ewsQuickBooks = ewsCreateAccount.getEwsCompany().getQuickBooks();
        }

        if (pPSPTransmission.getRequest() instanceof EwsValidateSubscription) {
            EwsValidateSubscription ewsValidateSubscription = (EwsValidateSubscription) pPSPTransmission.getRequest();
            ewsQuickBooks = ewsValidateSubscription.getQuickBooks();
        }

        if (ewsQuickBooks != null) {
            OFXAPPVERObject ofxAPPVERObject = new OFXAPPVERObject(ewsQuickBooks.getAppVersion());
            sourceSystemTransmissionDTO.setApplicationVersion(ofxAPPVERObject.getQBVersionStr());
            sourceSystemTransmissionDTO.setApplicationId(ofxAPPVERObject.getFlavorId());
            sourceSystemTransmissionDTO.setTaxTableId(ofxAPPVERObject.getTaxTableId());
        }
        ProcessResult prUnhandled = PayrollServices.transmissionManagerSecondary.initializeTransmission
                (SourceSystemCode.QBDT, pPSID, pPSPTransmission.getTransmissionId(),
                        sourceSystemTransmissionDTO, pPSPTransmission.getInitializeDateTime());
        if (!prUnhandled.isSuccess()) {
            logger.warn("Unhandled ProcessResult failure from TransmissionManager.initializeTransmission(): " + prUnhandled.toString());
        }
    }

    private static void logPSPResponse(String pPSID, PSPTransmission pPSPTransmission) throws Exception {

        Marshaller ewsResponseMarshaller = mEwsResponseContext.createMarshaller();
        ewsResponseMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        ewsResponseMarshaller.marshal(pPSPTransmission.getResponse(), stringWriter);
        String responseXML = stringWriter.toString();

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO(pPSPTransmission.getTransmissionType(), responseXML);

        sourceSystemTransmissionDTO.setResponseDocument(responseXML);
        sourceSystemTransmissionDTO.setResponseToken((long) 0);
        ProcessResult prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission
                (SourceSystemCode.QBDT, pPSID, pPSPTransmission.getTransmissionId(),
                        sourceSystemTransmissionDTO, pPSPTransmission.getFinalizeDateTime());

        if (!prUnhandled.isSuccess()) {
            logger.warn("Unhandled ProcessResult failure from TransmissionManager.finalizeTransmission(): " + prUnhandled.toString());
        }
    }

    private static void logAS400RequestAndResponse(String pPSID, AS400Transmission pAS400Transmission) throws Exception {
        cleanAS400Transmission(pAS400Transmission);

        Marshaller psiMessageMarshaller = mPSIMessageContext.createMarshaller();
        psiMessageMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        psiMessageMarshaller.marshal(pAS400Transmission.getRequest(), stringWriter);
        String requestXML = stringWriter.toString();

        stringWriter = new StringWriter();
        psiMessageMarshaller.marshal(pAS400Transmission.getResponse(), stringWriter);
        String responseXML = stringWriter.toString();

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO(pAS400Transmission.getTransmissionType(), requestXML);

        sourceSystemTransmissionDTO.setDescription("AS400 - " + pAS400Transmission.getTransmissionType().toString());
        sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.EWS);
        sourceSystemTransmissionDTO.setToSourceSystem(SourceSystemCode.AS400);
        sourceSystemTransmissionDTO.setRequestToken((long) 0);

        ProcessResult prUnhandled = PayrollServices.transmissionManagerSecondary.initializeTransmission
                (SourceSystemCode.QBDT, pPSID, pAS400Transmission.getTransmissionId(),
                        sourceSystemTransmissionDTO, pAS400Transmission.getInitializeDateTime());
        if (!prUnhandled.isSuccess()) {
            logger.warn("Unhandled ProcessResult failure from TransmissionManager.initializeTransmission(): " + prUnhandled.toString());
        }

        sourceSystemTransmissionDTO.setResponseDocument(responseXML);
        sourceSystemTransmissionDTO.setResponseToken((long) 0);
        prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission
                (SourceSystemCode.QBDT, pPSID, pAS400Transmission.getTransmissionId(),
                        sourceSystemTransmissionDTO, pAS400Transmission.getFinalizeDateTime());

        if (!prUnhandled.isSuccess()) {
            logger.warn("Unhandled ProcessResult failure from TransmissionManager.finalizeTransmission(): " + prUnhandled.toString());
        }
    }

    private static void cleanPSPTransmission(PSPTransmission pPSPTransmission) {
        EwsRequest ewsRequest = pPSPTransmission.getRequest();
        EwsResponse ewsResponse = pPSPTransmission.getResponse();

        switch (pPSPTransmission.getTransmissionType()) {
            case CreatePIN:
            case AuthenticatePIN:
                EwsBasePin ewsBasePin = (EwsBasePin) ewsRequest;
                EwsBasePinResponse ewsBasePinResponse = (EwsBasePinResponse) ewsResponse;

                ewsBasePin.setPin(EwsAdapterConst.MASK_STRING);
                ewsBasePinResponse.setPrivateKey(EwsAdapterConst.MASK_STRING);
                break;
            case ResetPIN:
                EwsResetPin ewsResetPin = (EwsResetPin) ewsRequest;
                EwsResetPinResponse ewsResetPinResponse = (EwsResetPinResponse) ewsResponse;

                ewsResetPin.setPinSignature(EwsAdapterConst.MASK_STRING);
                ewsResetPinResponse.setPin(EwsAdapterConst.MASK_STRING);
                ewsResetPinResponse.setPrivateKey(EwsAdapterConst.MASK_STRING);
                break;
            case ChangePIN:
                EwsUpdatePin ewsUpdatePin = (EwsUpdatePin) ewsRequest;
                EwsBasePinResponse ewsUpdatePinResponse = (EwsBasePinResponse) ewsResponse;

                ewsUpdatePin.setPin(EwsAdapterConst.MASK_STRING);
                ewsUpdatePin.setOldPin(EwsAdapterConst.MASK_STRING);
                ewsUpdatePinResponse.setPrivateKey(EwsAdapterConst.MASK_STRING);
                break;
            case AddService:
                EwsAddService ewsAddService = (EwsAddService) ewsRequest;
                ewsAddService.setPin(EwsAdapterConst.MASK_STRING);
                break;
            case DeactivateService:
                EwsDeactivateService ewsDeactivateService = (EwsDeactivateService) ewsRequest;
                ewsDeactivateService.setPin(EwsAdapterConst.MASK_STRING);
                break;
        }
    }

    private static void cleanAS400Transmission(AS400Transmission pAS400Transmission) {
        PSIMessageWSDTO request = pAS400Transmission.getRequest();
        PSIMessageWSDTO response = pAS400Transmission.getResponse();

        maskData(request, false);
        maskData(response, false);
    }

    private static void maskData(PSIMessageWSDTO pPSIMessageWSDTO, boolean maskBankAccount) {
        try {
            if (pPSIMessageWSDTO.getCompany() != null) {
                CompanyWSDTO companyWSDTO = pPSIMessageWSDTO.getCompany();
                if ((companyWSDTO.getPin() != null) && (companyWSDTO.getPin().length() > 0)) {
                    companyWSDTO.setPin(EwsAdapterConst.MASK_STRING);
                }

                if ((companyWSDTO.getOldPIN() != null) && (companyWSDTO.getOldPIN().length() > 0)) {
                    companyWSDTO.setOldPIN(EwsAdapterConst.MASK_STRING);
                }
            }

            //will not always mask bank account if going to mask in UI only for some agents
            if (maskBankAccount && pPSIMessageWSDTO.getFeature() != null) {
                FeatureWSDTO featureWSDTO = pPSIMessageWSDTO.getFeature();
                if (featureWSDTO.getBank() != null) {
                    featureWSDTO.getBank().setBankAccountNumber(EwsAdapterConst.MASK_STRING);
                    featureWSDTO.getBank().setBankRoutingNumber(EwsAdapterConst.MASK_STRING);
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static void logTransmissions(GetPayrollInfoWSDTO pRequest, PayrollInfoWSDTO pResponse) throws Exception {
        Marshaller getPayrollInfoMarshaller = mGetPayrollContext.createMarshaller();
        getPayrollInfoMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Marshaller payrollInfoMarshaller = mPayrollInfoContext.createMarshaller();
        payrollInfoMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        GetPayrollInfoWSDTO request = pRequest.clone();
        PayrollInfoWSDTO response = pResponse.clone();

        String psid = pRequest.getPayrollStatusWSDTO().getUserID();        

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO;
        try {
            if (psid != null && psid.length() > 0) {
                StringWriter stringWriter = new StringWriter();
                getPayrollInfoMarshaller.marshal(request, stringWriter);
                String requestXML = stringWriter.toString();

                stringWriter = new StringWriter();
                payrollInfoMarshaller.marshal(response, stringWriter);
                String responseXML = stringWriter.toString();

                String transmissionId = SpcfUniqueId.createInstance(true).toString();
                
                sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO(TransmissionType.QueryPayrollStatus, requestXML);

                sourceSystemTransmissionDTO.setDescription(TransmissionType.QueryPayrollStatus.toString());
                sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.EWS);
                sourceSystemTransmissionDTO.setRequestToken((long) 0);

                ProcessResult prUnhandled = PayrollServices.transmissionManagerSecondary.initializeTransmission
                        (SourceSystemCode.QBDT, psid, transmissionId, sourceSystemTransmissionDTO);
                if (!prUnhandled.isSuccess()) {
                    logger.warn("Unhandled ProcessResult failure from TransmissionManager.initializeTransmission(): " + prUnhandled.toString());
                }

                sourceSystemTransmissionDTO.setResponseDocument(responseXML);
                sourceSystemTransmissionDTO.setResponseToken((long) 0);
                prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission
                        (SourceSystemCode.QBDT, psid, transmissionId, sourceSystemTransmissionDTO);
                if (!prUnhandled.isSuccess()) {
                    logger.warn("Unhandled ProcessResult failure from TransmissionManager.finalizeTransmission(): " + prUnhandled.toString());
                }
            } else {
                logger.warn("Could not log transmission - PSID equals null");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

    }

    public static void logMigrationMsg(String psid, Company company, int statusCode) {
        
        SMSMigrationStatus smsMigrationStatus = null;

        try {
            StopWatch stopWatch = StopWatch.startTimer();
            DomainEntitySet<SMSMigration> smsMigrations = SMSMigration.getSmsMigrationByCompany(company);
            String timeTakenForMigLookup = stopWatch.stop().getElapsedTimeString();
            if (smsMigrations.isNotEmpty()) {
                BaseSMSMigration smsMigration = smsMigrations.get(0);
                if(smsMigration != null) {
                    smsMigrationStatus = smsMigration.getMigrationStatus();
                }
            }
            logger.info(String.format("Action=logMigrationMsg, responseCode=%d, AMS_Migrated=%s, MigrationStatus=%s, realmId=%s, psid=%s, time=%s",
                    statusCode, company.isMoneyMovementOnboardingEnabled(), smsMigrationStatus, company.getIAMRealmId(), psid, timeTakenForMigLookup));
        } catch (Exception e) {
            logger.error("Action=logMigrationMsg, status=error, psid=" + psid + ", responseCode=" + statusCode, e);
        }
    }

}
