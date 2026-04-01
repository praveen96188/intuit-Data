package com.intuit.sbd.payroll.psp.domainsecondary;

import com.intuit.sbd.payroll.psp.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PayrollProcessCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.entitybase.BaseSourceSystemTransmission;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.spi.StandardLevel;


import javax.persistence.Entity;
import java.util.*;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class SourceSystemTransmission extends BaseSourceSystemTransmission {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static SpcfLogger logger = Application.getLogger(SourceSystemTransmission.class);

    public static DomainEntitySet<SourceSystemTransmission> findCompanyTransmissions(String pSourceCompanyId, SourceSystemCode pSourceSystemCd) {
        return findCompanyTransmissions(pSourceCompanyId, pSourceSystemCd, null, null, null);
    }

    public static DomainEntitySet<SourceSystemTransmission> findCompanyTransmissions(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, SpcfCalendar pFromDate, SpcfCalendar pToDate, SourceSystemCode pFromSourceSystemCode) {
        Company company = Company.findCompany(pSourceCompanyId, pSourceSystemCd);
        if(company == null || company.getId() == null)
            throw new RuntimeException("Company not found for SourceCompanyId="+pSourceCompanyId+" SourceSystemCd="+pSourceSystemCd);
        if (isSSTPartitionEnabled() && pFromDate == null) {
            Criterion<SourceSystemTransmission> partitionedQuery = getCriterionForSourceSystemTransmission(company.getId(), pFromSourceSystemCode, Application.getPreviousYearDate(), pToDate);
            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = getSourceSystemTransmissions(partitionedQuery);
            if (CollectionUtils.isNotEmpty(sourceSystemTransmissions)) {
                return sourceSystemTransmissions;
            }
            Application.printStackTrace("SST Full table scan,Company ID=" + company.getId().toString(), StandardLevel.WARN);
        }
        Criterion<SourceSystemTransmission> query = getCriterionForSourceSystemTransmission(company.getId(), pFromSourceSystemCode, pFromDate, pToDate);
        return getSourceSystemTransmissions(query);
    }

    private static Criterion<SourceSystemTransmission> getCriterionForSourceSystemTransmission(SpcfUniqueId companyId, SourceSystemCode pFromSourceSystemCode, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        Criterion<SourceSystemTransmission> query = SourceSystemTransmission.CompanyId().equalTo(companyId.toString());
        if (pFromSourceSystemCode != null) {
            query = query.And(SourceSystemTransmission.FromSourceSystem().equalTo(pFromSourceSystemCode));
        }
        if (pFromDate != null) {
            query = query.And(SourceSystemTransmission.CreatedDate().greaterOrEqualThan(pFromDate));
        }
        if (pToDate != null) {
            query = query.And(SourceSystemTransmission.CreatedDate().lessOrEqualThan(pToDate));
        }
        return query;
    }
    private static DomainEntitySet<SourceSystemTransmission> getSourceSystemTransmissions(Criterion<SourceSystemTransmission> query) {
        return ApplicationSecondary.find(SourceSystemTransmission.class, new Query<SourceSystemTransmission>().Where(query)
                .OrderBy(SourceSystemTransmission.CreatedDate().Descending()));

    }

    /**
     * This method is invoked from CompanyAdapter. startDate and toDate is passed from SAP ui
     * Partition Filter not required
     *
     * @param pIPAddress IpAddress
     * @param startDate  startDate
     * @param endDate    endDate
     * @return companyTransmissions
     */
    public static ArrayList<Object[]> findCompanyTransmissionByIPAndDate(String pIPAddress, SpcfCalendar startDate, SpcfCalendar endDate){
        String fromDate = startDate.format("yyyy-MM-dd");
        String toDate = endDate.format("yyyy-MM-dd");

        String select =
                " select max(st.CreatedDate) as created_date, st.CompanyId, st.IPAddress " +
                        " from com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission st";
        String where =
                " where st.CreatedDate >= TO_DATE( :startDate, 'YYYY-MM-DD')" +
                        "   and st.CreatedDate  <=TO_DATE( :endDate, 'YYYY-MM-DD')" +
                        "   and st.IPAddress LIKE :ipAddress" ;
        String groupBy = " group by st.CompanyId, st.IPAddress";
        ApplicationSecondary.beginUnitOfWork();
        org.hibernate.Query hibernateQuery = ApplicationSecondary.createHibernateQuery(select + where + groupBy);
        hibernateQuery.setParameter("startDate", fromDate);
        hibernateQuery.setParameter("endDate", toDate);
        hibernateQuery.setParameter("ipAddress", pIPAddress+"%");
        List<Object[]> sstResults = (List<Object[]>) hibernateQuery.list();
        MultiValuedMap<String, Object[]> sstResultMap = new ArrayListValuedHashMap<String, Object[]>();
        for (Object[] result: sstResults) {
            sstResultMap.put(result[1].toString(),result);
        }
        ApplicationSecondary.commitUnitOfWork();
        select =
                " select c.id, c.LegalName, c.SourceCompanyId, c.SourceSystemCd " +
                        " from  com.intuit.sbd.payroll.psp.domain.Company c ";
        where = " where c.id in (:ids)" ;
        List<String> list = new ArrayList<String>();
        list.addAll(sstResultMap.keySet());
        List<List<String>> companyIdsLists = ListUtils.partition(list, 1000);
        List<Object[]> companyResults = new ArrayList<Object[]>();
        Application.beginUnitOfWork();
        for (List<String>companyIdsList :companyIdsLists) {
            hibernateQuery = Application.createHibernateQuery(select + where );
            hibernateQuery.setParameterList("ids", companyIdsList);
            List<Object[]> tempResult = (List<Object[]>) hibernateQuery.list();
            companyResults.addAll(tempResult);
        }
        Application.commitUnitOfWork();
        ArrayList<Object[]> result = new ArrayList<Object[]>();
        for (Object[] companyResult : companyResults) {
            for (Object[] entry :sstResultMap.get(companyResult[0].toString())) {
                String[] stringResultEntry = new String[5];
                stringResultEntry[0] =  entry[0].toString();
                stringResultEntry[1] =  companyResult[1].toString();
                stringResultEntry[2] =  entry[2].toString();
                stringResultEntry[3] =  companyResult[2].toString();
                stringResultEntry[4] =  companyResult[3].toString();
                result.add(stringResultEntry);
            }
        }
        return result;
    }

    /**
     * This method is invoked only from AddQBDTOFX which is a test class in testutils
     * Partition Filter not required
     *
     * @param pCompany Company
     * @return responseToken
     */
    public static Long findLastTransmissionResponseToken(Company pCompany) {
        Long lastResponseToken = null;

        List<Long> transmissionTokenResults = findLastTransmission("findLastTransmissionResponseTokenSecondary", pCompany);
        if (transmissionTokenResults.size() > 0) {
            lastResponseToken = transmissionTokenResults.get(0);
        }

        return lastResponseToken;
    }

    /**
     * This method is invoked only from AddQBDTOFX which is a test class in testutils
     * Partition Filter not required
     *
     * @param pCompany Company
     * @return SourceSystemTransmission
     */
    public static SourceSystemTransmission findLastTransmission(Company pCompany) {
        SourceSystemTransmission lastTransmission = null;

        List<SourceSystemTransmission> transmissions = findLastTransmission("findLastTransmissionSecondary", pCompany);
        if (transmissions.size() > 0) {
            lastTransmission = transmissions.get(0);
        }

        return lastTransmission;
    }

    /**
     * This method is invoked only from AddQBDTOFX which is a test class in testutils
     * Partition Filter not required as it does the partition with fromDate
     *
     * @param pNamedQuery
     * @param pCompany    This is cahnged to SpcfUniqueId as part of database Split.
     * @param <T>
     * @return
     */
    private static <T> List<T> findLastTransmission(String pNamedQuery, Company pCompany) {
        int lookBackMonths = SystemParameter.findIntValue(SystemParameter.Code.LAST_TRANSMISSION_LOOKBACK_MONTHS, 4);
        SpcfCalendar lowerDateBound = PSPDate.getPSPTime();
        lowerDateBound.addMonths(lookBackMonths * -1);

        // first search - try against most recent partitions only
        // Id is used for splitting the database.
        List<T> transmissions =
                ApplicationSecondary.executeNamedQuery(pNamedQuery,
                                             new String[]{"company", "lowerDateBound"},
                                             new Object[]{pCompany.getId().toString(), lowerDateBound});

        if (transmissions.size() == 0 && pCompany.getCreatedDate().before(lowerDateBound)) {
            transmissions =
                ApplicationSecondary.executeNamedQuery(pNamedQuery,
                                             new String[]{"company", "lowerDateBound"},
                                             new Object[]{pCompany.getId().toString(), pCompany.getCreatedDate()});
        }

        return transmissions;
    }

    /**
     * This method fetch the failed company sst transmissions in a specific fromDate and toDate. FromDate and toDate is fixed
     * Partition Filter not required
     *
     * @param pCompany  Company
     * @param pFromDate FromDate
     * @param pToDate   ToDate
     * @return DomainEntitySet<SourceSystemTransmission>
     */
    public static DomainEntitySet<SourceSystemTransmission> findFailedCompanyTransmissions(Company pCompany, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        List<String> companyEventDetailsValue = findFailedCompanyEventDetailValue(pCompany);

        String[] paramNames = new String[4];
        paramNames[0] = "companyEventDetailsValue";
        paramNames[1] = "fromDate";
        paramNames[2] = "toDate";
        paramNames[3] = "companyId";


        Object[] paramValues = new Object[4];
        //Id is taken to support db split activity.
        paramValues[1] = pFromDate;
        if (pToDate != null) {
            paramValues[2] = pToDate;
        } else {
            paramValues[2] = CalendarUtils.getPSPDateFromDB();
        }
        paramValues[3] = pCompany.getId().toString();
        DomainEntitySet<SourceSystemTransmission> result = new DomainEntitySet<SourceSystemTransmission>();
        for (List<String> companyEventDetailsList: ListUtils.partition(companyEventDetailsValue,1000)) {
            paramValues[0] = companyEventDetailsList;
            result.addAll(ApplicationSecondary.findByNamedQuery("findFailedCompanyTransmissionsSecondary", paramNames, paramValues));
        }
        return result;
    }
    private static List<String> findFailedCompanyEventDetailValue(Company pCompany) {
        String[] paramNames = new String[1];
        paramNames[0] = "company";

        Object[] paramValues = new Object[1];
        //Id is taken to support db split activity.
        paramValues[0] = pCompany;
        return Application.executeNamedQuery("findFailedCompanyEventDetailValue", paramNames, paramValues);
    }

    public static SourceSystemTransmission findSourceSystemTransmissionByIdentifier(String pTransmissionIdentifier) {
        if (isSSTPartitionEnabled()) {
            SourceSystemTransmission sourceSystemTransmissions = getSourceSystemTransmissionByIdentifier(pTransmissionIdentifier, Application.getPreviousYearDate());
            if (Objects.nonNull(sourceSystemTransmissions)) {
                return sourceSystemTransmissions;
            }
            Application.printStackTrace("SST Full table scan,Transmission Identifier=" + pTransmissionIdentifier, StandardLevel.WARN);
        }
        return getSourceSystemTransmissionByIdentifier(pTransmissionIdentifier);
    }

    private static SourceSystemTransmission getSourceSystemTransmissionByIdentifier(String pTransmissionIdentifier) {
        DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, SourceSystemTransmission.TransmissionIdentifier().equalTo(pTransmissionIdentifier));
        if (sourceSystemTransmissions.size() > 0) {
            return sourceSystemTransmissions.get(0);
        } else {
            return null;
        }
    }

    public static SourceSystemTransmission getSourceSystemTransmissionByIdentifier(String pTransmissionIdentifier, SpcfCalendar pFromDate) {
        Criterion<SourceSystemTransmission> sourceSystemTransmissionCriterion = SourceSystemTransmission.TransmissionIdentifier().equalTo(pTransmissionIdentifier)
                .And(SourceSystemTransmission.CreatedDate().greaterThan(pFromDate));
        DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, sourceSystemTransmissionCriterion);
        if (CollectionUtils.isNotEmpty(sourceSystemTransmissions)) {
            return sourceSystemTransmissions.get(0);
        } else {
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public SourceSystemTransmission()
	{
		super();
	}

    public String getCompanyName()
    {
        return getCompany().getLegalName();
    }

    /**
     * Method to return the transmision for  payroll runs for a given SourceSystemTransmissionID.
     * @return DomainEntitySet of the payroll run transmissions.
     */
    public DomainEntitySet<TransmissionPayrollRun> getTransmissionPayrollRunCollectionFromDB() {
        String sourceSystemTransmissionId = this.getId().toString();
        Expression<TransmissionPayrollRun> query = new Query<TransmissionPayrollRun>()
                .Where(TransmissionPayrollRun.SourceSystemTransmissionId().equalTo(sourceSystemTransmissionId));
        DomainEntitySet<TransmissionPayrollRun> transmissionPayrollRuns = Application.find(TransmissionPayrollRun.class,
                query);
        return transmissionPayrollRuns;
    }

    protected DomainEntitySet<TransmissionPayrollRun> mTransmissionPayrollRunSet = new DomainEntitySet<TransmissionPayrollRun>();

    @SuppressWarnings("unchecked")
    /**
     * This has been done to acheive the DB Split for SST.
     * We try to read the data from cache and if it's not present then we read from DB and set in the cache.
     */
    public DomainEntitySet<TransmissionPayrollRun> getTransmissionPayrollRunCollection()
    {
        DomainEntitySet<TransmissionPayrollRun> tpr =  Application.getSessionCache().getNonHibernateObject("TRANSMISSION_KEY" + ":" + this.getId());
        if (null == tpr || tpr.isEmpty()) {
            mTransmissionPayrollRunSet = getTransmissionPayrollRunCollectionFromDB();
        }
        else {
            mTransmissionPayrollRunSet = tpr;
        }
        return mTransmissionPayrollRunSet;
    }

    @SuppressWarnings("unchecked")
    public void addTransmissionPayrollRun(TransmissionPayrollRun pTransmissionPayrollRun)
    {

        getTransmissionPayrollRunCollection().add(pTransmissionPayrollRun);
        Application.getSessionCache().addNonHibernateObject("TRANSMISSION_KEY" + ":" + this.getId(), mTransmissionPayrollRunSet);
    }

    @SuppressWarnings("unchecked")
    public void removeTransmissionPayrollRun(TransmissionPayrollRun pTransmissionPayrollRun)
    {
        getTransmissionPayrollRunCollection().remove(pTransmissionPayrollRun);
    }


    /**
     * Method to return a Comapny from SST.
     * @return Company Object.
     */
    public Company getCompany() {
        String companyId = this.getCompanyId();
        return Application.findById(Company.class, SpcfUniqueId.createInstance(companyId));
    }

    /**
     * Method to return a Comapny from SST.
     * @param PayrollRun Object
     * @return Company Object.
     */
    public static SourceSystemTransmission getInitialTransmission(PayrollRun pPayrollRun) {
        for (TransmissionPayrollRun tpr : pPayrollRun.getTransmissionPayrollRunCollection()) {
            if (tpr.getPayrollProcess() == PayrollProcessCode.SubmitPayroll) {
                return getSourceSystemTransmissionById(tpr.getSourceSystemTransmissionId());
            }
        }
        return null;
    }

    private static boolean isSSTPartitionEnabled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_PARTITION_SST, false);
    }

    public static SourceSystemTransmission getSourceSystemTransmissionById(String sstId) {
        if (isSSTPartitionEnabled()) {
            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = getSourceSystemTransmissionById(sstId, Application.getPreviousYearDate());
            if (CollectionUtils.isNotEmpty(sourceSystemTransmissions)) {
                return sourceSystemTransmissions.get(0);
            }
            Application.printStackTrace("SST Full table scan,SST id=" + sstId, StandardLevel.WARN);
        }
        return ApplicationSecondary.findById(SourceSystemTransmission.class, SpcfUniqueId.createInstance(sstId));
    }

    public static DomainEntitySet<SourceSystemTransmission> getSourceSystemTransmissionById(String sstId, SpcfCalendar fromDate) {

        return ApplicationSecondary.find(SourceSystemTransmission.class, SourceSystemTransmission.CreatedDate().greaterThan(fromDate)
                .And(SourceSystemTransmission.Id().equalTo(SpcfUniqueId.createInstance(sstId))));
    }

    /**
     *  Returns source system transmission records
      * @param pCompany pCompany
     * @param type transmissionType
     * @return DomainEntitySet<SourceSystemTransmission>
     */
    public static DomainEntitySet<SourceSystemTransmission> getSSTByCompanyAndTransmissionType(Company pCompany, TransmissionType type) {
        if (isSSTPartitionEnabled()) {
            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = getSSTByCompanyAndTransmissionType(pCompany, type, Application.getPreviousYearDate());
            if (CollectionUtils.isNotEmpty(sourceSystemTransmissions)) {
                return sourceSystemTransmissions;
            }
            Application.printStackTrace("SST Full table scan,Company Id=" + pCompany.getId().toString(), StandardLevel.WARN);
        }
        return ApplicationSecondary.find(SourceSystemTransmission.class, getSSTQueryForCompanyIdAndType(getSSTCriterionForCompanyIdAndType(pCompany, type)));
    }

    public static DomainEntitySet<SourceSystemTransmission> getSSTByCompanyAndTransmissionType(Company pCompany, TransmissionType type, SpcfCalendar fromDate) {
        Criterion<SourceSystemTransmission> criterion = getSSTCriterionForCompanyIdAndType(pCompany, type).And(SourceSystemTransmission.CreatedDate().greaterThan(fromDate));
        return ApplicationSecondary.find(SourceSystemTransmission.class, getSSTQueryForCompanyIdAndType(criterion));
    }

    /**
     * Search the sst record in the partition first
     * if not present then scan full table
     * @param mCompany mCompany
     * @param mTransmissionId mTransmissionId
     * @return SourceSystemTransmission
     */
    public static SourceSystemTransmission findSourceSystemTransmissionByCompanyAndTransmissionIdentifier(Company mCompany, String mTransmissionId) {
        if (isSSTPartitionEnabled()) {
            SourceSystemTransmission sourceSystemTransmission = getSSTByCompanyAndTransmissionIdentifier(mCompany, mTransmissionId, Application.getPreviousYearDate());
            if (Objects.nonNull(sourceSystemTransmission)) {
                return sourceSystemTransmission;
            }
            Application.printStackTrace("SST Full table scan,Transmission identifier=" + mTransmissionId, StandardLevel.WARN);
        }
        return getSSTByCompanyAndTransmissionIdentifier(mCompany, mTransmissionId);
    }

    /**
     * Search SST records with companyId, TransmissionId, fromDate
     * @param mCompany mCompany
     * @param mTransmissionId mTransmissionId
     * @param fromDate fromDate
     * @return SourceSystemTransmission
     */
    public static SourceSystemTransmission getSSTByCompanyAndTransmissionIdentifier(Company mCompany, String mTransmissionId, SpcfCalendar fromDate) {
        Criterion<SourceSystemTransmission> sourceSystemTransmissionPartitionCriterion = getSSTQueryForCompanyIdAndTransmissionIdentifier(mCompany,mTransmissionId)
                .And(SourceSystemTransmission.CreatedDate().greaterThan(fromDate));
        DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, sourceSystemTransmissionPartitionCriterion);
        if (CollectionUtils.isNotEmpty(sourceSystemTransmissions)) {
            return sourceSystemTransmissions.get(0);
        } else {
            return null;
        }
    }

    /**
     * Search SST records with companyId, TransmissionId
     * @param mCompany mCompany
     * @param mTransmissionId mTransmissionId
     * @return SourceSystemTransmission
     */
    public static SourceSystemTransmission getSSTByCompanyAndTransmissionIdentifier(Company mCompany, String mTransmissionId) {
        Criterion<SourceSystemTransmission> sourceSystemTransmissionCriterion = getSSTQueryForCompanyIdAndTransmissionIdentifier(mCompany,mTransmissionId);
        DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, sourceSystemTransmissionCriterion);
        if (CollectionUtils.isNotEmpty(sourceSystemTransmissions)) {
            return sourceSystemTransmissions.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the criterion for sst with companyId and transmissionType
     * @param pCompany pCompany
     * @return Criterion<SourceSystemTransmission>
     */
    private static Criterion<SourceSystemTransmission> getSSTCriterionForCompanyIdAndType(Company pCompany, TransmissionType type) {
        return SourceSystemTransmission.CompanyId().equalTo(pCompany.getId().toString()).And(com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission.Type().equalTo(type));
    }

    /**
     * Returns the expression for sst with companyId and transmissionType
     * @param where where clause
     * @return Expression<SourceSystemTransmission>
     */
    private static Expression<SourceSystemTransmission> getSSTQueryForCompanyIdAndType(Criterion<SourceSystemTransmission> where) {
        return new Query<SourceSystemTransmission>()
                .Where(where)
                .OrderBy(SourceSystemTransmission.CreatedDate().Descending()).LimitResults(0, 1);
    }

    /**
     * Returns criterion for SST with companyId and transmissionId
     * @param mCompany mCompany
     * @param mTransmissionId mTransmissionId
     * @return Criterion<SourceSystemTransmission>
     */
    private static Criterion<SourceSystemTransmission> getSSTQueryForCompanyIdAndTransmissionIdentifier(Company mCompany, String mTransmissionId) {
        return SourceSystemTransmission.TransmissionIdentifier().equalTo(mTransmissionId).And(SourceSystemTransmission.CompanyId().equalTo(mCompany.getId().toString()));
    }

    public static boolean isBillPaymentServiceEnabled() {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_BILLPAYMENT_SERVICE_ENABLED, false);
    }
}
