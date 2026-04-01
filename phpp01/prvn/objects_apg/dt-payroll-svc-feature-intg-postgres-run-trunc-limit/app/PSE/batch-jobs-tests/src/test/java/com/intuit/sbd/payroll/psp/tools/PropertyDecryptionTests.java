package com.intuit.sbd.payroll.psp.tools;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationProxy;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 25, 2010
 * Time: 11:32:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyDecryptionTests {
    @Test
    public void testDecryptProperty() {
        // decrypted property value should be: test
        String prop = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_password");
        assertEquals("Property decryption failed: ", "test", prop);
    }

    @Test
    public void testIDPSDecodeProperty() {
        // encrypted property value should be: IDPS(bank-password)
        // decoded property value should be: test
        ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration(ConfigurationModule.BatchJobs);
        String encProp = config.getString("psp_batch_bank_password");        

        assertEquals("Unexpected value for encrypted property: ",
                     "IDPS(bank-password)",
                     encProp);

        String decodedProp = ConfigurationProxy.decodeProperty(encProp);

        assertEquals("Property decryption failed: ", "test", decodedProp);
    }
}
