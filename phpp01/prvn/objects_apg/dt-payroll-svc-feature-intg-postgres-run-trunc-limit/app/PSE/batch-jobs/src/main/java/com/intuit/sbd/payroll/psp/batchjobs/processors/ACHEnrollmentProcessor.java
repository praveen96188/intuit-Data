package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHEnrollments.ACHEnrollmentManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * User: ihannur
 * Date: 2/5/13
 * Time: 12:51 PM
 */
public class ACHEnrollmentProcessor extends BatchJobProcessor {

    SpcfCalendar mEffectiveDate;

    public ACHEnrollmentProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
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
        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(new ProcessAddRequests());
        executeStep(new TransmitPendingTransmissionFilesStep());
        executeStep(new ArchiveACHEnrollmentFilesStep());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class ProcessAddRequests extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHEnrollmentBatchJob);
                ACHEnrollmentManager.createACHEnrollmentFile(mEffectiveDate, true);

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessAddRequests ", t);
            }
        }
    }

    public class TransmitPendingTransmissionFilesStep extends BatchJobProcessorStep {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.ACHEnrollmentBatchJob);

                ACHEnrollmentManager.transmitPendingACHEnrollmentFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step TransmitPendingTransmissionFilesStep - ACHEnrollment", t);
            }
        }
    }

    public class ArchiveACHEnrollmentFilesStep extends BatchJobProcessorStep {
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
