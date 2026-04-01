package com.intuit.sbd.payroll.psp.gateways.iam.invitation;

import lombok.extern.slf4j.Slf4j;
import com.intuit.sbd.payroll.psp.gateways.iam.identity.IdentityConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InvitationFactory {
    private IUSInvitationManager iusInvitationManager;
    private IDLMInvitationManager idlmInvitationManager;
    private IdentityConfigManager identityConfigManager;

    @Autowired
    public InvitationFactory(IUSInvitationManager iusInvitationManager,IDLMInvitationManager idlmInvitationManager, IdentityConfigManager identityConfigManager) {
        this.iusInvitationManager = iusInvitationManager;
        this.idlmInvitationManager = idlmInvitationManager;
        this.identityConfigManager = identityConfigManager;
    }

    public InvitationClient getInvitationClientInstance() {
        return identityConfigManager.isIDLMEnabledForInvitations() ? idlmInvitationManager : iusInvitationManager;
        }
}
