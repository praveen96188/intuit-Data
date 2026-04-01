package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.SecondOffload;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.psp.common.gateway.JSSGateway;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 23, 2008
 * Time: 7:08:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class SecondOffloadProcess extends Process implements IProcess {
    private OffloadGroup offloadGroup;
    private SpcfCalendar offloadDateTime;

    public SecondOffloadProcess(OffloadGroup pOffloadGroup, SpcfCalendar pOffloadDateTime) {
        offloadGroup = pOffloadGroup;
        offloadDateTime = pOffloadDateTime;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // the requested offload group must be valid (non-null)
        if (offloadGroup == null) {
            validationResult.getMessages().InvalidOffloadGroup(EntityName.SecondOffload, "SecondOffload");
            return validationResult;
        }

        // the requested second offload date/time must be valid (non-null)
        if (offloadDateTime == null) {
            validationResult.getMessages().InvalidOffloadDateTime(EntityName.SecondOffload, "SecondOffload");
            return validationResult;
        }

        // the requested second offload date/time must have today's date and must have a time of 7PM
        SpcfCalendar today7PM = PSPDate.getPSPTime();
        today7PM.setValues(today7PM.getYear(), today7PM.getMonth(), today7PM.getDay(), 19, 0, 0, 0);
        if (today7PM.compareTo(offloadDateTime) != 0) {
            String date = offloadDateTime.format("yyyyMMdd HH:mm:ss");
            validationResult.getMessages().InvalidOffloadDateTime(EntityName.SecondOffload, "SecondOffload", date);
            return validationResult;
        }

        // check to ensure a second offload is not already scheduled for today
        SecondOffload secondOffload = offloadGroup.getSecondOffload(offloadDateTime);
        if (secondOffload != null) {
            String date = offloadDateTime.format("yyyyMMdd HH:mm:ss");
            validationResult.getMessages().SecondOffloadAlreadyScheduled(EntityName.SecondOffload, "SecondOffload", date);
        }

        // check to ensure it's not past 5PM (a second offload must be scheduled before 5PM)
        SpcfCalendar now = PSPDate.getPSPTime();
        SpcfCalendar today5PM = PSPDate.getPSPTime();
        today5PM.setValues(today5PM.getYear(), today5PM.getMonth(), today5PM.getDay(), 17, 0, 0, 0);
        if (now.compareTo(today5PM) > 0) {
            String date = offloadDateTime.format("yyyyMMdd HH:mm:ss");
            validationResult.getMessages().TooLateToScheduleSecondOffload(EntityName.SecondOffload, "SecondOffload", date);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        offloadGroup.createSecondOffload(offloadDateTime);

        try {
            // schedule daily batch jobs for second offload
            String jobId = BatchUtils.scheduleJob(BatchJobType.ScheduledDailyBatchJobs.name(), null, "");
            BatchUtils.scheduleJob(BatchJobType.ScheduledDailyBatchJobsMonitor.name(), null, jobId);
        } catch (RuntimeException e) {
            String date = offloadDateTime.format("yyyyMMdd HH:mm:ss");
            processResult.getMessages().FailedToScheduleSecondOffload(EntityName.SecondOffload, "SecondOffload", date);
        } catch (Exception e) {
            String date = offloadDateTime.format("yyyyMMdd HH:mm:ss");
            processResult.getMessages().FailedToScheduleSecondOffload(EntityName.SecondOffload, "SecondOffload", date);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return processResult;
    }

}
