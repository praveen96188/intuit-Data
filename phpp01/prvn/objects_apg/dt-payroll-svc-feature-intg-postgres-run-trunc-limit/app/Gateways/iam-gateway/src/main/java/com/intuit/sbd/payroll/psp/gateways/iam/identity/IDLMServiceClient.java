package com.intuit.sbd.payroll.psp.gateways.iam.identity;

import com.intuit.identity.graphql.sdk.client.exceptions.IdentityGraphQLException;
import com.intuit.identity.idlm.exception.IDLMException;
import com.intuit.sbd.payroll.psp.gateways.iam.IDLMClientWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IDLMServiceClient implements IdentityServiceClient{
    private IDLMClientWrapper idlmClientWrapper;

    @Autowired
    public IDLMServiceClient(IDLMClientWrapper idlmClientWrapper) {
        this.idlmClientWrapper = idlmClientWrapper;
    }
    @Override
    public String addUserToRealm(String userAuthId, String realmId) throws IdentityGraphQLException, IDLMException {
        return idlmClientWrapper.addUserToRealm(userAuthId, realmId);
    }

    @Override
    public String getPersonaInRealmIfExists(String userAuthId, String realmId) throws IdentityGraphQLException, IDLMException {
        String profileId = idlmClientWrapper.getPersonaInRealmIfExists(userAuthId, realmId);
        log.info("action=getPersonaInRealmIfExists,profileId={}", profileId);
        return profileId;
    }
}
