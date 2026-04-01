package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.IERSGateway;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessage;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsSubscriptionBillingInfo;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsValidateSubscription;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsValidateSubscriptionResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEinSubscriptionStatus;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsPaymentMethod;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ers.ERSListener;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOListener;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.amo.GetCustomerAssetResponseTypeDTO;
import com.intuit.sbd.payroll.psp.gateways.amo.IAMOWSGateway;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * User: rnorian
 * Date: Jul 13, 2010
 * Time: 9:39:27 PM
 */
public class ValidateSubscriptionProcess extends BaseProcess {
    private static SpcfLogger logger = PayrollServices.getLogger(ValidateSubscriptionProcess.class);

    private EwsValidateSubscription mRequest;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    public ValidateSubscriptionProcess(EwsValidateSubscription pRequest) {
        this.mRequest = pRequest;
        this.mPSID = pRequest.getPsid();
        this.mEIN = pRequest.getEin();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Validate_Subscription Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsValidateSubscriptionResponse execute() {
        EwsValidateSubscriptionResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.ValidateSubscription);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsValidateSubscriptionResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsValidateSubscriptionResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();

            try {
                mPSPTransmission.setFinalizeDateTime(PSPDate.getPSPTime());
                mPSPTransmission.setResponse(response);

                LoggingUtils.logTransmissions(mTransmissionsList);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return response;

    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();
    }

    @Override
    protected EwsValidateSubscriptionResponse process() throws Exception {
        // defaults to code = 0, message = 'Success'
        EwsValidateSubscriptionResponse response = new EwsValidateSubscriptionResponse();

        //todo_rhn: does PSP store subscription numbers w/leading zeroes?
        Entitlement entitlement = Entitlement.findEntitlementBySubscriptionNumber(mRequest.getSubscriptionNumber());
        if (entitlement == null) {
            throw new EwsException(EwsMessages.subscriptionNumberDoesNotExistError());
        }
        
        EntitlementUnit entitlementUnit = null;
        for (EntitlementUnit eu : entitlement.getActiveEntitlementUnitCollection()) {
            if (eu.getFedTaxId().equals(mRequest.getEin())) {
                entitlementUnit = eu;
                break;
            }
        }

        if (entitlementUnit == null) {
            response.setSubscriptionStatus(EwsEinSubscriptionStatus.EinNotSubscribed);
            return response;
        }

        mPspCompany = entitlementUnit.getCompany();
        updateCompanyQuickBooksInfo(mPspCompany);
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());

        // Sync data from ERS to PSP if needed
        if (entitlement.hasDummyEntitlementCode()) {
            Entitlement ersEntitlement = syncEntitlementDataFromERS(mPspCompany, entitlement);
            if (ersEntitlement != null) {
                entitlement = ersEntitlement;
                entitlementUnit = Application.refresh(entitlementUnit);
            }
        }

        boolean isAMOSyncEnabled = SystemParameter.findBooleanValue(SystemParameter.Code.AMO_WS_EWS_SYNC_ENABLED, true);
        if (isAMOSyncEnabled) {
            // Sync data from AMO to PSP if needed
            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
            spcfCalendar.addDays(15);
            if (entitlement.getNextChargeDate() != null && entitlement.getNextChargeDate().before(spcfCalendar)) {
                //Sync data from AMO to PSP to make sure we have the current next charge date and subscription end date.
                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
                EntitlementUnit amoEntitlementUnit = syncEntitlementUnitDataFromAMO(mPspCompany, entitlementUnit.getId(), entitlementUnitDTO);
                if (amoEntitlementUnit != null) {
                    entitlementUnit = amoEntitlementUnit;
                    entitlement = amoEntitlementUnit.getEntitlement();
                }
            }
        }

        EwsSubscriptionBillingInfo billingInfo = new EwsSubscriptionBillingInfo();
        response.setSubscriptionBillingInfo(billingInfo);
        response.setSubscriptionStatus(determineSubscriptionStatus(entitlementUnit));
        response.setEntitlementCreationDate(CalendarUtils.convertToCalendar(entitlement.getCreatedDate()));
        response.setSubType(String.valueOf(entitlement.getEntitlementCode().getQuickBooksSubtype()));
        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setFundingModel(mPspCompany.getFundingModel().getFundingModelCd());
        response.setCompanyLegalInfo(EwsFactory.createEwsLegalInfo(mPspCompany.getLegalName(), mPspCompany.getLegalAddress()));

        billingInfo.setCreditCardType(entitlement.getCreditCardType());
        billingInfo.setCreditCardNumber(entitlement.getCreditCardNumber());
        billingInfo.setCreditCardExp(entitlement.getCreditCardExpiration());

        if (entitlement.getSubscriptionEndDate() != null) {
            response.setSubscriptionEndDate(CalendarUtils.convertToCalendar(entitlement.getSubscriptionEndDate()));
        }

        // if AMO message has not arrived, these values will be null
        if (entitlement.getNextChargeDate() != null && !entitlement.getEntitlementCode().isAssisted()) {
            billingInfo.setSubscriptionNextBillDate(CalendarUtils.convertToCalendar(entitlement.getNextChargeDate()));
            if (response.getSubscriptionEndDate() == null) {
                response.setSubscriptionEndDate(billingInfo.getSubscriptionNextBillDate());
            }
        }

        if (entitlement.getPaymentMethodType() != null) {
            billingInfo.setPaymentMethod(EwsPaymentMethod.valueOf(entitlement.getPaymentMethodType().name()));
        }

        try {
            CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);
            if (companyBankAccount != null) {
                response.setQbAccountName(companyBankAccount.getSourceBankAccountName());
            }
        } catch (Exception e) {
            //do nothing
        }

        if (entitlementUnit.getLastValidationDate() == null ||
                CalendarUtils.getDifferenceInHours(entitlementUnit.getLastValidationDate(), PSPDate.getPSPTime()) >= 1) {
            //Update the lastValidationDate with pspDate
            entitlementUnit.setLastValidationDate(PSPDate.getPSPTime());
            Application.save(entitlementUnit);
        }

        if (mRequest.getPsid() != null && mRequest.getPsid().length() > 0) {
            if (!mRequest.getPsid().equals(mPspCompany.getSourceCompanyId())) {
                EwsMessage ewsMessage = EwsMessages.psidMismatch(mRequest.getEin(), mRequest.getSubscriptionNumber(),  mRequest.getPsid(), mPspCompany.getSourceCompanyId());
                logger.warn(ewsMessage);
                CompanyEvent.createPSIDMismatchEvent(mPspCompany, mPSID, String.valueOf(ewsMessage.getCode()), ewsMessage.getMessage());
            }
        }

        return response;
    }

    private EwsEinSubscriptionStatus determineSubscriptionStatus(EntitlementUnit pEntitlementUnit) {
        EwsEinSubscriptionStatus ewsSubscriptionStatus = null;

        Entitlement entitlement = pEntitlementUnit.getEntitlement();
        switch (entitlement.getEntitlementState()) {
            case Enabled:
                switch (pEntitlementUnit.getEntitlementUnitStatus()) {
                    case PendingActivation:
                    case PendingReactivation:
                    case Activated:
                    case ErrorActivating:
                    case ActivationHold:
                        ewsSubscriptionStatus = EwsEinSubscriptionStatus.Activated;
                        break;
                    case PendingDeactivation:
                    case Deactivated:
                    case ErrorDeactivating:
                    case DeactivationHold:
                        ewsSubscriptionStatus = EwsEinSubscriptionStatus.EinNotSubscribed;
                        break;
                }
                break;
            case Disabled:
                ewsSubscriptionStatus = EwsEinSubscriptionStatus.Deactivated;
                break;
        }

        return ewsSubscriptionStatus;
    }

    private void updateCompanyQuickBooksInfo(Company company) {
        if (mRequest.getQuickBooks() == null)
            return;

        try {
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

            OFXAPPVERObject ofxappverObject = new OFXAPPVERObject(mRequest.getQuickBooks().getAppVersion());
            companyDTO.getQuickBooksInfo().setApplicationVersion(ofxappverObject.getQBVersionStr());
            companyDTO.getQuickBooksInfo().setTaxTableId(ofxappverObject.getTaxTableId());
            companyDTO.getQuickBooksInfo().setLicenseNumber(mRequest.getQuickBooks().getLicenseNumber());
            companyDTO.getQuickBooksInfo().setQuickbooksSku(ofxappverObject.getFlavorId());


            ProcessResult pr = PayrollServices.companyManager.updateQBCompanyInfo(company.getSourceSystemCd(),
                                                                            company.getSourceCompanyId(),
                                                                            companyDTO);
            if (!pr.isSuccess()) {
                logger.warn("could not update company QuickBooks information during account validation -\n" + pr);
            }
        } catch (Throwable t) {
            logger.warn("could not update company QuickBooks information during account validation", t);
        }
    }

    private Entitlement syncEntitlementDataFromERS(Company pCompany, Entitlement pEntitlement) throws EwsException{
        if (pEntitlement.hasPendingOrRecentMessages()) {
            throw new EwsException(EwsMessages.ersConnectionError());
        }

        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(pEntitlement);

        ERSListener ersListener = new ERSListener(pCompany, TransmissionType.QueryEntitlement);
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        if(ersGateway == null) {
            throw new EwsException(EwsMessages.ersConnectionError());
        }

        EntitlementInfoDTO entitlementInfoDTO;
        try {
            entitlementInfoDTO = ersGateway.getEntitlementInfo(pEntitlement.getLicenseNumber(),
                                                               pEntitlement.getEntitlementOfferingCode(),
                                                               false,
                                                               ersListener);
        } catch (Throwable t) {
            logger.error("Error occurred while calling ERS", t);
            throw new EwsException(EwsMessages.ersConnectionError());
        }

        entitlementDTO.setAssetItemNumber(pEntitlement.getEntitlementCode().getAssetItemNumber());
        entitlementDTO.setEditionType(entitlementInfoDTO.getEditionType());
        entitlementDTO.setNumberOfEmployeesType(entitlementInfoDTO.getNumberOfEmployeesType());

        if (pEntitlement.hasPendingOrRecentMessages()) {
            throw new EwsException(EwsMessages.ersConnectionError());
        }

        ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        if (!processResult.isSuccess()) {
            logger.warn(String.format("PSID:%s- ", mPSID) + processResult.toString());
        }

        return processResult.getResult();
    }

    private EntitlementUnit syncEntitlementUnitDataFromAMO(Company pCompany, SpcfUniqueId pEntitlementUnitId, EntitlementUnitDTO pEntitlementUnitDTO) {
        EntitlementUnit entitlementUnit = null;

        try {
            String license = pEntitlementUnitDTO.getLicenseNumber();
            String eoc = pEntitlementUnitDTO.getEntitlementOfferingCode();

            AMOListener amoListener = new AMOListener(pCompany, TransmissionType.QueryCustomerAsset);
            IAMOWSGateway amowsGateway = AMOWSGatewayFactory.createInstance();
            if (amowsGateway == null) {
                return entitlementUnit;
            }

            GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = amowsGateway.getCustomerAsset(license, eoc, amoListener);
            if (getCustomerAssetResponseTypeDTO == null) {
                return entitlementUnit;
            }

            getCustomerAssetResponseTypeDTO.copyAmoDtoToPspDto(pEntitlementUnitDTO);

            ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.syncEntitlementUnit(pEntitlementUnitId, pEntitlementUnitDTO);
            if (!processResult.isSuccess()) {
                logger.warn(String.format("PSID:%s - ", mPSID) + processResult.toString());
            }

            entitlementUnit = processResult.getResult();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }

        return entitlementUnit;
    }
}
