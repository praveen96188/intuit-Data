package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPKafkaResponse;
import com.intuit.sbd.payroll.psp.emailsender.gateway.OINPKafkaServiceGateway;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventRequest;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPKafkaRequest;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementation of OINPBulkKafkaStrategy
 *
 * @author nramesh1
 */

@Component
public class OINPBulkKafkaStrategy extends OINPSendStrategyAbstract {

    private OINPKafkaServiceGateway oinpKafkaServiceGateway;

    @Autowired
    public OINPBulkKafkaStrategy(EmailRequestValidator emailRequestValidator, OINPRequestUtility oinpRequestHelper, OINPKafkaServiceGateway oinpKafkaServiceGateway)
    {
        super(emailRequestValidator, oinpRequestHelper);
        this.oinpKafkaServiceGateway = oinpKafkaServiceGateway;
    }

    @Override
    public EmailStrategyType getSupportedEmailStrategyType() {
        return EmailStrategyType.OINPBulkKafka;
    }

    @Override
    protected void preProcess(EmailRequest emailRequest) {

        super.preProcess(emailRequest);
    }

    @Override
    protected OINPKafkaResponse process(EmailRequest emailRequest) throws Exception {

        OINPKafkaRequest request = oinpRequestUtility.createOINPKafkaRequest(emailRequest);

        // publish event
        return oinpKafkaServiceGateway.publishEventViaKafka(request);
    }

    @Override
    protected void postProcess(EmailRequest emailRequest, EmailResponse response) throws Exception {
        super.postProcess(emailRequest, response);
    }

    @Override
    protected OINPEventRequest createOINPEvent(EmailRequest emailRequest) {
        return oinpRequestUtility.createOINPRequest(emailRequest);
    }
}
