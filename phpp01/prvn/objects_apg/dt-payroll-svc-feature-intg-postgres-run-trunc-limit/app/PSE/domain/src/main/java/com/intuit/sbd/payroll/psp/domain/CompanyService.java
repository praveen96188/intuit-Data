package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Hand-written business logic
 */
public class CompanyService extends BaseCompanyService {

    public static SpcfLogger logger = SpcfLogManager.getLogger(CompanyService.class);

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<CompanyService> findCompanyServices(SourceSystemCode pSourceSystemCd, ServiceCode pServiceCd) {
        String[] paramNames = new String[3];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "serviceCd";
        paramNames[2] = "excludeDeletedCompany";

        Object[] paramValues = new Object[3];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pServiceCd;
        paramValues[2] = !AuthUser.hasSAPAdminAccess();
        return Application.findByNamedQueryUsingCache(CompanyService.class, "company.query.companyservice.by.sourcesystem", paramNames, paramValues);
    }

    public static DomainEntitySet<CompanyService> findCompanyServices(
            SourceSystemCode pSourceSystemCd,
            ServiceCode pServiceCd,
            String pLegalNameFragment) {
        String[] paramNames = new String[4];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "legalNameWithPercentSigns";
        paramNames[2] = "serviceCd";
        paramNames[3] = "excludeDeletedCompany";

        Object[] paramValues = new Object[4];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = "%" + pLegalNameFragment + "%";
        paramValues[2] = pServiceCd;
        paramValues[3] = !AuthUser.hasSAPAdminAccess();

        return Application.findByNamedQueryUsingCache(CompanyService.class, "company.query.companyservice.by.legalname", paramNames, paramValues);
    }

    public static DomainEntitySet<CompanyService> findCompanyServicesByFedTaxId(
            SourceSystemCode pSourceSystemCd,
            ServiceCode pServiceCd,
            String pFedTaxId) {
        String[] paramNames = new String[4];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "fedTaxIdEncList";
        paramNames[2] = "serviceCd";
        paramNames[3] = "excludeDeletedCompany";

        Object[] paramValues = new Object[4];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pFedTaxId);
        paramValues[2] = pServiceCd;
        paramValues[3] = !AuthUser.hasSAPAdminAccess();

        String queryName = "company.query.companyservice.by.fedtaxidenc";

        DomainEntitySet<CompanyService> results = Application.findByNamedQueryUsingCache(CompanyService.class, queryName, paramNames, paramValues);
        return results;
    }

    public static CompanyService findActiveCompanyServiceByFedTaxId(
            SourceSystemCode pSourceSystemCd,
            ServiceCode pServiceCd,
            String pFedTaxId) {
        Criterion<CompanyService> criterion = CompanyService.Company().SourceSystemCd().equalTo(pSourceSystemCd);
        if(pFedTaxId == null){
            criterion = criterion.And(CompanyService.Company().FedTaxIdEnc().isNull());
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,pFedTaxId);
            criterion = criterion.And(CompanyService.Company().FedTaxIdEnc().in(fedTaxIdEncList));
        }
            criterion = criterion.And(CompanyService.Service().ServiceCd().equalTo(pServiceCd))
                    .And(CompanyService.StatusCd().notIn(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled));

        Expression<CompanyService> query =
                new Query<CompanyService>()
                        .Where(criterion)
                        .OrderBy(CompanyService.CreatedDate().Descending());

        DomainEntitySet<CompanyService> companyServices = Application.find(CompanyService.class, query);

        if (companyServices.size() > 1) {
            throw new RuntimeException(String.format("Query for active CompanyService %s by FedTaxId %s on SourceSystemCode %s did not return 0 or 1 results as expected", pServiceCd, pFedTaxId, pSourceSystemCd));
        }
        
        return companyServices.isEmpty() ? null : companyServices.get(0);
    }

    public static DomainEntitySet<CompanyService> findCompanyServicesBySourceCompanyId(
            SourceSystemCode pSourceSystemCd,
            ServiceCode pServiceCd,
            String pSourceCompanyId) {
        String[] paramNames = new String[4];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "sourceCompanyId";
        paramNames[2] = "serviceCd";
        paramNames[3] = "excludeDeletedCompany";

        Object[] paramValues = new Object[4];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pSourceCompanyId;
        paramValues[2] = pServiceCd;
        paramValues[3] = !AuthUser.hasSAPAdminAccess();

        return Application.findByNamedQueryUsingCache(CompanyService.class, "company.query.companyservice.by.sourcecompanyid", paramNames, paramValues);
    }

    public static CompanyService findCompanyService(SourceSystemCode pSourceSystemCode, String SourceCompanyId, ServiceCode pServiceCd) {
        Service service = Application.findById(Service.class, pServiceCd);
        if (service == null) {
            return null;
        }

        Company company = Company.findCompany(SourceCompanyId, pSourceSystemCode);

        CompanyService foundCompService = null;

        NaturalKey naturalKey = new NaturalKey(CompanyService.class, company.getId(), pServiceCd);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundCompService = Application.findById(CompanyService.class, primaryKey);
        } else {
            for (CompanyService cs : company.getCompanyServiceCollection()) {
                if (cs.getService().getServiceCd() == pServiceCd) {
                    foundCompService = cs;
                    Application.getSessionCache().addPrimaryKey(naturalKey, cs.getId());
                }
            }
        }
        return foundCompService;
    }


    public static CompanyService findCompanyService(final Company pCompany, ServiceCode pServiceCd) {
        Service service = Application.findById(Service.class, pServiceCd);
        if (service == null) {
            return null;
        }

        CompanyService foundCompService = null;

        NaturalKey naturalKey = new NaturalKey(CompanyService.class, pCompany.getId(), pServiceCd);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundCompService = Application.findById(CompanyService.class, primaryKey);
        } else {
            for (CompanyService cs : pCompany.getCompanyServiceCollection()) {
                if (cs.getService().getServiceCd() == pServiceCd) {
                    foundCompService = cs;
                    Application.getSessionCache().addPrimaryKey(naturalKey, cs.getId());
                }
            }
        }
        return foundCompService;
    }

    public static DomainEntitySet<CompanyService> findTerminatedCompanyServicesByFeinExcludingSourceSystemIdAndServiceCode(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pFedTaxId, ServiceCode pServiceCode) {
        Criterion<CompanyService> criterion = CompanyService.Company().SourceSystemCd().equalTo(pSourceSystemCode);
        if(pFedTaxId == null){
            criterion = criterion.And(CompanyService.Company().FedTaxIdEnc().isNull());
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,pFedTaxId);
            criterion = criterion.And(CompanyService.Company().FedTaxIdEnc().in(fedTaxIdEncList));
        }
                criterion = criterion.And(CompanyService.Company().SourceCompanyId().notEqualTo(pSourceCompanyId))
                        .And(CompanyService.Service().ServiceCd().notEqualTo(pServiceCode))
                        .And(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.Terminated));
        Expression<CompanyService> query =
                new Query<CompanyService>()
                        .Where(criterion)
                        .OrderBy(CompanyService.CreatedDate().Descending());
        return Application.find(CompanyService.class, query);
    }

    public static DomainEntitySet<CompanyService> findActiveCompanyServicesByFeinExcludingSourceSystemIdAndServiceCode(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pFedTaxId, ServiceCode pServiceCode) {
        Criterion<CompanyService> criterion = CompanyService.Company().SourceSystemCd().equalTo(pSourceSystemCode);
        if(pFedTaxId == null){
            criterion = criterion.And(CompanyService.Company().FedTaxIdEnc().isNull());
        }else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,pFedTaxId);
            criterion = criterion.And(CompanyService.Company().FedTaxIdEnc().in(fedTaxIdEncList));
        }
                criterion = criterion.And(CompanyService.Company().SourceCompanyId().notEqualTo(pSourceCompanyId))
                        .And(CompanyService.Service().ServiceCd().notEqualTo(pServiceCode))
                        .And(CompanyService.StatusCd().notIn(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled));

        Expression<CompanyService> query =
                new Query<CompanyService>()
                        .Where(criterion)
                        .OrderBy(CompanyService.CreatedDate().Descending());
        return Application.find(CompanyService.class, query);
    }

    /**
     * Returns the corresponding company status for a service sub-status code
     *
     * @param pServiceSubStatusCd
     * @return
     */
    public static ServiceStatusCode getServiceStatus(ServiceSubStatusCode pServiceSubStatusCd) {
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, pServiceSubStatusCd);
        return serviceSubStatus.getServiceStatus().getServiceStatusCd();
    }

    public static ServiceSubStatusCode findServiceStatusForCompanyService(String pCompanyId, ServiceCode pServiceCd) {
        ServiceSubStatusCode serviceSubStatusCd = null;

        String[] paramNames = new String[3];
        paramNames[0] = "companyId";
        paramNames[1] = "serviceCd";
        paramNames[2] = "excludeDeletedCompany";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompanyId;
        paramValues[1] = pServiceCd;
        paramValues[2] = !AuthUser.hasSAPAdminAccess();

        List<ServiceSubStatusCode> statuses = Application.executeNamedQuery("findServiceStatusForCompanyService", paramNames, paramValues);

        if (statuses.size() > 1) {
            throw new RuntimeException("Query for service status by companyid " + pCompanyId + " and service " + pServiceCd + " did not return 0 or 1 results as expected");
        }

        if (!statuses.isEmpty()) {
            serviceSubStatusCd = statuses.get(0);
        }

        return serviceSubStatusCd;
    }

    public static DomainEntitySet<CompanyService> findActiveCompanyServiceByServiceCode(ServiceCode pServiceCode) {
        Expression<CompanyService> query = null;
        query = new Query<CompanyService>()
                .Where(CompanyService.Service().ServiceCd().equalTo(pServiceCode)
                        .And(CompanyService.StatusCd().notEqualTo(ServiceSubStatusCode.Cancelled).And(CompanyService.StatusCd().notEqualTo(ServiceSubStatusCode.Terminated))))
                .EagerLoad(CompanyService.Company());
        return Application.find(CompanyService.class, query);
    }

    public static CompanyService findActiveCompanyServiceByCompanyAndServiceCode(Company pCompany, ServiceCode pServiceCode) {
        Expression<CompanyService> query =
                new Query<CompanyService>()
                        .Where(CompanyService.Company().equalTo(pCompany)
                                .And(CompanyService.Service().ServiceCd().equalTo(pServiceCode)
                                .And(CompanyService.StatusCd().notIn(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled))));

        DomainEntitySet<CompanyService> companyServices = Application.find(CompanyService.class, query);

        return companyServices.isEmpty() ? null : companyServices.get(0);
    }

    public static DomainEntitySet<CompanyService> findActiveCompanyServices(Company pCompany) {
        Expression<CompanyService> query =
                new Query<CompanyService>()
                        .Where(CompanyService.Company().equalTo(pCompany)
                            .And(CompanyService.StatusCd().notIn(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled)));

        DomainEntitySet<CompanyService> companyServices = Application.find(CompanyService.class, query);
        return companyServices;
    }    

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CompanyService() {
        super();
    }

    /**
     * Checks if a system capability is allowed for the current status of the specified company service and
     * the company onHold status.
     *
     * @param pSystemCapabilityCd
     * @return
     */
    public boolean isAllowedCapability(SystemCapabilityCode pSystemCapabilityCd) {
        Company company = getCompany();
        SystemCapability systemCapability = Application.findById(SystemCapability.class, pSystemCapabilityCd);
        boolean allowedCapability = false;

        if (company.isCompanyOnHold()) {
            // Check if any of the on-hold reasons doesn't allow the capability
            for (ServiceSubStatusCode onHoldReasonCd : getCompany().getCurrentOnHoldReasonCodes()) {
                ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCd);
                allowedCapability = serviceSubStatus.getSystemCapabilityCollection().contains(systemCapability);
                if (!allowedCapability) {
                    // We don't have to continue checking; we've found one on-hold reason that doesn't allow the capability
                    break;
                }
            }
        } else {
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, getStatusCd());
            allowedCapability = serviceSubStatus.getSystemCapabilityCollection().contains(systemCapability);
        }
        return allowedCapability;
    }

    /**
     * Updates the service status
     *
     * @param pNewServiceSubStatusCd
     */
    public void updateCompanyServiceStatus(ServiceSubStatusCode pNewServiceSubStatusCd) {
        updateCompanyServiceStatus(pNewServiceSubStatusCd, true);
    }

    /**
     * Updates the service status
     *
     * @param pNewServiceSubStatusCd
     * @param pCreateEvents
     */
    public void updateCompanyServiceStatus(ServiceSubStatusCode pNewServiceSubStatusCd, boolean pCreateEvents) {
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, pNewServiceSubStatusCd);
        if (serviceSubStatus.getServiceStatus().getServiceStatusCd() == ServiceStatusCode.OnHold) {
            throw new RuntimeException(String.format("An On Hold service status '{0}' cannot be assigned to a service.", pNewServiceSubStatusCd));
        }
        if (getStatusCd() == pNewServiceSubStatusCd) {
            // nothing to do
            return;
        }

        ServiceSubStatusCode currentServiceSubStatusCode = getStatusCd();

        if (pCreateEvents) {
            CompanyEvent.createServiceStatusChangeEvent(this,
                                                        currentServiceSubStatusCode,
                                                        pNewServiceSubStatusCd,
                                                        PSPDate.getPSPTime());
        }
//
        // Maintain Company Info suspicious of fraud if service has liability and company is being terminated or being moved out of terminated status
        // This could be called more then once if they have more then one liable service
        if ((getService().doesPSPMoveMoneyForService() || getService().getServiceCd().equals(ServiceCode.RiskAssessment)) && pNewServiceSubStatusCd.equals(ServiceSubStatusCode.Terminated)) {
            // Moving a company to the terminated status
            FraudCompany.addFraudRecords(getCompany());
        }

        if (getService().doesPSPMoveMoneyForService() && currentServiceSubStatusCode.equals(ServiceSubStatusCode.Terminated)) {
            // Moving a company out of the terminated status
            FraudCompany.removeFraudRecords(getCompany());
        }

        // Active -> Cancel -> Active scenario
        boolean isReactivatingService =
                (currentServiceSubStatusCode == ServiceSubStatusCode.Cancelled || currentServiceSubStatusCode == ServiceSubStatusCode.Terminated)
                && (pNewServiceSubStatusCd != ServiceSubStatusCode.Cancelled && pNewServiceSubStatusCd != ServiceSubStatusCode.Terminated);


        // don't allow DD service to be cancelled if Tax is still active for QBDT companies (Assisted)
        if(getCompany().getSourceSystemCd() == SourceSystemCode.QBDT &&
                getService().getServiceCd() == ServiceCode.DirectDeposit &&
                (pNewServiceSubStatusCd == ServiceSubStatusCode.Cancelled || pNewServiceSubStatusCd == ServiceSubStatusCode.Terminated) &&
                getCompany().isCompanyOnService(ServiceCode.Tax)) {
            throw new RuntimeException("Direct deposit service cannot be cancelled or terminated while Tax service is still active");
        }

        setStatusCd(pNewServiceSubStatusCd);
        setStatusEffectiveDate(PSPDate.getPSPTime());

        if(getService().getServiceCd() == ServiceCode.Tax && pNewServiceSubStatusCd == ServiceSubStatusCode.ActiveCurrent) {
            for (EntitlementUnit entitlementUnit : getCompany().getEntitlementUnitCollection()) {
                if(entitlementUnit.getEntitlementUnitStatus() == EntitlementUnitStatusCode.ActivationHold) {
                    entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
                }
            }
            // Tax service is active now, Enroll in to FL-ACH enrollment if not enrolled and all other conditions are met.
            ACHEnrollment.createACHEnrollment(getCompany(), false);
        }

        if(getService().getServiceCd() == ServiceCode.Tax && pNewServiceSubStatusCd.in(ServiceSubStatusCode.PendingFirstPayroll, ServiceSubStatusCode.ActiveCurrent)) {
            CompanyOffering companyOffering = getCompany().getDirectDepositCompanyOffering();
            Offering defaultTaxOffering = Offering.findDefaultOffering(getCompany(), ServiceCode.Tax);

            // Update CompanyOffering if moving from DIY+DD to Assisted offering
            boolean isMigrating = companyOffering.getOffering().getReportingType() == null || companyOffering.getOffering().getReportingType().equals(com.intuit.sbd.payroll.psp.domain.ReportingType.DirectDeposit);
            if(isMigrating && !defaultTaxOffering.equals(companyOffering.getOffering())) {
                Application.delete(companyOffering);     // Deleting existing CompanyOffering
                getCompany().removeCompanyOffering(companyOffering);

                CompanyOffering defaultCompanyOffering = new CompanyOffering();
                defaultCompanyOffering.setCompany(getCompany());
                defaultCompanyOffering.setOffering(defaultTaxOffering);
                Application.save(defaultCompanyOffering);
                getCompany().addCompanyOffering(defaultCompanyOffering);

                CompanyEvent.createOfferingUpdatedEvent(getCompany(), companyOffering.getOffering().getSKU(), defaultCompanyOffering.getOffering().getSKU());
                if (SystemParameter.findBooleanValue(SystemParameter.Code.SPECIAL_OFFER_ACTIVE) && validateForSpecialOffer(getCompany())) {
                    String autoOffer = SystemParameter.findStringValue(SystemParameter.Code.SPECIAL_OFFER_CODE);
                    getCompany().claimOfferForCompany(Offer.findOfferByOfferCode(autoOffer));
                    logger.info("Company=" + getCompany().getSourceCompanyId() + " Offer automatically added: " + autoOffer);

                }
            }
        }

        Application.save(this);
    }

    //Method to check special offer running
    private boolean validateForSpecialOffer(Company pCompany) {
        String specialOfferCode = SystemParameter.findStringValue(SystemParameter.Code.SPECIAL_OFFER_CODE);
        if (specialOfferCode == null) {
            logger.info("No special offer running");
            return false;
        }
        CompanyOffering companyOffering = pCompany.getOffering(com.intuit.sbd.payroll.psp.domain.ServiceCode.DirectDeposit);
        if (companyOffering == null) {
            logger.info("Company=" + pCompany.getSourceCompanyId() + " No offering attached");
            return false;
        }
        if (!companyOffering.getOffering().getOfferingCode().equals(com.intuit.sbd.payroll.psp.domain.OfferingCode.SYMFY14)) {
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

    public ServiceSubStatusCode getNextValidServiceStatus(ServiceSubStatusCode pStepVerificationStatus) {
        if (getStatusCd() == ServiceSubStatusCode.ActiveCurrent ||
            getStatusCd() == ServiceSubStatusCode.ActiveSeasonal ||
            getStatusCd() == ServiceSubStatusCode.Terminated) {
            return getStatusCd();
        }

        //Special case for reactivate service
        if (pStepVerificationStatus != ServiceSubStatusCode.Cancelled &&
                getStatusCd() == ServiceSubStatusCode.Cancelled) {            
            return getStatusCd();
        }

        List<ServiceSubStatusCode> serviceSetupSteps = getSetupServiceSteps();

        SourceSystem sourceSystem = Application.findById(SourceSystem.class, getCompany().getSourceSystemCd());
        ServiceSubStatus serviceSubStatus;
        ArrayList<ServiceSubStatusCode> codesToRemove = new ArrayList<ServiceSubStatusCode>();
        for (ServiceSubStatusCode serviceSetupStep : serviceSetupSteps) {
            serviceSubStatus = Application.findById(ServiceSubStatus.class, serviceSetupStep);
            if (!serviceSubStatus.getSourceSystemCollection().contains(sourceSystem)) {
                codesToRemove.add(serviceSetupStep);
            }
        }

        for (ServiceSubStatusCode serviceSubStatusCode : codesToRemove) {
            serviceSetupSteps.remove(serviceSubStatusCode);
        }

        if(pStepVerificationStatus != null) {
            serviceSetupSteps.remove(pStepVerificationStatus);
        }

        if(CompanyBankAccount.findActiveCompanyBankAccount(getCompany()) != null) {
            serviceSetupSteps.remove(ServiceSubStatusCode.PendingBankVerification);
        }

        if(getCompany().getCompanyPINCollection().size() > 0) {
            serviceSetupSteps.remove(ServiceSubStatusCode.PendingPinCreation);
        }

        if(pStepVerificationStatus == ServiceSubStatusCode.PendingTaxAcceptance) {
            serviceSetupSteps.remove(ServiceSubStatusCode.PendingEnrollment);
        }

        if(serviceSetupSteps.size() > 0) {
            return serviceSetupSteps.get(0);
        }

        return ServiceSubStatusCode.ActiveCurrent;
    }

    public List<ServiceSubStatusCode> getSetupServiceSteps() {
        List<ServiceSubStatusCode> serviceSubStatusCodes = new ArrayList<ServiceSubStatusCode>();

        switch (getService().getServiceCd()) {
            case Cloud:
            case ThirdParty401k:
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingPinCreation);
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingFirstPayroll);
                break;

            case DirectDeposit:
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingBankVerification);
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingPinCreation);
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingFirstPayroll);
                break;

            case Tax:
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingSetup);
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingBalanceFile);
                serviceSubStatusCodes.add(ServiceSubStatusCode.PendingFirstPayroll);
        }

        return serviceSubStatusCodes;
    }

    public Collection<ServiceSubStatusCode> getAvailableServiceSubStatusesCodes() {
        DomainEntitySet<ServiceSubStatus> serviceSubStatuses = new DomainEntitySet<ServiceSubStatus>();

        String hqlQuery = " select distinct ss " +
                "           from com.intuit.sbd.payroll.psp.domain.ServiceSubStatus as ss" +
                "           join ss.ServiceSet as svc" +
                "           where svc.ServiceCd = '" + getService().getServiceCd() + "'";

        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hqlQuery);
        serviceSubStatuses.addAll(hibernateQuery.list());

        ArrayList<ServiceSubStatusCode> serviceSubStatusCodes = new ArrayList<ServiceSubStatusCode>();
        for (ServiceSubStatus serviceSubStatus : serviceSubStatuses) {
            serviceSubStatusCodes.add(serviceSubStatus.getServiceSubStatusCd());
        }

        return serviceSubStatusCodes;
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(Company.class, getCompany().getId(), getService().getServiceCd());
    }


    public FundingModel getEffectiveFundingModel() {
        return this.getCompany().getFundingModel();
    }

    public static boolean wasCompanyOnServiceForDate(Company pCompany, ServiceCode pServiceCode, SpcfCalendar pDate) {
        if (pDate == null || pCompany == null) {
            return false;
        }

        CompanyService tp401kService = CompanyService.findCompanyService(pCompany, pServiceCode);
        if (tp401kService == null) {
            return false;
        }

        SpcfCalendar serviceStartDate = tp401kService.getServiceStartDate();
        if (serviceStartDate == null) {
            return false;
        }
        //Don't care about the hours, only the date
        pDate.setValues(pDate.getYear(), pDate.getMonth(), pDate.getDay(), 0, 0, 0, 0);
        serviceStartDate.setValues(serviceStartDate.getYear(), serviceStartDate.getMonth(), serviceStartDate.getDay(), 0, 0, 0, 0);

        return serviceStartDate.before(pDate);
    }

    //this should probably be something like "isCompanyOnFinTxnService" but I don't really know exactly what.
    public static boolean isCompanyOnDirectDepositOrTaxService(Company pCompany) {
        return findCompanyService(pCompany, ServiceCode.DirectDeposit) != null || findCompanyService(pCompany, ServiceCode.Tax) != null;
    }

    /*
     * Checks the "main" services to see if all are cancelled or any are termed.
     * When that is the case, additional validation is performed for certain actions.
     */
    public boolean additionalCancelTermValidationRequired() {
        return isCancelTerm();
    }

    public boolean isCancelTerm() {
        return getStatusCd().in(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled);
    }

    public boolean isV3Service() {
        return getService().getServiceCd()== com.intuit.sbd.payroll.psp.domain.ServiceCode.CloudV3;
     }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder .append(getService())
                .append("  Status: ").append(getStatusCd().name())
                .append("  StatusEffDate: ").append(getStatusEffectiveDate())
                .append("  StartDate: ").append(getServiceStartDate());
        return builder.toString();
    }

    public boolean isActive() {
        return ServiceSubStatusCode.ActiveCurrent.equals(getStatusCd());
    }

    public boolean isPending() {
        return getStatusCd().in(ServiceSubStatusCode.PendingFirstPayroll,
                                ServiceSubStatusCode.PendingBalanceFile,
                                ServiceSubStatusCode.PendingEnrollment,
                                ServiceSubStatusCode.PendingBankVerification,
                                ServiceSubStatusCode.PendingPinCreation,
                                ServiceSubStatusCode.PendingSetup,
                                ServiceSubStatusCode.PendingTaxAcceptance,
                                ServiceSubStatusCode.PendingPrefundingWire);
    }

    public boolean isIntuitResponsibleForLiabilities(SpcfCalendar pPaycheckDate) {
        return !pPaycheckDate.before(getServiceStartDate());
    }

    @Override
    public void setStatusCd(ServiceSubStatusCode pStatusCd) {
        if(!ObjectUtils.equals(getStatusCd(), pStatusCd)) {
            if(pStatusCd.in(ServiceSubStatusCode.Cancelled, ServiceSubStatusCode.Terminated) && getCompany() != null && getCompany().getSourceSystemCd() == SourceSystemCode.QBDT) {
                // update the token so we push back the service status
                getCompany().getNextToken();
                Application.save(getCompany());
            }
        }

        super.setStatusCd(pStatusCd);
    }
    
    public static List<Company> filterVMPEnabledCompanies(Company[] companyArray) {
        Expression<CompanyService> query = new Query<CompanyService>()
                .Where(CompanyService.Service().ServiceCd().equalTo(ServiceCode.ViewMyPaycheck)
                        .And(CompanyService.Company().in(companyArray))
                        .And(CompanyService.StatusCd().notEqualTo(ServiceSubStatusCode.Cancelled)
                                .And(CompanyService.StatusCd().notEqualTo(ServiceSubStatusCode.Terminated))))
                .EagerLoad(CompanyService.Company());
        DomainEntitySet<CompanyService> companyServiceSet = Application.find(CompanyService.class, query);
        List<Company> vmpEnabledCompanyList = new ArrayList<>();
        if (companyServiceSet != null && companyServiceSet.size() != 0) {
            companyServiceSet.forEach(companyService -> {
                vmpEnabledCompanyList.add(companyService.getCompany());
            });
        }
        return vmpEnabledCompanyList;
    }
}
