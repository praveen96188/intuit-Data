package com.intuit.sbd.payroll.psp.adapters.sap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SAPSpringConfig {

    @Bean
    public HttpFirewall looseHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedPeriod(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }

    @Bean
    public boolean initializeSAPFilterChainProxy(FilterChainProxy filterChainProxy, HttpFirewall httpFirewall) {
        filterChainProxy.setFirewall(httpFirewall);
        return true;
    }

    @Bean("SAPAuthenticationOperations")
    @ConfigurationProperties(prefix="payroll.sap.authentication.operation")
    public List<String> getSAPAuthenticationOperations(){
        return new ArrayList<>();
    }

}
