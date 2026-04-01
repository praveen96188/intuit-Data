package com.intuit.ems.payroll.psp.gateway.brm;

import com.intuit.iep.abcsimpl.commoncomponents.v1.EntitlementIdType;
import com.intuit.iep.abcsimpl.commoncomponents.v1.LicenseIdType;
import com.intuit.iep.intuitserviceusage.v1.ESBIntuitServiceUsageService;
import com.intuit.iep.intuitserviceusage.v1.IntuitServiceUsage;
import com.intuit.iep.serviceusage.intuitserviceusageabo.v1.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.developer.JAXWSProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/14/12
 * Time: 1:47 PM
 */
public class BRMGateway implements IBRMGateway {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(BRMGateway.class);

    private static final String WSDL_LOCATION = "resources/IntuitBRMUsage.wsdl";
    private static final String WSDL_NS = "http://www.intuit.com/iep/IntuitServiceUsage/V1";
    private static final String SERVICE_NAME = "ESB_IntuitServiceUsage_Service";
    private static final QName SERVICE_QNAME = new QName(WSDL_NS, SERVICE_NAME);
    private String mBRMURL = null;
    private String mLocalURL = null;
    private IntuitServiceUsage mPort = null;
    private Marshaller mMarshaller;
    private int mRequestTimeout;

    /**
     * Constructor
     *
     * @throws Throwable
     */
    public BRMGateway() throws Throwable {
        JAXBContext jaxbContext = JAXBContext.newInstance("com.intuit.iep.abcsimpl.commoncomponents.v1:com.intuit.iep.serviceusage.intuitserviceusageabo.v1");
        mMarshaller = jaxbContext.createMarshaller();
        mMarshaller.setProperty("jaxb.formatted.output", true);
        readConfigurationParameters();
        ESBIntuitServiceUsageService service;
        try {
            logger.info("executing BRM end-point to get symphony usage info");
            service = new ESBIntuitServiceUsageService(
                    new URL(mBRMURL),
                    SERVICE_QNAME);
        } catch (Throwable t) {
            String path;
            if (mLocalURL != null && mLocalURL.trim().length() > 0) {
                path = mLocalURL + "/" + WSDL_LOCATION;
            } else {
                path = Application.findFileOnClassPath(WSDL_LOCATION);
            }

            File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                logger.error(String.format("Cannot find or read WSDL file / Exists: %s, CanRead: %s, File: %s.", file.exists(), file.canRead(), path));
            }

            // try to construct the service from the local copy of the wsdl
            try {
                logger.info("constructing BRM service from local copy to get symphony usage info");
                service = new ESBIntuitServiceUsageService(new URL("file:///" + path), SERVICE_QNAME);
            } catch (Exception e) {
                logger.error("Unable to connect to the BRM web service. ", e);
                throw e;
            }
        }

        mPort = service.getSoapIntuitServiceUsageIntuitServiceUsage();

        ((BindingProvider) mPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mBRMURL);
        ((BindingProvider) mPort).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) mPort).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) mPort).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) mPort).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, mRequestTimeout);
    }

    /**
     * Wrapper for createServiceUsage  call
     * Sample Request:
     * <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
     * <SOAP-ENV:Body>
     * <v1:CreateServiceUsage xmlns:v1="http://www.intuit.com/iep/ServiceUsage/IntuitServiceUsageABO/V1">
     * <v1:TransactionInfo>
     * <v1:TransactionId>1111</v1:TransactionId>
     * <v1:SalesOrganization>EMS</v1:SalesOrganization>
     * </v1:TransactionInfo>
     * <v1:ServiceUsage>
     * <v1:LicenseId SchemeName="ERS">507089670193486</v1:LicenseId>
     * <v1:EntitlementId SchemeName="ERS">389857</v1:EntitlementId>
     * <v1:ActivityName>PayrollEE</v1:ActivityName>
     * <v1:QuantityUsed>1</v1:QuantityUsed>
     * </v1:ServiceUsage>
     * </v1:CreateServiceUsage>
     * </SOAP-ENV:Body>
     * </SOAP-ENV:Envelope>
     */
    public CreateServiceResponse createUsage(String pTransactionId,
                                             SpcfCalendar pTransactionDateTime,
                                             String pSalesOrganization,
                                             String pLicenceId,
                                             String pLicenceSchemaName,
                                             String pEntitlementId,
                                             String pEntitlementSchemeName,
                                             String pActivityName,
                                             BigInteger pQuantityUsed) throws Throwable {
        CreateServiceUsageType createServiceUsageRequest = new CreateServiceUsageType();

        CreateServiceUsageType.ServiceUsage su = new CreateServiceUsageType.ServiceUsage();
        su.setActivityName(pActivityName);
        EntitlementIdType et = new EntitlementIdType();
        et.setSchemeName(pEntitlementSchemeName);
        et.setValue(pEntitlementId);
        su.setEntitlementId(et);
        su.setQuantityUsed(pQuantityUsed);
        EntitlementIdType et3 = new EntitlementIdType();
        et3.setSchemeName(pLicenceSchemaName);
        et3.setValue(pLicenceId);
        su.setLicenseId(et3);

        createServiceUsageRequest.setServiceUsage(su);
        TransactionInfoType ti = new TransactionInfoType();
        ti.setSalesOrganization(pSalesOrganization);
        ti.setTransactionId(pTransactionId);
        ti.setTransactionDatetime(DatatypeFactory.newInstance().newXMLGregorianCalendar(CalendarUtils.convertCalendarToXmlStringNoMilliSeconds(pTransactionDateTime)));

        createServiceUsageRequest.setTransactionInfo(ti);
        CreateServiceUsageResponseType response = null;
        try {
            response = mPort.createUsage(createServiceUsageRequest);
            return handleCreateServiceResponse(response);

        } catch (com.intuit.iep.intuitserviceusage.v1.IntuitFaultMsg e) {
            throw e;
        }

    }

    /**
     * Wrapper for  queryUsageBalance  call
     * Sample Request:
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v1="http://www.intuit.com/iep/ServiceUsage/IntuitServiceUsageABO/V1">
     * <soapenv:Header/>
     * <soapenv:Body>
     * <v1:QueryUsageBalance>
     * <v1:TransactionInfo>
     * <v1:TransactionId>111</v1:TransactionId>
     * <!--Optional:-->
     * <v1:TransactionDatetime>2012-08-10T10:31:32Z</v1:TransactionDatetime>
     * <v1:SalesOrganization>EMS</v1:SalesOrganization>
     * </v1:TransactionInfo>
     * <v1:UsageBalance>
     * <v1:LicenseId SchemeName="ERS">507089670193486</v1:LicenseId>
     * <v1:EntitlementId SchemeName="ERS">389857</v1:EntitlementId>
     * <!--Optional:
     * <v1:ActivityName></v1:ActivityName> -->
     * </v1:UsageBalance>
     * </v1:QueryUsageBalance>
     * </soapenv:Body>
     * </soapenv:Envelope>
     */
    public QueryUsageBalanceResponse queryUsageBalance(String pTransactionId,
                                                       String pSalesOrganization,
                                                       String pLicenseId,
                                                       String pLicenceSchemaName,
                                                       String pEntitlementId,
                                                       String pEntitlementSchemeName,
                                                       String pActivityName,
                                                       XMLGregorianCalendar pTransactionDatetime
    ) throws Throwable {

        Holder<List<QueryUsageBalanceResponseType.Balance>> balance = new Holder<java.util.List<QueryUsageBalanceResponseType.Balance>>();
        Holder<ResponseTypeType> result = new Holder<ResponseTypeType>();
        TransactionInfoType transactionInfo = new TransactionInfoType();
        transactionInfo.setTransactionId(pTransactionId);
        transactionInfo.setSalesOrganization(pSalesOrganization);
        transactionInfo.setTransactionDatetime(pTransactionDatetime);
        QueryUsageBalanceType.UsageBalance usageBalance = new QueryUsageBalanceType.UsageBalance();
        LicenseIdType lt = new LicenseIdType();
        lt.setSchemeName(pLicenceSchemaName);
        lt.setValue(pLicenseId);
        usageBalance.setActivityName(pActivityName);
        usageBalance.setLicenseId(lt);
        EntitlementIdType et2 = new EntitlementIdType();
        et2.setSchemeName(pEntitlementSchemeName);
        et2.setValue(pEntitlementId);
        usageBalance.setEntitlementId(et2);
        try {
            mPort.queryUsageBalance(transactionInfo, usageBalance, balance, result);
            return handleQueryUsageBalanceResponse(balance, result);
        } catch (com.intuit.iep.intuitserviceusage.v1.IntuitFaultMsg e) {
            throw e;
        }
    }

    private void readConfigurationParameters() {
        boolean manageTransaction = !Application.hasActiveTransaction();

        mBRMURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_server_url");
        logger.info("getting BRM server url"+mBRMURL);
        mLocalURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_local_url");
        try {
            if (manageTransaction) {
                Application.beginUnitOfWork();
            }

            mRequestTimeout = SystemParameter.findIntValue(SystemParameter.Code.ERS_REQUEST_TIMEOUT, 10000);
        } finally {
            if (manageTransaction) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    /**
     * Sample Response
     * <env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
     * <env:Header/>
     * <env:Body xmlns="http://www.intuit.com/iep/ServiceUsage/IntuitServiceUsageABO/V1">
     * <CreateServiceUsageResponse xmlns:ns0="http://www.intuit.com/iep/ServiceUsage/IntuitServiceUsageABO/V1">
     * <ns0:Result>
     * <ns0:Success>Y</ns0:Success>
     * </ns0:Result>
     * </CreateServiceUsageResponse>
     * </env:Body>
     * </env:Envelope>
     *
     * @param pResponse
     */
    private CreateServiceResponse handleCreateServiceResponse(CreateServiceUsageResponseType pResponse) {
        if (pResponse == null || pResponse.getResult() == null) return null;
        return new CreateServiceResponse(pResponse.getResult());
    }

    /**
     * Sample Response
     * <env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v1="http://www.intuit.com/iep/ServiceUsage/IntuitServiceUsageABO/V1" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
     * <env:Header/>
     * <env:Body xmlns="http://www.intuit.com/iep/ServiceUsage/IntuitServiceUsageABO/V1">
     * <QueryUsageBalanceResponse xmlns:ns0="http://www.intuit.com/iep/ServiceUsage/IntuitServiceUsageABO/V1">
     * <ns0:Balance>
     * <ns0:ActivityName>Currency</ns0:ActivityName>
     * <ns0:Balance>4.86</ns0:Balance>
     * </ns0:Balance>
     * <ns0:Result>
     * <ns0:Success>Y</ns0:Success>
     * </ns0:Result>
     * </QueryUsageBalanceResponse>
     * </env:Body>
     * </env:Envelope>
     *
     * @param pBalance
     * @param pResult
     */
    private QueryUsageBalanceResponse handleQueryUsageBalanceResponse(Holder<List<QueryUsageBalanceResponseType.Balance>> pBalance,
                                                                      Holder<ResponseTypeType> pResult) {
        if (pBalance == null && pResult == null) return null;
        ResponseTypeType responseType = null;
        List<QueryUsageBalanceResponseType.Balance> balanceList = null;
        if (pResult != null) responseType = pResult.value;
        if (pBalance != null) balanceList = pBalance.value;
        return new QueryUsageBalanceResponse(responseType, balanceList);
    }

    public CreateServiceResponse createUsage(String pLicenseId,
                                             String pEntitlementId,
                                             SpcfCalendar pTransactionDateTime,
                                             int pQuantityUsed) throws Exception {
        String transactionId = UUID.randomUUID().toString();
        try {
            logger.info("creating usage balance api call");
            CreateServiceResponse response = createUsage(transactionId, pTransactionDateTime, "EMS", pLicenseId, "ERS", pEntitlementId, "ERS", "PayrollEE", BigInteger.valueOf(pQuantityUsed));
            if (!"Y".equals(response.getSuccess())) {
                throw new Exception("failed to send usage data to BRM. TransactionId: " + transactionId + " LicenceId: " + pLicenseId + " EntitlementId: " + pEntitlementId
                                            + " errorCode: " + response.getErrorCode() + " errorDescription: " + response.getErrorDescription());
            }
            return response;
        } catch (Throwable e) {
            throw new Exception("failed to send usage data to BRM. TransactionId: " + transactionId + " LicenceId: " + pLicenseId + " EntitlementId: " + pEntitlementId, e);
        }
    }

    public QueryUsageBalanceResponse queryUsageBalance(String pLicenseId,
                                                       String pEntitlementId) throws Exception {
        String transactionId = UUID.randomUUID().toString();
        try {
            logger.info("QueryUsageBalance api call");
            QueryUsageBalanceResponse response = queryUsageBalance(transactionId, "EMS", pLicenseId, "ERS", pEntitlementId, "ERS", null, null);
            return response;
        } catch (Throwable e) {
            throw new Exception("failed to query usage data from BRM. TransactionId: " + transactionId + " LicenceId: " + pLicenseId + " EntitlementId: " + pEntitlementId, e);
        }
    }
}
