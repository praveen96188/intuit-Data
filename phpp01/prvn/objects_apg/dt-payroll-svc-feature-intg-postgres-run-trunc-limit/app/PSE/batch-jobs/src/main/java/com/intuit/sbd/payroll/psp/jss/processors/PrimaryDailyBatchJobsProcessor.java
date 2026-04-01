package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.BatchJobSetup;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.SecondOffload;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobManager;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.util.QuartzUtils;
import com.intuit.sbd.payroll.psp.jss.util.TimeExpressionConverter;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 4/24/17
 * Time: 1:07 AM
 * To change this template use File | Settings | File Templates.
 */

@ScheduledJob(name = "PrimaryDailyBatchJobs", resourcePath = "/high", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class PrimaryDailyBatchJobsProcessor extends DailyBatchJobsProcessor {
    
    public PrimaryDailyBatchJobsProcessor(String[] pArguments) {
        super(pArguments);
    }

    public PrimaryDailyBatchJobsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    protected AchOffloadType getAchOffloadType() {
        return AchOffloadType.PRIMARY;
    }

    protected void establishOffloadDate() {
        SpcfCalendar now = PSPDate.getPSPTime();
        SpcfCalendar offloadDate = null;
        String commandLine = getJobInstanceParameters().trim();

        if (commandLine.length() == 0) {
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

        getLogger().info("Primary offload time " +
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

    @Override
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

        getLogger().info("Starting daily batch jobs (primary)");

        executeStep(StartPrimaryAchOffloadMonitor.class);
        executeStep(NotifyAchOffloadStarted.class);
        executeStep(OffloadAchData.class);
        executeStep(CreateAchFiles.class);
        executeStep(UploadAchFiles.class);
        executeStep(NotifyAchOffloadComplete.class);
        executeStep(PauseLedgerBalanceProcessor.class);
        executeStep(DownloadDicrFilesDelayPeriod.class);
        executeStep(DownloadDicrFiles.class);
        executeStep(ArchiveDailyFiles.class);
        executeStep(UpdateMoneyMovementTransaction.class);
        executeStep(UpdateFinancialTransaction.class);
        executeStep(UpdatePayrollStatus.class);
        //Honor kill switch for extract and ensure a second offload isn't scheduled.  If we have a second offload scheduled, we'll do the extract after that completes, and it will
        // take care of both the first and second offload
        boolean shouldRunATFExtract = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_DATA_EXTRACT, false);
        if (!isSecondOffloadScheduled() && shouldRunATFExtract) {
            executeStep(ScheduleATFDataExtract.class);
        }
        executeStep(InsertFinancialTransactionState.class); // uses batch id from batch job context (or finds it if reentering)
        executeStep(CreateTransactionOffloadedEvents.class);

        if (isSecondOffloadScheduled() && !otherOffloadCompletedInsertFinancialTransactionStateStep()) {
            getLogger().info("Executed Primary ACH Offload only - deferring other daily jobs for second offload.");
        } else {
            executeStep(MissedPayrollProcessor.class);
            executeStep(MissedTransactionProcessor.class);
            executeStep(GemsAccountsReceivableProcessor.class);
            executeStep(ScheduleSalesTaxExceptionProcessor.class);
        }

        executeStep(ResumeLedgerBalanceProcessor.class);

        getLogger().info("Completed daily batch jobs (primary). Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class StartPrimaryAchOffloadMonitor extends JSSBatchJobStep<PrimaryDailyBatchJobsProcessor> {
        public void execute() {
        	JSSBatchJobManager.scheduleJob(String.valueOf(BatchJobType.PrimaryAchOffloadMonitor), getBatchJobProcessor().getJobId());
        }
    }

    public static class ScheduleATFDataExtract extends JSSBatchJobStep<PrimaryDailyBatchJobsProcessor> {
        public void execute() {
        	JSSBatchJobManager.scheduleJob(String.valueOf(BatchJobType.ATFDataExtract), ATFDataExtractRunType.UpdatedData.toString());
        }
    }

    public static class PauseLedgerBalanceProcessor extends JSSBatchJobStep<PrimaryDailyBatchJobsProcessor> {
        public void execute() {
            //Do not run on LOCAL environment
            if(Application.getEnvironmentName().equalsIgnoreCase("LOCAL")){
                getLogger().info("Skip pausing of LedgerBalance job ");
                return;
            }

            JSSBatchJobManager.updateJobSuspendStatus(BatchJobType.LedgerBalance.name(), true);
            
            getLogger().info("Pausing LedgerBalance job");
        }
    }

    public static class ResumeLedgerBalanceProcessor extends JSSBatchJobStep<PrimaryDailyBatchJobsProcessor> {
        public void execute() {
            //Do not run on LOCAL environment
            if(Application.getEnvironmentName().equalsIgnoreCase("LOCAL")){
                getLogger().info("Skip resuming of LedgerBalance job");
                return;
            }

            try {
            	BatchJobSetup batchJobSetup = BatchJobManager.getBatchJobSetup(BatchJobType.LedgerBalance); 
                JSSBatchJobManager.updateJobSuspendStatus(BatchJobType.LedgerBalance.name(), false);
                getLogger().info("Resuming LedgerBalance job");
                
                SpcfCalendar currentOffloadDate = getBatchJobProcessor().getOffloadDate().copy();
                currentOffloadDate.addDays(1); // Adding a day because LedgerBalance runs the day after the offload
                
                SpcfCalendar currentTime = PSPDate.getPSPTime();
                String quartzSchedule = TimeExpressionConverter.convertFluxToQuartz(batchJobSetup.getJobTimerExpression());
                long calculatedScheduleTime = QuartzUtils.getNextValidTimeAfter(currentOffloadDate.getTimeInMilliseconds(), quartzSchedule);
                getLogger().info("calculatedScheduleTime "+new Date(calculatedScheduleTime)+" Current Time "+new Date(currentTime.getTimeInMilliseconds()));
                if(currentTime.getTimeInMilliseconds() >= calculatedScheduleTime){
                    JSSBatchJobManager.runJob(BatchJobType.LedgerBalance.name());
                    getLogger().info("LedgerBalance job was scheduled at "+ new Date(calculatedScheduleTime)+" but the PrimaryDailyBatchJobs job completed on "+currentTime+" . So expediting the LedgerBalance job");
                }
            } catch(Throwable t){
                getLogger().warn("Exception while resuming/expediting the LedgerBalance job", t);
            }
        }
    }
}
