package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;


import com.intuit.sbd.payroll.psp.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.GovernmentId;
import com.intuit.v4.network.Contact;
import com.intuit.v4.network.definitions.ContactMethod;
import com.intuit.v4.network.definitions.Person;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PspEmployeeToV4ContactMapper extends BeanMapper<Employee, Contact> {

    private final PspEmployeeToV4ContactProfilesMapper pspEmployeeToV4ContactProfilesMapper;
    private final PspEmployeeToV4PersonMapper pspEmployeeToV4PersonMapper;
    private final PspEmployeeToV4ContactMethodMapper pspEmployeeToV4ContactMethodMapper;

    @Autowired
    public PspEmployeeToV4ContactMapper(PspEmployeeToV4ContactProfilesMapper pspEmployeeToV4ContactProfilesMapper,
                                        PspEmployeeToV4PersonMapper pspEmployeeToV4PersonMapper,
                                        PspEmployeeToV4ContactMethodMapper pspEmployeeToV4ContactMethodMapper) {
        this.pspEmployeeToV4ContactProfilesMapper = pspEmployeeToV4ContactProfilesMapper;
        this.pspEmployeeToV4PersonMapper = pspEmployeeToV4PersonMapper;
        this.pspEmployeeToV4ContactMethodMapper = pspEmployeeToV4ContactMethodMapper;
    }

    @Override
    public Contact mapToTarget(Employee employee, Class<Contact> t) {

        Contact employeeContact = new Contact();

        employeeContact.setId(GlobalId.create("", employee.getId().toString()));
        employeeContact.setDisplayName(StringUtils.defaultString(employee.getFullName()));

        employeeContact.setProfiles(pspEmployeeToV4ContactProfilesMapper.mapToTarget(employee, Contact.Profiles.class));
        employeeContact.setPerson(pspEmployeeToV4PersonMapper.mapToTarget(employee,Person.class));
        employeeContact.setGovernmentIds(createV4GovernmentIdsFromPspEmployee(employee));
        employeeContact.setContactMethods(Collections.singletonList(pspEmployeeToV4ContactMethodMapper.
                mapToTarget(employee, ContactMethod.class)));

        return employeeContact;
    }

    private List<GovernmentId> createV4GovernmentIdsFromPspEmployee(Employee employee) {
        GovernmentId governmentId = new GovernmentId();
        governmentId.setIdType(Guideline401kConstants.SSN);
        governmentId.setGovernmentId(employee.getTaxId());
        return Collections.singletonList(governmentId);
    }
}
