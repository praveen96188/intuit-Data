package com.intuit.sbd.payroll.psp.gateways.iam.identity;


import com.intuit.sbd.payroll.psp.gateways.iam.IUSClientWrapper;
import org.springframework.stereotype.Component;

@Component
public class IUSServiceClient implements IdentityServiceClient{
    @Override
    public String addUserToRealm(String userAuthId, String realmId) {
        return IUSClientWrapper.addUserToRealm(userAuthId, realmId);
    }

    @Override
    public String getPersonaInRealmIfExists(String userAuthId, String realmId) {
        return IUSClientWrapper.getPersonaInRealmIfExists(userAuthId, realmId);
    }
}
