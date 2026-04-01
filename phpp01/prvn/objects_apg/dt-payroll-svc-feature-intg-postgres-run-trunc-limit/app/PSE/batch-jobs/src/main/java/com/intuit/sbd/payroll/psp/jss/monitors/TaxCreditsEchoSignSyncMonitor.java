package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.TaxCreditsEchoSignSyncProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 05/20/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "TaxCreditsEchoSignMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class TaxCreditsEchoSignSyncMonitor extends JSSBatchJobMonitor {

    public TaxCreditsEchoSignSyncMonitor(String[] pArguments) {
        super(pArguments);
        setWarnOnMultipleAuditEntries(false);
    }

    public TaxCreditsEchoSignSyncMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        setWarnOnMultipleAuditEntries(false);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.TaxCreditsEchoSign;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return TaxCreditsEchoSignSyncProcessor.ProcessEchoSignSyncs.class;
    }

}
