package com.intuit.sbd.payroll.psp.gateways.validationservice;

import com.intuit.sbd.payroll.psp.gateways.validationservice.gateway.ValidationServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.validationservice.gateway.ValidationServiceGatewayImpl;
import com.intuit.sbd.payroll.psp.payments.PaymentServiceAuthorizationManager;
import com.intuit.sbg.psp.validationservices.ValidationServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationServiceGatewayConfig {

    @Bean
    public ValidationServiceGateway validationServiceGateway(PaymentServiceAuthorizationManager authorizationManager, ValidationServiceClient validationServiceClient) {
        return new ValidationServiceGatewayImpl(authorizationManager, validationServiceClient);
    }

}
