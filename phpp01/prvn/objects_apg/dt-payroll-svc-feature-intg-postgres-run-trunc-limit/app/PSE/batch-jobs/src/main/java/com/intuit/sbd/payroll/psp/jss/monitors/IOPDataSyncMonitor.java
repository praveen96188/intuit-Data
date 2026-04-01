package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.IopSyncProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Mar 17, 2011
 * Time: 3:50:41 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name="IOPDataSyncMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class IOPDataSyncMonitor extends JSSBatchJobMonitor {
    public IOPDataSyncMonitor(String[] pArguments) {
        super(pArguments);
        setWarnOnMultipleAuditEntries(false);
    }

    public IOPDataSyncMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        setWarnOnMultipleAuditEntries(false);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.IOPDataSync;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return IopSyncProcessor.IopSync.class;
    }
}
