package com.intuit.sbd.payroll.psp.emailsender;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaPublisherConfig;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.payroll.iam.IamClientConfig;
import com.intuit.sbg.psp.payroll.iam.client.offline.OfflineTicketConfig;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import com.intuit.sbg.psp.webserviceclient.WebServiceClientConfig;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author vishalb849
 */
@Configuration
@EnableRetry
@EnableAsync
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.emailsender"})
@Import(value = {WebServiceClientConfig.class, KafkaPublisherConfig.class, IamClientConfig.class})
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:email-client.yaml")
public class EmailConfig {

    public static final String PSP_OFFLINE_TICKET = "psp.offline-ticket";

    @Bean
    public Client client() {
        ClientHandler clientHandler = new URLConnectionClientHandler();
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(JacksonJaxbJsonProvider.class);
        Client client = new Client(clientHandler, clientConfig);
        return client;
    }

    @Bean
    public HystrixCommandAspect hystrixAspect() {
        return new HystrixCommandAspect();
    }

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    /*@Bean
    public EmailSettings emailSettings() {
        String url = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_serviceurl");
        String sendgridApi = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_api_sendgrid");
        String sendgridWithAttachmentApi = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_api_sendgridwithattachment");
        String exactTargetApi = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_api_exacttarget");
        int postRetryCount = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_et_retry_count"));
        int postRetryIntervalExponential = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_et_retry_intervalexp"));
        EmailSettings emailSettings = new EmailSettings(url, postRetryCount, postRetryIntervalExponential, sendgridApi, sendgridWithAttachmentApi, exactTargetApi);
        return emailSettings;
    }*/

    @Bean
    public boolean initializePSPOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        addEmailServicesOfflineTicketConfig(offlineTicketConfig);
        return true;
    }

    @Bean
    public EmailAuthorizationManager emailAuthorizationManager(AuthorizationManager authorizationManager, OfflineTicketClient offlineTicketClient) {
        return new EmailAuthorizationManager(authorizationManager, offlineTicketClient);
    }

    private void addEmailServicesOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        offlineTicketConfig.addOfflineTicketConfig(PSP_OFFLINE_TICKET);
    }
}
