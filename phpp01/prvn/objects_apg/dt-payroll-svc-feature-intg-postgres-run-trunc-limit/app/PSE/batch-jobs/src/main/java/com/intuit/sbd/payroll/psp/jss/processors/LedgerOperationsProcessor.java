package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ledgeroperations.ProcessLedgerOperations;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * User: dweinberg
 * Date: 04/05/17
 * Time: 2:04 PM
 */
@ScheduledJob(name = "LedgerOperations", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class LedgerOperationsProcessor extends JSSBatchJob {

	public LedgerOperationsProcessor(String[] pArguments) {
		super(pArguments);
	}

	public LedgerOperationsProcessor(String[] pArguments, String pJobId) {
		super(pArguments, pJobId);
	}

	@Override
	public void execute() {
		getLogger().info("Starting ledger operations batch job");
		StopWatch timer = StopWatch.startTimer();

		executeStep(ProcessQueuedLedgerOperationJobs.class);

		getLogger().info(
				"Completed ledger operations batch job. Elapsed time: "
						+ timer.stop().getElapsedTimeString());
	}

	public static class ProcessQueuedLedgerOperationJobs extends JSSBatchJobStep<LedgerOperationsProcessor> {
		public void execute() {
			try {
				PayrollServices
						.setCurrentPrincipal(SystemPrincipal.LedgerOperationsBatchJob);
				new ProcessLedgerOperations().process();
			} catch (Throwable t) {
				throw new RuntimeException(
						"Exception in job step ProcessQueuedLedgerOperationJobs ",
						t);
			}
		}
	}
}
