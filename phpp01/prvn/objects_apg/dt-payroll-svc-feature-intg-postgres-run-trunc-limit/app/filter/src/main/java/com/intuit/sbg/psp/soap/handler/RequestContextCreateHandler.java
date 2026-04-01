package com.intuit.sbg.psp.soap.handler;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.helper.BaseCompanyContextHelper;
import com.intuit.sbd.payroll.psp.context.helper.CompanyContextHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.soap.utils.SoapMessageContentExtractor;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class RequestContextCreateHandler implements SOAPHandler<SOAPMessageContext> {


    private static List<String> SOURCE_COMPANY_ID= Arrays.asList("PSID", "psid", "SourceCompanyId", "sourceCompanyId", "CompanyId", "CompanyID", "UserID");

    private static List<String> EIN= Arrays.asList("EIN", "ein");

    private static List<String> REALM_ID= Arrays.asList("RealmId");

    private CompanyContextHelper companyContextHelper;
    private PSPRequestContextManager pspRequestContextManager;

    /*
     * Setting Company Information in some workflows where Company Information is not required and can cause unwanted effects
     * Ex - Create_Account - Here a new PSID will be generated and any existing company information (if present) is not relevant
     * And there are some other workflows where we don't have company information, so excluding those also to save time.
     * */
    private static List<String> EXCLUDED_OPERATIONS = Arrays.asList("Create_Account", "Authenticate",
            "GetAgencyRules", "Query_Industry_Types", "Query_Account",
            "Deactivate_Entitlement", "Validate_Subscription");

    public RequestContextCreateHandler() {
        companyContextHelper = PayrollApplicationBeanFactory.getBean(CompanyContextHelper.class);
        pspRequestContextManager =  PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if  ((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            handleResponse();
            return true;
        }
        handleRequest(context);
        return true;
    }

    private void handleRequest(SOAPMessageContext context) {
        String operation = null;
        try {
            pspRequestContextManager.clearRequestContext();
            Company company;
            log.info("Event=SetRequestContext Type=SOAP Status=Started");
            SOAPMessage soapMessage = context.getMessage();
            SOAPBody soapBody = soapMessage.getSOAPBody();
            operation = getOperation(soapBody);
            if(EXCLUDED_OPERATIONS.contains(operation)) {
                log.info("Event=SetRequestContext Type=SOAP Status=NotRequired Operation={}", operation);
                pspRequestContextManager.setRequestContext(null, RequestType.SOAP, operation);
                return;
            }
            company = getCompany(soapBody, operation);
            if(operation.equals("QueryPayrollStatus"))
                company = getCompanyforQueryPayrollStatus(soapBody);

            pspRequestContextManager.setRequestContext(company, RequestType.SOAP, operation);
       } catch (Exception e) {
            log.error("Event=SetRequestContext Type=SOAP Status=Error Operation={}", operation, e);
        }
    }

    private void handleResponse() {
        try {
            pspRequestContextManager.clearRequestContext();
        } catch (Exception e) {
            log.error("Event=ClearRequestContext Type=SOAP Status=Error", e);
        }
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {

    }

    private Company getCompany(SOAPBody soapBody, String operation) {
        Company company = null;
        try {
            Application.beginUnitOfWork();
            String sourceCompanyId = retrieveCompanyIdentifierFromRequestBody(soapBody, SOURCE_COMPANY_ID);
            if(sourceCompanyId != null) {
                return companyContextHelper.getCompanyByPSID(sourceCompanyId);
            }

            /*
            In case multiple companies with same EIN/RealmId are found, we are not setting the Request Context because of ambiguity issues.
            * */

            String ein = retrieveCompanyIdentifierFromRequestBody(soapBody, EIN);
            if(ein != null) {
                return companyContextHelper.getCompanyByEIN(ein);
            }

            String realmId = retrieveCompanyIdentifierFromRequestBody(soapBody, REALM_ID);
            if(realmId != null) {
                return companyContextHelper.getCompanyByRealmId(realmId);
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
        return company;
    }

    private String retrieveCompanyIdentifierFromRequestBody(SOAPBody soapBody, List<String> patterns) {
        String value = null;
        for(String pattern : patterns) {
            value = SoapMessageContentExtractor.extractContentIfOnlySingleElementPresent(soapBody, pattern);
            if(value != null)
                return value;
        }
        return value;
    }

    private String getOperation(SOAPBody body){
        String operation = null;

        for (Iterator<Node> iterator = body.getChildElements(); iterator.hasNext(); ) {
            Object child = iterator.next();
            if (child instanceof SOAPElement) {
                operation = ((SOAPElement) child).getLocalName();
            }
        }
        return operation;
    }

    private Company getCompanyforQueryPayrollStatus(SOAPBody soapBody) {
        Company company = null;
        String sourceCompanyId = SoapMessageContentExtractor.extractAttributeForElement(soapBody, "GetPayrollStatus", "UserID");
        if(sourceCompanyId != null) {
            company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        }
        return company;
    }
}
