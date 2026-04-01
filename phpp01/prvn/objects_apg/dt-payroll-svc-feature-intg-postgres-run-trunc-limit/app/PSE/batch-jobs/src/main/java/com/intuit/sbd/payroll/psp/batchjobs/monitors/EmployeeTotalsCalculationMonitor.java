package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.EmployeeTotalsCalculationProcess;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * User: ihannur
 * Date: 5/24/12
 * Time: 1:55 PM
 */
public class EmployeeTotalsCalculationMonitor extends BatchJobProcessorMonitor {
    public EmployeeTotalsCalculationMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.EmployeeTotalsCalculationProcess,
                EmployeeTotalsCalculationProcess.EETotalsJobIsComplete.class);
    }
}
