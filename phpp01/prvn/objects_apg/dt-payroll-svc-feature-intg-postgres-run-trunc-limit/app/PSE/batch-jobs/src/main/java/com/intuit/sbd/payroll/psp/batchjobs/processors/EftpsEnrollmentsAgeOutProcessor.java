package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Dec 21, 2010
 * Time: 4:48:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsEnrollmentsAgeOutProcessor extends BatchJobProcessor  {

    public EftpsEnrollmentsAgeOutProcessor(BatchJobProcessor.RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void execute() {

        if (BatchUtils.isWeekendOrHoliday()) {
            logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        logger.info("Starting Eftps enrollments age out job");


        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessEftpsEnrollmentsAgeOut());

        logger.info("Completed Eftps enrollments age out batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public class ProcessEftpsEnrollmentsAgeOut extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EftpsAgeOutBatchJob);

                EdiManager.ageOutEnrollments();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessEftpsEnrollmentsAgeOut ", t);
            }
        }
    }
}
