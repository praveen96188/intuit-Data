package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadATFFinalizedPayments;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadNYDTFPayments;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class AchDebitOffloadProcessor extends BatchJobProcessor {

    private SpcfCalendar processDate;
    private String paymentTemplateCd;

    public AchDebitOffloadProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void validateRuntimeParameters() {
        processDate = PSPDate.getPSPTime();
        paymentTemplateCd = null;

        String commandLine = getJobInstanceParameters().trim();
        if ((getRunMode() == RunMode.NotUsingFlux) && (commandLine.length() > 0)) {
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
        logger.info("Starting ACH Debit Offload batch job for date: " + processDate);
        StopWatch timer = StopWatch.startTimer();

        //The order has been reversed because OffloadATFFinalizedPayments is marking zero dollar transactions
        // as acknowledged by agency is they are ACHDebit regardless of tax payment status
        executeStep(new CreateNYDTFPaymentsFileStep());
        executeStep(new SendNYDTFPaymentsFileStep());
        executeStep(new OffloadATFFinalizedPaymentsStep());

        logger.info("Completed ACH Debit Offload batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class OffloadATFFinalizedPaymentsStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchDebitOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new OffloadATFFinalizedPayments().process(processDate);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step AchDebitOffload ", t);
            }
        }
    }

    public class CreateNYDTFPaymentsFileStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AchDebitOffloadBatchJob);

                try {
                    PayrollServices.beginUnitOfWork();

                    new OffloadNYDTFPayments().createFiles(processDate);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step AchDebitOffloadForNYDTFStep ", t);
            }
        }
    }

    public class SendNYDTFPaymentsFileStep extends BatchJobProcessorStep {
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
        BatchJobManager.runJob(BatchJobType.AchDebitOffload, new String[]{"20110920"});
    }

}
