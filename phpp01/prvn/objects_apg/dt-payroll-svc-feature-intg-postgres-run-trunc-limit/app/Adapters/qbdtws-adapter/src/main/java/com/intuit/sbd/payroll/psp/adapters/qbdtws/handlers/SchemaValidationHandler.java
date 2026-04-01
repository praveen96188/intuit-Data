package com.intuit.sbd.payroll.psp.adapters.qbdtws.handlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.developer.JAXWSProperties;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 24, 2009
 * Time: 1:11:03 PM
 */
public class SchemaValidationHandler implements LogicalHandler<LogicalMessageContext> {
    private static SpcfLogger logger = PayrollServices.getLogger(SchemaValidationHandler.class);
    private static Map<String,Schema> schemaMap = new HashMap<String, Schema>(2);

    public boolean handleMessage(LogicalMessageContext pContext) {
        if  ((Boolean) pContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            return true;
        }

        LogicalMessage logicalMessage = pContext.getMessage();
        Source payload = logicalMessage.getPayload();

        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.QBDTWSAdapter));

            String schemaUri = ((WSEndpoint) pContext.get(JAXWSProperties.WSENDPOINT)).getServiceName().getLocalPart() + "_schema1.xsd";
            if (!schemaMap.containsKey(schemaUri)) {
                synchronized (schemaMap) {
                    try {
                        schemaMap.put(schemaUri, getSchemaFromURI(schemaUri));
                    } catch (Exception e) {
                        //if we can't get the schema, log the error, but continue.
                        logger.error(e);
                    }

                }
            }

            Schema schema = schemaMap.get(schemaUri);
            if (schema != null) {
                Validator validator = schema.newValidator();
                validator.validate(payload);
            }
        } catch (Exception e) {
            StringWriter buffer = new StringWriter();
            try {
                PrintWriter out = new PrintWriter(buffer);
                StreamResult streamResult = new StreamResult(out);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(payload, streamResult);
            } catch (TransformerException e1) { }
            String request = buffer.toString();
            // remove pin from the log
            request = request.replaceAll("<PIN>[^<>]*</PIN", "<PIN>****</PIN>");
            logger.error(e.getMessage() + "\n" + request, e);
            throw new WebServiceException(e.getMessage());
        }

        return true;
    }

    public boolean handleFault(LogicalMessageContext pContext) {
        return true;
    }

    public void close(MessageContext messageContext) {
    }

    private Schema getSchemaFromURI(String pURI) throws MalformedURLException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return schemaFactory.newSchema(new File(Application.findFileOnClassPath(pURI)));
    }

}
