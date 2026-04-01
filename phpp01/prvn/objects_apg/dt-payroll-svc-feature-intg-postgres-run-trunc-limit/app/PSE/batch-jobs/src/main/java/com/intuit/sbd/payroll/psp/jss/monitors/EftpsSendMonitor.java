package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.EftpsSendProcessor;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 4/25/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "EftpsSendMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EftpsSendMonitor extends JSSBatchJobMonitor {

    public EftpsSendMonitor(String[] pArguments) {
        super(pArguments);
    }

    public EftpsSendMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.EftpsSend;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return EftpsSendProcessor.ProcessPendingTransmissions.class;
    }

    public void execute() throws Exception {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            getLogger().warn(getClass().getSimpleName() + " monitor skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
