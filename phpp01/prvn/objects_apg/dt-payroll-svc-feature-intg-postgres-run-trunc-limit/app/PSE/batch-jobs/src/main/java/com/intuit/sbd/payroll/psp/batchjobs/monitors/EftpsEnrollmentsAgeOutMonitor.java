package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsEnrollmentsAgeOutProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Dec 21, 2010
 * Time: 4:52:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsEnrollmentsAgeOutMonitor extends BatchJobProcessorMonitor {
    public EftpsEnrollmentsAgeOutMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.EftpsEnrollmentsAgeOut,
                EftpsEnrollmentsAgeOutProcessor.ProcessEftpsEnrollmentsAgeOut.class);
    }

    public void execute() {
        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        super.execute();
    }


}
