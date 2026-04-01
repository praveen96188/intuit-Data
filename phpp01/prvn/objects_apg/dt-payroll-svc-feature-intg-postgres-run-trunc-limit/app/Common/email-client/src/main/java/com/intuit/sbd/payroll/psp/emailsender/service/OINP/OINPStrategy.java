package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.gateway.OINPRestServiceGateway;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventRequest;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSendStrategy;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementation of OINPStrategy
 *
 * @author nramesh1
 */

@Component
public class OINPStrategy extends OINPSendStrategyAbstract implements EmailSendStrategy {

    private OINPRestServiceGateway oinpRestServiceGateway;

    @Autowired
    public OINPStrategy(EmailRequestValidator emailRequestValidator, OINPRequestUtility oinpRequestHelper, OINPRestServiceGateway oinpRestServiceGateway)
    {
        super(emailRequestValidator, oinpRequestHelper);
        this.oinpRestServiceGateway = oinpRestServiceGateway;
    }

    @Override
    public EmailStrategyType getSupportedEmailStrategyType() {
        return EmailStrategyType.OINP;
    }

    @Override
    protected void preProcess(EmailRequest emailRequest) {

        super.preProcess(emailRequest);
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
        emailRequest.setMediaType("");
    }

    @Override
    protected EmailResponse process(EmailRequest emailRequest) throws Exception {

        // create OINPEvent
        OINPEventRequest event = createOINPEvent(emailRequest);

        // publish event
        return oinpRestServiceGateway.publishEventToOINP(event);
    }

    @Override
    protected void postProcess(EmailRequest emailRequest, EmailResponse response) throws Exception {
        super.postProcess(emailRequest, response);
        emailRequest.setMediaType("");
    }

    @Override
    protected OINPEventRequest createOINPEvent(EmailRequest emailRequest) {
        return oinpRequestUtility.createOINPRequest(emailRequest);
    }
}
