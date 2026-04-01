package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.PspCertificateManager;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.*;

import com.intuit.ias.notification.pub.wsdl.NotificationPort;
import com.intuit.ias.notification.pub.xsd.SendResponse;
import com.intuit.ias.notification.pub.xsd.SendRequest;
import com.intuit.ias.notification.pub.xsd.NotificationDataType;
import com.intuit.ias.notification.pub.xsd.NotificationTypeDataType;
import com.intuit.ias.notification.pub.xsd.BasicSenderDataType;
import com.intuit.ias.notification.pub.xsd.BasicContentDataType;
import com.intuit.ias.notification.pub.xsd.AttributeDataType;
import com.intuit.ias.notification.pub.xsd.BasicRecipientDataType;
import com.intuit.ias.notification.pub.xsd.ContentFormatDataType;
import com.intuit.ias.common.xsd.ErrorDataType;
import com.intuit.ias.common.xsd.HeaderDataType;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 20, 2008
 * Time: 8:12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotificationService {

    private static final String KEY_STORE_ALIAS = "email.gateway";
    private static final String SSL_SOCKET_FACTORY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";

    /*
     * Class constructor
     */
    public NotificationService()
    {}

    /*
     * Notification service request and response
     *
     * @param serviceURL Notification URL concatenated with '?wsld'
     * @param namespace Target name as defined in the WSDL
     * @param serviceName Service endpoint name as defined in the WSDL
     * @param keyStore SSL certificate file
     * @param keyStorePassword SSL password
     */
    @SuppressWarnings("unchecked")  
    private void testService(String serviceURL, String namespace, String serviceName)
    {
        try
        {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump","true");

            // For more verbose logging, change log levels from'info' to 'debug'
            Application.initializeLogger("resources/log4j2-test.xml");
            // Notification service
            URL wsdlLocation = new URL(serviceURL + "?wsdl");

            QName qName = new QName(namespace, serviceName);

            Service service;
            try {
                service = new com.intuit.ias.notification.pub.wsdl.NotificationService(wsdlLocation, qName);
            } catch (Exception e) {
                String wsdlPath = EmailUtils.getConfig("iasns-servicepath");
                service = new com.intuit.ias.notification.pub.wsdl.NotificationService(new URL("file:///" + wsdlPath), qName);
            }

            NotificationPort notificationPort = service.getPort(NotificationPort.class);
            Map<String, Object> requestContext = ((BindingProvider)notificationPort).getRequestContext();

            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, EmailUtils.getConfig("iasns-serviceurl"));

            if (serviceURL.startsWith("https:")) {
                requestContext.put(SSL_SOCKET_FACTORY, PspCertificateManager.getSSLSocketFactory(KEY_STORE_ALIAS));
            }

            System.out.println("Send request...");

            SendResponse response = notificationPort.send(getSOAPHeaderHolder(), getRequest());

            displayResponse(response);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Gets SOAP Body
     */
    @SuppressWarnings("unchecked")
    protected SendRequest getRequest()
    {
        // Construct the Send request
        SendRequest request = new SendRequest();

        // Request version
        request.setVersion("1.0");

        NotificationDataType notification = new NotificationDataType();

        // 1. NotificationType
        notification.setNotificationType(NotificationTypeDataType.EMAIL);

        // 2. DeliveryType
        notification.setDeliveryType(getDeliveryType());

        // 3. SenderProfile
        notification.setSenderProfile(getSenderProfile());

        // 4. ContentProfile
        notification.setContentProfile(getContentProfile());

        // 5. Destinations
        notification.setDestinations(getDestinations());

        // Add Notification to SendRequest
        request.setNotification(notification);

        return request;
    }

    /*
     * Generates DeliveryType
     */
    @SuppressWarnings("unchecked")
    private NotificationDataType.DeliveryType getDeliveryType()
    {
        NotificationDataType.DeliveryType deliveryType = new NotificationDataType.DeliveryType();

        deliveryType.setImmediate(new NotificationDataType.DeliveryType.Immediate());

        return deliveryType;
    }

    /*
     * Generates SenderProfile
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.SenderProfile getSenderProfile()
    {
        NotificationDataType.SenderProfile senderProfile = new NotificationDataType.SenderProfile();

        // SenderId
        senderProfile.setSenderId("PSP");

        // Sender
        BasicSenderDataType sender = new BasicSenderDataType();

        sender.setAddress("ken_paul@intuit.com");
        sender.setName("Ken Paul");

        senderProfile.setSender(sender);

        return senderProfile;
    }

    /*
     * Generates ContentProfile
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.ContentProfile getContentProfile()
    {
        NotificationDataType.ContentProfile contentProfile = new NotificationDataType.ContentProfile();

        // Content
        BasicContentDataType content = new BasicContentDataType();

        // ContentProviderId - Optional
        content.setContentProviderId("test_content_provider_id");

        // Content Subject - Optional
        //content.setSubject("");

        // ContentId
        // content.setContentId("TriggerSend");
        // content.setContentId("SubExpire");
        content.setContentId("PSP_DEV_TEST_EMAIL");

        contentProfile.setContent((BasicContentDataType) content);

        // Template Properties
        NotificationDataType.ContentProfile.TemplateProperties properties = new NotificationDataType.ContentProfile.TemplateProperties();

        List<AttributeDataType> attributes = properties.getAttribute();

        // Attribute 1
        AttributeDataType attribute = new AttributeDataType();
        attribute.setName("OfferingName");
        attribute.setValue("Test Payroll Service Offering");

        attributes.add(attribute);

//        // Attribute 2
//        AttributeDataType attribute2 = new AttributeDataType();
//        attribute2.setName("cont_name2");
//        attribute2.setValue("cont_value2");
//
//        attributes.add(attribute2);

        contentProfile.setTemplateProperties(properties);

        return contentProfile;
    }

    /*
     * Generates Destinations
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.Destinations getDestinations()
    {
        NotificationDataType.Destinations destinations = new NotificationDataType.Destinations();

        List<NotificationDataType.Destinations.Destination> destList = destinations.getDestination();

        destList.add(getDestination("Company123", "Kenny Paul", "ken_paul@intuit.com"));
        destList.add(getDestination("Company456", "Kenneth Paul", "ken_paul@intuit.com"));
//        destList.add(getDestination(2));
//        destList.add(getDestination(3));

        return destinations;
    }

    /*
     * Generates Destination
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.Destinations.Destination getDestination(String pCompanyId, String pContactName, String pContactEmailAddress)
    {
        NotificationDataType.Destinations.Destination destination = new NotificationDataType.Destinations.Destination();

        destination.setRecipientId(pCompanyId);

        BasicRecipientDataType recipient = new BasicRecipientDataType();

        destination.setRecipient(recipient);

        // Recipient e-mail address
        recipient.setAddress(pContactEmailAddress);

        // Recipient name
        recipient.setName(pContactName);

        destination.setRecipient(recipient);

        // Format
        destination.setFormat(ContentFormatDataType.HTML);

        // Template properties
        NotificationDataType.Destinations.Destination.TemplateProperties properties = new NotificationDataType.Destinations.Destination.TemplateProperties();

        List<AttributeDataType> attributes = properties.getAttribute();

        // Attribute 1
        AttributeDataType attribute = new AttributeDataType();
        attribute.setName("PSPTestParam");
        attribute.setValue("Test Value 999");
//        attribute.setValue("<table cellpadding=\"2\" cellspacing=\"0\" width=\"600\" ID=\"Table1\" Border=\"0\"><tr><td><font face=\"verdana\" size=\"1\" color=\"#444444\">This email was sent by: <b>Test Value 1</b><br><br></font></td></tr><tr><td><font face=\"verdana\" size=\"1\" color=\"#444444\">This email was setSent by: <b>Test Value 2</b><br><br></font></td></tr></table>");
//        attribute.setValue("<table cellpadding=\"2\" cellspacing=\"0\" width=\"600\" ID=\"Table1\" Border=\"0\"><tr><td><font face=\"verdana\" size=\"1\" color=\"#444444\">This email was sent by: <b>Test Value 1</b><br><br></font></td></tr><tr><td><font face=\"verdana\" size=\"1\" color=\"#444444\">This email was setSent by: <b>Test Value 2</b><br><br></font></td></tr></table>");
//        attribute.setValue("<![CDATA[<table cellpadding=\"2\" cellspacing=\"0\" width=\"600\" ID=\"Table1\" Border=\"0\"><tr><td><font face=\"verdana\" size=\"1\" color=\"#444444\">This email was sent by: <b>Test Value 1</b><br><br></font></td></tr><tr><td><font face=\"verdana\" size=\"1\" color=\"#444444\">This email was setSent by: <b>Test Value 2</b><br><br></font></td></tr></table>]]>");
//        attribute.setValue("<![CDATA[<table cellpadding=\\\"2\\\" cellspacing=\\\"0\\\" width=\\\"600\\\" ID=\\\"Table1\\\" Border=\\\"0\\\"><tr><td><font face=\\\"verdana\\\" size=\\\"1\\\" color=\\\"#444444\\\">This email was sent by: <b>Test Value 1</b><br><br></font></td></tr><tr><td><font face=\\\"verdana\\\" size=\\\"1\\\" color=\\\"#444444\\\">This email was setSent by: <b>Test Value 2</b><br><br></font></td></tr></table>]]>");

        attributes.add(attribute);

        destination.setTemplateProperties(properties);

        return destination;
    }

    /*
     * Displays SOAP repsonse
     */
    @SuppressWarnings("unchecked")
    private void displayResponse(SendResponse response)
    {
        System.out.println("==================================");
        System.out.println("Response");
        System.out.println("==================================");

        if (response == null)
        {
            System.out.println("Response is null");
            return;
        }

        ErrorDataType error = response.getError();

        if (error != null)
        {
            displayError(error);
            return;
        }

        List<SendResponse.Result> results = response.getResult();

        for (SendResponse.Result result : results)
        {
            System.out.println("   RecipientId: " + result.getRecipientId());

            if (result.getSuccess() != null)
            {
                System.out.println("   Success");
            }

            else
            {
                displayError(result.getError());
            }
        }
    }

    /*
     * Displays error
     */
    @SuppressWarnings("unchecked")
    private void displayError(ErrorDataType error)
    {
        if (error != null)
        {
            System.out.println("   Error Category:    " + error.getCategory());
            System.out.println("   Error Code:        " + error.getCode());
            System.out.println("   Error Description: " + error.getDescription());
            System.out.println("   Error Source:      " + error.getSource());
        }
    }

    /*
     * Gets SOAP Header
     */
    @SuppressWarnings("unchecked")
    protected Holder getSOAPHeaderHolder()
    {
        HeaderDataType header = new HeaderDataType();

        header.setServiceVersion("1.0");
        header.setTransactionId("test123");
        header.setSourceSystem("PSP");
        header.setCallerSystem("PSPEmailGateway");
        //header.setEndpoint("PSPEmailGateway");

        try
        {
            DatatypeFactory df = DatatypeFactory.newInstance();

            XMLGregorianCalendar createDate = df.newXMLGregorianCalendar();
            Calendar now = GregorianCalendar.getInstance(TimeZone.getDefault());

            createDate.setYear(now.get(Calendar.YEAR));
            createDate.setMonth(now.get(Calendar.MONTH) + 1);
            createDate.setDay(now.get(Calendar.DAY_OF_MONTH));
            createDate.setHour(now.get(Calendar.HOUR_OF_DAY));
            createDate.setMinute(now.get(Calendar.MINUTE));
            createDate.setSecond(now.get(Calendar.SECOND));

            header.setCreationDateTime(createDate);
        }

        catch (DatatypeConfigurationException e)
        {
            e.printStackTrace();
        }

        Holder<HeaderDataType> headerHolder = new Holder(header);

        return headerHolder;
    }

    /*
     * Main
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        NotificationService client = new NotificationService();

        System.out.println("NotificationService...");

        // Service Endpoint;
        String serviceURL = "http://notificationdev.bosptc.intuit.net/ias/notification/public";
        //String serviceURL = "https://ntfsvce2e.ptc.intuit.com/ias/notification/public";
        //String serviceURL = "https://notification.sdg.ie.intuit.com/ias/notification/public";
        String namespace = "http://www.intuit.com/ias/notification/pub/wsdl";
        String serviceName = "NotificationService";

        client.testService(serviceURL, namespace, serviceName);
    }
}
