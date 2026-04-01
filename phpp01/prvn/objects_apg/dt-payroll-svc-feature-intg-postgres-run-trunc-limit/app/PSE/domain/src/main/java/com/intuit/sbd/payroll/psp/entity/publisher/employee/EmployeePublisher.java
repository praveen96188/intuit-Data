package com.intuit.sbd.payroll.psp.entity.publisher.employee;

import com.intuit.payroll.api.employee.model.EmployeeCDM;
import com.intuit.payroll.api.employee.model.PrivilegedEmployeeCDM;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.AbstractEntityPublisher;
import com.intuit.sbd.payroll.psp.mapper.cdm.CDMMapper;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class EmployeePublisher extends AbstractEntityPublisher<Employee> {

    String activeProfile;

    @Autowired
    public EmployeePublisher(KafkaSDKPublisher kafkaPublisher, CDMMapper cdmMapper, @Value("${spring.profiles.active}") String activeProfile) {
        super(kafkaPublisher, cdmMapper);
        this.activeProfile = activeProfile.replace("iks", "");
        this.activeProfile = this.activeProfile.replace("dr", "");
    }

    @Override
    protected Class getCDMMapperClass() {
        return EmployeeCDM.class;
    }

    @Override
    protected String getTopic() {
        String topicName = "sbseg-dtpayroll-employee-restricted";
        if (activeProfile.equalsIgnoreCase("prd")) {
            return topicName;
        }
        return String.join(DELIMITER, topicName, activeProfile);
    }

    @Override
    protected Class getRestrictedCDMMapperClass() {
        return PrivilegedEmployeeCDM.class;
    }

    @Override
    public Class<?> getEntityClass() {
        return Employee.class;
    }

    @Override
    protected Company getCompany(EntityContext<Employee> entityContext){
        Employee employee = entityContext.getCurrentEntity();
        return employee.getCompany();
    }


}
