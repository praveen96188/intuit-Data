package com.intuit.sbd.payroll.psp.gateways.iam.identity.grants;

import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.sbd.payroll.psp.gateways.iam.identity.IdentityAuthManager;
import com.intuit.sbg.psp.grantsservices.GrantsServicesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class Identity2GrantClient implements IdentityGrantClient {

    private IdentityAuthManager authorizationManager;
    private GrantsServicesClient grantsServicesClient;

    @Autowired
    public Identity2GrantClient(IdentityAuthManager authorizationManager, GrantsServicesClient grantsServicesClient) {
        this.authorizationManager = authorizationManager;
        this.grantsServicesClient = grantsServicesClient;
    }

    @Override
    public List<Grant> getAllGrants(String realmId) {
        try {
            log.info("Action=Identity2GrantClient.getAllGrants,Status=started,RealmId={}", realmId);
            authorizationManager.setAuthorizationContext(null);
            List<Grant> grants = grantsServicesClient.getAllGrants(realmId);
            log.info("Action=Identity2GrantClient.getAllGrants,Status=completed,RealmId={}", realmId);
            return grants;
        } catch (Exception e) {
            log.error("Action=Identity2GrantClient.getAllGrants,Status=Exception,RealmId={}", realmId, e);
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
    }

    @Override
    public void updateGrants(Grant grant) {
        try {
            log.info("Action=Identity2GrantClient.updateGrants,Status=started,RealmId={}", grant.getRealmId());
            authorizationManager.setAuthorizationContext(grant.getRealmId());
            grantsServicesClient.updateGrants(grant, grant.getOfferingId());
            log.info("Action=Identity2GrantClient.updateGrants,Status=completed,RealmId={}", grant.getRealmId());
        } catch (Exception e) {
            log.error("Action=Identity2GrantClient.updateGrants,Status=Exception,RealmId={}", grant.getRealmId(), e);
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
    }

    @Override
    public void addGrants(Grant grant, boolean isUserContextRequired) {
        try {
            log.info("Action=Identity2GrantClient.addGrants,Status=started,RealmId={}", grant.getRealmId());
            if (!isUserContextRequired) {
                authorizationManager.setAuthorizationContext(null);
            }
            grantsServicesClient.addGrants(grant);
            log.info("Action=Identity2GrantClient.addGrants,Status=completed,RealmId={}", grant.getRealmId());
        } catch (Exception e) {
            log.error("Action=Identity2GrantClient.addGrants,Status=Exception,RealmId={}", grant.getRealmId(), e);
            throw e;
        } finally {
            if (!isUserContextRequired) {
                authorizationManager.removeAuthorizationContext();
            }
        }
    }
}