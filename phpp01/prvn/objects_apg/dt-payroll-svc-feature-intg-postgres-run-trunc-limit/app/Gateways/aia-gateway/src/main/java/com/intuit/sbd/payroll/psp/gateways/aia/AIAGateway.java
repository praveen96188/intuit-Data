package com.intuit.sbd.payroll.psp.gateways.aia;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.utils.threads.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.developer.JAXWSProperties;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.rmi.ServerException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader.getHeader;
/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * modifier: vishalb849
 * Date: 11/14/18
 * Time: 1:47 PM
 */
public class AIAGateway implements IAIAGateway {
    public static final String MONTHLY_SUBSCRIPTION_ITEM_NAME = "1100520";
    public static final String ANNUAL_SUBSCRIPTION_ITEM_NAME = "1100521";
    public static final String PAYROLL_EE_ITEM_NAME = "PayrollEE";

    private static final SpcfLogger logger = SpcfLogManager.getLogger(AIAGateway.class);
    private static final String WSDL_NS = "http://xmlns.intuit.net/ABCS/Common/Industry/Communications/QueryBillingTransactionsIntuitReqABCS/V1";
    private static final String SERVICE_NAME = "ESB_QueryBillingTransactionsIntuitEBS_Service";
    private static final QName SERVICE_QNAME = new QName(WSDL_NS, SERVICE_NAME);
    private static final String AIA_CURRENCY_CODE = "USD";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private String mAIAURLAWS = null;
    private String mLocalURL = null;
    private String mAIABPAWSURL = null;
    private int mRequestTimeout;
    private int mMaxNumberOfRecords = 50;
    private QueryBillingTransactionsIntuitReqABCS mPort = null;
    private static ThreadLocal<ESBQueryBillingTransactionsIntuitEBSService> mQueryBillingTransactionsServiceThreadLocal = new ThreadLocal<ESBQueryBillingTransactionsIntuitEBSService>();
    private static final String WSDL_LOCATION = "resources/QueryBillingTransactionsIntuitEBS.wsdl";
    ESBQueryBillingTransactionsIntuitEBSService service = null;
    /**
     * Constructor
     *
     * @throws Throwable
     */
    public AIAGateway() throws Throwable {
        readConfigurationParameters();
        try{
            logger.info("calling to the local copy of wsdl");
            service = getService();
        } catch(Exception e){
            throw e;
        }
        //ESBQueryBillingTransactionsIntuitEBSService service = new ESBQueryBillingTransactionsIntuitEBSService(new URL(URL), SERVICE_QNAME);
        mPort = service.getSoapQueryBillingTransactionsIntuitEBSQueryBillingTransactionsIntuitReqABCS();
        ((BindingProvider) mPort).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) mPort).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) mPort).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) mPort).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, mRequestTimeout);

        // Setting SoapAction manually. Because after upgrading JAX-WS from 2.1.7 to 2.2.7. With default SoapAction URI it fails as it is appended with Request string.
        // For each operation we need to set SoapAction URI before making call go through successfully
        ((BindingProvider) mPort).getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        logger.info("Hitting with endpoint behind gateway with PA+");
        //Passing url for authenticated POST call
        mAIAURLAWS = mAIAURLAWS + "?wsdl";
        ((BindingProvider) mPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mAIAURLAWS);
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDENTITY2_ENABLED_FOR_AIA,true)) {
            String intuitTid = OfflineTicketHeader.generateTid();
            logger.info("AuthN: Identity 2 - executing with endpoint behind AIA Gateway  intuit_tid="+ intuitTid);
            ((BindingProvider) mPort).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, OfflineTicketHeader.getAuthNHeader(MediaType.APPLICATION_XML, intuitTid));
        }else {
            logger.info("AuthN: Identity 1 - executing with endpoint behind AIA Gateway");
            ((BindingProvider) mPort).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, new OfflineTicketHeader().getHeader(ConfigType.PSP, MediaType.APPLICATION_XML));
        }
        }
    //method to get the service locally
    private ESBQueryBillingTransactionsIntuitEBSService getService() throws MalformedURLException{
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
            logger.info("constructing AIA service from local copy ");
            service = new ESBQueryBillingTransactionsIntuitEBSService(new URL("file:///" + path), SERVICE_QNAME);
        } catch (Exception e) {
            logger.error("Unable to connect to the AIA web service. ", e);
            throw e;
        }
        return service;
    }

    /**
     * Call to get the invoice list from BRM
     *
     * @param pCustomerId       CAN
     * @param pBillingProfileId Customer's BillingProfileId
     * @return List of BillInfo objects from the response
     */
    public List<BillInfo> queryInvoiceList(String pCustomerId, String pBillingProfileId) throws IntuitFaultMsg {
        return queryInvoiceList(pCustomerId, null, mMaxNumberOfRecords, null, pBillingProfileId, pCustomerId, null, null, null);

    }

    public List<BillInfo> queryInvoiceList(String pCustomerId, String pBillingProfileId, Date fromDate, Date toDate) throws IntuitFaultMsg{
        return queryInvoiceList(pCustomerId, null, mMaxNumberOfRecords, null, pBillingProfileId, pCustomerId, null,fromDate, toDate);
    }

    /**
     * Get invoice details for each BillPOID
     *
     * @param pCustomerId       CAN
     * @param pBillingProfileId Csutomer Billing Profile Id
     * @param pBillPOID         BillPOID from AIA response
     * @return List of ItemCharge objects
     */
    public List<ItemCharge> queryInvoiceDetails(String pCustomerId, String pBillingProfileId, String pBillPOID) throws IntuitFaultMsg {
        return queryInvoiceDetails(mPort, pCustomerId, null, mMaxNumberOfRecords, null, pBillingProfileId, pCustomerId, pBillPOID);
    }

    /**
     * Get invoice details for each BillPOID
     *
     * @param pCustomerId       CAN
     * @param pBillingProfileId Csutomer Billing Profile Id
     * @param pBillPOID         BillPOID from AIA response
     * @return List of ItemCharge objects
     */
    public List<ItemCharge> queryInvoiceDetails(QueryBillingTransactionsIntuitReqABCS pPort, String pCustomerId, String pBillingProfileId, String pBillPOID) throws IntuitFaultMsg {
        return queryInvoiceDetails(pPort, pCustomerId, null, mMaxNumberOfRecords, null, pBillingProfileId, pCustomerId, pBillPOID);
    }

    /**
     * Get duration for each itemChargeId
     *
     * @param pCustomerId       Customer CAN
     * @param pBillingProfileId BillingProfileId
     * @param pBillPOID         POID from the response
     * @param pItemChargeId     ItemChargeId from the AIA response
     * @return Durtaion aka Usage Count
     */

    public int queryEventDetails(String pCustomerId, String pBillingProfileId, String pBillPOID, String pItemChargeId) throws IntuitFaultMsg {
        return queryEventDetails(mPort, pCustomerId, null, mMaxNumberOfRecords, null, pBillingProfileId, pCustomerId, pBillPOID, pItemChargeId);
    }

    /**
     * Get duration for each itemChargeId
     *
     * @param pCustomerId       Customer CAN
     * @param pBillingProfileId BillingProfileId
     * @param pBillPOID         POID from the response
     * @param pItemChargeId     ItemChargeId from the AIA response
     * @return Durtaion aka Usage Count
     */

    public int queryEventDetails(QueryBillingTransactionsIntuitReqABCS pPort, String pCustomerId, String pBillingProfileId, String pBillPOID, String pItemChargeId) throws IntuitFaultMsg {
        return queryEventDetails(pPort, pCustomerId, null, mMaxNumberOfRecords, null, pBillingProfileId, pCustomerId, pBillPOID, pItemChargeId);
    }

    /**
     * Get Duration (or) Usage Counts for the Item Charges
     *
     * @param pCustomerId       CAN
     * @param pBillingProfileId BillingProfileId for this customer
     * @param pBillPOID         BillPOID from the AIA response
     * @param pBillDate         BillDate from the response
     * @return List of ItemCharge objects
     */
    private List<ItemCharge> processBillPOID(String pAiaUrl, String pCustomerId, String pBillingProfileId, String pBillPOID, SpcfCalendar pBillDate) throws Exception {
        if (mQueryBillingTransactionsServiceThreadLocal.get() == null) {
            mQueryBillingTransactionsServiceThreadLocal.set(createQueryBillingTransactionService(pAiaUrl));
        }
        QueryBillingTransactionsIntuitReqABCS port = mQueryBillingTransactionsServiceThreadLocal.get().getSoapQueryBillingTransactionsIntuitEBSQueryBillingTransactionsIntuitReqABCS();
        ((BindingProvider) port).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) port).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, mRequestTimeout);

        // Setting SoapAction manually. Because after upgrading JAX-WS from 2.1.7 to 2.2.7. With default SoapAction URI it fails as it is appended with Request string.
        // For each operation we need to set SoapAction URI before making call go through successfully
        ((BindingProvider) mPort).getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);

        List<ItemCharge> itemCharges = queryInvoiceDetails(port, pCustomerId, pBillingProfileId, pBillPOID);
        if (itemCharges != null) {
            for (ItemCharge itemCharge : itemCharges) {
                if (itemCharge != null) {
                    String itemChargeId = itemCharge.getItemChargeId();
                    int usageCount = queryEventDetails(port, pCustomerId, pBillingProfileId, pBillPOID, itemChargeId);
                    itemCharge.setUsageCount(usageCount);
                    itemCharge.setBillDate(pBillDate);
                }
            }
        }
        return itemCharges;
    }

    /**
     * Wrapper for the SOAP call for queryInvoiceList
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:com="http://www.intuit.net/xml/CommonBillingProfileABO">
     * <soapenv:Header/>
     * <soapenv:Body>
     * <com:QueryMultipleBillingInfoRequest Language="?" Locale="?" MessageId="?" EnterpriseServerName="?">
     * <!--Zero or more repetitions:-->
     * <com:BillToPartyList>
     * <com:AccountId>784457309</com:AccountId>
     * <!--Optional:-->
     * <com:AccountName>Smoke Test 0926</com:AccountName>
     * <!--Optional:-->
     * <com:MaximumNumberOfRecords>100</com:MaximumNumberOfRecords>
     * <com:CurrencyCode>100</com:CurrencyCode>
     * <com:BillingProfileName>American Express_4343_26_TRN-565KY8N</com:BillingProfileName>
     * <com:BillingProfileId>TRN-4YJ64BD</com:BillingProfileId>
     * <com:ServiceAccountId>784457309</com:ServiceAccountId>
     * <com:BillPOID></com:BillPOID>
     * </com:BillToPartyList>
     * </com:QueryMultipleBillingInfoRequest>
     * </soapenv:Body>
     * </soapenv:Envelope>
     */
    private List<BillInfo> queryInvoiceList(String pAccountId,  //CAN customer id in entitlement
                                            String pAccountName,
                                            int pMaxNumberOfRecords, //50
                                            String pBillingProfileName,
                                            String pBillingProfileId,
                                            String pServiceAccountId, //CAN
                                            String pBillPOID,
                                            Date fromDate,
                                            Date toDate) throws IntuitFaultMsg {

        // Setting SoapAction URI manually.
        ((BindingProvider) mPort).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "QueryInvoiceList");

        QueryMutipleBillsType req = new QueryMutipleBillsType();
        List<BillToPartyType> billToPartyList = new ArrayList<BillToPartyType>();
        String lang = "?";
        String enterpriseServerName = "?";
        String locale = "?";
        req.setLanguage(lang);
        req.setEnterpriseServerName(enterpriseServerName);
        req.setLocale(locale);

        BillToPartyType billToPartyType = new BillToPartyType();
        billToPartyType.setAccountId(pAccountId);
        billToPartyType.setAccountName(pAccountName);
        billToPartyType.setMaximumNumberOfRecords(String.valueOf(pMaxNumberOfRecords));
        billToPartyType.setCurrencyCode(AIA_CURRENCY_CODE);
        billToPartyType.setBillingProfileName(pBillingProfileName);
        billToPartyType.setBillingProfileId(pBillingProfileId);
        billToPartyType.setServiceAccountId(pServiceAccountId);
        billToPartyType.setBillPOID(pBillPOID);
        billToPartyList.add(billToPartyType);
        req.billToPartyList = billToPartyList;
        QueryInvoiceProfileResponseType response = mPort.queryInvoiceList(req);
        return filterBillPOIDsForLastYear(response, fromDate, toDate);
    }

    /**
     * Wrapper for the SOAP call for queryBillingProfile
     */
    public String queryBillingProfile(String canNum) throws Throwable{

        String billingProfileID = null;
        try {
            logger.info("Preparing AIA call to get BP for CAN=" + canNum);

            // HardFixing SOAP request(xml)
            String prefix = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v2=\"http://xmlns.oracle.com/EnterpriseObjects/Core/EBO/CustomerParty/V2\" xmlns:v21=\"http://xmlns.oracle.com/EnterpriseObjects/Core/Common/V2\" xmlns:add=\"http://schemas.xmlsoap.org/ws/2003/03/addressing\" xmlns:urn=\"urn:oasis:names:tc:xacml:2.0:context:schema:cd:04\">\n" +
                    "   <soapenv:Header/>\n" +
                    "   <soapenv:Body>\n" +
                    "      <QueryCustomerPartyListEBM xmlns:ns1=\"http://xmlns.oracle.com/EnterpriseObjects/Core/EBO/CustomerParty/V2\" xmlns=\"http://xmlns.oracle.com/EnterpriseObjects/Core/EBO/CustomerParty/V2\">\n" +
                    "         <corecom:EBMHeader xmlns:corecom=\"http://xmlns.oracle.com/EnterpriseObjects/Core/Common/V2\">\n" +
                    "            <corecom:Sender>\n" +
                    "               <corecom:ID>Quote</corecom:ID>\n" +
                    "            </corecom:Sender>\n" +
                    "            <corecom:BusinessScope>\n" +
                    "               <corecom:ID>";

            String middle = "</corecom:ID>\n" +
                    "            </corecom:BusinessScope>\n" +
                    "         </corecom:EBMHeader>\n" +
                    "         <ns1:DataArea>\n" +
                    "            <ns1:Query>\n" +
                    "               <corecom:QueryCriteria xmlns:corecom=\"http://xmlns.oracle.com/EnterpriseObjects/Core/Common/V2\">\n" +
                    "                  <corecom:QueryExpression>\n" +
                    "                     <corecom:ValueExpression queryOperatorCode=\"EQUALS\">\n" +
                    "                        <corecom:ElementPath>Identification/AlternateObjectKey/ID</corecom:ElementPath>\n" +
                    "                        <corecom:Value>";

            String postfix = "</corecom:Value>\n" +
                    "                     </corecom:ValueExpression>\n" +
                    "                  </corecom:QueryExpression>\n" +
                    "               </corecom:QueryCriteria>\n" +
                    "            </ns1:Query>\n" +
                    "         </ns1:DataArea>\n" +
                    "      </QueryCustomerPartyListEBM>\n" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";

            // SOAP request framing
            HttpPost post = getBillingProfilePost(canNum, prefix, middle, postfix);

            // Making SOAP request to AIA
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(post);

            // Throw Exp if its not OK status
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                throw new ServerException(response.getStatusLine().toString());

            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();

            // Configure Namespace
            xPath.setNamespaceContext(new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    if ("env".equals(prefix))
                        return "http://schemas.xmlsoap.org/soap/envelope/";
                    if ("ns0".equals(prefix))
                        return "http://xmlns.oracle.com/EnterpriseObjects/Core/EBO/CustomerParty/V2";
                    if ("corecom".equals(prefix))
                        return "http://xmlns.oracle.com/EnterpriseObjects/Core/Common/V2";
                    throw new IllegalArgumentException(prefix);
                }

                public String getPrefix(String namespaceURI) {
                    throw new UnsupportedOperationException();
                }

                public Iterator<String> getPrefixes(String namespaceURI) {
                    throw new UnsupportedOperationException();
                }
            });

            // Filter the response
            InputSource inputSource = new InputSource(new StringReader(EntityUtils.toString(response.getEntity())));
            NodeList nodes = (NodeList) xPath.evaluate("//ns0:CustomerPartyBillingProfile", inputSource, XPathConstants.NODESET);

            // Fetching BP from response
            outerLoop:
            for (int i = 0; i < nodes.getLength(); i++) {

                Node bp = null;
                boolean foundBP = false;
                NodeList cNodes = nodes.item(i).getChildNodes();
                for (int j = 0; j < cNodes.getLength(); j++) {
                    Node child = cNodes.item(j);
                    if (child.getNodeName().equalsIgnoreCase("corecom:Identification"))
                        bp = child;

                    if (child.getNodeName().equalsIgnoreCase("ns0:PreferredIndicator") && child.getTextContent().equalsIgnoreCase("true"))
                        foundBP = true;

                    if(foundBP && bp != null) {
                        Node tmp = bp.getFirstChild();
                        while (!tmp.getNodeName().equalsIgnoreCase("corecom:ApplicationObjectKey"))
                            tmp = tmp.getNextSibling();

                        tmp = tmp.getFirstChild();
                        while (!tmp.getNodeName().equalsIgnoreCase("corecom:ID"))
                            tmp = tmp.getNextSibling();

                        billingProfileID = tmp.getTextContent();  // This contains BP
                        break outerLoop;
                    }
                }
            }

        }catch (Throwable e) {
            logger.error("Failed to get BP for CAN=" + canNum, e);
            throw e;
        }

        logger.info("AIA returned BP=" + billingProfileID + " for CAN=" + canNum);
        return billingProfileID;
    }

    private HttpPost getBillingProfilePost(String canNum, String prefix, String middle, String postfix) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(mAIABPAWSURL);
        post.setEntity(new StringEntity(prefix + canNum + middle + canNum + postfix));
        post.setHeader("Content-type", "text/xml; charset=UTF-8");
        post.setHeader("SOAPAction", "QueryCustomerPartyList");

        getHeader(ConfigType.PSP, MediaType.TEXT_XML)
                .forEach((key, val) -> val.stream().findFirst().ifPresent(
                        firstVal -> post.setHeader(key, firstVal)
                ));
        return post;
    }

    /**
     * Wrapper for the SOAP call for queryInvoiceDetail
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:com="http://www.intuit.net/xml/CommonBillingProfileABO">
     * <soapenv:Header/>
     * <soapenv:Body>
     * <com:QueryBillingInfoRequest Language="?" Locale="?" MessageId="?" EnterpriseServerName="?">
     * <com:AccountId>784457309</com:AccountId>
     * <!--Optional:-->
     * <com:AccountName>Smoke Test 0926</com:AccountName>
     * <!--Optional:-->
     * <com:MaximumNumberOfRecords>100</com:MaximumNumberOfRecords>
     * <!--Optional:-->
     * <com:CurrencyCode>?</com:CurrencyCode>
     * <!--Optional:-->
     * <com:BillingProfileName>American Express_4343_26_TRN-565KY8N</com:BillingProfileName>
     * <com:BillingProfileId>TRN-4YJ64BD</com:BillingProfileId>
     * <com:ServiceAccountId>784457309</com:ServiceAccountId>
     * <!--Optional:-->
     * <com:BillPOID>0.0.0.1 /bill 36335873 0</com:BillPOID>
     * </com:QueryBillingInfoRequest>
     * </soapenv:Body>
     * </soapenv:Envelope>
     */
    private List<ItemCharge> queryInvoiceDetails(QueryBillingTransactionsIntuitReqABCS pPort,
                                                 String pAccountId,
                                                 String pAccountName,
                                                 int pMaximumNumberOfRecords,
                                                 String pBillingProfileName,
                                                 String pBillingProfileId,
                                                 String pServiceAccountId,
                                                 String pBillPOID) throws IntuitFaultMsg {
        // Setting SoapAction URI manually.
        ((BindingProvider) mPort).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "QueryInvoiceDetails");

        QueryAccountBalanceSummaryType req = new QueryAccountBalanceSummaryType();
        String lang = "?";
        String enterpriseServerName = "?";
        String locale = "?";
        req.setLanguage(lang);
        req.setEnterpriseServerName(enterpriseServerName);
        req.setLocale(locale);
        req.setAccountId(pAccountId);
        req.setAccountName(pAccountName);
        req.setMaximumNumberOfRecords(String.valueOf(pMaximumNumberOfRecords));
        req.setCurrencyCode(AIA_CURRENCY_CODE);
        req.setBillingProfileId(pBillingProfileId);
        req.setBillingProfileName(pBillingProfileName);
        req.setServiceAccountId(pServiceAccountId);
        req.setBillPOID(pBillPOID);
        QueryBilledInvoiceDetailsResponseType response = pPort.queryInvoiceDetails(req);
        return getItemCharges(response);
    }

    /**
     * Wrapper for SOAP call, getEventRequest
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:com="http://www.intuit.net/xml/CommonBillingProfileABO">
     * <soapenv:Header/>
     * <soapenv:Body>
     * <com:QueryBilledItemEventDetailRequest>
     * <com:BillingProfile Language="?" Locale="?" MessageId="?" EnterpriseServerName="?">
     * <com:AccountId>784457309</com:AccountId>
     * <!--Optional:-->
     * <com:AccountName>Smoke Test 0926</com:AccountName>
     * <!--Optional:-->
     * <com:MaximumNumberOfRecords>100</com:MaximumNumberOfRecords>
     * <!--Optional:-->
     * <com:CurrencyCode>?</com:CurrencyCode>
     * <!--Optional:-->
     * <com:BillingProfileName>American Express_4343_26_TRN-565KY8N</com:BillingProfileName>
     * <com:BillingProfileId>TRN-565KY8N</com:BillingProfileId>
     * <com:ServiceAccountId>784457309</com:ServiceAccountId>
     * <!--Optional:-->
     * <com:BillPOID>0.0.0.1 /bill 36335873 0</com:BillPOID>
     * </com:BillingProfile>
     * <com:ItemChargeID>0.0.0.1 /item/usage 37788317 5</com:ItemChargeID>
     * </com:QueryBilledItemEventDetailRequest>
     * </soapenv:Body>
     * </soapenv:Envelope>
     */
    private int queryEventDetails(QueryBillingTransactionsIntuitReqABCS pPort,
                                  String pAccountId,
                                  String pAccountName,
                                  int pMaximumNumberOfRecords,
                                  String pBillingProfileName,
                                  String pBillingProfileId,
                                  String pServiceAccountId,
                                  String pBillPOID,
                                  String pItemChargeId) throws IntuitFaultMsg {

        // Setting SoapAction URI manually.
        ((BindingProvider) mPort).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "QueryEventDetails");

        QueryBilledItemEventDetailRequestType req = new QueryBilledItemEventDetailRequestType();
        BillingProfileType bpt = new BillingProfileType();
        String lang = "?";
        String enterpriseServerName = "?";
        String locale = "?";
        bpt.setLanguage(lang);
        bpt.setEnterpriseServerName(enterpriseServerName);
        bpt.setLocale(locale);

        bpt.setAccountId(pAccountId);
        bpt.setAccountName(pAccountName);
        bpt.setMaximumNumberOfRecords(String.valueOf(pMaximumNumberOfRecords));
        bpt.setCurrencyCode(AIA_CURRENCY_CODE);
        bpt.setBillingProfileId(pBillingProfileId);
        bpt.setBillingProfileName(pBillingProfileName);
        bpt.setServiceAccountId(pServiceAccountId);
        bpt.setBillPOID(pBillPOID);
        req.setBillingProfile(bpt);
        req.setItemChargeID(pItemChargeId);
        QueryBilledItemEventDetailsType eventDetailsResponse = pPort.queryEventDetails(req);
        return getUsageCounts(eventDetailsResponse);
    }

    /**
     * Initialize the members with config parameters
     */
    private void readConfigurationParameters() {
        boolean manageTransaction = !Application.hasActiveTransaction();
        mAIAURLAWS = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "aiaGateway_server_awsurl");
        mLocalURL = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "aiaGateway_local_url");
        mAIABPAWSURL = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "aiaGateway_billingProfile_server_awsurl");
        String maxNumberOfRecords = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter,"aiaGateway_max_bills");
        if(StringUtils.isNumeric(maxNumberOfRecords)) {
            mMaxNumberOfRecords=Integer.valueOf(maxNumberOfRecords);
        }  else {
            logger.info("The configuration for psp_aia_max_bills is not set correctly. Defaulting to 50");
            mMaxNumberOfRecords=50;
        }
        try {
            if (manageTransaction) {
                Application.beginUnitOfWork();
            }

            mRequestTimeout = SystemParameter.findIntValue(SystemParameter.Code.AIA_REQUEST_TIMEOUT, 10000);
        } finally {
            if (manageTransaction) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    /**
     * Creates a new instance of the ESBQueryBillingTransactionsIntuitEBSService
     *
     * @param pAiaUrl WSDL url
     * @return Instance of   ESBQueryBillingTransactionsIntuitEBSService
     * @throws Exception
     */
    private static ESBQueryBillingTransactionsIntuitEBSService createQueryBillingTransactionService(String pAiaUrl) throws Exception {
        return new ESBQueryBillingTransactionsIntuitEBSService(new URL(pAiaUrl), SERVICE_QNAME);
    }

    /**
     * Parse the getQueryList SOAP response, filter bills from the last year
     *
     * @param pQueryInvoiceProfileResponse SOAP response
     * @return BillInfo objects
     */
    private List<BillInfo> filterBillPOIDsForLastYear(QueryInvoiceProfileResponseType pQueryInvoiceProfileResponse, Date fromDate, Date toDate) {
        List<BillInfo> billPOIDs = new ArrayList<BillInfo>();

        boolean nonEmptyResponse = (pQueryInvoiceProfileResponse != null) &&
                (pQueryInvoiceProfileResponse.getDataArea() != null) &&
                (pQueryInvoiceProfileResponse.getDataArea().getBilledInvoiceProfile() != null) &&
                (pQueryInvoiceProfileResponse.getDataArea().getBilledInvoiceProfile().getBilledInvoiceProfileList() != null) &&
                (pQueryInvoiceProfileResponse.getDataArea().getBilledInvoiceProfile().getBilledInvoiceProfileList().size() > 0);
        if (!nonEmptyResponse) {
            return null;
        }

        BilledInvoiceProfileListType billingProfile = pQueryInvoiceProfileResponse.getDataArea().getBilledInvoiceProfile();

        SpcfCalendar fromDateRange,toDateRange;

        if (fromDate == null || toDate == null) {
            toDateRange = PSPDate.getPSPTime();
            fromDateRange= toDateRange.copy();
            fromDateRange.addYears(-1);
        }else {
            //Specify the range which the data should be fetched
            fromDateRange = CalendarUtils.convertToSpcfCalendar(fromDate);
            toDateRange = CalendarUtils.convertToSpcfCalendar(toDate);
        }

        logger.debug("Bill Size before filtering:: " + billingProfile.getBilledInvoiceProfileList().size());
        for (BilledInvoiceProfileType billedInvoiceProfile : billingProfile.getBilledInvoiceProfileList()) {
            if (billedInvoiceProfile != null) {
                try {
                    boolean zeroDollarBill =  billedInvoiceProfile.getAmountDue()==null || billedInvoiceProfile.getAmountDue().equals("0");
                    String dueDateStr = billedInvoiceProfile.getDueDate();
                    Date dueDate = SIMPLE_DATE_FORMAT.parse(dueDateStr);
                    SpcfCalendar dueDateCal = CalendarUtils.convertToSpcfCalendar(dueDate);

                    //if (!zeroDollarBill && dueDateCal.after(oneYearAgo)) {
                    if (dueDateCal.after(fromDateRange) && dueDateCal.before(toDateRange))  {
                        logger.debug("bill is from one year ago - needs to be processed : " + billedInvoiceProfile.getBillPOID());
                        BillInfo billInfo = new BillInfo();
                        billInfo.setBillDate(dueDateCal);
                        billInfo.setBillPOID(billedInvoiceProfile.getBillPOID());
                        billPOIDs.add(billInfo);
                    }
                } catch (ParseException pe) {
                    logger.error("Invalid date date " + billedInvoiceProfile.getDueDate());
                }
            }
        }
        //}
        logger.debug("Bill Size after filtering:: " + billPOIDs.size());
        return billPOIDs;
    }

    /**
     * Parse the queryInvoiceDetails response - return item charges for symphony items.
     *
     * @param pQueryBilledInvoiceDetailsResponse
     *         SOAP Response
     * @return ItemCharge from the SOAP response
     */
    private List<ItemCharge> getItemCharges(QueryBilledInvoiceDetailsResponseType pQueryBilledInvoiceDetailsResponse) {
        List<ItemCharge> itemCharges = new ArrayList<ItemCharge>();

        boolean nonEmptyResponse = (pQueryBilledInvoiceDetailsResponse != null) &&
                (pQueryBilledInvoiceDetailsResponse.getDataArea() != null) &&
                (pQueryBilledInvoiceDetailsResponse.getDataArea().getBilledInvoiceDetails() != null) &&
                (pQueryBilledInvoiceDetailsResponse.getDataArea().getBilledInvoiceDetails().getBilledUsageServiceCharges() != null) &&
                (pQueryBilledInvoiceDetailsResponse.getDataArea().getBilledInvoiceDetails().getBilledUsageServiceCharges().getBilledUsageServiceChargesList() != null) &&
                (pQueryBilledInvoiceDetailsResponse.getDataArea().getBilledInvoiceDetails().getBilledUsageServiceCharges().getBilledUsageServiceChargesList().size() > 0);
        if (!nonEmptyResponse) {
            return null;
        }

        BilledUsageChargesListType invoiceDetails = pQueryBilledInvoiceDetailsResponse.getDataArea().getBilledInvoiceDetails();
        List<BilledUsageServiceChargesType> billedUsageServiceChargesList = invoiceDetails.getBilledUsageServiceCharges().getBilledUsageServiceChargesList();
        //System.out.println("Item Charges before filtering : " + billedUsageServiceChargesList.size());
        for (BilledUsageServiceChargesType usageServiceCharges : billedUsageServiceChargesList) {
            boolean itemChargesAvailable = (usageServiceCharges != null) &&
                    (usageServiceCharges.getBilledUsageItemCharges() != null) &&
                    (usageServiceCharges.getBilledUsageItemCharges().getBilledUsageItemChargesList() != null) &&
                    (usageServiceCharges.getBilledUsageItemCharges().getBilledUsageItemChargesList().size() > 0);
            if (itemChargesAvailable) {
                BilledUsageItemChargesListType usageItemChargesList = usageServiceCharges.getBilledUsageItemCharges();
                List<BilledUsageItemChargesType> billedUsageItemCharges = usageItemChargesList.getBilledUsageItemChargesList();
                for (BilledUsageItemChargesType billedUsageItemCharge : billedUsageItemCharges) {
                    if (billedUsageItemCharge != null) {
                        String itemName = billedUsageItemCharge.getItemName();
                        boolean isUsageBillingSku = (itemName != null && (itemName.contains(MONTHLY_SUBSCRIPTION_ITEM_NAME) ||
                                itemName.contains(ANNUAL_SUBSCRIPTION_ITEM_NAME) || itemName.contains(PAYROLL_EE_ITEM_NAME)));
                        if (isUsageBillingSku) {
                            String itemCharge = billedUsageItemCharge.getItemCharge();
                            String itemChargeId = billedUsageItemCharge.getItemChargeId();
                            ItemCharge itemChargeObj = new ItemCharge();
                            itemChargeObj.setItemChargeAmount(itemCharge);
                            itemChargeObj.setItemChargeId(itemChargeId);
                            itemChargeObj.setItemName(itemName);
                            itemCharges.add(itemChargeObj);
                        }
                    }
                }
            }
        }
        //System.out.println("Item Charges after filtering : " + itemCharges.size());
        return itemCharges;
    }

    /**
     * Parse the queryEventDetails SOAP response and return the BRM Usage count
     *
     * @param pEventDetailsResponse SOAP Response
     * @return Usage Count
     */
    private Integer getUsageCounts(QueryBilledItemEventDetailsType pEventDetailsResponse) {
        boolean nonEmptyResponse = (pEventDetailsResponse != null) &&
                (pEventDetailsResponse.getDataArea() != null) &&
                (pEventDetailsResponse.getDataArea().getBilledItemEventDetails() != null) &&
                (pEventDetailsResponse.getDataArea().getBilledItemEventDetails().getBilledItemEventDetail() != null);
        if (!nonEmptyResponse) {
            return 0;
        }
        List<BilledItemEventDetailType> billedItemEventDetailList = pEventDetailsResponse.getDataArea().getBilledItemEventDetails().getBilledItemEventDetail();
        int usageCount = 0;
        for (BilledItemEventDetailType billEventDetail : billedItemEventDetailList) {
            if (billEventDetail != null) {
                String durationStr = billEventDetail.getDuration();
                if (durationStr != null && StringUtils.isNumeric(durationStr)) {
                    int duration = Integer.parseInt(durationStr);
                    usageCount += duration;
                }
            }
        }
        return usageCount;
    }

}