package com.intuit.sbd.payroll.psp.jss.monitors;

/*
 * Copyright (c) 2009 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobMonitor;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.FraudPayrollsProcessor;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 05/15/17
 * Time: 9:30 AM
 */

@ScheduledJob(name = "FraudPayrollsMonitor", resourcePath = "/monitor", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class, singleton = false)
public class FraudPayrollsMonitor extends JSSBatchJobMonitor {

    public FraudPayrollsMonitor(String[] pArguments) {
        super(pArguments);
        setWarnOnMultipleAuditEntries(false);
    }

    public FraudPayrollsMonitor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        setWarnOnMultipleAuditEntries(false);
    }

    @Override
    public BatchJobType getBatchJobToMonitor() {
        return BatchJobType.FraudPayrolls;
    }

    @Override
    public Class<?> getBatchJobActionToMonitor() {
        return FraudPayrollsProcessor.ProcessFraudPayrolls.class;
    }

}