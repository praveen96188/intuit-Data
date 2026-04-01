package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EmailGatewayProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2009
 * Time: 3:38:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailGatewayMonitor extends BatchJobProcessorMonitor {
    public EmailGatewayMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.EmailGateway,
              EmailGatewayProcessor.ProcessEmails.class);

        setWarnOnMultipleAuditEntries(false);
    }
}
