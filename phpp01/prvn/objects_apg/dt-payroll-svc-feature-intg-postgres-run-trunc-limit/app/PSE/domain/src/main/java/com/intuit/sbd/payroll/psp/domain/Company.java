package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.exceptions.UniqueCompanyNotFoundException;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.EmptyCriterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.SortableProperty;
import com.intuit.sbd.payroll.psp.query.clauses.GroupByClause;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowPackager;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.WorkflowPackager;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollableResults;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Hand-written business logic
 */
public class Company extends BaseCompany implements IUpdatable {
    public static SpcfLogger logger = SpcfLogManager.getLogger(Company.class);
    public static int MAX_LENGTH_NOTES = 4000;
    public static String LEGAL_ADDRESS = "Legal";
    public static String MAILING_ADDRESS = "Mailing";
    public static String FedTaxIdKeyName ="Company_FedTaxId";
    public static String PrivateKeyEncKeyName = "Company_PrivateKey";
    PublishStatusWorkflowPackager publishStatusWorkflowPackager = null;

    private static final String COMPANY_TOKEN_CACHE_KEY = "Cache:COMPANY_TOKEN";
    public static final long EXCLUDE_TOKEN = -2;
    WorkflowPackager workflowPackager = null;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders & counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Company findCompanyNoEagerLoad(String pSourceCompanyId, SourceSystemCode pSourceSystemCd) {

        Company foundCompany = null;
        DomainEntitySet<Company> companies = Application.find(Company.class,
                                                              Company.SourceCompanyId().equalTo(pSourceCompanyId)
                                                                     .And(Company.SourceSystemCd().equalTo(pSourceSystemCd)));

        if (companies.size() > 1) {
            throw new RuntimeException("Query for companies by source system " + pSourceSystemCd + " and source company id " + pSourceCompanyId + " did not return 0 or 1 results as expected");
        }

        if (!companies.isEmpty()) {
            foundCompany = companies.get(0);
        }

        return foundCompany;
    }

    public static List<String> findActiveCompaniesOnDDService (){
       List<String> sourceCompanyIds = Application.executeNamedQuery("findActiveCompaniesOnDirectDeposit", new String[]{"excludeDeletedCompany"}, new Object[]{!AuthUser.hasSAPAdminAccess()});
       return sourceCompanyIds;
    }

    public static Company findCompany(String pSourceCompanyId, SourceSystemCode pSourceSystemCd) {
        Company foundCompany = null;

        NaturalKey naturalKey = new NaturalKey(Company.class, pSourceSystemCd, pSourceCompanyId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundCompany = Application.findById(Company.class, primaryKey);
        } else {
            Expression<Company> query =
                    new Query<Company>()
                            .Where(Company.SourceCompanyId().equalTo(pSourceCompanyId)
                                          .And(Company.SourceSystemCd().equalTo(pSourceSystemCd)));
            if(pSourceSystemCd == SourceSystemCode.QBDT) {
                // QuickbooksInfo is a one-to-one relationship, so it will be eagerly fetched by hibernate. This saves us a trip to the db.
                ((Query<Company>)query).EagerLoad(Company.CompanyBankAccountSet(), Company.CompanyServiceSet(), Company.QuickbooksInfo());
            } else {
                ((Query<Company>)query).EagerLoad(Company.CompanyBankAccountSet(), Company.CompanyServiceSet());
            }


            DomainEntitySet<Company> companies = Application.find(Company.class, query);

            if (companies.size() > 1) {
                throw new RuntimeException("Query for companies by source system " + pSourceSystemCd + " and source company id " + pSourceCompanyId + " did not return 0 or 1 results as expected");
            }

            if (!companies.isEmpty()) {
                foundCompany = companies.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, foundCompany.getId());
            }
        }
        return foundCompany;
    }

    public static DomainEntitySet<Company> getAllCompaniesByRealm(String companyRealmId) {
        Criterion<Company> companyCriterion = Company.IAMRealmId().equalTo(companyRealmId)
                .And(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        DomainEntitySet<Company> companies = Application.find(Company.class, companyCriterion);
        return companies;
    }

    public static DomainEntitySet<Company> getAllCompaniesByRealms(List<String> realms) {
        Criterion<Company> companyCriterion = Company.IAMRealmId().in(realms)
                .And(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        DomainEntitySet<Company> companies = Application.find(Company.class, companyCriterion);
        return companies;
    }

    public static DomainEntitySet<FraudCompany> findMatchingCompaniesByFraudCriteria(final Company pCompany) {
        int numElementsToCheck = 1;
        String hql=null;
        String[] paramNames = new String[numElementsToCheck];
        Object[] paramValues = new Object[numElementsToCheck];
        paramNames[0] = "fedTaxIdEncList";

        hql = " Select fraudComp " +
                " from com.intuit.sbd.payroll.psp.domain.FraudCompany as fraudComp " +
                " join fetch fraudComp.Company ";
        if(pCompany.getFedTaxId() != null) {
            paramValues[0] = EncryptionUtils.deterministicEncryptWithAllKeys(FraudCompany.FedTaxIdKeyName, pCompany.getFedTaxId());
            hql = hql + " where fraudComp.FedTaxIdEnc in (:fedTaxIdEncList) ";
        } else {
            paramValues[0] = null;
            hql = hql + " where fraudComp.FedTaxIdEnc=:fedTaxIdEncList";
        }
        return Application.findByHQLQuery(hql, paramNames, paramValues, 0, 25);
    }

    public static Company findActiveCompany(SourceSystemCode pSrcSystemCd, String pFedTaxId) {
        if (pSrcSystemCd.equals(SourceSystemCode.IOP)) {
            throw new RuntimeException("Query 'findActiveCompany(SourceSystemCode pSrcSystemCd, String pFedTaxId)' cannot be used for IOP customers");
        }

        Company foundCompany = null;
        String[] paramNames = new String[2];
        paramNames[0] = "companyFedTaxIdEncList";
        paramNames[1] = "companySrcSysCd";

        Object[] paramValues = new Object[2];
        String namedQuery = "findActiveCompaniesForEINENC";
        paramValues[0] = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pFedTaxId);
        paramValues[1] = pSrcSystemCd;


        DomainEntitySet<Company> companies = Application.findByNamedQueryUsingCache(Company.class, namedQuery, paramNames, paramValues);
        if (companies.size() > 1) {
            throw new RuntimeException("Query for active companies by source-system/fein " + pSrcSystemCd + "/" + pFedTaxId + " did not return 0 or 1 results as expected");
        }

        if (!companies.isEmpty()) {
            foundCompany = companies.get(0);
        }

        return foundCompany;
    }

    public static Company findActiveCompanyWithPSID(SourceSystemCode pSrcSystemCd, String pFedTaxId, String pSrcCompanyId) {
        if (pSrcSystemCd.equals(SourceSystemCode.IOP)) {
            throw new RuntimeException("Query 'findActiveCompanyWithPSID(SourceSystemCode pSrcSystemCd, String pFedTaxId, String pSrcCmpId)' cannot be used for IOP customers");
        }

        Company foundCompany = null;
        String[] paramNames = new String[3];
        paramNames[0] = "companyFedTaxIdEncList";
        paramNames[1] = "companySrcSysCd";
        paramNames[2] = "companySrcCmpId";

        Object[] paramValues = new Object[3];
        paramValues[0] = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pFedTaxId);
        paramValues[1] = pSrcSystemCd;
        paramValues[2] = pSrcCompanyId;

        String namedQuery = "findActiveCompaniesForEINENCAndPSID";

        DomainEntitySet<Company> companies = Application.findByNamedQueryUsingCache(Company.class, namedQuery, paramNames, paramValues);

        if (companies.size() > 1) {
            throw new RuntimeException("Query for active companies by source-system/fein/psid " + pSrcSystemCd + "/" + pFedTaxId + "/" + pSrcCompanyId + " did not return 0 or 1 results as expected");
        }

        if (!companies.isEmpty()) {
            foundCompany = companies.get(0);
        }

        return foundCompany;
    }

    public static DomainEntitySet<Company> findActiveCompanies(SourceSystemCode pSrcSystemCd, String pFedTaxId) {
        String[] paramNames = new String[2];
        paramNames[0] = "companyFedTaxIdEncList";
        paramNames[1] = "companySrcSysCd";

        Object[] paramValues = new Object[2];
        paramValues[0] = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pFedTaxId);
        paramValues[1] = pSrcSystemCd;

        String namedQuery = "findActiveCompaniesForEINENC";

        DomainEntitySet<Company> results = Application.findByNamedQueryUsingCache(Company.class, namedQuery, paramNames, paramValues);
        return results;
    }

    public static boolean isEINInUse(String pFedTaxId) {
        return isEINInUse(pFedTaxId, null);
    }

    public static boolean isEINInUse(String pFedTaxId, SourceSystemCode pSourceSystemCode) {
        //this is equivalent to checking the EIN on the companies with Activated statuses

        Criterion<EntitlementUnit> entitlementUnitCriterion = null;
        if(pFedTaxId == null) {
            entitlementUnitCriterion = EntitlementUnit.FedTaxIdEnc().isNull();
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, pFedTaxId);
            entitlementUnitCriterion = EntitlementUnit.FedTaxIdEnc().in(fedTaxIdEncList);
        }

        boolean einInUseByEntitlement = Application.find(EntitlementUnit.class,
                EntitlementUnit.EntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES)
                        .And(entitlementUnitCriterion)).size() > 0;

        Criterion<CompanyService> companyServiceCriterion = null;
        if(pFedTaxId == null) {
            companyServiceCriterion = CompanyService.Company().FedTaxIdEnc().isNull();
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pFedTaxId);
            companyServiceCriterion = CompanyService.Company().FedTaxIdEnc().in(fedTaxIdEncList);
        }

        if(!AuthUser.hasSAPAdminAccess()) {
            companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().IsDgDisassociated().equalTo(false));
        }

        companyServiceCriterion = companyServiceCriterion.And(CompanyService.StatusCd().notIn(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled));

        if (pSourceSystemCode != null) {
            companyServiceCriterion = companyServiceCriterion.And(CompanyService.Company().SourceSystemCd().equalTo(pSourceSystemCode));
        }

        boolean einInUseByStatus = Application.find(CompanyService.class, companyServiceCriterion).size() > 0;

        return einInUseByEntitlement || einInUseByStatus;
    }

    public static DomainEntitySet<Company> findCompaniesByCompanyIdList(List<SpcfUniqueId> companyIds) {
        Criterion<Company> companyExpression = null;
        if (companyIds == null) {
            return null;
        }
        return Application.find(Company.class, Company.Id().in(companyIds));
    }

    public static DomainEntitySet<Company> findCompaniesBySourceCompanyIds(SourceSystemCode pSourceSystemCd, List<String> pSourceCompanyIds) {
        Criterion<Company> companyExpression = null;
        if(pSourceCompanyIds == null) {
            return null;
        }

        companyExpression = Company.SourceCompanyId().in(pSourceCompanyIds);
        companyExpression = companyExpression.And(Company.SourceSystemCd().equalTo(pSourceSystemCd));
        return Application.find(Company.class, companyExpression);
    }

    public static DomainEntitySet<Company> findCompanies(SourceSystemCode pSourceSystemCd, String pFedTaxId) {
        Criterion<Company> companyExpression = null;
        if(pFedTaxId == null) {
            companyExpression = Company.FedTaxIdEnc().isNull();
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pFedTaxId);
            companyExpression = Company.FedTaxIdEnc().in(fedTaxIdEncList);
        }
        companyExpression = companyExpression.And(Company.SourceSystemCd().equalTo(pSourceSystemCd));
        return Application.find(Company.class, companyExpression);
    }

    public static DomainEntitySet<Company> findTerminatedCompanies(final String pFedTaxId) {
        String[] paramNames = new String[1];
        // paramNames[1] = "companySrcSysCd";

        Object[] paramValues = new Object[1];

        String namedQuery = "findCompaniesTermedForEINENC";
        paramNames[0] = "companyFedTaxIdEncList";
        paramValues[0] = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, pFedTaxId);

        DomainEntitySet<Company> results = Application.findByNamedQueryUsingCache(Company.class, namedQuery, paramNames, paramValues);
        return results;
    }

    public static DomainEntitySet<Company> searchCompaniesByEIN(final String pFedTaxId) {
        int maxResults = SystemParameter.findIntValue(SystemParameter.Code.PSPUI_MAX_COMPANY_SEARCH_RESULTS, 100);
        String searchInput = pFedTaxId.replaceAll("-", "");
        List<String> companyFedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,searchInput);
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.FedTaxIdEnc().in(companyFedTaxIdEncList)).LimitResults(0, maxResults));
        return companies;
    }

    public static DomainEntitySet<Company> searchCompaniesBySourceCompanyId(final String pSourceCompanyId) {
        int maxResults = SystemParameter.findIntValue(SystemParameter.Code.PSPUI_MAX_COMPANY_SEARCH_RESULTS, 100);
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceCompanyId().like(pSourceCompanyId + "%", true)).LimitResults(0, maxResults));
        return companies;
    }

    public enum MatchType{
        EXACT,
        CONTAINS,
        BEGINS_WITH
    }

    public static DomainEntitySet<Company> searchCompaniesByLegalName(final String pLegalName) {
        return searchCompaniesByLegalName(pLegalName, MatchType.BEGINS_WITH, true);
    }

    public static DomainEntitySet<Company> searchCompaniesByLegalName(
            final String pLegalName, MatchType matchType, boolean caseInsensitive) {
        int maxResults = SystemParameter.findIntValue(SystemParameter.Code.PSPUI_MAX_COMPANY_SEARCH_RESULTS, 100);

        Criterion<Company> legalNameCriterion;
        //adding this condition to improve performance by taking advantage of indexing
        if(matchType.equals(MatchType.EXACT) && !caseInsensitive)
            legalNameCriterion = Company.LegalName().equalTo(pLegalName);
        else
            legalNameCriterion = Company.LegalName().like(getLikeStringBasedOnMatchType(pLegalName, matchType), caseInsensitive);

        return Application.find(Company.class, new Query<Company>().Where(legalNameCriterion)
                .LimitResults(0, maxResults));
    }

    private static String getLikeStringBasedOnMatchType(String property, MatchType matchType){
        switch(matchType){
            case EXACT:
                return property;
            case CONTAINS:
                return "%" + property + "%";
            case BEGINS_WITH:
            default:
                return property + "%";
        }
    }

    private static DomainEntitySet<Company> filterCompaniesforSAP(DomainEntitySet<Company> companies) {
        boolean isAdmin = AuthUser.isAuthorizedtoAccessDGDeletedCompanies();
        logger.info("This is a SAP request. The current user admin status is : " + isAdmin);

        if (!isAdmin) {
            // filter out dg deleted companies
            DomainEntitySet<Company> filteredCompanies = filterDGDeletedCompanies(companies);
            return filteredCompanies;
        }
        return companies;
    }

    private static DomainEntitySet<Company> filterDGDeletedCompanies(DomainEntitySet<Company> companies){
        logger.info("Filtering out DG deleted companies as part of DG discoverability.");
        Predicate<Company> isNotDGDeletedCompany = comp -> !comp.getIsDgDisassociated();
        return new DomainEntitySet<Company>(companies.stream().filter(isNotDGDeletedCompany).collect(Collectors.toSet()));
    }

    private static DomainEntitySet<Company> filterCompanies(DomainEntitySet<Company> companies){
        boolean isDGDiscoverabilityEnabled = isDGDeleteFeatureEnabled();
        logger.info("feature flag for DG Discoverability changes is DG_DISCOVERABILITY_FEATURE=" + isDGDiscoverabilityEnabled);

        if(isDGDiscoverabilityEnabled){
            // feature flag of DG discoverability is ON.
            if(!Objects.isNull(Application.getCurrentPrincipal())){
                // PSP current principal is not null.
                if(Application.getCurrentPrincipal().isAgent()){
                    // SAP flow.
                    return filterCompaniesforSAP(companies);
                }else {
                    //Non SAP flow
                    return filterDGDeletedCompanies(companies);
                }
            }else{
                //if PSP principal is null
                //Non SAP flow
                return filterDGDeletedCompanies(companies);
            }
        }
        return companies;
    }

    public static DomainEntitySet<Company> searchCompaniesByLicenseNumber(final String licenseNumber) {
        DomainEntitySet<Company> companies = new DomainEntitySet<Company>();

        DomainEntitySet<EntitlementUnit> entitlementsUnits =
                Application.find(EntitlementUnit.class,
                                 EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber));

        for (EntitlementUnit eu : entitlementsUnits) {
            if (!companies.contains(eu.getCompany())) {
                companies.add(eu.getCompany());
            }
        }
        return filterCompanies(companies);
    }


    public static DomainEntitySet<Company> searchCompaniesByLicenseNumberAndEoc(final String licenseNumber, final String eoc) {
        DomainEntitySet<Company> companies = new DomainEntitySet<Company>();

        DomainEntitySet<EntitlementUnit> entitlementsUnits =
                Application.find(EntitlementUnit.class,
                        EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)
                                .And(EntitlementUnit.Entitlement().EntitlementOfferingCode().equalTo(eoc)));

        for (EntitlementUnit eu : entitlementsUnits) {
            if (!companies.contains(eu.getCompany())) {
                companies.add(eu.getCompany());
            }
        }
        return filterCompanies(companies);
    }

    public static DomainEntitySet<Company> searchCompaniesByCAN(final String cAN) {
        DomainEntitySet<Company> companies = new DomainEntitySet<Company>();

        DomainEntitySet<EntitlementUnit> entitlementsUnits =
                Application.find(EntitlementUnit.class,
                                 EntitlementUnit.Entitlement().CustomerId().equalTo(cAN));

        for (EntitlementUnit eu : entitlementsUnits) {
            if (!companies.contains(eu.getCompany())) {
                companies.add(eu.getCompany());
            }
        }
        return filterCompanies(companies);
    }

    public static DomainEntitySet<Company> searchCompaniesByServiceKey(final String serviceKey) {
        DomainEntitySet<Company> companies = new DomainEntitySet<Company>();

        DomainEntitySet<EntitlementUnit> entitlementsUnits =
                Application.find(EntitlementUnit.class,
                                 EntitlementUnit.ServiceKey().equalTo(serviceKey));

        for (EntitlementUnit eu : entitlementsUnits) {
            if (!companies.contains(eu.getCompany())) {
                companies.add(eu.getCompany());
            }
        }
        return filterCompanies(companies);
    }

    public static DomainEntitySet<Company> searchCompaniesByRegistrationNumber(final String regNum) {
        DomainEntitySet<Company> companies = new DomainEntitySet<Company>();

        DomainEntitySet<QuickbooksInfo> qbInfos =
                Application.find(QuickbooksInfo.class,
                        QuickbooksInfo.LicenseNumber().equalTo(regNum));

        for (QuickbooksInfo qbObj : qbInfos) {
            if (!companies.contains(qbObj.getCompany())) {
                companies.add(qbObj.getCompany());
            }
        }
        return filterCompanies(companies);
    }

    public static DomainEntitySet<Company> searchCompaniesByAnything(final String searchInput) {
        int maxResults = SystemParameter.findIntValue(SystemParameter.Code.PSPUI_MAX_COMPANY_SEARCH_RESULTS, 100);

        //intuitively, using an or would be faster, but oracle won't use indexes, so this uses indexes and short circuits.
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceCompanyId().like(searchInput.toUpperCase() + "%", false)).LimitResults(0, maxResults));
        if (companies.isNotEmpty()) {
            return companies;
        }

        String searchInputOpt = searchInput.replaceAll("-", "");
        List<String> companyFedTaxIdEncList= EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,searchInputOpt);
        companies = Application.find(Company.class, new Query<Company>().Where(Company.FedTaxIdEnc().in(companyFedTaxIdEncList)).LimitResults(0, maxResults));

        if (companies.isNotEmpty()) {
            return companies;
        }

        companies = Application.find(Company.class, new Query<Company>().Where(Company.LegalName().like(searchInput + "%", true)).LimitResults(0, maxResults));
        if (companies.isNotEmpty()) {
            return companies;
        }

        companies = Application.find(Company.class, Company.IAMRealmId().equalTo(searchInput));
        if (companies.isNotEmpty()) {
            return companies;
        }

        DomainEntitySet<EntitlementUnit> entitlementsUnits =
                Application.find(EntitlementUnit.class,
                                 EntitlementUnit.ServiceKey().equalTo(searchInput)
                                                .Or(EntitlementUnit.Entitlement().LicenseNumber().equalTo(searchInput))
                                                .Or(EntitlementUnit.Entitlement().CustomerId().equalTo(searchInput)));

        DomainEntitySet<Company> euSearchCompanies = new DomainEntitySet<Company>();

        for (EntitlementUnit eu : entitlementsUnits) {
            if (!euSearchCompanies.contains(eu.getCompany())) {
                euSearchCompanies.add(eu.getCompany());
            }
        }
        return filterCompanies(euSearchCompanies);

    }

    public static List<SpcfUniqueId> findWorkforceEligibleCompanies(int maxCompanies, int lastPayrollRunDurationCompany,
                                                                    String publishStatusWorkforce, int maxRowsToFetch,
                                                                    boolean isDD) {

        lastPayrollRunDurationCompany = - lastPayrollRunDurationCompany;
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addDays(lastPayrollRunDurationCompany);

        String[] paramNames = new String[3];
        paramNames[0] = "publishStatusWorkforce";
        paramNames[1] = "fromDate";
        paramNames[2] = "isDD";

        Object[] paramValues = new Object[3];
        paramValues[0] = publishStatusWorkforce;
        paramValues[1] = fromDate;
        paramValues[2] = isDD;

        List<SpcfUniqueId> companyIds = Application.executeNamedQuery("findEligibleCompaniesforWorkforceInvitation", paramNames, paramValues, -1, maxRowsToFetch);

        return companyIds.stream().distinct().limit(maxCompanies).collect(Collectors.toList());
    }


    public static DomainEntitySet<Company> findCompaniesBySourceSystemAndService(SourceSystemCode pSourceSystemCd, ServiceCode pServiceCode) {
        String[] paramNames = new String[3];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "serviceCd";
        paramNames[2] = "excludeDeletedCompany";

        Object[] paramValues = new Object[3];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pServiceCode;
        paramValues[2] = !AuthUser.hasSAPAdminAccess();

        return Application.findByNamedQuery("findCompaniesBySourceSystemAndService", paramNames, paramValues);
    }

    public static DomainEntitySet<Company> findCompaniesBySourceSystemAndPendingTaxService(SourceSystemCode pSourceSystemCd) {
        String[] paramNames = new String[1];
        paramNames[0] = "sourceSystemCd";

        Object[] paramValues = new Object[1];
        paramValues[0] = pSourceSystemCd;

        return Application.findByNamedQuery("findCompaniesBySourceSystemAndPendingTaxService", paramNames, paramValues);
    }

    public EntitlementUnit findEnabledEntitlementUnitByAssetItemCd(AssetItemCode... pAssetItemCodes) {
        Criterion<EntitlementUnit> where =
                EntitlementUnit.Entitlement().EntitlementState().equalTo(EntitlementStateCode.Enabled)
                               .And(EntitlementUnit.Entitlement().EntitlementCode().AssetItemCd().in(pAssetItemCodes));

        DomainEntitySet<EntitlementUnit> entitlementUnits = getEntitlementUnitCollection().find(where);

        return entitlementUnits.isEmpty() ? null : entitlementUnits.get(0);
    }

    public DomainEntitySet<EntitlementUnit> getActiveEntitlementUnits() {
        return getEntitlementUnitCollection().find(EntitlementUnit.EntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES));

    }

    public DomainEntitySet<EntitlementUnit> getPrimaryEntitlementUnits() {
        return getEntitlementUnitCollection().find(EntitlementUnit.Entitlement().EntitlementCode().IsPrimary().equalTo(true));
    }

    /**
     * Returns all companies with the given pending activation substatuses (e.g. Bank verification, First Payroll)
     *
     * @param subStatuses  The ArrayList of ServiceSubStatusCodes to search for
     * @param pFirstResult index of first result
     * @param pMaxResults  max number of results
     * @return A list of distinct companies with the given substatuses
     */
    public static DomainEntitySet<Company> findCompaniesByPendingSubStatus(ArrayList<ServiceSubStatusCode> subStatuses,
                                                                           String pOrderBy,
                                                                           boolean pOrderDesc,
                                                                           int pFirstResult,
                                                                           int pMaxResults) {
        GroupByClause<Company> query = new Query<Company>().Where(getCompaniesByPendingSubStatusExpression(subStatuses));

        SortableProperty<Company, ?> sortProperty ;

        if (StringUtils.equalsIgnoreCase(pOrderBy, "sourceSystemCd")) {
            sortProperty = Company.SourceSystemCd();
        } else if (StringUtils.equalsIgnoreCase(pOrderBy, "fein")) {
            sortProperty = Company.LegalName();
        } else if (StringUtils.equalsIgnoreCase(pOrderBy, "companyId")) {
            sortProperty = Company.SourceCompanyId();
        } else {
            sortProperty = Company.LegalName();
        }

        if (pOrderDesc) {
            sortProperty = sortProperty.Descending();
        }

        return Application.find(Company.class, query.OrderBy(sortProperty, Company.SourceCompanyId(), Company.Id()).LimitResults(pFirstResult, pMaxResults));
    }

    public static long getCompaniesByPendingSubStatusCount(ArrayList<ServiceSubStatusCode> subStatuses) {
        return Application.executeScalarAggQuery(Company.class, new Query<Company>().Select(Company.Id().Count()).Where(getCompaniesByPendingSubStatusExpression(subStatuses)));
    }

    private static Criterion<Company> getCompaniesByPendingSubStatusExpression(Collection<ServiceSubStatusCode> subStatuses) {
        return Company.CompanyServiceSet().Exists(CompanyService.StatusCd().in(subStatuses))
                      .And(Company.OnHoldReasonSet().NotExists(OnHoldReason.ExpirationDate().isNull()));
    }

    /**
     * Returns all companies with the given on hold substatuses (e.g. Fraud, ACH Reject)
     *
     * @param subStatuses  The ArrayList of ServiceSubStatusCodes to search for
     * @param pFirstResult index of first result
     * @param pMaxResults  max number of results
     * @return A list of distinct companies with the given substatuses
     */
    public static DomainEntitySet<Company> findCompaniesByOnHoldSubStatus(ArrayList<ServiceSubStatusCode> subStatuses,
                                                                    String pOrderBy,
                                                                    boolean pOrderDesc,
                                                                    int pFirstResult,
                                                                    int pMaxResults) {
        GroupByClause<Company> query = new Query<Company>().Where(getCompaniesByOnHoldSubStatusExpression(subStatuses));

        SortableProperty<Company, ?> sortProperty ;

        if (StringUtils.equalsIgnoreCase(pOrderBy, "sourceSystemCd")) {
            sortProperty = Company.SourceSystemCd();
        } else if (StringUtils.equalsIgnoreCase(pOrderBy, "fein")) {
            sortProperty = Company.LegalName();
        } else if (StringUtils.equalsIgnoreCase(pOrderBy, "companyId")) {
            sortProperty = Company.SourceCompanyId();
        } else {
            sortProperty = Company.LegalName();
        }

        if (pOrderDesc) {
            sortProperty = sortProperty.Descending();
        }

        return Application.find(Company.class, query.OrderBy(sortProperty, Company.SourceCompanyId(), Company.Id()).LimitResults(pFirstResult, pMaxResults));
    }

    public static long getCompaniesByOnHoldSubStatusCount(ArrayList<ServiceSubStatusCode> subStatuses) {
        return Application.executeScalarAggQuery(Company.class, new Query<Company>().Select(Company.Id().Count()).Where(getCompaniesByOnHoldSubStatusExpression(subStatuses)));
    }

    private static Criterion<Company> getCompaniesByOnHoldSubStatusExpression(ArrayList<ServiceSubStatusCode> subStatuses) {
        //Find all OnHoldReason objects (which are attached to companies) where:
        // 1) we passed in the on hold reason code and
        // 2) the expiration date is null (e.g. the company is still on hold for this reason
        return Company.OnHoldReasonSet().Exists(OnHoldReason.ExpirationDate().isNull().And(OnHoldReason.OnHoldReasonCd().in(subStatuses)));
    }


    private static void buildFraudAddressNotes(
            DomainEntitySet<FraudAddress> pAddresses,
            SortedMap<String, Company> matchedCompanyMap,
            SortedMap<String, String> companyFieldMatches,
            String pAddressType) {
        for (FraudAddress currAddress : pAddresses) {
            String key = currAddress.getCompany().getId().toString();
            String notes = "";
            if (companyFieldMatches.containsKey(key)) {
                notes = companyFieldMatches.get(key);
            }

            matchedCompanyMap.put(key, currAddress.getCompany());
            notes += pAddressType + ": " + "\n" + currAddress.getAddressLine1() + " \n";

            if (currAddress.getAddressLine2() != null) {
                notes += currAddress.getAddressLine2() + "\n";
            }

            if (currAddress.getAddressLine3() != null) {
                notes += currAddress.getAddressLine3() + "\n";
            }

            notes += currAddress.getCity() + ", " + currAddress.getState() + " "
                    + currAddress.getZipCode() + "\n";

            companyFieldMatches.put(key, notes);
        }
    }

    public static Company getBookTransferCompany() {
        String intuitCompanyId = SystemParameter.findStringValue(SystemParameter.Code.BOOK_TRANSFER_INTUIT_COMPANY_ID);
        String ids[] = intuitCompanyId.split(":");
        if (ids.length != 2) {
            throw new RuntimeException("system parameter for value for BOOK_TRANSFER_INTUIT_COMPANY_ID is invalid");
        }
        SourceSystemCode companyCd = SourceSystemCode.valueOf(ids[0]);
        Company company = Company.findCompany(ids[1], companyCd);

        if (company == null) {
            throw new RuntimeException("BOOK TRANSFER INTUIT COMPANY is not found");
        }
        return company;

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public Company() {
        super();

        setTaxExemptStatus(com.intuit.sbd.payroll.psp.domain.TaxExemptStatusCode.New);
    }

    /*
     * Checks to see if any of the company's services are terminated.  If so, returns true; else, returns false
     *
     */
    public boolean isCompanyTerminated() {
        for (CompanyService currCompService : getCompanyServiceCollection()) {
            ServiceSubStatusCode subStatusCd = currCompService.getStatusCd();
            if (subStatusCd.equals(ServiceSubStatusCode.Terminated)) {
                return true;
            }
        }
        return false;
    }

    public long getServiceStartToken() {
        if (isCompanyOnService(ServiceCode.Tax)) {
            CompanyEvent ofxServiceActivatedEvent = CompanyEvent.findOFXServiceActivatedEvent(getCompanyService(ServiceCode.Tax));
            if (ofxServiceActivatedEvent != null) {
                String startingToken = ofxServiceActivatedEvent.getCompanyEventDetailValue(EventDetailTypeCode.OFXToken);
                if (startingToken != null) {
                    return Long.parseLong(startingToken);
                }
            }
        }
        return 0;
    }

    public String getOnHoldNotesString() {
        DomainEntitySet<OnHoldReason> onHoldReasons = getCurrentOnHoldReasonsDomainEntitySet();
        onHoldReasons = onHoldReasons.sort(OnHoldReason.OnHoldReasonCd());
        StringBuilder onHoldNotesString = new StringBuilder();
        if (onHoldReasons != null && onHoldReasons.size() > 0) {
            //if there are any on hold reasons, we'll return them in the form "(reasonName1, reasonName2, reasonname3)"
            onHoldNotesString.append("(");
            for (Iterator<OnHoldReason> iter = onHoldReasons.iterator(); iter.hasNext(); ) {
                OnHoldReason onHoldReason = iter.next();
                ServiceSubStatusCode serviceSubStatusCd = onHoldReason.getOnHoldReasonCd();
                ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, serviceSubStatusCd);
                onHoldNotesString.append(serviceSubStatus.getName());
                if (iter.hasNext()) {
                    onHoldNotesString.append(", ");
                }
            }
            onHoldNotesString.append(")");
        }
        return onHoldNotesString.toString();
    }

    public Collection<ServiceSubStatusCode> getCurrentOnHoldReasonCodes() {
        Collection<ServiceSubStatusCode> currentOnHoldReasonCodes = new ArrayList<ServiceSubStatusCode>();
        for (OnHoldReason onHoldReason : getCurrentOnHoldReasons()) {
            currentOnHoldReasonCodes.add(onHoldReason.getOnHoldReasonCd());
        }
        return currentOnHoldReasonCodes;
    }

    public Collection<OnHoldReason> getCurrentOnHoldReasons() {
        Collection<OnHoldReason> currentOnHoldReasons = new ArrayList<OnHoldReason>();
        for (OnHoldReason onHoldReason : getOnHoldReasonCollection().sort(OnHoldReason.EffectiveDate())) {
            if (null == onHoldReason.getExpirationDate()) {
                currentOnHoldReasons.add(onHoldReason);
            }
        }
        return currentOnHoldReasons;
    }

    public DomainEntitySet<OnHoldReason> getCurrentOnHoldReasonsDomainEntitySet() {
        DomainEntitySet<OnHoldReason> currentOnHoldReasons = new DomainEntitySet<OnHoldReason>();
        for (OnHoldReason onHoldReason : getOnHoldReasonCollection()) {
            if (null == onHoldReason.getExpirationDate()) {
                currentOnHoldReasons.add(onHoldReason);
            }
        }
        return currentOnHoldReasons;
    }

    /**
     * Checks if any of the contacts in the company is an account signatory
     *
     * @return boolean
     */
    public boolean hasAccountSignatoryContact() {
        for (Contact contact : getContactCollection()) {
            if (contact != null && contact.getAuthSignerYnInd()) {
                return true;
            }
        }

        return false;
    }

    public boolean isEligibleForEftps() {
        return hasService(ServiceCode.Tax);
    }

    public boolean isEligibleForRAF() {
        return hasService(ServiceCode.Tax);
    }

    /*
     * Returns the status of the company's current EFTPS enrollment, or null if no enrollment has been attempted.
     *
     */
    public EftpsEnrollmentStatus getCurrentEnrollmentStatus() {
        EftpsEnrollment current = getCurrentEnrollment();
        return (current == null) ? null : current.getStatusCd();
    }

    /*
     * Returns the company's current EftpsEnrollment entity, or null if no enrollment has been attempted.
     *
     */
    public EftpsEnrollment getCurrentEnrollment() {
        DomainEntitySet<EftpsEnrollment> enrollments = getAllEnrollments().find(EftpsEnrollment.Secondary().equalTo(false));
        if (enrollments == null || enrollments.isEmpty()) {
            return null;
        } else {
            return enrollments.get(0);
        }
    }

    /*
     * Returns the status of the company's current EFTPS enrollment, or null if no enrollment has been attempted.
     *
     */
    public ACHEnrollmentStatus getCurrentACHEnrollmentStatus() {
        ACHEnrollment currentACHEnrollment = getCurrentACHEnrollment();
        return (currentACHEnrollment == null) ? null : currentACHEnrollment.getStatus();
    }

    /*
     * Returns the company's current ACHEnrollment entity, or null if no enrollment has been attempted.
     *
     */
    public ACHEnrollment getCurrentACHEnrollment() {
        DomainEntitySet<ACHEnrollment> achEnrollments = getAllACHEnrollments();
        if (achEnrollments == null || achEnrollments.isEmpty()) {
            return null;
        } else {
            return achEnrollments.get(0);
        }
    }

    public RAFEnrollment getCurrentRAFEnrollment() {
        DomainEntitySet<RAFEnrollment> enrollments = getAllRAFEnrollments();
        if (enrollments == null || enrollments.isEmpty()) {
            return null;
        } else {
            return enrollments.get(0);
        }
    }

    public RAFEnrollmentStatus getCurrentRAFEnrollmentStatus() {
        RAFEnrollment current = getCurrentRAFEnrollment();
        return (current == null) ? null : current.getStatus();
    }

    /*
     * Returns all EftpsEnrollment entites related to the company, orted by CreatedDate in descending order newest first.
     *
     */
    public DomainEntitySet<EftpsEnrollment> getAllEnrollments() {
        CompanyAgency irsAgency = CompanyAgency.findCompanyAgency(this, Agency.IRS);
        if(irsAgency != null) {
            return irsAgency.getEftpsEnrollmentCollection().sort(EftpsEnrollment.StatusEffectiveDate().Descending());
        } else {
            return new DomainEntitySet<EftpsEnrollment>();
        }
    }

    /*
     * Returns all ACHEnrollment entities related to the company, ordered by CreatedDate in descending order newest first.
     *
     */
    public DomainEntitySet<ACHEnrollment> getAllACHEnrollments() {
        DomainEntitySet<ACHEnrollment> allACHEnrollments = getAllACHEnrollmentsIncludingCancelled();
        if (allACHEnrollments == null) {
            return null;
        }
        return allACHEnrollments.find(ACHEnrollment.Status().notEqualTo(ACHEnrollmentStatus.Cancelled));
    }

    public DomainEntitySet<ACHEnrollment> getAllACHEnrollmentsIncludingCancelled() {
        CompanyAgency flAgency = getCompanyAgencyCollection().findEntity(CompanyAgency.Agency().AgencyId().equalTo(Agency.FL_AGENT_ID));
        if(flAgency != null) {
            return flAgency.getACHEnrollmentCollection().sort(ACHEnrollment.StatusEffectiveDate().Descending());
        } else {
            return null;
        }
    }


    public DomainEntitySet<RAFEnrollment> getAllRAFEnrollments() {

        Expression<RAFEnrollment> query = new Query<RAFEnrollment>()
                .Where(RAFEnrollment.CompanyAgency().Company().equalTo(this))
                .OrderBy(RAFEnrollment.CreatedDate().Descending());

        DomainEntitySet<RAFEnrollment> enrollments = Application.find(RAFEnrollment.class, query);

        //Look in memory if we can't find enrollments going directly to the DB
        if (enrollments.size() == 0) {
            CompanyAgency irsAgency = CompanyAgency.findCompanyAgency(this, Agency.IRS);
            enrollments = irsAgency.getRAFEnrollmentCollection();
            return enrollments.sort(RAFEnrollment.StatusEffectiveDate().Descending());
        }

        return enrollments;

    }

    /**
     * Compares the current Company values for enrollment-related data with values used for the last enrollment attempt.
     *
     * @return true if any enrollment-related data value has changed, else false
     */
    public boolean enrollmentDataHasChanged() {
        EftpsEnrollment eftpsEnrollment = getCurrentEnrollment();

        if (eftpsEnrollment == null) {
            return true; // was nothing, is now something
        }

        EftpsEnrollmentDetail eftpsEnrollmentDetail = eftpsEnrollment.findEnrollmentDetail();

        if (!StringUtils.equals(eftpsEnrollmentDetail.getFedTaxId(), getFedTaxId()) ||
                !StringUtils.equals(eftpsEnrollmentDetail.getLegalName(), getLegalName())) {
            return true;
        }

        Address legalAddress = getLegalAddress();

        if (legalAddress == null) {
            return true; // current enrollment has an address (cuz it's required), company has none
        }

        //noinspection RedundantIfStatement
        if (!StringUtils.equals(eftpsEnrollmentDetail.getLegalZip(), legalAddress.getZipCode())) {
            return true; // something changed
        }

        return false; // nothing has changed
    }

    public Company setFraudFlag() {

        setIsFlaggedForFraud(true);
        return Application.save(this);
    }

    public OnHoldReason getCurrentOnHoldReason(ServiceSubStatusCode pOnHoldReasonCd) {
        for (OnHoldReason onHoldReason : getOnHoldReasonCollection()) {
            if (onHoldReason.getExpirationDate() == null && onHoldReason.getOnHoldReasonCd() == pOnHoldReasonCd) {
                return onHoldReason;
            }
        }
        return null;
    }

    public int[] getCutoffTimeElements() {
        String cutOffTimeString = getOffloadGroup().getCutoffTime();
        String[] hhMMss = cutOffTimeString.split(":");
        return new int[]{Integer.parseInt(hhMMss[0]), Integer.parseInt(hhMMss[1]), Integer.parseInt(hhMMss[2])};
    }

    public boolean isCompanyInDebtToIntuit() {
        // verify if the company is in debt to Intuit for any prior Payroll
        SpcfDecimal erReturnDebtAmt =
                LedgerAccount.getLedgerAccountBalance(this, LedgerAccountCode.ERReturnReceivable);
        SpcfDecimal badDebtAmt =
                LedgerAccount.getLedgerAccountBalance(this, LedgerAccountCode.BadDebt);

        return erReturnDebtAmt.compareTo(SpcfDecimal.createInstance("0.00")) == 1 ||
                badDebtAmt.compareTo(SpcfDecimal.createInstance("0.00")) == 1;

    }

    /**
     * For the purposes of this method, if a customer submits a payroll on a holiday, and the payroll date - the funding
     * model days results in a date that's prior to the current date (put another way, if a user submits on MLK for a
     * target of Wednesday, because of the back calculation of 2 funding model days from Wednesday, it results in a
     * proper target of friday, which is prior to the submission date which means the payroll is considered backdated)
     * PSRV001128
     *
     * @param pTargetCheckDate Targeted Checkdate
     * @return True if a backdated payroll as a result of calculating x number of business days (based on funding model)
     *         from the targeted check date
     */
    public boolean isPayrollSubmissionBackdated(SpcfCalendar pTargetCheckDate) {
        SpcfCalendar currentTime = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
        SpcfCalendar nextValidPaycheckDepositDay = pTargetCheckDate.copy();
        int fundingModelDays = getFundingModel().getNumberOfFundingDays();

        // Check if Paycheck deposit date falls on a holiday or weekend and adjust it to the next business day
        while (CalendarUtils.isWeekendOrHoliday(nextValidPaycheckDepositDay)) {
            CalendarUtils.addBusinessDays(nextValidPaycheckDepositDay, 1);
        }

        // Adjust paycheck deposit day according to the company's funding model
        SpcfCalendar limitCalendar = nextValidPaycheckDepositDay.copy();
        CalendarUtils.addBusinessDays(limitCalendar, -1 * fundingModelDays);

        // Backdated payroll
        return currentTime.after(limitCalendar);

    }

    public long getPayrollCount() {
        long payrollCount = 0;
        if (isCompanyOnService(ServiceCode.Tax)) {
            Expression<Paycheck> query = new Query<Paycheck>()
                    .Select(Paycheck.PayrollRun().Id().CountDistinct())
                    .Where(Paycheck.Company().equalTo(this)
                                   .And(Paycheck.QbdtPaycheckInfo().Company().equalTo(this))
                                   .And(Paycheck.PayrollRun().PayrollRunStatus().notIn(PayrollStatus.Superseded))
                                   .And(Paycheck.SourcePaycheckId().like("-%").Not())
                                   .And((Paycheck.PayrollRun().PayrollRunType().notIn(PayrollType.CloudOnly))
                                                .Or(Paycheck.PayrollRun().PayrollRunType().in(PayrollType.CloudOnly).And(Paycheck.PayrollRun().PayrollDirectDepositAmount().greaterThan(SpcfMoney.ZERO)))
                                                .Or(Paycheck.PayrollRun().FinancialTransactionSet().Exists(
                                                        FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound)
                                                                            .Or(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax))
                                                                            .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Voided, TransactionStateCode.Cancelled))
                                                                            .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH)).And(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO))))
                                   ));

            payrollCount = Application.executeScalarAggQuery(Paycheck.class, query);
        } else {
            payrollCount += Application.executeScalarAggQuery(PayrollRun.class, new Query<PayrollRun>()
                    .Select(PayrollRun.Id().CountDistinct())
                    .Where(PayrollRun.Company().equalTo(this)
                                     .And(PayrollRun.PayrollRunStatus().notIn(PayrollStatus.Superseded))
                                     .And(PayrollRun.PaycheckSet().NotExists(Paycheck.SourcePaycheckId().like("-%")))
                                     .And((PayrollRun.PayrollRunType().notIn(PayrollType.CloudOnly))
                                                  .Or(PayrollRun.PayrollRunType().in(PayrollType.CloudOnly).And(PayrollRun.PayrollDirectDepositAmount().greaterThan(SpcfMoney.ZERO)))
                                                  .Or(PayrollRun.FinancialTransactionSet().Exists(
                                                          FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound)
                                                                              .Or(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax))
                                                                              .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Voided, TransactionStateCode.Cancelled))
                                                                              .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH)).And(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO))))
                                     )));
        }
        if (getQuickbooksInfo() != null) {
            payrollCount += getQuickbooksInfo().getAS400PayrollCount();
        }
        return payrollCount;
    }

    /*
     * This method will check for the following occurrences for all companies in
     * the database where the status's are Terminated or Hold: 1. Company Legal
     * Name 2. Company DBA Name 3. Company Email 4. Address 5. Contact First or
     * Last Name 6. Contact Phone Number 7. Company QB Reg num 8. Company Agreement num
     * <p/>
     * pCompanyNotes is an "out" parameter and must be initialized before calling this method
     *
     */
    public boolean companyMeetsFraudCriteria(
            StringBuilder pCompanyNotes) {
        boolean exceptionState = false;

        SortedMap<String, Company> matchedCompanyMap = new TreeMap<String, Company>();
        SortedMap<String, String> companyFieldMatches = new TreeMap<String, String>();

        findSimilarFraudCompanies(matchedCompanyMap, companyFieldMatches);
        //findSimilarFraudAddresses(matchedCompanyMap, companyFieldMatches);
        //findSimilarFraudContacts(matchedCompanyMap, companyFieldMatches);

        // If any companies matched, assemble the company notes entry
        if (matchedCompanyMap.size() > 0) {
            exceptionState = true;
            /* Assemble the company notes */
            Set<String> set = matchedCompanyMap.keySet();
            for (String key : set) {
                Company company = matchedCompanyMap.get(key);
                String compStatus;
                if (company.isCompanyTerminated()) {
                    compStatus = "Terminated";
                } else {
                    compStatus = "On Hold " + getOnHoldNotesString();
                }

                pCompanyNotes.append("This company was not activated because one or more fields match the company,  ");
                pCompanyNotes.append(company.getLegalName());
                pCompanyNotes.append("(Source System=");
                pCompanyNotes.append(company.getSourceSystemCd());
                pCompanyNotes.append(" Source ID=");
                pCompanyNotes.append(company.getSourceCompanyId());
                pCompanyNotes.append(") with status of ");
                pCompanyNotes.append(compStatus);
                pCompanyNotes.append(".  The list of fields matched are as follows:\n");
                pCompanyNotes.append(companyFieldMatches.get(key));
            }
        }
        if (pCompanyNotes.length() > MAX_LENGTH_NOTES) {
            pCompanyNotes.setLength(MAX_LENGTH_NOTES);
        }

        return exceptionState;
    }

    public void findSimilarFraudContacts(
            SortedMap<String, Company> matchedCompanyMap,
            SortedMap<String, String> companyFieldMatches) {
        // Get all contacts with matching phone numbers
        // Get all contacts with matching contacts names
        // Get all contacts with matching email addresses

        List<Contact> contacts = new ArrayList<Contact>(getContactCollection());
        Collections.sort(contacts, new Comparator<Contact>() {
            public int compare(Contact pContact1, Contact pContact2) {
                int contact1Priority = getRolePriority(pContact1.getContactRoleCd());
                int contact2Priority = getRolePriority(pContact2.getContactRoleCd());

                if(contact1Priority < contact2Priority) {
                    return -1;
                } else if(contact1Priority > contact2Priority) {
                    return 1;
                } else {
                    return 0;
                }
            }

            private int getRolePriority(ContactRole pContactRole) {
                if(pContactRole == null) {
                    return 10;
                }

                switch (pContactRole) {
                    case PayrollAdmin:
                        return 0;
                    case PrimaryPrincipal:
                        return 1;
                    case SecondaryPrincipal:
                        return 2;
                    case Other:
                        return 3;
                    default:
                        return 5;
                }
            }
        });

        for (Contact contact : contacts) {
            List<FraudContact> similarContacts = contact.findFraudContactsLike();
            Company.logger.debug("Number of contacts matched:" + similarContacts.size());
            //Iterate through each of the returned contacts
            for (FraudContact currContact : similarContacts) {
                Company company = currContact.getCompany();
                //Add the company to the matched map
                String notes = "";
                String key = company.getId().toString();
                if (companyFieldMatches.containsKey(key)) {
                    notes = companyFieldMatches.get(key);
                }
                matchedCompanyMap.put(key, company);

                if (currContact.getFirstName().equalsIgnoreCase(contact.getFirstName())
                        && currContact.getLastName().equalsIgnoreCase(contact.getLastName())) {
                    notes += "CONTACT NAME:" + contact.getFirstName() + " " + contact.getLastName() + "\n";
                }

                // Only check for a phone match if both contacts have one
                if (currContact.getPhone() != null && contact.getPhone() != null) {
                    if (currContact.getPhone().equalsIgnoreCase(contact.getPhone())) {
                        notes += "CONTACT PHONE:" + contact.getPhone() + "\n";
                    }
                }

                // Only check for a email match if both contacts have one
                if (currContact.getEmail() != null && contact.getEmail() != null) {
                    if (currContact.getEmail().equalsIgnoreCase(contact.getEmail())) {
                        notes += "CONTACT EMAIL:" + contact.getEmail() + "\n";
                    }
                }
                logger.info(notes);
                companyFieldMatches.put(key, notes);
            }
        }
    }

    public void findSimilarFraudAddresses(
            SortedMap<String, Company> matchedCompanyMap,
            SortedMap<String, String> companyFieldMatches) {
        // Iterate through each address within the source company BO
        DomainEntitySet<FraudAddress> addressesLikeLegal = null;
        if (getLegalAddress() != null) {
            addressesLikeLegal = FraudAddress.findFraudAddressesLike(getLegalAddress());
        }

        DomainEntitySet<FraudAddress> addressesLikeMailing = null;
        if (getMailingAddress() != null) {
            addressesLikeMailing = FraudAddress.findFraudAddressesLike(getMailingAddress());
        }

        if (addressesLikeLegal != null && addressesLikeLegal.size() > 0) {
            Company.buildFraudAddressNotes(addressesLikeLegal, matchedCompanyMap, companyFieldMatches,
                                           LEGAL_ADDRESS);
        }
        if (addressesLikeMailing != null && addressesLikeMailing.size() > 0) {
            Company.buildFraudAddressNotes(addressesLikeMailing, matchedCompanyMap, companyFieldMatches,
                                           MAILING_ADDRESS);
        }
    }

    public void findSimilarFraudCompanies(
            SortedMap<String, Company> matchedCompanyMap,
            SortedMap<String, String> companyFieldMatches) {

        // Get all BO's with matching legal, DBA names, qb registration numbers, agreement numbers, or email
        DomainEntitySet<FraudCompany> fraudCompanies = findMatchingCompaniesByFraudCriteria(this);

        for (FraudCompany fraudCompany : fraudCompanies) {
            Company c = fraudCompany.getCompany();

            String key = c.getId().toString();

            String notes = "";
            if (companyFieldMatches.containsKey(key)) {
                notes = companyFieldMatches.get(key);
            }
            matchedCompanyMap.put(key, c);

            if (getFedTaxId() != null && getFedTaxId().equalsIgnoreCase(c.getFedTaxId())) {
                notes += "FED TAX ID: " + getFedTaxId() + "\n";
            }

            companyFieldMatches.put(key, notes);
        }

    }


    public Contact getContact(final String pContactId) {
        Contact foundContact = null;
        DomainEntitySet<Contact> contacts = Application.find(Contact.class,
                                                             Contact.Company().equalTo(this)
                                                                    .And(Contact.SourceContactId().equalTo(pContactId)));
        if (contacts.size() > 1) {
            throw new RuntimeException("Query for contact by ContactId " + pContactId + " and company " + this + " did not return 0 or 1 results as expected");
        }

        if (!contacts.isEmpty()) {
            foundContact = contacts.get(0);
        }

        return foundContact;
    }


    /**
    @deprecated dangerous method--some "Cloud" employees are Assisted and have DD
     */
    @Deprecated
    public DomainEntitySet<Employee> getDirectDepositEmployees() {
        Expression<Employee> query = new Query<Employee>().Where(Employee.Company().equalTo(this))
                                                        .EagerLoad(Employee.QbdtEmployeeInfo());

        DomainEntitySet<Employee> eeSet = new DomainEntitySet<Employee>();
        for (Employee ee : Application.find(Employee.class, query)) {
            if (ee.getQbdtEmployeeInfo() == null || !ee.getQbdtEmployeeInfo().getIsAssisted()) {
                eeSet.add(ee);
            }
        }
        return eeSet;
    }

    public DomainEntitySet<Employee> getEmployees() {
        return Application.find(Employee.class, Employee.Company().equalTo(this));
    }

    /**
     @deprecated dangerous method--"Cloud employee" concept largely meaningless.  Will be cloud if Assisted OFX or QBDTWS, but not if DD OFX.
     */
    @Deprecated
    public DomainEntitySet<Employee> getCloudEmployees(Property<? super Employee, ?>... eagerLoadPaths) {
        Expression<Employee> query =
                new Query<Employee>()
                        .Where(Employee.Company().equalTo(this)
                                       .And(Employee.QbdtEmployeeInfo().IsAssisted().equalTo(true)));

        if (eagerLoadPaths != null && eagerLoadPaths.length > 0) {
            query = ((GroupByClause<Employee>) query).EagerLoad(eagerLoadPaths);
        }

        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        for (Employee employee : employees) {
            employee.cache();
        }

        return employees;
    }


    public DomainEntitySet<FinancialTransaction> getFinancialTransactions() {
        return Application.find(FinancialTransaction.class, FinancialTransaction.Company().equalTo(this));
    }

    public DomainEntitySet<MoneyMovementTransaction> findPendingTaxPayments() {
        return MoneyMovementTransaction.findTaxPayments().setCompany(this).setPending().find();
    }

    /*
     * Adds an On Hold Reason to a company. Some financial transactions may be put on hold due to the new On Hold Reason.
     *
     */
    public OnHoldReason addOnHoldReason(ServiceSubStatusCode pOnHoldReasonCd) {
        ServiceSubStatus onHoldStatus = Application.findById(ServiceSubStatus.class, pOnHoldReasonCd);
        if (onHoldStatus.getServiceStatus().getServiceStatusCd() != ServiceStatusCode.OnHold) {
            throw new RuntimeException("Not an OnHold service status: " + pOnHoldReasonCd);
        }

        OnHoldReason onHoldReason = getCurrentOnHoldReason(pOnHoldReasonCd);
        if (onHoldReason != null) {
            return onHoldReason;
        }

        Collection<ServiceSubStatusCode> oldOnHoldReasonCodes = getCurrentOnHoldReasonCodes();

        onHoldReason = new OnHoldReason();
        onHoldReason.setEffectiveDate(PSPDate.getPSPTime());
        onHoldReason.setOnHoldReasonCd(pOnHoldReasonCd);
        onHoldReason.setCompany(this);
        onHoldReason = Application.save(onHoldReason);
        addOnHoldReason(onHoldReason);
        onHoldReason = Application.save(onHoldReason);

        Collection<ServiceSubStatusCode> newOnHoldReasonCodes = getCurrentOnHoldReasonCodes();

        CompanyEvent.createServiceStatusChangeEvent(
                this,
                oldOnHoldReasonCodes,
                newOnHoldReasonCodes,
                PSPDate.getPSPTime());

        Application.save(this);

        return onHoldReason;
    }

    /*
     * Removes an On Hold Reason from a company. Also removes some financial transactions from on hold that were
     * on hold due to the same On Hold Reason.
     *
     */
    public OnHoldReason removeOnHoldReason(ServiceSubStatusCode pOnHoldReasonCd) {
        ServiceSubStatus onHoldStatus = Application.findById(ServiceSubStatus.class, pOnHoldReasonCd);
        if (onHoldStatus.getServiceStatus().getServiceStatusCd() != ServiceStatusCode.OnHold) {
            throw new RuntimeException("Not an OnHold service status: " + pOnHoldReasonCd);
        }

        // Expire OnHoldReason
        OnHoldReason onHoldReason = getCurrentOnHoldReason(pOnHoldReasonCd);
        if (onHoldReason == null) {
            return null;
        }

        Collection<ServiceSubStatusCode> oldOnHoldReasonCodes = getCurrentOnHoldReasonCodes();

        onHoldReason.setExpirationDate(PSPDate.getPSPTime());
        onHoldReason = Application.save(onHoldReason);

        Collection<ServiceSubStatusCode> newOnHoldReasonCodes = getCurrentOnHoldReasonCodes();

        CompanyEvent.createServiceStatusChangeEvent(
                this,
                oldOnHoldReasonCodes,
                newOnHoldReasonCodes,
                PSPDate.getPSPTime());

        //todo_rhn: this logic is a candidate for RemoveOnHoldStatusCore
        if (newOnHoldReasonCodes.isEmpty()) {
            FraudCompany.removeFraudRecords(this);
        }

        // Update OnHold status on financial transactions
        TransactionState createdTxnState = Application.findById(TransactionState.class, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> pendingFinancialTransactions = null;

        boolean enableRemoveOnHoldNewFlow = FeatureFlags.get().booleanValue(FeatureFlags.Key.REMOVE_ONHOLD_NEW_FLOW, false);

        if(enableRemoveOnHoldNewFlow) {
            logger.info("REMOVE_ONHOLD_NEW_FLOW is on, going to new flow for PSID = "+this.getSourceCompanyId());
            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                            .Where(FinancialTransaction.Company().equalTo(this)
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(createdTxnState))
                                    .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH).Or(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.EFE)))
                                    .And(FinancialTransaction.OnHold().equalTo(true)))
                            .OrderBy(FinancialTransaction.TransactionType().Descending());
            pendingFinancialTransactions = Application.find(FinancialTransaction.class, query);
        }else{
            logger.info("REMOVE_ONHOLD_NEW_FLOW is off, going to default flow  for PSID = "+this.getSourceCompanyId());
            pendingFinancialTransactions = Application.find(FinancialTransaction.class,FinancialTransaction.Company().equalTo(this)
                    .And(FinancialTransaction.CurrentTransactionState().equalTo(createdTxnState))
                    .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH).Or(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.EFE)))
                    .And(FinancialTransaction.OnHold().equalTo(true)));
        }

        Set<PayrollRun> payrollRunSet = new HashSet<>();
        for (FinancialTransaction txn : pendingFinancialTransactions) {
            // check if the transaction is not offloadable for the current onHold status
            boolean bShouldPutTxnTypeOnHold = TransactionType.isExcludedFromOffload(txn.getTransactionType(), txn.getSku(), onHoldStatus, txn.getBillingDetail());
            if (bShouldPutTxnTypeOnHold && txn.getCurrentTransactionState().equals(createdTxnState)) {
                // if no other active onholds exists for the Financial Transaction remove the on hold status
                if (txn.getCurrentOnHoldReasons().size() == 0) {
                    Company.logger.debug(String.format("Transaction of type %s with id %s for company %s:%s was removed from on hold",
                                                       txn.getTransactionType().getTransactionTypeCd(), txn.getId(),
                                                       getSourceSystemCd(), getSourceCompanyId()));
                    txn.updateOnHold(false);
                    if (txn.getPayrollRun() != null)
                        payrollRunSet.add(txn.getPayrollRun());
                    Application.save(txn);
                }
            }
        }
        //if removing fraud review, deactivate all signup fraud events
        if (pOnHoldReasonCd == ServiceSubStatusCode.FraudReview) {
            FraudEvent.deactiveCompanyFraudEvents(this, FraudEventCategory.SignUp);
        }

        Application.save(this);
        return onHoldReason;
    }

    public boolean isAllowedCapability(SystemCapabilityCode pSystemCapabilityCd) {
        return isAllowedCapability(pSystemCapabilityCd, null);
    }

    /*
     * Checks if the given capability is allowed for a company.
     * A capability is allowed for a company if any of the services allows a given capability.
     * If company is On Hold, a capability is allowed only if all active On Hold Reasons allow it.
     *
     */
    public boolean isAllowedCapability(SystemCapabilityCode pSystemCapabilityCd, ServiceCode pServiceToTestFor) {
        DomainEntitySet<CompanyService> companyServices = getCompanyServiceCollection();
        SystemCapability systemCapability = Application.findById(SystemCapability.class, pSystemCapabilityCd);
        Service service = null;
        if (pServiceToTestFor != null) {
            service = Application.findById(Service.class, pServiceToTestFor);
        }
        boolean isAllowedCapability;

        // If there are no services associated with a company and they are attempting to add a service, always allow them to do so
        if (!isCompanyTerminated() && pSystemCapabilityCd == SystemCapabilityCode.AddService) {
            isAllowedCapability = true;
        } else {
            if (isCompanyOnHold()) {
                // The capability is allowed if all On Hold Reasons allow it
                boolean allReasonsAllowIt = true;
                for (OnHoldReason onHoldReason : getCurrentOnHoldReasons()) {
                    if (onHoldReason.getExpirationDate() == null) {
                        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReason.getOnHoldReasonCd());

                        if (serviceSubStatus.getServiceSet().contains(service) || service == null) {
                            allReasonsAllowIt = serviceSubStatus.getSystemCapabilityCollection().contains(systemCapability);
                        }

                        if (!allReasonsAllowIt) {
                            // We've found an On Hold Reason that doesn't allow the capability, there is no point in looking further
                            break;
                        }
                    }
                }
                isAllowedCapability = allReasonsAllowIt;
            } else {
                // The capability is allowed if any of the services allows it
                boolean anyServiceAllowsIt = false;
                for (CompanyService currCompanyService : companyServices) {
                    // query svc table first
                    if (currCompanyService.getService().getServiceCd().equals(pServiceToTestFor)) {
                        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, currCompanyService.getStatusCd());
                        if (!serviceSubStatus.getServiceSet().contains(service)) {
                            return true;
                        }
                    }

                    anyServiceAllowsIt = currCompanyService.isAllowedCapability(pSystemCapabilityCd);
                    if (anyServiceAllowsIt) {
                        break;
                    }
                }

                isAllowedCapability = anyServiceAllowsIt;
            }
        }

        return isAllowedCapability;
    }

    /*
     * Checks if company has any active On Hold Reasons.
     *
     */
    public boolean isCompanyOnHold() {
        return getCurrentOnHoldReasons().size() > 0;
    }

    public boolean isCompanyOnAS400HoldOnly() {
        //If the company's only got one hold and they have an existing AS400 hold, they are only on hold from the AS400
        return getCurrentOnHoldReasons().size() == 1 && getExistingAS400Hold() != null;
    }

    /*
     * Checks if a PIN has been created for the Company
     *
     */
    public boolean isPINCreated() {

        DomainEntitySet<CompanyEvent> pinCreatedEvents = CompanyEvent.findCompanyEvents(this, EventTypeCode.PINCreated, CompanyEventStatus.Active, null, null);
        return (pinCreatedEvents.size() > 0) || (getCompanyPINCollection().size() > 0);
    }

    public Contact getContactByRoleCode(final ContactRole pContactRoleCd) {
        Contact foundContact = null;

        DomainEntitySet<Contact> contacts = getContactsByRoleCode(pContactRoleCd);
        if (contacts.size() > 1) {
            throw new RuntimeException("Query for contact by ContactId " + pContactRoleCd + " and company " + this + " did not return 0 or 1 results as expected");
        }

        if (!contacts.isEmpty()) {
            foundContact = contacts.get(0);
        }

        return foundContact;
    }

    public DomainEntitySet<Contact> getContactsByRoleCode(final ContactRole pContactRoleCd) {
        return Application.find(Contact.class,
                                Contact.Company().equalTo(this)
                                       .And(Contact.ContactRoleCd().equalTo(pContactRoleCd)));
    }

    /*
     * Checks to see if all of the company's services are Cancelled.  If so, returns true; else, returns false
     *
     */
    public boolean isCompanyCancelled() {
        boolean companyCancelled = false;
        for (CompanyService currCompService : getCompanyServiceCollection()) {
            ServiceSubStatusCode subStatusCd = currCompService.getStatusCd();
            if (subStatusCd.equals(ServiceSubStatusCode.Cancelled)) {
                companyCancelled = true;
            } else {
                return false;
            }
        }
        return companyCancelled;
    }

    /*
     * Checks the "main" services to see if ALL are cancelled or ANY are termed.
     * When that is the case, additional validation is performed for certain actions (returns true)
     */
    private boolean servicesRequireAdditionalCancelTermValidation(boolean pNeedLoosenForDIY) {
        if (pNeedLoosenForDIY) {
            return hasService(ServiceCode.Tax) &&
                    (getService(ServiceCode.Tax).getStatusCd() == ServiceSubStatusCode.Terminated
                    || (getService(ServiceCode.Tax).getStatusCd() == ServiceSubStatusCode.Cancelled
                    && (!hasService(ServiceCode.DirectDeposit) || hasCancelledService(ServiceCode.DirectDeposit))));
        } else {
            return servicesRequireAdditionalCancelTermValidation();
        }
    }

    private boolean servicesRequireAdditionalCancelTermValidation() {
        DomainEntitySet<CompanyService> mainServices = getCompanyServiceCollection()
                .find(CompanyService.Service().ServiceCd().in(ServiceCode.DirectDeposit, ServiceCode.Tax));

        if (mainServices.size() == 0) {
            return hasService(ServiceCode.ThirdParty401k) &&
                    (getService(ServiceCode.ThirdParty401k).getStatusCd() == ServiceSubStatusCode.Cancelled || getService(ServiceCode.ThirdParty401k).getStatusCd() == ServiceSubStatusCode.Terminated);
        }

        boolean companyCancelled = true;
        for (CompanyService companyService : mainServices) {
            if (companyService.getStatusCd() == ServiceSubStatusCode.Terminated) {
                return true;
            } else if (companyService.getStatusCd() != ServiceSubStatusCode.Cancelled) {
                companyCancelled = false;
            }
        }
        return companyCancelled;
    }

    /**
     * @param customerAllowed can the customer make this change (via EWS, QBDT, etc.)
     * @param agentAllowed    can an agent make this change (via SAP)
     * @param systemAllowed   can the system make this change (via batch jobs, including AS/400 sync [including agent change on AS/400])
     * @return true if passes extra validation or not needed
     */
    public boolean passesAdditionalCancelTermValidation(boolean customerAllowed, boolean agentAllowed, boolean systemAllowed) {
        return passesAdditionalCancelTermValidation(customerAllowed, agentAllowed, systemAllowed, false);
    }

    public boolean passesAdditionalCancelTermValidation(boolean customerAllowed, boolean agentAllowed, boolean systemAllowed, boolean pNeedLoosenForDIY) {
        if (!servicesRequireAdditionalCancelTermValidation(pNeedLoosenForDIY)) {
            return true;
        }
        if (Application.getCurrentPrincipal().isCustomer()) {
            if (!customerAllowed) {
                return false;
            }
        } else if (Application.getCurrentPrincipal().isAgent()) {
            if (!agentAllowed) {
                return false;
            }
            AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
            boolean canCreateTxn = foundUser.hasOperation(OperationId.UpdateCancelTermCompany);
            if (!canCreateTxn) {
                return false;
            }
        } else {
            if (!systemAllowed) {
                return false;
            }
        }
        return true;
    }

    /*
     * Just because this method returns true does not mean that the bank account is not active.  It just means that it has been
     * deactivated at some point in the past
     */
    public boolean deactivatedCBAExistsForSourceBankAccountId(String pSourceBankAccountId) {
        return CompanyBankAccount.findDeactivatedCompanyBankAccounts(this, pSourceBankAccountId).size() > 0;
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(Company.class, getSourceSystemCd(), getSourceCompanyId());
    }

    public SpcfCalendar getExpectedReversalDate() {
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(getOffloadGroup());
        int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);
        CalendarUtils.addBusinessDays(settlementDate, achWaitPeriodDays);
        return settlementDate;
    }

    public Collection<OnHoldReason> getExpiredOnHoldReasons() {
        Collection<OnHoldReason> expiredOnHoldReasons = new ArrayList<OnHoldReason>();
        for (OnHoldReason onHoldReason : getOnHoldReasonCollection()) {
            if (onHoldReason.getExpirationDate() != null) {
                expiredOnHoldReasons.add(onHoldReason);
            }
        }
        return expiredOnHoldReasons;
    }

    /**
     * Function to return the status of the company across the services.
     *
     * @return ServiceSubStatusCode
     */
    public ServiceSubStatusCode getCompanyStatus() {
        //TODO: we need to refactor this method once we get the clear requirements for statuses in v2
        ServiceSubStatusCode serviceSubStatusCode = null;

        //If all of the company's services are Cancelled then return CompanyStatus as 'Cancelled'
        if (isCompanyCancelled()) {
            return ServiceSubStatusCode.Cancelled;
        }
        //If any of the company's services are terminated then return CompanyStatus as 'Terminated'
        else if (isCompanyTerminated()) {
            return ServiceSubStatusCode.Terminated;
        } else {
            //If any of the company's services high level status is 'Active', then return the first company service's
            //service substatus code
            for (CompanyService currCompService : getCompanyServiceCollection()) {
                ServiceStatusCode serviceStatusCode = CompanyService.getServiceStatus(currCompService.getStatusCd());
                if (serviceStatusCode.equals(ServiceStatusCode.Active)) {
                    return currCompService.getStatusCd();
                }
            }
        }

        //If none of the above criteria's are met then all of the company's services are in 'Pending Activation' status.
        //So return the first company service's substatus code.
        if (getCompanyServiceCollection() != null && getCompanyServiceCollection().size() > 0) {
            serviceSubStatusCode = getCompanyServiceCollection().iterator().next().getStatusCd();
        }

        return serviceSubStatusCode;
    }

    public CompanyService getCompanyService(ServiceCode pServiceCd) {
        for (CompanyService companyService : getCompanyServiceCollection()) {
            if (companyService.getService().getServiceCd() == pServiceCd) {
                return companyService;
            }
        }
        return null;
    }

    public boolean isCompanyHold(ServiceSubStatusCode pOnHoldReason) {
        return getCurrentOnHoldReason(pOnHoldReason) != null;
    }

    /**
     * Claims an offer for a company.  Creates and saves the CompanyOffer entity relating the Company to the Offer.
     * If the company has already claimed this offer, this method returns the existing CompanyOffer unchanged.
     *
     * @param pOffer offer to claim
     * @return the CompanyOffer that is created to relate the Company and Offer, or the existing one
     */
    public CompanyOffer claimOfferForCompany(Offer pOffer, SpcfCalendar pExpirationDate) {
        // first, see if this offer is already claimed by this company.  If so, then if the expiration
        // date is populated, return the offer with the new expiration date
        CompanyOffer claimed = getClaimedOffer(pOffer);
        if (claimed != null) {
            if (pExpirationDate != null) {
                claimed.setEndDate(pExpirationDate);
                claimed = Application.save(claimed);
            }
            return claimed;
        } // else offer not yet claimed, so proceed

        CompanyOffer co = new CompanyOffer();
        co.setCompany(this);
        co.setOffer(pOffer);

        CompanyEvent.createOfferClaimedEvent(this, pOffer.getOfferCd());

        // props that depend on the offer's begin-event
        switch (pOffer.getBeginEvent()) {
            case SignupEvent:
            case ActivationEvent:
            case RedemptionEvent:
                // set the begin-date to the later of today or the offer's effective-date
                SpcfCalendar today = PSPDate.getPSPTime();
                if (pOffer.getEffectiveDate().toLocal().compareTo(today) > 0)
                    co.setBeginDate(pOffer.getEffectiveDate().toLocal());
                else
                    co.setBeginDate(today);
                break;

            case FirstUseEvent:
                // begin-date will be the day they submit their first payroll...
                // don't know when that will be, so don't assign it
                break;

            default:
                throw new RuntimeException("Unexpected value for Offer.BeginEvent (" + pOffer.getBeginEvent() + ") in OfferBE.claim()");
        }

        // props that depend on the offer's end-event...
        switch (pOffer.getEndEvent()) {
            case DateEvent:
                if (pExpirationDate == null) {
                    co.setEndDate(pOffer.getEndDate().toLocal());
                } else {
                    co.setEndDate(pExpirationDate.toLocal());
                }
                co.setUsagesRemaining(0);
                break;

            case DurationEvent:
                if (pExpirationDate == null) {
                    if (co.getBeginDate() != null) {
                        co.setEndDate(co.getBeginDate().toLocal().copy());
                        co.getEndDate().addDays(pOffer.getDurationDays());
                    }
                } else {
                    co.setEndDate(pExpirationDate.toLocal());
                }
                co.setUsagesRemaining(0);
                break;

            case PayrollUsageEvent:
                co.setEndDate(null);
                co.setUsagesRemaining(pOffer.getUsagesAllowed());
                break;

            default:
                throw new RuntimeException("Unexpected value for Offer.EndEvent (" + pOffer.getEndEvent() + ") in OfferBE.claim()");
        }

        // all done
        co = Application.save(co);

        NaturalKey naturalKey = new NaturalKey(CompanyOffer.class, getId());
        Application.getSessionCache().addPrimaryKey(naturalKey, co.getId());

        return co;
    }

    public CompanyOffer claimOfferForCompany(Offer pOffer) {
        return claimOfferForCompany(pOffer, null);
    }

    /*
     * Cancels a Company's use of an Offer by deleting the Company's association with the Offer.  If the given Company
     * has not claimed the given Offer, this method does nothing.
     *
     */
    public void cancelOfferForCompany(Offer pOffer) {
        // get CompanyOffer for company and offer
        CompanyOffer claimed = getClaimedOffer(pOffer);
        if (claimed != null) {
            CompanyEvent.createOfferRemovedEvent(this, pOffer.getOfferCd());
            Application.delete(claimed);
        } // else that offer wasn't claimed by that company, so do nothing
    }

    /**
     * Finds an offer claimed by the company that is applicable to the given charge, breaking any ties.
     *
     * @param pCharge charge to find offer by
     * @return The offer, or null if no offer qualifies.
     */
    public Offer getApplicableOffer(OfferingServiceCharge pCharge) {
        // get offers claimed by company
        DomainEntitySet<CompanyOffer> claimed = getCompanyOffers();

        // test each to see whether it's active and applies to the charge
        CompanyOffer coBest = null;
        for (CompanyOffer co : claimed) {
            if (co.companyOfferIsActive() && co.getOffer().offerIsApplicable(pCharge)) {
                // this is the tie-breaking test
                if (coBest == null || (co.getBeginDate() != null && co.getBeginDate().before(coBest.getBeginDate()))) {
                    coBest = co;
                }
            }
        }

        if (coBest == null)
            return null;
        else
            return coBest.getOffer();
    }

    public CompanyOffering getOffering(ServiceCode pServiceCode) {
        if (pServiceCode == null) {
            return null;
        }

        for (CompanyOffering companyOffering : getCompanyOfferingCollection()) {
            if (companyOffering.getOffering().getServiceCode().equals(pServiceCode)) {
                return companyOffering;
            }
        }

        Expression<CompanyOffering> query =
                new Query<CompanyOffering>()
                        .Where(CompanyOffering.Company().equalTo(this)
                                              .And(CompanyOffering.Offering().ServiceCode().equalTo(pServiceCode)));

        DomainEntitySet<CompanyOffering> companyOfferings = Application.find(CompanyOffering.class, query);
        if (companyOfferings.size() > 1) {
            throw new RuntimeException("Did not find 0 or one company offerings for service as expected: " + this.getId() + " " + pServiceCode);
        } else if (companyOfferings.size() == 1) {
            return companyOfferings.get(0);
        } else {
            return null;
        }
    }

    /*
     * Gets offers claimed by a company
     *
     */
    public DomainEntitySet<CompanyOffer> getCompanyOffers() {
        // get offers claimed by company
        DomainEntitySet<CompanyOffer> claimed;

        NaturalKey naturalKey = new NaturalKey(CompanyOffer.class, getId());
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            claimed = new DomainEntitySet<CompanyOffer>();
            claimed.add(Application.findById(CompanyOffer.class, primaryKey));
            return claimed;
        }

        claimed = Application.find(CompanyOffer.class, CompanyOffer.Company().equalTo(this));
        return claimed;
    }

    /*
     * Get the CompanyOffer for the given Company and Offer, or null if the company has not claimed that offer.
     *
     */
    public CompanyOffer getClaimedOffer(Offer pOffer) {
        DomainEntitySet<CompanyOffer> claimed =
                Application.find(CompanyOffer.class,
                                 CompanyOffer.Company().equalTo(this)
                                             .And(CompanyOffer.Offer().equalTo(pOffer)));
        for (Iterator<CompanyOffer> iterator = claimed.iterator(); iterator.hasNext(); ) {
            CompanyOffer compOffer = iterator.next();
            if(!Application.getHibernateSession().contains(compOffer)){
                iterator.remove();
            }
        }
        return claimed.getFirst();
    }

    /**
     * Iterate through a company's CompanyService collection and return the first CompanyService matching
     * the specified service code.  (This method ITERATES through the company instance's collection and
     * therefore is most useful when these collectoins have been eager fetched.  Otherwise, use a finder method.
     *
     * @param serviceCode service to find
     * @return the CompanyService matching the serviceCode if found, null otherwise
     */
    public CompanyService getService(ServiceCode serviceCode) {
        for (CompanyService companyService : getCompanyServiceCollection()) {
            if (companyService.getService().getServiceCd() == serviceCode) {
                return companyService;
            }
        }

        return null;
    }

    public boolean hasService(ServiceCode pServiceCode) {
        return getService(pServiceCode) != null;
    }

    //on any service
    public boolean isCompanyOnService() {
        return isCompanyOnService(ServiceCode.values());
    }

    public boolean isCompanyOnService(ServiceCode... pServiceCodes) {
        for (ServiceCode serviceCode : pServiceCodes) {
            if (isCompanyOnService(serviceCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCompanyOnService(ServiceCode pServiceCode) {
        CompanyService companyService = CompanyService.findCompanyService(this, pServiceCode);
        return companyService != null && companyService.getStatusCd().notIn(ServiceSubStatusCode.Cancelled, ServiceSubStatusCode.Terminated);
    }

    public boolean hasCancelledService(ServiceCode pServiceCode) {
        CompanyService companyService = CompanyService.findCompanyService(this, pServiceCode);
        return companyService != null && companyService.getStatusCd().in(ServiceSubStatusCode.Cancelled, ServiceSubStatusCode.Terminated);

    }

    public boolean isCompanyOnTerminatedService(ServiceCode pServiceCode) {
        CompanyService companyService = CompanyService.findCompanyService(this, pServiceCode);
        return companyService != null && companyService.getStatusCd().in(ServiceSubStatusCode.Terminated);
    }

    //Active here meaning not cancelled, not termed, and _not pending_, but could be on hold
    public boolean isCompanyOnActiveService(ServiceCode pServiceCode) {
        return isCompanyOnService(pServiceCode) && !CompanyService.findCompanyService(this, pServiceCode).isPending();
    }

    public boolean doesPSPMoveMoneyFor() {
        for (CompanyService companyService : getCompanyServiceCollection()) {
            if (hasService(companyService.getService().getServiceCd()) && companyService.getService().doesPSPMoveMoneyForService()) {
                return true;
            }
        }
        return false;
    }

    public SpcfCalendar getNextValidPaycheckDepositDate(SpcfCalendar pTargetCheckDate) {
        SpcfCalendar currentTime = PSPDate.getPSPTime();
        SpcfCalendar nextValidPaycheckDepositDay = pTargetCheckDate.copy();
        int fundingModelDays = getFundingModel().getNumberOfFundingDays();

        OffloadGroup offloadGroup = getOffloadGroup();

        // Check if Paycheck deposit date falls on a holiday or weekend and adjust it to the next business day
        while (CalendarUtils.isWeekendOrHoliday(nextValidPaycheckDepositDay)) {
            CalendarUtils.addBusinessDays(nextValidPaycheckDepositDay, 1);
        }

        // Adjust paycheck deposit day according to the company's funding model
        SpcfCalendar limitCalendar = nextValidPaycheckDepositDay.copy();
        CalendarUtils.addBusinessDays(limitCalendar, -1 * fundingModelDays);
        limitCalendar = offloadGroup.getCalendarForActualCutoffTime(limitCalendar);

        // Backdated payroll
        while (currentTime.after(limitCalendar)) {
            CalendarUtils.addBusinessDays(nextValidPaycheckDepositDay, 1);
            limitCalendar = nextValidPaycheckDepositDay.copy();
            CalendarUtils.addBusinessDays(limitCalendar, -1 * fundingModelDays);
            limitCalendar = offloadGroup.getCalendarForActualCutoffTime(limitCalendar);
        }

        return nextValidPaycheckDepositDay;
    }

    public DomainEntitySet<CompanyOffer> getActiveCompanyOffersForOffering(OfferingCode pOfferingCode) {
        DomainEntitySet<CompanyOffer> companyOffers = new DomainEntitySet<CompanyOffer>();
        for (CompanyOffer companyOffer : getCompanyOffers()) {
            if (companyOffer.companyOfferIsActive()) {
                for (OfferingServiceCharge offeringServiceCharge : companyOffer.getOffer().getOfferingServiceChargeCollection()) {
                    if (offeringServiceCharge.getOfferingServiceChargeGroup().getOffering().getOfferingCode() == pOfferingCode) {
                        companyOffers.add(companyOffer);
                        // only add offer once
                        break;
                    }
                }
            }
        }
        return companyOffers;
    }

    public EntitlementUnit getEntitlementUnit(Entitlement pEntitlement, String pFEIN) {
        Criterion<EntitlementUnit> where = EntitlementUnit.Entitlement().equalTo(pEntitlement);
        if(pFEIN == null){
            where = where.And(EntitlementUnit.FedTaxIdEnc().isNull());
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName,pFEIN);
            where = where.And(EntitlementUnit.FedTaxIdEnc().in(fedTaxIdEncList));
        }
        DomainEntitySet<EntitlementUnit> entitlementUnits = getEntitlementUnitCollection().find(where);

        if (entitlementUnits.size() > 0) {
            return entitlementUnits.get(0);
        }
        return null;
    }

    public EntitlementUnit getActivePrimaryEntitlementUnit() {
        return getActiveEntitlementUnits().find(EntitlementUnit.Entitlement().EntitlementCode().IsPrimary().equalTo(true)).getFirst();
    }

    public boolean isTaxExempt() {
        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        return (getTaxExemptStatus() != null && getTaxExemptStatus() == TaxExemptStatusCode.Exempt && (getTaxExemptExpirationDate() == null || getTaxExemptExpirationDate().toLocal().after(today))) ||
                (getTaxExemptStatus() == null && getTaxExemptExpirationDate() != null && getTaxExemptExpirationDate().toLocal().after(today));
    }

    /**
     * tracks newly added strikes that may or may not have been flushed to the database;
     * these newStrikes are added into the return of getCurrentStrikeEvents if they aren't already present
     */
    private Deque<CompanyEvent> newStrikes = new ArrayDeque<CompanyEvent>();

    public CompanyEvent addStrikeEvent(StrikeReason pStrikeReason, String pStrikeReasonDescription,
                                       SpcfCalendar pEffectiveDate, DomainEntitySet<FinancialTransaction> pFinancialTransactions) {
        CompanyEvent strikeEvent = CompanyEvent.createStrikeEvent(this,
                                                                  pStrikeReason,
                                                                  pStrikeReasonDescription,
                                                                  pEffectiveDate,
                                                                  pFinancialTransactions);
        newStrikes.push(strikeEvent);
        return strikeEvent;
    }


    public CompanyEvent addStrikeEvent(StrikeReason pStrikeReason, String pStrikeReasonDescription,
                                       SpcfCalendar pEffectiveDate, FinancialTransaction pFinancialTransaction) {
        CompanyEvent strikeEvent = CompanyEvent.createStrikeEvent(this,
                                                                  pStrikeReason,
                                                                  pStrikeReasonDescription,
                                                                  pEffectiveDate,
                                                                  pFinancialTransaction);
        newStrikes.push(strikeEvent);
        return strikeEvent;
    }

    /**
     * Active strike events in the last 12 months.
     */
    public DomainEntitySet<CompanyEvent> getCurrentStrikeEvents() {
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addMonths(-1 * 12);
        return getStrikeEvents(fromDate);
    }

    public DomainEntitySet<CompanyEvent> getStrikeEvents(SpcfCalendar fromDate) {
        DomainEntitySet<CompanyEvent> strikes =
                CompanyEvent.findCompanyEvents(this, EventTypeCode.Strike, CompanyEventStatus.Active, fromDate, null);

        Iterator<CompanyEvent> transEvents = newStrikes.descendingIterator();
        while (transEvents.hasNext()) {
            CompanyEvent transientStrike = transEvents.next();

            Criterion<CompanyEvent> existsQuery =
                    CompanyEvent.EventTypeCd().equalTo(transientStrike.getEventTypeCd())
                                .And(CompanyEvent.EventTimeStamp().equalTo(transientStrike.getEventTimeStamp()))
                                .And(CompanyEvent.Id().equalTo(transientStrike.getId()));

            if (strikes.find(existsQuery).size() == 0 && (transientStrike.getEventTimeStamp().compareTo(fromDate) >= 0)) {
                strikes.add(transientStrike);
            }
        }

        return strikes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Company")
               .append("  SourceSystem: ").append(getSourceSystemCd())
               .append("  SourceCompanyId: ").append(getSourceCompanyId());
        return builder.toString();
    }

    public String getSourceSystemCompanyId() {
        return getSourceSystemCd() + ":" + getSourceCompanyId();
    }

    public ServiceSubStatusCode getExistingAS400Hold() {
        if (isCompanyHold(ServiceSubStatusCode.AS400DirectDepositLimitHold)) {
            return ServiceSubStatusCode.AS400DirectDepositLimitHold;
        } else if (isCompanyHold(ServiceSubStatusCode.AS400Hold)) {
            return ServiceSubStatusCode.AS400Hold;
        } else {
            return null;
        }
    }

    public void recalculateDailyLiabilities(SpcfCalendar pQuarterStartDate, SpcfCalendar pRunDate) {
        SpcfCalendar twoBusinessDaysAway = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        twoBusinessDaysAway.setValues(pRunDate.getYear(), pRunDate.getMonth(), pRunDate.getDay());
        CalendarUtils.addBusinessDays(twoBusinessDaysAway, 2);

        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(pQuarterStartDate);

        // note - get any companies that may have changes so that we can also capture any deletes/ voids i.e.; don't worry about
        //the status of the paycheck
        String[] paramNames = new String[4];
        paramNames[0] = "twoDaysAway";
        paramNames[1] = "beginDate";
        paramNames[2] = "endDate";
        paramNames[3] = "company";

        Object[] paramValues = new Object[4];
        paramValues[0] = twoBusinessDaysAway;
        paramValues[1] = pQuarterStartDate;
        paramValues[2] = CalendarUtils.getLastDayOfQuarter(pQuarterStartDate);
        paramValues[3] = this;

        List<Object[]> retList =
                Application.executeNamedQuery("findDailyTaxes", paramNames, paramValues);

        List<Object[]> retList2 =
                Application.executeNamedQuery("findDailyAdjustments", paramNames, paramValues);

        retList.addAll(retList2);

        //Delete daily liabilities for company/quarter
        deleteExistingDailyLiabilities(pQuarterStartDate, quarterEndDate);

        DomainEntitySet<CompanyDailyLiability> liabilities = new DomainEntitySet<CompanyDailyLiability>();

        //Replace daily liabilities for company/quarter with new ones
        for (Object[] currObj : retList) {

            SpcfMoney taxAmount = (SpcfMoney) currObj[0];
            SpcfMoney taxableWagesAmount = (SpcfMoney) currObj[1];
            String lawId = (String) currObj[2];
            SpcfCalendar liabilityDate = (SpcfCalendar) currObj[3];
            SpcfMoney tipsWagesAmount = new SpcfMoney("0.00");

            if (taxAmount == null) {
                taxAmount = new SpcfMoney("0.00");
            }

            if (taxableWagesAmount == null) {
                taxableWagesAmount = new SpcfMoney("0.00");
            }

            //If the length is 6, that means it's a paycheck, and it has more data that we need to process than if it's a liab adjustment, including tips and the status of the paycheck
            if (currObj.length == 6) {
                tipsWagesAmount = (SpcfMoney) currObj[4];

                //If the paycheck is inactive, we don't want to include its amounts in the extract to ATF.  However, we still must include it in the file just in case
                // the company's entire set of liabilities for the quarter has already been sent to ATF and is now being completely voided out.  In this case, we already have
                // $$ sitting in ATF for the company/law/quarter, and we now need ATF back to back it out by sending it $0 amounts
                PaycheckStatusCode status = (PaycheckStatusCode) currObj[5];
                if (status == PaycheckStatusCode.Inactive) {
                    taxAmount = new SpcfMoney("0");
                    tipsWagesAmount = new SpcfMoney("0");
                    taxableWagesAmount = new SpcfMoney("0");
                }
            }

            Criterion<CompanyDailyLiability> existsQuery =
                    CompanyDailyLiability.Law().LawId().equalTo(lawId)
                                         .And(CompanyDailyLiability.LiabilityDate().equalTo(liabilityDate));

            DomainEntitySet<CompanyDailyLiability> existingLiabilities = liabilities.find(existsQuery);
            CompanyDailyLiability liabilityForDate;

            if (existingLiabilities.size() > 1) {
                logger.error("Found >1 CDLS for law, date, and company: " + this.getId() + ":" + lawId + ":" + liabilityDate);
            } else if (existingLiabilities.size() == 1) {
                liabilityForDate = existingLiabilities.get(0);
                liabilityForDate.setTaxableWages(new SpcfMoney(taxableWagesAmount.add(liabilityForDate.getTaxableWages())));
                liabilityForDate.setTaxAmount(new SpcfMoney(taxAmount.add(liabilityForDate.getTaxAmount())));
                liabilityForDate.setTotalTipsAmount(new SpcfMoney(tipsWagesAmount.add(liabilityForDate.getTotalTipsAmount())));
                Application.save(liabilityForDate);
                liabilities.add(liabilityForDate);
            } else {
                liabilityForDate = new CompanyDailyLiability();
                liabilityForDate.setCompany(this);
                liabilityForDate.setLaw(Application.findById(Law.class, lawId));
                liabilityForDate.setLiabilityDate(liabilityDate);
                liabilityForDate.setTaxableWages(taxableWagesAmount);
                liabilityForDate.setTaxAmount(taxAmount);
                liabilityForDate.setTotalTipsAmount(tipsWagesAmount);
                liabilityForDate = Application.save(liabilityForDate);
                liabilities.add(liabilityForDate);
            }
        }
    }

    private void deleteExistingDailyLiabilities(SpcfCalendar pStartDate, SpcfCalendar pEndDate) {
        StringBuilder builder = new StringBuilder();
        // Delete all existing CDLs for the company and quarter
        builder.append(" delete from com.intuit.sbd.payroll.psp.domain.CompanyDailyLiability cdl")
               .append(" where cdl.Company = :company")
               .append(" and cdl.LiabilityDate >= :startDate")
               .append(" and cdl.LiabilityDate <= :endDate");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        query.setParameter("company", this);
        query.setParameter("startDate", pStartDate);
        query.setParameter("endDate", pEndDate);
        query.executeUpdate();
    }

    public boolean isServiceSupportedAsOf(ServiceCode pServiceCode, SpcfCalendar pDate) {
        if (pServiceCode == null || pDate == null) {
            return false;
        }

        CompanyService companyService = getService(pServiceCode);
        if (companyService == null || companyService.getServiceStartDate() == null) {
            return false;
        }

        SpcfCalendar serviceStartDate = companyService.getServiceStartDate().copy();
        SpcfCalendar compareDate = pDate.copy();
        CalendarUtils.clearTime(serviceStartDate);
        CalendarUtils.clearTime(compareDate);

        return !pDate.before(serviceStartDate);
    }

    public boolean hasSentBalanceFile() {
        return !CompanyEvent.findCompanyEvents(this, EventTypeCode.BalanceFileReceived).isEmpty();
    }

    public void setNextToken(long token) {
        setCurrentToken(token);
        Application.getSessionCache().addNonHibernateObject(COMPANY_TOKEN_CACHE_KEY + ":" + getId(), getCurrentToken());
    }

    public long getNextToken() {
        Long token = Application.getSessionCache().getNonHibernateObject(COMPANY_TOKEN_CACHE_KEY + ":" + getId());
        if (token == null) {
            setNextToken(getCurrentToken() + 1);
            token = getCurrentToken();
        }
        return token;
    }

    public void usedPayrollTransactionId(String pNextPayrollTransactionId) {
        try {
            usedPayrollTransactionId(Long.parseLong(pNextPayrollTransactionId));
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    public void usedPayrollTransactionId(long pNextPayrollTransactionId) {
        long maxNextPayrollTransactionId = Math.max(pNextPayrollTransactionId, Long.parseLong(getNextPayrollTransactionId()));
        if (maxNextPayrollTransactionId == pNextPayrollTransactionId) {
            maxNextPayrollTransactionId++;
        }
        setNextPayrollTransactionId(Long.toString(maxNextPayrollTransactionId));
        Application.save(this);
    }

    public void usedPaycheckId(String pNextPaycheckId) {
        try {
            usedPaycheckId(Long.parseLong(pNextPaycheckId));
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    public void usedPaycheckId(long pNextPaycheckId) {
        long maxNextPaycheckId = Math.max(pNextPaycheckId, Long.parseLong(getNextPaycheckId()));
        if (maxNextPaycheckId == pNextPaycheckId) {
            maxNextPaycheckId++;
        }
        setNextPaycheckId(Long.toString(maxNextPaycheckId));
        Application.save(this);
    }

    public void usedEmployeeId(String pNextEmployeeId) {
        try {
            usedNextEmployeeId(Long.parseLong(pNextEmployeeId));
        } catch (NumberFormatException e) {
            // ignore, DD employees do not have a numeric employee id
        }
    }

    public void usedNextEmployeeId(long pNextEmployeeId) {
        long maxNextEmployeeId = Math.max(pNextEmployeeId, Long.parseLong(getNextEmployeeId()));
        if (maxNextEmployeeId == pNextEmployeeId) {
            maxNextEmployeeId++;
        }
        setNextEmployeeId(Long.toString(maxNextEmployeeId));
        Application.save(this);
    }

    public void usedPayrollItemId(String pNextPayrollItemId) {
        try {
            usedPayrollItemId(Long.parseLong(pNextPayrollItemId));
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    // yeah, I changed the name got a problem with it
    // deprecated NextPaylineTransactionId, stupid name
    public void usedPayrollItemId(long pNextPayrollItemId) {
        long maxNextPayrollItemId = Math.max(pNextPayrollItemId, Long.parseLong(getNextPayrollItemId()));
        if (maxNextPayrollItemId == pNextPayrollItemId) {
            maxNextPayrollItemId++;
        }
        setNextPayrollItemId(Long.toString(maxNextPayrollItemId));
        Application.save(this);
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setLegalAddress(Address pLegalAddress) {
        if (!ObjectUtils.equals(getLegalAddress(), pLegalAddress)) {
            onUpdate();
        }
        super.setLegalAddress(pLegalAddress);
    }

    @Override
    public void setLegalName(String pLegalName) {
        if (!ObjectUtils.equals(getLegalName(), pLegalName)) {
            onUpdate();
        }
        super.setLegalName(pLegalName);
    }


    public void setFedTaxId(String pFedTaxId) {
        if (!ObjectUtils.equals(getFedTaxId(), pFedTaxId)) {
            onUpdate();
        }
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }

    @Override
    public void setQuickbooksInfo(QuickbooksInfo pQuickbooksInfo) {
        if (!ObjectUtils.equals(getQuickbooksInfo(), pQuickbooksInfo)) {
            onUpdate();
        }
        super.setQuickbooksInfo(pQuickbooksInfo);
    }

    public void onUpdate() {
        if (getQuickbooksInfo() != null) {
            getQuickbooksInfo().onUpdate();
        }
    }

    @Override
    public void setOffloadGroup(OffloadGroup pOffloadGroup) {
        Boolean wasModified = getOffloadGroup() != null && !ObjectUtils.equals(getOffloadGroup(), pOffloadGroup);
        super.setOffloadGroup(pOffloadGroup);

        if (wasModified) {
            DomainEntitySet<MoneyMovementTransaction> pendingMmts =
                    Application.find(MoneyMovementTransaction.class,
                                     MoneyMovementTransaction.Company().equalTo(this)
                                                             .And(MoneyMovementTransaction.Status().equalTo((PaymentStatus.Created))
                                                                                          .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit))));
            for (MoneyMovementTransaction mmt : pendingMmts) {
                mmt.recalculateOffloadBatch();
            }
        }
    }

    public SpcfCalendar getUsageBillingEffectiveDate() {
        // if company is not QBDT company or if its QBDT version is not compatible for usage billing, say no
        if (getQuickbooksInfo() == null) {
            return null;
        } else {
            OFXAPPVERObject ofxAPPVERObject = new OFXAPPVERObject(getQuickbooksInfo().getApplicationVersion());
             if (!ofxAPPVERObject.usageBillingSupported()) {
                 return null;
             }
        }

        EntitlementUnit primaryEntitlementUnit = getActivePrimaryEntitlementUnit();
        if (primaryEntitlementUnit == null) {
            return null;
        }
        if (primaryEntitlementUnit.getEntitlement().getEntitlementCode().getIsUsageBilling()) {
            SpcfCalendar creationDate = primaryEntitlementUnit.getEntitlement().getCreatedDate().copy();
            creationDate.addDays(-1);
            return creationDate;
        } else {
            return null;
        }
    }

    public boolean onUsageBilling() {
        return getUsageBillingEffectiveDate() != null;
    }

    public boolean isCloudOnly(){
        return !isCompanyOnService(ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.Tax, ServiceCode.ThirdParty401k, ServiceCode.ViewMyPaycheck);
    }

    public boolean isMigratingToAssisted() {
        CompanyService directDeposit = getService(ServiceCode.DirectDeposit);
        CompanyService tax = getService(ServiceCode.Tax);
        return tax != null && tax.getStatusCd().in(ServiceSubStatusCode.PendingBalanceFile, ServiceSubStatusCode.PendingSetup) &&
                directDeposit != null && directDeposit.getStatusCd() == ServiceSubStatusCode.ActiveCurrent;
    }

    public CompanyOffering getDirectDepositCompanyOffering() {
        return getCompanyOfferingCollection().findEntity(CompanyOffering.Offering().ServiceCode().equalTo(ServiceCode.DirectDeposit));
    }

    public Long getNumberOfCompleteDDPayrolls() {
        Long numberOfCompletePayrollRuns = Application.getSessionCache().getNonHibernateObject("NumberOfCompletePayrolls" + this.getId());
        if(numberOfCompletePayrollRuns == null) {
            numberOfCompletePayrollRuns = 0L;
            if (getQuickbooksInfo() != null) {
                numberOfCompletePayrollRuns = getQuickbooksInfo().getAS400PayrollCount();
            }

            numberOfCompletePayrollRuns += PayrollRun.getPayrollRunCountByType(this, PayrollStatus.Complete, PayrollType.Regular, PayrollType.BillPayment,
                                                                               PayrollType.CloudOnly); // todo had to add cloud only so that old assisted companies would not be picked up for fraud in R10 we will add a dd payroll count to the service

            Application.getSessionCache().addNonHibernateObject("NumberOfCompletePayrolls" + this.getId(), numberOfCompletePayrollRuns);
        }

        return numberOfCompletePayrollRuns;
    }

    public boolean isInvalidContactEmailId(String pEmailId) {
        Contact contact = Application.find(Contact.class, Contact.Company().equalTo(this).And(Contact.Email().equalTo(pEmailId).And(Contact.HasInvalidEmail().equalTo(true)))).getFirst();
        Payee payee = Application.find(Payee.class, Payee.Company().equalTo(this).And(Payee.Email().equalTo(pEmailId).And(Payee.HasInvalidEmail().equalTo(true)))).getFirst();
        return payee != null || contact != null;
    }

    public SpcfCalendar getWatermarkDate() {

        // Get the latest DD paycheck for this company
        Expression<Paycheck> queryExpression = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().Company().equalTo(this)
                    .And(Paycheck.PaycheckSplitSet().Exists(new EmptyCriterion<PaycheckSplit>())))  // join with PaycheckSplitSet, for DD paychecks
                .OrderBy(Paycheck.PayrollRun().PaycheckDate().Descending())
                .LimitResults(0,1);  // just get one
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class, queryExpression);

        if(paychecks == null || paychecks.isEmpty()) {
            return null;
        }
        //if last paycheck date > today and ALL FUTURE paychecks (whose paycheck date > today) have list ids,
        // choose today; otherwise, choose last paycheck date.

        // The watermark date is initially set to the latest paycheck date
        SpcfCalendar watermarkDate = paychecks.get(0).getPayrollRun().getPaycheckDate();

        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);

        // if the watermark date is in the future
        if (watermarkDate.toLocal().after(today)) {
            // check if there are future DD payrolls, without ListIds
            if(!hasFutureDDPayrollsWithoutListId(today)) {
                // Set the watermark date to today's date - iff all the future paychecks have list Ids
                watermarkDate = today;
            }
        }

        return watermarkDate;
    }

    public SpcfCalendar getTestDebitVerificationDate() {
        Criterion<FinancialTransaction> where = FinancialTransaction.TransactionType().TransactionTypeCd()
                .in(TransactionTypeCode.EmployerVerificationDebit)
                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed))
                .And(FinancialTransaction.Company().equalTo(this)) ;


        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Select(FinancialTransaction.SettlementDate())
                        .Where(where).OrderBy(FinancialTransaction.SettlementDate());

       List<SpcfCalendar> settlementDate = Application.executeQuery(FinancialTransaction.class, query);
        if(!settlementDate.isEmpty()) {
            return settlementDate.get(0);
        }

       return null;
    }

    public Boolean isCompanyRequiredForOFACScreening()
    {
        if(this.getSourceSystemCd().equals(SourceSystemCode.QBDT) && (this.isCompanyOnService(ServiceCode.DirectDeposit)))
        {
            return true;
        }
        return false;
    }

    private boolean hasFutureDDPayrollsWithoutListId(SpcfCalendar today) {

        Expression<Paycheck> query = new Query<Paycheck>()
                .Select(Paycheck.Id().Count())
                .Where(Paycheck.Company().equalTo(this)
                    .And(Paycheck.PaycheckSplitSet().Exists(new EmptyCriterion<PaycheckSplit>()))  // join with PaycheckSplitSet, for DD paychecks
                    .And(Paycheck.PayrollRun().PaycheckDate().greaterThan(today))  // future
                    .And(Paycheck.QbdtPaycheckInfo().ListId().isNull())) ;
        long payrollCount = Application.executeScalarAggQuery(Paycheck.class, query);

        return payrollCount > 0;
    }

    /**
     * PSP-13615
     * Find Companies having Disabled Processing Flag in QuickBooksInfo
     * where RequestProcessingFlagChanged Company Event is created within begin_hrs and end_hrs
     * @return Company DomainEntity
     */
    public static DomainEntitySet<Company> findProcessingDisabledCompanies() {
        String[] paramNames = new String[4];
        paramNames[0] = "event_type_code";
        paramNames[1] = "begin_hrs";
        paramNames[2] = "end_hrs";
        paramNames[3] = "excludeDeletedCompany";

        Object[] paramValues = new Object[4];
        paramValues[0] = EventTypeCode.RequestProcessingFlagChanged.toString();
        paramValues[1] = SystemParameter.findIntValue(SystemParameter.Code.RESET_QBDT_FLAGS_BEGIN_TIME, 24);
        paramValues[2] = SystemParameter.findIntValue(SystemParameter.Code.RESET_QBDT_FLAGS_END_TIME, 48);
        paramValues[3] = !AuthUser.hasSAPAdminAccess();

        return Application.findByNamedQuery(
                Application.getQueryName("findProcessingDisabledCompanies"), paramNames, paramValues);
    }

    /**
     * PSP-14288 method to check if Company is DDMigrated
     * both Phase1 or Phase2 DD Migrated
     * @return
     */
    public boolean isDDMigrated() {
        logger.info("Current offload group : " + this.getOffloadGroup().getName());
        if (this.getOffloadGroup().getOffloadGroupCd().equals(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.PSPOFFLOADS).getOffloadGroupCd())
                || this.getOffloadGroup().getOffloadGroupCd().equals(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.DirectDepositService).getOffloadGroupCd()))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    /**
     * Is this phase 2 DD migrated
     * @return
     */
    public boolean isPhase2DDMigrated() {
        logger.info("Current offload group : " + this.getOffloadGroup().getName());
        if (this.getOffloadGroup().getOffloadGroupCd().equals(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.DirectDepositService).getOffloadGroupCd()))
    		 return Boolean.TRUE;
        return Boolean.FALSE;
    }

    /**
     * Is this phase 2 DD migrated
     * @return
     */
    public boolean isPhase1DDMigrated() {
        logger.info("Current offload group : " + this.getOffloadGroup().getName());
        if (this.getOffloadGroup().getOffloadGroupCd().equals(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.PSPOFFLOADS).getOffloadGroupCd()))
        		return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName, getFedTaxIdEnc());
    }

    public void setPrivateKey(String pPrivateKey)
    {
        super.setPrivateKeyEnc(EncryptionUtils.probabilisticEncrypt(PrivateKeyEncKeyName, pPrivateKey, getId().toString()));
    }

    public String getPrivateKey()
    {
        return EncryptionUtils.probabilisticDecrypt(PrivateKeyEncKeyName, getPrivateKeyEnc());
    }

    public WorkflowPackager getWorkFlowPackager() {

        if(Objects.isNull(this.workflowPackager)){
            String workFlowsFlag = super.getOIIFlag();
            this.workflowPackager = new WorkflowPackager(workFlowsFlag);
        }

        return this.workflowPackager;
    }

    public PublishStatusWorkflowPackager getPublishStatusWorkFlowPackager() {

        if(Objects.isNull(this.publishStatusWorkflowPackager)){
            String workFlowsFlag = super.getPublishStatus();
            this.publishStatusWorkflowPackager = new PublishStatusWorkflowPackager(workFlowsFlag);
        }

        return this.publishStatusWorkflowPackager;
    }

    public void setWorkflowState(Workflows workflow, WorkflowState workflowState) {
        this.getWorkFlowPackager().setWorkflowState(workflow, workflowState);
    }
    public void setPublishStatusWorkflowState(CompanyPublishStatusWorkflows workflow, PublishStatusWorkflowState workflowState) {
        this.getPublishStatusWorkFlowPackager().setWorkflowState(workflow, workflowState);
    }
    //checks for OII Flag bit for ENABLE_VMP in PSP_COMPANY table
    public boolean isVMPEnabled() {
        return getWorkflowState(Workflows.ENABLE_VMP).equals(WorkflowState.ENABLED);
    }

    public boolean isMoneyMovementOnboardingEnabled() {
        return getWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING).equals(WorkflowState.ENABLED);
    }

    public boolean isOIIEnabled() {
        return getWorkflowState(Workflows.OII).equals(WorkflowState.ENABLED);
    }

    public WorkflowState getWorkflowState(Workflows workflow) {
        return this.getWorkFlowPackager().getWorkflowState(workflow);
    }
    public PublishStatusWorkflowState getPublishStatusWorkflowState(CompanyPublishStatusWorkflows workflow) {
        return this.getPublishStatusWorkFlowPackager().getWorkflowState(workflow);
    }
    public String getOIIFlag()
    {
        String workFlowsFlag = this.getWorkFlowPackager().getWorkFlowsFlagString();
        //logger.info("Workflows Flag value for a company " + this.getSourceSystemCompanyId() + " is " + workFlowsFlag);

        return workFlowsFlag;
    }

    public String getPublishStatus() {
        String workFlowsFlag = this.getPublishStatusWorkFlowPackager().getWorkFlowsFlagString();
        return workFlowsFlag;
    }

    public static Company findActiveCompanyByRealmId(String realmId) {
        Company foundCompany = null;

        Criterion<EntitlementUnit> entitlementUnitCriterion = EntitlementUnit.EntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES);

        Criterion<Company> companyCriterion = Company.IAMRealmId().equalTo(realmId).And(Company.EntitlementUnitSet().Exists(entitlementUnitCriterion));

        DomainEntitySet<Company> companies = Application.find(Company.class, companyCriterion);

        if (companies.size() > 1) {
            throw new UniqueCompanyNotFoundException("Query for companies by realmId "+ realmId + " did not return 0 or 1 results as expected");
        }

        if (!companies.isEmpty()) {
            foundCompany = companies.get(0);
        }

        return foundCompany;
    }

    public static DomainEntitySet<Company> findAllCompaniesByRealmId(String realmId) {
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.IAMRealmId().equalTo(realmId));
        return companies;
    }

    public static ScrollableResults findCompanyTaxPayments(String pCompanyId, SpcfCalendar pBeginDate, SpcfCalendar pEndDate) {

        String[] paramNames = new String[]{"companyId","payPeriodBeginDate","payPeriodEndDate"};

        Object[] paramValues = new Object[]{pCompanyId,pBeginDate,pEndDate};

        return Application.scrollableResultsByNamedQuery("findCompanyTaxPaymentDetails", paramNames, paramValues, -1, -1);
    }

    public DomainEntitySet<PayrollRun> findPendingPayrolls(){
        DomainEntitySet<PayrollRun> pendingPayrolls = Application.find(PayrollRun.class, new Query<PayrollRun>()
                .Where(PayrollRun.PayrollRunStatus().equalTo(com.intuit.sbd.payroll.psp.domain.PayrollStatus.Pending)
                        .And(PayrollRun.Company().equalTo(this))));
        return pendingPayrolls;

    }

    public static List<Company> findCompanyByNonAuthCriteria(String ssn, String firstName, String lastName,
                                                             String sourceSystemCode, String contactRole,
                                                             String email, String phoneNumber) {

        String unformattedSsn = ssn.replaceAll("[^0-9]", "");
        String unformattedPhone = phoneNumber.replaceAll("[^0-9]", "");
        List<String> encryptedSsnList = EncryptionUtils.deterministicEncryptWithAllKeys(Contact.SSOKeyName, unformattedSsn);

        String[] paramNames = new String[]{"firstName", "lastName", "ssnList", "email", "phone", "sourceSystemCode", "role", "excludeDeletedCompany"};
        Object[] paramValues = new Object[]{StringUtils.lowerCase(firstName), StringUtils.lowerCase(lastName),
                encryptedSsnList, StringUtils.lowerCase(email), unformattedPhone,
                sourceSystemCode, contactRole,!AuthUser.hasSAPAdminAccess()};
        return Application.executeNamedQuery(
                Application.getQueryName("findCompaniesByNonAuthCriteria"), paramNames, paramValues);
    }

    public static boolean isDGDeleteFeatureEnabled(){
        return Boolean.parseBoolean(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_DG_DISCOVERABILITY_FEATURE"));
    }

}
