package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author vdammur1
 */
@Slf4j
public class CustomEmailWorkFlowFactory {

    private static final String L2SM_SEND_REMINDER_EMAILS = "L2SM_SEND_REMINDER_EMAILS";

    public ICustomEmailWorkFlowProcessor getInstance(String workflow) {
        if (Objects.nonNull(workflow) && workflow.equalsIgnoreCase(L2SM_SEND_REMINDER_EMAILS)) {
            log.info("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowFactory, Method=getInstance, Msg=Returning L2SMSendReminderEmails workflow");
            return new L2SMSendReminderEmails();
        } else {
            String err = String.format("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowFactory, Method=getInstance, Status=Error, Msg=No instance available for workflow=%s", workflow);
            log.error(err);
            throw new RuntimeException();
        }
    }
}
