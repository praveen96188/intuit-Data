package com.intuit.sbd.payroll.psp.batchjobs.offload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.hibernate.ScrollableResults;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * User: dhaddan
 * Date: Sep 23, 2008
 * Time: 11:53:33 AM
 */
public class CreateTransactionOffloadedEvents {
    private static SpcfLogger logger = Application.getLogger(CreateTransactionOffloadedEvents.class);

    private int mInterval;
    private int mMinPoolSize;
    private int mMaxPoolSize;
    private int mMaxWait;

    private PSPRequestContextManager pspRequestContextManager;

    public CreateTransactionOffloadedEvents() {
        mInterval = SystemParameter.findIntValue(SystemParameter.Code.OFFLOAD_EVENTS_THREAD_POOL_INTERVAL, 60);
        mMaxWait = SystemParameter.findIntValue(SystemParameter.Code.OFFLOAD_EVENTS_THREAD_POOL_MAX_WAIT, 5 * 60);
        mMinPoolSize = SystemParameter.findIntValue(SystemParameter.Code.OFFLOAD_EVENTS_MIN_THREAD_POOL_SIZE, 10);
        mMaxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.OFFLOAD_EVENTS_MAX_THREAD_POOL_SIZE, 40);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * Create offloaded events for offload batches that haven't been processed by this batch job yet
     */
    public void createTransactionOffloadedEvents() {
        //retrieve all offload batches that haven't been processed yet
        ExecutorService threadPool = null;
        try {
            threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval,
                                                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(threadPool);

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            logger.info("Beginning to queue transaction offload event creation");

            DomainEntitySet<OffloadBatch> offloadBatches = OffloadBatch.getOffloadBatchesForOffloadEventCreation();

            logger.info("Queueing transaction offloaded events for " + offloadBatches.size() + " batch(es)");

            int numEmailGroups = 0;
            for (OffloadBatch currBatch : offloadBatches) {
                logger.info("Queueing fee events");
                //for each offload batch, retrieve all the QBDT fees
                ScrollableResults finTxns = FinancialTransaction.findFeeTransactionsForOffloadedBatch(currBatch, -1);
                while (finTxns.next()) {
                    ++numEmailGroups;
                    final SpcfUniqueId currTxnId = (SpcfUniqueId) finTxns.get(0);
                    final SpcfUniqueId companyId = (SpcfUniqueId) finTxns.get(1);

                    completionService.submit(new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            return processFeeEvent(currTxnId, companyId);
                        }
                    });
                }

                logger.info("Queueing bill payment events");
                // Find all Bill Payment Transactions
                // Group by company so the threads don't try to access the same Payee if the email is invalid
                finTxns = FinancialTransaction.findBPTransactionsForOffloadedBatch(currBatch, -1);

                SpcfUniqueId lastCompanyId = null;
                //transactionId -> payrollId
                Map<SpcfUniqueId, SpcfUniqueId> transactionsForCompany = new HashMap<SpcfUniqueId, SpcfUniqueId>();

                while (finTxns.next()) {
                    SpcfUniqueId currTxnId = (SpcfUniqueId) finTxns.get(0);
                    SpcfUniqueId payrollRunId = (SpcfUniqueId) finTxns.get(1);
                    SpcfUniqueId companyId = (SpcfUniqueId) finTxns.get(2);

                    if (lastCompanyId != null && !companyId.equals(lastCompanyId)) {
                        ++numEmailGroups;
                        submitBillPaymentEvents(completionService, transactionsForCompany);
                        transactionsForCompany = new HashMap<SpcfUniqueId, SpcfUniqueId>();
                    }
                    transactionsForCompany.put(currTxnId, payrollRunId);
                    lastCompanyId = companyId;
                }
                if (!transactionsForCompany.isEmpty()) {
                    ++numEmailGroups;
                    submitBillPaymentEvents(completionService, transactionsForCompany);
                }
            }

            logger.info("Finished queueing transaction offloaded events for " + offloadBatches.size() + " batch(es) and " + numEmailGroups + " companies/events");
            PayrollServices.rollbackUnitOfWork();

            logger.info("Beginning to take");
            for (int i = 0; i < numEmailGroups; i++) {
                //noinspection EmptyCatchBlock
                try {
                    completionService.take();
                } catch (InterruptedException e) {}
                if ((i + 1) % 1000 == 0) {
                    logger.info("Completed " + i + " companies/events");
                }
            }


            logger.info("Updating offload batches as event creation complete");
            PayrollServices.beginUnitOfWork();
            for (OffloadBatch currBatch : offloadBatches) {
                Application.refresh(currBatch);

                //update the batch as processed
                currBatch.setIsOffloadedTransactionsEventCreationComplete(true);

                Application.save(currBatch);

            }
            PayrollServices.commitUnitOfWork();


            logger.info("Leaving transaction offload event creation");
        } finally {
            PayrollServices.rollbackUnitOfWork();
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
            }
        }
    }

    private Boolean processFeeEvent(SpcfUniqueId transactionId, SpcfUniqueId companyId) {
        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(SystemPrincipal.FeeEventsBatchJob);

            Company company = Application.findById(Company.class, companyId);
            pspRequestContextManager.setRequestContext(company, RequestType.OLAP, "PrimaryDailyBatchJobs");
            FinancialTransaction transaction = Application.findById(FinancialTransaction.class, transactionId);

            //PSRV004188 - 	Fee Offloaded Events should not be created for 0 fee transactions
            if (transaction != null && transaction.getFinancialTransactionAmount().isGreaterThan(SpcfMoney.ZERO)) {
                //create a fee offloaded event for each
                CompanyEvent.createFeeOffloadedEvent(company, transaction.getId());
            }

            PayrollServices.commitUnitOfWork();

            return true;
        } catch (Throwable t) {
            logger.error("Error processing event for transaction " + transactionId.toString(), t);
            return false;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
    }

    private void submitBillPaymentEvents(CompletionService<Boolean> completionService, final Map<SpcfUniqueId, SpcfUniqueId> transactionsForCompany) {
        if (!transactionsForCompany.isEmpty()) {
            completionService.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    for (Map.Entry<SpcfUniqueId, SpcfUniqueId> entry : transactionsForCompany.entrySet()) {
                        processBillPaymentEvent(entry.getKey(), entry.getValue());
                    }
                    return true;
                }
            });
        }
    }

    private Boolean processBillPaymentEvent(SpcfUniqueId transactionId, SpcfUniqueId payrollRunId) {
        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(SystemPrincipal.FeeEventsBatchJob);

            PayrollRun payrollRun = Application.findById(PayrollRun.class, payrollRunId);
            pspRequestContextManager.setRequestContext(payrollRun.getCompany(), RequestType.OLAP, "PrimaryDailyBatchJobs");
            FinancialTransaction transaction = Application.findById(FinancialTransaction.class, transactionId);

            CompanyEvent.createBillPaymentOffloadedEvent(payrollRun, transaction.getBillPaymentSplit());

            PayrollServices.commitUnitOfWork();

            return true;
        } catch (Throwable t) {
            logger.error("Error processing event for transaction " + transactionId.toString(), t);
            return false;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
    }

}
