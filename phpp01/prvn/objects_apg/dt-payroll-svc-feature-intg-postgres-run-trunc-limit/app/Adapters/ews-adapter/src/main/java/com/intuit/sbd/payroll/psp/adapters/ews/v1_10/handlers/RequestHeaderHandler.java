package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.handlers;

import com.intuit.sbg.payroll.authorization.soap.handler.PayrollSOAPAuthenticationHandler;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Enumeration;
import java.util.Set;

/**
 * RequestHeaderHandler
 *
 * It will store all the headers as attributes for all calls coming to EWS Adapter
 */

public class RequestHeaderHandler implements SOAPHandler<SOAPMessageContext> {
    private static Logger logger = LoggerFactory.getLogger(RequestHeaderHandler.class);

    public static final String HANDLER_MESSAGE_OUTBOUND = "javax.xml.ws.handler.message.outbound";
    public static final String SERVLET_REQUEST = "javax.xml.ws.servlet.request";

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        //Outbound Part - After processing the request
        if ((Boolean)context.get(HANDLER_MESSAGE_OUTBOUND)) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) context.get(SERVLET_REQUEST);
            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            while(headerNames.hasMoreElements()){
                String headerName = headerNames.nextElement();
                RequestAttributesUtils.removeAttribute(headerName);
            }
        }
        //Before processing the request
        else {
            HttpServletRequest httpServletRequest = (HttpServletRequest) context.get(SERVLET_REQUEST);

            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            while(headerNames.hasMoreElements()){
                String headerName = headerNames.nextElement();
                RequestAttributesUtils.setAttribute(headerName, httpServletRequest.getHeader(headerName));
            }
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }
}