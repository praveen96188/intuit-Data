package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintManualChecks;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 4/13/17
 * Time: 12:41 AM
 * To change this template use File | Settings | File Templates.
 */

@ScheduledJob(name="CheckPrint", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class CheckPrintProcessor extends JSSBatchJob {
    public CheckPrintProcessor(String[] pArguments) {
        super(pArguments);
    }

    public CheckPrintProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() throws Exception {
        getLogger().info("Starting check print batch job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(ProcessManualChecks.class);
        getLogger().info("Completed check print batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessManualChecks extends JSSBatchJobStep<CheckPrintProcessor> {
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



