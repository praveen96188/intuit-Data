package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.PayrollTaxHelper;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.net.SpcfMalformedUrlException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Sep 17, 2011
 * Time: 11:04:41 AM
 */
public class StateACHPaymentCleanup {
    private static SpcfLogger logger = Application.getLogger(StateACHPaymentCleanup.class);

    public static void main(String[] args) {
        boolean commitArg = false;
        if (args.length > 0) {
            commitArg = Boolean.parseBoolean(args[0]);
        }
        final Boolean commit = commitArg;

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AS400Migration);

        logger.info("Beginning Transaction cleanup...");

        StringBuilder report = new StringBuilder();
        report.append("\nCommit is set to ").append(commit).append("\n");

        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * (2);
        logger.info("Creating thread pool with " + threadCount + " threads");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            final DomainEntitySet<PaymentTemplate> supportedPaymentTemplates =
                    Application.find(PaymentTemplate.class, PaymentTemplate.SupportStartDate().greaterOrEqualThan(SpcfCalendar.createInstance(2011, 9, 30, SpcfTimeZone.getLocalTimeZone())));
            DomainEntitySet<MoneyMovementTransaction> supportedMoneyMovementTransactions = findSupportedMoneyMovementTransactions(supportedPaymentTemplates, null, false);


            String foundPayments = "Found " + supportedMoneyMovementTransactions.size() + " payments to be fixed. \n";
            logger.info(foundPayments);
            report.append(foundPayments);

            final Set<SpcfUniqueId> companyIds = new HashSet<SpcfUniqueId>();
            for (MoneyMovementTransaction supportedMoneyMovementTransaction : supportedMoneyMovementTransactions) {
                SpcfUniqueId companyId = supportedMoneyMovementTransaction.getCompany().getId();
                if (!companyIds.contains(companyId)) {
                    companyIds.add(companyId);
                }
            }

            String foundCompanies = "Found " + companyIds.size() + " companies with payments. \n";
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
                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));
                            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                            Company company = Application.findById(Company.class, companyId);

                            DomainEntitySet<MoneyMovementTransaction> supportedCompanyMoneyMovementTransactions = findSupportedMoneyMovementTransactions(supportedPaymentTemplates, company, true);

                            // // cancel all of the financial transactions and collect the associated payroll runs
                            DomainEntitySet<FinancialTransaction> transactionsToCancel = new DomainEntitySet<FinancialTransaction>();
                            DomainEntitySet<PayrollRun> companyPayrollRuns = new DomainEntitySet<PayrollRun>();
                            for (MoneyMovementTransaction moneyMovementTransaction : supportedCompanyMoneyMovementTransactions) {
                                moneyMovementTransaction.setMoneyMovementPaymentMethod(null);
                                for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                                    if (!companyPayrollRuns.contains(financialTransaction.getPayrollRun())) {
                                        companyPayrollRuns.add(financialTransaction.getPayrollRun());
                                    }
                                    transactionsToCancel.add(financialTransaction);
                                }
                            }

                            int numberOfPayrolls = companyPayrollRuns.size();
                            String foundPayrolls = "Found " + numberOfPayrolls + " payrolls for company " + company.getSourceCompanyId() + "\n";
                            logger.info(foundPayrolls);
                            companyReport.append(foundPayrolls);

                            for (FinancialTransaction financialTransaction : transactionsToCancel) {
                                financialTransaction.cancelFinancialTransaction();
                            }

                            logger.info("cancelled " + transactionsToCancel.size() + " transactions for " + company.getSourceCompanyId());

                            companyReport.append("\nPayrolls for company: ").append(company.getSourceCompanyId()).append("\n");

                            companyPayrollRuns.sort(PayrollRun.PayrollRunDate());
                            int numPayrolls = 0;
                            for (PayrollRun payrollRun : companyPayrollRuns) {
                                companyReport.append("\tId:             ").append(payrollRun.getId().toString()).append("\n")
                                        .append("\tRun Date:       ").append(payrollRun.getPayrollRunDate().toString()).append("\n")
                                        .append("\tCheck Date:     ").append(payrollRun.getPaycheckDate().toString()).append("\n");
                                FinancialTransaction taxDebit = payrollRun.getEmployerTaxDebitTransaction();
                                companyReport.append("\tInitial Debit:  $").append(taxDebit.getFinancialTransactionAmount().toString()).append("\n");
                                // Build the Law Map for this payrollrun
                                HashMap<Law, SpcfDecimal> lawMap = new HashMap<Law, SpcfDecimal>();
                                DomainEntitySet<FinancialTransaction> payrollCanceledFTs = transactionsToCancel.find(FinancialTransaction.PayrollRun().equalTo(payrollRun));

                                for (FinancialTransaction ft : payrollCanceledFTs) {
                                    SpcfMoney lawAmount = new SpcfMoney(lawMap.get(ft.getLaw()) != null ?  lawMap.get(ft.getLaw()) : SpcfMoney.ZERO);
                                    if (TransactionType.addsToPayment(ft.getTransactionType() != null ? ft.getTransactionType().getTransactionTypeCd() : null)) {
                                        lawMap.put(ft.getLaw(), lawAmount.add(ft.getFinancialTransactionAmount()));
                                    } else if (TransactionType.subtractsFromPayment(ft.getTransactionType().getTransactionTypeCd())) {
                                        lawMap.put(ft.getLaw(), lawAmount.subtract(ft.getFinancialTransactionAmount()));
                                    }
                                }

                                // Add a 941 law
                                Law fit = Application.findById(Law.class, Law.FIT);
                                lawMap.put(fit, SpcfMoney.ZERO);
                                
                                PayrollTaxHelper.updateTaxTransactions(payrollRun, lawMap);

                                taxDebit = payrollRun.getEmployerTaxDebitTransaction();
                                companyReport.append("\tNew Debit:      $").append(taxDebit.getFinancialTransactionAmount().toString()).append("\n\n");

                                logger.info("updated tax transactions on " + ++numPayrolls + " of " + companyPayrollRuns.size() + " payrolls for " + company.getSourceCompanyId());
                            }

                            logger.info("completed updating tax transactions on " + company.getSourceCompanyId());

                            if (commit) {
                                logger.info("Committing updates for " + company.getSourceCompanyId());
                                PayrollServices.commitUnitOfWork();
                            }

                            success = true;
                        } catch (Throwable t) {
                            logger.error("Error processing company " + companyId.toString(), t);
                        } finally {
                            PayrollServices.rollbackUnitOfWork();
                        }
                        if (!success) {
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
                    logger.error("Failed to fix " + companyId.toString());
                }
            }

            logger.info("Success on " + successes + " companies out of " + companyIds.size());

            logger.info("Finished fixing transactions");
        } catch (Throwable t) {
            logger.error("An error occurred", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
        }

        logger.info(report.toString());

    }

    // find all mmts created for supported templates

    private static DomainEntitySet<MoneyMovementTransaction> findSupportedMoneyMovementTransactions(DomainEntitySet<PaymentTemplate> supportedPaymentTemplates, Company company, boolean eagerLoadTransactions) {
        Criterion<MoneyMovementTransaction> where = MoneyMovementTransaction.InitiationDate().greaterOrEqualThan(SpcfCalendar.createInstance(2011, 9, 28, SpcfTimeZone.getLocalTimeZone()))
                .And(MoneyMovementTransaction.InitiationDate().lessOrEqualThan(SpcfCalendar.createInstance(2012, 2, 3, SpcfTimeZone.getLocalTimeZone())))
                .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit))
                .And(MoneyMovementTransaction.PaymentTemplate().in(supportedPaymentTemplates.toArray(new PaymentTemplate[supportedPaymentTemplates.size()])));

        if (company != null) {
            where = where.And(MoneyMovementTransaction.Company().equalTo(company));
        }

        if (eagerLoadTransactions) {
            return Application.find(MoneyMovementTransaction.class, new Query<MoneyMovementTransaction>().Where(where)
                    .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.FinancialTransactionSet()));
        } else {
            return Application.find(MoneyMovementTransaction.class, new Query<MoneyMovementTransaction>().Where(where)
                    .EagerLoad(MoneyMovementTransaction.Company()));
        }
    }
}
