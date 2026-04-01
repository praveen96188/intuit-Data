package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.enrollments.EnrollmentDeleteSelection;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.hibernate.FlushMode;

/**
 * Created by: dweinberg
 * Date: 4/25/13
 * Time: 12:24 PM
 *
 * Migrated To JSS by: nloharuka
 * Date: 5/03/17
 * PSP-13001
 */

@ScheduledJob(name = "EnrollmentDeleteSelectionProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EnrollmentDeleteSelectionProcessor extends JSSBatchJob {

    public EnrollmentDeleteSelectionProcessor(String[] pArguments) {
        super(pArguments);
    }
    public EnrollmentDeleteSelectionProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting EnrollmentDeleteSelectionProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(SelectACHEnrollmentsForDelete.class);
        executeStep(SelectRAFEnrollmentsForDelete.class);
        executeStep(WriteMonthlyEnrollmentReport.class);

        getLogger().info("Completed EnrollmentDeleteSelectionProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class SelectACHEnrollmentsForDelete extends JSSBatchJobStep<EnrollmentDeleteSelectionProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EnrollmentDeleteSection);
                new EnrollmentDeleteSelection().selectACHEnrollmentsForDelete();
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                getLogger().error("Error in step SelectACHEnrollmentsForDelete", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class SelectRAFEnrollmentsForDelete extends JSSBatchJobStep<EnrollmentDeleteSelectionProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EnrollmentDeleteSection);
                new EnrollmentDeleteSelection().selectRAFEnrollmentsForDelete();
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                getLogger().error("Error in step SelectRAFEnrollmentsForDelete", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class WriteMonthlyEnrollmentReport extends JSSBatchJobStep<EnrollmentDeleteSelectionProcessor> {
        public void execute() {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                PayrollServices.setCurrentPrincipal(SystemPrincipal.EnrollmentDeleteSection);
                new EnrollmentDeleteSelection().writeMonthlyEnrollmentReport();
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                getLogger().error("Error in step WriteMonthlyEnrollmentReport", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }
}
