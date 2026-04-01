package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.PSPDate;
import flex.messaging.endpoints.SecureHTTPEndpoint;
import flex.messaging.messages.Message;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Writes the PSPDate into all response message headers to allow the client to
 * auto-synchronize its time with the server.
 * @see SecureAmfxMessageDeserializer
 */
public class SAPSecureHTTPEndpoint extends SecureHTTPEndpoint {

    public SAPSecureHTTPEndpoint() {
        this(false);
    }

    public SAPSecureHTTPEndpoint(boolean enableManagement) {
        super(enableManagement);
        this.deserializerClass = SecureAmfxMessageDeserializer.class;
    }

    public Message serviceMessage(Message message) {
        Message ack = super.serviceMessage(message);
        String pspTime = Long.toString(PSPDate.getPSPTime().getTimeInMilliseconds());
        ack.setHeader(SAPHTTPEndpoint.PSP_TIME_HEADER, pspTime);
        return ack;
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) {
        res.addHeader("Cache-Control", "no-cache,no-store");
        res.addHeader("Pragma", "no-cache");
        res.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        super.service(req, res);
    }
}
