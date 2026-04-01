package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.jss.ATFDataExtract.EmployeeTotalsCalculationProcess;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: ihannur
 * Date: 5/24/12
 * Time: 1:55 PM
 */
@ScheduledJob(name = "EmployeeTotalsCalculationMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EmployeeTotalsCalculationMonitor extends JSSBatchJobMonitor {
    public EmployeeTotalsCalculationMonitor(String[] pArguments) {
        super(pArguments);
    }

    public EmployeeTotalsCalculationMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }
    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.EmployeeTotalsCalculationProcess;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return EmployeeTotalsCalculationProcess.EETotalsJobIsComplete.class;
    }
}
