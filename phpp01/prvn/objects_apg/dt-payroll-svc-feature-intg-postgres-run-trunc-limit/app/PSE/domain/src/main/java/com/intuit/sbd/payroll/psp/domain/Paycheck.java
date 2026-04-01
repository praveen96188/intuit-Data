package com.intuit.sbd.payroll.psp.domain;


import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.exceptions.CompensationCollectionNotFoundException;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.sbd.payroll.psp.util.IProcessObserver;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.Hibernate;
import java.util.*;

/**
 * Hand-written business logic
 */
public class Paycheck extends BasePaycheck implements IUpdatable , EntityChangeListener {

    public static SpcfLogger logger = SpcfLogManager.getLogger(Paycheck.class);

    // Recall
    public static final String VOID_FUNDS_RECOVERED = "Payroll Service funds recovered";
    // Void
    public static final String VOID_FUNDS_NOT_RECOVERED = "Payroll Service funds not recovered";

    public static final String QBDT_LIST_ID_DELIMITER = "-";

    private Boolean mPayCardPaycheck = null;
    private String mOriginalSourceId = null;
    private boolean isDuplicate=false;
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public NaturalKey getNaturalKey() {
        return new NaturalKey(Paycheck.class, getPayrollRun().getCompany().getId(), getSourcePaycheckId());
    }

    public static Paycheck findPaycheck(Company pCompany, String pSourcePaycheckId) {
        NaturalKey naturalKey = new NaturalKey(Paycheck.class, pCompany.getId(), pSourcePaycheckId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            return Application.findById(Paycheck.class, primaryKey);
        } else {
            String[] paramNames = new String[2];
            paramNames[0] = "company_seq";
            paramNames[1] = "paycheck_id";

            Object[] paramValues = new Object[2];
            paramValues[0] = pCompany.getId().toString();
            paramValues[1] = pSourcePaycheckId;

            DomainEntitySet<Paycheck> retList = Application.findByNamedQueryUsingCache(Paycheck.class, "SqlPaycheck", paramNames, paramValues);
            if (retList.size() > 0) {
                Paycheck paycheck = retList.get(0);
                paycheck.cache();
                return paycheck;
            }
            return null;
        }
    }

    
    public static Paycheck findPaycheckByQbdtListId(Company pCompany, String pListId) {
        NaturalKey naturalKey = new NaturalKey(Paycheck.class, pCompany.getId(), pListId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            return Application.findById(Paycheck.class, primaryKey);
        } else {
            DomainEntitySet<Paycheck> retList = Application.find(Paycheck.class, Paycheck.Company().equalTo(pCompany).And(Paycheck.QbdtPaycheckInfo().ListId().equalTo(pListId)));
            if (retList.size() > 1) {
                throw new RuntimeException("Query for paycheck by listId" + pListId + " did not return 0 or 1 results as expected");
            }

            if (!retList.isEmpty()) {
                Paycheck paycheck = retList.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, paycheck.getId());
                return paycheck;
            } else {
                return null;
            }
        }
    }

    public static DomainEntitySet<Paycheck> findPaychecks(Company pCompany, Set<String> pSourcePaycheckIds) {
        DomainEntitySet<Paycheck> paychecks = new DomainEntitySet<Paycheck>();

        for (Iterator<String> iterator = pSourcePaycheckIds.iterator(); iterator.hasNext();) {
            String sourcePaycheckId = iterator.next();
            NaturalKey naturalKey = new NaturalKey(Paycheck.class, pCompany.getId(), sourcePaycheckId);
            SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

            if (primaryKey != null) {
                paychecks.add(Application.<Paycheck>findById(Paycheck.class, primaryKey));
                iterator.remove();
            }
        }

        if(pSourcePaycheckIds.size() > 0) {
            List<String> sourcePaycheckIds = new ArrayList<String>(pSourcePaycheckIds);
            int i = 0;
            String[] sourceIdArray = pSourcePaycheckIds.toArray(new String[pSourcePaycheckIds.size()]);
            while (i < sourcePaycheckIds.size())   {
                /**
                 special case possible if source paycheck id changes. This will happen in some service migration cases.
                 @see {@link com.intuit.sbd.payroll.psp.processes.ChangePaycheckSourceIdsCore}
                 */
                int lastIndex = (i + 1000 < sourcePaycheckIds.size()) ? i + 1000 : sourcePaycheckIds.size();
                paychecks.addAll(Application.<Paycheck>findByNamedQuery("SqlPaychecks",
                                                                        new String[]{"company_seq", "paycheck_ids"},
                                                                        new Object[]{pCompany.getId().toString(), sourcePaycheckIds.subList(i, lastIndex)})
                                            .find(Paycheck.SourcePaycheckId().in(sourceIdArray)));
                i = lastIndex + 1;
            }

            for (Paycheck paycheck : paychecks) {
                paycheck.cache();
            }
        }

        return paychecks;
    }

    public static DomainEntitySet<Paycheck> findPaychecksByQBListIds(Company pCompany, Set<String> pListIds) {
        DomainEntitySet<Paycheck> paychecks = new DomainEntitySet<Paycheck>();

        if(pListIds.size() > 0) {
            List<String> listIds = new ArrayList<String>(pListIds);
            int i = 0;
            while (i < listIds.size())   {
                int lastIndex = (i + 1000 < listIds.size()) ? i + 1000 : listIds.size();
                paychecks.addAll(Application.find(Paycheck.class, new Query<Paycheck>()
                        .Where(Paycheck.QbdtPaycheckInfo().Company().equalTo(pCompany)
                                       .And(Paycheck.QbdtPaycheckInfo().ListId().in(listIds.subList(i, lastIndex))))
                        .EagerLoad(Paycheck.QbdtPaycheckInfo()))
                                            .find(Paycheck.QbdtPaycheckInfo().ListId().in(listIds)));
                i = lastIndex + 1;
            }
        }

        return paychecks;
    }

    public static Paycheck findPaycheckInStatus(Company pCompany, String pSourcePaycheckId, PaycheckStatusCode... pStatusCodes) {
        NaturalKey naturalKey = new NaturalKey(Paycheck.class, pCompany.getId(), pSourcePaycheckId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            Paycheck paycheck = Application.findById(Paycheck.class, primaryKey);
            for (PaycheckStatusCode statusCode : pStatusCodes) {
                if (paycheck.getStatus() == statusCode)
                    return paycheck;
            }
            return null;
        } else {
            Set<String> statuses = new HashSet<String>();
            for (PaycheckStatusCode pcsc : pStatusCodes) {
                statuses.add(pcsc.toString());
            }

            DomainEntitySet<Paycheck> retList = Application.<Paycheck>findByNamedQuery("SqlPaychecksByStatus",
                                                  new String[]{"company_seq", "paycheck_id", "status_ids"},
                                                  new Object[]{pCompany.getId().toString(), pSourcePaycheckId, statuses})
                 .find(Paycheck.SourcePaycheckId().equalTo(pSourcePaycheckId));

            if (retList.size() > 0) {
                Paycheck paycheck = retList.get(0);
                paycheck.cache();
                return paycheck;
            }
            return null;
       }
    }

    public static Paycheck findNonCanceledPaycheck(Company pCompany, String pSourcePaycheckId) {

        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "sourcePaycheckId";
        paramNames[2] = "txnState";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = pSourcePaycheckId;
        paramValues[2] = TransactionStateCode.Cancelled;

        DomainEntitySet<Paycheck> retList = Application.findByNamedQueryUsingCache(Paycheck.class, "findPaycheckByCompanyExcludeTxnState", paramNames, paramValues);
        if (retList.size() > 0) {
            return retList.get(0);
        }
        return null;
    }

    public static DomainEntitySet<ThirdParty401kPaycheckPendingState> findTP401kOffloadablePaychecks(SpcfCalendar pInitiationDate) {
        String[] paramNames = new String[1];
        paramNames[0] = "initiationDate";

        Object[] paramValues = new Object[1];
        paramValues[0] = pInitiationDate;

        return Application.findByNamedQuery("findTP401kOffloadablePaychecks", paramNames, paramValues);
    }

    public static DomainEntitySet<Paycheck> findNonFinalTP401kPaychecks(Employee employee) {
        String[] paramNames = new String[2];
        paramNames[0] = "sourceEmployee";
        paramNames[1] = "company";

        Object[] paramValues = new Object[2];
        paramValues[0] = employee;
        paramValues[1] = employee.getCompany();

        return Application.findByNamedQuery("findNonFinalTP401kPaychecks", paramNames, paramValues);
    }

    public static DomainEntitySet<Paycheck> findTP401kQueuedPaychecks(Employee employee) {
        String[] paramNames = new String[3];
        paramNames[0] = "today";
        paramNames[1] = "company";
        paramNames[2] = "sourceEmployeeId";

        Object[] paramValues = new Object[3];
        paramValues[0] = PSPDate.getPSPTime();
        paramValues[1] = employee.getCompany();
        paramValues[2] = employee.getSourceEmployeeId();

        DomainEntitySet<Paycheck> retList =
                Application.findByNamedQuery("findTP401kQueuedPaychecks", paramNames, paramValues);

        return retList;
    }

    public static DomainEntitySet<Paycheck> findActivePaychecks(Company pCompany, String pSourcePayrollRunId) {
        Expression<Paycheck> query =
                new Query<Paycheck>()
                        .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                                .And(Paycheck.PayrollRun().SourcePayRunId().equalTo(pSourcePayrollRunId))
                                .And(Paycheck.Status().equalTo(PaycheckStatusCode.Active)));

        return Application.find(Paycheck.class, query);
    }
    
    
    public static DomainEntitySet<Paycheck> findPaychecksbyStatus(Company pCompany, List<String> pSourcePayrollRunId, PaycheckStatusCode status) {
        
        Expression<Paycheck> query =
                new Query<Paycheck>()
                        .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                                .And(Paycheck.PayrollRun().SourcePayRunId().in(pSourcePayrollRunId))
                                .And(Paycheck.Status().equalTo(status)));

        return Application.find(Paycheck.class, query);
    }
    
    public static DomainEntitySet<Paycheck> findPaychecksNotInStatus( SpcfUniqueId pSourcePayrollfk, PaycheckStatusCode status) {        
       
    	Expression<Paycheck> query =
                new Query<Paycheck>()
                        .Where(Paycheck.PayrollRun().Id().equalTo(pSourcePayrollfk)
                                .And(Paycheck.Status().notEqualTo(status)));

        return Application.find(Paycheck.class, query);
    }

    //@TODO: I'm assuming this logic needs to change
    public static DomainEntitySet<Paycheck> findHistoricalPaychecks(Company pCompany, SpcfCalendar pServiceStartDate) {
        Expression<Paycheck> query;

        if (pServiceStartDate == null) {
            query =
                    new Query<Paycheck>()
                            .Where(Paycheck.PayrollRun().Company().equalTo(pCompany));
        } else {
            query =
                    new Query<Paycheck>()
                            .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                                    .And(Paycheck.PayrollRun().PaycheckSettlementDate().lessThan(pServiceStartDate))
                            );
        }
        return Application.find(Paycheck.class, query);
    }

    public static DomainEntitySet<Paycheck> findPaychecksByEmployee(Company pCompany, Employee pEmployee) {
        Expression<Paycheck> query = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                        .And(Paycheck.DDEmployee().equalTo(pEmployee)));

        return Application.find(Paycheck.class, query);
    }

    public static DomainEntitySet<Paycheck> findPaychecksBySourceEmployee(Company pCompany, Employee pEmployee) {
        Expression<Paycheck> query = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                        .And(Paycheck.SourceEmployee().equalTo(pEmployee)))
                .EagerLoad(Paycheck.PayrollRun());

        return Application.find(Paycheck.class, query);
    }

    @SuppressWarnings("unchecked")
    public static DomainEntitySet<Paycheck> findCloudPaychecksBySourcePayrollRunId(Company pCompany, String pSourcePayrollRunId) {
        Expression<Paycheck> query = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                        .And(Paycheck.SourceEmployee().isNotNull())
                        .And(Paycheck.PayrollRun().SourcePayRunId().equalTo(pSourcePayrollRunId)))
                .EagerLoad(Paycheck.PayrollRun());

        return Application.find(Paycheck.class, query);
    }

    public static DomainEntitySet<Paycheck> findNonSupersededPaychecksByEmployee(Company pCompany, Employee pEmployee, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        Criterion<Paycheck> paycheckWhere = Paycheck.PayrollRun().Company().equalTo(pCompany)
                                                    .And((Paycheck.DDEmployee().equalTo(pEmployee).Or(Paycheck.SourceEmployee().equalTo(pEmployee))))
                                                    .And(Paycheck.PayrollRun().PayrollRunStatus().notEqualTo(PayrollStatus.Superseded))
                .And((Paycheck.SourcePaycheckId().like("-%")).Not());
        if (pFromDate != null) {
            paycheckWhere = paycheckWhere.And(Paycheck.PayrollRun().PaycheckDate().greaterOrEqualThan(pFromDate));
        }
        if (pToDate != null) {
            paycheckWhere = paycheckWhere.And(Paycheck.PayrollRun().PaycheckDate().lessOrEqualThan(pToDate));
        }

        return Application.find(Paycheck.class, new Query<Paycheck>().Where(paycheckWhere)
                                                                     .OrderBy(Paycheck.PayrollRun().PaycheckDate()).EagerLoad(Paycheck.QbdtPaycheckInfo()));
    }

    public static long findPaycheckCountByEmployee(Company pCompany, Employee pEmployee, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        Expression<Paycheck> query = new Query<Paycheck>()
                .Select(Paycheck.SourcePaycheckId().Count())
                .Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                        .And((Paycheck.DDEmployee().equalTo(pEmployee).Or(Paycheck.SourceEmployee().equalTo(pEmployee))))
                        .And(Paycheck.PayrollRun().PaycheckDate().between(pFromDate, pToDate)));

        return Application.executeScalarAggQuery(Paycheck.class, query).longValue();
    }

    public static DomainEntitySet<Paycheck> findPaychecksWithGreaterToken(Company pCompany, long pSyncToken, SpcfCalendar pPaycheckDate, boolean pExcludeNonAssisted) {

        Query<Paycheck> query = new Query<Paycheck>();
        query = (Query<Paycheck>) query.EagerLoad(Paycheck.PayrollRun(),
                                                  Paycheck.QbdtPaycheckInfo(),
                                                  Paycheck.SourceEmployee());
        Criterion<Paycheck> where = Paycheck.QbdtPaycheckInfo().Company().equalTo(pCompany)
                                            .And(Paycheck.QbdtPaycheckInfo().Token().greaterThan(pSyncToken));

        if(pPaycheckDate != null) {
            where = where.And(Paycheck.PayrollRun().PaycheckDate().greaterOrEqualThan(pPaycheckDate));
        }
        if (pExcludeNonAssisted) {
            where = where.And(Paycheck.QbdtPaycheckInfo().IsAssisted().equalTo(true));
        }
        query = (Query<Paycheck>) query.Where(where)
                                       .ReadOnly(true);
        return Application.find(Paycheck.class, query);
    }

    public static DomainEntitySet<Compensation> findPaycheckCompensationsWithGreaterToken(Company pCompany, long pSyncToken, SpcfCalendar pPaycheckDate, boolean pExcludeNonAssisted) {
        Query<Compensation> query = new Query<Compensation>();
        query = (Query<Compensation>) query.EagerLoad(Compensation.QbdtPaylineInfo(),
                                                      Compensation.CompanyPayrollItem(),
                                                      Compensation.CompanyPayrollItem().AdditionalPayrollItem(),
                                                      Compensation.Paycheck(),
                                                      Compensation.Paycheck().QbdtPaycheckInfo(),
                                                      Compensation.Paycheck().SourceEmployee());

        Criterion<Compensation> where = Compensation.Paycheck().QbdtPaycheckInfo().Company().equalTo(pCompany)
                                                    .And(Compensation.Paycheck().QbdtPaycheckInfo().Token().greaterThan(pSyncToken));

        if(pPaycheckDate != null) {
            where = where.And(Compensation.Paycheck().PayrollRun().PaycheckDate().greaterOrEqualThan(pPaycheckDate));
        }
        if (pExcludeNonAssisted) {
            where = where.And(Compensation.Paycheck().QbdtPaycheckInfo().IsAssisted().equalTo(true));
        }
        query = (Query<Compensation>) query.Where(where)
                                           .ReadOnly(true);
        return Application.find(Compensation.class, query);
    }

    public static DomainEntitySet<Deduction> findPaycheckDeductionsWithGreaterToken(Company pCompany, long pSyncToken, SpcfCalendar pPaycheckDate, boolean pExcludeNonAssisted) {

        Query<Deduction> query = new Query<Deduction>();
        query = (Query<Deduction>) query.EagerLoad(Deduction.QbdtPaylineInfo(),
                                                   Deduction.CompanyPayrollItem(),
                                                   Deduction.CompanyPayrollItem().AdditionalPayrollItem(),
                                                   Deduction.Paycheck(),
                                                   Deduction.Paycheck().QbdtPaycheckInfo(),
                                                   Deduction.Paycheck().SourceEmployee());

        Criterion<Deduction> where = Deduction.Paycheck().QbdtPaycheckInfo().Company().equalTo(pCompany)
                                              .And(Deduction.Paycheck().QbdtPaycheckInfo().Token().greaterThan(pSyncToken));
        if(pPaycheckDate != null) {
            where = where.And(Deduction.Paycheck().PayrollRun().PaycheckDate().greaterOrEqualThan(pPaycheckDate));
        }
        if (pExcludeNonAssisted) {
            where = where.And(Deduction.Paycheck().QbdtPaycheckInfo().IsAssisted().equalTo(true));
        }
        query.Where(where)
             .ReadOnly(true);
        return Application.find(Deduction.class, query);
    }

    public static DomainEntitySet<EmployerContribution> findPaycheckEmployerContributionsWithGreaterToken(Company pCompany, long pSyncToken, SpcfCalendar pPaycheckDate, boolean pExcludeNonAssisted) {

        Query<EmployerContribution> query = new Query<EmployerContribution>();
        query = (Query<EmployerContribution>) query.EagerLoad(EmployerContribution.QbdtPaylineInfo(),
                                                              EmployerContribution.CompanyPayrollItem(),
                                                              EmployerContribution.CompanyPayrollItem().AdditionalPayrollItem(),
                                                              EmployerContribution.Paycheck(),
                                                              EmployerContribution.Paycheck().QbdtPaycheckInfo(),
                                                              EmployerContribution.Paycheck().SourceEmployee());
        Criterion<EmployerContribution> where  = EmployerContribution.Paycheck().QbdtPaycheckInfo().Company().equalTo(pCompany)
                                                                     .And(EmployerContribution.Paycheck().QbdtPaycheckInfo().Token().greaterThan(pSyncToken));
        if(pPaycheckDate != null) {
            where = where.And(EmployerContribution.Paycheck().PayrollRun().PaycheckDate().greaterOrEqualThan(pPaycheckDate));
        }
        if (pExcludeNonAssisted) {
            where = where.And(EmployerContribution.Paycheck().QbdtPaycheckInfo().IsAssisted().equalTo(true));
        }
        query = (Query<EmployerContribution>) query.Where(where)
                                                   .ReadOnly(true);
        return Application.find(EmployerContribution.class, query);
    }

    public static DomainEntitySet<Tax> findPaycheckTaxesWithGreaterToken(Company pCompany, long pSyncToken, SpcfCalendar pPaycheckDate, boolean pExcludeNonAssisted) {

        Query<Tax> query = new Query<Tax>();
        query = (Query<Tax>) query.EagerLoad(Tax.CompanyLaw(),
                                             Tax.CompanyLaw().AdditionalCompanyLaw(),
                                             Tax.Paycheck(),
                                             Tax.Paycheck().QbdtPaycheckInfo(),
                                             Tax.Paycheck().SourceEmployee());
        Criterion<Tax> where = Tax.Paycheck().QbdtPaycheckInfo().Company().equalTo(pCompany)
                                  .And(Tax.Paycheck().QbdtPaycheckInfo().Token().greaterThan(pSyncToken));
        if(pPaycheckDate != null) {
            where = where.And(Tax.Paycheck().PayrollRun().PaycheckDate().greaterOrEqualThan(pPaycheckDate));
        }
        if (pExcludeNonAssisted) {
            where = where.And(Tax.Paycheck().QbdtPaycheckInfo().IsAssisted().equalTo(true));
        }
        query = (Query<Tax>) query.Where(where)
                                  .ReadOnly(true);
        return Application.find(Tax.class, query);
    }

    public static DomainEntitySet<PaycheckSplit> findPaycheckSplitsWithGreaterToken(Company pCompany, long pSyncToken, SpcfCalendar pPaycheckDate, boolean pExcludeNonAssisted) {

        Query<PaycheckSplit> query = new Query<PaycheckSplit>();
        query = (Query<PaycheckSplit>) query.EagerLoad(PaycheckSplit.EmployeeBankAccount(),
                                                       PaycheckSplit.EmployeeBankAccount().BankAccount(),
                                                       PaycheckSplit.Paycheck(),
                                                       PaycheckSplit.Paycheck().QbdtPaycheckInfo(),
                                                       PaycheckSplit.Paycheck().SourceEmployee(),
                                                       PaycheckSplit.FinancialTransaction());
        Criterion<PaycheckSplit> where = PaycheckSplit.Paycheck().QbdtPaycheckInfo().Company().equalTo(pCompany)
                                                      .And(PaycheckSplit.Paycheck().QbdtPaycheckInfo().Token().greaterThan(pSyncToken));
        if(pPaycheckDate != null) {
            where = where.And(PaycheckSplit.Paycheck().PayrollRun().PaycheckDate().greaterOrEqualThan(pPaycheckDate));
        }
        if (pExcludeNonAssisted) {
            where = where.And(PaycheckSplit.Paycheck().QbdtPaycheckInfo().IsAssisted().equalTo(true));
        }
        query = (Query<PaycheckSplit>) query.Where(where)
                                            .ReadOnly(true);
        return Application.find(PaycheckSplit.class, query);
    }

    public static DomainEntitySet<Paycheck> findPaychecksVoidedDuringSubmission(Company pCompany, long pVoidToken) {

        Expression<Paycheck> query = new Query<Paycheck>()
                .Where(Paycheck.QbdtPaycheckInfo().Company().equalTo(pCompany)
                               .And(Paycheck.QbdtPaycheckInfo().VoidToken().greaterOrEqualThan(pVoidToken)))
                .EagerLoad(Paycheck.QbdtPaycheckInfo(),
                           Paycheck.SourceEmployee())
                .ReadOnly(true);
        return Application.find(Paycheck.class, query);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Paycheck() {
        super();
    }

    public boolean isVoided() {
        return this.getCompanyAdjustmentSubmission() != null;
    }

    public boolean isRecalled() {
        return getStatus() == PaycheckStatusCode.Inactive && getCompanyAdjustmentSubmission() == null;
    }

    public boolean isVoidedOrRecalled() {
        return isVoided() || isRecalled();
    }

    public DomainEntitySet<PaycheckSplit> getPaycheckSplits() {
        DomainEntitySet<PaycheckSplit> paycheckSplits = Application.find(PaycheckSplit.class, PaycheckSplit.Paycheck().equalTo(this));
        return paycheckSplits;
    }

    public boolean hasBeenOffloadedTOTOK() {
        //todo_rhn_401k add a InProcess state to mark when the batch job is executing
        return  getThirdParty401kPaycheck() != null &&
                (getThirdParty401kPaycheck().getCurrentStateCd().equals(ThirdParty401kPaycheckStateCode.Sent)
                || (isOffloadableToTOK() && PSPDate.getPSPTime().after(getThirdParty401kPaycheck().getInitiationDate())));
    }

    public boolean isOffloadableToTOK() {
        Employee sourceEmployee = getSourceEmployee();
        if (sourceEmployee == null) {
            return false;
        }
        ArrayList<String> validationErrors = sourceEmployee.isValidForCensusFile();
        if (validationErrors != null && validationErrors.size() > 0) {
            return false;
        }

        return getThirdParty401kPaycheck().getCurrentStateCd().equals(ThirdParty401kPaycheckStateCode.Pending);
    }

    public boolean isTOKPaycheck() {
        return getThirdParty401kPaycheck() != null;
    }

    public SpcfCalendar getTOKSendDate() {
        if (getThirdParty401kPaycheck().getCurrentStateCd() == ThirdParty401kPaycheckStateCode.Sent) {
            return getThirdParty401kPaycheck().getInitiationDate();
        } else {
            return null;
        }
    }

    public boolean originallyMissedCutoff() {
        if (getSourceEmployee() == null) {
            return false;
        } else {
            return getThirdParty401kPaycheck() != null && getThirdParty401kPaycheck().getCurrentStateCd().
                    equals(ThirdParty401kPaycheckStateCode.Ineligible);
        }
    }

    public static DomainEntitySet<Paycheck> findCompanyPaychecksFrom(Company pCompany, SpcfCalendar pFromDate) {
        Expression<Paycheck> query = new Query<Paycheck>().Where(Paycheck.PayrollRun().Company().equalTo(pCompany)
                .And(Paycheck.PayrollRun().PaycheckDate().greaterOrEqualThan(pFromDate)));
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class, query);
        for (Paycheck paycheck : paychecks) {
            paycheck.cache();
        }

        return paychecks;
    }

    public static List<SpcfUniqueId> findAssistedPaychecksByPaycheckDateRange(SpcfCalendar startPaycheckDate, SpcfCalendar endPaycheckDate) {
        String[] paramNames = new String[2];
        paramNames[0] = "startPaycheckDate";
        paramNames[1] = "endPaycheckDate";

        Object[] paramValues = new Object[2];
        paramValues[0] = startPaycheckDate;
        paramValues[1] = endPaycheckDate;


        return Application.executeNamedQuery("findAssistedPaychecksByPaycheckDateRange", paramNames, paramValues);
    }
    /*
    Number of paychecks for which all paycheck splits are to PayCard bank accounts
     */
    public static int getPayCardPaycheckCount(Collection<Paycheck> paychecks) {
        int count = 0;
        for (Paycheck p : paychecks) {
            if(p.isPayCardPaycheck()){
                count++;
            }
        }
        return count;
    }

    public boolean isPayCardPaycheck() {
        if(mPayCardPaycheck == null) {
            mPayCardPaycheck = false;
            for (PaycheckSplit ps : getPaycheckSplitCollection()) {
                if (ps.getEmployeeBankAccount().getBankAccount().isPayCardAccount()) {
                    mPayCardPaycheck = true;
                    break;
                }
            }
        }

        return mPayCardPaycheck;
    }

    /**
     * returns total netamount for a paycheck based on paychecksplit
     * @param paycheck
     * @return
     */
    public SpcfMoney findTotalAmountPerPaycheck(){
    	DomainEntitySet<PaycheckSplit> paychecksplits = getPaycheckSplitCollection();
    	//initialize, no need null check reqd in calling method
		SpcfMoney totalPaycheckAmount = SpcfMoney.ZERO;
		//total paycheck amount = sum of all paychecksplits
		if(paychecksplits !=null && paychecksplits.size()>0){
			for(PaycheckSplit paychecksplit:paychecksplits){
				totalPaycheckAmount=(SpcfMoney) totalPaycheckAmount.add(paychecksplit.getPaycheckSplitAmount());
			}
		}
		return totalPaycheckAmount;
	}
    
    /**
     * A paycheck that was not received as part of the DirectDeposit or Assisted service.
     * @return true if the paycheck SourcePaychedId has a hyphen in its value
     */
    public boolean isDIYPaycheck() {
        // horrible way to check but works and is universal:
        // DIY Paychecks use 'ListId' value as identifier. Format is <internal-seq>-<timestamp>.
        return (getSourcePaycheckId().indexOf("-") != -1);
    }

    public static int findEmployeeCount(Company company, SpcfCalendar paymentPeriodBegin,
                                                                              SpcfCalendar paymentPeriodEnd, Law law) {
        int i = 0;
        String[] paramNames = new String[4];
        paramNames[i++] = "company";
        paramNames[i++] = "paymentPeriodBegin";
        paramNames[i++] = "paymentPeriodEnd";
        paramNames[i++] = "law";

        i = 0;
        Object[] paramValues = new Object[4];
        paramValues[i++] = company;
        paramValues[i++] = paymentPeriodBegin;
        paramValues[i++] = paymentPeriodEnd;
        paramValues[i++] = law;

        ArrayList<Long> employeeCount = Application.executeNamedQuery("findEmployeeCount", paramNames, paramValues);

        if (employeeCount.size() == 1) {
            long count = (long) employeeCount.get(0);
            return (int) count;
        } else {
            // TODO: Should this throw exception?
            return 0;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("Paycheck ")
                    .append("  SrcId:").append(getSourcePaycheckId());
            if (getQbdtPaycheckInfo() != null) {
                builder.append("  ListId: ").append(getQbdtPaycheckInfo().getListId());
            }
            builder.append("  Status: ").append(getStatus().name())
                    .append("  Gross:  ").append(getGrossAmount())
                    .append("  Net:  ").append(getNetAmount())
                    .append("  EE: ");
            if (getDDEmployee() != null) {
                builder.append(getDDEmployee().getFullName());
            } else if (getSourceEmployee() != null) {
                builder.append(getSourceEmployee().getFullName());
            } else {
                builder.append("null");
            }
            if (getPayrollRun() != null) {
                builder.append("  CheckDate: ").append(getPayrollRun().getPaycheckDate());
            }
        } catch (Throwable t) {}
        return builder.toString();
    }

    private void updateProcessObservers() {
        if(getCompany() != null && getCompany().getSourceSystemCd() == SourceSystemCode.QBDT) {
            IProcessObserver processObserver = Application.getProcessObserver(FinancialTransaction.QBDT_PROCESS_OBSERVER);
            if(processObserver != null) {
                processObserver.addItem(getPayrollRun());
            }
        }
    }

    public boolean isFromDDService() {
        return getSourceEmployee() == null || getSourceEmployee().getQbdtEmployeeInfo() == null || !getSourceEmployee().getQbdtEmployeeInfo().getIsAssisted();
    }

    public void cache() {
        Application.getSessionCache().addPrimaryKey(getNaturalKey(), getId());
    }

    // transient variable used to track paycheck ids that get update inside quickbooks
    public String getOriginalSourceId() {
        return mOriginalSourceId;
    }

    public void setOriginalSourceId(String pOriginalSourceId) {
        mOriginalSourceId = pOriginalSourceId;
    }

    // ----- QBDT Token overrides -----

    @Override
    public void setSourcePaycheckId(String pSourcePaycheckId) {
        if(!ObjectUtils.equals(getSourcePaycheckId(), pSourcePaycheckId)) {
            onUpdate();
        }

        super.setSourcePaycheckId(pSourcePaycheckId);

        if(getCompany() != null && getSourcePaycheckId() != null) {
            getCompany().usedPaycheckId(getSourcePaycheckId());
        }
    }

    @Override
    public void setPayPeriodBeginDate(SpcfCalendar pPayPeriodBeginDate) {
        if(!ObjectUtils.equals(getPayPeriodBeginDate(), pPayPeriodBeginDate)) {
            onUpdate();
        }
        super.setPayPeriodBeginDate(pPayPeriodBeginDate);
    }

    @Override
    public void setPayPeriodEndDate(SpcfCalendar pPayPeriodEndDate) {
        if(!ObjectUtils.equals(getPayPeriodEndDate(), pPayPeriodEndDate)) {
            onUpdate();
        }
        super.setPayPeriodEndDate(pPayPeriodEndDate);
    }

    @Override
    public void setStatus(PaycheckStatusCode pStatus) {
        if(!ObjectUtils.equals(getStatus(), pStatus)) {
            onUpdate();
            if(pStatus == PaycheckStatusCode.Inactive && getQbdtPaycheckInfo() != null && !getIsYTDAdjustment()) {
                getQbdtPaycheckInfo().setVoidToken(getCompany().getNextToken());
            }
            updateProcessObservers();
        }

        super.setStatus(pStatus);
    }

    @Override
    public void setNetAmount(SpcfMoney pNetAmount) {
        if(!ObjectUtils.equals(getNetAmount(), pNetAmount)) {
            onUpdate();
        }
        super.setNetAmount(pNetAmount);
    }

    @Override
    public void setQbdtPaycheckInfo(QbdtPaycheckInfo pQbdtPaycheckInfo) {
        if(!ObjectUtils.equals(getQbdtPaycheckInfo(), pQbdtPaycheckInfo)) {
            onUpdate();
            // update void token if a new paycheck is voided
            if(getQbdtPaycheckInfo() == null && getStatus() == PaycheckStatusCode.Inactive && !getIsYTDAdjustment()) {
                pQbdtPaycheckInfo.setVoidToken(getCompany().getNextToken());
            }
        }
        super.setQbdtPaycheckInfo(pQbdtPaycheckInfo);
    }

    @Override
    public void setIsYTDAdjustment(boolean pIsYTDAdjustment) {
        if(!ObjectUtils.equals(getIsYTDAdjustment(), pIsYTDAdjustment)) {
            if(getStatus() == PaycheckStatusCode.Inactive && getQbdtPaycheckInfo() != null && pIsYTDAdjustment) {
                getQbdtPaycheckInfo().setVoidToken(getCompany().getNextToken());
            }
            onUpdate();
        }
        super.setIsYTDAdjustment(pIsYTDAdjustment);
    }

    @Override
    public void setCompanyAdjustmentSubmission(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        if(!ObjectUtils.equals(getCompanyAdjustmentSubmission(), pCompanyAdjustmentSubmission)) {
            onUpdate();
            if(pCompanyAdjustmentSubmission != null && getQbdtPaycheckInfo() != null && !getIsYTDAdjustment()) {
                getQbdtPaycheckInfo().setVoidToken(getCompany().getNextToken());
            }
            updateProcessObservers();
        }
        super.setCompanyAdjustmentSubmission(pCompanyAdjustmentSubmission);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }

        super.setCompany(pCompany);

        if(getCompany() != null && getSourcePaycheckId() != null) {
            getCompany().usedPaycheckId(getSourcePaycheckId());
        }
    }

    @Override
    public void setSourceEmployee(Employee pSourceEmployee) {
        if(!ObjectUtils.equals(getSourceEmployee(), pSourceEmployee)) {
            onUpdate();
        }
        super.setSourceEmployee(pSourceEmployee);
    }

    @Override
    public void addEmployerContribution(EmployerContribution pEmployerContribution) {
        super.addEmployerContribution(pEmployerContribution);
        onUpdate();
    }

    @Override
    public void removeEmployerContribution(EmployerContribution pEmployerContribution) {
        super.removeEmployerContribution(pEmployerContribution);
        onUpdate();
    }

    @Override
    public void addCompensation(Compensation pCompensation) {
        super.addCompensation(pCompensation);
        onUpdate();
    }

    @Override
    public void removeCompensation(Compensation pCompensation) {
        super.removeCompensation(pCompensation);
        onUpdate();
    }

    @Override
    public void addDeduction(Deduction pDeduction) {
        super.addDeduction(pDeduction);
        onUpdate();
    }

    @Override
    public void removeDeduction(Deduction pDeduction) {
        super.removeDeduction(pDeduction);
        onUpdate();
    }

    @Override
    public void addTax(Tax pTax) {
        super.addTax(pTax);
        onUpdate();
    }

    @Override
    public void removeTax(Tax pTax) {
        super.removeTax(pTax);
        onUpdate();
    }

    @Override
    public void addPaycheckSplit(PaycheckSplit pPaycheckSplit) {
        super.addPaycheckSplit(pPaycheckSplit);
        onUpdate();
    }

    @Override
    public void removePaycheckSplit(PaycheckSplit pPaycheckSplit) {
        super.removePaycheckSplit(pPaycheckSplit);
        onUpdate();
    }

    public void onUpdate() {
        if(getQbdtPaycheckInfo() != null) {
            getQbdtPaycheckInfo().onUpdate();
        }
    }

    public boolean isDDPaycheck() {
        return !getPaycheckSplits().isEmpty();
    }
  
    public boolean isPaycheckActive(){
        if(getCompany().isDDMigrated())
            return ((getStatus() == PaycheckStatusCode.Active) ||  (getStatus() == PaycheckStatusCode.Created));
        return getStatus() == PaycheckStatusCode.Active;
    }

    /**
     *
     * Get Paycheck List Id from the QBDT Paycheck Info
     *
     * @return
     */
    public String getListId() {
        QbdtPaycheckInfo qbdtPaycheckInfo = getQbdtPaycheckInfo();
        if (Objects.isNull(qbdtPaycheckInfo)) {
            return null;
        }

        return getQbdtPaycheckInfo().getListId();
    }

    /**
     * Extract the QBDT Paycheck Created Date from the QBDT Paycheck List Id.
     *
     *  QBDT Paycheck List Id follows the following format,  RecNum-CreatedEpochTime
     *
     *  Eg: 31110720-1311145200
     *
     *  In the above example,
     *
     *  RecNum = 31110720
     *  CreatedEpochTime = 1311145200
     *
     * @return
     */
    public SpcfCalendar getQdbtPaycheckCreateTime() {
        String listId = getListId();

        if(Objects.isNull(listId)){
            logger.warn(String.format("QBDT Paycheck List Id is NULL for PaycheckId=%s", getId()));
            return null;
        }

        String[] paycheckListIdParts = listId.split(QBDT_LIST_ID_DELIMITER);
        if(paycheckListIdParts.length != 2){
            logger.warn(String.format("Non standard QBDTListID=%s found for PaycheckId=%s", listId, getId()));
            return null;
        }

        String recNum = paycheckListIdParts[0];
        String createdEpochTimeString = paycheckListIdParts[1];

        SpcfCalendar qdbtPaycheckCreateTime = getDate(createdEpochTimeString);

        if(Objects.isNull(qdbtPaycheckCreateTime)){
            logger.warn(String.format("Invalid epoch time found in the QBDTListID=%s for PaycheckId=%s", listId, getId()));
            return null;
        }

        return qdbtPaycheckCreateTime;
    }

    /**
     *
     * <p>Any Paycheck which is created before the Entitlement Unit Billing Start Date is not considered for Usage Billing.</p>
     *
     * <p>Entitlement Unit Billing Start Date is the maximum of below dates
     *      <ol>
     *          <li>{@link com.intuit.sbd.payroll.psp.domain.EntitlementUnit#getCreatedDate()}</li>
     *          <li>{@link com.intuit.sbd.payroll.psp.domain.Entitlement#getSubscriptionStartDate()}</li>
     *      </ol>
     * </p>
     *
     * @return
     */
    public boolean isPaycheckCreatedDateLessThanBillingStartDate(){
        SpcfCalendar qdbtPaycheckCreateTime = getQdbtPaycheckCreateTime();

        List<SpcfCalendar> possibleEntitlementUnitBillingStartDates = new ArrayList<>();

        if (Objects.isNull(qdbtPaycheckCreateTime)) {
            return false;
        }

        EntitlementUnit primaryEntitlementUnit = getCompany().getActivePrimaryEntitlementUnit();

        if (Objects.isNull(primaryEntitlementUnit)) {
            return false;
        }

        Entitlement entitlement = primaryEntitlementUnit.getEntitlement();

        SpcfCalendar entitlementUnitCreatedDate = primaryEntitlementUnit.getCreatedDate();
        SpcfCalendar entitlementSubscriptionStartDate = entitlement.getSubscriptionStartDate();

        if(Objects.nonNull(entitlementUnitCreatedDate)){
            possibleEntitlementUnitBillingStartDates.add(entitlementUnitCreatedDate);
        }

        if(Objects.nonNull(entitlementSubscriptionStartDate)){
            possibleEntitlementUnitBillingStartDates.add(entitlementSubscriptionStartDate);
        }

        if(possibleEntitlementUnitBillingStartDates.isEmpty()){
            logger.warn(String.format("EntitlementUnitCreatedDate=%s, EntitlementSubscriptionStartDate=%s for PaycheckId=%s",
                    entitlementUnitCreatedDate, entitlementSubscriptionStartDate, getId()));
            return false;
        }

        SpcfCalendar entitlementUnitBillingStartDate = Collections.max(possibleEntitlementUnitBillingStartDates);

        logger.info(String.format("EntitlementUnitBillingStartDate=%s for Company=%s for PaycheckId=%s (Identified using EntitlementUnitCreatedDate=%s, EntitlementSubscriptionStartDate=%s) ",
                entitlementUnitBillingStartDate, getCompany().getSourceCompanyId(), getId(), entitlementUnitCreatedDate, entitlementSubscriptionStartDate));


        if(qdbtPaycheckCreateTime.before(entitlementUnitBillingStartDate)){
            return true;
        }

        return false;
    }


	
    public SpcfCalendar getDate(String epochTimeString){
        try {
            long epochTime = Long.parseLong(epochTimeString);
            SpcfCalendar date = SpcfCalendar.createInstance(epochTime*1000);
            return date;
        } catch(Exception e) {
            logger.warn(String.format("Invalid epoch time found  %s", epochTimeString));
            return null;
        }
    }
    
    /**
     * set the mandatory properties
     * @param pacheck
     * @return
     */
    @Override
    public JsonObject getChangedAttribute(){
        JsonObject json = new JsonObject();
    	try{
	        JsonObject jsonProperties = new JsonObject();
	        if(this.getNetAmount()!=null){
	        	jsonProperties.addProperty("NetAmount", this.getNetAmount().toString());
	        }
	        jsonProperties.addProperty("PaycheckId", this.getId().toString());
	        
		    long version=this.getVersion() +1;
		    jsonProperties.addProperty("Version", String.valueOf(version));
	    	jsonProperties.addProperty("ModifiedDate", PSPDate.getPSPTime().toString());

	        if(this.getPayrollRun()!=null && this.getPayrollRun().getPaycheckSettlementDate()!=null){
	        	jsonProperties.addProperty("PayrollRun.PaycheckSettlementDate", this.getPayrollRun().getPaycheckSettlementDate().toString());  
	        }
	        json.addProperty("SessionId", this.getSessionId());
	        if(this.getSessionId()!=null){
	        	logger.info("session id is no null for paycheck with paycheckid "+this.getId().toString());
	        }
	        json.add("Paycheck",jsonProperties);
    	}catch(Exception ex){
        	logger.error("couldnt set paycheck with exception {} "+ ex.getMessage());
    	}
        return json;

    }
    

	@Override
	public Long getEntityVersion() {
		return this.getVersion();
	}
	
	
	@Override
	public String getuniqueId() {
		return this.getId().toString();
	}
	
	@Override
    public String getEntitiesName(){
		return "Paycheck";	
    }
	

	@Override
	public void isDuplicate(boolean duplicate) {
		 this.isDuplicate=duplicate;
	}

	@Override
	public boolean getDuplicate() {
		return isDuplicate;
	}

    public boolean isEqual(Paycheck paycheckToUpdate) {
    	logger.info("Paycheck isEqual compare");
    	try{
			if(!(this.getStatus().toString()).equals(paycheckToUpdate.getStatus().toString())){
		    	logger.info("the status is different");
				return false;
			}

    	}catch(Exception ex){
	    	logger.error("error occurred while comparing EmployeeBankAccount"+ex.getMessage());
    	}
		return true;
	}

    /**
     * @return Gross Amount
     */
    public SpcfMoney findGrossAmountPerPaycheck() {
        // Non-Assisted: Take gross amount from Paystub
        Paystub paystub = Paystub.findPaystub(this);
        if (Objects.nonNull(paystub)) {
            return paystub.getGrossPay();
        }

        // Assisted: Aggregate amount from Compensation
        DomainEntitySet<Compensation> compensations = this.getCompensationCollection();
        SpcfDecimal grossAmount = new SpcfDecimalImpl("0");
        if (!compensations.isEmpty()) {
            for (Compensation compensation : compensations) {
                grossAmount = grossAmount.add(compensation.getCompensationAmount());
            }
        } else {
            logger.warn("Action=findGrossAmountPerPaycheck, Msg=CompensationNotFound, PaycheckId=" + this.getId());
        }
        return new SpcfMoney(grossAmount);
    }

}
