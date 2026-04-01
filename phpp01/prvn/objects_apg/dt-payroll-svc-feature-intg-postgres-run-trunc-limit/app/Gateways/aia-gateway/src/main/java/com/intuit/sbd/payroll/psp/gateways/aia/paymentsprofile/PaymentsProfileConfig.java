package com.intuit.sbd.payroll.psp.gateways.aia.paymentsprofile;

import com.intuit.sbg.psp.payroll.iam.client.offline.OfflineTicketConfig;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@Getter
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.gateways.aia.paymentsprofile"})
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:aia-gateway.yml")
public class PaymentsProfileConfig {

    @Value("${payments.profile.url}")
    private String baseUrl;
    @Value("${payments.profile.endpoints.search-payment-profile-eocliccan}")
    private String searchPaymentProfile;

    public String getSearchPaymentsProfileResponseEocLicCan(){
        return String.join("", this.getBaseUrl(), this.getSearchPaymentProfile());
    }


}
