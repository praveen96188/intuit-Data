package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.ConfigFileModel;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author vdammur1
 */
@Slf4j
public class CustomEmailWorkFlowManager {

    private static final int TIME_THRESHOLD = 300000; // 5 minutes

    public CustomEmailWorkFlowManager() {

    }

    public Set<String> getWorkflowsToExecute(ConfigFileModel[] configs) throws ParseException {
        // TODO: Write Unit Tests for this code
        Set<String> workflowsToExecute = new HashSet<>();
        SpcfCalendar pspSpcfTimeTime = PSPDate.getPSPTime();

        SimpleDateFormat format
                = new SimpleDateFormat("HH:mm");
        Date pspDate = pspSpcfTimeTime.toDate();
        String pspTimeString = new SimpleDateFormat("H:mm").format(pspDate);
        Date pspFormattedDate = format.parse(pspTimeString);

        for (int i = 0; i < configs.length; i++) {
            ConfigFileModel config = configs[i];
            log.info("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowManager, Method=getWorkflowsToExecute, workflowName={}, isOverrideScheduleTime={}", config.getWorkflowName(), config.isOverrideScheduleTime());
            if(config.isOverrideScheduleTime()) {
                workflowsToExecute.add(config.getWorkflowName());
                continue;
            }
            String scheduledTimeString = config.getScheduledTime();
            log.info("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowManager, Method=getWorkflowsToExecute, workflowName={}, scheduledTime={}", config.getWorkflowName(), config.getScheduledTime());
            Date configDate = format.parse(scheduledTimeString);

            long differenceInMilliSeconds
                    = Math.abs(pspFormattedDate.getTime() - configDate.getTime());

            if(differenceInMilliSeconds <= TIME_THRESHOLD) {
                String workFlowtoExecute = config.getWorkflowName();
                workflowsToExecute.add(workFlowtoExecute);
            } else {
                log.info("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowManager, Method=getWorkflowsToExecute, Msg=TimeDifferenceGreaterThanThreshold, TIME_THRESHOLD={}", TIME_THRESHOLD);
            }
        }
        return workflowsToExecute;
    }
}
