package com.intuit.sbd.payroll.psp.gateways.iam.identity;

import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class IdentityAuthManager {

    private OfflineTicketClient offlineTicketClient;

    @Autowired
    public IdentityAuthManager(OfflineTicketClient offlineTicketClient) {
        this.offlineTicketClient = offlineTicketClient;
    }

    public void setAuthorizationContext(String realmId) {
        String offlineTicket = Objects.isNull(realmId) ? offlineTicketClient.getOfflineTicket() : offlineTicketClient.getOfflineTicket(realmId);
        RequestAttributesUtils.setAttribute(ContextConstants.AUTHN_CONTEXT, offlineTicket);
    }

    public void removeAuthorizationContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.AUTHN_CONTEXT);
    }
}