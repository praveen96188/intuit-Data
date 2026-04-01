/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/CompanyAdapter.java#34 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.IERSGateway;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.CreateAccountProcess;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.aspect.CompanyIdentifierType;
import com.intuit.sbd.payroll.psp.context.aspect.TenantId;
import com.intuit.sbd.payroll.psp.domain.CheckPrintBatchStatus;
import com.intuit.sbd.payroll.psp.domain.OfferingCode;
import com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.DateFormatUtils;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.sap.lcds.proxy.PSPEntityProxy;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ers.ERSListener;
import com.intuit.sbd.payroll.psp.common.MalformedOFXException;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OFXToJavaMappingError;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.IndustryType;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOListener;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOWSGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.amo.GetCustomerAssetResponseTypeDTO;
import com.intuit.sbd.payroll.psp.gateways.amo.IAMOWSGateway;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompGateway;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompGatewayImpl;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.io.PropertyProxyRegistry;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;


import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;

/**
 * CompanyAdapter -- Provides all of the SAP methods for creating, reading, updating, and deleting Company
 * objects using SAP DTOs as the interface.  This class is responsible for translating CRUD operations on
 * Company objects in the SAP DTO format into PSP core actions.
 *
 * @author Joe Warmelink
 */
public class CompanyAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(CompanyAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    public static final int MAX_RESULTS = 100;

    public static final ServiceCode[] servicesThatCanBeAdded = {
            ServiceCode.BillPayment
    };

    public CompanyAdapter() {
        this(true);
    }

    public CompanyAdapter(boolean initializeProxies) {
        if (initializeProxies) {
            registerProxies();
        }
    }

    /* Insert all beans into here to convert enums to strings */
    private void registerProxies() {

        PSPEntityProxy entityProxy = new PSPEntityProxy();
        PropertyProxyRegistry.getRegistry().register(SAPAddress.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPCompany.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPCompanyNote.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPContact.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPFundingModel.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPSourceSystem.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPServiceSubStatus.class, entityProxy);

        PropertyProxyRegistry.getRegistry().register(SAPCompanyStrike.class, entityProxy);

        PropertyProxyRegistry.getRegistry().register(SAPCompanyEvent.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPCompanyEventDetail.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPCompanyBankAccount.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPServiceStatus.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPServiceSubStatus.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPUserOperation.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPEmployeeBankAccountHistory.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPEmployeeBankAccountHistoryItem.class, entityProxy);

        PropertyProxyRegistry.getRegistry().register(SAPOfferingServiceCharge.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPCompanyEventQueryReturn.class, entityProxy);


        PropertyProxyRegistry.getRegistry().register(SAPCompanyPaymentTemplate.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPDepositFrequency.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPLawItem.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPEntitlementSearchResult.class, entityProxy);
    }

    private SAPCompanySearchResult getSAPCompanySearchResult(Company company) {
        SAPCompanySearchResult searchResult = new SAPCompanySearchResult();
        ArrayList<SAPCompanyServiceStatus> sapCompanyServiceStatuses = new ArrayList<SAPCompanyServiceStatus>();
        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            SAPCompanyServiceStatus sapCompanyServiceStatus = new SAPCompanyServiceStatus();
            sapCompanyServiceStatus.setDisplayStatus(getCompanyDisplayStatus(company, companyService));
            sapCompanyServiceStatus.setServiceCd(companyService.getService().getServiceCd().toString());
            sapCompanyServiceStatuses.add(sapCompanyServiceStatus);
        }
        setSAPCompanySearchResult(company, sapCompanyServiceStatuses, searchResult);

        searchResult.setEntitlements(new ArrayList<SAPEntitlementSearchResult>());
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
            searchResult.getEntitlements().add(CompanyTranslator.getSAPEntitlementSearchResultFromDomainEntity(entitlementUnit));
        }

        return searchResult;
    }

    private void setSAPCompanySearchResult(Company company, ArrayList<SAPCompanyServiceStatus> pSAPCompanyServiceStatuses, SAPCompanySearchResult searchResult) {
        searchResult.setKey(new SAPCompanyKey(company.getSourceSystemCd().toString(), company.getSourceCompanyId()));
        searchResult.setFein(company.getFedTaxId());
        searchResult.setLegalName(company.getLegalName());
        searchResult.setPSID(company.getSourceCompanyId());
        searchResult.setServices(pSAPCompanyServiceStatuses);
    }


    private SAPCompanyStatusSearchResult getSAPCompanyStatusSearchResult(Company company, boolean isOnHold) {
        SAPCompanyStatusSearchResult searchResult = new SAPCompanyStatusSearchResult();
        // todo just picking a service and displaying that--should show multiples?
        ArrayList<SAPCompanyServiceStatus> sapCompanyServiceStatuses = new ArrayList<SAPCompanyServiceStatus>();
        SAPCompanyServiceStatus sapCompanyServiceStatus = new SAPCompanyServiceStatus();

        CompanyService selectedCompanyService = null;
        // PSRV004223 :: For OnHold searches, select the company whose
        // display status is "On Hold"
        if (isOnHold) {
            for (CompanyService companyService : company.getCompanyServiceCollection()) {
                SAPDisplayStatus displayStatus = getCompanyDisplayStatus(company, companyService);
                sapCompanyServiceStatus.setDisplayStatus(displayStatus);
                if (displayStatus.getDisplayStatus().equals("On Hold")) {
                    selectedCompanyService = companyService;
                }
            }
        } else {
            for (CompanyService companyService : company.getCompanyServiceCollection()) {
                if (!companyService.getStatusCd().equals(ServiceSubStatusCode.ActiveCurrent)) {
                    selectedCompanyService = companyService;
                }
            }
        }

        if (selectedCompanyService == null) {
            selectedCompanyService = company.getCompanyServiceCollection().get(0);
        }
        sapCompanyServiceStatus.setDisplayStatus(getCompanyDisplayStatus(company, selectedCompanyService));
        sapCompanyServiceStatus.setServiceCd(selectedCompanyService.getService().getServiceCd().toString());
        sapCompanyServiceStatuses.add(sapCompanyServiceStatus);
        setSAPCompanySearchResult(company, sapCompanyServiceStatuses, searchResult);

        searchResult.setNumberOfStrikes(CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike, null, null, null).size());

        searchResult.setBalanceDue(CompanyTranslator.getCompanyBalanceDueFromDomainEntity(company));

        return searchResult;

    }

    /*
    The logic for determining how a company's status is displayed to the user lives in this method.
    This is generally for summary information in use in searches, banner, etc.
    Everything should use it, ideally.
    Status is what is displayed directly to the user
    Details are displayed as a tool tip

    Current assumption is that there is only one service
    and that we will display everything agnostic to which status it is.
    The detials are just a description of the actual service //todo this seems not the best
    */
    private SAPDisplayStatus getCompanyDisplayStatus(Company company, CompanyService pCompanyService) {
        SAPDisplayStatus displayStatus = new SAPDisplayStatus();

        setDisplayStatusFromService(displayStatus, pCompanyService);

        //when the company is on hold, we invent a "status" to show to the user
        //the status is "hold" and the sub status is a collection of on hold reasons.
        if (company.isCompanyOnHold()) {
            // only show the service as on hold. Not all on hold reasons effect all services.
            Collection<ServiceSubStatusCode> currentServiceOnHoldReasons = findServiceOnHoldReasons(company, pCompanyService);

            if (currentServiceOnHoldReasons.size() != 0) {
                displayStatus.setDisplayStatus("On Hold");
                StringBuilder sb = new StringBuilder();

                for (ServiceSubStatusCode serviceSubStatusCode : currentServiceOnHoldReasons) {
                    ServiceSubStatus subStatus = Application.findById(ServiceSubStatus.class, serviceSubStatusCode);
                    sb.append(subStatus.getName())
                            .append("\n");
                }
                sb.delete(sb.length() - 1, sb.length());
                displayStatus.setDisplaySubStatus(sb.toString());
            }
        }


        return displayStatus;
    }

    private Collection<ServiceSubStatusCode> findServiceOnHoldReasons(Company pCompany, CompanyService pCompanyService) {
        Collection<ServiceSubStatusCode> serviceSubStatusCodes = pCompanyService.getAvailableServiceSubStatusesCodes();
        Collection<ServiceSubStatusCode> currentOnHoldReasons = pCompany.getCurrentOnHoldReasonCodes();
        Collection<ServiceSubStatusCode> currentServiceOnHoldReasons = new ArrayList<ServiceSubStatusCode>();
        for (ServiceSubStatusCode currentOnHoldReason : currentOnHoldReasons) {
            if (serviceSubStatusCodes.contains(currentOnHoldReason)) {
                currentServiceOnHoldReasons.add(currentOnHoldReason);
            }
        }

        return currentServiceOnHoldReasons;
    }

    private void setDisplayStatusFromService(SAPDisplayStatus ds, CompanyService cs) {
        if (cs != null) {
            ServiceSubStatus serviceSubStatus =
                    Application.findById(ServiceSubStatus.class, cs.getStatusCd());
            ds.setDisplayStatus(serviceSubStatus.getServiceStatus().getName());
            ds.setDisplayDetails(serviceSubStatus.getServiceStatus().getDescription());
            ds.setDisplaySubStatus(serviceSubStatus.getName());
        }
    }

    //todo poorly named, fix later
    @FlexMethod
    public ArrayList<SAPCompanySearchResult> search(String searchMethod, String searchInput) throws Throwable {
        ArrayList<SAPCompanySearchResult> retList = new ArrayList<SAPCompanySearchResult>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<Company> spcfCompanies;
            if (searchMethod.equals("findSmartly")) {
                spcfCompanies = Company.searchCompaniesByAnything(searchInput);
            } else if (searchMethod.equals("findByPSID")) {
                spcfCompanies = Company.searchCompaniesBySourceCompanyId(searchInput);
            } else if (searchMethod.equals("findByFEIN")) {
                spcfCompanies = Company.searchCompaniesByEIN(searchInput);
            } else if (searchMethod.equals("findByLegalNamePattern")) {
                spcfCompanies = Company.searchCompaniesByLegalName(searchInput);
            } else if (searchMethod.equals("findByServiceKey")) {
                spcfCompanies = Company.searchCompaniesByServiceKey(searchInput);
            } else if (searchMethod.equals("findByLicenseNumber")) {
                spcfCompanies = Company.searchCompaniesByLicenseNumber(searchInput);
            } else if (searchMethod.equals("findByCAN")) {
                spcfCompanies = Company.searchCompaniesByCAN(searchInput);
            } else if (searchMethod.equals("findByRegistrationNumber")) {
                spcfCompanies = Company.searchCompaniesByRegistrationNumber(searchInput);
            } else if (searchMethod.equals("findByRealmId")) {
                spcfCompanies = Company.findAllCompaniesByRealmId(searchInput);
            } else {
                throw new Exception(searchMethod + " not valid search method");
            }

            //To hide Book Transfer Company in UI.
            spcfCompanies.remove(Company.getBookTransferCompany());

            spcfCompanies.sort(Company.LegalName());
            for (Company company : spcfCompanies) {
                SAPCompanySearchResult companyInfo = getSAPCompanySearchResult(company);
                retList.add(companyInfo);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException(String.format("Error occurred while searching for a company. Method: %s. Input: %s.", searchMethod, searchInput), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return retList;
    }

    @FlexMethod
    public SAPCompany findCompany(String source, @TenantId(IdType = CompanyIdentifierType.PSID) String id) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company spcfCompany = Company.findCompanyNoEagerLoad(id, SourceSystemCode.valueOf(source));
            if (spcfCompany == null)
                return null;

            return CompanyTranslator.getSAPCompanyFromDomainEntity(spcfCompany);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company.", source, id, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public SAPTaxCompanyServiceInfo getCompanyCancellationInfo(String source, @TenantId(IdType = CompanyIdentifierType.PSID) String id) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(id, SourceSystemCode.valueOf(source));
            if (company == null)
                throw aeFactory.companyNotFoundException();
            CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);

            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) (taxService != null ? taxService : new TaxCompanyServiceInfo());

            return CompanyTranslator.getSAPTaxCompanyServiceInfo(taxCompanyServiceInfo);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting company cancellation info.", source, id, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public void updateCompanyCancellationInfo(String pSource,
                                              @TenantId(IdType = CompanyIdentifierType.PSID) String pId,
                                              SAPTaxCompanyServiceInfo pSapTaxCompanyServiceInfo) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pId, SourceSystemCode.valueOf(pSource));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }
            CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) taxService;

            TaxServiceInfoDTO taxServiceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(taxCompanyServiceInfo);

            taxServiceInfoDTO.setFileAnnualReturns(pSapTaxCompanyServiceInfo.getFileAnnualReturns());
            taxServiceInfoDTO.setFinalAnnualReturns(pSapTaxCompanyServiceInfo.getIsFinal());

            if (pSapTaxCompanyServiceInfo.getLastPayrollDate() != null) {

                taxServiceInfoDTO.setLastPayrollDate(SAPTranslator.getSpcfCalendarFromDate(pSapTaxCompanyServiceInfo.getLastPayrollDate()));
            } else {
                taxServiceInfoDTO.setLastPayrollDate(null);
            }
            taxServiceInfoDTO.setLastQuarterToFile(Integer.parseInt(pSapTaxCompanyServiceInfo.getLastTaxQuarter()));

            ProcessResult pr = PayrollServices.companyManager.updateService(company.getSourceSystemCd(), company.getSourceCompanyId(), taxServiceInfoDTO);
            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating cancellation info", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating company cancellation info.", pSource, pId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public List<SAPCompanyStrike> getStrikeInfo(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        List<SAPCompanyStrike> sapStrikeList = new ArrayList<SAPCompanyStrike>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            DomainEntitySet<CompanyEvent> strikeEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.Strike, null, null, null);

            Map<String, String> userCache = new HashMap<String, String>();
            for (CompanyEvent companyEvent : strikeEventList) {
                if (companyEvent.getEventTypeCd().equals(EventTypeCode.Strike)) {
                    sapStrikeList.add(CompanyTranslator.getSAPCompanyStrikeFromDomainEntity(companyEvent, userCache));
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting strike info", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapStrikeList;
    }

    @FlexMethod
    public int getPayrollRunCount(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            return (int) company.getPayrollCount();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting payroll run count", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return -1;
    }

    @FlexMethod
    public int getBankReturnTransactionCount(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            return TransactionReturn.findTransactionReturnCount(company);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting bank return count", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return -1;
    }

    @FlexMethod
    public SAPPINInfo getPINInfo(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            return CompanyTranslator.getPINInfo(company);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting PIN info", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public String getFundingModelCd(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            return company.getFundingModel().getFundingModelCd();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting payroll run count", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public SAPCompanyBankAccount getActiveBankAccount(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            CompanyBankAccount activeAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);

            if (activeAccount != null) {
                return CompanyTranslator.getSAPCompanyBankAccountFromDomainEntity(activeAccount, PIIMask.authenticatedUserCanViewFullBankAccountNumbers());
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting active bank account", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public boolean isDebugLogging(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }
            return company.getDebugLogging();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting debug logging", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return false;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.FundingModelUpdate)
    public void updateCompanyFundingModel(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pFundingModelCd) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            // set funding model
            FundingModel fundingModel = PayrollServices.entityFinder.findById(FundingModel.class, pFundingModelCd);

            if (fundingModel == null) {
                aeFactory.throwGenericException("Error unknown funding model");
            }

            // update the company default funding model
            prList.add(
                    PayrollServices.companyManager.updateCompanyFundingModel(
                            SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId, fundingModel));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error updating funding model.", "Company", pCompanyId, prList);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating funding model.", pSourceSystemCd, pCompanyId, "FundingModel", pFundingModelCd, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    //Action = goEvent no operation assigned
    @FlexMethod
    public void addCompanyNote(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String companyEventId, String companyEventTransmissionId, SAPCompanyNote sapCompanyNote) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            if (companyEventId == null && companyEventTransmissionId != null) {
                Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
                DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventDetailTypeCode.TransmissionId, companyEventTransmissionId);
                if (companyEvents.size() > 0) {
                    companyEventId = companyEvents.get(0).getId().toString();
                } else {
                    aeFactory.throwGenericException("Company event not found with transmission id:" + companyEventTransmissionId + " company: " + pSourceSystemCd + ":" + pCompanyId);
                }
            }

            ProcessResult processResult = PayrollServices.companyManager.addCompanyNote(
                    SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId, companyEventId,
                    sapCompanyNote.getInsertUserId(), sapCompanyNote.getNotes(), sapCompanyNote.getAlert());


            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error adding company note", processResult);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding company note", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public void removeCompanyNoteAlert(String noteId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            CompanyNote note = Application.findById(CompanyNote.class, SpcfUniqueId.createInstance(noteId));
            note.setAlert(false);
            Application.save(note);
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error removing company note alert: " + noteId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public SAPCompanyNote getMostRecentAlertNote(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }
            DomainEntitySet<CompanyNote> companyNotes = Application.find(CompanyNote.class, CompanyNote.Company().equalTo(company).And(CompanyNote.Alert().equalTo(true)));
            if (companyNotes.isNotEmpty()) {
                return CompanyTranslator.getSAPCompanyNoteFromDomainEntity(companyNotes.sort(CompanyNote.<CompanyNote>CreatedDate().Descending()).getFirst());
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting debug logging", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }


    @FlexMethod
    @Operation(operationIds = OperationId.StrikeAdd)
    public void addCompanyStrike(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, Date pStrikeDate, String pStrikeReason) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(PayrollServices.companyManager.addStrikeEvent(
                    SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId,
                    pStrikeReason,
                    SAPTranslator.getSpcfCalendarFromDate(pStrikeDate)));
            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding company Strike.", "Company", pCompanyId, prList);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding company Strike.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.StrikeCancel)
    public void cancelCompanyStrike(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pStrikeId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(PayrollServices.companyManager.cancelStrikeEvent(
                    SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId,
                    SpcfUniqueId.createInstance(pStrikeId)));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error cancelling company Strike.", "Company", pCompanyId, prList);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error cancelling company Strike.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPCompanyEvent> getLimitViolationEvents(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                                              String pSourceSystemCd,
                                                              Date pFromDate,
                                                              Date pToDate) throws Throwable {

        ArrayList<SAPCompanyEvent> sapLimitViolationEventList = new ArrayList<SAPCompanyEvent>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<CompanyEvent> limitViolationEventList = CompanyEvent.findCompanyEvents(company,
                    EventTypeCode.LimitViolation,
                    null,
                    SAPTranslator.getSpcfCalendarFromDate(pFromDate),
                    SAPTranslator.getSpcfCalendarFromDate(pToDate),
                    true);

            Map<String, String> userCache = new HashMap<String, String>();
            for (CompanyEvent companyEvent : limitViolationEventList) {
                sapLimitViolationEventList.add(CompanyTranslator.getSAPCompanyEventFromDomainEntity(companyEvent, userCache));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving limit violation events.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapLimitViolationEventList;
    }

    @FlexMethod
    public ArrayList<SAPCompanyBankAccountHistory> getCompanyBankAccountsHistory(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        ArrayList<SAPCompanyBankAccountHistory> sapCompanyBankAccountHistoryList = new ArrayList<SAPCompanyBankAccountHistory>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<CompanyBankAccount> companyBankAccountsList = CompanyBankAccount.findCompanyBankAccounts(company);

            for (CompanyBankAccount companyBankAccount : companyBankAccountsList) {
                DomainEntitySet<PropertyAudit> propertyAudit =
                        PayrollServices.entityFinder.find(PropertyAudit.class,
                                PropertyAudit.Company().equalTo(company)
                                        .And(PropertyAudit.ObjectIdentifier().equalTo(companyBankAccount.getId().toString())));

                sapCompanyBankAccountHistoryList.add(
                        CompanyTranslator.getSAPCompanyBankAccountHistoryFromDomainEntities(companyBankAccount, propertyAudit, PIIMask.authenticatedUserCanViewFullBankAccountNumbers()));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company bank account history.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapCompanyBankAccountHistoryList;
    }

    @FlexMethod
    public ArrayList<SAPEmployeeBankAccountHistory> getEmployeeBankAccountHistory(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pEmployeeId, String pSourceSystemCd) throws Throwable {

        ArrayList<SAPEmployeeBankAccountHistory> employeeBankAccountHistory = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            CompanyTranslator companyTranslator = new CompanyTranslator();

            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = EmployeeBankAccount.findEmployeeBankAccounts(company, pEmployeeId, null);

            employeeBankAccountHistory = companyTranslator.setSAPEmployeeBankAccountHistory(employeeBankAccounts, company, PIIMask.authenticatedUserCanViewFullBankAccountNumbers());

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employee bank account history.", pSourceSystemCd, pCompanyId, "Employee", pEmployeeId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return employeeBankAccountHistory;
    }

    @FlexMethod
    public ArrayList<SAPEmployeeBankAccountHistory> getVendorBankAccountHistory(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pVendorId) throws Throwable {

        ArrayList<SAPEmployeeBankAccountHistory> employeeBankAccountHistory = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            CompanyTranslator companyTranslator = new CompanyTranslator();

            DomainEntitySet<PayeeBankAccount> payeeBankAccounts = PayeeBankAccount.findPayeeBankAccounts(company, pVendorId);

            employeeBankAccountHistory = companyTranslator.setSAPVendorBankAccountHistory(payeeBankAccounts, company, PIIMask.authenticatedUserCanViewFullBankAccountNumbers());

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding vendor bank account history.", pSourceSystemCd, pCompanyId, "Vendor", pVendorId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return employeeBankAccountHistory;
    }

    @FlexMethod
    public ArrayList<SAPEmployeeInfo> getEmployees(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd) throws Throwable {

        ArrayList<SAPEmployeeInfo> sapEmployeeList = new ArrayList<SAPEmployeeInfo>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<Employee> employeeList = Application.find(Employee.class, new Query<Employee>().Where(Employee.Company().equalTo(company)).LimitResults(0, 1000));

            boolean canViewEEPII = PIIMask.authenticatedUserCanViewEEPII();

            for (Employee employee : employeeList) {
                SpcfCalendar lastPayDate = employee.getLastPayDate();
                if (employee.getEmployeeBankAccountCollection().size() > 0) {
                    sapEmployeeList.add(CompanyTranslator.getSAPEmployeeInfoFromDomainEntity(employee, lastPayDate, canViewEEPII));
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employees.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapEmployeeList;
    }

    @FlexMethod
    public SAPEmployeeInfo getEmployee(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd, String sourceEmployeeId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            Employee employee = Employee.findEmployee(company, sourceEmployeeId);

            SpcfCalendar lastPayDate = employee.getLastPayDate();

            return CompanyTranslator.getSAPEmployeeInfoFromDomainEntity(employee, lastPayDate, PIIMask.authenticatedUserCanViewEEPII());

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employee.", pSourceSystemCd, pCompanyId, "Employee", sourceEmployeeId, t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPEmployeeInfo> getCloudEmployees(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd, Boolean pUserViewsNonServiceableData) throws Throwable {

        ArrayList<SAPEmployeeInfo> sapEmployeeList = new ArrayList<SAPEmployeeInfo>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));


            DomainEntitySet<Employee> employeeList = Employee.findCloudEmployees(company);

            boolean canViewEEPII = PIIMask.authenticatedUserCanViewEEPII();

            for (Employee employee : employeeList) {
                SpcfCalendar lastPayDate = employee.getLastPayrollReceivedDate();

                sapEmployeeList.add(CompanyTranslator.getSAPEmployeeInfoFromDomainEntity(employee, lastPayDate, canViewEEPII));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving cloud employees for company", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapEmployeeList;
    }

    @FlexMethod
    public ArrayList<SAPVendorInfo> getVendors(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd) throws Throwable {

        ArrayList<SAPVendorInfo> sapVendorList = new ArrayList<SAPVendorInfo>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<Payee> payeeList = Payee.findPayees(company);

            for (Payee payee : payeeList) {
                sapVendorList.add(CompanyTranslator.getSAPVendorInfoFromDomainEntity(payee));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding vendors.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapVendorList;
    }

    //Not used in View
    @FlexMethod
    public void deactivateCompanyBankAccount(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                             String pSourceSystemCd,
                                             String pBankAccountId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<CompanyBankAccount> pr = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.valueOf(pSourceSystemCd),
                    pCompanyId, pBankAccountId, false, false);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error deactivating bank account.", pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error deactivating company bank account.", pSourceSystemCd, pCompanyId, "BankAccount", pBankAccountId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.DDLimitUpdate,
            OperationId.DDStatusUpdate
    })
    public void saveCompanyService(String pSourceSystemCd,
                                   @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                   String pServiceCode,
                                   ArrayList<SAPServiceSubStatus> pSubStatuses,
                                   SAPCompanyDdLimits pSAPCompanyDdLimits,
                                   String pFundingModelCd) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult pr = null;

            SourceSystemCode sourceSystemCd = SourceSystemCode.valueOf(pSourceSystemCd);
            ServiceCode serviceCode = ServiceCode.valueOf(pServiceCode);

            if (pSubStatuses != null) {
                ArrayList<ServiceSubStatus> subStatuses = new ArrayList<ServiceSubStatus>();

                for (SAPServiceSubStatus sapServiceSubStatus : pSubStatuses) {
                    subStatuses.add(getServiceSubStatusFromStatusCd(sapServiceSubStatus.getSubStatusCd()));
                }

                DomainEntitySet<ServiceSubStatus> subStatusesToSave = new DomainEntitySet<ServiceSubStatus>();
                for (ServiceSubStatus serviceSubStatusToSave : subStatuses) {
                    subStatusesToSave.add(serviceSubStatusToSave);
                }
                pr = PayrollServices.companyManager.updateSubStatuses(sourceSystemCd, pCompanyId, serviceCode, subStatusesToSave);

                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Saving service was not successful.", pr);
                }
            }

            if (pSAPCompanyDdLimits != null) {
                if (serviceCode == ServiceCode.DirectDeposit) {
                    pr = PayrollServices.companyManager.updateDDLimits(
                            SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId,
                            SAPTranslator.getSpcfMoneyFromDouble(pSAPCompanyDdLimits.getPerPayrollLimit()),
                            SAPTranslator.getSpcfMoneyFromDouble(pSAPCompanyDdLimits.getPerEmployeeLimit())
                    );
                } else if (serviceCode == ServiceCode.BillPayment) {
                    pr = PayrollServices.companyManager.updateBPLimits(
                            SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId,
                            SAPTranslator.getSpcfMoneyFromDouble(pSAPCompanyDdLimits.getPerPayrollLimit()),
                            SAPTranslator.getSpcfMoneyFromDouble(pSAPCompanyDdLimits.getPerEmployeeLimit())
                    );
                }

                if (pr != null && !pr.isSuccess()) {
                    aeFactory.throwGenericException("Error updating company dd limits: " + pr.toString());
                }
            }

            // todo there is not an api method yet to update the funding model on a service
            /*else {
                prList.add(
                    PayrollServices.companyManager.updateCompanyFundingModel(
                            SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId, fundingModel));
            }*/

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error saving company service.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private ServiceSubStatus getServiceSubStatusFromStatusCd(ServiceSubStatusCode serviceSubStatusCd) throws Exception {
        ServiceSubStatus retSubStatus = null;

        DomainEntitySet<ServiceSubStatus> serviceSubStatusList =
                PayrollServices.entityFinder.findObjects(ServiceSubStatus.class);

        for (ServiceSubStatus subStatus : serviceSubStatusList) {
            if (serviceSubStatusCd.equals(subStatus.getServiceSubStatusCd())) {
                retSubStatus = subStatus;
                break;
            }
        }

        return retSubStatus;
    }

    @FlexMethod
    public SAPDisplayStatus getCompanyDisplayStatus(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            // todo update for multiple services
            return getCompanyDisplayStatus(company, company.getService(ServiceCode.DirectDeposit));
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company display status.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public SAPCompanyStatus getCompanyStatus(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, boolean findTransitions, boolean findLimitViolations) throws Throwable {
        SAPCompanyStatus companyStatus = new SAPCompanyStatus();
        companyStatus.setSourceSystemCd(pSourceSystemCd);
        companyStatus.setCompanyId(pCompanyId);

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            ArrayList<SAPCompanyServiceStatus> serviceStatusCollection = new ArrayList<SAPCompanyServiceStatus>();
            SAPCompanyServiceStatus ddService = null;
            SAPCompanyServiceStatus taxService = null;
            for (CompanyService companyService : company.getCompanyServiceCollection()) {
                SAPCompanyServiceStatus sapCompanyServiceStatus = getCompanyServiceStatus(companyService, findTransitions);

                int ddLimitViolations = 0;
                int bpLimitViolations = 0;
                if (findLimitViolations) {
                    DomainEntitySet<CompanyEvent> limitVoilationEvents = CompanyEvent.findCompanyEvents(company,
                            EventTypeCode.LimitViolation,
                            CompanyEventStatus.Active,
                            null,
                            null,
                            true);

                    for (CompanyEvent limitVoilationEvent : limitVoilationEvents) {
                        Collection<String> serviceCodes = limitVoilationEvent.getCompanyEventDetailValues(EventDetailTypeCode.ServiceCode);
                        if (serviceCodes.size() > 0 && serviceCodes.contains(ServiceCode.BillPayment.toString())) {
                            bpLimitViolations++;
                        } else {
                            // dd limit violation events do not have a service code
                            ddLimitViolations++;
                        }
                    }
                }

                CompanyOffering companyOffering = company.getOffering(companyService.getService().getServiceCd());
                DomainEntitySet<Offering> offerings = company.getActivePrimaryEntitlementUnit() == null ? new DomainEntitySet<Offering>() :
                        Offering.findAvailableOfferings(companyService.getService().getServiceCd(), company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementCode());
                if (companyOffering != null) {
                    offerings.remove(companyOffering.getOffering());
                    sapCompanyServiceStatus.setOffering(companyOffering.getOffering().getName());
                    sapCompanyServiceStatus.setOfferingDetails(BillingTranslator.getOfferingFromDomainEntity(companyOffering.getOffering(), companyOffering.getOffering().getPayrollCharges()));
                    DomainEntitySet<CompanyOffer> companyOffers = company.getActiveCompanyOffersForOffering(companyOffering.getOffering().getOfferingCode());
                    if (companyOffers.size() > 0) {
                        // a company is only allowed one active offer per offering
                        CompanyOffer companyOffer = companyOffers.get(0);
                        sapCompanyServiceStatus.setOffer(companyOffer.getOffer().getName());
                        sapCompanyServiceStatus.setOfferExpirationDate(SAPTranslator.getDateFromSpcfCalendar(companyOffer.getEndDate()));
                    }
                    sapCompanyServiceStatus.setCanEditOffer(companyOffering.getOffering().getApplicableOffers().size() > 0);
                }
                sapCompanyServiceStatus.setCanEditOffering(offerings.size() > 0);

                // special logic for individual service types
                if (companyService instanceof DDCompanyServiceInfo) {
                    ddService = sapCompanyServiceStatus;
                    sapCompanyServiceStatus.setDdLimits(new SAPCompanyDdLimits());
                    sapCompanyServiceStatus.getDdLimits().setPerPayrollLimit(SAPTranslator.getDoubleFromSpcfMoney(((DDCompanyServiceInfo) companyService).getOverrideCompanyLimitAmount()));
                    sapCompanyServiceStatus.getDdLimits().setPerEmployeeLimit(SAPTranslator.getDoubleFromSpcfMoney(((DDCompanyServiceInfo) companyService).getOverrideEmployeeLimitAmount()));
                    sapCompanyServiceStatus.getDdLimits().setDefaultEmployeeLimit(SAPTranslator.getDoubleFromSpcfMoney(companyOffering.getOffering().getLimitRule().findLimitValueByName(LimitValueType.DefaultEmployeeLimit).getDecimalValue()));
                    sapCompanyServiceStatus.getDdLimits().setDefaultPayrollLimit(SAPTranslator.getDoubleFromSpcfMoney(companyOffering.getOffering().getLimitRule().findLimitValueByName(LimitValueType.DefaultCompanyLimit).getDecimalValue()));

                    SAPDirectDepositServiceInformation sapDirectDepositServiceInformation = new SAPDirectDepositServiceInformation();
                    sapDirectDepositServiceInformation.setConsecutiveLimitVoilationCount(((DDCompanyServiceInfo) companyService).getConsecutiveLimitViolationCount());
                    sapDirectDepositServiceInformation.setTotalLimitVoilationCount(ddLimitViolations);
                    sapDirectDepositServiceInformation.setUnderwritingPlatform(getUnderwritingPlatform(company));
                    sapCompanyServiceStatus.setDirectDepositAdditionalInfo(sapDirectDepositServiceInformation);
                } else if (companyService instanceof BPCompanyServiceInfo) {
                    sapCompanyServiceStatus.setDdLimits(new SAPCompanyDdLimits());
                    sapCompanyServiceStatus.getDdLimits().setPerPayrollLimit(SAPTranslator.getDoubleFromSpcfMoney(((BPCompanyServiceInfo) companyService).getOverrideCompanyLimitAmount()));
                    sapCompanyServiceStatus.getDdLimits().setPerEmployeeLimit(SAPTranslator.getDoubleFromSpcfMoney(((BPCompanyServiceInfo) companyService).getOverridePayeeLimitAmount()));
                    sapCompanyServiceStatus.getDdLimits().setDefaultEmployeeLimit(SAPTranslator.getDoubleFromSpcfMoney(companyOffering.getOffering().getLimitRule().findLimitValueByName(LimitValueType.DefaultEmployeeLimit).getDecimalValue()));
                    sapCompanyServiceStatus.getDdLimits().setDefaultPayrollLimit(SAPTranslator.getDoubleFromSpcfMoney(companyOffering.getOffering().getLimitRule().findLimitValueByName(LimitValueType.DefaultCompanyLimit).getDecimalValue()));

                    SAPBillPaymentServiceInformation sapBillPaymentServiceInformation = new SAPBillPaymentServiceInformation();
                    sapBillPaymentServiceInformation.setConsecutiveLimitVoilationCount(((BPCompanyServiceInfo) companyService).getConsecutiveLimitViolationCount());
                    sapBillPaymentServiceInformation.setTotalLimitVoilationCount(bpLimitViolations);
                    sapCompanyServiceStatus.setBillPaymentAdditionalInfo(sapBillPaymentServiceInformation);
                } else if (companyService instanceof ThirdParty401kCompanyServiceInfo) {
                    ThirdParty401kCompanyServiceInfo tp401kService = (ThirdParty401kCompanyServiceInfo) companyService;
                    sapCompanyServiceStatus.setCustodialId(tp401kService.getCustodialId());
                    sapCompanyServiceStatus.setSafeHarbor(tp401kService.getHasSafeHarbor());
                } else if (companyService instanceof TaxCompanyServiceInfo) {
                    taxService = sapCompanyServiceStatus;
                    taxService.setIsAssistedActive(companyService.isActive());
                }

                if (companyService.getService().getServiceCd() == ServiceCode.CheckDistribution) {
                    CheckPrintSignature checkPrintSignature = CheckPrintSignature.findCheckPrintSignature(company);
                    sapCompanyServiceStatus.setHasSignatureFile(checkPrintSignature != null && checkPrintSignature.getSignature() != null);
                }

                // if the funding model is null the service uses the default company funding model
                if (companyService.getFundingModel() != null) {
                    sapCompanyServiceStatus.setFundingModelCd(companyService.getFundingModel().getFundingModelCd());
                }


                // add the service to the return collection
                serviceStatusCollection.add(sapCompanyServiceStatus);
            }

            if (company.isCompanyOnService(ServiceCode.Tax) || (!company.isCompanyOnService(ServiceCode.DirectDeposit) && company.hasService(ServiceCode.Tax))) {
                //super special fun-time kludge since they want to see the DD offering/offer under Assisted.  Will show if Active Assisted or inactive assisted and inactive DD
                if (ddService != null && taxService != null) {
                    taxService.setOffer(ddService.getOffer());
                    ddService.setOffer(null);

                    taxService.setOfferExpirationDate(ddService.getOfferExpirationDate());
                    ddService.setOfferExpirationDate(null);

                    taxService.setOffering(ddService.getOffering());
                    ddService.setOffering(null);

                    taxService.setOfferingDetails(ddService.getOfferingDetails());
                    ddService.setOfferingDetails(null);

                    taxService.setCanEditOffer(ddService.getCanEditOffer());
                    ddService.setCanEditOffer(false);

                    taxService.setCanEditOffering(ddService.getCanEditOffering());
                    ddService.setCanEditOffering(false);
                }
            }

            Collections.sort(serviceStatusCollection, new Comparator<SAPCompanyServiceStatus>() {
                public int compare(SAPCompanyServiceStatus a, SAPCompanyServiceStatus b) {
                    Collator collator = Collator.getInstance();
                    return collator.compare(a.getServiceCd(), b.getServiceCd());
                }
            });
            companyStatus.setServiceStatusCollection(serviceStatusCollection);

            companyStatus.setFlaggedForFraud(company.getIsFlaggedForFraud());
            companyStatus.setAvailableServices(getAvailableServices(company));
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company status.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return companyStatus;
    }

    private ArrayList<String> getAvailableServices(Company company) {
        ArrayList<String> services = new ArrayList<String>();
        for (ServiceCode serviceCode : servicesThatCanBeAdded) {
            if (company.getService(serviceCode) == null) {

                if (serviceCode == ServiceCode.BillPayment &&
                        company.getSourceSystemCd() == SourceSystemCode.QBDT &&
                        company.getService(ServiceCode.DirectDeposit) != null) {
                    services.add(serviceCode.toString());
                }
            }
        }
        return services;
    }

    private SAPCompanyServiceStatus getCompanyServiceStatus(CompanyService companyService, boolean findTransitions) {
        SAPCompanyServiceStatus companyServiceStatus = new SAPCompanyServiceStatus();
        companyServiceStatus.setServiceCd(companyService.getService().getServiceCd().toString());
        SAPDisplayStatus displayStatus = getCompanyDisplayStatus(companyService.getCompany(), companyService);
        companyServiceStatus.setDisplayStatus(displayStatus);

        ServiceSubStatus currentSubStatus = Application.findById(ServiceSubStatus.class, companyService.getStatusCd());
        ServiceStatus currentStatus = currentSubStatus.getServiceStatus();

        companyServiceStatus.setCanUpdateStatus(currentSubStatus.canMoveFromSubStatus());
        if (companyService.getServiceStartDate() != null) {
            companyServiceStatus.setServiceStartDate(SAPTranslator.getDateFromSpcfCalendar(companyService.getServiceStartDate()));
        }

        companyServiceStatus.setFirstTaxQuarter(SAPTranslator.getDateFromSpcfCalendar(companyService.getServiceStartDate()));
        if (companyService.getService().getServiceCd() == ServiceCode.Tax) {
            companyServiceStatus.setW2PrintingPreference(((TaxCompanyServiceInfo) companyService).getW2DeliveryPreferenceCd() == DeliveryPreferenceCode.Electronic ? "Self Print" : "Intuit Print");
        }

        ArrayList<ServiceSubStatus> currentSubStatuses = new ArrayList<ServiceSubStatus>(1);
        currentSubStatuses.add(currentSubStatus);
        companyServiceStatus.setStatus(CompanyTranslator.getSAPServiceStatusFromDomainEntity(currentStatus, currentSubStatuses, true));

        // handle on-hold reasons
        // if a company has 'on-hold' reasons, change serviceStatus to 'on-hold' and turn the on-hold reasons into serviceSubStatuses
        Collection<ServiceSubStatusCode> onHoldReasons = findServiceOnHoldReasons(companyService.getCompany(), companyService);

        // on hold reasons converted to ServiceSubStatus entities
        ArrayList<ServiceSubStatus> holdSubStatuses = new ArrayList<ServiceSubStatus>(onHoldReasons.size());
        if (onHoldReasons.size() > 0) {
            ServiceStatus onHoldStatus = PayrollServices.entityFinder.findById(ServiceStatus.class, ServiceStatusCode.OnHold);

            // convert hold reasons to hold sub-statuses
            for (ServiceSubStatusCode serviceSubStatusCode : onHoldReasons) {
                ServiceSubStatus holdSubStatus = PayrollServices.entityFinder.findById(ServiceSubStatus.class, serviceSubStatusCode);
                holdSubStatuses.add(holdSubStatus);
            }

            companyServiceStatus.setStatus(CompanyTranslator.getSAPServiceStatusFromDomainEntity(onHoldStatus, holdSubStatuses, true));
        }

        if (findTransitions) {
            CompanyService taxService = companyService.getCompany().getCompanyService(ServiceCode.Tax);
            // this list includes OnHold and its sub-statuses
            DomainEntitySet<ServiceStatus> serviceStatuses = PayrollServices.entityFinder.findObjects(ServiceStatus.class);
            ArrayList<SAPServiceStatus> allowedTransitions = new ArrayList<SAPServiceStatus>(serviceStatuses.size());
            //todo Change is manually removed/set at the service status to be at the service level, not at the status level
            for (ServiceStatus serviceStatus : serviceStatuses) {
                // the BE method filters out SubService statuses by role permissions
                DomainEntitySet<ServiceSubStatus> possibleTransitions =
                        ServiceSubStatus.findPossibleSubStatuses(serviceStatus.getServiceStatusCd(),
                                companyService.getService().getServiceCd());
                //PrefundingWire for Assisted is not implemented, once we implement that we can remove below block
                ServiceSubStatus pendingPrefundingWire = possibleTransitions.find(ServiceSubStatus.ServiceSubStatusCd().equalTo(ServiceSubStatusCode.PendingPrefundingWire)).getFirst();
                if (pendingPrefundingWire != null && taxService != null && taxService.isActive()) {
                    possibleTransitions.remove(pendingPrefundingWire);
                }
                if (possibleTransitions.size() > 0)
                    allowedTransitions.add(CompanyTranslator.getSAPServiceStatusFromDomainEntity(serviceStatus, possibleTransitions, false));
            }

            //The list of "allowed transitions" must include the company's current status even if the agent doesn't have
            //rights to that status.  Otherwise, the UI will not display the current status in the list.
            if (!allowedTransitions.contains(companyServiceStatus.getStatus())) {
                boolean merged = false;
                for (SAPServiceStatus allowedTransition : allowedTransitions) {
                    if (allowedTransition.getServiceStatusCd().equals(companyServiceStatus.getStatus().getServiceStatusCd())) {
                        for (SAPServiceSubStatus currentSAPServiceSubStatus : companyServiceStatus.getStatus().getServiceSubStatusList()) {
                            if (!allowedTransition.getServiceSubStatusList().contains(currentSAPServiceSubStatus)) {
                                allowedTransition.getServiceSubStatusList().add(currentSAPServiceSubStatus);
                            }
                        }
                        merged = true;
                    }
                }

                if (!merged)
                    allowedTransitions.add(companyServiceStatus.getStatus());
            }


            companyServiceStatus.setAllowedTransitions(allowedTransitions);

            // can update check (these needs to match validation code in UpdateRolSubStatusCore.java):
            // 1 - if status is not hold, can agent's role move from current sub-status
            // 2 - if status is hold, can agent's role remove current hold reasons
            // 3 - if status is hold, can agent's role set/add other hold reasons
            if (ServiceStatusCode.valueOf(companyServiceStatus.getStatus().getServiceStatusCd()) != ServiceStatusCode.OnHold) {
                companyServiceStatus.setCanUpdateStatus(currentSubStatus.canMoveFromSubStatus());
            } else {
                companyServiceStatus.setCanUpdateStatus(false);
                for (ServiceSubStatus holdSubStatus : holdSubStatuses) {
                    companyServiceStatus.setCanUpdateStatus(holdSubStatus.canMoveFromSubStatus());
                    if (companyServiceStatus.getCanUpdateStatus())
                        break;
                }

                if (!companyServiceStatus.getCanUpdateStatus()) {
                    DomainEntitySet<ServiceSubStatus> possilbeHoldReasons =
                            ServiceSubStatus.findPossibleSubStatuses(ServiceStatusCode.OnHold, ServiceCode.DirectDeposit);
                    //PrefundingWire for Assisted is not implemented, once we implement that we can remove below block
                    ServiceSubStatus pendingPrefundingWire = possilbeHoldReasons.find(ServiceSubStatus.ServiceSubStatusCd().equalTo(ServiceSubStatusCode.PendingPrefundingWire)).getFirst();
                    if (pendingPrefundingWire != null && taxService != null && taxService.isActive()) {
                        possilbeHoldReasons.remove(pendingPrefundingWire);
                    }
                    for (ServiceSubStatus possibleHoldReason : possilbeHoldReasons) {
                        if (!holdSubStatuses.contains(possibleHoldReason)) {
                            // we've found a hold reason that is not currently set that the agent can add
                            companyServiceStatus.setCanUpdateStatus(true);
                            break;
                        }
                    }
                }
            }

            //this is a bit of a hack since we don't typically allow operations based on service
            //something like this will be needed until we do
            if (companyService.getService().getServiceCd().equals(ServiceCode.Cloud)) {
                AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
                if (foundUser != null && foundUser.hasOperation(OperationId.CancelCloud)) {
                    DomainEntitySet<ServiceSubStatus> cancelledServiceSubStatus = Application.find(ServiceSubStatus.class, ServiceSubStatus.Name().equalTo("Cancelled"));
                    allowedTransitions.add(CompanyTranslator.getSAPServiceStatusFromDomainEntity(cancelledServiceSubStatus.get(0)));
                }

            }
        }

        return companyServiceStatus;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.CreateFeeTransaction, OperationId.AddManualFeeTransactions})
    public ArrayList<SAPOfferingServiceChargePrice> getFeeOfferingServiceChargePrices(String pSourceSystemCd,
                                                                                      @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pPayrollRunId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            ArrayList<SAPOfferingServiceChargePrice> chargePrices = new ArrayList<SAPOfferingServiceChargePrice>();

            boolean isManualFeePayroll;
            if (StringUtils.isNotEmpty(pPayrollRunId)) {
                PayrollRun payrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(pPayrollRunId));
                isManualFeePayroll = false;
                //if payroll run is fee-only, then true
            } else {
                isManualFeePayroll = true;
            }

            OfferingServiceChargeType[] fees;
            if (isManualFeePayroll) {
                fees = new OfferingServiceChargeType[]{
                        OfferingServiceChargeType.AmendedSSN,
                        OfferingServiceChargeType.Amendments,
                        OfferingServiceChargeType.OtherFee,
                };
            } else {
                fees = new OfferingServiceChargeType[]{
                        OfferingServiceChargeType.ReversalFee,
                        OfferingServiceChargeType.DebitReturnFee,
                        OfferingServiceChargeType.PaymentArrangementFee};
            }

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            //not currently possible to have two offerings with these fees, but would just pick one (just a default so no problem)
            for (OfferingServiceChargeType fee : fees) {
                SpcfMoney chargePrice = null;
                OfferingServiceChargeGroup group = null;
                for (CompanyOffering companyOffering : company.getCompanyOfferingCollection()) {
                    Offering offering = companyOffering.getOffering();
                    group = OfferingServiceChargeGroup.findOfferingServiceChargeGroup(offering, fee);
                    if (group != null) {
                        OfferingServiceCharge charge = group.selectTier(1);
                        OfferingServiceChargePrice price = charge.getCurrentPrice();
                        if (price != null) {
                            chargePrice = price.getBasePrice();
                            if (!chargePrice.isGreaterThan(SpcfMoney.ZERO)) {
                                chargePrice = price.getUnitPrice();
                            }
                            break;
                        }
                    }
                }

                if (chargePrice != null) {
                    //PSP-25375: Overriding the fee value in SAP ask from Agents
                    switch (fee){
                        case OtherFee:
                            chargePrices.add(getCancelationFeeChargePrice(group));
                            int timesAllowed = 3;
                            for (int i = 0; i < timesAllowed; i++) {
                                chargePrices.add(CompanyTranslator.getSAPOfferingServiceChargePrice(group, chargePrice));
                            }
                            break;
                        case AmendedSSN:
                            chargePrice = new SpcfMoney("250");
                            chargePrices.add(CompanyTranslator.getSAPOfferingServiceChargePrice(group, chargePrice));
                            break;
                        case Amendments:
                            chargePrice = new SpcfMoney("375");
                            chargePrices.add(CompanyTranslator.getSAPOfferingServiceChargePrice(group, chargePrice));
                            break;
                        default:
                            chargePrices.add(CompanyTranslator.getSAPOfferingServiceChargePrice(group, chargePrice));
                    }
                }
            }

            return chargePrices;

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding offering service fee charge prices.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private static SAPOfferingServiceChargePrice getCancelationFeeChargePrice(OfferingServiceChargeGroup group) {
        SAPOfferingServiceChargePrice cancellationChargePrices = CompanyTranslator.getSAPOfferingServiceChargePrice(group, new SpcfMoney("150.00"));
        cancellationChargePrices.setDisplayName("Cancellation Fee");
        cancellationChargePrices.setMemo("Cancellation Fee");
        return cancellationChargePrices;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddBankAccountRandomDebits, OperationId.AddAssistedBankAccountBypassRandomDollarDebitPreBALF, OperationId.AddBankAccountByPassRandomDebits, OperationId.AddAssistedBankAccountPreBALF, OperationId.AddBankAccountByPassRandomDebitsPostBALF, OperationId.AddBankAccountRandomDebitsPostBALF})
    public void editBankAccount(String pSourceSystemCd,
                                @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
                                String pCompanyBankAccountID,
                                String pSourceBankAccountName,
                                String pAccountNumber,
                                String pRoutingNumber,
                                String pAccountType,
                                String pBankName) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            CompanyBankAccountDTO companyBankAccountDTO = CompanyTranslator.createCompanyBankAccountDTO(pCompanyBankAccountID,
                    pSourceBankAccountName,
                    pAccountNumber,
                    pRoutingNumber,
                    pAccountType,
                    pBankName);

            // Check if the company bank account exists
            Company foundCompany = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyBankAccount foundCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(foundCompany,
                    companyBankAccountDTO.getCompanyBankAccountID());

            ArrayList<ProcessResult> results = new ArrayList<ProcessResult>();

            results.add(PayrollServices.companyManager.updateCompanyBankAccountWithAccountService(SourceSystemCode.valueOf(pSourceSystemCd),
                    pSourceCompanyId,
                    companyBankAccountDTO));


            for (ProcessResult result : results) {
                if (!result.isSuccess())
                    aeFactory.throwGenericException("Error saving bank account edit.", result);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error saving bank account edit.", pSourceSystemCd, pSourceCompanyId, "BankAccount", pCompanyBankAccountID, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddBankAccountRandomDebits, OperationId.AddAssistedBankAccountBypassRandomDollarDebitPreBALF, OperationId.AddBankAccountByPassRandomDebits, OperationId.AddAssistedBankAccountPreBALF, OperationId.AddBankAccountByPassRandomDebitsPostBALF, OperationId.AddBankAccountRandomDebitsPostBALF})
    public void addBankAccount(String pSourceSystemCd,
                               @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
                               String pCompanyBankAccountID,
                               String pSourceBankAccountName,
                               String pAccountNumber,
                               String pRoutingNumber,
                               String pAccountType,
                               String pBankName,
                               boolean pShouldAddRandomDebits,
                               boolean pShouldAllowPendingTransactions,
                               boolean pShouldMovePendingTransactionsToAccount) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            CompanyBankAccountDTO companyBankAccountDTO = CompanyTranslator.createCompanyBankAccountDTO(pCompanyBankAccountID,
                    pSourceBankAccountName,
                    pAccountNumber,
                    pRoutingNumber,
                    pAccountType,
                    pBankName);

            // Check if the company bank account exists
            Company foundCompany = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyBankAccount foundCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(foundCompany,
                    companyBankAccountDTO.getCompanyBankAccountID());

            // if only the source bank account name has changed it is only an update
            boolean isUpdateOnly = companyBankAccountDTO.getCompanyBankAccountID().equals(foundCompanyBankAccount.getSourceBankAccountId());
            isUpdateOnly = isUpdateOnly && companyBankAccountDTO.getBankAccountDTO().getAccountNumber().equals(foundCompanyBankAccount.getBankAccount().getAccountNumber());
            isUpdateOnly = isUpdateOnly && companyBankAccountDTO.getBankAccountDTO().getRoutingNumber().equals(foundCompanyBankAccount.getBankAccount().getRoutingNumber());
            isUpdateOnly = isUpdateOnly && companyBankAccountDTO.getBankAccountDTO().getAccountType() == foundCompanyBankAccount.getBankAccount().getAccountTypeCd();

            ArrayList<ProcessResult> results = new ArrayList<ProcessResult>();

            if (isUpdateOnly) {

                if (foundCompanyBankAccount.getStatusCd().equals(BankAccountStatus.PendingVerification)) {
                    results.add(PayrollServices.companyManager.changeCompanyBankAccountWithAccountService(SourceSystemCode.valueOf(pSourceSystemCd),
                            pSourceCompanyId,
                            companyBankAccountDTO,
                            pShouldAddRandomDebits,
                            pShouldAllowPendingTransactions,
                            pShouldMovePendingTransactionsToAccount,true,true));
                } else {
                    results.add(PayrollServices.companyManager.updateCompanyBankAccountWithAccountService(SourceSystemCode.valueOf(pSourceSystemCd),
                            pSourceCompanyId,
                            companyBankAccountDTO));
                }


            } else {
                results.add(PayrollServices.companyManager.changeCompanyBankAccountWithAccountService(SourceSystemCode.valueOf(pSourceSystemCd),
                        pSourceCompanyId,
                        companyBankAccountDTO,
                        pShouldAddRandomDebits,
                        pShouldAllowPendingTransactions,
                        pShouldMovePendingTransactionsToAccount,true,true));
            }


            for (ProcessResult result : results) {
                if (!result.isSuccess()) {
                    aeFactory.throwGenericException("Error adding bank account.", result);
                }
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding bank account.", pSourceSystemCd, pSourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ResetVerificationAmounts)
    public void resetVerifyAttempts(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd, String pBankAccountId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<CompanyBankAccount> pr = PayrollServices.companyManager.resetBankAccountVerifyRetryCount(SourceSystemCode.valueOf(pSourceSystemCd),
                    pCompanyId, pBankAccountId);

            //We have to always commit due to the verify retry count needing to be set
            PayrollServices.commitUnitOfWork();

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Unable to reset verify attempts.", pr);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error reseting verify attempts.", pSourceSystemCd, pCompanyId, "BankAccount", pBankAccountId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public SAPCompanyBankAccount getCompanyBankAccount(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd) throws Throwable {

        SAPCompanyBankAccount sapCompanyBankAccount = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyBankAccount companyBankAccount;

            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
            // if no active bank account, check for any account of status - pending verification
            if (companyBankAccount == null) {
                companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, BankAccountStatus.PendingVerification);
            }

            if (companyBankAccount != null) {
                sapCompanyBankAccount =
                        CompanyTranslator.getSAPCompanyBankAccountFromDomainEntity(companyBankAccount, PIIMask.authenticatedUserCanViewFullBankAccountNumbers());
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company bank account.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapCompanyBankAccount;
    }

    @FlexMethod
    public int getBankVerificationLimit(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company != null) {
                CompanyOffering ddOffering = company.getOffering(ServiceCode.DirectDeposit);
                CompanyOffering raOffering = company.getOffering(ServiceCode.RiskAssessment);
                if (ddOffering != null) {
                    return ddOffering.getOffering().getLimitRule().findLimitValueByName(LimitValueType.CompanyBankAccountVerificationAttemptLimit).getIntegerValue();
                } else if (raOffering != null) {
                    return raOffering.getOffering().getLimitRule().findLimitValueByName(LimitValueType.CompanyBankAccountVerificationAttemptLimit).getIntegerValue();
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company's bank verification limit", pSourceSystemCd, pCompanyId, t);
            return 0;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @FlexMethod
    public ArrayList<SAPRandomDebit> getRandomDebitTransactions(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd, String pSourceBankAccountId) throws Throwable {

        ArrayList<SAPRandomDebit> randomDebitTransactions = new ArrayList<SAPRandomDebit>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, pSourceBankAccountId);
            if (companyBankAccount != null) {
                DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinancialTransactions(
                        SourceSystemCode.valueOf(pSourceSystemCd),
                        pCompanyId,
                        companyBankAccount.getBankAccount(),
                        TransactionTypeCode.EmployerVerificationDebit,
                        null);

                HashMap<String, DomainEntitySet<FinancialTransaction>> transactionMap = new HashMap<String, DomainEntitySet<FinancialTransaction>>();
                for (FinancialTransaction txn : financialTransactions) {
                    // get all transactions except cancelled ones
                    if (txn.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Cancelled) {
                        // build map key
                        String key = "";
                        if (txn.getSettlementDate() != null) {
                            key += SAPTranslator.getDateFromSpcfCalendar(txn.getSettlementDate()).toString();
                        }
                        if (txn.getMoneyMovementTransaction() != null && txn.getMoneyMovementTransaction().getOffloadBatch() != null) {
                            key += SAPTranslator.getDateFromSpcfCalendar(txn.getMoneyMovementTransaction().getOffloadBatch().getOffloadDate()).toString();
                        }

                        // add pair
                        if (transactionMap.get(key) != null) {
                            DomainEntitySet<FinancialTransaction> list = transactionMap.get(key);
                            list.add(txn);
                            transactionMap.put(key, list);
                        } else {
                            DomainEntitySet<FinancialTransaction> list = new DomainEntitySet<FinancialTransaction>();
                            list.add(txn);
                            transactionMap.put(key, list);
                        }
                    }
                }

                for (String key : transactionMap.keySet()) {
                    randomDebitTransactions.add(
                            CompanyTranslator.getSAPRandomDebitsFromDomainEntities(transactionMap.get(key)));
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding random debit transactions.", pSourceSystemCd, pCompanyId, "BankAccount", pSourceBankAccountId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return randomDebitTransactions;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.GenerateRandomDebits)
    public void reinitiateRandomDebit(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                      String pSourceSystemCd,
                                      String pBankAccountId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<CompanyBankAccount> pr = PayrollServices.companyManager.reinitiateBankAccountRandomDebits(SourceSystemCode.valueOf(pSourceSystemCd),
                    pCompanyId, pBankAccountId);
            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.rollbackUnitOfWork();
                aeFactory.throwGenericException("Error creating random debits for bank account.", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error reinitating random debits.", pSourceSystemCd, pCompanyId, "BankAccount", pBankAccountId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public boolean isPendingRandomDebit(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                        String pSourceSystemCd) throws Throwable {

        DomainEntitySet<FinancialTransaction> finTransactions;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            finTransactions = FinancialTransaction.findFinancialTransactions(SourceSystemCode.valueOf(pSourceSystemCd),
                    pCompanyId,
                    TransactionTypeCode.EmployerVerificationDebit,
                    TransactionStateCode.Created);     // check the type

            return (finTransactions.size() != 0);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error checking for pending random debits.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return false;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddBankAccountByPassRandomDebits, OperationId.AddAssistedBankAccountBypassRandomDollarDebitPreBALF, OperationId.AddBankAccountByPassRandomDebitsPostBALF})
    public void verifyCompanyBankAccount(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                         String pSourceSystemCd,
                                         String pBankAccountId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<CompanyBankAccount> pr = PayrollServices.companyManager.verifyCompanyBankAccount(SourceSystemCode.valueOf(pSourceSystemCd),
                    pCompanyId,
                    pBankAccountId,
                    null,
                    null,
                    true);
            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.rollbackUnitOfWork();
                aeFactory.throwGenericException("Error verifying bank account.", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error verifying bank account.", pSourceSystemCd, pCompanyId, "BankAccount", pBankAccountId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.GeneratePin)
    public String generateRandomPin(String pSourceSystemCd,
                                    @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pCaseId) throws Throwable {
        String generatedPIN = PINUtils.generateRandomPIN();
        try {
            ThreadLocalManager.setValue(pCaseId);
            PayrollServices.beginUnitOfWork();


            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            boolean isCompanyPinCreated = company.isPINCreated();

            ProcessResult processResult;

            if (!isCompanyPinCreated) {
                processResult =
                        PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.valueOf(pSourceSystemCd),
                                pSourceCompanyId,
                                generatedPIN);
            } else {
                processResult =
                        PayrollServices.subscriptionManager.updateCompanyPIN(SourceSystemCode.valueOf(pSourceSystemCd),
                                pSourceCompanyId,
                                generatedPIN);
            }

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error generating random pin.", processResult);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error generating random pin.", pSourceSystemCd, pSourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadLocalManager.flush();
        }
        return generatedPIN;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.GeneratePin)
    public void unlockCompany(String pSourceSystemCd,
                              @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            SourceSystemCode code = SourceSystemCode.valueOf(pSourceSystemCd);

            ProcessResult processResult = PayrollServices.subscriptionManager.unlockPINOnce(code, pSourceCompanyId);

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error unlocking company.", processResult);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error unlocking company.", pSourceSystemCd, pSourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPTransmission> findTransmissions(String pSourceSystemCd,
                                                        @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
                                                        Date pFromDate,
                                                        Date pToDate,
                                                        String pFromSourceSystemCode) throws Throwable {
        ArrayList<SAPTransmission> returnList = new ArrayList<SAPTransmission>();
        try {
            PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.MANUAL, true);
            SourceSystemCode fromSourceSystemCode = SourceSystemCode.valueOf(pFromSourceSystemCode);
            if (fromSourceSystemCode == SourceSystemCode.AMO) {
                DomainEntitySet<EntitlementMessage> entitlementMessages = EntitlementMessage.findCompanyEntitlementMessages(SourceSystemCode.valueOf(pSourceSystemCd),
                        pSourceCompanyId,
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate));
                for (EntitlementMessage entitlementMessage : entitlementMessages) {
                    returnList.add(CompanyTranslator.getTransmissionFromDomainEntity(entitlementMessage, false));
                }
            } else {
                DomainEntitySet<SourceSystemTransmission> transmissionList = SourceSystemTransmission.findCompanyTransmissions(pSourceCompanyId,
                        SourceSystemCode.valueOf(pSourceSystemCd),
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                        (pFromSourceSystemCode != null) ? fromSourceSystemCode : null);
                for (SourceSystemTransmission transmission : transmissionList) {
                    returnList.add(CompanyTranslator.getTransmissionFromDomainEntity(transmission, false));
                }
            }


        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding transmissions.", pSourceSystemCd, pSourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
        return returnList;
    }

    @FlexMethod
    public SAPTransmission findTransmissionById(String pTransmissionId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.MANUAL, true);
            SourceSystemTransmission transmission = SourceSystemTransmission.getSourceSystemTransmissionById(pTransmissionId);

            if (transmission == null) {
                EntitlementMessage entitlementMessage = PayrollServices.entityFinder.findById(EntitlementMessage.class,
                        SpcfUniqueId.createInstance(pTransmissionId));
                return CompanyTranslator.getTransmissionFromDomainEntity(entitlementMessage, true);
            } else {
                return CompanyTranslator.getTransmissionFromDomainEntity(transmission, true);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding transmission.", "Transmission", pTransmissionId, t);
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
        return null;
    }

    @FlexMethod
    public SAPSearchResults<SAPTransmission> findTransmissionByIPAndDate(String pIPAddress, Date pFromDate, Date pToDate) throws Throwable {

        ArrayList<SAPTransmission> returnList = new ArrayList<SAPTransmission>();
        SAPSearchResults<SAPTransmission> sapSearchResults = new SAPSearchResults<SAPTransmission>();

        try {
            ArrayList<Object[]> transmissionList = null;
            transmissionList = SourceSystemTransmission.findCompanyTransmissionByIPAndDate(pIPAddress,
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate));
            int records = 0;
            for (Object[] transmission : transmissionList) {
                returnList.add(CompanyTranslator.getSAPTransmissionByIPAndDate(transmission));
                records++;
            }
            sapSearchResults.setTotalRecords(records);
            sapSearchResults.setReturnsList(returnList);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding transmissions.", pIPAddress, pFromDate.toString() + pToDate.toString(), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapSearchResults;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RemoveFromSignupFraudHold)
    public void removeFraudFlag(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<Company> pr = PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.valueOf(pSourceSystemCd),
                    pCompanyId);
            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error removing fraud flag.", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error removing fraud flag.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditDebugLogging)
    public void switchDebugLogging(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID)  String pCompanyId, boolean pDebugLogging) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

            companyDTO.setDebugLogging(pDebugLogging);
            ProcessResult<Company> pr = PayrollServices.companyManager.updateCompany(
                    companyDTO.getSourceSystemCd(),
                    companyDTO.getCompanyId(),
                    companyDTO);

            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error switching debug logging flag.", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error switching debug logging flag.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditProcessTransmissions)
    public void switchProcessTransmissions(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID)  String pCompanyId, boolean pProcessTransmissions) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (!SourceSystemCode.QBDT.toString().equals(pSourceSystemCd) || !(company.isCompanyOnService(ServiceCode.Cloud) || company.isCompanyOnService(ServiceCode.Tax))) {
                throw new RuntimeException("Transmission processing cannot be changed unless the company is on DIY/DD/Assisted service.");
            }

            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

            companyDTO.getQuickBooksInfo().setProcessTransmissions(pProcessTransmissions);
            ProcessResult<Company> pr = PayrollServices.companyManager.updateQBCompanyInfo(companyDTO.getSourceSystemCd(),
                    companyDTO.getCompanyId(),
                    companyDTO);

            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating process transmissions flag.", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating process transmissions flag.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditAllowTransmissions)
    public void switchAllowTransmissions(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID)  String pCompanyId, boolean pAllowTransmissions) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (!SourceSystemCode.QBDT.toString().equals(pSourceSystemCd) || !(company.isCompanyOnService(ServiceCode.Cloud) || company.isCompanyOnService(ServiceCode.Tax))) {
                throw new RuntimeException("Allow Transmissions cannot be changed unless the company is on DIY/DD/Assisted service.");
            }

            boolean oldValue = company.getQuickbooksInfo().getAllowTransmissions();

            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.getQuickBooksInfo().setAllowTransmissions(pAllowTransmissions);
            ProcessResult<Company> pr = PayrollServices.companyManager.updateQBCompanyInfo(companyDTO.getSourceSystemCd(),
                    companyDTO.getCompanyId(),
                    companyDTO);

            if (pr.isSuccess()) {
                CompanyEvent.createAllowTransmissionChangedEvent(company, pAllowTransmissions, oldValue);
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating allow transmissions flag.", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating allow transmissions flag.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewSignupFraudQueue)
    public ArrayList<SAPFraudEvent> findCompanyFraudEvents(String pEinCid,
                                                           String pFraudEventCategory,
                                                           double pPayrollAmount,
                                                           Date pFromDate,
                                                           Date pToDate,
                                                           ArrayList<String> eventTypeCodes) throws Throwable {

        ArrayList<SAPFraudEvent> returnList = new ArrayList<SAPFraudEvent>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar fromDate = (pFromDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate));
            SpcfCalendar toDate = (pToDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate));
            SpcfMoney payrollNetAmount = SAPTranslator.getSpcfMoneyFromDouble(pPayrollAmount);

            Collection<EventTypeCode> eventTypeCodeCollection = null;
            if (eventTypeCodes != null && eventTypeCodes.size() > 0) {
                eventTypeCodeCollection = new ArrayList<EventTypeCode>();
                for (String eventTypeCode : eventTypeCodes) {
                    eventTypeCodeCollection.add(EventTypeCode.valueOf(eventTypeCode));
                }
            }

            DomainEntitySet<FraudEvent> fraudEvents =
                    FraudEvent.findActiveCompanyFraudEventsReport(pEinCid,
                            (pFraudEventCategory != null) ? FraudEventCategory.valueOf(pFraudEventCategory) : null,
                            fromDate, toDate,
                            payrollNetAmount,
                            eventTypeCodeCollection);

            for (FraudEvent fraudEvent : fraudEvents) {
                returnList.add(CompanyTranslator.getSAPFraudEvent(fraudEvent));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding fraud events.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnList;
    }

    @FlexMethod
    public ArrayList<SAPCompanyEventType> getFraudEventTypes() throws Throwable {
        ArrayList<SAPCompanyEventType> sapFraudEventTypes = new ArrayList<SAPCompanyEventType>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<EventType> fraudEventTypes = Application.find(EventType.class,
                    new Query<EventType>().Where(
                            EventType.EventGroupCd().equalTo(EventGroup.Fraud)
                                    .And(EventType.EventTypeCd().notEqualTo(EventTypeCode.FraudFlagRemovedEvent)))
                            .OrderBy(EventType.Name()));

            for (EventType et : fraudEventTypes) {
                sapFraudEventTypes.add(CompanyTranslator.getCompanyEventTypeFromDomainEntity(et));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding fraud event types", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapFraudEventTypes;
    }

    @FlexMethod
    public ArrayList<SAPCompanyNote> findCompanyNotes(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pCompanyEventId, String pCompanyEventTransmissionId) throws Throwable {
        ArrayList<SAPCompanyNote> sapCompanyNotes = new ArrayList<SAPCompanyNote>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            for (CompanyNote companyNote : company.getCompanyNoteCollection()) {
                boolean isCompanyEventNote = (pCompanyEventId != null && companyNote.getCompanyEvent() != null && companyNote.getCompanyEvent().getId().toString().equals(pCompanyEventId));
                boolean isCompanyEventTransmissionNote = pCompanyEventTransmissionId != null && companyNote.getCompanyEvent() != null &&
                        pCompanyEventTransmissionId.equals(companyNote.getCompanyEvent().getEventDetailInfo().get(EventDetailTypeCode.TransmissionId));

                if ((pCompanyEventId == null && pCompanyEventTransmissionId == null) || isCompanyEventNote || isCompanyEventTransmissionNote) {
                    sapCompanyNotes.add(CompanyTranslator.getSAPCompanyNoteFromDomainEntity(companyNote));
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving notes ", pSourceSystemCd, pCompanyId, "CompanyEvent", pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapCompanyNotes;
    }

    @FlexMethod
    public ArrayList<SAPCompanyNote> findManualNotes(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        ArrayList<SAPCompanyNote> sapCompanyNotes = new ArrayList<SAPCompanyNote>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<CompanyEvent> manualNoteEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.ManualNoteEvent, null, null, null);

            for (CompanyEvent event : manualNoteEvents) {
                for (CompanyNote companyNote : event.getCompanyNoteCollection()) {
                    sapCompanyNotes.add(CompanyTranslator.getSAPCompanyNoteFromDomainEntity(companyNote));
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving notes ", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapCompanyNotes;
    }

    @FlexMethod
    public List<SAPCompanyEventGroup> findCompanyEventGroups(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            HqlBuilder hql = new HqlBuilder(true, "select distinct ce.EventTypeCd from com.intuit.sbd.payroll.psp.domain.CompanyEvent ce where ce.Company = :company");
            hql.setParameter("company", company);
            List<EventTypeCode> eventTypeCodes = hql.list();

            Map<EventGroup, SAPCompanyEventGroup> eventGroups = new HashMap<EventGroup, SAPCompanyEventGroup>();

            for (EventTypeCode eventTypeCode : eventTypeCodes) {
                EventType eventType = PayrollServices.entityFinder.findById(EventType.class, eventTypeCode);

                //Look for group in existing groups added
                if (eventType.getEventGroupCd() != null) {
                    SAPCompanyEventGroup sapEventGroup = eventGroups.get(eventType.getEventGroupCd());
                    if (sapEventGroup == null) {
                        sapEventGroup = CompanyTranslator.getSAPCompanyEventGroup(eventType.getEventGroupCd());
                        eventGroups.put(eventType.getEventGroupCd(), sapEventGroup);
                    }
                    sapEventGroup.getChildren().add(CompanyTranslator.getSAPCompanyEventGroupItem(eventType));
                }

            }

            return new ArrayList<SAPCompanyEventGroup>(eventGroups.values());


        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company event groups.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public List<SAPUser> findCompanyEventCreators(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            List<Object[]> userIds = Application.executeNamedQuery("findUserNameForCompanyEvents", new String[]{"companyId"}, new Object[]{company.getId().toString()});
            List<SAPUser> sapUsers = new ArrayList<SAPUser>();
            SAPUser sapUser = new SAPUser();
            sapUser.setCorpId("");
            sapUser.setFirstName("");
            sapUser.setLastName("");
            sapUsers.add(sapUser);
            for (Object[] userId : userIds) {
                sapUser = new SAPUser();
                sapUser.setCorpId((String) userId[0]);
                sapUser.setFirstName((String) userId[1]);
                sapUser.setLastName((String) userId[2]);
                sapUsers.add(sapUser);
            }
            return sapUsers;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company event creators.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public SAPCompanyEventQueryReturn findCompanyEvents(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, Date pFromDate, Date pToDate, String creatorId, ArrayList<String> eventTypes, boolean includeAS400Events) throws Throwable {
        SAPCompanyEventQueryReturn sapCompanyEventQueryReturn = new SAPCompanyEventQueryReturn();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            ArrayList<SAPCompanyEvent> sapCompanyEvents = new ArrayList<SAPCompanyEvent>();

            Company company = Company.findCompanyNoEagerLoad(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            //Convert from and to date to SPCF begin and end dates
            SpcfCalendar fromDate = (pFromDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate));
            SpcfCalendar toDate = (pToDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate));

            // Build event type array
            EventTypeCode[] eventTypeCodes;

            if (eventTypes == null || eventTypes.size() == 0) {
                eventTypeCodes = null;
            } else {
                eventTypeCodes = new EventTypeCode[eventTypes.size()];
                for (int i = 0; i < eventTypes.size(); i++) {
                    eventTypeCodes[i] = EventTypeCode.valueOf(eventTypes.get(i));
                }
            }

            DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEventsByTypes(company, eventTypeCodes, creatorId, fromDate, toDate, 501);
            if (companyEventList.size() > 500) {
                companyEventList.remove(500);
                sapCompanyEventQueryReturn.setMoreEventsExistForQuery(true);
            }

            Map<String, String> userCache = new HashMap<String, String>();
            for (CompanyEvent companyEvent : companyEventList) {
                EventType eventType = PayrollServices.entityFinder.findById(EventType.class, companyEvent.getEventTypeCd());
                sapCompanyEvents.add(CompanyTranslator.getSAPCompanyEventFromDomainEntity(companyEvent, eventType, userCache));
            }

            sapCompanyEventQueryReturn.setEvents(sapCompanyEvents);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company events.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapCompanyEventQueryReturn;
    }

    @FlexMethod
    public ArrayList<SAPCompanyEvent> getRecentCompanyEvents(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, int max) throws Throwable {
        ArrayList<SAPCompanyEvent> sapCompanyEvents = new ArrayList<SAPCompanyEvent>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            // keep a list of the transmission events processed
            List<String> transmissionIds = new ArrayList<String>();

            DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findRecentCompanyEvents(company, max);
            EventType eventType;
            Map<String, String> userCache = new HashMap<String, String>();
            for (CompanyEvent companyEvent : companyEventList) {
                switch (companyEvent.getEventTypeCd()) {
                    // if the event is one of the following merge all of the related events into one event in the ui
                    case InvalidSourceSystemTransmissionInformation:
                    case InvalidPaycheckInformation:
                    case InvalidEmployeeInformation:
                        String transmissionId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.TransmissionId);
                        if (transmissionId != null) {
                            if (!transmissionIds.contains(transmissionId)) {
                                transmissionIds.add(transmissionId);
                                DomainEntitySet<CompanyEvent> companyEvents = new DomainEntitySet<CompanyEvent>();
                                companyEvents.add(companyEvent);
                                sapCompanyEvents.add(CompanyTranslator.getSAPCompanyEventForTransactionResponse(transmissionId, companyEvents));
                            }
                            break;
                        }

                    default:
                        eventType = PayrollServices.entityFinder.findById(EventType.class, companyEvent.getEventTypeCd());
                        sapCompanyEvents.add(CompanyTranslator.getSAPCompanyEventFromDomainEntity(companyEvent, eventType, userCache));
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding recent events.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapCompanyEvents;
    }

    @FlexMethod
    public void resendEmail(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String emailId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult pr = PayrollServices.companyManager.resendEmail(SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId, emailId, true);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error resending email.", pr);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error resending email.", pSourceSystemCd, pCompanyId, "Email", emailId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @FlexMethod
    public void sendEmailToMtl(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String emailSeqId, String sessionUserEmailAddress) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult pr = PayrollServices.companyManager.sendEmailToMtl(SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId, emailSeqId, true, sessionUserEmailAddress);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error sending email to your email ID (SSO login)", pr);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error sending email to your email ID (SSO login)", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }


    @FlexMethod
    public ArrayList<SAPCompanyServiceStatusHistoryItem> findCompanyServiceStatusHistory(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemId,
            Date pFromDate) throws Throwable {


        ArrayList<SAPCompanyServiceStatusHistoryItem> historyItems = new ArrayList<SAPCompanyServiceStatusHistoryItem>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemId));

            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.ServiceStatusChange
                    , CompanyEventStatus.Active, SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate), null, true);

            Collection<String> oldReasons;
            Collection<String> newReasons;
            String oldStatus;
            String newStatus;
            String serviceCd;
            for (CompanyEvent companyEvent : companyEventList) {
                oldReasons = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.OldOnHoldReason);
                newReasons = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.NewOnHoldReason);

                oldStatus = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldServiceStatus);
                newStatus = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewServiceStatus);

                oldStatus = (oldReasons.size() > 0) ? "On Hold" : oldStatus;
                newStatus = (newReasons.size() > 0) ? "On Hold" : newStatus;

                serviceCd = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ServiceCode);
                if (serviceCd == null) {
                    serviceCd = "All";
                }

                historyItems.add(CompanyTranslator.getSAPCompanyStatusHistoryItemFromDomainEntity(serviceCd, newStatus, oldStatus, companyEvent.getEventTimeStamp(), SAPTranslator.getUserNameFromUserID(companyEvent.getCreatorId()), newReasons, oldReasons));
            }

            Collections.reverse(historyItems);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company service history", pSourceSystemId, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return historyItems;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditTokens)
    public void adjustCompanyTokens(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, SAPQBDTTokens tokens) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            if (StringUtils.isNotEmpty(tokens.getHighToken())) {
                companyDTO.setCurrentToken(Long.parseLong(tokens.getHighToken()));
            }
            if (StringUtils.isNotEmpty(tokens.getEmployeeNextId())) {
                companyDTO.setNextEmployeeId(tokens.getEmployeeNextId());
            }
            if (StringUtils.isNotEmpty(tokens.getPaycheckNextId())) {
                companyDTO.setNextPaycheckId(tokens.getPaycheckNextId());
            }
            if (StringUtils.isNotEmpty(tokens.getPayrollItemNextId())) {
                companyDTO.setNextPayrollItemId(tokens.getPayrollItemNextId());
            }
            if (StringUtils.isNotEmpty(tokens.getPayrollTxNextId())) {
                companyDTO.setNextPayrollTransactionId(tokens.getPayrollTxNextId());
            }

            ProcessResult pr = PayrollServices.companyManager.updateCompanyTokensAndIdsCore(SourceSystemCode.valueOf(pSourceSystemCode), pSourceCompanyId, companyDTO, false);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error adjusting company tokens.", pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adjusting company tokens.", pSourceSystemCode, pSourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }


    }

    @FlexMethod
    public SAPSearchResults<SAPCompanyStatusSearchResult> getCompaniesByServiceSubstatuses(ArrayList<SAPServiceSubStatus> pSubStatuses,
                                                                                           boolean searchForOnHoldStatuses,
                                                                                           String pOrderBy,
                                                                                           boolean pOrderDesc,
                                                                                           int pFirstResult,
                                                                                           int pMaxResults) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            ArrayList<ServiceSubStatusCode> subStatuses = new ArrayList<ServiceSubStatusCode>();
            for (SAPServiceSubStatus sapServiceSubStatus : pSubStatuses) {
                subStatuses.add(getServiceSubStatusFromStatusCd(sapServiceSubStatus.getSubStatusCd()).getServiceSubStatusCd());
            }

            long totalCompanies;
            Collection<Company> companies;
            if (searchForOnHoldStatuses) {
                companies = Company.findCompaniesByOnHoldSubStatus(subStatuses,
                        pOrderBy,
                        pOrderDesc,
                        pFirstResult,
                        pMaxResults);
                totalCompanies = Company.getCompaniesByOnHoldSubStatusCount(subStatuses);
            } else {
                companies = Company.findCompaniesByPendingSubStatus(subStatuses,
                        pOrderBy,
                        pOrderDesc,
                        pFirstResult,
                        pMaxResults);
                totalCompanies = Company.getCompaniesByPendingSubStatusCount(subStatuses);
            }
            ArrayList<SAPCompanyStatusSearchResult> sapCompanies = new ArrayList<SAPCompanyStatusSearchResult>();
            for (Company currCompany : companies) {
                SAPCompanyStatusSearchResult currSAPCompany = getSAPCompanyStatusSearchResult(currCompany, searchForOnHoldStatuses);
                sapCompanies.add(currSAPCompany);
            }
            SAPSearchResults<SAPCompanyStatusSearchResult> sapSearchResults = new SAPSearchResults<SAPCompanyStatusSearchResult>();
            sapSearchResults.setTotalRecords(totalCompanies);
            sapSearchResults.setReturnsList(sapCompanies);
            return sapSearchResults;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding companies by service status.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }


    @FlexMethod
    public SAPCompanyLegalInfo getCompanyLegalInfo(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
                aeFactory.throwGenericException("Error finding company legal information: Source System Code was blank or missing.");
            }

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null)
                throw aeFactory.companyNotFoundException();

            EntityChange entityChange = EntityChange.findMostRecentEntityChangeForCompany(company);
            return CompanyTranslator.getCompanyLegalInfoFromDomainEntity(company, entityChange);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company legal information.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.EditCompanyLegalInformation,
            OperationId.EditAssistedCompanyLegalInfo,
            OperationId.EditAssistedCompanyLegalInfoPendingActivation
    })
    public void updateEntityChange(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, SAPEntityChangeDTO pEntityChangeDTO)
            throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null)
                throw aeFactory.companyNotFoundException();

            EntityChange entityChange = EntityChange.findEntityChange(company, pEntityChangeDTO.getOldEIN(), pEntityChangeDTO.getNewEIN());
            if (entityChange == null) {
                aeFactory.throwGenericException("The entity change record was not found.");
            }
            Date effectiveDate = pEntityChangeDTO.getEffectiveDate();

            EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
            entityChangeDTO.setNewEIN(entityChange.getNewEIN());
            entityChangeDTO.setOldEIN(entityChange.getOldEIN());
            PspPrincipal currentPrincipal = Application.getCurrentPrincipal();
            String currentId = null;
            if (currentPrincipal != null) {
                currentId = currentPrincipal.getId();
                entityChangeDTO.setUserId(currentId);
            }
            entityChangeDTO.setEffectiveDate(new DateDTO(effectiveDate));
            entityChangeDTO.setHasNewDataFile(entityChange.getHasNewDataFile());
            entityChangeDTO.setIsSuccessor(pEntityChangeDTO.getIsSuccessor());
            entityChangeDTO.setIsError(pEntityChangeDTO.getIsError());
            ProcessResult<EntityChange> pr = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), entityChangeDTO);
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error updating Entity Change", pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Exception e) {
            aeFactory.throwGenericException("Error updating effective date.", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.EditCompanyLegalInformation,
            OperationId.EditAssistedCompanyLegalInfo,
            OperationId.EditAssistedCompanyLegalInfoPendingActivation
    })
    public void updateCompanyLegalInfo(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, SAPCompanyLegalInfo pLegalInfo, String pCaseId)
            throws Throwable {
        try {
            ThreadLocalManager.setValue(pCaseId);
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null)
                throw aeFactory.companyNotFoundException();

            CompanyDTO companyDTO = new DTOFactory().create(company);
            companyDTO.setLegalName(pLegalInfo.getLegalName());
            companyDTO.setDBA(pLegalInfo.getDoingBusinessAs());
            companyDTO.setLegalAddress(CompanyTranslator.getAddressDTOFromSAPAddress(pLegalInfo.getAddress()));
            companyDTO.setFein(pLegalInfo.getEin());
            companyDTO.setCompanyAdditionalInfo(CompanyTranslator.getCompanyAdditionalInfoFromSAPLegalInfo(pLegalInfo));
            if (!company.getFedTaxId().equals(pLegalInfo.getEin())) {
                EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
                entityChangeDTO.setHasNewDataFile(false);
                entityChangeDTO.setIsSuccessor(true);
                entityChangeDTO.setIsError(pLegalInfo.getIsOldEinError());
                if (pLegalInfo.getEinEffectiveDate() != null) {
                    entityChangeDTO.setEffectiveDate(new DateDTO(pLegalInfo.getEinEffectiveDate()));
                }
                companyDTO.setEntityChange(entityChangeDTO);
            }

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            prList.add(
                    PayrollServices.companyManager.updateCompanyWithAccountService(SourceSystemCode.valueOf(pSourceSystemCd),
                            pCompanyId,
                            companyDTO)
            );

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error updating company legal information.", "Company",
                        pSourceSystemCd + ":" + pCompanyId, prList);
            } else {
                PayrollServices.commitUnitOfWork();
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating company legal information.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadLocalManager.flush();
        }

    }

    @FlexMethod
    public SAPCompanyContacts getCompanyContacts(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
                aeFactory.throwGenericException("Error finding company contacts: Source System Code was blank or missing.");
            }

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null)
                throw aeFactory.companyNotFoundException();

            return CompanyTranslator.getSAPCompanyContactsFromDomainEntity(company);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company contacts.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.EditCompanyContactInformation,
            OperationId.EditPrincipalContactsDIYOnly,
            OperationId.EditAssistedPayrollContactsInActiveStatus,
            OperationId.EditAssistedPayrollContactsInPendingActivation,
            OperationId.EditAssistedPrincipalContacts,
            OperationId.EditAssistedPrincipalContactsInPendingActivation
    })
    public void updateCompanyContacts(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, ArrayList<SAPContact> pContacts, String pCaseId)
            throws Throwable {
        try {
            ThreadLocalManager.setValue(pCaseId);
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }


            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

            ArrayList<ContactDTO> contactDTOs = new ArrayList<ContactDTO>(pContacts.size());
            for (SAPContact contact : pContacts) {
                contactDTOs.add(CompanyTranslator.getContactDTOFromSAPContact(contact));
            }

            companyDTO.setContacts(contactDTOs);


            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            prList.add(
                    PayrollServices.companyManager.updateCompanyWithAccountService(SourceSystemCode.valueOf(pSourceSystemCd),
                            pCompanyId,
                            companyDTO));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error updating company contact information", "Company",
                        pSourceSystemCd + ":" + pCompanyId, prList);
            } else {
                PayrollServices.commitUnitOfWork();
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating company contacts.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadLocalManager.flush();
        }

    }

    @FlexMethod
    public SAPTaxExemptInfo getTaxExemptStatus(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        SAPTaxExemptInfo sapTaxExemptInfo = new SAPTaxExemptInfo();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            sapTaxExemptInfo.setExemptStatus(company.getTaxExemptStatus().toString());
            sapTaxExemptInfo.setExpirationDate(SAPTranslator.getDateFromSpcfCalendar(company.getTaxExemptExpirationDate()));
            sapTaxExemptInfo.setIsCurrentlyExempt(company.isTaxExempt());

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding tax exemption status", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapTaxExemptInfo;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditTaxExemptFlag)
    public void updateTaxExemptStatus(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, SAPTaxExemptInfo info) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            CompanyDTO companyDTO = new DTOFactory().create(company);
            companyDTO.setTaxExemptStatus(TaxExemptStatusCode.valueOf(info.getExemptStatus()));
            if (info.getExpirationDate() != null) {
                companyDTO.setTaxExemptExpirationDate(new DateDTO(info.getExpirationDate()));
            } else {
                companyDTO.setTaxExemptExpirationDate(null);
            }

            ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error updating company sales tax information.", processResult);
            } else {
                PayrollServices.commitUnitOfWork();
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating company legal information.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPServiceStatus> getServiceStatusList() throws Throwable {
        ArrayList<SAPServiceStatus> sapServiceSubStatusList = new ArrayList<SAPServiceStatus>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<ServiceSubStatus> serviceStatusList =
                    PayrollServices.entityFinder.findObjects(ServiceSubStatus.class);

            for (ServiceSubStatus serviceSubStatus : serviceStatusList) {
                sapServiceSubStatusList.add(CompanyTranslator.getSAPServiceStatusFromDomainEntity(serviceSubStatus));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding service status list.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapServiceSubStatusList;
    }

    @FlexMethod
    public ArrayList<SAPServiceSubStatus> getServiceSubStatusList() throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            ArrayList<SAPServiceSubStatus> sapServiceSubStatusList = new ArrayList<SAPServiceSubStatus>();
            DomainEntitySet<ServiceSubStatus> serviceStatusList =
                    PayrollServices.entityFinder.findObjects(ServiceSubStatus.class);

            for (ServiceSubStatus serviceSubStatus : serviceStatusList) {
                sapServiceSubStatusList.add(CompanyTranslator.getSAPServiceSubStatusFromDomainEntity(serviceSubStatus));
            }
            return sapServiceSubStatusList;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding service sub status list.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public ArrayList<SAPFundingModel> getFundingModelList() throws Throwable {

        ArrayList<SAPFundingModel> returnVal = new ArrayList<SAPFundingModel>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<FundingModel> fundingModelList =
                    PayrollServices.entityFinder.findObjects(FundingModel.class);

            for (FundingModel model : fundingModelList) {
                returnVal.add(CompanyTranslator.getSAPFundingModelFromDomainEntity(model));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding funding model list.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    @FlexMethod
    public ArrayList<SAPSourceSystem> getSourceSystemList() throws Throwable {
        ArrayList<SAPSourceSystem> returnVal = new ArrayList<SAPSourceSystem>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<SourceSystem> sourceSystemList =
                    PayrollServices.entityFinder.findObjects(SourceSystem.class);

            for (SourceSystem sourceSystem : sourceSystemList) {
                returnVal.add(CompanyTranslator.getSAPSourceSystemFromDomainEntity(sourceSystem));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding source system list.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AddOffer, OperationId.AddAssistedOfferPreBALF, OperationId.AddAssistedOfferPostBALF})
    public ArrayList<SAPOffer> getCompanyOffers(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String serviceCd) throws Throwable {
        ArrayList<SAPOffer> sapOffers = new ArrayList<SAPOffer>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            CompanyOffering companyOffering = company.getOffering(ServiceCode.valueOf(serviceCd));
            if (companyOffering != null) {
                DomainEntitySet<CompanyOffer> companyOffers = company.getActiveCompanyOffersForOffering(companyOffering.getOffering().getOfferingCode());
                for (CompanyOffer companyOffer : companyOffers) {
                    if (companyOffer.companyOfferIsActive()) {
                        sapOffers.add(BillingTranslator.getCompanyOfferFromDomainEntity(companyOffer));
                    }
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company offers.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapOffers;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.AddVendorPaymentService,
            OperationId.AddCheckDistributionService
    })
    public void addServiceToCompany(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pServiceCd, String caseId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            ThreadLocalManager.setValue(caseId);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            // add service
            ServiceInfoDTO serviceInfoDTO;
            ServiceCode serviceCode = ServiceCode.valueOf(pServiceCd);
            switch (serviceCode) {
                case CheckDistribution:
                    serviceInfoDTO = new CheckDistributionServiceInfoDTO();
                    break;
                default:
                    serviceInfoDTO = new ServiceInfoDTO();
            }
            serviceInfoDTO.setServiceCode(serviceCode);

            ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), serviceInfoDTO);

            if (companyServicePR == null || !companyServicePR.isSuccess()) {
                aeFactory.throwGenericException("Error adding company service", companyServicePR);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding company to PSP", pSourceSystemCd, pCompanyId, t);
        } finally {
            ThreadLocalManager.flush();
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCheckPrintQueue)
    public SAPSearchResults<SAPCheckPrintingBatch> findCheckPrintingBatches(String pEinPsid,
                                                                            String pBatchStatus,
                                                                            Date pCheckFromDate,
                                                                            Date pCheckToDate,
                                                                            Date pPrintFromDate,
                                                                            Date pPrintToDate,
                                                                            String pOrderBy,
                                                                            boolean pOrderDesc,
                                                                            int pFirstResult,
                                                                            int pMaxResults) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar checkFromDate = (pCheckFromDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_BeginDay(pCheckFromDate));
            SpcfCalendar checkToDate = (pCheckToDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_EndDay(pCheckToDate));
            SpcfCalendar printFromDate = (pPrintFromDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_BeginDay(pPrintFromDate));
            SpcfCalendar printToDate = (pPrintToDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_EndDay(pPrintToDate));

            Criterion<CompanyPaycheckBatch> where = null;
            if (pEinPsid != null) {
                List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pEinPsid);
                where = CompanyPaycheckBatch.Company().FedTaxIdEnc().in(fedTaxIdEncList)
                        .Or(CompanyPaycheckBatch.Company().SourceCompanyId().equalTo(pEinPsid));
            }

            if (checkFromDate != null) {
                Criterion<CompanyPaycheckBatch> checkDateWhere = CompanyPaycheckBatch.PaycheckDate().greaterOrEqualThan(checkFromDate);
                if (where == null) {
                    where = checkDateWhere;
                } else {
                    where = where.And(checkDateWhere);
                }
            }

            if (checkToDate != null) {
                Criterion<CompanyPaycheckBatch> checkDateWhere = CompanyPaycheckBatch.PaycheckDate().lessOrEqualThan(checkToDate);
                if (where == null) {
                    where = checkDateWhere;
                } else {
                    where = where.And(checkDateWhere);
                }
            }

            if (printFromDate != null) {
                Criterion<CompanyPaycheckBatch> printDateWhere = CompanyPaycheckBatch.BaseSentToPrinter().greaterOrEqualThan(printFromDate);
                if (where == null) {
                    where = printDateWhere;
                } else {
                    where = where.And(printDateWhere);
                }
            }

            if (printToDate != null) {
                Criterion<CompanyPaycheckBatch> printDateWhere = CompanyPaycheckBatch.BaseSentToPrinter().lessOrEqualThan(printToDate);
                if (where == null) {
                    where = printDateWhere;
                } else {
                    where = where.And(printDateWhere);
                }
            }

            if (pBatchStatus != null) {
                Criterion<CompanyPaycheckBatch> batchStatusWhere = CompanyPaycheckBatch.BaseCheckPrintBatchStatusCode().equalTo(CheckPrintBatchStatus.valueOf(pBatchStatus));
                if (where == null) {
                    where = batchStatusWhere;
                } else {
                    where = where.And(batchStatusWhere);
                }
            }

            ScalarProperty orderBy = null;
            if (pOrderBy == null) {
                orderBy = CompanyPaycheckBatch.Company().FedTaxIdEnc();
            } else if (pOrderBy.equalsIgnoreCase("ein")) {
                orderBy = CompanyPaycheckBatch.Company().FedTaxIdEnc();
            } else if (pOrderBy.equalsIgnoreCase("legalName")) {
                orderBy = CompanyPaycheckBatch.Company().LegalName();
            } else if (pOrderBy.equalsIgnoreCase("paycheckDate")) {
                orderBy = CompanyPaycheckBatch.PaycheckDate();
            } else if (pOrderBy.equalsIgnoreCase("sentToPrinterDate")) {
                orderBy = CheckPrintBatch.SentToPrinter();
            } else if (pOrderBy.equalsIgnoreCase("paycheckCount")) {
                orderBy = CheckPrintBatch.NumberOfChecks();
            } else if (pOrderBy.equalsIgnoreCase("printStatus")) {
                orderBy = CheckPrintBatch.CheckPrintBatchStatusCode();
            }

            if (orderBy != null && pOrderDesc) {
                orderBy.Descending();
            }

            Expression<CompanyPaycheckBatch> query =
                    new Query<CompanyPaycheckBatch>()
                            .Where(where)
                            .OrderBy(orderBy)
                            .EagerLoad(CompanyPaycheckBatch.Company())
                            .LimitResults(pFirstResult, pMaxResults);

            DomainEntitySet<CompanyPaycheckBatch> checkPrintBatches = Application.find(CompanyPaycheckBatch.class, query);

            Expression<CompanyPaycheckBatch> countQuery = new Query<CompanyPaycheckBatch>().Select(CheckPrintBatch.Id().Count()).Where(where);
            List countPrintBatches = Application.executeQuery(CompanyPaycheckBatch.class, countQuery);

            SAPSearchResults<SAPCheckPrintingBatch> sapSearchResults = new SAPSearchResults<SAPCheckPrintingBatch>();
            sapSearchResults.setTotalRecords(Long.parseLong(countPrintBatches.get(0).toString()));

            ArrayList<SAPCheckPrintingBatch> sapCheckPrintingBatches = new ArrayList<SAPCheckPrintingBatch>();
            for (CompanyPaycheckBatch checkPrintBatch : checkPrintBatches) {
                sapCheckPrintingBatches.add(CompanyTranslator.getSAPCheckPrintingBatchFromCheckPrintBatch(checkPrintBatch));
            }
            if (orderBy == null || "ein".equalsIgnoreCase(pOrderBy)) {
                Collections.sort(sapCheckPrintingBatches, (SAPCheckPrintingBatch a, SAPCheckPrintingBatch b) -> b.getEin().compareTo(a.getEin()));
                if (!pOrderDesc)
                    Collections.reverse(sapCheckPrintingBatches);
            }
            sapSearchResults.setReturnsList(sapCheckPrintingBatches);

            return sapSearchResults;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding check print batches", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        SAPSearchResults<SAPCheckPrintingBatch> sapSearchResults = new SAPSearchResults<SAPCheckPrintingBatch>();
        sapSearchResults.setTotalRecords(0);
        sapSearchResults.setReturnsList(new ArrayList<SAPCheckPrintingBatch>());
        return sapSearchResults;
    }

    @SuppressWarnings({"JpaQlInspection"})
    @FlexMethod
    @Operation(operationIds = OperationId.ViewCheckPrintQueue)
    public SAPSearchResults<SAPAgencyCheckBatch> findAgencyPrintingBatches(String pPaymentTemplateCd,
                                                                           String pBatchStatus,
                                                                           Date pInitiationFromDate,
                                                                           Date pInitiationToDate,
                                                                           Date pPrintFromDate,
                                                                           Date pPrintToDate,
                                                                           String pOrderBy,
                                                                           boolean pOrderDesc,
                                                                           int pFirstResult,
                                                                           int pMaxResults) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar initiationFromDate = (pInitiationFromDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_BeginDay(pInitiationFromDate));
            SpcfCalendar initiationToDate = (pInitiationToDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_EndDay(pInitiationToDate));
            SpcfCalendar printFromDate = (pPrintFromDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_BeginDay(pPrintFromDate));
            SpcfCalendar printToDate = (pPrintToDate == null) ? null : (SAPTranslator.getSpcfCalendarFromDate_EndDay(pPrintToDate));

            Collection<AgencyCheckBatch> agencyCheckBatches;

            String hqlSelect = "SELECT distinct acb";
            StringBuilder hqlFrom = new StringBuilder(" FROM com.intuit.sbd.payroll.psp.domain.AgencyCheckBatch AS acb\n");
            String hqlCountSelect = "SELECT count(distinct acb)";
            StringBuilder hqlWhere = new StringBuilder(" WHERE");
            String hqlSelectMMT = " JOIN acb.PaymentBatchAssocSet AS pba\n JOIN pba.MoneyMovementTransaction AS mmt\n";
            StringBuilder hqlOrderBy = new StringBuilder(" ORDER BY");

            Boolean firstWhereExpression = true;
            if (initiationFromDate != null || initiationToDate != null) {
                /*  Adding MMT JOIN */
                hqlFrom.append(hqlSelectMMT);
                if (initiationFromDate != null) {
                    firstWhereExpression = false;
                    hqlWhere.append(" mmt.InitiationDate >= :pInitiationFromDate");
                }
                if (initiationToDate != null) {
                    if (!firstWhereExpression) {
                        hqlWhere.append(" AND");
                    }
                    firstWhereExpression = false;
                    hqlWhere.append(" mmt.InitiationDate <= :pInitiationToDate");
                }
            }

            /*Lets go through the rest of the parameters    */
            if (pPaymentTemplateCd != null) {
                if (!firstWhereExpression) {
                    hqlWhere.append(" AND");
                }
                firstWhereExpression = false;
                hqlWhere.append(" acb.PaymentTemplate.PaymentTemplateCd = :pPaymentTemplateCd");
            }

            if (pBatchStatus != null) {
                if (!firstWhereExpression) {
                    hqlWhere.append(" AND");
                }
                firstWhereExpression = false;
                hqlWhere.append(" acb.CheckPrintBatchStatusCode = :printStatus");
            }

            if (printFromDate != null) {
                if (!firstWhereExpression) {
                    hqlWhere.append(" AND");
                }
                firstWhereExpression = false;
                hqlWhere.append(" acb.SentToPrinter >= :printFromDate");
            }

            if (printToDate != null) {
                if (!firstWhereExpression) {
                    hqlWhere.append(" AND");
                }
                firstWhereExpression = false;
                hqlWhere.append(" acb.SentToPrinter <= :printToDate");
            }

            if (hqlWhere.toString().equalsIgnoreCase(" WHERE")) {
                hqlWhere = new StringBuilder();
            }

            ScalarProperty orderBy = null;
            if (pOrderBy == null) {
                hqlOrderBy.append(" acb.PaymentTemplate.PaymentTemplateCd");
            } else if (pOrderBy.equalsIgnoreCase("paymentTemplateCd")) {
                hqlOrderBy.append(" acb.PaymentTemplate.PaymentTemplateCd");
            } else if (pOrderBy.equalsIgnoreCase("initiationDate")) {
                hqlOrderBy.append(" mmt.initiationDate");
            } else if (pOrderBy.equalsIgnoreCase("sentToPrinterDate")) {
                hqlOrderBy.append(" acb.sentToPrinter");
            } else if (pOrderBy.equalsIgnoreCase("paycheckCount")) {
                hqlOrderBy.append(" acb.NumberOfChecks");
            } else if (pOrderBy.equalsIgnoreCase("printStatus")) {
                hqlOrderBy.append(" acb.CheckPrintBatchStatusCode");
            }

            if (orderBy != null && pOrderDesc) {
                hqlOrderBy.append(" desc");
            }

            SAPSearchResults<SAPAgencyCheckBatch> sapSearchResults = new SAPSearchResults<SAPAgencyCheckBatch>();
            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hqlSelect + hqlFrom + hqlWhere.toString() + hqlOrderBy.toString());
            org.hibernate.Query hibernateCountQuery = Application.createHibernateQuery(hqlCountSelect + hqlFrom + hqlWhere.toString() + hqlOrderBy.toString());
            if (pPaymentTemplateCd != null) {
                hibernateQuery.setParameter("pPaymentTemplateCd", pPaymentTemplateCd);
                hibernateCountQuery.setParameter("pPaymentTemplateCd", pPaymentTemplateCd);
            }
            if (pBatchStatus != null) {
                hibernateQuery.setParameter("printStatus", CheckPrintBatchStatus.valueOf(pBatchStatus));
                hibernateCountQuery.setParameter("printStatus", CheckPrintBatchStatus.valueOf(pBatchStatus));
            }
            if (initiationFromDate != null) {
                hibernateQuery.setParameter("pInitiationFromDate", initiationFromDate);
                hibernateCountQuery.setParameter("pInitiationFromDate", initiationFromDate);
            }
            if (initiationToDate != null) {
                hibernateQuery.setParameter("pInitiationToDate", initiationToDate);
                hibernateCountQuery.setParameter("pInitiationToDate", initiationToDate);
            }
            if (printFromDate != null) {
                hibernateQuery.setParameter("printFromDate", printFromDate);
                hibernateCountQuery.setParameter("printFromDate", printFromDate);
            }
            if (printToDate != null) {
                hibernateQuery.setParameter("printToDate", printToDate);
                hibernateCountQuery.setParameter("printToDate", printToDate);
            }
            hibernateQuery.setFirstResult(pFirstResult);
            hibernateQuery.setMaxResults(pMaxResults);
            agencyCheckBatches = hibernateQuery.list();

            List countAgencyCheckBatches = hibernateCountQuery.list();
            sapSearchResults.setTotalRecords(Long.parseLong(countAgencyCheckBatches.get(0).toString()));

            ArrayList<SAPAgencyCheckBatch> sapCheckPrintingBatches = new ArrayList<SAPAgencyCheckBatch>();
            for (AgencyCheckBatch agencyCheckBatch : agencyCheckBatches) {
                sapCheckPrintingBatches.add(CompanyTranslator.getSAPAgencyCheckPrinting(agencyCheckBatch));
            }
            sapSearchResults.setReturnsList(sapCheckPrintingBatches);

            return sapSearchResults;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding check print batches", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.UpdateCheckPrintBatchStatus)
    public void savePrintBatchStatus(String pCheckPrintBatchId, String pNewBatchStatus) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<CheckPrintBatch> processResult =
                    PayrollServices.batchJobManager.updateCheckPrintBatchStatus(pCheckPrintBatchId, CheckPrintBatchStatus.valueOf(pNewBatchStatus));

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error saving batch " + pNewBatchStatus + " for batch " + pCheckPrintBatchId, processResult);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error saving check print batch status.", "CheckPrintBatch", pCheckPrintBatchId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @FlexMethod
    @Operation(operationIds = OperationId.AddUpdateCheckPrintSignature)
    public void uploadSignatureFile(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, byte[] signatureImage) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<CheckPrintSignature> processResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(
                    SourceSystemCode.valueOf(sourceSystemCd), companyId, signatureImage);

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error uploading signature.", processResult);
            }

            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error uploading signature file.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCheckPrintSignature)
    public byte[] getCompanySignatureImage(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            CheckPrintSignature checkPrintSignature = CheckPrintSignature.findCheckPrintSignature(company);

            if (checkPrintSignature != null) {
                return checkPrintSignature.getSignatureImage();
            }
        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding signature", sourceSystemCd, companyId, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AddUpdateCheckPrintSignature)
    public void addCheckPrintTestBatch(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult<CompanyPaycheckBatch> processResult = PayrollServices.companyManager.addCheckPrintTestBatch(
                    SourceSystemCode.valueOf(pSourceSystemCd), pCompanyId);

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error uploading signature", processResult);
            }

            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding test print batch.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @FlexMethod
    @Operation(operationIds = OperationId.ViewFullBankAccountNumbers)
    public SAPSearchResults<SAPBankAccountSearchResult> findBankAccounts(String pRoutingNumber,
                                                                         String pAccountNumber,
                                                                         String pOrderBy,
                                                                         boolean pOrderDesc,
                                                                         int pFirstResult,
                                                                         int pMaxResults) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            String countSelect = " Select count(ba.BANK_ACCOUNT_SEQ)";
            String employeeSelect = " SELECT " +
                    " ba.BANK_ACCOUNT_SEQ," +
                    " c.SOURCE_SYSTEM_CD AS SOURCE_SYSTEM_CD," +
                    " c.SOURCE_COMPANY_ID AS SOURCE_COMPANY_ID," +
                    " c.LEGAL_NAME AS LEGAL_NAME," +
                    " CASE" +
                    "  WHEN e.EMPLOYEE_SEQ IS NOT NULL" +
                    "   THEN 'Employee'" +
                    "  ELSE NULL" +
                    " END AS TYPE," +
                    " CASE" +
                    "  WHEN e.EMPLOYEE_SEQ IS NOT NULL" +
                    "   THEN i.LAST_NAME || ', ' || i.FIRST_NAME" +
                    "  ELSE NULL" +
                    " END AS OWNER_NAME," +
                    " eba.STATUS_CD AS STATUS_CD";
            String employeeFrom = " FROM PSP_EMPLOYEE_BANK_ACCOUNT eba" +
                    " LEFT JOIN PSP_BANK_ACCOUNT ba" +
                    " ON eba.BANK_ACCOUNT_FK = ba.BANK_ACCOUNT_SEQ" +
                    " LEFT JOIN PSP_EMPLOYEE e" +
                    " ON eba.EMPLOYEE_FK = e.EMPLOYEE_SEQ" +
                    " LEFT JOIN PSP_INDIVIDUAL i" +
                    " ON e.EMPLOYEE_SEQ = i.INDIVIDUAL_SEQ" +
                    " LEFT JOIN PSP_COMPANY c" +
                    " ON e.COMPANY_FK = c.COMPANY_SEQ ";

            String payeeSelect = " SELECT " +
                    " ba.BANK_ACCOUNT_SEQ," +
                    " c.SOURCE_SYSTEM_CD AS SOURCE_SYSTEM_CD," +
                    " c.SOURCE_COMPANY_ID AS SOURCE_COMPANY_ID," +
                    " c.LEGAL_NAME AS LEGAL_NAME," +
                    " CASE" +
                    "  WHEN p.PAYEE_SEQ IS NOT NULL" +
                    "   THEN 'Payee'" +
                    "  ELSE NULL" +
                    " END AS TYPE," +
                    " CASE" +
                    "  WHEN p.PAYEE_SEQ IS NOT NULL" +
                    "   THEN p.NAME" +
                    "  ELSE NULL" +
                    " END AS OWNER_NAME," +
                    " pba.STATUS_CD AS STATUS_CD";
            String payeeFrom = " FROM PSP_PAYEE_BANK_ACCOUNT pba" +
                    " LEFT JOIN PSP_BANK_ACCOUNT ba" +
                    " ON pba.BANK_ACCOUNT_FK = ba.BANK_ACCOUNT_SEQ" +
                    " LEFT JOIN PSP_PAYEE p" +
                    " ON pba.PAYEE_FK = p.PAYEE_SEQ" +
                    " LEFT JOIN PSP_COMPANY c" +
                    " ON p.COMPANY_FK = c.COMPANY_SEQ";

            String companySelect = " SELECT " +
                    " ba.BANK_ACCOUNT_SEQ," +
                    " c.SOURCE_SYSTEM_CD AS SOURCE_SYSTEM_CD," +
                    " c.SOURCE_COMPANY_ID AS SOURCE_COMPANY_ID," +
                    " c.LEGAL_NAME AS LEGAL_NAME," +
                    " CASE" +
                    "  WHEN cba.COMPANY_BANK_ACCOUNT_SEQ IS NOT NULL" +
                    "   THEN 'Company'" +
                    "  ELSE NULL" +
                    " END AS TYPE," +
                    " CASE" +
                    "  WHEN cba.COMPANY_BANK_ACCOUNT_SEQ IS NOT NULL" +
                    "   THEN ''" +
                    "  ELSE NULL" +
                    " END AS OWNER_NAME," +
                    " cba.STATUS_CD AS STATUS_CD";
            String companyFrom = " FROM PSP_COMPANY_BANK_ACCOUNT cba" +
                    " LEFT JOIN PSP_BANK_ACCOUNT ba" +
                    " ON cba.BANK_ACCOUNT_FK = ba.BANK_ACCOUNT_SEQ" +
                    " LEFT JOIN PSP_COMPANY c" +
                    " ON cba.COMPANY_FK = c.COMPANY_SEQ";

            String where = " where";

            if (pRoutingNumber != null && pRoutingNumber.trim().length() > 0) {
                where += " ba.Routing_Number = :routingNumber";
            }

            if (pAccountNumber != null && pAccountNumber.trim().length() > 0) {
                if (pRoutingNumber != null && pRoutingNumber.trim().length() > 0) {
                    where += " and";
                }
                where += " ba.Account_Number_Enc in (:accountNumberEncList)";
            }

            String orderBy = " ORDER BY";
            if (pOrderBy == null) {
                orderBy += " LEGAL_NAME, OWNER_NAME";
            } else if (pOrderBy.equals("companyLegalName")) {
                orderBy += " LEGAL_NAME";
            } else if (pOrderBy.equals("accountOwnerName")) {
                orderBy += " OWNER_NAME";
            } else if (pOrderBy.equals("accountType")) {
                orderBy += " TYPE";
            } else if (pOrderBy.equals("accountStatus")) {
                orderBy += " STATUS_CD";
            }

            if (pOrderDesc) {
                orderBy += " desc";
            }

            Session session = Application.getHibernateSession();
            String bankAccountsQuery = companySelect + companyFrom + where +
                    " UNION" +
                    employeeSelect + employeeFrom + where +
                    " UNION" +
                    payeeSelect + payeeFrom + where +
                    orderBy;
            SQLQuery bankAccountsSQLQuery = session.createSQLQuery(bankAccountsQuery);

            if (pFirstResult != -1) {
                bankAccountsSQLQuery.setFirstResult(pFirstResult);
            }
            if (pMaxResults != -1) {
                bankAccountsSQLQuery.setMaxResults(pMaxResults);
            }

            String countQuery = countSelect + companyFrom + where +
                    " UNION ALL" +
                    countSelect + employeeFrom + where +
                    " UNION ALL" +
                    countSelect + payeeFrom + where;
            SQLQuery countSQLQuery = session.createSQLQuery(countQuery);

            if (pRoutingNumber != null && pRoutingNumber.trim().length() > 0) {
                bankAccountsSQLQuery.setString("routingNumber", pRoutingNumber);
                countSQLQuery.setString("routingNumber", pRoutingNumber);
            }

            if (pAccountNumber != null && pAccountNumber.trim().length() > 0) {
                List<String> bankAccountEncList = EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName, pAccountNumber);
                bankAccountsSQLQuery.setParameterList("accountNumberEncList", bankAccountEncList);
                countSQLQuery.setParameterList("accountNumberEncList", bankAccountEncList);
            }

            List<Object[]> bankAccounts = bankAccountsSQLQuery.list();
            List<BigDecimal> count = countSQLQuery.list();

            ArrayList<SAPBankAccountSearchResult> sapBankAccountSearchResults = new ArrayList<SAPBankAccountSearchResult>(bankAccounts.size());
            for (Object[] objects : bankAccounts) {
                sapBankAccountSearchResults.add(
                        CompanyTranslator.getSAPBankAccountSearchResultFromDomainEntities(objects));
            }

            long totalCount = 0;
            for (BigDecimal decimal : count) {
                totalCount += decimal.longValue();
            }

            SAPSearchResults<SAPBankAccountSearchResult> sapSearchResults = new SAPSearchResults<SAPBankAccountSearchResult>();
            sapSearchResults.setTotalRecords(totalCount);
            sapSearchResults.setReturnsList(sapBankAccountSearchResults);

            return sapSearchResults;
        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding bank accounts", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        SAPSearchResults<SAPBankAccountSearchResult> sapSearchResults = new SAPSearchResults<SAPBankAccountSearchResult>();
        sapSearchResults.setTotalRecords(0);
        sapSearchResults.setReturnsList(new ArrayList<SAPBankAccountSearchResult>());
        return sapSearchResults;
    }


    @FlexMethod
    public ArrayList<SAPEntitlementSearchResult> findCurrentEINs(String licenseNumber, String eoc) throws Throwable {
        ArrayList<SAPEntitlementSearchResult> results = new ArrayList<SAPEntitlementSearchResult>();
        if (licenseNumber == null || eoc == null) {
            aeFactory.throwGenericException("LicenseNumber/EOC or orderNumber must be specified");
        }

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc);

            if (entitlement == null) {
                return results;
            }

            for (EntitlementUnit entitlementUnit : entitlement.getEntitlementUnitCollection()) {
                results.add(CompanyTranslator.getSAPEntitlementSearchResultFromDomainEntity(entitlementUnit));
            }

        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding entitlements", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        Collections.sort(results, (SAPEntitlementSearchResult a, SAPEntitlementSearchResult b) -> b.getFein().compareTo(a.getFein()));
        Collections.reverse(results);

        return results;
    }

    @FlexMethod
    public ArrayList<SAPEntitlementSearchResult> findEntitlementUnits(String ein) throws Throwable {
        ArrayList<SAPEntitlementSearchResult> results = new ArrayList<SAPEntitlementSearchResult>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            for (Company company : Company.searchCompaniesByEIN(ein)) {
                for (EntitlementUnit entitlementUnit : company.getActiveEntitlementUnits()) {
                    results.add(CompanyTranslator.getSAPEntitlementSearchResultFromDomainEntity(entitlementUnit));
                }
            }

        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding companies", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return results;

    }

    //This returns the same as the EU by EIN so the screen can be more easily reused
    @FlexMethod
    public ArrayList<SAPEntitlementSearchResult> findCompaniesByEIN(String ein) throws Throwable {
        ArrayList<SAPEntitlementSearchResult> results = new ArrayList<SAPEntitlementSearchResult>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            for (Company company : Company.searchCompaniesByEIN(ein)) {
                if (company.isCompanyOnService()) {
                    results.add(CompanyTranslator.getSAPEntitlementSearchResultFromCompany(company));
                }
            }

        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding companies", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return results;

    }

    @FlexMethod
    public SAPEntitlementInfo getLicenseFromOrderNumber(String orderNumber) throws Throwable {
        if (orderNumber == null) {
            aeFactory.throwGenericException("orderNumber must not be null");
        }

        SAPEntitlementInfo entitlementInfo = new SAPEntitlementInfo();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            //first check existing entitlements
            DomainEntitySet<Entitlement> entitlements =
                    Application.find(Entitlement.class,
                            Entitlement.OrderNumber().equalTo(orderNumber));

            if (entitlements.size() > 0) {
                Entitlement entitlement = entitlements.get(0);
                return CompanyTranslator.getSAPEntitlementInfoFromDomainEntity(entitlement);
            }

            //if not, check any pending AMO messages
            DomainEntitySet<EntitlementMessage> entitlementMessages =
                    Application.find(EntitlementMessage.class,
                            EntitlementMessage.OrderNumber().equalTo(orderNumber)
                                    .And(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.New)));
            if (entitlementMessages.size() > 0) {
                return CompanyTranslator.getSAPEntitlementInfoFromPendingMessage(entitlementMessages);
            }


            //if can't find, throw error
            aeFactory.throwGenericException("Order number not found");
        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding order", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }


        return entitlementInfo;
    }

    @FlexMethod
    public boolean hasActiveEINsForLicenseExceededMaxAllowed(String licenseNumber, String eoc) throws Throwable {

        boolean response = false;

        if (licenseNumber == null || eoc == null) {
            aeFactory.throwGenericException("LicenseNumber/EOC or orderNumber must be specified");
        }

        try {
            int mMaxNumberOfRecords = 1;
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc);

            if (entitlement == null) {
                return false;
            }
            // We are Limiting only for Diamond Customers
            if(!entitlement.getEntitlementCode().isDiamondAssisted()) {
                return false;
            }
            int numberOfActiveEUs = entitlement.getActiveEntitlementUnitCollection().size();
            String maxEUsAllowedForDiamondAssisted = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "diamond-assisted");

            if(maxEUsAllowedForDiamondAssisted.compareTo("") == 0 || !StringUtils.isNumeric(maxEUsAllowedForDiamondAssisted)) {
                aeFactory.throwGenericException("Failed to read config value license_number=" +licenseNumber);
            }
            mMaxNumberOfRecords=Integer.valueOf(maxEUsAllowedForDiamondAssisted);

            if (numberOfActiveEUs >= mMaxNumberOfRecords) {
                response = true;
            }
        } catch (Exception e) {
            aeFactory.throwGenericException("Error checking if the active license limit is crossed for license_number=" +licenseNumber);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return response;
    }

    @FlexMethod
    public SAPEntitlementInfo getEntitlementInfo(String licenseNumber, String eoc) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            //first check existing entitlements
            Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc);

            if (entitlement != null) {
                return CompanyTranslator.getSAPEntitlementInfoFromDomainEntity(entitlement);
            }

            //if not check any pending AMO messages
            DomainEntitySet<EntitlementMessage> entitlementMessages =
                    Application.find(EntitlementMessage.class,
                            EntitlementMessage.LicenseNumber().equalTo(licenseNumber)
                                    .And(EntitlementMessage.EntitlementOfferingCode().equalTo(eoc))
                                    .And(EntitlementMessage.Status().in(EntitlementMessageStatusCode.New, EntitlementMessageStatusCode.SkippedEntitlementNotFound))
                                    .And(EntitlementMessage.Message().isNotNull()));
            if (entitlementMessages.size() > 0) {
                return CompanyTranslator.getSAPEntitlementInfoFromPendingMessage(entitlementMessages);
            }


        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding entitlement info", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return null;
    }

    @FlexMethod
    public ArrayList<SAPEntitlementUnit> getEntitlementUnits(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {
        ArrayList<SAPEntitlementUnit> sapEntitlementUnits = new ArrayList<SAPEntitlementUnit>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                sapEntitlementUnits.add(CompanyTranslator.getSAPEntitlementUnitFromDomainEntity(entitlementUnit));
            }

        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding entitlement units", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapEntitlementUnits;
    }

    //Gets contacts from AMO and other companies on the entitlement
    @FlexMethod
    @Operation(operationIds = {
            OperationId.AddDIYEIN,
            OperationId.AddAssistedEIN
    })
    public ArrayList<SAPContact> getAdditionalContacts(String licenseNumber, String eoc) throws Throwable {
        ArrayList<SAPContact> additionalContacts = new ArrayList<SAPContact>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            //check ALL AMO messages for this entitlement
            DomainEntitySet<EntitlementMessage> entitlementMessages =
                    Application.find(EntitlementMessage.class,
                            EntitlementMessage.LicenseNumber().equalTo(licenseNumber)
                                    .And(EntitlementMessage.EntitlementOfferingCode().equalTo(eoc))
                                    .And(EntitlementMessage.Message().isNotNull()));
            if (entitlementMessages.size() > 0) {
                additionalContacts.addAll(CompanyTranslator.getSAPContactsFromMessages(entitlementMessages));
            }

            //check other companies on this entitlement

            //if a contact has already been copied, don't include in list more than once (shallow checking as does not need to be perfect)
            Set<String> addedContacts = new TreeSet<String>();

            Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc);
            if (entitlement != null) {
                DomainEntitySet<EntitlementUnit> entitlementUnits = entitlement.getEntitlementUnitCollection();

                for (EntitlementUnit entitlementUnit : entitlementUnits) {
                    for (Contact contact : entitlementUnit.getCompany().getContactCollection()) {
                        String key = contact.getFirstMiddleLastName() + contact.getPhone();
                        if (contact.getMailingAddress() != null) {
                            key += contact.getMailingAddress().getAddressLine1();
                        }
                        if (!addedContacts.contains(key)) {
                            addedContacts.add(key);
                            SAPContact sapContact = CompanyTranslator.getSAPContactFromDomainEntity(contact);
                            sapContact.setDescription(entitlementUnit.getCompany().getFedTaxId() + " " + contact.getContactRoleCd().name());
                            additionalContacts.add(sapContact);
                        }
                    }
                }
            }


        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding AMO (Siebel) contacts", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return additionalContacts;
    }

    //Gets contacts from AMO and other companies on the entitlement
    @FlexMethod
    @Operation(operationIds = {
            OperationId.AddDIYEIN,
            OperationId.AddAssistedEIN
    })
    public ArrayList<SAPAddress> getAdditionalAddresses(String licenseNumber, String eoc) throws Throwable {
        ArrayList<SAPAddress> additionalAddresses = new ArrayList<SAPAddress>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            //check ALL AMO messages for this entitlement
            DomainEntitySet<EntitlementMessage> entitlementMessages =
                    Application.find(EntitlementMessage.class,
                            EntitlementMessage.LicenseNumber().equalTo(licenseNumber)
                                    .And(EntitlementMessage.EntitlementOfferingCode().equalTo(eoc))
                                    .And(EntitlementMessage.Message().isNotNull()));
            if (entitlementMessages.size() > 0) {
                additionalAddresses.addAll(CompanyTranslator.getSAPAddressesFromMessages(entitlementMessages));
            }

            //check other companies on this entitlement

            Set<String> addedAddresses = new TreeSet<String>();

            Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc);
            if (entitlement != null) {
                DomainEntitySet<EntitlementUnit> entitlementUnits = entitlement.getEntitlementUnitCollection();

                for (EntitlementUnit entitlementUnit : entitlementUnits) {


                    addAddress(additionalAddresses, addedAddresses, entitlementUnit.getCompany().getLegalAddress(), entitlementUnit.getCompany().getFedTaxId() + " Legal Address");
                    addAddress(additionalAddresses, addedAddresses, entitlementUnit.getCompany().getMailingAddress(), entitlementUnit.getCompany().getFedTaxId() + " Mailing Address");


                    for (Contact contact : entitlementUnit.getCompany().getContactCollection()) {
                        addAddress(additionalAddresses, addedAddresses, contact.getMailingAddress(), entitlementUnit.getCompany().getFedTaxId() + " " + contact.getContactRoleCd().name() + " Address");
                    }
                }
            }

        } catch (Exception e) {
            aeFactory.throwGenericException("Error finding AMO (Siebel) contacts", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return additionalAddresses;
    }

    private void addAddress(List<SAPAddress> addresses, Set<String> addedAddresses, Address address, String description) {
        if (address != null) {
            String key = address.getAddressLine1() + address.getCity() + address.getZipCode();
            if (!addedAddresses.contains(key)) {
                SAPAddress sapAddress = CompanyTranslator.getSAPAddressFromDomainEntity(address);
                sapAddress.setDescription(description);
                addresses.add(sapAddress);
                addedAddresses.add(key);
            }
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.AddDIYEIN,
            OperationId.AddAssistedEIN
    })
    public void addCompany(SAPAddCompany addCompany) throws Throwable {
        EwsCreateAccount ewsCreateAccount = new EwsCreateAccount();

        //Similar check is in AddOrUpdateEntitlementUnitCore process, will prevent when updating. Adding new will prevent it here.
        Entitlement entitlement = Entitlement.findEntitlement(addCompany.getLicenseNumber(), addCompany.getEoc(), true);
        if (entitlement != null && EntitlementUnit.getActiveEntitlementUnitByFedTaxId(addCompany.getLegalInfo().getEin(), entitlement) != null) {
            aeFactory.throwGenericException(String.format("Entitlement with LicenseNumber: %s and EOC: %s already has Activated EntitlementUnit with FEIN: %s.", addCompany.getLicenseNumber(),
                    addCompany.getEoc(), addCompany.getLegalInfo().getEin()));
        }

        EwsCompany ewsCompany = new EwsCompany();

        for (SAPContact contact : addCompany.getContacts()) {
            if (contact.getContactRoleCd() == ContactRole.PrimaryPrincipal) {
                ewsCompany.setPrimaryPrincipal(CompanyTranslator.getEWSContactFromSAPContact(contact));
            } else if (contact.getContactRoleCd() == (ContactRole.PayrollAdmin)) {
                ewsCompany.setPayrollAdmin(CompanyTranslator.getEWSContactFromSAPContact(contact));
            }
        }

        ewsCompany.setEin(addCompany.getLegalInfo().getEin());
        ewsCompany.setDba(StringUtils.isEmpty(addCompany.getLegalInfo().getDoingBusinessAs()) ? addCompany.getLegalInfo().getLegalName() : addCompany.getLegalInfo().getDoingBusinessAs());

        EwsLegalInfo ewsLegalInfo = CompanyTranslator.getEWSAddressFromSAPAddress(EwsLegalInfo.class, addCompany.getLegalInfo().getAddress());
        ewsLegalInfo.setLegalName(addCompany.getLegalInfo().getLegalName());
        ewsCompany.setLegalInfo(ewsLegalInfo);
        ewsCompany.setMailingAddress(ewsLegalInfo);
        ewsCreateAccount.setEwsCompany(ewsCompany);

        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        EwsEntitlement ewsEntitlement = new EwsEntitlement();
        ewsEntitlement.setAssetItemNumber(addCompany.getItemNumber());
        ewsEntitlement.setLicenseNumber(addCompany.getLicenseNumber());
        ewsEntitlement.setEntitlementOfferingCode(addCompany.getEoc());
        ewsEntitlement.setBillingAccountId(addCompany.getServiceAccountId());
        ewsEntitlement.setAddEin(false); //required but not used
        ewsEntitlements.add(ewsEntitlement);

        ewsCreateAccount.setEwsEntitlements(ewsEntitlements);

        EwsServices ewsServices = new EwsServices();
        EwsBaseService cloudService = new EwsBaseService();
        ewsServices.setCloudService(cloudService);
        ewsCreateAccount.setEwsServices(ewsServices);

        CreateAccountProcess createAccountProcess = new CreateAccountProcess(ewsCreateAccount, false);
        EwsCreateAccountResponse response = createAccountProcess.execute();

        if (response.getEwsResponseStatus().getCode() != 0) {
            aeFactory.throwGenericException("Error creating company: " + response.getEwsResponseStatus().getMessage());
        }

        try {
            PayrollServices.beginUnitOfWork();

            String psid = response.getPsid();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

            if (StringUtils.isNotEmpty(addCompany.getPriceType())) {
                companyDTO.setPriceType(addCompany.getPriceType());
            }

            ArrayList<ContactDTO> contactDTOs = new ArrayList<ContactDTO>();
            //pull existing domain contacts in so that we have their source ids on PA and PP
            Contact domainPayrollAdmin = company.getContactByRoleCode(ContactRole.PayrollAdmin);
            ContactDTO dtoPayrollAdmin = PayrollServices.dtoFactory.create(domainPayrollAdmin);
            contactDTOs.add(dtoPayrollAdmin);
            Contact domainPrimaryPrincipal = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
            ContactDTO dtoPrimaryPrincipal = PayrollServices.dtoFactory.create(domainPrimaryPrincipal);
            contactDTOs.add(dtoPrimaryPrincipal);

            //Add SP and Other
            for (SAPContact contact : addCompany.getContacts()) {
                if (!(contact.getContactRoleCd().equals(ContactRole.PayrollAdmin) || contact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal))) {
                    contactDTOs.add(CompanyTranslator.getContactDTOFromSAPContact(contact));
                }
            }

            companyDTO.setContacts(contactDTOs);

            ProcessResult pr = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),
                    company.getSourceCompanyId(),
                    companyDTO);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error updating new company contacts", pr);
            }

            if (addCompany.getIsSuccessorEntityChange()) {
                EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
                entityChangeDTO.setIsSuccessor(true);
                entityChangeDTO.setIsError(false);
                entityChangeDTO.setHasNewDataFile(true);
                entityChangeDTO.setOldEIN(addCompany.getOldEIN());
                entityChangeDTO.setNewEIN(addCompany.getLegalInfo().getEin());
                entityChangeDTO.setEffectiveDate(new DateDTO(addCompany.getEinEffectiveDate()));
                PspPrincipal currentPrincipal = Application.getCurrentPrincipal();
                String currentId = null;
                if (currentPrincipal != null) {
                    currentId = currentPrincipal.getId();
                    entityChangeDTO.setUserId(currentId);
                }
                ProcessResult pr2 = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        entityChangeDTO);
                if (!pr2.isSuccess()) {
                    aeFactory.throwGenericException("Error creating Entity Change", pr);
                }
            }

            //add the offer (bad) Changed implementation to include validation also on this offer not returning error, but failing silently
            if (StringUtils.isNotEmpty(addCompany.getOfferCode())) {
                //Offer offer = Offer.findOfferByOfferCode(addCompany.getOfferCode());
                //if (offer != null) {
                //  company.claimOfferForCompany(offer);
                ProcessResult pr3 = PayrollServices.companyManager.claimOfferForCompany(addCompany.getOfferCode(), null, company);
                if (!pr3.isSuccess()) {
                    for (Message message : pr3.getMessages()) {
                        logger.info("Company=" + company.getSourceSystemCompanyId() + " MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                    }
                }
            }


            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            aeFactory.throwGenericException("Error creating company", e);
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            PayrollServices.rollbackUnitOfWork();
        }


    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.AddToEINDIY,
            OperationId.AddToEINAssisted,
            OperationId.MoveEINDIYAssisted,
            OperationId.MoveEINDIYDIY
    })
    public void addEntitlementUnitToCompany(String sourceSystemCd, String sourceCompanyId, String licenseNumber, String eoc, String itemNumber) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            //Similar check is in AddOrUpdateEntitlementUnitCore process, will prevent when updating. Adding new will prevent it here.
            Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc, true);
            if (entitlement != null && EntitlementUnit.getActiveEntitlementUnitByFedTaxId(company.getFedTaxId(), entitlement) != null) {
                aeFactory.throwGenericException(String.format("Entitlement with LicenseNumber: %s and EOC: %s already has Activated EntitlementUnit with FEIN: %s.", licenseNumber,
                        eoc, company.getFedTaxId()));
            }

            EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();
            if (entitlement != null) {
                entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlement, entitlementUnitDTO);
            } else {
                entitlementUnitDTO.setLicenseNumber(licenseNumber);
                entitlementUnitDTO.setEntitlementOfferingCode(eoc);
            }
            entitlementUnitDTO.setAssetItemNumber(itemNumber);
            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
            entitlementUnitDTO.setFedTaxId(company.getFedTaxId());


            ProcessResult result = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(SourceSystemCode.valueOf(sourceSystemCd), sourceCompanyId, entitlementUnitDTO);
            if (!result.isSuccess()) {
                aeFactory.throwGenericException("Error adding entitlement unit to company", result);
            }
            PayrollServices.commitUnitOfWork();

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public String getPriceType(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company c = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (c == null) {
                throw aeFactory.companyNotFoundException();
            }
            return c.getPriceType();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting price type", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public List<String> getIndustryTypes() throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            return IndustryType.getAllIndustryTypes();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting industry types", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<String> getAvailablePriceTypes(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId) throws Throwable {
        try {
            ArrayList<String> priceTypes = new ArrayList<String>();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();

            if (entitlementUnit == null) {
                entitlementUnit = company.getPrimaryEntitlementUnits().sort(EntitlementUnit.<EntitlementUnit>CreatedDate().Descending()).getFirst();
            }

            if (entitlementUnit != null) {
                String assetItemNumber = entitlementUnit.getEntitlement().getEntitlementCode().getAssetItemNumber();
                DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = EntitlementCodeOffering.findEntitlementCodeOfferingsGroupByPriceType(assetItemNumber);

                for (EntitlementCodeOffering entitlementCodeOffering : entitlementCodeOfferings) {
                    String priceType = entitlementCodeOffering.getPriceType();
                    if (StringUtils.isEmpty(priceType)) {
                        priceType = "";
                    }
                    if (!priceTypes.contains(priceType)) {
                        priceTypes.add(priceType);
                    }
                }
            }
            return priceTypes;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting price types", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<String> getAvailablePriceTypes(String assetItemNumber) throws Throwable {
        try {
            ArrayList<String> priceTypes = new ArrayList<String>();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = EntitlementCodeOffering.findEntitlementCodeOfferingsGroupByPriceType(assetItemNumber);

            for (EntitlementCodeOffering entitlementCodeOffering : entitlementCodeOfferings) {
                String priceType = entitlementCodeOffering.getPriceType();
                if (StringUtils.isEmpty(priceType)) {
                    priceType = "";
                }
                if (!priceTypes.contains(priceType)) {
                    priceTypes.add(priceType);
                }
            }
            return priceTypes;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting price types", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AddUpdatePriceType)
    public void setAssistedPriceTypeAndOffer(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID)  String sourceCompanyId, String priceType, String offerCode) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            logger.info("Add or Update Price Types:" + priceType + ", offercode:" + offerCode + ", psid:" + sourceCompanyId);
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));

            CompanyDTO dto = PayrollServices.dtoFactory.create(company);
            dto.setPriceType(priceType);

            ProcessResult updatePR = PayrollServices.companyManager.updateCompany(SourceSystemCode.valueOf(sourceSystemCd), sourceCompanyId, dto);

            if (!updatePR.isSuccess()) {
                aeFactory.throwGenericException("Error setting price type", updatePR);
            }

            //bad
            for (CompanyOffer companyOffer : company.getCompanyOffers()) {
                Offer offer = companyOffer.getOffer();
                company.cancelOfferForCompany(offer);
            }

            if (!StringUtils.isEmpty(offerCode)) {
                ProcessResult offerPR = PayrollServices.companyManager.claimOfferForCompany(offerCode, null, company);

                if (!offerPR.isSuccess()) {
                    aeFactory.throwGenericException("Error setting offer", offerPR);
                }
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error setting price type and offer", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public SAPOffer getAssistedOffer(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID)  String sourceCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }
            CompanyOffer offer = company.getCompanyOffers().getFirst();
            if (offer == null) {
                return null;
            }
            return BillingTranslator.getCompanyOfferFromDomainEntity(offer);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting company offer", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.DeactivateEIN,
            OperationId.DeactivateEINActive,
            OperationId.DeactivateEINPendingActivation
    })
    public void deactivateEntitlementUnit(String id) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            EntitlementUnit eu = Application.findById(EntitlementUnit.class, SpcfUniqueId.createInstance(id));

            EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(eu);
            dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);

            ProcessResult pr = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(eu.getCompany().getSourceSystemCd(), eu.getCompany().getSourceCompanyId(), dto);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error deactivating entitlement unit", pr);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error deactivating entitlement unit", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.ReactivateEINAssisted,
            OperationId.ReactivateEINDIY
    })
    public void reactivateEntitlementUnit(String id) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            EntitlementUnit eu = Application.findById(EntitlementUnit.class, SpcfUniqueId.createInstance(id));

            if (eu.isHistoric()) {
                aeFactory.throwGenericException("Historic entitlement units cannot be updated");
            }
            EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(eu);
            dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingReactivation);

            ProcessResult pr = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(eu.getCompany().getSourceSystemCd(), eu.getCompany().getSourceCompanyId(), dto);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error reactivating entitlement unit", pr);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error reactivating entitlement unit", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.AddToEINDIY,
            OperationId.AddToEINAssisted,
            OperationId.MoveEINDIYAssisted,
            OperationId.MoveEINDIYDIY
    })
    public void moveEntitlementUnit(String fromEntitlementId, String toLicenseNumber, String toEoc, String toItemNumber) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            //deactivate old

            EntitlementUnit eu = Application.findById(EntitlementUnit.class, SpcfUniqueId.createInstance(fromEntitlementId));

            if (eu.isHistoric()) {
                aeFactory.throwGenericException("Historic entitlement units cannot be updted");
            }

            EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(eu);

            Entitlement toEntitlement = Entitlement.findEntitlement(toLicenseNumber, toEoc);
            if (toEntitlement != null && EntitlementUnit.getActiveEntitlementUnitByFedTaxId(dto.getFedTaxId(), toEntitlement) != null) {
                aeFactory.throwGenericException(String.format("Entitlement with LicenseNumber: %s and EOC: %s already has Activated EntitlementUnit with FEIN: %s.", toLicenseNumber,
                        toEoc, dto.getFedTaxId()));
            }

            dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);

            ProcessResult pr = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(eu.getCompany().getSourceSystemCd(), eu.getCompany().getSourceCompanyId(), dto);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error moving: deactivating old entitlement unit", pr);
            }

            //activate new

            Company company = eu.getCompany();
            Entitlement entitlement = Entitlement.findEntitlement(toLicenseNumber, toEoc);
            EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();

            if (entitlement != null) {
                entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlement, entitlementUnitDTO);
            } else {
                entitlementUnitDTO.setLicenseNumber(toLicenseNumber);
                entitlementUnitDTO.setEntitlementOfferingCode(toEoc);
            }

            if (toItemNumber == null || toItemNumber.equals("")) {
                if (entitlement == null) {
                    throw new Exception("Item number not specified and cannot find new entitlement");
                }
                toItemNumber = entitlement.getEntitlementCode().getAssetItemNumber();
            }
            entitlementUnitDTO.setAssetItemNumber(toItemNumber);
            entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
            entitlementUnitDTO.setFedTaxId(company.getFedTaxId());

            ProcessResult result = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
            if (!result.isSuccess()) {
                aeFactory.throwGenericException("Error moving: adding new entitlement unit to company", result);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error moving entitlement unit", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    //the first time the user goes to the IRL, there won't be any EUs so the
    //Entitlement won't be created so I will not know what kind of asset it is without
    //parsing the AMO messages.  But I have the item number from Siebel so I can just use that.
    @FlexMethod
    public SAPAssetInfo getAssetInfo(String itemNumber) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            //null, null will be fine since all the information we're looking at here will be the same
            //and everything should have a dummy
            EntitlementCode code = EntitlementCode.findEntitlementCode(itemNumber, null, null);

            if (code == null) {
                String knownInvalidItemCodes = SystemParameter.findStringValue(SystemParameter.Code.SIEBEL_ITEMS_NOT_IN_PSP, "");
                if (knownInvalidItemCodes.contains(itemNumber)) {
                    aeFactory.throwGenericException(String.format("Item number '%s' does not exist. Please make sure you have the correct row selected in Siebel before you click the launch product ui button.", itemNumber));
                } else {
                    throw new Exception("Item code " + itemNumber + " does not exist");
                }
            }

            return CompanyTranslator.getSAPAssetInfoFromDomainEntity(code);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding asset info", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;

    }

    @FlexMethod
    public ArrayList<SAPEntityChangeDTO> getEntityChangeHistory(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID)  String pCompanyId) throws Throwable {
        ArrayList<SAPEntityChangeDTO> entityChangeDTOs = new ArrayList<SAPEntityChangeDTO>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            Expression<EntityChange> query = new Query<EntityChange>().Where(EntityChange.Company().equalTo(company))
                    .OrderBy(EntityChange.ModifiedDate().Descending());
            DomainEntitySet<EntityChange> entityChanges = Application.find(EntityChange.class, query);
            for (EntityChange entityChange : entityChanges) {
                SAPEntityChangeDTO entityChangeDTO = new SAPEntityChangeDTO();
                entityChangeDTO.setCompanyKey(new SAPCompanyKey(pSourceSystemCode, pCompanyId));
                entityChangeDTO.setEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(entityChange.getEffectiveDate()));
                entityChangeDTO.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(entityChange.getModifiedDate()));
                entityChangeDTO.setAgentId(SAPTranslator.getUserNameFromUserID(entityChange.getAgentId()));
                entityChangeDTO.setNewEIN(entityChange.getNewEIN());
                entityChangeDTO.setOldEIN(entityChange.getOldEIN());
                entityChangeDTO.setIsSuccessor(entityChange.getIsSuccessor());
                entityChangeDTO.setHasNewDataFile(entityChange.getHasNewDataFile());
                entityChangeDTO.setIsError(entityChange.getIsError());
                entityChangeDTOs.add(entityChangeDTO);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting entity change history", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return entityChangeDTOs;
    }

    @FlexMethod
    public SAPEntityChangeDTO getEntityChange(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID)  String pCompanyId, String pOldEin, String pNewEin) throws Throwable {
        SAPEntityChangeDTO entityChangeDTO = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            EntityChange entityChange = EntityChange.findEntityChange(company, pOldEin, pNewEin);
            entityChangeDTO = new SAPEntityChangeDTO();
            entityChangeDTO.setCompanyKey(new SAPCompanyKey(pSourceSystemCode, pCompanyId));
            entityChangeDTO.setEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(entityChange.getEffectiveDate()));
            entityChangeDTO.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(entityChange.getModifiedDate()));
            entityChangeDTO.setAgentId(SAPTranslator.getUserNameFromUserID(entityChange.getAgentId()));
            entityChangeDTO.setNewEIN(entityChange.getNewEIN());
            entityChangeDTO.setOldEIN(entityChange.getOldEIN());
            entityChangeDTO.setIsSuccessor(entityChange.getIsSuccessor());
            entityChangeDTO.setIsError(entityChange.getIsError());
            entityChangeDTO.setHasNewDataFile(entityChange.getHasNewDataFile());
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting entity change history", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return entityChangeDTO;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RecalculateLedgerBalances)
    public void reCalculateLedgerBalances(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID)  String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            ProcessResult result = PayrollServices.companyManager.recalculateCompanyLedgerBalances(SourceSystemCode.valueOf(pSourceSystemCode), pCompanyId);

            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException(String.format("Error in recalculating ledger balances for Company (SystemCode: %s, CompanyId: %s)", pSourceSystemCode, pCompanyId), result);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException(String.format("Error in recalculating ledger balances for Company (SystemCode: %s, CompanyId: %s)", pSourceSystemCode, pCompanyId), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public SAPQuickbooksInfo getQuickbooksInfo(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }
            return CompanyTranslator.getQuickbooksInfoFromDomainEntity(company.getQuickbooksInfo());
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting Quickbooks info", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public List<SAPQuickBooksFileId> getAvailableQuickBooksFileIds(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID)  String sourceCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.MANUAL, true);

            int monthsOfTransmissions = SystemParameter.findIntValue(SystemParameter.Code.QB_FILE_ID_SEARCH_MONTHS_PRIOR, 1);

            SpcfCalendar sixMonthsAgo = PSPDate.getPSPTime();
            sixMonthsAgo.addMonths(monthsOfTransmissions);

            List<SAPQuickBooksFileId> sapFileIds = new ArrayList<SAPQuickBooksFileId>();
            Set<String> fileIds = new HashSet<String>();
            DomainEntitySet<SourceSystemTransmission> companyTransmissions = SourceSystemTransmission.findCompanyTransmissions(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd), sixMonthsAgo, null, SourceSystemCode.QBDT);
            for (SourceSystemTransmission companyTransmission : companyTransmissions) {
                DomainEntityChangeManager.setDomainEntityChangeModelContext(companyTransmission.getClass(), companyTransmission);
                String ofxStr = companyTransmission.getRequestDocument();
                if (StringUtils.isNotEmpty(ofxStr)) {
                    //noinspection EmptyCatchBlock
                    try {
                        OFX requestOfx = OFXManager.ofxRequestToJava(ofxStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
                        if (requestOfx.getSIGNONMSGSRQV1() != null
                                && requestOfx.getSIGNONMSGSRQV1().getSONRQ() != null) {
                            String fileId = requestOfx.getSIGNONMSGSRQV1().getSONRQ().getIQBFILEID();
                            if (StringUtils.isNotEmpty(fileId) && !fileIds.contains(fileId)) {
                                fileIds.add(fileId);
                                sapFileIds.add(new SAPQuickBooksFileId(fileId, SAPTranslator.getDateFromSpcfCalendar(companyTransmission.getInitializeDateTime())));
                            }
                        }
                    } catch (MalformedOFXException e) {
                        //ignore any malformed requests
                    } catch (OFXToJavaMappingError e) {
                        //ignore any malformed requests
                    }
                }
                DomainEntityChangeManager.removeDomainEntityChangeModel();
            }
            return sapFileIds;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting QuickBooks file IDs", t);
            return null;
        } finally {
            DomainEntityChangeManager.removeDomainEntityChangeModel();
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.EditQBFileID})
    public void deleteVMPData(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyId,SourceSystemCode.valueOf(sourceSystemCd));
            ProcessResult processResult = PayrollServices.paystubManager.deletePaystub(company);

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error Deleting VMP Data", processResult);
            }

            PayrollServices.commitUnitOfWork();


        } catch (Throwable t) {
            logger.warn("Source system := " + sourceSystemCd + "Source company psid := " + sourceCompanyId);
            aeFactory.throwGenericException("Error Deleting VMP Data", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @FlexMethod
    @Operation(operationIds = {OperationId.EditChartOfAccounts, OperationId.EditQBFileID})
    public void updateQuickbooksInfo(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId, String feeCoa, String saleTaxCoa, String fileId, String caseId) throws Throwable {
        try {
            ThreadLocalManager.setValue(caseId);
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));

            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.getQuickBooksInfo().setCoaFeeAccountName(feeCoa);
            companyDTO.getQuickBooksInfo().setCoaSalesTaxAccountName(saleTaxCoa);
            if (StringUtils.isEmpty(fileId)) {
                fileId = null;
            }
            companyDTO.getQuickBooksInfo().setFileId(fileId);

            ProcessResult pr = PayrollServices.companyManager.updateCompany(SourceSystemCode.valueOf(sourceSystemCd), sourceCompanyId, companyDTO);

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error updating chart of accounts", pr);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating chart of accounts", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadLocalManager.flush();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RemoveTaxTableSuspension)
    public void updateSubscriptionEndDate(String pEntitlementUnitId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            EntitlementUnit entitlementUnit = Application.findById(EntitlementUnit.class, SpcfUniqueId.createInstance(pEntitlementUnitId));
            if (entitlementUnit == null) {
                aeFactory.throwGenericException("Unable to find EntitlementUnit specified.");
            }

            Entitlement entitlement = entitlementUnit.getEntitlement();
            if (entitlement == null) {
                aeFactory.throwGenericException("Unable to find Entitlement specified.");
            }

            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
            if (entitlement.getSubscriptionEndDate() == null) {
                SpcfCalendar subscriptionEndDate = PSPDate.getPSPTime();
                subscriptionEndDate.addDays(-30);

                entitlementUnitDTO.setSubscriptionEndDate(subscriptionEndDate);
            } else {
                entitlementUnitDTO.setSubscriptionEndDate(null);
            }

            ProcessResult processResult = PayrollServices.entitlementManager.updateEntitlement(entitlementUnitDTO);
            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error updating entitlement", processResult);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error clearing subscription end date.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public void syncEntitlementUnitFromSourceSystems(String pEntitlementUnitId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            List<String> syncOptions = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.ERS_DATA_SYNC_OPTIONS, "EntitlementState,EntitlementUnitStatus").split(","));
            if (syncOptions.isEmpty()) {
                aeFactory.throwGenericException("Sync is currently disabled.");
            }

            EntitlementUnit entitlementUnit = Application.findById(EntitlementUnit.class, SpcfUniqueId.createInstance(pEntitlementUnitId));
            if (entitlementUnit == null) {
                aeFactory.throwGenericException("Unable to find EntitlementUnit specified.");
            }

            //noinspection ConstantConditions
            if (entitlementUnit.isHistoric()) {
                aeFactory.throwGenericException("Historic entitlement units cannot be updated");
            }

            //check before syncing for pending
            if (entitlementUnit.getEntitlement().hasPendingOrRecentMessages()) {
                aeFactory.throwGenericException("This entitlement has pending or recent AMO messages.  Please wait for these to process instead of manually syncing.");
            }

            EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);

            boolean ersUpdated = syncEntitlementDataFromERS(entitlementUnit.getCompany(), syncOptions, entitlementUnitDTO);
            boolean amoUpdated = syncEntitlementDataFromAMO(entitlementUnit.getCompany(), entitlementUnitDTO);

            //also check now after the long wait from the syncs.
            if (entitlementUnit.getEntitlement().hasPendingOrRecentMessages()) {
                aeFactory.throwGenericException("This entitlement has pending or recent AMO messages.  Please wait for these to process instead of manually syncing.");
            }

            if (ersUpdated || amoUpdated) {
                ProcessResult processResult = PayrollServices.entitlementManager.syncEntitlementUnit(entitlementUnit.getId(), entitlementUnitDTO);
                if (!processResult.isSuccess()) {
                    aeFactory.throwGenericException("Error updating entitlement", processResult);
                }
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error syncing entitlement data with source system.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private boolean syncEntitlementDataFromERS(Company pCompany, List<String> pSyncOptions, EntitlementUnitDTO pEntitlementUnitDTO) {
        boolean response = false;

        try {
            String license = pEntitlementUnitDTO.getLicenseNumber();
            String eoc = pEntitlementUnitDTO.getEntitlementOfferingCode();

            ERSListener ersListener = new ERSListener(pCompany, TransmissionType.QueryEntitlement);
            IERSGateway ersGateway = ERSGatewayFactory.createInstance();
            if (ersGateway == null) {
                return response;
            }

            EntitlementInfoDTO entitlementInfoDTO = ersGateway.getEntitlementInfo(license, eoc, true, ersListener);
            if (entitlementInfoDTO == null) {
                return response;
            }

            response = true;

            entitlementInfoDTO.copyErsDtoToPspDto(pSyncOptions, pEntitlementUnitDTO);
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }

        return response;
    }

    private boolean syncEntitlementDataFromAMO(Company pCompany, EntitlementUnitDTO pEntitlementUnitDTO) {
        boolean response = false;

        try {
            String license = pEntitlementUnitDTO.getLicenseNumber();
            String eoc = pEntitlementUnitDTO.getEntitlementOfferingCode();

            AMOListener amoListener = new AMOListener(pCompany, TransmissionType.QueryCustomerAsset);
            IAMOWSGateway amowsGateway = AMOWSGatewayFactory.createInstance();
            if (amowsGateway == null) {
                return response;
            }

            GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = amowsGateway.getCustomerAsset(license, eoc, amoListener);
            if (getCustomerAssetResponseTypeDTO == null) {
                return response;
            }

            response = true;

            getCustomerAssetResponseTypeDTO.copyAmoDtoToPspDto(pEntitlementUnitDTO);
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }

        return response;
    }

    @FlexMethod
    public String getWorkersCompServiceInfo(String sourceSystemCd,String sourceCompanyId) throws Throwable {
        try {
            WorkersCompGateway gateway = new WorkersCompGatewayImpl();
            String xml = gateway.getDisplayDataForHelpDesk(sourceSystemCd, sourceCompanyId);
            return xml;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting workers comp info from WorkersComp Service.", t);
            return null;
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditCompanyContactInformation)
    public void removeInvalidFlagOnEmailAddresses(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pEmailAddress) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.companyManager.removeInvalidFlagOnCompanyContactsAndPayees(SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, pEmailAddress);
            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error Removing invalid flag on Email addresses", processResult);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error Removing invalid flag on Email addresses.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPPriceTypeOffer> getOfferByPriceType(String itemNumber) throws Throwable {
        try {

            EntitlementCode ec = EntitlementCode.findEntitlementCode(itemNumber);
            SAPPriceTypeOffer sapPriceTypeOfferBean = null;
            ArrayList<SAPPriceTypeOffer> sapPriceTypeOfferList = new ArrayList();
            //Adding blank price type and offer
            SAPPriceTypeOffer sPrcOffer = new SAPPriceTypeOffer();
            sPrcOffer.setPriceType("");
            ArrayList<SAPOffer> sOfferList = new ArrayList<SAPOffer>();
            sOfferList.add(new SAPOffer("", ""));
            sPrcOffer.setOfferList(sOfferList);
            sapPriceTypeOfferList.add(sPrcOffer);

            String[] offeringCodes;
            Offer offer;
            String priceTypeVal = null;
            ArrayList<SAPOffer> sapOfferList = null;
            Set<SAPOffer> offerSet = null;
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = EntitlementCodeOffering.findEntitlementCodeOfferingsGroupByPriceType(itemNumber);
            for (EntitlementCodeOffering entitlementCodeOffering : entitlementCodeOfferings) {
                if (null != entitlementCodeOffering.getPriceType()) {

                    //this block of code is to maintain one to many mapping between pricetype/offering code to offer
                    if (!entitlementCodeOffering.getPriceType().equals(priceTypeVal)) {

                        sapPriceTypeOfferList = setPriceTypePfferList(sapPriceTypeOfferBean, sapOfferList, offerSet, sapPriceTypeOfferList);
                        sapPriceTypeOfferBean = new SAPPriceTypeOffer();
                        sapPriceTypeOfferBean.setPriceType(entitlementCodeOffering.getPriceType());
                        priceTypeVal = entitlementCodeOffering.getPriceType();
                        offerSet = new HashSet<SAPOffer>();
                    }
                    Offering offering = entitlementCodeOffering.getOffering();
                    logger.info("offering:" + offering.getOfferingCode().name() + "entitlementCodeOffering.getPriceType():" + entitlementCodeOffering.getPriceType());

                    String strSpecialOffer = SystemParameter.findStringValue(SystemParameter.Code.ENTITLED_OFFER_CODE);
                    offeringCodes = strSpecialOffer.split(",");
                    for (String mofferingCodes : offeringCodes) {
                        String tempOfferCode = mofferingCodes.split(":")[0];
                        logger.info("strSpecialOffer:" + strSpecialOffer + "offeringCodes:" + mofferingCodes);
                        if (offering.getOfferingCode().name().trim().equalsIgnoreCase(tempOfferCode.trim()) || priceTypeVal.trim().equalsIgnoreCase(tempOfferCode.trim())) {
                            //Special offers not applicable on Diamond Assisted
                            if ("null".equalsIgnoreCase(mofferingCodes.split(":")[1]) || (null != ec && ec.isDiamondAssisted())) {
                                offerSet.add(new SAPOffer("", ""));
                            } else {
                                offer = validateAndGetOffer(mofferingCodes.split(":")[1]);
                                if (null != offer) {
                                    offerSet.add(new SAPOffer(offer.getOfferCd(), offer.getDescription()));
                                    logger.info("offerSet:" + offerSet);
                                }
                            }
                        }
                        
                    }
                }
            }

            sapPriceTypeOfferList = setPriceTypePfferList(sapPriceTypeOfferBean, sapOfferList, offerSet, sapPriceTypeOfferList);

            logger.info("sapPriceTypeOfferList:" + sapPriceTypeOfferList);

            return sapPriceTypeOfferList;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting special offer", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    ArrayList<SAPPriceTypeOffer> setPriceTypePfferList(SAPPriceTypeOffer sapPriceTypeOfferBean, ArrayList<SAPOffer> sapOfferList, Set<SAPOffer> offerSet, ArrayList<SAPPriceTypeOffer> sapPriceTypeOfferList) {
        if (sapPriceTypeOfferBean != null) {
            sapOfferList = new ArrayList<SAPOffer>();
            sapOfferList.addAll(offerSet);
            //if special offer is null then show default offer.
            if (sapOfferList.size() == 0) {
                sapOfferList = getDefaultOfferList();
                logger.info("sap Default OfferList:" + sapOfferList);
            }
            sapPriceTypeOfferBean.setOfferList(sapOfferList);
            sapPriceTypeOfferList.add(sapPriceTypeOfferBean);
        }
        return sapPriceTypeOfferList;
    }


    @FlexMethod
    public ArrayList<SAPOffer> getOfferByItemNumber(String itemNumber) throws Throwable {
        try {
            ArrayList<SAPOffer> retList = getDefaultOfferList();

            if (!doesSpecialOfferCodeExist()) {
                return retList;
            }

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = EntitlementCodeOffering.findEntitlementCodeOfferingsGroupByPriceType(itemNumber);
            for (EntitlementCodeOffering entitlementCodeOffering : entitlementCodeOfferings) {
                Offering offering = entitlementCodeOffering.getOffering();
                if (offering != null && offering.getServiceCode().equals(ServiceCode.DirectDeposit) && offering.getOfferingCode().equals(OfferingCode.SYMFY14)) {
                    Offer specialOffer = validateAndGetOffer(SystemParameter.findStringValue(SystemParameter.Code.SPECIAL_OFFER_CODE));
                    if (specialOffer == null) {
                        return retList;
                    } else {
                        return getSpecialOfferList(specialOffer);
                    }
                }
            }
            return retList;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting special offer", t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPOffer> getOfferByCompanyKey(SAPCompanyKey companyKey) throws Throwable {
        try {
            ArrayList<SAPOffer> retList = getDefaultOfferList();

            if (!doesSpecialOfferCodeExist()) {
                return retList;
            }

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompanyNoEagerLoad(companyKey.getCompanyId(), SourceSystemCode.valueOf(companyKey.getSourceSystemCd()));
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
            CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);
            if (companyOffering == null || !companyOffering.getOffering().getOfferingCode().equals(OfferingCode.SYMFY14)) {
                return retList;
            }
            Offer specialOffer = validateAndGetOffer(SystemParameter.findStringValue(SystemParameter.Code.SPECIAL_OFFER_CODE));
            if (specialOffer == null) {
                return retList;
            } else {
                return getSpecialOfferList(specialOffer);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting special offer", t);
            return null;
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public boolean checkIfOpenLedgerOperationFTsExist(SAPCompanyKey companyKey) throws Throwable {
        try {
            Company company = Company.findCompanyNoEagerLoad(companyKey.getCompanyId(), SourceSystemCode.valueOf(companyKey.getSourceSystemCd()));
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
            long ftCount = FinancialTransaction.findFinancialTransactionCountFromLedgerOperations(company);
            if (ftCount > 0) {
                return true;
            }
            return false;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error checking for ledger operations", t);
            return false;
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Offer validateAndGetOffer(String offerCode) {
        Offer offer = Offer.findOfferByOfferCode(offerCode);
        if (offer == null) {
            return null;
        }
        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        if (offer.getEndDate() != null) {
            SpcfCalendar offerEndDate = offer.getEndDate().toLocal();
            CalendarUtils.clearTime(offerEndDate);
            if (offerEndDate.before(today)) {
                return null;
            }
        }
        if (offer.getEffectiveDate() != null) {
            SpcfCalendar offerEffectiveDate = offer.getEffectiveDate().toLocal();
            CalendarUtils.clearTime(offerEffectiveDate);
            if (offerEffectiveDate.after(today)) {
                return null;
            }
        }
        return offer;
    }

    private ArrayList<SAPOffer> getDefaultOfferList() {
        ArrayList<SAPOffer> retList = new ArrayList<SAPOffer>();
        retList.add(new SAPOffer("", ""));
        String autoAssistedOfferCode = SystemParameter.findStringValue(SystemParameter.Code.EWS_ASSISTED_AUTO_OFFER_CODE);
        if (autoAssistedOfferCode != null) {
            Offer autoAssistedOffer = validateAndGetOffer(autoAssistedOfferCode);
            if (autoAssistedOffer != null) {
                retList.add(new SAPOffer(autoAssistedOffer.getOfferCd(), autoAssistedOffer.getDescription()));
            }
        }
        return retList;
    }

    private boolean doesSpecialOfferCodeExist() {
        boolean specialOfferActive = SystemParameter.findBooleanValue(SystemParameter.Code.SPECIAL_OFFER_ACTIVE);
        String specialOfferCode = SystemParameter.findStringValue(SystemParameter.Code.SPECIAL_OFFER_CODE);
        if (!specialOfferActive || specialOfferCode == null) {
            return false;
        }
        return true;
    }

    private ArrayList<SAPOffer> getSpecialOfferList(Offer specialOffer) {
        ArrayList<SAPOffer> retList = new ArrayList<SAPOffer>();
        retList.add(new SAPOffer("", ""));
        retList.add(new SAPOffer(specialOffer.getOfferCd(), specialOffer.getDescription()));
        return retList;
    }

    private String getUnderwritingPlatform(Company company) {
        return company.isMoneyMovementOnboardingEnabled()?"SMS":"PSP";
    }
}


