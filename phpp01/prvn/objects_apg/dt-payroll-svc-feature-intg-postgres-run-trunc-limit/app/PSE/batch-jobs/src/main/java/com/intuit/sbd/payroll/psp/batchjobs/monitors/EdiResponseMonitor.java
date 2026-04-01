package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EdiResponseProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 24, 2011
 * Time: 11:50:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class EdiResponseMonitor extends BatchJobProcessorMonitor {
    public EdiResponseMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
    super(pRunMode,
            pBatchJobType,
            pJobId,
            pJobIdToMonitor,
            BatchJobType.EdiResponse,
            EdiResponseProcessor.ArchiveProcessedFiles.class);
    }

    public void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
