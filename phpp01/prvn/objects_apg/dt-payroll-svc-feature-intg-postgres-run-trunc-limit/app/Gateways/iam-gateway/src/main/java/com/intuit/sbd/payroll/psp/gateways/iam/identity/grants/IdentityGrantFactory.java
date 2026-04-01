package com.intuit.sbd.payroll.psp.gateways.iam.identity.grants;

import com.intuit.sbd.payroll.psp.gateways.iam.identity.IdentityConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdentityGrantFactory {

    private Identity1GrantClient identity1GrantClient;
    private Identity2GrantClient identity2GrantClient;
    private IdentityConfigManager identityConfigManager;

    @Autowired
    public IdentityGrantFactory(Identity1GrantClient identity1GrantClient, Identity2GrantClient identity2GrantClient, IdentityConfigManager identityConfigManager) {
        this.identity1GrantClient = identity1GrantClient;
        this.identity2GrantClient = identity2GrantClient;
        this.identityConfigManager = identityConfigManager;
    }

    public IdentityGrantClient getIdentityGrantsClientInstance() {
        return identityConfigManager.isGrant2Enabled() ? identity2GrantClient : identity1GrantClient;
    }
}