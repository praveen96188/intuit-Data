package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.SalesTaxExceptionProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.SecondOffload;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 10/11/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class SalesTaxExceptionMonitor extends BatchJobProcessorMonitor {

    public SalesTaxExceptionMonitor (BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.PrimaryDailyBatchJobs,
              SalesTaxExceptionProcessor.class);
    }

    public void execute() {
        //
        // Intentionally not calling super class' execute method since we don't want the default monitoring behavior.
        //

        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        executeStep(new SalesTaxExceptionMonitorStep());
    }

    public class SalesTaxExceptionMonitorStep extends BatchJobProcessorStep {
        //
        // Since this monitor is for the Primary offload by default, if there is a second offload scheduled
        // the Missed Payrolls Processor will run under the BatchJobType.ScheduledDailyBatchJobs job type
        // so we need to correct the audit trail search for this condition.
        //

        public void execute() {
            BatchJobType jobType = BatchJobType.PrimaryDailyBatchJobs; // default to PrimaryDailyBatchJobs

            try {
                PayrollServices.beginUnitOfWork();

                SecondOffload secondOffload = OffloadGroup.findStandardOffloadGroup().getSecondOffload(PSPDate.getPSPTime());

                if (secondOffload != null) {
                    jobType = BatchJobType.ScheduledDailyBatchJobs;
                }

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            verifyJobStepFinished(getBatchJobToMonitorId(), jobType, getJobActionToMonitor(), jobStepTimeConstraint);
        }
    }

}
