package com.intuit.sbd.payroll.psp.batchjobs.zeropayments;

import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AchZeroPaymentsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.PayrollTaxHelper;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class ProcessZeroPayments {
    private static final SpcfLogger logger = Application.getLogger(AchZeroPaymentsProcessor.class);

    private SpcfCalendar processingDate;

    // Exception Payment Template Frequencies for Zero Payment Creation
    // The zero pmt MMT is created with a different payment frequency from the actual payment frequency
    // for the states of MD (Accelerated) and OK (Semiweekly)

    private static final HashMap<String, DepositFrequencyCode> exceptionPaymentTemplateFrequencies = new HashMap<String, DepositFrequencyCode>();

    static {
        exceptionPaymentTemplateFrequencies.put("MD-MW506-PAYMENT", DepositFrequencyCode.ACCELERATED);
        exceptionPaymentTemplateFrequencies.put("OK-OW9A-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

    }

    // Payment Templates that need a zero Payment Created with an "Ignore" status - They need to be included in reports only
    // The payment is not sent to the agency
    // These payment templates are stored as System Parameters: ZERO_PAYMENT_COUPON_REPORT_REQUIRED and ZERO_PAYMENT_RECON_FILE_REQUIRED
    private DomainEntitySet<PaymentTemplate> paymentTemplatesForReportsOnly;
    private DomainEntitySet<PaymentTemplateFrequency> paymentTemplateFrequenciesForReportsOnly = new DomainEntitySet<PaymentTemplateFrequency>();

    private PSPRequestContextManager pspRequestContextManager;

    public ProcessZeroPayments() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);

    }

    /**
     * main method to be invoked by the scheduler
     *
     * @param args
     */
    public static void main(String args[]) {
        try {
            //BatchJobManager.runJob(BatchJobType.AchZeroPayments, args);
            if ((args == null) || (args.length > 2) || (args.length == 0)) {
                throw new RuntimeException("Wrong number of parameters. Usage: ProcessZeroPayments <yyyyMMdd> <PaymentTemplateCd>");
            }

            if (!args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
                throw new RuntimeException("Invalid processing date format " + args[0] + ". Correct format: yyyyMMdd");
            }

            String paymentTemplateCd = null;
            if (args.length > 1) {
                paymentTemplateCd = args[1];
            }

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ProcessZeroPayments));

            PayrollServices.beginUnitOfWork();
            new ProcessZeroPayments().process(SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[0]), paymentTemplateCd);
            PayrollServices.rollbackUnitOfWork();

        } catch (Throwable t) {
            t.printStackTrace();
            logger.fatal(t.getMessage(), t);
            PayrollServices.rollbackUnitOfWork();
            System.exit(1);
        }
    }

    /**
     * Process Zero Payments for Active Companies with no liabilities
     *
     * @param pProcessingDate
     * @return
     */
    public void process(SpcfCalendar pProcessingDate) {
        if (pProcessingDate == null) {
            throw new RuntimeException("Invalid processing date (null).");
        }

        process(pProcessingDate, null);
    }

    /**
     * Create Required Zero Payments for Active Companies with no liabilities
     *
     * @param pProcessingDate
     * @return
     */
    public void process(SpcfCalendar pProcessingDate, String pPaymentTemplateCd) {
        if (pProcessingDate == null) {
            throw new RuntimeException("Invalid processing date (null).");
        }

        // Get the payment templates from System Parameter
        paymentTemplatesForReportsOnly = PaymentTemplate.getPaymentTemplatesFromSystemParameter(pProcessingDate, true, true);

        // Get the payment template frequencies for the coupon/recon reports
        for (PaymentTemplate pt : paymentTemplatesForReportsOnly) {
            paymentTemplateFrequenciesForReportsOnly.addAll(pt.getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.Obsolete().equalTo(false)));
        }

        ExecutorService threadPool = null;
        try {
            StopWatch swTotalProcess = StopWatch.startTimer();
            threadPool = ThreadingUtils.createNewFixedThreadPool();
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            processingDate = SpcfCalendar.createInstance(pProcessingDate.getYear(), pProcessingDate.getMonth(),
                                                         pProcessingDate.getDay(), SpcfTimeZone.getLocalTimeZone());

            // Find PaymentTemplateFrequencies to process
            // First get the payment template frequencies from the Agency Rules
            // These are the ones that require a zero payment to be sent to the agency
            // Then we need to create zero payments to include in the coupon/recon reports
            // These payments do not get sent to the agency so they need to be created with
            // a tax payment status of "Ignore"
            ArrayList<PaymentTemplateFrequency> paymentTemplateFrequencies = null;
            if (pPaymentTemplateCd == null) {
                paymentTemplateFrequencies = PaymentTemplateFrequency.getZeroPaymentRequiredPaymentFrequencies(processingDate);
                logger.info("found " + paymentTemplateFrequencies.size() + " zero payment required frequencies in " + swTotalProcess.getElapsedTimeString());
            } else {
                PaymentTemplate pt = PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd);
                if (pt == null) {
                    throw new RuntimeException("Payment template not found: " + pPaymentTemplateCd);
                }
                paymentTemplateFrequencies = new ArrayList<PaymentTemplateFrequency>();
                paymentTemplateFrequencies.addAll(pt.getPaymentTemplateFrequencyCollection().find(PaymentTemplateFrequency.Obsolete().equalTo(false)));
                logger.info("found " + paymentTemplateFrequencies.size() + " zero payment required frequencies for payment template " + pPaymentTemplateCd + " " + swTotalProcess.getElapsedTimeString());
            }

            paymentTemplateFrequencies.addAll(paymentTemplateFrequenciesForReportsOnly);

            for (PaymentTemplateFrequency ptf : paymentTemplateFrequencies) {
                PaymentTemplate paymentTemplate = ptf.getPaymentTemplate();

                StopWatch swPaymentTemplateFreq = StopWatch.startTimer();
                DomainEntitySet<EffectiveDepositFrequency> edfs = EffectiveDepositFrequency.findCompaniesAndEffectiveDepositFrequencyAtDate(ptf, pProcessingDate);
                logger.info("processing payment template: " + paymentTemplate.getPaymentTemplateCd() + "=" + ptf.getPaymentFrequencyId().name() + "   " + edfs.size() + " companies found in " + swPaymentTemplateFreq.getElapsedTimeString());

                // Calculate payment period for deposit frequency that includes the processing date
                IPaymentPeriod paymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                        paymentTemplate.getPaymentTemplateCd(),
                        ptf.getPaymentFrequencyId().toString(),
                        CalendarUtils.convertToRulesCalendar(pProcessingDate));

                // Process only if the payment period is not null
                if (paymentPeriod != null) {

                    // Process each EffectiveDepositFrequency in a different thread
                    for (int i = 0; i < edfs.size(); i++) {
                        final EffectiveDepositFrequency finalEDF = edfs.get(i);
                        final SpcfCalendar finalProcessingDate = pProcessingDate.copy();
                        final IPaymentPeriod finalPaymentPeriod = paymentPeriod;
                        completionService.submit(new Callable<Integer>() {
                            public Integer call() {
                                return processEffectiveDepositFrequency(finalProcessingDate, finalPaymentPeriod, finalEDF);
                            }
                        });
                    }

                    // Get the results of each thread execution
                    int edfCount = 0;
                    int zeroPaymentsCreated = 0;
                    try {
                        for (int t = 0; t < edfs.size(); t++) {
                            edfCount++;
                            Future<Integer> f = completionService.take();
                            zeroPaymentsCreated += f.get();

                            if (edfCount % 100 == 0) {
                                logger.info("working -- completed processing " + edfCount + " effective deposit frequencies (" + zeroPaymentsCreated + " new zero payments created)" + swPaymentTemplateFreq.getElapsedTimeString());
                            }
                        }
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    catch (ExecutionException e) {
                        ThreadingUtils.launderThrowable(e.getCause());
                    }

                    // Keep cache clean by evicting edfs
                    for (int i = 0; i < edfs.size(); i++) {
                        Application.evict(edfs.get(i));
                    }

                    logger.info("finished processing payment template: " + ptf.getPaymentTemplate().getPaymentTemplateCd() + ":" + ptf.getPaymentFrequencyId().name() + " in " + swPaymentTemplateFreq.getElapsedTimeString() + "   company count: " + edfs.size() + "   payments created: " + zeroPaymentsCreated + "   total process elapsed: " + swTotalProcess.getElapsedTimeString());
                }
            }
        }
        finally {
            ThreadingUtils.shutdownAndAwaitTermination(threadPool);
        }
    }

    private int processEffectiveDepositFrequency(SpcfCalendar pProcessingDate, IPaymentPeriod paymentPeriod, EffectiveDepositFrequency edf) {
        int zeroPayments = 0;
        try {
            pspRequestContextManager.setRequestContext(null, RequestType.OLAP, "AchZeroPayments");
            PayrollServices.setCurrentPrincipal(SystemPrincipal.ProcessZeroPayments);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            edf = Application.findById(EffectiveDepositFrequency.class, edf.getId());

            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = edf.getCompanyAgencyPaymentTemplate();
            Company company = companyAgencyPaymentTemplate.getCompanyAgency().getCompany();

            try {
                pspRequestContextManager.setRequestContextCompany(company);
                // Not all EDFs are on tax companies because of VMP.  Only process for tax or prior tax companies
                if (!company.hasService(ServiceCode.Tax)) {
                    return zeroPayments;
                }

                // Check if this company needs a zero payment created
                // If the company is in a cancelled status check if last tax quarter is before the current quarter. If not, we still need a zero
                // payment to be created

                boolean zeroPaymentRequired = true;

                if (!company.isCompanyOnService(ServiceCode.Tax)) {
                    CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
                    TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) taxService;
                    int lastTaxQuarter = taxCompanyServiceInfo.getLastQuarterToFile();
                    // lastTaxQuarter = 0 means PSP is not required to create a zero payment
                    // So if lastTaxQuarter is less than the current quarter don't create a payment
                    if (lastTaxQuarter < CalendarUtils.getYearAndQuarterAsInt(pProcessingDate)) {
                        zeroPaymentRequired = false;
                    }
                }


                // Also need to check if the tax items are active for this company/payment template
                zeroPaymentRequired = zeroPaymentRequired && companyAgencyPaymentTemplate.hasActiveLaw();
                PaymentTemplate paymentTemplate = companyAgencyPaymentTemplate.getPaymentTemplate();
                PaymentTemplateFrequency zeroPaymentFrequency = edf.getPaymentTemplateFrequency();
                String paymentTemplateCd = paymentTemplate.getPaymentTemplateCd();
                IPaymentPeriod zeroPaymentPeriod = paymentPeriod;
                if (exceptionPaymentTemplateFrequencies.containsKey(paymentTemplateCd) && exceptionPaymentTemplateFrequencies.get(paymentTemplateCd).equals(edf.getPaymentTemplateFrequency().getPaymentFrequencyId())){
                    zeroPaymentFrequency = PaymentTemplateFrequency.findPaymentTemplateFrequency(paymentTemplate, DepositFrequencyCode.MONTHLY);
                    zeroPaymentPeriod = getPaymentPeriodForMonthlyZeroPayment(pProcessingDate,zeroPaymentFrequency);
                }

                CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
                if(taxService != null && taxService.getServiceStartDate() != null) {
                    zeroPaymentRequired = zeroPaymentRequired &&
                            !taxService.getServiceStartDate().after(CalendarUtils.convertToSpcfCalendar(zeroPaymentPeriod.getToAccrualDate()));

                }
                DomainEntitySet<MoneyMovementTransaction> mmts = new DomainEntitySet<MoneyMovementTransaction>();
                // Try to find a mmt for the company, payment template, paymentPeriod
                SpcfCalendar settlementDate = pProcessingDate.toLocal().copy();
                if (zeroPaymentFrequency.equals(edf.getPaymentTemplateFrequency())){
                    mmts = MoneyMovementTransaction.findMoneyMovementTransactionsByPaymentPeriod(
                            company,
                            paymentTemplate,
                            paymentPeriod);
                    settlementDate = CalendarUtils.convertToSpcfCalendar(paymentPeriod.getToAccrualDate());
                } else  {
                    mmts = findMMTsForSpecificFrequencies(pProcessingDate,zeroPaymentFrequency,company);
                    zeroPaymentPeriod = getPaymentPeriodForMonthlyZeroPayment(pProcessingDate,zeroPaymentFrequency);
                    settlementDate = CalendarUtils.convertToSpcfCalendar(zeroPaymentPeriod.getToAccrualDate());
                    // For backdated payrolls: if a zero payment is created but not sent yet and a backdated payroll is sent
                    // the zero payment needs to be deleted
                    if (mmts.size() > 0 && mmts.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().greaterThan(SpcfMoney.ZERO)).size() > 0) {
                        deleteZeroPaymentForSpecificFrequencies(pProcessingDate,zeroPaymentFrequency,company);
                    }
                }

                if (zeroPaymentRequired) {

                    // Create zero payment if no tax payment was found
                    if (mmts.size() == 0 && MoneyMovementTransaction.getMMTsInMemory(company, paymentTemplate).size() == 0) {
                        BankAccount debitBankAccount = PayrollTaxHelper.getIntuitTaxBankAccount();
                        BankAccount creditBankAccount = null;

                        // Determine Settlement Type for the FT
                        SettlementType settlementType = FinancialTransaction.getDefaultTaxSettlementType(company, paymentTemplate);

                        // Clear time on settlement date
                        CalendarUtils.clearTime(settlementDate);

                        PaymentTemplateBankAccount pmtTemplateBankAccount = PaymentTemplateBankAccount.findActiveBankAccount(paymentTemplate);
                        if(pmtTemplateBankAccount != null) {
                            creditBankAccount = pmtTemplateBankAccount.getBankAccount();
                        }
                        FinancialTransaction ft = FinancialTransaction.createFinancialTransaction(company, null, null, creditBankAccount, debitBankAccount,
                                BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.AgencyTaxCredit,
                                SpcfMoney.ZERO, settlementType, settlementDate, paymentTemplate.getLawCollection().get(0));


                        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

                        // If the paymentTemplate is for Report Only, set the MMT TaxPaymentStatus to Ignore
                        if (paymentTemplatesForReportsOnly.contains(paymentTemplate) && isZeroPayment(mmt)) {
                            mmt.setTaxPaymentStatus(TaxPaymentStatus.Ignore);
                        }
                        // Modify mmt for exception zero Payments
                        if (!zeroPaymentFrequency.equals(edf.getPaymentTemplateFrequency())) {
                            IPaymentPeriod paymentPeriodZeroPayment = getPaymentPeriodForMonthlyZeroPayment(pProcessingDate,zeroPaymentFrequency);
                            mmt.setPaymentPeriodBegin(CalendarUtils.convertToSpcfCalendar(paymentPeriodZeroPayment.getFromAccrualDate()));
                            mmt.setPaymentPeriodEnd(CalendarUtils.convertToSpcfCalendar(paymentPeriodZeroPayment.getToAccrualDate()));
                            mmt.updateDueDate(CalendarUtils.convertToSpcfCalendar(paymentPeriodZeroPayment.getDueDate()));
                            MoneyMovementTransaction.recreateEntryDetailRecords(mmt);
                        }
                        // Add MMT to cache
                        MoneyMovementTransaction.getMMTsInMemory(company, paymentTemplate).add(mmt);
                        mmt = Application.save(mmt);
                        zeroPayments = 1;
                    }
                } else {
                    // Zero MMT was created and is not required anymore, delete it
                    for (MoneyMovementTransaction mmt : mmts) {
                        //this list also includes executed MMTs because of other checks.  Only attempt to delete pending ones.
                        if (mmt.isPendingMMT() && isZeroPayment(mmt)) {
                            deleteZeroPayment(mmt);
                        }

                    }
                }
                PayrollServices.commitUnitOfWork();
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }
        catch (Throwable t) {
            logger.error("error processing zero payment for effective deposit frequency " + edf.getId(), t);
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
        return zeroPayments;
    }
    private IPaymentPeriod getPaymentPeriodForMonthlyZeroPayment(SpcfCalendar pProcessingDate, PaymentTemplateFrequency pPaymentTemplateFrequency)  {
        // Calculate the payment period for the previous month
        SpcfCalendar paymentPeriodDate = pProcessingDate.copy();
        paymentPeriodDate.addMonths(-1);
        PaymentTemplate paymentTemplate = pPaymentTemplateFrequency.getPaymentTemplate();

        return MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(),
                                                         pPaymentTemplateFrequency.getPaymentFrequencyId().toString(),  CalendarUtils.convertToRulesCalendar(paymentPeriodDate));
    }

    private DomainEntitySet<MoneyMovementTransaction> findMMTsForSpecificFrequencies(SpcfCalendar pProcessingDate, PaymentTemplateFrequency pPaymentTemplateFrequency, Company pCompany)  {
        // Calculate the payment period for the previous month
        SpcfCalendar paymentPeriodDate = pProcessingDate.copy();
        paymentPeriodDate.addMonths(-1);
        PaymentTemplate paymentTemplate = pPaymentTemplateFrequency.getPaymentTemplate();

        IPaymentPeriod paymentPeriodZeroPayment = getPaymentPeriodForMonthlyZeroPayment(pProcessingDate, pPaymentTemplateFrequency);

        // Find MMTs with Payment Periods within the zero Payment Period

        Criterion<MoneyMovementTransaction> mmtCriteria;

        mmtCriteria = MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate)
                                              .And(MoneyMovementTransaction.Company().equalTo(pCompany));

        mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.PaymentPeriodEnd().greaterOrEqualThan(CalendarUtils.convertToSpcfCalendar(paymentPeriodZeroPayment.getFromAccrualDate()))
                                                              .And(MoneyMovementTransaction.PaymentPeriodBegin().lessOrEqualThan(CalendarUtils.convertToSpcfCalendar(paymentPeriodZeroPayment.getToAccrualDate()))))
                                 .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed));
        Expression<MoneyMovementTransaction> mmtQuery =
                new Query<MoneyMovementTransaction>()
                        .Where(mmtCriteria)
                        .OrderBy(MoneyMovementTransaction.InitiationDate());

        return Application.find(MoneyMovementTransaction.class, mmtQuery);

    }

    private void deleteZeroPaymentForSpecificFrequencies(SpcfCalendar pProcessingDate, PaymentTemplateFrequency pPaymentTemplateFrequency, Company pCompany)  {
        // Calculate the payment period for the previous month
        SpcfCalendar paymentPeriodDate = pProcessingDate.copy();
        paymentPeriodDate.addMonths(-1);
        PaymentTemplate paymentTemplate = pPaymentTemplateFrequency.getPaymentTemplate();

        IPaymentPeriod paymentPeriodZeroPayment = getPaymentPeriodForMonthlyZeroPayment(pProcessingDate, pPaymentTemplateFrequency);

        // Find a Zero Amount MMT with Initiation Date > processing Date

        Criterion<MoneyMovementTransaction> mmtCriteria;

        mmtCriteria = MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate)
                                              .And(MoneyMovementTransaction.Company().equalTo(pCompany));

        mmtCriteria = mmtCriteria.And(MoneyMovementTransaction.PaymentPeriodBegin().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriodZeroPayment.getFromAccrualDate()))
                                                              .And(MoneyMovementTransaction.PaymentPeriodEnd().equalTo(CalendarUtils.convertToSpcfCalendar(paymentPeriodZeroPayment.getToAccrualDate())))
                                                              .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold, PaymentStatus.Executed)))
                                 .And(MoneyMovementTransaction.InitiationDate().greaterThan(pProcessingDate))
                                 .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO));
        Expression<MoneyMovementTransaction> mmtQuery =
                new Query<MoneyMovementTransaction>()
                        .Where(mmtCriteria);


        MoneyMovementTransaction zeroPayment = Application.find(MoneyMovementTransaction.class, mmtQuery).getFirst();
        if (zeroPayment != null)  {
            MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(zeroPayment.getFirstFinancialTransaction());
        }

    }

    private boolean isZeroPayment(MoneyMovementTransaction pMoneyMovementTransaction) {
        //Unusually defensive because there are other bugs in this area.

        if (!pMoneyMovementTransaction.getMoneyMovementTransactionAmount().equals(SpcfMoney.ZERO)) {
            return false;
        }

        //only an "Zero Payment" if it has only the single, $0 ATC
        if (pMoneyMovementTransaction.getFinancialTransactionCollection().size() > 1) {
            return false;
        }

        FinancialTransaction ft = pMoneyMovementTransaction.getFirstFinancialTransaction();
        //noinspection SimplifiableIfStatement
        if (ft.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.AgencyTaxCredit) {
            return false;
        }

        return ft.getFinancialTransactionAmount().equals(SpcfMoney.ZERO);
    }

    private void deleteZeroPayment(MoneyMovementTransaction pMoneyMovementTransaction)  {
        MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(pMoneyMovementTransaction.getFirstFinancialTransaction());
    }
}
