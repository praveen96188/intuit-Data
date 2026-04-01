package com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Apr, 17 2012
 * Time: 11:04:41 AM
 */
public class SUIRatePaymentsCleanUp {
    private static SpcfLogger logger = Application.getLogger(SUIRatePaymentsCleanUp.class);
    public static final String ATO_CACHE_KEY = "Cache:AgencyTaxOverAppliedFTs";
    private static final String CREDIT_INDICATOR = "C";
    private static final String DEBIT_INDICATOR = "D";

    private static boolean mCommit = false;
    private static String companyFile = null;
    private static SpcfCalendar mStartDate = null;
    private static SpcfCalendar mEndDate = null;
    private static SpcfCalendar mStartPaycheckDate = null;
    private static SpcfCalendar mEndPaycheckDate = null;
    private static String mAdjustmentType = null;

    private static final String COMMIT_COMMAND = "-commit";
    private static final String COMPANY_FILE_COMMAND = "-companyFile";
    private static final String START_DATE_COMMAND = "-startDate";
    private static final String END_COMMAND = "-endDate";
    private static final String START_PAYCHECK_DATE_COMMAND = "-startPaycheckDate";
    private static final String END_PAYCHECK_DATE_COMMAND = "-endPaycheckDate";
    private static final String ADJUSTMENT_TYPE_COMMAND = "-adjustmentType";

    private PSPRequestContextManager pspRequestContextManager;

    public SUIRatePaymentsCleanUp() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public static void main(String[] args) {
        parseArgs(args);
        new SUIRatePaymentsCleanUp().process(companyFile, mCommit, mStartDate, mEndDate, mStartPaycheckDate, mEndPaycheckDate, mAdjustmentType);
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(COMPANY_FILE_COMMAND)) {
                    companyFile = argParts[1];
                } else if (argParts[0].equals(COMMIT_COMMAND)) {
                    mCommit = Boolean.parseBoolean(argParts[1]);
                } else if (argParts[0].equals(START_DATE_COMMAND)) {
                    mStartDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);
                } else if (argParts[0].equals(END_COMMAND)) {
                    mEndDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);
                } else if (argParts[0].equals(START_PAYCHECK_DATE_COMMAND)) {
                    mStartPaycheckDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);
                } else if (argParts[0].equals(END_PAYCHECK_DATE_COMMAND)) {
                    mEndPaycheckDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);
                } else {
                    if (argParts[0].equals(ADJUSTMENT_TYPE_COMMAND)) {
                        mAdjustmentType = argParts[1];
                    } else
                        throw new RuntimeException("Invalid command: " + argParts[0]);
                }
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }
    }


    private static ArrayList<String> readCompanyIdsFromFile(String pFileName) {
        ArrayList<String> companyIds = new ArrayList<String>();
        try {
            FileReader fileReader = new FileReader(new File(System.getProperty("user.dir") + File.separatorChar + pFileName));
            BufferedReader input = new BufferedReader(fileReader);
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    companyIds.add(line.trim());
                }
            } finally {
                input.close();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return companyIds;
    }


    public void process(String pCompanyFile, final boolean pCommit, SpcfCalendar pStartDate, final SpcfCalendar pEndDate, final SpcfCalendar pPaycheckStartDate, final SpcfCalendar pPaycheckEndDate, final String pAdjustmentType) {

        PayrollServices.setCurrentPrincipal(SystemPrincipal.SUIRatePaymentsCleanup);
        ArrayList<String> pCompanyIds = new ArrayList<String>();
        if (pCompanyFile != null) {
            pCompanyIds = readCompanyIdsFromFile(pCompanyFile);
        }
        logger.info("Beginning SUI Adjustments cleanup...");

        StringBuilder report = new StringBuilder();
        report.append("\nCommit is set to ").append(pCommit).append("\n");

        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * (2);
        logger.info("Creating thread pool with " + threadCount + " threads");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            SpcfCalendar initialDate = null;
            Expression<EventLog> expr = new Query<EventLog>().Where(EventLog.ArchitectureName().equalTo("PSP")
                                                                            .And(EventLog.ComponentName().equalTo("SUIRatePaymentsCleanUp"))
                                                                            .And(EventLog.ApplicationName().equalTo("SUIRatePaymentsCleanUp")))
                                                             .OrderBy(EventLog.CreatedDate().Descending());
            DomainEntitySet<EventLog> cleanupEntries = PayrollServices.entityFinder.find(EventLog.class, expr);

            if (cleanupEntries.size() > 0 && pStartDate ==
                    null) {
                initialDate = cleanupEntries.get(0).getMessageDttm().toLocal();
            } else if (pStartDate != null) {
                initialDate = pStartDate;
            }

            final SpcfCalendar lastProcessedDate = initialDate;


            final DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = findLiabilityAdjustments(null, initialDate, pEndDate, pPaycheckStartDate, pPaycheckEndDate, pAdjustmentType);

            String foundAdjustments = "Found " + liabilityAdjustments.size() + " adjustments to be processed. \n";
            logger.info(foundAdjustments);
            report.append(foundAdjustments);

            final Set<SpcfUniqueId> companyIds = new HashSet<SpcfUniqueId>();
            for (LiabilityAdjustment liabilityAdjustment : liabilityAdjustments) {
                SpcfUniqueId companyId = liabilityAdjustment.getCompany().getId();

                if (!companyIds.contains(companyId)) {
                    if (pCompanyIds != null && pCompanyIds.size() > 0) {
                        if (pCompanyIds.contains(companyId.toString())) {
                            companyIds.add(companyId);
                        }
                    } else {
                        companyIds.add(companyId);
                    }
                }
            }

            String foundCompanies = "Found " + companyIds.size() + " companies with adjustments. \n";
            logger.info(foundCompanies);
            report.append(foundCompanies);

            CompletionService<StringBuilder> completionService = new ExecutorCompletionService<StringBuilder>(executor);

            for (final SpcfUniqueId companyId : companyIds) {
                completionService.submit(new Callable<StringBuilder>() {
                    public StringBuilder call() {
                        StringBuilder companyReport = new StringBuilder();
                        boolean success = false;
                        try {
                            Application.initialize();
                            ApplicationSecondary.initialize();
                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.SUIRatePaymentsCleanup));
                            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                            Company company = Application.findById(Company.class, companyId);
                            pspRequestContextManager.setRequestContext(company, RequestType.OLAP, BatchJobType.EoqSUIAdjustments.toString());

                            DomainEntitySet<LiabilityAdjustment> companyLiabilityAdjustments = findLiabilityAdjustments(company, lastProcessedDate, pEndDate, pPaycheckStartDate, pPaycheckEndDate, pAdjustmentType);

                            String foundAdjustments = " *** PROCESSING COMPANY  " + company.getSourceCompanyId() + ". Verifying: " + companyLiabilityAdjustments.size() + " adjustments." + "\n";
                            companyReport.append(foundAdjustments);
                            SpcfDecimal suiAccountBalance = getSUIAccountBalance(company, pPaycheckEndDate);
                            companyReport.append(" Current SUI Account Balance for company: " + company.getSourceCompanyId() + ": " + suiAccountBalance.toString() + "\n");
                            Map<Law, SpcfMoney> processedAmounts = new HashMap<Law, SpcfMoney>();

                            for (LiabilityAdjustment companyLiabilityAdjustment : companyLiabilityAdjustments) {
                                // Check if adjustment has not been processed yet
                                DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.ManualNoteEvent, EventDetailTypeCode.UniqueIdentifier, companyLiabilityAdjustment.getId().toString());
                                if (companyEventDetails.size() > 0) {
                                    logger.info("Adjustment " + companyLiabilityAdjustment.getId().toString() + " amount: " + companyLiabilityAdjustment.getAmount().toString() + " has already been processed. \n");
                                } else {
                                    Law adjustmentLaw = companyLiabilityAdjustment.getLaw();
                                    // Calculate new ATO amount based on ATR balance and adjustment/variance calculation
                                    if (companyLiabilityAdjustment.getAmount().isLessThan(SpcfMoney.ZERO)) {
                                        // Negative Adjustments
                                        //    Find ERSUITaxPayable Law Total
                                        //    If Total > 0
                                        //       Void all ATOs associated with the payroll
                                        //       Get ATR balance for the quarter/law
                                        //       If ATR Balance >= 0
                                        //              New ATO Amount =  ATR Amount  + Already processed negative adjustments amount - ERSUITaxPayable
                                        //       else
                                        //              x = SUM (voided ATOs) + ATRBalance  - ERSUITaxPayable
                                        //
                                        //       If x > 0
                                        //               New ATO Amount =  x
                                        //       else
                                        //                Cancel ERSUITaxPayable
                                        //                new ERSUITaxPayable amount =  ERSUITaxPayable + x
                                        //       Create new ATO if New ATO amount > 0
                                        //       Create new ERSUITaxPayable if new ERSUITaxPayable amount > 0


                                        // SpcfMoney totalVarianceAmount = getTotalVarianceAmount(company, TransactionTypeCode.EmployerSUITaxPayable, adjustmentLaw);
                                        SpcfMoney totalVarianceAmount = getVarianceAccountBalance(company, companyLiabilityAdjustment.getPayrollRun().getPaycheckDate(), companyLiabilityAdjustment.getLaw());
                                        if (totalVarianceAmount.isGreaterThan(SpcfMoney.ZERO)) {
                                            // logger.info("Variance Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString() );
                                            // companyReport.append("Variance Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString());
                                            logger.info("  Fixing Negative Adjustment " + companyLiabilityAdjustment.getId().toString() + " Amount: " + companyLiabilityAdjustment.getAmount().toString() + " for company " + company.getSourceCompanyId());
                                            companyReport.append("  Fixing Negative Adjustment " + companyLiabilityAdjustment.getId().toString() + " Amount: " + companyLiabilityAdjustment.getAmount().toString() + " for company " + company.getSourceCompanyId() + "\n");
                                            CompanyEvent.createCompanyEventAndDetail(company, EventTypeCode.ManualNoteEvent, EventDetailTypeCode.UniqueIdentifier, companyLiabilityAdjustment.getId().toString());
                                            SpcfMoney totalProcessedAmount = getProcessedAdjustmentsAmount(company, lastProcessedDate, pPaycheckStartDate, pPaycheckEndDate, adjustmentLaw);
                                            if (processedAmounts.containsKey(adjustmentLaw)) {
                                                totalProcessedAmount = new SpcfMoney(totalProcessedAmount.add(processedAmounts.get(adjustmentLaw)));
                                            }
                                            // totalProcessedAmount <= 0
                                            if (totalProcessedAmount.negate().isLessThan(totalVarianceAmount)) {
                                                SpcfMoney newATOAmount = SpcfMoney.ZERO;
                                                SpcfMoney voidedAmount = SpcfMoney.ZERO;

                                                // Get Current ATR Balance (before void)
                                                Map<Law, SpcfMoney> lawMap = getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, adjustmentLaw.getPaymentTemplate(), company, companyLiabilityAdjustment.getPayrollRun().getPaycheckDate());
                                                SpcfMoney atrBalanceBeforeVoid = (lawMap.get(adjustmentLaw) != null) ? lawMap.get(adjustmentLaw) : SpcfMoney.ZERO;
                                                companyReport.append("  Current Agency Tax Refund Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + atrBalanceBeforeVoid.toString() + "\n");

                                                // Void ATOS   '[
                                                DomainEntitySet<FinancialTransaction> atos = getATOs(company, companyLiabilityAdjustment, adjustmentLaw);
                                                if (!Application.getSessionCache().isEntityCollectionCached(FinancialTransaction.class, ATO_CACHE_KEY)) {
                                                    Application.getSessionCache().addEntityCollection(FinancialTransaction.class, ATO_CACHE_KEY, new DomainEntitySet<FinancialTransaction>());
                                                }


                                                for (FinancialTransaction ato : atos) {
                                                    ato.updateFinancialTransactionState(TransactionStateCode.Voided);
                                                    Application.getSessionCache().addEntity(FinancialTransaction.class, ATO_CACHE_KEY, ato);
                                                    voidedAmount = new SpcfMoney(voidedAmount.add(ato.getFinancialTransactionAmount()));
                                                }

                                                // Get  ATR Balance after void
                                                lawMap = getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, adjustmentLaw.getPaymentTemplate(), company, companyLiabilityAdjustment.getPayrollRun().getPaycheckDate());
                                                SpcfMoney atrBalanceAfterVoid = (lawMap.get(adjustmentLaw) != null) ? lawMap.get(adjustmentLaw) : SpcfMoney.ZERO;

                                                SpcfMoney balance = new SpcfMoney(atrBalanceBeforeVoid.subtract(totalVarianceAmount));
                                                if (atrBalanceAfterVoid.isGreaterThanEqualTo(SpcfMoney.ZERO) && balance.isGreaterThanEqualTo(SpcfMoney.ZERO)) {
                                                    newATOAmount = new SpcfMoney(atrBalanceBeforeVoid.subtract(totalVarianceAmount));
                                                } else {
                                                    if (balance.isGreaterThanEqualTo(SpcfMoney.ZERO)) {
                                                        newATOAmount = new SpcfMoney(voidedAmount.subtract(totalVarianceAmount));
                                                    } else {
                                                        if (atrBalanceAfterVoid.isLessThan(SpcfMoney.ZERO)) {
                                                            newATOAmount = voidedAmount;
                                                        }

                                                    }
                                                }

                                                BankAccount intuitTaxAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.AgencyTaxCredit, CreditDebitCode.Debit).getBankAccount();
                                                if (newATOAmount.isGreaterThan(SpcfMoney.ZERO)) {
                                                    FinancialTransaction ato = FinancialTransaction.createAgencyTaxOverpaymentTransaction(companyLiabilityAdjustment.getPayrollRun(), adjustmentLaw, intuitTaxAccount, newATOAmount);
                                                    Application.getSessionCache().addEntity(FinancialTransaction.class, ATO_CACHE_KEY, ato);
                                                }
                                                lawMap = getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.AgencyTaxRefund, adjustmentLaw.getPaymentTemplate(), company, companyLiabilityAdjustment.getPayrollRun().getPaycheckDate());
                                                atrBalanceAfterVoid = (lawMap.get(adjustmentLaw) != null) ? lawMap.get(adjustmentLaw) : SpcfMoney.ZERO;

                                                logger.info("  Agency Tax Refund Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + atrBalanceAfterVoid.toString());
                                                companyReport.append("  Agency Tax Refund Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + atrBalanceAfterVoid.toString() + "\n");
                                            }
                                            //totalVarianceAmount = getVarianceAccountBalance(company, companyLiabilityAdjustment.getPayrollRun().getPaycheckDate(), companyLiabilityAdjustment.getLaw());
                                            //logger.info(" New Adjusted Variance Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString() );
                                            //companyReport.append(" New Adjusted Variance Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString() + "\n" );

                                        }

                                        if (processedAmounts.containsKey(adjustmentLaw)) {
                                            processedAmounts.put(adjustmentLaw, new SpcfMoney(processedAmounts.get(adjustmentLaw).add(companyLiabilityAdjustment.getAmount().negate())));
                                        } else {
                                            processedAmounts.put(adjustmentLaw, new SpcfMoney(companyLiabilityAdjustment.getAmount().negate()));
                                        }
                                    } else {
                                        // Positive Adjustments
                                        //    Find ERSUITaxReceivable Law Total
                                        //    New ATC Amount =  Adjustment Amount  - ERSUITaxReceivable
                                        //    If Total > 0
                                        //      Cancel Created ATC
                                        //      Cancel ERSUITaxReceivable (all transactions)
                                        //      if New ATC amount > 0
                                        //          Create new ATC
                                        //      else (still money in variance, create new ERSUITaxReceivable)
                                        //          Create new ERSUITaxReceivable: Amount equals ATC Amount * -1

                                        // SpcfMoney totalVarianceAmount = getTotalVarianceAmount(company, TransactionTypeCode.EmployerSUITaxReceivable, companyLiabilityAdjustment.getLaw());

                                        SpcfMoney totalVarianceAmount = getVarianceAccountBalance(company, companyLiabilityAdjustment.getPayrollRun().getPaycheckDate(), companyLiabilityAdjustment.getLaw());
                                        if (totalVarianceAmount.isLessThan(SpcfMoney.ZERO)) {
                                            //   logger.info("Variance Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString() );
                                            //   companyReport.append("Variance Account Balance for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString()  + "\n" );
                                            logger.info("  Fixing Positive Adjustment " + companyLiabilityAdjustment.getId().toString() + " Amount: " + companyLiabilityAdjustment.getAmount().toString() + " for company " + company.getSourceCompanyId());
                                            companyReport.append(("  Fixing Positive Adjustment " + companyLiabilityAdjustment.getId().toString() + " Amount: " + companyLiabilityAdjustment.getAmount().toString() + " for company " + company.getSourceCompanyId() + "\n"));
                                            CompanyEvent.createCompanyEventAndDetail(company, EventTypeCode.ManualNoteEvent, EventDetailTypeCode.UniqueIdentifier, companyLiabilityAdjustment.getId().toString());
                                            FinancialTransaction atc = getATC(company, companyLiabilityAdjustment, adjustmentLaw);
                                            if (atc != null) {
                                                SpcfMoney newATCAmount = companyLiabilityAdjustment.getAmount();
                                                newATCAmount = new SpcfMoney(newATCAmount.add(totalVarianceAmount));

                                                SpcfMoney varianceTxnAmount = SpcfMoney.ZERO;

                                                if (newATCAmount.isGreaterThan(SpcfMoney.ZERO)) {
                                                    atc.cancelFinancialTransaction();
                                                    varianceTxnAmount = new SpcfMoney(totalVarianceAmount.negate());
                                                    createTaxCredit(companyLiabilityAdjustment.getPayrollRun(), adjustmentLaw, new SpcfMoney(newATCAmount.add(varianceTxnAmount)));
                                                } else {
                                                    varianceTxnAmount = new SpcfMoney(newATCAmount.subtract(totalVarianceAmount));
                                                }
                                                if (varianceTxnAmount.isGreaterThan(SpcfMoney.ZERO)) {
                                                    FinancialTransaction atd = createTaxDebit(companyLiabilityAdjustment.getPayrollRun(), adjustmentLaw, varianceTxnAmount);
                                                    SpcfCalendar settlementDate = getSettlementDate(companyLiabilityAdjustment.getPayrollRun().getPaycheckDate());
                                                    FinancialTransaction erSUITaxPayable = FinancialTransaction.createFinancialTransaction(company, companyLiabilityAdjustment.getPayrollRun(), null, null, null,
                                                                                                                                           BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.EmployerSUITaxPayable,
                                                                                                                                           new SpcfMoney(varianceTxnAmount),
                                                                                                                                           SettlementType.ApplyForward,
                                                                                                                                           settlementDate, adjustmentLaw);
                                                    erSUITaxPayable = Application.save(erSUITaxPayable);
                                                    if (!Application.getSessionCache().isEntityCollectionCached(FinancialTransaction.class, ATO_CACHE_KEY)) {
                                                        Application.getSessionCache().addEntityCollection(FinancialTransaction.class, ATO_CACHE_KEY, new DomainEntitySet<FinancialTransaction>());
                                                    }
                                                    logger.info("  Adjusting SUI Balance for company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId());
                                                    companyReport.append("  Adjusting SUI Balance for company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + erSUITaxPayable.getFinancialTransactionAmount().toString() + "\n");

                                                    Application.getSessionCache().addEntity(FinancialTransaction.class, ATO_CACHE_KEY, erSUITaxPayable);
                                                    atd.setRelatableTransaction(erSUITaxPayable);
                                                    if (atd.getMoneyMovementTransaction() != null) {
                                                        erSUITaxPayable.setMoneyMovementTransaction(atd.getMoneyMovementTransaction());
                                                        atd.getMoneyMovementTransaction().addFinancialTransaction(erSUITaxPayable);
                                                    }
                                                }
                                            } else {
                                                logger.info(" No adjustments to SUI Balance Account for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString());
                                                companyReport.append(" No adjustments to SUI Balance Account for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString() + "\n");

                                            }

                                            //totalVarianceAmount = getVarianceAccountBalance(company, companyLiabilityAdjustment.getPayrollRun().getPaycheckDate(), companyLiabilityAdjustment.getLaw());
                                            //logger.info("   Variance Account Balance After processing adjustment for Company:  " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString() );
                                            //companyReport.append("   Variance Account Balance After processing adjustment for Company: " + company.getSourceCompanyId() + " - " + company.getLegalName() + " Law: " + companyLiabilityAdjustment.getLaw().getLawId() + ": " + totalVarianceAmount.toString()  + "\n" );
                                        }
                                    }
                                }
                                // logger.info("Processed " + companyLiabilityAdjustments.size() + " adjustments for " + company.getSourceCompanyId());
                            }

                            suiAccountBalance = getSUIAccountBalance(company, pPaycheckEndDate);
                            companyReport.append(" New SUI Account Balance for company: " + company.getSourceCompanyId() + ": " + suiAccountBalance.toString() + "\n");

                            if (pCommit) {
                                logger.info("Committing updates for " + company.getSourceCompanyId());
                                PayrollServices.commitUnitOfWork();
                            }

                            success = true;
                        } catch (Throwable t) {
                            logger.error("Error processing company " + companyId.toString() + " Processing Date: " + lastProcessedDate.toString(), t);
                        } finally

                        {
                            PayrollServices.rollbackUnitOfWork();
                            pspRequestContextManager.clearRequestContext();
                        }

                        if (!success)

                        {
                            return null;
                        }

                        return companyReport;
                    }
                });
            }


            int successes = 0;
            int total = 0;

            for (SpcfUniqueId companyId : companyIds) {
                Future<StringBuilder> f = completionService.take();
                StringBuilder companyReport = f.get();
                total++;
                logger.info("Completed processing " + total + " of " + companyIds.size() + " companies");
                if (companyReport != null) {
                    successes++;
                    report.append(companyReport);
                } else {
                    logger.error("Failed to process adjustments for company " + companyId.toString());
                }
            }

            // Create Event Log to keep track of last processing Date

            if (pCommit && pCompanyIds == null && pEndDate == null) {
                createEventLog();
                PayrollServices.commitUnitOfWork();
            }

            logger.info("Success on " + successes + " companies out of " + companyIds.size());

            logger.info("Finished fixing adjustments");
        } catch (
                Throwable t
                )

        {
            logger.error("An error occurred", t);
        } finally

        {
            PayrollServices.rollbackUnitOfWork();
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
        }

        logger.info(report.toString());
    }

    // find Liability Adjustments

    private static DomainEntitySet<LiabilityAdjustment> findLiabilityAdjustments(Company company, SpcfCalendar pInitialDate, SpcfCalendar pEndDate, SpcfCalendar pPaycheckStartDate, SpcfCalendar pPaycheckEndDate, String pAdjustmentType) {

        CalendarUtils.clearTime(pPaycheckStartDate);
        CalendarUtils.endOfDay(pPaycheckEndDate);
        Criterion<LiabilityAdjustment> where = LiabilityAdjustment.PayrollRun().PayrollRunDate().greaterOrEqualThan(pInitialDate)
                .And(LiabilityAdjustment.PayrollRun().PaycheckDate().between(pPaycheckStartDate, pPaycheckEndDate)
                        .And(LiabilityAdjustment.Law().PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI))
                        .And((LiabilityAdjustment.Amount().isNotNull()))
                        .And(LiabilityAdjustment.QbdtTransactionInfo().Id().isNotNull()))
                .And(LiabilityAdjustment.QbdtTransactionInfo().Company().equalTo(LiabilityAdjustment.PayrollRun().Company()));
        if (company != null) {
            where = where.And(LiabilityAdjustment.Company().equalTo(company));
        }

        if (pEndDate != null) {
            CalendarUtils.endOfDay(pEndDate);
            where = where.And(LiabilityAdjustment.PayrollRun().PayrollRunDate().lessOrEqualThan(pEndDate));
        }

        if (pAdjustmentType != null) {
            if (pAdjustmentType.equals("negative")) {
                where = where.And(LiabilityAdjustment.Amount().lessThan(SpcfMoney.ZERO));
            } else if (pAdjustmentType.equals("positive")) {
                where = where.And(LiabilityAdjustment.Amount().greaterThan(SpcfMoney.ZERO));
            }
        }
        return Application.find(LiabilityAdjustment.class, new Query<LiabilityAdjustment>().Where(where)
                                                                                           .EagerLoad(LiabilityAdjustment.Company()));

    }


    private static SpcfMoney getProcessedAdjustmentsAmount(Company pCompany, SpcfCalendar pDate, SpcfCalendar pPaycheckStartDate, SpcfCalendar pPaycheckEndDate, Law pLaw) {
        SpcfMoney totalProcessedAmount = SpcfMoney.ZERO;

        Expression<LiabilityAdjustment> processedAdjustmentQuery = new Query<LiabilityAdjustment>()
                .Select(LiabilityAdjustment.Amount().Sum())
                .Where(LiabilityAdjustment.PayrollRun().PayrollRunDate().between(SpcfCalendar.createInstance(2012, 4, 4, SpcfTimeZone.getLocalTimeZone()), pDate)
                                          .And(LiabilityAdjustment.PayrollRun().PaycheckDate().between(pPaycheckStartDate, pPaycheckEndDate)
                                                                  .And(LiabilityAdjustment.Law().equalTo(pLaw))
                                                                  .And(LiabilityAdjustment.Amount().lessThan(SpcfMoney.ZERO))
                                                                  .And(LiabilityAdjustment.QbdtTransactionInfo().Id().isNotNull())
                                                                  .And(LiabilityAdjustment.Company().equalTo(pCompany))));


        List processedAmountSumList = Application.executeQuery(LiabilityAdjustment.class, processedAdjustmentQuery);
        if (processedAmountSumList.size() > 0) {
            totalProcessedAmount = (SpcfMoney) processedAmountSumList.get(0) != null ? (SpcfMoney) processedAmountSumList.get(0) : SpcfMoney.ZERO;
        }

        return totalProcessedAmount;
    }

    private static SpcfMoney getVarianceAccountBalance(Company pCompany, SpcfCalendar pDate, Law pLaw) {
        SpcfMoney balance;
        Map<Law, SpcfMoney> lawBalanceMap = getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode.ERSUITaxDue, pLaw.getPaymentTemplate(), pCompany, pDate);
        balance = lawBalanceMap.containsKey(pLaw) ? lawBalanceMap.get(pLaw) : SpcfMoney.ZERO;
        return balance;
    }

    private static DomainEntitySet<FinancialTransaction> getVarianceTransactions(Company pCompany, TransactionTypeCode pTransactionTypeCode, Law pLaw) {

        Expression<FinancialTransaction> varianceQuery = new Query<FinancialTransaction>()
                .Where(FinancialTransaction.Company().equalTo(pCompany)
                                           .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(pTransactionTypeCode))
                                           .And(FinancialTransaction.Law().equalTo(pLaw)));
        DomainEntitySet<FinancialTransaction> varianceTransactions = Application.find(FinancialTransaction.class, varianceQuery);

        return varianceTransactions;
    }

    private static DomainEntitySet<FinancialTransaction> getATOs(Company pCompany, LiabilityAdjustment pLiabilityAdjustment, Law pLaw) {

        TransactionState executedTxnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
        TransactionState completedTxnState = Application.findById(TransactionState.class, TransactionStateCode.Completed);

        Expression<FinancialTransaction> ftQuery = new Query<FinancialTransaction>()
                .Where(FinancialTransaction.Company().equalTo(pCompany)
                                           .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxOverpayment))
                                           .And(FinancialTransaction.CurrentTransactionState().in(executedTxnState, completedTxnState))
                                           .And(FinancialTransaction.Law().equalTo(pLaw))
                                           .And(FinancialTransaction.PayrollRun().equalTo(pLiabilityAdjustment.getPayrollRun())));
        return Application.find(FinancialTransaction.class, ftQuery);

    }


    private static FinancialTransaction getATC(Company pCompany, LiabilityAdjustment pLiabilityAdjustment, Law pLaw) {

        TransactionState createdTxnState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        Expression<FinancialTransaction> ftQuery = new Query<FinancialTransaction>()
                .Where(FinancialTransaction.Company().equalTo(pCompany)
                                           .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit))
                                           .And(FinancialTransaction.Law().equalTo(pLaw))
                                           .And(FinancialTransaction.CurrentTransactionState().equalTo(createdTxnState))
                                           .And(FinancialTransaction.MoneyMovementTransaction().TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold))
                                           .And(FinancialTransaction.PayrollRun().equalTo(pLiabilityAdjustment.getPayrollRun())));
        DomainEntitySet<FinancialTransaction> fts = Application.find(FinancialTransaction.class, ftQuery);

        if (fts.size() > 0) {
            return fts.get(0);
        }

        return null;
    }

    private static SpcfCalendar getSettlementDate(SpcfCalendar pDate) {
        int year = pDate.getYear();
        int quarter = CalendarUtils.getQuarterAsInt(pDate) + 1;
        if (quarter == 5) {
            quarter = 1;
            year++;
        }
        return CalendarUtils.getLastDayOfQuarter(year, quarter);
    }

    private static FinancialTransaction createTaxCredit(PayrollRun pPayrollRun, Law pLaw, SpcfMoney pAmount) {
        if (pAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            return null;
        }

        SpcfCalendar settlementDate = getSettlementDate(pPayrollRun.getPaycheckDate());
        return FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, null,
                                                               BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.AgencyTaxCredit,
                                                               pAmount,
                                                               FinancialTransaction.getDefaultTaxSettlementType(pPayrollRun.getCompany(), pLaw.getPaymentTemplate()),
                                                               settlementDate, pLaw);
    }

    private static FinancialTransaction createTaxDebit(PayrollRun pPayrollRun, Law pLaw, SpcfMoney pAmount) {
        if (pAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            return null;
        }
        SpcfCalendar settlementDate = getSettlementDate(pPayrollRun.getPaycheckDate());
        return FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, null,
                                                               BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.AgencyTaxDebit,
                                                               pAmount,
                                                               FinancialTransaction.getDefaultTaxSettlementType(pPayrollRun.getCompany(), pLaw.getPaymentTemplate()),
                                                               settlementDate, pLaw);
    }

    private static void createEventLog() {
        EventLog eventLog = new EventLog();
        eventLog.setCreatedDate(PSPDate.getPSPTime().toLocal());
        eventLog.setMessageDttm(PSPDate.getPSPTime().toLocal());
        eventLog.setApplicationName("SUIRatePaymentsCleanUp");
        eventLog.setArchitectureName("PSP");
        eventLog.setComponentName("SUIRatePaymentsCleanUp");
        eventLog.setMessage("Cleaning up SUI Liability Adjustments");
        Application.save(eventLog);
    }

    //
    // Ledger Balance Methods
    //
    private static Map<Law, SpcfMoney> getLedgerAccountBalanceByPaymentTemplateAndQuarter(LedgerAccountCode pLedgerAccountCode,
                                                                                          PaymentTemplate pPaymentTemplate,
                                                                                          Company pCompany,
                                                                                          SpcfCalendar pCheckDate) {

        Integer quarter = null;
        Integer year = null;
        if (pCheckDate != null) {
            quarter = CalendarUtils.getQuarterAsInt(pCheckDate);
            year = pCheckDate.getYear();
        }
        Map<Law, Map<String, SpcfMoney>> creditDebitValuesMap =
                findLedgerEntriesSumByAccountCodeAndPaymentTemplateAndQuarter(pCompany, pLedgerAccountCode, pPaymentTemplate, quarter, year);

        if (Application.getSessionCache().isEntityCollectionCached(FinancialTransaction.class, FinancialTransaction.ATO_CACHE_KEY)) {
            DomainEntitySet<FinancialTransaction> financialTransactions = Application.getSessionCache().getEntityCollection(FinancialTransaction.class, FinancialTransaction.ATO_CACHE_KEY);
            if (pPaymentTemplate != null) {
                DomainEntitySet<Law> laws = pPaymentTemplate.getLawCollection();
                financialTransactions = financialTransactions.find(FinancialTransaction.Law().in(laws.toArray(new Law[laws.size()])));
            }

            for (FinancialTransaction financialTransaction : financialTransactions) {
//                if(CalendarUtils.getFirstDayOfQuarter(year, quarter).compareTo(financialTransaction.getMoneyMovementTransaction().getPaymentPeriodBegin()) != 1 &&
//                        CalendarUtils.getLastDayOfQuarter(year, quarter).compareTo(financialTransaction.getMoneyMovementTransaction().getPaymentPeriodEnd()) != -1) {
                Law law = financialTransaction.getLaw();
                if (creditDebitValuesMap.get(law) == null) {
                    Map<String, SpcfMoney> amountMap = new HashMap<String, SpcfMoney>();
                    amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                    amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                    creditDebitValuesMap.put(law, amountMap);
                }
                SpcfMoney debit = creditDebitValuesMap.get(law).get(DEBIT_INDICATOR);

                debit = (SpcfMoney) debit.add(financialTransaction.getFinancialTransactionAmount());
                creditDebitValuesMap.get(law).put(DEBIT_INDICATOR, debit);
                //         }
            }
        }

        Map<Law, SpcfMoney> resultMap = new HashMap<Law, SpcfMoney>();
        for (Law law : creditDebitValuesMap.keySet()) {
            Map<String, SpcfMoney> lawMap = creditDebitValuesMap.get(law);
            SpcfDecimal total = SpcfMoney.ZERO;
            if (resultMap.containsKey(law)) {
                total = resultMap.get(law);
            }
            total = total.add(lawMap.get(CREDIT_INDICATOR)).subtract(lawMap.get(DEBIT_INDICATOR));
            resultMap.put(law, new SpcfMoney(total));
        }
        return resultMap;
    }

    private static Map<Law, Map<String, SpcfMoney>> findLedgerEntriesSumByAccountCodeAndPaymentTemplateAndQuarter(
            Company pCompany,
            LedgerAccountCode ledgerAccountCode,
            PaymentTemplate pPaymentTemplate,
            Integer pQuarter,
            Integer pYear) {

        DomainEntitySet<PayrollRun> payrollRunsToExclude = PayrollRun.getPayrollsInMemory(pCompany);
        List<TransactionTypeCode> postingTransactionTypeCodes = PostingRule.findPostingRuleTransactionTypesByLedgerAccount(ledgerAccountCode);

        NaturalKey naturalKey = null;
        Map<Law, Map<String, SpcfMoney>> cachedMap = null;
        if (pQuarter != null && pYear != null) {
            StringBuilder payrollRunIds = new StringBuilder();
            for (PayrollRun payrollRun : payrollRunsToExclude) {
                if (!payrollRun.isNew()) {
                    payrollRunIds.append(payrollRun.getId().toString());
                }
            }

            int payrollRunHash = payrollRunIds.length() > 0 ? payrollRunIds.toString().hashCode() : 0;
            naturalKey = new NaturalKey(SpcfMoney.class, pCompany.getId(), ledgerAccountCode, pPaymentTemplate == null ? "None" : pPaymentTemplate.getPaymentTemplateCd(), pQuarter, pYear, payrollRunHash);
            cachedMap = Application.getSessionCache().getNonHibernateObject(naturalKey);
        }


        if (cachedMap == null) {
            cachedMap = new HashMap<Law, Map<String, SpcfMoney>>();

            // ATOA transaction is special in that it is not assiciated with a payroll
            if (postingTransactionTypeCodes.contains(TransactionTypeCode.AgencyTaxOverpaymentApplied)) {
                String select =
                        " select law.LawId, " +
                                " sum(case when postingRule.CreditDebitInd = 'C' then finTxn.FinancialTransactionAmount else 0 end) as CreditAmt," +
                                " sum(case when postingRule.CreditDebitInd = 'D' then finTxn.FinancialTransactionAmount else 0 end) as DebitAmt" +
                                " from  com.intuit.sbd.payroll.psp.domain.FinancialTransaction finTxn," +
                                "       com.intuit.sbd.payroll.psp.domain.FinancialTransactionState finTxnState," +
                                "       com.intuit.sbd.payroll.psp.domain.PostingRule as postingRule," +
                                "       com.intuit.sbd.payroll.psp.domain.Law as law";
                String where =
                        " where finTxn.Company = :company" +
                                "   and finTxnState.FinancialTransaction = finTxn" +
                                "   and finTxn.TransactionType.TransactionTypeCd=postingRule.TransactionType.TransactionTypeCd" +
                                "   and finTxnState.TransactionState.TransactionStateCd=postingRule.TransactionState.TransactionStateCd" +
                                "   and finTxnState.TransactionStateEffectiveDate >= " +
                                "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                                "   and " +
                                Application.getTruncFunctionString("finTxnState.TransactionStateEffectiveDate") +
                                " >= " +
                                "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                                "   and postingRule.LedgerAccount.LedgerAccountCd = :ledgerAccountCode" +
                                "   and finTxn.TransactionType.TransactionTypeCd = com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.AgencyTaxOverpaymentApplied" +
                                "   and finTxn.PayrollRun is null" +
                                "   and finTxn.Law = law";

                if (pPaymentTemplate != null) {
                    where += "   and law.PaymentTemplate = :paymentTemplate";
                }

                if (pQuarter != null && pYear != null) {
                    select += "       , com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction mmt";
                    where += " and finTxn.MoneyMovementTransaction.Id = mmt.Id" +
                            " and mmt.PaymentPeriodBegin between :quarterStart and :quarterEnd";
                }

                String groupBy = " group by law.LawId";

                org.hibernate.Query hibernateQuery = Application.getHibernateSession().createQuery(select + where + groupBy);

                hibernateQuery.setParameter("company", pCompany);
                hibernateQuery.setParameter("ledgerAccountCode", ledgerAccountCode);

                if (pPaymentTemplate != null) {
                    hibernateQuery.setParameter("paymentTemplate", pPaymentTemplate);
                }

                if (pQuarter != null && pYear != null) {
                    hibernateQuery.setParameter("quarterStart", CalendarUtils.getFirstDayOfQuarter(pYear, pQuarter));
                    SpcfCalendar quarterEnd = CalendarUtils.getLastDayOfQuarter(pYear, pQuarter);
                    CalendarUtils.endOfDay(quarterEnd);
                    hibernateQuery.setParameter("quarterEnd", quarterEnd);
                }

                @SuppressWarnings({"unchecked"})
                List<Object[]> results = (List<Object[]>) hibernateQuery.list();

                for (Object[] result : results) {
                    Law law = Application.findById(Law.class, result[0]);
                    Map<String, SpcfMoney> amountMap = cachedMap.get(law);
                    if (amountMap == null) {
                        amountMap = new HashMap<String, SpcfMoney>();
                        amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                        amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                        cachedMap.put(law, amountMap);
                    }


                    if (result[1] != null) {
                        amountMap.put(CREDIT_INDICATOR, new SpcfMoney(amountMap.get(CREDIT_INDICATOR).add((SpcfMoney) result[1])));
                    }
                    if (result[2] != null) {
                        amountMap.put(DEBIT_INDICATOR, new SpcfMoney(amountMap.get(DEBIT_INDICATOR).add((SpcfMoney) result[2])));
                    }
                }
            }

            String select =
                    " select law.LawId," +
                            " sum(case when postingRule.CreditDebitInd = 'C' then finTxn.FinancialTransactionAmount else 0 end) as CreditAmt," +
                            " sum(case when postingRule.CreditDebitInd = 'D' then finTxn.FinancialTransactionAmount else 0 end) as DebitAmt" +
                            " from  com.intuit.sbd.payroll.psp.domain.FinancialTransaction finTxn," +
                            "       com.intuit.sbd.payroll.psp.domain.FinancialTransactionState finTxnState," +
                            "       com.intuit.sbd.payroll.psp.domain.PostingRule postingRule," +
                            "       com.intuit.sbd.payroll.psp.domain.Law as law";
            String where =
                    " where finTxn.Company = :company" +
                            "   and finTxnState.FinancialTransaction = finTxn" +
                            "   and finTxn.TransactionType.TransactionTypeCd=postingRule.TransactionType.TransactionTypeCd" +
                            "   and finTxnState.TransactionState.TransactionStateCd=postingRule.TransactionState.TransactionStateCd" +
                            "   and finTxnState.TransactionStateEffectiveDate >= " +
                            "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                            "   and " +
                            Application.getTruncFunctionString("finTxnState.TransactionStateEffectiveDate") +
                            " >= " +
                            "FN_DATE_ADD(" + Application.getTruncFunctionString("finTxn.CreatedDate") + ", -2 , 'day')" +
                            "   and postingRule.LedgerAccount.LedgerAccountCd = :ledgerAccountCode" +
                            "   and finTxn.TransactionType.TransactionTypeCd in (:transactionTypes)" +
                            "   and finTxn.Law = law";

            if (pPaymentTemplate != null) {
                where += "   and law.PaymentTemplate = :paymentTemplate";
            }


            if (payrollRunsToExclude.size() > 0) {
                where += "   and finTxn.PayrollRun not in (:excludedPayrollRuns)";
            }

            if (pQuarter != null && pYear != null) {
                select += " , com.intuit.sbd.payroll.psp.domain.PayrollRun pr";
                where += " and finTxn.PayrollRun = pr" +
                        " and pr.PaycheckDate between :quarterStart and :quarterEnd";
            }

            String groupBy = " group by law.LawId";

            org.hibernate.Query hibernateQuery = Application.getHibernateSession().createQuery(select + where + groupBy);

            hibernateQuery.setParameter("company", pCompany);
            hibernateQuery.setParameter("ledgerAccountCode", ledgerAccountCode);

            if (pPaymentTemplate != null) {
                hibernateQuery.setParameter("paymentTemplate", pPaymentTemplate);
            }

            postingTransactionTypeCodes.remove(TransactionTypeCode.AgencyTaxOverpaymentApplied);
            hibernateQuery.setParameterList("transactionTypes", postingTransactionTypeCodes);

            if (payrollRunsToExclude.size() > 0) {
                hibernateQuery.setParameterList("excludedPayrollRuns", payrollRunsToExclude);
            }

            if (pQuarter != null && pYear != null) {
                hibernateQuery.setParameter("quarterStart", CalendarUtils.getFirstDayOfQuarter(pYear, pQuarter));
                SpcfCalendar quarterEnd = CalendarUtils.getLastDayOfQuarter(pYear, pQuarter);
                CalendarUtils.endOfDay(quarterEnd);
                hibernateQuery.setParameter("quarterEnd", quarterEnd);
            }

            @SuppressWarnings({"unchecked"})
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();

            for (Object[] result : results) {
                Law law = Application.findById(Law.class, result[0]);
                Map<String, SpcfMoney> amountMap = cachedMap.get(law);
                if (amountMap == null) {
                    amountMap = new HashMap<String, SpcfMoney>();
                    amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                    amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                    cachedMap.put(law, amountMap);
                }


                if (result[1] != null) {
                    amountMap.put(CREDIT_INDICATOR, new SpcfMoney(amountMap.get(CREDIT_INDICATOR).add((SpcfMoney) result[1])));
                }
                if (result[2] != null) {
                    amountMap.put(DEBIT_INDICATOR, new SpcfMoney(amountMap.get(DEBIT_INDICATOR).add((SpcfMoney) result[2])));
                }
            }

            if (naturalKey != null) {
                Application.getSessionCache().addNonHibernateObject(naturalKey, cachedMap);
            }
        }

        // deep copy the cached map so that we don't cache the result map
        Map<Law, Map<String, SpcfMoney>> resultMap = new HashMap<Law, Map<String, SpcfMoney>>();
        for (Law law : cachedMap.keySet()) {
            Map<String, SpcfMoney> amountMap = new HashMap<String, SpcfMoney>();
            amountMap.put(CREDIT_INDICATOR, new SpcfMoney(cachedMap.get(law).get(CREDIT_INDICATOR)));
            amountMap.put(DEBIT_INDICATOR, new SpcfMoney(cachedMap.get(law).get(DEBIT_INDICATOR)));
            resultMap.put(law, amountMap);
        }

        // add transaction amounts for payrolls in memory
        for (PayrollRun payrollRun : payrollRunsToExclude) {
            Map<Law, Map<String, SpcfMoney>> payrollBalances =
                    LedgerAccount.findLedgerEntriesSumByAccountCodePaymentTemplateQuarterAndPayroll(ledgerAccountCode, payrollRun, pPaymentTemplate, pQuarter, pYear);
            for (Law law : payrollBalances.keySet()) {

                Map<String, SpcfMoney> payrollAmountMap = payrollBalances.get(law);
                if (payrollAmountMap == null) {
                    continue;
                }

                Map<String, SpcfMoney> amountMap = resultMap.get(law);
                if (amountMap == null) {
                    amountMap = new HashMap<String, SpcfMoney>();
                    amountMap.put(CREDIT_INDICATOR, SpcfMoney.ZERO);
                    amountMap.put(DEBIT_INDICATOR, SpcfMoney.ZERO);
                    resultMap.put(law, amountMap);
                }

                amountMap.put(CREDIT_INDICATOR, new SpcfMoney(amountMap.get(CREDIT_INDICATOR).add(payrollAmountMap.get(CREDIT_INDICATOR))));
                amountMap.put(DEBIT_INDICATOR, new SpcfMoney(amountMap.get(DEBIT_INDICATOR).add(payrollAmountMap.get(DEBIT_INDICATOR))));
            }
        }

        return resultMap;
    }

    private static SpcfDecimal getSUIAccountBalance(Company pCompany, SpcfCalendar pProcessingDate) {
        DomainEntitySet<FinancialTransaction> financialTransactions = null;
        if (Application.getSessionCache().isEntityCollectionCached(FinancialTransaction.class, FinancialTransaction.ATO_CACHE_KEY)) {
            financialTransactions = Application.getSessionCache().getEntityCollection(FinancialTransaction.class, FinancialTransaction.ATO_CACHE_KEY);
        }
        DomainEntitySet<FinancialTransaction> suiAdjustmentTransactions =
                PayrollServices.entityFinder.find(FinancialTransaction.class, createSUIAdjustmentCriterion(pCompany, pProcessingDate));
        //PSP-11857: Add manually recreated transactions to the existing DomainEntitySet
        suiAdjustmentTransactions.addAll(PayrollServices.entityFinder.find(FinancialTransaction.class, createReturnedSUIAdjustmentCriterion(pCompany, pProcessingDate)));

        if (financialTransactions != null && financialTransactions.size() > 0) {
            for (FinancialTransaction ft : financialTransactions) {
                if (!suiAdjustmentTransactions.contains(ft)) {
                    suiAdjustmentTransactions.add(ft);
                }
            }
        }
        SpcfDecimal balance = SpcfMoney.ZERO;
        for (FinancialTransaction suiAdjustmentTransaction : suiAdjustmentTransactions) {
            if (suiAdjustmentTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerSUITaxPayable) {
                balance = balance.add(suiAdjustmentTransaction.getFinancialTransactionAmount());
            } else if (suiAdjustmentTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerSUITaxReceivable) {
                balance = balance.subtract(suiAdjustmentTransaction.getFinancialTransactionAmount());
            }
        }
        return balance;
    }

    private static Criterion<FinancialTransaction> createSUIAdjustmentCriterion(Company pCompany, SpcfCalendar pProcessingDate) {
        // Get the collection of variance transactions that made up the balance
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(pProcessingDate);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(pProcessingDate);
        quarterEndDate.addDays(1);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                                                                                       TransactionTypeCode.EmployerSUITaxPayable))
                                    .And(FinancialTransaction.MoneyMovementTransaction().PaymentPeriodBegin().greaterOrEqualThan(quarterBeginDate))
                                    .And(FinancialTransaction.MoneyMovementTransaction().PaymentPeriodEnd().lessThan(quarterEndDate))
                                    .And(FinancialTransaction.OriginalTransaction().isNull());
        if (pCompany != null) {
            where = where.And(FinancialTransaction.Company().equalTo(pCompany));
        }

        return where;
    }

    /**
     * PSP-11857
     * Pick manually recreated transactions for balance calculation
     * @param pCompany
     * @param pProcessingDate
     * @return
     */
    private static Criterion<FinancialTransaction> createReturnedSUIAdjustmentCriterion(Company pCompany, SpcfCalendar pProcessingDate) {
        // Get the collection of returned and recreated variance transactions that made up the balance
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(pProcessingDate);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(pProcessingDate);
        quarterEndDate.addDays(1);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable))
                        .And(FinancialTransaction.MoneyMovementTransaction().PaymentPeriodBegin().greaterOrEqualThan(quarterBeginDate))
                        .And(FinancialTransaction.MoneyMovementTransaction().PaymentPeriodEnd().lessThan(quarterEndDate))
                        .And(FinancialTransaction.OriginalTransaction().isNotNull())
                        .And(FinancialTransaction.OriginalTransaction().CurrentTransactionState()
                                        .TransactionStateCd().in(TransactionStateCode.Returned))
                        .And(FinancialTransaction.OriginalTransaction().TransactionType()
                                                .TransactionTypeCd().equalTo(FinancialTransaction.TransactionType().TransactionTypeCd())
                        ) ;
        if (pCompany != null) {
            where = where.And(FinancialTransaction.Company().equalTo(pCompany));
        }
        return where;
    }
}
