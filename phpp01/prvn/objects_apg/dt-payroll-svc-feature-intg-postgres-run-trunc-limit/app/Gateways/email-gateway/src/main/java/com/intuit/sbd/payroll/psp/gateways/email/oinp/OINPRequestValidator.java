package com.intuit.sbd.payroll.psp.gateways.email.oinp;

import com.intuit.sbd.payroll.psp.common.utils.OINPServicesConfig;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import org.apache.commons.lang3.Validate;
import org.apache.commons.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OINPRequestValidator {

    private OINPServicesConfig oinpServicesConfig ;

    @Autowired
    public OINPRequestValidator(OINPServicesConfig oinpServicesConfig)
    {
        this.oinpServicesConfig = oinpServicesConfig;
    }

    public boolean validateTemplate(String templateId) {

        try {

            Validate.notBlank(templateId,"Bad request, Template id is Null or empty");
            Validate.isTrue(oinpServicesConfig.getTemplates().containsKey(templateId), "Bad Request, templateId is not valid");
            Validate.notBlank(oinpServicesConfig.getTemplateName(templateId), "Bad Request, template-name is Null or empty");
            Validate.notBlank(oinpServicesConfig.getTemplateObjectType(templateId), "Bad Request, template object-type is Null or empty");

            return true;
        } catch (Exception e) {

            throw new EmailProcessingException("OINP: Template ID: " + templateId + " failed Validation", e);
        }
    }

    public boolean validateRecipient(IEventEmail pEventEmail) {

        if (pEventEmail == null) {
            throw new RuntimeException("Email property object is null.");
        }

        if ((pEventEmail.getRecipientEmail() == null) || (pEventEmail.getRecipientEmail().length() == 0)) {
            throw new RuntimeException("Email recipient address is null or empty.");
        }

        EmailValidator emailValidator = EmailValidator.getInstance();

        if (!emailValidator.isValid(pEventEmail.getRecipientEmail())) {
            throw new RuntimeException("Email recipient address not valid.");
        }

        return true;
    }

    public boolean validateSender(String senderAddress) {

        if ((senderAddress == null) || (senderAddress.length() == 0)) {
            throw new RuntimeException("Email sender address is null or empty.");
        }

        EmailValidator emailValidator = EmailValidator.getInstance();

        if (!emailValidator.isValid(senderAddress)) {
            throw new RuntimeException("Email sender address not valid.");
        }

        return true;
    }
}
