package com.intuit.sbd.payroll.psp.jss.processors;

/**
 * Created with IntelliJ IDEA.
 * User: mvillani
 * Date: 9/12/12
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS.SendMonthlyDataToTFS;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;

@ScheduledJob(name = "SendMonthlyDataToTFSProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class SendMonthlyDataToTFSProcessor extends JSSBatchJob {

   
    public SendMonthlyDataToTFSProcessor(String[] pArguments) {
        super(pArguments);
    }
    public SendMonthlyDataToTFSProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        //Todo_MV verify if any validation required
        super.validateRuntimeParameters();
    }

    @Override
    protected void execute() {
    	 getLogger().info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.SendMonthlyDataToTFSBatchJob);

        executeStep( SendMonthlyDataToTFSStep.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class SendMonthlyDataToTFSStep extends JSSBatchJobStep<SendMonthlyDataToTFSProcessor> {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.SendMonthlyDataToTFSBatchJob);
            StopWatch sw = StopWatch.startTimer();
            String commandLine = getBatchJobProcessor().getJobInstanceParameters().trim();
            String[] args = commandLine.split(" ");

            SendMonthlyDataToTFS sendMonthlyDataToTFS = new SendMonthlyDataToTFS();
            sendMonthlyDataToTFS.main(args);

            getLogger().info("Completed send monthly data to TFS in "+ sw.getElapsedTimeString());

        }

    }
}