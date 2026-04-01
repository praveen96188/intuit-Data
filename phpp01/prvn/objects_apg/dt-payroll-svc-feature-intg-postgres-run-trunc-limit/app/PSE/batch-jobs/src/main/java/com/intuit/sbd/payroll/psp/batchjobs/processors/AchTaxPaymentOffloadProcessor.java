package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 11, 2009
 * Time: 9:49:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class AchTaxPaymentOffloadProcessor extends DailyBatchJobsProcessor {
    public AchTaxPaymentOffloadProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    protected AchOffloadType getAchOffloadType() {
        return AchOffloadType.TAX_PAYMENT;
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

        establishOffloadDate(offloadDate, true);
    }

    protected boolean validateOffloadTime() {
        SpcfCalendar cal, offloadTime = getOffloadDate();
        String currentDateStr = offloadTime.format(BatchUtils.DATE_FORMAT);
        String cutoffTimeStr = OffloadGroup.findTaxPaymentOffloadGroup().getCutoffTime();

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
        boolean isValid = !(offloadTime.before(cutoffTime));

        logger.info("Primary offload time " +
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

        logger.info("Starting daily batch jobs (primary)");

//        executeStep(new StartAchTaxPaymentOffloadMonitor());
        executeStep(new NotifyAchOffloadStarted());
        executeStep(new OffloadAchData());
        executeStep(new CreateAchFiles());
        executeStep(new UploadAchFiles());
        executeStep(new NotifyAchOffloadComplete());
        executeStep(new DownloadDicrFilesDelayPeriod());
        executeStep(new DownloadDicrFiles());
        executeStep(new ArchiveDailyFiles());
        executeStep(new UpdateMoneyMovementTransaction());
        executeStep(new UpdateFinancialTransaction()); // uses batch id from batch job context (or finds it if reentering)
        executeStep(new InsertFinancialTransactionState()); // uses batch id from batch job context (or finds it if reentering)
        executeStep(new UpdateTaxPaymentsAgencyStatus());

        logger.info("Completed ACH Tax Payment Offload. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

//    public class StartAchTaxPaymentOffloadMonitor extends BatchJobProcessorStep {
//        public void execute() {
//            BatchJobManager batchJobManager = new BatchJobManager();
//            batchJobManager.scheduleJob(BatchJobType.AchTaxPaymentOffloadMonitor , getJobId());
//        }
//    }
}
