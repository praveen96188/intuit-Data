package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHEnrollments.ACHEnrollmentManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: ihannur
 * Date: 2/12/13
 * Time: 11:01 AM
 */
@ScheduledJob(name = "ACHEnrollmentResponseBatchJob", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ACHEnrollmentResponseProcessor extends JSSBatchJob {

    public ACHEnrollmentResponseProcessor(String[] pArguments) {
        super(pArguments);
    }

    public ACHEnrollmentResponseProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessResponseFilesStep.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public static class ProcessResponseFilesStep extends JSSBatchJobStep<ACHEnrollmentResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHEnrollmentResponseBatchJob);

                ACHEnrollmentManager.processResponseFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessResponseFilesStep - ACHEnrollmentResponse", t);
            }
        }
    }

}
