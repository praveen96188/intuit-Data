package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.CompanyToBankPrincipalCDMMapper")
@Slf4j
public class CompanyToBankPrincipalCDMMapper extends BeanMapper<Company, BankPrincipalCDM> {

    @Override
    public BankPrincipalCDM mapToTarget(Company s, Class<BankPrincipalCDM> target) {
        DomainEntitySet<Contact> contacts = s.getContactsByRoleCode(ContactRole.PrimaryPrincipal);
        if (isNull(contacts) || contacts.isEmpty() || isNull(contacts.get(0))) {
            throw new IllegalArgumentException("Company_Contact=null, CompanyId=" + s.getId());
        }
        Contact contact = contacts.get(0);
        BankPrincipalCDM t = getMapper().mapToTarget(contact, BankPrincipalCDM.class);
        t.setTaxIdentifier(s.getFedTaxId());
        return t;
    }

}
