package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSDTOManager;
import intuit.osp.common.wsf.base.WSGlobalParameter;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import javax.xml.soap.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * This is an Axis request flow handler to set the user context for a given web service call for purposes of keeping
 * an audit trail of any database work performed within the service invocation.
 * <p/>
 *
 */
public class WSFAuditHandler extends BasicHandler {

    private static final SpcfLogger logger = PayrollServices.getLogger(WSFAuditHandler.class);

    public WSFAuditHandler() {
        super();
    }

    public void invoke(MessageContext context) throws AxisFault {
        if (context.isClient()) {
            doClient(context);
        }
        else {
            doServer(context);
        }
    }

    public void doClient(MessageContext context) throws AxisFault {
        logger.debug("Entering WSF audit handler (client)...");
    }

    public void doServer(MessageContext context) throws AxisFault {
        logger.debug("Entering WSF audit handler (server)...");

        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.QBOEAdapter));

        try {
            SOAPMessage msg = context.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPElement wsfRequestElement = getWSFGlobalProperties(se.getBody());
            logger.debug("Request: "+se.toString());

            if (wsfRequestElement != null) {
                Properties props = new Properties();
                Name key = null;

                for (Iterator iter = wsfRequestElement.getAllAttributes(); iter.hasNext();) {
                    key = (Name) iter.next();
                    props.setProperty(key.getLocalName(), wsfRequestElement.getAttributeValue(key));
                }

                doPreProcessing(props);
            }
        }
        catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

        logger.debug("Exiting WSF audit handler (server)...");
    }

    private SOAPElement getWSFGlobalProperties(SOAPElement element) {
        if (element == null) {
            return null;
        }

        SOAPElement wsfElement = null;

        for (Iterator iter = element.getChildElements(); (wsfElement == null) && iter.hasNext();) {
            SOAPElement child = (SOAPElement) iter.next();

            logger.debug("(WSFAuditHandler) SOAP Element name: " + child.getNodeName());

            if (child.getNodeName().equalsIgnoreCase(WSDTOManager.WSF_MESSAGE_NODE_NAME)) {
                wsfElement = child;
            }
            else {
                wsfElement = getWSFGlobalProperties((SOAPElement)child.getFirstChild());
            }
        }

        return wsfElement;
    }

    private void doPreProcessing(Properties props) {
        Enumeration keys = props.propertyNames();
        String key = null;

        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();

            if (key.equals(WSGlobalParameter.WS_GLOBAL_PARAM_DB_USER)) {
                changeUserContext(props.getProperty(key));
            }
        }
    }

    private void changeUserContext(String dbUserName) {
        String uName = dbUserName.trim();
        logger.debug("(WSFAuditHandler) Setting user context, id = " + uName);
        AuthUser user = AuthUser.findUser(uName);
        if (user != null) {
            PayrollServices.setCurrentPrincipal(user.createPrincipal());
        } else {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.QBOEAdapter, uName));
        }
    }
}
