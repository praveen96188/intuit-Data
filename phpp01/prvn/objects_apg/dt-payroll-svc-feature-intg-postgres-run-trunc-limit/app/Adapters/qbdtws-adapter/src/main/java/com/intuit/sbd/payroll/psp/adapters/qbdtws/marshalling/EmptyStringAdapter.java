package com.intuit.sbd.payroll.psp.adapters.qbdtws.marshalling;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

/**
 * User: rnorian
 * Date: Jan 14, 2010
 * Time: 5:12:30 PM
 */
public class EmptyStringAdapter extends CollapsedStringAdapter {
    /**
     * After a string's value is collapsed according to the configured rules, further collapse the value to 'null' if
     * the string is an empty string.
     *
     * This conversion is performed to simplify change tracking when comparing an unmarshalled JAXB DTO received by
     * the web service against domain entity values retrieved from Oracle.  Oracle stores empty strings as null.  This
     * adapter makes sure that empty strings are treated consistently across JAXB and Oracle.
     */
    @Override
    public String unmarshal(String s) {
        String value = super.unmarshal(s);
        if (value.length() == 0)
            value = null;

        return value;
    }
}
