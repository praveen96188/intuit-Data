package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.PSPDate;
import flex.messaging.endpoints.HTTPEndpoint;
import flex.messaging.messages.Message;

/**
 * Writes the PSPDate into all response message headers to allow the client to
 * auto-synchronize its time with the server.
 */
public class SAPHTTPEndpoint extends HTTPEndpoint {
    public static final String PSP_TIME_HEADER = "PSPTime";

    public Message serviceMessage(Message message) {
        Message ack = super.serviceMessage(message);
        String pspTime = Long.toString(PSPDate.getPSPTime().getTimeInMilliseconds());
        ack.setHeader(PSP_TIME_HEADER, pspTime);
        return ack;
    }
}
