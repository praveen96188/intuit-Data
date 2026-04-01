package com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jul 18, 2008
 * Time: 4:16:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessFraudulentPayrolls {
    private static SpcfLogger logger = Application.getLogger(ProcessFraudulentPayrolls.class);

    private PayrollFraudBatch mFraudBatch;
    private int interval;
    private int minPoolSize;
    private int maxPoolSize;
    private int maxWait;

    public ProcessFraudulentPayrolls() {
        interval = SystemParameter.findIntValue(SystemParameter.Code.FRAUD_CONTROLS_THREAD_POOL_INTERVAL, 60);
        maxWait = SystemParameter.findIntValue(SystemParameter.Code.FRAUD_CONTROLS_THREAD_POOL_MAX_WAIT, 5 * 60);
        minPoolSize = SystemParameter.findIntValue(SystemParameter.Code.FRAUD_CONTROLS_MIN_THREAD_POOL_SIZE, 10);
        maxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.FRAUD_CONTROLS_MAX_THREAD_POOL_SIZE, 40);
    }
    public PayrollFraudBatch getFraudBatch() {
        return mFraudBatch;
    }

    public static void main(String args[]) {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.FraudulentPayrollsBatchJob));

            // Fraudulent Payrolls process
            try {
                PayrollServices.beginUnitOfWork();

                new ProcessFraudulentPayrolls().processFraudulentPayrolls();

                PayrollServices.commitUnitOfWork();
            }
            finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
        catch (Throwable ex) {
            logger.fatal("Exception in ProcessFraudulentPayrolls.main() ", ex);
            System.exit(1);
        }
    }

    public void processFraudulentPayrolls() {
        logger.info("Fraudulent Payrolls Process Started");
        StopWatch stopWatch = new StopWatch().start();

        SpcfCalendar processStartTime = PSPDate.getPSPTime();
        logger.debug("After PSPDate.getPSPTime()");

        int batchSize = 320;
        try {
            SystemParameter systemParameter = SystemParameter.findSystemParameter("PROCESS_FRAUD_PAYROLLS_BATCH_SIZE");
            if (systemParameter != null) {
                batchSize = Integer.parseInt(systemParameter.getSystemParameterValue());
            }
        } catch (Throwable t) {
        }

        Application.getHibernateSession().setFlushMode(FlushMode.MANUAL);

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByFraudBatchFlag(batchSize);

        // We need to process all payrolls for a given company on the same thread, so we first group them
        int directDepositPayrollRunCount = 0;
        Map<Company, List<PayrollRun>> payrollsPerCompany = new HashMap<Company, List<PayrollRun>>();
        for (int i = 0; i < payrollRuns.size(); i++) {
            PayrollRun payrollRun = payrollRuns.get(i);

            List<PayrollRun> payrollRunsPerCompany = payrollsPerCompany.get(payrollRun.getCompany());
            if (payrollRunsPerCompany == null) {
                payrollRunsPerCompany = new ArrayList<PayrollRun>();
                payrollsPerCompany.put(payrollRun.getCompany(), payrollRunsPerCompany);
            }

            payrollRunsPerCompany.add(payrollRun);
            directDepositPayrollRunCount++;
        }

        // Process the payrolls
        logger.info("Number of DirectDeposit payrolls: " + directDepositPayrollRunCount + " - total payrolls in run: " + payrollRuns.size());
        multithreadPayrollRunProcessing(stopWatch, payrollsPerCompany);

        // All payrolls were processed
        logger.info("completed processing " + directDepositPayrollRunCount + " DirectDeposit payrolls (total payrolls: " + payrollRuns.size() + ") for fraud in " + stopWatch.getElapsedTimeString());
        Application.getHibernateSession().setFlushMode(FlushMode.AUTO);

        if (payrollRuns.size() > 0) {
            logger.info("Creating new payroll fraud batch");
            mFraudBatch = new PayrollFraudBatch();
            mFraudBatch.setStartTime(processStartTime);
            mFraudBatch.setEndTime(PSPDate.getPSPTime());
            mFraudBatch.setNumberOfPayrollsProcessed(payrollRuns.size());
            Application.save(mFraudBatch);
        }

        logger.info("Completed business logic for payroll fraud batch");
    }

    private void multithreadPayrollRunProcessing(StopWatch stopWatch, Map<Company, List<PayrollRun>> payrollsPerCompany) {
        ExecutorService threadPool = null;
        try {
            // Create threadPool with given parameters
            threadPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, interval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            // Load the suspect bank account list
            final Map<String, ArrayList<SuspectBankAccountInfo>> suspectEmployeeBankAccountMap = SuspectBankAccountInfo.loadSuspectBankAccountMap();

            // Process each company in a separate thread
            int numberOfProcessedCompanies = 0;
            for (List<PayrollRun> companyPayrollList : payrollsPerCompany.values()) {
                numberOfProcessedCompanies++;
                final List<PayrollRun> finalPayrollRunsPerCompany = companyPayrollList;
                completionService.submit(new Callable<Integer>() {
                    public Integer call() {
                        return processPayrollRunsForCompany(finalPayrollRunsPerCompany, suspectEmployeeBankAccountMap);
                    }
                });
            }

            // Get the results of each thread execution
            int ddPayrollsCount = 0;
            try {
                for (int t = 0; t < numberOfProcessedCompanies; t++) {
                    Future<Integer> f = completionService.take();
                    ddPayrollsCount += f.get();

                    if (ddPayrollsCount % 40 == 0) {
                        logger.info("working -- completed processing " + (ddPayrollsCount + 1) + " payrolls (" + ddPayrollsCount + "DD payrolls) for fraud in " + stopWatch.getElapsedTimeString());
                    }
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException e) {
                ThreadingUtils.launderThrowable(e.getCause());
            }
        }
        finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, interval, maxWait);
            }

        }
    }

    private int processPayrollRunsForCompany(List<PayrollRun> payrollRuns, Map<String, ArrayList<SuspectBankAccountInfo>> suspectEmployeeBankAccountMap) {
        int ddPayrollsCount = 0;
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.FraudulentPayrollsBatchJob));

            Application.beginUnitOfWork(FlushMode.MANUAL);

            // Inject suspect bank accounts into session, so it is not read again
            Application.getSessionCache().addNonHibernateObject("SuspectEmployeeBankAccountMap", suspectEmployeeBankAccountMap);

            boolean companyHasEvent = false;
            for (PayrollRun payrollRun : payrollRuns) {
                payrollRun = PayrollRun.findPayrollRun(payrollRun.getId());

                //Only perform these fraud checks if the payroll contains an EmployerDdDebit
                DomainEntitySet<FinancialTransaction> employerDdDebits = payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit}, null);

                Company company = payrollRun.getCompany();
                if (!employerDdDebits.isEmpty() || company.hasService(ServiceCode.RiskAssessment)) {
                    //workaround to get around the fact that we are now using MANUAL flushing...this is the only event we actually look for that this ProcessFraudulentPayrolls creates
                    if (!companyHasEvent) {
                        boolean createdEventForCompany = payrollRun.numberOfPayrollsPerDayExceeded();
                        if (createdEventForCompany) {
                            companyHasEvent = true;
                        }
                    }

                    switch (payrollRun.getPayrollRunType()) {
                        case Regular:
                            //Regular Payroll Controls
                            if(company.getSourceSystemCd().equals(SourceSystemCode.QBOE)){
                                payrollRun.singleEmployeePercentageIncrease();
                                payrollRun.employeePaidTooManyTimes();
                                payrollRun.employeePaidPercentageGreaterThanOthers();
                                payrollRun.employeesPaidToTheSameBank();
                            }
                            //payrollRun.employeePaidGreaterThanMax();
                            payrollRun.employeePaidGreaterThanMaxForBankAcctUpdate();
                            payrollRun.totalPayrollExceedsLimit();
                            payrollRun.employeePaidEvenDollarAmount();
                            payrollRun.currentPayrollPercentageIncrease();
                            payrollRun.payrollProcessedTooSoon();
                            //payrollRun.isEmployeeInTerminatedCompany(); //DWeinberg [5/7/10] Removing this because a) creates only false positives and b) performs very poorly
                            //payrollRun.isEmployeeBankAccountInTerminatedOrFraudHoldCompany();
                            payrollRun.employeeBankAccountChanged();
                            payrollRun.employeesPaidToTheSameBankAccount();
                            payrollRun.checkEmployeeInactivityFraud();
                            break;

                        case BillPayment:
                            // Bill Payment Controls
                            payrollRun.totalBillPaymentSubmissionExceedsLimit();
                            //payrollRun.payeePaidGreaterThanMax();
                            payrollRun.payeePaidGreaterThanMaxForBankAcctUpdate();
                            if(company.getSourceSystemCd().equals(SourceSystemCode.QBOE)){
                                payrollRun.payeePaidTooManyTimes();
                                payrollRun.employeesPaidToTheSameBank();
                            }
                            payrollRun.payrollProcessedTooSoon();
                            payrollRun.checkPayeeInactivityFraud();
                            payrollRun.payeePaidEvenDollarAmount();
                            //payrollRun.isPayeeBankAccountInTerminatedOrFraudHoldCompany();
                            break;
                    }
                    ddPayrollsCount++;
                }

                payrollRun.setProcessedByFraudBatchJob(true);
            }

            Application.commitUnitOfWork();
        }
        catch (Throwable t) {
            StringBuilder errorMessage = new StringBuilder("Error processing company: " + payrollRuns.get(0).getCompany().getLegalName() + " Payrolls not processed: ");
            PayrollServices.rollbackUnitOfWork();

            PayrollServices.beginUnitOfWork();

            // Update the fraud tokens so these payrolls get picked up by next fraud run
            for (PayrollRun payrollRun : payrollRuns) {
                payrollRun = Application.refresh(payrollRun);
                errorMessage.append(payrollRun.getId().toString() + ", ");
            }

            errorMessage.append(" :" + t.getMessage());

            PayrollServices.commitUnitOfWork();

            logger.error(errorMessage, t);
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return ddPayrollsCount;
    }
}