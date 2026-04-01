package com.intuit.sbd.payroll.psp.adapters.qbdtws.marshalling;

/**
 * User: rnorian
 * Date: Mar 3, 2010
 * Time: 9:52:17 PM
 */
public class ToUpperStringAdapter extends EmptyStringAdapter {
    @Override
    public String unmarshal(String s) {
        String val = super.unmarshal(s);
        if (val != null)
            val = val.toUpperCase();
        return val;
    }
}
