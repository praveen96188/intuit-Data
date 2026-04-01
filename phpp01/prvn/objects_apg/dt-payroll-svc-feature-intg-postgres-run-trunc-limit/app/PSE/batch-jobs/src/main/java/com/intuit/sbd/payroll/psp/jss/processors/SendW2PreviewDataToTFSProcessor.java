package com.intuit.sbd.payroll.psp.jss.processors;

/**
 * Created with IntelliJ IDEA.
 * User: mvillani
 * Date: 9/12/12
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS.TFSW2PreviewSendJob;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

@ScheduledJob(name = "SendW2PreviewDataToTFSProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class SendW2PreviewDataToTFSProcessor extends JSSBatchJob {
	
    public SendW2PreviewDataToTFSProcessor(String[] pArguments) {
        super(pArguments);
    }
    public SendW2PreviewDataToTFSProcessor(String[] pArguments, String pJobId) {
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
        PayrollServices.setCurrentPrincipal(SystemPrincipal.SendW2DataToTFSBatchJob);

        executeStep(SendW2DataToTFSStep.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class SendW2DataToTFSStep extends JSSBatchJobStep<SendW2PreviewDataToTFSProcessor> {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.SendW2DataToTFSBatchJob);
            StopWatch sw = StopWatch.startTimer();
            String commandLine = getBatchJobProcessor().getJobInstanceParameters().trim();
            String[] args = commandLine.split(" ");
            TFSW2PreviewSendJob sendW2PreviewDataToTFS = new TFSW2PreviewSendJob();
            sendW2PreviewDataToTFS.main(args);

            getLogger().info("completed SendW2PreviewDataToTFSProcessor - in "+ sw.getElapsedTimeString());

        }

    }
}