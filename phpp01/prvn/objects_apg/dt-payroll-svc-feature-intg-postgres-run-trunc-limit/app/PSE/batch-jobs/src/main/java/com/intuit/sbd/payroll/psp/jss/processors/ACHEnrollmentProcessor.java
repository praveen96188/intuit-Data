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
 * User: ihannur
 * Date: 2/5/13
 * Time: 12:51 PM
 */
@ScheduledJob(name = "ACHEnrollmentBatchJob", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ACHEnrollmentProcessor extends JSSBatchJob {

    SpcfCalendar mEffectiveDate;

    public ACHEnrollmentProcessor(String[] pArguments) {
        super(pArguments);
    }
    public ACHEnrollmentProcessor(String[] pArguments, String pJobId) {
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
                    if(CalendarUtils.getFirstDayOfQuarter(localEffectiveDate).equals(localEffectiveDate)) {
                        mEffectiveDate = localEffectiveDate;
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("Invalid parameter. Usage <effectiveDate (firstDayOfQuarter)>");
    }

    @Override
    protected void execute() {
    	getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(ProcessAddRequests.class);
        executeStep(TransmitPendingTransmissionFilesStep.class);
        executeStep(ArchiveACHEnrollmentFilesStep.class);

        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class ProcessAddRequests extends JSSBatchJobStep<ACHEnrollmentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHEnrollmentBatchJob);
                ACHEnrollmentManager.createACHEnrollmentFile(getBatchJobProcessor().mEffectiveDate, true);

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessAddRequests ", t);
            }
        }
    }

    public static class TransmitPendingTransmissionFilesStep extends JSSBatchJobStep<ACHEnrollmentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHEnrollmentBatchJob);

                ACHEnrollmentManager.transmitPendingACHEnrollmentFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransmitPendingTransmissionFilesStep - ACHEnrollment", t);
            }
        }
    }

    public static class ArchiveACHEnrollmentFilesStep extends JSSBatchJobStep<ACHEnrollmentProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHEnrollmentBatchJob);

                ACHEnrollmentManager.archiveACHEnrollmentFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveACHEnrollmentFilesStep - ACHEnrollment", t);
            }
        }
    }
}
