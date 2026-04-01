package com.intuit.sbd.payroll.psp.batchjobs.processors;

/*
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;

/**
 * User: Satish Mandavilli
 * Flux Workflow to process fraud payrolls.
 */
public class FraudPayrollsProcessor extends BatchJobProcessor {
    public FraudPayrollsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    public void execute() {
        logger.info("Starting fraud payrolls batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessFraudPayrolls());

        logger.info("Completed fraud payrolls batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessFraudPayrolls extends BatchJobProcessorStep {
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