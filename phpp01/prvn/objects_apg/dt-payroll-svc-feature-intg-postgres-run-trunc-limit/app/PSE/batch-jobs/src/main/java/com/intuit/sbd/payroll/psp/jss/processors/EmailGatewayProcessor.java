package com.intuit.sbd.payroll.psp.jss.processors;

/*
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.gateways.email.EmailGateway;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: Satish Mandavilli
 * Jss work flow for PSP Email Gateway.
 */
@ScheduledJob(name = "EmailGateway", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class EmailGatewayProcessor extends JSSBatchJob {
	public EmailGatewayProcessor(String[] pArguments) {
		super(pArguments);
	}

	public EmailGatewayProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public void execute() {
		getLogger().info("Starting email gateway batch job");
		StopWatch timer = StopWatch.startTimer();

		executeStep(ProcessEmails.class);

		getLogger().info("Completed email gateway batch job. Elapsed time: "
				+ timer.stop().getElapsedTimeString());
	}

	public static class ProcessEmails extends JSSBatchJobStep<EmailGatewayProcessor> {
		public void execute() {
			try {
				PayrollServices
						.setCurrentPrincipal(SystemPrincipal.EmailGateway);
				new EmailGateway().processCompanyEventsForEmail();
			} catch (Throwable t) {
				throw new RuntimeException(
						"Exception in job step ProcessEmails ", t);
			}
		}
	}
}