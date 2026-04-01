package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadBatchStatus;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:39:44 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "PrimaryAchOffloadMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class PrimaryAchOffloadMonitor extends AchOffloadMonitor {

    public PrimaryAchOffloadMonitor(String[] pArguments) {
        super(pArguments);
    }

    public PrimaryAchOffloadMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.PrimaryDailyBatchJobs;
    }

    protected void doFinalOffloadChecks() {
        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar today = PSPDate.getPSPTime();
            CalendarUtils.clearTime(today);

            // since this is the primary offload monitor, we want the earliest completed offload batch
            OffloadGroup offloadGroup = OffloadGroup.findStandardOffloadGroup();
            callfinalOffloadChecks(today, offloadGroup);
            //since thi sis Primary Monitor no need to check CCDPlus file
            if(SystemParameter.isSystemInTestState()){
                offloadGroup= OffloadGroup.findPSPOffloadsOffloadGroup();
                callfinalOffloadChecks(today, offloadGroup);
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void callfinalOffloadChecks(SpcfCalendar today, OffloadGroup offloadGroup) {
        Expression<OffloadBatch> query =
                new Query<OffloadBatch>()
                        .Where(OffloadBatch.OffloadDate().equalTo(today)
                                .And(OffloadBatch.OffloadGroup().equalTo(offloadGroup))
                                .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed)))
                        .OrderBy(OffloadBatch.CreatedDate()); // sort ascending

        DomainEntitySet<OffloadBatch> batchSet = Application.find(OffloadBatch.class, query);
        if (batchSet.isEmpty()) {
            //in case of scheduled and any other offloadbatch, it should still throw error
            if(SystemParameter.isSystemInTestState() && (offloadGroup.getOffloadGroupCd().equals("PSPO") || offloadGroup.getOffloadGroupCd().equals("STD"))){
                getLogger().error("Unable to locate offload batch for offload Group: "+offloadGroup+" offload date: " +
                        today.format(BatchUtils.DATE_FORMAT));
                return;
            }
            throw new RuntimeException("Unable to locate offload batch for offload Group: "+offloadGroup+" offload date: " +
                    today.format(BatchUtils.DATE_FORMAT));
        } else {
            doFinalOffloadChecks(batchSet.get(0));
        }
    }
}
