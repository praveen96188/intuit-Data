package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.paycycle.ops.eftpsBp.PaymentFile;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Dec 21, 2010
 * Time: 5:23:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsPaymentProcessor extends BatchJobProcessor {

    private SpcfCalendar initiationDate;
    private PaymentMethod paymentMethod;
    private PaymentFile.PaymentFileMode paymentFileMode;
    private List<Integer> paymentFileIds = new ArrayList<Integer>();
    private String bepsPaymentReferenceNumber;
    private SpcfCalendar bepsPaymentSettlementDate;
    private String[] reinitiationRejectCodes = new String[]{};
    private boolean thereArePendingPaymentsLeft = false;

    public EftpsPaymentProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    public SpcfCalendar getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(SpcfCalendar pInitiationDate) {
        initiationDate = pInitiationDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod pPaymentMethod) {
        paymentMethod = pPaymentMethod;
    }

    public PaymentFile.PaymentFileMode getPaymentFileMode() {
        return paymentFileMode;
    }

    public void setPaymentFileMode(PaymentFile.PaymentFileMode pPaymentFileMode) {
        paymentFileMode = pPaymentFileMode;
    }

    public List<Integer> getPaymentFileIds() {
        return paymentFileIds;
    }

    public void setPaymentFileIds(List<Integer> pFileIdList) {
        paymentFileIds = pFileIdList;
    }

    public String getBepsPaymentReferenceNumber() {
        return bepsPaymentReferenceNumber;
    }

    public void setBepsPaymentReferenceNumber(String pBepsPaymentReferenceNumber) {
        bepsPaymentReferenceNumber = pBepsPaymentReferenceNumber;
    }

    public SpcfCalendar getBepsPaymentSettlementDate() {
        return bepsPaymentSettlementDate;
    }

    public void setBepsPaymentSettlementDate(SpcfCalendar pBepsPaymentSettlementDate) {
        bepsPaymentSettlementDate = pBepsPaymentSettlementDate;
    }

    public String[] getReinitiationRejectCodes() {
        return reinitiationRejectCodes;
    }

    public void setReinitiationRejectCodes(String[] pReinitiationRejectCodes) {
        reinitiationRejectCodes = pReinitiationRejectCodes;
    }

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        SpcfCalendar initiationDate = null;
        String commandLine = getJobInstanceParameters().trim();

        if ((getRunMode() == RunMode.UsingFlux) || (commandLine.length() == 0)) {
            initiationDate = now;
        } else {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                        initiationDate = SpcfCalendar.createInstance(clDate.getYear(),
                                                                     clDate.getMonth(),
                                                                     clDate.getDay(),
                                                                     now.getHour(),
                                                                     now.getMinute(),
                                                                     now.getSecond(),
                                                                     now.getMillisecond(),
                                                                     SpcfTimeZone.getLocalTimeZone());
                    } else if (arg.matches(PaymentFile.PaymentFileMode.PFM_100K.name())) {
                        paymentFileMode = PaymentFile.PaymentFileMode.PFM_100K;
                    } else if (arg.matches(PaymentFile.PaymentFileMode.PFM_NEXT_DAY.name())) {
                        paymentFileMode = PaymentFile.PaymentFileMode.PFM_NEXT_DAY;
                    } else if (arg.matches(PaymentFile.PaymentFileMode.PFM_SAME_DAY.name())) {
                        paymentFileMode = PaymentFile.PaymentFileMode.PFM_SAME_DAY;
                    } else if (arg.matches(PaymentMethod.EFTPS.name())) {
                        paymentMethod = PaymentMethod.EFTPS;
                    } else if (arg.matches(PaymentMethod.EFTPSDirectDebit.name())) {
                        paymentMethod = PaymentMethod.EFTPSDirectDebit;
                    } else if (arg.matches("FileIds=.*")) {
                        String fileIds = arg.substring(arg.indexOf("=") + 1);
                        for (String fileId : fileIds.split(",")) {
                            try {
                                paymentFileIds.add(Integer.parseInt(fileId));
                            } catch (NumberFormatException nfe) {
                                throw new RuntimeException("could not parse FileId= " + fileId);
                            }
                        }
                    } else if (arg.matches("BepsRefNum=.*") && arg.length() > "BepsRefNum=".length()) {
                        bepsPaymentReferenceNumber = arg.substring("BepsRefNum=".length());
                    } else if (arg.matches("BepsSettlementDate=.*") && arg.length() > "BepsSettlementDate=".length()) {
                        String paymentSettlementDate = arg.substring("BepsSettlementDate=".length());
                        if (paymentSettlementDate.matches(BatchUtils.VALIDYYYYMMDD)) {
                            bepsPaymentSettlementDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT,
                                                                           paymentSettlementDate);
                        } else {
                            throw new RuntimeException("invalid BepsSettlementDate= " + paymentSettlementDate);
                        }
                    } else if (arg.matches("RejectCodes=.*") && arg.length() > "RejectCodes=".length()) {
                        String rejectCds = arg.substring("RejectCodes=".length());
                        setReinitiationRejectCodes(rejectCds.split(","));
                    }
                }
            }

            if (initiationDate == null) {
                initiationDate = MoneyMovementTransaction.getNextEFTPSInitiationDate();
            }
        }

        setInitiationDate(initiationDate);

        if (paymentFileMode == PaymentFile.PaymentFileMode.PFM_SAME_DAY && (bepsPaymentReferenceNumber == null || bepsPaymentSettlementDate == null)) {
            throw new RuntimeException(
                    "PaymentFileMode = PFM_SAME_DAY  -- BepsRefNum and BepsSettlementDate must be specified");
        }
    }

    @Override
    protected void validateStepRuntimeParameters(String stepName) {
        validateRuntimeParameters();
        if (stepName.equals(Mark100kPaymentsAsSent.class.getSimpleName()) || stepName.equals(MarkNextDayPaymentsAsSent.class.getSimpleName())) {
            if (paymentFileIds == null || paymentFileIds.size() == 0) {
                throw new RuntimeException(
                        "no FileIds= argument found.  These value(s) are required for marking payments as sent.  Values are found with SELECT FILE_ID, EF.* from PSP_EFTPS_FILE EF where FILE_TYPE = 'EftpsPayment' and TRUNC(CREATED_DATE) = date 'YYYY-MM-DD'");
            }
        }
    }

    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        logger.info("Starting Eftps process payments job");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);

        setPaymentFileMode(PaymentFile.PaymentFileMode.PFM_NEXT_DAY);
        setPaymentMethod(PaymentMethod.EFTPS);
        do {
            executeStep(new MarkNextDayPaymentsAsProcessing());
            executeStep(new GenerateNextDayPaymentFile());
            executeStep(new MarkNextDayPaymentsAsSent());
            executeStep(new RecordNextDayPaymentEvents());
            setJobId(SpcfUniqueId.createInstance(true).toString());   // must change job id for the next execution/iteration
        } while (thereArePendingPaymentsLeft());

        setPaymentFileMode(PaymentFile.PaymentFileMode.PFM_100K);
        setPaymentMethod(PaymentMethod.EFTPSDirectDebit);
        do {
            executeStep(new Mark100kPaymentsAsProcessing());
            executeStep(new Generate100kPaymentFile());
            executeStep(new Mark100kPaymentsAsSent());
            executeStep(new Record100kPaymentEvents());
            setJobId(SpcfUniqueId.createInstance(true).toString());   // must change job id for the next execution/iteration
        } while (thereArePendingPaymentsLeft());
        logger.info("Completed Eftps process payments batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public boolean thereArePendingPaymentsLeft() {
        return thereArePendingPaymentsLeft;
    }

    public void setThereArePendingPaymentsLeft(boolean thereArePendingPaymentsLeft) {
        this.thereArePendingPaymentsLeft = thereArePendingPaymentsLeft;
    }    

    public class MarkPaymentsAsProcessing extends BatchJobProcessorStep {
        public void execute() {
            try {
                StopWatch sw = StopWatch.startTimer();
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
                PayrollServices.beginUnitOfWork();
                int maxRecordsToProcess = EftpsUtil.getMaxPaymentsToProcessPerBatchRun();
                int rowsUpdated = MoneyMovementTransaction.markPaymentsInProcessForDate(getPaymentMethod(),
                                                                                        getInitiationDate(),
                                                                                        maxRecordsToProcess);
                PayrollServices.commitUnitOfWork();
                logger.info(
                        "updated " + rowsUpdated + " payment MoneyMovementTransactions to status InProcess for payment method " + getPaymentMethod() + " in " + sw
                                .getElapsedTimeString());
                if (maxRecordsToProcess == rowsUpdated) {
                    logger.info("NOTICE: the EftpsPayment batch job will be automatically run again after this execution completes - maximum number of payments in a batch run has been met: " + maxRecordsToProcess + ".");
                    thereArePendingPaymentsLeft = true;
                }
                else {
                    thereArePendingPaymentsLeft = false;
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step MarkPaymentsAsProcessing ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class MarkNextDayPaymentsAsProcessing extends MarkPaymentsAsProcessing {}
    public class Mark100kPaymentsAsProcessing extends MarkPaymentsAsProcessing {}

    public class GeneratePaymentFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
                // unit of work is controlled w/in EdiManager.processPayments since it can create multiple files
                setPaymentFileIds(EdiManager.processPayments(getPaymentFileMode(), getBepsPaymentReferenceNumber(), getBepsPaymentSettlementDate()));
                logger.info("generated files with file_ids: " + getPaymentFileIds());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step GeneratePaymentFile = " + getPaymentFileMode() + ":" + getPaymentMethod() , t);
            }
        }
    }

    public class GenerateNextDayPaymentFile extends GeneratePaymentFile { }
    public class Generate100kPaymentFile extends GeneratePaymentFile { }

    /**
     * Calls a stored procedure to update financial transactions related to tax payment MMTs that have had a payment
     * file generated and generates an event for each payment.
     *
     * @see \dev\psp\<rel>\pse\domain\src\main\sql\procedure\prc_eftps_payments_processed.sql
     */
    public class MarkPaymentsAsSent extends BatchJobProcessorStep {
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
            for (Integer paymentFileId : getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    logger.info("calling prc_eftps_payments_sent for EftpsFile.FileId = " + paymentFileId);
                    EdiManager.markPaymentsAsSent(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    logger.error("error in MarkPaymentsAsSent - file control number: " + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }

    public class MarkNextDayPaymentsAsSent extends MarkPaymentsAsSent { }
    public class Mark100kPaymentsAsSent extends MarkPaymentsAsSent { }

    public class RecordPaymentEvents extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
            for (Integer paymentFileId : getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    logger.info("calling prc_eftps_payments_sent_events for EftpsFile.FileId = " + paymentFileId);
                    EdiManager.insertPaymentSentStatusChangeEvent(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    logger.error("error in RecordPaymentEvents - file control number: " + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }

    public class RecordNextDayPaymentEvents extends RecordPaymentEvents {}
    public class Record100kPaymentEvents extends RecordPaymentEvents {}

    public class InitiateRepayment extends BatchJobProcessorStep {
        @Override
        public void execute() {
            for (Integer paymentFileId : paymentFileIds) {
                int repaymentInitiationSuccess = 0, repaymentInitiationFailure = 0, skipped = 0;
                ExecutorService executor = null;
                try {
                    EftpsFile paymentFile = EftpsFile.getEftpsFileByFileId(paymentFileId);
                    if (paymentFile != null) {
                        PayrollServices.beginUnitOfWork();
                        logger.info("fetching rejected payments");
                        DomainEntitySet<EftpsPaymentDetail> rejectedPayments = getRejectedPayments(paymentFile, getReinitiationRejectCodes());
                        PayrollServices.rollbackUnitOfWork();

                        logger.info("preparing to initiate repayment for " + rejectedPayments.size() + " payments");

                        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
                        ConcurrentLinkedQueue<SpcfUniqueId> rejectedMoneyMovementQueue = new ConcurrentLinkedQueue<SpcfUniqueId>();
                        for (EftpsPaymentDetail rejectedPayment : rejectedPayments) {
                            rejectedMoneyMovementQueue.add(rejectedPayment.getMoneyMovementTransaction().getId());
                        }

                        class ThreadResult {
                            public int succeeded = 0;
                            public int skipped = 0;
                            public int failed = 0;
                        }

                        CompletionService<ThreadResult> completionService = new ExecutorCompletionService<ThreadResult>(executor);
                        for (final SpcfUniqueId rejectedMoneyMovementId : rejectedMoneyMovementQueue) {
                            completionService.submit(new Callable<ThreadResult>() {
                                public ThreadResult call() throws Exception {
                                    ThreadResult result = new ThreadResult();
                                    try {
                                        PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
                                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                                        if (alreadyReinitiated(rejectedMoneyMovementId)) {
                                            result.skipped++;
                                            logger.info("skipped: " + rejectedMoneyMovementId);
                                        } else if (initiateRepayment(rejectedMoneyMovementId)) {
                                            PayrollServices.commitUnitOfWork();
                                            result.succeeded++;
                                        } else {
                                            result.failed++;
                                        }
                                    } catch (Throwable t) {
                                        result.failed++;
                                        logger.warn("failure on repayment initiation for payment: " + rejectedMoneyMovementId.toString());
                                    } finally {
                                        PayrollServices.rollbackUnitOfWork();
                                    }
                                    return result;
                                }
                            });
                        }

                        for (int i = 0; i < rejectedMoneyMovementQueue.size(); i++) {
                            Future<ThreadResult> f = completionService.take();
                            ThreadResult result = f.get();
                            repaymentInitiationSuccess += result.succeeded;
                            repaymentInitiationFailure += result.failed;
                            skipped += result.skipped;

                            if ( (repaymentInitiationSuccess + repaymentInitiationFailure + skipped) % 250 == 0) {
                                logger.info("completed: " + (repaymentInitiationSuccess + repaymentInitiationFailure + skipped) + "   success: " + repaymentInitiationSuccess + "  failure: " + repaymentInitiationFailure + "  skipped: " + skipped);
                            }

                            if ( repaymentInitiationFailure > 0 && (repaymentInitiationFailure % 5) == 0) {
                                logger.warn("FAILURE COUNT: " + repaymentInitiationFailure + "  -- consider killing?");
                            }
                        }


                        logger.info("completed repayment initiation for file_id = " + paymentFileId + "   success: " + repaymentInitiationSuccess + "  failure: " + repaymentInitiationFailure  + "  skipped: " + skipped);
                    }
                } catch (Throwable t) {
                    logger.warn("failure on payment initiation for file: " + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                    ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
                }
            }
        }

        private DomainEntitySet<EftpsPaymentDetail> getRejectedPayments(EftpsFile pPaymentFile, String[] pRejectCodes) {
            Expression<EftpsPaymentDetail> query =
                    new Query<EftpsPaymentDetail>()
                            .Where(EftpsPaymentDetail.ParentFile().equalTo(pPaymentFile)
                                    .And(EftpsPaymentDetail.RejectCd().in(pRejectCodes)))
                            .EagerLoad(EftpsPaymentDetail.MoneyMovementTransaction());
            return Application.find(EftpsPaymentDetail.class, query);
        }

        private boolean alreadyReinitiated(SpcfUniqueId pRejectedMoneyMovementId) {
            Expression<MoneyMovementTransaction> query =
                    new Query<MoneyMovementTransaction>()
                            .Select(MoneyMovementTransaction.Id().Count())
                            .Where(MoneyMovementTransaction.OriginalTransaction().Id().equalTo(pRejectedMoneyMovementId));

            return Application.executeScalarAggQuery(MoneyMovementTransaction.class, query) > 0;
        }

        private boolean initiateRepayment(SpcfUniqueId pRejectedMoneyMovementId) {


            ProcessResult result =
                    PayrollServices.paymentManager.initiateTaxRepayment(
                            pRejectedMoneyMovementId.toString(),
                            getInitiationDate(), false);
            if (!result.isSuccess()) {
                logger.warn("FAILED to initiate repayment for MMT: " + pRejectedMoneyMovementId.toString());
            }

            return result.isSuccess();
        }
    }

    public static void main(String[] args) {
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment, GenerateNextDayPaymentFile.class,
                                   PaymentFile.PaymentFileMode.PFM_NEXT_DAY.name());
    }
}
