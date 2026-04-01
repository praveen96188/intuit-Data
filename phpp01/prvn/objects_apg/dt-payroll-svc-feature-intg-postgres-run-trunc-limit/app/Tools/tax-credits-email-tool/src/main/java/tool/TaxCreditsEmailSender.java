package tool;

import com.intuit.ias.common.xsd.ErrorDataType;
import com.intuit.ias.common.xsd.HeaderDataType;
import com.intuit.ias.notification.pub.wsdl.NotificationPort;
import com.intuit.ias.notification.pub.wsdl.NotificationService;
import com.intuit.ias.notification.pub.xsd.*;
import org.apache.commons.lang.WordUtils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: dweinberg
 * Date: Apr 20, 2010
 * Time: 3:29:36 PM
 */
public class TaxCreditsEmailSender {

    private static final String sfSourceSystem = "PSP";
    private static final String sfProviderSystem = "PSPEmailGateway";

    private final NotificationPort mNotificationPort;
    private final String mSenderId; // controlled value (specified by IAS on a per-client basis)
    private String mSenderName = null;
    private String mSenderAddress = null;

    public TaxCreditsEmailSender(NotificationPort mNotificationPort, String mSenderId, String mSenderName, String mSenderAddress) {
        this.mNotificationPort = mNotificationPort;
        this.mSenderId = mSenderId;
        this.mSenderName = mSenderName;
        this.mSenderAddress = mSenderAddress;
    }

    @SuppressWarnings({"unchecked"}) 
    public void sendEmail(TaxCreditsEmail email, EmailTrigger trigger) {
        try {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump","true");

            String currentTransactionId = "<unknown>";

            HeaderDataType header = getSOAPHeader();

            currentTransactionId = header.getTransactionId();

            SendRequest body = getSOAPBody(trigger, email);

//            Binding binding = ((BindingProvider) mNotificationPort).getBinding();
//            List<Handler> handlerList = binding.getHandlerChain();
//            if (handlerList == null) {
//                handlerList = new ArrayList<Handler>();
//            }
//
//            handlerList.add(new SOAPHandler<SOAPMessageContext>(){
//                public Set<QName> getHeaders() {
//                    return null;
//                }
//
//                public boolean handleMessage(SOAPMessageContext context) {
//                    SOAPMessage msg = context.getMessage();
//                    try {
//                        msg.writeTo(System.out);
//                    } catch (SOAPException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return true;
//                }
//
//                public boolean handleFault(SOAPMessageContext context) {
//                    return true;
//                }
//
//                public void close(MessageContext context) {
//
//                }
//            });
//
//            binding.setHandlerChain(handlerList);

            SendResponse response = mNotificationPort.send(new Holder(header), body);

            processResponse(response, currentTransactionId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings("unchecked")
    protected HeaderDataType getSOAPHeader() throws Exception {
        HeaderDataType header = new HeaderDataType();
        header.setServiceVersion("1.0");
        header.setTransactionId("PSP-" + UUID.randomUUID().toString());
        header.setSourceSystem(sfSourceSystem);
        header.setCallerSystem(sfProviderSystem);


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
        return header;
    }

 @SuppressWarnings("unchecked")
    public SendRequest getSOAPBody(EmailTrigger trigger, TaxCreditsEmail email) {
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
        notification.setContentProfile(getContentProfile(trigger));

        // 5. Destinations (attributes here apply to individual recipients)
        notification.setDestinations(getDestinations(email));

        // Add Notification to SendRequest
        request.setNotification(notification);

        return request;
    }

    @SuppressWarnings("unchecked")
    private NotificationDataType.DeliveryType getDeliveryType() {
        NotificationDataType.DeliveryType deliveryType = new NotificationDataType.DeliveryType();

        deliveryType.setImmediate(new NotificationDataType.DeliveryType.Immediate());

        return deliveryType;
    }

    @SuppressWarnings("unchecked")
    protected NotificationDataType.SenderProfile getSenderProfile() {
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

    @SuppressWarnings("unchecked")
    protected NotificationDataType.ContentProfile getContentProfile(EmailTrigger trigger) {
        NotificationDataType.ContentProfile contentProfile = new NotificationDataType.ContentProfile();

        // Content
        BasicContentDataType content = new BasicContentDataType();

        // ContentProviderId - Optional
        content.setContentProviderId(sfProviderSystem);

        // Content Subject - Optional
        //content.setSubject("");

        // ContentId - Required
        content.setContentId( trigger.getTriggerId());

        contentProfile.setContent(content);

        // Template Attributes (Caution: these attributes are common to all email recipients in this request)
        NotificationDataType.ContentProfile.TemplateProperties properties =
                new NotificationDataType.ContentProfile.TemplateProperties();
        List<AttributeDataType> attributes = properties.getAttribute();


        //Must be an attribute here for schema validation
        attributes.add(getAttribute("Unused","Unused"));

        contentProfile.setTemplateProperties(properties);


        return contentProfile;
    }

    @SuppressWarnings("unchecked")
    protected NotificationDataType.Destinations getDestinations(TaxCreditsEmail email) {
        NotificationDataType.Destinations destinations = new NotificationDataType.Destinations();

        List<NotificationDataType.Destinations.Destination> destList = destinations.getDestination();
        destList.add(getDestination(email));

        return destinations;
    }

    @SuppressWarnings("unchecked")
    protected NotificationDataType.Destinations.Destination getDestination(TaxCreditsEmail email) {
        NotificationDataType.Destinations.Destination destination = new NotificationDataType.Destinations.Destination();

        destination.setRecipientId("TaxCreditsRecipient");

        BasicRecipientDataType recipient = new BasicRecipientDataType();

        // Recipient name and email address
        recipient.setName(WordUtils.capitalizeFully(email.getName()));
        recipient.setAddress(email.getEmailAddress());        
        destination.setRecipient(recipient);

        destination.setFormat(ContentFormatDataType.HTML);

        // Template Attributes
        NotificationDataType.Destinations.Destination.TemplateProperties properties =
                new NotificationDataType.Destinations.Destination.TemplateProperties();
        List<AttributeDataType> attributes = properties.getAttribute();


        //prepare attributes
        String firstName = WordUtils.capitalizeFully(email.getFirstName());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        String postMarkDate = sdf.format(email.getPostmarkDate());
        Calendar cal = Calendar.getInstance();
        cal.setTime(email.getPostmarkDate());
        cal.add(Calendar.DATE, -4);
        String postMarkMinusFour = sdf.format(cal.getTime());
        cal.add(Calendar.DATE, -1);
        String postMarkMinusFive = sdf.format(cal.getTime());

        String url;
        if (email.isTc555()) {
            url = "https://www.intuitemployertaxcredits.com/TC555";
        } else {
            url = "https://www.intuitemployertaxcredits.com/TC101";
        }

        // Set Attributes
        attributes.add(getAttribute("CFNAME",firstName));
        attributes.add(getAttribute("POSTMARKDATE",postMarkDate));
        attributes.add(getAttribute("POSTMARKMINUESFIVE",postMarkMinusFive));
        attributes.add(getAttribute("POSTMARKMINUSFOUR",postMarkMinusFour));
        attributes.add(getAttribute("GOTOURL",url));
        attributes.add(getAttribute("SourcePayrollSystem","TXCR"));

        destination.setTemplateProperties(properties);


        return destination;
    }

    protected AttributeDataType getAttribute(String name, String value) {
        AttributeDataType attribute = new AttributeDataType();
        attribute.setName(name);
        attribute.setValue(value);

        return attribute;
    }

    @SuppressWarnings("unchecked")
    private void processResponse(SendResponse response, String pServiceTransactionId) throws Exception {
        if (response == null) {
            throw new Exception("IAS Notification Service is not responding to requests. A request " +
                    "was made to send email, but a null response was received " +
                    "[Service Transaction ID: " + pServiceTransactionId + "]");
        }

        ErrorDataType error = response.getError();

        if ((error != null)) {
            throw new Exception(processError(error, null, pServiceTransactionId));
        }
    }

@SuppressWarnings("unchecked")
    private String processError(ErrorDataType pError, String pRecipientId, String pServiceTransactionId) {
        StringBuffer err = new StringBuffer();

            err.append("\n");

        if (pError != null) {
            err.append("  *** Service Error Details ***");
            err.append("\n");

            err.append("  * Service Transaction ID: ");
            err.append(pServiceTransactionId);
            err.append("\n");

            err.append("  * Recipient ID:           ");
            err.append((pRecipientId != null) ? pRecipientId : "<unknown>");
            err.append("\n");

            err.append("  * Error Category:         ");
            err.append(pError.getCategory());
            err.append("\n");

            err.append("  * Error Code:             ");
            err.append(pError.getCode());
            err.append("\n");

            err.append("  * Error Description:      ");
            err.append(pError.getDescription());
            err.append("\n");

            err.append("  * Error Source:           ");
            err.append(pError.getSource());
            err.append("\n");

            err.append("  *****************************");
        } else {
            err.append("An unspecified error has occurred in the IAS Notification Service:");
            err.append("\n");
            err.append("  [ Service Transaction ID ] ");
            err.append(pServiceTransactionId);
            err.append("\n");
            err.append("  [ Recipient ID ] ");
            err.append((pRecipientId != null) ? pRecipientId : "<unknown>");
        }

        return err.toString();
    }

    public static TaxCreditsEmailSender createInstance() {
        try {
            URL wsdlLocation = new URL("https://ntfsvce2e.ptc.intuit.com/ias/notification/public?wsdl");
            QName qName = new QName("http://www.intuit.com/ias/notification/pub/wsdl", "NotificationService");
            NotificationService notificationService = new NotificationService(wsdlLocation, qName);
            NotificationPort notificationPort = notificationService.getPort(NotificationPort.class);
            return new TaxCreditsEmailSender(notificationPort,
                    "PSP",
                    "Intuit Employer Tax Credit Services",
                    "TaxCredits@intuit.com");
        } catch (Exception e){
            throw new RuntimeException(e);
        }

    }

}
