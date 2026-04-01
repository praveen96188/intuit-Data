package com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 26, 2010
 * Time: 2:41:34 PM
 */
public class TaxCreditsAdapterTests {
    @Test
    public void testHttpRequest() throws Exception {
        TaxCreditsAdapter taxCreditsAdapter = new TaxCreditsAdapter();

        assertFalse("address in a rc or ez", taxCreditsAdapter.isAddressInRCorEZ("6320 Kalamath Ct", "89433"));
        assertTrue("address not in a rc or ez", taxCreditsAdapter.isAddressInRCorEZ("100 E 5TH AVE", "43201"));
        assertTrue("address not in a rc or ez", taxCreditsAdapter.isAddressInRCorEZ("764 S BLANCHE ST", "62964"));
        assertTrue("address not in a rc or ez", taxCreditsAdapter.isAddressInRCorEZ("321 TUSCALOOSA ST", "35462"));

        // invalid address
        boolean addressFound = false;
        try {
            addressFound = taxCreditsAdapter.isAddressInRCorEZ("123 Street", "89511");
        } catch (AddressDoesNotExistException ae) {
            assertNotNull(ae);
        } catch (Exception e) {
            fail();
        }
        assertFalse(addressFound);
    }
}
