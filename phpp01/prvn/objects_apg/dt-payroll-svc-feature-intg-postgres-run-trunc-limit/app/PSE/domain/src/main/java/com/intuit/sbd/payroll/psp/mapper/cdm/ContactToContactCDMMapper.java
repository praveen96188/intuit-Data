package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.company.model.ContactCDM;
import com.intuit.sbd.payroll.psp.domain.Contact;
import org.springframework.stereotype.Component;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.ContactToContactCDMMapper")
public class ContactToContactCDMMapper extends BeanMapper<Contact, ContactCDM> {

	@Override
	public ContactCDM mapToTarget(Contact contact, Class<ContactCDM> contactCDMClass) {
		ContactCDM contactCDM = new ContactCDM();
		contactCDM.setFirstName(contact.getFirstName());
		contactCDM.setMiddleName(contact.getMiddleName());
		contactCDM.setLastName(contact.getLastName());
		contactCDM.setTitle(contact.getTitle());
		contactCDM.setWorkPhone(contact.getPhone());
		contactCDM.setHomePhone(contact.getSecondPhone());
		contactCDM.setEmailAddress(contact.getEmail());
		return null;
	}
}
