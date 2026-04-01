package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.enrollments.EnrollmentDeleteSelection;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import org.hibernate.FlushMode;

/**
 * User: dweinberg
 * Date: 4/25/13
 * Time: 12:24 PM
 */
public class EnrollmentDeleteSelectionProcessor extends BatchJobProcessor {

    public EnrollmentDeleteSelectionProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {
        logger.info("Starting EnrollmentDeleteSelectionProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new SelectACHEnrollmentsForDelete());
        executeStep(new SelectRAFEnrollmentsForDelete());
        executeStep(new WriteMonthlyEnrollmentReport());

        logger.info("Completed EnrollmentDeleteSelectionProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class SelectACHEnrollmentsForDelete extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EnrollmentDeleteSection);
                new EnrollmentDeleteSelection().selectACHEnrollmentsForDelete();
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                logger.error("Error in step SelectACHEnrollmentsForDelete", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class SelectRAFEnrollmentsForDelete extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EnrollmentDeleteSection);
                new EnrollmentDeleteSelection().selectRAFEnrollmentsForDelete();
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                logger.error("Error in step SelectRAFEnrollmentsForDelete", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public class WriteMonthlyEnrollmentReport extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EnrollmentDeleteSection);
                new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                logger.error("Error in step WriteMonthlyEnrollmentReport", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }
}
