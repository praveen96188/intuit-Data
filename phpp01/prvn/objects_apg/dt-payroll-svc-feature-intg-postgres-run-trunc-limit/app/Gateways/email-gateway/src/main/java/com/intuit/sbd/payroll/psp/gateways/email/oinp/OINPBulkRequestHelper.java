package com.intuit.sbd.payroll.psp.gateways.email.oinp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.OINPServicesConfig;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.service.OINP.OINPRequestUtility;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OINPBulkRequestHelper {

    //This class is used to create Email request for sending events via OINP service (Kafka).

    private final SpcfLogger sfLogger = Application.getLogger(OINPBulkRequestHelper.class);
    private final String mSenderAddress = EmailUtils.getConfig("iasns-senderaddress");

    private OINPServicesConfig oinpServicesConfig ;
    private OINPRequestValidator oinpRequestValidator;

    @Autowired
    public OINPBulkRequestHelper(OINPServicesConfig oinpServicesConfig, OINPRequestValidator oinpRequestValidator)
    {
        this.oinpServicesConfig = oinpServicesConfig;
        this.oinpRequestValidator = oinpRequestValidator;
    }

    public Map<String, Object> getCommonTemplateAttributes(EventEmailTemplate pTemplate) {

        Map<String, Object> commonAttributes = new HashMap<>();

        if (!pTemplate.getProperties().isEmpty()) {
            // Template Attributes (Caution: these attributes are common to all email recipients in this request)

            for (Map.Entry<Object, Object> pair : pTemplate.getProperties().entrySet()) {
                String name = pair.getKey().toString();
                String value = pair.getValue().toString().replaceAll("'", "&#8217;");

                if (sfLogger.isDebugEnabled()) {
                    sfLogger.debug("   Template Parameter: " + name + " = " + value);
                }
                commonAttributes.put(name,value);
            }
        }

        return commonAttributes;
    }

    protected EmailRequest getEmailRequest(String templateId, IEventEmail emailEvent, Map<String,Object> commonAttributes) {

        try {

            oinpRequestValidator.validateRecipient(emailEvent);

            oinpRequestValidator.validateSender(mSenderAddress);

            String intuit_tid = "PSP-" + SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");

            EmailRequest.EmailRequestBuilder builder = EmailRequest.builder()
                    .templateName(oinpServicesConfig.getTemplateName(templateId))
                    .templateObjectType(oinpServicesConfig.getTemplateObjectType(templateId))
                    .templateAttributes(getTemplateAttributes(emailEvent, commonAttributes))
                    .intuitTid(intuit_tid)
                    .emailStrategyType(EmailStrategyType.OINPBulkKafka);

            return builder.build();

        } catch (Exception e) {
            emailEvent.failedValidation(e.getMessage());

            throw new EmailProcessingException("OINP: validation failed for event.", e);
        }
    }

    private Map<String, Object> getTemplateAttributes(IEventEmail emailEvent, Map<String,Object> commonAttributes) {

        Map<String,Object> attributes = new HashMap<>();

        // add fromEmail
        attributes.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, mSenderAddress);

        // add toEmail
        if (EmailUtils.isInternlDistributionOnly()) {
            attributes.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, EmailUtils.getConfig("internaldistributionlist"));
        } else {
            attributes.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, emailEvent.getRecipientEmail());
        }

        // add common template Attributes
        attributes.putAll(commonAttributes);

        // add unique attributes
        attributes.putAll(getUniqueEventAttributes(emailEvent));

        return attributes;

    }

    private Map<String,Object> getUniqueEventAttributes(IEventEmail pEventEmail) {

        Map<String, Object> uniqueAttributes = new HashMap<>();

        if (!pEventEmail.getProperties().isEmpty()) {
            // Template Attributes

            for (Map.Entry<Object, Object> pair : pEventEmail.getProperties().entrySet()) {
                String name = pair.getKey().toString();
                String value = pair.getValue().toString().replaceAll("'", "&#8217;");

                // Set Attributes
                if (sfLogger.isDebugEnabled()) {
                    sfLogger.debug("   Email Parameter:   " + name + " = " + value);
                }
                uniqueAttributes.put(name,value);
            }
        }
        return uniqueAttributes;
    }

}
