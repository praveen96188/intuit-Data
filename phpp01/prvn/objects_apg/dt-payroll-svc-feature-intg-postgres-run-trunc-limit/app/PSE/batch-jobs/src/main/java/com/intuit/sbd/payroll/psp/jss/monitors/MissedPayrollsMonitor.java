package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.SecondOffload;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.PrimaryDailyBatchJobsProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:39:10 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "MissedPayrollsMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class MissedPayrollsMonitor extends JSSBatchJobMonitor {
    public MissedPayrollsMonitor(String[] pArguments) {
        super(pArguments);
    }

    public MissedPayrollsMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.PrimaryDailyBatchJobs;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return PrimaryDailyBatchJobsProcessor.MissedPayrollProcessor.class;
    }

    public void execute() {
        //
        // Intentionally not calling super class' execute method since we don't want the default monitoring behavior.
        //

        if (BatchUtils.isWeekendOrHoliday()) {
            getLogger().warn(getClass().getSimpleName() + " monitor skipped (weekend or bank holiday) ");
            return;
        }

        executeStep(MissedPayrollsMonitorProcessorStep.class);
    }

    public static class MissedPayrollsMonitorProcessorStep extends JSSBatchJobStep<MissedPayrollsMonitor> {
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

            getBatchJobProcessor().verifyJobStepFinished(getBatchJobProcessor().getBatchJobToMonitorId(), jobType, getBatchJobProcessor().getBatchJobActionToMonitor(), jobStepTimeConstraint);
        }
    }
}
