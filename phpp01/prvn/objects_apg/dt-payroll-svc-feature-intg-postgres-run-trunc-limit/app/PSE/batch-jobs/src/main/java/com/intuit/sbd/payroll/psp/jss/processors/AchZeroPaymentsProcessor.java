package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.zeropayments.ProcessZeroPayments;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobManager;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 */
@ScheduledJob(name = "AchZeroPayments", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class AchZeroPaymentsProcessor extends JSSBatchJob {

    private SpcfCalendar processDate;
    private String paymentTemplateCd;

  
    public AchZeroPaymentsProcessor(String[] pArguments) {
        super(pArguments);
    }
    public AchZeroPaymentsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        processDate = PSPDate.getPSPTime();
        paymentTemplateCd = null;

        String commandLine = getJobInstanceParameters().trim();
        if ( commandLine.length() > 0 ) {
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
    	getLogger().info("Starting Zero Payments batch job for date= " + processDate + " Payment Template = " + (paymentTemplateCd == null ? "None specified" : paymentTemplateCd));
        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessZeroPaymentsStep.class);

        getLogger().info("Completed Zero Payments batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessZeroPaymentsStep extends  JSSBatchJobStep<AchZeroPaymentsProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ProcessZeroPayments);

                try {
                    PayrollServices.beginUnitOfWork();

                    new ProcessZeroPayments().process(getBatchJobProcessor().processDate, getBatchJobProcessor().paymentTemplateCd);

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
    	JSSBatchJobManager.runJob(BatchJobType.AchZeroPayments.name());
    }

}
