package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.intuit.sbd.payroll.psp.gateways.salestax.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.gateways.salestax.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.util.RetryHelper;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.openapplications.oagis.Address;
import org.openapplications.oagis.AddressesType;
import org.openapplications.oagis.Amount;
import org.openapplications.oagis.ApplicationArea;
import org.openapplications.oagis.BODDocument;
import org.openapplications.oagis.BODFailureDocument;
import org.openapplications.oagis.BusinessDocument;
import org.openapplications.oagis.ConfirmBODDataArea;
import org.openapplications.oagis.ConfirmBODDocument;
import org.openapplications.oagis.Contact;
import org.openapplications.oagis.ContactsDocument;
import org.openapplications.oagis.DocumentId;
import org.openapplications.oagis.DocumentIds;
import org.openapplications.oagis.Get;
import org.openapplications.oagis.GetQuoteDocument;
import org.openapplications.oagis.Header;
import org.openapplications.oagis.ItemId;
import org.openapplications.oagis.ItemIds;
import org.openapplications.oagis.NounFailure;
import org.openapplications.oagis.NounOutcomeDocument;
import org.openapplications.oagis.OrderItem;
import org.openapplications.oagis.OrderQuantity;
import org.openapplications.oagis.PartiesDocument;
import org.openapplications.oagis.PartyInstitutional;
import org.openapplications.oagis.Person;
import org.openapplications.oagis.PersonName;
import org.openapplications.oagis.Quantity;
import org.openapplications.oagis.QuoteDocument;
import org.openapplications.oagis.QuoteHeader;
import org.openapplications.oagis.QuoteLine;
import org.openapplications.oagis.ReturnCriteria;
import org.openapplications.oagis.ShowQuoteDocument;
import org.openapplications.oagis.Tax;
import org.openapplications.oagis.Telephone;
import org.openapplications.oagis.UnitPrice;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;

import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 3, 2008
 * Time: 11:36:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class SalesTaxGatewayImpl implements ISalesTaxGateway {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(SalesTaxGatewayImpl.class);
    private static final String XML_VERSION = "V3-0";
    private static final String SELECT_EXPRESSION_CALCULATE_TAX = "Calculate Tax";
    private static final String CURRENCY = "USD";
    private static final String IAS_GET_QUOTE_URL_PROP = "salestax.url";
    private static final String IAS_GET_QUOTE_AWS_URL_PROP = "salestax.awsurl";
    private static final String CONNECT_TIMEOUT_PROP = "salestax.msConnectTimeout";
    private static final String READ_TIMEOUT_PROP = "salestax.msReadTimeout";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 10000; // typical production time is about 1,200 ms... about 1 call in 450 takes longer than 10,000 ms

    private static String sfQuoteInterfaceURL;
    private static int sfConnectTimeout;
    private static int sfReadTimeout;

    static {
        setSalesTaxGatewayParameterInfo(null, null, null);
    }

    public static void setSalesTaxGatewayParameterInfo(String pQuoteInterfaceURL,
                                                       Integer pConnectTimeout,
                                                       Integer pReadTimeout) {

        if (pQuoteInterfaceURL != null) {
            sfQuoteInterfaceURL = pQuoteInterfaceURL;
        } else {
            sfQuoteInterfaceURL = ConfigurationManager.getSettingValue(ConfigurationModule.SalesTaxGateway,
                    IAS_GET_QUOTE_AWS_URL_PROP);
        }

        logger.info("Quote Interface: "+sfQuoteInterfaceURL);

        if (pConnectTimeout != null) {
            sfConnectTimeout = pConnectTimeout;
        } else {
            int msConnectTimeout;

            try {
                String value = ConfigurationManager.getSettingValue(ConfigurationModule.SalesTaxGateway,
                                                                    CONNECT_TIMEOUT_PROP);
                msConnectTimeout = Integer.parseInt(value);
            } catch (Throwable e) {
                msConnectTimeout = DEFAULT_CONNECT_TIMEOUT_MS;
            }
            sfConnectTimeout = msConnectTimeout;
        }

        if (pReadTimeout != null) {
            sfReadTimeout = pReadTimeout;
        } else {
            int msReadTimeout;

            try {
                String value = ConfigurationManager.getSettingValue(ConfigurationModule.SalesTaxGateway,
                                                                    READ_TIMEOUT_PROP);
                msReadTimeout = Integer.parseInt(value);
            } catch (Throwable e) {
                msReadTimeout = DEFAULT_READ_TIMEOUT_MS;
            }

            sfReadTimeout = msReadTimeout;
        }
    }

    /**
     * Function to call the IAS Get Quote Inteface by passing the XML over HTTP.
     *
     * @param pSalesTaxRequest SalesTaxRequest
     * @return SalesTaxResponse
     */
    public SalesTaxResponse send(SalesTaxRequest pSalesTaxRequest) {
        logger.debug("send()");
        if (nonZeroRequest(pSalesTaxRequest)) {
            SalesTaxResponse salesTaxResponse = new SalesTaxResponse();
            try {
                String generatedXmlStr = this.generateGetQuoteXml(pSalesTaxRequest);

                salesTaxResponse = this.postXmlToQuoteInterface(generatedXmlStr);
            }
            catch (Exception ex) {
                logger.error("Error while executing send() " + ex);
            }

            return salesTaxResponse;
        }
        else {
            return null;
        }
    }

    private Boolean nonZeroRequest(SalesTaxRequest pSalesTaxRequest) {
        for (SalesTaxRequestLine taxRequestLine : pSalesTaxRequest.getSalesTaxRequestLineList()) {
            if (taxRequestLine.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to post the XML to IAS Get Quote Interface by calling the given URL and get the response in the form
     * of XML.
     *
     * @param pGeneratedXmlStr   String
     * @return SalesTaxResponse
     */
    private SalesTaxResponse postXmlToQuoteInterface(String pGeneratedXmlStr) throws Exception {
        logger.debug("postXmlToQuoteInterface()");
        RetryHelper retryHelper = new RetryHelper();
        SalesTaxResponse response=retryHelper.retryTemplate().execute(new RetryCallback<SalesTaxResponse, Exception>() {

            @Override
            public SalesTaxResponse doWithRetry(RetryContext retryContext) throws Exception {
                int retryCount = retryContext.getRetryCount();
                logger.info("RetryCount for SalesTax service call - "+retryCount);
                return executeHttpMethod(pGeneratedXmlStr, true);
            }
        });
        return response;
    }

    private SalesTaxResponse executeHttpMethod(String pGeneratedXmlStr, boolean withHeader) throws Exception{
        HttpURLConnection conn;
        BufferedInputStream response;
        try {
            sfQuoteInterfaceURL = withHeader? ConfigurationManager.getSettingValue(ConfigurationModule.SalesTaxGateway, IAS_GET_QUOTE_AWS_URL_PROP):ConfigurationManager.getSettingValue(ConfigurationModule.SalesTaxGateway,
                    IAS_GET_QUOTE_URL_PROP);
                URL url = new URL(sfQuoteInterfaceURL);
                logger.info(String.format("SalesTax is called using URL=%s, ConnectTimeOut=%s, ReadTimeOut=%s", url, sfConnectTimeout, sfReadTimeout));
                conn = (HttpURLConnection) url.openConnection();
                String tid = SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");
                if(withHeader){
                    String offlineTicket = OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP);
                    if (offlineTicket != null) {
                        conn.setRequestProperty("Authorization", offlineTicket);
                        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_XML);
                        conn.setRequestProperty("intuit_tid", tid);
                    }
                } else {
                conn.setRequestProperty("Content-Type", MediaType.APPLICATION_XML);
            }
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(sfConnectTimeout);
            conn.setReadTimeout(sfReadTimeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(pGeneratedXmlStr.getBytes());
            os.flush();
            os.close();

            response = new BufferedInputStream(conn.getInputStream());

            return this.processResponse(response);
        }
        catch (Exception ex) {
            String errorMessage = withHeader? "Error in postXmlToQuoteInterface() executeHttpMethod()" : "Error in postXmlToQuoteInterface() executeHttpMethodWithoutHeader()";
            logger.error(errorMessage + ex);
            ex.printStackTrace();
            throw new Exception("salestax call failed: "+errorMessage);
        }

    }

    /**
     * Function to generate the XML document using the sources generated by XSD's
     *
     * @param pSalesTaxRequest SalesTaxRequest
     * @return String
     */
    private String generateGetQuoteXml(SalesTaxRequest pSalesTaxRequest) {
        logger.debug("generateGetQuoteXml()");
        GetQuoteDocument doc = null;
        try {
            doc = GetQuoteDocument.Factory.newInstance();

            //Document Root
            GetQuoteDocument.GetQuote quoteNode = doc.addNewGetQuote();

            //Add Application Area Node
            ApplicationArea appAreaNode = quoteNode.addNewApplicationArea();
            appAreaNode.setCreationDateTime(Calendar.getInstance());
            appAreaNode.addNewUserArea().setVersion(XML_VERSION);

            //Add DataArea Node
            GetQuoteDocument.GetQuote.DataArea dataAreaNode = quoteNode.addNewDataArea();

            //Add Get Node
            Get getNode = dataAreaNode.addNewGet();

            //Confirm Always
            getNode.setConfirm(Get.Confirm.ALWAYS);

            //Add Return Criteria
            ReturnCriteria retCriteriaNode = getNode.addNewReturnCriteria();
            ReturnCriteria.SelectExpression[] selExpressions = new ReturnCriteria.SelectExpression[1];

            //Calculate Tax
            XmlString xmlString = XmlString.Factory.newInstance();
            xmlString.setStringValue(SELECT_EXPRESSION_CALCULATE_TAX);

            selExpressions[0] = ReturnCriteria.SelectExpression.Factory.newInstance();
            selExpressions[0].set(xmlString);

            retCriteriaNode.setSelectExpressionArray(selExpressions);

            //Add Quote Node
            QuoteDocument.Quote quote = dataAreaNode.addNewQuote();

            //Add Quote Header Node
            QuoteHeader quoteHeaderNode = quote.addNewHeader();

            //Add Document Ids Node
            DocumentIds docIds = quoteHeaderNode.addNewDocumentIds();

            // Document ID - OrderID
            DocumentId docId = docIds.addNewDocumentId();

            docId.setId(pSalesTaxRequest.getDocumentId());

            // Document DateTime
            quoteHeaderNode.setDocumentDateTime(pSalesTaxRequest.getDocumentDateTime());

            //Add Parties Node
            PartiesDocument.Parties partiesNode = quoteHeaderNode.addNewParties();

            //Add Customer Party
            PartyInstitutional party = partiesNode.addNewCustomerParty();

            //Add Business Node
            BusinessDocument.Business business = party.addNewBusiness();
            business.setName(pSalesTaxRequest.getCompanyName());

            //Add Addresses Node
            AddressesType addressType = party.addNewAddresses();

            //Add Primary Address Node
            Address address = addressType.addNewPrimaryAddress();

            String[] addressLines = new String[3];
            addressLines[0] = pSalesTaxRequest.getAddressLine1();
            addressLines[1] = pSalesTaxRequest.getAddressLine2();
            addressLines[2] = pSalesTaxRequest.getAddressLine3();

            address.setAddressLineArray(addressLines);

            address.setCity(pSalesTaxRequest.getCity());
            address.setStateOrProvince(pSalesTaxRequest.getState());
            address.setCountry(pSalesTaxRequest.getCountry());
            address.setPostalCode(pSalesTaxRequest.getZipCode());

            //Add Contact Node
            ContactsDocument.Contacts contactType = party.addNewContacts();

            //Add Primary Contact Node
            Contact contact = contactType.addNewPrimaryContact();

            //Add Person Node
            Person person = contact.addNewPerson();
            PersonName personName = person.addNewPersonName();
            personName.setGivenName(pSalesTaxRequest.getFirstName());
            personName.setFamilyName(pSalesTaxRequest.getLastName());

            //Add Telephone
            Telephone telephone = contact.addNewTelephone();
            telephone.setType(Telephone.Type.WORK);
            telephone.setStringValue(pSalesTaxRequest.getPhoneNumber());

            //Add Email Address
            String[] emailAddress = new String[1];
            emailAddress[0] = pSalesTaxRequest.getEmail();

            contact.setEMailAddressArray(emailAddress);

            //Add User Area Node
            QuoteHeader.UserArea userAreaNode = quoteHeaderNode.addNewUserArea();

            userAreaNode.setSalesOrganization("PSP");
            userAreaNode.setTaxHandling(QuoteHeader.UserArea.TaxHandling.STANDARD);

            //Add Line Node
            ArrayList<SalesTaxRequestLine> requestLineList = pSalesTaxRequest.getSalesTaxRequestLineList();
            int index = 0;
            for (SalesTaxRequestLine requestLine : requestLineList) {
                //Add Line
                QuoteLine lineNode = dataAreaNode.getQuote().addNewLine();
                lineNode.setLineNumber(new BigInteger("" + (++index)));

                //Add Order Item Node
                OrderItem orderItemNode = lineNode.addNewOrderItem();
                ItemIds itemIdsNode = orderItemNode.addNewItemIds();

                //Add Alternate Id Node
                ItemId itemIdNode = itemIdsNode.addNewItemId();
                ItemId.Id idNode = itemIdNode.addNewId();
                idNode.setStringValue(requestLine.getSKU());

                //Add Order Quantity Node
                OrderQuantity orderQuanityNode = lineNode.addNewOrderQuantity();
                orderQuanityNode.setUom(OrderQuantity.Uom.EACH);
                orderQuanityNode.setStringValue("" + requestLine.getQuantity());

                //Add Unit Price Node
                UnitPrice unitPriceNode = lineNode.addNewUnitPrice();
                Amount amountNode = unitPriceNode.addNewAmount();
                amountNode.setCurrency(CURRENCY);
                amountNode.setStringValue("" + requestLine.getAmount());

                Quantity quantityNode = unitPriceNode.addNewPerQuantity();
                quantityNode.setUom(Quantity.Uom.EACH);
                quantityNode.setStringValue("" + requestLine.getQuantity());
            }

        }
        catch (Exception ex) {
            logger.error("Error while generateGetQuoteXml() " + ex);
        }

        return doc.xmlText();
    }

    /**
     * Function to build the SalesTaxResponse object by using the response xml.
     *
     * @param pResponse InputStream
     * @return SalesTaxResponse
     */
    private SalesTaxResponse processResponse(InputStream pResponse) {
        SalesTaxResponse salesTaxResponse = new SalesTaxResponse();

        try {
            XmlObject xmlObjExpected = XmlObject.Factory.parse(pResponse);

            if (xmlObjExpected instanceof ShowQuoteDocument) {
                salesTaxResponse.setSuccess(true);

                ShowQuoteDocument showQuotedoc = (ShowQuoteDocument) xmlObjExpected;

                QuoteHeader quoteHeader = showQuotedoc.getShowQuote().getDataArea().getQuote().getHeader();
                PartiesDocument.Parties partyNode = quoteHeader.getParties();

                if (partyNode != null) {
                    PartyInstitutional customerParty = partyNode.getCustomerPartyArray(0);
                    Address address = customerParty.getAddresses().getPrimaryAddressArray(0);

                    salesTaxResponse.setTaxJurisdiction(address.getTaxJurisdiction());

                    QuoteHeader.UserArea userArea = quoteHeader.getUserArea();
                    salesTaxResponse.setTotalTaxAmount(userArea.getTotalTax().getBigDecimalValue());

                    QuoteLine[] lineArray = showQuotedoc.getShowQuote().getDataArea().getQuote().getLineArray();

                    SalesTaxResponseLine responseLine;

                    for (QuoteLine aLineArray : lineArray) {
                        responseLine = new SalesTaxResponseLine();
                        ItemId idNode = aLineArray.getOrderItem().getItemIds().getItemId();

                        responseLine.setSKU(idNode.getId().getStringValue());

                        Tax taxNode = aLineArray.getTax();
                        responseLine.setTaxAmount(taxNode.getTaxAmount().getBigDecimalValue());
                        responseLine.setTaxRate(taxNode.getPercentQuantity().getBigDecimalValue());

                        salesTaxResponse.addLine(responseLine);
                    }
                }
            }
            else if (xmlObjExpected instanceof ConfirmBODDocument) {
                salesTaxResponse.setSuccess(false);
                ConfirmBODDocument doc = (ConfirmBODDocument) xmlObjExpected;
                ConfirmBODDataArea dataAreaNode = doc.getConfirmBOD().getDataArea();
                BODDocument.BOD[] bodArray = dataAreaNode.getBODArray();

                for (BODDocument.BOD aBodArray : bodArray) {
                    Header bodHeaderNode = aBodArray.getHeader();
                    BODFailureDocument.BODFailure failureNode = bodHeaderNode.getBODFailure();

                    com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage failureMsg =
                            new com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage();
                    failureMsg.setErrorCode(failureNode.getErrorMessage().getReasonCode());
                    failureMsg.setErrorDescription(failureNode.getErrorMessage().getDescription());

                    salesTaxResponse.setSummaryErrorMessage(failureMsg);

                    NounOutcomeDocument.NounOutcome[] nounOutComeNodeArray = aBodArray.getNounOutcomeArray();

                    for (NounOutcomeDocument.NounOutcome aNounOutComeNode : nounOutComeNodeArray) {
                        NounFailure[] nounFailureNodeArray = aNounOutComeNode.getNounFailureArray();

                        ArrayList<com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage> detailErrorMsgList =
                                new ArrayList<com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage>();

                        for (NounFailure nounFailureNode : nounFailureNodeArray) {
                            com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage errorMessage =
                                    new com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage();

                            errorMessage.setErrorCode(nounFailureNode.getErrorMessage().getReasonCode());
                            errorMessage.setErrorDescription(nounFailureNode.getErrorMessage().getDescription());
                            detailErrorMsgList.add(errorMessage);
                        }
                        salesTaxResponse.setDetailErrorMessageList(detailErrorMsgList);
                    }
                }
            }
        }
        catch (Exception ex) {
            logger.error("Error while processResponse() " + ex);
        }

        return salesTaxResponse;
    }
}
