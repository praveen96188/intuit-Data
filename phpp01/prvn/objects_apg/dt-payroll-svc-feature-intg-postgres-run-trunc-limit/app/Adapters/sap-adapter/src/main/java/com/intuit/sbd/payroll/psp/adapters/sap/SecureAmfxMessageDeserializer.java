package com.intuit.sbd.payroll.psp.adapters.sap;

import flex.messaging.io.amfx.AmfxMessageDeserializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class SecureAmfxMessageDeserializer extends AmfxMessageDeserializer {
    @Override
    /**
     * This prevents the SAX XML parser from expanding entities
     */
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        return new InputSource();
    }
}
