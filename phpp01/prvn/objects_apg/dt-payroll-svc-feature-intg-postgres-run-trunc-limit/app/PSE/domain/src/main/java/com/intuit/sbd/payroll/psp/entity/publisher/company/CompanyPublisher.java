package com.intuit.sbd.payroll.psp.entity.publisher.company;

import com.intuit.payroll.api.company.model.CompanyCDM;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.AbstractEntityPublisher;
import com.intuit.sbd.payroll.psp.mapper.cdm.CDMMapper;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CompanyPublisher extends AbstractEntityPublisher<Company> {

    private String activeProfile;

    @Autowired
    public CompanyPublisher(KafkaSDKPublisher kafkaPublisher, CDMMapper cdmMapper, @Value("${spring.profiles.active}") String activeProfile) {
        super(kafkaPublisher, cdmMapper);
        this.activeProfile = activeProfile.replace("iks", "");
        this.activeProfile = this.activeProfile.replace("dr", "");
    }

    @Override
    protected Class getCDMMapperClass() {
        return CompanyCDM.class;
    }

    @Override
    protected String getTopic() {
        String topicName = "sbseg-dtpayroll-company-restricted";
        if (activeProfile.equalsIgnoreCase("prd")) {
            return topicName;
        }
        return String.join(DELIMITER, topicName, activeProfile);
    }

    @Override
    protected Company getCompany(EntityContext<Company> entityContext) {
        return entityContext.getCurrentEntity();
    }

    @Override
    protected Class getRestrictedCDMMapperClass() {
        return CompanyCDM.class;
    }

    @Override
    public Class<?> getEntityClass() {
        return Company.class;
    }

    @Override
    protected Map<String, String> getAdditionalHeaders(EntityContext<Company> entityContext) {
        Map<String, String> map =  super.getAdditionalHeaders(entityContext);
        Company company = getCompany(entityContext);
        map.put(EQUIFAX_INSUFFICIENT_DATA, Boolean.toString(!(company.hasService(ServiceCode.Tax) || company.hasService(ServiceCode.ViewMyPaycheck)
        || (company.hasService(ServiceCode.CloudV3)
        && !company.isCompanyOnTerminatedService(ServiceCode.CloudV3)))));
        return map;
    }
}
