package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 8:31:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class AchOffloadCompleteMonitor extends AchOffloadMonitor {
    public AchOffloadCompleteMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.PrimaryDailyBatchJobs,
                DailyBatchJobsProcessor.MissedTransactionProcessor.class);

        setDoFinalOffloadChecksOnly(true);
    }

    public void execute() {
        super.execute();

        if (!isSecondOffloadScheduled()) {
            //
            // If no second offload is scheduled, run the monitor as configured (all primary offload steps).
            //
            verifyJobStepFinished(getBatchJobToMonitorId(), getJobTypeToMonitor(), getJobActionToMonitor(), monitorTimeConstraint);
        } else {
            //
            // If a second offload is scheduled, not all job steps were executed for the primary offload, so run the
            // monitor for the truncated primary offload and then run the monitor for the complete second offload.
            //
            verifyJobStepFinished(getBatchJobToMonitorId(), getJobTypeToMonitor(), DailyBatchJobsProcessor.CreateTransactionOffloadedEvents.class, monitorTimeConstraint);
            verifyJobStepFinished(getBatchJobToMonitorId(), BatchJobType.ScheduledDailyBatchJobs, getJobActionToMonitor(), monitorTimeConstraint);
        }
    }

    protected boolean isSecondOffloadScheduled() {
        boolean secondOffloadScheduled = false;

        try {
            PayrollServices.beginUnitOfWork();

            SecondOffload secondOffload =
                    OffloadGroup.findStandardOffloadGroup().getSecondOffload(PSPDate.getPSPTime());

            secondOffloadScheduled = (secondOffload != null);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return secondOffloadScheduled;
    }

    protected void doFinalOffloadChecks() {
        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar today = PSPDate.getPSPTime();
            CalendarUtils.clearTime(today);

            // since this is the overall offload monitor, we want all completed offload batches
            OffloadGroup offloadGroup = OffloadGroup.findStandardOffloadGroup();
            Expression<OffloadBatch> query =
                    new Query<OffloadBatch>()
                            .Where(OffloadBatch.OffloadDate().equalTo(today)
                                    .And(OffloadBatch.OffloadGroup().equalTo(offloadGroup))
                                    .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed)))
                            .OrderBy(OffloadBatch.CreatedDate()); // sort ascending

            DomainEntitySet<OffloadBatch> batchSet = Application.find(OffloadBatch.class, query);

            if (batchSet.isEmpty()) {
                throw new RuntimeException("Unable to locate offload batch(es) for offload date: " +
                        today.format(BatchUtils.DATE_FORMAT));
            } else {
                // we want to check all offload batches at this point
                for (OffloadBatch batch : batchSet) {
                    doFinalOffloadChecks(batch);
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
