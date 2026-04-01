package com.intuit.sbd.payroll.psp.domain;

import com.google.common.collect.Lists;
import com.intuit.payroll.agency.api.IJurisdiction;
import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Hand-written business logic
 */
public class PayrollRun extends BasePayrollRun {
    private SpcfLogger logger = Application.getLogger(PayrollRun.class);
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final String PAYROLLS_IN_MEMORY_CACHE_KEY = "PayrollInMemoryFromPayrollSubmit";

    // if this property is set to false the calling process is responsible for calculating
    // sales tax on any billing details on this payroll run
    private boolean mCalculateSalesTax = true;

    public boolean calculateSalesTax() {
        return mCalculateSalesTax;
    }

    public void setCalculateSalesTax(boolean pCalculateSalesTax) {
        mCalculateSalesTax = pCalculateSalesTax;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static PayrollRun findPayrollRun(Company pCompany, String pSourcePayrollRunId) {
        NaturalKey naturalKey = new NaturalKey(PayrollRun.class, pCompany.getId(), pSourcePayrollRunId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            return Application.findById(PayrollRun.class, primaryKey);
        } else {
            Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany)
                    .And(PayrollRun.SourcePayRunId().equalTo(pSourcePayrollRunId));

            DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, where);
            if (payrollRuns.size() == 0) {
                return null;
            } else {
                Application.getSessionCache().addPrimaryKey(naturalKey, payrollRuns.get(0).getId());
                return payrollRuns.get(0);
            }
        }
    }

    public static PayrollRun findPayrollRun(SpcfUniqueId pId) {
        Expression<PayrollRun> query = new Query<PayrollRun>()
                .Where(PayrollRun.Id().equalTo(pId))
                .EagerLoad(PayrollRun.Company(), PayrollRun.Company().QuickbooksInfo());

        DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, query);
        if (payrollRuns.size() == 0) {
            return null;
        } else {
            return payrollRuns.get(0);
        }
    }

    public static DomainEntitySet<PayrollRun> findPayrollRuns(final Company pCompany) {
        return findPayrollRuns(pCompany, null, null);
    }

    public static DomainEntitySet<PayrollRun> findPayrollRuns(
            final Company pCompany,
            final SpcfCalendar pFromDate,
            final SpcfCalendar pToDate) {

        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany);
        if (pFromDate != null) {
            where = where.And(PayrollRun.PayrollRunDate().greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(PayrollRun.PayrollRunDate().lessOrEqualThan(pToDate));
        }

        //PSRV001420
        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(where)
                        .OrderBy(PayrollRun.CreatedDate());

        DomainEntitySet<PayrollRun> companyPayrollRuns = Application.find(PayrollRun.class, query);
        return companyPayrollRuns;
    }

    public static PayrollRun findPayrollRunByIdStatusAndSourceCompanyId(String sourceCompanyId, SpcfUniqueId payrollRunSeq, com.intuit.sbd.payroll.psp.domain.PayrollStatus payrollStatus) {

        Criterion<PayrollRun> criterion = PayrollRun.Company().SourceCompanyId().equalTo(sourceCompanyId).And(PayrollRun.Id().equalTo(payrollRunSeq)).And(PayrollRun.PayrollRunStatus().equalTo(payrollStatus));
        Expression<PayrollRun> query = new Query<PayrollRun>().Where(criterion);
        DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, query);

        if (CollectionUtils.isNotEmpty(payrollRuns)) {
            return payrollRuns.get(0);
        }
        return null;
    }

    /*
    Returns payroll runs on this company that were earlier than _this_ payroll run but on or after pFromDate.
    "Earlier" is defined as a strict total order using the payroll run date and breaking ties with the payroll fraud batch token.
        i.e. if pr1 is earlier than pr2 and pr2 is earlier than pr1, then pr1 = pr2
     */
    public DomainEntitySet<PayrollRun> findEarlierPayrollRuns(SpcfCalendar pFromDate) {
        return findPayrollRuns(getCompany(), pFromDate, getPayrollRunDate()).find(
                PayrollRun.PayrollRunDate().lessOrEqualThan(getPayrollRunDate()))
                .sort(PayrollRun.<PayrollRun>CreatedDate());
    }

    public static DomainEntitySet<PayrollRun> getPayrollsInMemory(Company company) {
        DomainEntitySet<PayrollRun> payrollsInMemory = Application.getSessionCache().getNonHibernateObject(PAYROLLS_IN_MEMORY_CACHE_KEY + ":" + company.getId());

        if (payrollsInMemory == null) {
            payrollsInMemory = new DomainEntitySet<PayrollRun>();
            Application.getSessionCache().addNonHibernateObject(PAYROLLS_IN_MEMORY_CACHE_KEY + ":" + company.getId(), payrollsInMemory);
        }

        return payrollsInMemory;
    }

    public static DomainEntitySet<PayrollRun> findPayrollRunsByType(
            final Company pCompany,
            final SpcfCalendar pFromDate,
            final SpcfCalendar pToDate,
            PayrollType... pPayrollRunType) {

        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany)
                .And(PayrollRun.PayrollRunType().in(pPayrollRunType));

        if (pFromDate != null) {
            where = where.And(PayrollRun.PayrollRunDate().greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(PayrollRun.PayrollRunDate().lessOrEqualThan(pToDate));
        }

        //PSRV001420
        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(where)
                        .OrderBy(PayrollRun.CreatedDate());

        DomainEntitySet<PayrollRun> companyPayrollRuns = Application.find(PayrollRun.class, query);
        return companyPayrollRuns;
    }

    public static DomainEntitySet<PayrollRun> findPayrollRunsBySettlementDate(
            final Company pCompany,
            final SpcfCalendar pFromDate,
            final SpcfCalendar pToDate) {

        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany)
                .And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Canceled));
        if (pFromDate != null) {
            where = where.And(PayrollRun.PaycheckSettlementDate().greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(PayrollRun.PaycheckSettlementDate().lessOrEqualThan(pToDate));
        }

        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(where)
                        .OrderBy(PayrollRun.PaycheckSettlementDate());

        DomainEntitySet<PayrollRun> companyPayrollRuns = Application.find(PayrollRun.class, query);
        return companyPayrollRuns;
    }

	public static DomainEntitySet<PayrollRun> findPayrollRunsByPaycheckDate(
        final Company company,
        final SpcfCalendar fromDate,
        final SpcfCalendar toDate) {

        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(company);
        if (fromDate != null) {
            where = where.And(PayrollRun.PaycheckDate().greaterOrEqualThan(fromDate));
        }

		if (toDate != null) {
			where = where.And(PayrollRun.PaycheckDate().lessOrEqualThan(toDate));
		}

		Expression<PayrollRun> query =
				new Query<PayrollRun>()
						.Where(where)
						.OrderBy(PayrollRun.PaycheckDate());

		DomainEntitySet<PayrollRun> companyPayrollRuns = Application.find(PayrollRun.class, query);
		return companyPayrollRuns;
	}


    public static DomainEntitySet<PayrollRun> findPayrollRunsByState(
            final PayrollStatus[] pPayrollStatus) {
        return Application.find(PayrollRun.class, PayrollRun.PayrollRunStatus().in(pPayrollStatus));
    }

    public static DomainEntitySet<PayrollRun> findPayrollRunsByTaxPaymentPeriod(
            final Company pCompany,
            final IPaymentPeriod pPaymentPeriod,
            final SpcfCalendar pNewAccrualDate) {

        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany)
                .And(PayrollRun.PaycheckDate().lessOrEqualThan(CalendarUtils.convertToSpcfCalendar(pPaymentPeriod.getToAccrualDate())));
        if(pNewAccrualDate == null){
            where = where.And(PayrollRun.PaycheckDate().greaterOrEqualThan(CalendarUtils.convertToSpcfCalendar(pPaymentPeriod.getFromAccrualDate())));
        } else {
            where = where.And(PayrollRun.PaycheckDate().greaterOrEqualThan(pNewAccrualDate));
        }


        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(where)
                        .EagerLoad(PayrollRun.PaycheckSet(), PayrollRun.LiabilityAdjustmentSet());

        DomainEntitySet<PayrollRun> companyPayrollRuns = Application.find(PayrollRun.class, query);

        DomainEntitySet<PayrollRun> payrollRunsInMemory = PayrollRun.getPayrollsInMemory(pCompany).find(where);
        for (PayrollRun payrollInMemory : payrollRunsInMemory) {
            if (!companyPayrollRuns.contains(payrollInMemory)) {
                companyPayrollRuns.add(payrollInMemory);
            }
        }

        companyPayrollRuns = companyPayrollRuns.sort(PayrollRun.PaycheckDate(), PayrollRun.PayrollRunDate(), PayrollRun.PayrollRunType());
        return companyPayrollRuns;
    }

    public static DomainEntitySet<PayrollRun> findReversalsOffloadedPayrollRunsForDateRange(
            final SourceSystemCode pSourceSystemCd,
            final SpcfCalendar pLowerBound,
            final SpcfCalendar pUpperBound) {
        DomainEntitySet<PayrollRun> payrollCollection = null;

        TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.EmployeeDdReversalDebit);

        String[] paramNames = new String[5];
        int i = 0;
        paramNames[i++] = "sourceSystemCd";
        paramNames[i++] = "payrollStatus";
        paramNames[i++] = "transactionType";
        paramNames[i++] = "lowerBound";
        paramNames[i++] = "upperBound";

        Object[] paramValues = new Object[5];
        i = 0;
        paramValues[i++] = pSourceSystemCd;
        paramValues[i++] = PayrollStatus.ReversalsOffloaded;
        paramValues[i++] = transactionType;
        paramValues[i++] = pLowerBound;
        paramValues[i++] = pUpperBound;

        payrollCollection = Application.findByNamedQuery("findReversalsOffloadedPayrollRunsForDateRange",
                paramNames, paramValues);

        return payrollCollection;
    }

    public static DomainEntitySet<PayrollRun> findVoidedPayrollsWithPendingApplyForward(
            final Company pCompany) {
        DomainEntitySet<PayrollRun> payrollCollection = null;
        TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.EmployerTaxCredit);
        TransactionState createdState = Application.findById(TransactionState.class, TransactionStateCode.Created);

        String[] paramNames = new String[4];
        int i = 0;
        paramNames[i++] = "company";
        paramNames[i++] = "transactionType";
        paramNames[i++] = "createdState";
        paramNames[i++] = "settlementType";

        Object[] paramValues = new Object[4];
        i = 0;
        paramValues[i++] = pCompany;
        paramValues[i++] = transactionType;
        paramValues[i++] = createdState;
        paramValues[i++] = SettlementType.ApplyForward;

        payrollCollection =
                Application.findByNamedQuery("findVoidedPayrollsWithPendingApplyForward",
                        paramNames, paramValues);

        return payrollCollection;
    }

    public static DomainEntitySet<PayrollRun> findPayrollsByStatusOnOrBeforeMMTInitiationDate(
            final SpcfCalendar pInitiationDate, final SourceSystemCode pSourceSystemCd, final PayrollStatus... pStatusList) {
        SpcfCalendar initiationDateMinus30 = pInitiationDate.copy();
        initiationDateMinus30.addDays(-30);

        int i = 0;
        String[] paramNames = new String[4];
        paramNames[i++] = "initDate";
        paramNames[i++] = "initDateMinus30";
        paramNames[i++] = "sourceSystemCd";
        paramNames[i] = "statusList";

        i = 0;
        Object[] paramValues = new Object[4];

        // since we are using java.sql.Timestamp w/out explicitly setting the calendar to specify the TZ, need to use sys_extract_utc in HQL
        // see java.sql.PreparedStatement.setTimestamp
        paramValues[i++] = new Timestamp(pInitiationDate.getTimeInMilliseconds());
        paramValues[i++] = new Timestamp(initiationDateMinus30.getTimeInMilliseconds());
        paramValues[i++] = pSourceSystemCd.name();

        Set<String> statuses = new HashSet<String>();
        for (PayrollStatus payrollStatus: pStatusList) {
            statuses.add(payrollStatus.name());
        }
        paramValues[i] = statuses;

        return Application.findByNamedQuery(
                Application.getQueryName("SqlFindPayrollsByStatusOnOrBeforeMMTInitiationDate"), paramNames, paramValues);
    }

    public static DomainEntitySet<PayrollRun> findPayrollsByStatusOnOrBeforeMMTInitiationDateEagerLoad(
            final SpcfCalendar pInitiationDate, final SourceSystemCode pSourceSystemCd, final PayrollStatus... pStatusList) {
        DomainEntitySet<PayrollRun> payrollRuns = new DomainEntitySet<PayrollRun>();
        List<SpcfUniqueId> allPayrollRunIds = findPayrollIdsByStatusOnOrBeforeMMTInitiationDate(pInitiationDate, pSourceSystemCd, pStatusList);

        List<List<SpcfUniqueId>> payrollRunIdLists = Lists.partition(allPayrollRunIds, 1000);

        for(List<SpcfUniqueId> payrollRunIds : payrollRunIdLists) {
            Expression<PayrollRun> query =
                    new Query<PayrollRun>()
                            .Where(PayrollRun.Id().in(payrollRunIds))
                            .EagerLoad(PayrollRun.FinancialTransactionSet().Filter().Company().equalTo(PayrollRun.Company()))
                            .EagerLoad(PayrollRun.FinancialTransactionSet().Filter().QbdtTransactionInfo().Company().equalTo(PayrollRun.Company()))
                            .EagerLoad(PayrollRun.FinancialTransactionSet().Filter().MoneyMovementTransaction().Company().equalTo(PayrollRun.Company()))
                            .EagerLoad(PayrollRun.FinancialTransactionSet().Filter().MoneyMovementTransaction().QbdtTransactionInfo().Company().equalTo(PayrollRun.Company()));
            payrollRuns.addAll(Application.find(PayrollRun.class, query));
        }
        payrollRuns = payrollRuns.sort(PayrollRun.CreatedDate(), PayrollRun.PaycheckSettlementDate());
        return payrollRuns;
    }

    public static List<SpcfUniqueId> findPayrollIdsByStatusOnOrBeforeMMTInitiationDate(
            final SpcfCalendar pInitiationDate, final SourceSystemCode pSourceSystemCd, final PayrollStatus... pStatusList) {
        SpcfCalendar initiationDateMinus30 = pInitiationDate.copy();
        initiationDateMinus30.addDays(-30);

        int i = 0;
        String[] paramNames = new String[4];
        paramNames[i++] = "initDate";
        paramNames[i++] = "initDateMinus30";
        paramNames[i++] = "sourceSystemCd";
        paramNames[i] = "statusList";

        i = 0;
        Object[] paramValues = new Object[4];

        // since we are using java.sql.Timestamp w/out explicitly setting the calendar to specify the TZ, need to use sys_extract_utc in HQL
        // see java.sql.PreparedStatement.setTimestamp
        paramValues[i++] = new Timestamp(pInitiationDate.getTimeInMilliseconds());
        paramValues[i++] = new Timestamp(initiationDateMinus30.getTimeInMilliseconds());
        paramValues[i++] = pSourceSystemCd.name();

        Set<String> statuses = new HashSet<String>();
        for (PayrollStatus payrollStatus: pStatusList) {
            statuses.add(payrollStatus.name());
        }
        paramValues[i] = statuses;

        return Application.executeNamedQuery(
                Application.getQueryName("SqlFindPayrollIdsByStatusOnOrBeforeMMTInitiationDate"), paramNames, paramValues);
    }

    public static PayrollRun findPriorPayrollRunBySettlementDateAndCompany(final Company pCompany,
                                                                           final SpcfCalendar pSettlementDate,
                                                                           final String pSourcePayrunId) {
        DomainEntitySet<PayrollRun> payrollCollection = null;

        String[] paramNames = new String[2];
        int i = 0;
        paramNames[i++] = "company";
        //paramNames[i++] = "settlementDate";
        paramNames[i] = "sourcePayRunId";

        Object[] paramValues = new Object[2];
        i = 0;
        paramValues[i++] = pCompany;
        //paramValues[i++] = pSettlementDate;
        paramValues[i] = pSourcePayrunId;

        payrollCollection =
                Application.findByNamedQuery("findPriorPayrollRunBySettlementDateAndCompany", paramNames, paramValues);

        if (payrollCollection.size() > 0) {
            return payrollCollection.get(0);
        }

        return null;
    }

    public static PayrollRun findNextPayrollRunBySettlementDateAndCompany(final Company pCompany,
                                                                          final SpcfCalendar pSettlementDate,
                                                                          final String pSourcePayrunId) {
        DomainEntitySet<PayrollRun> payrollCollection = null;

        String[] paramNames = new String[2];
        int i = 0;
        paramNames[i++] = "company";
        //paramNames[i++] = "settlementDate";
        paramNames[i] = "sourcePayRunId";

        Object[] paramValues = new Object[2];
        i = 0;
        paramValues[i++] = pCompany;
        //paramValues[i++] = pSettlementDate;
        paramValues[i] = pSourcePayrunId;

        payrollCollection =
                Application.findByNamedQuery("findNextPayrollRunBySettlementDateAndCompany", paramNames, paramValues);

        if (payrollCollection.size() > 0) {
            return payrollCollection.get(0);
        }

        return null;
    }

    public static PayrollRun findFirstCompanyPayrollRun(Company company) {
        DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, new Query<PayrollRun>().Where(PayrollRun.Company().equalTo(company)).OrderBy(PayrollRun.PaycheckDate()).LimitResults(0, 1));
        if (payrollRuns != null && payrollRuns.size() > 0) {
            return payrollRuns.get(0);
        }
        return null;
    }

    public static PayrollRun findLatestCompanyPayrollRun(Company company) {
        DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, new Query<PayrollRun>().Where(PayrollRun.Company().equalTo(company)).OrderBy(PayrollRun.PaycheckDate().Descending()).LimitResults(0, 1));
        if (payrollRuns != null && payrollRuns.size() > 0) {
            return payrollRuns.get(0);
        }
        return null;
    }


    public static DomainEntitySet<PayrollRun> findPayrollRunsByState(
            final Company pCompany,
            final PayrollStatus pPayrollStatus) {

        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany);
        if (pPayrollStatus != null) {
            where = where.And(PayrollRun.PayrollRunStatus().equalTo(pPayrollStatus));
        }

        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(where)
                        .OrderBy(PayrollRun.SourcePayRunId());

        DomainEntitySet<PayrollRun> companyPayrollRuns = Application.find(PayrollRun.class, query);

        return companyPayrollRuns;
    }

    public static Long getNextFraudBatchToken() {
        Long nextFraudBatchToken = 0L;
        String[] paramNames = new String[0];
        Object[] paramValues = new Object[0];

        List<Long> retList = Application.executeNamedQuery("findNextFraudBatchToken", paramNames, paramValues);

        if (retList.size() > 0) {
            if (retList.get(0) != null) {
                nextFraudBatchToken = retList.get(0);
            }
        }

        return nextFraudBatchToken;
    }

    public static DomainEntitySet<PayrollRun> findPayrollRunsForQuarter(Company pCompany, SpcfCalendar pFirstDayOfQuarter) {
        SpcfCalendar lastDayOfQuarter = CalendarUtils.getLastDayOfQuarter(pFirstDayOfQuarter);
        Expression<PayrollRun> prQuery = new Query<PayrollRun>()
                .Where(Company().equalTo(pCompany)
                        .And(PaycheckDate().between(pFirstDayOfQuarter, lastDayOfQuarter))).OrderBy(PayrollRunDate());

        DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, prQuery);

        return payrollRuns;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public PayrollRun() {
        super();
    }

    public PayrollRun updatePayrollRunStatus(PayrollStatus pPayrollStatus) {
        if ((getPayrollRunStatus() == PayrollStatus.PendingWire) &&
                (pPayrollStatus != PayrollStatus.PendingWire)) {
            setWireExpectedDate(null);
            setCollectionStageCd(null);
        }
        setPayrollRunStatus(pPayrollStatus);
        return Application.save(this);
    }

    public Map<String, SpcfDecimal> getLawAmountsForNonVoidedPaychecks() {
        Map<String, SpcfDecimal> lawAmountsMap = new HashMap<String, SpcfDecimal>();
        Expression<Paycheck> query =
                new Query<Paycheck>()
                        .Where(Paycheck.PayrollRun().equalTo(this)
                                .And(Paycheck.CompanyAdjustmentSubmission().isNull()));

        DomainEntitySet<Paycheck> nonVoidedPaychecks = Application.find(Paycheck.class, query);
        for (Paycheck paycheck : nonVoidedPaychecks) {
            DomainEntitySet<Tax> taxes = paycheck.getTaxCollection();
            for (Tax tax : taxes) {
                SpcfDecimal lawAmount = lawAmountsMap.get(tax.getLaw().getLawId());

                if (lawAmount == null) {
                    lawAmount = tax.getTaxLiabilityAmount();
                } else {
                    lawAmount = lawAmount.add(tax.getTaxLiabilityAmount());
                }
                lawAmountsMap.put(tax.getLaw().getLawId(), lawAmount);
            }
        }

        return lawAmountsMap;
    }

    public DomainEntitySet<Paycheck> getPaychecksBySettlementDate() {
        String[] paramNames = new String[2];
        paramNames[0] = "company";
        paramNames[1] = "paycheckSettlementDate";
        Object[] paramValues = new Object[2];
        paramValues[0] = getCompany();
        paramValues[1] = getPaycheckSettlementDate().toLocal();

        DomainEntitySet<Paycheck> payChecks =
                Application.findByNamedQuery("findPaychecksBySettlementDate", paramNames, paramValues);
        return payChecks;

    }

    public SpcfMoney getTotalAmountBySettlementDate() {
        String[] paramNames = new String[2];
        paramNames[0] = "company";
        paramNames[1] = "paycheckSettlementDate";
        Object[] paramValues = new Object[2];
        paramValues[0] = getCompany();
        paramValues[1] = getPaycheckSettlementDate().toLocal();
        DomainEntitySet<Paycheck> payChecks =
                Application.findByNamedQuery("findPaychecksBySettlementDate", paramNames, paramValues);
        SpcfDecimal totalAmount = new SpcfMoney("0.00");
        for (Paycheck paycheck : payChecks) {
            for (PaycheckSplit split : paycheck.getPaycheckSplitCollection()) {
                if (getCompany().hasService(ServiceCode.RiskAssessment)){
                    if (paycheck.getStatus().equals(PaycheckStatusCode.Active)) {
                        totalAmount = totalAmount.add(split.getPaycheckSplitAmount());
                    }
                } else {
                    if (split.getFinancialTransaction() != null &&
                            split.getFinancialTransaction().getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Cancelled) {
                        totalAmount = totalAmount.add(split.getPaycheckSplitAmount());
                    }
                }
            }
        }

        return new SpcfMoney(totalAmount);
    }

    public PaycheckSplit getPaycheckSplit(String pSourcePaycheckSplitId) {
        PaycheckSplit paycheckSplit = null;
        String[] paramNames = new String[2];
        paramNames[0] = "payrollRun";
        paramNames[1] = "sourcePaycheckSplitId";


        Object[] paramValues = new Object[2];
        paramValues[0] = this;
        paramValues[1] = pSourcePaycheckSplitId;

        DomainEntitySet<PaycheckSplit> retList =
                Application.findByNamedQueryUsingCache(PaycheckSplit.class, "findPaycheckSplitByPayrollAndSplitId", paramNames, paramValues);
        if (retList != null && retList.size() > 0) {
            paycheckSplit = retList.get(0);
        }

        return paycheckSplit;

    }

    public BillPaymentSplit getBillPaymentSplit(String pSourceBillPaymentSplitId) {
        BillPaymentSplit billPaymentSplit = null;

        Expression<BillPaymentSplit> query =
                new Query<BillPaymentSplit>()
                        .Where(BillPaymentSplit.BillPayment().PayrollRun().equalTo(this)
                                .And(BillPaymentSplit.SourceId().equalTo(pSourceBillPaymentSplitId)))
                        .OrderBy(BillPaymentSplit.CreatedDate())
                        .EagerLoad(BillPaymentSplit.BillPayment());


        DomainEntitySet<BillPaymentSplit> retList = Application.find(BillPaymentSplit.class, query);
        if (retList != null && retList.size() > 0) {
            billPaymentSplit = retList.get(0);
        }

        return billPaymentSplit;

    }

    public ProcessResult convertSplitIdsToPaycheckIds(List<String> pDdTransactionList, List<String> pPaycheckIds) {
        ProcessResult validationResult = new ProcessResult();
        Map<String, FinancialTransaction> pTxnCancelMap = new HashMap<String, FinancialTransaction>();
        // If a transaction id list was passed in, verify that the transactions exist and
        // that they belong to the associated payroll run
        if ((pDdTransactionList != null) && !pDdTransactionList.isEmpty()) {
            Paycheck check;
            PaycheckSplit split;
            DomainEntitySet<PaycheckSplit> paycheckSplits;
            boolean txnFound;
            String amountStr;

            // Verify that no partial paychecks are being cancelled. In other words, check this payroll for any
            // paychecks with multiple DD Transactions (Paycheck Splits) and make sure that either all splits
            // or no splits are canceled for a given paycheck.
            for (String transId : pDdTransactionList) {
                // For economy, if we've already verified a transaction id in the list, move on.
                // (a transaction can only make it to the pTxnCancelMap if it's been verified)
                if (pTxnCancelMap.containsKey(transId)) {
                    continue;
                }

                // this query takes the payroll run and the dd transaction id
                split = getPaycheckSplit(transId);

                if (split == null) {
                    validationResult.getMessages().TransactionDoesNotExist(
                            EntityName.DDTransaction,
                            transId,
                            transId,
                            getCompany().getSourceSystemCd().toString(),
                            getCompany().getSourceCompanyId());
                } else {
                    check = split.getPaycheck();
                    if (!pPaycheckIds.contains(check.getSourcePaycheckId())) {
                        pPaycheckIds.add(check.getSourcePaycheckId());
                    }
                    paycheckSplits = check.getPaycheckSplitCollection();

                    // need to make sure all splits for a given paycheck are cancelled.
                    if (paycheckSplits.size() == 1) {
                        pTxnCancelMap.put(transId, split.getFinancialTransaction());
                    } else {
                        amountStr = "";

                        for (PaycheckSplit s : paycheckSplits) {
                            txnFound = false;
                            for (String txnId : pDdTransactionList) {
                                if (txnId.equalsIgnoreCase(s.getSourceDdTxnId())) {
                                    pTxnCancelMap.put(txnId, s.getFinancialTransaction());
                                    txnFound = true;
                                    break; // found it, so we're done here.
                                }
                            }

                            // If a split's associated transaction id is NOT in the incoming list of transactions
                            // to be cancelled, save the amount for later error reporting back to the client.
                            if (!txnFound) {
                                amountStr += ((amountStr.length() == 0) ? "" : ", ") +
                                        s.getPaycheckSplitAmount().toString();
                            }
                        }

                        // If amountStr is not empty, there are split transactions that are not being cancelled;
                        // this is an error, so we need to record it for return to the client.
                        if (amountStr.length() > 0) {
                            Employee employee = check.getDDEmployee();
                            String name, employeeName;

                            name = employee.getFirstName();
                            employeeName = (((name != null) && (name.length() > 0)) ? name + " " : "");
                            name = employee.getMiddleName();
                            employeeName += (((name != null) && (name.length() > 0)) ? name + " " : "");
                            name = employee.getLastName();
                            employeeName += (((name != null) && (name.length() > 0)) ? name + " " : "");

                            // Save the error message to send back to the client
                            validationResult.getMessages().CannotCancelPartialPaychecks(
                                    EntityName.DDTransaction,
                                    transId,
                                    employeeName.trim(),
                                    split.getPaycheckSplitAmount().toString(),
                                    amountStr);
                        }
                    }

                }
            }
        }
        return validationResult;
    }

    public ProcessResult convertSplitIdsToBillPaymentIds(List<String> pDdTransactionList, List<String> pBillPaymentIds) {
        ProcessResult validationResult = new ProcessResult();
        Map<String, FinancialTransaction> txnCancelMap = new HashMap<String, FinancialTransaction>();
        // If a transaction id list was passed in, verify that the transactions exist and
        // that they belong to the associated payroll run
        if ((pDdTransactionList != null) && !pDdTransactionList.isEmpty()) {
            BillPayment billPayment;
            BillPaymentSplit billPaymentSplit;
            DomainEntitySet<BillPaymentSplit> billPaymentSplits;
            String amountStr;

            // Verify that no partial bill payments are being cancelled. In other words, check this payroll for any
            // bill payments with multiple splits and make sure that either all splits
            // or no splits are canceled for a given paycheck.
            for (String transId : pDdTransactionList) {
                // For economy, if we've already verified a transaction id in the list, move on.
                // (a transaction can only make it to the pTxnCancelMap if it's been verified)
                if (txnCancelMap.containsKey(transId)) {
                    continue;
                }

                // this query takes the payroll run and the bill payment split id
                billPaymentSplit = getBillPaymentSplit(transId);

                if (billPaymentSplit == null) {
                    validationResult.getMessages().TransactionDoesNotExist(
                            EntityName.BillPaymentSplit,
                            transId,
                            transId,
                            getCompany().getSourceSystemCd().toString(),
                            getCompany().getSourceCompanyId());
                } else {
                    billPayment = billPaymentSplit.getBillPayment();
                    if (!pBillPaymentIds.contains(billPaymentSplit.getSourceId())) {
                        pBillPaymentIds.add(billPayment.getSourceId());
                    }
                    billPaymentSplits = billPayment.getBillPaymentSplitCollection();

                    // need to make sure all splits for a given paycheck are cancelled.
                    if (billPaymentSplits.size() == 1) {
                        txnCancelMap.put(transId, billPaymentSplit.getFinancialTransaction());
                    } else {
                        amountStr = "";

                        for (BillPaymentSplit paymentSplit : billPaymentSplits) {
                            if (pDdTransactionList.contains(paymentSplit.getSourceId())) {
                                txnCancelMap.put(paymentSplit.getSourceId(), paymentSplit.getFinancialTransaction());
                            } else {
                                amountStr += ((amountStr.length() == 0) ? "" : ", ") +
                                        paymentSplit.getAmount().toString();
                            }
                        }

                        // If amountStr is not empty, there are split transactions that are not being cancelled;
                        // this is an error, so we need to record it for return to the client.
                        if (amountStr.length() > 0) {
                            Payee payee = billPayment.getPayee();

                            // Save the error message to send back to the client
                            validationResult.getMessages().CannotCancelPartialPayment(
                                    EntityName.BillPayment,
                                    transId,
                                    payee.getName().trim(),
                                    billPaymentSplit.getAmount().toString(),
                                    amountStr);
                        }
                    }

                }
            }
        }
        return validationResult;
    }

    public CompanyBankAccount getCompanyBankAccountForService(ServiceCode pServiceCd) {
        CompanyBankAccount companyBankAccount = null;
        for (CompanyServiceBankAccount companyServiceBankAccount : getCompanyServiceBankAccountCollection()) {
            if (companyServiceBankAccount.getCompanyService().getService().getServiceCd().equals(pServiceCd)) {
                companyBankAccount = companyServiceBankAccount.getCompanyBankAccount();
                return companyBankAccount;
            }
        }
        return companyBankAccount;
    }

    public FinancialTransaction getNonCancelledEmployerDDDebit() {
        Criterion criteria = FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled)
                        .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH)));
        DomainEntitySet<FinancialTransaction> txnList = getFinancialTransactionCollection().find(criteria);

        FinancialTransaction erDdDebit = null;
        if (txnList.size() == 1)
            erDdDebit = txnList.get(0);

        return erDdDebit;
    }

    public boolean isOffloaded() {
        PayrollStatus payrollRunStatus = getPayrollRunStatus();
        if (payrollRunStatus == PayrollStatus.Canceled)
            return false;

        FinancialTransaction erDbDebit = getNonCancelledEmployerDDDebit();
        if (null == erDbDebit) {
            return false;
        }

        SpcfCalendar initDate = erDbDebit.getInitiationDate().toLocal();
        SpcfCalendar limitCalendar = getCompany().getOffloadGroup().getCalendarForCutoffTime(initDate);
        SpcfCalendar now = PSPDate.getPSPTime();

        return now.after(limitCalendar);
    }

    public FinancialTransaction getNonCancelledEmployerTaxDebit() {
        FinancialTransaction erTaxDebit = null;

        DomainEntitySet<FinancialTransaction> erTaxDebits = getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed, TransactionStateCode.Returned)));

        if(erTaxDebits.size() > 0) {
            erTaxDebit = erTaxDebits.get(0);
        }

        return erTaxDebit;
    }

    public FinancialTransaction getEmployerTaxDebit(TransactionStateCode ...pTransactionStateCodes) {
        FinancialTransaction erTaxDebit = null;

        DomainEntitySet<FinancialTransaction> erTaxDebits = getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStateCodes)));

        if(erTaxDebits.size() > 0) {
            erTaxDebit = erTaxDebits.get(0);
        }

        return erTaxDebit;
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions(TransactionState pTxnState, TransactionType pTxnType) {
        return getFinancialTransactions(
                new TransactionTypeCode[]{pTxnType.getTransactionTypeCd()},
                new TransactionStateCode[]{pTxnState.getTransactionStateCd()});
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions(TransactionStateCode pTxnState, TransactionTypeCode pTxnType) {
        return getFinancialTransactions(
                new TransactionTypeCode[]{pTxnType},
                new TransactionStateCode[]{pTxnState});
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions(PaymentTemplate pPaymentTemplate, TransactionStateCode pTxnStateCode, TransactionTypeCode... pTxnTypeCode) {

        Criterion<FinancialTransaction> ftCriteria = FinancialTransaction.PayrollRun().equalTo(this)
                .And(FinancialTransaction.Law().PaymentTemplate().equalTo(pPaymentTemplate))
                .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(pTxnTypeCode))
                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(pTxnStateCode));


        DomainEntitySet<FinancialTransaction> financialTransactions = getFinancialTransactionCollection().find(ftCriteria);

        return financialTransactions;
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions(TransactionTypeCode... pTxnTypes) {
        if (pTxnTypes == null) {
            return new DomainEntitySet<FinancialTransaction>();
        }

        return getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(pTxnTypes));
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions(TransactionState pTxnState, TransactionAssociationType pAssocType) {
        String[] paramNames = new String[3];
        paramNames[0] = "payrollRun";
        paramNames[1] = "transactionAssociationType";
        paramNames[2] = "transactionState";

        Object[] paramValues = new Object[3];
        paramValues[0] = this;
        paramValues[1] = pAssocType;
        paramValues[2] = pTxnState;

        return Application.findByNamedQueryUsingCache(
                FinancialTransaction.class,
                "findFinTxnForPayrollRunTxnAssocType",
                paramNames,
                paramValues);
    }

    /**
     * Obtains list of financial transactions for a given payroll run, transaction type code and transaction state
     *
     * @param pTransactionTypeCodes  String array of Transaction Type codes (optional, pass null if not used)
     * @param pTransactionStateCodes String array of TransactionState Codes (optional, pass null if not used)
     * @return List of payroll run financial transactions
     */
    public DomainEntitySet<FinancialTransaction> getFinancialTransactions(
            TransactionTypeCode[] pTransactionTypeCodes, TransactionStateCode[] pTransactionStateCodes) {
        if (!ArrayUtils.isEmpty(pTransactionTypeCodes) && !ArrayUtils.isEmpty(pTransactionStateCodes)) {
            return getFinancialTransactionCollection()
                    .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(pTransactionTypeCodes)
                            .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStateCodes)));
        } else if (!ArrayUtils.isEmpty(pTransactionTypeCodes)) {
            return getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(pTransactionTypeCodes));
        } else if (!ArrayUtils.isEmpty(pTransactionStateCodes)) {
            return getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStateCodes));
        } else {
            return getFinancialTransactionCollection();
        }
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactionsDB(
            TransactionTypeCode[] pTransactionTypeCodes, TransactionStateCode[] pTransactionStateCodes) {

        Criterion<FinancialTransaction> criterion = FinancialTransaction.PayrollRun().equalTo(this);

        if (!ArrayUtils.isEmpty(pTransactionTypeCodes)) {
            criterion = criterion.And(FinancialTransaction.TransactionType().TransactionTypeCd().in(pTransactionTypeCodes));
        }
        if (!ArrayUtils.isEmpty(pTransactionStateCodes)) {
            criterion = criterion.And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStateCodes));
        }

        return Application.find(FinancialTransaction.class, criterion);
    }

    /**
     * Returns those PayrollRun FTs that match the given criteria.  Any of the criteria may be null, in which case
     * that condition is ignored.
     *
     * @param pCategory
     * @param pGroupCd
     * @param pStateCd
     * @return an DomainEntitySet<FinancialTransaction> of matching transactions
     */
    public DomainEntitySet<FinancialTransaction> getFinancialTransactions(
            TransactionCategory pCategory,
            TransactionTypeGroupCode pGroupCd,
            TransactionStateCode pStateCd) {
        DomainEntitySet<FinancialTransaction> transactions = new DomainEntitySet<FinancialTransaction>();

        for (FinancialTransaction ft : getFinancialTransactionCollection()) {
            TransactionType ftType = ft.getTransactionType();
            if ((pGroupCd == null || pGroupCd == ftType.getTransactionTypeGroupCd()) &&
                    (pCategory == null || pCategory == ftType.getTransactionCategory()) &&
                    (pStateCd == null || pStateCd == ft.calculateCurrentTransactionState().getTransactionStateCd())) {
                transactions.add(ft);
            }
        }

        return transactions;
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(PayrollRun.class, getCompany().getId(), getSourcePayRunId());
    }

    /**
     * Checks if an action event is valid for a PayrollRun
     *
     * @param pActionEvent
     * @return boolean - true if action is valid
     */
    public boolean validateAction(ActionEvent pActionEvent) {

        // this one is always valid
        if (pActionEvent.getCode() == ActionEventCode.ERFraudOrEscalationRefund) {
            return true; // always valid
        }

        PayrollRun.loadPayrollRunActionCache();

        NaturalKey naturalKey = new NaturalKey(PayrollRunAction.class, getPayrollRunStatus(), pActionEvent);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);
        if (primaryKey == null)
            return false;

        switch (pActionEvent.getCode()) {
            case DDTransactionReverse:
                // Get the employee credit transactions with a state of CREATED or EXECUTED for the payroll
                DomainEntitySet<FinancialTransaction> financialTxs = getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Completed, TransactionStateCode.Executed});
                return financialTxs.size() > 0;
            case DDTransactionCancel:
                // Get the employee credit transactions with a state of CREATED for the payroll
                financialTxs = getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

                return financialTxs.size() > 0;
            case DDRedebitAdd:
                financialTxs = getFinancialTransactions(
                        null,
                        new TransactionStateCode[]{TransactionStateCode.Returned});

                for (FinancialTransaction finTxn : financialTxs) {
                    if (TransactionType.isImpoundTransactionType(finTxn.getTransactionType().getTransactionTypeCd()) ||
                            TransactionType.isRedebitTransactionType(finTxn.getTransactionType().getTransactionTypeCd()) ||
                            finTxn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerFeeDebit) ||
                            finTxn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.ServiceSalesAndUseTax)) {
                        return true;
                    }
                }
                return false;
            case RecordPrefundingWire:
                // validate payroll has not already offloaded with an ach debit
                DomainEntitySet<FinancialTransaction> financialTransactions = getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax},
                        new TransactionStateCode[]{TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned, TransactionStateCode.Voided});
                for (FinancialTransaction financialTransaction : financialTransactions) {
                    if (financialTransaction.getSettlementTypeCd() == SettlementType.ACH) {
                        return false;
                    }
                }

                //does not apply to tax companies
                //PrefundingWire for Assisted is not implemented, once we implement that we can add this option
                return !getEmployerTaxDebitTransactions().isNotEmpty();
            case CancelAdjustment:
                //payroll must be a manual adjustment
                return getManualAdjustmentNote() != null;
            case ERFeeAdd:
                return getPayrollRunStatus() != PayrollStatus.Pending || getPayrollRunType() == PayrollType.FeeOnly;
        }

        return true;
    }

    private static void loadPayrollRunActionCache() {
        final String CACHE_KEY = "Cache:PayrollRunAction";
        if (Application.getSessionCache().getNonHibernateObject(CACHE_KEY) == null) {
            // prime the application session cache with PayrollRunAction objects that will be queried when
            // determining the set of valid actions available against a payroll
            // EAGER LOAD ACTION EVENT -- how do you do that w/out supplying an expression???
            DomainEntitySet<ActionEvent> actionEvents = Application.findObjects(ActionEvent.class);
            DomainEntitySet<PayrollRunAction> payrollRunActions = Application.find(PayrollRunAction.class);
            for (PayrollRunAction payrollRunAction : payrollRunActions) {
                NaturalKey naturalKey = new NaturalKey(
                        PayrollRunAction.class,
                        payrollRunAction.getStatus(),
                        payrollRunAction.getActionEvent());
                Application.getSessionCache().addPrimaryKey(naturalKey, payrollRunAction.getId());
            }

            Application.getSessionCache().addNonHibernateObject(CACHE_KEY, new Boolean(true));
        }
    }

    /**
     * Gets a list of valid action events for this payroll run
     *
     * @return a collection of valid action events for this payroll run
     */
    public Collection<ActionEvent> getValidPayrollRunActions(DomainEntitySet<ActionEvent> allPayrollRunActions) {

        Collection<ActionEvent> validActions = new ArrayList<ActionEvent>();
        for (ActionEvent actionEvent : allPayrollRunActions) {
            if (validateAction(actionEvent)) {
                validActions.add(actionEvent);
            }
        }
        return validActions;
    }

    public boolean canInitiateIntuitReversals() {
        //todo:v2 In V2, this will be data-driven
        switch (getPayrollRunStatus()) {
            case Complete:
            case OffloadedAll:
            case RedebitOffloaded:
            case AutoRedebitOffloaded:
                return false;
        }
        return true;
    }

    public SpcfDecimal getFeeReceivableAmount() {


        SpcfDecimal totalFeeReceivable = SpcfDecimal.createInstance("0.00");
        DomainEntitySet<FinancialTransaction> createdFeeAndSalesTaxTxns = getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit,
                TransactionTypeCode.ServiceSalesAndUseTax}, new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction currTxn : createdFeeAndSalesTaxTxns) {
            totalFeeReceivable = totalFeeReceivable.add(currTxn.getFinancialTransactionAmount());
        }
        return totalFeeReceivable;
    }

    public SpcfDecimal getUncollectedAmountForPayroll() {
        SpcfDecimal totalUncollectedAmount = SpcfDecimal.createInstance("0.00");
        SpcfDecimal erReturnReceivableBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                getSourcePayRunId(), getCompany());
        //Return the negation of this since a negative number means the company owes us $ and we are trying to get the total amount owed
        erReturnReceivableBalance = erReturnReceivableBalance.negate();

        totalUncollectedAmount = totalUncollectedAmount.add(erReturnReceivableBalance);

        return totalUncollectedAmount;
    }

    /**
     * PSRV001341 - Adding a condition where by if an employee is being paid within the companies 1st 3 payrolls
     * and that employee belongs to another company that was terminated, then the company needs to go on fraud hold
     * and an event created
     *
     * @return true if the company has an employee that was in a term'd company within the first 3 payrolls
     */
    public boolean isEmployeeInTerminatedCompany() {
        Long as400PayrollCount = 0L;
        boolean createdEvent = false;
        if (getCompany().getQuickbooksInfo() != null) {
            as400PayrollCount = getCompany().getQuickbooksInfo().getAS400PayrollCount();
        }

        int numberOfCompletePayrollRuns = getPayrollRunCountByStatus(getCompany(), PayrollStatus.Complete);
        if (numberOfCompletePayrollRuns + as400PayrollCount < 3) {
            for (Paycheck check : getDDPaycheckCollection()) {
                Employee employee = check.getDDEmployee();
                DomainEntitySet<Employee> employees = employee.findEmployeesWithSameNameFromTerminatedCompanies();
                if (employees.size() > 0) {
                    //if the ee matches 2 terminated companies, will just generate event for first one
                    Company matchedCompany = employees.get(0).getCompany();
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Add the employee in terminated company event
                    String note = "Employee " + employee.getFullName() + " was detected in company " +
                            "PSID: " + matchedCompany.getSourceCompanyId() + " that has been terminated.";

                    Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeeInTermedCompany, this, note));
                    createdEvent = true;
                }
            }
        }
        return createdEvent;
    }

    /**
     * Gets a list of valid action events for this payroll run
     *
     * @param pLedgerAccount
     * @return a collection of valid action events for this payroll run
     */
    public Collection<ActionEvent> getValidActions(LedgerAccount pLedgerAccount) {

        Collection<ActionEvent> validActions = new ArrayList<ActionEvent>();

        DomainEntitySet<ActionEvent> actionEvents = Application.findObjects(ActionEvent.class);
        for (ActionEvent actionEvent : actionEvents) {
            if (actionEvent.getType().equals(ActionType.LedgerAccount)) {
                DomainEntitySet<LedgerAccountAction> ledgerAccountActions =
                        Application.find(LedgerAccountAction.class,
                                LedgerAccountAction.ActionEvent().equalTo(actionEvent)
                                        .And(LedgerAccountAction.LedgerAccount().equalTo(pLedgerAccount)));

                if (ledgerAccountActions.size() == 1) {
                    LedgerAccountAction ledgerAccountAction = ledgerAccountActions.get(0);
                    if (validateAction(ledgerAccountAction.getActionEvent().getCode())) {
                        validActions.add(actionEvent);
                    }
                }
            }
        }
        return validActions;
    }

    public boolean validateAction(ActionEventCode pActionEventCode,
                                  TransactionAssociationType pExcludedTxnAssociationType) {
        ActionEvent actionEvent = Application.findById(ActionEvent.class, pActionEventCode);

        if (pActionEventCode.equals(ActionEventCode.Intuit5DayReturnTransfer)) {
            if (getCompany().isCompanyOnService(ServiceCode.Tax)) {
                return false;
            }
            SpcfMoney balanceAmount = LedgerAccount.getLedgerAccountBalanceByPayrollFinTxnCollection(
                    LedgerAccountCode.ERReturnReceivable,
                    getSourcePayRunId(),
                    getCompany());
            return balanceAmount.getSign() < 0;
        } if (pActionEventCode == ActionEventCode.VoidPayrollTaxPayment) {
            if (getPayrollRunStatus() == PayrollStatus.Pending || getPayrollRunStatus() == PayrollStatus.OffloadedAll || getPayrollRunStatus() == PayrollStatus.Complete) {
                return false;
            }
            //this is normally like what's in the DB, but not if there are ATDs.  Instead of comparing it to 0, we compare it to ERPayable
            SpcfDecimal taxCurrentCash = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.TaxCurrentCash, getSourcePayRunId(), getCompany()).negate();
            SpcfDecimal erPayable = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERPayable, getSourcePayRunId(), getCompany());
            SpcfDecimal erSUITaxDue = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERSUITaxDue, getSourcePayRunId(), getCompany());

            return taxCurrentCash.isGreaterThan(erPayable) && !taxCurrentCash.equals(erSUITaxDue);

        } else {
            boolean isValidActionByBalance = isValidActionByBalance(actionEvent.getLedgerAccountAction(), pExcludedTxnAssociationType);

            if (pActionEventCode == ActionEventCode.ApplyERPayableToBalanceDue) {
                return isValidActionByBalance && LedgerAccount.getLedgerAccountBalance(getCompany(), LedgerAccountCode.ERPayable).isGreaterThan(SpcfMoney.ZERO);
            }

            return isValidActionByBalance;
        }
    }

    private boolean isValidActionByBalance(LedgerAccountAction ledgerAccountAction, TransactionAssociationType pExcludedTxnAssociationType) {
        SpcfMoney amount = LedgerAccount.getLedgerAccountBalanceByPayroll(
                ledgerAccountAction.getLedgerAccount().getLedgerAccountCd(),
                getSourcePayRunId(),
                getCompany(),
                pExcludedTxnAssociationType);

        //If the ledger account balance is positive, that means there is a credit balance for the payroll in the given account
        //If the ledger account balance is negative, that means there is a debit balance for the payroll in the given account
        boolean bAmountIsPositive = amount.getSign() > 0;
        boolean bAmountIsNegative = amount.getSign() < 0;

        if (ledgerAccountAction.getCreditDebitIndicator() == CreditDebitCode.Credit && bAmountIsPositive) {
            return true;
        } else if (ledgerAccountAction.getCreditDebitIndicator() == CreditDebitCode.Debit && bAmountIsNegative) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Validates if an action is valid for a payrollrun based on the ledger account code associated with the action
     *
     * @param pActionEventCode
     * @return
     */
    public boolean validateAction(ActionEventCode pActionEventCode) {
        return validateAction(pActionEventCode, null);
    }

    public boolean isEmployeeBankAccountInTerminatedOrFraudHoldCompany() {
        boolean createdEvent = false;

        @SuppressWarnings("unchecked") Map<String, ArrayList<SuspectBankAccountInfo>> suspectEmployeeBankAccountMap =
                (Map<String, ArrayList<SuspectBankAccountInfo>>) Application.getSessionCache().getNonHibernateObject("SuspectEmployeeBankAccountMap");
        if (suspectEmployeeBankAccountMap == null) {
            suspectEmployeeBankAccountMap = SuspectBankAccountInfo.loadSuspectBankAccountMap();
            Application.getSessionCache().addNonHibernateObject("SuspectEmployeeBankAccountMap", suspectEmployeeBankAccountMap);
        }
        List<Object[]> paycheckSplitBankAccounts = null;
        paycheckSplitBankAccounts = Application.executeQuery(Paycheck.class, new Query<Paycheck>()
                .Select(Paycheck.PaycheckSplitSet().Filter().EmployeeBankAccount(),
                        Paycheck.PaycheckSplitSet().Filter().EmployeeBankAccount().BankAccount().RoutingNumber(),
                        Paycheck.PaycheckSplitSet().Filter().EmployeeBankAccount().BankAccount().AccountNumberEnc(),
                        Paycheck.PaycheckSplitSet().Filter().EmployeeBankAccount().BankAccount().AccountTypeCd())
                .Where(Paycheck.PayrollRun().equalTo(this))
                .OrderBy(Paycheck.PaycheckSplitSet().Filter().EmployeeBankAccount().BankAccount().RoutingNumber(),
                        Paycheck.PaycheckSplitSet().Filter().EmployeeBankAccount().BankAccount().AccountNumberEnc(),
                        Paycheck.PaycheckSplitSet().Filter().EmployeeBankAccount().BankAccount().AccountTypeCd()));


        for (Object[] paycheckSplitBankAccount : paycheckSplitBankAccounts) {
            EmployeeBankAccount employeeBankAccount = (EmployeeBankAccount) paycheckSplitBankAccount[0];
            String bankRoutingNumber = (String) paycheckSplitBankAccount[1];
            String bankAccountNumber = null;
            bankAccountNumber = EncryptionUtils.deterministicDecrypt(BankAccount.AccountNumberKeyName,(String) paycheckSplitBankAccount[2]);

            String bankAccountTypeCd = ((BankAccountType) paycheckSplitBankAccount[3]).name();

            String key = String.format("%s:%s:%s", bankRoutingNumber, bankAccountNumber, bankAccountTypeCd);
            ArrayList<SuspectBankAccountInfo> likeEmployeeBankAccounts = suspectEmployeeBankAccountMap.get(key);
            if (likeEmployeeBankAccounts != null && !likeEmployeeBankAccounts.isEmpty()) {
                SuspectBankAccountInfo likeEmployeeBankAccount = likeEmployeeBankAccounts.get(0);

                String likeCompanyId = likeEmployeeBankAccount.companyId;


                //flag the Company for Fraud
                getCompany().setFraudFlag();

                //Add the employee bank account in terminated company event
                String note = "Employee bank account " + bankAccountNumber +
                        " for employee " + employeeBankAccount.getEmployee().getFullName() +
                        ", matches employee/payee bank account " + likeEmployeeBankAccount.accountNumber +
                        " for " + likeEmployeeBankAccount.accountOwnerName +
                        " in company " + likeCompanyId + ".";

                CompanyEvent event = CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeeBankAccountInTermedCompany, this, note, employeeBankAccount.getEmployee());
                event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId, getSourcePayRunId());
                event.addCompanyEventDetail(EventDetailTypeCode.EmployeeBankAccountId, employeeBankAccount.getId().toString());

                createdEvent = true;

            }
        }

        return createdEvent;
    }

    public boolean isPayeeBankAccountInTerminatedOrFraudHoldCompany() {
        boolean createdEvent = false;

        @SuppressWarnings("unchecked") Map<String, ArrayList<SuspectBankAccountInfo>> suspectEmployeeBankAccountMap =
                (Map<String, ArrayList<SuspectBankAccountInfo>>) Application.getSessionCache().getNonHibernateObject("SuspectEmployeeBankAccountMap");
        if (suspectEmployeeBankAccountMap == null) {
            suspectEmployeeBankAccountMap = SuspectBankAccountInfo.loadSuspectBankAccountMap();
            Application.getSessionCache().addNonHibernateObject("SuspectEmployeeBankAccountMap", suspectEmployeeBankAccountMap);
        }

        List<Object[]> billPaymentSplitBankAccounts = Application.executeQuery(BillPayment.class, new Query<BillPayment>()
                                                        .Select(BillPayment.BillPaymentSplitSet().Filter().PayeeBankAccount(),
                                                                BillPayment.BillPaymentSplitSet().Filter().PayeeBankAccount().BankAccount().RoutingNumber(),
                                                                BillPayment.BillPaymentSplitSet().Filter().PayeeBankAccount().BankAccount().AccountNumberEnc(),
                                                                BillPayment.BillPaymentSplitSet().Filter().PayeeBankAccount().BankAccount().AccountTypeCd())
                                                        .Where(BillPayment.PayrollRun().equalTo(this))
                                                        .OrderBy(BillPayment.BillPaymentSplitSet().Filter().PayeeBankAccount().BankAccount().RoutingNumber(),
                                                                BillPayment.BillPaymentSplitSet().Filter().PayeeBankAccount().BankAccount().AccountNumberEnc(),
                                                                BillPayment.BillPaymentSplitSet().Filter().PayeeBankAccount().BankAccount().AccountTypeCd()));

        for (Object[] billPaymentSplitBankAccount : billPaymentSplitBankAccounts) {
            PayeeBankAccount payeeBankAccount = (PayeeBankAccount) billPaymentSplitBankAccount[0];
            String bankRoutingNumber = (String) billPaymentSplitBankAccount[1];
            String bankAccountNumber = EncryptionUtils.deterministicDecrypt(BankAccount.AccountNumberKeyName,(String) billPaymentSplitBankAccount[2]);
            String bankAccountTypeCd = ((BankAccountType) billPaymentSplitBankAccount[3]).name();

            String key = String.format("%s:%s:%s", bankRoutingNumber, bankAccountNumber, bankAccountTypeCd);
            ArrayList<SuspectBankAccountInfo> likeEmployeeBankAccounts = suspectEmployeeBankAccountMap.get(key);
            if (likeEmployeeBankAccounts != null && !likeEmployeeBankAccounts.isEmpty()) {
                SuspectBankAccountInfo likeEmployeeBankAccount = likeEmployeeBankAccounts.get(0);

                String likeCompanyId = likeEmployeeBankAccount.companyId;

                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Add the employee bank account in terminated company event
                String note = "Payee bank account " + bankAccountNumber +
                        " for employee " + payeeBankAccount.getPayee().getName() +
                        ", matches employee/payee bank account " + likeEmployeeBankAccount.accountNumber +
                        " for " + likeEmployeeBankAccount.accountOwnerName +
                        " in company " + likeCompanyId + ".";

                CompanyEvent event = CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeeBankAccountInTermedCompany, this, note, payeeBankAccount.getPayee());
                event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId, getSourcePayRunId());
                event.addCompanyEventDetail(EventDetailTypeCode.PayeeBankAccountId, payeeBankAccount.getId().toString());

                createdEvent = true;

            }
        }

        return createdEvent;
    }

    public boolean numberOfPayrollsPerDayExceeded() {
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(
                getCompany(), EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);
        boolean createdEvent = false;

        if (companyEventsList.size() == 0) {
            Long as400PayrollCount = 0L;
            if (getCompany().getQuickbooksInfo() != null) {
                as400PayrollCount = getCompany().getQuickbooksInfo().getAS400PayrollCount();
            }

            int numberOfCompletePayrollRuns = PayrollRun.getPayrollRunCountByType(getCompany(), PayrollStatus.Complete, PayrollType.Regular, PayrollType.BillPayment);
            if ((numberOfCompletePayrollRuns + as400PayrollCount.intValue()) < 6) {
                SpcfCalendar fromDate = getPayrollRunDate().toLocal().copy();
                fromDate.addDays(-3);

                SpcfCalendar toDate = getPayrollRunDate().toLocal().copy();
                toDate.addDays(3);

                DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRunsByType(
                        getCompany(), fromDate, toDate, PayrollType.Regular, PayrollType.BillPayment);

                if (payrollRunList.size() > 2) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create NumberOfPayrollsPerDayExceeded event
                    String note = "Company ran more than 2 payrolls in a 3 day period for payroll with check date "
                            + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                    Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.NumberOfPayrollsPerDayExceeded, this, note));
                    createdEvent = true;
                }
            }
        }
        return createdEvent;
    }

    public void employeePaidGreaterThanMax() {
        // Pull the Max EE Paycheck Amount and number of payrolls for Fraud Consideration from the SP Params

        String fraudEEPaidMax = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEPaidMax).getValue();
        SpcfMoney amountToCompare = new SpcfMoney(fraudEEPaidMax);

        int fraudEEPaidMaxXPayrolls = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEPaidMaxXPayrolls).getValue());

        Long numberOfCompletePayrollRuns = getCompany().getNumberOfCompleteDDPayrolls();

        if (numberOfCompletePayrollRuns < fraudEEPaidMaxXPayrolls) {
            for (Paycheck check : findDDPaychecksForFraud()) {
                SpcfMoney totalPaycheckAmt = new SpcfMoney("0.00");
                for (PaycheckSplit split : check.getPaycheckSplitCollection()) {
                    totalPaycheckAmt = new SpcfMoney(totalPaycheckAmt.add(split.getPaycheckSplitAmount()));
                }

                if (totalPaycheckAmt.isGreaterThan(amountToCompare)) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create event
                    String note = "EE " + check.getDDEmployee().getFirstName() + ":" + check.getDDEmployee().getLastName()
                            + " paid " + totalPaycheckAmt + " in a single paycheck for payroll with check date "
                            + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                    Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeePaidGreaterThanMax, this, note, check.getDDEmployee()));
                }
            }
        }
    }

    public void employeePaidGreaterThanMaxForBankAcctUpdate() {
        // Pull the Max EE Paycheck Amount and number of days of bank account update for Fraud Consideration from the SP Params

        String fraudEEAcctUpdateMax = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEAcctUpdateMax).getValue();
        SpcfMoney amountToCompare = new SpcfMoney(fraudEEAcctUpdateMax);

        int fraudEENumberOfDaysBankAcctUpdated = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEAcctUpdateXDays).getValue());

        SpcfCalendar dateToCompare = getPayrollRunDate().toLocal().copy();
        dateToCompare.addDays(fraudEENumberOfDaysBankAcctUpdated * -1);

            for (Paycheck check : findDDPaychecksForFraud()) {

                SpcfMoney totalPaycheckAmt = new SpcfMoney("0.00");
                int empBankAcctChangedCount=0;
                for (PaycheckSplit split : check.getPaycheckSplitCollection()) {
                    totalPaycheckAmt = new SpcfMoney(totalPaycheckAmt.add(split.getPaycheckSplitAmount()));
                    EmployeeBankAccount eBA = EmployeeBankAccount.findEmployeeBankAccount(split.getPaycheck().getDDEmployee(), split.getEmployeeBankAccount().getSourceBankAccountId());
                    if (eBA == null || eBA.getStatusEffectiveDate() == null) {
                        String property = eBA == null ? "EmployeeBankAccount" : "EmployeeBankAccount.StatusEffectiveDate";
                        logger.error("WARNING -- skipping employeeBankAccountChanged fraud check for bank account due to: " + property + "== NULL: PaycheckId:DDTxnId:EESrcBankId --" + split.getPaycheck().getSourcePaycheckId() + ":" + split.getSourceDdTxnId() + ":" + split.getEmployeeBankAccount().getSourceBankAccountId());
                        continue;
                    }
                    if(eBA.getStatusEffectiveDate().after(dateToCompare)){
                        empBankAcctChangedCount++;
                    }

                }

                if (totalPaycheckAmt.isGreaterThan(amountToCompare) && empBankAcctChangedCount>0 ) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create event
                    String note = "EE " + check.getDDEmployee().getFirstName() + ":" + check.getDDEmployee().getLastName()
                            + " paid " + totalPaycheckAmt + " in a single paycheck for payroll with check date "
                            + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT)+
                            " within "+fraudEENumberOfDaysBankAcctUpdated+" days of employee bank account update";

                    Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeePaidGreaterThanMax, this, note, check.getDDEmployee()));
                }

            }

    }

    private List<Paycheck> findDDPaychecksForFraud() {
        Expression<Paycheck> expression = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().equalTo(this)
                        .And(Paycheck.PaycheckSplitSet().Exists(PaycheckSplit.<PaycheckSplit>Id().isNotNull())))
                .EagerLoad(Paycheck.DDEmployee(),
                        Paycheck.PaycheckSplitSet(),
                        Paycheck.PaycheckSplitSet().Filter().FinancialTransaction())
                .ReadOnly(true);
        return new ArrayList<Paycheck>(Application.find(Paycheck.class, expression));
    }

    public void employeeBankAccountChanged() {
        // Get the parameter values for the fraud check

        Company company = getCompany();

        int fraudEENumberOfDaysBankAcctUpdated = Integer.parseInt(FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudEENumberOfDaysBankAcctUpdated).getValue());

        SpcfCalendar dateToCompare = getPayrollRunDate().toLocal().copy();
        dateToCompare.addDays(fraudEENumberOfDaysBankAcctUpdated * -1);

        int fraudEENumberOfPaychecksSpikeInPay = Integer.parseInt(FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudEENumberOfPaychecksSpikeInPay).getValue());


        SpcfMoney fraudEEPercentGreaterThanAverage = new SpcfMoney(FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudEEPercentGreaterThanAverage).getValue());


        for (Paycheck check : getDDPaycheckCollection()) {
            // Check if bank account has been updated in the last X days
            for (PaycheckSplit split : check.getPaycheckSplitCollection()) {
                //Find out if bank account has been modified
                EmployeeBankAccount eBA = EmployeeBankAccount.findEmployeeBankAccount(split.getPaycheck().getDDEmployee(), split.getEmployeeBankAccount().getSourceBankAccountId());
                if (eBA == null || eBA.getStatusEffectiveDate() == null) {
                    String property = eBA == null ? "EmployeeBankAccount" : "EmployeeBankAccount.StatusEffectiveDate";
                    logger.error("WARNING -- skipping employeeBankAccountChanged fraud check for bank account due to: " + property + "== NULL: PaycheckId:DDTxnId:EESrcBankId --" + split.getPaycheck().getSourcePaycheckId() + ":" + split.getSourceDdTxnId() + ":" + split.getEmployeeBankAccount().getSourceBankAccountId());
                    continue;
                }

                if (eBA.getStatusEffectiveDate().after(dateToCompare)) {
                    // If Bank Account has been updated, calculate the average amount of the last X paycheck splits that were paid to this account

                    DomainEntitySet<PaycheckSplit> splits;
                    if (getCompany().hasService(ServiceCode.RiskAssessment)) {
                        splits = PaycheckSplit.findPaycheckSplitsByEmployeeBankAccountForIOP(company, split.getEmployeeBankAccount(), this);
                    } else {
                        splits = PaycheckSplit.findPaycheckSplitsByEmployeeBankAccount(company, split.getEmployeeBankAccount(), this);
                    }

                    SpcfMoney averageSplitAmount = new SpcfMoney("0.00");
                    int count = 0;
                    for (PaycheckSplit pcs : splits) {
                        // Todo add comment here
                        if (!pcs.equals(split) && count < fraudEENumberOfPaychecksSpikeInPay) {
                            count++;
                            averageSplitAmount = new SpcfMoney(averageSplitAmount.add(pcs.getPaycheckSplitAmount()));

                        }
                    }
                    if (count > 0) {
                        SpcfDecimal decimalCount = SpcfDecimal.createInstance(count);
                        averageSplitAmount = new SpcfMoney(averageSplitAmount.divide(decimalCount));
                        if (isMoreThanXPercent(averageSplitAmount, split.getPaycheckSplitAmount(), fraudEEPercentGreaterThanAverage)) {
                            //Flag the Company for Fraud
                            getCompany().setFraudFlag();

                            //Create event
                            Employee employee = split.getEmployeeBankAccount().getEmployee();
                            String note = "EE " + employee.getFirstName() + ":" + employee.getLastName()
                                    + " paid over " + fraudEEPercentGreaterThanAverage + "% more than the average of the last " + count + " paychecks in payroll with paycheck date of "
                                    + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                            CompanyEvent event = CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeeBankAccountChangedSpikeInPay, this, note, employee);
                            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeBankAccountId, split.getEmployeeBankAccount().getSourceBankAccountId());
                            Application.save(event);
                        }
                    }
                }

            }
        }
    }

    public void employeesPaidToTheSameBank() {
        FraudValueType numberOfPayrollsType;
        FraudValueType percentType;
        FraudValueType totalType;
        if (getPayrollRunType() == PayrollType.BillPayment) {
            numberOfPayrollsType = FraudValueType.FraudBPNumberOfPaymentsToCheckSameBank;
            percentType = FraudValueType.FraudBPPercentPayeesPaidSameBank;
            totalType = FraudValueType.FraudBPTotalPayeesToCheckSameBank;
        } else {
            numberOfPayrollsType = FraudValueType.FraudPRNumberOfPayrollsToCheckSameBank;
            percentType = FraudValueType.FraudPRPercentEmployeesPaidSameBank;
            totalType = FraudValueType.FraudPRTotalEmployeesToCheckSameBank;
        }

        // Get the parameter values for the fraud check
        int fraudPRNumberOfPayrollsToCheckSameBank = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(numberOfPayrollsType).getValue());

        // Only check if the number of payrolls submitted by the company is less or equal the parameter
        int numberOfPayrolls = findPayrollRunsBySettlementDate(getCompany(), null, null)
                .find(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Complete)).size();
        if (numberOfPayrolls <= fraudPRNumberOfPayrollsToCheckSameBank) {

            // Get the percentage of employees paid to the same bank to check

            SpcfMoney fraudPRPercentEmployeesPaidSameBank = new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(percentType).getValue());

            // Get the number of employees paid in this payroll run
            int numberOfEmployees = getNumberOfEmployeesAndPayeesPaid();

            // verify the number of employees paid in this payroll run meets threshold for same bank check rule execution
            int fraudPRTotalEmployeesToCheckSameBank = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(totalType).getValue());
            if (numberOfEmployees < fraudPRTotalEmployeesToCheckSameBank)
                return;

            HashMap<String, Set<DomainEntity>> bankList = new HashMap<String, Set<DomainEntity>>();

            for (Paycheck check : getDDPaycheckCollection()) {
                for (PaycheckSplit split : check.getPaycheckSplitCollection()) {
                    String routingNumber = split.getEmployeeBankAccount().getBankAccount().getRoutingNumber();
                    if (!bankList.containsKey(routingNumber)) {
                        bankList.put(routingNumber, new HashSet<DomainEntity>());
                    }
                    bankList.get(routingNumber).add(check.getDDEmployee());
                }
            }

            for (BillPayment billPayment : getBillPaymentCollection()) {
                for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                    String routingNumber = billPaymentSplit.getPayeeBankAccount().getBankAccount().getRoutingNumber();
                    if (!bankList.containsKey(routingNumber)) {
                        bankList.put(routingNumber, new HashSet<DomainEntity>());
                    }
                    bankList.get(routingNumber).add(billPayment.getPayee());
                }
            }

            // Get the percentage of employees paid to the same bank
            // Divide the number of employees paid to the same bank by the total number of employees paid in this payroll
            // Use a 2 precision so when the result is multiplied by 100 the percent value will have no decimals
            // Use a "Down" Rounding type to truncate the number on the second decimal
            // For example 0.751, 0.755 and 0.759 will become 75% with this precision/rounding combination

            SpcfDecimal totalNumberOfEEsInPayroll = SpcfDecimal.createInstance(numberOfEmployees);

            for (String routingNumber : bankList.keySet()) {
                SpcfDecimal eEsPaidToBank = SpcfDecimal.createInstance(bankList.get(routingNumber).size());

                SpcfDecimal percent = eEsPaidToBank.divide(totalNumberOfEEsInPayroll, 2, SpcfDecimal.SpcfRoundingType.Down);
                percent = percent.multiply(SpcfDecimal.createInstance(100));

                if (percent.compareTo(fraudPRPercentEmployeesPaidSameBank) >= 0) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create LargePercentageOfEEsPaidToSameBank event
                    String note = "More than " + fraudPRPercentEmployeesPaidSameBank + "% of employees being paid to the same bank with routing number "
                            + routingNumber;

                    CompanyEvent event = CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeesPaidToSameBank, this, note);
                    event.addCompanyEventDetail(EventDetailTypeCode.Percentage, percent.toString());
                    Application.save(event);
                }
            }
        }
    }


    public void employeesPaidToTheSameBankAccount() {
        HashMap<String, HashMap<String, String>> employeesPaidToBankAccounts = new HashMap<String, HashMap<String, String>>();
        HashMap<String, BankAccount> banks = new HashMap<String, BankAccount>();

        SpcfCalendar checkDate = this.getPaycheckDate();

        DomainEntitySet<PaycheckSplit> employeeSplitsForCheckDate = null;
        DomainEntitySet<FinancialTransaction> employeeTxsForCheckDate = null;
        // Retrieve employee transactions for other payrolls on this check date so we can do the fraud validation
        if (getCompany().hasService(ServiceCode.RiskAssessment)) {
            employeeSplitsForCheckDate = PaycheckSplit.findFraudCheckPaycheckSplits(this.getCompany(), checkDate);
        } else {
            employeeTxsForCheckDate = FinancialTransaction.findFraudCheckFinancialTxns(this.getCompany(), checkDate);
        }

        // Iterate the paycheck splits, finding the bank account for each
        // If we haven't already examined the bank account, add the employee for the paycheck to a set of ees paid to the ba on the check date
        //      In addition, find ees paid to the ba on that check date that have already been persisted, and add them to the set
        // If we have already examined the bank account, just add the ee for the paycheck to the set of ees paid to the ba for the check date
        for (Paycheck check : getDDPaycheckCollection()) {
            for (PaycheckSplit split : check.getPaycheckSplitCollection()) {
                String sourceBankAccountAcctNum = split.getEmployeeBankAccount().getBankAccount().getAccountNumber();
                String sourceBankAccountRoutingNum = split.getEmployeeBankAccount().getBankAccount().getRoutingNumber();
                String sourceBankAccountKey = sourceBankAccountRoutingNum + ":" + sourceBankAccountAcctNum;

                HashMap<String, String> employeesForBankAccount = null;

                if (employeesPaidToBankAccounts.containsKey(sourceBankAccountKey)) {
                    employeesForBankAccount = employeesPaidToBankAccounts.get(sourceBankAccountKey);
                    if (!employeesForBankAccount.containsKey(check.getDDEmployee().getSourceEmployeeId())) {
                        employeesForBankAccount.put(check.getDDEmployee().getSourceEmployeeId(), split.getSourceDdTxnId());
                    }
                } else {
                    //first time we have seen this bank account.  Populate it with this record, and also persisted records
                    employeesForBankAccount = new HashMap<String, String>();

                    employeesForBankAccount.put(check.getDDEmployee().getSourceEmployeeId(), split.getSourceDdTxnId());

                    //check persisted records
                    if (getCompany().hasService(ServiceCode.RiskAssessment)) {
                        for (PaycheckSplit paycheckSplit : employeeSplitsForCheckDate) {
                            if (paycheckSplit.getEmployeeBankAccount().getBankAccount().getAccountNumber().equals(sourceBankAccountAcctNum) &&
                                    paycheckSplit.getEmployeeBankAccount().getBankAccount().getRoutingNumber().equals(sourceBankAccountRoutingNum)) {
                                String srcEmployeeId = paycheckSplit.getPaycheck().getDDEmployee().getSourceEmployeeId();
                                if (!employeesForBankAccount.containsKey(srcEmployeeId)) {
                                    employeesForBankAccount.put(srcEmployeeId, paycheckSplit.getSourceDdTxnId());
                                }
                            }
                        }
                    } else {
                        for (FinancialTransaction finTx : employeeTxsForCheckDate) {
                            if (finTx.getCreditBankAccount().getAccountNumber().equals(sourceBankAccountAcctNum) &&
                                    finTx.getCreditBankAccount().getRoutingNumber().equals(sourceBankAccountRoutingNum)) {
                                PaycheckSplit paycheckSplit = finTx.getPaycheckSplit();
                                String srcEmployeeId = paycheckSplit.getPaycheck().getDDEmployee().getSourceEmployeeId();
                                if (!employeesForBankAccount.containsKey(srcEmployeeId)) {
                                    employeesForBankAccount.put(srcEmployeeId, paycheckSplit.getSourceDdTxnId());
                                }
                            }
                        }
                    }

                    employeesPaidToBankAccounts.put(sourceBankAccountKey, employeesForBankAccount);

                    //also update bank account map
                    banks.put(sourceBankAccountKey, split.getEmployeeBankAccount().getBankAccount());
                }
            }
        }

        //now test
        int fraudPREmployeesSameBankAccountMax = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPREmployeesSameBankAccountMax).getValue());

        for (Map.Entry<String, HashMap<String, String>> employeesForBankAccount : employeesPaidToBankAccounts.entrySet()) {
            if (employeesForBankAccount.getValue().values().size() > fraudPREmployeesSameBankAccountMax) {
                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Create EmployeesPaidToSameBankAccount event
                String note = createNoteNumberOfEEsPaidToBA(employeesForBankAccount.getValue().values(),
                        banks.get(employeesForBankAccount.getKey()),
                        fraudPREmployeesSameBankAccountMax);
                Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeesPaidToSameBankAccount, this, note));
            }
        }
    }



    private String createNoteNumberOfEEsPaidToBA(Collection<String> pEEPacheckSplitIds,
                                                 BankAccount bankAccount,
                                                 int fraudPREmployeesSameBankAccountMax
    ) {
        NumberFormat usNumberFormat = NumberFormat.getCurrencyInstance(Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");

        //Main note message
        String message = "More than " + fraudPREmployeesSameBankAccountMax + " unique employees " +
                "paid into a single bank account on the same day \n\n";

        //Sort the items by payroll Id
        PaycheckSplit paycheckSplit;
        String paycheckSplitId;
        String srcPayrollRunId;

        Map<String, PaycheckSplit> paycheckSplitMap = new HashMap<String, PaycheckSplit>();
        Map<String, PaycheckSplit> paycheckSplitByPayrollRunId = new HashMap<String, PaycheckSplit>();
        HashMap<String, ArrayList<String>> payrollRunMap = new HashMap<String, ArrayList<String>>();
        for (String id : pEEPacheckSplitIds) {
            if (getCompany().hasService(ServiceCode.RiskAssessment)) {
                paycheckSplit = PaycheckSplit.findFraudCheckPaycheckSplits(this.getCompany(), id);
            } else {
                paycheckSplit = PaycheckSplit.findNonCanceledPaycheckSplit(this.getCompany(), id);
            }
            if (paycheckSplit != null) {
                paycheckSplitId = paycheckSplit.getSourceDdTxnId();
                srcPayrollRunId = paycheckSplit.getPaycheck().getPayrollRun().getSourcePayRunId();
                // save the paycheck splits in to maps to use later
                paycheckSplitMap.put(paycheckSplitId, paycheckSplit);
                paycheckSplitByPayrollRunId.put(srcPayrollRunId, paycheckSplit);

                ArrayList<String> paycheckSplitIdList = payrollRunMap.get(srcPayrollRunId);
                if (paycheckSplitIdList == null) paycheckSplitIdList = new ArrayList<String>();
                paycheckSplitIdList.add(paycheckSplitId);
                payrollRunMap.put(srcPayrollRunId, paycheckSplitIdList);
            }
        }


        StringBuffer note = new StringBuffer(message);
        //Add the employee bankaccount the failed
        if (bankAccount != null) {
            if (bankAccount.getBankName() != null) {
                note = note.append("Bank Name: ").append(bankAccount.getBankName()).append("\n");
            }
            note = note.append("Bank Routing: ").append(bankAccount.getRoutingNumber()).append("\n");
            note = note.append("Account Type: ").append(bankAccount.getAccountTypeCd().toString()).append("\n");
            note = note.append("Bank Account: ").append(bankAccount.getAccountNumber()).append("\n\n");
        }

        String payrollDateString = null;
        SpcfMoney netAmount = null;
        //loop thru each of the batches
        List<String> keys = new ArrayList<String>(payrollRunMap.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            ArrayList<String> paycheckSplitIdList = payrollRunMap.get(key);
            Collections.sort(paycheckSplitIdList);
            Employee employee = null;
            String maxlimitEmployeeName = "";

            note = note.append("Source Payroll Id: ").append(key).append("\n");
            paycheckSplit = paycheckSplitByPayrollRunId.get(key);
            SpcfCalendar paycheckDate = null;
            if (paycheckSplit != null) {
                paycheckDate = paycheckSplit.getPaycheck().getPayrollRun().getPaycheckDate().toLocal();
                payrollDateString = dateFormat.format(
                        CalendarUtils.convertToDate(paycheckDate));
                netAmount = paycheckSplit.getPaycheck().getPayrollRun().getPayrollDirectDepositAmount();
            }
            note = note.append("Payroll Date: ").append(payrollDateString).append("\n");
            if (netAmount != null) {
                note = note.append("Payroll Amount: ").append(usNumberFormat.format(SpcfUtils.convertToBigDecimal(netAmount))).append("\n");
            }
            note = note.append("\n");

            for (String payCheckSplitId : paycheckSplitIdList) {
                paycheckSplit = paycheckSplitMap.get(payCheckSplitId);

                //Add the employee details to the note
                if (paycheckSplit != null) {
                    employee = paycheckSplit.getPaycheck().getDDEmployee();
                    netAmount = paycheckSplit.getPaycheckSplitAmount();
                }

                if (employee != null) {
                    maxlimitEmployeeName = employee.getFirstName() +
                            " " + employee.getLastName();
                }

                note = note.append("Employee Name: ").append(maxlimitEmployeeName).append("\n");
                if (netAmount != null) {
                    note = note.append("Paycheck Amount: ").append(usNumberFormat.format(
                            SpcfUtils.convertToBigDecimal(netAmount))).append("\n");
                }
                note.append("\n");
            }
        }

        return note.toString();
    }

    public void employeePaidTooManyTimes() {
        // Get number of times an employee has been paid parameter to check for Fraud Consideration
        int fraudEEPaidXTimes = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEPaidXTimes).getValue());

        // Get number of days to count the number of paychecks for an employee

        int fraudEENumberOfDays = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEENumberOfDaysMultiplePaychecks).getValue());
        SpcfCalendar fromDate = getPayrollRunDate().copy();
        fromDate.addDays(fraudEENumberOfDays * -1);

        DomainEntitySet<PayrollRun> payrollRuns = findEarlierPayrollRuns(fromDate);

        // only include payrolls that have dd debits
        if (!getCompany().hasService(ServiceCode.RiskAssessment)) {
            payrollRuns = payrollRuns.find(PayrollRun.PayrollDirectDepositAmount().greaterThan(SpcfMoney.ZERO));
        }

        for (Paycheck check : getDDPaycheckCollection()) {
            int numberOfPaychecks = 0;
            //Find the paychecks for the same employee within the collection of payroll runs
            for (PayrollRun payrollRun : payrollRuns) {

                Criterion<Paycheck> criteria = Paycheck.DDEmployee().Id().equalTo(check.getDDEmployee().getId());

                DomainEntitySet<Paycheck> paychecks = payrollRun.getDDPaycheckCollection().find(criteria);
                // Add 1 for each separate payroll run that the employee has been paid
                if (paychecks.size() > 0) {
                    numberOfPaychecks = numberOfPaychecks + 1;
                }
            }


            if (numberOfPaychecks >= fraudEEPaidXTimes) {
                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Create EmployeePaidTooManyTimes event
                String note = "EE " + check.getDDEmployee().getFirstName() + ":" + check.getDDEmployee().getLastName()
                        + " paid " + numberOfPaychecks + " times in a period of " + fraudEENumberOfDays + " days.";

                Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeePaidTooManyTimes, this, note, check.getDDEmployee()));
            }
        }
    }

    public FinancialTransaction getDdDebit() {
        return getFinancialTransactions(TransactionTypeCode.EmployerDdDebit).getFirst();
    }

    public FinancialTransaction getTaxDebit() {
        return getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit).getFirst();
    }

    public static boolean isMoreThanXPercent(SpcfMoney pAmount1, SpcfMoney pAmount2, SpcfMoney pPercent) {

        SpcfMoney multiplier = new SpcfMoney(".01");
        SpcfMoney netAmount = new SpcfMoney(pAmount1.multiply(pPercent.multiply(multiplier)));
        netAmount = new SpcfMoney(netAmount.add(pAmount1));

        if (pAmount2.equals(new SpcfMoney("0.00")) && netAmount.equals(new SpcfMoney("0.00"))) {
            return false;
        }

        return pAmount2.compareTo(netAmount) >= 0;
    }

    public boolean isERTaxDebitCollected() {
        return getEmployerTaxDebitTransactions().find(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Completed))).isNotEmpty();
    }

    public DomainEntitySet<FinancialTransaction> getEmployerTaxDebitTransactions() {
        return getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerTaxRedebit}, null);
    }

    public FinancialTransaction getEmployerTaxDebitTransaction() {
        DomainEntitySet<FinancialTransaction> taxFTs = getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));

        if (taxFTs != null && taxFTs.size() > 0) {
            return taxFTs.get(0);
        }

        return null;
    }

    public FinancialTransaction getEmployerTaxDirectDebitTransaction() {
        DomainEntitySet<FinancialTransaction> taxFTs = getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));

        if (taxFTs != null && taxFTs.size() > 0) {
            return taxFTs.get(0);
        }

        return null;
    }

    public FinancialTransaction getEmployerDirectDepositDebitTransaction() {
        DomainEntitySet<FinancialTransaction> taxFTs = getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));

        if (taxFTs != null && taxFTs.size() > 0) {
            return taxFTs.get(0);
        }

        return null;
    }

    public DomainEntitySet<FinancialTransaction> getTransactionByTypeAndStatus(TransactionTypeCode transactionTypeCode, List<TransactionStateCode> transactionStateCodes, 
    		SettlementType settlementType) {
        DomainEntitySet<FinancialTransaction> financialTransactionSet = getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(transactionTypeCode)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(transactionStateCodes))
                        .And(FinancialTransaction.SettlementTypeCd().equalTo(settlementType)));

        return financialTransactionSet;
    }

    public FinancialTransaction getCreatedEmployerTaxDirectDebitTransaction() {
        DomainEntitySet<FinancialTransaction> taxFTs =
                getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

        if (taxFTs != null && taxFTs.size() > 0) {
            return taxFTs.get(0);
        }

        return null;
    }

    /**
     * @return
     */
    public boolean hasImpoundDebit() {

        DomainEntitySet<FinancialTransaction> taxFTs =
                getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        return (taxFTs != null && taxFTs.size() > 0);
    }

    public boolean hasFeeDebit() {
        return getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)).size() > 0;
    }

    public SpcfCalendar getImpoundSettlementDate() {
        DomainEntitySet<FinancialTransaction> impoundFts =
                getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        if(impoundFts != null && impoundFts.size() > 0){
            return impoundFts.getFirst().getSettlementDate();
        }

        return null;
    }

    public static DomainEntitySet<PayrollRun> findPayrollRunsByFraudBatchFlag(Integer pBatchSize) {
        Expression query = new Query<PayrollRun>()
                .Where(PayrollRun.ProcessedByFraudBatchJob().equalTo(false))
                .OrderBy(PayrollRun.CreatedDate())
                .EagerLoad(PayrollRun.Company());

        if (pBatchSize != null) {
            query = ((Query) query).LimitResults(0, pBatchSize);
        }

        return Application.find(PayrollRun.class, query);
    }

    public HashMap<FinancialTransaction, SpcfMoney> getCollectedDDAmount() {
        // in order to be collected the transactions must be in a complete state
        DomainEntitySet<FinancialTransaction> ddFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned});

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        if (ddFTs != null && ddFTs.size() > 0) {
            FinancialTransaction erDebitTx = ddFTs.get(0);
            TransactionSummary summary = erDebitTx.summarizeRelatedTransactions();

            if (summary.amtCollected.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(erDebitTx, new SpcfMoney(summary.amtCollected));
            } else {
                map.put(erDebitTx, new SpcfMoney(SpcfMoney.ZERO));
            }
        }

        return map;
    }

    public boolean hasVoidedPaycheck() {
        return getPaycheckCollection().find(Paycheck.CompanyAdjustmentSubmission().isNotNull()).size() > 0;
    }

    public boolean hasVoidedOrRecalledPaycheck() {
        return getPaycheckCollection().find(
                Paycheck.CompanyAdjustmentSubmission().isNotNull()
                        .Or(Paycheck.Status().equalTo(PaycheckStatusCode.Inactive))).size() > 0;
    }

    public void employeePaidPercentageGreaterThanOthers() {
        // Get the allowed percentage to check for Fraud Consideration
        SpcfMoney percentAllowed =  new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEPercentGreaterThanOtherEEs).getValue());

        // Get number of days to count from the employee's hire date
        int fraudEENewEmployeeAddedXDays = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEENewEmployeeAddedXDays).getValue());

        SpcfCalendar fromDate = getPayrollRunDate().copy();
        fromDate.addDays(fraudEENewEmployeeAddedXDays * -1);

        // verify that there are actually employees who are new
        // -- if employee's hire date is within the checked period
        DomainEntitySet<Paycheck> newEmployeePaychecks =
                getDDPaycheckCollection().find(Paycheck.DDEmployee().StatusEffectiveDate().greaterThan(fromDate));
        if (newEmployeePaychecks.size() == 0)
            return;

        // verify that there are existing employees in payroll run to compare net amounts to
        DomainEntitySet<Paycheck> existingEmployeePaychecks =
                getDDPaycheckCollection().find(Paycheck.DDEmployee().StatusEffectiveDate().lessOrEqualThan(fromDate));
        if (existingEmployeePaychecks.size() == 0)
            return;

        HashMap<SpcfUniqueId, SpcfMoney> amtsPerNewEmployeeForPayroll = getNetAmountPerEmployee(newEmployeePaychecks);
        HashMap<SpcfUniqueId, SpcfMoney> amtsPerEmployeeForPayroll = getNetAmountPerEmployee(existingEmployeePaychecks);

        // find the max non-new employee amount
        List<SpcfMoney> sortedAmts = new ArrayList<SpcfMoney>(amtsPerEmployeeForPayroll.values());
        Collections.sort(sortedAmts);
        SpcfMoney maxExistingEmpAmount = sortedAmts.get(sortedAmts.size() - 1);

        for (SpcfUniqueId newEmpId : amtsPerNewEmployeeForPayroll.keySet()) {
            //Get the total amount for the paycheck
            SpcfMoney newEmployeeAmt = amtsPerNewEmployeeForPayroll.get(newEmpId);
            if (isMoreThanXPercent(maxExistingEmpAmount, newEmployeeAmt, percentAllowed)) {
                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Create EmployeePaidPercentageGreaterThanOthers event
                Employee newEmployee = Application.findById(Employee.class, newEmpId);
                String note = "EE " + newEmployee.getFirstName() + ":" + newEmployee.getLastName()
                        + " paid over " + percentAllowed + "% more than any other EE in payroll with check date "
                        + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeePaidPercentageGreaterThanOthers, this, note, newEmployee));
            }
        }
    }

    public void totalPayrollExceedsLimit() {
        // Pull the Max Payroll Amount and number of payrolls for Fraud Consideration from the SP Params
        SpcfMoney amountToCompare = new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPRMax).getValue());
        int fraudPRMaxXPayrolls = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPRMaxXPayrolls).getValue());

        Long numberOfCompletePayrollRuns = getCompany().getNumberOfCompleteDDPayrolls();

        SpcfDecimal totalAmount;
        if (getCompany().hasService(ServiceCode.RiskAssessment)) {
            totalAmount = SpcfDecimal.createInstance(0.00);
            for (Paycheck paycheck : getDDPaycheckCollection()) {
                totalAmount = totalAmount.add(paycheck.getNetAmount());
            }
        } else {
            totalAmount = getPayrollDirectDepositAmount();
        }

        if (numberOfCompletePayrollRuns < fraudPRMaxXPayrolls) {
            if (totalAmount.isGreaterThan(amountToCompare)) {
                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Create event
                String note = "Company ran a total payroll of amount " + totalAmount +
                        " for payroll with check date "
                        + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.TotalPayrollExceedsLimit, this, note));
            }
        }
    }

    /**
     * Method to get the net amount for each employee in the paycheck list.
     *
     * @param pPaychecks DomainEntitySet<Paycheck>
     * @return HashMap<SpcfUniqueId, SpcfMoney>
     */
    private HashMap<SpcfUniqueId, SpcfMoney> getNetAmountPerEmployee(DomainEntitySet<Paycheck> pPaychecks) {
        HashMap<SpcfUniqueId, SpcfMoney> amountsPerEmployee = new HashMap<SpcfUniqueId, SpcfMoney>();

        if (pPaychecks != null) {
            //iterate paychecks
            for (Paycheck currPaycheck : pPaychecks.find(Paycheck.DDEmployee().isNotNull())) {
                SpcfUniqueId spsEEId = currPaycheck.getDDEmployee().getId();
                SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
                for (PaycheckSplit split : currPaycheck.getPaycheckSplitCollection()) {
                    if (getCompany().hasService(ServiceCode.RiskAssessment)) {
                        if (currPaycheck.getStatus().equals(PaycheckStatusCode.Active)) {
                            totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(split.getPaycheckSplitAmount());
                        }
                    } else {
                        if (split.getFinancialTransaction() != null &&
                                split.getFinancialTransaction().getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Cancelled) {
                            totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(split.getPaycheckSplitAmount());
                        }
                    }
                }

                if (!amountsPerEmployee.containsKey(spsEEId)) {
                    //add id to hashmap as key if does not exist
                    amountsPerEmployee.put(spsEEId, totalPaycheckNetAmount);
                } else {
                    //add amount to current value in hashmap
                    SpcfMoney combinedAmount = (SpcfMoney) amountsPerEmployee.get(spsEEId).add(totalPaycheckNetAmount);
                    amountsPerEmployee.put(spsEEId, combinedAmount);
                }
            }
        }
        return amountsPerEmployee;
    }

    /**
     * @return number of unique employees with paycheck splits + number of unique payees with bill payment splits.
     * Assumes nothing has been cancelled
     */
    private int getNumberOfEmployeesAndPayeesPaid() {
        Set<DomainEntity> employeeAndPayeeSet = new HashSet<DomainEntity>();
        for (Paycheck paycheck : getPaycheckCollection()) {
            if (paycheck.getPaycheckSplitCollection().isNotEmpty()) {
                employeeAndPayeeSet.add(paycheck.getDDEmployee());
            }
        }
        for (BillPayment billPayment : getBillPaymentCollection()) {
            if (billPayment.getBillPaymentSplitCollection().isNotEmpty()) {
                employeeAndPayeeSet.add(billPayment.getPayee());
            }
        }
        return employeeAndPayeeSet.size();
    }


    public void employeePaidEvenDollarAmount() {
        // Pull the number of payrolls for Fraud Consideration from the SP Params
        int fraudEERoundPaidXPayrolls = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEERoundPaidXPayrolls).getIntValue();

        Long numberOfCompletePayrollRuns = getCompany().getNumberOfCompleteDDPayrolls();
        String fraudEERoundPaidXAmount = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEERoundPaidXAmount).getValue();
        SpcfMoney amountToCompare = new SpcfMoney(fraudEERoundPaidXAmount);

        if (numberOfCompletePayrollRuns < fraudEERoundPaidXPayrolls) {
            for (Paycheck check : findDDPaychecksForFraud()) {
                SpcfMoney totalPaycheckAmt = new SpcfMoney("0.00");
                for (PaycheckSplit split : check.getPaycheckSplitCollection()) {
                    totalPaycheckAmt = new SpcfMoney(totalPaycheckAmt.add(split.getPaycheckSplitAmount()));
                }

                if (isEvenDollarAmountExceedsLimit(totalPaycheckAmt, amountToCompare )) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create event
                    String note = "EE " + check.getDDEmployee().getFirstName() + ":" + check.getDDEmployee().getLastName()
                            + " paid " + totalPaycheckAmt + ", an even dollar amount which is greater than " + amountToCompare
                            +", for payroll with check date "+ StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);


                    CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeePaidEvenDollarAmount, this, note, check.getDDEmployee());
                }
            }
        }
    }

    /**
     * @see #employeePaidEvenDollarAmount() for model
     */
    public void payeePaidEvenDollarAmount() {
        // Pull the number of payrolls for Fraud Consideration from the SP Params
        int fraudEERoundPaidXPayrolls = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPRoundPaidXPayrolls).getIntValue();
        String fraudBPRoundPaidXAmount = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPRoundPaidXAmount).getValue();
        SpcfMoney amountToCompare = new SpcfMoney(fraudBPRoundPaidXAmount);
        Long numberOfCompletePayrollRuns = getCompany().getNumberOfCompleteDDPayrolls();

        //for (Paycheck check : findDDPaychecksForFraud()) {

        if (numberOfCompletePayrollRuns < fraudEERoundPaidXPayrolls) {
            for (BillPayment billPayment : getBillPaymentCollection()) {
                SpcfMoney totalPaycheckAmt = new SpcfMoney("0.00");
                for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                    totalPaycheckAmt = new SpcfMoney(totalPaycheckAmt.add(billPaymentSplit.getAmount()));
                }

                if (isEvenDollarAmountExceedsLimit(totalPaycheckAmt, amountToCompare ) ) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create event
                    String note = "Payee " + billPayment.getPayee().getName()
                            + " paid " + totalPaycheckAmt + ", an even dollar amount which is greater than " + amountToCompare
                            +", for vendor payment with check date "  + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                    CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.EmployeePaidEvenDollarAmount, this, note, billPayment.getPayee());
                }
            }
        }
    }

    private Boolean isEvenDollarAmountExceedsLimit(SpcfMoney pPaycheckAmount, SpcfMoney pAmountThreshold) {
        int fractionalPart = pPaycheckAmount.getFractionalPart();
        if (fractionalPart == 0 && pPaycheckAmount.isGreaterThan(pAmountThreshold)) {
            return true;
        }
        return false;
    }




    public void currentPayrollPercentageIncrease() {
        // Pull the Max allowable increase and number of payrolls for Fraud Consideration from the SP Params
        String payrollPercentIncreaseString = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPRPercentIncreaseMax).getValue();
        SpcfMoney percentIncreaseAllowed = new SpcfMoney(payrollPercentIncreaseString);

        int fraudXPayrolls = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPRPercentIncreaseMaxXPayrolls).getValue());

        Long numberOfCompletePayrollRuns = getCompany().getNumberOfCompleteDDPayrolls();

        if (numberOfCompletePayrollRuns <= fraudXPayrolls) {

            SpcfCalendar settlementDate = getPaycheckSettlementDate().toLocal();

            PayrollRun priorPayroll = PayrollRun.findPriorPayrollRunBySettlementDateAndCompany(
                    getCompany(), settlementDate, getSourcePayRunId());

            // Get the total paycheck amount for the current Settlement Date
            SpcfMoney currentSettlementDateAmount = getTotalAmountBySettlementDate();

            if (priorPayroll != null) {

                // Get the total paycheck amount for the prior Settlement Date
                SpcfMoney priorSettlementDateAmount = priorPayroll.getTotalAmountBySettlementDate();

                boolean flag = isMoreThanXPercent(priorSettlementDateAmount, currentSettlementDateAmount, percentIncreaseAllowed);
                if (flag) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create NumberOfPayrollsPerDayExceeded event
                    String note = "Payroll(s) for the settlement date of "
                            + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT)
                            + " have a combined amount of " + currentSettlementDateAmount
                            + " which is over " + payrollPercentIncreaseString + "%"
                            + " more than prior payroll amount.";

                    Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.CurrentPayrollPercentageIncrease, this, note));
                }
            }

            PayrollRun nextPayroll = PayrollRun.findNextPayrollRunBySettlementDateAndCompany(getCompany(), settlementDate, getSourcePayRunId());

            if (nextPayroll != null) {

                // Get the total paycheck amount for the next Settlement Date
                SpcfMoney nextSettlementDateAmount = nextPayroll.getTotalAmountBySettlementDate();

                boolean flag = isMoreThanXPercent(currentSettlementDateAmount, nextSettlementDateAmount, percentIncreaseAllowed);
                if (flag) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    String note = "Payroll(s) for the settlement date of "
                            + StringFormatter.formatDate(nextPayroll.getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT)
                            + " have a combined amount of " + nextSettlementDateAmount
                            + " which is over " + payrollPercentIncreaseString + "%"
                            + " more than prior payroll amount.";

                    Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.CurrentPayrollPercentageIncrease, nextPayroll, note));
                }
            }
        }
    }

    public void singleEmployeePercentageIncrease() {
        // Pull the Max allowable increase and number of payrolls for Fraud Consideration from the SP Params
        String percentIncreaseAllowedString = FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEPercentIncreaseMax).getValue();
        SpcfMoney percentIncreaseAllowed = new SpcfMoney(percentIncreaseAllowedString);

        int fraudXPayrolls = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudEEPercentIncreaseMaxXPayrolls).getValue());

        Long numberOfCompletePayrollRuns = getCompany().getNumberOfCompleteDDPayrolls();

        if (numberOfCompletePayrollRuns <= fraudXPayrolls) {
            SpcfCalendar settlementDate = getPaycheckSettlementDate().toLocal();
            CalendarUtils.clearTime(settlementDate);

            PayrollRun priorPayroll = PayrollRun.findPriorPayrollRunBySettlementDateAndCompany(
                    getCompany(), settlementDate, getSourcePayRunId());

            if (priorPayroll != null) {
                DomainEntitySet<Paycheck> priorPaychecks = priorPayroll.getPaychecksBySettlementDate();
                HashMap<SpcfUniqueId, SpcfMoney> amtsPerEmployeeForPriorPayroll = getNetAmountPerEmployee(priorPaychecks);

                DomainEntitySet<Paycheck> currentPaychecks = getPaychecksBySettlementDate();
                HashMap<SpcfUniqueId, SpcfMoney> amtsPerEmployeeForCurrentPayroll = getNetAmountPerEmployee(currentPaychecks);

                for (SpcfUniqueId employeeId : amtsPerEmployeeForCurrentPayroll.keySet()) {
                    if (amtsPerEmployeeForPriorPayroll.containsKey(employeeId)) {
                        boolean flag = isMoreThanXPercent(amtsPerEmployeeForPriorPayroll.get(employeeId), amtsPerEmployeeForCurrentPayroll.get(employeeId), percentIncreaseAllowed);
                        if (flag) {
                            Employee employee = Application.findById(Employee.class, employeeId);
                            //Flag the Company for Fraud
                            getCompany().setFraudFlag();

                            //Create SingleEmployeePercentageIncrease event
                            String note = "Employee " + employee.getFirstName() + " " + employee.getLastName()
                                    + " has a combined amount of " + amtsPerEmployeeForCurrentPayroll.get(employeeId)
                                    + " for the settlement date of "
                                    + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT)
                                    + " which is over " + percentIncreaseAllowedString + "%"
                                    + " more than in the prior payroll.";

                            Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.SingleEmployeePercentageIncrease, this, note, employee));
                        }
                    }
                }
            }

            PayrollRun nextPayroll = PayrollRun.findNextPayrollRunBySettlementDateAndCompany(
                    getCompany(), settlementDate, getSourcePayRunId());

            if (nextPayroll != null) {
                DomainEntitySet<Paycheck> currentPaychecks = getPaychecksBySettlementDate();
                HashMap<SpcfUniqueId, SpcfMoney> amtsPerEmployeeForCurrentPayroll = getNetAmountPerEmployee(currentPaychecks);

                DomainEntitySet<Paycheck> nextPaychecks = nextPayroll.getPaychecksBySettlementDate();
                HashMap<SpcfUniqueId, SpcfMoney> amtsPerEmployeeForNextPayroll = getNetAmountPerEmployee(nextPaychecks);

                for (SpcfUniqueId employeeId : amtsPerEmployeeForCurrentPayroll.keySet()) {
                    if (amtsPerEmployeeForNextPayroll.containsKey(employeeId)) {
                        boolean flag = isMoreThanXPercent(amtsPerEmployeeForCurrentPayroll.get(employeeId), amtsPerEmployeeForNextPayroll.get(employeeId), percentIncreaseAllowed);
                        if (flag) {
                            Employee employee = Application.findById(Employee.class, employeeId);

                            //Flag the Company for Fraud
                            getCompany().setFraudFlag();

                            //Create SingleEmployeePercentageIncrease event
                            String note = "Employee " + employee.getFirstName() + " " + employee.getLastName()
                                    + " has a combined amount of " + amtsPerEmployeeForNextPayroll.get(employeeId)
                                    + " for the settlement date of "
                                    + StringFormatter.formatDate(nextPayroll.getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT)
                                    + " which is over " + percentIncreaseAllowedString + "%"
                                    + " more than in the prior payroll.";

                            Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.SingleEmployeePercentageIncrease, nextPayroll, note, employee));
                        }
                    }
                }
            }
        }
    }

    public void payrollProcessedTooSoon() {

        // Pull the number of payrolls and the number of days to check for Fraud Consideration from the SP Params
        FraudValueType numberOfDaysType;
        FraudValueType numberOfPayrollsType;
        FraudValueType minPayrollAmountType;
        if (getPayrollRunType() == PayrollType.BillPayment) {
            numberOfDaysType = FraudValueType.FraudBPNumberOfDaysForXPayments;
            numberOfPayrollsType = FraudValueType.FraudBPNumberOfPaymentsInXDays;
            minPayrollAmountType = FraudValueType.FraudBPXPayrollAmount;

        } else {
            numberOfDaysType = FraudValueType.FraudPRNumberOfDaysForXPayrolls;
            numberOfPayrollsType = FraudValueType.FraudPRNumberOfPayrollsInXDays;
            minPayrollAmountType = FraudValueType.FraudPRXPayrollAmount;
        }
        FraudRule fraudRule = FraudRule.findFraudRule(getCompany());
        int numberOfDays = fraudRule.findFraudValueByName(numberOfDaysType).getIntValue();
        int numberOfPayrolls = fraudRule.findFraudValueByName(numberOfPayrollsType).getIntValue();
        String minPayrollAmountStr = fraudRule.findFraudValueByName(minPayrollAmountType).getValue();
        SpcfMoney minPayrollAmount = new SpcfMoney(minPayrollAmountStr);


        SpcfCalendar fraudCheckDate = null;

        if (getCompany().hasService(ServiceCode.RiskAssessment)) {
            fraudCheckDate = getCompany().getSignUpDate();
        }
        else {
            fraudCheckDate = getCompany().getTestDebitVerificationDate();
        }


        // If the test debit verification date is less or equal the number of days to check, get the number of payrolls submitted since the sign up date

        if (fraudCheckDate!=null && CalendarUtils.businessDaysFromDateToDate(fraudCheckDate, getPayrollRunDate().toLocal()) <= numberOfDays) {
            int numberOfSubmittedPayrolls = PayrollRun.getPayrollRunCountByType(getCompany(), null, minPayrollAmount, PayrollType.Regular, PayrollType.BillPayment);
            if (numberOfSubmittedPayrolls >= numberOfPayrolls) {
                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Create PayrollProcessedTooSoon event
                String note = "Company ran " + numberOfSubmittedPayrolls + " payrolls of amount greater than "+ minPayrollAmount.toString() +" with in " + numberOfDays
                        + " days of test verification debit date  of "+ StringFormatter.formatDate(fraudCheckDate, PayrollRun.DATE_FORMAT);

                Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.PayrollProcessedTooSoon, this, note));
            }
        }
    }

    public HashMap<FinancialTransaction, SpcfMoney> getCollectedTaxAmount() {

        // in order to be collected the transactions must be in a complete state
        DomainEntitySet<FinancialTransaction> taxFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit},
                new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned});

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        if (taxFTs != null && taxFTs.size() > 0) {
            FinancialTransaction erDebitTx = taxFTs.get(0);
            TransactionSummary summary = erDebitTx.summarizeRelatedTransactions();
            if (summary.amtCollected.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(erDebitTx, new SpcfMoney(summary.amtCollected));
            } else {
                map.put(erDebitTx, new SpcfMoney(SpcfMoney.ZERO));
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getCollectedFeeAmounts() {
        DomainEntitySet<FinancialTransaction> fts =
                FinancialTransaction.findNonRedebitFeeFinancialTransactions(
                        getCompany(),
                        getSourcePayRunId());
        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction ft : fts) {
            TransactionSummary summary = ft.summarizeRelatedTransactions();
            map.put(ft, new SpcfMoney(summary.amtCollected));
        }
        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getCollectedSalesTaxAmounts() {
        DomainEntitySet<FinancialTransaction> fts = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax}, null);
        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction ft : fts) {
            TransactionSummary summary = ft.summarizeRelatedTransactions();
            map.put(ft, new SpcfMoney(summary.amtCollected));
        }
        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUncollectedDDAmount() {
        DomainEntitySet<FinancialTransaction> ddFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned});

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        if (ddFTs != null && ddFTs.size() > 0) {
            for (FinancialTransaction erDebitTx : ddFTs) {
                TransactionSummary txnSummary = erDebitTx.summarizeRelatedTransactions();
                if (txnSummary.amtUncollected.compareTo(SpcfMoney.ZERO) > 0) {
                    map.put(erDebitTx, new SpcfMoney(txnSummary.amtUncollected));
                } else {
                    map.put(erDebitTx, new SpcfMoney(SpcfMoney.ZERO));
                }
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUncollectedTaxAmount() {
        SpcfMoney totalUncollectedAmount = new SpcfMoney(SpcfMoney.ZERO);

        DomainEntitySet<FinancialTransaction> taxFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit},
                new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned});

        DomainEntitySet<FinancialTransaction> taxCreditFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxCredit},
                new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned});

        for (FinancialTransaction currTaxCredit : taxCreditFTs) {
            if (!currTaxCredit.getSettlementTypeCd().equals(SettlementType.ApplyForward)) {
                TransactionSummary txnSummary = currTaxCredit.summarizeRelatedTransactions();
                totalUncollectedAmount = new SpcfMoney(totalUncollectedAmount.add(txnSummary.amtPending));
            }
        }

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        if (taxFTs != null && taxFTs.size() > 0) {
            FinancialTransaction erDebitTx = taxFTs.get(0);
            TransactionSummary txnSummary = erDebitTx.summarizeRelatedTransactions();

            if (txnSummary.amtUncollected.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(erDebitTx, new SpcfMoney(txnSummary.amtUncollected.subtract(totalUncollectedAmount)));

            } else {
                map.put(erDebitTx, new SpcfMoney(SpcfMoney.ZERO));
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUncollectedFeeAmounts() {

        DomainEntitySet<FinancialTransaction> nonRedebitFeeFTs =
                FinancialTransaction.findNonRedebitFeeFinancialTransactions(
                        getCompany(),
                        getSourcePayRunId());
        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction financialTxn : nonRedebitFeeFTs) {
            TransactionSummary txnSummary = financialTxn.summarizeRelatedTransactions();
            if (txnSummary.amtUncollected.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(financialTxn, new SpcfMoney(txnSummary.amtUncollected));
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUncollectedSalesTaxAmounts() {

        DomainEntitySet<FinancialTransaction> salesTaxFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax}, null);
        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction financialTxn : salesTaxFTs) {
            TransactionSummary txnSummary = financialTxn.summarizeRelatedTransactions();
            if (txnSummary.amtUncollected.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(financialTxn, new SpcfMoney(txnSummary.amtUncollected));
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUnrecoveredDirectDepositAmount() {
        DomainEntitySet<FinancialTransaction> ddFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned});

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for(int i=0; ddFTs != null && i < ddFTs.size(); i++) {
            FinancialTransaction erDebitTx = ddFTs.get(i);
            TransactionSummary txnSummary = erDebitTx.summarizeRelatedTransactions();
            if (txnSummary.amtWrittenOff.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(erDebitTx, new SpcfMoney(txnSummary.amtWrittenOff));
            } else {
                map.put(erDebitTx, new SpcfMoney(SpcfMoney.ZERO));
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUnrecoveredTaxAmount() {
        DomainEntitySet<FinancialTransaction> taxFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit},
                new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Completed, TransactionStateCode.Executed, TransactionStateCode.Returned});

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        if (taxFTs != null && taxFTs.size() > 0) {
            FinancialTransaction erDebitTx = taxFTs.get(0);
            TransactionSummary txnSummary = erDebitTx.summarizeRelatedTransactions();
            if (txnSummary.amtWrittenOff.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(erDebitTx, new SpcfMoney(txnSummary.amtWrittenOff));
            } else {
                map.put(erDebitTx, SpcfMoney.ZERO);
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUnrecoveredFeeAmounts() {

        DomainEntitySet<FinancialTransaction> nonRedebitFeeFTs =
                FinancialTransaction.findNonRedebitFeeFinancialTransactions(
                        getCompany(),
                        getSourcePayRunId());
        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction financialTxn : nonRedebitFeeFTs) {
            TransactionSummary txnSummary = financialTxn.summarizeRelatedTransactions();
            if (txnSummary.amtWrittenOff.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(financialTxn, new SpcfMoney(txnSummary.amtWrittenOff));
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getUnrecoveredSalesTaxAmounts() {

        DomainEntitySet<FinancialTransaction> salesTaxFTs = getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax}, null);
        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction financialTxn : salesTaxFTs) {
            TransactionSummary txnSummary = financialTxn.summarizeRelatedTransactions();
            if (txnSummary.amtWrittenOff.compareTo(SpcfMoney.ZERO) > 0) {
                map.put(financialTxn, new SpcfMoney(txnSummary.amtWrittenOff));
            }
        }

        return map;
    }

    public HashMap<Law, SpcfDecimal> getLiabilityAmountsByLaw() {
        return getLiabilityAmountsByLaw(false, null, false);
    }

    public HashMap<Law, SpcfDecimal> getLiabilityAmountsByLaw(boolean isPriorQuarterCalculation, String pSourceAdjustmentId){
        return getLiabilityAmountsByLaw(isPriorQuarterCalculation, pSourceAdjustmentId, false);
    }

    public HashMap<Law, SpcfDecimal> getLiabilityAmountsByLaw(boolean isPriorQuarterCalculation, String pSourceAdjustmentId, Boolean pAlwaysRecordFTs) {
        HashMap<Law, SpcfDecimal> liabilities = new HashMap<Law, SpcfDecimal>();
        SpcfCalendar paycheckDate = getPaycheckDate().copy();
        CalendarUtils.clearTime(paycheckDate);

        boolean isPayrollOnService = isPriorQuarterCalculation || getCompany().isServiceSupportedAsOf(ServiceCode.Tax, paycheckDate) || pAlwaysRecordFTs;

        if (pSourceAdjustmentId==null) {
            for (Paycheck paycheck : getPaycheckCollection()) {
                if (paycheck.getStatus() != PaycheckStatusCode.Active) {
                    continue;
                }
                for (Tax liability : paycheck.getTaxCollection()) {
                    Law law = liability.getLaw();
                    boolean isLawSupported =
                            law.getPaymentTemplate().isSupportedAsOfDate(paycheckDate);

                    // Only add the amount if the Payment Template is supported by PSP as of the paycheck date
                    if (isLawSupported && isPayrollOnService) {
                        SpcfDecimal amount = liability.getTaxLiabilityAmount();

                        if (amount != null && amount.isZero() && law.isAEIC()) {
                            continue;
                        }

                        SpcfDecimal curAmount = liabilities.get(law);
                        if (curAmount == null) {
                            liabilities.put(law, amount);
                        } else {
                            liabilities.put(law, curAmount.add(amount));
                        }
                    }
                }
            }
        }


        //
        // add adjustments
        for (LiabilityAdjustment tax : getLiabilityAdjustmentCollection()) {
            //Skip this adjustment if it doesn't correspond to the adjustment submission we are interested in
            if (pSourceAdjustmentId != null && !pSourceAdjustmentId.equals(tax.getCompanyAdjustmentSubmission().getSourceId())) {
                continue;
            }
            Law law = tax.getLaw();
            boolean isLawSupported =
                    law.getPaymentTemplate().isSupportedAsOfDate(paycheckDate);

            if (!tax.getIsReconcilingAdjustment() && isLawSupported && isPayrollOnService) {
                SpcfDecimal amount = tax.getAmount();

                if (amount != null && !amount.isZero()) {
                    if (liabilities.get(law) != null) {
                        amount = amount.add(liabilities.get(law));
                    }
                    liabilities.put(law, amount);
                }
            }
        }

        //
        return liabilities;
    }

    public Map<Law, SpcfDecimal> getFinancialTransactionLiabilityBalancesByLaw() {
        Map<Law, SpcfDecimal> liabilityBalances = new HashMap<>();
        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                                                                                                       TransactionTypeCode.AgencyDirectCredit,
                                                                                                       TransactionTypeCode.AgencyTaxDebit,
                                                                                                       TransactionTypeCode.AgencyDirectDebit,
                                                                                                       TransactionTypeCode.AgencyTaxOverpaymentApplied));

        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection().find(where)) {
            Law law = financialTransaction.getLaw();
            if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                liabilityBalances.put(law, liabilityBalances.computeIfAbsent(law, key -> SpcfMoney.ZERO).add(financialTransaction.getFinancialTransactionAmount()));
            } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                liabilityBalances.put(law, liabilityBalances.computeIfAbsent(law, key -> SpcfMoney.ZERO).subtract(financialTransaction.getFinancialTransactionAmount()));
            }
        }

        return liabilityBalances;
    }

    public DomainEntitySet<FinancialTransaction> getTaxPaymentTransactions() {
        DomainEntitySet<FinancialTransaction> taxPaymentTransactions = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            if (financialTransaction.isTaxPaymentTransaction()) {
                if (financialTransaction.getMoneyMovementTransaction() != null) {
                    taxPaymentTransactions.add(financialTransaction);
                }
            }
        }
        return taxPaymentTransactions;
    }

    public Set<String> getStatesForTaxPayments() {
        Set<String> stateList = new TreeSet<String>(); // unique and sorted

        //
        // Iterate over all tax transactions in this payroll run to determine which states are referenced
        //
        for (FinancialTransaction taxTxn : getTaxPaymentTransactions()) {
            Law law = taxTxn.getLaw();

            if (law != null) {
                PaymentTemplate pmtTemplate = law.getPaymentTemplate();

                if (pmtTemplate != null) {
                    Agency agency = pmtTemplate.getAgency();

                    if (agency != null) {
                        IJurisdiction jurisdiction = agency.getJurisdiction();

                        if (jurisdiction != null) {
                            String state = jurisdiction.getStateID();

                            if (state != null) {
                                stateList.add(state.toUpperCase());
                            }
                        }
                    }
                }
            }
        }

        return stateList;
    }

    public SpcfDecimal getTotalAgencyTaxCredits(Law pLaw) {
        Criterion<FinancialTransaction> criterion = FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit).And(FinancialTransaction.Law().equalTo(pLaw));
        SpcfDecimal hpdeTaxAmount = new SpcfMoney("0.0");
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection().find(criterion)) {
            hpdeTaxAmount = hpdeTaxAmount.add(financialTransaction.getFinancialTransactionAmount());
        }
        return hpdeTaxAmount;
    }

    public Map<Law, SpcfDecimal> getTotalTaxLiabilities() {
        Map<Law, SpcfDecimal> liabilities = new HashMap<Law, SpcfDecimal>();
        for (Paycheck paycheck : getPaycheckCollection()) {
            if (paycheck.getStatus() != PaycheckStatusCode.Active) continue;
            for (Tax liability : paycheck.getTaxCollection()) {
                Law law = liability.getLaw();
                if (law.getPaymentTemplate().isSupportedAsOfDate(paycheck.getPayrollRun().getPaycheckDate())) {
                    SpcfDecimal amount = liability.getTaxLiabilityAmount();
                    SpcfDecimal curAmount = liabilities.get(law);
                    if (curAmount == null) {
                        liabilities.put(law, amount);
                    } else {
                        liabilities.put(law, curAmount.add(amount));
                    }
                }
            }
        }


        return liabilities;
    }

    public SpcfCalendar getVoidedTaxDebitCompletionDate() {
        FinancialTransaction erTaxDebit = getNonCancelledEmployerTaxDebit();
        return erTaxDebit.getTransactionCompletionDate();
    }




    public DomainEntitySet<Tax> getNonCancelledPaycheckTaxes() {
        DomainEntitySet<Tax> taxes = new DomainEntitySet<Tax>();
        DomainEntitySet<Paycheck> paychecks = getPaycheckCollection();
        for (Paycheck paycheck : paychecks) {
            if (paycheck.getStatus() == PaycheckStatusCode.Active) {
                for (Tax tax : paycheck.getTaxCollection()) {
                    taxes.add(tax);
                }
            }
        }
        return taxes;
    }

    public SpcfCalendar getExpectedResolutionDate() {
        SpcfCalendar resolutionDate = null;
        DomainEntitySet<FinancialTransaction> transactions;
        switch (getPayrollRunStatus()) {
            case PendingAutoRedebit:
            case PendingRedebit:
                // include DD, Fee, and Sales Tax redebits
                transactions = getFinancialTransactions(
                        TransactionCategory.Employer,
                        TransactionTypeGroupCode.Redebit,
                        TransactionStateCode.Created);
                // use the latest settlement date
                for (FinancialTransaction ft : transactions) {
                    if (resolutionDate == null || ft.getSettlementDate().toLocal().after(resolutionDate)) {
                        resolutionDate = ft.getSettlementDate().toLocal().copy();
                    }
                }
                if (resolutionDate != null) {
                    CalendarUtils.addBusinessDays(resolutionDate, 4);
                }
                break;

            case AutoRedebitOffloaded:
            case RedebitOffloaded:
                // include DD, Fee, and Sales Tax redebits
                transactions = getFinancialTransactions(
                        TransactionCategory.Employer,
                        TransactionTypeGroupCode.Redebit,
                        TransactionStateCode.Executed);
                // use the latest settlement date
                for (FinancialTransaction ft : transactions) {
                    if (resolutionDate == null || ft.getSettlementDate().toLocal().after(resolutionDate)) {
                        resolutionDate = ft.getSettlementDate().toLocal().copy();
                    }
                }
                if (resolutionDate != null) {
                    CalendarUtils.addBusinessDays(resolutionDate, 4);
                }
                break;

            case PendingReversals:
                transactions = getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});
                if (transactions.size() > 0) {
                    resolutionDate = transactions.get(0).getSettlementDate().toLocal().copy();
                    CalendarUtils.addBusinessDays(resolutionDate, 4);
                }
                break;

            case ReversalsOffloaded:
                transactions = getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
                if (transactions.size() > 0) {
                    resolutionDate = transactions.get(0).getSettlementDate().toLocal().copy();
                    CalendarUtils.addBusinessDays(resolutionDate, 4);
                }
                break;

            case ReversalsFinished:
                transactions = getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                        new TransactionStateCode[]{TransactionStateCode.Completed});
                if (transactions.size() > 0) {
                    resolutionDate = transactions.get(0).getSettlementDate().toLocal().copy();
                    CalendarUtils.addBusinessDays(resolutionDate, 4);
                }
                break;

            case PendingWire:
                resolutionDate = getWireExpectedDate().toLocal().copy();
                break;
        }

        // this may still be null, meaning no expected resolution is defined for this payroll run
        // given its status and existing FTs
        return resolutionDate;
    }


    public void applyPendingOverPayments(PaymentTemplate pPaymentTemplate, SpcfDecimal pMaxOverpaymentAmount, SpcfCalendar pPaycheckDate, boolean pIsDirectDebit, BankAccount pDebitBankAccount) {
        if(pMaxOverpaymentAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            return;
        }

        Company company = getCompany();
        SpcfCalendar debitSettlementDate = null;

        TransactionTypeCode employerTransactionType;
        TransactionTypeCode agencyTransactionType;
        if(pIsDirectDebit) {
            employerTransactionType = TransactionTypeCode.EmployerTaxDirectOverpaymentApplied;
            agencyTransactionType = TransactionTypeCode.AgencyDirectDebit;
        } else {
            CompanyService service = company.getService(ServiceCode.Tax);
            debitSettlementDate = FinancialTransaction.getSettlementDate(service, this);
            employerTransactionType = TransactionTypeCode.EmployerTaxOverpaymentApplied;
            agencyTransactionType = TransactionTypeCode.AgencyTaxDebit;
        }

        // get AgencyTaxRefund balance for this template from the database
        Map<Law, SpcfMoney> agencyTaxRefundBalanceMap =
                LedgerAccount.getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, pPaymentTemplate, company, pPaycheckDate);
        if (pPaymentTemplate.isRolledUpAnnually()) {
            //special rule: FUTA can be applied across quarters, but not across years
            LedgerAccount.addLedgerBalanceFromPriorQuartersInYear(agencyTaxRefundBalanceMap, LedgerAccountCode.AgencyTaxRefund, company, pPaycheckDate, pPaymentTemplate);
        }

        // check agency tax refund account first ** need to keep track of the max over payment amount so we don't create negative payments
        for (Law law : pPaymentTemplate.getLawCollection()) {
            if(pMaxOverpaymentAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
                break;
            }

            SpcfDecimal agencyTaxRefundBalance = agencyTaxRefundBalanceMap.get(law);
            if(agencyTaxRefundBalance == null) {
                agencyTaxRefundBalance = SpcfMoney.ZERO;
            }

            // If the balance amount we have to apply is greater than the maximum applied amount, use the max allowed amount, otherwise apply the whole balance amount
            SpcfDecimal appliedAgencyTaxRefundAmount;
            if (pMaxOverpaymentAmount.isGreaterThanEqualTo(agencyTaxRefundBalance)) {
                appliedAgencyTaxRefundAmount = agencyTaxRefundBalance;
            } else {
                appliedAgencyTaxRefundAmount = pMaxOverpaymentAmount;
            }

            // Create EmployerTaxOverpaymentApplied / AgencyTaxDebit - this will remove money from both Agency tax Refund and ERPayable
            // The settlement Date on EmployerTaxOverpaymentApplied needs to be the same as the ERDebit Settlement Date
            if (appliedAgencyTaxRefundAmount.isGreaterThan(SpcfMoney.ZERO)) {
                FinancialTransaction erTaxOverpaymentApplied =
                        FinancialTransaction.createFinancialTransaction(company, this, null, null, null, null, null,
                                employerTransactionType,
                                new SpcfMoney(appliedAgencyTaxRefundAmount),
                                SettlementType.ApplyForward,
                                debitSettlementDate, law);
                FinancialTransaction agencyTaxDebit =
                        FinancialTransaction.createFinancialTransaction(company, this, null, null, pDebitBankAccount, BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit,
                                agencyTransactionType,
                                new SpcfMoney(appliedAgencyTaxRefundAmount),
                                SettlementType.ApplyForward,
                                null, law);

                erTaxOverpaymentApplied.setRelatableTransaction(agencyTaxDebit);
                agencyTaxDebit.addRelatedTransactions(erTaxOverpaymentApplied);
                pMaxOverpaymentAmount = pMaxOverpaymentAmount.subtract(appliedAgencyTaxRefundAmount);
            }
        }
    }

    public void applyPendingCredits(SpcfDecimal pMaxDebitAmount) {
        if(pMaxDebitAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            return;
        }

        Company company = getCompany();
        CompanyService service = company.getService(ServiceCode.Tax);
        SpcfCalendar debitSettlementDate = FinancialTransaction.getSettlementDate(service, this);

        // get ERPayable balance for this law from the database
        SpcfDecimal erPayableBalance =
                LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company, LedgerAccountCode.ERPayable);

        // If the balance amount we have to apply is greater than the maximum applied amount, use the Max allowed Amount, otherwise apply the whole balance amount
        SpcfDecimal appliedERPayableAmount;
        if (pMaxDebitAmount.isGreaterThanEqualTo(erPayableBalance)) {
            appliedERPayableAmount = erPayableBalance;
        } else {
            appliedERPayableAmount = pMaxDebitAmount;
        }

        // Create ERTaxCreditApplied Financial Transaction
        if (appliedERPayableAmount.isGreaterThan(SpcfMoney.ZERO)) {
            FinancialTransaction.createFinancialTransaction(company, this, null, null, null, null, null,
                    TransactionTypeCode.EmployerTaxCreditApplied,
                    new SpcfMoney(appliedERPayableAmount),
                    SettlementType.ApplyForward,
                    debitSettlementDate, null);
        }
    }

    public boolean isSubmissionForPriorQuarter(SpcfCalendar pSubmissionDate) {
        //if the paycheck date is before the first day of the quarter in which the void was submitted, this void is for a prior quarter
        SpcfCalendar firstDayOfSubmittedQuarter = CalendarUtils.getFirstDayOfQuarter(pSubmissionDate);

        if (getPaycheckDate().before(firstDayOfSubmittedQuarter)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Function to cancel all the Financial Transactions for a Payroll Run
     *
     * @param pTxnTypeCodes String[]
     */
    public void cancelPayrollFinancialTransactions(TransactionTypeCode[] pTxnTypeCodes) {
        DomainEntitySet<FinancialTransaction> finTxnList =
                getFinancialTransactions(
                        pTxnTypeCodes,
                        new TransactionStateCode[]{TransactionStateCode.Created});

        if (!finTxnList.isEmpty()) {
            for (FinancialTransaction finTxn : finTxnList) {
                finTxn.updateFinancialTransactionState(TransactionStateCode.Cancelled);
                switch (getPayrollRunType()) {
                    case Regular:
                        PaycheckSplit paycheckSplit = finTxn.getPaycheckSplit();
                        if (paycheckSplit != null) {
                            Paycheck paycheck = paycheckSplit.getPaycheck();
                            if (paycheck != null) {
                                paycheck.setStatus(PaycheckStatusCode.Inactive);
                                Application.save(paycheck);
                            }
                        }
                        break;
                    case BillPayment:

                        BillPaymentSplit billPaymentSplit = finTxn.getBillPaymentSplit();
                        if (billPaymentSplit != null) {
                            BillPayment billPayment = billPaymentSplit.getBillPayment();
                            if (billPayment != null) {
                                billPayment.setStatus(BillPaymentStatusCode.Inactive);
                                Application.save(billPayment);
                            }
                        }
                        break;
                }

            }

            // create a transaction response associated with all transactions that have just been cancelled
            TransactionResponse.createTransactionResponse(getCompany(), (Collection) finTxnList, null);
        }
    }

    public static SpcfMoney getLedgerAccountBalancesForPayrollsAndLaw(DomainEntitySet<PayrollRun> pPayrollRuns, Law pLaw, LedgerAccountCode pLedgerAccountCode) {
        return getLedgerAccountBalancesForPayrollsAndLaw(pPayrollRuns, pLaw, pLedgerAccountCode, null);
    }

    public static SpcfMoney getLedgerAccountBalancesForPayrollsAndLaw(DomainEntitySet<PayrollRun> pPayrollRuns, Law pLaw, LedgerAccountCode pLedgerAccountCode, SpcfCalendar pPaycheckDate) {
        SpcfDecimal amount = SpcfMoney.ZERO;

        SpcfCalendar quarterStart = null;
        SpcfCalendar quarterEnd = null;
        if(pPaycheckDate != null) {
            quarterStart = CalendarUtils.getFirstDayOfQuarter(pPaycheckDate);
            quarterEnd = CalendarUtils.getLastDayOfQuarter(pPaycheckDate);
        }

        for (PayrollRun payrollRun : pPayrollRuns) {
            if(pPaycheckDate == null || (payrollRun.getPaycheckDate().between(quarterStart, quarterEnd))) {
                amount = amount.add(LedgerAccount.getLedgerAccountBalanceByPayrollAndLaw(pLedgerAccountCode, pLaw, payrollRun));
            }
        }
        return new SpcfMoney(amount);
    }

    public HashMap<FinancialTransaction, SpcfMoney> getPrefundingPayrollAmounts() {
        DomainEntitySet<FinancialTransaction> achTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                .Where(FinancialTransaction.PayrollRun().equalTo(this)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit))
                        .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH))));

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction achTransaction : achTransactions) {
            if (isAchPrefundTransaction(achTransaction)) {
                map.put(achTransaction, achTransaction.getPrefundingAchTransactionBalance());
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getPrefundingFeeAmounts() {
        DomainEntitySet<FinancialTransaction> achTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                .Where(FinancialTransaction.PayrollRun().equalTo(this)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit))
                        .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH))));

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction achTransaction : achTransactions) {
            if (isAchPrefundTransaction(achTransaction)) {
                SpcfMoney amountDue = achTransaction.getPrefundingAchTransactionBalance();
                if (amountDue.compareTo(new SpcfMoney("0.00")) > 0) {
                    map.put(achTransaction, amountDue);
                }
            }
        }

        return map;
    }

    public HashMap<FinancialTransaction, SpcfMoney> getPrefundingTaxAmounts() {
        DomainEntitySet<FinancialTransaction> achTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                .Where(FinancialTransaction.PayrollRun().equalTo(this)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.ServiceSalesAndUseTax))
                        .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH))));

        HashMap<FinancialTransaction, SpcfMoney> map = new HashMap<FinancialTransaction, SpcfMoney>();
        for (FinancialTransaction achTransaction : achTransactions) {
            if (isAchPrefundTransaction(achTransaction)) {
                SpcfMoney amountDue = achTransaction.getPrefundingAchTransactionBalance();
                if (amountDue.compareTo(new SpcfMoney("0.00")) > 0) {
                    map.put(achTransaction, amountDue);
                }
            }
        }

        return map;
    }

    private boolean isAchPrefundTransaction(FinancialTransaction pFinancialTransaction) {
        if (pFinancialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Cancelled) {
            DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                    .Where(FinancialTransaction.PayrollRun().equalTo(this)
                            .And(FinancialTransaction.OriginalTransaction().equalTo(pFinancialTransaction))));
            // if there are transactions that referance this transction they must be prefund transactions
            return financialTransactions.size() > 0;
        }
        return true;
    }

    /*
    This is an expensive operation so some performance short-cuts have been implemented:
    1) Only look for note if it's an Adjustment payroll since only Adjustment payrolls can be manual adjustments
    2) Cache the value returned locally (used particularly because the SAP action validation uses this value as so does the translator)
     */
    private boolean manualAdjustmentNoteCached = false;
    private CompanyEventDetail cachedManualAdjustmentNote;
    public CompanyEventDetail getManualAdjustmentNote() {
        if (!manualAdjustmentNoteCached) {
            CompanyEventDetail manualAdjustmentNote = getPayrollRunType() != PayrollType.Adjustment ? null : CompanyEvent.getManualAdjustmentNote(getCompany(), EventDetailTypeCode.PayrollRunId, getId().toString());

            DomainEntitySet<CompanyEventDetail> refundEventDetails = CompanyEvent.findCompanyEventDetails(getCompany(), EventTypeCode.PendingPaymentRefunded, EventDetailTypeCode.PayrollRunId, getId().toString());
            if (refundEventDetails.size() > 0) {
                CompanyEvent companyEvent = refundEventDetails.getFirst().getCompanyEvent();
                String mmtId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.UniqueIdentifier);
                manualAdjustmentNote = CompanyEvent.getFullRefundNote(getCompany(), EventDetailTypeCode.UniqueIdentifier, mmtId);
            }
            manualAdjustmentNoteCached = true;
            cachedManualAdjustmentNote = manualAdjustmentNote;
        }
        return cachedManualAdjustmentNote;
    }


    public static int getPayrollRunCount(Company pCompany) {
        return (int)pCompany.getPayrollCount();
    }


    public static int getPayrollRunCountByStatus(Company pCompany, PayrollStatus pPayrollStatus) {
        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Select(PayrollRun.Id().Count())
                        .Where(PayrollRun.PayrollRunStatus().equalTo(pPayrollStatus)
                                .And(PayrollRun.Company().equalTo(pCompany)));


        List countPayrollRuns = Application.executeQuery(PayrollRun.class, query);
        return Integer.parseInt(countPayrollRuns.get(0).toString());
    }

    public static int getPayrollRunCountByType(Company pCompany, PayrollStatus pPayrollStatus, PayrollType... pPayrollRunType) {

        Criterion<PayrollRun> where = PayrollRun.PayrollRunType().in(pPayrollRunType)
                .And(PayrollRun.Company().equalTo(pCompany));

        if (pPayrollStatus != null) {
            where = where.And(PayrollRunStatus().equalTo(pPayrollStatus));
        }

        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Select(PayrollRun.Id().Count())
                        .Where(where);


        List countPayrollRuns = Application.executeQuery(PayrollRun.class, query);
        return Integer.parseInt(countPayrollRuns.get(0).toString());
    }

    public static int getPayrollRunCountByType(Company pCompany, PayrollStatus pPayrollStatus, SpcfMoney amount, PayrollType... pPayrollRunType) {

        Criterion<PayrollRun> where = PayrollRun.PayrollRunType().in(pPayrollRunType)
                .And(PayrollRun.Company().equalTo(pCompany)).And(PayrollRun.PayrollDirectDepositAmount().greaterThan(amount));

        if (pPayrollStatus != null) {
            where = where.And(PayrollRunStatus().equalTo(pPayrollStatus));
        }
        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Select(PayrollRun.Id().Count())
                        .Where(where);


        List countPayrollRuns = Application.executeQuery(PayrollRun.class, query);
        return Integer.parseInt(countPayrollRuns.get(0).toString());
    }
// Bill Payment Fraud Controls

    public void payeePaidGreaterThanMax() {
        // Pull the Max Payee Check Amount and number of payrolls for Fraud Consideration from the SP Params
        SpcfMoney amountToCompare = new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPayeePaidMax).getValue());


        int fraudPayeePaidMaxXPayrolls = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPayeePaidMaxXPayrolls).getValue());

        Long as400PayrollCount = 0L;
        if (getCompany().getQuickbooksInfo() != null) {
            as400PayrollCount = getCompany().getQuickbooksInfo().getAS400PayrollCount();
        }
        int numberOfCompletePayrollRuns = getPayrollRunCountByStatus(getCompany(), PayrollStatus.Complete);

        if ((numberOfCompletePayrollRuns + as400PayrollCount) < fraudPayeePaidMaxXPayrolls) {
            for (BillPayment billPayment : getBillPaymentCollection()) {
                SpcfMoney totalBillPaymentAmt = new SpcfMoney("0.00");
                for (BillPaymentSplit split : billPayment.getBillPaymentSplitCollection()) {
                    totalBillPaymentAmt = new SpcfMoney(totalBillPaymentAmt.add(split.getAmount()));
                }

                if (totalBillPaymentAmt.compareTo(amountToCompare) > 0) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create Payee event
                    String note = "Payee " + billPayment.getPayee().getName()
                            + " paid " + totalBillPaymentAmt + " in a single payment with date "
                            + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                    CompanyEvent event = CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.PayeePaidGreaterThanMax, this, note);
                    event.addCompanyEventDetail(EventDetailTypeCode.PayeeId, billPayment.getPayee().getId().toString());
                }
            }
        }
    }

    public void payeePaidGreaterThanMaxForBankAcctUpdate() {
        // Pull the Max Payee Check Amount and number of days of bank account update for Fraud Consideration from the SP Params
        SpcfMoney amountToCompare = new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPAcctUpdateMax).getValue());


        int fraudPayeeXDaysBankAcctUpdated = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPAcctUpdateXDays).getValue());


        SpcfCalendar dateToCompare = getPayrollRunDate().toLocal().copy();
        dateToCompare.addDays(fraudPayeeXDaysBankAcctUpdated * -1);

            for (BillPayment billPayment : getBillPaymentCollection()) {
                SpcfMoney totalBillPaymentAmt = new SpcfMoney("0.00");
                int payeeBankAcctUpdateCount=0;
                for (BillPaymentSplit split : billPayment.getBillPaymentSplitCollection()) {

                    totalBillPaymentAmt = new SpcfMoney(totalBillPaymentAmt.add(split.getAmount()));
                        PayeeBankAccount pBA = split.getPayeeBankAccount();
                        if (pBA == null || pBA.getStatusEffectiveDate() == null) {
                            String property = pBA == null ? "PayeeBankAccount" : "PayeeBankAccount.StatusEffectiveDate";
                            logger.error("WARNING -- skipping payeeBankAccountChanged fraud check for bank account due to: " + property + "== NULL: PaycheckId:BillRefNo:PayeeSrcBankId --" + split.getBillPayment().getSourceId() + ":" + split.getReferenceNumber() + ":" + split.getPayeeBankAccount().getSourceBankAccountId());
                            continue;
                        }
                        if(pBA.getStatusEffectiveDate().after(dateToCompare)){
                            payeeBankAcctUpdateCount++;
                        }

                }

                if (totalBillPaymentAmt.compareTo(amountToCompare) > 0 && payeeBankAcctUpdateCount>0) {
                    //Flag the Company for Fraud
                    getCompany().setFraudFlag();

                    //Create Payee event
                    String note = "Payee " + billPayment.getPayee().getName()
                            + " paid " + totalBillPaymentAmt + " in a single payment with date "
                            + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT)
                            +" within "+fraudPayeeXDaysBankAcctUpdated+" days of payee bank account update";

                    CompanyEvent event = CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.PayeePaidGreaterThanMax, this, note);
                    event.addCompanyEventDetail(EventDetailTypeCode.PayeeId, billPayment.getPayee().getId().toString());
                }
            }

    }

    public void totalBillPaymentSubmissionExceedsLimit() {
        // Pull the Max BillPayment Amount and number of payrolls for Fraud Consideration from the SP Params

        SpcfMoney amountToCompare = new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPMax).getValue());

        int fraudBPMaxXPayrolls = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPMaxXPayrolls).getValue());

        Long as400PayrollCount = 0L;
        if (getCompany().getQuickbooksInfo() != null) {
            as400PayrollCount = getCompany().getQuickbooksInfo().getAS400PayrollCount();
        }
        int numberOfCompletePayrollRuns = getPayrollRunCountByStatus(getCompany(), PayrollStatus.Complete);
        if ((numberOfCompletePayrollRuns + as400PayrollCount) < fraudBPMaxXPayrolls) {
            if (getPayrollDirectDepositAmount().isGreaterThan(amountToCompare)) {
                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Create NumberOfPayrollsPerDayExceeded event
                String note = "Company ran a total bill payment submission of amount " + getPayrollDirectDepositAmount() +
                        " for date "
                        + StringFormatter.formatDate(getPaycheckSettlementDate().toLocal(), PayrollRun.DATE_FORMAT);

                Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.TotalBillPaymentExceedsLimit, this, note));
            }
        }
    }

    public void payeePaidTooManyTimes() {
        // Get number of times an payee has been paid parameter to check for Fraud Consideration
        int fraudPayeePaidXTimes = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPayeePaidXTimes).getValue());

        // Get number of days to count the number of payments for a payee
        int fraudPayeeNumberOfDays = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudPayeeNumberOfDaysMultiplePayments).getValue());


        SpcfCalendar fromDate = getPayrollRunDate().copy();
        fromDate.addDays(fraudPayeeNumberOfDays * -1);

        DomainEntitySet<PayrollRun> payrollRuns = findEarlierPayrollRuns(fromDate);

        for (BillPayment billPayment : getBillPaymentCollection()) {
            int numberOfBillPayments = 0;
            //Find the bill payments for the same payee within the collection of payroll runs
            for (PayrollRun payrollRun : payrollRuns) {

                Criterion<BillPayment> criteria = BillPayment.Payee().Id().equalTo(billPayment.getPayee().getId());

                DomainEntitySet<BillPayment> billPayments = payrollRun.getBillPaymentCollection().find(criteria);
                // Add 1 for each separate payroll run that the employee has been paid
                if (billPayments.size() > 0) {
                    numberOfBillPayments = numberOfBillPayments + 1;
                }
            }


            if (numberOfBillPayments >= fraudPayeePaidXTimes) {
                //Flag the Company for Fraud
                getCompany().setFraudFlag();

                //Create PayeePaidTooManyTimes event
                String note = "Payee " + billPayment.getPayee().getName()
                        + " paid " + numberOfBillPayments + " times in a period of " + fraudPayeeNumberOfDays + " days.";

                CompanyEvent event = CompanyEvent.createFraudPayrollEvent(getCompany(), EventTypeCode.PayeePaidTooManyTimes, this, note);
                event.addCompanyEventDetail(EventDetailTypeCode.PayeeId, billPayment.getPayee().getId().toString());
                Application.save(event);
            }
        }
    }

    public static boolean isBeforeTOKCutoff(SpcfCalendar pSettlementDate, SpcfCalendar pPayrollRunDate) {
        SpcfCalendar settlementDateCopy = pSettlementDate.copy();
        int waitPeriod = SourcePayrollParameter.findIntValue(SourceSystemCode.QBDT, SourcePayrollParameterCode.ThirdParty401kOffloadWaitPeriod);
        CalendarUtils.addBusinessDays(settlementDateCopy, waitPeriod);
        return OffloadGroup.isBeforeTOKCutoffTime(settlementDateCopy, pPayrollRunDate);
    }

    public boolean isBeforeTOKCutoff() {
        return isBeforeTOKCutoff(getPaycheckDate(), getPayrollRunDate());
    }

    public void checkEmployeeInactivityFraud() {
        SpcfMoney inactivityLimitAmount;
        int inactivityDaysLimit;
        EventTypeCode eventTypeCode;
        CompanyService service;
        // add up all of the credit transactions
        SpcfDecimal totalCreditAmount = SpcfDecimal.createInstance(0.00);
        SpcfCalendar inactivityStartDate = PSPDate.getPSPTime();
        Map<String,String> employessWithUBankAccountMap = new HashMap<String, String>();

        inactivityLimitAmount = new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudDDInactivityPayrollAmount).getValue());
        inactivityDaysLimit = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudDDInactivityDays).getValue());
        inactivityStartDate.addDays(-inactivityDaysLimit);
        eventTypeCode = EventTypeCode.InactivityDDPayrollAmountExceeded;
        if (getCompany().hasService(ServiceCode.RiskAssessment)) {
            service = getCompany().getService(ServiceCode.RiskAssessment);
        } else {
            service = getCompany().getService(ServiceCode.DirectDeposit);
        }
        // make sure they have been on service long enough
        if (service.getCreatedDate().after(inactivityStartDate)) {
            return;
        }
        List payrollRunCount = Application.executeQuery(PayrollRun.class, new Query<PayrollRun>()
                .Select(PayrollRun.Id().Count())
                .Where(PayrollRun.Company().equalTo(getCompany())
                        .And(PayrollRun.PayrollRunDate().greaterOrEqualThan(inactivityStartDate))));
        // if there has been no payrolls submitted within the inactivity window, other than the current payroll
        if (Long.parseLong(payrollRunCount.get(0).toString()) > 1) {
            return;
        }



        for (Paycheck paycheck : getDDPaycheckCollection()) {
            if (paycheck.getStatus().equals(PaycheckStatusCode.Active)) {
                for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
                    EmployeeBankAccount eBA = paycheckSplit.getEmployeeBankAccount();
                    if (eBA != null && eBA.getStatusEffectiveDate() != null && eBA.getStatusEffectiveDate().after(inactivityStartDate)) {
                        String employeeName = paycheckSplit.getPaycheck().getDDEmployee().getFirstName()+" "+paycheckSplit.getPaycheck().getDDEmployee().getLastName();
                        String sourceEmployeeId = paycheckSplit.getPaycheck().getDDEmployee().getSourceEmployeeId();
                        if(!employessWithUBankAccountMap.containsKey(sourceEmployeeId))
                            employessWithUBankAccountMap.put(paycheckSplit.getPaycheck().getDDEmployee().getSourceEmployeeId(),employeeName);
                    }
                    totalCreditAmount = totalCreditAmount.add(paycheckSplit.getPaycheckSplitAmount());
                }

            }
        }

        if (totalCreditAmount.compareTo(inactivityLimitAmount) > 0 && employessWithUBankAccountMap.size()>0) {
            getCompany().setFraudFlag();
            Collection<String> employeeNames= employessWithUBankAccountMap.values();
            String[] updatedEmployees = employeeNames.toArray(new String[0]);

            String updatedEmployeesStr=StringUtils.join(updatedEmployees, ',');

            //Create event
            String note = "Company ran a payroll for " + totalCreditAmount +
                    ". This company has been inactive for " + inactivityDaysLimit + " or more days." +
                    " Employees: "+updatedEmployeesStr + " account/s were updated" ;

            Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), eventTypeCode, this, note));

        }


    }

    public void checkPayeeInactivityFraud() {
        SpcfMoney inactivityLimitAmount;
        int inactivityDaysLimit;
        EventTypeCode eventTypeCode;
        CompanyService service;
        // add up all of the credit transactions
        SpcfDecimal totalCreditAmount = SpcfDecimal.createInstance(0.00);
        SpcfCalendar inactivityStartDate = PSPDate.getPSPTime();
        inactivityLimitAmount = new SpcfMoney(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPInactivityPayrollAmount).getValue());
        inactivityDaysLimit = Integer.parseInt(FraudRule.findFraudRule(getCompany()).findFraudValueByName(FraudValueType.FraudBPInactivityDays).getValue());
        inactivityStartDate.addDays(-inactivityDaysLimit);
        eventTypeCode = EventTypeCode.InactivityBPPayrollAmountExceeded;
        Map<String,String> payeessWithUBankAccountMap = new HashMap<String, String>();

        if (getCompany().getSourceSystemCd().equals(SourceSystemCode.IOP)) {
            // IOP companies do not have a BillPayment service to use for the start date
            service = getCompany().getService(ServiceCode.RiskAssessment);
        } else {
            service = getCompany().getService(ServiceCode.BillPayment);
        }

        // make sure they have been on service long enough
        if (service.getCreatedDate().after(inactivityStartDate)) {
            return;
        }
        List payrollRunCount = Application.executeQuery(PayrollRun.class, new Query<PayrollRun>()
                .Select(PayrollRun.Id().Count())
                .Where(PayrollRun.Company().equalTo(getCompany())
                        .And(PayrollRun.PayrollRunDate().greaterOrEqualThan(inactivityStartDate))));
        // if there has been no payrolls submitted within the inactivity window, other than the current payroll
        if (Long.parseLong(payrollRunCount.get(0).toString()) > 1) {
            return;
        }

        for (BillPayment billPayment : getBillPaymentCollection()) {
            if (billPayment.getStatus().equals(BillPaymentStatusCode.Active)) {
                for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                    PayeeBankAccount pBA = billPaymentSplit.getPayeeBankAccount();
                    if (pBA != null && pBA.getStatusEffectiveDate() != null && pBA.getStatusEffectiveDate().after(inactivityStartDate)) {
                        String payeeName = billPaymentSplit.getBillPayment().getPayee().getName();
                        String sourcePayeeId = billPaymentSplit.getBillPayment().getPayee().getSourcePayeeId();
                        if(!payeessWithUBankAccountMap.containsKey(sourcePayeeId)) {

                            payeessWithUBankAccountMap.put(sourcePayeeId,payeeName);
                        }

                    }
                    totalCreditAmount = totalCreditAmount.add(billPaymentSplit.getAmount());


                }
            }
        }
        if (totalCreditAmount.compareTo(inactivityLimitAmount) > 0 && payeessWithUBankAccountMap.size() > 0) {
            getCompany().setFraudFlag();
            Collection<String> payeeNames= payeessWithUBankAccountMap.values();
            String[] updatedPayees = payeeNames.toArray(new String[0]);

            String updatedPayeeStr=StringUtils.join(updatedPayees, ',');

            //Create event
            String note = "Company ran a payroll for " + totalCreditAmount +
                    ". This company has been inactive for " + inactivityDaysLimit + " or more days." +
                    " Payees: "+updatedPayeeStr + " account/s were updated" ;

            Application.save(CompanyEvent.createFraudPayrollEvent(getCompany(), eventTypeCode, this, note));

        }



    }




    public FinancialTransaction getUnsubmittedDirectImpound() {
        DomainEntitySet<FinancialTransaction> directDebits = getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        if(directDebits.size() > 0) {
            FinancialTransaction directDebit = directDebits.get(0);
            if(directDebit.getMoneyMovementTransaction() != null && directDebit.getMoneyMovementTransaction().isPendingTaxPayment()) {
                return directDebit;
            }
        }
        return null;
    }

    public boolean hasImpoundOffloaded() {
        DomainEntitySet<FinancialTransaction> impoundTxnList =
                FinancialTransaction.findFinancialTransactionsByAssociationType(
                        this.getCompany(), this, TransactionAssociationType.Impound);
        for (FinancialTransaction finTxn : impoundTxnList) {
            MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
            if (mmTxn != null && !mmTxn.isPendingMMT()) {
                return true;
            }
        }

        return false;
    }

    // this method will look for all employer debit transaction to see if they have offloaded
    public boolean havePayrollDebitTransactionsOffloaded() {
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(this)
                                .And(FinancialTransaction.Company().equalTo(getCompany()))
                                .And(FinancialTransaction.TransactionType().NACHABatchType().equalTo(NACHABatchType.Payroll))
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled)))
                        .OrderBy(FinancialTransaction.CreatedDate().Descending())
                        .EagerLoad(FinancialTransaction.MoneyMovementTransaction());

        for (FinancialTransaction finTxn : Application.find(FinancialTransaction.class, query)) {
            MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
            if (mmTxn != null && !mmTxn.isPendingMMT()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasTaxImpoundOffloaded() {
        return hasTaxImpoundOffloaded(true);
    }

    public boolean hasTaxImpoundOffloaded(boolean pIncludeDirectDebit) {
        DomainEntitySet<FinancialTransaction> taxTxnList =
                FinancialTransaction.findFinancialTransactionsByAssociationType(
                        this.getCompany(), this, TransactionAssociationType.Impound);

        if (taxTxnList.size() > 0) {
            for (FinancialTransaction finTxn : taxTxnList) {
                MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
                if (mmTxn != null) {
                    // 100k payment
                    if (mmTxn.isTaxPayment() && pIncludeDirectDebit && !mmTxn.isPendingTaxPayment()) {
                        return true;
                    }
                    // ach
                    else if(!mmTxn.isPendingMMT()){
                        return true;
                    }
                }
                // cut over
                else if (TransactionTypeCode.EmployerTaxDebit.equals(finTxn.getTransactionType().getTransactionTypeCd()) &&
                        finTxn.getQbdtTransactionInfo() != null &&
                        !TransactionStateCode.Created.equals(finTxn.getCurrentTransactionState().getTransactionStateCd())) {
                    return true;
                }
            }
        }

        return false;
    }



    //This is an "Adjustment" as opposed to a "Liability Adjustment."  Not a real PR, just one for the sake of balancing the ledger
    //assumes that if any liability adjustment isReconciling that the entire PR is reconciling

    public boolean isReconcilingAdjustment() {
        if (getPaycheckCollection().size() > 0) {
            return false;
        }
        for (LiabilityAdjustment la : getLiabilityAdjustmentCollection()) {
            if (la.getIsReconcilingAdjustment()) {
                return true;
            }
        }
        return false;
    }

    //where we are storing liabilities but not debiting ourselves

    public boolean isManuallyRecordedPayroll() {
        return getPayrollRunType().equals(PayrollType.Adjustment) && getFinancialTransactionCollection().size() == 0;
    }

    public static PayrollRun createAdjustmentPayrollRun(Company pCompany, SpcfCalendar pLiabilityAdjustmentDate) {
        PayrollRun payrollRun = new PayrollRun();

        payrollRun.setSourcePayRunId(SpcfUniqueId.generateRandomUniqueIdString());

        // Associate Company and Payroll Run
        payrollRun.setCompany(pCompany);
        payrollRun.setFundingModel(pCompany.getFundingModel().getFundingModelCd());

        // Set PayrollRun date
        payrollRun.setPayrollRunDate(PSPDate.getPSPTime());

        //Set paycheck date and paycheck settlement date
        payrollRun.setPaycheckDate(pLiabilityAdjustmentDate);

        SpcfCalendar settlementDate = pCompany.getNextValidPaycheckDepositDate(pLiabilityAdjustmentDate);
        payrollRun.setPaycheckSettlementDate(settlementDate);

        // Set PayrollRun status
        payrollRun.setPayrollRunStatus(PayrollStatus.Pending);

        // Set PayrollRun type = Adjustment
        payrollRun.setPayrollRunType(PayrollType.Adjustment);

        payrollRun = Application.save(payrollRun);

        return payrollRun;
    }

    public static PayrollRun createFeePayrollRun(Company pCompany, SpcfCalendar pPaycheckDate) {
        PayrollRun payrollRun = new PayrollRun();

        payrollRun.setSourcePayRunId(SpcfUniqueId.generateRandomUniqueIdString());

        // Associate Company and Payroll Run
        payrollRun.setCompany(pCompany);
        payrollRun.setFundingModel(pCompany.getFundingModel().getFundingModelCd());

        // Set PayrollRun date
        payrollRun.setPayrollRunDate(PSPDate.getPSPTime());

        //Set paycheck date and paycheck settlement date
        payrollRun.setPaycheckDate(pPaycheckDate);

        SpcfCalendar settlementDate = pCompany.getNextValidPaycheckDepositDate(pPaycheckDate);
        payrollRun.setPaycheckSettlementDate(settlementDate);

        // Set PayrollRun status
        payrollRun.setPayrollRunStatus(PayrollStatus.Pending);

        // Set PayrollRun type = FeeOnly
        payrollRun.setPayrollRunType(PayrollType.FeeOnly);

        payrollRun = Application.save(payrollRun);

        return payrollRun;
    }

    public boolean isHistoricalPayroll() {
        SpcfCalendar paycheckDate = getPaycheckDate().copy();
        if (paycheckDate == null) {
            return false;
        }

        if(getCompany().getService(ServiceCode.Tax) == null || !getCompany().isCompanyOnService(ServiceCode.Tax) || getCompany().isMigratingToAssisted()) {
            return false;
        }

        return !getCompany().isServiceSupportedAsOf(ServiceCode.Tax, paycheckDate);
    }

    public DomainEntitySet<Paycheck> eagerLoadPaychecks(List<String> pSourcePaycheckIds, boolean eagerLoadDD, boolean eagerLoadTax) {
        Query<Paycheck> paycheckQuery = new Query<Paycheck>();
        paycheckQuery.Where(Paycheck.PayrollRun().equalTo(this)
                .And(Paycheck.SourcePaycheckId().in(pSourcePaycheckIds.toArray(new String[pSourcePaycheckIds.size()]))));

        if(eagerLoadDD) {
            paycheckQuery.EagerLoad(Paycheck.PaycheckSplitSet());
        }

        if(eagerLoadTax) {
            paycheckQuery.EagerLoad(Paycheck.TaxSet());
        }

        /**@see {@link Paycheck#findPaychecks(Company, java.util.Set)}*/
        DomainEntitySet<Paycheck> paychecks =
                Application.find(Paycheck.class, paycheckQuery).find(Paycheck.SourcePaycheckId().in(pSourcePaycheckIds.toArray(new String[pSourcePaycheckIds.size()])));
        for (Paycheck paycheck : paychecks) {
            Application.getSessionCache().addPrimaryKey(paycheck.getNaturalKey(), paycheck.getId());
        }

        return paychecks;
    }

    public DomainEntitySet<FinancialTransaction> findFinancialTransactionsByLawTypeState(Law pLaw, TransactionTypeCode pTransactionTypeCode, TransactionStateCode ... pTransactionStateCodes) {
        Criterion<FinancialTransaction> financialTransactionCriterion =
                FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(pTransactionTypeCode)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStateCodes));
        if(pLaw != null) {
            financialTransactionCriterion = financialTransactionCriterion.And(FinancialTransaction.Law().equalTo(pLaw));
        }

        return getFinancialTransactionCollection().find(financialTransactionCriterion);
    }

    public boolean isSuperseded() {
        return getPayrollRunStatus() == PayrollStatus.Superseded;
    }

    public boolean isBackDated(){
        SpcfCalendar payrollRunCreateDate = getCreatedDate().copy();
        CalendarUtils.clearTime(payrollRunCreateDate);
        return payrollRunCreateDate.after(getPaycheckDate());
    }

    public DomainEntitySet<Paycheck> getDDPaycheckCollection() {
        return getPaycheckCollection().find(Paycheck.DDEmployee().isNotNull());
    }

    //get bank account and debit amount from impounds.
    public PayrollDebitInfo getPayrollDebitInfo() {
        PayrollDebitInfo returnInfo = new PayrollDebitInfo();

        if (getCompany().hasService(ServiceCode.RiskAssessment)) {
            returnInfo.achAmount = getPayrollDirectDepositAmount();
        } else {
            DomainEntitySet<FinancialTransaction> financialTransactions =
                    getFinancialTransactionCollection().find(
                            FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound)
                                    .Or(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax))
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Voided, TransactionStateCode.Cancelled))
                                    .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH)));
            returnInfo.achAmount = SpcfMoney.ZERO;
            for(FinancialTransaction financialTransaction : financialTransactions){
                if (returnInfo.bankAccount == null) {
                    returnInfo.bankAccount = financialTransaction.getDebitBankAccount();
                }
                if (financialTransaction.getMoneyMovementTransaction() != null) {
                    returnInfo.achAmount = new SpcfMoney(returnInfo.achAmount.add(financialTransaction.getFinancialTransactionAmount()));
                }
            }
        }

        return returnInfo;
    }

    public class PayrollDebitInfo {
        public SpcfMoney achAmount = SpcfMoney.ZERO;
        public BankAccount bankAccount;
    }

    @Override
    public void setPaycheckSettlementDate(SpcfCalendar settlementDate) {
        if (settlementDate != null && !CalendarUtils.isTimeClear(settlementDate)) {
            throw new RuntimeException("PayCheckSettlementDate being set to wrong time: " + settlementDate.toString());
        }
        super.setPaycheckSettlementDate(settlementDate);
    }

    @Override
    public void setPayrollRunStatus(PayrollStatus payrollRunStatus) {
        setPayrollRunStatus(payrollRunStatus, true);
    }

    public boolean updateEETotalsCalculationRequired() {

        if(Application.find(EmpTotalsPayrollRun.class, EmpTotalsPayrollRun.PayrollRun().equalTo(this)).getFirst() != null) {
            return true;
        }

        if(!getLiabilityAdjustmentCollection().isEmpty()) {
            return true;
        }

        for (Paycheck paycheck : getPaycheckCollection()) {
            if(paycheck.getQbdtPaycheckInfo() != null &&
                    (!paycheck.getTaxCollection().isEmpty() || !paycheck.getCompensationCollection().isEmpty() || !paycheck.getDeductionCollection().isEmpty() || !paycheck.getEmployerContributionCollection().isEmpty())) {
                return true;
            }
        }

        return false;
    }

    public void updateEECalculationToken() {
        if (getCompany().isCompanyOnService(ServiceCode.Tax)) {
            setEECalculationToken(PayrollRun.fetchNextEECalculationToken());
        }

    }

    public void setPayrollRunStatus(PayrollStatus payrollRunStatus, boolean createAtfPayrollRecord)
    {
        // If we are creating ATF records and the status is going from Pending to Complete.
        if (createAtfPayrollRecord && PayrollStatus.Complete.equals(payrollRunStatus) && getPayrollRunStatus().equals(PayrollStatus.Pending)) {
            ATFPayrollsToProcess newPayrollToProcess = new ATFPayrollsToProcess();
            newPayrollToProcess.setPayrollRun(this);
            Application.save(newPayrollToProcess);
        }
        super.setPayrollRunStatus(payrollRunStatus);
    }

    public static Long fetchNextUsageBillingToken() {
        return Application.nextSequenceValue(SequenceId.SEQ_USAGE_BILLING_TOKEN, Long.class);
    }

    public static Long fetchAssistedNextUsageBillingToken() {
        return Application.nextSequenceValue(SequenceId.SEQ_ASST_USAGE_BILLING_TOKEN, Long.class);
    }

    public static Long fetchNextEECalculationToken() {
        return Application.nextSequenceValue(SequenceId.SEQ_EE_CALCULATION_TOKEN, Long.class);
    }

    /**
     * @param pCompany
     * @return
     */
    public static PayrollRun findLastPayrollRunWithActivePaychecks(Company pCompany) {
        PayrollRun payrollRun = null;

        DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, new Query<PayrollRun>().Where(PayrollRun.Company().equalTo(pCompany).And(PayrollRun.PaycheckSet().Exists(Paycheck.Status().equalTo(PaycheckStatusCode.Active).And(Paycheck.Company().equalTo(pCompany))))).OrderBy(PayrollRun.PayrollRunDate().Descending()).LimitResults(0, 1));
        if (payrollRuns != null && payrollRuns.size() > 0) {
            payrollRun = payrollRuns.get(0);
        }
        return payrollRun;

    }
    public void adjustEEInitiationDatesFor5D() {

        if (!this.getFundingModel().equals(FundingModel.Codes.FIVE_DAY)) {
            return;
        }
        logger.info("Funding model is 5D, adjusting employee initiation dates, payroll run id="+this.getId());
        DomainEntitySet<FinancialTransaction> employeeTransactionSet = this.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.EmployeeDdCredit);
        DomainEntitySet<FinancialTransaction> employerTransactionSet = this.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit}, new TransactionStateCode[]{TransactionStateCode.Created,
                TransactionStateCode.Executed});

        if (employeeTransactionSet.isNotEmpty() && employerTransactionSet.isNotEmpty()) {
            for (int i = 0; i < employeeTransactionSet.size(); i++) {
                correctEEInitiationDates(employeeTransactionSet.get(i), employerTransactionSet.get(0));
            }
        }
    }

    private void correctEEInitiationDates(FinancialTransaction eeFT, FinancialTransaction erFT) {
        SpcfCalendar eeInitiationDate = eeFT.getMoneyMovementTransaction().getInitiationDate().copy();
        SpcfCalendar erInitiationDate = erFT.getMoneyMovementTransaction().getInitiationDate().copy();

        SpcfCalendar expectedEEInitiationDate = erInitiationDate.copy();
        CalendarUtils.addBusinessDays(expectedEEInitiationDate, 3);

        CalendarUtils.clearTime(eeInitiationDate);
        CalendarUtils.clearTime(expectedEEInitiationDate);

        boolean isMMTInitDateCorrect = eeInitiationDate.equals(expectedEEInitiationDate);

        if (!isMMTInitDateCorrect && erFT.getCurrentTransactionState().getTransactionStateCd().in(TransactionStateCode.Created))  {
            SpcfCalendar erOrginalInitiationDate = erFT.getMoneyMovementTransaction().getInitiationDate().copy();
            CalendarUtils.addBusinessDays(erOrginalInitiationDate, 3);
            logger.info("Updating the employee MMT. MMT id="+ eeFT.getMoneyMovementTransaction().getId()+", old init date="+eeInitiationDate+", new init date=" + expectedEEInitiationDate);
            eeFT.getMoneyMovementTransaction().updateInitiationDate(erOrginalInitiationDate);
        }

        //Check if it requires settlement date update as well
        SpcfCalendar eeSettlementDate = eeFT.getSettlementDate().copy();
        SpcfCalendar expectedSettlementDate = expectedEEInitiationDate.copy();
        CalendarUtils.addBusinessDays(expectedSettlementDate, 2);
        CalendarUtils.clearTime(eeSettlementDate);
        CalendarUtils.clearTime(expectedSettlementDate);

        boolean isFtSettlementDateCorrect = eeSettlementDate.equals(expectedSettlementDate);

        if(!isFtSettlementDateCorrect){
            SpcfCalendar expectedOriginalSettlementDate = eeFT.getMoneyMovementTransaction().getInitiationDate().copy();
            CalendarUtils.addBusinessDays(expectedOriginalSettlementDate, 2);
            logger.info("Updating the employee FT. FT id="+ eeFT.getId()+", old settlement date="+eeFT.getSettlementDate()+", new settlement date=" + expectedOriginalSettlementDate);
            eeFT.setSettlementDate(expectedOriginalSettlementDate);
        }
    }
}
