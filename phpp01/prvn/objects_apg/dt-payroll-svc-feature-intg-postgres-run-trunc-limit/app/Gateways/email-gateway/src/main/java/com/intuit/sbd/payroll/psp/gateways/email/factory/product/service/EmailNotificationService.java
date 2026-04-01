package com.intuit.sbd.payroll.psp.gateways.email.factory.product.service;

import com.intuit.ias.notification.pub.wsdl.NotificationPort;
import com.intuit.ias.notification.pub.wsdl.Fault;
import com.intuit.ias.notification.pub.xsd.SendResponse;
import com.intuit.ias.notification.pub.xsd.SendRequest;
import com.intuit.ias.notification.pub.xsd.NotificationDataType;
import com.intuit.ias.notification.pub.xsd.NotificationTypeDataType;
import com.intuit.ias.notification.pub.xsd.BasicSenderDataType;
import com.intuit.ias.notification.pub.xsd.BasicContentDataType;
import com.intuit.ias.notification.pub.xsd.AttributeDataType;
import com.intuit.ias.notification.pub.xsd.BasicRecipientDataType;
import com.intuit.ias.notification.pub.xsd.ContentFormatDataType;
import com.intuit.ias.common.xsd.HeaderDataType;
import com.intuit.ias.common.xsd.ErrorDataType;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.xml.ws.Holder;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 20, 2008
 * Time: 9:32:28 PM
 */
public class EmailNotificationService {
    private static final SpcfLogger sfLogger = Application.getLogger(EmailNotificationService.class);
    private static final String sfSourceSystem = "PSP";
    private static final String sfProviderSystem = "PSPEmailGateway";

    private final NotificationPort mNotificationPort;
    private final String mSenderId; // controlled value (specified by IAS on a per-client basis)
    private String mSenderName = null;
    private String mSenderAddress = null;

    public EmailNotificationService(NotificationPort pNotificationPort, String pSenderId, String pSenderName, String pSenderAddress) {
        mNotificationPort = pNotificationPort;
        mSenderId = pSenderId;
        mSenderName = pSenderName;
        mSenderAddress = pSenderAddress;
    }

    /*
     * Generates DeliveryType
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.DeliveryType  getDeliveryType() {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting delivery type...");
        }

        NotificationDataType.DeliveryType deliveryType = new NotificationDataType.DeliveryType();

        deliveryType.setImmediate(new NotificationDataType.DeliveryType.Immediate());

        return deliveryType;
    }

    /*
     * Generates SenderProfile
     */
    @SuppressWarnings("unchecked")
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

    /*
     * Generates ContentProfile
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.ContentProfile getContentProfile(EventEmailTemplate pTemplate) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting content profile...");
        }

        NotificationDataType.ContentProfile contentProfile = new NotificationDataType.ContentProfile();

        // Content
        BasicContentDataType content = new BasicContentDataType();

        // ContentProviderId - Optional
        content.setContentProviderId(sfProviderSystem);

        // Content Subject - Optional
        //content.setSubject("");

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

    /*
     * Generates Destinations
     */
    @SuppressWarnings("unchecked")
    protected NotificationDataType.Destinations getDestinations(List<IEventEmail> pEventEmailList) {
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

    /*
     * Generates Destination
     */
    @SuppressWarnings("unchecked")
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
                destination.setFormat(ContentFormatDataType.TEXT);
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

    /*
     * Gets SOAP Body
     */
    @SuppressWarnings("unchecked")
    protected SendRequest getSOAPBody(EventEmailTemplate pTemplate, List<IEventEmail> pEventEmailList) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting SOAP body...");
        }

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
        notification.setContentProfile(getContentProfile(pTemplate));

        // 5. Destinations (attributes here apply to individual recipients)
        notification.setDestinations(getDestinations(pEventEmailList));

        // Add Notification to SendRequest
        request.setNotification(notification);

        return request;
    }


    /*
     * Gets SOAP Header
     */
    @SuppressWarnings("unchecked")
    protected HeaderDataType getSOAPHeader() {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("Getting SOAP header...");
        }

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
            sfLogger.error("Error configuring SOAP header. ", e);
        }

        return header;
    }

    /*
     * Process SOAP repsonse
     *
     * PSRV003705 - Multiple emails (added pEmailBatch to only process errors for these emails instead of entire template)
     */
    @SuppressWarnings("unchecked")
    protected void processResponse(SendResponse response, EventEmailTemplate pTemplate, String pServiceTransactionId, List<IEventEmail> pEmailBatch) {
        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("===========================================");
            sfLogger.debug("Processing notification service response...");
            sfLogger.debug("===========================================");
        }

        if (response == null) {
            String errStr = String.format("Service error (response is null) [Service Transaction ID: %s]", pServiceTransactionId);

            pTemplate.serviceFault(errStr, pEmailBatch);

            throw new EmailServiceException("IAS Notification Service is not responding to requests. A request " +
                    "was made to send email, but a null response was received " +
                    "[Service Transaction ID: " + pServiceTransactionId + "]");
        }

        ErrorDataType error = response.getError();

        if ((error != null) && !error.getCategory().equalsIgnoreCase("BUSINESS_LOGIC")) {
            //
            // SYSTEM and INPUT errors are handled here (request level failures)
            //

            if (error.getCategory().equalsIgnoreCase("INPUT")) {
                //
                // Retry will *not* be attempted for each destination
                //
                String errStr = String.format("Service input error (%s) [Service Transaction ID: %s]", error.getDescription(), pServiceTransactionId);

                pTemplate.failedValidation(errStr, pEmailBatch);

                throw new EmailServiceException(processError(error, null, pServiceTransactionId)); // disallows continue
            } else { // SYSTEM
                //
                // Retry will be attempted for each destination
                //
                String errStr = String.format("Service system error (%s) [Service Transaction ID: %s]", error.getDescription(), pServiceTransactionId);

                pTemplate.serviceReturned(errStr, pEmailBatch);

                throw new EmailProcessingException(processError(error, null, pServiceTransactionId)); // allows continue
            }
        } else {
            //
            // error == null (no errors) or BUSINESS_LOGIC errors are handled here (destination level failures)
            //

            if (sfLogger.isDebugEnabled()) {
                sfLogger.debug("   ServiceTransactionId: " + pServiceTransactionId);
            }

            List<SendResponse.Result> results = response.getResult();

            for (SendResponse.Result result : results) {
                String recipientId = result.getRecipientId();

                if (sfLogger.isDebugEnabled()) {
                    sfLogger.debug("   RecipientId: " + recipientId);
                }

                if (result.getSuccess() != null) {
                    if (sfLogger.isDebugEnabled()) {
                        sfLogger.debug("   Success");
                    }

                    pTemplate.emailSent(recipientId);
                } else {
                    if (sfLogger.isDebugEnabled()) {
                        sfLogger.debug("   Failed");
                    }

                    String err = processError(result.getError(), recipientId, pServiceTransactionId);

                    if (recipientId != null) {
                        //
                        // Retry will not be attempted for this destination
                        //
                        if (result.getError() != null && result.getError().getDescription() != null &&
                                "SEND-NTS-31010".equals(result.getError().getCode()) &&
                                result.getError().getDescription().contains("Error Code: 24")) {

                            pTemplate.failedWithListDetectiveError(recipientId, result.getError().getCode(), result.getError().getDescription());

                        } else {
                            pTemplate.failedValidation(err, recipientId);
                        }
                    } else {
                        // since we can't associate the error with a recipient, we need to simply report the error
                        // here (identified recipients will be reported/logged elsewhere)
                        sfLogger.error(err);
                    }
                }
            }
        }
    }

    /*
     * Log error
     */
    @SuppressWarnings("unchecked")
    protected String processError(ErrorDataType pError, String pRecipientId, String pServiceTransactionId) {
        StringBuilder err = new StringBuilder();

        err.append(EmailUtils.sfNewLine);

        if (pError != null) {
            err.append("  *** Service Error Details ***");
            err.append(EmailUtils.sfNewLine);

            err.append("  * Service Transaction ID: ");
            err.append(pServiceTransactionId);
            err.append(EmailUtils.sfNewLine);

            err.append("  * Recipient ID:           ");
            err.append((pRecipientId != null) ? pRecipientId : "<unknown>");
            err.append(EmailUtils.sfNewLine);

            err.append("  * Error Category:         ");
            err.append(pError.getCategory());
            err.append(EmailUtils.sfNewLine);

            err.append("  * Error Code:             ");
            err.append(pError.getCode());
            err.append(EmailUtils.sfNewLine);

            err.append("  * Error Description:      ");
            err.append(pError.getDescription());
            err.append(EmailUtils.sfNewLine);

            err.append("  * Error Source:           ");
            err.append(pError.getSource());
            err.append(EmailUtils.sfNewLine);

            err.append("  *****************************");
        } else {
            err.append("An unspecified error has occurred in the IAS Notification Service:");
            err.append(EmailUtils.sfNewLine);
            err.append("  [ Service Transaction ID ] ");
            err.append(pServiceTransactionId);
            err.append(EmailUtils.sfNewLine);
            err.append("  [ Recipient ID ] ");
            err.append((pRecipientId != null) ? pRecipientId : "<unknown>");
        }

        return err.toString();
    }

    protected void logEmailDigest(HeaderDataType pHeader, SendRequest pBody) {
        //
        // PSRV003631 - Log a digest of email id's so we can detect redundant emails
        //

        StringBuilder digest = new StringBuilder();

        for (NotificationDataType.Destinations.Destination dest : pBody.getNotification().getDestinations().getDestination()) {
            digest.append(String.format("[Txn id: %s, Rec id: %s]%n", pHeader.getTransactionId(), dest.getRecipientId()));
        }

        if (digest.length() > 0) {
            sfLogger.info(String.format("%n<begin email digest>%n%s<end email digest>", digest.toString()));
        }
    }

    @SuppressWarnings("unchecked")
    public void sendEmail(EventEmailTemplate pTemplate) throws Exception {
        // This routine will handle all error states for the individual email events, so unless the routine
        // returns an EmailServiceException, we need to commit the transction when this method returns.

        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("================================================");
            sfLogger.debug("Sending email request to notification service...");
            sfLogger.debug("================================================");
        }

        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        String curTransactionId = "<unknown>";
        List<IEventEmail> curEmailBatch = null; // PSRV003705 - Multiple emails

        try {
            for (List<IEventEmail> emailBatch : pTemplate.getRecipientsToTransmit()) {
                if (emailBatch.isEmpty()) continue;

                curTransactionId = "<unknown>";
                curEmailBatch = emailBatch;

                HeaderDataType header = getSOAPHeader();

                curTransactionId = header.getTransactionId();

                SendRequest body = getSOAPBody(pTemplate, emailBatch);

                // PSRV003631 - Log id's so we can detect redundant emails
                logEmailDigest(header, body);

                StopWatch timer = StopWatch.startTimer();

                SendResponse response = mNotificationPort.send(new Holder(header), body);
                sfLogger.info("Sending Email Via IAS.Template Id: " + pTemplate.getTemplateId() + " Time Taken : " + timer.stop().getElapsedTimeString() + ". Number of Recipients: " + body.getNotification().getDestinations().getDestination().size());

                processResponse(response, pTemplate, curTransactionId, emailBatch);
            }
        } catch (Fault e) {
            pTemplate.serviceFault("Service fault [Service Transaction ID: " + curTransactionId + "]", curEmailBatch);

            throw new EmailProcessingException("A SOAP fault was returned by the IAS Notification Service " +
                    "[Service Transaction ID: " + curTransactionId + "]", e);
        } catch (EmailServiceException e) {
            throw e;
        } catch (EmailProcessingException e) {
            throw e;
        } catch (Throwable t) {
            if (curEmailBatch != null) {
                pTemplate.serviceFault("Service fault [Service Transaction ID: " + curTransactionId + "]", curEmailBatch);
            }

            throw new EmailServiceException("A service error has occurred in the IAS Notification Service " +
                    "[Service Transaction ID: " + curTransactionId + "]", t);
        }
    }

    }
