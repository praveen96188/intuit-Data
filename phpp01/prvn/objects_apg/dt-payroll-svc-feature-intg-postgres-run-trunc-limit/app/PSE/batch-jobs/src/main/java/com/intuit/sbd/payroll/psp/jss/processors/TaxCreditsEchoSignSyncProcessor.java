package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.taxcredits.SyncEchoSignDocuments;
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
 * ?
 */

@ScheduledJob(name = "TaxCreditsEchoSign", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class TaxCreditsEchoSignSyncProcessor extends JSSBatchJob {

    public TaxCreditsEchoSignSyncProcessor(String[] pArguments) {
        super(pArguments);
    }

    public TaxCreditsEchoSignSyncProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(ProcessEchoSignSyncs.class);
        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessEchoSignSyncs extends JSSBatchJobStep<TaxCreditsEchoSignSyncProcessor> {

        @Override
        public void execute() {
            try {
                try {
                    PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));

                    PayrollServices.beginUnitOfWork();
                    new SyncEchoSignDocuments().sync();                    
                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                getLogger().error("Error: TaxCreditsEchoSignSyncProcessor failed.", t);
                throw new RuntimeException("Exception in job step ProcessEchoSignSyncs ", t);
            }
        }
    }
}
