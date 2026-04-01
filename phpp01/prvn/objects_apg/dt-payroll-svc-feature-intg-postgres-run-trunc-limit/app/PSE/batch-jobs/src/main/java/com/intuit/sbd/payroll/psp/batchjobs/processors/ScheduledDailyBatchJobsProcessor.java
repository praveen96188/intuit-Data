package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 11, 2009
 * Time: 9:50:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledDailyBatchJobsProcessor extends DailyBatchJobsProcessor {
    public ScheduledDailyBatchJobsProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    protected AchOffloadType getAchOffloadType() {
        return AchOffloadType.SCHEDULED;
    }

    protected void establishOffloadDate() {
        establishOffloadDate(PSPDate.getPSPTime(), true);
    }

    protected boolean validateOffloadTime() {
        SpcfCalendar offloadTime = getOffloadDate();
        SecondOffload secondOffload = OffloadGroup.findStandardOffloadGroup().getSecondOffload(offloadTime);

        if (secondOffload == null) {
            throw new RuntimeException("No scheduled (second) offload record could be located in the database for " +
                    "offload date: " + offloadTime.format("yyyyMMdd"));
        }

        // need all dates in local time for comparison
        String currentDateStr = offloadTime.format(BatchUtils.DATE_FORMAT);
        String cutoffTimeStr = secondOffload.getOverrideCutoffTime();
        SpcfCalendar cal = SpcfCalendar.parse(BatchUtils.DATE_FORMAT + BatchUtils.TIME_FORMAT, currentDateStr + cutoffTimeStr);
        SpcfCalendar cutoffTime = SpcfCalendar.createInstance(cal.getYear(),
                cal.getMonth(),
                cal.getDay(),
                cal.getHour(),
                cal.getMinute(),
                cal.getSecond(),
                cal.getMillisecond(),
                SpcfTimeZone.getLocalTimeZone());

        // invalid time if: offloadTime < cutoffTime
        boolean isValid = !offloadTime.before(cutoffTime);

        logger.info("Scheduled (second) offload time " +
                offloadTime.format(BatchUtils.DATE_TIME_FORMAT) +
                " is " + (isValid ? "valid" : "invalid (blackout)"));

        return isValid;
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

        logger.info("Starting daily batch jobs (scheduled)");

        executeStep(new StartScheduledAchOffloadMonitor());
        executeStep(new NotifyAchOffloadStarted());
        executeStep(new OffloadAchData());
        executeStep(new CreateAchFiles());
        executeStep(new UploadAchFiles());
        executeStep(new NotifyAchOffloadComplete());
        executeStep(new DownloadDicrFilesDelayPeriod());
        executeStep(new DownloadDicrFiles());
        executeStep(new ArchiveDailyFiles());
        executeStep(new UpdateMoneyMovementTransaction());
        executeStep(new UpdateFinancialTransaction()); 
        executeStep(new UpdatePayrollStatus());
        boolean shouldRunATFExtract = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_DATA_EXTRACT, false);
        if (shouldRunATFExtract) {
            executeStep(new ScheduleATFDataExtract());
        }

        executeStep(new InsertFinancialTransactionState()); // uses batch id from batch job context (or finds it if reentering)
        executeStep(new CreateTransactionOffloadedEvents());
        if(otherOffloadCompletedInsertFinancialTransactionStateStep()) {
            executeStep(new MissedPayrollProcessor());
            executeStep(new MissedTransactionProcessor());
            executeStep(new GemsAccountsReceivableProcessor());
            executeStep(new ScheduleSalesTaxExceptionProcessor());
        }

        logger.info("Completed daily batch jobs (scheduled). Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class StartScheduledAchOffloadMonitor extends BatchJobProcessorStep {
        public void execute() {
            BatchJobManager batchJobManager = new BatchJobManager();
            batchJobManager.scheduleJob(BatchJobType.ScheduledAchOffloadMonitor, getJobId());
        }
    }
    
    public class ScheduleATFDataExtract extends BatchJobProcessorStep {
        public void execute() {
            BatchJobManager batchJobManager = new BatchJobManager();
            batchJobManager.scheduleJob(BatchJobType.ATFDataExtract, ATFDataExtractRunType.UpdatedData.toString());
        }
    }
}
