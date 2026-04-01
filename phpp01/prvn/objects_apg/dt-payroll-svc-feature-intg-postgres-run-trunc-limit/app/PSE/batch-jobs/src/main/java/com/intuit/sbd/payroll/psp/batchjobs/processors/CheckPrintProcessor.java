package com.intuit.sbd.payroll.psp.batchjobs.processors;

/*
 * Copyright (c) 2009 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintManualChecks;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

public class CheckPrintProcessor extends BatchJobProcessor {

    public CheckPrintProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting check print batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessManualChecks());

        logger.info("Completed check print batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessManualChecks extends BatchJobProcessorStep {
        public void execute() {
            try {
                try {
                    PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.CheckPrintBatchJob));

                    PayrollServices.beginUnitOfWork();

                    new PrintManualChecks().processAgencyCheckBatches(SourceSystemCode.QBDT);

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessPaychecks ", t);
            }
        }
    }
}