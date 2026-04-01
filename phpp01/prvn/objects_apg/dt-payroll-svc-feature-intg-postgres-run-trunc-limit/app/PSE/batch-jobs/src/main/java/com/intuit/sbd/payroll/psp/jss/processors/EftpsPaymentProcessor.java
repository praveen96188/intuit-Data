package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.EftpsFile;
import com.intuit.sbd.payroll.psp.domain.EftpsPaymentDetail;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.paycycle.ops.eftpsBp.PaymentFile;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: RVL
 * Date: 4/25/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "EftpsPayment", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EftpsPaymentProcessor extends JSSBatchJob {

    private SpcfCalendar initiationDate;
    private PaymentMethod paymentMethod;
    private PaymentFile.PaymentFileMode paymentFileMode;
    private List<Integer> paymentFileIds = new ArrayList<Integer>();
    private String bepsPaymentReferenceNumber;
    private SpcfCalendar bepsPaymentSettlementDate;
    private String[] reinitiationRejectCodes = new String[]{};
    private boolean thereArePendingPaymentsLeft = false;

    public EftpsPaymentProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EftpsPaymentProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
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

        if (commandLine.length() == 0) {
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
            getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        getLogger().info("Starting Eftps process payments job");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);

        setPaymentFileMode(PaymentFile.PaymentFileMode.PFM_NEXT_DAY);
        setPaymentMethod(PaymentMethod.EFTPS);
        do {
            executeStep(MarkNextDayPaymentsAsProcessing.class);
            executeStep(GenerateNextDayPaymentFile.class);
            executeStep(MarkNextDayPaymentsAsSent.class);
            executeStep(RecordNextDayPaymentEvents.class);

            setJobId(SpcfUniqueId.createInstance(true).toString());   // must change job id for the next execution/iteration
        } while (thereArePendingPaymentsLeft());

        setPaymentFileMode(PaymentFile.PaymentFileMode.PFM_100K);
        setPaymentMethod(PaymentMethod.EFTPSDirectDebit);
        do {
            executeStep(Mark100kPaymentsAsProcessing.class);
            executeStep(Generate100kPaymentFile.class);
            executeStep(Mark100kPaymentsAsSent.class);
            executeStep(Record100kPaymentEvents.class);

            setJobId(SpcfUniqueId.createInstance(true).toString());   // must change job id for the next execution/iteration
        } while (thereArePendingPaymentsLeft());
        getLogger().info("Completed Eftps process payments batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public boolean thereArePendingPaymentsLeft() {
        return thereArePendingPaymentsLeft;
    }

    public void setThereArePendingPaymentsLeft(boolean thereArePendingPaymentsLeft) {
        this.thereArePendingPaymentsLeft = thereArePendingPaymentsLeft;
    }    

    public static class MarkPaymentsAsProcessing extends JSSBatchJobStep<EftpsPaymentProcessor> {
        public void execute() {
            try {
                StopWatch sw = StopWatch.startTimer();
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
                PayrollServices.beginUnitOfWork();
                int maxRecordsToProcess = EftpsUtil.getMaxPaymentsToProcessPerBatchRun();
                int rowsUpdated = MoneyMovementTransaction.markPaymentsInProcessForDate(getBatchJobProcessor().getPaymentMethod(),
                        getBatchJobProcessor().getInitiationDate(),
                                                                                        maxRecordsToProcess);
                PayrollServices.commitUnitOfWork();
                getLogger().info(
                        "updated " + rowsUpdated + " payment MoneyMovementTransactions to status InProcess for payment method " + getBatchJobProcessor().getPaymentMethod() + " in " + sw
                                .getElapsedTimeString());
                if (maxRecordsToProcess == rowsUpdated) {
                    getLogger().info("NOTICE: the EftpsPayment batch job will be automatically run again after this execution completes - maximum number of payments in a batch run has been met: " + maxRecordsToProcess + ".");
                    getBatchJobProcessor().thereArePendingPaymentsLeft = true;
                }
                else {
                    getBatchJobProcessor().thereArePendingPaymentsLeft = false;
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step MarkPaymentsAsProcessing ", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class MarkNextDayPaymentsAsProcessing extends MarkPaymentsAsProcessing {}
    public static class Mark100kPaymentsAsProcessing extends MarkPaymentsAsProcessing {}

    public static class GeneratePaymentFile extends JSSBatchJobStep<EftpsPaymentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
                // unit of work is controlled w/in EdiManager.processPayments since it can create multiple files
                getBatchJobProcessor().setPaymentFileIds(EdiManager.processPayments(getBatchJobProcessor().getPaymentFileMode(), getBatchJobProcessor().getBepsPaymentReferenceNumber(), getBatchJobProcessor().getBepsPaymentSettlementDate()));
                getLogger().info("generated files with file_ids: " + getBatchJobProcessor().getPaymentFileIds());
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step GeneratePaymentFile = " + getBatchJobProcessor().getPaymentFileMode() + ":" + getBatchJobProcessor().getPaymentMethod() , t);
            }
        }
    }

    public static class GenerateNextDayPaymentFile extends GeneratePaymentFile { }
    public static class Generate100kPaymentFile extends GeneratePaymentFile { }

    /**
     * Calls a stored procedure to update financial transactions related to tax payment MMTs that have had a payment
     * file generated and generates an event for each payment.
     *
     * @see \dev\psp\<rel>\pse\domain\src\main\sql\procedure\prc_eftps_payments_processed.sql
     */
    public static class MarkPaymentsAsSent extends JSSBatchJobStep<EftpsPaymentProcessor> {
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
            for (Integer paymentFileId : getBatchJobProcessor().getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    getLogger().info("calling prc_eftps_payments_sent for EftpsFile.FileId = " + paymentFileId);
                    EdiManager.markPaymentsAsSent(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    getLogger().error("error in MarkPaymentsAsSent - file control number: " + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }

    public static class MarkNextDayPaymentsAsSent extends MarkPaymentsAsSent { }
    public static class Mark100kPaymentsAsSent extends MarkPaymentsAsSent { }

    public static class RecordPaymentEvents extends JSSBatchJobStep<EftpsPaymentProcessor> {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsPaymentsBatchJob);
            for (Integer paymentFileId : getBatchJobProcessor().getPaymentFileIds()) {
                try {
                    PayrollServices.beginUnitOfWork();
                    getLogger().info("calling prc_eftps_payments_sent_events for EftpsFile.FileId = " + paymentFileId);
                    EdiManager.insertPaymentSentStatusChangeEvent(paymentFileId);
                    PayrollServices.commitUnitOfWork();
                } catch (Throwable t) {
                    getLogger().error("error in RecordPaymentEvents - file control number: " + paymentFileId, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }

    public static class RecordNextDayPaymentEvents extends RecordPaymentEvents {}
    public static class Record100kPaymentEvents extends RecordPaymentEvents {}

    public static class InitiateRepayment extends JSSBatchJobStep<EftpsPaymentProcessor> {
        @Override
        public void execute() {
            for (Integer paymentFileId : getBatchJobProcessor().paymentFileIds) {
                int repaymentInitiationSuccess = 0, repaymentInitiationFailure = 0, skipped = 0;
                ExecutorService executor = null;
                try {
                    EftpsFile paymentFile = EftpsFile.getEftpsFileByFileId(paymentFileId);
                    if (paymentFile != null) {
                        PayrollServices.beginUnitOfWork();
                        getLogger().info("fetching rejected payments");
                        DomainEntitySet<EftpsPaymentDetail> rejectedPayments = getRejectedPayments(paymentFile,  getBatchJobProcessor().getReinitiationRejectCodes());
                        PayrollServices.rollbackUnitOfWork();

                        getLogger().info("preparing to initiate repayment for " + rejectedPayments.size() + " payments");

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
                                            getLogger().info("skipped: " + rejectedMoneyMovementId);
                                        } else if (initiateRepayment(rejectedMoneyMovementId)) {
                                            PayrollServices.commitUnitOfWork();
                                            result.succeeded++;
                                        } else {
                                            result.failed++;
                                        }
                                    } catch (Throwable t) {
                                        result.failed++;
                                        getLogger().warn("failure on repayment initiation for payment: " + rejectedMoneyMovementId.toString());
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
                                getLogger().info("completed: " + (repaymentInitiationSuccess + repaymentInitiationFailure + skipped) + "   success: " + repaymentInitiationSuccess + "  failure: " + repaymentInitiationFailure + "  skipped: " + skipped);
                            }

                            if ( repaymentInitiationFailure > 0 && (repaymentInitiationFailure % 5) == 0) {
                                getLogger().warn("FAILURE COUNT: " + repaymentInitiationFailure + "  -- consider killing?");
                            }
                        }


                        getLogger().info("completed repayment initiation for file_id = " + paymentFileId + "   success: " + repaymentInitiationSuccess + "  failure: " + repaymentInitiationFailure  + "  skipped: " + skipped);
                    }
                } catch (Throwable t) {
                    getLogger().warn("failure on payment initiation for file: " + paymentFileId, t);
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
                            getBatchJobProcessor().getInitiationDate(), false);
            if (!result.isSuccess()) {
                getLogger().warn("FAILED to initiate repayment for MMT: " + pRejectedMoneyMovementId.toString());
            }

            return result.isSuccess();
        }
    }

    public static void main(String[] args) {
        try {
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment.name(), GenerateNextDayPaymentFile.class.getName(),
                    PaymentFile.PaymentFileMode.PFM_NEXT_DAY.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
