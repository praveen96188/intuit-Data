package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.iop.SyncIOPData;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
@ScheduledJob(name="IOPDataSync", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class IopSyncProcessor extends JSSBatchJob {
    public IopSyncProcessor(String[] pArguments) {
        super(pArguments);
    }

    public IopSyncProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting IOP Sync batch job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(IopSync.class);
        getLogger().info("Completed IOP Sync batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class IopSync extends JSSBatchJobStep<IopSyncProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                new SyncIOPData().process();
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();
                throw new RuntimeException("Exception in job step SyncIOPData ", t);
            }
        }
    }

}
