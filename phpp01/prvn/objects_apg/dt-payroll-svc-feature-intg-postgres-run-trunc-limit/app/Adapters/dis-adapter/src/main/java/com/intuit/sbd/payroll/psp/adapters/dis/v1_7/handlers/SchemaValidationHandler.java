package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.handlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/handlers/SchemaValidationHandler.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * This class implemnts JAX-WS's LogicalHandler.  We are using it to setup QBDT so we can utilize it.
 */
public class SchemaValidationHandler implements LogicalHandler<LogicalMessageContext> {
    private static SpcfLogger logger = null;
    private static Map<String,Schema> schemaMap = new HashMap<String, Schema>(2);
    private static final String XSD_FILE = "DISAdapterService_schema7.xsd";

    static {
        try {
            logger = PayrollServices.getLogger(SchemaValidationHandler.class);
        } catch (Exception e) {
            System.out.println("Error creating SpcfLogger: " + e.getMessage());
            throw new WebServiceException(e);
        }
    }

    /***
     * Handle the logic for each message call.  We need to set the principal in QBDT in order to
     *     start accessing it.
     * @param pContext
     * @return
     */
    public boolean handleMessage(LogicalMessageContext pContext) {
        if  ((Boolean) pContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            return true;
        }

        LogicalMessage logicalMessage = pContext.getMessage();
        Source payload = logicalMessage.getPayload();

        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.DISAdapter));

            if (!schemaMap.containsKey(XSD_FILE)) {
                synchronized (schemaMap) {
                    try {
                        schemaMap.put(XSD_FILE, getSchemaFromURI(XSD_FILE));
                    } catch (Exception e) {
                        //if we can't get the schema, log the error, but continue.
                        logger.error(e);
                    }

                }
            }

            Schema schema = schemaMap.get(XSD_FILE);
            if (schema != null) {
                Validator validator = schema.newValidator();
                validator.validate(payload);
            }
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        return true;
    }

    /**
     *
     * @param pContext
     * @return boolean
     */
    public boolean handleFault(LogicalMessageContext pContext) {
        return true;
    }

    /**
     *
     * @param messageContext
     */
    public void close(MessageContext messageContext) {
    }

    /**
     * Get the schema from the supplied URI.
     *
     * @param pURI
     * @return
     * @throws java.net.MalformedURLException
     * @throws org.xml.sax.SAXException
     */
    private Schema getSchemaFromURI(String pURI) throws MalformedURLException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return schemaFactory.newSchema(new File(Application.findFileOnClassPath(pURI)));
    }

}
