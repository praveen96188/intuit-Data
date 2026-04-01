package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalTitle;
import com.intuit.sbd.payroll.psp.domain.Contact;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class ContactToBankPrincipalCDMMapper extends BeanMapper<Contact, BankPrincipalCDM> {

    @Override
    public void directFieldToFieldMapping() {
        addBidirectionalFieldMapping("firstName", "firstName");
        addBidirectionalFieldMapping("lastName", "lastName");
        addBidirectionalFieldMapping("dateOfBirth", "birthDate");
        addBidirectionalFieldMapping("socialSecurityNumber", "taxIdentifier");
    }

    @Override
    public void mapAtoB(Contact contact, BankPrincipalCDM bankPrincipalCDM, MappingContext context) {
        bankPrincipalCDM.setTitle(BankPrincipalTitle.fromValue(contact.getTitle()));
        bankPrincipalCDM.setAddress(getEntityCDMMapper().mapToTarget(contact.getMailingAddress(), AddressCDM.class));
    }
}
