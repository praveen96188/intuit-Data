package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EnrollmentDeleteSelectionProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * User: dweinberg
 * Date: 4/25/13
 * Time: 12:29 PM
 */
public class EnrollmentDeleteSelectionMonitor extends BatchJobProcessorMonitor {
    public EnrollmentDeleteSelectionMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.EnrollmentDeleteSelectionProcessor,
              EnrollmentDeleteSelectionProcessor.SelectRAFEnrollmentsForDelete.class);
    }
}
