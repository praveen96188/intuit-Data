package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsSendProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * User: rnorian
 * Date: 5/19/11
 * Time: 9:22 AM
 */
public class EftpsSendMonitor extends BatchJobProcessorMonitor {
    public EftpsSendMonitor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
                pBatchJobType,
                pJobId,
                pJobIdToMonitor,
                BatchJobType.EftpsSend,
                EftpsSendProcessor.ProcessPendingTransmissions.class);
    }

    public void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " monitor skipped (bank holiday) ");
            return;
        }

        super.execute();
    }
}
