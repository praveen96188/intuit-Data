package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Contact;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.Individual;
import ma.glasnost.orika.MappingContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class CompanyToBankPrincipalCDMMapper extends BeanMapper<Company, BankPrincipalCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("fedTaxId", "taxIdentifier");
	}

	@Override
	public void mapAtoB(Company company, BankPrincipalCDM bankPrincipalCDM, MappingContext context) {
		Contact contact = this.getSpecificContact(company, ContactRole.PrimaryPrincipal);
		Individual individual = this.findIndividualInfo(contact);
		if (individual != null) {
			bankPrincipalCDM
					.setAddress(getEntityCDMMapper().mapToTarget(individual.getMailingAddress(), AddressCDM.class));
		}
		if (contact != null) {
			bankPrincipalCDM.setFirstName(contact.getFirstName());
			bankPrincipalCDM.setLastName(contact.getLastName());
			if (contact.getDateOfBirth() != null) {
				DateTime dateTimeDateOfBirth = new DateTime(contact.getDateOfBirth().toLocal().getTimeInMilliseconds())
						.toDateTime(DateTimeZone.UTC);
				LocalDate date = new LocalDate(dateTimeDateOfBirth.getYear(), dateTimeDateOfBirth.getMonthOfYear(),
						dateTimeDateOfBirth.getDayOfMonth());
				bankPrincipalCDM.setBirthDate(date);
			}
		}
	}

	private Individual findIndividualInfo(Contact contact) {
		DomainEntitySet<Individual> individual = Application.find(Individual.class,
				Individual.Id().equalTo(contact.getId()));

		if (!individual.isEmpty()) {
			return individual.get(0);
		}
		return null;
	}

	private Contact getSpecificContact(Company company, ContactRole contactRole) {
		DomainEntitySet<Contact> contact = Application.find(Contact.class,
				Contact.Company().equalTo(company).And(Contact.ContactRoleCd().equalTo(contactRole)));
		if (!contact.isEmpty()) {
			return contact.get(0);
		}
		return null;
	}

}
