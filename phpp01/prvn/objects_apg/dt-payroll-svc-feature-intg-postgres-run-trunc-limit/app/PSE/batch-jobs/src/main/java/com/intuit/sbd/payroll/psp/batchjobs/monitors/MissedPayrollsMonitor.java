package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SecondOffload;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.PSPDate;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:39:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class MissedPayrollsMonitor extends BatchJobProcessorMonitor {
    public MissedPayrollsMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.PrimaryDailyBatchJobs,
              DailyBatchJobsProcessor.MissedPayrollProcessor.class);
    }

    public void execute() {
        //
        // Intentionally not calling super class' execute method since we don't want the default monitoring behavior.
        //

        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        executeStep(new MissedPayrollsMonitorProcessorStep());
    }

    public class MissedPayrollsMonitorProcessorStep extends BatchJobProcessorStep {
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
