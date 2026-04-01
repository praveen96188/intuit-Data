package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.EdiSendProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 24, 2011
 * Time: 11:50:08 AM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "EdiSendMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EdiSendMonitor  extends JSSBatchJobMonitor {
    public EdiSendMonitor(String[] pArguments) {
        super(pArguments);
    }

    public EdiSendMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.EdiSend;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return EdiSendProcessor.ProcessPendingTransmissions.class;
    }

    public void execute() throws Exception{
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
        	getLogger().warn(getClass().getSimpleName() + " monitor skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
