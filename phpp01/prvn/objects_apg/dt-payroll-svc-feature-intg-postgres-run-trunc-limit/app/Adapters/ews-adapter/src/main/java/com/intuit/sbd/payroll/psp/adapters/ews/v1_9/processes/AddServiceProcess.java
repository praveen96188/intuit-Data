package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.PSIMessageWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.As400Factory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

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

    static {
        logger = PayrollServices.getLogger(CreateAccountProcess.class);
    }

    public AddServiceProcess(EwsAddService pRequest) {
        mRequest = pRequest;
        this.mPSID = pRequest.getPsid();

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

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
            processEwsException(e, response);
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

        mPspCompany = PspFactory.findCompany(mPSID);
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
    }

    @Override
    protected EwsAddServiceResponse process() throws Exception {
        EwsAddServiceResponse response = new EwsAddServiceResponse();

        //Add Services
        CompanyService companyService;
        ServiceInfoDTO serviceInfoDTO = null;
        EwsServicesResponse ewsServicesResponse;
        DomainEntitySet<EntitlementUnit> entitlementUnits = null;
        EwsBaseServices ewsBaseServices = mRequest.getEwsBaseServices();

        entitlementUnits = mPspCompany.getEntitlementUnitCollection();

        ewsServicesResponse = new EwsServicesResponse();

        
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
                    throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
                }

                //Flush because all services were cancelled and we are creating a new company
                Application.getHibernateSession().flush();

                mCreateAccountRequest = EwsFactory.createEwsCreateAccount(mRequest, mPspCompany, entitlementUnit, hadBillPayment);
                CreateAccountProcess process = new CreateAccountProcess(mCreateAccountRequest, true);
                EwsCreateAccountResponse createAccountResponse = process.execute();
                mCreateAccountTransmissionsList = process.getTransmissionsList();

                if (createAccountResponse.getEwsResponseStatus().getCode() != 0) {
                    throw new EwsException(EwsMessages.systemError());
                }

                //Flush again to find the newly created company
                Application.getHibernateSession().flush();

                String newPSID = createAccountResponse.getPsid();
                Company newCompany = PspFactory.findCompany(newPSID);

                ewsServicesResponse.setAssistedResponse(createAccountResponse.getEwsServicesResponse().getAssistedResponse());

                if (had401k) {
                    migrateThirdParty401kService((ThirdParty401kCompanyServiceInfo) mPspCompany.getService(ServiceCode.ThirdParty401k), newCompany);
                }

                mPSID = newPSID;
                mPspCompany = newCompany;
                entitlementUnits = mPspCompany.getEntitlementUnitCollection();
            } else {
                TaxCompanyServiceInfo taxCompanyServiceInfo = addAssistedService(null, ewsBaseServices, entitlementUnits, mRequest.getForceRandomDollar());
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
            companyService = addDirectDepositService(ewsBaseServices, entitlementUnits, mRequest.getForceRandomDollar());
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
}
