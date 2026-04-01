package com.intuit.sbd.payroll.psp.gateways.walletservice.gateway;

import com.intuit.sbd.payroll.psp.payments.PaymentServiceAuthorizationManager;
import com.intuit.sbg.psp.walletservice.WalletClient;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceGatewayImpl implements WalletServiceGateway {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(WalletServiceGatewayImpl.class);

    private PaymentServiceAuthorizationManager authorizationManager;
    private WalletClient walletClient;

    @Autowired
    public WalletServiceGatewayImpl(PaymentServiceAuthorizationManager paymentServiceAuthorizationManager, WalletClient walletClient) {
        this.authorizationManager = paymentServiceAuthorizationManager;
        this.walletClient = walletClient;
    }

    @Override
    public String getDetokenizeBankAccountString(String banToken) {
        String bankAccountNumber = null;
        try {
            authorizationManager.setAuthorizationContext();
            bankAccountNumber = this.walletClient.getDetokenizedBankAccount(banToken);
        } catch (Exception walletClientException) {
            throw walletClientException;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
        return bankAccountNumber;
    }
}
