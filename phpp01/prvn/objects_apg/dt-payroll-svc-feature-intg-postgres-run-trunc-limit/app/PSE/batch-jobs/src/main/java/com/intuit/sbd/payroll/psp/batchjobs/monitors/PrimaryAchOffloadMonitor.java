package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadBatch;
import com.intuit.sbd.payroll.psp.domain.OffloadBatchStatus;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:39:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrimaryAchOffloadMonitor extends AchOffloadMonitor {
    public PrimaryAchOffloadMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.PrimaryDailyBatchJobs);
    }

    protected void doFinalOffloadChecks() {
        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar today = PSPDate.getPSPTime();
            CalendarUtils.clearTime(today);

            // since this is the primary offload monitor, we want the earliest completed offload batch
            OffloadGroup offloadGroup = OffloadGroup.findStandardOffloadGroup();
            Expression<OffloadBatch> query =
                    new Query<OffloadBatch>()
                            .Where(OffloadBatch.OffloadDate().equalTo(today)
                                    .And(OffloadBatch.OffloadGroup().equalTo(offloadGroup))
                                    .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed)))
                            .OrderBy(OffloadBatch.CreatedDate()); // sort ascending

            DomainEntitySet<OffloadBatch> batchSet = Application.find(OffloadBatch.class, query);

            if (batchSet.isEmpty()) {
                throw new RuntimeException("Unable to locate primary offload batch for offload date: " +
                        today.format(BatchUtils.DATE_FORMAT));
            } else {
                doFinalOffloadChecks(batchSet.get(0));
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
