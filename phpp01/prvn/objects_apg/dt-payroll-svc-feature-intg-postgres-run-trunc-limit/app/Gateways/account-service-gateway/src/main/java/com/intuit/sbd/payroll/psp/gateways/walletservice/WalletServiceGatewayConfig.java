package com.intuit.sbd.payroll.psp.gateways.walletservice;

import com.intuit.sbd.payroll.psp.gateways.walletservice.gateway.WalletServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.walletservice.gateway.WalletServiceGatewayImpl;
import com.intuit.sbd.payroll.psp.payments.PaymentServiceAuthorizationManager;
import com.intuit.sbg.psp.walletservice.WalletClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletServiceGatewayConfig {

    @Bean
    public WalletServiceGateway walletServiceGateway(PaymentServiceAuthorizationManager authorizationManager, WalletClient walletClient) {
        return new WalletServiceGatewayImpl(authorizationManager, walletClient);
    }

}
