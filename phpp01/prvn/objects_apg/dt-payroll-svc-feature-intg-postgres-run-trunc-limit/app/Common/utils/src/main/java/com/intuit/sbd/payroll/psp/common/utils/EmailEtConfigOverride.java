package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.netflix.hystrix.HystrixCommandProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Configuration
@Component
public class EmailEtConfigOverride {
    @Bean
    public EmailSettings emailSettings() {
        String url = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_serviceurl");
        String sendgridApi = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_api_sendgrid");
        String sendgridWithAttachmentApi = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_api_sendgridwithattachment");
        String exactTargetApi = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_api_exacttarget");
        int postRetryCount = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_et_retry_count"));
        int postRetryIntervalExponential = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_et_retry_intervalexp"));
        EmailSettings emailSettings = new EmailSettings(url, postRetryCount, postRetryIntervalExponential, sendgridApi, sendgridWithAttachmentApi, exactTargetApi);
        emailSettings.addRequestFilter(new AddHeaderRequestFilter());
        return emailSettings;
    }

    /**
     * Use this method to configure Hystrix Command Properties.
     * <p>
     * Not created as a Spring Bean as this sets the properties on the Singleton instance of HystrixCommandProperties
     */
    @PostConstruct
    public void hystrixCommandPropertiesConfiguration() {
        int timeout = 3000;
        HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeout);
        // Use the below statement to override Hystrix Command Key Properties
        //com.netflix.config.ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", 5000);
    }
}
