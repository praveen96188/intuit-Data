package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadBatch;
import com.intuit.sbd.payroll.psp.domain.OffloadBatchStatus;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.SecondOffload;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.jss.processors.PrimaryDailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 8:31:21 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "AchOffloadCompleteMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class AchOffloadCompleteMonitor extends AchOffloadMonitor {
    public AchOffloadCompleteMonitor(String[] pArguments) {
        super(pArguments);
        init();
    }

    public AchOffloadCompleteMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        init();
    }
    
    private void init(){
    	setDoFinalOffloadChecksOnly(true);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.PrimaryDailyBatchJobs;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return PrimaryDailyBatchJobsProcessor.MissedTransactionProcessor.class;
    }

    public void execute() {
        super.execute();

        if (!isSecondOffloadScheduled()) {
            //
            // If no second offload is scheduled, run the monitor as configured (all primary offload steps).
            //
            verifyJobStepFinished(getBatchJobToMonitorId(), getBatchJobToMonitor(), getBatchJobActionToMonitor(), monitorTimeConstraint);
        } else {
            //
            // If a second offload is scheduled, not all job steps were executed for the primary offload, so run the
            // monitor for the truncated primary offload and then run the monitor for the complete second offload.
            //
            verifyJobStepFinished(getBatchJobToMonitorId(), getBatchJobToMonitor(), DailyBatchJobsProcessor.CreateTransactionOffloadedEvents.class, monitorTimeConstraint);
            verifyJobStepFinished(getBatchJobToMonitorId(), BatchJobType.ScheduledDailyBatchJobs, getBatchJobActionToMonitor(), monitorTimeConstraint);
        }
    }

    protected boolean isSecondOffloadScheduled() {
        boolean secondOffloadScheduled = false;
        try {
            PayrollServices.beginUnitOfWork();
            SecondOffload secondOffload = OffloadGroup.findStandardOffloadGroup().getSecondOffload(PSPDate.getPSPTime());
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
