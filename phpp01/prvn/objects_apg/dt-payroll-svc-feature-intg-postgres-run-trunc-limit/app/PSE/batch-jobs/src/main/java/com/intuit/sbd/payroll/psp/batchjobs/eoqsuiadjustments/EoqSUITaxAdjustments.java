package com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
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
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class EoqSUITaxAdjustments {
    private static final SpcfLogger logger = Application.getLogger(EoqSUITaxAdjustments.class);

    private static SpcfCalendar mProcessingDateArg;
    private static String mProcssingMessage;
    private static boolean mCommit = false;

    private static final String PROCESSING_DATE_COMMAND = "-processingDate";
    private static final String PROCESSING_MSG_COMMAND = "-processingMessage";
    private static final String COMMIT_COMMAND = "-commit";

    private SpcfCalendar processingDate;

    private PSPRequestContextManager pspRequestContextManager;

    public EoqSUITaxAdjustments() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /*
     * main method to be invoked by the scheduler
     *
     */

    public static void main(String[] args) {
        parseArgs(args);
        new EoqSUITaxAdjustments().process(mProcessingDateArg, mProcssingMessage, mCommit);
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(PROCESSING_DATE_COMMAND)) {
                    mProcessingDateArg = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);
                } else if (argParts[0].equals(PROCESSING_MSG_COMMAND)) {
                    mProcssingMessage = argParts[1];
                } else if (argParts[0].equals(COMMIT_COMMAND)) {
                    mCommit = Boolean.valueOf(argParts[1]);
                }
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }
    }

    /*
     * Process EOQ SUI Adjustments
     *
     */
    public void process(SpcfCalendar pProcessingDate, String pProcessingMessage, boolean pCommit) {
        if (pProcessingDate == null) {
            throw new RuntimeException("Invalid processing date (null).");
        }
        StopWatch swTotalProcess = StopWatch.startTimer();     // output total time at the end using this
        ExecutorService threadPool = null;
        try {
            threadPool = ThreadingUtils.createNewFixedThreadPool();
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);
            PayrollServices.beginUnitOfWork();
            processingDate = SpcfCalendar.createInstance(pProcessingDate.getYear(), pProcessingDate.getMonth(),
                                                         pProcessingDate.getDay(), SpcfTimeZone.getLocalTimeZone());
            Expression<FinancialTransaction> query = new Query<FinancialTransaction>()
                    .Select(FinancialTransaction.Company().Id().Distinct())
                    .Where(createSUIAdjustmentCriterion(null, processingDate))
                    .OrderBy(FinancialTransaction.Company().Id());

            ScrollableResults companyIds = Application.findScrollable(FinancialTransaction.class, query);

            int totalCompanies = 0;
            try {
                while (companyIds.next()) {
                    processCompany(completionService, ((SpcfUniqueId) companyIds.get(0)), pProcessingMessage, pCommit);
                    totalCompanies++;
                }
            } catch (GenericJDBCException ex) {
                // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
                if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                    throw new RuntimeException(ex);
                }
            } finally {
                companyIds.close();
            }
            // Get the results of each thread execution
            int companyCount = 0;
            try {
                for (int t = 0; t < totalCompanies; t++) {
                    companyCount++;
                    Future<Integer> f = completionService.take();
                    if (companyCount % 100 == 0) {
                        logger.info("working -- completed processing " + companyCount + " companies");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            swTotalProcess.stop();
            String message = String.format("Done processing: processed %,d companies in %s",
                                           totalCompanies,
                                           swTotalProcess.getElapsedTimeString());
            logger.info(message);
            if (pCommit) {
                logger.info("Committing updates ");
                PayrollServices.commitUnitOfWork();
            }
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool);
            }
            PayrollServices.rollbackUnitOfWork();
        }

    }

    private void processCompany(CompletionService<Integer> completionService, final SpcfUniqueId companyId, final String processingMessage, final boolean pCommit) {
        completionService.submit(new Callable<Integer>() {
            public Integer call() {
                return processCompany(companyId, processingDate, processingMessage, pCommit);
            }
        });
    }

    private int processCompany(SpcfUniqueId pCompany, SpcfCalendar pProcessingDate, String pProcessingMessage, boolean pCommit) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EoqSUITaxAdjustmentsBatchJob);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Company company = PayrollServices.entityFinder.findById(Company.class, pCompany);
            pspRequestContextManager.setRequestContext(company, RequestType.OLAP, BatchJobType.EoqSUIAdjustments.toString());
            SpcfCalendar previousQuarterDate = CalendarUtils.getLastDayOfPreviousQuarter(pProcessingDate);
            DomainEntitySet<FinancialTransaction> suiAdjustmentTransactions =
                    PayrollServices.entityFinder.find(FinancialTransaction.class, createSUIAdjustmentCriterion(company, pProcessingDate));
            //PSP-11857: Add manually recreated transactions to the existing DomainEntitySet
            suiAdjustmentTransactions.addAll(PayrollServices.entityFinder.find(FinancialTransaction.class, createReturnedSUIAdjustmentCriterion(company, pProcessingDate)));
            //Return in case there are no transactions to be processed
            if (suiAdjustmentTransactions.isEmpty()) {
                return 0;
            }
            // Find the balance for ERSUI and create either debit or credit
            SpcfDecimal balance = SpcfMoney.ZERO;
            for (FinancialTransaction suiAdjustmentTransaction : suiAdjustmentTransactions) {
                if (suiAdjustmentTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerSUITaxPayable) {
                    balance = balance.add(suiAdjustmentTransaction.getFinancialTransactionAmount());
                } else if (suiAdjustmentTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerSUITaxReceivable) {
                    balance = balance.subtract(suiAdjustmentTransaction.getFinancialTransactionAmount());
                }
            }
            // Settlement Date = 2 business days after processing date
            SpcfCalendar adjustmentSettlementDate = pProcessingDate.copy();
            SpcfCalendar today = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(today);
            if (adjustmentSettlementDate.before(today)) {
                adjustmentSettlementDate = today;
            }
            CalendarUtils.addBusinessDays(adjustmentSettlementDate, 2);
            // Create Adjustment payroll run
            PayrollRun payrollRun = PayrollRun.createAdjustmentPayrollRun(company, previousQuarterDate);
            PayrollRun.getPayrollsInMemory(company).add(payrollRun);
            Application.getSessionCache().addPrimaryKey(payrollRun.getNaturalKey(), payrollRun.getId());
            // Get Company and Intuit Bank Accounts
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
            if (companyBankAccount == null) {
                throw new RuntimeException("Company does not have an active bank account");
            }
            // If balance is negative it means the company owes Intuit, so create Employer Tax Debit
            // Otherwise, create Employer Tax Credit
            FinancialTransaction erTaxFT;
            FinancialTransaction erSUITaxAdjustment = null;
            EventTypeCode eventTypeCode = null;
            if (balance.isLessThan(SpcfMoney.ZERO)) {
                // Create ER debit
                erTaxFT = FinancialTransaction.createERDebitTransaction(payrollRun, companyBankAccount,
                        TransactionTypeCode.EmployerTaxDebit,
                        new SpcfMoney(balance.negate()),
                        SettlementType.ACH,
                        adjustmentSettlementDate,
                        payrollRun.getCompany().getService(ServiceCode.Tax));
                // Create ER SUI Tax Collection
                erSUITaxAdjustment = FinancialTransaction.createFinancialTransaction(company, payrollRun, null, null, null,
                        BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.EmployerSUITaxCollection,
                        new SpcfMoney(balance.negate()),
                        SettlementType.ApplyForward,
                        adjustmentSettlementDate, null);
                erSUITaxAdjustment.setRelatableTransaction(erTaxFT);
                erSUITaxAdjustment.updateFinancialTransactionState(TransactionStateCode.Executed);
                erSUITaxAdjustment.updateFinancialTransactionState(TransactionStateCode.Completed);
                eventTypeCode = EventTypeCode.SUIEoqDebitCreated;
            } else if (balance.isGreaterThan(SpcfMoney.ZERO)) {
                // Create ER Credit
                IntuitBankAccount intuitBankAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.EmployerTaxCredit, CreditDebitCode.Debit);
                if (intuitBankAccount == null) {
                    throw new RuntimeException("Intuit Bank Account not found for EmployerTaxCredit transaction type");
                }
                erTaxFT = FinancialTransaction.createFinancialTransaction(company, payrollRun, null,
                        companyBankAccount.getBankAccount(),
                        intuitBankAccount.getBankAccount(),
                        BankAccountOwnerType.Company,
                        BankAccountOwnerType.Intuit,
                        TransactionTypeCode.EmployerTaxCredit,
                        new SpcfMoney(balance),
                        SettlementType.ACH,
                        adjustmentSettlementDate);
                // Create ER SUI Tax Refund
                erSUITaxAdjustment =
                        FinancialTransaction.createFinancialTransaction(company, payrollRun, null, null, null,
                                BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit, TransactionTypeCode.EmployerSUITaxRefund,
                                new SpcfMoney(balance),
                                SettlementType.ApplyForward,
                                adjustmentSettlementDate, null);
                erSUITaxAdjustment.setRelatableTransaction(erTaxFT);
                erSUITaxAdjustment.updateFinancialTransactionState(TransactionStateCode.Executed);
                erSUITaxAdjustment.updateFinancialTransactionState(TransactionStateCode.Completed);
                eventTypeCode = EventTypeCode.SUIEoqCreditCreated;
            }
            if (erSUITaxAdjustment != null) {
                for (FinancialTransaction ft : suiAdjustmentTransactions) {
                    erSUITaxAdjustment.addAssociatedTransactions(ft);
                    ft.setOriginalTransaction(erSUITaxAdjustment);
                    erSUITaxAdjustment.addAssociatedTransactions(ft);
                }
                ProcessResult processResult = new ProcessResult();
                // create a liability check for the liability adjustments
                for (PayrollRun memoryPayrollRun : PayrollRun.getPayrollsInMemory(company)) {
                    processResult.merge(PayrollServices.companyManager.generateLiabilityChecks(company, memoryPayrollRun));
                    // Create Event
                    if (eventTypeCode != null && pProcessingMessage == null) {
                        CompanyEvent.createSUIAdjustmentEvent(memoryPayrollRun, eventTypeCode);
                    } else if (pProcessingMessage != null) {
                        CompanyEvent.createCompanyEventAndDetail(company, EventTypeCode.ManualNoteEvent, EventDetailTypeCode.NoteText, pProcessingMessage);
                    }
                }
                if (pCommit) {
                    if (processResult.isSuccess()) {
                        PayrollServices.commitUnitOfWork();
                    } else {
                        throw new RuntimeException(processResult.toString());
                    }
                }
            }
            if (payrollRun != null && payrollRun.updateEETotalsCalculationRequired()) {
                EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
            }

        } catch (Throwable t) {
            logger.error("Error processing company " + pCompany.toString(), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
        return 0;
    }

    private static Criterion<FinancialTransaction> createSUIAdjustmentCriterion(Company pCompany, SpcfCalendar pProcessingDate) {
        // Get the collection of variance transactions that made up the balance
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(pProcessingDate);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(pProcessingDate);
        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed)
                                    .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                                                                                       TransactionTypeCode.EmployerSUITaxPayable))
                                    .And(FinancialTransaction.SettlementDate().between(quarterBeginDate, quarterEndDate));

        /**PSP-11857: Removed OriginalTransaction is Null condition to include all the companies that satisfy above filter criteria and
         * Added it to if clause to check this criteria only while filtering the transactions for each company.
         */
        if (pCompany != null) {
            where = where
                    .And(FinancialTransaction.OriginalTransaction().isNull())
                    .And(FinancialTransaction.Company().equalTo(pCompany));
        }
        return where;
    }

    /**
     * PSP-11857
     * Pick manually recreated transactions for processing
     * @param pCompany
     * @param pProcessingDate
     * @return
     */
    private static Criterion<FinancialTransaction> createReturnedSUIAdjustmentCriterion(Company pCompany, SpcfCalendar pProcessingDate) {
        // Get the collection of variance returned and recreated transactions that made up the balance
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(pProcessingDate);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(pProcessingDate);
        Criterion<FinancialTransaction> where =
                FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed)
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerSUITaxReceivable,
                                TransactionTypeCode.EmployerSUITaxPayable))
                        .And(FinancialTransaction.SettlementDate().between(quarterBeginDate, quarterEndDate))
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
