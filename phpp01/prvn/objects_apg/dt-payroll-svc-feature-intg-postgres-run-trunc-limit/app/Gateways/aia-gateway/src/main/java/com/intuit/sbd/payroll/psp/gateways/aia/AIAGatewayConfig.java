package com.intuit.sbd.payroll.psp.gateways.aia;

import com.intuit.sbg.psp.payroll.iam.client.offline.OfflineTicketConfig;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:aia-gateway.yml")
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.gateways.aia.paymentsprofile"})
public class AIAGatewayConfig {

    public static final String PAYMENTS_PROFILE_OFFLINE_TICKET_ACCOUNT_SERVICES = "payments.offline-ticket.account-services";

    @Bean
    public boolean initializePaymentProfileServicesOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        addAccountServicesOfflineTicketConfig(offlineTicketConfig);
        return true;
    }

    private void addAccountServicesOfflineTicketConfig(OfflineTicketConfig offlineTicketConfig) {
        offlineTicketConfig.addOfflineTicketConfig(PAYMENTS_PROFILE_OFFLINE_TICKET_ACCOUNT_SERVICES);
    }

}
