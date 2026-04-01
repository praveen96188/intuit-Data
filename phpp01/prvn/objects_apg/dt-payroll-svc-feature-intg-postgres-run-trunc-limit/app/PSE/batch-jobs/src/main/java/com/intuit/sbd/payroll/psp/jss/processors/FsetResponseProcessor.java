package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpFsetConnection;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbd.payroll.psp.batchjobs.fset.FsetManager;
import com.intuit.sbd.payroll.psp.domain.FsetFile;
import com.intuit.sbd.payroll.psp.domain.FsetFileStatus;

/**
 * User: ihannur
 * Date: 9/12/12
 * Time: 4:35 PM
 */
@ScheduledJob(name = "FsetResponseProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class FsetResponseProcessor extends JSSBatchJob {

	public FsetResponseProcessor(String[] pArguments) {
        super(pArguments);
	}
	public FsetResponseProcessor(String[] pArguments, String pJobId) {
	        super(pArguments, pJobId);
	}
    @Override
    protected void execute() {
        if (CalendarUtils.isHoliday(PSPDate.getPSPTime())) {
            getLogger().warn(getClass().getSimpleName() + " skipped (bank holiday) ");
            return;
        }

        getLogger().info("Starting FSET Filing job");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);

        executeStep(CheckForResponseFilesStep.class);
        executeStep(ProcessFsetResponseFileStep.class);
        executeStep(ArchiveFsetFilesStep.class);

        getLogger().info("Completed FSET Filing process batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class CheckForResponseFilesStep extends JSSBatchJobStep<FsetResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);

                SftpFsetConnection sftpFsetConnection = new SftpFsetConnection();
                sftpFsetConnection.downloadFiles();

            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step CheckForResponseFilesStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class ProcessFsetResponseFileStep extends JSSBatchJobStep<FsetResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);
                PayrollServices.beginUnitOfWork();
                DomainEntitySet<FsetFile> fsetFiles = Application.find(FsetFile.class, FsetFile.StatusCd().equalTo(FsetFileStatus.ReceivedByAgency));
                new FsetManager().processResponseFile(fsetFiles);
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ProcessFsetResponseFileStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public static class ArchiveFsetFilesStep extends JSSBatchJobStep<FsetResponseProcessor> {
        public void execute() {
            try {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.FsetFilingBatchJob);
                new FsetManager().archiveFsetFiles();
            } catch (Throwable t) {
                throw new RuntimeException("Exception in job step ArchiveFsetFilesStep - FSET", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }
}
