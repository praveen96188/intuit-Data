package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventRequest;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSendStrategyAbstract;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OINPSendStrategyAbstract extends EmailSendStrategyAbstract {

    protected OINPRequestUtility oinpRequestUtility;

    public OINPSendStrategyAbstract(EmailRequestValidator emailrequestValidator, OINPRequestUtility oinpRequestUtility) {
        super(null,null, emailrequestValidator,null);
        this.oinpRequestUtility = oinpRequestUtility;
    }

    protected abstract OINPEventRequest createOINPEvent(EmailRequest emailRequest);

    @Override
    protected void preProcess(EmailRequest emailRequest) {
        emailRequestValidator.validateOINPTemplateProperties(emailRequest.getTemplateName(), emailRequest.getTemplateObjectType(), oinpRequestUtility.getSourceServiceName());
    }


}
