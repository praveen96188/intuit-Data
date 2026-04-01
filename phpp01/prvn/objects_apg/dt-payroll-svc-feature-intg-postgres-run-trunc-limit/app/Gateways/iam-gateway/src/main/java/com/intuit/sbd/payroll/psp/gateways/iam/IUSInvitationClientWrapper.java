package com.intuit.sbd.payroll.psp.gateways.iam;

import com.intuit.client.ius.IUSInvitationClient;
import com.intuit.client.ius.IUSRestTransport;
import com.intuit.platform.integration.ius.common.types.CreateInvitationsRequest;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.platform.integration.ius.common.types.Invitations;
import com.intuit.sbg.psp.webserviceclient.ratelimiter.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IUSInvitationClientWrapper {

    @Autowired
    private IUSRestTransport iusRestTransport;

    @Autowired
    private RateLimiterService<Invitations> invitationsRateLimiterService;

    public Invitations sendInvite(IAMTicket iamTicket, CreateInvitationsRequest createInvitationsRequest, boolean isResend) {
        IUSInvitationClient.setTransport(iusRestTransport);
        return invitationsRateLimiterService.execute(() -> IUSInvitationClient.createInvitation(iamTicket, createInvitationsRequest, isResend));
    }
}
