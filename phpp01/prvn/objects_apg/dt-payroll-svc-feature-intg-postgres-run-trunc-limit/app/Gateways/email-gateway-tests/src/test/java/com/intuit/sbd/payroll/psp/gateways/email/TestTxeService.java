package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.sbd.payroll.psp.common.utils.EmailEtConfigOverride;

import com.intuit.sbd.payroll.psp.common.utils.OINPServicesConfig;
import com.intuit.sbd.payroll.psp.domain.EventEmailParamTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;
import com.intuit.sbd.payroll.psp.emailsender.*;
import com.intuit.sbd.payroll.psp.emailsender.AttributeDataType;
import com.intuit.sbd.payroll.psp.emailsender.BasicContentDataType;
import com.intuit.sbd.payroll.psp.emailsender.BasicRecipientDataType;
import com.intuit.sbd.payroll.psp.emailsender.BasicSenderDataType;
import com.intuit.sbd.payroll.psp.emailsender.ContentFormatDataType;
import com.intuit.sbd.payroll.psp.emailsender.NotificationDataType;
import com.intuit.sbd.payroll.psp.emailsender.NotificationTypeDataType;
import com.intuit.sbd.payroll.psp.emailsender.SendRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.model.ExactTargetResult;
import com.intuit.sbd.payroll.psp.emailsender.model.ExactTargetResults;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSenderService;

import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 6/4/12
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestTxeService {
    private static final String sfSourceSystem = "PSP";
    private static final String sfProviderSystem = "PSPEmailGateway";

    private final static String mSenderId = EmailUtils.getConfig("iasns-senderid");
    private final static String mSenderName = EmailUtils.getConfig("iasns-sendername");
    private final static String mSenderAddress = "shariq_mahmood@intuit.com";

    private final static ApplicationContext applicationContext = new AnnotationConfigApplicationContext(EmailConfig.class, EmailEtConfigOverride.class, OINPServicesConfig.class);

    private String mContentId = "";


    @SuppressWarnings("unchecked")
    private void testService(EventEmailTemplateTypeCode pTemplateType) {
        try {


            System.out.println("Send request...");

            mContentId = pTemplateType.toString();


            //Get Request
            EmailRequest req = getEmailRequest();

            EmailSenderService emailSenderService = applicationContext.getBean(EmailSenderService.class);


            //Send Request
            EmailResponse response = emailSenderService.sendMail(req);


            displayResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing EmailNotificationService instance.", e);
        }
    }


    protected EmailRequest getEmailRequest() {
        System.out.println("Getting body...");

        // Construct the Send request
        SendRequest request = new SendRequest();


        NotificationDataType notification = new NotificationDataType();

        notification.setNotificationType(NotificationTypeDataType.EMAIL);
        notification.setDeliveryType(getDeliveryType());
        notification.setSenderProfile(getSenderProfile());
        notification.setContentProfile(getContentProfile());
        notification.setDestinations(getDestinations());

        request.setNotification(notification);
        // Request version
        request.setVersion("1.0");


        return EmailRequest.builder().sendRequest(request).emailStrategyType(EmailStrategyType.ExactTarget).build();
    }

    /*
     * Generates DeliveryType
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.DeliveryType getDeliveryType() {

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


    private NotificationDataType.ContentProfile getContentProfile() {
        System.out.println("Getting content profile...");


        NotificationDataType.ContentProfile contentProfile = new NotificationDataType.ContentProfile();

        // Content
        BasicContentDataType content = new BasicContentDataType();

        // ContentProviderId - Optional
        content.setContentProviderId(sfProviderSystem);

        // Content Subject - Optional
        content.setSubject("");

        // ContentId - Required
        content.setContentId(mContentId);

        contentProfile.setContent(content);

        System.out.println("   Content ID: " + mContentId);


        return contentProfile;
    }

    private NotificationDataType.Destinations getDestinations() {
        System.out.println("Getting destinations...");


        NotificationDataType.Destinations destinations = new NotificationDataType.Destinations();

        List<NotificationDataType.Destinations.Destination> destList = destinations.getDestination();
        int errCount = 0;

        destList.add(getDestination("Company123", "Ankit Agarwal", "ankit_agarwal@intuit.com"));
        destList.add(getDestination("Company456", "Ankit Agarwal", "ankit_agarwal@intuit.com"));

        return destinations;
    }

    protected NotificationDataType.Destinations.Destination getDestination(String pCompanyId, String pContactName, String pContactEmailAddress) {

        System.out.println("Getting destination...");


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


        // Template Attributes
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
    private void displayResponse(EmailResponse response) {
        System.out.println("==================================");
        System.out.println("Response");
        System.out.println("==================================");

        if (response == null) {
            System.out.println("Response is null");
            return;
        }


        if (response.getStatus() != HttpStatus.SC_OK) {
            System.out.println("Error:" + response.getResponseBody());
            return;
        } else {
            ExactTargetResults exactTargetResults = response.getResult();
            
            System.out.println("Email Sent Sucessfully!");

            for (ExactTargetResult result : exactTargetResults.getResult()) {

                String recipientId = result.getRecipientId();

                System.out.println("## "+ recipientId + " ##" );

            }

        }


    }


    /*
     * Main
     */


    public static void main(String[] args) {
        try {
            TestTxeService client = new TestTxeService();

            System.out.println("NotificationService...");

            client.testService(EventEmailTemplateTypeCode.DDSignupConfirmation);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
