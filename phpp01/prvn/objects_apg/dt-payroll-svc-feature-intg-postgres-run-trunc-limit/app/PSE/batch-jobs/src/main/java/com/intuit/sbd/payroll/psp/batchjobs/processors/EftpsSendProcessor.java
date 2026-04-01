package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * User: rnorian
 * Date: 5/9/11
 * Time: 8:09 PM
 */
public class EftpsSendProcessor extends BatchJobProcessor {
    public EftpsSendProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessAS400Files());
        executeStep(new ProcessPendingTransmissions());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    /**
     * Will parse and record EftpsPaymentDetail records for AS400 files waiting to be transmitted (i.e. in eftps/AS400
     * directory.  File will be placed in PendingTransmission status.
     */
    public class ProcessAS400Files extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.processAS400Files();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessAS400Files ", t);
            }
        }
    }

    public class ProcessPendingTransmissions extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsFileCommBatchJob);

                EdiManager.processPendingTransmissions();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessPendingTransmissions ", t);
            }
        }
    }

}
