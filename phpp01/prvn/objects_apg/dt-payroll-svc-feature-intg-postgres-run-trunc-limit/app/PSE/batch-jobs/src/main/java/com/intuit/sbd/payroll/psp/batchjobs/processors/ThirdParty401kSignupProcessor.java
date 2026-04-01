package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kSignUpFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpTP401kSignupFileDownload;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kSignUpBatch;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kBatchStatusCode;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;

/**
 * User: Jeff Jones
 * Flux Workflow to download and process signup data from the 401k provider.
 */
public class ThirdParty401kSignupProcessor extends BatchJobProcessor {

    public ThirdParty401kSignupProcessor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        logger.info("Starting third party 401k signup batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new DownloadTP401kSignupFile());
        executeStep(new ProcessTP401kSignUpBatch());
        executeStep(new ArchiveTP401kSignUpBatch());

        logger.info("Completed third party 401k signup batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class DownloadTP401kSignupFile extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new SftpTP401kSignupFileDownload().download();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step DownloadTP401kSignupFile ", t);
            }
        }
    }

    public class ProcessTP401kSignUpBatch extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new ThirdParty401kSignUpFileParser().processSignupBatchs();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessTP401kSignUpBatch ", t);
            }
        }
    }

    public class ArchiveTP401kSignUpBatch extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ThirdParty401kBatchJob);

                new ThirdParty401kSignUpFileParser().archiveTP401kSignUpBatch();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveTP401kSignUpBatch ", t);
            }
        }
    }
}
