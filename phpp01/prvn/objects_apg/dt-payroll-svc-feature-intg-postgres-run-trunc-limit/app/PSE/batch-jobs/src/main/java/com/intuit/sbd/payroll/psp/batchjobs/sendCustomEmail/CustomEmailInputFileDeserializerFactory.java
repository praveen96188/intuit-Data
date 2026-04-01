package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public  class CustomEmailInputFileDeserializerFactory {

    // Add your workflows here
    private static final String L2SM_SEND_REMINDER_EMAILS = "L2SM_SEND_REMINDER_EMAILS";

    public static InputFileDeserializer getInstance(String workflow) {
        if (Objects.nonNull(workflow) && workflow.equalsIgnoreCase(L2SM_SEND_REMINDER_EMAILS))
            return new L2SMInputFileDeserializer();
        else {
            String err = String.format("No instance for InputFileDeserializer available for workflow=%s", workflow);
            throw new RuntimeException(err);
        }
    }
}
