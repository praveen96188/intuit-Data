package com.intuit.sbd.payroll.psp.gateways.iam.auth;

import com.intuit.identity.graphql.sdk.client.auth.Authentication;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.NonNull;

public class OfflineAuth implements Authentication {
    private final String offlineAuthHeader;

    public OfflineAuth(@NonNull String offlineTicket) {
        this.offlineAuthHeader = offlineTicket;
    }

    public OfflineAuth(@NonNull IAMTicket iamTicket) {
        this(iamTicket.getTicket());
    }

    @Override
    public String getAuthorizationHeader() {
        return offlineAuthHeader;
    }

}
