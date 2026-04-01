package com.intuit.sbd.payroll.psp.batchjobs.processors;

/*
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.gateways.email.EmailGateway;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;

/**
 * User: Satish Mandavilli
 * Flux Workflow for PSP Email Gateway.
 */
public class EmailGatewayProcessor extends BatchJobProcessor {
    public EmailGatewayProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    public void execute() {
        logger.info("Starting email gateway batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessEmails());

        logger.info("Completed email gateway batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessEmails extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EmailGateway);
                new EmailGateway().processCompanyEventsForEmail();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessEmails ", t);
            }
        }
    }
}