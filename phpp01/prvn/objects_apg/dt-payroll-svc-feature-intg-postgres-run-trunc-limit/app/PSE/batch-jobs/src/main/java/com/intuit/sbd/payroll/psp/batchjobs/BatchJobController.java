package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.BatchJobStatus;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch job controller will control the execution of batch jobs in the app layer
 * Created by Ankit on 7/31/14.
 */
public class BatchJobController {

    public static List<BatchJobType> restrictedBatchJobTypes = new ArrayList<BatchJobType>();
    public static SpcfLogger logger;

    static {
        restrictedBatchJobTypes.add(BatchJobType.LedgerBalance);
        restrictedBatchJobTypes.add(BatchJobType.PrimaryDailyBatchJobs);
        restrictedBatchJobTypes.add(BatchJobType.NightlyBatchJobs);
        restrictedBatchJobTypes.add(BatchJobType.AchTaxPaymentOffload);
        logger = Application.getLogger(BatchJobController.class);
    }

    /**
     *
     * @param pBatchJobType
     * @return true if the given batch job type has an instance running, false otherwise
     */
    private static boolean isBatchJobRunning(BatchJobType pBatchJobType) {

        Expression<BatchJobStatus> batchJobStatusQuery =
                new Query<BatchJobStatus>()
                        .Where(BatchJobStatus.JobType().equalTo(pBatchJobType));
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        PayrollServices.rollbackUnitOfWork();
        if (batchJobStatusSet == null || batchJobStatusSet.isEmpty()) {
            return false;
        } else {
            return batchJobStatusSet.getFirst().getIsRunning();
        }

    }

    /**
     * This method should be called prior to starting any batch job, it will perform a series of steps to ensure that
     *  batch job controller is in sync with the running batch jobs.
     * @param pBatchJobType
     */
    public static void batchJobStarted(BatchJobType pBatchJobType) {
        //1. Mark the batch job as started
        BatchJobController.markBatchJobAsStarted(pBatchJobType);
    }

    /**
     * This method should be called after any batch job has finished, it will perform a series of steps to ensure that
     *  batch job controller is in sync with the running batch jobs.
     * @param pBatchJobType
     */
    public static void batchJobFinished(BatchJobType pBatchJobType) {
        //1. Mark the batch job as finished
        BatchJobController.markBatchJobAsFinished(pBatchJobType);
    }

    /**
     * This method returns a boolean to notify the caller if it is safe to run this batch job or not. The intent here
     *  is that all the logic to be checked before running a batch job should be called from this method.
     * @param pBatchJobType
     * @return true if caller can safely run the batch job, else otherwise
     */
    public static boolean canRunBatchJob(BatchJobType pBatchJobType) {
        logger.info("Checking if batch job type " + pBatchJobType + " can be run");
        if (restrictedBatchJobTypes.contains(pBatchJobType)) {
            if (BatchJobController.isBatchJobRunning(pBatchJobType)) {
                logger.info("Returning false for canRunBatchJob " + pBatchJobType);
                logger.info("If you are trying to rerun batch job " + pBatchJobType + " , please mark the record in table BatchJobStatus as not running");
                return Boolean.FALSE;
            } else {
                logger.info("Returning true for canRunBatchJob " + pBatchJobType);
                return Boolean.TRUE;
            }
        }
        logger.info("Returning true for canRunBatchJob " + pBatchJobType);
        return Boolean.TRUE;
    }

    private static synchronized void markBatchJobAsStarted(BatchJobType pBatchJobType) {
        synchronized (pBatchJobType) {
            PayrollServices.beginUnitOfWork();
            BatchJobStatus batchJobStatus = null;
            Expression<BatchJobStatus> batchJobStatusQuery =
                    new Query<BatchJobStatus>()
                            .Where(BatchJobStatus.JobType().equalTo(pBatchJobType));
            DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
            if (batchJobStatusSet == null || batchJobStatusSet.isEmpty()) {
                batchJobStatus = new BatchJobStatus();
                batchJobStatus.setJobType(pBatchJobType);
            } else {
                batchJobStatus = batchJobStatusSet.getFirst();
            }
            batchJobStatus.setLastStartedTimeStamp(new SpcfCalendarImpl());
            batchJobStatus.setIsRunning(Boolean.TRUE);
            Application.save(batchJobStatus);
            PayrollServices.commitUnitOfWork();
        }
    }

    private static void markBatchJobAsFinished(BatchJobType pBatchJobType) {
        synchronized (pBatchJobType) {
            PayrollServices.beginUnitOfWork();
            BatchJobStatus batchJobStatus = null;
            Expression<BatchJobStatus> batchJobStatusQuery =
                    new Query<BatchJobStatus>()
                            .Where(BatchJobStatus.JobType().equalTo(pBatchJobType));
            DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
            if (batchJobStatusSet == null || batchJobStatusSet.isEmpty()) {
                batchJobStatus = new BatchJobStatus();
                batchJobStatus.setJobType(pBatchJobType);
            } else {
                batchJobStatus = batchJobStatusSet.getFirst();
            }
            batchJobStatus.setLastEndedTimeStamp(new SpcfCalendarImpl());
            batchJobStatus.setIsRunning(Boolean.FALSE);
            Application.save(batchJobStatus);
            PayrollServices.commitUnitOfWork();
        }
    }
}
