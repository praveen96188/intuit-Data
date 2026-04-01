package com.intuit.sbd.payroll.psp.gateways.email.txe;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.emailsender.AttributeDataType;
import com.intuit.sbd.payroll.psp.emailsender.BasicContentDataType;
import com.intuit.sbd.payroll.psp.emailsender.BasicRecipientDataType;
import com.intuit.sbd.payroll.psp.emailsender.BasicSenderDataType;
import com.intuit.sbd.payroll.psp.emailsender.ContentFormatDataType;
import com.intuit.sbd.payroll.psp.emailsender.NotificationDataType;
import com.intuit.sbd.payroll.psp.emailsender.NotificationTypeDataType;
import com.intuit.sbd.payroll.psp.emailsender.SendRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.validator.EmailValidator;

import java.util.List;
import java.util.Map;

public class TxeExactTargetRequestlHelper {

    //This class is used to create ExactTarget request for TXE service.

    private static final SpcfLogger sfLogger = Application.getLogger(TxeExactTargetRequestlHelper.class);
    private static final String sfProviderSystem = "PSPEmailGateway";

    private final static String mSenderId = EmailUtils.getConfig("iasns-senderid");
    private final static String mSenderName = EmailUtils.getConfig("iasns-sendername");
    private final static String mSenderAddress = EmailUtils.getConfig("iasns-senderaddress");

    protected NotificationDataType.DeliveryType getDeliveryType() {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting delivery type...");
        }

        NotificationDataType.DeliveryType deliveryType = new NotificationDataType.DeliveryType();

        deliveryType.setImmediate(new NotificationDataType.DeliveryType.Immediate());
        return deliveryType;
    }

    protected NotificationDataType.SenderProfile getSenderProfile() {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting sender profile...");
            sfLogger.debug("   Sender ID:      " + mSenderId);
            sfLogger.debug("   Sender Name:    " + mSenderName);
            sfLogger.debug("   Sender Address: " + mSenderAddress);
        }

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

    private NotificationDataType.ContentProfile getContentProfile(EventEmailTemplate pTemplate) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting content profile...");
        }

        NotificationDataType.ContentProfile contentProfile = new NotificationDataType.ContentProfile();

        // Content
        BasicContentDataType content = new BasicContentDataType();

        // ContentProviderId - Optional
        content.setContentProviderId(sfProviderSystem);

        // Content Subject - Optional
        content.setSubject("");

        // ContentId - Required
        content.setContentId(pTemplate.getTemplateId().toString());

        contentProfile.setContent(content);

        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("   Template ID: " + pTemplate.getTemplateId().toString());
        }

        if (!pTemplate.getProperties().isEmpty()) {
            // Template Attributes (Caution: these attributes are common to all email recipients in this request)
            NotificationDataType.ContentProfile.TemplateProperties properties =
                    new NotificationDataType.ContentProfile.TemplateProperties();
            List<AttributeDataType> attributes = properties.getAttribute();

            for (Map.Entry<Object, Object> pair : pTemplate.getProperties().entrySet()) {
                String name = pair.getKey().toString();
                String value = pair.getValue().toString().replaceAll("'", "&#8217;");

                // Set Attributes
                AttributeDataType attribute = new AttributeDataType();
                attribute.setName(name);
                attribute.setValue(value);

                if (sfLogger.isDebugEnabled()) {
                    sfLogger.debug("   Template Parameter: " + name + " = " + value);
                }

                attributes.add(attribute);
            }

            contentProfile.setTemplateProperties(properties);
        }

        return contentProfile;
    }

    private NotificationDataType.Destinations getDestinations(List<IEventEmail> pEventEmailList) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting destinations...");
        }

        NotificationDataType.Destinations destinations = new NotificationDataType.Destinations();

        List<NotificationDataType.Destinations.Destination> destList = destinations.getDestination();
        int errCount = 0;

        for (IEventEmail eventEmail : pEventEmailList) {
            try {
                destList.add(getDestination(eventEmail));
            } catch (Exception e) {
                eventEmail.failedValidation(e.getMessage());
                ++errCount;
            }
        }

        // if all emails failed, we have nothing to send, so abort
        if (pEventEmailList.size() == errCount) {
            throw new EmailProcessingException("All emails failed validation, aborting send request.");
        }

        return destinations;
    }

    protected NotificationDataType.Destinations.Destination getDestination(IEventEmail pEventEmail) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting destination...");
        }

        if (pEventEmail == null) {
            throw new RuntimeException("Email property object is null.");
        }

        if ((pEventEmail.getRecipientId() == null) || (pEventEmail.getRecipientId().length() == 0)) {
            throw new RuntimeException("Email recipient id is null or empty.");
        }

        if ((pEventEmail.getRecipientName() == null) || (pEventEmail.getRecipientName().length() == 0)) {
            throw new RuntimeException("Email recipient name is null or empty.");
        }

        if ((pEventEmail.getRecipientEmail() == null) || (pEventEmail.getRecipientEmail().length() == 0)) {
            throw new RuntimeException("Email recipient address is null or empty.");
        }

        EmailValidator emailValidator = EmailValidator.getInstance();

        if (!emailValidator.isValid(pEventEmail.getRecipientEmail())) {
            throw new RuntimeException("Email recipient address not valid.");
        }

        NotificationDataType.Destinations.Destination destination = new NotificationDataType.Destinations.Destination();

        destination.setRecipientId(pEventEmail.getRecipientId());

        BasicRecipientDataType recipient = new BasicRecipientDataType();

        // Recipient name and email address
        if (EmailUtils.isInternlDistributionOnly()) {
            recipient.setName("Company ID " + pEventEmail.getCompanyId());
            recipient.setAddress(EmailUtils.getConfig("internaldistributionlist"));
        } else {
            recipient.setName(pEventEmail.getRecipientName().replaceAll("'", "&#8217;"));
            recipient.setAddress(pEventEmail.getRecipientEmail());
        }

        destination.setRecipient(recipient);

        // Preferred Email Format
        switch (pEventEmail.getPreferredFormat()) {
            case TEXT:
                destination.setFormat(com.intuit.sbd.payroll.psp.emailsender.ContentFormatDataType.TEXT);
                break;
            default: // HTML
                destination.setFormat(ContentFormatDataType.HTML);
                break;
        }

        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("   Recipient ID:      " + pEventEmail.getRecipientId());
            sfLogger.debug("   Recipient Name:    " + pEventEmail.getRecipientName());
            sfLogger.debug("   Recipient Address: " + pEventEmail.getRecipientEmail());
            sfLogger.debug("   Preferred Format:  " + pEventEmail.getPreferredFormat().toString());
        }

        if (!pEventEmail.getProperties().isEmpty()) {
            // Template Attributes
            NotificationDataType.Destinations.Destination.TemplateProperties properties =
                    new NotificationDataType.Destinations.Destination.TemplateProperties();
            List<AttributeDataType> attributes = properties.getAttribute();

            for (Map.Entry<Object, Object> pair : pEventEmail.getProperties().entrySet()) {
                String name = pair.getKey().toString();
                String value = pair.getValue().toString().replaceAll("'", "&#8217;");

                // Set Attributes
                AttributeDataType attribute = new AttributeDataType();
                attribute.setName(name);
                attribute.setValue(value);

                if (sfLogger.isDebugEnabled()) {
                    sfLogger.debug("   Email Parameter:   " + name + " = " + value);
                }

                attributes.add(attribute);
            }

            destination.setTemplateProperties(properties);
        }

        return destination;
    }


    protected EmailRequest getEmailRequest(EventEmailTemplate pTemplate, List<IEventEmail> emailBatch) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting body...");
        }
        // Construct the Send request
        SendRequest request = new SendRequest();


        NotificationDataType notification = new NotificationDataType();

        notification.setNotificationType(NotificationTypeDataType.EMAIL);
        notification.setDeliveryType(getDeliveryType());
        notification.setSenderProfile(getSenderProfile());
        notification.setContentProfile(getContentProfile(pTemplate));
        notification.setDestinations(getDestinations(emailBatch));

        request.setNotification(notification);
        // Request version
        request.setVersion("1.0");


        return EmailRequest.builder().sendRequest(request).emailStrategyType(EmailStrategyType.ExactTarget).build();

    }

}

