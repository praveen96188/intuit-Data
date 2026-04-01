package com.intuit.sbg.psp.soap.handler;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbg.payroll.authorization.expression.PayrollAuthentication;
import com.intuit.sbg.payroll.authorization.soap.handler.AbstractPayrollSOAPSecurityHandler;
import com.intuit.sbg.payroll.authorization.utils.AuthenticationConstants;
import com.intuit.sbg.psp.soap.utils.SoapMessageContentExtractor;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Objects;

public class PayloadExtractingHandler extends AbstractPayrollSOAPSecurityHandler {

    private static Logger logger = LoggerFactory.getLogger(PayloadExtractingHandler.class);

    private static String SOURCE_COMPANY_ID= "PSID";

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if  ((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            logger.info("Payload Extraction MESSAGE OUTBOUND completed");
            return true;
        }
        logger.info("Payload Extraction of the request started");
        SOAPMessage soapMessage = context.getMessage();
        try {
            SOAPBody soapBody = soapMessage.getSOAPBody();
            String sourceCompanyId = getSourceCompanyId(soapBody);
            logger.info("Successfully extracted the PSID={} from Soap Body", sourceCompanyId);
            setAuthentication(sourceCompanyId);
        } catch (SOAPException e) {
            logger.error("Error while extracting content from Payload", e);
        }
        logger.info("Payload Extraction of the request completed");
        return true;
    }

    private void setAuthentication(String sourceCompanyId) {
        if(Objects.isNull(sourceCompanyId)) {
            return;
        }

        String realmId = findRealmBySourceCompanyId(sourceCompanyId);

        setPayrollAuthentication(realmId);
    }

    private void setPayrollAuthentication(String realmId) {
        if(Objects.isNull(realmId)) {
            return;
        }

        logger.info("Setting the Payroll Authentication for RealmId={}", realmId);

        PayrollAuthentication payrollAuthentication = getPayrollAuthentication();

        if(Objects.isNull(payrollAuthentication)) {
            return;
        }

        logger.info("Successfully got the Payroll Authentication from Request Context");

        payrollAuthentication.setRealmId(realmId);
    }

    private PayrollAuthentication getPayrollAuthentication() {
        return RequestAttributesUtils.getAttribute(AuthenticationConstants.PAYROLL_AUTHENTICATION_CONTEXT, PayrollAuthentication.class);
    }

    private String getSourceCompanyId(SOAPBody soapBody) {
        return SoapMessageContentExtractor.extractContent(soapBody, SOURCE_COMPANY_ID);
    }

    private String findRealmBySourceCompanyId(String sourceCompanyId) {
        Application.beginUnitOfWork();
        String realmId = null;
        try {
            Company company =  Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
            if(Objects.nonNull(company)) {
                realmId = company.getIAMRealmId();
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
        return realmId;
    }

}
