package com.intuit.sbd.payroll.psp.gateways.iam.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdentityServiceClientFactory {
    private IDLMServiceClient idlmServiceClient;
    private IUSServiceClient iusServiceClient;
    private IdentityConfigManager identityConfigManager;

    @Autowired
    public IdentityServiceClientFactory(IDLMServiceClient idlmServiceClient, IUSServiceClient iusServiceClient, IdentityConfigManager identityConfigManager) {
        this.idlmServiceClient = idlmServiceClient;
        this.iusServiceClient = iusServiceClient;
        this.identityConfigManager = identityConfigManager;
    }

    public IdentityServiceClient getIdentityServiceClient() {
        return identityConfigManager.isIDLMEnabled() ? idlmServiceClient : iusServiceClient;
    }

}
