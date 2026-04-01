package com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.context.threading.ChildThreadRequestContextHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.math.BigDecimal;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class OffloadATFFinalizedPayments {
    private static final SpcfLogger logger = Application.getLogger(OffloadATFFinalizedPayments.class);

    private SpcfCalendar processingDate;

    private PSPRequestContextManager pspRequestContextManager;

    public OffloadATFFinalizedPayments() {
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
                throw new RuntimeException("Wrong number of parameters. Usage: OffloadATFFinalizedPayments <yyyyMMdd>");
            }

            if (!args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
                throw new RuntimeException("Invalid processing date format " + args[0] + ". Correct format: yyyyMMdd");
            }


            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AchDebitOffloadBatchJob));

            PayrollServices.beginUnitOfWork();
            new OffloadATFFinalizedPayments().process(SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[0]));
            PayrollServices.rollbackUnitOfWork();

        } catch (Throwable t) {
            t.printStackTrace();
            logger.fatal(t.getMessage(), t);
            PayrollServices.rollbackUnitOfWork();
            System.exit(1);
        }
    }


    /**
     * Offload ACH Debit Transactions
     *
     * @param pProcessingDate
     * @return
     */
    public void process(SpcfCalendar pProcessingDate) {
        if (pProcessingDate == null) {
            throw new RuntimeException("Invalid processing date (null).");
        }


        ExecutorService threadPool = null;
        try {
            StopWatch swTotalProcess = StopWatch.startTimer();     // output total time at the end using this
            threadPool = ThreadingUtils.createNewFixedThreadPool();
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            processingDate = SpcfCalendar.createInstance(pProcessingDate.getYear(), pProcessingDate.getMonth(),
                    pProcessingDate.getDay(), SpcfTimeZone.getLocalTimeZone());

            // Find Money Movement Transactions to process
            PaymentMethod[] paymentMethods = {PaymentMethod.ACHDebit};
            TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                    MoneyMovementTransaction.findTaxPayments()
                            .setInitiationDate(processingDate)
                            .setPaymentMethods( paymentMethods)
                            .setTaxPaymentStatuses(taxPaymentStatuses)
                            .find();

            // Include zero dollar transactions that are Ready To Send since they are not able to be finalized through the UI.
            DomainEntitySet<MoneyMovementTransaction> zeroDollarTransactions =
                    MoneyMovementTransaction.findTaxPayments()
                                            .setInitiationDate(processingDate)
                                            .setPaymentMethods( paymentMethods)
                                            .setReadyToSend()
                                            .setTransactionAmount(SpcfMoney.ZERO)
                                            .find();
            moneyMovementTransactions.addAll(zeroDollarTransactions);

            // Process each Money Movement Transaction in a different thread
            for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
                try {
                    pspRequestContextManager.setRequestContextCompany(mmt.getCompany());
                    final MoneyMovementTransaction finalMMT = mmt;
                    // final SpcfCalendar finalProcessingDate = pProcessingDate.copy();
                    //PSP-12357
                    BigDecimal mmtAmount = SpcfUtils.convertToBigDecimal(mmt.getMoneyMovementTransactionAmount());

                    // Check to ensure MMT amount is a positive dollar amount
                    if (mmtAmount.compareTo(BigDecimal.ZERO) < 0) {

                        // If payment is negative dollar amount, log an error (skip payment and move on to next)
                        String msg = String.format("MoneyMovementTransaction amount for company %s:%s is < $0.00 " +
                                        "(MMT id: %s, MMT amount: $%,.2f).",
                                mmt.getCompany().getSourceSystemCd(),
                                mmt.getCompany().getSourceCompanyId(),
                                mmt.getId(),
                                mmtAmount);
                        logger.error(msg+" Skipping AchDebitOffload of ATFFinalized Negative MMT.");
                        continue;
                    }
                    ChildThreadRequestContextHelper childThreadRequestContextHelper = new ChildThreadRequestContextHelper();
                    childThreadRequestContextHelper.loadThreadLocals();
                    completionService.submit(new Callable<Integer>() {
                        public Integer call() {
                            return processMoneyMovementTransaction( finalMMT.getId(), childThreadRequestContextHelper);
                        }
                    });
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }
            // Get the results of each thread execution
            int mmtCount = 0;

            try {
                for (int t = 0; t < moneyMovementTransactions.size(); t++) {
                    mmtCount++;
                    Future<Integer> f = completionService.take();

                    if (mmtCount % 100 == 0) {
                        logger.info("working -- completed processing " + mmtCount + " money movement transactions");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(threadPool);
        }
    }

    private int processMoneyMovementTransaction(SpcfUniqueId pMMTId, ChildThreadRequestContextHelper childThreadRequestContextHelper) {

        try {
            childThreadRequestContextHelper.setThreadLocals();
            PayrollServices.setCurrentPrincipal(SystemPrincipal.AchDebitOffloadBatchJob);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            MoneyMovementTransaction mmt = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, pMMTId);
            mmt.setTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency);
            mmt.setStatus(PaymentStatus.Executed);
            TransactionState executedState = TransactionState.findTransactionState(TransactionStateCode.Executed);
            for (FinancialTransaction ft:mmt.getFinancialTransactionCollection()) {
                ft.addTaxPaymentTransactionState(executedState);
            }

            mmt = Application.save(mmt);
            PayrollServices.commitUnitOfWork();

        } catch (Throwable t) {
            logger.error("error processing money movement transaction " + pMMTId.toString(), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            childThreadRequestContextHelper.clearThreadLocals();
        }
        return 0;
    }


}
