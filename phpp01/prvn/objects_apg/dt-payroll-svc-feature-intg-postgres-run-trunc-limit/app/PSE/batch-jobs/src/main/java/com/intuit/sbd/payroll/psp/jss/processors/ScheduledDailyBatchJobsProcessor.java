package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobManager;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 11, 2009
 * Time: 9:50:01 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "ScheduledDailyBatchJobs", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ScheduledDailyBatchJobsProcessor extends DailyBatchJobsProcessor {

    public ScheduledDailyBatchJobsProcessor(String[] pArguments) {
        super(pArguments);
    }

    public ScheduledDailyBatchJobsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
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

        getLogger().info("Scheduled (second) offload time " +
                offloadTime.format(BatchUtils.DATE_TIME_FORMAT) +
                " is " + (isValid ? "valid" : "invalid (blackout)"));

        return isValid;
    }

    protected void execute() {
        if (!isValidOffloadDate()) {
            getLogger().warn("Daily batch jobs skipped (specified processing date is weekend or bank holiday: " +
                    getOffloadDate().format(BatchUtils.DATE_FORMAT) + ")");
            return;
        }

        if (!isValidOffloadTime()) {
            throw new RuntimeException("Invalid offload time for " + getAchOffloadType().toString() +
                                       " offload (" + getOffloadDate().format("yyyyMMdd HH:mm:ss") + ")");
        }

        StopWatch timer = StopWatch.startTimer();

        getLogger().info("Starting daily batch jobs (scheduled)");

        executeStep(StartScheduledAchOffloadMonitor.class);
        executeStep(NotifyAchOffloadStarted.class);
        executeStep(OffloadAchData.class);
        executeStep(CreateAchFiles.class);
        executeStep(UploadAchFiles.class);
        executeStep(NotifyAchOffloadComplete.class);
        executeStep(DownloadDicrFilesDelayPeriod.class);
        executeStep(DownloadDicrFiles.class);
        executeStep(ArchiveDailyFiles.class);
        executeStep(UpdateMoneyMovementTransaction.class);
        executeStep(UpdateFinancialTransaction.class);
        executeStep(UpdatePayrollStatus.class);
        boolean shouldRunATFExtract = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_DATA_EXTRACT, false);
        if (shouldRunATFExtract) {
            executeStep(ScheduleATFDataExtract.class);
        }

        executeStep(InsertFinancialTransactionState.class); // uses batch id from batch job context (or finds it if reentering)
        executeStep(CreateTransactionOffloadedEvents.class);
        if(otherOffloadCompletedInsertFinancialTransactionStateStep()) {
            executeStep(MissedPayrollProcessor.class);
            executeStep(MissedTransactionProcessor.class);
            executeStep(GemsAccountsReceivableProcessor.class);
            executeStep(ScheduleSalesTaxExceptionProcessor.class);
        }

        getLogger().info("Completed daily batch jobs (scheduled). Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class StartScheduledAchOffloadMonitor extends JSSBatchJobStep<ScheduledDailyBatchJobsProcessor> {
        public void execute() {
            JSSBatchJobManager.scheduleJob(String.valueOf(BatchJobType.PrimaryAchOffloadMonitor), getBatchJobProcessor().getJobId());
        }
    }
    
    public static class ScheduleATFDataExtract extends JSSBatchJobStep<ScheduledDailyBatchJobsProcessor> {
        public void execute() {
            JSSBatchJobManager.scheduleJob(String.valueOf(BatchJobType.ATFDataExtract), ATFDataExtractRunType.UpdatedData.toString());
        }
    }
}
