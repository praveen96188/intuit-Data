package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.amo.ProcessNewAMOMessages;
import com.intuit.sbd.payroll.psp.batchjobs.amo.ProcessSavedAMOMessages;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 3:45:00 PM
 */
@ScheduledJob(name = "AMOMessageProcessor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class AMOMessageProcessor extends JSSBatchJob {
	 public AMOMessageProcessor(String[] pArguments) {
	        super(pArguments);
	    }
	    public AMOMessageProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	    }

    @Override
    public void execute() {
    	getLogger().info("Starting amo message batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessSavedAMOMessagesStep.class);
        executeStep(ProcessNewAMOMessagesStep.class);

        getLogger().info("Completed amo message batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessSavedAMOMessagesStep extends JSSBatchJobStep<AMOMessageProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AMOBatchJob));
                new ProcessSavedAMOMessages().processSavedAMOMessages();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessSavedAMOMessages ", t);
            }  finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class ProcessNewAMOMessagesStep extends JSSBatchJobStep<AMOMessageProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AMOBatchJob));
                new ProcessNewAMOMessages().processNewAMOMessages();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessNewAMOMessages ", t);
            }
        }
    }
}
