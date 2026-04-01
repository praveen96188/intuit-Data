package com.intuit.sbd.payroll.psp.jss.monitors.JPMCDDScreening;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.jss.processors.JPMCDDScreening.TPSUReportProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by charithah418 on 9/2/2016.
 */
@ScheduledJob(name = "TPSUReportMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class TPSUReportMonitor extends JSSBatchJobMonitor {
        public TPSUReportMonitor(String[] pArguments) {
            super(pArguments);
        }

        public TPSUReportMonitor(String[] pArguments, String pJobId) {
            super(pArguments, pJobId);
        }

        @Override
        public BatchJobType getBatchJobToMonitor() {
            return BatchJobType.TPSUReportProcessor;
        }
        @Override
        public Class<?> getBatchJobActionToMonitor() {
            return TPSUReportProcessor.ArchiveTPSUFileStep.class;
        }

    public void execute() throws Exception {
        SpcfCalendar pSpcfCalendar = PSPDate.getPSPTime();
        CalendarUtils.clearTime(pSpcfCalendar);
        SpcfCalendar firstBusinessDay = CalendarUtils.getFirstBusinessDayOfMonth(pSpcfCalendar);
        if (!pSpcfCalendar.equals(firstBusinessDay)) {
            getLogger().warn(getClass().getSimpleName() + " monitor skipped (Not first business day of the month) ");
            return;
        }
        super.execute();
        
    }
}
