package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.CheckPrintProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 4/13/17
 * Time: 1:22 AM
 * To change this template use File | Settings | File Templates.
 */

@ScheduledJob(name="CheckPrintMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class CheckPrintMonitor extends JSSBatchJobMonitor {
    public CheckPrintMonitor(String[] pArguments) {
        super(pArguments);
        setWarnOnMultipleAuditEntries(Boolean.FALSE);
    }

    public CheckPrintMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        setWarnOnMultipleAuditEntries(Boolean.FALSE);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.CheckPrint;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return CheckPrintProcessor.ProcessManualChecks.class;
    }
}
