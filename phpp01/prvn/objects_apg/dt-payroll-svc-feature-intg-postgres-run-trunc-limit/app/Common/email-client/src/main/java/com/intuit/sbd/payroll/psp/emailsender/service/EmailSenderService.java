package com.intuit.sbd.payroll.psp.emailsender.service;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;


/**
 * This is the class to send email POST call to  TxE
 *
 * @author vishalb849
 */
@Service
public class EmailSenderService {

    private final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

    private Map<EmailStrategyType, EmailSendStrategy> emailSenderServiceMap = new HashMap<>();

    @Autowired
    private List<EmailSendStrategy> emailSenderServices;

    public Map<EmailStrategyType, EmailSendStrategy> getEmailSenderServiceMap() {
        return emailSenderServiceMap;
    }

    @PostConstruct
    public void init() {
        for (EmailSendStrategy emailSendStrategy : emailSenderServices) {
            emailSenderServiceMap.put(emailSendStrategy.getSupportedEmailStrategyType(), emailSendStrategy);
        }
    }

    //TODO: After completion of all phases, below 2 methods (sendEmail and asyncSendEmail) can be removed (Hystrix via web-service client). rename sendEmailViaOINP and asyncSendEmailViaOINP to sendEmail and asyncSendEmail

    @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{@emailSettings.postRetryCount ?: 1}", backoff = @Backoff(delayExpression = "#{@emailSettings.postRetryIntervalExponential ?:1000}", multiplier = 2))
    @HystrixCommand(groupKey = "TransactionEmailServiceSender", commandKey = "TransactionEmailServiceSender",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "25000")})
    public EmailResponse sendMail(EmailRequest emailRequest) throws Exception {

        logger.info("Email Send Started ");

        EmailSendStrategy emailSendStrategy = emailSenderServiceMap.get(emailRequest.getEmailStrategyType());

        return emailSendStrategy.sendMail(emailRequest);
    }

    @Async("threadPoolTaskExecutor")
    @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{@emailSettings.postRetryCount ?: 1}", backoff = @Backoff(delayExpression = "#{@emailSettings.postRetryIntervalExponential ?:1000}", multiplier = 2))
    @HystrixCommand(groupKey = "TransactionEmailServiceSender", commandKey = "TransactionEmailServiceSender",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "20000")})
    public Future<EmailResponse> asyncSendMail(EmailRequest emailRequest) throws Exception {

        System.out.println("Execute call asynchronously."+Thread.currentThread().getName());

        EmailSendStrategy emailSendStrategy = emailSenderServiceMap.get(emailRequest.getEmailStrategyType());

        return emailSendStrategy.asyncSendMail(emailRequest);
    }

    @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{@emailSettings.postRetryCount ?: 1}", backoff = @Backoff(delayExpression = "#{@emailSettings.postRetryIntervalExponential ?:1000}", multiplier = 2))
    public EmailResponse sendMailViaOINP(EmailRequest emailRequest) throws Exception {

        logger.info("OINP: Email Send Started ");

        EmailSendStrategy emailSendStrategy = emailSenderServiceMap.get(emailRequest.getEmailStrategyType());

        return emailSendStrategy.sendMail(emailRequest);
    }

    @Async("threadPoolTaskExecutor")
    @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{@emailSettings.postRetryCount ?: 1}", backoff = @Backoff(delayExpression = "#{@emailSettings.postRetryIntervalExponential ?:1000}", multiplier = 2))
    public Future<EmailResponse> asyncSendMailViaOINP(EmailRequest emailRequest) throws Exception {

        System.out.println("OINP: Execute call asynchronously."+Thread.currentThread().getName());

        EmailSendStrategy emailSendStrategy = emailSenderServiceMap.get(emailRequest.getEmailStrategyType());

        return emailSendStrategy.asyncSendMail(emailRequest);
    }
}
