package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHEnrollments.ACHEnrollmentManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * User: RVL
 * Date: 05/20/17
 * Time: 9:30 AM
 * ?
 */

@ScheduledJob(name = "ACHDeEnrollmentBatchJob", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ACHDeEnrollmentProcessor extends JSSBatchJob {

    SpcfCalendar mEffectiveDate;

    public ACHDeEnrollmentProcessor(String[] pArguments) {
        super(pArguments);
    }

    public ACHDeEnrollmentProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void validateRuntimeParameters() {
        String commandLine = getJobInstanceParameters().trim();
        if(commandLine != null) {
            String[] args = commandLine.split(" ");
            if(args.length == 1) {
                if (args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
                    SpcfCalendar effectiveDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[0]);
                    SpcfCalendar localEffectiveDate = SpcfCalendar.createInstance(effectiveDate.getYear(), effectiveDate.getMonth(), effectiveDate.getDay(), SpcfTimeZone.getLocalTimeZone());
                    if(CalendarUtils.getLastDayOfQuarter(localEffectiveDate).equals(localEffectiveDate)) {
                        mEffectiveDate = localEffectiveDate;
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("Invalid parameter. Usage <effectiveDate (lastDayOfQuarter)>");
    }

    @Override
    protected void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessDeleteRequests.class);
        executeStep(TransmitPendingTransmissionFilesStep.class);
        executeStep(ArchiveACHEnrollmentFilesStep.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessDeleteRequests extends JSSBatchJobStep<ACHDeEnrollmentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHDeEnrollmentBatchJob);

                ACHEnrollmentManager.createACHEnrollmentFile(getBatchJobProcessor().mEffectiveDate, false);

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessDeleteRequests ", t);
            }
        }
    }

    public static class TransmitPendingTransmissionFilesStep extends JSSBatchJobStep<ACHDeEnrollmentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHDeEnrollmentBatchJob);

                ACHEnrollmentManager.transmitPendingACHEnrollmentFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransmitPendingTransmissionFilesStep - ACHDeEnrollment", t);
            }
        }
    }

    public static class ArchiveACHEnrollmentFilesStep extends JSSBatchJobStep<ACHDeEnrollmentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHDeEnrollmentBatchJob);

                ACHEnrollmentManager.archiveACHEnrollmentFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveACHEnrollmentFilesStep - ACHDeEnrollment", t);
            }
        }
    }
}
