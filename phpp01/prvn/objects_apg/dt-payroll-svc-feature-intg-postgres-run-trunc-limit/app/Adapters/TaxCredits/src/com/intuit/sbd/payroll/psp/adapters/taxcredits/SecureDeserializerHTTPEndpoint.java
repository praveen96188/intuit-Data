package com.intuit.sbd.payroll.psp.adapters.taxcredits;

import flex.messaging.endpoints.SecureHTTPEndpoint;

/**
 * @see SecureAmfxMessageDeserializer
 */
public class SecureDeserializerHTTPEndpoint extends SecureHTTPEndpoint {

    public SecureDeserializerHTTPEndpoint() {
        this(false);
    }

    public SecureDeserializerHTTPEndpoint(boolean enableManagement) {
        super(enableManagement);
        this.deserializerClass = SecureAmfxMessageDeserializer.class;
    }

}
