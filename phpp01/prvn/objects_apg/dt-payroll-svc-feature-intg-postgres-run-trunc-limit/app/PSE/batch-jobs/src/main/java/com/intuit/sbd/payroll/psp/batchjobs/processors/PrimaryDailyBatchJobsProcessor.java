package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.FluxUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.regex.Pattern;
import java.util.Date;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 11, 2009
 * Time: 9:49:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrimaryDailyBatchJobsProcessor extends DailyBatchJobsProcessor {
    public PrimaryDailyBatchJobsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    protected AchOffloadType getAchOffloadType() {
        return AchOffloadType.PRIMARY;
    }

    protected void establishOffloadDate() {
        SpcfCalendar now = PSPDate.getPSPTime();
        SpcfCalendar offloadDate = null;
        String commandLine = getJobInstanceParameters().trim();

        if ((getRunMode() == RunMode.UsingFlux) || (commandLine.length() == 0)) {
            offloadDate = now;
        } else {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                        offloadDate = SpcfCalendar.createInstance(clDate.getYear(),
                                                                  clDate.getMonth(),
                                                                  clDate.getDay(),
                                                                  now.getHour(),
                                                                  now.getMinute(),
                                                                  now.getSecond(),
                                                                  now.getMillisecond(),
                                                                  SpcfTimeZone.getLocalTimeZone());
                    }
                }
            }

            if (offloadDate == null) {
                offloadDate = now;
            }
        }

        establishOffloadDate(offloadDate, false);
    }

    protected boolean validateOffloadTime() {
        SpcfCalendar cal, offloadTime = getOffloadDate();
        String currentDateStr = offloadTime.format(BatchUtils.DATE_FORMAT);
        String cutoffTimeStr = OffloadGroup.findStandardOffloadGroup().getCutoffTime();
        String rolloverTimeStr = BatchUtils.getConfigString("psp_offload_date_rollover_time", "15:00");
        Pattern pattern = Pattern.compile("([0-2]?[0-9]:[0-5][0-9]).*");
        Matcher matcher;

        // normalize the cutoff time value to ##:##
        matcher = pattern.matcher(cutoffTimeStr);
        if (matcher.matches()) {
            cutoffTimeStr = matcher.group(1);

            if (cutoffTimeStr.matches("[0-9]:[0-5][0-9]")) {
                cutoffTimeStr = "0" + cutoffTimeStr;
            }
        }

        // normalize the rollover time value to ##:##
        matcher = pattern.matcher(rolloverTimeStr);
        if (matcher.matches()) {
            rolloverTimeStr = matcher.group(1);

            if (rolloverTimeStr.matches("[0-9]:[0-5][0-9]")) {
                rolloverTimeStr = "0" + rolloverTimeStr;
            }
        }

        // need date in local time for comparison
        cal = SpcfCalendar.parse(BatchUtils.DATE_FORMAT + BatchUtils.TIME_FORMAT, currentDateStr + rolloverTimeStr);
        SpcfCalendar rolloverTime = SpcfCalendar.createInstance(cal.getYear(),
                                                                cal.getMonth(),
                                                                cal.getDay(),
                                                                cal.getHour(),
                                                                cal.getMinute(),
                                                                cal.getSecond(),
                                                                cal.getMillisecond(),
                                                                SpcfTimeZone.getLocalTimeZone());

        // need date in local time for comparison
        cal = SpcfCalendar.parse(BatchUtils.DATE_FORMAT + BatchUtils.TIME_FORMAT, currentDateStr + cutoffTimeStr);
        SpcfCalendar cutoffTime = SpcfCalendar.createInstance(cal.getYear(),
                                                              cal.getMonth(),
                                                              cal.getDay(),
                                                              cal.getHour(),
                                                              cal.getMinute(),
                                                              cal.getSecond(),
                                                              cal.getMillisecond(),
                                                              SpcfTimeZone.getLocalTimeZone());

        // invalid time if: rolloverTime < offloadTime < cutoffTime
        boolean isValid = !(rolloverTime.before(offloadTime) && offloadTime.before(cutoffTime));

        logger.info("Primary offload time " +
                    offloadTime.format(BatchUtils.DATE_TIME_FORMAT) +
                    " is " + (isValid ? "valid" : "invalid (blackout)"));

        return isValid;
    }

    protected boolean isSecondOffloadScheduled() {
        boolean secondOffloadScheduled = false;

        try {
            PayrollServices.beginUnitOfWork();

            SecondOffload secondOffload = OffloadGroup.findStandardOffloadGroup().getSecondOffload(getOffloadDate());

            secondOffloadScheduled = (secondOffload != null);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return secondOffloadScheduled;
    }

    protected void execute() {
        if (!isValidOffloadDate()) {
            logger.warn("Daily batch jobs skipped (specified processing date is weekend or bank holiday: " +
                        getOffloadDate().format(BatchUtils.DATE_FORMAT) + ")");
            return;
        }

        if (!isValidOffloadTime()) {
            throw new RuntimeException("Invalid offload time for " + getAchOffloadType().toString() +
                                       " offload (" + getOffloadDate().format("yyyyMMdd HH:mm:ss") + ")");
        }

        StopWatch timer = StopWatch.startTimer();

        logger.info("Starting daily batch jobs (primary)");

        executeStep(new StartPrimaryAchOffloadMonitor());
        executeStep(new NotifyAchOffloadStarted());
        executeStep(new OffloadAchData());
        executeStep(new CreateAchFiles());
        executeStep(new UploadAchFiles());
        executeStep(new NotifyAchOffloadComplete());
        executeStep(new PauseLedgerBalanceProcessor());
        executeStep(new DownloadDicrFilesDelayPeriod());
        executeStep(new DownloadDicrFiles());
        executeStep(new ArchiveDailyFiles());
        executeStep(new UpdateMoneyMovementTransaction());
        executeStep(new UpdateFinancialTransaction());        
        executeStep(new UpdatePayrollStatus());
        //Honor kill switch for extract and ensure a second offload isn't scheduled.  If we have a second offload scheduled, we'll do the extract after that completes, and it will
        // take care of both the first and second offload
        boolean shouldRunATFExtract = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_DATA_EXTRACT, false);
        if (!isSecondOffloadScheduled() && shouldRunATFExtract) {
            executeStep(new ScheduleATFDataExtract());
        }
        executeStep(new InsertFinancialTransactionState()); // uses batch id from batch job context (or finds it if reentering)
        executeStep(new CreateTransactionOffloadedEvents());

        if (isSecondOffloadScheduled() && !otherOffloadCompletedInsertFinancialTransactionStateStep()) {
            logger.info("Executed Primary ACH Offload only - deferring other daily jobs for second offload.");
        } else {
            executeStep(new MissedPayrollProcessor());
            executeStep(new MissedTransactionProcessor());
            executeStep(new GemsAccountsReceivableProcessor());
            executeStep(new ScheduleSalesTaxExceptionProcessor());
        }
        
        executeStep(new ResumeLedgerBalanceProcessor());
        
        logger.info("Completed daily batch jobs (primary). Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class StartPrimaryAchOffloadMonitor extends BatchJobProcessorStep {
        public void execute() {
            BatchJobManager batchJobManager = new BatchJobManager();
            batchJobManager.scheduleJob(BatchJobType.PrimaryAchOffloadMonitor, getJobId());
        }
    }

    public class ScheduleATFDataExtract extends BatchJobProcessorStep {
        public void execute() {
            BatchJobManager batchJobManager = new BatchJobManager();
            batchJobManager.scheduleJob(BatchJobType.ATFDataExtract, ATFDataExtractRunType.UpdatedData.toString());
        }
    }
    
    public class PauseLedgerBalanceProcessor extends BatchJobProcessorStep {
        public void execute() {
        	//Do not run on LOCAL environment
        	if(Application.getEnvironmentName().equalsIgnoreCase("LOCAL")){
        		logger.info("Skip pausing of LedgerBalance job ");
        		return;
        	}
        	
        	try {
	        	String jobNameSpace = BatchJobProcessor.getJobNamespace(BatchJobType.LedgerBalance);
	        	FluxUtils.pauseBatchJob(jobNameSpace);
        	} catch(Throwable t){
        		logger.warn("Exception while pauing the LedgerBalance job", t);
        	}
	        logger.info("Pausing LedgerBalance job");
        }
    }
    
    public class ResumeLedgerBalanceProcessor extends BatchJobProcessorStep {
        public void execute() {
        	//Do not run on LOCAL environment
        	if(Application.getEnvironmentName().equalsIgnoreCase("LOCAL")){
        		logger.info("Skip resuming of LedgerBalance job");
        		return;
        	}
        	
        	try {
	        	BatchJobSetup batchJobSetup = BatchJobManager.getBatchJobSetup(BatchJobType.LedgerBalance);       	
	        	String jobNameSpace = BatchJobProcessor.getJobNamespace(BatchJobType.LedgerBalance);
	        	FluxUtils.resumeBatchJob(jobNameSpace);
		        logger.info("Resuming LedgerBalance job");
		        
		        SpcfCalendar currentTime = PSPDate.getPSPTime();
	            long calculatedScheduleTime = FluxUtils.getTodayScheduleTime(batchJobSetup.getJobTimerExpression());            
	            if(currentTime.getTimeInMilliseconds() >= calculatedScheduleTime){
	            	FluxUtils.expeditBatchJob(jobNameSpace);
	            	logger.info("LedgerBalance job was scheduled at "+ new Date(calculatedScheduleTime)+" but the PrimaryDailyBatchJobs job completed on "+currentTime+" . So expediting the LedgerBalance job");
	            }
        	} catch(Throwable t){
        		logger.warn("Exception while resuming/expediting the LedgerBalance job", t);
        	}
        }
    }

}
