package com.intuit.sbd.payroll.psp.entity.publisher.payee;

import com.intuit.payroll.api.contractor.model.ContractorCDM;
import com.intuit.payroll.api.contractor.model.PrivilegedContractorCDM;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.AbstractEntityPublisher;
import com.intuit.sbd.payroll.psp.mapper.cdm.CDMMapper;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PayeePublisher extends AbstractEntityPublisher<Payee> {


    private String activeProfile;

    @Autowired
    public PayeePublisher(KafkaSDKPublisher kafkaPublisher, CDMMapper cdmMapper, @Value("${spring.profiles.active}") String activeProfile) {
        super(kafkaPublisher, cdmMapper);
        this.activeProfile = activeProfile.replace("iks", "");
        this.activeProfile = this.activeProfile.replace("dr", "");
    }

    @Override
    protected Class getCDMMapperClass() {
        return ContractorCDM.class;
    }

    @Override
    protected String getTopic() {
        String topicName = "sbseg-dtpayroll-contractor-restricted";
        if (activeProfile.equalsIgnoreCase("prd")) {
            return topicName;
        }
        return String.join(DELIMITER, topicName, activeProfile);
    }

    @Override
    protected Company getCompany(EntityContext<Payee> entityContext) {
        Payee payee = entityContext.getCurrentEntity();
        return payee.getCompany();
    }

    @Override
    protected Class getRestrictedCDMMapperClass() {
        return ContractorCDM.class;
    }

    @Override
    public Class<?> getEntityClass() {
        return Payee.class;
    }

}
