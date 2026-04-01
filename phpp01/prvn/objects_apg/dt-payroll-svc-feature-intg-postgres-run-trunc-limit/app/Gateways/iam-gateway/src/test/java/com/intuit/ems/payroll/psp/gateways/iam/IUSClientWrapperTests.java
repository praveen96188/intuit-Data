package com.intuit.ems.payroll.psp.gateways.iam;

import com.intuit.platform.integration.ius.common.types.Persona;
import com.intuit.platform.integration.ius.common.types.RealmIdPersonaIdPair;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class IUSClientWrapperTests {

    private RealmManager realmManager;

    public IUSClientWrapperTests() {
        realmManager = new RealmManager();
    }

    @Test
    public void testMapPersonaToCompanyRealmTest() {
        String realmId = createRealmWithPayrollGrant();
        String consumerRealmId = "9130356166903416";

        List<Persona> adminPersonas = IUSClientWrapper.findAdminPersonaForConsumerRealmId(consumerRealmId);
        String authId = adminPersonas.get(0).getUserId();

        String personaId = IUSClientWrapper.getPersonaInRealmIfExists(authId, realmId);
        assertNull(personaId);

        personaId = IUSClientWrapper.addUserToRealm(authId, realmId);
        assertNotNull(personaId);
    }

    private String createRealmWithPayrollGrant() {
        String realmId = createRealm();
        realmManager.addPayrollGrantToRealm(realmId);
        return realmId;
    }

    private String createRealm() {
        List<RealmIdPersonaIdPair> realmIdPersonaIdPairs = IUSClientWrapper.createRealm("1", "TRON", "qbdttrontest+231020001@gmail.com");

        if (realmIdPersonaIdPairs.isEmpty()) {
            throw new RuntimeException("Error in Realm creation");
        }

        RealmIdPersonaIdPair realmIdPersonaIdPair = realmIdPersonaIdPairs.get(0);
        return realmIdPersonaIdPair.getRealmId();
    }
}
