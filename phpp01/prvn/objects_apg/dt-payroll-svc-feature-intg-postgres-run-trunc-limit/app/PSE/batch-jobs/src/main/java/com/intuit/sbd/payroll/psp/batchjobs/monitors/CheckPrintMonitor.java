package com.intuit.sbd.payroll.psp.batchjobs.monitors;

/*
 * Copyright (c) 2009 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessorMonitor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.FraudPayrollsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.CheckPrintProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;

public class CheckPrintMonitor extends BatchJobProcessorMonitor {
    public CheckPrintMonitor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobIdToMonitor) {
        super(pRunMode,
              pBatchJobType,
              pJobId,
              pJobIdToMonitor,
              BatchJobType.CheckPrint,
              CheckPrintProcessor.ProcessManualChecks.class);

        setWarnOnMultipleAuditEntries(false);
    }
}