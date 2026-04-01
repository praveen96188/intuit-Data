package com.intuit.sbd.payroll.psp.gateways.email;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Set;

public class Handler implements SOAPHandler<SOAPMessageContext> {

// Initialize OtputStream (fos) etc. ...

    public boolean handleMessage(SOAPMessageContext c) {

        SOAPMessage msg = c.getMessage();

        boolean request = ((Boolean) c.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

        try {
            OutputStream fos = new FileOutputStream("d:\\dev\\psp\\rel-1.7\\log.xml");
            if (request) { // This is a request message.
                // Write the message to the output stream
                msg.writeTo(fos);
            } else { // This is the response message
                msg.writeTo(fos);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext c) {
        SOAPMessage msg = c.getMessage();
        try {
            //msg.writeTo(fos);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void close(MessageContext c) {

    }

    public Set getHeaders() {
        // Not required for logging
        return null;
    }
}
