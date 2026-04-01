package com.intuit.sbd.payroll.psp.payments;

import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.payroll.iam.client.offline.OfflineTicketConfig;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:account-services-gateway.yml")
public class PaymentServiceGatewayConfig {
    public static final String PAYMENTS_OFFLINE_TICKET_ACCOUNT_SERVICES = "payments.offline-ticket.account-services";

    @Bean
    public boolean initializePaymentServicesOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        log.info("Inside initializePaymentServicesOfflineTicketConfig method of class PaymentServiceGatewayConfig");
        addAccountServicesOfflineTicketConfig(offlineTicketConfig);
        return true;
    }

    @Bean
    public PaymentServiceAuthorizationManager paymentServiceAuthorizationManager(AuthorizationManager authorizationManager, OfflineTicketClient offlineTicketClient) {
        return new PaymentServiceAuthorizationManager(authorizationManager, offlineTicketClient);
    }

    private void addAccountServicesOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        log.info("Inside addAccountServicesOfflineTicketConfig method of class PaymentServiceGatewayConfig");
        offlineTicketConfig.addOfflineTicketConfig(PAYMENTS_OFFLINE_TICKET_ACCOUNT_SERVICES);
    }
}
