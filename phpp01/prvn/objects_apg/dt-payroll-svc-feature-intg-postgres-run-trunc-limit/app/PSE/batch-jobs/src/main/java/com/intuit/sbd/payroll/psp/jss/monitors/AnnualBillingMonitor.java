package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.processors.AnnualBillingProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 10/11/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "AnnualBillingMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class AnnualBillingMonitor extends JSSBatchJobMonitor {

    public AnnualBillingMonitor(String[] pArguments) {
        super(pArguments);
        setWarnOnMultipleAuditEntries(false);
    }
    public AnnualBillingMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        setWarnOnMultipleAuditEntries(false);
    }
    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.AnnualBillingProcessor;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return AnnualBillingProcessor.AnnualBillingStep.class;
    }

}
