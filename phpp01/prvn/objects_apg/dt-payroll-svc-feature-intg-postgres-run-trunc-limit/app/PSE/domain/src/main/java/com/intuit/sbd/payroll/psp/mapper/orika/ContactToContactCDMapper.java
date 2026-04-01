package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.company.model.ContactCDM;
import com.intuit.sbd.payroll.psp.domain.Contact;
import org.springframework.stereotype.Component;

@Component
public class ContactToContactCDMapper extends BeanMapper<Contact, ContactCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("firstName", "firstName");
		addBidirectionalFieldMapping("middleName", "middleName");
		addBidirectionalFieldMapping("lastName", "lastName");
		addBidirectionalFieldMapping("suffix", "title");
		addBidirectionalFieldMapping("phone", "workPhone");
		addBidirectionalFieldMapping("phone", "businessPhone");
		addBidirectionalFieldMapping("secondPhone", "homePhone");
		addBidirectionalFieldMapping("email", "emailAddress");
	}

}
