package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.ias.common.xsd.ErrorDataType;
import com.intuit.ias.common.xsd.HeaderDataType;
import com.intuit.ias.notification.pub.wsdl.*;
import com.intuit.ias.notification.pub.xsd.*;
import com.intuit.sbd.payroll.psp.common.utils.PspCertificateManager;
import com.intuit.sbd.payroll.psp.domain.EventEmailParamTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 6/4/12
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestNotificationService {
    private static final String KEY_STORE_ALIAS = "email.gateway";
    private static final String SSL_SOCKET_FACTORY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";
    private static final String sfSourceSystem = "PSP";
    private static final String sfProviderSystem = "PSPEmailGateway";

    private final String mSenderId = EmailUtils.getConfig("iasns-senderid"); // controlled value (specified by IAS on a per-client basis)
    private String mSenderName = EmailUtils.getConfig("iasns-sendername");
    private String mSenderAddress = "ankit_agarwal@intuit.com"; //EmailUtils.getConfig("iasns-senderaddress");

    private String mContentId = "";

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
    private void testService(EventEmailTemplateTypeCode pTemplateType) {
        try {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump","true");

            String serviceUrl = EmailUtils.getConfig("iasns-serviceurl");
            URL wsdlLocation = new URL(serviceUrl + "?wsdl");
            QName qName = new QName(EmailUtils.getConfig("iasns-namespace"), EmailUtils.getConfig("iasns-servicename"));
            Service notificationService;

            try {
                notificationService = new com.intuit.ias.notification.pub.wsdl.NotificationService(wsdlLocation, qName);
            } catch (Exception e) {
                String wsdlPath = EmailUtils.getConfig("iasns-servicepath");
                notificationService = new com.intuit.ias.notification.pub.wsdl.NotificationService(new URL("file:///" + wsdlPath), qName);
            }

            NotificationPort notificationPort = notificationService.getPort(NotificationPort.class);
            Map<String, Object> requestContext = ((BindingProvider)notificationPort).getRequestContext();

            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceUrl);

            if (serviceUrl.startsWith("https:")) {
                requestContext.put(SSL_SOCKET_FACTORY, PspCertificateManager.getSSLSocketFactory(KEY_STORE_ALIAS));
            }

            System.out.println("Send request...");

            mContentId = pTemplateType.toString();

            SendResponse response = notificationPort.send(new Holder(getSOAPHeader()), getRequest());

            displayResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing EmailNotificationService instance.", e);
        }
    }

    /*
     * Gets SOAP Body
     */
    @SuppressWarnings("unchecked")
    protected SendRequest getRequest() {
        System.out.println("Getting SOAP body...");

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

        // 4. ContentProfile (attributes here apply to all recipients)
        notification.setContentProfile(getContentProfile());

        // 5. Destinations (attributes here apply to individual recipients)
        notification.setDestinations(getDestinations());

        // Add Notification to SendRequest
        request.setNotification(notification);

        return request;
    }

    /*
     * Generates DeliveryType
     */
    @SuppressWarnings("unchecked")
    private NotificationDataType.DeliveryType getDeliveryType() {
        System.out.println("Getting delivery type...");

        NotificationDataType.DeliveryType deliveryType = new NotificationDataType.DeliveryType();

        deliveryType.setImmediate(new NotificationDataType.DeliveryType.Immediate());

        return deliveryType;
    }

    /*
     * Generates SenderProfile
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.SenderProfile getSenderProfile() {
        System.out.println("Getting sender profile...");
        System.out.println("   Sender ID:      " + mSenderId);
        System.out.println("   Sender Name:    " + mSenderName);
        System.out.println("   Sender Address: " + mSenderAddress);

        NotificationDataType.SenderProfile senderProfile = new NotificationDataType.SenderProfile();

        // SenderId
        senderProfile.setSenderId(mSenderId);

        // Sender
        BasicSenderDataType sender = new BasicSenderDataType();

        sender.setName(mSenderName);
        sender.setAddress(mSenderAddress);

        senderProfile.setSender(sender);

        return senderProfile;
    }

    /*
     * Generates ContentProfile
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.ContentProfile getContentProfile() {
        System.out.println("Getting content profile...");

        NotificationDataType.ContentProfile contentProfile = new NotificationDataType.ContentProfile();

        // Content
        BasicContentDataType content = new BasicContentDataType();

        // ContentProviderId - Optional
        content.setContentProviderId(sfProviderSystem);

        // Content Subject - Optional
        //content.setSubject("");

        // ContentId - Required
        content.setContentId(mContentId);

        contentProfile.setContent(content);

        System.out.println("   Content ID: " + mContentId);

//        if (!pTemplate.getProperties().isEmpty()) {
//            // Template Attributes (Caution: these attributes are common to all email recipients in this request)
//            NotificationDataType.ContentProfile.TemplateProperties properties =
//                    new NotificationDataType.ContentProfile.TemplateProperties();
//            List<AttributeDataType> attributes = properties.getAttribute();
//
//            for (Map.Entry<Object, Object> pair : pTemplate.getProperties().entrySet()) {
//                String name = pair.getKey().toString();
//                String value = pair.getValue().toString();
//
//                // Set Attributes
//                AttributeDataType attribute = new AttributeDataType();
//                attribute.setName(name);
//                attribute.setValue(value);
//
//                if (sfLogger.isDebugEnabled()) {
//                    sfLogger.debug("   Template Parameter: " + name + " = " + value);
//                }
//
//                attributes.add(attribute);
//            }
//
//            contentProfile.setTemplateProperties(properties);
//        }

        return contentProfile;
    }

    /*
     * Generates Destinations
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.Destinations getDestinations() {
        System.out.println("Getting destinations...");

        NotificationDataType.Destinations destinations = new NotificationDataType.Destinations();

        List<NotificationDataType.Destinations.Destination> destList = destinations.getDestination();

        destList.add(getDestination("Company123", "Ankit Agarwal", "ankit_agarwal@intuit.com"));
        destList.add(getDestination("Company456", "Ankit Agarwal", "ankit_agarwal@intuit.com"));

        return destinations;
    }

    /*
     * Generates Destination
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.Destinations.Destination getDestination(String pCompanyId, String pContactName, String pContactEmailAddress) {
        String recipientId = String.format("RECIPIENT-ID-%s-%s", pCompanyId, UUID.randomUUID().toString());

        System.out.println("Getting destination...");

        NotificationDataType.Destinations.Destination destination = new NotificationDataType.Destinations.Destination();

        destination.setRecipientId(recipientId);

        BasicRecipientDataType recipient = new BasicRecipientDataType();

        // Recipient name and email address
        recipient.setName(pContactName);
        recipient.setAddress(pContactEmailAddress);

        destination.setRecipient(recipient);
        destination.setFormat(ContentFormatDataType.HTML);

        System.out.println("   Recipient ID:      " + recipientId);
        System.out.println("   Recipient Name:    " + pCompanyId);
        System.out.println("   Recipient Address: " + EmailUtils.getConfig("internaldistributionlist"));
        System.out.println("   Preferred Format:  " + ContentFormatDataType.HTML.toString());

        //
        // Set up the common mail params for all PSP emails
        //

        NotificationDataType.Destinations.Destination.TemplateProperties properties =
                new NotificationDataType.Destinations.Destination.TemplateProperties();
        List<AttributeDataType> attributes = properties.getAttribute();
        AttributeDataType attribute;
        String name, value;

        name = EventEmailParamTypeCode.PayrollAdminFirstName.toString();
        value = "Ken";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute = new AttributeDataType();
        attribute.setName(name);
        attribute.setValue(value);
        attributes.add(attribute);

        name = EventEmailParamTypeCode.PayrollAdminLastName.toString();
        value = "Paul";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute = new AttributeDataType();
        attribute.setName(name);
        attribute.setValue(value);
        attributes.add(attribute);

        name = EventEmailParamTypeCode.PayrollAdminEmail.toString();
        value = "ankit_agarwal@intuit.com";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute = new AttributeDataType();
        attribute.setName(name);
        attribute.setValue(value);
        attributes.add(attribute);

        name = EventEmailParamTypeCode.CompanyLegalName.toString();
        value = "Joe's Garage";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute = new AttributeDataType();
        attribute.setName(name);
        attribute.setValue(value);
        attributes.add(attribute);

        name = EventEmailParamTypeCode.SourcePayrollSystem.toString();
        value = "QBDT";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute = new AttributeDataType();
        attribute.setName(name);
        attribute.setValue(value);
        attributes.add(attribute);

        destination.setTemplateProperties(properties);

        return destination;
    }

    /*
     * Displays SOAP repsonse
     */
    @SuppressWarnings("unchecked")
    private void displayResponse(SendResponse response) {
        System.out.println("==================================");
        System.out.println("Response");
        System.out.println("==================================");

        if (response == null) {
            System.out.println("Response is null");
            return;
        }

        ErrorDataType error = response.getError();

        if (error != null) {
            displayError(error);
            return;
        }

        List<SendResponse.Result> results = response.getResult();

        for (SendResponse.Result result : results) {
            System.out.println("   RecipientId: " + result.getRecipientId());

            if (result.getSuccess() != null) {
                System.out.println("   Success");
            } else {
                displayError(result.getError());
            }
        }
    }

    /*
     * Displays error
     */
    @SuppressWarnings("unchecked")
    private void displayError(ErrorDataType error) {
        if (error != null) {
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
    protected HeaderDataType getSOAPHeader() {
        System.out.println("Getting SOAP header...");

        HeaderDataType header = new HeaderDataType();
        header.setServiceVersion("1.0");
        header.setTransactionId("PSP-" + UUID.randomUUID().toString());
        header.setSourceSystem(sfSourceSystem);
        header.setCallerSystem(sfProviderSystem);

        try {
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
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Error configuring SOAP header. ", e);
        }

        return header;
    }

    /*
     * Main
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            TestNotificationService client = new TestNotificationService();

            System.out.println("NotificationService...");

            client.testService(EventEmailTemplateTypeCode.DDSignupConfirmation);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
