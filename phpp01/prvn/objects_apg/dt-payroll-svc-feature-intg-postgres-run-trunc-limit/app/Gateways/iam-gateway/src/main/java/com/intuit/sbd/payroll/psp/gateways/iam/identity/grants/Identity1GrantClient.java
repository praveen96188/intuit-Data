package com.intuit.sbd.payroll.psp.gateways.iam.identity.grants;

import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSClientWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Identity1GrantClient implements IdentityGrantClient {

    @Override
    public List<Grant> getAllGrants(String realmId) {
        return IUSClientWrapper.findAllGrantsForRealmId(realmId);
    }

    @Override
    public void updateGrants(Grant grant) {
        IUSClientWrapper.updateGrant(grant);
    }

    @Override
    public void addGrants(Grant grant, boolean isUserContextRequired) {
        if(isUserContextRequired) {
            IUSClientWrapper.addGrantWithUserContext(grant);
        } else {
            IUSClientWrapper.addGrant(grant);
        }
    }
}
