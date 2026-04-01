package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.zeropayments.ProcessZeroPayments;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
public class AchZeroPaymentsProcessor extends BatchJobProcessor {

    private SpcfCalendar processDate;
    private String paymentTemplateCd;

    public AchZeroPaymentsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
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
                    else {
                        paymentTemplateCd = arg;
                    }
                }
            }
        }
    }

    @Override
    public void execute() {
        logger.info("Starting Zero Payments batch job for date= " + processDate + " Payment Template = " + (paymentTemplateCd == null ? "None specified" : paymentTemplateCd));
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessZeroPaymentsStep());

        logger.info("Completed Zero Payments batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessZeroPaymentsStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ProcessZeroPayments);

                try {
                    PayrollServices.beginUnitOfWork();

                    new ProcessZeroPayments().process(processDate, paymentTemplateCd);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessZeroPayments ", t);
            }
        }
    }

    public static void main(String[] args) {
        BatchJobManager.runJob(BatchJobType.AchZeroPayments, new String[]{"20110920"});
    }

}
