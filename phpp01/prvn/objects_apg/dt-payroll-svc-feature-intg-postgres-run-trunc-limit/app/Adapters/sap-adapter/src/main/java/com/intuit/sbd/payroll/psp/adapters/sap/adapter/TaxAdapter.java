package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.google.common.collect.Lists;
import com.intuit.payroll.agency.api.IPaymentFrequency;
import com.intuit.payroll.agency.api.IRulesInfo;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.api.RulesObjectBroker;
import com.intuit.payroll.agency.dao.FrequencyData;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.SAPException;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.aspect.CompanyIdentifierType;
import com.intuit.sbd.payroll.psp.context.aspect.TenantId;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.threading.ChildThreadRequestContextHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.SortableProperty;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.hibernate.FlushMode;
import org.hibernate.type.IntegerType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * User: cyoder Date: May 4, 2009 Time: 1:33:20 PM
 */
public class TaxAdapter {

    private static final SpcfLogger logger = PayrollServices.getLogger(CompanyAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final SpcfDecimal ZERO_VALUE = (SpcfDecimal) new SpcfMoney("0.00");
    private static final String MA_WH = "MA-M941-PAYMENT";
    // quarters
    private static final String LS_Q1 = "Q1";
    private static final String LS_Q2 = "Q2";
    private static final String LS_Q3 = "Q3";
    private static final String LS_Q4 = "Q4";
    private static final String LS_CLA = "CLA";
    private static final String LS_VOID = "Void";
    private static final String LS_PAYROLL = "Payroll";
    private static final String LS_PAYMENT = "Payment";
    private static final String LS_REFUND = "Refund";
    private static final String LS_TOR = "Take on Return";
    private static final String LS_ADJUSTMENT = "Adjustment";
    private static int mInterval;
    private static int mMinPoolSize;
    private static int mMaxPoolSize;
    private static int mMaxWait;
    private static int mBatchSize;
    public static String QUARTER_OUT_OF_RANGE_EXCEPTION_STRING = "Quarter must be current, one prior, or one future";
    public PSPRequestContextManager pspRequestContextManager;

    public TaxAdapter() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    @FlexMethod
    public ArrayList<SAPAgency> getAgencyList() throws Throwable {

        ArrayList<SAPAgency> returnVal = new ArrayList<SAPAgency>();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            DomainEntitySet<Agency> allAgencies = Application.findObjects(Agency.class);

            for (int i = 0; i < allAgencies.size(); i++) {
                TaxTranslator.getSAPAgencyFromDomainEntity(allAgencies.get(i), returnVal, false);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting agency list.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    private String getStatusStringFromMoneyMovementTransaction(MoneyMovementTransaction mmt) {

        if (mmt == null) {
            return null;
        }

        StringBuilder statusBuilder = new StringBuilder();

        boolean isReExecuting = mmt.isReExecutingTaxPayment();

        switch (mmt.getTaxPaymentStatus()) {
            case OnHold:
                if (isReExecuting) {
                    statusBuilder.append("Re-execution ");
                }
                statusBuilder.append("On Hold - ");
                for (TaxPaymentOnHoldReason tpOnHoldReason : mmt.getActiveOnHoldReasons()) {
                    statusBuilder.append(tpOnHoldReason.getOnHoldReasonCd().toString()).append(", ");
                }
                statusBuilder.delete(statusBuilder.length() - 2, statusBuilder.length());
                break;
            case ReadyToSend:
                statusBuilder.append("Pending");
                if (isReExecuting) {
                    statusBuilder.append(" Re-execution");
                }
                break;
            case ATFFinalized:
                statusBuilder.append("Finalized");
                break;
            case SentToAgency:
                if (isReExecuting) {
                    statusBuilder.append("Re-executed");
                } else {
                    statusBuilder.append("Executed");
                }
                statusBuilder.append(" - Sent to Agency");
                break;
            case AcknowledgedByAgency:
            case ReturnedTaxPaid:
                if (mmt.getFirstFinancialTransaction().getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Completed)) {
                    statusBuilder.append("Completed");
                } else {
                    if (isReExecuting) {
                        statusBuilder.append("Re-executed");
                    } else {
                        statusBuilder.append("Executed");
                    }
                    statusBuilder.append(" - Acknowledged by Agency");
                }
                break;
            case RejectedByAgency:
                statusBuilder.append("Rejected - ");
                EftpsPaymentDetail eftpsPaymentDetail = EftpsPaymentDetail.findPaymentDetailByMoneyMovementTransaction(mmt);
                statusBuilder.append(eftpsPaymentDetail.getReason());
                break;
            case ReturnedTaxNotPaid:
                statusBuilder.append("Returned - ");
                eftpsPaymentDetail = EftpsPaymentDetail.findPaymentDetailByMoneyMovementTransaction(mmt);
                statusBuilder.append(eftpsPaymentDetail.getReturnCd());
        }

        return statusBuilder.toString();
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCompanyTaxPayments)
    public ArrayList<SAPTaxPaymentYear> getPaymentTemplateYears(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, boolean includePossibleBackdateYears) throws Throwable {

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            SpcfCalendar maxPaymentDate = getMaxPaymentDate(company);
            Map<CompanyAgencyPaymentTemplate, CompanyAgencyPaymentTemplate.SpcfCalendarRange> companyTemplateValidDates = CompanyAgencyPaymentTemplate.getCompanyTemplateValidDates(company);

            Map<Integer, SAPTaxPaymentYear> yearTemplateMap = new HashMap<Integer, SAPTaxPaymentYear>();
            for (Map.Entry<CompanyAgencyPaymentTemplate, CompanyAgencyPaymentTemplate.SpcfCalendarRange> entry : companyTemplateValidDates.entrySet()) {
                int maxYear = entry.getValue().end != null ? entry.getValue().end.getYear() : maxPaymentDate.getYear();
                for (int curYear = entry.getValue().begin.getYear(); curYear <= maxYear; curYear++) {
                    if (includePossibleBackdateYears || entry.getKey().getPaymentTemplate().getProcessingStartDate() == null || entry.getKey().getPaymentTemplate().getProcessingStartDate().getYear() <= curYear) {
                        if (!yearTemplateMap.containsKey(curYear)) {
                            SAPTaxPaymentYear sapTaxPaymentYear = new SAPTaxPaymentYear();
                            sapTaxPaymentYear.setYear(Integer.toString(curYear));
                            sapTaxPaymentYear.setPaymentTemplates(new ArrayList<SAPPaymentTemplate>());
                            yearTemplateMap.put(curYear, sapTaxPaymentYear);
                        }
                        yearTemplateMap.get(curYear).getPaymentTemplates().add(TaxTranslator.getPaymentTemplateFromDomainEntity(entry.getKey()));
                    }
                }
            }

            return new ArrayList<SAPTaxPaymentYear>(yearTemplateMap.values());

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving payment template years.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCompanyTaxPayments)
    public SAPPaymentTemplateYearPayment getTemplateYearPayment(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String taxYear, String paymentTemplateCd) throws Throwable {

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            SAPPaymentTemplateYearPayment yearPayment = new SAPPaymentTemplateYearPayment();
            yearPayment.setPaymentTemplateCd(paymentTemplateCd);
            yearPayment.setTaxYear(taxYear);
            yearPayment.setTemplateQuarterPayments(new ArrayList<SAPPaymentTemplateQuarterPayment>());

            for (int i = 1; i <= 4; i++) {
                SpcfCalendar quarterStart = TaxPeriod.getQuarterStart(i, Integer.parseInt(taxYear));

                SAPPaymentTemplateQuarterPayment templateQuarterPayment = getTemplateQuarterPayment(company,
                        paymentTemplateCd,
                        "Q" + Integer.toString(i),
                        quarterStart,
                        TaxPeriod.getPeriodEndDate(quarterStart));
                if (templateQuarterPayment != null) {
                    yearPayment.getTemplateQuarterPayments().add(templateQuarterPayment);
                }
            }

            SpcfMoney paymentsMade = (SpcfMoney) ZERO_VALUE;
            SpcfMoney pendingPayments = (SpcfMoney) ZERO_VALUE;
            for (SAPPaymentTemplateQuarterPayment quarterPayment : yearPayment.getTemplateQuarterPayments()) {
                pendingPayments = (SpcfMoney) SpcfUtils.add(pendingPayments, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(quarterPayment.getPendingPaymentsTotal()));
                paymentsMade = (SpcfMoney) SpcfUtils.add(paymentsMade, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(quarterPayment.getPaymentsMadeTotal()));
            }

            yearPayment.setPendingPaymentsTotal(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pendingPayments));
            yearPayment.setPaymentsMadeTotal(SAPTranslator.getDoubleFromSpcfMoneyNullZero(paymentsMade));
            yearPayment.setYearPaymentsTotal(SAPTranslator.getDoubleFromSpcfMoneyNullZero(SpcfUtils.add(pendingPayments, paymentsMade)));

            return yearPayment;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving template year payments.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private SAPPaymentTemplateQuarterPayment getTemplateQuarterPayment(Company company,
                                                                       String paymentTemplateCd,
                                                                       String quarter,
                                                                       SpcfCalendar fromDate,
                                                                       SpcfCalendar toDate) throws Throwable {

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        if (paymentTemplate.getSupportStartDate().after(toDate)) {
            return null;
        }

        SAPPaymentTemplateQuarterPayment quarterPayment = new SAPPaymentTemplateQuarterPayment();
        quarterPayment.setQuarter(quarter);
        quarterPayment.setYear(Integer.toString(fromDate.getYear()));
        quarterPayment.setPaymentTemplateCd(paymentTemplateCd);
        quarterPayment.setPendingPayments(new ArrayList<SAPPayment>());
        quarterPayment.setPaymentsMade(new ArrayList<SAPPayment>());

        MoneyMovementTransaction.TaxPaymentsFinder finder = MoneyMovementTransaction.findTaxPayments()
                .setCompany(company)
                .setPaymentTemplateCd(paymentTemplateCd)
                .setPeriodEndDateBegin(fromDate)
                .setPeriodEndDate(toDate)
                .setNonHPDE();

        DomainEntitySet<MoneyMovementTransaction> pendingPayments =
                finder.setPendingOrFinalized().find();
        DomainEntitySet<MoneyMovementTransaction> paymentsMade =
                finder.setExecutedOrSuccessful().find();

        PaymentMethod highestPriorityPaymentMethod = null;
        PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = paymentTemplate.getPaymentTemplatePaymentMethods().find(PaymentTemplatePaymentMethod.PaymentMethodOrder().equalTo(1)).getFirst();
        if (paymentTemplatePaymentMethod != null) {
            highestPriorityPaymentMethod = paymentTemplatePaymentMethod.getPaymentMethod();
        }

        SpcfMoney pendingPaymentsTotal = (SpcfMoney) ZERO_VALUE;
        for (MoneyMovementTransaction payment : pendingPayments) {
            String statusString = getStatusStringFromMoneyMovementTransaction(payment);
            List<SAPPaymentMethod> paymentMethods = null;
            if (!payment.getMoneyMovementTransactionAmount().equals(SpcfMoney.ZERO)) {

                if (payment.getMoneyMovementPaymentMethod() != highestPriorityPaymentMethod) {
                    paymentMethods = getAllPaymentMethods(payment.getCompany().getSourceSystemCd().name(), payment.getCompany().getSourceCompanyId(), paymentTemplateCd, null);
                }

                quarterPayment.getPendingPayments().add(TaxTranslator.getPayment(payment, statusString, paymentMethods));
                pendingPaymentsTotal = (SpcfMoney) SpcfUtils.add(pendingPaymentsTotal, payment.getMoneyMovementTransactionAmount());
            }
        }
        quarterPayment.setPendingPaymentsTotal(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pendingPaymentsTotal));

        SpcfMoney paymentsMadeTotal = (SpcfMoney) ZERO_VALUE;
        for (MoneyMovementTransaction payment : paymentsMade) {
            String statusString = getStatusStringFromMoneyMovementTransaction(payment);
            if (!payment.getMoneyMovementTransactionAmount().equals(SpcfMoney.ZERO)) {
                quarterPayment.getPaymentsMade().add(TaxTranslator.getPayment(payment, statusString, null));
                paymentsMadeTotal = (SpcfMoney) SpcfUtils.add(paymentsMadeTotal, payment.getMoneyMovementTransactionAmount());
            }
        }
        quarterPayment.setPaymentsMadeTotal(SAPTranslator.getDoubleFromSpcfMoneyNullZero(paymentsMadeTotal));

        quarterPayment.setNotStarted(!(pendingPayments.size() > 0 || paymentsMade.size() > 0));
        quarterPayment.setQuarterPaymentsTotal(SAPTranslator.getDoubleFromSpcfMoneyNullZero(SpcfUtils.add(pendingPaymentsTotal, paymentsMadeTotal)));

        return quarterPayment;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCompanyTaxPayments)
    public ArrayList<SAPPaymentTemplateQuarterPayment> getPaymentTemplateQuarters(String sourceSystemCd,
                                                                                  @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                                                                  boolean includePossibleBackdateYears) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            SpcfCalendar maxPaymentDate = getMaxPaymentDate(company);
            Map<CompanyAgencyPaymentTemplate, CompanyAgencyPaymentTemplate.SpcfCalendarRange> companyTemplateValidDates = CompanyAgencyPaymentTemplate.getCompanyTemplateValidDates(company);

            ArrayList<SAPPaymentTemplateQuarterPayment> sapPaymentTemplateQuarterPayments = new ArrayList<SAPPaymentTemplateQuarterPayment>();

            for (Map.Entry<CompanyAgencyPaymentTemplate, CompanyAgencyPaymentTemplate.SpcfCalendarRange> entry : companyTemplateValidDates.entrySet()) {
                SpcfCalendar processingStartDate = entry.getKey().getPaymentTemplate().getProcessingStartDate();
                int maxYear = entry.getValue().end != null ? entry.getValue().end.getYear() : maxPaymentDate.getYear();
                for (int curYear = entry.getValue().begin.getYear(); curYear <= maxYear; curYear++) {
                    int beginQuarter;
                    int maxQuarter;
                    if (curYear == entry.getValue().begin.getYear()) {
                        beginQuarter = TaxPeriod.getQuarterNumber(entry.getValue().begin);
                    } else {
                        beginQuarter = 1;
                    }
                    if (curYear == maxYear) {
                        maxQuarter = entry.getValue().end != null ? TaxPeriod.getQuarterNumber(entry.getValue().end) : TaxPeriod.getQuarterNumber(maxPaymentDate);
                    } else {
                        maxQuarter = 4;
                    }

                    for (int curQuarter = beginQuarter; curQuarter <= maxQuarter; curQuarter++) {
                        if (includePossibleBackdateYears || processingStartDate == null || processingStartDate.getYear() < curYear
                                || (processingStartDate.getYear() == curYear && TaxPeriod.getQuarterNumber(processingStartDate) <= curQuarter)) {
                            sapPaymentTemplateQuarterPayments.add(TaxTranslator.createQuarterPayment("Q" + curQuarter,
                                    Integer.toString(curYear),
                                    entry.getKey().getPaymentTemplate().getPaymentTemplateAbbrev(),
                                    entry.getKey().getPaymentTemplate().getPaymentTemplateCd()));
                        }
                    }
                }
            }

            return sapPaymentTemplateQuarterPayments;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving payment template quarters.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private SpcfCalendar getMaxPaymentDate(Company company) {
        //show payments up to either today or the latest payroll
        PayrollRun latestPayrollRun = PayrollRun.findLatestCompanyPayrollRun(company);

        SpcfCalendar maxPaymentDate;

        if (latestPayrollRun == null) {
            maxPaymentDate = PSPDate.getPSPTime();
        } else {
            maxPaymentDate = latestPayrollRun.getPaycheckDate();

            if (maxPaymentDate.before(PSPDate.getPSPTime())) {
                maxPaymentDate = PSPDate.getPSPTime();
            }
        }

        return maxPaymentDate;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCompanyTaxPayments)
    public SAPPaymentTemplateQuarterPayment getPaymentTemplateQuarterPayment(String sourceSystemCd,
                                                                             @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                                                             String paymentTemplateCd,
                                                                             String year,
                                                                             String quarter) throws Throwable {

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (quarter.equals(LS_Q1)) {
                return getTemplateQuarterPayment(company, paymentTemplateCd, LS_Q1,
                        TaxPeriod.Q1_BEGIN(year), TaxPeriod.Q1_END(year));
            } else if (quarter.equals(LS_Q2)) {
                return getTemplateQuarterPayment(company, paymentTemplateCd, LS_Q2,
                        TaxPeriod.Q2_BEGIN(year), TaxPeriod.Q2_END(year));
            } else if (quarter.equals(LS_Q3)) {
                return getTemplateQuarterPayment(company, paymentTemplateCd, LS_Q3,
                        TaxPeriod.Q3_BEGIN(year), TaxPeriod.Q3_END(year));
            } else {
                return getTemplateQuarterPayment(company, paymentTemplateCd, LS_Q4,
                        TaxPeriod.Q4_BEGIN(year), TaxPeriod.Q4_END(year));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error payment template quarter payment.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCompanyTaxPayments)
    public ArrayList<SAPTaxPaymentCheckDateSet> findPaymentDetailTransactions(String moneyMovementTransactionId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            ArrayList<SAPTaxPaymentCheckDateSet> sapTaxPaymentCheckDateSets = new ArrayList<SAPTaxPaymentCheckDateSet>();

            Expression<MoneyMovementTransaction> query =
                    new Query<MoneyMovementTransaction>()
                            .Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.createInstance(moneyMovementTransactionId)))
                            .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet());

            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, query);

            if (moneyMovementTransactions.size() != 1) {
                throw new Exception("Money movement transaction with id:" + moneyMovementTransactionId + " not found");
            }

            MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.get(0);

            Map<Date, ArrayList<FinancialTransaction>> checkDateMap = new TreeMap<Date, ArrayList<FinancialTransaction>>();
            for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                if (financialTransaction.getTransactionType().getTransactionCategory().equals(TransactionCategory.Agency)) {
                    if (!TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd()) && !TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                        continue;
                    }
                    Date checkDate = financialTransaction.getPayrollRun() == null ? SpcfUtils.convertSpcfCalendarToDate(financialTransaction.getCreatedDate()) : SAPTranslator.getDateFromSpcfCalendar(financialTransaction.getPayrollRun().getPaycheckDate());
                    if (checkDateMap.get(checkDate) == null) {
                        ArrayList<FinancialTransaction> financialTransactionsList = new ArrayList<FinancialTransaction>();
                        financialTransactionsList.add(financialTransaction);
                        checkDateMap.put(checkDate, financialTransactionsList);
                    } else {
                        checkDateMap.get(checkDate).add(financialTransaction);
                    }
                }
            }

            for (Date checkDate : checkDateMap.keySet()) {
                sapTaxPaymentCheckDateSets.add(TaxTranslator.getTaxPaymentCheckDateSetFromDomainEntities(checkDateMap.get(checkDate)));
            }
            return sapTaxPaymentCheckDateSets;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving payment detail transactions.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public List<SAPAgency> getCompanyAgencyTemplates(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, boolean thisCompanyOnly) throws Throwable {

        List<SAPAgency> agencies = new ArrayList<SAPAgency>();
        if (thisCompanyOnly) {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
                Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
                DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, new Query<CompanyAgency>().Where(CompanyAgency.Company().equalTo(company)));
                for (CompanyAgency companyAgency : companyAgencies) {
                    agencies.add(TaxTranslator.getSAPAgencyFromDomainEntity(companyAgency.getAgency(), companyAgency.getAgency().getPaymentTemplateCollection()));
                }
            } catch (Throwable t) {
                aeFactory.throwGenericException(String.format("Error obtaining Agencies for %s:%s", pSourceSystemCode, pSourceCompanyId), t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } else {
            agencies = getCompleteAgencyList();
        }
        Collections.sort(agencies, new Comparator<SAPAgency>() {
            public int compare(SAPAgency agency1, SAPAgency agency2) {

                if (!agency1.getAgencyId().equalsIgnoreCase("IRS") && (agency1.getAgencyId().compareTo(agency2.getAgencyId()) > 0 || agency2.getAgencyId().equalsIgnoreCase("IRS"))) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return agencies;
    }

    private ArrayList<SAPAgency> getCompleteAgencyList() throws Throwable {

        ArrayList<SAPAgency> returnVal = new ArrayList<SAPAgency>();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            DomainEntitySet<Agency> allAgencies = Application.find(Agency.class, Agency.AgencyId().notEqualTo("NOCALC"));

            for (int i = 0; i < allAgencies.size(); i++) {
                TaxTranslator.getSAPAgencyFromDomainEntity(allAgencies.get(i), returnVal, true);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting agency list.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewTaxLedger)
    public ArrayList<String> findCompanyTaxYears(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Exception {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            SpcfCalendar maxPaymentDate = getMaxPaymentDate(company);
            Map<CompanyAgencyPaymentTemplate, CompanyAgencyPaymentTemplate.SpcfCalendarRange> companyTemplateValidDates = CompanyAgencyPaymentTemplate.getCompanyTemplateValidDates(company);

            Set<String> yearSet = new HashSet<String>();
            for (Map.Entry<CompanyAgencyPaymentTemplate, CompanyAgencyPaymentTemplate.SpcfCalendarRange> entry : companyTemplateValidDates.entrySet()) {
                int maxYear = entry.getValue().end != null ? entry.getValue().end.getYear() : maxPaymentDate.getYear();
                for (int curYear = entry.getValue().begin.getYear(); curYear <= maxYear; curYear++) {
                    if (!yearSet.contains(Integer.toString(curYear))) {
                        yearSet.add(Integer.toString(curYear));
                    }
                }
            }

            return new ArrayList<String>(yearSet);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.ViewTaxLedger,
            OperationId.CreateManualLedgerEntry})
    public ArrayList<SAPAgency> findCompanyAgencies(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Exception {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            ArrayList<SAPAgency> agencies = new ArrayList<SAPAgency>();

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();

            companyAgencies = companyAgencies.find(CompanyAgency.Agency().AgencySupported().equalTo(true));

            for (CompanyAgency companyAgency : companyAgencies) {
                agencies.add(TaxTranslator.getSAPAgencyFromDomainEntity(companyAgency.getAgency(), companyAgency.getAgency().getPaymentTemplateCollection()));
            }

            return agencies;

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewTaxLedger)
    public ArrayList<SAPLawTransactions> findTaxTransactions(String sourceSystemCd,
                                                             @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                                             String transactionDescription,
                                                             String agencyCd,
                                                             String paymentTemplateCd,
                                                             String specifiedLawId,
                                                             String paymentMethod,
                                                             Date yearQuarterStartDate,
                                                             Date yearQuarterEndDate,
                                                             boolean includeNotPostedPayments) throws Throwable {
        return findTaxTransactions(sourceSystemCd,
                companyId,
                transactionDescription,
                agencyCd,
                paymentTemplateCd,
                specifiedLawId,
                paymentMethod,
                yearQuarterStartDate,
                yearQuarterEndDate,
                includeNotPostedPayments,
                true);
    }

    public ArrayList<SAPLawTransactions> findTaxTransactions(String sourceSystemCd,
                                                             String companyId,
                                                             String transactionDescription,
                                                             String agencyCd,
                                                             String paymentTemplateCd,
                                                             String specifiedLawId,
                                                             String paymentMethod,
                                                             Date yearQuarterStartDate,
                                                             Date yearQuarterEndDate,
                                                             boolean includeNotPostedPayments,
                                                             boolean beginNewHibernateSession) throws Throwable {

        ArrayList<SAPLawTransactions> sapLawTransactions = new ArrayList<SAPLawTransactions>();
        try {
            if (beginNewHibernateSession) {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            }

            boolean includePayrolls = StringUtils.isEmpty(transactionDescription) || transactionDescription.equals(LS_PAYROLL);
            boolean includePayments = StringUtils.isEmpty(transactionDescription) || transactionDescription.equals(LS_PAYMENT);
            boolean includeAdjustments = StringUtils.isEmpty(transactionDescription) || transactionDescription.equals(LS_ADJUSTMENT);
            boolean includeTORs = StringUtils.isEmpty(transactionDescription) || transactionDescription.equals(LS_TOR);
            boolean includeRefund = StringUtils.isEmpty(transactionDescription) || transactionDescription.equals(LS_REFUND);

            Company company = Company.findCompanyNoEagerLoad(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            CompanyAgency specifiedCompanyAgency = StringUtils.isNotEmpty(agencyCd) ? CompanyAgency.findCompanyAgency(company, agencyCd) : null;
            PaymentTemplate specifiedPaymentTemplate = StringUtils.isNotEmpty(paymentTemplateCd) ? PaymentTemplate.findPaymentTemplate(paymentTemplateCd) : null;

            SpcfCalendar yearQuarterStartCalendar = SAPTranslator.getSpcfCalendarFromDate(yearQuarterStartDate);

            SpcfCalendar yearStartCalendar = TaxPeriod.Q1_BEGIN(yearQuarterStartCalendar.getYear()); //Always get all the transactions from starting of the year to have correct YTD values
            if (specifiedPaymentTemplate != null && specifiedPaymentTemplate.getSupportStartDate() != null && specifiedPaymentTemplate.getSupportStartDate().after(yearStartCalendar)) {
                yearStartCalendar = specifiedPaymentTemplate.getSupportStartDate();
            }

            SpcfCalendar yearQuarterEndCalendar = SAPTranslator.getSpcfCalendarFromDate(yearQuarterEndDate);

            // init transactions map and summary
            Map<String, SAPLawTransactions> sapLawTransactionsMap = new HashMap<String, SAPLawTransactions>();
            TaxLedger.initializeTransactionsMap(sapLawTransactionsMap, company.getCompanyAgencyCollection(), specifiedCompanyAgency, specifiedPaymentTemplate, specifiedLawId, yearStartCalendar);
            SAPLawTransactions summaryTransactions = TaxLedger.initializeSummary(sapLawTransactionsMap, specifiedCompanyAgency, specifiedPaymentTemplate);

            TaxLedger.findTaxTransactions(paymentTemplateCd, paymentMethod, includeNotPostedPayments, includePayrolls, includePayments, includeRefund,  includeAdjustments, includeTORs, yearStartCalendar, yearQuarterEndCalendar, company, sapLawTransactionsMap, summaryTransactions, yearQuarterStartCalendar);

            for (String returnLaw : sapLawTransactionsMap.keySet()) {
                SAPLawTransactions lawTransactions = sapLawTransactionsMap.get(returnLaw);
                if (lawTransactions.getTaxTransactions() != null && lawTransactions.getTaxTransactions().size() > 0) {
                    sapLawTransactions.add(lawTransactions);
                }
            }

            if (summaryTransactions != null) {
                sapLawTransactions.add(summaryTransactions);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding tax transactions.", sourceSystemCd, companyId, t);
        } finally {
            if (beginNewHibernateSession) {
                PayrollServices.rollbackUnitOfWork();
            }
        }

        return sapLawTransactions;
    }

    @FlexMethod
    public ArrayList<SAPEmployeeTaxLedgerItem> findEmployeeLedgerItems(SAPLedgerItemDetailsCriterion pLedgerItemDetailsCriterion) throws Throwable {

        ArrayList<SAPEmployeeTaxLedgerItem> sapEmployeeTaxLedgerItems = new ArrayList<SAPEmployeeTaxLedgerItem>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pLedgerItemDetailsCriterion.getCompanyId(), SourceSystemCode.valueOf(pLedgerItemDetailsCriterion.getSourceSystemCd()));

            pspRequestContextManager.setRequestContextCompany(company);

            PayrollRun selectedPayrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(pLedgerItemDetailsCriterion.getPayrollRunId()));
            CompanyAdjustmentSubmission selectedVoid =
                    pLedgerItemDetailsCriterion.getVoidId() != null
                            ? PayrollServices.entityFinder.findById(CompanyAdjustmentSubmission.class, SpcfUniqueId.createInstance(pLedgerItemDetailsCriterion.getVoidId()))
                            : null;

            // init transactions map and used laws

            Law law = Application.find(Law.class, Law.LawId().equalTo(pLedgerItemDetailsCriterion.getLawId())).getFirst();
            PaymentTemplate paymentTemplate = law.getPaymentTemplate();

            if (selectedVoid == null && (pLedgerItemDetailsCriterion.getIsQTD() || pLedgerItemDetailsCriterion.getIsYTD())) {
                return findEmployeeLedgerItemsInDatabase(company, paymentTemplate, law, selectedPayrollRun, pLedgerItemDetailsCriterion.getIsYTD());
            }

            EmployeeAccumulator ea = new EmployeeAccumulator();

            if (pLedgerItemDetailsCriterion.getVoidId() != null && !pLedgerItemDetailsCriterion.getIsQTD() && !pLedgerItemDetailsCriterion.getIsYTD()) {
                DomainEntitySet<Paycheck> voidedPaychecks = selectedVoid.getPaychecksForCompanyVoid();

                for (Paycheck paycheck : voidedPaychecks) {
                    String employeeId = ea.addEmployee(paycheck.getSourceEmployee());

                    for (Tax tax : paycheck.getTaxCollection().find(Tax.Law().equalTo(law))) {
                        ea.subtractTotalWages(employeeId, tax.getTotalWagesAmount());
                        ea.subtractTaxableWages(employeeId, tax.getTaxableWagesAmount());
                        ea.subtractTaxAmount(employeeId, tax.getTaxLiabilityAmount());
                        if (tax.getLaw().getLawAbbrev().equalsIgnoreCase("FICA EE") || tax.getLaw().getLawAbbrev().equalsIgnoreCase("FICA ER")) {
                            /*  Add the tips to the taxableWages. (actually, subtract from them)    */
                            ea.addTaxableWages(employeeId, tax.getTipsTaxableWageAmount());
                            ea.subtractTaxTipsAmount(employeeId, tax.getTipsTaxableWageAmount());
                            ea.setEmployeeShowTaxTips(true);
                        }
                    }
                }

                DomainEntitySet<LiabilityAdjustment> voidedAdjustments = selectedVoid.getLiabilityAdjustmentsForCompanyVoid();
                for (LiabilityAdjustment liabilityAdjustment : voidedAdjustments.find(LiabilityAdjustment.Law().equalTo(law))) {
                    String employeeId = ea.addEmployee(liabilityAdjustment.getEmployee());

                    ea.subtractTotalWages(employeeId, liabilityAdjustment.getTotalWages());
                    ea.subtractTaxableWages(employeeId, liabilityAdjustment.getTaxableWages());
                    ea.subtractTaxAmount(employeeId, liabilityAdjustment.getAmount());

                }
            } else {

                DomainEntitySet<PayrollRun> payrollRuns = new DomainEntitySet<PayrollRun>();
                SpcfCalendar yearQuarterStartDate = TaxPeriod.Q1_BEGIN(selectedPayrollRun.getPaycheckDate().getYear());

                /*  Now get max(first_day_of_year_of_paycheck, not_null(Agency_Support_date));    */
                if (paymentTemplate.getSupportStartDate() != null && paymentTemplate.getSupportStartDate().after(yearQuarterStartDate)) {
                    yearQuarterStartDate = paymentTemplate.getSupportStartDate();
                }

                if (pLedgerItemDetailsCriterion.getIsQTD()) {
                    payrollRuns = PayrollServices.entityFinder.find(PayrollRun.class, new Query<PayrollRun>().Where(PayrollRun.Company().equalTo(company)
                            .And(PayrollRun.PaycheckDate().greaterOrEqualThan(TaxPeriod.getQuarterStart(selectedPayrollRun.getPaycheckDate())))
                            .And(PayrollRun.PaycheckDate().lessOrEqualThan(selectedPayrollRun.getPaycheckDate()))
                            .And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Superseded))
                            .And(PayrollRun.PaycheckSet().NotExists(Paycheck.SourcePaycheckId().like("-%"))))
                            .OrderBy(PayrollRun.PaycheckDate(), PayrollRun.CreatedDate(), PayrollRun.SourcePayRunId())
                            .EagerLoad(PayrollRun.PaycheckSet()));
                } else if (pLedgerItemDetailsCriterion.getIsYTD()) {
                    payrollRuns = PayrollServices.entityFinder.find(PayrollRun.class, new Query<PayrollRun>().Where(PayrollRun.Company().equalTo(company)
                            .And(PayrollRun.PaycheckDate().greaterOrEqualThan(yearQuarterStartDate))
                            .And(PayrollRun.PaycheckDate().lessOrEqualThan(selectedPayrollRun.getPaycheckDate()))
                            .And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Superseded))
                            .And(PayrollRun.PaycheckSet().NotExists(Paycheck.SourcePaycheckId().like("-%"))))
                            .OrderBy(PayrollRun.PaycheckDate(), PayrollRun.CreatedDate(), PayrollRun.SourcePayRunId())
                            .EagerLoad(PayrollRun.PaycheckSet()));
                } else {
                    payrollRuns.add(selectedPayrollRun);
                }

                for (PayrollRun payrollRun : payrollRuns) {
                    if (!pLedgerItemDetailsCriterion.isIncludeNotPostedPayments() && payrollRun.getPayrollRunStatus().equals(PayrollStatus.Pending)) {
                        continue;
                    }
                    for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                        // skip paychecks that were recalled
                        if (paycheck.isRecalled()) {
                            continue;
                        }

                        if (shouldCountLiabilityForItem(paycheck.isVoided(), payrollRun, selectedPayrollRun, paycheck.getCompanyAdjustmentSubmission(), selectedVoid)) {
                            // add paycheck wages
                            for (Tax tax : paycheck.getTaxCollection().find(Tax.Law().equalTo(law))) {
                                String employeeId = ea.addEmployee(paycheck.getSourceEmployee());
                                ea.addTotalWages(employeeId, tax.getTotalWagesAmount());
                                ea.addTaxableWages(employeeId, tax.getTaxableWagesAmount());
                                if (tax.getLaw().getLawAbbrev().equalsIgnoreCase("FICA EE") || tax.getLaw().getLawAbbrev().equalsIgnoreCase("FICA ER")) {
                                    /*  Add the tips to the taxableWages. (actually, subtract from them)    */
                                    ea.subtractTaxableWages(employeeId, tax.getTipsTaxableWageAmount());
                                    ea.addTaxTipsAmount(employeeId, tax.getTipsTaxableWageAmount());
                                    ea.setEmployeeShowTaxTips(true);
                                }
                                ea.addTaxAmount(employeeId, tax.getTaxLiabilityAmount());
                            }
                        }
                    }

                    for (LiabilityAdjustment liabilityAdjustment : payrollRun.getLiabilityAdjustmentCollection().find(LiabilityAdjustment.Law().equalTo(law))) {
                        String employeeId = ea.addEmployee(liabilityAdjustment.getEmployee());
                        ea.addTotalWages(employeeId, liabilityAdjustment.getTotalWages());
                        ea.addTaxableWages(employeeId, liabilityAdjustment.getTaxableWages());
                        ea.addTaxAmount(employeeId, liabilityAdjustment.getAmount());
                    }
                }
            }

            for (String employeeId : ea.getEmployeeIds()) {
                SAPEmployeeTaxLedgerItem sapEmployeeTaxLedgerItem = ea.getEmployee(employeeId);
                sapEmployeeTaxLedgerItems.add(sapEmployeeTaxLedgerItem);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employee ledger items.", pLedgerItemDetailsCriterion.getSourceSystemCd(), pLedgerItemDetailsCriterion.getCompanyId(), t);
        } finally {
            pspRequestContextManager.clearRequestContextCompany();
            PayrollServices.rollbackUnitOfWork();
        }

        return sapEmployeeTaxLedgerItems;
    }

    //fast version that runs in database.  Only used for QTD/YTD and if not selecting a void.
    private ArrayList<SAPEmployeeTaxLedgerItem> findEmployeeLedgerItemsInDatabase(Company company, PaymentTemplate paymentTemplate, Law law, PayrollRun selectedPayrollRun, boolean ytd) {

        SpcfCalendar earliestDate;
        if (ytd) {
            earliestDate = TaxPeriod.Q1_BEGIN(selectedPayrollRun.getPaycheckDate().getYear());
        } else {
            earliestDate = TaxPeriod.getQuarterStart(selectedPayrollRun.getPaycheckDate());
        }

        SpcfCalendar lastDate = selectedPayrollRun.getPaycheckDate();
        lastDate.addHours(8); //because of sql.timestamp conversion.  <= so no DST problem
        ArrayList<Object[]> taxLedgerEmployeeBreakdown = Application.executeNamedQuery("taxLedgerEmployeeBreakdownEnc",
                new String[]{"companyId", "paycheckStart", "paycheckEnd", "paymentTemplate", "lawId"},
                new Object[]{company.getId().toString(), new Timestamp(earliestDate.getTimeInMilliseconds()),
                        new Timestamp(lastDate.getTimeInMilliseconds()), paymentTemplate.getPaymentTemplateCd(),
                        law.getLawId()});
        

        ArrayList<SAPEmployeeTaxLedgerItem> returnList = new ArrayList<SAPEmployeeTaxLedgerItem>();
        for (Object[] objects : taxLedgerEmployeeBreakdown) {
            String name = (String) objects[0];
            String ssn = EncryptionUtils.deterministicDecrypt(Employee.TaxIdKeyName,(String) objects[1]);
            SpcfDecimal totalWages = new SpcfDecimalImpl((BigDecimal) objects[2]);
            SpcfDecimal taxableWages = new SpcfDecimalImpl((BigDecimal) objects[3]);
            SpcfDecimal tips = new SpcfDecimalImpl((BigDecimal) objects[4]);
            SpcfDecimal tax = new SpcfDecimalImpl((BigDecimal) objects[5]);

            SAPEmployeeTaxLedgerItem item = new SAPEmployeeTaxLedgerItem();
            if (law.getLawAbbrev().equalsIgnoreCase("FICA EE") || law.getLawAbbrev().equalsIgnoreCase("FICA ER")) {
                taxableWages.subtract(tips);
                item.setShowTaxTips(true);
            }
            item.setTaxableWages(SAPTranslator.getDoubleFromSpcfMoneyNullZero(taxableWages));
            item.setTotalWages(SAPTranslator.getDoubleFromSpcfMoneyNullZero(totalWages));
            item.setTaxTips(SAPTranslator.getDoubleFromSpcfMoneyNullZero(tips));
            item.setTaxAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(tax));
            item.setEmployeeName(name);
            item.setSocialSecurityNumber(ssn);
            returnList.add(item);
        }

        return returnList;

    }

    class EmployeeAccumulator {

        private Map<String, SAPEmployeeTaxLedgerItem> employeesMap = new HashMap<String, SAPEmployeeTaxLedgerItem>();
        private Map<String, SpcfDecimal> employeesTotalWagesMap = new HashMap<String, SpcfDecimal>();
        private Map<String, SpcfDecimal> employeesTaxableWagesMap = new HashMap<String, SpcfDecimal>();
        private Map<String, SpcfDecimal> employeesTaxAmountMap = new HashMap<String, SpcfDecimal>();
        private Map<String, SpcfDecimal> employeesTaxTipsMap = new HashMap<String, SpcfDecimal>();
        private Boolean employeeShowTaxTips = false;

        public String addEmployee(Employee employee) {

            if (employee == null) {
                String employeeId = "CLA";
                if (!employeesMap.containsKey(employeeId)) {
                    SAPEmployeeTaxLedgerItem sapEmployeeTaxLedgerItem = new SAPEmployeeTaxLedgerItem();
                    sapEmployeeTaxLedgerItem.setEmployeeName("CLA");
                    sapEmployeeTaxLedgerItem.setSocialSecurityNumber("");
                    employeesMap.put(employeeId, sapEmployeeTaxLedgerItem);
                    employeesTotalWagesMap.put(employeeId, ZERO_VALUE);
                    employeesTaxableWagesMap.put(employeeId, ZERO_VALUE);
                    employeesTaxAmountMap.put(employeeId, ZERO_VALUE);
                    employeesTaxTipsMap.put(employeeId, ZERO_VALUE);
                }
                return employeeId;
            } else {
                String employeeId = employee.getId().toString();
                if (!employeesMap.containsKey(employeeId)) {
                    SAPEmployeeTaxLedgerItem sapEmployeeTaxLedgerItem = new SAPEmployeeTaxLedgerItem();
                    sapEmployeeTaxLedgerItem.setEmployeeName(SAPTranslator.getEmployeeFullName(employee));
                    sapEmployeeTaxLedgerItem.setSocialSecurityNumber(employee.getTaxId());
                    employeesMap.put(employeeId, sapEmployeeTaxLedgerItem);
                    employeesTotalWagesMap.put(employeeId, ZERO_VALUE);
                    employeesTaxableWagesMap.put(employeeId, ZERO_VALUE);
                    employeesTaxAmountMap.put(employeeId, ZERO_VALUE);
                    employeesTaxTipsMap.put(employeeId, ZERO_VALUE);
                }
                return employeeId;
            }
        }

        public void addTotalWages(String employeeId, SpcfMoney totalWages) {

            employeesTotalWagesMap.put(employeeId, SpcfUtils.add(employeesTotalWagesMap.get(employeeId), totalWages));
        }

        public void addTaxableWages(String employeeId, SpcfMoney taxableWages) {

            employeesTaxableWagesMap.put(employeeId, SpcfUtils.add(employeesTaxableWagesMap.get(employeeId), taxableWages));
        }

        public void addTaxAmount(String employeeId, SpcfMoney taxAmount) {

            employeesTaxAmountMap.put(employeeId, SpcfUtils.add(employeesTaxAmountMap.get(employeeId), taxAmount));
        }

        public void addTaxTipsAmount(String employeeId, SpcfMoney taxAmount) {

            employeesTaxTipsMap.put(employeeId, SpcfUtils.add(employeesTaxTipsMap.get(employeeId), taxAmount));
        }

        public void subtractTotalWages(String employeeId, SpcfMoney totalWages) {

            employeesTotalWagesMap.put(employeeId, SpcfUtils.subtract(employeesTotalWagesMap.get(employeeId), totalWages));
        }

        public void subtractTaxableWages(String employeeId, SpcfMoney taxableWages) {

            employeesTaxableWagesMap.put(employeeId, SpcfUtils.subtract(employeesTaxableWagesMap.get(employeeId), taxableWages));
        }

        public void subtractTaxAmount(String employeeId, SpcfMoney taxAmount) {

            employeesTaxAmountMap.put(employeeId, SpcfUtils.subtract(employeesTaxAmountMap.get(employeeId), taxAmount));
        }

        public void subtractTaxTipsAmount(String employeeId, SpcfMoney taxAmount) {

            employeesTaxTipsMap.put(employeeId, SpcfUtils.subtract(employeesTaxTipsMap.get(employeeId), taxAmount));
        }

        public Iterable<String> getEmployeeIds() {

            return employeesMap.keySet();
        }

        public void setEmployeeShowTaxTips(Boolean pEmployeeShowTaxTips) {

            employeeShowTaxTips = pEmployeeShowTaxTips;
        }

        public SAPEmployeeTaxLedgerItem getEmployee(String employeeId) {

            double totalWages = SAPTranslator.getDoubleFromSpcfMoneyNullZero(employeesTotalWagesMap.get(employeeId));
            double taxableWages = SAPTranslator.getDoubleFromSpcfMoneyNullZero(employeesTaxableWagesMap.get(employeeId));
            double taxAmount = SAPTranslator.getDoubleFromSpcfMoneyNullZero(employeesTaxAmountMap.get(employeeId));
            double taxTips = SAPTranslator.getDoubleFromSpcfMoneyNullZero(employeesTaxTipsMap.get(employeeId));
            SAPEmployeeTaxLedgerItem sapEmployeeTaxLedgerItem = employeesMap.get(employeeId);
            sapEmployeeTaxLedgerItem.setTotalWages(totalWages);
            sapEmployeeTaxLedgerItem.setTaxableWages(taxableWages);
            sapEmployeeTaxLedgerItem.setTaxAmount(taxAmount);
            sapEmployeeTaxLedgerItem.setTaxTips(taxTips);
            sapEmployeeTaxLedgerItem.setShowTaxTips(employeeShowTaxTips);
            return sapEmployeeTaxLedgerItem;
        }
    }

    @SuppressWarnings({"RedundantIfStatement"})
    private boolean shouldCountLiabilityForItem(boolean isVoided, PayrollRun itemPayrollRun, PayrollRun selectedPayrollRun, CompanyAdjustmentSubmission itemVoid, CompanyAdjustmentSubmission selectedVoid) {

        if (!isVoided) {
            return true;
        } else {
            //if it's in the paycheck collection returned and it's not voided, will count it.  If the paycheck is voided, then multiple scenarios:
            if (!itemPayrollRun.equals(selectedPayrollRun)) {
                //not the payroll the user selected (was voided in an older payroll so should be netted out) -- do not count voided liability
                return false;
            } else {
                if (selectedVoid == null) {
                    //user selected this voided payroll before the void -- do count the liability
                    return true;
                } else if (!itemVoid.getSubmissionDate().after(selectedVoid.getSubmissionDate())) {
                    //payroll the user selected, but there were multiple voids and the user has selected this one or one after this one
                    return false;
                } else {
                    //payroll user selected, paycheck was voided after selected void
                    return true;
                }
            }
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateManualLedgerEntry)
    public ArrayList<SAPManualLedgerTaxLine> getManualLedgerLines(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String paymentTemplateCd, String specifiedLawId, Date checkDate) throws Throwable {

        ArrayList<SAPManualLedgerTaxLine> sapManualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            PaymentTemplate specifiedPaymentTemplate = StringUtils.isNotEmpty(paymentTemplateCd) ? PaymentTemplate.findPaymentTemplate(paymentTemplateCd) : null;

            SpcfCalendar checkDateCalendar = SAPTranslator.getSpcfCalendarFromDate(checkDate);
            SpcfCalendar yearStartCalendar = TaxPeriod.Q1_BEGIN(checkDateCalendar.getYear()); //Always get all the transactions from starting of the year to have correct YTD values

            if (specifiedPaymentTemplate != null && specifiedPaymentTemplate.getSupportStartDate() != null && specifiedPaymentTemplate.getSupportStartDate().after(yearStartCalendar)) {
                yearStartCalendar = specifiedPaymentTemplate.getSupportStartDate();
            }
            SpcfCalendar yearQuarterStartCalendar = TaxPeriod.getQuarterStart(checkDateCalendar); // Selected quarter start date

            // init transactions map and summary
            Map<String, SAPLawTransactions> sapLawTransactionsMap = new HashMap<String, SAPLawTransactions>();
            TaxLedger.initializeTransactionsMap(sapLawTransactionsMap, company.getCompanyAgencyCollection(), null, specifiedPaymentTemplate, specifiedLawId, yearQuarterStartCalendar);
            SAPLawTransactions summaryTransactions = TaxLedger.initializeSummary(sapLawTransactionsMap, null, specifiedPaymentTemplate);

            TaxLedger.findTaxTransactions(paymentTemplateCd, null, false, true, true, false,  true, true, yearStartCalendar, checkDateCalendar, company, sapLawTransactionsMap, summaryTransactions, yearQuarterStartCalendar);

            for (SAPLawTransactions lawTransactions : sapLawTransactionsMap.values()) {
                sapManualLedgerTaxLines.add(getManualLedgerTaxLine(company, lawTransactions));
            }

            Collections.sort(sapManualLedgerTaxLines, new Comparator<SAPManualLedgerTaxLine>() {
                public int compare(SAPManualLedgerTaxLine o1, SAPManualLedgerTaxLine o2) {

                    return o1.getLaw().getName().compareTo(o2.getLaw().getName());
                }
            });

            if (summaryTransactions != null) {
                sapManualLedgerTaxLines.add(getManualLedgerTaxLine(company, summaryTransactions));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting manual ledger lines", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapManualLedgerTaxLines;
    }

    private SAPManualLedgerTaxLine getManualLedgerTaxLine(Company pCompany, SAPLawTransactions lawTransactions) {

        SAPManualLedgerTaxLine sapManualLedgerTaxLine = new SAPManualLedgerTaxLine();

        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(pCompany, lawTransactions.getLaw().getLawId());
        // this field should really be named "associate with liability check"
        sapManualLedgerTaxLine.setCompanyLawExists(companyLaw != null || Law.NON_QBDT_LAWS.contains(lawTransactions.getLaw().getLawId()));

        sapManualLedgerTaxLine.setLaw(lawTransactions.getLaw());
        SAPTaxTransaction lastTransaction = lawTransactions.getTaxTransactions().size() == 0 ? null : lawTransactions.getTaxTransactions().get(lawTransactions.getTaxTransactions().size() - 1);
        SAPQTDYTDs originalQTDYTDs = new SAPQTDYTDs();
        if (lastTransaction != null) {
            originalQTDYTDs.setQtdLiability(lastTransaction.getQTDTaxes());
            originalQTDYTDs.setQtdWages(lastTransaction.getQTDWages());
            originalQTDYTDs.setYtdLiability(lastTransaction.getYTDTaxes());
            originalQTDYTDs.setYtdWages(lastTransaction.getYTDWages());
        }
        originalQTDYTDs.setTaxBalance(lawTransactions.getCurrentTaxesSum());

        sapManualLedgerTaxLine.setOriginalQTDYTD(originalQTDYTDs);
        return sapManualLedgerTaxLine;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateManualLedgerEntry)
    public void createManualLedgerEntry(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String entryType, ArrayList<SAPManualLedgerTaxLine> lines, Date checkDate, String memo, int recordingOption, Date datePaid, Boolean allowLimitOutsideOfBoundaries) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            if (entryType.equals("Liabilities/Wages") || entryType.equals("Reconciling Adjustment")) {
                ProcessResult<CompanyAdjustmentSubmission> pr;
                boolean isReconciling = entryType.equals("Reconciling Adjustment");
                CompanyAdjustmentSubmissionDTO dto = new CompanyAdjustmentSubmissionDTO();
                dto.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
                dto.setLiabilityAdjustmentDTOs(new ArrayList<LiabilityAdjustmentDTO>());
                dto.setIsVoid(false);
                dto.setMemo(memo);
                SpcfMoney total = SpcfMoney.ZERO;
                for (SAPManualLedgerTaxLine line : lines) {
                    checkLedgerAmountLimit(companyId, entryType, line, allowLimitOutsideOfBoundaries);
                    if (line.getAmount() != 0 || line.getWageAmount() != 0) {
                        total = new SpcfMoney(SpcfUtils.add(total, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(line.getAmount())));
                        LiabilityAdjustmentDTO laDTO = new LiabilityAdjustmentDTO();
                        laDTO.setAmount(SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(line.getAmount()));
                        laDTO.setEffectiveDate(new DateDTO(checkDate));
                        laDTO.setLawId(line.getLaw().getLawId());
                        laDTO.setReconcilingAdjustment(isReconciling);
                        laDTO.setTaxableWages(SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(line.getWageAmount()));
                        laDTO.setTotalWages(SpcfMoney.ZERO);
                        dto.getLiabilityAdjustmentDTOs().add(laDTO);
                    }
                }
                dto.setTotalAmount(total);
                LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
                liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
                liabilityAdjustmentOptionsDTO.setDebitCustomer(recordingOption == 0 && !isReconciling);
                liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(recordingOption == 0 && !isReconciling);
                liabilityAdjustmentOptionsDTO.setForceToRecordFTs(recordingOption == 0 && !isReconciling);

                pr = PayrollServices.payrollManager.addLiabilityAdjustments(SourceSystemCode.valueOf(sourceSystemCd), companyId, null, dto, new DateDTO(checkDate), liabilityAdjustmentOptionsDTO);

                if (pr.isSuccess()) {
                    // do not return manual ledger keying liability checks to QB
                    CompanyAdjustmentSubmission companyAdjustmentSubmission = pr.getResult();
                    if (companyAdjustmentSubmission != null) {
                        for (LiabilityCheck liabilityCheck : companyAdjustmentSubmission.getPayrollRun().getLiabilityCheckCollection()) {
                            if (liabilityCheck.getType() == LiabilityCheckType.EmployerDebit) {
                                liabilityCheck.getQbdtTransactionInfo().setToken(-2);
                                Application.save(liabilityCheck.getQbdtTransactionInfo());
                        }
                    }
                }
                    PayrollServices.commitUnitOfWork();
                } else {
                    aeFactory.throwGenericException("Error creating manual ledger entry", pr);
                    }
            } else {
                ProcessResult<PayrollRun> pr;
                CustomerTaxPaymentDTO dto = new CustomerTaxPaymentDTO();
                SpcfCalendar checkCalendar = SAPTranslator.getSpcfCalendarFromDate(checkDate);
                dto.setYear(checkCalendar.getYear());
                dto.setQuarter(TaxPeriod.getQuarterNumber(checkCalendar));
                dto.setPaymentDate(new DateDTO(datePaid));
                dto.setPaymentTemplateId(lines.get(0).getLaw().getPaymentTemplateCd());
                dto.setMemo(memo);
                dto.setApplyPayments(recordingOption == 3);
                dto.setPaymentAmounts(new HashMap<String, BigDecimal>());
                for (SAPManualLedgerTaxLine line : lines) {
                    if (line.getAmount() != 0) {
                        checkLedgerAmountLimit(companyId, entryType, line, allowLimitOutsideOfBoundaries);
                        dto.getPaymentAmounts().put(line.getLaw().getLawId(), new BigDecimal(line.getAmount()));
                    }
                }

                pr = PayrollServices.payrollManager.addCustomerTaxPayment(SourceSystemCode.valueOf(sourceSystemCd), companyId, dto);

                if (pr.isSuccess()) {
                    // do not return manual ledger keying liability checks to QB
                    PayrollRun payrollRun = pr.getResult();
                    if (payrollRun != null) {
                        for (LiabilityCheck liabilityCheck : payrollRun.getLiabilityCheckCollection()) {
                            if (liabilityCheck.getType() == LiabilityCheckType.EmployerDebit) {
                                liabilityCheck.getQbdtTransactionInfo().setToken(-2);
                                Application.save(liabilityCheck.getQbdtTransactionInfo());
                            }
                        }
                    }
                    PayrollServices.commitUnitOfWork();
                } else {
                    aeFactory.throwGenericException("Error creating manual ledger entry", pr);
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating manual ledger entry", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static void checkLedgerAmountLimit(String companyId, String entryType, SAPManualLedgerTaxLine line, Boolean allowLimitOutsideOfBoundaries) throws Exception {
        if (allowLimitOutsideOfBoundaries) return;
        boolean manualLedgerTaxLimitEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MANUAL_LEDGER_LIMIT_ENABLED, true);
        int manualLedgerTaxLimit = SystemParameter.findIntValue(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT, 10000);
        if (manualLedgerTaxLimitEnabled && line.getAmount()> manualLedgerTaxLimit){
            logger.error("Liability amount greater then limit="+ manualLedgerTaxLimit +" amount="+ line.getAmount() +" companyId="+ companyId +" entryType="+ entryType +" law="+ line.getLaw().getName());
            aeFactory.throwGenericException("The liability amount you've entered is higher than the permissible limit. " +
                    "Enter an amount less than $"+ manualLedgerTaxLimit +" to create the entry.");
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewGlobalEnrollments)
    public ArrayList<SAPEnrollmentDetail> getEFTPSEnrollmentRejections() throws Throwable {

        ArrayList<SAPEnrollmentDetail> returnList = new ArrayList<SAPEnrollmentDetail>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Expression<EftpsEnrollment> expr = new Query<EftpsEnrollment>()
                    .Where(EftpsEnrollment.StatusCd().equalTo(EftpsEnrollmentStatus.Rejected)
                            .And(EftpsEnrollment.Secondary().equalTo(false)))
                    .OrderBy(EftpsEnrollment.CreatedDate().Descending());
            DomainEntitySet<EftpsEnrollment> enrollments = PayrollServices.entityFinder.find(EftpsEnrollment.class, expr);
            for (EftpsEnrollment enrollment : enrollments) {
                EftpsEnrollmentDetail eftpsEnrollmentDetail = enrollment.findEnrollmentDetail();
                SAPEnrollmentDetail sapEnrollmentDetail = new SAPEnrollmentDetail();
                if (eftpsEnrollmentDetail != null) {
                    sapEnrollmentDetail.setCompanyName(eftpsEnrollmentDetail.getLegalName());
                    sapEnrollmentDetail.setEin(eftpsEnrollmentDetail.getFedTaxId());
                    if (eftpsEnrollmentDetail.getResponseDate() != null) {
                        sapEnrollmentDetail.setRejectionDate(SAPTranslator.getDateFromSpcfCalendar(eftpsEnrollmentDetail.getResponseDate()));
                    }
                    sapEnrollmentDetail.setStatus(eftpsEnrollmentDetail.getRejectReason());
                    sapEnrollmentDetail.setCompanyKey(new SAPCompanyKey(eftpsEnrollmentDetail.getEftpsEnrollment().getCompanyAgency().getCompany()));
                }
                returnList.add(sapEnrollmentDetail);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting rejected enrollments.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnList;
    }

    @FlexMethod
    public SAPEftpsEnrollmentHistory getEftpsEnrollmentsHistory(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        SAPEftpsEnrollmentHistory sapEftpsEnrollmentHistory = new SAPEftpsEnrollmentHistory();
        Company company;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            /*  Get company first   */
            company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            DomainEntitySet<EftpsEnrollment> enrollments = company.getAllEnrollments();
            if (enrollments.isEmpty()) {
                /*  No history if there are no enrollments  */
                sapEftpsEnrollmentHistory.setCanRe_enroll(false);
                return sapEftpsEnrollmentHistory;
            }
            sapEftpsEnrollmentHistory.setCanRe_enroll(company.getCurrentEnrollment().isAllowedTransition(EftpsEnrollmentStatus.PendingEnrollment));
            for (EftpsEnrollment eftpsEnrollment : enrollments) {
                SAPEftpsEnrollmentItem sapEnrollmentItem = new SAPEftpsEnrollmentItem();
                sapEnrollmentItem.setEnrollmentId(eftpsEnrollment.getId().toString());
                sapEnrollmentItem.setSecondaryEnrollment(eftpsEnrollment.getSecondary());
                EftpsEnrollmentDetail enrollmentDetail = eftpsEnrollment.findEnrollmentDetail();
                if (enrollmentDetail != null) {
                    /*  Get enrollment detail if it exists -- these were the details actually sent to agency*/
                    sapEnrollmentItem.setEin(enrollmentDetail.getFedTaxId());
                    sapEnrollmentItem.setLegalName(enrollmentDetail.getLegalName());
                    sapEnrollmentItem.setLegalZip(enrollmentDetail.getLegalZip());
                } else {
                    /*  Get those details from the company -- these are the details that _will_ be sent to the agency */
                    sapEnrollmentItem.setEin(company.getFedTaxId());
                    sapEnrollmentItem.setLegalName(company.getLegalName());
                    sapEnrollmentItem.setLegalZip(company.getLegalAddress().getZipCode());
                }

                DomainEntitySet<CompanyEventDetail> retList = CompanyEvent.findCompanyEventDetailForEventDetailValue(company, EventDetailTypeCode.NewStringValue, EventDetailTypeCode.UniqueIdentifier, sapEnrollmentItem.getEnrollmentId());

                for (CompanyEventDetail companyEventDetail : retList) {
                    SAPEnrollmentStatusChange sapEftpsEnrollmentStatusChange = new SAPEnrollmentStatusChange();
                    sapEftpsEnrollmentStatusChange.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(companyEventDetail.getCreatedDate()));
                    sapEftpsEnrollmentStatusChange.setModifiedBy(SAPTranslator.getUserNameFromUserID(companyEventDetail.getModifierId()));
                    sapEftpsEnrollmentStatusChange.setStatus(companyEventDetail.getValue());
                    sapEnrollmentItem.getStatusChanges().add(sapEftpsEnrollmentStatusChange);
                }
                sapEftpsEnrollmentHistory.getEnrollments().add(sapEnrollmentItem);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting enrollments history.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapEftpsEnrollmentHistory;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.ResolveEFTPSReject
    })
    public void initiateReEnrollment(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            EftpsEnrollment enrollment = company.getCurrentEnrollment();

            if (enrollment == null) {
                aeFactory.throwGenericException("Company has no current enrollment");
            } else {
                ProcessResult pr = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), pCompanyId, EftpsEnrollmentStatus.PendingEnrollment);
                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Error initiating re-enrollment", pr);
                }
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error initiating re-enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManualEFTPSEnrollments)
    public void createManualEFTPSEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String ein, String legalName, String zip) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));

            ProcessResult pr = PayrollServices.companyManager.createSecondaryEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), ein.replaceAll("-", ""), legalName, zip);
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error creating manual re-enrollment", pr);
            }
            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating manual re-enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static enum PaymentsSearchType {

        Pending,
        Executed,
        Rejected
    }

    private static enum PaymentsStatusType {

        NotOnHold,
        OnHold,
        OnEnrollmentHold,
        OnAgentHold,
        OnCompanyHold,
        OnBackDateHold,
        Rejected,
        Returned,
        Finalized,
        NotFinalized,
        NfNotOnHold,
    }

    @FlexMethod
    public ArrayList<SAPLawAmount> getLawAmounts(String mmtId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        Map<Law, SAPLawAmount> lawAmounts = new HashMap<Law, SAPLawAmount>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            MoneyMovementTransaction moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
            PaymentTemplate paymentTemplate = moneyMovementTransaction.getPaymentTemplate();
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(moneyMovementTransaction.getCompany(), paymentTemplate);
            for (CompanyLaw companyLaw : companyAgencyPaymentTemplate.getCompanyAgency().getCompanyLawCollection().find(CompanyLaw.AdditionalCompanyLaw().isNull())) {
                if (companyLaw.getLaw().getPaymentTemplate().equals(paymentTemplate)) {
                    lawAmounts.put(companyLaw.getLaw(), new SAPLawAmount(companyLaw.getLaw().getLawTypeCd(), companyLaw.getLaw().getLawId()));
                }
            }

            for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                    lawAmounts.get(financialTransaction.getLaw()).addAmount(financialTransaction.getFinancialTransactionAmount());
                } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                    lawAmounts.get(financialTransaction.getLaw()).addAmount(financialTransaction.getFinancialTransactionAmount().negate());
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Law Amounts for the Tax payment.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        ArrayList<SAPLawAmount> sapLawAmounts = new ArrayList<SAPLawAmount>(lawAmounts.values());
        Collections.sort(sapLawAmounts, new Comparator<SAPLawAmount>() {
            public int compare(SAPLawAmount o1, SAPLawAmount o2) {

                if (o1.getLaw().contains("SUI-ER") && !o2.getLaw().contains("SUI-ER")) {
                    return -1;
                } else if (o2.getLaw().contains("SUI-ER") && !o1.getLaw().contains("SUI-ER")) {
                    return 1;
                } else {
                    return o1.getLaw().compareTo(o2.getLaw());
                }
            }
        });
        return sapLawAmounts;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageSUITaxPayments)
    public void finalizePayment(String mmtId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
            ProcessResult pr = PayrollServices.paymentManager.finalizeSUIPayments(Arrays.asList(mmt),
                    mmt.getPaymentTemplate(),
                    TaxPeriod.getYearNumber(mmt.getPaymentPeriodEnd()),
                    TaxPeriod.getQuarterNumber(mmt.getPaymentPeriodEnd()));

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error finalizing payment", pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finalizing payment", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageSUITaxPayments)
    public void unFinalizePayment(String mmtId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
            ProcessResult pr = PayrollServices.paymentManager.unfinalizeSUIPayments(Arrays.asList(mmt),
                    mmt.getPaymentTemplate(),
                    TaxPeriod.getYearNumber(mmt.getPaymentPeriodEnd()),
                    TaxPeriod.getQuarterNumber(mmt.getPaymentPeriodEnd()));

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error un-finalizing payment", pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error un-finalizing payment", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageSUITaxPayments)
    public int finalizePayments(SAPPaymentSearch searchCriteria) throws Throwable {

        int paymentsFinalized = 0;
        int paymentsToFinalize;
        try {
            PayrollServices.beginUnitOfWork();

            HqlBuilder hql = getTaxPaymentsHQLBuilder(searchCriteria);
            hql.setReadOnly(false);

            paymentsToFinalize = hql.<MoneyMovementTransaction>list().size();

            ProcessResult pr = PayrollServices.paymentManager.finalizeSUIPayments(hql.<MoneyMovementTransaction>list(),
                    PaymentTemplate.findPaymentTemplate(searchCriteria.getPaymentTemplate()),
                    searchCriteria.getQuarter().getYear(),
                    searchCriteria.getQuarter().getQuarter());

            paymentsFinalized = paymentsToFinalize - hql.<MoneyMovementTransaction>list().size();

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error finalizing payments", pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finalizing payments", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentsFinalized;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageSUITaxPayments)
    public int updateInitiationDates(SAPPaymentSearch searchCriteria, Date newInitiationDate) throws Throwable {

        int paymentsChanged = 0;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            readConfigurationParameters();

            SpcfCalendar newDate = SAPTranslator.getSpcfCalendarFromDate(newInitiationDate).copy();

            if (searchCriteria.getPaymentTemplate() == null || searchCriteria.getPaymentTemplate().isEmpty()) {
                aeFactory.throwGenericException("Error updating initiation dates : Payment Type missing. Please select Agency and Payment type again.");
            }
            HqlBuilder hql = getTaxPaymentsHQLBuilder(searchCriteria);
            hql.setReadOnly(true);

            Map<String, List<SpcfUniqueId>> mmtIdListByCompany = new HashMap<>();
            List<MoneyMovementTransaction> mmtList = hql.<MoneyMovementTransaction>list();

            for(MoneyMovementTransaction mmt: mmtList) {
                String sourceCompanyId = mmt.getCompany().getSourceCompanyId();
                if(!mmtIdListByCompany.containsKey(sourceCompanyId)){
                    mmtIdListByCompany.put(sourceCompanyId, new ArrayList<>());
                }
                mmtIdListByCompany.get(sourceCompanyId).add(mmt.getId());
            }

            multithreadedUpdateInitiationDate(mmtIdListByCompany, newDate);
            paymentsChanged = mmtList.size();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating initiation dates", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentsChanged;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageSUITaxPayments)
    public int updateGroupPaymentMethods(SAPPaymentSearch searchCriteria, String paymentMethod) throws Throwable {

        int paymentsChanged = 0;
        try {

            PayrollServices.beginUnitOfWork();

            HqlBuilder hql = getTaxPaymentsHQLBuilder(searchCriteria);
            hql.setReadOnly(false);

            ArrayList<ProcessResult> processResultList = new ArrayList<ProcessResult>();
            for (MoneyMovementTransaction mmt : hql.<MoneyMovementTransaction>list()) {

                PaymentMethod newPaymentMethod = PaymentMethod.valueOf(paymentMethod);

                if (mmt.getMoneyMovementPaymentMethod() != newPaymentMethod
                        && CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(mmt.getCompany(), mmt.getPaymentTemplate()).getCompanyPaymentTemplatePaymentMethod(newPaymentMethod).getEnabledForPayment(mmt)) {
                    processResultList.add(PayrollServices.paymentManager.changePaymentMethod(mmt.getCompany().getSourceSystemCd(), mmt.getCompany().getSourceCompanyId(), mmt.getId(), newPaymentMethod));
                    paymentsChanged++;
                }
            }
            if (aeFactory.errorsOccurred(processResultList)) {
                aeFactory.throwGenericException("Error updating payment methods", processResultList);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating payment methods", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentsChanged;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageSUITaxPayments)
    public void editPaymentAmount(String mmtId, ArrayList<String> splitFTIDs, ArrayList<SAPLawAmount> lawAmounts, String memo, boolean immediateDebitOrCredit, boolean allowLimitOutsideOfBoundaries, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        try {
            boolean updatePaymentsAmountLimitEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_UPDATE_PAYMENTS_AMOUNT_LIMIT_ENABLED, true);
            PayrollServices.beginUnitOfWork();
            int defaultUpdatePaymentsAmountLimit = SystemParameter.findIntValue(SystemParameter.Code.DEFAULT_UPDATE_PAYMENTS_AMOUNT, 500);
            MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
            if (splitFTIDs.size() > 0) {
                List<FinancialTransaction> ftList = new ArrayList<FinancialTransaction>(splitFTIDs.size());
                for (String splitFTID : splitFTIDs) {
                    FinancialTransaction ft = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(splitFTID));
                    ftList.add(ft);
                }

                ProcessResult splitResult = PayrollServices.paymentManager.splitSUIPayments(ftList, StringUtils.defaultIfEmpty(memo, null));
                if (!splitResult.isSuccess()) {
                    aeFactory.throwGenericException("Error splitting payments", splitResult);
                }
            }

            Map<Law, SpcfMoney> lawMap = new HashMap<Law, SpcfMoney>();
            for (SAPLawAmount lawAmount : lawAmounts) {
                if(!allowLimitOutsideOfBoundaries && updatePaymentsAmountLimitEnabled && lawAmount.getAmount() > defaultUpdatePaymentsAmountLimit) {
                    aeFactory.throwGenericException("The payment amount you have entered is higher than the permissible limit. " +
                            "Enter an amount less than $"+defaultUpdatePaymentsAmountLimit+" to create the entry.");
                }
                lawMap.put(Application.findById(Law.class, lawAmount.getLawId()), SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(lawAmount.getAmount()));
            }
            ProcessResult adjustResult = PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawMap, immediateDebitOrCredit, memo);
            if (!adjustResult.isSuccess()) {
                aeFactory.throwGenericException("Error editing payment amount", adjustResult);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adjusting payment amount", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
    public static final String SORT_PSID_STATUS_INIT_DATE = "SortPSIDStatusInitDate";

    @FlexMethod
    @Operation(operationIds = OperationId.ViewGlobalTaxPayments)
    public SAPSearchResults<SAPPayment> findTaxPayments(SAPPaymentSearch searchCriteria, Integer firstResult, Integer maxResults, String sortColumn, Boolean sortDescending) throws Throwable {

        SAPSearchResults<SAPPayment> sapSearchResults = new SAPSearchResults<SAPPayment>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            //load parameters
            HqlBuilder hql = getTaxPaymentsHQLBuilder(searchCriteria);
            hql.setReadOnly(true);

            Object[] countAndTotal = hql.<Object[]>select("select count(Id), sum(mmt.MoneyMovementTransactionAmount)").get(0);
            sapSearchResults.setTotalRecords((Long) countAndTotal[0]);
            sapSearchResults.setTotalAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero((SpcfMoney) countAndTotal[1]));

            hql.append("order by ");
            if (sortColumn == null || sortColumn.equals("settlementDate") || sortColumn.equals("initiationDate")
                    || sortColumn.equals("agencyId") || sortColumn.equals("ein")) {
                hql.append("mmt.InitiationDate");
            } else if (sortColumn.equals("dueDate")) {
                hql.append("mmt.DueDate");
            } else if (sortColumn.equals("amount")) {
                hql.append("mmt.MoneyMovementTransactionAmount");
            } else if (sortColumn.equals("companyName")) {
                hql.append("mmt.Company.LegalName");
            } else if (sortColumn.equals("paymentType")) {
                hql.append("mmt.PaymentTemplate");
            } else if (sortColumn.equals("psId")) {
                hql.append("mmt.Company.SourceCompanyId");
            } else if (sortColumn.equals(SORT_PSID_STATUS_INIT_DATE)) {
                // magic sort column just for WebService
                hql.append("mmt.Company.SourceCompanyId, mmt.TaxPaymentStatus, mmt.InitiationDate");
            }

            if (sortDescending != null && sortDescending) {
                hql.append("desc");
            }
            //must break all ties so that pages are consistent
            hql.append(", mmt.Id");

            //payment template required only if pending search
            PaymentMethod highestPriorityPaymentMethod = null;
            PaymentsSearchType searchType = PaymentsSearchType.valueOf(searchCriteria.getSearchType());
            if (searchType == PaymentsSearchType.Pending && StringUtils.isNotEmpty(searchCriteria.getPaymentTemplate())) {
                PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, searchCriteria.getPaymentTemplate());
                PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = paymentTemplate.getPaymentTemplatePaymentMethods().find(PaymentTemplatePaymentMethod.PaymentMethodOrder().equalTo(1)).getFirst();
                if (paymentTemplatePaymentMethod != null) {
                    highestPriorityPaymentMethod = paymentTemplatePaymentMethod.getPaymentMethod();
                }
            }

            ArrayList<SAPPayment> payments = new ArrayList<SAPPayment>();
            for (MoneyMovementTransaction moneyMovementTransaction : hql.<MoneyMovementTransaction>list(firstResult, maxResults)) {
                try {
                    pspRequestContextManager.setRequestContextCompany(moneyMovementTransaction.getCompany());
                    String mmtStatus = getSimpleStatus(moneyMovementTransaction);
                    List<SAPPaymentMethod> paymentMethods = null;
                    if (highestPriorityPaymentMethod != null && moneyMovementTransaction.getMoneyMovementPaymentMethod() != highestPriorityPaymentMethod) {
                        paymentMethods = getAllPaymentMethods(moneyMovementTransaction.getCompany().getSourceSystemCd().name(),
                                moneyMovementTransaction.getCompany().getSourceCompanyId(),
                                searchCriteria.getPaymentTemplate(), null);
                    }
                    payments.add(TaxTranslator.getPayment(moneyMovementTransaction, mmtStatus, paymentMethods));
                }finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }
            sapSearchResults.setReturnsList(payments);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Pending Tax payments.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapSearchResults;
    }

    public HqlBuilder getTaxPaymentsHQLBuilder(SAPPaymentSearch searchCriteria) throws Exception {

        PaymentsSearchType searchType = PaymentsSearchType.valueOf(searchCriteria.getSearchType());
        PaymentsStatusType statusType = StringUtils.isEmpty(searchCriteria.getStatus()) ? null : PaymentsStatusType.valueOf(WordUtils.capitalizeFully(searchCriteria.getStatus()).replaceAll("[ -]", ""));

        Agency agency = null;
        if (StringUtils.isNotEmpty(searchCriteria.getAgencyAbbrev())) {
            agency = Application.find(Agency.class, Agency.AgencyAbbrev().equalTo(searchCriteria.getAgencyAbbrev())).getFirst();
        }

        Collection<Company> companies = null;
        if (StringUtils.isNotEmpty(searchCriteria.getCompanyIds())) {
            companies = Application.find(Company.class, Company.SourceCompanyId().in(searchCriteria.getCompanyIds().split("\\s+")));
        }

        PaymentTemplate paymentTemplate = null;
        if (StringUtils.isNotEmpty(searchCriteria.getPaymentTemplate())) {
            paymentTemplate = Application.findById(PaymentTemplate.class, searchCriteria.getPaymentTemplate());
        }

        @SuppressWarnings({"JpaQlInspection"})
        HqlBuilder hql = new HqlBuilder(true, "from com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction mmt where mmt.MoneyMovementTransactionAmount != 0");
        switch (searchType) {
            case Pending:
                hql.append("and mmt.Status in (:paymentStatus)");
                hql.setParameterList("paymentStatus", PaymentStatus.Created, PaymentStatus.OnHold);

                hql.append("and mmt.TaxPaymentStatus in (:taxPaymentStatuses)");
                if (statusType == null) {
                    hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold, TaxPaymentStatus.ATFFinalized);
                } else if (statusType == PaymentsStatusType.NotFinalized) {
                    hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold);
                } else if (statusType == PaymentsStatusType.Finalized) {
                    hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.ATFFinalized);
                } else if (statusType == PaymentsStatusType.NfNotOnHold) {
                    hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.ReadyToSend);
                } else if (statusType == PaymentsStatusType.NotOnHold) {
                    hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.ATFFinalized, TaxPaymentStatus.ReadyToSend);
                } else {
                    hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.OnHold);
                    if (statusType != PaymentsStatusType.OnHold) {  //i.e. more specific than this
                        hql.append("and exists (from mmt.TaxPaymentOnHoldReasonSet as onHoldReason where onHoldReason.ExpirationDate is null and onHoldReason.OnHoldReasonCd = :onHoldReason) ");
                        switch (statusType) {
                            case OnAgentHold:
                                hql.setParameter("onHoldReason", PaymentOnHoldReason.Agent);
                                break;
                            case OnEnrollmentHold:
                                hql.setParameter("onHoldReason", PaymentOnHoldReason.Enrollment);
                                break;
                            case OnCompanyHold:
                                hql.setParameter("onHoldReason", PaymentOnHoldReason.Company);
                                break;
                            case OnBackDateHold:
                                hql.setParameter("onHoldReason", PaymentOnHoldReason.BackDate);
                                break;
                        }
                    }
                }
                break;
            case Executed:
                hql.append("and mmt.Status = :paymentStatus");
                hql.setParameter("paymentStatus", PaymentStatus.Executed);
                hql.append("and mmt.TaxPaymentStatus in (:taxPaymentStatuses)");
                hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency);
                break;
            case Rejected:
                hql.append("and mmt.Status = :paymentStatus");
                hql.setParameter("paymentStatus", PaymentStatus.Executed);
                if (statusType == null) {
                    hql.append("and mmt.TaxPaymentStatus in (:taxPaymentStatuses)");
                    hql.setParameterList("taxPaymentStatuses", TaxPaymentStatus.RejectedByAgency, TaxPaymentStatus.ReturnedTaxNotPaid);
                } else {
                    hql.append("and mmt.TaxPaymentStatus = :taxPaymentStatus");
                    if (statusType == PaymentsStatusType.Rejected) {
                        hql.setParameter("taxPaymentStatus", TaxPaymentStatus.RejectedByAgency);
                    } else if (statusType == PaymentsStatusType.Returned) {
                        hql.setParameter("taxPaymentStatus", TaxPaymentStatus.ReturnedTaxNotPaid);
                    }
                }
        }

        if (paymentTemplate != null) {
            hql.append("and mmt.PaymentTemplate = :paymentTemplate");
            hql.setParameter("paymentTemplate", paymentTemplate);
        } else if (agency != null) {
            hql.append("and mmt.PaymentTemplate.Agency = :agency");
            hql.setParameter("agency", agency);
        }

        //Users search by settlement date but we actually search by initiation date
        int initiationDateOffset = -1;
        if (searchCriteria.getSettlementStartDate() != null) {
            hql.append("and mmt.InitiationDate >= :fromInitDate");
            SpcfCalendar fromDate = SAPTranslator.getSpcfCalendarFromDate(searchCriteria.getSettlementStartDate()).copy();
            CalendarUtils.addBusinessDays(fromDate, initiationDateOffset);
            logger.info("Settlement Date - Start Date " +fromDate);
            hql.setParameter("fromInitDate", fromDate);
        }
        if (searchCriteria.getSettlementEndDate() != null) {
            hql.append("and mmt.InitiationDate <= :toInitDate");
            SpcfCalendar toDate = SAPTranslator.getSpcfCalendarFromDate(searchCriteria.getSettlementEndDate()).copy();
            CalendarUtils.addBusinessDays(toDate, initiationDateOffset);
            logger.info("Settlement Date - End Date: " +toDate);
            hql.setParameter("toInitDate", toDate);
        }
        if (searchCriteria.getSettlementStartDate() == null && searchCriteria.getSettlementEndDate() == null && searchCriteria.getQuarter() != null) {
            //if nothing has been set explicitly, use the quarter to calculate a reasonably narrow initiation date
            //note that the upper bound cannot be calculated this way as backdates have no limit
            hql.append("and mmt.InitiationDate >= :fromInitDate");
            hql.setParameter("fromInitDate", searchCriteria.getQuarter().getFirstDayOfQuarterMinus45Days());
        }

        if (searchCriteria.getInitiationStartDate() != null) {
            hql.append("and mmt.InitiationDate >= :fromInitDate");
            SpcfCalendar fromDate = SAPTranslator.getSpcfCalendarFromDate(searchCriteria.getInitiationStartDate()).copy();
            logger.info("Initiation Start Date:" + fromDate);
            hql.setParameter("fromInitDate", fromDate);
        }
        if (searchCriteria.getInitiationEndDate() != null) {
            hql.append("and mmt.InitiationDate <= :toInitDate");
            SpcfCalendar toDate = SAPTranslator.getSpcfCalendarFromDate(searchCriteria.getInitiationEndDate()).copy();
            logger.info("Initiation End Date:" + toDate);
            hql.setParameter("toInitDate", toDate);
        }
        if (searchCriteria.getInitiationStartDate() == null && searchCriteria.getInitiationEndDate() == null && searchCriteria.getQuarter() != null) {
            //if nothing has been set explicitly, use the quarter to calculate a reasonably narrow initiation date
            //note that the upper bound cannot be calculated this way as backdates have no limit
            hql.append("and mmt.InitiationDate >= :fromInitDate");
            hql.setParameter("fromInitDate", searchCriteria.getQuarter().getFirstDayOfQuarterMinus45Days());
        }

        if (searchCriteria.getQuarter() != null) {
            hql.append("and mmt.PaymentPeriodEnd between :firstDayOfQuarter and :lastDayOfQuarter");
            hql.setParameter("firstDayOfQuarter", searchCriteria.getQuarter().getFirstDayOfQuarter());
            hql.setParameter("lastDayOfQuarter", searchCriteria.getQuarter().getLastDayOfQuarter());
        }

        if (StringUtils.equals(searchCriteria.getPaymentMethod(), "None")) {
            hql.append(" and mmt.MoneyMovementPaymentMethod is null");
        } else if (StringUtils.isNotEmpty(searchCriteria.getPaymentMethod())) {
            hql.append(" and mmt.MoneyMovementPaymentMethod = :paymentMethod");
            hql.setParameter("paymentMethod", PaymentMethod.valueOf(searchCriteria.getPaymentMethod()));
        }

        if (companies != null) {
            if (companies.isEmpty()) {
                hql.append("and 0=1");
            } else if (companies.size() > 1000){
                logger.info("Number of companies "+companies.size());
                aeFactory.throwGenericException("PSID Exceeds The Limit - 1000");
            }else {
                logger.info("Number of companies "+companies.size());
                hql.append("and mmt.Company in (:companies)");
                hql.setParameter("companies", companies);
            }
        }

        if (searchCriteria.getOverduePaymentsOnly() && agency != null && paymentTemplate != null) { //search only supported if specified
            hql.append("and " +
                    Application.getTruncFunctionString("mmt.DueDate") +
                    " <= :maximumPendingDueDate");
            //todo verify for SUI
            PaymentMethod latestPaymentMethod;
            if (agency.isIRS()) {
                latestPaymentMethod = PaymentMethod.EFTPS;
            } else {
                latestPaymentMethod = PaymentMethod.CheckPayment;
            }
            SpcfCalendar earliestSettlementDate = getEarliestSettlementDate(latestPaymentMethod, paymentTemplate);
            CalendarUtils.clearTime(earliestSettlementDate);
            if(Application.isOracleDB()) {
                hql.setParameter("maximumPendingDueDate", earliestSettlementDate);
            } else {
                hql.setParameter("maximumPendingDueDate", SpcfUtils.convertSpcfCalendarToDate(earliestSettlementDate));
            }
        }
        return hql;
    }

    public static String getSimpleStatus(MoneyMovementTransaction moneyMovementTransaction) {

        String statusDetails = null;
        if (moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.RejectedByAgency)) {
            statusDetails = "Rejected - ";
            if (moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.EFTPS || moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.EFTPSDirectDebit) {
                EftpsPaymentDetail eftpsPaymentDetail = EftpsPaymentDetail.findPaymentDetailByMoneyMovementTransaction(moneyMovementTransaction);
                statusDetails += eftpsPaymentDetail.getReason();
            } else if (moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.EDI) {
                EdiPaymentDetail ediPaymentDetail = EdiPaymentDetail.findPaymentDetailByMoneyMovementTransaction(moneyMovementTransaction);
                if (ediPaymentDetail != null) {
                    statusDetails += ediPaymentDetail.getErrorMessage();
                }
            }
        } else if (moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.ReturnedTaxNotPaid)) {
            statusDetails = "Returned ";
            if (moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.EFTPS || moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.EFTPSDirectDebit) {
                EftpsPaymentDetail eftpsPaymentDetail = EftpsPaymentDetail.findPaymentDetailByMoneyMovementTransaction(moneyMovementTransaction);
                statusDetails += "(" + eftpsPaymentDetail.getReturnCd().toString() + ")";
            }
        }
        if (statusDetails != null) {
            if (moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                DomainEntitySet<TransactionReturn> transactionReturns = Application.find(TransactionReturn.class, TransactionReturn.MoneyMovementTransaction().equalTo(moneyMovementTransaction));
                if (transactionReturns.size() > 0) {
                    statusDetails += "(" + transactionReturns.get(0).getBankReturnCd() + ")";
                }
            } else if (moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.CheckPayment) {
                DomainEntitySet<VoidedCheck> voidedChecks = Application.find(VoidedCheck.class, VoidedCheck.MoneyMovementTransaction().equalTo(moneyMovementTransaction));
                if (voidedChecks.size() > 0) {
                    statusDetails += voidedChecks.get(0).getReason();
                }
            } else if (moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.SuperCheck) {
                AgencyCheckBatch agencyCheckBatch = moneyMovementTransaction.getAgencyCheckBatch();
                if (agencyCheckBatch != null && agencyCheckBatch.getVoidedCheck() != null) {
                    statusDetails += agencyCheckBatch.getVoidedCheck().getReason();
                }
            }
        } else {
            if (moneyMovementTransaction.getTaxPaymentStatus() == TaxPaymentStatus.ATFFinalized) {
                statusDetails = "Finalized";
            } else if (moneyMovementTransaction.isPendingTaxPayment()) {
                if (moneyMovementTransaction.getOriginalTransaction() == null) {
                    statusDetails = "Pending";
                } else {
                    statusDetails = "Pending Re-initiation";
                }
            } else {
                statusDetails = moneyMovementTransaction.getTaxPaymentStatus().toString();
            }
        }
        return statusDetails;
    }

    @FlexMethod
    public SAPSearchResults<SAPRAFEnrollmentDetail> getRAFEnrollmentsByStatusAndCompany(SAPRAFEnrollmentSearch search, boolean pIncludePayrollStatus, int pFirstResult, int pMaxResults) throws Throwable {

        ArrayList<SAPRAFEnrollmentDetail> returnList = new ArrayList<SAPRAFEnrollmentDetail>();
        SAPSearchResults<SAPRAFEnrollmentDetail> resultsToReturn = new SAPSearchResults<SAPRAFEnrollmentDetail>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<RAFEnrollment> enrollments = RAFEnrollment.getRAFEnrollmentsByStatus(RAFEnrollmentStatus.valueOf(search.getStatus()),
                    search.getPSIDEINList(),
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(search.getCreationDateStart()),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(search.getCreationDateEnd()),
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(search.getLastUpdateDateStart()),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(search.getLastUpdateDateEnd()),
                    pFirstResult,
                    pMaxResults);

            if (pMaxResults > 0) {
                resultsToReturn.setTotalRecords(RAFEnrollment.getRAFEnrollmentsByStatusCount(RAFEnrollmentStatus.valueOf(search.getStatus()),
                        search.getPSIDEINList(),
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(search.getCreationDateStart()),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(search.getCreationDateEnd()),
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(search.getLastUpdateDateStart()),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(search.getLastUpdateDateEnd())));
            } else {
                resultsToReturn.setTotalRecords(enrollments.size());
            }

            for (RAFEnrollment enrollment : enrollments) {
                boolean companyHasPayrolls = false;
                if (pIncludePayrollStatus) {
                    companyHasPayrolls = Application.executeScalarAggQuery(PayrollRun.class, new Query<PayrollRun>()
                            .Select(PayrollRun.Id().Count())
                            .Where(PayrollRun.Company().equalTo(enrollment.getCompanyAgency().getCompany()))) > 0;
                }
                returnList.add(TaxTranslator.getSAPRAFEnrollmentDetailFromDomainEntity(enrollment, enrollment.getRAFEnrollmentDetail(), companyHasPayrolls));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting rejected enrollments.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        resultsToReturn.setReturnsList(returnList);
        return resultsToReturn;
    }

    @FlexMethod
    public SAPRAFEnrollmentHistory getRAFEnrollmentsHistory(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID)String pCompanyId) throws Throwable {

        SAPRAFEnrollmentHistory saprafEnrollmentHistory = new SAPRAFEnrollmentHistory();
        Company company;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            /*  Get company first   */
            company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            DomainEntitySet<RAFEnrollment> enrollments = company.getAllRAFEnrollments();
            if (enrollments.isEmpty()) {
                /*  No history if there are no enrollments  */
                saprafEnrollmentHistory.setCanRe_enroll(false);
                return saprafEnrollmentHistory;
            }
            saprafEnrollmentHistory.setCanRe_enroll(company.isCompanyOnService(ServiceCode.Tax) && company.getCurrentRAFEnrollment().getStatus() == RAFEnrollmentStatus.Rejected);
            for (RAFEnrollment rafEnrollment : enrollments) {
                SAPRAFEnrollmentHistoryItem sapRafEnrollmentHistoryItem = new SAPRAFEnrollmentHistoryItem();
                sapRafEnrollmentHistoryItem.setEnrollmentId(rafEnrollment.getId().toString());
                sapRafEnrollmentHistoryItem.setCanDelete(rafEnrollment.getStatus() == RAFEnrollmentStatus.Enrolled && (company.getService(ServiceCode.Tax).getStatusCd().equals(ServiceSubStatusCode.Cancelled) || company.getService(ServiceCode.Tax).getStatusCd().equals(ServiceSubStatusCode.Terminated)));
                sapRafEnrollmentHistoryItem.setRejectedReason(rafEnrollment.getStatusReason());
                RAFEnrollmentDetail enrollmentDetail = rafEnrollment.getRAFEnrollmentDetail();
                if (enrollmentDetail != null) {
                    /*  Get enrollment detail if it exists -- these were the details actually sent to agency*/
                    sapRafEnrollmentHistoryItem.setEin(enrollmentDetail.getFedTaxid());
                    sapRafEnrollmentHistoryItem.setLegalName(enrollmentDetail.getLegalName());
                    sapRafEnrollmentHistoryItem.setLegalZip(enrollmentDetail.getLegalZipCode());
                    sapRafEnrollmentHistoryItem.setFirstFilingQuarter(enrollmentDetail.getF941TaxPeriod());
                } else {
                    /*  Get those details from the company -- these are the details that _will_ be sent to the agency */
                    sapRafEnrollmentHistoryItem.setEin(company.getFedTaxId());
                    sapRafEnrollmentHistoryItem.setLegalName(company.getLegalName());
                    sapRafEnrollmentHistoryItem.setLegalZip(company.getLegalAddress().getZipCode());
                }
                /*  Now get a history of status changes from the company event log, the following query has been implemented in HQL */

                DomainEntitySet<CompanyEventDetail> retList = CompanyEvent.findCompanyEventDetailForEventDetailValue(company, EventDetailTypeCode.NewStringValue, EventDetailTypeCode.UniqueIdentifier, sapRafEnrollmentHistoryItem.getEnrollmentId());

                for (CompanyEventDetail companyEventDetail : retList) {
                    SAPEnrollmentStatusChange sapRafEnrollmentStatusChange = new SAPEnrollmentStatusChange();
                    sapRafEnrollmentStatusChange.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(companyEventDetail.getCreatedDate()));
                    sapRafEnrollmentStatusChange.setModifiedBy(SAPTranslator.getUserNameFromUserID(companyEventDetail.getModifierId()));
                    sapRafEnrollmentStatusChange.setStatus(companyEventDetail.getValue());
                    sapRafEnrollmentHistoryItem.getStatusChanges().add(sapRafEnrollmentStatusChange);
                }
                saprafEnrollmentHistory.getEnrollments().add(sapRafEnrollmentHistoryItem);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting enrollments history.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return saprafEnrollmentHistory;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void updateRAFEnrollmentStatus(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pNewStatus) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            RAFEnrollment enrollment = company.getCurrentRAFEnrollment();

            if (enrollment == null) {
                aeFactory.throwGenericException("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + "  has no current RAF enrollment");
            } else {
                ProcessResult pr = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                        company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                        RAFEnrollmentStatus.valueOf(pNewStatus));
                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Error updating enrollment for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
                }
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void rejectRAFEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pReason) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            RAFEnrollment enrollment = company.getCurrentRAFEnrollment();

            if (enrollment == null) {
                aeFactory.throwGenericException("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + "  has no current RAF enrollment");
            } else {
                ProcessResult pr = PayrollServices.companyManager.rejectRAFEnrollment(enrollment, pReason);
                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Error rejecting RAF enrollment for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
                }
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error rejecting RAF enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateRAFFile)
    public void initiateRAFTapeCreation(String pActionCode) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult pr = PayrollServices.batchJobManager.initiateRAFTapeCreation(RAFActionCode.valueOf(pActionCode));
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error initiating RAF Tape Creation", pr);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error writing RAF Tape", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateRAFFile)
    public void initiateACHFileCreation(String pACHEnrollmentFileType, SAPQuarter pSAPQuarter) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            ACHEnrollmentFileType type = ACHEnrollmentFileType.valueOf(pACHEnrollmentFileType);
            switch (type) {
                case Add:
                    AdministrationAdapter.scheduleJob(BatchJobType.ACHEnrollmentBatchJob.name(), null, pSAPQuarter.getFirstDayOfQuarter().format(BatchUtils.DATE_FORMAT));
                    break;
                case Delete:
                    AdministrationAdapter.scheduleJob(BatchJobType.ACHDeEnrollmentBatchJob.name(), null, pSAPQuarter.getLastDayOfQuarter().format(BatchUtils.DATE_FORMAT));
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating ACHEnrollment file: " + pACHEnrollmentFileType, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPEnrollmentFile> findEnrollmentFiles(String actionCode) throws Throwable {

        ArrayList<SAPEnrollmentFile> returnList = new ArrayList<SAPEnrollmentFile>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            DomainEntitySet<RAFEnrollmentFile> rafEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByActionCode(RAFActionCode.valueOf(actionCode));
            for (RAFEnrollmentFile rafEnrollmentFile : rafEnrollmentFiles) {
                if (StringUtils.isNotEmpty(rafEnrollmentFile.getFileName())) {
                    returnList.add(TaxTranslator.getSAPEnrollmentFile(rafEnrollmentFile.getCreatedDate(), rafEnrollmentFile.getId(), "RAF"));
                }
            }

            DomainEntitySet<ACHEnrollmentFile> achEnrollmentFiles = ACHEnrollmentFile.getACHFilesByActionCode(ACHEnrollmentFileType.valueOf(actionCode));
            for (ACHEnrollmentFile achEnrollmentFile : achEnrollmentFiles) {
                if (StringUtils.isNotEmpty(achEnrollmentFile.getFileName())) {
                    returnList.add(TaxTranslator.getSAPEnrollmentFile(achEnrollmentFile.getCreatedDate(), achEnrollmentFile.getId(), "ACH"));
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting RAF enrollment by Action Code", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnList;
    }

    @FlexMethod
    public ArrayList<SAPQuarter> getACHEnrollmentQuarters(String actionCode) throws Throwable {

        ArrayList<SAPQuarter> returnList = new ArrayList<SAPQuarter>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            ACHEnrollmentStatus enrollmentStatus = ACHEnrollmentStatus.PendingEnrollment;
            switch (ACHEnrollmentFileType.valueOf(actionCode)) {
                case Add:
                    enrollmentStatus = ACHEnrollmentStatus.PendingEnrollment;
                    break;
                case Delete:
                    enrollmentStatus = ACHEnrollmentStatus.PendingDelete;
            }
            Expression<ACHEnrollment> query = new Query<ACHEnrollment>()
                    .Select(ACHEnrollment.EffectiveDate().Distinct())
                    .Where(ACHEnrollment.Status().equalTo(enrollmentStatus));
            List<SpcfCalendar> result = Application.executeQuery(ACHEnrollment.class, query);
            for (SpcfCalendar effectiveDate : result) {
                returnList.add(new SAPQuarter(effectiveDate));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting enrollment quarters by Action Code", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnList;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.ManageRAFEnrollment})
    public void reInitiateRAFEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            RAFEnrollment enrollment = company.getCurrentRAFEnrollment();

            if (enrollment == null) {
                aeFactory.throwGenericException("Company has no current RAF enrollment");
            } else if (enrollment.getStatus() != RAFEnrollmentStatus.Rejected) {
                aeFactory.throwGenericException("Company is not in a rejected state to be re-enrolled");
            } else {
                ProcessResult pr = PayrollServices.companyManager.reInitiateRAFEnrollment(enrollment);
                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Error initiating RAF re-enrollment", pr);
                }
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error initiating RAF re-enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void deleteRAFEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pEnrollmentID) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            RAFEnrollment enrollment = company.getCurrentRAFEnrollment();

            if (enrollment == null) {
                aeFactory.throwGenericException("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + "  has no current RAF enrollment");
            } else {
                RAFEnrollment rafEnrollment = Application.findById(RAFEnrollment.class, SpcfUniqueId.createInstance(pEnrollmentID));
                if (rafEnrollment != null) {
                    ProcessResult pr = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                            company.getSourceSystemCd(), company.getSourceCompanyId(), rafEnrollment,
                            RAFEnrollmentStatus.PendingDeleteTape);
                    if (!pr.isSuccess()) {
                        aeFactory.throwGenericException("Error updating enrollment for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
                    }
                    PayrollServices.commitUnitOfWork();
                } else {
                    aeFactory.throwGenericException("Error deleting enrollment: No such RAF enrollment");
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateRAFFile)
    public void reInitiateRAFTapeCreation(String pFileId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            RAFEnrollmentFile rafEnrollmentFile = Application.findById(RAFEnrollmentFile.class, SpcfUniqueId.createInstance(pFileId));
            ProcessResult pr = PayrollServices.batchJobManager.initiateRAFTapeRecreation(rafEnrollmentFile);
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error initiating RAF Tape recreation", pr);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error initiating RAF Tape recreation", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void cancelDeleteRAFEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pEnrollmentId) throws Throwable {

        try {
            SAPRAFEnrollmentHistory saprafEnrollmentHistory = this.getRAFEnrollmentsHistory(pSourceSystemCode, pCompanyId);
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            RAFEnrollment rafEnrollment = Application.findById(RAFEnrollment.class, SpcfUniqueId.createInstance(pEnrollmentId));
            if (saprafEnrollmentHistory == null || rafEnrollment == null) {
                aeFactory.throwGenericException("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + "  has no RAF enrollments");
            }
            RAFEnrollmentStatus lastEnrollmentStatus = null;
            for (int i = 0; i < saprafEnrollmentHistory.getEnrollments().size(); i++) {
                SAPRAFEnrollmentHistoryItem enrollmentHistoryItem = saprafEnrollmentHistory.getEnrollments().get(i);
                if (enrollmentHistoryItem.getEnrollmentId().equals(pEnrollmentId)) {
                    int size = enrollmentHistoryItem.getStatusChanges().size();
                    lastEnrollmentStatus = RAFEnrollmentStatus.valueOf(enrollmentHistoryItem.getStatusChanges().get(size - 2).getStatus());
                    logger.info("Last Enrollment Status : " + lastEnrollmentStatus);
                    break;
                }
            }
            if (lastEnrollmentStatus != null) {
                ProcessResult pr = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                        company.getSourceSystemCd(), company.getSourceCompanyId(), rafEnrollment,
                        lastEnrollmentStatus);
                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Error updating enrollment for company: " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
                }
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void enrollAllRAFEnrollments(SAPRAFEnrollmentSearch enrollmentCriteria) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<RAFEnrollment> enrollments = RAFEnrollment.getRAFEnrollmentsByStatus(RAFEnrollmentStatus.valueOf(enrollmentCriteria.getStatus()),
                    enrollmentCriteria.getPSIDEINList(),
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(enrollmentCriteria.getCreationDateStart()),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(enrollmentCriteria.getCreationDateEnd()),
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(enrollmentCriteria.getLastUpdateDateStart()),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(enrollmentCriteria.getLastUpdateDateEnd()),
                    -1,
                    -1);

            for (RAFEnrollment enrollment : enrollments) {
                Company company = enrollment.getCompanyAgency().getCompany();
                ProcessResult pr = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                        company.getSourceSystemCd(), company.getSourceCompanyId(), enrollment, RAFEnrollmentStatus.Enrolled);
                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Error updating enrollment for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
                }
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error enrolling all enrollments", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.ManageRAFEnrollment, OperationId.ViewGlobalEnrollments})
    public SAPSearchResults<SAPACHEnrollmentDetail> findACHEnrollments(String status, int pFirstResult, int pMaxResults, String sortColumn, Boolean sortDescending) throws Throwable {
        ArrayList<SAPACHEnrollmentDetail> returnList = new ArrayList<SAPACHEnrollmentDetail>();
        SAPSearchResults<SAPACHEnrollmentDetail> resultsToReturn = new SAPSearchResults<SAPACHEnrollmentDetail>();
        try {
            ACHEnrollmentStatus enrollmentStatus = ACHEnrollmentStatus.valueOf(status);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            SortableProperty orderBy = ACHEnrollment.Id();
           
           if(sortColumn!=null) {
               if (sortColumn.equals("companyName")) {
                   orderBy = ACHEnrollment.CompanyAgency().Company().LegalName();
               } else if (sortColumn.equals("companyId")) {
                   orderBy = ACHEnrollment.CompanyAgency().Company().SourceCompanyId();
               } else if (sortColumn.equals("creationDate")) {
                   if (enrollmentStatus.in(ACHEnrollmentStatus.PendingEnrollment, ACHEnrollmentStatus.PendingDelete)) {
                       orderBy = ACHEnrollment.CreatedDate();
                   } else {
                       orderBy = ACHEnrollment.ACHEnrollmentDetail().CreatedDate();
                   }
               } else if (sortColumn.equals("modifiedDate")) {
                   if (enrollmentStatus.in(ACHEnrollmentStatus.PendingEnrollment, ACHEnrollmentStatus.PendingDelete)) {
                       orderBy = ACHEnrollment.ModifiedDate();
                   } else {
                       orderBy = ACHEnrollment.ACHEnrollmentDetail().ModifiedDate();
                   }
               } else if (sortColumn.equals("rejectionReason")) {
                   orderBy = ACHEnrollment.StatusReason();
               } else if (sortColumn.equals("effectiveDate")) {
                   orderBy = ACHEnrollment.EffectiveDate();
               }
           }
            if (sortDescending) {
                orderBy = orderBy.Descending();
            }
            DomainEntitySet<ACHEnrollment> enrollments = Application.find(ACHEnrollment.class, new Query<ACHEnrollment>().Where(ACHEnrollment.Status().equalTo(enrollmentStatus))
                    .OrderBy(orderBy)
                    .LimitResults(pFirstResult, pMaxResults));
            if (pMaxResults > 0) {
                Long totalRecords = Application.executeScalarAggQuery(ACHEnrollment.class, new Query<ACHEnrollment>()
                        .Select(ACHEnrollment.Id().Count())
                        .Where(ACHEnrollment.Status().equalTo(enrollmentStatus)));
                resultsToReturn.setTotalRecords(totalRecords);
            } else {
                resultsToReturn.setTotalRecords(enrollments.size());
            }

            for (ACHEnrollment enrollment : enrollments) {
                returnList.add(TaxTranslator.getSAPACHEnrollmentDetailFromDomainEntity(enrollment, enrollment.getACHEnrollmentDetail()));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding ACH enrollments.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        resultsToReturn.setReturnsList(returnList);
        return resultsToReturn;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void uploadACHResponseFile(String fileName, byte[] file) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult pr = PayrollServices.companyManager.uploadACHResponseFile(fileName, new String(file));
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error uploading ACH enrollment response file", pr);
            } else {
                // Scheduling ACHEnrollment batch job to process the uploaded file
                AdministrationAdapter.scheduleJob(BatchJobType.ACHEnrollmentResponseBatchJob.name(), null);
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error uploading ACH enrollment response file", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void updateACHEnrollmentAsEnrolled(String pSourceSystemCode, @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));

            ProcessResult pr = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.Enrolled);
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error updating ACH enrollment for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating ACH enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.ManageRAFEnrollment})
    public void reInitiateACHEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));

            ProcessResult pr = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.PendingEnrollment);
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error re-initiating ACH enrollment for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error re-initiating ACH enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void deleteACHEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pEnrollmentID) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));

            ACHEnrollment achEnrollment = Application.findById(ACHEnrollment.class, SpcfUniqueId.createInstance(pEnrollmentID));
            if (achEnrollment == null || achEnrollment != company.getCurrentACHEnrollment()) {
                aeFactory.throwGenericException("Error deleting enrollment: enrollment must be most current");
                return;
            }

            ProcessResult pr = PayrollServices.companyManager.deleteACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI);
            if (!pr.isSuccess() || pr.getResult() == null) {
                aeFactory.throwGenericException("Error deleting enrollment for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error deleting enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageRAFEnrollment)
    public void cancelDeleteACHEnrollment(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pEnrollmentId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));

            ProcessResult pr = PayrollServices.companyManager.updateACHEnrollmentStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.FL_SUI, ACHEnrollmentStatus.Cancelled);
            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error cancelling ACH enrollment delete for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId(), pr);
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error cancelling ACH enrollment delete", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.ManageRAFEnrollment, OperationId.ViewGlobalEnrollments})
    public SAPACHEnrollmentHistory getACHEnrollmentsHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        SAPACHEnrollmentHistory sapAchEnrollmentHistory = new SAPACHEnrollmentHistory();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            DomainEntitySet<ACHEnrollment> enrollments = company.getAllACHEnrollmentsIncludingCancelled();
            if (enrollments.isEmpty()) {
                sapAchEnrollmentHistory.setCanRe_enroll(false);
                return sapAchEnrollmentHistory;
            }
            ACHEnrollment topACHEnrolled = company.getCurrentACHEnrollment();
            PaymentTemplate flTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
            sapAchEnrollmentHistory.setCanRe_enroll(company.isCompanyOnService(ServiceCode.Tax) && company.getCurrentACHEnrollment().getStatus() == ACHEnrollmentStatus.EnrollmentRejected);
            for (ACHEnrollment achEnrollment : enrollments) {
                SAPACHEnrollmentHistoryItem sapAchEnrollmentHistoryItem = new SAPACHEnrollmentHistoryItem();
                sapAchEnrollmentHistoryItem.setEnrollmentId(achEnrollment.getId().toString());
                //can only delete if most recent enrollment and it's current enrolled
                //PSRV004218 - if most recent enrollment was in cancelled state, then look for the top
                //enrollement with ENROLLED status -- and enable delete on it
                //PSRV004209 - No ability to delete when the company is still active
                Boolean isFirstEnrolled = achEnrollment == enrollments.getFirst()
                        && (achEnrollment.getStatus() == ACHEnrollmentStatus.Enrolled);
                Boolean isTopEnrolled = achEnrollment == topACHEnrolled
                        && (achEnrollment.getStatus() == ACHEnrollmentStatus.Enrolled);
                Boolean firstACHCancelled = (enrollments.getFirst().getStatus() == ACHEnrollmentStatus.Cancelled);
                Boolean mostRecentEnrollment = (isFirstEnrolled || (firstACHCancelled && isTopEnrolled));
                Boolean pitemInactive = false;

                //FL Entrollment - if pitem in inactive, then button should display
                if (achEnrollment.getCompanyAgency() != null && achEnrollment.getCompanyAgency().getAgency() == flTemplate.getAgency()) {
                    DomainEntitySet<CompanyLaw> companyLaws = achEnrollment.getCompanyAgency().getCompanyLawCollection().find(CompanyLaw.AdditionalCompanyLaw().isNull());
                    for (CompanyLaw companyLaw : companyLaws) {
                        if (companyLaw.getFilingStatus() == PayrollItemStatus.Inactive) {
                            pitemInactive = true;
                            break;
                        }
                    }
                }
                Boolean canDelete = mostRecentEnrollment && (company.getService(ServiceCode.Tax).isCancelTerm() || pitemInactive);
                sapAchEnrollmentHistoryItem.setCanDelete(canDelete);
                sapAchEnrollmentHistoryItem.setRejectedReason(achEnrollment.getStatusReason());
                ACHEnrollmentDetail enrollmentDetail = achEnrollment.getACHEnrollmentDetail();
                if (enrollmentDetail != null) {
                    /*  Get enrollment detail if it exists -- these were the details actually sent to agency */
                    sapAchEnrollmentHistoryItem.setEin(enrollmentDetail.getFEIN());
                    sapAchEnrollmentHistoryItem.setLegalName(enrollmentDetail.getLegalName());
                    sapAchEnrollmentHistoryItem.setAgencyId(enrollmentDetail.getAgencyId());
                } else {
                    /*  Get those details from the company -- these are the details that _will_ be sent to the agency */
                    sapAchEnrollmentHistoryItem.setEin(company.getFedTaxId());
                    sapAchEnrollmentHistoryItem.setLegalName(company.getLegalName());
                    sapAchEnrollmentHistoryItem.setAgencyId(CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI)).getAgencyTaxpayerId());
                }
                /*  Now get a history of status changes from the company event log, the following query has been implemented in HQL */

                DomainEntitySet<CompanyEventDetail> historyEventDetails = CompanyEvent.findCompanyEventDetailForEventDetailValue(company, EventDetailTypeCode.NewStringValue, EventDetailTypeCode.ACHEnrollmentId, sapAchEnrollmentHistoryItem.getEnrollmentId());

                for (CompanyEventDetail companyEventDetail : historyEventDetails) {
                    SAPEnrollmentStatusChange sapRafEnrollmentStatusChange = new SAPEnrollmentStatusChange();
                    sapRafEnrollmentStatusChange.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(companyEventDetail.getCreatedDate()));
                    sapRafEnrollmentStatusChange.setModifiedBy(SAPTranslator.getUserNameFromUserID(companyEventDetail.getModifierId()));
                    sapRafEnrollmentStatusChange.setStatus(companyEventDetail.getValue());
                    sapAchEnrollmentHistoryItem.getStatusChanges().add(sapRafEnrollmentStatusChange);
                }
                sapAchEnrollmentHistory.getEnrollments().add(sapAchEnrollmentHistoryItem);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting enrollments history.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapAchEnrollmentHistory;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public List<SAPAgencyInfoDTO> getAgencyInfoArray(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId) throws Throwable {

        ArrayList<SAPAgencyInfoDTO> agencyInfos = new ArrayList<SAPAgencyInfoDTO>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
            if (rafEnrollment == null) {
                aeFactory.throwGenericException("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + " has no current RAF enrollment");
                return null;
            }

            EftpsEnrollment eftpsEnrollment = company.getCurrentEnrollment();
            if (eftpsEnrollment == null) {
                aeFactory.throwGenericException("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + " has no current EFTPS enrollment");
                return null;
            }

            DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, new Query<CompanyAgency>().Where(CompanyAgency.Company().equalTo(company)));
            for (CompanyAgency companyAgency : companyAgencies) {
                SAPAgencyInfoDTO sapAgency = getAgencyInfo(companyAgency);
                if (!sapAgency.getCompanyPaymentTemplates().isEmpty()) {
                    if (companyAgency.getAgency().isIRS()) {
                        sapAgency.setCurrentRAFStatus(rafEnrollment.getStatus().toString());
                        sapAgency.setCurrentEFTPSStatus(eftpsEnrollment.getStatusCd().toString());
                        if (company.getNameControl() != null) {
                            sapAgency.setNameControl(company.getNameControl());
                        } else {
                            sapAgency.setNameControl("N/A");
                        }
                        sapAgency.setErFicaDeferralEnabled(companyAgency.getErFicaDeferralEnabled());
                    }
                    if (companyAgency.getAgency().getAgencyId().equals(Agency.FL_AGENT_ID)) {
                        ACHEnrollment achEnrollment = company.getCurrentACHEnrollment();
                        sapAgency.setCurrentACHEnrollmentStatus(achEnrollment == null ? "None" : achEnrollment.getStatus().toString());
                    }
                    /*  Only add agencies with at least one supported template  */
                    agencyInfos.add(sapAgency);
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException(String.format("Error obtaining Agencies for %s:%s", pSourceSystemCode, pSourceCompanyId), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return agencyInfos;
    }

    private SAPAgencyInfoDTO getAgencyInfo(CompanyAgency companyAgency) throws Throwable {

        SAPAgencyInfoDTO sapAgencyInfoDTO = new SAPAgencyInfoDTO();
        Company company = companyAgency.getCompany();
        try {
            sapAgencyInfoDTO.setAgency(TaxTranslator.getSAPAgencyFromDomainEntity(companyAgency.getAgency(), null));

            List<CompanyAgencyPaymentTemplate> paymentTemplates = new ArrayList<CompanyAgencyPaymentTemplate>(companyAgency.getCompanyAgencyPaymentTemplateCollection());
            Collections.sort(paymentTemplates, new Comparator<CompanyAgencyPaymentTemplate>() {
                public int compare(CompanyAgencyPaymentTemplate o1, CompanyAgencyPaymentTemplate o2) {

                    int order1 = getOrder(o1.getPaymentTemplate());
                    int order2 = getOrder(o2.getPaymentTemplate());
                    if (order1 != order2) {
                        return order1 - order2;
                    }
                    return o1.getPaymentTemplate().getPaymentTemplateCd().compareTo(o2.getPaymentTemplate().getPaymentTemplateCd());
                }

                private int getOrder(PaymentTemplate template) {

                    if (template.isIRS941()) {
                        return 0;
                    }
                    if (template.getCategory() == PaymentTemplateCategory.Withholding) {
                        return 1;
                    }
                    if (template.getCategory() == PaymentTemplateCategory.SUI) {
                        return 2;
                    }
                    return 3;
                }
            });
            for (CompanyAgencyPaymentTemplate companyPaymentTemplate : paymentTemplates) {
                /*  Do not process this template if it is not supported */
                if (companyPaymentTemplate.getPaymentTemplate().getSupportStartDate() == null) {
                    continue;
                }
                SAPCompanyPaymentTemplate sapCompanyPaymentTemplate = populateCompanyPaymentTemplate(company, companyPaymentTemplate, companyPaymentTemplate.getPaymentTemplate().getAgencyIds());
                sapAgencyInfoDTO.getCompanyPaymentTemplates().add(sapCompanyPaymentTemplate);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException(String.format("Error obtaining Agency information for %s:%s", company.getSourceSystemCd(), company.getSourceCompanyId()), t);
        }
        return sapAgencyInfoDTO;
    }

    private SAPCompanyPaymentTemplate populateCompanyPaymentTemplate(Company pCompany, CompanyAgencyPaymentTemplate pCompanyPaymentTemplate, DomainEntitySet<PaymentTemplateAgencyId> pAdditionalAgencyIds) {

        SpcfCalendar firstDayOfPreviousQuarter = CalendarUtils.getFirstDayOfPreviousQuarter(PSPDate.getPSPTime());
        SAPCompanyPaymentTemplate sapCompanyPaymentTemplate = new SAPCompanyPaymentTemplate();
        SAPDepositFrequency depositFrequency = new SAPDepositFrequency();
        depositFrequency.setDepositFrequency(EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(pCompany, pCompanyPaymentTemplate.getPaymentTemplate(), PSPDate.getPSPTime()).getPaymentTemplateFrequency().getPaymentFrequencyId().toString());
        sapCompanyPaymentTemplate.setCurrentDepositFrequency(depositFrequency);
        //this is intentionally different from the list that is retrieved when actually editing the DF.  Agents may need to fix data for obsolete DFs
        sapCompanyPaymentTemplate.setCanChangeDepositFrequency(PaymentTemplateFrequency.findSupportedFrequencies(pCompanyPaymentTemplate.getPaymentTemplate()).size() > 1
                && !pCompanyPaymentTemplate.getPaymentTemplate().isFollowsFederal()
                && !pCompanyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd().equals(PaymentTemplate.NY_METRO)
                && !pCompanyPaymentTemplate.getPaymentTemplate().getAgency().isIRS()
                && pCompanyPaymentTemplate.getPaymentTemplate().getCategory() != PaymentTemplateCategory.SUI);

        if (pCompanyPaymentTemplate.getPaymentTemplate().isIRS941()) {
            CompanyAgencyFormTemplate formTemplate = pCompanyPaymentTemplate.getFormTemplate();
            SAPFilerType filerType = TaxTranslator.get941FilerTypeFromFormTemplate(formTemplate);
            sapCompanyPaymentTemplate.setIs944Filer(filerType.getFilerType().equals("944"));
            sapCompanyPaymentTemplate.setFilerType(filerType.getFilerType());
            if (filerType.getEffectiveQuarter().compareTo(SAPQuarter.currentQuarter()) > 0) {
                sapCompanyPaymentTemplate.setFilerTypeFutureEffectiveQuarter(filerType.getEffectiveQuarter());
            }
        }

        SAPPaymentTemplate sapPaymentTemplate = new SAPPaymentTemplate();
        sapPaymentTemplate.setPaymentTemplateCd(pCompanyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd());
        sapPaymentTemplate.setPaymentTemplateName(pCompanyPaymentTemplate.getPaymentTemplate().getPaymentTemplateAbbrev());
        sapPaymentTemplate.setAgencyName(pCompanyPaymentTemplate.getCompanyAgency().getAgency().getAgencyId());
        sapPaymentTemplate.setSupportStartDate(SpcfUtils.convertSpcfCalendarToDate(pCompanyPaymentTemplate.getPaymentTemplate().getSupportStartDate()));
        sapPaymentTemplate.setProcessingStartDate(SAPTranslator.getDateFromSpcfCalendar(pCompanyPaymentTemplate.getPaymentTemplate().getProcessingStartDate()));
        sapPaymentTemplate.setFollowsFedDepositFrequency(pCompanyPaymentTemplate.getPaymentTemplate().isFollowsFederal());
        sapCompanyPaymentTemplate.setPaymentTemplate(sapPaymentTemplate);
        sapCompanyPaymentTemplate.setAgencyTaxpayerId(pCompanyPaymentTemplate.getAgencyTaxpayerId());
        for (PaymentTemplateAgencyId agencyId : pCompanyPaymentTemplate.getPaymentTemplate().getAgencyIds()) {
            sapPaymentTemplate.getAgencyIDs().add(agencyId.getId().toString());
        }
        getPaymentMethods(pCompanyPaymentTemplate, sapCompanyPaymentTemplate);

        for (PaymentTemplateAgencyId additionalAgencyId : pAdditionalAgencyIds) {
            CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId = pCompanyPaymentTemplate.getCompanyPaymentTemplateAgencyIdCollection()
                    .find(CompanyPaymentTemplateAgencyId.Name().equalTo(additionalAgencyId.getName())).getFirst();

            if (companyPaymentTemplateAgencyId != null) {
                sapCompanyPaymentTemplate.getAdditionalIds().add(TaxTranslator.getAdditionalAgencyIdFromDomainEntity(companyPaymentTemplateAgencyId));
            } else {
                sapCompanyPaymentTemplate.getAdditionalIds().add(TaxTranslator.getMissingAdditionalAgencyId(additionalAgencyId.getName()));
            }
        }

        for (CompanyFilingAmount companyFilingAmount : pCompanyPaymentTemplate.getActiveAndMissingCompanyFilingAmounts().sort(CompanyFilingAmount.Name())) {
            CompanyFilingAmount previousQtrFilingAmount = null;
            if (companyFilingAmount.getAdditionalFilingAmount() != null) {
                previousQtrFilingAmount = pCompanyPaymentTemplate.getCompanyFilingAmount(companyFilingAmount.getAdditionalFilingAmount(), firstDayOfPreviousQuarter);
            }

            sapCompanyPaymentTemplate.getActiveFilingAmounts().add(TaxTranslator.getCompanyFilingAmountHistory(companyFilingAmount, previousQtrFilingAmount));
        }

        /*  Get current rates for these laws    */
        addLawRateDetails(pCompanyPaymentTemplate, sapCompanyPaymentTemplate);
        return sapCompanyPaymentTemplate;
    }

    private void getPaymentMethods(CompanyAgencyPaymentTemplate pCompanyPaymentTemplate, SAPCompanyPaymentTemplate pSAPCompanyPaymentTemplate) {

        for (CompanyPaymentTemplatePaymentMethod paymentTemplatePaymentMethod : pCompanyPaymentTemplate.getCompanyPaymentTemplatePaymentMethodCollection()) {
            SAPPaymentMethod paymentMethod = TaxTranslator.getCompanyPaymentTemplatePaymentMethodFromDomainEntity(paymentTemplatePaymentMethod, null);
            pSAPCompanyPaymentTemplate.getPaymentMethods().add(paymentMethod);
            if (paymentMethod.getHasManualRequirement()) {
                pSAPCompanyPaymentTemplate.setRegisteredForACH(paymentMethod.getIsAgentEnabled());
            }
        }
        List<SAPPaymentMethod> unsortedPaymentMethods = pSAPCompanyPaymentTemplate.getPaymentMethods();
        if (pCompanyPaymentTemplate.getCompanyAgency().getAgency().isIRS()) {
            Collections.sort(unsortedPaymentMethods, new Comparator<SAPPaymentMethod>() {
                public int compare(SAPPaymentMethod o1, SAPPaymentMethod o2) {

                    if (o1.getPaymentMethodName().equalsIgnoreCase("EFTPS")) {
                        return -1;
                    }
                    if (o2.getPaymentMethodName().equalsIgnoreCase("EFTPS")) {
                        return 1;
                    }
                    return o1.getPaymentMethodName().compareToIgnoreCase(o2.getPaymentMethodName());
                }
            });
            pSAPCompanyPaymentTemplate.setPaymentMethods(unsortedPaymentMethods);
        } else {
            Collections.sort(unsortedPaymentMethods, new Comparator<SAPPaymentMethod>() {
                public int compare(SAPPaymentMethod o1, SAPPaymentMethod o2) {

                    return o1.getPaymentMethodOrder() - o2.getPaymentMethodOrder();
                }
            });
        }
    }

    private void addLawRateDetails(CompanyAgencyPaymentTemplate pCompanyPaymentTemplate, SAPCompanyPaymentTemplate pSAPCompanyPaymentTemplate) {

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class, new Query<CompanyLaw>()
                .Where(CompanyLaw.Law().PaymentTemplate().equalTo(pCompanyPaymentTemplate.getPaymentTemplate())
                        .And(CompanyLaw.CompanyAgency().equalTo(pCompanyPaymentTemplate.getCompanyAgency()))
                        .And(CompanyLaw.AdditionalCompanyLaw().isNull()))
                .OrderBy(CompanyLaw.Law().LawAbbrev()));
        for (CompanyLaw companyLaw : companyLaws) {
            if (companyLaw.getLaw().getIsEmployerTax() && pCompanyPaymentTemplate.getPaymentTemplate().getCategory() == PaymentTemplateCategory.SUI) {
                pSAPCompanyPaymentTemplate.setHasSUIERRates(true);
            }

            SAPCompanyLawRateDetail companyLawRateDetail = new SAPCompanyLawRateDetail();
            companyLawRateDetail.setLawName(companyLaw.getLaw().getLawAbbrev());
            companyLawRateDetail.setLawId(companyLaw.getLaw().getLawId());
            companyLawRateDetail.setSourceLawID(companyLaw.getSourceId());
            companyLawRateDetail.setSourceLawDescription(companyLaw.getSourceDescription());
            companyLawRateDetail.setExempt((companyLaw.getExemptionStatus() != null && companyLaw.getExemptionStatus().equals(LawStatus.Exempt)));
            companyLawRateDetail.setReimbursable((companyLaw.getReimbursableStatus() != null && companyLaw.getReimbursableStatus().equals(ReimbursableStatus.Reimbursable)));
            companyLawRateDetail.setInactive(companyLaw.getFilingStatus() != null && companyLaw.getFilingStatus().equals(PayrollItemStatus.Inactive));
            companyLawRateDetail.setAgencyId(pSAPCompanyPaymentTemplate.getPaymentTemplate().getAgencyName());

            CompanyLawRate effectiveLawRate = CompanyLawRate.findEffectiveLawRate(companyLaw, PSPDate.getPSPTime());
            if (effectiveLawRate == null) {
                companyLawRateDetail.setRate(Double.NaN);
            } else {
                companyLawRateDetail.setRate(effectiveLawRate.getRate());
            }

            pSAPCompanyPaymentTemplate.getLawRates().add(companyLawRateDetail);
        }
    }

    private List<SAPPaymentMethod> getAllPaymentMethods(String pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, MoneyMovementTransaction pPayment) throws Throwable {

        List<SAPPaymentMethod> paymentMethods = new ArrayList<SAPPaymentMethod>();
        Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
        /*  Find payment methods for this company */
        DomainEntitySet<CompanyPaymentTemplatePaymentMethod> paymentTemplatePaymentMethods = Application.find(CompanyPaymentTemplatePaymentMethod.class, new Query<CompanyPaymentTemplatePaymentMethod>()
                .Where(CompanyPaymentTemplatePaymentMethod.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(company)
                        .And(CompanyPaymentTemplatePaymentMethod.CompanyAgencyPaymentTemplate().PaymentTemplate().PaymentTemplateCd().equalTo(pPaymentTemplateCd))));
        for (CompanyPaymentTemplatePaymentMethod templatePaymentMethod : paymentTemplatePaymentMethods) {
            paymentMethods.add(TaxTranslator.getCompanyPaymentTemplatePaymentMethodFromDomainEntity(templatePaymentMethod, pPayment));
        }
        return paymentMethods;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public List<SAPCompanyFilingAmountHistory> getCompanyFilingAmountHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPaymentTemplateCd) throws Throwable {

        List<SAPCompanyFilingAmountHistory> filingAmountHistory = new ArrayList<SAPCompanyFilingAmountHistory>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd));
            for (CompanyFilingAmount companyFilingAmount : companyAgencyPaymentTemplate.getCompanyFilingAmountCollection()) {
                filingAmountHistory.add(TaxTranslator.getCompanyFilingAmountHistory(companyFilingAmount, null));
            }

        } catch (Exception e) {
            aeFactory.throwGenericException(String.format("Error getting filing amount history for company %s : %s [%s]", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd), e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return filingAmountHistory;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public List<SAPCompanyAgencyPaymentTemplateAgencyId> getAdditionalAgencyIdsHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPaymentTemplateCd) throws Throwable {

        List<SAPCompanyAgencyPaymentTemplateAgencyId> additionalAgencyIds = new ArrayList<SAPCompanyAgencyPaymentTemplateAgencyId>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            if (company == null) {
                aeFactory.throwGenericException(String.format("Company %s : %s does not exist", pSourceSystemCode, pSourceCompanyId));
            }
            DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = Application.find(CompanyAgencyPaymentTemplate.class, new Query<CompanyAgencyPaymentTemplate>()
                    .Where(CompanyAgencyPaymentTemplate.CompanyAgency().Company().equalTo(company)
                            .And(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo(pPaymentTemplateCd))));
            /*  There should only be one companyAgencyPaymentTemplate per company for a given templateCode  */
            for (CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId : companyAgencyPaymentTemplates.get(0).getCompanyPaymentTemplateAgencyIdCollection()) {
                additionalAgencyIds.add(TaxTranslator.getAdditionalAgencyIdFromDomainEntity(companyPaymentTemplateAgencyId));
            }
        } catch (Exception e) {
            aeFactory.throwGenericException(String.format("Error getting additional agency ids for company %s : %s [%s]", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd), e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return additionalAgencyIds;
    }

    @FlexMethod
    public List<SAPPaymentMethod> getPaymentMethodsHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPaymentTemplateCd, String pFieldName) throws Throwable {

        List<SAPPaymentMethod> sapPaymentMethods = new ArrayList<SAPPaymentMethod>(5);
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            if (company == null) {
                throw new Exception(String.format("Company %s:%s not found.", pSourceSystemCode, pSourceCompanyId));
            }
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd);
            if (paymentTemplate == null) {
                throw new Exception(String.format("Payment Template %s not found.", pPaymentTemplateCd));
            }
            DomainEntitySet<PropertyAudit> propertyAudits = PropertyAudit.findPropertyAudits(company, "CompanyPaymentTemplatePaymentMethod", pFieldName, null);
            for (PropertyAudit propertyAudit : propertyAudits) {
                CompanyPaymentTemplatePaymentMethod templatePaymentMethod = Application.findById(CompanyPaymentTemplatePaymentMethod.class, SpcfUniqueId.createInstance(propertyAudit.getObjectIdentifier()));
                if (templatePaymentMethod != null && templatePaymentMethod.getCompanyAgencyPaymentTemplate().getPaymentTemplate().equals(paymentTemplate)) {
                    SAPPaymentMethod sapPaymentMethod = new SAPPaymentMethod();
                    sapPaymentMethod.setModifiedDate(SpcfUtils.convertSpcfCalendarToDate(propertyAudit.getCreatedDate()));
                    sapPaymentMethod.setIsEnabled(propertyAudit.getNewPropertyValue().equals("1"));
                    sapPaymentMethod.setChangedBy(SAPTranslator.getUserNameFromUserID(propertyAudit.getUserId()));
                    sapPaymentMethod.setPaymentMethodName(templatePaymentMethod.getPaymentMethod().name());
                    sapPaymentMethods.add(sapPaymentMethod);
                }
            }
        } catch (Exception e) {
            aeFactory.throwGenericException(String.format("Error getting Company PaymentTemplate Payment Methods History for %s:%s - %s\n%s\n", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, e.getMessage()));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPaymentMethods;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public List<SAPPropertyAudit> getAgencyIdHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPaymentTemplateCd) throws Throwable {

        List<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>(5);
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            if (company == null) {
                throw new Exception(String.format("Company %s:%s not found.", pSourceSystemCode, pSourceCompanyId));
            }
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd);
            if (paymentTemplate == null) {
                throw new Exception(String.format("Payment Template %s not found.", pPaymentTemplateCd));
            }
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);
            DomainEntitySet<PropertyAudit> propertyAudits = PropertyAudit.findPropertyAudits(company, "CompanyAgencyPaymentTemplate", "AgencyTaxpayerId", companyAgencyPaymentTemplate.getId().toString(), null);
            propertyAudits.addAll(PropertyAudit.findPropertyAudits(company, "CompanyAgencyPaymentTemplate", "AgencyTaxpayerIdEnc", companyAgencyPaymentTemplate.getId().toString(), null));
            propertyAudits.addAll(PropertyAudit.findPropertyAudits(company, "CompanyAgencyPaymentTemplate", "AgencyTaxpayerIdPt", companyAgencyPaymentTemplate.getId().toString(), null));
            for (PropertyAudit propertyAudit : propertyAudits) {
                sapPropertyAudits.add(PropertyAuditTranslator.getSAPPropertyAuditFromDomainEntity(propertyAudit));
            }
        } catch (Exception e) {
            aeFactory.throwGenericException(String.format("Error getting Agency ID History for %s:%s - %s\n%s\n", pSourceSystemCode, pSourceCompanyId, pPaymentTemplateCd, e.getMessage()));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPropertyAudits;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public List<SAPPropertyAudit> getCompanyAgencyHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pAgencyId) throws Throwable {

        List<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>(5);
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            if (company == null) {
                throw new Exception(String.format("Company %s:%s not found.", pSourceSystemCode, pSourceCompanyId));
            }
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, pAgencyId);
            if (companyAgency == null) {
                throw new Exception(String.format("Company Agency %s not found.", pAgencyId));
            }

            DomainEntitySet<PropertyAudit> propertyAudits = PropertyAudit.findPropertyAudits(company, "CompanyAgency", "ErFicaDeferralEnabled", companyAgency.getId().toString(), null);
            for (PropertyAudit propertyAudit : propertyAudits) {
                sapPropertyAudits.add(PropertyAuditTranslator.convertBooleanPropertyAudit(propertyAudit, "Enabled", "Disabled"));
            }
        } catch (Exception e) {
            aeFactory.throwGenericException(String.format("Error getting Company Agency History for %s:%s - %s\n%s\n", pSourceSystemCode, pSourceCompanyId, pAgencyId, e.getMessage()));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPropertyAudits;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditAgencyIDs)
    public void updateErFicaDeferral(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, boolean pEnabled) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(SourceSystemCode.valueOf(pSourceSystemCode), pSourceCompanyId, Agency.IRS);
            if (companyAgency == null) {
                throw new Exception(String.format("Company Agency %s not found.", Agency.IRS));
            }

            CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
            companyAgencyDTO.setErFicaDeferralEnabled(pEnabled);
            //noinspection rawtypes
            ProcessResult pr = PayrollServices.companyManager.updateCompanyAgency(SourceSystemCode.QBDT, pSourceCompanyId, Agency.IRS, companyAgencyDTO);

            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating er fica deferral", pr);
            }
        } catch (Exception e) {
            aeFactory.throwGenericException(String.format("Error updating er fica deferral for %s:%s - %s\n", pSourceSystemCode, pSourceCompanyId, e.getMessage()));
            logger.error("Error updating er fica deferral", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public SAPCompanyLawRatesHistory getCompanyLawRatesHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pPaymentTemplateCd) throws Throwable {

        SAPCompanyLawRatesHistory lawRatesHistory = new SAPCompanyLawRatesHistory();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));

            DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class, new Query<CompanyLaw>()
                    .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)
                            .And(CompanyLaw.Law().PaymentTemplate().PaymentTemplateCd().equalTo(pPaymentTemplateCd))
                            .And(CompanyLaw.AdditionalCompanyLaw().isNull())));
            for (CompanyLaw companyLaw : companyLaws) {
                DomainEntitySet<CompanyLawRate> companyLawRates = Application.find(CompanyLawRate.class, new Query<CompanyLawRate>()
                        .Where(CompanyLawRate.CompanyLaw().equalTo(companyLaw))
                        .OrderBy(CompanyLawRate.EffectiveDate(), CompanyLawRate.CreatedDate().Descending()));
                lawRatesHistory.getCompanyLawNames().add(companyLaw.getLaw().getLawAbbrev());

                SAPCompanyLawRateDetail effectiveRate = null;
                for (CompanyLawRate companyLawRate : companyLawRates) {
                    SAPCompanyLawRateDetail companyLawRatesDetail = new SAPCompanyLawRateDetail();
                    companyLawRatesDetail.setLawId(companyLaw.getLaw().getLawId());
                    companyLawRatesDetail.setLawName(companyLaw.getLaw().getLawAbbrev());
                    companyLawRatesDetail.setRate(companyLawRate.getRate());
                    companyLawRatesDetail.setEffectiveQuarter(TaxTranslator.getSAPQuarter(companyLawRate.getEffectiveDate()));
                    companyLawRatesDetail.setChangeDate(SAPTranslator.getDateFromSpcfCalendar(companyLawRate.getModifiedDate()));
                    companyLawRatesDetail.setChangedBy(SAPTranslator.getUserNameFromUserID(companyLawRate.getModifierId()));
                    companyLawRatesDetail.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(companyLawRate.getCreatedDate()));
                    companyLawRatesDetail.setCreatedBy(SAPTranslator.getUserNameFromUserID(companyLawRate.getCreatorId()));
                    if (companyLawRate.getInvalidDate() != null) {
                        companyLawRatesDetail.setInvalidDate(SAPTranslator.getDateFromSpcfCalendar(companyLawRate.getInvalidDate()));
                    }
                    lawRatesHistory.getCompanyLawRateDetails().add(companyLawRatesDetail);

                    if (companyLawRate.getInvalidDate() == null && !companyLawRate.getEffectiveDate().after(PSPDate.getPSPTime())) {
                        effectiveRate = companyLawRatesDetail;
                    }
                }

                if (effectiveRate != null) {
                    effectiveRate.setIsCurrent(true);
                }

            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error initiating RAF re-enrollment", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return lawRatesHistory;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public ArrayList<SAPDepositFrequency> getDepositFrequencyHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pPaymentTemplateCd) throws Throwable {

        ArrayList<SAPDepositFrequency> sapDepositFrequencies = new ArrayList<SAPDepositFrequency>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, pPaymentTemplateCd);

            DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies = EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, paymentTemplate, null, null);
            SAPDepositFrequency lastEffective = null;
            for (EffectiveDepositFrequency effectiveDepositFrequency : effectiveDepositFrequencies) {
                SAPDepositFrequency depositFrequency = new SAPDepositFrequency();
                depositFrequency.setDepositFrequency(effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId().toString());
                depositFrequency.setEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(effectiveDepositFrequency.getEffectiveDate()));
                depositFrequency.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(effectiveDepositFrequency.getModifiedDate()));
                depositFrequency.setInvalidDate(SAPTranslator.getDateFromSpcfCalendar(effectiveDepositFrequency.getInvalidDate()));
                depositFrequency.setModifierId(SAPTranslator.getUserNameFromUserID(effectiveDepositFrequency.getModifierId()));
                if (effectiveDepositFrequency.getInvalidDate() == null && !effectiveDepositFrequency.getEffectiveDate().toLocal().after(PSPDate.getPSPTime())) {
                    lastEffective = depositFrequency;
                }
                sapDepositFrequencies.add(depositFrequency);
            }

            if (lastEffective != null) {
                lastEffective.setIsCurrent(true);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting Deposit Frequency history", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapDepositFrequencies;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public String getDefaultDepositFrequency(String pPaymentTemplateCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, pPaymentTemplateCd);
            return paymentTemplate.getDefaultDepositFrequency();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting default deposit frequency", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public ArrayList<SAPFilerType> getFilerTypeHistory(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {

        ArrayList<SAPFilerType> sapFilerTypes = new ArrayList<SAPFilerType>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            DomainEntitySet<CompanyAgencyFormTemplate> companyAgencyFormTemplates = getCompanyAgencyFormTemplates(pSourceSystemCode, pCompanyId);
            SAPFilerType sapFilerType;
            for (CompanyAgencyFormTemplate formTemplate : companyAgencyFormTemplates) {
                sapFilerType = new SAPFilerType();
                sapFilerType.setFilerType(formTemplate.getFormTemplate().getFormTemplateCd().split("-")[1]);
                sapFilerType.setEffectiveQuarter(TaxTranslator.getSAPQuarter(formTemplate.getEffectiveDate()));
                sapFilerType.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(formTemplate.getModifiedDate()));
                sapFilerType.setInvalidDate(SAPTranslator.getDateFromSpcfCalendar(formTemplate.getInvalidDate()));
                sapFilerType.setModifierId(SAPTranslator.getUserNameFromUserID(formTemplate.getModifierId()));
                sapFilerTypes.add(sapFilerType);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting Filer Type history", pSourceSystemCode, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapFilerTypes;
    }

    private DomainEntitySet<CompanyAgencyFormTemplate> getCompanyAgencyFormTemplates(String pSourceSystemCode, String pCompanyId) throws Throwable {

        String paymentTemplateCd = "IRS-941-PAYMENT";
        Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
        PaymentTemplate paymentTemplate = Application.findById(PaymentTemplate.class, paymentTemplateCd);

        return Application.find(CompanyAgencyFormTemplate.class, new Query<CompanyAgencyFormTemplate>()
                .Where(CompanyAgencyFormTemplate.FormTemplate().PaymentTemplate().equalTo(paymentTemplate)
                        .And(CompanyAgencyFormTemplate.CompanyAgency().Company().equalTo(company)))
                .OrderBy(CompanyAgencyFormTemplate.EffectiveDate().Descending()));
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageTaxPayments)
    public void updatePayDate(String paymentId, Date pNewPayDate, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            //Converting from settlement date to Initiation date by subtracting offset days.
            SpcfCalendar newDate = SAPTranslator.getSpcfCalendarFromDate(pNewPayDate).copy();
            MoneyMovementTransaction moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            CalendarUtils.addBusinessDays(newDate, MoneyMovementTransaction.getPaymentMethodDayOffset(moneyMovementTransaction.getMoneyMovementPaymentMethod(), moneyMovementTransaction.getPaymentTemplate()) * -1);

            ProcessResult result = PayrollServices.paymentManager.updateInitiationDate(paymentId, newDate);
            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error uploading new Settlement Date.", result);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error while updating Init Date", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getStatusHistoryData(String paymentId, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            sapPropertyAudits = getStatusHistoryDataForAllMMTs(paymentId);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPropertyAudits;
    }

    /*
     Transaction states and tax payment statuses.  Messy, but should be complete.
     */
    private ArrayList<SAPPropertyAudit> getStatusHistoryDataForAllMMTs(String paymentId) throws Throwable {

        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();
        try {
            MoneyMovementTransaction moneyMovementTransaction = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            if (moneyMovementTransaction.getFinancialTransactionCollection().size() > 0) {
                FinancialTransaction financialTransaction = moneyMovementTransaction.getFinancialTransactionCollection().get(0);
                for (FinancialTransactionState financialTransactionState : financialTransaction.getFinancialTransactionStates()) {
                    SAPPropertyAudit sapPropertyAudit = new SAPPropertyAudit();
                    sapPropertyAudit.setAuditDate(CalendarUtils.convertToDate(financialTransactionState.getCreatedDate()));
                    sapPropertyAudit.setUserId(SAPTranslator.getUserNameFromUserID(financialTransactionState.getCreatorId()));
                    if (financialTransactionState.getTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created)) {
                        if (moneyMovementTransaction.getOriginalTransaction() == null) {
                            sapPropertyAudit.setOldPropertyValue("Pending");
                        } else {
                            sapPropertyAudit.setOldPropertyValue("Pending Re-initiation");
                        }
                    } else if (financialTransactionState.getTransactionState().getTransactionStateCd().equals(TransactionStateCode.Returned)) {
                        sapPropertyAudit.setOldPropertyValue(getSimpleStatus(moneyMovementTransaction));
                    } else {
                        sapPropertyAudit.setOldPropertyValue(financialTransactionState.getTransactionState().getTransactionStateCd().toString());
                    }
                    sapPropertyAudits.add(sapPropertyAudit);
                }
                if (moneyMovementTransaction.getOriginalTransaction() != null) {
                    sapPropertyAudits.addAll(getStatusHistoryDataForAllMMTs(moneyMovementTransaction.getOriginalTransaction().getId().toString()));
                }
                for (PropertyAudit propertyAudit : PropertyAudit.findPropertyAudits(moneyMovementTransaction.getCompany(), "MoneyMovementTransaction", "TaxPaymentStatus", moneyMovementTransaction.getId().toString(), null)) {
                    SAPPropertyAudit sapPropertyAudit = new SAPPropertyAudit();
                    sapPropertyAudit.setAuditDate(SAPTranslator.getDateFromSpcfCalendar(propertyAudit.getAuditDate()));
                    if (StringUtils.equals(propertyAudit.getNewPropertyValue(), "ReadyToSend")) {
                        sapPropertyAudit.setOldPropertyValue("Pending");
                    } else {
                        sapPropertyAudit.setOldPropertyValue(StringUtils.replace(propertyAudit.getNewPropertyValue(), "ATF", ""));
                    }
                    sapPropertyAudit.setUserId(SAPTranslator.getUserNameFromUserID(propertyAudit.getUserId()));
                    sapPropertyAudits.add(sapPropertyAudit);
                }

            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding tax payment status history.", "Payment", paymentId, t);
        }
        return sapPropertyAudits;
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getHoldsHistoryData(String paymentId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            sapPropertyAudits = getHoldsHistoryDataForAllMMTs(paymentId);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPropertyAudits;
    }

    private ArrayList<SAPPropertyAudit> getHoldsHistoryDataForAllMMTs(String paymentId) throws Throwable {

        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();
        try {
            MoneyMovementTransaction moneyMovementTransaction = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            for (TaxPaymentOnHoldReason taxPaymentOnHoldReason : moneyMovementTransaction.getTaxPaymentOnHoldReasonCollection()) {
                SAPPropertyAudit sapPropertyAudit = new SAPPropertyAudit();
                sapPropertyAudit.setAuditDate(CalendarUtils.convertToDate(taxPaymentOnHoldReason.getEffectiveDate()));
                sapPropertyAudit.setUserId(SAPTranslator.getUserNameFromUserID(taxPaymentOnHoldReason.getCreatorId()));
                sapPropertyAudit.setOldPropertyValue("Added " + taxPaymentOnHoldReason.getOnHoldReasonCd().toString() + " Hold");
                sapPropertyAudits.add(sapPropertyAudit);
                if (taxPaymentOnHoldReason.getExpirationDate() != null) {
                    SAPPropertyAudit sapPropertyAuditRemove = new SAPPropertyAudit();
                    sapPropertyAuditRemove.setAuditDate(CalendarUtils.convertToDate(taxPaymentOnHoldReason.getExpirationDate()));
                    sapPropertyAuditRemove.setUserId(SAPTranslator.getUserNameFromUserID(taxPaymentOnHoldReason.getModifierId()));
                    sapPropertyAuditRemove.setOldPropertyValue("Removed " + taxPaymentOnHoldReason.getOnHoldReasonCd().toString() + " Hold");
                    sapPropertyAudits.add(sapPropertyAuditRemove);
                }
            }
            if (moneyMovementTransaction.getOriginalTransaction() != null) {
                sapPropertyAudits.addAll(getHoldsHistoryDataForAllMMTs(moneyMovementTransaction.getOriginalTransaction().getId().toString()));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding tax payment hold history.", "Payment", paymentId, t);
        }
        return sapPropertyAudits;
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getPaymentsPayDateAuditHistory(String paymentId, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();
        SimpleDateFormat propertyValueDateFormatter = new SimpleDateFormat("dd-MMM-yy");
        SimpleDateFormat outputdateFormatter = new SimpleDateFormat("MM/dd/yyyy");

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            MoneyMovementTransaction mmt = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            Criterion<PropertyAudit> propertyAuditCriterion = PropertyAudit.Company().equalTo(mmt.getCompany())
                    .And(PropertyAudit.ObjectIdentifier().equalTo(paymentId))
                    .And(PropertyAudit.ClassName().equalTo(MoneyMovementTransaction.class.getSimpleName()))
                    .And(PropertyAudit.PropertyName().equalTo(MoneyMovementTransaction.InitiationDate().getPropertyName()));
            DomainEntitySet<PropertyAudit> propertyAudits =
                    PayrollServices.entityFinder.find(PropertyAudit.class, new Query<PropertyAudit>().Where(propertyAuditCriterion).OrderBy(PropertyAudit.AuditDate()));

            if (propertyAudits.size() == 0) {
                SAPPropertyAudit auditData = new SAPPropertyAudit();
                SpcfCalendar spcfCalendar = mmt.getInitiationDate().copy();
                CalendarUtils.addBusinessDays(spcfCalendar, MoneyMovementTransaction.getPaymentMethodDayOffset(mmt.getMoneyMovementPaymentMethod(), mmt.getPaymentTemplate()));
                auditData.setNewPropertyValue(outputdateFormatter.format(CalendarUtils.convertToDate(spcfCalendar)));
                auditData.setAuditDate(CalendarUtils.convertToDate(mmt.getCreatedDate()));
                auditData.setUserId(SAPTranslator.getUserNameFromUserID(mmt.getCreatorId()));
                sapPropertyAudits.add(auditData);
            } else {
                SAPPropertyAudit auditData = PropertyAuditTranslator.getSAPPropertyAuditFromTaxPaymentHistory(propertyAudits.get(0));
                if (auditData != null) {
                    if (auditData.getOldPropertyValue() != null) {
                        Date newValue = propertyValueDateFormatter.parse(auditData.getOldPropertyValue());
                        SpcfCalendar spcfCalendar = CalendarUtils.convertToSpcfCalendar(newValue).copy();
                        CalendarUtils.addBusinessDays(spcfCalendar, MoneyMovementTransaction.getPaymentMethodDayOffset(mmt.getMoneyMovementPaymentMethod(), mmt.getPaymentTemplate()));
                        auditData.setNewPropertyValue(outputdateFormatter.format(CalendarUtils.convertToDate(spcfCalendar)));
                    }
                    auditData.setAuditDate(CalendarUtils.convertToDate(mmt.getCreatedDate()));
                    sapPropertyAudits.add(auditData);
                }
            }
            for (PropertyAudit propertyAudit : propertyAudits) {
                SAPPropertyAudit auditData = PropertyAuditTranslator.getSAPPropertyAuditFromTaxPaymentHistory(propertyAudit);
                Date newValue = propertyValueDateFormatter.parse(auditData.getNewPropertyValue());
                SpcfCalendar spcfCalendar = CalendarUtils.convertToSpcfCalendar(newValue).copy();
                CalendarUtils.addBusinessDays(spcfCalendar, MoneyMovementTransaction.getPaymentMethodDayOffset(mmt.getMoneyMovementPaymentMethod(), mmt.getPaymentTemplate()));
                auditData.setNewPropertyValue(outputdateFormatter.format(CalendarUtils.convertToDate(spcfCalendar)));
                sapPropertyAudits.add(auditData);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding tax payment Pay date history.", "Payment", paymentId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapPropertyAudits;
    }

    @FlexMethod
    public ArrayList<SAPPaymentDetails> getPaymentAmountDetails(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String paymentId) throws Throwable {

        ArrayList<SAPPaymentDetails> sapPaymentDetails = new ArrayList<SAPPaymentDetails>();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            Company company = Company.findCompany(
                    companyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            Criterion<FinancialTransaction> fftCriterion = FinancialTransaction.Company().equalTo(company)
                    .And(FinancialTransaction.MoneyMovementTransaction().Id().equalTo(SpcfUniqueId.createInstance(paymentId)));
            DomainEntitySet<FinancialTransaction> financialTransactions =
                    PayrollServices.entityFinder.find(FinancialTransaction.class,
                            new Query<FinancialTransaction>().Where(fftCriterion).OrderBy(FinancialTransaction.CreatedDate()));

            for (FinancialTransaction financialTransaction : financialTransactions) {
                SAPPaymentDetails details = TaxTranslator.getPaymentDetails(financialTransaction);
                if (details != null) {
                    sapPaymentDetails.add(details);
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding tax payment Details.", "Payment", paymentId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapPaymentDetails;
    }

    @FlexMethod
    public SAPOffloadDate getNextInitiationDate(String pPaymentMethod) throws Throwable {

        SAPOffloadDate sapOffloadDate = new SAPOffloadDate();
        PaymentMethod paymentMethod = PaymentMethod.valueOf(pPaymentMethod);
        try {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar newOffloadDate = MoneyMovementTransaction.getNextInitiationDate(paymentMethod);
            sapOffloadDate.setOffloadDate(SAPTranslator.getDateFromSpcfCalendar(newOffloadDate));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapOffloadDate;
    }

    @FlexMethod
    public SAPOffloadDate getOffloadDate(String pPaymentMethod, String pPaymentTemplate) throws Throwable {

        SAPOffloadDate sapOffloadDate = new SAPOffloadDate();
        PaymentMethod paymentMethod = PaymentMethod.valueOf(pPaymentMethod);
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            //Adding offset business day to next offload date as are validating against settlement date not with initiation date.
            SpcfCalendar newOffloadDate = MoneyMovementTransaction.getNextInitiationDate(paymentMethod);
            CalendarUtils.addBusinessDays(newOffloadDate, MoneyMovementTransaction.getPaymentMethodDayOffset(paymentMethod, PaymentTemplate.findPaymentTemplate(pPaymentTemplate)));
            sapOffloadDate.setOffloadDate(SAPTranslator.getDateFromSpcfCalendar(newOffloadDate));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapOffloadDate;
    }

    private SpcfCalendar getEarliestSettlementDate(PaymentMethod paymentMethod, PaymentTemplate pPaymentTemplate) {
        SpcfCalendar newOffloadDate = MoneyMovementTransaction.getNextInitiationDate(paymentMethod);
        CalendarUtils.addBusinessDays(newOffloadDate, MoneyMovementTransaction.getPaymentMethodDayOffset(paymentMethod, pPaymentTemplate));
        return newOffloadDate;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageTaxPayments)
    public void addTaxPaymentAgentOnHoldReason(String paymentId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            MoneyMovementTransaction moneyMovementTransaction = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            ProcessResult result = PayrollServices.paymentManager.addTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.Agent);
            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error while adding Agent Onhold reason:", result);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error while adding Agent Onhold reason", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageTaxPayments)
    public void removePaymentOnHoldReason(String paymentId, String holdReasonCd, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            MoneyMovementTransaction moneyMovementTransaction = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            ProcessResult result = PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.valueOf(holdReasonCd));
            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error while removing Onhold reason:", result);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error while removing Onhold reason", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageTaxPayments)
    public void updatePaymentMethod(String paymentId, String pPaymentMethod, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            PaymentMethod paymentMethod = PaymentMethod.valueOf(pPaymentMethod);
            MoneyMovementTransaction moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));

            ProcessResult result = PayrollServices.paymentManager.changePaymentMethod(moneyMovementTransaction.getCompany().getSourceSystemCd(),
                    moneyMovementTransaction.getCompany().getSourceCompanyId(), moneyMovementTransaction.getId(), paymentMethod);
            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating new payment method.", result);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating new payment method.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPPropertyAudit> getPaymentMethodAuditHistory(String paymentId, @TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            MoneyMovementTransaction mmt = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));

            Criterion<PropertyAudit> propertyAuditCriterion = PropertyAudit.Company().equalTo(mmt.getCompany())
                    .And(PropertyAudit.ObjectIdentifier().equalTo(paymentId))
                    .And(PropertyAudit.ClassName().equalTo(MoneyMovementTransaction.class.getSimpleName()))
                    .And(PropertyAudit.PropertyName().equalTo(MoneyMovementTransaction.MoneyMovementPaymentMethod().getPropertyName()));
            DomainEntitySet<PropertyAudit> propertyAudits =
                    PayrollServices.entityFinder.find(PropertyAudit.class, new Query<PropertyAudit>().Where(propertyAuditCriterion).OrderBy(PropertyAudit.AuditDate()));

            if (propertyAudits.size() == 0) {
                SAPPropertyAudit auditData = new SAPPropertyAudit();
                auditData.setNewPropertyValue(mmt.getMoneyMovementPaymentMethodString());
                auditData.setAuditDate(CalendarUtils.convertToDate(mmt.getCreatedDate()));
                auditData.setUserId(SAPTranslator.getUserNameFromUserID(mmt.getCreatorId()));
                sapPropertyAudits.add(auditData);
            } else {
                SAPPropertyAudit auditData = PropertyAuditTranslator.getSAPPropertyAuditFromTaxPaymentHistory(propertyAudits.get(0));
                auditData.setNewPropertyValue(auditData.getOldPropertyValue());
                auditData.setAuditDate(CalendarUtils.convertToDate(mmt.getCreatedDate()));
                sapPropertyAudits.add(auditData);
            }
            for (PropertyAudit propertyAudit : propertyAudits) {
                SAPPropertyAudit auditData = PropertyAuditTranslator.getSAPPropertyAuditFromTaxPaymentHistory(propertyAudit);
                sapPropertyAudits.add(auditData);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding tax payment Pay date history.", "Payment", paymentId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapPropertyAudits;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageTaxPayments)
    public void rejectPayment(String paymentId, String reason, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult result = PayrollServices.paymentManager.rejectPayment(paymentId, reason);
            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error rejecting payment.", result);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error while rejecting payment", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ManageTaxPayments)
    public void initiateRepayment(String paymentId, SAPTaxRepaymentOptions options, String psid) throws Throwable {

        Map<String, List<String>> paymentGroups = new HashMap<String, List<String>>();
        SpcfCalendar newInitDate;
        PaymentMethod paymentMethod;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            readConfigurationParameters();

            MoneyMovementTransaction selectedMmt = null;
            try {
                pspRequestContextManager.setRequestContextCompanyFromPSID(psid);
                selectedMmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }

            paymentMethod = options.getNewPaymentMethod() == null ? null : PaymentMethod.valueOf(options.getNewPaymentMethod());
            newInitDate = MoneyMovementTransaction.getNextInitiationDate(paymentMethod != null ? paymentMethod : selectedMmt.getMoneyMovementPaymentMethod());

            if (options.getUpdateAll()) {
                for (MoneyMovementTransaction moneyMovementTransaction : MoneyMovementTransaction.findTaxPayments()
                        .setPaymentTemplate(selectedMmt.getPaymentTemplate())
                        .setInitiationDate(selectedMmt.getInitiationDate())
                        .setPeriodEndDate(selectedMmt.getPaymentPeriodEnd())
                        .setPaymentMethods(new PaymentMethod[]{selectedMmt.getMoneyMovementPaymentMethod()})
                        .setRejectedOrReturned()
                        .find()) {
                    String companyId = moneyMovementTransaction.getCompany().getId().toString();
                    if (!paymentGroups.containsKey(companyId)) {
                        paymentGroups.put(companyId, new ArrayList<String>());
                    }
                    paymentGroups.get(companyId).add(moneyMovementTransaction.getId().toString());
                }
            } else {
                paymentGroups.put(selectedMmt.getCompany().getId().toString(), Arrays.asList(paymentId));
            }

            multithreadedInitiateRepayment(paymentGroups, newInitDate, options.getRecreate(), paymentMethod);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error initiating repayment.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public List<SAPPaymentMethod> getValidPaymentMethods(String paymentId, @TenantId(IdType = CompanyIdentifierType.PSID)String companyId) throws Throwable {

        ArrayList<SAPPaymentMethod> paymentMethods = new ArrayList<SAPPaymentMethod>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            MoneyMovementTransaction moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            Company company = moneyMovementTransaction.getCompany();
            return getAllPaymentMethods(company.getSourceSystemCd().name(), company.getSourceCompanyId(), moneyMovementTransaction.getPaymentTemplate().getPaymentTemplateCd(), moneyMovementTransaction);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting list of Payment methods.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentMethods;
    }

    @FlexMethod
    public ArrayList<String> getValidPaymentMethodsByTemplate(String paymentTemplateCd) throws Throwable{
        PaymentTemplate paymentTemplate=null;
        ArrayList<String> paymentMethodNamesList = new ArrayList<String>();
        try {
            paymentTemplate = Application.findById(PaymentTemplate.class, paymentTemplateCd);
        }
        catch(Exception e) {
            aeFactory.throwGenericException("Error updating payment methods : Payment Type missing. Please select Agency and Payment type again.");
        }
        if(paymentTemplate==null) {
            aeFactory.throwGenericException("Error updating payment methods : Payment Type missing. Please select Agency and Payment type again.");
        }
        DomainEntitySet<PaymentTemplatePaymentMethod> paymentTemplatePaymentMethodList = paymentTemplate.getPaymentTemplatePaymentMethods();

        for (PaymentTemplatePaymentMethod p : paymentTemplatePaymentMethodList) {
            paymentMethodNamesList.add(p.getPaymentMethod().toString());
        }

        return paymentMethodNamesList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewMoneyMovementScreen)
    public ArrayList<SAPPaymentForVerification> getMoneyMovementTransactionsForVerification(String pSourceSystemCode,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, Date pInitiationStartDate, Date pInitiationEndDate,
                                                                                            String pTotalAmountFrom, String pTotalAmountTo, String pRelatedAmountFrom, String pRelatedAmountTo,
                                                                                            String pStateTemplate) throws Throwable {

        ArrayList<SAPPaymentForVerification> sapPaymentForVerifications = new ArrayList<SAPPaymentForVerification>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));

            Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.Company().equalTo(company)
                    .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.EFTPS, PaymentMethod.EFTPSDirectDebit, PaymentMethod.ACHCredit, PaymentMethod.CheckPayment, PaymentMethod.SuperCheck, PaymentMethod.EDI))
                    .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReturnedTaxNotPaid, TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency, TaxPaymentStatus.RejectedByAgency, TaxPaymentStatus.ReturnedTaxPaid));

            if (!StringUtils.isEmpty(pStateTemplate)) {
                mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().like("%" + pStateTemplate.toUpperCase() + "%"));
            }
            if (pInitiationStartDate != null) {
                mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.InitiationDate().greaterOrEqualThan(SAPTranslator.getSpcfCalendarFromDate(pInitiationStartDate)));
            }
            if (pInitiationEndDate != null) {
                mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.InitiationDate().lessOrEqualThan(SAPTranslator.getSpcfCalendarFromDate(pInitiationEndDate)));
            }
            if (!StringUtils.isEmpty(pTotalAmountFrom)) {
                mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.MoneyMovementTransactionAmount().greaterOrEqualThan(SAPTranslator.getSpcfMoneyFromString(pTotalAmountFrom)));
            }
            if (!StringUtils.isEmpty(pTotalAmountTo)) {
                mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.MoneyMovementTransactionAmount().lessOrEqualThan(SAPTranslator.getSpcfMoneyFromString(pTotalAmountTo)));
            }
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                    new Query<MoneyMovementTransaction>().Where(mmtCriteria)
                            .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet()));
            if (!StringUtils.isEmpty(pRelatedAmountFrom) || !StringUtils.isEmpty(pRelatedAmountTo)) {
                Criterion<FinancialTransaction> fftCriteria;
                if (!StringUtils.isEmpty(pRelatedAmountFrom) && !StringUtils.isEmpty(pRelatedAmountTo)) {
                    fftCriteria = FinancialTransaction.FinancialTransactionAmount().greaterOrEqualThan(SAPTranslator.getSpcfMoneyFromString(pRelatedAmountFrom))
                            .And(FinancialTransaction.FinancialTransactionAmount().lessOrEqualThan(SAPTranslator.getSpcfMoneyFromString(pRelatedAmountTo)));
                } else if (!StringUtils.isEmpty(pRelatedAmountFrom)) {
                    fftCriteria = FinancialTransaction.FinancialTransactionAmount().greaterOrEqualThan(SAPTranslator.getSpcfMoneyFromString(pRelatedAmountFrom));
                } else {
                    fftCriteria = FinancialTransaction.FinancialTransactionAmount().lessOrEqualThan(SAPTranslator.getSpcfMoneyFromString(pRelatedAmountTo));
                }
                for (Iterator<MoneyMovementTransaction> iterator = moneyMovementTransactions.iterator(); iterator.hasNext();) {
                    MoneyMovementTransaction moneyMovementTransaction = iterator.next();
                    if (moneyMovementTransaction.getFinancialTransactionCollection().find(fftCriteria).size() == 0) {
                        iterator.remove();
                    }
                }
            }

            IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();

            for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
                if (shouldShowMMT(moneyMovementTransaction, rulesInfo)) {
                    sapPaymentForVerifications.add(TaxTranslator.getPaymentsForVerification(moneyMovementTransaction));
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting list of Payments.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPaymentForVerifications;
    }

    /**
     * Checks if the zero payment report is required for the
     * PaymentTemplateFrequency and amount
     *
     * @param moneyMovementTransaction The MMT to check for a zero payment
     * @return If the MMT should be added
     */
    private boolean shouldShowMMT(MoneyMovementTransaction moneyMovementTransaction, IRulesInfo rulesInfo) {

        if (!moneyMovementTransaction.getMoneyMovementTransactionAmount().equals(SpcfMoney.ZERO)) {
            // Always show non-zero payments
            return true;
        }

        DepositFrequencyCode depositFrequencyCode = moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId();
        IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(moneyMovementTransaction.getPaymentFrequency()
                .getPaymentTemplate().getPaymentTemplateCd());

        if (!depositFrequencyCode.equals(DepositFrequencyCode.NOCALC)) {
            IPaymentFrequency paymentFrequency = paymentTemplate.getPaymentFrequency(depositFrequencyCode.toString());

            FrequencyData freq = (FrequencyData) paymentFrequency;
            if (freq != null && freq.isZeroPaymentRequired()) {
                // Zero payment required
                return true;
            }
        }

        return false;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewCheckPrintQueue)
    public ArrayList<SAPPaymentTemplate> getSupportedPaymentTemplates() throws Throwable {

        ArrayList<SAPPaymentTemplate> paymentTemplates = new ArrayList<SAPPaymentTemplate>();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            DomainEntitySet<PaymentTemplate> allPaymentTemplates = Application.find(PaymentTemplate.class, new Query<PaymentTemplate>().Where(PaymentTemplate.SupportStartDate().isNotNull()));
            for (PaymentTemplate paymentTemplate : allPaymentTemplates) {
                paymentTemplates.add(TaxTranslator.getPaymentTemplateFromDomainEntity(paymentTemplate));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting PaymentTemplate list.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return paymentTemplates;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewMoneyMovementScreen)
    public ArrayList<SAPPaymentTemplate> getSupportedPaymentTemplatesForCompany(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId) throws Throwable {

        ArrayList<SAPPaymentTemplate> paymentTemplates = new ArrayList<SAPPaymentTemplate>();
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            DomainEntitySet<CompanyAgencyPaymentTemplate> supportedCompanyAgencyPaymentTemplates = CompanyAgencyPaymentTemplate.findSupportedCompanyAgencyPaymentTemplates(company);
            for (CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate : supportedCompanyAgencyPaymentTemplates) {
                paymentTemplates.add(TaxTranslator.getPaymentTemplateFromDomainEntity(companyAgencyPaymentTemplate));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException(String.format("Error getting supported payment template list for company %s:%s.\n%s", pSourceSystemCd, pSourceCompanyId, t));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return paymentTemplates;
    }

    /**
     * Finds all items for the data sync tool. Searches should either be a) ID
     * searches (idSearchType, fromId, toId are not null) i) Finds all items in
     * the ID range specified b) Type searches (type not null) i) Finds all
     * items of the type specified ii) May also filter on an item-specific date
     *
     * @param sourceSystemCd company
     * @param sourceCompanyId company
     * @param itemType what type of item to retrieve
     * @param idSearchTypeString which ID to search on; of [EmployeeID,
     * PayrollItemID, Token] or null if no ID search
     * @param fromId least possible ID (based on the ID search type)
     * @param toId greatest possible ID (based on the ID search type)
     * @param typeString which type of item to search; of [PayrollItems,
     * ItemsStopped, Employees] or null if
     * @param fromDate earliest created/modified/stopped date, depending on
     * which type is selected; or null if no type search
     * @param toDate latest created/modified/stopped date, depending on which
     * type is selected; or null if no type search
     * @param checkNumber check number to match on or null/empty
     * @param amount amount to match on or null/empty
     * @param pItemName P-Item name to match on or null/empty
     * @param pageSize page size
     * @param orderBy employee column to sort by or null for default sort
     * @param descending sort employee results descending?
     * @param firstResult first employee result for paging
     * @return SAPSearchResults<itemType> of each item
     */
    @FlexMethod
    @Operation(operationIds = OperationId.AccessDataSyncTool)
    public SAPSearchResults<? extends SAPDataSyncDetail> getDataSyncDetails(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId, String itemType, String idSearchTypeString, int fromId, int toId, String typeString, Date fromDate, Date toDate, String checkNumber, String amount, String pItemName,
                                                                            int pageSize,
                                                                            String orderBy, boolean descending, int firstResult) {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            IdSearchType idSearchType = idSearchTypeString == null ? null : IdSearchType.valueOf(idSearchTypeString);
            TypeSearchType type = typeString == null ? null : TypeSearchType.valueOf(typeString);
            SpcfCalendar fromCalendar = SAPTranslator.getSpcfCalendarFromDate_BeginDay(fromDate);
            SpcfCalendar toCalendar = SAPTranslator.getSpcfCalendarFromDate_EndDay(toDate);
            SpcfMoney amountMoney = SAPTranslator.getSpcfMoneyFromString(amount, null);

            if (itemType.equals("Paychecks")) {
                return getPaycheckDataSyncDetails(company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amountMoney, pItemName, orderBy, descending, firstResult, pageSize);
            } else if (itemType.equals("Payroll Transactions")) {
                return getPayrollTransactionDataSyncDetails(company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amountMoney, pItemName, orderBy, descending, firstResult, pageSize);
            } else if (itemType.equals("Employees")) {
                return getEmployeeDataSyncDetails(company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amountMoney, pItemName, orderBy, descending, firstResult, pageSize);
            } else if (itemType.equals("Payroll Items")) {
                return getPItemDataSyncDetails(company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amountMoney, pItemName, orderBy, descending, firstResult, pageSize);
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private enum IdSearchType {

        TransactionID,
        PaycheckID,
        EmployeeID,
        PayrollItemID,
        Token
    }

    private enum TypeSearchType {

        PayrollTxns,
        Paychecks,
        PayrollItems,
        ItemsStopped,
        Employees
    }

    private SAPSearchResults<SAPDataSyncDetailEmployee> getEmployeeDataSyncDetails(Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName,
                                                                                   String orderBy, boolean descending, int firstResult, int maxResults) {

        boolean hasNonNumericSourceIds = hasNonNumericSourceIds(company.getSourceSystemCd().toString(), company.getSourceCompanyId());

        HqlBuilder hql = new HqlBuilder(true, "select distinct ee from com.intuit.sbd.payroll.psp.domain.Employee ee join fetch ee.QbdtEmployeeInfoSet qei where ee.Company = :company ");

        hql.setParameter("company", company);

        if (idSearchType != null) {
            switch (idSearchType) {
                case EmployeeID:
                    if (hasNonNumericSourceIds) {
                        hql.append("and ee.SourceEmployeeId between :fromId and :toId");
                        hql.setParameter("fromId", Integer.toString(fromId));
                        hql.setParameter("toId", Integer.toString(toId));
                    } else {
                        hql.append("and cast(ee.SourceEmployeeId as int) between :fromId and :toId");
                        hql.setParameter("fromId", fromId, new IntegerType());
                        hql.setParameter("toId", toId, new IntegerType());
                    }
                    break;
                case PayrollItemID:
                case PaycheckID:
                case TransactionID:
                    return new SAPSearchResults<SAPDataSyncDetailEmployee>();
                case Token:
                    hql.append("and cast(qei.Token as int) between :fromId and :toId");
                    hql.setParameter("fromId", fromId, new IntegerType());
                    hql.setParameter("toId", toId, new IntegerType());
            }
        }

        if (type != null) {
            switch (type) {
                case Employees:
                    break;
                case PayrollItems:
                case Paychecks:
                case PayrollTxns:
                    return new SAPSearchResults<SAPDataSyncDetailEmployee>();
                case ItemsStopped:
                    hql.append("and qei.Token = -1");
                    if (fromCalendar != null) {
                        hql.append("and qei.ModifiedDate >= :fromDate");
                        hql.setParameter("fromDate", fromCalendar);
                    }
                    if (toCalendar != null) {
                        hql.append("and qei.ModifiedDate <= :toDate");
                        hql.setParameter("toDate", toCalendar);
                    }
            }
        }

        if (!StringUtils.isEmpty(checkNumber)) {
            return new SAPSearchResults<SAPDataSyncDetailEmployee>();
        }
        if (amount != null) {
            return new SAPSearchResults<SAPDataSyncDetailEmployee>();
        }
        if (!StringUtils.isEmpty(pItemName)) {
            return new SAPSearchResults<SAPDataSyncDetailEmployee>();
        }

        String employeeIdOrderBy = hasNonNumericSourceIds ? "ee.SourceEmployeeId" : "cast (ee.SourceEmployeeId as int)";
        String genericOrder = ", ".concat(employeeIdOrderBy).concat(", ee.Id");

        hql.append("order by");
        if (!StringUtils.isEmpty(orderBy)) {
            if (orderBy.equals("token")) {
                hql.append("qei.Token");
            } else if (orderBy.equals("employeeId")) {
                hql.append(employeeIdOrderBy);
                genericOrder = ", ee.Id";
            } else if (orderBy.equals("employeeName")) {
                hql.append("ee.LastName");
                if (descending) {
                    hql.append("desc");
                }
                hql.append(", ee.FirstName");
            }
            if (descending) {
                hql.append("desc");
            }
        } else {
            hql.append("qei.Token desc");
        }
        //break ties
        hql.append(genericOrder);

        List<Employee> employees = hql.list();

        ArrayList<SAPDataSyncDetailEmployee> returnList = new ArrayList<SAPDataSyncDetailEmployee>();

        for (Employee ee : employees.subList(firstResult, Math.min(employees.size(), firstResult + maxResults))) {
            returnList.add(TaxTranslator.getSapDataSyncDetailEmployee(ee));
        }

        return new SAPSearchResults<SAPDataSyncDetailEmployee>(employees.size(), returnList);
    }

    @FlexMethod
    public SAPManualLedgerLimit getManualLedgerLimit(String sourceSystemCd, String sourceCompanyId) {
        SAPManualLedgerLimit result = new SAPManualLedgerLimit();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            result.setWarningLimit(SystemParameter.findIntValue(SystemParameter.Code.MANUAL_LEDGER_TAX_WARNING_LIMIT, 10000));
            result.setBlockLimit(SystemParameter.findIntValue(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT, 10000));
            result.setLimitEnabled(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MANUAL_LEDGER_LIMIT_ENABLED, true));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return result;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AccessApplication)
    //Determine if any of the employees have a source ID that isn't numeric (like from an old DD submission)
    //non numeric values will occur before or after all of the numeric values, so we can check only the extrema
    public boolean hasNonNumericSourceIds(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId) {

        boolean manageSession = !Application.hasActiveTransaction();
        if (manageSession) {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
        }
        try {
            HqlBuilder aggHql = new HqlBuilder(true, "from com.intuit.sbd.payroll.psp.domain.Employee ee where ee.Company.SourceCompanyId = :companyId and ee.Company.SourceSystemCd = :systemCd");
            aggHql.setParameter("companyId", sourceCompanyId);
            aggHql.setParameter("systemCd", SourceSystemCode.valueOf(sourceSystemCd));
            List<Object> extremaList = aggHql.select("select max(ee.SourceEmployeeId), min(ee.SourceEmployeeId)");
            for (Object extremum : (Object[]) extremaList.get(0)) {
                if (!StringUtils.isNumeric((String) extremum)) {
                    return true;
                }
            }
            return false;
        } finally {
            if (manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    private SAPSearchResults<SAPDataSyncDetailPayrollItem> getPItemDataSyncDetails(Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName,
                                                                                   String orderBy, boolean descending, int firstResult, int maxResults) {

        HqlBuilder hql = new HqlBuilder(true, "from com.intuit.sbd.payroll.psp.domain.QbdtPayrollItemInfo info "
                + "left outer join fetch info.CompanyLaw cl "
                + "left outer join fetch info.CompanyPayrollItem cpi "
                + "where info.Company = :company "
                + " and info.Token != -2 ");
        hql.setParameter("company", company);

        if (idSearchType != null) {
            switch (idSearchType) {
                case EmployeeID:
                case TransactionID:
                case PaycheckID:
                    return new SAPSearchResults<SAPDataSyncDetailPayrollItem>();
                case PayrollItemID:
                    hql.append("and (cast(cl.SourceId as int) between :fromId and :toId or cast(cpi.SourcePayrollItemId as int) between :fromId and :toId)");
                    break;
                case Token:
                    hql.append("and cast(info.Token as int) between :fromId and :toId");
            }
            hql.setParameter("fromId", fromId, new IntegerType());
            hql.setParameter("toId", toId, new IntegerType());
        }
        if (type != null) {
            switch (type) {
                case Employees:
                case PayrollTxns:
                case Paychecks:
                    return new SAPSearchResults<SAPDataSyncDetailPayrollItem>();
                case PayrollItems:
                    break;
                case ItemsStopped:
                    hql.append("and info.Token = -1");
            }
            if (fromCalendar != null) {
                hql.append("and info.ModifiedDate >= :fromDate");
                hql.setParameter("fromDate", fromCalendar);
            }
            if (toCalendar != null) {
                hql.append("and info.ModifiedDate <= :toDate");
                hql.setParameter("toDate", toCalendar);
            }
        }

        if (!StringUtils.isEmpty(checkNumber)) {
            return new SAPSearchResults<SAPDataSyncDetailPayrollItem>();
        }
        if (amount != null) {
            return new SAPSearchResults<SAPDataSyncDetailPayrollItem>();
        }
        if (!StringUtils.isEmpty(pItemName)) {
            hql.append("and lower(COALESCE(cast(coalesce(cl.SourceDescription, cpi.SourceDescription) as java.lang.String),'')) like :sourceDescription");
            hql.setParameterLike("sourceDescription", pItemName);
        }

        hql.append("order by");
        if (!StringUtils.isEmpty(orderBy)) {
            if (orderBy.equals("token")) {
                hql.append("info.Token");
            } else if (orderBy.equals("payrollItemId")) {
                hql.append("cast (coalesce(cl.SourceId, cpi.SourcePayrollItemId) as int)");
            } else if (orderBy.equals("payrollItemName")) {
                hql.append("coalesce(cl.SourceDescription, cpi.SourceDescription)");
            }
            if (descending) {
                hql.append("desc");
            }
        } else {
            hql.append("info.Token desc");
        }
        //break ties
        hql.append(", cast(cl.SourceId as int), cast(cpi.SourcePayrollItemId as int), info.Id");

        List<QbdtPayrollItemInfo> payrollItemInfos = hql.list();

        ArrayList<SAPDataSyncDetailPayrollItem> returnList = new ArrayList<SAPDataSyncDetailPayrollItem>();

        for (QbdtPayrollItemInfo info : payrollItemInfos.subList(firstResult, Math.min(payrollItemInfos.size(), firstResult + maxResults))) {
            returnList.add(TaxTranslator.getSapDataSyncDetailPayrollItem(info));
        }

        return new SAPSearchResults<SAPDataSyncDetailPayrollItem>(payrollItemInfos.size(), returnList);
    }

    private SAPSearchResults<SAPDataSyncDetailPaycheck> getPaycheckDataSyncDetails(Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName,
                                                                                   String orderBy, boolean descending, int firstResult, int maxResults) {

        boolean hasNonNumericSourceIds = hasNonNumericSourceIds(company.getSourceSystemCd().toString(), company.getSourceCompanyId());

        HqlBuilder hql = new HqlBuilder(true, "select pc from com.intuit.sbd.payroll.psp.domain.Paycheck pc "
                + "join fetch pc.QbdtPaycheckInfoSet qbps "
                + "join fetch pc.PayrollRun "
                + "join fetch pc.SourceEmployee "
                + "where qbps.Token > -2 "
                + "and qbps.IsAssisted = true "
                + "and pc.Company = :company");
        hql.setParameter("company", company);

        if (idSearchType != null) {
            switch (idSearchType) {
                case EmployeeID:
                    hql.append("and cast(pc.SourceEmployee.SourceEmployeeId as int) between :fromId and :toId");
                    if (hasNonNumericSourceIds) {
                        hql.setParameter("fromId", Integer.toString(fromId));
                        hql.setParameter("toId", Integer.toString(toId));
                    } else {
                        hql.setParameter("fromId", fromId, new IntegerType());
                        hql.setParameter("toId", toId, new IntegerType());
                    }
                    break;
                case PayrollItemID:
                case TransactionID:
                    return new SAPSearchResults<SAPDataSyncDetailPaycheck>();
                case Token:
                    hql.append("and cast(qbps.Token as int) between :fromId and :toId");
                    hql.setParameter("fromId", fromId, new IntegerType());
                    hql.setParameter("toId", toId, new IntegerType());
                    break;
                case PaycheckID:
                    hql.append("and cast(pc.SourcePaycheckId as int) between :fromId and :toId");
                    hql.setParameter("fromId", fromId, new IntegerType());
                    hql.setParameter("toId", toId, new IntegerType());
                    break;
            }
        }
        if (type != null) {
            switch (type) {
                case Paychecks:
                    if (fromCalendar != null) {
                        hql.append("and pc.PayrollRun.PaycheckDate >= :fromDate");
                        hql.setParameter("fromDate", fromCalendar);
                    }
                    if (toCalendar != null) {
                        hql.append("and pc.PayrollRun.PaycheckDate <= :toDate");
                        hql.setParameter("toDate", toCalendar);
                    }
                    break;
                case Employees:
                case PayrollTxns:
                case PayrollItems:
                    return new SAPSearchResults<SAPDataSyncDetailPaycheck>();
                case ItemsStopped:
                    hql.append("and qbps.Token = -1");
                    if (fromCalendar != null) {
                        hql.append("and qbps.ModifiedDate >= :fromDate");
                        hql.setParameter("fromDate", fromCalendar);
                    }
                    if (toCalendar != null) {
                        hql.append("and qbps.ModifiedDate <= :toDate");
                        hql.setParameter("toDate", toCalendar);
                    }
                    break;
            }
        }

        if (!StringUtils.isEmpty(checkNumber)) {
            hql.append("and lower(COALESCE(cast(qbps.CheckNumber as java.lang.String),'')) like :checkNumber");
            hql.setParameterLike("checkNumber", checkNumber);
        }
        if (amount != null) {
            hql.append("and pc.NetAmount = :amount");
            hql.setParameter("amount", amount);
        }
        if (!StringUtils.isEmpty(pItemName)) {
            return new SAPSearchResults<SAPDataSyncDetailPaycheck>();
        }

        String employeeIdOrderBy = hasNonNumericSourceIds ? "pc.SourceEmployee.SourceEmployeeId" : "cast (pc.SourceEmployee.SourceEmployeeId as int)";

        hql.append("order by");
        if (!StringUtils.isEmpty(orderBy)) {
            if (orderBy.equals("token")) {
                hql.append("qbps.Token");
            } else if (orderBy.equals("paycheckId")) {
                hql.append("cast (pc.SourcePaycheckId as int)");
            } else if (orderBy.equals("paycheckType")) {
                hql.append("pc.IsYTDAdjustment");
            } else if (orderBy.equals("employeeId")) {
                hql.append(employeeIdOrderBy);
            } else if (orderBy.equals("employeeName")) {
                hql.append("pc.SourceEmployee.LastName");
                if (descending) {
                    hql.append("desc");
                }
                hql.append(", pc.SourceEmployee.FirstName");
            } else if (orderBy.equals("checkDate")) {
                hql.append("pc.PayrollRun.PaycheckDate");
            } else if (orderBy.equals("checkNumber")) {
                hql.append("qbps.CheckNumber");
            } else if (orderBy.equals("amount")) {
                hql.append("pc.NetAmount");
            }
            if (descending) {
                hql.append("desc");
            }
        } else {
            hql.append("qbps.Token desc");
        }
        //break ties
        hql.append(", cast (pc.SourcePaycheckId as int), pc.Id");

        List<Paycheck> paychecks = hql.list();

        ArrayList<SAPDataSyncDetailPaycheck> returnList = new ArrayList<SAPDataSyncDetailPaycheck>();

        for (Paycheck paycheck : paychecks.subList(firstResult, Math.min(paychecks.size(), firstResult + maxResults))) {
            returnList.add(TaxTranslator.getSapDataSyncDetailPaycheck(paycheck));
        }

        return new SAPSearchResults<SAPDataSyncDetailPaycheck>(paychecks.size(), returnList);
    }

    private SAPSearchResults<SAPDataSyncDetailPayrollTransaction> getPayrollTransactionDataSyncDetails(Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName,
                                                                                                       final String orderBy, final boolean descending, int firstResult, int maxResults) {

        ArrayList<SAPDataSyncDetailPayrollTransaction> payrollTransactions = new ArrayList<SAPDataSyncDetailPayrollTransaction>();
        payrollTransactions.addAll(getPriorPaymentsAndRefunds(company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName));
        payrollTransactions.addAll(getLiabilityAdjustments(company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName));
        payrollTransactions.addAll(getLiabilityChecks(company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName));
        payrollTransactions.addAll(getQBDTOnlyPayrollTransactions(QbdtPayrollTransactionType.DDReturn, company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName));
        payrollTransactions.addAll(getQBDTOnlyPayrollTransactions(QbdtPayrollTransactionType.FundsTransfer, company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName));
        payrollTransactions.addAll(getQBDTOnlyPayrollTransactions(QbdtPayrollTransactionType.LiabilityCheck, company, idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName));

        Collections.sort(payrollTransactions, new Comparator<SAPDataSyncDetailPayrollTransaction>() {
            public int compare(SAPDataSyncDetailPayrollTransaction o1, SAPDataSyncDetailPayrollTransaction o2) {

                int compare = 0;
                if (!StringUtils.isEmpty(orderBy)) {
                    if (orderBy.equals("token")) {
                        compare = compareInt(o1.getToken(), o2.getToken());
                    } else if (orderBy.equals("payrollTransactionId")) {
                        compare = compareInt(o1.getPayrollTransactionId(), o2.getPayrollTransactionId());
                    } else if (orderBy.equals("payrollTransactionType")) {
                        compare = nullSafeNaturalCompare(o1.getPayrollTransactionType(), o2.getPayrollTransactionType());
                    } else if (orderBy.equals("employeeId")) {
                        compare = compareInt(o1.getEmployeeId(), o2.getEmployeeId());
                    } else if (orderBy.equals("employeeName")) {
                        compare = nullSafeNaturalCompare(o1.getEmployeeName(), o2.getEmployeeName());
                    } else if (orderBy.equals("transactionDate")) {
                        compare = nullSafeNaturalCompare(o1.getTransactionDate(), o2.getTransactionDate());
                    } else if (orderBy.equals("amount")) {
                        compare = nullSafeNaturalCompare(o1.getAmount(), o2.getAmount());
                    }

                    if (descending) {
                        compare *= -1;
                    }
                } else {
                    compare = -1 * compareInt(o1.getToken(), o2.getToken());
                }

                // break ties
                if (compare == 0) {
                    compare = nullSafeNaturalCompare(o1.getPayrollTransactionId(), o2.getPayrollTransactionId());
                }
                if (compare == 0) {
                    compare = nullSafeNaturalCompare(o1.getDetailId(), o2.getDetailId());
                }

                return compare;
            }

            private int compareInt(Integer i1, Integer i2) {

                return i1.compareTo(i2);
            }

            private int compareInt(String i1, String i2) {

                Integer nullCompare = nullCompare(i1, i2);
                if (nullCompare != null) {
                    return nullCompare;
                }

                if (!StringUtils.isNumeric(i1)) {
                    if (StringUtils.isNumeric(i2)) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    if (!StringUtils.isNumeric(i2)) {
                        return -1;
                    }
                }
                return compareInt(Integer.parseInt(i1), Integer.parseInt(i2));
            }

            private Integer nullCompare(Object o1, Object o2) {

                if (o1 == null) {
                    if (o2 == null) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    if (o2 == null) {
                        return -1;
                    }
                }
                return null;
            }

            private <T> int nullSafeNaturalCompare(Comparable<T> o1, T o2) {

                Integer nullCompare = nullCompare(o1, o2);
                if (nullCompare != null) {
                    return nullCompare;
                }
                return o1.compareTo(o2);
            }
        });

        return new SAPSearchResults<SAPDataSyncDetailPayrollTransaction>(payrollTransactions.size(),
                new ArrayList<SAPDataSyncDetailPayrollTransaction>(payrollTransactions.subList(firstResult, Math.min(payrollTransactions.size(), firstResult + maxResults))));
    }

    private Collection<SAPDataSyncDetailPayrollTransaction> getPriorPaymentsAndRefunds(Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName) {

        HqlBuilder hql = new HqlBuilder(true, "select distinct pps "
                + "from com.intuit.sbd.payroll.psp.domain.PriorPaymentSubmission pps "
                + "join fetch pps.QbdtTransactionInfoSet "
                + "left outer join fetch pps.QbdtPayrollTransactionSet "
                + "where pps.Company = :company");
        hql.setParameter("company", company);

        if (idSearchType != null) {
            switch (idSearchType) {
                case EmployeeID:
                case PayrollItemID:
                case PaycheckID:
                    return Collections.emptyList();
                case TransactionID:
                    hql.append("and cast(pps.SourceId as int) between :fromId and :toId");
                    hql.setParameter("fromId", fromId, new IntegerType());
                    hql.setParameter("toId", toId, new IntegerType());
                    break;
                case Token:
                    //in memory
                    break;
            }
        }
        if (type != null) {
            switch (type) {
                case Paychecks:
                case Employees:
                case PayrollItems:
                    return Collections.emptyList();
                case PayrollTxns:
                    if (fromCalendar != null) {
                        hql.append("and pps.CreatedDate >= :fromDate");
                        hql.setParameter("fromDate", fromCalendar);
                    }
                    if (toCalendar != null) {
                        hql.append("and pps.CreatedDate <= :toDate");
                        hql.setParameter("toDate", toCalendar);
                    }
                    break;
                case ItemsStopped:
                    //in memory
                    break;
            }
        }

        if (!StringUtils.isEmpty(checkNumber)) {
            return Collections.emptyList();
        }
        if (amount != null) {
            //do this in memory
        }
        if (!StringUtils.isEmpty(pItemName)) {
            return Collections.emptyList();
        }

        List<PriorPaymentSubmission> priorPaymentSubmissions = hql.list();

        List<SAPDataSyncDetailPayrollTransaction> returnList = new ArrayList<SAPDataSyncDetailPayrollTransaction>();

        for (PriorPaymentSubmission pps : priorPaymentSubmissions) {
            if ((pps.getQbdtPayrollTransaction() != null && pps.getQbdtPayrollTransaction().getTransactionType() == QbdtPayrollTransactionType.LiabilityCheck)) {
                //QuickBooks stores fake liability checks as PPS, so exclude them here.
                continue;
            }

            SAPDataSyncDetailPayrollTransaction sapDataSyncDetailPayrollTransaction = TaxTranslator.getSapDataSyncDetailPayrollTransaction(pps);

            if (sapDataSyncDetailPayrollTransaction.getToken() == -2) {
                continue;
            }
            if (idSearchType != null && idSearchType == IdSearchType.Token && (sapDataSyncDetailPayrollTransaction.getToken() < fromId || sapDataSyncDetailPayrollTransaction.getToken() > toId)) {
                continue;
            }
            if (amount != null && !SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(sapDataSyncDetailPayrollTransaction.getAmount()).equals(amount)) {
                continue;
            }
            if (type != null && type == TypeSearchType.ItemsStopped) {
                if (sapDataSyncDetailPayrollTransaction.getToken() != -1) {
                    continue;
                }
                if (fromCalendar != null && pps.getModifiedDate().before(fromCalendar)) {
                    continue;
                }
                if (toCalendar != null && pps.getModifiedDate().after(toCalendar)) {
                    continue;
                }
            }

            returnList.add(sapDataSyncDetailPayrollTransaction);

        }

        return returnList;
    }

    private Collection<SAPDataSyncDetailPayrollTransaction> getLiabilityAdjustments(Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName) {

        HqlBuilder hql = new HqlBuilder(true, "select distinct cas "
                + " from com.intuit.sbd.payroll.psp.domain.CompanyAdjustmentSubmission cas "
                + " join cas.QbdtTransactionInfoSet qtis "
                + "where cas.Company = :company "
                + "and qtis.Token != -2 "
                + "and cas.OriginalSubmission is null");
        hql.setParameter("company", company);

        List commonList = addCommonPayrollTransactionCriteria(hql, "cas", idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName);
        if (commonList != null) {
            return Collections.emptyList();
        }

        List<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = hql.list();

        List<SAPDataSyncDetailPayrollTransaction> returnList = new ArrayList<SAPDataSyncDetailPayrollTransaction>();

        for (CompanyAdjustmentSubmission cas : companyAdjustmentSubmissions) {
            returnList.add(TaxTranslator.getSapDataSyncDetailPayrollTransaction(cas));
        }

        return returnList;
    }

    private Collection<SAPDataSyncDetailPayrollTransaction> getLiabilityChecks(Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName) {

        HqlBuilder hql = new HqlBuilder(true, "select distinct lc from com.intuit.sbd.payroll.psp.domain.LiabilityCheck lc "
                + " join lc.QbdtTransactionInfoSet qtis "
                + "where lc.Company = :company "
                + "and qtis.Token != -2");
        hql.setParameter("company", company);

        List commonList = addCommonPayrollTransactionCriteria(hql, "lc", idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName);
        if (commonList != null) {
            return Collections.emptyList();
        }

        List<LiabilityCheck> liabilityChecks = hql.list();

        List<SAPDataSyncDetailPayrollTransaction> returnList = new ArrayList<SAPDataSyncDetailPayrollTransaction>();

        for (LiabilityCheck lc : liabilityChecks) {
            returnList.add(TaxTranslator.getSapDataSyncDetailPayrollTransaction(lc));
        }

        return returnList;
    }

    private Collection<SAPDataSyncDetailPayrollTransaction> getQBDTOnlyPayrollTransactions(QbdtPayrollTransactionType payrollTransactionType, Company company, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName) {

        HqlBuilder hql = new HqlBuilder(true, "select distinct pt from com.intuit.sbd.payroll.psp.domain.QbdtPayrollTransaction pt "
                + " join pt.QbdtTransactionInfoSet qtis "
                + "where pt.Company = :company "
                + "and pt.TransactionType = :transactionType "
                + "and qtis.Token != -2");
        hql.setParameter("company", company);
        hql.setParameter("transactionType", payrollTransactionType);

        List commonList = addCommonPayrollTransactionCriteria(hql, "pt", idSearchType, fromId, toId, type, fromCalendar, toCalendar, checkNumber, amount, pItemName);
        if (commonList != null) {
            return Collections.emptyList();
        }

        List<QbdtPayrollTransaction> payrollTransactions = hql.list();

        List<SAPDataSyncDetailPayrollTransaction> returnList = new ArrayList<SAPDataSyncDetailPayrollTransaction>();

        for (QbdtPayrollTransaction pt : payrollTransactions) {
            returnList.add(TaxTranslator.getSapDataSyncDetailPayrollTransaction(pt));
        }

        return returnList;
    }

    //returns either an empty list if the data determines there are no results or null if successfully added the HQL
    private List addCommonPayrollTransactionCriteria(HqlBuilder hql, String prefix, IdSearchType idSearchType, int fromId, int toId, TypeSearchType type, SpcfCalendar fromCalendar, SpcfCalendar toCalendar, String checkNumber, SpcfMoney amount, String pItemName) {

        String strPrefix = null;
        if (idSearchType != null) {
            switch (idSearchType) {
                case EmployeeID:
                case PayrollItemID:
                case PaycheckID:
                    return Collections.emptyList();
                case TransactionID:
                    hql.append("and cast(" + prefix + ".SourceId as int) between :fromId and :toId");
                    break;
                case Token:
                    hql.append("and cast(qtis.Token as int) between :fromId and :toId");
                    break;

            }
            hql.setParameter("fromId", fromId, new IntegerType());
            hql.setParameter("toId", toId, new IntegerType());
        }
        if (type != null) {
            switch (type) {
                case Paychecks:
                case Employees:
                case PayrollItems:
                    return Collections.emptyList();
                case PayrollTxns:
                    if (fromCalendar != null) {
                        hql.append("and " + prefix + ".CreatedDate >= :fromDate");
                        hql.setParameter("fromDate", fromCalendar);
                    }
                    if (toCalendar != null) {
                        hql.append("and " + prefix + ".CreatedDate <= :toDate");
                        hql.setParameter("toDate", toCalendar);
                    }
                    break;
                case ItemsStopped:
                    hql.append("and qtis.Token = -1");
                    if (fromCalendar != null) {
                        hql.append("and qtis.ModifiedDate >= :fromDate");
                        hql.setParameter("fromDate", fromCalendar);
                    }
                    if (toCalendar != null) {
                        hql.append("and qtis.ModifiedDate <= :toDate");
                        hql.setParameter("toDate", toCalendar);
                    }
                    break;
            }
        }

        if (!StringUtils.isEmpty(checkNumber)) {
            return Collections.emptyList();
        }
        if (amount != null) {
            hql.append("and " + prefix + ".Amount = :amount");
            hql.setParameter("amount", amount);
        }
        if (!StringUtils.isEmpty(pItemName)) {
            return Collections.emptyList();
        }

        return null;
    }

    @FlexMethod
    public SAPQBDTTokens getQBDTTokens(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId) throws Throwable {

        SAPQBDTTokens tokens = new SAPQBDTTokens();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }
            tokens.setHighToken(Long.toString(company.getCurrentToken()));
            tokens.setEmployeeNextId(StringUtils.defaultString(company.getNextEmployeeId()));
            tokens.setPaycheckNextId(StringUtils.defaultString(company.getNextPaycheckId()));
            tokens.setPayrollTxNextId(StringUtils.defaultString(company.getNextPayrollTransactionId()));
            tokens.setPayrollItemNextId(StringUtils.defaultString(company.getNextPayrollItemId()));

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return tokens;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AccessDataSyncTool)
    public void updatedDataSyncTokensOnSelectedItems(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId, SAPDataSyncItems items, String action, boolean undelete, String comment, String pCaseId) throws Throwable {

        try{
            ThreadLocalManager.setValue(pCaseId);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));

            UpdateDataSyncTokensDTO dto = new UpdateDataSyncTokensDTO();

            dto.setUndelete(undelete);
            dto.setComment(comment);
            dto.setAction(UpdateDataSyncTokensDTO.Action.valueOf(action));

            dto.setEmployees(getSpcfUniqueIdsFromStrings(items.getEmployeeIds()));
            dto.setPayrollItems(getSpcfUniqueIdsFromStrings(items.getPayrollItemIds()));
            dto.setPaychecks(getSpcfUniqueIdsFromStrings(items.getPaychecks()));
            dto.setPriorPaymentsAndRefunds(getSpcfUniqueIdsFromStrings(items.getPriorPayments()));
            dto.setLiabilityAdjustments(getSpcfUniqueIdsFromStrings(items.getLiabilityAdjustments()));
            dto.setLiabilityChecks(getSpcfUniqueIdsFromStrings(items.getLiabilityChecks()));
            dto.setQbdtOnlyPayrollTransactions(getSpcfUniqueIdsFromStrings(items.getQbdtPayrollTransactions()));

            ProcessResult pr = PayrollServices.companyManager.updateDataSyncTokens(SourceSystemCode.valueOf(sourceSystemCd), sourceCompanyId, dto);

            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating sync tokens on selected items", pr);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating sync tokens on selected items.", sourceSystemCd, sourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadLocalManager.flush();
        }
    }

    private Collection<SpcfUniqueId> getSpcfUniqueIdsFromStrings(Collection<String> strings) {

        Set<SpcfUniqueId> spcfUniqueIds = new HashSet<SpcfUniqueId>();
        for (String string : strings) {
            spcfUniqueIds.add(SpcfUniqueId.createInstance(string));
        }
        return spcfUniqueIds;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.AccessDataSyncTool)
    public void updateDataSyncTokens(String sourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId, String action, ArrayList<String> actions, String comment, String pCaseId) throws Throwable {

        try {
            ThreadLocalManager.setValue(pCaseId);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));

            UpdateDataSyncTokensDTO dto = new UpdateDataSyncTokensDTO();

            dto.setComment(comment);
            dto.setAction(UpdateDataSyncTokensDTO.Action.valueOf(action));

            dto.setEmployees(new HashSet<SpcfUniqueId>());
            dto.setPayrollItems(new HashSet<SpcfUniqueId>());
            dto.setPaychecks(new HashSet<SpcfUniqueId>());
            dto.setPriorPaymentsAndRefunds(new HashSet<SpcfUniqueId>());
            dto.setLiabilityAdjustments(new HashSet<SpcfUniqueId>());
            dto.setLiabilityChecks(new HashSet<SpcfUniqueId>());
            dto.setQbdtOnlyPayrollTransactions(new HashSet<SpcfUniqueId>());

            //create dates for determining prior year, current year, or neither.
            //note that this relies on the strange semantics in that hql/sql between is inclusive and greaterThan is exclusive.
            SpcfCalendar priorYearStart = PSPDate.getPSPTime();
            priorYearStart.addYears(-1);
            priorYearStart = CalendarUtils.getFirstDayOfTheYear(priorYearStart);

            SpcfCalendar priorYearEnd = PSPDate.getPSPTime();
            priorYearEnd = CalendarUtils.getFirstDayOfTheYear(priorYearEnd);
            priorYearEnd.addMilliseconds(-1);

            if (actions.contains("stopAllPayrollTxns") || actions.contains("stopAllData")) {
                addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company)));
            } else {
                if (actions.contains("stopAllPriorYearTxnsPaychecks") || actions.contains("pushAllPriorYearTxnsPaychecks")) {
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.QbdtPayrollTransaction().PeriodEndDate().between(priorYearStart, priorYearEnd))));
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.MoneyMovementTransaction().PaymentPeriodEnd().between(priorYearStart, priorYearEnd))));
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.LiabilityCheck().PeriodEndDate().between(priorYearStart, priorYearEnd))));
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.LiabilityAdjustment().PayrollRun().PaycheckDate().between(priorYearStart, priorYearEnd))));
                }
                if (actions.contains("pushCurrentYearPayrollTxns")) {
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.QbdtPayrollTransaction().PeriodEndDate().greaterThan(priorYearEnd))));
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.MoneyMovementTransaction().PaymentPeriodEnd().greaterThan(priorYearEnd))));
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.LiabilityAdjustment().PayrollRun().PaycheckDate().greaterThan(priorYearEnd))));
                    addPayrollTransactionsToDTO(dto, Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(company).And(QbdtTransactionInfo.LiabilityCheck().PeriodEndDate().greaterThan(priorYearEnd))));
                }
            }

            if (actions.contains("stopAllPaychecks") || actions.contains("stopAllData")) {
                addPaychecksToDTO(dto, Application.find(Paycheck.class, Paycheck.Company().equalTo(company)));
            } else {
                if (actions.contains("stopAllPriorYearTxnsPaychecks") || actions.contains("pushAllPriorYearTxnsPaychecks")) {
                    addPaychecksToDTO(dto, Application.find(Paycheck.class, Paycheck.Company().equalTo(company).And(Paycheck.PayrollRun().PaycheckDate().between(priorYearStart, priorYearEnd))));
                }
                if (actions.contains("pushCurrentYearPaychecks")) {
                    addPaychecksToDTO(dto, Application.find(Paycheck.class, Paycheck.Company().equalTo(company).And(Paycheck.PayrollRun().PaycheckDate().greaterThan(priorYearEnd))));
                }
            }

            if (actions.contains("stopAllEEsWithoutCurrentYearPayroll")) {
                addEmployeesToDTO(dto, Employee.findEmployeesWithoutCurrentYearPaychecks(company));
            } else if (actions.contains("stopAllEmployees") || actions.contains("pushEmployees") || actions.contains("stopAllData")) {
                addEmployeesToDTO(dto, Application.find(Employee.class, Employee.Company().equalTo(company).And(Employee.QbdtEmployeeInfo().isNotNull())));
            }

            if (actions.contains("stopAllPayrollItems") || actions.contains("pushPayrollItems") || actions.contains("stopAllData")) {
                addPayrollItemsToDTO(dto, Application.find(QbdtPayrollItemInfo.class, QbdtPayrollItemInfo.Company().equalTo(company)));
            }

            ProcessResult pr = PayrollServices.companyManager.updateDataSyncTokens(SourceSystemCode.valueOf(sourceSystemCd), sourceCompanyId, dto);

            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating sync tokens", pr);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating sync tokens.", sourceSystemCd, sourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadLocalManager.flush();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateERPenaltiesAndInterestRefunds)
    public List<SAPTransaction> findRefundTransactions(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId) throws Throwable {

        List<SAPTransaction> transactions = new ArrayList<SAPTransaction>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            //Get FTs with Transaction type = EmployerPenaltiesRefundCredit, EmployerInterestRefundCredit, EmployerPenaltiesRefundDebit, EmployerInterestRefundDebit
            DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findNonCancelledFinancialTransactions(company, TransactionTypeCode.EmployerPenaltiesRefundCredit,
                    TransactionTypeCode.EmployerInterestRefundCredit, TransactionTypeCode.EmployerPenaltiesRefundDebit, TransactionTypeCode.EmployerInterestRefundDebit);

            for (FinancialTransaction financialTransaction : financialTransactions) {
                transactions.add(TaxTranslator.getSAPTransaction(financialTransaction));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Refund transactions", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return transactions;

    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateCourtesyRefund)
    public List<SAPTransaction> findCourtesyRefundTransactions(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId) throws Throwable {

        List<SAPTransaction> transactions = new ArrayList<SAPTransaction>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            //Current code always creates these as ERCourtesyRefundCredit, but old code created them as EmployerFeeRefundCredit
            DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransactions(company, TransactionTypeCode.EmployerFeeRefundCredit, TransactionTypeCode.ERCourtesyRefundCredit).find(FinancialTransaction.PayrollRun().isNull());
            financialTransactions = financialTransactions.sort(FinancialTransaction.SettlementDate().Descending());
            for (FinancialTransaction financialTransaction : financialTransactions) {
                transactions.add(TaxTranslator.getSAPTransaction(financialTransaction));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Refund transactions", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return transactions;

    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateERPenaltiesAndInterestRefunds)
    public DomainEntitySet<FinancialTransaction> createPenaltiesAndInterestRefunds(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, double pPenaltiesRefundAmount, double pInterestRefundAmount, String pNote, String pSettlementTypeCd) throws Throwable {

        DomainEntitySet<FinancialTransaction> retrunObj = null;
        try {
            boolean penaltyAndInterestRefundLimitEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_PENALTY_AND_INTEREST_REFUNDS_LIMIT_ENABLED, true);
            PayrollServices.beginUnitOfWork();
            SpcfMoney penaltiesAmount = SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(pPenaltiesRefundAmount);
            SpcfMoney interestAmount = SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(pInterestRefundAmount);
            int defaultPenaltyRefundLimit = SystemParameter.findIntValue(SystemParameter.Code.DEFAULT_PENALTY_REFUND_AMOUNT, 500);
            int defaultInterestRefundLimit = SystemParameter.findIntValue(SystemParameter.Code.DEFAULT_INTEREST_REFUND_AMOUNT, 500);
            if (penaltyAndInterestRefundLimitEnabled) {
                if (penaltiesAmount.getIntegerPart() > defaultPenaltyRefundLimit &&
                        interestAmount.getIntegerPart() > defaultInterestRefundLimit) {
                    aeFactory.throwGenericException("The Penalty refund amount and Interest amount you have entered is higher than the permissible limit.\n" +
                            "Penalty amount should be less $" + defaultPenaltyRefundLimit + " and Interest amount should be less $" + defaultInterestRefundLimit + " to create the entry.");
                }
                if (penaltiesAmount.getIntegerPart() > defaultPenaltyRefundLimit) {
                    aeFactory.throwGenericException("The penalty refund amount you have entered is higher than the permissible limit. " +
                            "Enter an amount less than $" + defaultPenaltyRefundLimit + " to create the entry.");
                }
                if (interestAmount.getIntegerPart() > defaultInterestRefundLimit) {
                    aeFactory.throwGenericException("The interest refund amount you have entered is higher than the permissible limit. " +
                            "Enter an amount less than $" + defaultInterestRefundLimit + " to create the entry.");
                }
            }
            SettlementTypeDTO settlementType = SettlementTypeDTO.valueOf(pSettlementTypeCd);
            ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(SourceSystemCode.valueOf(pSourceSystemCd), pSourceSystemId,
                    penaltiesAmount, interestAmount, pNote, settlementType);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error creating Penalties and Interest Refund transactions", processResult);
            }
            retrunObj = processResult.getResult();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating Penalties and Interest Refund transactions", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return retrunObj;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateERPenaltiesAndInterestRefunds)
    public void createRefundDebit(String pFinancialTransactionId, String pNote, String pSettlementTypeCd,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            SettlementTypeDTO settlementType = SettlementTypeDTO.valueOf(pSettlementTypeCd);
            ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addRefundDebit(pFinancialTransactionId, pNote, settlementType);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error creating Refund Debit transaction", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating Refund Debit transaction", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateCourtesyRefund)
    public void createCourtesyRefund(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, double pRefundAmount, String pNote, String pSettlementTypeCd) throws Throwable {

        try {
            boolean courtesyRefundLimitEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_COURTESY_REFUNDS_LIMIT_ENABLED, true);
            PayrollServices.beginUnitOfWork();
            SpcfMoney refundAmount = SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(pRefundAmount);
            int defaultCourtesyRefundLimit = SystemParameter.findIntValue(SystemParameter.Code.DEFAULT_COURTESY_REFUND_AMOUNT, 500);
            if (courtesyRefundLimitEnabled && refundAmount.getIntegerPart() > defaultCourtesyRefundLimit) {
                aeFactory.throwGenericException("The courtesy refund amount you have entered is higher than the permissible limit. " +
                        "Enter an amount less than $"+defaultCourtesyRefundLimit+" to create the entry.");
            }
            SettlementTypeDTO settlementType = SettlementTypeDTO.valueOf(pSettlementTypeCd);
            ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(SourceSystemCode.valueOf(pSourceSystemCd), pSourceSystemId,
                    refundAmount, pNote, settlementType);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error creating Courtesy Refund transaction", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating Courtesy Refund transaction", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateManualLedgerEntry)
    public void createPendingTaxRefund(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPaymentId, String memo) throws Throwable {

        try {

            boolean isPendingPaymentRefundEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_PENDING_PAYMENT_REFUND_ENABLED, true);
            logger.info("Value of feature flag IS_PENDING_PAYMENT_REFUND_ENABLED is : " + isPendingPaymentRefundEnabled);

            PayrollServices.beginUnitOfWork();

            if (isPendingPaymentRefundEnabled) {
                logger.info("Starting Refund for the company: " + pSourceCompanyId);
                MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(pPaymentId));
                SpcfCalendar mPaymentDate = PSPDate.getPSPTime();
                SpcfCalendar mmtDate = mmt.getPaymentPeriodEnd();

                CustomerTaxPaymentDTO dto = new CustomerTaxPaymentDTO();
                dto.setApplyPayments(true);
                dto.setMemo(memo);
                dto.setPaymentDate(new DateDTO(mPaymentDate));
                dto.setPaymentTemplateId(mmt.getPaymentTemplate().getPaymentTemplateCd());
                dto.setYear(mmtDate.getYear());
                dto.setQuarter(CalendarUtils.getQuarterAsInt(mmtDate));
                dto.setImmediateCredit(true);
                dto.setPaymentAmounts(new HashMap<String, BigDecimal>());

                //Put the sum of all law amount in the map
                for (FinancialTransaction financialTransaction : mmt.getFinancialTransactionCollection()) {
                    String lawId = financialTransaction.getLaw().getLawId();
                    logger.info("law id: " + lawId);
                    if (StringUtils.isNotEmpty(lawId)) {
                        SpcfDecimal newLawAmount = dto.getPaymentAmounts().containsKey(lawId) ? SpcfUtils.convertToSpcfDecimal(dto.getPaymentAmounts().get(lawId)) : SpcfMoney.ZERO;
                        if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                            logger.info("Adding to law " + lawId + " to " + financialTransaction.getFinancialTransactionAmount());
                            newLawAmount = newLawAmount.add(financialTransaction.getFinancialTransactionAmount());
                        } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                            newLawAmount = newLawAmount.subtract(financialTransaction.getFinancialTransactionAmount());
                            logger.info("Subtracting to law " + lawId + " to " + financialTransaction.getFinancialTransactionAmount());
                        }
                        dto.getPaymentAmounts().put(financialTransaction.getLaw().getLawId(), SpcfUtils.convertToBigDecimal(newLawAmount));
                    }
                }
                logger.info("Size of map: " + dto.getPaymentAmounts().size());

                for (String lawId : dto.getPaymentAmounts().keySet()) {
                   logger.info("Law id is: " + lawId);
                   logger.info("Law id value is: " + dto.getPaymentAmounts().get(lawId));
                }

                //BigDecimal lawAmountSum = dto.getPaymentAmounts().values().stream().reduce(BigDecimal.ZERO, (a, b) -> new BigDecimal(String.valueOf(a.add(b))));
                BigDecimal lawAmountSum = BigDecimal.ZERO;
                for (BigDecimal value : dto.getPaymentAmounts().values()) {
                    lawAmountSum = lawAmountSum.add(value);
                }
                if (!lawAmountSum.equals(SpcfUtils.convertToBigDecimal(mmt.getMoneyMovementTransactionAmount()))) {
                    aeFactory.throwGenericException("Sum of law amounts " + lawAmountSum + " is not equal to refund amount " + mmt.getMoneyMovementTransactionAmount().toString());
                }

                ProcessResult pr = PayrollServices.financialTransactionManager.createPendingTaxRefund(SourceSystemCode.valueOf(pSourceSystemCd),
                        pSourceCompanyId, pPaymentId, dto);

                if (!pr.isSuccess()) {
                    aeFactory.throwGenericException("Error creating full refund", pr);
                } else {
                    PayrollServices.commitUnitOfWork();
                    logger.info("Refunded Successfully for the company: " + pSourceCompanyId);
                }
            } else {
                aeFactory.throwGenericException("Refunding Pending Payment is disabled.");
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating full refund", pSourceSystemCd, pSourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @FlexMethod
    @Operation(operationIds = OperationId.ViewOverpayments)
    public List<SAPTemplateQuarterAmount> getAgencyTaxRefundBreakdown(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId) throws Throwable {

        List<SAPTemplateQuarterAmount> amounts = new ArrayList<SAPTemplateQuarterAmount>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            Map<SpcfCalendar, Map<PaymentTemplate, Map<Law, SpcfMoney>>> ledgerAccountBalanceForAllTemplates = LedgerAccount.getLedgerAccountBalanceForAllTemplates(LedgerAccountCode.AgencyTaxRefund, company);
            Map<PaymentTemplate, Map<Integer, SpcfMoney>> annualMap = new HashMap<PaymentTemplate, Map<Integer, SpcfMoney>>();

            for (Map.Entry<SpcfCalendar, Map<PaymentTemplate, Map<Law, SpcfMoney>>> quarterEntry : ledgerAccountBalanceForAllTemplates.entrySet()) {
                for (Map.Entry<PaymentTemplate, Map<Law, SpcfMoney>> templateEntry : quarterEntry.getValue().entrySet()) {
                    SpcfDecimal amount = SpcfMoney.ZERO;
                    for (SpcfMoney lawAmount : templateEntry.getValue().values()) {
                        amount = amount.add(lawAmount);
                    }
                    PaymentTemplate template = templateEntry.getKey();
                    if (template.isRolledUpAnnually()) {
                        if (!annualMap.containsKey(template)) {
                            annualMap.put(template, new HashMap<Integer, SpcfMoney>());
                        }
                        if (!annualMap.get(template).containsKey(quarterEntry.getKey().getYear())) {
                            annualMap.get(template).put(quarterEntry.getKey().getYear(), SpcfMoney.ZERO);
                        }
                        annualMap.get(template).put(quarterEntry.getKey().getYear(), new SpcfMoney(annualMap.get(template).get(quarterEntry.getKey().getYear()).add(amount)));
                    } else if (!amount.isZero()) {
                        SAPTemplateQuarterAmount sapTemplateQuarterAmount = new SAPTemplateQuarterAmount();
                        sapTemplateQuarterAmount.setPaymentTemplateCd(template.getPaymentTemplateCd());
                        sapTemplateQuarterAmount.setQuarter(TaxTranslator.getSAPQuarter(quarterEntry.getKey()));
                        sapTemplateQuarterAmount.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount));
                        amounts.add(sapTemplateQuarterAmount);
                    }

                }
            }

            for (Map.Entry<PaymentTemplate, Map<Integer, SpcfMoney>> annualEntry : annualMap.entrySet()) {
                for (Map.Entry<Integer, SpcfMoney> annualYearEntry : annualEntry.getValue().entrySet()) {
                    if (!annualYearEntry.getValue().isZero()) {
                        SAPTemplateQuarterAmount sapTemplateQuarterAmount = new SAPTemplateQuarterAmount();
                        sapTemplateQuarterAmount.setPaymentTemplateCd(annualEntry.getKey().getPaymentTemplateCd());
                        sapTemplateQuarterAmount.setQuarter(new SAPQuarter(annualYearEntry.getKey(), 4));
                        sapTemplateQuarterAmount.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(annualYearEntry.getValue()));
                        sapTemplateQuarterAmount.setIsAnnual(true);
                        amounts.add(sapTemplateQuarterAmount);
                    }
                }

            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Refund transactions", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return amounts;

    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateTOR)
    public void createTORTransactions(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, SAPQuarter quarter) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar lastDayOfQuarter = quarter.getLastDayOfQuarter();
            CalendarUtils.clearTime(lastDayOfQuarter);
            ProcessResult processResult = PayrollServices.financialTransactionManager.addTORTransactions(SourceSystemCode.valueOf(pSourceSystemCd), pSourceSystemId, paymentTemplateCd, lastDayOfQuarter);

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error creating TOR transactions", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating TOR transactions", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditSUIRateCurrQTR)
    //find "normal" update rates--all laws for the template, but only this quarter, previous quarter, and next quarter
    public List<SAPQuarterLawRates> findEditableQuarterRates(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Map<SAPQuarter, SAPQuarterLawRates> quarterLawRates = new HashMap<SAPQuarter, SAPQuarterLawRates>();

            SAPQuarter currentQuarter = SAPQuarter.currentQuarter();
            quarterLawRates.put(currentQuarter, new SAPQuarterLawRates(currentQuarter));
            quarterLawRates.get(currentQuarter).setUnderBlackout(false); //not yet implemented

            SAPQuarter previousQuarter = currentQuarter.previousQuarter();
            quarterLawRates.put(previousQuarter, new SAPQuarterLawRates(previousQuarter));

            SAPQuarter nextQuarter = currentQuarter.nextQuarter();
            quarterLawRates.put(nextQuarter, new SAPQuarterLawRates(nextQuarter));

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

            for (CompanyLaw companyLaw : companyAgencyPaymentTemplate.getCompanyAgency().getCompanyLawCollection()
                    .find(CompanyLaw.Law().PaymentTemplate().equalTo(paymentTemplate))) {
                if (!companyLaw.getLaw().getIsEmployerTax() && !AuthUser.getLoggedInUser().hasOperation(OperationId.EditRatesOtherLaws)) {
                    continue;
                }
                for (SAPQuarter sapQuarter : quarterLawRates.keySet()) {
                    CompanyLawRate effectiveLawRate = CompanyLawRate.findEffectiveLawRate(companyLaw, sapQuarter.getFirstDayOfQuarter());
                    quarterLawRates.get(sapQuarter).getLawRates().add(TaxTranslator.getSAPLawRate(companyLaw.getLaw(), effectiveLawRate));
                }
            }

            ArrayList<SAPQuarterLawRates> sapQuarterLawRates = new ArrayList<SAPQuarterLawRates>(quarterLawRates.values());
            Collections.sort(sapQuarterLawRates, new Comparator<SAPQuarterLawRates>() {
                public int compare(SAPQuarterLawRates o1, SAPQuarterLawRates o2) {

                    return o1.getQuarter().compareTo(o2.getQuarter());
                }
            });
            for (SAPQuarterLawRates sapQuarterLawRate : sapQuarterLawRates) {
                Collections.sort(sapQuarterLawRate.getLawRates(), new Comparator<SAPLawRate>() {
                    public int compare(SAPLawRate o1, SAPLawRate o2) {

                        return o1.getLaw().getName().compareTo(o2.getLaw().getName());
                    }
                });
            }

            return sapQuarterLawRates;

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Editable Quarter Rates", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.RateSuperUser, OperationId.EditFilerType})
    public int getFirstTaxYear(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            SpcfCalendar serviceStartDate = company.getCompanyService(ServiceCode.Tax).getServiceStartDate();
            if (null != serviceStartDate) {
                return serviceStartDate.getYear();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error getting first tax year", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return 0;
    }

    @FlexMethod
    //find all the valid rates for the template, group by law
    @Operation(operationIds = OperationId.RateSuperUser)
    public List<SAPLawQuarterRates> findAllEditableRates(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            List<SAPLawQuarterRates> lawQuarterRates = new ArrayList<SAPLawQuarterRates>();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

            for (CompanyLaw companyLaw : companyAgencyPaymentTemplate.getCompanyAgency().getCompanyLawCollection()
                    .find(CompanyLaw.Law().PaymentTemplate().equalTo(paymentTemplate)
                            .And(CompanyLaw.AdditionalCompanyLaw().isNull()))
                    .sort(CompanyLaw.Law().LawAbbrev())) {
                SAPLawQuarterRates sapLawQuarterRates = new SAPLawQuarterRates();
                sapLawQuarterRates.setLaw(TaxTranslator.getLawItemsFromDomainEntity(companyLaw.getLaw()));
                sapLawQuarterRates.setRates(new ArrayList<SAPQuarterRate>());
                for (CompanyLawRate companyLawRate : companyLaw.getCompanyLawRateCollection()
                        .find(CompanyLawRate.InvalidDate().isNull())
                        .sort(CompanyLawRate.EffectiveDate())) {
                    sapLawQuarterRates.getRates().add(TaxTranslator.getSAPQuarterRate(companyLawRate));
                }
                lawQuarterRates.add(sapLawQuarterRates);
            }

            return lawQuarterRates;

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding All Editable Rates", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditSUIRateCurrQTR)
    public void updateRates(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, SAPQuarterLawRates rates, boolean pushToQuickbooks) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            //paranoid rechecking of operations for the benefit of DIS adapter
            if (rates.getQuarter().compareTo(SAPQuarter.currentQuarter()) != 0) {
                if (rates.getQuarter().compareTo(SAPQuarter.currentQuarter().nextQuarter()) != 0
                        && rates.getQuarter().compareTo(SAPQuarter.currentQuarter().previousQuarter()) != 0) {
                    aeFactory.throwGenericException(QUARTER_OUT_OF_RANGE_EXCEPTION_STRING);
                }
                requireAdditionalOperation(OperationId.EditRatesInOtherQTRs);
            }
            if (!pushToQuickbooks) {
                requireAdditionalOperation(OperationId.RateSuperUser);
            }

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            List<ProcessResult> prList = new ArrayList<ProcessResult>();
            for (SAPLawRate rate : rates.getLawRates()) {
                if (!Double.isNaN(rate.getNewPercentage()) && rate.getNewPercentage() != rate.getCurrentPercentage()) {
                    Law law = Application.findById(Law.class, rate.getLaw().getLawId());

                    if (law.getPaymentTemplate().getCategory() != PaymentTemplateCategory.SUI || !law.getIsEmployerTax()) {
                        requireAdditionalOperation(OperationId.EditRatesOtherLaws);
                    }
                    LawRateRange lawRateRange = law.getLawRateRange();
                    if (lawRateRange != null) {
                        if (lawRateRange.getMinRate() != null && rate.getNewPercentage() < SAPTranslator.getDoubleFromSpcfMoneyNullZero(lawRateRange.getMinRate()) * 100) {
                            requireAdditionalOperation(OperationId.RateSuperUser);
                        }
                        if (lawRateRange.getMaxRate() != null && rate.getNewPercentage() > SAPTranslator.getDoubleFromSpcfMoneyNullZero(lawRateRange.getMaxRate()) * 100) {
                            requireAdditionalOperation(OperationId.RateSuperUser);
                        }
                    }

                    prList.add(PayrollServices.companyManager.updateCompanyLawRate(company.getSourceSystemCd(),
                            company.getSourceCompanyId(),
                            law,
                            rates.getQuarter().getFirstDayOfQuarter(),
                            rate.getNewPercentage() / 100.,
                            pushToQuickbooks));
                }
            }

            if (!aeFactory.errorsOccurred(prList)) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating rates", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating rates", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void requireAdditionalOperation(OperationId operation) throws Throwable {

        if (!AuthUser.getLoggedInUser().hasOperation(operation)) {
            aeFactory.throwGenericException(operation.name() + " is required");
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RateSuperUser)
    public void updateAllRates(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, List<SAPLawQuarterRates> rates, boolean pushToQuickbooks) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            List<ProcessResult> prList = new ArrayList<ProcessResult>();
            for (SAPLawQuarterRates rate : rates) {
                List<CompanyLawRateDTO> lawRates = new ArrayList<CompanyLawRateDTO>();
                for (SAPQuarterRate sapQuarterRate : rate.getRates()) {
                    CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
                    SpcfCalendar effectiveDate = sapQuarterRate.getQuarter().getFirstDayOfQuarter();
                    companyLawRateDTO.setEffectiveDate(new DateDTO(effectiveDate));
                    companyLawRateDTO.setRate(sapQuarterRate.getNewPercentage() / 100.);
                    lawRates.add(companyLawRateDTO);
                }

                CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, rate.getLaw().getLawId());

                prList.add(PayrollServices.companyManager.addOrUpdateCompanyLawRates(SourceSystemCode.valueOf(pSourceSystemCd), pSourceSystemId, companyLaw.getSourceId(), lawRates, pushToQuickbooks));
            }

            if (!aeFactory.errorsOccurred(prList)) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating rates", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating rates", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditFilingAmts)
    public List<SAPQuarterCompanyFilingAmounts> findEditableAdditionalFilingAmounts(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Map<SAPQuarter, SAPQuarterCompanyFilingAmounts> quarterAdditionalFilingAmounts = new HashMap<SAPQuarter, SAPQuarterCompanyFilingAmounts>();

            SAPQuarter currentQuarter = SAPQuarter.currentQuarter();
            quarterAdditionalFilingAmounts.put(currentQuarter, new SAPQuarterCompanyFilingAmounts(currentQuarter));

            SAPQuarter previousQuarter = currentQuarter.previousQuarter();
            quarterAdditionalFilingAmounts.put(previousQuarter, new SAPQuarterCompanyFilingAmounts(previousQuarter));

            SAPQuarter nextQuarter = currentQuarter.nextQuarter();
            quarterAdditionalFilingAmounts.put(nextQuarter, new SAPQuarterCompanyFilingAmounts(nextQuarter));

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

            for (AdditionalFilingAmount additionalFilingAmount : paymentTemplate.getAdditionalFilingAmountCollection()) {
                for (SAPQuarter sapQuarter : quarterAdditionalFilingAmounts.keySet()) {
                    CompanyFilingAmount companyFilingAmount = companyAgencyPaymentTemplate.getCompanyFilingAmount(additionalFilingAmount, sapQuarter.getFirstDayOfQuarter());
                    if (companyFilingAmount != null) {
                        quarterAdditionalFilingAmounts.get(sapQuarter).getAmounts().add(TaxTranslator.getCompanyFilingAmount(companyFilingAmount));
                    } else {
                        quarterAdditionalFilingAmounts.get(sapQuarter).getAmounts().add(TaxTranslator.getCompanyFilingAmount(additionalFilingAmount));
                    }
                }
            }

            ArrayList<SAPQuarterCompanyFilingAmounts> sapQuarterCompanyFilingAmounts = new ArrayList<SAPQuarterCompanyFilingAmounts>(quarterAdditionalFilingAmounts.values());
            Collections.sort(sapQuarterCompanyFilingAmounts, new Comparator<SAPQuarterCompanyFilingAmounts>() {
                public int compare(SAPQuarterCompanyFilingAmounts o1, SAPQuarterCompanyFilingAmounts o2) {

                    return o1.getQuarter().compareTo(o2.getQuarter());
                }
            });
            for (SAPQuarterCompanyFilingAmounts sapQuarterCompanyFilingAmount : sapQuarterCompanyFilingAmounts) {
                Collections.sort(sapQuarterCompanyFilingAmount.getAmounts(), new Comparator<SAPCompanyFilingAmount>() {
                    public int compare(SAPCompanyFilingAmount o1, SAPCompanyFilingAmount o2) {

                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }

            return sapQuarterCompanyFilingAmounts;

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Editable Additional Filing Amounts", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditFilingAmts)
    public void updateAdditionalFilingAmounts(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, SAPQuarterCompanyFilingAmounts amounts) throws Throwable {
        //set based on the "newValue"
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            List<ProcessResult> prList = new ArrayList<ProcessResult>();
            for (SAPCompanyFilingAmount amount : amounts.getAmounts()) {
                if (StringUtils.isNotEmpty(amount.getNewValue())) {
                    CompanyFilingAmountDTO companyFilingAmountDTO = new CompanyFilingAmountDTO();
                    double amountValue = Double.parseDouble(amount.getNewValue());
                    if (amount.getIsRate()) {
                        amountValue /= 100.;
                    }
                    companyFilingAmountDTO.setAmount(amountValue);
                    companyFilingAmountDTO.setEffectiveDate(new DateDTO(amounts.getQuarter().getFirstDayOfQuarter()));
                    companyFilingAmountDTO.setName(amount.getName());

                    prList.add(PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyFilingAmountDTO));
                }
            }

            if (!aeFactory.errorsOccurred(prList)) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating filing amounts", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating filing amounts", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditDepositFreq)
    public SAPDepositFrequencyCollection getAllDepositFrequencies(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd) throws Throwable {

        SAPDepositFrequencyCollection depositFrequencyCollection = new SAPDepositFrequencyCollection();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

            depositFrequencyCollection.setAvailableFrequencies(new ArrayList<String>());
            List<DepositFrequencyCode> activeFrequencies = PaymentTemplateFrequency.findActiveFrequencies(paymentTemplate);

            for (DepositFrequencyCode supportedFrequency : activeFrequencies) {
                depositFrequencyCollection.getAvailableFrequencies().add(supportedFrequency.toString());
            }

            depositFrequencyCollection.setDefaultDepositFrequency(paymentTemplate.getDefaultDepositFrequency());

            depositFrequencyCollection.setDepositFrequencies(new ArrayList<SAPDepositFrequency>());
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);
            for (EffectiveDepositFrequency effectiveDepositFrequency : companyAgencyPaymentTemplate.getEffectiveDepositFrequencyCollection()
                    .find(EffectiveDepositFrequency.InvalidDate().isNull())
                    .sort(EffectiveDepositFrequency.EffectiveDate())) {
                depositFrequencyCollection.getDepositFrequencies().add(TaxTranslator.getDepositFrequencyFromDomainEntity(effectiveDepositFrequency));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding deposit frequencies", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return depositFrequencyCollection;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditDepositFreq)
    public void updateDepositFrequencies(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, List<SAPDepositFrequency> depositFrequencies) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

            List<ProcessResult> prList = new ArrayList<ProcessResult>();

            updateDepositFrequencies(prList, company, companyAgencyPaymentTemplate, depositFrequencies);

            if (paymentTemplateCd.equals(PaymentTemplate.NY_WH)) { //Special rule: copy NY WH=>Metro
                CompanyAgencyPaymentTemplate metroPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));
                if (metroPaymentTemplate != null) {
                    updateDepositFrequencies(prList, company, metroPaymentTemplate, depositFrequencies);
                }
            }

            if (!aeFactory.errorsOccurred(prList)) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating Deposit Frequencies", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating Deposit Frequencies", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void updateDepositFrequencies(List<ProcessResult> prList, Company company, CompanyAgencyPaymentTemplate capt, List<SAPDepositFrequency> depositFrequencies) {
        //todo this logic should be in core process
        Set<SpcfCalendar> currentEffectiveDates = new HashSet<SpcfCalendar>();
        for (SAPDepositFrequency depositFrequency : depositFrequencies) {
            SpcfCalendar effectiveDate = SAPTranslator.getSpcfCalendarFromDate(depositFrequency.getEffectiveDate());
            currentEffectiveDates.add(effectiveDate);

            if (depositFrequency.getObsoleteFrequency() != null) {
                //if sending over an obsolete DF, don't try to do anything with it
                continue;
            }

            EffectiveDepositFrequencyDTO effectiveDepositFrequencyDTO = new EffectiveDepositFrequencyDTO();
            effectiveDepositFrequencyDTO.setAgencyId(capt.getPaymentTemplate().getAgency().getAgencyId());
            effectiveDepositFrequencyDTO.setEffectiveDate(effectiveDate);
            effectiveDepositFrequencyDTO.setPaymentFrequencyId(DepositFrequencyCode.valueOf(depositFrequency.getDepositFrequency()));
            effectiveDepositFrequencyDTO.setPaymentTemplateCd(capt.getPaymentTemplate().getPaymentTemplateCd());
            prList.add(PayrollServices.paymentManager.updateDepositFrequency(company.getSourceSystemCd(), company.getSourceCompanyId(), effectiveDepositFrequencyDTO));
        }

        DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequenciesToInvalidate =
                capt.getEffectiveDepositFrequencyCollection()
                        .find(EffectiveDepositFrequency.EffectiveDate().notIn(currentEffectiveDates.toArray(new SpcfCalendar[currentEffectiveDates.size()]))
                                .And(EffectiveDepositFrequency.InvalidDate().isNull()));
        for (EffectiveDepositFrequency effectiveDepositFrequency : effectiveDepositFrequenciesToInvalidate) {
            EffectiveDepositFrequencyDTO effectiveDepositFrequencyDTO = PayrollServices.dtoFactory.create(effectiveDepositFrequency);
            prList.add(PayrollServices.paymentManager.invalidateDepositFrequency(company.getSourceSystemCd(), company.getSourceCompanyId(), effectiveDepositFrequencyDTO));
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditAgencyIDs)
    //the SAPCompanyAgencyPaymentTemplateAgencyId with a name of null is the "main Agency ID"
    public List<SAPCompanyAgencyPaymentTemplateAgencyId> findAgencyIDs(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd) throws Throwable {

        List<SAPCompanyAgencyPaymentTemplateAgencyId> agencyIds = new ArrayList<SAPCompanyAgencyPaymentTemplateAgencyId>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

            agencyIds.add(TaxTranslator.getMainAgencyIdFromDomainEntity(companyAgencyPaymentTemplate));

            for (PaymentTemplateAgencyId additionalAgencyId : companyAgencyPaymentTemplate.getPaymentTemplate().getAgencyIds()
                    .sort(PaymentTemplateAgencyId.Name())) {
                CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId = companyAgencyPaymentTemplate.getCompanyPaymentTemplateAgencyIdCollection()
                        .find(CompanyPaymentTemplateAgencyId.Name().equalTo(additionalAgencyId.getName())).getFirst();

                if (companyPaymentTemplateAgencyId != null) {
                    agencyIds.add(TaxTranslator.getAdditionalAgencyIdFromDomainEntity(companyPaymentTemplateAgencyId));
                } else {
                    agencyIds.add(TaxTranslator.getMissingAdditionalAgencyId(additionalAgencyId.getName()));
                }
            }

            buildAgencyIDRequirements(agencyIds, companyAgencyPaymentTemplate);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding agency IDs", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return agencyIds;
    }

    private void buildAgencyIDRequirements(List<SAPCompanyAgencyPaymentTemplateAgencyId> agencyIds, CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate) {

        for (SAPCompanyAgencyPaymentTemplateAgencyId agencyId : agencyIds) {
            agencyId.setPaymentMethodRequirements(new ArrayList<SAPPaymentMethodAgencyIdRequirements>());
            for (PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplatePaymentMethods()
                    .sort(PaymentTemplatePaymentMethod.PaymentMethod())) {
                CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod = companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethod(paymentTemplatePaymentMethod.getPaymentMethod());
                for (PaymentMethodRequirement paymentMethodRequirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
                    if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                        AgencyIdRequirement agencyIdRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                        if (matches(agencyId, agencyIdRequirement)) {
                            String idToTest = agencyId.getId();

                            SAPAgencyIdRequirement patternRequirement = new SAPAgencyIdRequirement();
                            patternRequirement.setIsFulfilled(agencyIdRequirement.matchesPattern(idToTest));
                            if (StringUtils.isEmpty(agencyIdRequirement.getPattern())) {

                                if (!agencyIdRequirement.getRequired() && MA_WH.equals(paymentTemplatePaymentMethod.getPaymentTemplate().getPaymentTemplateCd())) {
                                    patternRequirement.setRequirementString(String.format("Agency ID not required. Format must be %s.", agencyIdRequirement.getExample()));
                                    patternRequirement.setIsFulfilled(Boolean.TRUE);

                                } else {
                                    patternRequirement.setRequirementString(String.format("%s must be set",
                                            agencyId.getName() != null ? agencyId.getName() : "Agency ID"));



                                }
                            } else {
                                patternRequirement.setRequirementString(String.format("%s must be in format, \"%s\" (Example: %s)",
                                        agencyId.getName() != null ? agencyId.getName() : "Agency ID",
                                        agencyIdRequirement.getPattern(),
                                        agencyIdRequirement.getExample()));
                            }

                            agencyId.addOrGetRequirementsForPaymentMethod(paymentTemplatePaymentMethod.getPaymentMethod()).getRequirements().add(patternRequirement);

                            if (agencyIdRequirement.getCustomRequirement() != AgencyIdCustomRequirement.None) {
                                SAPAgencyIdRequirement customRequirement = new SAPAgencyIdRequirement();
                                customRequirement.setIsFulfilled(agencyIdRequirement.meetsCustomRequirements(idToTest, companyAgencyPaymentTemplate.getCompanyAgency().getCompany().getFedTaxId()));
                                customRequirement.setRequirementString(agencyIdRequirement.getCustomRequirementDescription());
                                agencyId.addOrGetRequirementsForPaymentMethod(paymentTemplatePaymentMethod.getPaymentMethod()).getRequirements().add(customRequirement);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean matches(SAPCompanyAgencyPaymentTemplateAgencyId sapAgencyId, AgencyIdRequirement requirement) {

        if (requirement.getPaymentTemplateAgencyId() == null) {
            return sapAgencyId.getName() == null;
        } else {
            return (requirement.getPaymentTemplateAgencyId().getName().equals(sapAgencyId.getName()));
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditAgencyIDs)
    public List<SAPCompanyAgencyPaymentTemplateAgencyId> checkAgencyIDs(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, List<SAPCompanyAgencyPaymentTemplateAgencyId> agencyIds) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

            buildAgencyIDRequirements(agencyIds, companyAgencyPaymentTemplate);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error checking agency IDs", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return agencyIds;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditAgencyIDs)
    public void updateAgencyIDs(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, List<SAPCompanyAgencyPaymentTemplateAgencyId> agencyIds) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            CompanyAgencyPaymentTemplate nyMetroCapt = null;
            if (paymentTemplate.getPaymentTemplateCd().equals(PaymentTemplate.NY_WH)) { //special rule: copy NY WH->NY Metro
                nyMetroCapt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));
            }

            List<ProcessResult> prList = new ArrayList<ProcessResult>();
            for (SAPCompanyAgencyPaymentTemplateAgencyId agencyId : agencyIds) {
                if (agencyId.getName() == null) {
                    CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);
                    updateAID(prList, company, capt, agencyId.getId());
                    if (nyMetroCapt != null) {  //special rule: copy NY WH->NY Metro, if NY Metro is present
                        updateAID(prList, company, nyMetroCapt, agencyId.getId());
                    }

                } else {
                    AgencyIdDTO agencyIdDTO = new AgencyIdDTO();
                    agencyIdDTO.setPaymentTemplateCd(paymentTemplateCd);
                    agencyIdDTO.setIdName(agencyId.getName());
                    agencyIdDTO.setAgencyTaxpayerId(agencyId.getId());

                    prList.add(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));

                    if (nyMetroCapt != null) { //special rule: copy NY WH->NY Metro, if NY Metro is present
                        agencyIdDTO.setPaymentTemplateCd(PaymentTemplate.NY_METRO);
                        prList.add(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
                    }
                }


                PaymentMethod paymentMethod = paymentTemplate.getAgentEnabledRequiredPaymentMethod();
                if (paymentMethod != null) {
                    PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplateCd,
                            paymentMethod, /*agentEnabled*/ false);
                }
            }


            /*Ny-1MN the ACH credit should be automatically set to true when state access code
             is entered and valid agency id is present.
             It should be overidden by manual link.*/

            if (paymentTemplate.getPaymentTemplateCd().equals(PaymentTemplate.NY_WH)) {
                PaymentMethod paymentMethod = paymentTemplate.getAgentEnabledRequiredPaymentMethod();
                if (paymentMethod != null) {
                    for (SAPCompanyAgencyPaymentTemplateAgencyId agencyId : agencyIds) {
                        if (agencyId.getName() != null && agencyId.getName().equals("State Access Code") && (agencyId.getId() != null && !agencyId.getId().equals(""))) {
                            PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplateCd,
                                    paymentMethod, /*agentEnabled*/ true);
                            if (nyMetroCapt != null) {
                                //special rule: copy NY WH->NY Metro, if NY Metro is present
                                PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.NY_METRO,
                                        paymentMethod, /*agentEnabled*/ true);
                            }
                        }
                    }
                }
            }
            if (paymentTemplate.getPaymentTemplateCd().equals(PaymentTemplate.PA_WH)) {
                PaymentMethod paymentMethod = paymentTemplate.getAgentEnabledRequiredPaymentMethod();
                if (paymentMethod != null) {
                    for (SAPCompanyAgencyPaymentTemplateAgencyId agencyId : agencyIds) {
                            if (agencyId.getId() != null && !agencyId.getId().equals("") && agencyId.getPaymentMethodRequirements().get(0).getRequirements().get(0).getIsFulfilled()) {
                                PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplateCd,
                                        paymentMethod, /*agentEnabled*/ true);
                            } else {
                                PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplateCd,
                                        paymentMethod, /*agentEnabled*/ false);
                            }
                        }
                    }
                }
            if (!aeFactory.errorsOccurred(prList)) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating AIDs", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating AIDs", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void updateAID(List<ProcessResult> prList, Company company, CompanyAgencyPaymentTemplate capt, String id) {

        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(capt.getCompanyAgency());
        companyAgencyDTO.getCompanyAgencyPaymentTemplate(capt.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId(id);
        prList.add(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), capt.getCompanyAgency().getAgency().getAgencyId(), companyAgencyDTO));
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditFilingFlags)
    public List<SAPLawFlags> getAllLawFlags(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd) throws Throwable {

        List<SAPLawFlags> lawFlags = new ArrayList<SAPLawFlags>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

            for (CompanyLaw companyLaw : companyAgencyPaymentTemplate.getCompanyAgency().getCompanyLawCollection()
                    .find(CompanyLaw.Law().PaymentTemplate().equalTo(paymentTemplate)
                            .And(CompanyLaw.AdditionalCompanyLaw().isNull()))
                    .sort(CompanyLaw.Law().LawAbbrev())) {
                SAPLawFlags lawFlag = new SAPLawFlags();
                lawFlag.setLaw(TaxTranslator.getLawItemsFromDomainEntity(companyLaw.getLaw()));
                lawFlag.setInactive(companyLaw.getFilingStatus() == PayrollItemStatus.Inactive);
                lawFlag.setExempt(companyLaw.getExemptionStatus() == LawStatus.Exempt);
                lawFlag.setReimbursable(companyLaw.getReimbursableStatus() == ReimbursableStatus.Reimbursable);
                lawFlags.add(lawFlag);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding law flags", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return lawFlags;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditFilingFlags)
    public void updateLawFlags(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, List<SAPLawFlags> lawFlags) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            List<ProcessResult> prList = new ArrayList<ProcessResult>();
            for (SAPLawFlags lawFlag : lawFlags) {
                CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, lawFlag.getLaw().getLawId()));
                companyLawDTO.setFilingStatus(lawFlag.getInactive() ? PayrollItemStatus.Inactive : PayrollItemStatus.Active);
                companyLawDTO.setExemptionStatus(lawFlag.getExempt() ? LawStatus.Exempt : LawStatus.NonExempt);
                companyLawDTO.setReimbursableStatus(lawFlag.getReimbursable() ? ReimbursableStatus.Reimbursable : ReimbursableStatus.NotReimbursable);

                prList.add(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
            }

            if (!aeFactory.errorsOccurred(prList)) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating Law Flags", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating Law Flags", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewAgencyInfo)
    public List<SAPPropertyAudit> getLawFlagHistory(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd) throws Throwable {

        ArrayList<SAPPropertyAudit> sapPropertyAudits = new ArrayList<SAPPropertyAudit>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<PropertyAudit> propertyAudits = Application.find(PropertyAudit.class,
                    PropertyAudit.Company().equalTo(company)
                            .And(PropertyAudit.ClassName().equalTo(CompanyLaw.class.getSimpleName())));
            for (PropertyAudit propertyAudit : propertyAudits) {
                CompanyLaw companyLaw = Application.findById(CompanyLaw.class, SpcfUniqueId.createInstance(propertyAudit.getObjectIdentifier()));
                if (companyLaw.getAdditionalCompanyLaw() != null) {
                    continue;
                }
                if (companyLaw.getLaw().getPaymentTemplate().getPaymentTemplateCd().equals(paymentTemplateCd)) {
                    SAPPropertyAudit sapPropertyAudit = PropertyAuditTranslator.getSAPPropertyAuditBasicsFromDomainEntity(propertyAudit);

                    if (propertyAudit.getPropertyName().equals(CompanyLaw.FilingStatus().getPropertyName())) {
                        sapPropertyAudit.setPropertyName("Inactive");
                        sapPropertyAudit.setOldPropertyValue(propertyAudit.getOldPropertyValue().equals(PayrollItemStatus.Inactive.name()) ? "Y" : "N");
                        sapPropertyAudit.setNewPropertyValue(propertyAudit.getNewPropertyValue().equals(PayrollItemStatus.Inactive.name()) ? "Y" : "N");
                    } else if (propertyAudit.getPropertyName().equals(CompanyLaw.ExemptionStatus().getPropertyName())) {
                        sapPropertyAudit.setPropertyName("Exempt");
                        sapPropertyAudit.setOldPropertyValue(propertyAudit.getOldPropertyValue().equals(LawStatus.Exempt.name()) ? "Y" : "N");
                        sapPropertyAudit.setNewPropertyValue(propertyAudit.getNewPropertyValue().equals(LawStatus.Exempt.name()) ? "Y" : "N");
                    } else if (propertyAudit.getPropertyName().equals(CompanyLaw.ReimbursableStatus().getPropertyName())) {
                        sapPropertyAudit.setPropertyName("Reimbursable");
                        sapPropertyAudit.setOldPropertyValue(propertyAudit.getOldPropertyValue().equals(ReimbursableStatus.Reimbursable.name()) ? "Y" : "N");
                        sapPropertyAudit.setNewPropertyValue(propertyAudit.getNewPropertyValue().equals(ReimbursableStatus.Reimbursable.name()) ? "Y" : "N");
                    } else {
                        continue;
                    }

                    sapPropertyAudit.setCategory(companyLaw.getLaw().getLawAbbrev());

                    sapPropertyAudits.add(sapPropertyAudit);
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error loading law flag history", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapPropertyAudits;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditFilerType)
    public SAPFilerType getFilerType(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.getIRS_941());
            CompanyAgencyFormTemplate formTemplate = companyAgencyPaymentTemplate.getFormTemplate();
            return TaxTranslator.get941FilerTypeFromFormTemplate(formTemplate);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding filer type", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditFilerType)
    public void updateFilerType(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, SAPFilerType filerType) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, Agency.IRS);
            CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);

            //remove 941/944 that are on or after the specified.
            Iterator<FormTemplateDTO> formTemplateIterator = companyAgencyDTO.getFormTemplateDtoList().iterator();
            while (formTemplateIterator.hasNext()) {
                FormTemplateDTO formTemplateDTO = formTemplateIterator.next();
                if (formTemplateDTO.is941944() && !filerType.getEffectiveQuarter().getFirstDayOfQuarter().after(formTemplateDTO.getEffectiveDate().toLocal())) {
                    formTemplateIterator.remove();
                }
            }

            //then add the specified one in
            companyAgencyDTO.getFormTemplateDtoList().add(TaxTranslator.get941FormTemplateDTOFromSAPFilerType(filerType));

            ProcessResult pr = PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO);

            if (pr.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating Filer Type", pr);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating Filer Type", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditACHRegFlag)
    //updates the agentEnabled flag on the payment method for the template that requires it
    public void updateAgentEnabled(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String paymentTemplateCd, boolean agentEnabled) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

            List<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(),
                    company.getSourceCompanyId(),
                    paymentTemplateCd,
                    paymentTemplate.getAgentEnabledRequiredPaymentMethod(),
                    agentEnabled));

            if (paymentTemplateCd.equals(PaymentTemplate.NY_WH) && CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO)) != null) { //copy NY WH => NY Metro
                prList.add(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        PaymentTemplate.NY_METRO,
                        paymentTemplate.getAgentEnabledRequiredPaymentMethod(),
                        agentEnabled));
            }

            // If Cancel Registration is clicked for FL SUI, we need to send again in ADD file to agency.
            if (PaymentTemplate.FL_SUI.equals(paymentTemplateCd) && !agentEnabled) {
                prList.add(PayrollServices.companyManager.addACHEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplateCd, true));
            }

            if (!aeFactory.errorsOccurred(prList)) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating agent enabled", prList);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating agent enabled", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public List<SAPCompanyLaw> findCompanyLaws(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));

            List<SAPCompanyLaw> sapCompanyLaws = new ArrayList<SAPCompanyLaw>();
            DomainEntitySet<CompanyLaw> companyLaws = CompanyLaw.findAllCompanyLaws(company);
            for (CompanyLaw companyLaw : companyLaws) {
                SAPCompanyLaw sapCompanyLaw = PayrollRunTranslator.getSAPCompanyLawFromDomainEntity(companyLaw);
                //Finding non-duplicated Company Laws and clearing latest Id
                if (sapCompanyLaw.getSourceId().equals(sapCompanyLaw.getLatestId()) && companyLaws.find(CompanyLaw.AdditionalCompanyLaw().SourceId().equalTo(sapCompanyLaw.getSourceId())).getFirst() == null) {
                    sapCompanyLaw.setLatestId(null);
                }
                sapCompanyLaws.add(sapCompanyLaw);
            }

            return sapCompanyLaws;

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company laws", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EditAgencyIDs)
    public void editCompanyLawAgencyId(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceSystemId, String sourceId, String agencyId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceSystemId, SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);

            CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
            companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(agencyId);

            ProcessResult<CompanyLaw> processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error updating company law agency ID", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating company law agency ID", pSourceSystemCd, pSourceSystemId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    private void addPayrollTransactionsToDTO(UpdateDataSyncTokensDTO dto, Iterable<QbdtTransactionInfo> qbdtTransactionInfos) {

        for (QbdtTransactionInfo qbdtTransactionInfo : qbdtTransactionInfos) {
            if (qbdtTransactionInfo.getToken() == -2) {
                continue;
            }
            if (qbdtTransactionInfo.getPriorPaymentSubmission() != null && !dto.getPriorPaymentsAndRefunds().contains(qbdtTransactionInfo.getPriorPaymentSubmission().getId())) {
                dto.getPriorPaymentsAndRefunds().add(qbdtTransactionInfo.getPriorPaymentSubmission().getId());
            } else if (qbdtTransactionInfo.getCompanyAdjustmentSubmission() != null) {
                dto.getLiabilityAdjustments().add(qbdtTransactionInfo.getCompanyAdjustmentSubmission().getId());
            } else if (qbdtTransactionInfo.getLiabilityAdjustment() != null && qbdtTransactionInfo.getLiabilityAdjustment().getCompanyAdjustmentSubmission() != null) {
                dto.getLiabilityAdjustments().add(qbdtTransactionInfo.getLiabilityAdjustment().getCompanyAdjustmentSubmission().getId());
            } else if (qbdtTransactionInfo.getLiabilityCheck() != null) {
                dto.getLiabilityChecks().add(qbdtTransactionInfo.getLiabilityCheck().getId());
            } else if (qbdtTransactionInfo.getQbdtPayrollTransaction() != null) {
                QbdtPayrollTransactionType qbdtPayrollTransactionType = qbdtTransactionInfo.getQbdtPayrollTransaction().getTransactionType();
                switch (qbdtPayrollTransactionType) {
                    case DDReturn:
                    case FundsTransfer:
                    case LiabilityCheck:
                    case LiabilityAdjustment:
                        dto.getQbdtOnlyPayrollTransactions().add(qbdtTransactionInfo.getQbdtPayrollTransaction().getId());
                }
            }
        }
    }

    private void addPaychecksToDTO(UpdateDataSyncTokensDTO dto, Iterable<Paycheck> paychecks) {

        for (Paycheck paycheck : paychecks) {
            if (paycheck.getQbdtPaycheckInfo() == null || paycheck.getQbdtPaycheckInfo().getToken() == -2 || !paycheck.getQbdtPaycheckInfo().getIsAssisted()) {
                continue;
            }
            dto.getPaychecks().add(paycheck.getId());
        }
    }

    private void addEmployeesToDTO(UpdateDataSyncTokensDTO dto, Iterable<Employee> employees) {

        for (Employee employee : employees) {
            if (employee.getQbdtEmployeeInfo() == null || employee.getQbdtEmployeeInfo().getToken() == -2 || !employee.getQbdtEmployeeInfo().getIsAssisted()) {
                continue;
            }
            dto.getEmployees().add(employee.getId());
        }
    }

    private void addPayrollItemsToDTO(UpdateDataSyncTokensDTO dto, Iterable<QbdtPayrollItemInfo> payrollItemInfos) {

        for (QbdtPayrollItemInfo payrollItemInfo : payrollItemInfos) {
            if (payrollItemInfo.getToken() == -2) {
                continue;
            }
            dto.getPayrollItems().add(payrollItemInfo.getId());
        }
    }

    private void multithreadedUpdateInitiationDate(Map<String, List<SpcfUniqueId>> mmtIdListByCompany, final SpcfCalendar pNewDate) throws Throwable {

        ExecutorService threadPool = null;
        CompletionService<ArrayList<ProcessResult>> completionService;

        try {
            threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            completionService = new ExecutorCompletionService<ArrayList<ProcessResult>>(threadPool);
            final PspPrincipal pspPrincipal = Application.getCurrentPrincipal();

            int cycleCount = 0;

            for(Map.Entry<String, List<SpcfUniqueId>> mmtEntry : mmtIdListByCompany.entrySet()){
                List<List<SpcfUniqueId>> mmtListsToProcess = Lists.partition(mmtEntry.getValue(), 100);

                final String sourceCompanyId = mmtEntry.getKey();
                final RequestContext requestContext = pspRequestContextManager.getRequestContext();

                for(List<SpcfUniqueId> mmtList: mmtListsToProcess){
                    final List<SpcfUniqueId> finalMmtList = mmtEntry.getValue();
                    completionService.submit(new Callable<ArrayList<ProcessResult>>() {
                        public ArrayList<ProcessResult> call() throws Exception {

                            ArrayList<ProcessResult> processResultList = new ArrayList<ProcessResult>();
                            try {
                                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                                PayrollServices.setCurrentPrincipal(pspPrincipal);
                                pspRequestContextManager.setRequestContext(requestContext);
                                pspRequestContextManager.setRequestContextCompanyFromPSID(sourceCompanyId);

                                for (SpcfUniqueId spcfUniqueId : finalMmtList) {
                                    processResultList.add(PayrollServices.paymentManager.updateInitiationDate(spcfUniqueId.toString(), pNewDate));
                                }
                                PayrollServices.commitUnitOfWork();
                            } catch (Throwable t) {
                                logger.warn(t);
                            } finally {
                                pspRequestContextManager.clearRequestContext();
                                PayrollServices.rollbackUnitOfWork();
                            }
                            return processResultList;
                        }
                    });
                    cycleCount++;
                }

            }

            try {
                for (int t = 0; t < cycleCount; t++) {
                    Future<ArrayList<ProcessResult>> processResultList = completionService.take();
                    if (aeFactory.errorsOccurred(processResultList.get())) {
                        aeFactory.throwGenericException("Error updating initiation dates", processResultList.get());
                    }
                }
            } catch (InterruptedException e) {
                logger.warn(e);
                Thread.currentThread().interrupt();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error updating initiation dates", t);
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
            }
        }
    }

    private void multithreadedInitiateRepayment(Map<String, List<String>> paymentIds, final SpcfCalendar newInitiationDate, final boolean recreate, final PaymentMethod newPaymentMethod) throws Throwable {

        ExecutorService threadPool = null;
        CompletionService<ArrayList<ProcessResult>> completionService;

        try {
            threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            completionService = new ExecutorCompletionService<ArrayList<ProcessResult>>(threadPool);
            final PspPrincipal pspPrincipal = Application.getCurrentPrincipal();

            int cycleCount = 0;
            for (Map.Entry<String, List<String>> paymentGroup:paymentIds.entrySet()) {
                    final String sourceCompanySeq = paymentGroup.getKey();
                    final List<String> finalIdBatch = new ArrayList<String>(paymentGroup.getValue());
                    final RequestContext requestContext = pspRequestContextManager.getRequestContext();

                    completionService.submit(new Callable<ArrayList<ProcessResult>>() {
                        public ArrayList<ProcessResult> call() throws Exception {

                            ArrayList<ProcessResult> processResultList = new ArrayList<ProcessResult>();

                            try {
                                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                                PayrollServices.setCurrentPrincipal(pspPrincipal);
                                pspRequestContextManager.setRequestContext(requestContext);
                                pspRequestContextManager.setRequestContextCompanyFromSeq(sourceCompanySeq);

                                for (String paymentId : finalIdBatch) {
                                    ProcessResult processResult = PayrollServices.paymentManager.initiateTaxRepayment(paymentId, newInitiationDate, recreate);

                                    if (!processResult.isSuccess()) {
                                        processResultList.add(processResult);
                                    } else {
                                        if (newPaymentMethod != null) {
                                            MoneyMovementTransaction newMmt = (MoneyMovementTransaction) processResult.getResult();
                                            processResult = PayrollServices.paymentManager.changePaymentMethod(newMmt.getCompany().getSourceSystemCd(), newMmt.getCompany().getSourceCompanyId(), newMmt.getId(), newPaymentMethod);
                                            if (!processResult.isSuccess()) {
                                                processResultList.add(processResult);
                                            }
                                        }
                                    }
                                }

                                PayrollServices.commitUnitOfWork();
                            } catch (Throwable t) {
                                throw new RuntimeException("Error initiating repayment", t);
                            } finally {
                                pspRequestContextManager.clearRequestContext();
                                PayrollServices.rollbackUnitOfWork();
                            }

                            return processResultList;
                        }
                    });
                    cycleCount++;
            }

            try {
                for (int t = 0; t < cycleCount; t++) {
                    Future<ArrayList<ProcessResult>> processResultList = completionService.take();
                    if (aeFactory.errorsOccurred(processResultList.get())) {
                        aeFactory.throwGenericException("Error initiating repayment", processResultList.get());
                    }
                }
            } catch (InterruptedException e) {
                logger.warn(e);
                Thread.currentThread().interrupt();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error initiating repayment", t);
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
            }
        }
    }

    private void readConfigurationParameters() {

        mInterval = SystemParameter.findIntValue(SystemParameter.Code.APP_SERVER_THREAD_POOL_INTERVAL, 60);
        mMaxWait = SystemParameter.findIntValue(SystemParameter.Code.APP_SERVER_THREAD_POOL_MAX_WAIT, 5 * 60);
        mMinPoolSize = SystemParameter.findIntValue(SystemParameter.Code.APP_SERVER_MIN_THREAD_POOL_SIZE, 4);
        mMaxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.APP_SERVER_MAX_THREAD_POOL_SIZE, 4);
        mBatchSize = SystemParameter.findIntValue(SystemParameter.Code.APP_SERVER_BATCH_SIZE, 1000);
    }
}
