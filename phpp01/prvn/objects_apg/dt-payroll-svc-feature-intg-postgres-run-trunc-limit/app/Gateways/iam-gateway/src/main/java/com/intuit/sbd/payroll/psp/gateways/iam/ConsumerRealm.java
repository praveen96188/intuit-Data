package com.intuit.sbd.payroll.psp.gateways.iam;

import com.intuit.platform.integration.ius.common.types.Persona;
import com.intuit.platform.integration.ius.common.types.User;
import com.intuit.sbd.payroll.psp.gateways.iam.exception.MultiplePersonaConsumerException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ihannur
 * Date: 6/17/13
 * Time: 4:52 PM
 */
public class ConsumerRealm {
    private static final String CONSUMER_REALM_HAS_MORE_THAN_ONE_PERSONA = "Consumer realm has more than one Persona!";

    public User getUserForConsumerRealmId(String consumerRealmId){
        List<User> adminUsersForRealmId = new ArrayList<>();
        List<Persona> adminPersonas = IUSClientWrapper.findAdminPersonaForConsumerRealmId(consumerRealmId);

        if(adminPersonas != null && !adminPersonas.isEmpty()) {
            if(adminPersonas.size() > 1){
                throw new MultiplePersonaConsumerException(CONSUMER_REALM_HAS_MORE_THAN_ONE_PERSONA);
            }
            String authId = adminPersonas.get(0).getUserId();
            adminUsersForRealmId = IUSClientWrapper.findUsersForAuthId(authId);
        }

        if (adminUsersForRealmId != null && !adminUsersForRealmId.isEmpty()) {
            return adminUsersForRealmId.get(0);
        }
        return null;
    }

    public String getAuthIdFromConsumerRealmId(String consumerRealmId) {
        List<Persona> adminPersonas = IUSClientWrapper.findAdminPersonaForConsumerRealmId(consumerRealmId);

        if (adminPersonas != null && !adminPersonas.isEmpty()) {
            if (adminPersonas.size() > 1) {
                throw new MultiplePersonaConsumerException(CONSUMER_REALM_HAS_MORE_THAN_ONE_PERSONA);
            }
            return adminPersonas.get(0).getUserId();
        }
        return null;
    }
}
