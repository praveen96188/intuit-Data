package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.Contact;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.CompanyBankAccountToBankAccountSubCDMMapper")
public class CompanyBankAccountToBankAccountSubCDMMapper extends BeanMapper<CompanyBankAccount, BankAccountSubCDM> {

    @Override
    public BankAccountSubCDM mapToTarget(CompanyBankAccount companyBankAccount, Class<BankAccountSubCDM> bankAccountSubCDMClass) {
        if (Objects.isNull(companyBankAccount.getBankAccount()))
            return null;

        BankAccountSubCDM bankAccountSubCDM = getMapper().mapToTarget(companyBankAccount.getBankAccount(),BankAccountSubCDM.class);
        bankAccountSubCDM.setOwnerType("employer.principal");
        Contact primaryPrincipal = this.getSpecificContact(companyBankAccount.getCompany(),
                ContactRole.PrimaryPrincipal);
        bankAccountSubCDM.setPrincipal(getMapper().mapToTarget(primaryPrincipal, BankPrincipalCDM.class));
        return bankAccountSubCDM;
    }

    private Contact getSpecificContact(Company company, com.intuit.sbd.payroll.psp.domain.ContactRole contactRole) {
        DomainEntitySet<Contact> contact = Application.find(Contact.class,
                Contact.Company().equalTo(company).And(Contact.ContactRoleCd().equalTo(contactRole)));
        if (!contact.isEmpty()) {
            return contact.get(0);
        }
        return null;
    }
}