package com.intuit.sbd.payroll.psp.batchjobs.monitors.JPMCDDScreening;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.OFACReportProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.NightlyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by suganyas315 on 7/27/15.
 *
 */
public class OFACReportMonitor extends BatchJobProcessorMonitor {
    public OFACReportMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.OFACReportProcessor,
              OFACReportProcessor.ArchiveFileStep.class);
    }

    public void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        super.execute();
    }
}
