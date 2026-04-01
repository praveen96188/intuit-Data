package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpTP401kFileUpload;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kBatchProcess;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * User: Jeff Jones
 * Flux Workflow to upload employee and paycheck data to 401k provider.
 */
public class ThirdParty401kOffloadProcessor extends BatchJobProcessor{

    public ThirdParty401kOffloadProcessor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        logger.info("Starting third party 401k offload batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new Create401kFiles());
        executeStep(new Upload401kFiles());
        executeStep(new Archive401kFiles());

        logger.info("Completed third party 401k offload batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }
    
    public class Create401kFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new ThirdParty401kBatchProcess().createFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step Create401kFiles ", t);
            }
        }
    }

    public class Upload401kFiles extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new SftpTP401kFileUpload().upload();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step Upload401kFiles ", t);
            }
        }
    }

    public class Archive401kFiles extends BatchJobProcessorStep {
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
