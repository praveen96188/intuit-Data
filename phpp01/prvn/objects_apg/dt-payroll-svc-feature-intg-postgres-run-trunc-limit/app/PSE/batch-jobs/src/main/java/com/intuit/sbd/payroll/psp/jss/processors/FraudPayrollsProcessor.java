package com.intuit.sbd.payroll.psp.jss.processors;

/*
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: RVL
 * Date: 05/15/17
 * Time: 9:30 AM
 * Run fraud rules engine against newly received payroll runs
 */

@ScheduledJob(name = "FraudPayrolls", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class FraudPayrollsProcessor extends JSSBatchJob {

    public FraudPayrollsProcessor(String[] pArguments) {
        super(pArguments);
    }

    public FraudPayrollsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    public void execute() {
        getLogger().info("Starting fraud payrolls batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessFraudPayrolls.class);

        getLogger().info("Completed fraud payrolls batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessFraudPayrolls extends JSSBatchJobStep<FraudPayrollsProcessor> {
        public void execute() {
            try {
                try {
                    PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.FraudulentPayrollsBatchJob));

                    PayrollServices.beginUnitOfWork();

                    new ProcessFraudulentPayrolls().processFraudulentPayrolls();

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessFraudPayrolls ", t);
            }
        }
    }
}