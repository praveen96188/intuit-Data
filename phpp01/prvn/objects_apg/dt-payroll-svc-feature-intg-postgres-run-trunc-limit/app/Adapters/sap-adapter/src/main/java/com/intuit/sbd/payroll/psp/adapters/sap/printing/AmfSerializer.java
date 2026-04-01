package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.Amf3Output;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author ronen
 */
public class AmfSerializer {

    public AmfSerializer() {
        SerializationContext serializationContext = SerializationContext.getSerializationContext();// Threadlocal SerializationContent
        //serializationContext.enableSmallMessages = true;
        serializationContext.instantiateTypes = true;
        serializationContext.supportRemoteClass = true;// use _remoteClass field
        serializationContext.legacyCollection = false;// false  Legacy Flex 1.5 behavior was to return a java.util.Collection for Array, New Flex 2+ behavior is to return Object[] for AS3 Array
        serializationContext.legacyMap = false;// false Legacy flash.xml.XMLDocument Type
        serializationContext.legacyXMLDocument = false;// true New E4X XML Type
        //serializationContext.legacyXMLNamespaces = false;// determines whether the constructed Document is name-space aware
        serializationContext.legacyThrowable = false;
        serializationContext.legacyBigNumbers = false;
        serializationContext.restoreReferences = false;
        serializationContext.logPropertyErrors = false;
        serializationContext.ignorePropertyErrors = true;
        context = serializationContext;
    }

    private SerializationContext context;

    public SerializationContext getContext() {
        return context;
    }

    public void setContext(SerializationContext context) {
        this.context = context;
    }

    public <T> String toAmf(final T source) throws IOException {
        final StringBuffer buffer = new StringBuffer();
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final Amf3Output amf3Output = new Amf3Output(context);
        amf3Output.setOutputStream(bout);
        amf3Output.writeObject(source);
        amf3Output.flush();
        amf3Output.close();
        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bout.toByteArray());
    }

    public <T> T fromAmf(final String amf) throws ClassNotFoundException, IOException {
        final BASE64Decoder decoder = new BASE64Decoder();
        byte[] input = decoder.decodeBuffer(amf);
        InputStream bIn = new ByteArrayInputStream(input);
        Amf3Input amf3Input = new Amf3Input(context);
        amf3Input.setInputStream(bIn);
        return (T) amf3Input.readObject();
    }
}