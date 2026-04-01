package com.intuit.sbd.payroll.psp.jss.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.EftpsEnrollmentsAgeOutProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 4/25/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "EftpsEnrollmentsAgeOutMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class EftpsEnrollmentsAgeOutMonitor extends JSSBatchJobMonitor {

    public EftpsEnrollmentsAgeOutMonitor(String[] pArguments) {
        super(pArguments);
    }

    public EftpsEnrollmentsAgeOutMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.EftpsEnrollmentsAgeOut;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return EftpsEnrollmentsAgeOutProcessor.ProcessEftpsEnrollmentsAgeOut.class;
    }

    public void execute() throws Exception {
        if (BatchUtils.isWeekendOrHoliday()) {
            getLogger().warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        super.execute();
    }


}
