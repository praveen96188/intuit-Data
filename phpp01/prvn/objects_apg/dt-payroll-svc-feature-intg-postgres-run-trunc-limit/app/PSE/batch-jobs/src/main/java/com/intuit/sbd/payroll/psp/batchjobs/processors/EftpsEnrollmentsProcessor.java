package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Dec 16, 2010
 * Time: 10:45:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsEnrollmentsProcessor extends BatchJobProcessor {

    public EftpsEnrollmentsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {

        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            logger.warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        logger.info("Starting Eftps process enrollments job");


        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessEftpsEnrollments());

        logger.info("Completed Eftps process enrollments batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public class ProcessEftpsEnrollments extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsEnrollmentBatchJob);

                EdiManager.processEnrollments();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessEftpsEnrollments ", t);
            }
        }
    }
}
