package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsRuntimeException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;
import com.intuit.sbd.payroll.psp.common.utils.log.MoneyMovementLogHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ConstantValues;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateTRONGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.authorization.utils.RequestSourceIdentifier;
import com.intuit.sbg.psp.walletservice.WalletClientException;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FlushMode;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jeff Jones
 */
public class AddServiceProcess extends BaseProcess {

    private EwsAddService mRequest;
    private static final SpcfLogger logger;

    private EwsCreateAccount mCreateAccountRequest;

    private boolean createdNewAccount = false;
    private boolean had401k = false;
    private boolean hadBillPayment = false;

    private TransmissionsList mCreateAccountTransmissionsList;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    private BankAccountDeTokenizer bankAccountDeTokenizer;

    private RequestSourceIdentifier requestSourceIdentifier;
    private AddOrUpdateTRONGrantProcessor addOrUpdateTRONGrantProcessor;

    private WalletBankAccountUtil walletBankAccountUtil;


    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public AddServiceProcess(EwsAddService pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);
        requestSourceIdentifier = PayrollApplicationBeanFactory.getBean(RequestSourceIdentifier.class);

        bankAccountDeTokenizer = new BankAccountDeTokenizer();
        walletBankAccountUtil = new WalletBankAccountUtil();

        logger.info("Processing Add_Service Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsAddServiceResponse execute() {
        EwsAddServiceResponse response = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.AddService);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            if (mRequest.getEwsBaseServices().getAssistedService() != null && !createdNewAccount) {

                CompanyBankAccount activeCBA = CompanyBankAccount.findActiveCompanyBankAccount(mPspCompany);
                if (mRequest.getPin() == null || activeCBA == null) {

                    String newPsid = PayrollServices.companyManager.createSourceCompanyId(SourceSystemCode.QBDT,
                                                                                          mPspCompany.getLegalAddress().getState());
                    updatePSID(newPsid);
                    response.setPsid(mPSID);

                    //Activate Feature
                    if (mPspCompany.getActivePrimaryEntitlementUnit() != null &&
                            mPspCompany.getActivePrimaryEntitlementUnit().getEntitlement() != null) {
                        mSubscriptionNumber = mPspCompany.getActivePrimaryEntitlementUnit().getEntitlement().getSubscriptionNumber();
                    }

                } else {
                    //Migrate Account
                    List<EntitlementUnit> entitlementUnits = new ArrayList<EntitlementUnit>();
                    entitlementUnits.add(mPspCompany.getActivePrimaryEntitlementUnit());

                }

            }

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {

            response = new EwsAddServiceResponse();

            try {
                updateTronFlag(e.getCode());
                processEwsException(e, response);
            }catch(EwsException exception){
               processEwsException(exception, response);
            }

        } catch (Throwable t){
            response = new EwsAddServiceResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();

            if (mCreateAccountTransmissionsList != null) {
                try {
                    mPSPTransmission.setFinalizeDateTime(PSPDate.getPSPTime());
                    mPSPTransmission.setResponse(response);

                    LoggingUtils.logTransmissions(mCreateAccountTransmissionsList);
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }

            try {
                removeLoggerContext();
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
        detokenizeBankAccountNumber(mRequest.getEwsBaseServices());
        mPspCompany = PspFactory.findCompany(mPSID);
        setWalletBankAccountNumber(mRequest.getEwsBaseServices());
        mRequest.validate();
        setLoggerContext();
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());

        //Validate the company has an active EntitlementUnit
        if (mPspCompany.getActivePrimaryEntitlementUnit() == null) {
            throw new EwsException(EwsMessages.noActiveEntitlementUnit());
        }

        if (mRequest.getPin() != null) {
            ProcessResult<Company> authenticationPR = PayrollServices.subscriptionManager.verifyCompanyPIN
                    (SourceSystemCode.QBDT, mPSID, mRequest.getPin());
            if (!authenticationPR.isSuccess()) {
                //Commit psp validation logic
                PayrollServices.commitUnitOfWork();
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                MessageList messageList = authenticationPR.getMessages();
                for (Message message : messageList) {
                    logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                }
                throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
            }
            mPspCompany = authenticationPR.getResult();
        }
        moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.AddService,"Completed Validation");
    }

    @Override
    protected EwsAddServiceResponse process() throws Exception {
        moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.AddService,"Started process");
        EwsAddServiceResponse response = new EwsAddServiceResponse();

        //Add Services
        CompanyService companyService;
        ServiceInfoDTO serviceInfoDTO = null;
        EwsServicesResponse ewsServicesResponse;
        DomainEntitySet<EntitlementUnit> entitlementUnits = null;
        EwsBaseServices ewsBaseServices = mRequest.getEwsBaseServices();

        entitlementUnits = mPspCompany.getEntitlementUnitCollection();

        ewsServicesResponse = new EwsServicesResponse();

        // Set OII Flags related to Direct Deposit/ Money Movement
        setMoneyMovementOnboarding(ewsBaseServices, mPspCompany, requestSourceIdentifier.isPayrollPlugin());
        
        if (ewsBaseServices.getAssistedService() != null) {
            if (mPspCompany.hasService(ServiceCode.Tax)) {
                createdNewAccount = true;

                cancelServices();

                // update company entitlement status
                EntitlementUnit entitlementUnit = mPspCompany.getActivePrimaryEntitlementUnit();
                EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
                entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);

                ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit
                        (mPspCompany.getSourceSystemCd(), mPspCompany.getSourceCompanyId(), entitlementUnitDTO);
                if (!processResult.isSuccess()) {
                    MessageList messageList = processResult.getMessages();
                    for (Message message : messageList) {
                        logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                    }
                    moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.AddService,"Error in process");
                    throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
                }

                //Flush because all services were cancelled and we are creating a new company
                Application.getHibernateSession().flush();
                try {
                    mCreateAccountRequest = EwsFactory.createEwsCreateAccount(mRequest, mPspCompany, entitlementUnit, hadBillPayment);
                } catch (EwsRuntimeException e){
                    logger.error("Exception in add service while create account with NGP psid="+mPspCompany.getSourceCompanyId(),e);
                    throw new EwsException(e.getEwsMessage());
                }
                CreateAccountProcess process = new CreateAccountProcess(mCreateAccountRequest, true);
                EwsCreateAccountResponse createAccountResponse = process.execute();
                mCreateAccountTransmissionsList = process.getTransmissionsList();

                if (createAccountResponse.getEwsResponseStatus().getCode() != 0) {
                    moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.AddService,
                            "Error in process",
                            "EwsResponseStatus Code !=0");
                    throw new EwsException(EwsMessages.systemError());
                }

                //Flush again to find the newly created company
                Application.getHibernateSession().flush();

                String newPSID = createAccountResponse.getPsid();
                Company newCompany = PspFactory.findCompany(newPSID);
                setMoneyMovementOnboarding(mRequest.getEwsBaseServices(), newCompany, requestSourceIdentifier.isPayrollPlugin());
                ewsServicesResponse.setAssistedResponse(createAccountResponse.getEwsServicesResponse().getAssistedResponse());

                if (had401k) {
                    migrateThirdParty401kService((ThirdParty401kCompanyServiceInfo) mPspCompany.getService(ServiceCode.ThirdParty401k), newCompany);
                }

                mPSID = newPSID;
                mPspCompany = newCompany;
                entitlementUnits = mPspCompany.getEntitlementUnitCollection();
            } else {
                TaxCompanyServiceInfo taxCompanyServiceInfo = addAssistedService(null, ewsBaseServices, entitlementUnits, mRequest.getForceRandomDollar(), isPSPRandomDollarVerificationRequired());
                CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);
                ewsServicesResponse.setAssistedResponse(EwsFactory.createEwsAssistedServiceResponse(mPspCompany, taxCompanyServiceInfo, companyBankAccount));
            }
        } else {
            if (EwsFactory.isCompanyAssisted(mPspCompany)) {
                TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) mPspCompany.getCompanyService(ServiceCode.Tax);
                if (taxCompanyServiceInfo != null) {
                    CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);
                    ewsServicesResponse.setAssistedResponse(EwsFactory.createEwsAssistedServiceResponse(mPspCompany, taxCompanyServiceInfo, companyBankAccount));
                }
            }

            if (EwsFactory.isCompanyMigrating(mPspCompany)) {
                companyService = mPspCompany.getCompanyService(ServiceCode.DirectDeposit);
                if (companyService != null) {
                    CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);
                    ewsServicesResponse.setDirectDepositResponse(EwsFactory.createEwsDirectDepositServiceResponse(companyService, companyBankAccount));
                }
            }
        }

        companyService = mPspCompany.getCompanyService(ServiceCode.Cloud);
        if (companyService != null) {
            ewsServicesResponse.setCloudResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
        }

        if (ewsBaseServices.getDirectDepositService() != null) {
            companyService = addDirectDepositService(ewsBaseServices, entitlementUnits, mRequest.getForceRandomDollar(), isPSPRandomDollarVerificationRequired());
            CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);
            ewsServicesResponse.setDirectDepositResponse(EwsFactory.createEwsDirectDepositServiceResponse(companyService, companyBankAccount));
        } else {
            if (!EwsFactory.isCompanyAssisted(mPspCompany) || EwsFactory.isCompanyMigrating(mPspCompany)) {
                companyService = mPspCompany.getCompanyService(ServiceCode.DirectDeposit);
                if (companyService != null) {
                    CompanyBankAccount companyBankAccount = PspFactory.findCompanyBankAccount(mPspCompany);
                    ewsServicesResponse.setDirectDepositResponse(EwsFactory.createEwsDirectDepositServiceResponse(companyService, companyBankAccount));
                }
            }
        }

        if (ewsBaseServices.getBillPayment() != null) {
            EwsBaseService billPaymentService = ewsBaseServices.getBillPayment();
            serviceInfoDTO = PspFactory.createServiceInfoDTO(billPaymentService, ServiceCode.BillPayment, null);
            companyService = addService(serviceInfoDTO);
            ewsServicesResponse.setBillPaymentResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
        } else {
            companyService = mPspCompany.getCompanyService(ServiceCode.BillPayment);
            if (companyService != null) {
                ewsServicesResponse.setBillPaymentResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
            }
        }

        if(ewsBaseServices.getViewMyPaycheck() != null && !mPspCompany.isCompanyOnService(ServiceCode.ViewMyPaycheck)) {
            EwsBaseService viewMyPaycheckService = ewsBaseServices.getViewMyPaycheck();
            serviceInfoDTO = PspFactory.createServiceInfoDTO(viewMyPaycheckService, ServiceCode.ViewMyPaycheck, null);
            companyService = addService(serviceInfoDTO);

            //Specifically for AutoEnableFlow - Sets OII and CompanyEvent
            setOIICompanyEventAutoEnableVMP(mPspCompany);
            ewsServicesResponse.setViewMyPaycheckResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
        } else {
            companyService = mPspCompany.getCompanyService(ServiceCode.ViewMyPaycheck);
            if(companyService != null) {
                ewsServicesResponse.setViewMyPaycheckResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
            }
        }

        if(ewsBaseServices.getCloudV2() != null && !mPspCompany.isCompanyOnService(ServiceCode.CloudV2)) {
            EwsBaseService cloudV2Service = ewsBaseServices.getCloudV2();
            serviceInfoDTO = PspFactory.createServiceInfoDTO(cloudV2Service, ServiceCode.CloudV2, null);
            companyService = addService(serviceInfoDTO);
            ewsServicesResponse.setCloudV2Response(EwsFactory.createEwsBaseServiceResponse(companyService));
        } else {
            companyService = mPspCompany.getCompanyService(ServiceCode.CloudV2);
            if(companyService != null) {
                ewsServicesResponse.setCloudV2Response(EwsFactory.createEwsBaseServiceResponse(companyService));
            }
        }

        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setEwsServicesResponse(ewsServicesResponse);
        moneyMovementTrackingLog(logger, MoneyMovementLogHelper.EventType.AddService,"Completed process");
        return response;
    }

    private void cancelServices() throws Exception {
        ProcessResult pr = new ProcessResult();
        if (this.mPspCompany.hasService(ServiceCode.Tax)) {
            // cancel all existing services
            DomainEntitySet<CompanyService> activeServices =
                    this.mPspCompany.getCompanyServiceCollection().find(CompanyService.StatusCd().notIn
                            (ServiceSubStatusCode.Cancelled, ServiceSubStatusCode.Terminated));
            for (CompanyService companyService : activeServices) {
                switch (companyService.getService().getServiceCd()) {
                    case BillPayment:
                        hadBillPayment = true;
                        break;
                    case ThirdParty401k:
                        had401k = true;
                        break;
                }
                        pr.merge(PayrollServices.companyManager.updateServiceStatus(this.mPspCompany.getSourceSystemCd(), this.mPspCompany.getSourceCompanyId(), companyService.getService().getServiceCd(), ServiceSubStatusCode.Cancelled));
                        assertSuccess(pr);
                }
            }
        }
    
    private void assertSuccess(ProcessResult pProcessResult) throws Exception {
        if (!pProcessResult.isSuccess()) {
            MessageList messageList = pProcessResult.getMessages();
            for (Message message : messageList) {
                logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
            }
            throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
        }
    }

    protected void migrateThirdParty401kService(ThirdParty401kCompanyServiceInfo pExistingService, Company pNewCompany) throws Exception {
        ThirdParty401kServiceInfoDTO tp401kServiceInfoDTO = new ThirdParty401kServiceInfoDTO();
        tp401kServiceInfoDTO.setCustodialId(pExistingService.getCustodialId());
        tp401kServiceInfoDTO.setServiceStartDate(pExistingService.getServiceStartDate());
        tp401kServiceInfoDTO.setHasSafeHarbor(pExistingService.getHasSafeHarbor());

        addService(tp401kServiceInfoDTO, pNewCompany);
    }

    /**
     * setOIICompanyEventAutoEnableVMP(company)
     *     //Takes company object as parameter
     *     //0. Checks whether the addition header is present in request header or not
     *     //1. Sets OII BIT for the company in PSP_COMPANY TABLE
     *     //2. Create a company event i. AutoEnabledVMP with 3 event detail ( UserID. PSID, Description -sourceName )
     */
    private void setOIICompanyEventAutoEnableVMP(Company company) {

        //0. sourceName specifies the request source present in request header. If null, skip this function
        String sourceName = RequestAttributesUtils.getAttribute(ConstantValues.HEADER_INTUIT_AUTO_VMP_SOURCE, String.class);

        //If object is null, request will be considered from EWS and return
        if(Objects.isNull(sourceName)){
            logger.info(String.format("Add VMP Service completed from EWS PSID=%s", company.getSourceCompanyId()));
            return;
        }
        logger.info(String.format("Add Auto VMP Service completed from %s realmId=%s PSID=%s",sourceName, company.getIAMRealmId(), company.getSourceCompanyId()));

        //1. Setting the bit in OII Flag, if not already set
        if(!company.isVMPEnabled()){
            ProcessResult processResult = PayrollServices.companyManager.
                    addOrUpdateCompanyIdentity(company, ConstantValues.OII_FLAG_ENABLE_VMP_BIT);

            if(!processResult.isSuccess()) {
                logger.error("Unable to addOrUpdateCompanyIdentity for PSID="+company.getSourceSystemCompanyId()+" because "+getErrorMessage(processResult.getErrorMessages()));
            } else {
                logger.info("Successfully Added OrUpdated CompanyIdentity for PSID="+company.getSourceSystemCompanyId());
            }
        }

        //2. Create a company event AutoEnabledVMP with sourceName, authId, PSID
        String authId = null;
        AuthorizationContext authorizationContext = RequestAttributesUtils.getAttribute(ContextConstants.USER_AUTHORIZATION_CONTEXT, AuthorizationContext.class);
        if(Objects.nonNull(authorizationContext)){
            authId = authorizationContext.getUserId();
        }
        CompanyEvent.createAutoEnableVMPEvent(company, authId, sourceName);
    }

    private void setMoneyMovementOnboarding(EwsBaseServices ewsBaseServices, Company company, boolean isPayrollPlugin) {
        if(Objects.isNull(ewsBaseServices)) {
            return;
        }

        if(!isPayrollPlugin) {
            logger.info("Add DD Service initiated from EWS");
            return;
        }

        EwsDirectDepositService ewsDirectDepositService = ewsBaseServices.getDirectDepositService();
        EwsAssistedService ewsAssistedService = ewsBaseServices.getAssistedService();

        if(Objects.isNull(ewsDirectDepositService) && Objects.isNull(ewsAssistedService)) {
            return;
        }

        String serviceType = Objects.nonNull(ewsDirectDepositService)? "DD": "Assisted";
        logger.info(String.format("Add %s Service initiated from Payroll Plugin", serviceType));
        ProcessResult processResult = PayrollServices.companyManager.
                addOrUpdateCompanyIdentity(company, "ENABLE_DIRECT_DEPOSIT");

        if(!processResult.isSuccess()) {
            logger.error("Unable to addOrUpdateCompanyIdentity for PSID="+company.getSourceSystemCompanyId()+" because "+getErrorMessage(processResult.getErrorMessages()));
        } else {
            logger.info("Successfully Added OrUpdated CompanyIdentity for PSID="+company.getSourceSystemCompanyId());
        }

    }

    private boolean isPSPRandomDollarVerificationRequired() {
        return !requestSourceIdentifier.isPayrollPlugin();
    }

    private void detokenizeBankAccountNumber(EwsBaseServices ewsBaseServices) throws EwsException {
        if(Objects.isNull(ewsBaseServices.getDirectDepositService()) && Objects.isNull(ewsBaseServices.getAssistedService())) {
            logger.info("Skipping detokenizing Bank Account Number because it is not a AddDirectDepositService or AddAssistedService workflow");
            return;
        }

        EwsBankAccount ewsBankAccount = getEwsBankAccount(ewsBaseServices);

        if(Objects.isNull(ewsBankAccount)) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AccountNumber", "BankAccount"));
        }

        String accountNumber = ewsBankAccount.getAccountNumber();

        if(!bankAccountDeTokenizer.isBanToken(accountNumber)) {
            return;
        }

        try {
            String detokenizedBankAccount = bankAccountDeTokenizer.getDetokenizedBankAccount(accountNumber);

            if(StringUtils.isEmpty(detokenizedBankAccount)) {
                throw new EwsException(EwsMessages.fieldDataNotValid("AccountNumber", "BankAccount"));
            }

            ewsBankAccount.setAccountNumber(detokenizedBankAccount);
        } catch (WalletClientException | HttpClientErrorException | CallNotPermittedException excp) {
            handleException(accountNumber, excp);
        }
    }

    /**
     *
     * @param accountNumber
     * @param exception
     * @throws EwsException
     */
    private void handleException(String accountNumber, Exception exception) throws EwsException {
        logger.error("Unable to detokenize banToken="+accountNumber, exception);
        throw new EwsException(EwsMessages.fieldDataNotValid("AccountNumber", "BankAccount"));
    }


    private void setWalletBankAccountNumber(EwsBaseServices ewsBaseServices) throws EwsException {
        if (Objects.isNull(ewsBaseServices.getDirectDepositService()) && Objects.isNull(ewsBaseServices.getAssistedService())) {
            logger.info("Skipping Bank Account from Wallet because it is not a AddDirectDepositService or AddAssistedService workflow");
            return;
        }

        EwsBankAccount ewsBankAccount = getEwsBankAccount(ewsBaseServices);

        if(Objects.isNull(ewsBankAccount)) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AccountNumber", "BankAccount"));
        }

        String accountNumber = ewsBankAccount.getAccountNumber();

        if(!walletBankAccountUtil.isValidWalletId(accountNumber)) {
            return;
        }

        try {
            WalletBankAccount walletBankAccount = walletBankAccountUtil.getWalletBankAccount(accountNumber, mPspCompany.getIAMRealmId());

            if(null == walletBankAccount) {
                throw new EwsException(EwsMessages.fieldDataNotValid("Account", "BankAccount"));
            }

            logger.info("Encrypted_AC_Num=" + EncryptionUtils.deterministicEncrypt(Payee.AccountNumberKeyName, walletBankAccount.getAccountNumber()));
            ewsBankAccount.setAccountNumber(walletBankAccount.getAccountNumber());
        } catch (Exception e) {
            logger.error("Unable to get Bank Account from Wallet="+accountNumber, e);
            throw new EwsException(EwsMessages.fieldDataNotValid("AccountNumber", "BankAccount"));
        }
    }



    private EwsBankAccount getEwsBankAccount(EwsBaseServices ewsBaseServices) {
        EwsDirectDepositService ewsDirectDepositService = ewsBaseServices.getDirectDepositService();

        if(Objects.nonNull(ewsDirectDepositService)) {
            return ewsDirectDepositService.getEwsBankAccount();
        }

        EwsAssistedService ewsAssistedService = ewsBaseServices.getAssistedService();

        if(Objects.nonNull(ewsAssistedService)) {
            return ewsAssistedService.getEwsBankAccount();
        }

        return null;
    }

    private String getErrorMessage(MessageList messageList) {
        if(Objects.isNull(messageList)){
            return StringUtils.EMPTY;
        }

        StringBuffer errorMessage = new StringBuffer();
        for (Message message : messageList) {
            errorMessage.append("MessageCode: ").append(message.getMessageCode())
                    .append(" Message: ").append(message.getMessage());
        }
        return errorMessage.toString();
    }

    private void updateTronFlag(int errorCode) throws EwsException {
        logger.info("Error code for Add Service is =+"+errorCode+" PSID="+mPSID);
        if(errorCode != 30137)
        {
            return;
        }

        if(!requestSourceIdentifier.isPayrollPlugin()){
            logger.info("Add DD Service initiated from EWS so skipping handling of ERROR");
            return;
        }
        addOrUpdateTRONGrantProcessor = new AddOrUpdateTRONGrantProcessor(mPspCompany);
        ProcessResult processResult= addOrUpdateTRONGrantProcessor.validate();
        if (!processResult.isSuccess()) {
            throw new EwsException(EwsMessages.IUSUpdatesFailed());
        }
        processResult = addOrUpdateTRONGrantProcessor.process();
        if (!processResult.isSuccess()) {
            throw new EwsException(EwsMessages.IUSUpdatesFailed());
        }

        logger.info("updating the OII flag in TransactionThread for the Exception case for PSID =+"+mPSID);


        PayrollServices.executeTransactionThread(new TransactionThread() {

            public ProcessResult transaction() {
                Company  localCompany = Application.findById(Company.class, mPspCompany.getId());
                setMoneyMovementOnboarding(mRequest.getEwsBaseServices(), localCompany,true);
                Application.save(localCompany);
                return new ProcessResult();
                }
            });



    }
}
