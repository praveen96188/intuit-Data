package com.intuit.sbd.payroll.psp.batchjobs.monitors.JPMCDDScreening;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.TPSUReportProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by charithah418 on 9/2/2016.
 */
public class TPSUReportMonitor extends BatchJobProcessorMonitor {
    public TPSUReportMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.TPSUReportProcessor,
              TPSUReportProcessor.ArchiveTPSUFileStep.class);
    }

    public void execute() {
        SpcfCalendar pSpcfCalendar = PSPDate.getPSPTime();
        CalendarUtils.clearTime(pSpcfCalendar);
        SpcfCalendar firstBusinessDay = CalendarUtils.getFirstBusinessDayOfMonth(pSpcfCalendar);
        if (!pSpcfCalendar.equals(firstBusinessDay)) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (Not first business day of the month) ");
            return;
        }

        super.execute();
    }
}
