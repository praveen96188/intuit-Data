package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.sbd.payroll.psp.mapper.guideline401k.address.PspAddressToV4AddressMapper;
import com.intuit.v4.common.Address;
import com.intuit.v4.common.EmailAddress;
import com.intuit.v4.common.Telephone;
import com.intuit.v4.network.definitions.ContactMethod;
import com.intuit.v4.network.definitions.ContactMethodLabelEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PspEmployeeToV4ContactMethodMapper extends BeanMapper<Employee, ContactMethod> {

    private final PspAddressToV4AddressMapper pspAddressToV4AddressMapper;

    @Autowired
    public PspEmployeeToV4ContactMethodMapper(PspAddressToV4AddressMapper pspAddressToV4AddressMapper) {
        this.pspAddressToV4AddressMapper = pspAddressToV4AddressMapper;
    }

    @Override
    public ContactMethod mapToTarget(Employee employee, Class<ContactMethod> t) {
        ContactMethod contactMethod = new ContactMethod();
        contactMethod.setLabels(Collections.singletonList(ContactMethodLabelEnum.LEGAL));
        Telephone telephone = new Telephone();
        telephone.setNumber(StringUtils.defaultString(employee.getPhone()));
        contactMethod.addTelephones(telephone);

        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setEmailAddress(StringUtils.defaultString(employee.getEmail()));
        contactMethod.addEmails(emailAddress);

        Address v4Address = pspAddressToV4AddressMapper.mapToTarget(employee.getMailingAddress(), Address.class);
        contactMethod.addAddresses(v4Address);

        //added based on request from IDG to also fill primary fields.
        //Note: these fields are deprecated from v4.
        contactMethod.setPrimaryAddress(v4Address);
        contactMethod.setPrimaryEmail(emailAddress);
        contactMethod.setPrimaryTelephone(telephone);
        return contactMethod;
    }
}
