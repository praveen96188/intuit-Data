package com.intuit.sbd.payroll.psp.adapters.qbdtws.marshalling;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

/**
 * User: rnorian
 * Date: Mar 10, 2010
 * Time: 1:52:41 PM
 */
public class FieldMaskAdapter extends EmptyStringAdapter {
    @Override
    public String unmarshal(String text) {
        return super.unmarshal(text);
    }

    /**
     * Do not allow PII values (such as SSN) to be written as XML streams in clear text.
     * i.e. to the connection log
     */
    @Override
    public String marshal(String s) {
        if (s != null && s.length() > 0)
            s = "****";
        return super.marshal(s);
    }
}
