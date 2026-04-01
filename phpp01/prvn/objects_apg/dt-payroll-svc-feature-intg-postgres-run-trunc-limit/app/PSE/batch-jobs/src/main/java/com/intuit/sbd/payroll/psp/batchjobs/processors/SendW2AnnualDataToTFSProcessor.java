package com.intuit.sbd.payroll.psp.batchjobs.processors;

/**
 * Created with IntelliJ IDEA.
 * User: mvillani
 * Date: 9/12/12
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS.SendW2DataToTFS;
import com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS.TFSW2AnnualSendJob;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;


public class SendW2AnnualDataToTFSProcessor extends BatchJobProcessor {

    public SendW2AnnualDataToTFSProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void validateRuntimeParameters() {
        //Todo_MV verify if any validation required
        super.validateRuntimeParameters();
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.SendW2DataToTFSBatchJob);

        executeStep(new SendW2DataToTFSStep());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class SendW2DataToTFSStep extends BatchJobProcessorStep {

        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.SendW2DataToTFSBatchJob);
            StopWatch sw = StopWatch.startTimer();
            String commandLine = getJobInstanceParameters().trim();
            String[] args = commandLine.split(" ");
            TFSW2AnnualSendJob sendW2AnnualDataToTFS = new TFSW2AnnualSendJob();
            sendW2AnnualDataToTFS.main(args);

            logger.info("completed SendW2AnnualDataToTFSProcessor - in "+ sw.getElapsedTimeString());

        }

    }
}