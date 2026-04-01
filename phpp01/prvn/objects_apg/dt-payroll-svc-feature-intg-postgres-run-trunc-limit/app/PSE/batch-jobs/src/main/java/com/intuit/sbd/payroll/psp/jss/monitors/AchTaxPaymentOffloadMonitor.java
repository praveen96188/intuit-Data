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
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:40:15 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "AchTaxPaymentOffloadMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class AchTaxPaymentOffloadMonitor extends AchOffloadMonitor {
    public AchTaxPaymentOffloadMonitor(String[] pArguments) {
        super(pArguments);
    }

    public AchTaxPaymentOffloadMonitor(String[] pArguments, String pJobId) {
        super(pArguments,pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.AchTaxPaymentOffload;
    }

    protected void doFinalOffloadChecks() {
        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar today = PSPDate.getPSPTime();
            CalendarUtils.clearTime(today);

            OffloadGroup taxPaymentOffloadGroup = OffloadGroup.findTaxPaymentOffloadGroup();
            Expression<OffloadBatch> query =
                    new Query<OffloadBatch>()
                          .Where(OffloadBatch.OffloadDate().equalTo(today)
                                 .And(OffloadBatch.OffloadGroup().equalTo(taxPaymentOffloadGroup))
                                 .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed)))
                          .OrderBy(OffloadBatch.CreatedDate().Descending());             

            DomainEntitySet<OffloadBatch> batchSet = Application.find(OffloadBatch.class, query);

            if (batchSet.size() < 2) {
                throw new RuntimeException("Unable to locate scheduled offload batch for offload date: " +
                                           today.format(BatchUtils.DATE_FORMAT));
            } else {
                doFinalOffloadChecks(batchSet.get(0));
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
