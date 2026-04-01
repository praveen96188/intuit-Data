package com.intuit.sbd.payroll.psp.gateways.iam.identity;

import com.intuit.identity.graphql.sdk.client.exceptions.IdentityGraphQLException;
import com.intuit.identity.idlm.exception.IDLMException;

public interface IdentityServiceClient {
    String addUserToRealm(String userAuthId, String realmId) throws IdentityGraphQLException, IDLMException;
    String getPersonaInRealmIfExists(String userAuthId, String realmId) throws IdentityGraphQLException, IDLMException;
    }
