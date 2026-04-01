package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.batchjobs.util.SftpTP401kFileUpload;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kBatchProcess;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;

/**
 * User: Jeff Jones
 * Flux Workflow to upload employee and paycheck data to 401k provider.
 */
@ScheduledJob(name = "ThirdParty401kOffload", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ThirdParty401kOffloadProcessor extends JSSBatchJob{    
    
    public ThirdParty401kOffloadProcessor(String[] pArguments) {
		super(pArguments);
	}

	public ThirdParty401kOffloadProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

    @Override
    protected void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
             getLogger().warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        getLogger().info("Starting third party 401k offload batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep( Create401kFiles.class);
        executeStep( Upload401kFiles.class);
        executeStep( Archive401kFiles.class);

         getLogger().info("Completed third party 401k offload batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }
    
    public static class Create401kFiles extends JSSBatchJobStep<ThirdParty401kOffloadProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new ThirdParty401kBatchProcess().createFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step Create401kFiles ", t);
            }
        }
    }

    public static class Upload401kFiles extends JSSBatchJobStep<ThirdParty401kOffloadProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new SftpTP401kFileUpload().upload();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step Upload401kFiles ", t);
            }
        }
    }

    public static class Archive401kFiles extends JSSBatchJobStep<ThirdParty401kOffloadProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new ThirdParty401kBatchProcess().archiveFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveTP401kFiles ", t);
            }
        }
    }
}
