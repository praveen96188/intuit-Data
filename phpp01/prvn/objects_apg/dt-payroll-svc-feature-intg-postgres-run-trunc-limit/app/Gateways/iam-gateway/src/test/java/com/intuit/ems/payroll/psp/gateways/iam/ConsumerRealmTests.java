package com.intuit.ems.payroll.psp.gateways.iam;


import com.intuit.platform.integration.ius.common.types.FullName;
import com.intuit.platform.integration.ius.common.types.User;
import com.intuit.sbd.payroll.psp.gateways.iam.ConsumerRealm;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: ihannur
 * Date: 6/17/13
 * Time: 4:58 PM
 */
public class ConsumerRealmTests {

    @Test
    public void testGetUserForConsumerRealmId() {
        ConsumerRealm consumerRealm = new ConsumerRealm();
        User user = consumerRealm.getUserForConsumerRealmId("222112271");
        assertNotNull("Response", user);
        assertEquals("Email Id:", "vmpqa2011+ee1bonus_iamtestpass@gmail.com", user.getEmail().getAddress());

        FullName fullName = user.getFullName().get(0);

        assertEquals("Given Name:", "Employee", fullName.getGivenName());
        assertEquals("Sur Name:", "Bonus Tax By ER", fullName.getSurName());
    }
}
