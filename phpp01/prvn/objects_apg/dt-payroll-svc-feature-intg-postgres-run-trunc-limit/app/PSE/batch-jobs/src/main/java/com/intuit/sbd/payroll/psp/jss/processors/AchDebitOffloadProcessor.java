package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadATFFinalizedPayments;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadNYDTFPayments;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
@ScheduledJob(name = "AchDebitOffload", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class AchDebitOffloadProcessor extends JSSBatchJob {

    private SpcfCalendar processDate;
    private String paymentTemplateCd;

    public AchDebitOffloadProcessor(String[] pArguments) {
        super(pArguments);
    }

    public AchDebitOffloadProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        processDate = PSPDate.getPSPTime();
        paymentTemplateCd = null;

        String commandLine = getJobInstanceParameters().trim();
        if (commandLine.length() > 0) {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);
                        processDate = SpcfCalendar.createInstance(clDate.getYear(), clDate.getMonth(), clDate.getDay(),
                                0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
                    }
                }
            }
        }
    }

    @Override
    public void execute() {
        getLogger().info("Starting ACH Debit Offload batch job for date: " + processDate);
        StopWatch timer = StopWatch.startTimer();

        //The order has been reversed because OffloadATFFinalizedPayments is marking zero dollar transactions
        // as acknowledged by agency is they are ACHDebit regardless of tax payment status
        executeStep(CreateNYDTFPaymentsFileStep.class);
        executeStep(SendNYDTFPaymentsFileStep.class);
        executeStep(OffloadATFFinalizedPaymentsStep.class);

        getLogger().info("Completed ACH Debit Offload batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class OffloadATFFinalizedPaymentsStep extends JSSBatchJobStep<AchDebitOffloadProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchDebitOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new OffloadATFFinalizedPayments().process(getBatchJobProcessor().processDate);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step AchDebitOffload ", t);
            }
        }
    }

    public static class CreateNYDTFPaymentsFileStep extends JSSBatchJobStep<AchDebitOffloadProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchDebitOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new OffloadNYDTFPayments().createFiles(getBatchJobProcessor().processDate);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step AchDebitOffloadForNYDTFStep ", t);
            }
        }
    }

   public static class SendNYDTFPaymentsFileStep extends JSSBatchJobStep<AchDebitOffloadProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchDebitOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new OffloadNYDTFPayments().sendAndArchiveFiles();

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step AchDebitOffloadForNYDTFStep ", t);
            }
        }
    }

    public static void main(String[] args) {

        try {
            BatchJobManager.runJob(BatchJobType.AchDebitOffload.name(), new String[]{"20110920"});
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
