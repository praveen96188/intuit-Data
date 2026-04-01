package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.iop.SyncIOPData;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class IopSyncProcessor extends BatchJobProcessor {

    public IopSyncProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting IOP Sync batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new IopSync());

        logger.info("Completed IOP Sync batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class IopSync extends BatchJobProcessorStep {
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
