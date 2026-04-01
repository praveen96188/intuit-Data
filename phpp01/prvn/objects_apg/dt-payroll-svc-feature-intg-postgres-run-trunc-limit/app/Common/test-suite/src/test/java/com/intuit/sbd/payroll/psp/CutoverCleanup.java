package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.LiabilityBalances;
import com.intuit.sbd.payroll.psp.processes.PayrollTaxHelper;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Sep 17, 2011
 * Time: 11:04:41 AM
 */
public class CutoverCleanup {
    private static SpcfLogger logger = Application.getLogger(CutoverCleanup.class);

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

            // Find all PayrollRuns that were offloaded and fts were cancelled but not recreated
            final List<SpcfUniqueId> payrollRunIds = getAffectedPayrolls();

            String foundPayrollRuns = "Found " + payrollRunIds.size() + " payrolls to be fixed. \n";
            logger.info(foundPayrollRuns);
            report.append(foundPayrollRuns);

            CompletionService<StringBuilder> completionService = new ExecutorCompletionService<StringBuilder>(executor);

            for (final SpcfUniqueId payrollRunId : payrollRunIds) {
                completionService.submit(new Callable<StringBuilder>() {
                    public StringBuilder call() {
                        StringBuilder companyReport = new StringBuilder();
                        boolean success = false;
                        try {
                            Application.initialize();
                            ApplicationSecondary.initialize();
                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));
                            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                            PayrollRun payrollRun = Application.findById(PayrollRun.class, payrollRunId);
                            Map<Law, SpcfDecimal> lawAmountMap = null;
                            lawAmountMap = LiabilityBalances.getLiabilityBalances(payrollRun, false);

                            // Remove federal laws from the map
                            for (Law law : getFederalLaws()) {
                                lawAmountMap.remove(law);
                            }

                            // Create fts
                            PayrollTaxHelper.createTaxTransactions(payrollRun, new PayrollTaxHelper.CreateTaxTransactionsOptions(false, lawAmountMap));

                            // Report: total AgencyTaxCredits are equal to the EmployerTaxDebit
                            SpcfCalendar nowMinus1Hour = PSPDate.getPSPTime().copy();
                            nowMinus1Hour.addHours(-1);

                            SpcfMoney totalAgencyTaxCreditAmounts = new SpcfMoney("0.0");
                            SpcfMoney employerTaxDebitAmount = new SpcfMoney("0.0");
                            for (FinancialTransaction ft : payrollRun.getFinancialTransactionCollection()) {
                                if (ft.getCreatedDate().compareTo(nowMinus1Hour) > 0) {
                                    logger.info("New financial transaction being created: transaction type " + ft.getTransactionType().getTransactionTypeCd().toString() + "  amount " + ft.getFinancialTransactionAmount().toString() + " law: " + (ft.getLaw() != null ? ft.getLaw().getLawAbbrev() + "-" + ft.getLaw().getPaymentTemplate().getPaymentTemplateCd().toString() : "null"));
                                    companyReport.append("New financial transaction being created: transaction type " + ft.getTransactionType().getTransactionTypeCd().toString() + "  amount " + ft.getFinancialTransactionAmount().toString() + " law: " + (ft.getLaw() != null ? ft.getLaw().getLawAbbrev() : "null") + "\n");
                                }

                                if (ft.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Cancelled &&
                                        ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxDebit ||
                                        ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxCreditApplied) {
                                    employerTaxDebitAmount = (SpcfMoney) employerTaxDebitAmount.add(ft.getFinancialTransactionAmount());
                                }
                                if (ft.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created &&
                                        ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxCredit) {
                                    totalAgencyTaxCreditAmounts = (SpcfMoney) totalAgencyTaxCreditAmounts.add(ft.getFinancialTransactionAmount());
                                }
                            }
                            if (employerTaxDebitAmount.compareTo(totalAgencyTaxCreditAmounts) == 0) {
                                companyReport.append("Total agency tax credits match employer tax debit + employer tax credit applied for payroll " + payrollRunId.toString());
                            } else {
                                logger.info("Total agency tax credits does not match employer tax debit + employer tax credit applied for payroll " + payrollRunId.toString());
                                logger.info("Total agency tax credits: " + totalAgencyTaxCreditAmounts.toString());
                                logger.info("Employer tax debit: " + employerTaxDebitAmount.toString());
                            }

                            logger.info("recreated transactions for payroll " + payrollRun.getId());

                            if (commit) {
                                logger.info("Committing updates for payroll " + payrollRun.getId());
                                PayrollServices.commitUnitOfWork();
                            }

                            success = true;
                        } catch (Throwable t) {
                            logger.error("Error processing payroll run " + payrollRunId.toString(), t);
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

            for (SpcfUniqueId companyId : payrollRunIds) {
                Future<StringBuilder> f = completionService.take();
                StringBuilder companyReport = f.get();
                total++;
                logger.info("Completed processing " + total + " of " + payrollRunIds.size() + " payroll runs");
                if (companyReport != null) {
                    successes++;
                    report.append(companyReport);
                } else {
                    logger.error("Failed to fix " + companyId.toString());
                }
            }

            logger.info("Success on " + successes + " payroll runs out of " + payrollRunIds.size());

            logger.info("Finished fixing transactions");
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

    private static List<SpcfUniqueId> getAffectedPayrolls() {
        String sqlStmt = "SELECT distinct  payroll_run_fk\n" +
                "FROM psp_financial_transaction ft3 \n" +
                "     LEFT JOIN psp_law l ON l.law_id = ft3.law_fk\n" +
                "WHERE transaction_type_fk != 'EmployerTaxDebit'\n" +
                "        AND payment_template_fk != 'IRS-941-PAYMENT'\n" +
                "        AND payment_template_fk != 'IRS-940-PAYMENT'\n" +
                "        AND current_transaction_state_fk = 'Cancelled'\n" +
                "        AND payroll_run_fk IN (\n" +
                "            SELECT payroll_run_fk\n" +
                "            FROM psp_financial_transaction ft \n" +
                "                 INNER JOIN psp_money_movement_transaction mmt ON ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq\n" +
                "            WHERE ft.transaction_type_fk = 'EmployerTaxDebit'\n" +
                "                     AND mmt.initiation_date = TIMESTAMP '2011-09-29 07:00:00'\n" +
                "                     AND mmt.money_movement_payment_method = 'ACHDirectDeposit'\n" +
                "                     AND EXISTS (\n" +
                "                         SELECT 'T'\n" +
                "                         FROM psp_financial_transaction ft2\n" +
                "                         WHERE ft2.transaction_type_fk = 'AgencyTaxCredit'\n" +
                "                                 AND ft2.payroll_run_fk = ft.payroll_run_fk\n" +
                "                                 AND ft2.law_fk IN (\n" +
                "                                     SELECT law_id\n" +
                "                                     FROM psp_law l JOIN psp_payment_template pt ON l.payment_template_fk = pt.payment_template_cd\n" +
                "                                 WHERE pt.support_start_date > DATE '2011-09-30')))\n" +
                "";


        List<SpcfUniqueId> results = new ArrayList<SpcfUniqueId>();
        Statement statement = null;
        try {
            statement = Application.getConnection().createStatement();
            statement.executeQuery(sqlStmt);
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                results.add(SpcfUniqueId.createInstance(resultSet.getString("payroll_run_fk")));
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Throwable tt) {
                    throw new RuntimeException(tt);
                }
            }
        }

        return results;
    }

    private static DomainEntitySet<Law> getFederalLaws() {
        DomainEntitySet<Law> laws = new DomainEntitySet<Law>();
        PaymentTemplate federalPT = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        laws.addAll(federalPT.getLawCollection());
        federalPT = PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT");
        laws.addAll(federalPT.getLawCollection());

        return laws;
    }

}
