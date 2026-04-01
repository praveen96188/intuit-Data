package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsEnrollmentsProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Dec 14, 2010
 * Time: 10:18:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsEnrollmentsMonitor extends BatchJobProcessorMonitor {
    public EftpsEnrollmentsMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.EftpsEnrollments,
                EftpsEnrollmentsProcessor.ProcessEftpsEnrollments.class);
    }

    public void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}


