package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.EMSBSToBRMDataSyncProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * @author cbhat
 * EMSBSToBRMDataSyncProcessor job runs last day of every month at 20:00 PST.
 * EMSBSToBRMDataSyncProcessorMonitor is the Monitor Job that will be scheduled at 20:20PST every last day of the month
 */
@ScheduledJob(name = "EMSBSToBRMDataSyncProcessorMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EMSBSToBRMDataSyncProcessorMonitor extends JSSBatchJobMonitor {

    public EMSBSToBRMDataSyncProcessorMonitor(String[] pArguments) {
        super(pArguments);
    }

    public EMSBSToBRMDataSyncProcessorMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.EMSBSToBRMDataSyncProcessor;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return EMSBSToBRMDataSyncProcessor.UploadEMSBSToBRMDataSyncFile.class;
    }

    @Override
    protected void execute() throws Exception {
        super.execute();
    }
}
