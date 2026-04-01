package com.intuit.sbd.payroll.psp.emailsender.service;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;

import java.util.concurrent.Future;

public interface EmailSendStrategy {

    EmailResponse sendMail(EmailRequest emailRequest) throws Exception;

    EmailStrategyType getSupportedEmailStrategyType();

    default Future<EmailResponse> asyncSendMail(EmailRequest emailRequest) throws Exception {
        System.out.println("Execute call asynchronously."+Thread.currentThread().getName());
        return new AsyncResult<EmailResponse>() {
            @Override
            public EmailResponse invoke() {
                try {
                    return sendMail(emailRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

}
