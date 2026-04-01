package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.handlers;

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

/**
 * @author Jeff Jones
 */
public class SchemaValidationHandler implements LogicalHandler<LogicalMessageContext> {

    private static SpcfLogger logger = null;
     private static Schema schema = null;
     private static final String XSD_FILE = "EWSAdapterService_schema9.xsd";

     static {
         try {
             logger = PayrollServices.getLogger(SchemaValidationHandler.class);
         } catch (Exception e) {
             System.out.println("Error creating SpcfLogger: " + e.getMessage());
             throw new WebServiceException(e);
         }
     }

     /**
      *
      * @param pContext
      * @return boolean
      */
     public boolean handleMessage(LogicalMessageContext pContext) {
         if  ((Boolean) pContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
             return true;
         }

         LogicalMessage logicalMessage = pContext.getMessage();
         Source payload = logicalMessage.getPayload();

         try {
             PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EWSAdapter));

             if (schema == null) {
                 schema = getSchemaFromURI(XSD_FILE);
             }
             Validator validator = schema.newValidator();
             validator.validate(payload);
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

     private Schema getSchemaFromURI(String pURI) throws MalformedURLException, SAXException {
         SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         return schemaFactory.newSchema(new File(Application.findFileOnClassPath(pURI)));
     }


}
