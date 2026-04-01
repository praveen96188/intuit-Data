package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.FsetResponseProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * User: ihannur
 * Date: 9/12/12
 * Time: 4:33 PM
 */
public class FsetResponseMonitor extends BatchJobProcessorMonitor {
    public FsetResponseMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.FsetResponseProcessor,
              FsetResponseProcessor.ArchiveFsetFilesStep.class);
    }

    public void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
