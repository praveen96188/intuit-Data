package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsAdapterConst;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Jeff Jones
 */
public class CreateAccountProcess extends BaseProcess {
    private EwsCreateAccount mRequest;
    private DomainEntitySet<EntitlementUnit> mEntitlementUnits;
    private static final SpcfLogger logger;
    private boolean mIsSecondaryProcess;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;
    private List<String> assistedItemNumbers;

    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public CreateAccountProcess(EwsCreateAccount pRequest, boolean pIsSecondaryProcess) {
        mRequest = pRequest;
        mIsSecondaryProcess = pIsSecondaryProcess;
        this.mEIN = pRequest.getEwsCompany().getEin();

        mEntitlementUnits = new DomainEntitySet<EntitlementUnit>();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);
        String assistedItemNumber = ConfigurationManager.getSettingValue(ConfigurationModule.EwsAdapter,
                EwsAdapterConst.ASSISTED_ITEM_NUMBER);
        if(Objects.nonNull(assistedItemNumber))
            assistedItemNumbers = Arrays.asList(assistedItemNumber.split(",", -1));
        logger.info("Processing Create_Account Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsCreateAccountResponse execute() {
        EwsCreateAccountResponse response = null;

        try {
            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.CreateAccount);
            mPSPTransmission.setRequest(mRequest);

            if (!mIsSecondaryProcess) {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            }

            validate();
            response = process();

            if (PspFactory.isActiveOnService(mPspCompany, ServiceCode.Tax)) {

                // TODO: TLD: Is this an accurate way to determine the Company's legal state?
                updatePSID(PayrollServices.companyManager.createSourceCompanyId(SourceSystemCode.QBDT,
                                                                                mPspCompany.getLegalAddress().getState()));
                response.setPsid(mPSID);

                if (!mIsSecondaryProcess) {
                    PayrollServices.commitUnitOfWork();
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                }

                //Company has already been saved in the AS400 if these fail we will just send back a partial response.
                //Activate Feature
                if (mPspCompany.getActivePrimaryEntitlementUnit() != null &&
                        mPspCompany.getActivePrimaryEntitlementUnit().getEntitlement() != null) {
                    mSubscriptionNumber = mPspCompany.getActivePrimaryEntitlementUnit().getEntitlement().getSubscriptionNumber();
                }
            }

            if (!mIsSecondaryProcess) {
                PayrollServices.commitUnitOfWork();
            }
        } catch (EwsException e) {
            response = new EwsCreateAccountResponse();
            processEwsException(e, response);
        } catch (Throwable t) {
            response = new EwsCreateAccountResponse();
            processThrowable(t, response);
        } finally {
            if (!mIsSecondaryProcess) {
                PayrollServices.rollbackUnitOfWork();
            }

            try {
                mPSPTransmission.setFinalizeDateTime(PSPDate.getPSPTime());
                mPSPTransmission.setResponse(response);

                if (!mIsSecondaryProcess) {
                    LoggingUtils.logTransmissions(mTransmissionsList);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();

        if (mRequest.getEwsServices().getCloudService() == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Cloud"));
        }
        
        //Service Specific Validation
        EwsCompany ewsCompany = mRequest.getEwsCompany();
        EwsServices ewsServices = mRequest.getEwsServices();

        if (ewsServices.getDirectDepositService() != null && ewsServices.getAssistedService() != null) {
            throw new EwsException(EwsMessages.ddAndAssistedServiceExistError());
        }

        if (ewsServices.getAssistedService() != null) {
            //Assisted Customer
            if (mRequest.getEwsCompany().getLegalInfo() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("LegalInfo"));
            }
        } else if (ewsServices.getDirectDepositService() != null) {
            //DD Customer
            if (mRequest.getEwsCompany().getLegalInfo() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("LegalInfo"));
            }

            if (!Validation.validateValue(ewsCompany.getDba(), false, "^(\\P{M}\\p{M}*){1,100}$")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("DBA", "Company"));
            }

            if (mRequest.getEwsCompany().getMailingAddress() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("MailingAddress"));
            }

            validateEditionAndTier(false, false);
        } else {
            //DIY or Assisted (with assisted eoc & item id and without adding the assisted in service)
            if (!Validation.validateValue(ewsCompany.getDba(), false, "^(\\P{M}\\p{M}*){1,100}$")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("DBA", "Company"));
            }

            if (ewsCompany.getMailingAddress() == null) {
                throw new EwsException(EwsMessages.objectCanNotBeNull("MailingAddress"));
            }
            ewsCompany.getMailingAddress().validate();

            validateEditionAndTier(true, true);
        }
       
        if (mRequest.getEwsCompany().getQuickBooks() != null) {
            mRequest.getEwsCompany().getQuickBooks().validate();
        }
    }

    private void validateEditionAndTier(boolean pAssistedEntitlementCheck, boolean pValidateBillingDetails) throws EwsException {
        PspPrincipal principal = Application.getCurrentPrincipal();
        for (EwsEntitlement ewsEntitlement : mRequest.getEwsEntitlements()) {

            if (pValidateBillingDetails && ewsEntitlement.getEwsBillingDetails() != null) {
                ewsEntitlement.getEwsBillingDetails().validate();
            }

            if (pAssistedEntitlementCheck && isEntitlementAssisted(ewsEntitlement)) {
                continue;
            }
            logger.info("Not an assisted Item Number = "+ ewsEntitlement.getAssetItemNumber());
            if (ewsEntitlement.getEdition() == null && !principal.isAgent()) {
                throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Edition", "Entitlement"));
            }

            if (ewsEntitlement.getTier() == null && !principal.isAgent() && !EntitlementCode.assetItemNumberUsesUsageBilling(ewsEntitlement.getAssetItemNumber())) {
                throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Tier", "Entitlement"));
            }

        }
    }

    private boolean isEntitlementAssisted(EwsEntitlement ewsEntitlement) {
        if (Objects.nonNull(assistedItemNumbers) && assistedItemNumbers.contains(ewsEntitlement.getAssetItemNumber()))
            return true;
        return false;
    }

    @Override
    protected EwsCreateAccountResponse process() throws Exception {
        EwsCreateAccountResponse response = new EwsCreateAccountResponse();

        response.setCompanyResponse(addCompany());
        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setEwsEntitlementUnitResponses(addEntitlements());
        response.setEwsServicesResponse(addServices(response.getCompanyResponse()));

        return response;
    }

    private EwsCompanyResponse addCompany() throws Exception{
        CompanyDTO companyDTO = PspFactory.createCompanyDTO(mRequest.getEwsCompany());
        ProcessResult<Company> companyPR = PayrollServices.companyManager.addCompany(companyDTO);
        if (!companyPR.isSuccess()) {
            MessageList messageList = companyPR.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }
            if (!messageList.get(0).getMessageCode().equals("1040")) {
                throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
            }
        }
        mPspCompany = companyPR.getResult();
        mPSID = mPspCompany.getSourceCompanyId();
        mEIN = mPspCompany.getFedTaxId();
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());

        return EwsFactory.createEwsCompanyResponse(mPspCompany);
    }

    private ArrayList<EwsEntitlementUnitResponse> addEntitlements() throws Exception{
        for (EwsEntitlement ewsEntitlement : mRequest.getEwsEntitlements()) {
            EntitlementUnitDTO entitlementUnitDTO = PspFactory.createCompanyEntitlementDTO(ewsEntitlement, mEIN);
            entitlementUnitDTO.setFedTaxId(mPspCompany.getFedTaxId());
            ProcessResult<EntitlementUnit> entitlementUnitPR = PayrollServices.entitlementManager
                    .addOrUpdateEntitlementUnit(mPspCompany.getSourceSystemCd(), mPspCompany.getSourceCompanyId(),
                            entitlementUnitDTO);
            if (!entitlementUnitPR.isSuccess()) {
                MessageList messageList = entitlementUnitPR.getMessages();
                for (Message message : messageList) {
                    logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                }
                throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
            }
            EntitlementUnit entitlementUnit = entitlementUnitPR.getResult();
            mEntitlementUnits.add(entitlementUnit);

            mSubscriptionNumber = entitlementUnit.getEntitlement().getSubscriptionNumber();

            if (entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.Assisted) ||
                    entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemCd().equals(AssetItemCode.AssistedAdvantage)) {
                mAddEin = ewsEntitlement.getAddEin();
            }
        }

        return EwsFactory.createEwsEntitlementUnitResponses(mEntitlementUnits);
    }

    private EwsServicesResponse addServices(EwsCompanyResponse pEwsCompanyResponse) throws Exception{
        //Add Services
        ServiceInfoDTO serviceInfoDTO;
        CompanyService companyService;
        EwsServicesResponse ewsServicesResponse;
        EwsServices ewsServices = mRequest.getEwsServices();

        EwsBaseService cloudService = ewsServices.getCloudService();
        serviceInfoDTO = PspFactory.createServiceInfoDTO(cloudService, ServiceCode.Cloud, null);
        companyService = addService(serviceInfoDTO);

        ewsServicesResponse = new EwsServicesResponse();

        ewsServicesResponse.setCloudResponse(EwsFactory.createEwsBaseServiceResponse(companyService));

        if (ewsServices.getDirectDepositService() != null) {
            companyService = addDirectDepositService(ewsServices, mEntitlementUnits, mRequest.getForceRandomDollar());
            ewsServicesResponse.setDirectDepositResponse(EwsFactory.createEwsDirectDepositServiceResponse(companyService, mCompanyBankAccount));
        } else if (ewsServices.getAssistedService() != null) {
            TaxCompanyServiceInfo taxCompanyServiceInfo = addAssistedService(mRequest.getEwsCompany(), ewsServices, mEntitlementUnits, mRequest.getForceRandomDollar());
            ewsServicesResponse.setAssistedResponse(EwsFactory.createEwsAssistedServiceResponse(mPspCompany, taxCompanyServiceInfo, mCompanyBankAccount, pEwsCompanyResponse));
        }

        if (ewsServices.getBillPayment() != null) {
            companyService = addBillPaymentService(ewsServices);
            ewsServicesResponse.setBillPaymentResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
        }

        if (ewsServices.getViewMyPaycheck() != null) {
            companyService = addViewMyPaycheckService(ewsServices);
            ewsServicesResponse.setViewMyPaycheckResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
        }

        //Add tp401k service if ein exits in signup queue
        /*
        companyService = addThirdParty401kService();
        if (companyService != null) {
            ewsServicesResponse.setThirdParty401kResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
        }
        */

        if (mPspCompany.isCompanyOnService(ServiceCode.CloudV2)) {
            companyService = mPspCompany.getCompanyService(ServiceCode.CloudV2);
        } else if (ewsServices.getCloudV2() != null) {
            companyService = addCloudV2Service(ewsServices);
        } else {
            companyService = null;
        }
        if(companyService != null) {
            ewsServicesResponse.setCloudV2Response(EwsFactory.createEwsBaseServiceResponse(companyService));
        }

        return ewsServicesResponse;
    }

    protected CompanyService addThirdParty401kService() {
        CompanyService companyService = null;

        ThirdParty401kSignUpQueue tp401kSignUpQueue =
                ThirdParty401kSignUpQueue.findThirdParty401kSignUpQueue(mPspCompany.getFedTaxId());
        if (tp401kSignUpQueue != null) {
            if (ThirdParty401kSignUpQueueStatusCode.Pending.equals(tp401kSignUpQueue.getStatus())) {

                DomainEntitySet<CompanyService> companyServices = CompanyService.findActiveCompanyServicesByFeinExcludingSourceSystemIdAndServiceCode
                        (mPspCompany.getSourceSystemCd(), mPspCompany.getSourceCompanyId(), mPspCompany.getFedTaxId(), ServiceCode.Cloud);
                if (companyServices.isEmpty()) {
                    ThirdParty401kServiceInfoDTO tp401kServiceInfoDTO = new ThirdParty401kServiceInfoDTO();
                    tp401kServiceInfoDTO.setCustodialId(tp401kSignUpQueue.getCustodialId());
                    tp401kServiceInfoDTO.setServiceStartDate(tp401kSignUpQueue.getEffectiveDate());
                    tp401kServiceInfoDTO.setHasSafeHarbor(tp401kSignUpQueue.getHasSafeHarbor());

                    try {
                        companyService = addService(tp401kServiceInfoDTO);

                        tp401kSignUpQueue.setStatus(ThirdParty401kSignUpQueueStatusCode.Processed);
                        Application.save(tp401kSignUpQueue);
                    } catch (Exception e) {
                        logger.error("PSID: " + mPSID + " " + e.getMessage(), e);
                    }
                } else {
                    logger.error("Unable to add 401k service for FEIN " + mPspCompany.getFedTaxId() + ", more than one active company was found.");
                }
            }
        }

        return companyService;
    }

    public TransmissionsList getTransmissionsList() {
        return mTransmissionsList;
    }
}
