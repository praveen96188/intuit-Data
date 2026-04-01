package com.intuit.sbd.payroll.psp.gateways.iam.identity.grants;

import com.intuit.platform.integration.ius.common.types.Grant;

import java.util.List;

public interface IdentityGrantClient {

    List<Grant> getAllGrants(String realmId);

    void updateGrants(Grant grant);

    void addGrants(Grant grant, boolean isUserContextRequired);
}
