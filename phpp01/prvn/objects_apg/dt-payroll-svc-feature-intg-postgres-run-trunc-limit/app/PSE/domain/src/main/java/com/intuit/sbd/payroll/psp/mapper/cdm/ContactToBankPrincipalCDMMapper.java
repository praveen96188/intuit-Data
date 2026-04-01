package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDMImpl;
import com.intuit.sbd.payroll.psp.domain.Contact;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.ContactToBankPrincipalCDMMapper")
public class ContactToBankPrincipalCDMMapper extends BeanMapper<Contact, BankPrincipalCDM>{

    @Override
    public BankPrincipalCDM mapToTarget(Contact contact, Class<BankPrincipalCDM> bankPrincipalCDMClass) {
        BankPrincipalCDM bankPrincipalCDM = new BankPrincipalCDMImpl();
        bankPrincipalCDM.setAddress(getMapper().mapToTarget(contact.getMailingAddress(), AddressSubCDM.class));
        bankPrincipalCDM.setFirstName(contact.getFirstName());
        bankPrincipalCDM.setLastName(contact.getLastName());
        if (contact.getDateOfBirth() != null) {
            DateTime dateTimeDateOfBirth = new DateTime(contact.getDateOfBirth().toLocal().getTimeInMilliseconds())
                    .toDateTime(DateTimeZone.UTC);
            LocalDate date = new LocalDate(dateTimeDateOfBirth.getYear(), dateTimeDateOfBirth.getMonthOfYear(),
                    dateTimeDateOfBirth.getDayOfMonth());
            bankPrincipalCDM.setBirthDate(date);
        }
        return bankPrincipalCDM;
    }
}
