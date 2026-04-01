package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.processors.ThirdParty401kSignupProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * User: Jeff Jones
 */
public class ThirdParty401kSignupMonitor extends BatchJobProcessorMonitor {
    public ThirdParty401kSignupMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.ThirdParty401kSignup,
              ThirdParty401kSignupProcessor.ArchiveTP401kSignUpBatch.class);
    }

    public void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        super.execute();
    }
}
