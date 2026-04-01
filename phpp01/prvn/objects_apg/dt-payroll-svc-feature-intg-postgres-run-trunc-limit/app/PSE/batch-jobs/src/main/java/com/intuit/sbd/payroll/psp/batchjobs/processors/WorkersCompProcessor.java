package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompGatewayImpl;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * User: michaelp696
 */
public class WorkersCompProcessor extends BatchJobProcessor {
    private WorkersCompGatewayImpl workersCompGateway;

    public WorkersCompProcessor(BatchJobProcessor.RunMode runMode, BatchJobType batchJobType, String jobId, String jobInstanceParameters) {
        super(runMode, batchJobType, jobId, jobInstanceParameters);
        this.workersCompGateway = new WorkersCompGatewayImpl();
    }

    @Override
    protected void execute() {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
        logger.info("Starting WorkersCompProcessor batch job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(new PullSubscriptionChanges());
        executeStep(new PushPayrollData());
        executeStep(new PushCompanyChanges());

        logger.info("Completed WorkersCompProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class PullSubscriptionChanges extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
            logger.info("Starting PullSubscriptionChanges step");
            StopWatch timer = StopWatch.startTimer();
            workersCompGateway.getSubscriptionChangesFromWC();
            logger.info("Completed PullSubscriptionChanges step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }

    public class PushPayrollData extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
            logger.info("Starting PushPayrollData step");
            StopWatch timer = StopWatch.startTimer();
            workersCompGateway.pushPayrollDataToWC();
            logger.info("Completed PushPayrollData step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }

    public class PushCompanyChanges extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.WorkersCompBatchJob);
            logger.info("Starting PushCompanyChanges step");
            StopWatch timer = StopWatch.startTimer();
            workersCompGateway.pushCompanyChanges();
            logger.info("Completed PushCompanyChanges step. Elapsed time: " + timer.stop().getElapsedTimeString());
        }
    }
}
