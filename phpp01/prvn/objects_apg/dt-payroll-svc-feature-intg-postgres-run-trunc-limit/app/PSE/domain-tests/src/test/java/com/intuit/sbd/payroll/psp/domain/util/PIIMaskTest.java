package com.intuit.sbd.payroll.psp.domain.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: TimothyD698
 * Date: 1/10/13
 */
public class PIIMaskTest {

    @Test
    public void testPIIMasking() {

        // Bank Account Masking
        String testString = "<AccountNumber>12345</AccountNumber>\n";
        assertEquals("<AccountNumber>*2345</AccountNumber>\n", PIIMask.getMaskedString(testString, Boolean.FALSE, Boolean.FALSE));

        testString = "<AccountNumber>12345</AccountNumber>\n";
        assertEquals(testString, PIIMask.getMaskedString(testString, Boolean.TRUE, Boolean.TRUE));

        // SSN Masking
        testString = "<SocialSecurityNumber>12345</SocialSecurityNumber>\n";
        assertEquals("<SocialSecurityNumber>*2345</SocialSecurityNumber>\n", PIIMask.getMaskedString(testString, Boolean.FALSE, Boolean.FALSE));

        testString = "<SocialSecurityNumber>12345</SocialSecurityNumber>\n";
        assertEquals(testString, PIIMask.getMaskedString(testString, Boolean.TRUE, Boolean.TRUE));

        // Test Empty Strings
        testString = "<AccountNumber></AccountNumber>\n";
        assertEquals(testString, PIIMask.getMaskedString(testString, Boolean.FALSE, Boolean.FALSE));
        testString = "<SocialSecurityNumber></SocialSecurityNumber>\n";
        assertEquals(testString, PIIMask.getMaskedString(testString, Boolean.FALSE, Boolean.FALSE));

    }
}
