package com.intuit.sbd.payroll.psp.gateways.accountservice;

import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGatewayImpl;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbd.payroll.psp.payments.PaymentServiceAuthorizationManager;
import com.intuit.sbg.psp.accountservices.AccountServicesClient;
import com.intuit.sbg.psp.accountservices.v4.AccountServicesV4Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountServiceGatewayConfig {

    @Bean
    public AccountServiceTranslator accountServiceTranslator() {
        return new AccountServiceTranslator();
    }

    @Bean
    public AccountServiceSyncDecisionManager accountServiceSyncDecisionManager(AccountServiceTranslator accountServiceTranslator) {
        return new AccountServiceSyncDecisionManager(accountServiceTranslator);
    }

    @Bean
    public AccountServiceGateway accountServiceGateway(AccountServicesClient accountServicesClient, AccountServicesV4Client accountServicesV4Client, PaymentServiceAuthorizationManager paymentServiceAuthorizationManager) {
        return new AccountServiceGatewayImpl(accountServicesClient,accountServicesV4Client,paymentServiceAuthorizationManager);
    }


}
