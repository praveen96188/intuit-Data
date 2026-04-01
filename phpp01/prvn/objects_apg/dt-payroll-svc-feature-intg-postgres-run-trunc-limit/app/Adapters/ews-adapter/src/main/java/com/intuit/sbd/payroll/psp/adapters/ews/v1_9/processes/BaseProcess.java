package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsAdapterConst;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsBankAccountType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsDeliveryType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.DebugUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * @author Jeff Jones
 */
public abstract class BaseProcess {

    private static final SpcfLogger logger;

    protected Company mPspCompany;
    protected String mPSID;
    protected String mEIN;
    protected boolean mAddEin;
    protected CompanyBankAccount mCompanyBankAccount;
    protected String mSubscriptionNumber;

    protected static final boolean runDebugCode;

    static {
        logger = PayrollServices.getLogger(BaseProcess.class);
        runDebugCode = Boolean.parseBoolean(ConfigurationManager.getSettingValue(ConfigurationModule.EwsAdapter,
                EwsAdapterConst.RUN_DEBUG_CODE));
    }

    protected void processEwsException(EwsException pEwsException, EwsResponse pEwsResponse) {
        logger.info("PSID: " + mPSID + " ErrorCode: " + pEwsException.getCode(), pEwsException);
        EwsFactory.updateEwsResponse(pEwsException.getEwsMessage(), pEwsResponse);
    }

    protected void processThrowable(Throwable pThrowable, EwsResponse pEwsResponse) {
        logger.error("PSID: " + mPSID + " " + pThrowable.getMessage(), pThrowable);
        EwsFactory.updateEwsResponse(EwsMessages.systemError(), pEwsResponse);
    }

    protected void OverrideRandomDebits(CompanyBankAccount pCompanyBankAccount) throws Exception {
        if (pCompanyBankAccount != null && runDebugCode) {
            logger.warn("Processing ForceRandomDollar flag.");
            DebugUtil.OverrideRandomDebits(mPspCompany.getSourceCompanyId(), pCompanyBankAccount.getSourceBankAccountId());
        }
    }

    protected CompanyService deactivateService(ServiceInfoDTO pServiceInfoDTO) throws Exception {
        ProcessResult<CompanyService> servicePR;

        servicePR = PayrollServices.companyManager.deactivateService(mPspCompany.getSourceSystemCd(),
                mPspCompany.getSourceCompanyId(),
                pServiceInfoDTO.getServiceCode());
        if (!servicePR.isSuccess()) {
            MessageList messageList = servicePR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }

            if (messageList.get(0).getMessageCode().equals("1013")) {
                throw new EwsException(EwsMessages.serviceAlreadyExists(EnumUtils.getReadableName(pServiceInfoDTO.getServiceCode())));
            }

            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }

        return servicePR.getResult();
    }

    protected CompanyService addService(ServiceInfoDTO pServiceInfoDTO) throws Exception {
        ProcessResult<CompanyService> servicePR;
        if (mPspCompany.getCompanyService(pServiceInfoDTO.getServiceCode()) == null) {
            servicePR = PayrollServices.companyManager.addService(mPspCompany.getSourceSystemCd(),
                    mPspCompany.getSourceCompanyId(),
                    pServiceInfoDTO);
        } else {
            servicePR = PayrollServices.companyManager.reactivateService(mPspCompany.getSourceSystemCd(),
                    mPspCompany.getSourceCompanyId(),
                    pServiceInfoDTO.getServiceCode());
        }

        if (!servicePR.isSuccess()) {
            MessageList messageList = servicePR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }

            if (messageList.get(0).getMessageCode().equals("1013")) {
                throw new EwsException(EwsMessages.serviceAlreadyExists(EnumUtils.getReadableName(pServiceInfoDTO.getServiceCode())));
            }

            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }
        return servicePR.getResult();
    }

    protected CompanyService addService(ServiceInfoDTO pServiceInfoDTO, Company pCompany) throws Exception {
        ProcessResult<CompanyService> servicePR;
        if (pCompany.getCompanyService(pServiceInfoDTO.getServiceCode()) == null) {
            servicePR = PayrollServices.companyManager.addService(pCompany.getSourceSystemCd(),
                    pCompany.getSourceCompanyId(),
                    pServiceInfoDTO);
        } else {
            servicePR = PayrollServices.companyManager.reactivateService(pCompany.getSourceSystemCd(),
                    pCompany.getSourceCompanyId(),
                    pServiceInfoDTO.getServiceCode());
        }

        if (!servicePR.isSuccess()) {
            MessageList messageList = servicePR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }

            if (messageList.get(0).getMessageCode().equals("1013")) {
                throw new EwsException(EwsMessages.serviceAlreadyExists(EnumUtils.getReadableName(pServiceInfoDTO.getServiceCode())));
            }

            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }
        return servicePR.getResult();
    }

    protected CompanyBankAccount addCompanyBankAccount(CompanyBankAccountDTO pCompanyBankAccountDTO, Boolean pOverrideRandomDebits) throws Exception {
        ProcessResult<CompanyBankAccount> companyBankAccountPR = PayrollServices.companyManager.addCompanyBankAccount(mPspCompany.getSourceSystemCd(),
                mPspCompany.getSourceCompanyId(),
                pCompanyBankAccountDTO,
                true,
                true);
        if (!companyBankAccountPR.isSuccess()) {
            MessageList messageList = companyBankAccountPR.getMessages();

            if (messageList.get(0).getMessageCode().equals("255")) {
                throw new EwsException(EwsMessages.dynamicError(messageList.get(0).getMessage()));
            }

            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }
        CompanyBankAccount companyBankAccount = companyBankAccountPR.getResult();

        if (pOverrideRandomDebits != null && pOverrideRandomDebits) {
            OverrideRandomDebits(companyBankAccount);
        }

        return companyBankAccount;
    }

    protected void updatePSID(String pPSID) {
        ProcessResult<Company> updateCompanyPr = PayrollServices.companyManager.updateSourceCompanyId
                (SourceSystemCode.QBDT, mPSID, pPSID);
        if (!updateCompanyPr.isSuccess()) {
            for (Message message : updateCompanyPr.getMessages()) {
                logger.error("Error updating SourceCompanyId with PSID from the AS/400. \nMessageCode: " + message.getMessageCode() + "\nMessage: " + message.getMessage());
            }
        }

        mPSID = pPSID;
        mPspCompany = updateCompanyPr.getResult();

    }

    protected void addOffer(String pOfferCode, String pPromotionId) {
        if ((pOfferCode != null && pOfferCode.trim().length() > 0) ||
                (pPromotionId != null && pPromotionId.trim().length() > 0)) {
            ProcessResult<CompanyOffer> offerPR = PayrollServices.companyManager.claimOfferForCompany(
                    pOfferCode, pPromotionId, mPspCompany);
            if (!offerPR.isSuccess()) {
                for (Message message : offerPR.getMessages()) {
                    logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                }
            }
        }
    }

    protected TaxCompanyServiceInfo addAssistedService(EwsCompany pEwsCompany,
                                                       EwsBaseServices pEwsBaseServices,
                                                       DomainEntitySet<EntitlementUnit> pEntitlementUnits,
                                                       Boolean pForceRandomDollar) throws Exception {

        TaxCompanyServiceInfo taxCompanyServiceInfo;

        EwsAssistedService assistedService = pEwsBaseServices.getAssistedService();

        EntitlementUnit entitlementUnit = null;
        for (EntitlementUnit eu : pEntitlementUnits) {
            if (eu.getEntitlement().getEntitlementCode().getAssetItemCd().in(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage)) {
                entitlementUnit = eu;
                break;
            }
        }

        if (entitlementUnit != null) {
            if (mPspCompany.getPriceType() == null) {
                CompanyDTO companyDTO = PayrollServices.dtoFactory.create(mPspCompany);
                companyDTO.setPriceType("Standard");

                ProcessResult<Company> processResult = PayrollServices.companyManager.updateCompany(mPspCompany.getSourceSystemCd(), mPspCompany.getSourceCompanyId(), companyDTO);
                if (!processResult.isSuccess()) {
                    MessageList messageList = processResult.getMessages();
                    for (Message message : messageList) {
                        logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                    }
                    if (!messageList.get(0).getMessageCode().equals("1040")) {
                        throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
                    }
                }
                mPspCompany = processResult.getResult();
            }

            //Add Tax Service
            TaxServiceInfoDTO taxServiceInfoDTO = (TaxServiceInfoDTO) PspFactory.createServiceInfoDTO(assistedService, ServiceCode.Tax, null);
            if (pEwsCompany != null) {
                if (EwsDeliveryType.electronic.equals(pEwsCompany.getW2DeliveryPreference())) {
                    taxServiceInfoDTO.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.Electronic);
                } else {
                    taxServiceInfoDTO.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.Mail);
                }

                if (EwsDeliveryType.mail.equals(pEwsCompany.getClientPacketDeliveryPreference())) {
                    taxServiceInfoDTO.setClientPacketDeliveryPreferenceCd(DeliveryPreferenceCode.Mail);
                } else {
                    taxServiceInfoDTO.setClientPacketDeliveryPreferenceCd(DeliveryPreferenceCode.Electronic);
                }
            }
            taxCompanyServiceInfo = (TaxCompanyServiceInfo) addService(taxServiceInfoDTO);
            mPspCompany = taxCompanyServiceInfo.getCompany();

            CompanyService ddCompanyService = mPspCompany.getCompanyService(ServiceCode.DirectDeposit);
            if (ddCompanyService == null || ddCompanyService.getStatusCd() == ServiceSubStatusCode.Cancelled) {
                //Add DD Service
                DDServiceInfoDTO ddServiceInfoDTO = new DDServiceInfoDTO();
                addService(ddServiceInfoDTO);
            }
        } else {
            throw new EwsException(EwsMessages.missingAssistedEntitlement());
        }

        //Add Company Bank Account only if it does not already exists or if its inactive.
        EwsBankAccount ewsBankAccount = assistedService.getEwsBankAccount();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(mPspCompany, ewsBankAccount.getAccountNumber(),
                ewsBankAccount.getRoutingNumber(), EwsBankAccountType.Checking.equals(ewsBankAccount.getAccountType()) ? BankAccountType.Checking : BankAccountType.Savings);
        if (companyBankAccount == null) {
            CompanyBankAccountDTO companyBankAccountDTO = PspFactory.createCompanyBankAccountDTO(assistedService.getEwsBankAccount());
            mCompanyBankAccount = addCompanyBankAccount(companyBankAccountDTO, pForceRandomDollar);
        }

        //Add Offer
        if (assistedService.getPromotionId() != null && assistedService.getPromotionId().length() > 0) {
            addOffer(null, assistedService.getPromotionId());
        } else {
            String autoOffer = SystemParameter.findStringValue(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE);
            if (SystemParameter.findBooleanValue(SystemParameter.Code.SPECIAL_OFFER_ACTIVE) && validateForSpecialOffer(mPspCompany)) {
                autoOffer = SystemParameter.findStringValue(SystemParameter.Code.SPECIAL_OFFER_CODE);
            }
            addOffer(autoOffer, null);
            logger.info("Company=" + mPspCompany.getSourceCompanyId() + " Offer automatically added: " + autoOffer);
        }

        return taxCompanyServiceInfo;
    }

    //Method to check special offer running
    private boolean validateForSpecialOffer(Company pCompany) {
        String specialOfferCode = SystemParameter.findStringValue(SystemParameter.Code.SPECIAL_OFFER_CODE);
        if (specialOfferCode == null) {
            logger.info("No special offer running");
            return false;
        }
        CompanyOffering companyOffering=pCompany.getOffering(ServiceCode.DirectDeposit);
         if (companyOffering == null) {
            logger.info("Company=" + pCompany.getSourceCompanyId() + " No offering attached");
            return false;
        }
        if (!companyOffering.getOffering().getOfferingCode().equals(OfferingCode.SYMFY14)) {
            logger.info("Company=" + pCompany.getSourceCompanyId() + " is not on SYMFY14 Offering");
            return false;
        }

        Offer specialOffer = Offer.findOfferByOfferCode(specialOfferCode);
        if (specialOffer == null) {
            logger.info(specialOfferCode + "is not valid offer");
            return false;
        }
        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        if (specialOffer.getEndDate() != null) {
            SpcfCalendar offerEndDate = specialOffer.getEndDate().toLocal();
            CalendarUtils.clearTime(offerEndDate);
            if (offerEndDate.before(today)) {
                logger.info("Company=" + pCompany.getSourceCompanyId() + " Special Offer " + specialOffer.getOfferCd() + " has already expired");
                return false;
            }
        }
        if (specialOffer.getEffectiveDate() != null) {
            SpcfCalendar offerEffectiveDate = specialOffer.getEffectiveDate().toLocal();
            CalendarUtils.clearTime(offerEffectiveDate);
            if (offerEffectiveDate.after(today)) {
                logger.info("Company=" + pCompany.getSourceCompanyId() + " Special Offer " + specialOffer.getOfferCd() + " is not available");
                return false;
            }
        }

        DomainEntitySet<CompanyOffer> companyOffers = pCompany.getCompanyOffers();
        for (CompanyOffer companyOffer : companyOffers) {
            if (companyOffer.getOffer().equals(Offer.findOfferByOfferCode("Twenty percent off Monthly Fees"))) {
                logger.info("Company=" + pCompany.getSourceCompanyId() + " TwentyPercentOfferRemoved");
                pCompany.cancelOfferForCompany(Offer.findOfferByOfferCode("Twenty percent off Monthly Fees"));
            }
        }
        return true;
    }


    protected CompanyService addDirectDepositService(EwsBaseServices pEwsBaseServices,
                                                     DomainEntitySet<EntitlementUnit> pEntitlementUnits,
                                                     boolean pForceRandomDollar) throws Exception {
        CompanyService companyService;
        ServiceInfoDTO serviceInfoDTO = null;
        EwsDirectDepositService directDepositService = pEwsBaseServices.getDirectDepositService();

        EntitlementUnit entitlementUnit = null;
        for (EntitlementUnit eu : pEntitlementUnits) {
            if (eu.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.DIY)) {
                entitlementUnit = eu;
                break;
            }
        }

        if (entitlementUnit != null) {
            serviceInfoDTO = new DDServiceInfoDTO();
            companyService = addService(serviceInfoDTO);
        } else {
            throw new EwsException(EwsMessages.missingDiyEntitlement());
        }

        //Add Company Bank Account only if it does not already exists or if its inactive.
        EwsBankAccount ewsBankAccount = directDepositService.getEwsBankAccount();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(mPspCompany, ewsBankAccount.getAccountNumber(),
                ewsBankAccount.getRoutingNumber(), EwsBankAccountType.Checking.equals(ewsBankAccount.getAccountType()) ? BankAccountType.Checking : BankAccountType.Savings);
        if (companyBankAccount == null) {
            CompanyBankAccountDTO companyBankAccountDTO = PspFactory.createCompanyBankAccountDTO(directDepositService.getEwsBankAccount());
            mCompanyBankAccount = addCompanyBankAccount(companyBankAccountDTO, pForceRandomDollar);
        }

        //Add Offer
        addOffer(directDepositService.getOfferCode(), null);

        return companyService;
    }

    protected CompanyService addBillPaymentService(EwsBaseServices pEwsBaseServices) throws Exception {
        CompanyService companyService;
        ServiceInfoDTO serviceInfoDTO = null;
        EwsBaseService ewsBaseService = pEwsBaseServices.getBillPayment();

        serviceInfoDTO = PspFactory.createServiceInfoDTO(ewsBaseService, ServiceCode.BillPayment, null);
        companyService = addService(serviceInfoDTO);

        return companyService;
    }

    protected CompanyService addViewMyPaycheckService(EwsBaseServices pEwsBaseServices) throws Exception {
        CompanyService companyService;
        ServiceInfoDTO serviceInfoDTO = null;
        EwsBaseService ewsBaseService = pEwsBaseServices.getViewMyPaycheck();

        serviceInfoDTO = PspFactory.createServiceInfoDTO(ewsBaseService, ServiceCode.ViewMyPaycheck, null);
        companyService = addService(serviceInfoDTO);

        return companyService;
    }

    protected CompanyService addCloudV2Service(EwsBaseServices pEwsBaseServices) throws Exception {
        CompanyService companyService;
        ServiceInfoDTO serviceInfoDTO = null;
        EwsBaseService ewsBaseService = pEwsBaseServices.getCloudV2();
        serviceInfoDTO = PspFactory.createServiceInfoDTO(ewsBaseService, ServiceCode.CloudV2, null);
        companyService = addService(serviceInfoDTO);
        return companyService;
    }

    public abstract EwsResponse execute();

    protected abstract void validate() throws Exception;

    protected abstract EwsResponse process() throws Exception;

}
