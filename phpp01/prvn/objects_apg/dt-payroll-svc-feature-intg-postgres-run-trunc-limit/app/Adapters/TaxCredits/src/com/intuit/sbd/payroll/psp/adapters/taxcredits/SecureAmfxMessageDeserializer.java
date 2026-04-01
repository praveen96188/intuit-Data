package com.intuit.sbd.payroll.psp.adapters.taxcredits;

import flex.messaging.io.amfx.AmfxMessageDeserializer;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.MessageException;
import org.xml.sax.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

/**
 * User: rnorian
 * Date: Feb 19, 2010
 * Time: 5:46:14 PM
 */
public class SecureAmfxMessageDeserializer extends AmfxMessageDeserializer {
    @Override
    /**
     * This prevents the SAX XML parser from expanding entities
     */
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        return new InputSource();
    }
}
