package com.intuit.sbd.payroll.psp.processes.publisher;

import com.intuit.payroll.api.employee.model.EmployeeCDM;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalSubCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class CDMVerifier {
    public void verifyEmployeeCdm(EmployeeCDM employeeCDM, Employee employee){
        if(Objects.isNull(employeeCDM) && Objects.isNull(employee))
            return;

        if(Objects.isNull(employeeCDM) || Objects.isNull(employee)){
            assertTrue("consumed employee did not match", false);
        }

        assertEquals("entity Id did not match", employee.getId().toString(), employeeCDM.getId());
        assertEquals("first name did not match", employee.getFirstName(), employeeCDM.getFirstName());
        assertEquals("consumer realmId did not match", employee.getConsumerRealmId(), employeeCDM.getConsumerFinanceRealm());
        assertEquals("middle name did not match", employee.getMiddleName(), employeeCDM.getMiddleName());
        assertEquals("last name did not match", employee.getLastName(), employeeCDM.getLastName());
        assertEquals("tax Id did not match", employee.getTaxId(), employeeCDM.getTaxId());
        assertEquals("email did not match", employee.getEmail(), employeeCDM.getBusinessEmail());
        assertEquals("phone did not match", employee.getPhone(), employeeCDM.getHomePhone());
        //assertEquals("", SpcfCalendar.toDateTime(employee.getCreatedDate()), employeeCDM.getCreated());
        assertEquals("birth date did not match", SpcfCalendar.toLocalDate(employee.getBirthDate()), employeeCDM.getBirthDate());
        //assertEquals("", SpcfCalendar.toDateTime(employee.getModifiedDate()), employeeCDM.getUpdated());
        assertEquals("entity version did not match", String.valueOf(employee.getVersion()), employeeCDM.getEntityVersion());
        assertEquals("gender did not match", Objects.isNull(employee.getGenderCd()) ? null : employee.getGenderCd().name(), employeeCDM.getGender());

        verifyAddress(employeeCDM.getHomeAddress(), employee.getMailingAddress());
        String cdmWorkAddress = Objects.isNull(employeeCDM.getWorkAddress()) ? null: employeeCDM.getWorkAddress().getRegion();
        assertEquals("Work state doesnot match", employee.getWorkState(), cdmWorkAddress);

        verifyEmployeeBankAccounts(employeeCDM.getBankAccounts(), employee.getEmployeeBankAccountCollection());

    }

    public void verifyAddress(AddressSubCDM addressCDM, Address address){
        if(Objects.isNull(addressCDM) && Objects.isNull(address))
            return;

        if(Objects.isNull(addressCDM) || Objects.isNull(address)){
            assertTrue("address do not match", false);
        }

        assertEquals("street address did not match", address.getStreetAddress(), addressCDM.getStreetAddress());
        assertEquals("city did not match", address.getCity(), addressCDM.getCity());
        assertEquals("country did not match", address.getCountry(), addressCDM.getCountry());
        assertEquals("zip code did not match", address.getFullZipCode(), addressCDM.getPostalCode());
        assertEquals("state did not match", address.getState(), addressCDM.getRegion());
    }

    public void verifyEmployeeBankAccounts(List<BankAccountSubCDM> bankAccountSubCDMs, DomainEntitySet<EmployeeBankAccount> employeeBankAccounts) {
        Map<String, EmployeeBankAccount> employeeBankAccountMap = new HashMap<>();

        for (EmployeeBankAccount employeeBankAccount:employeeBankAccounts) {
            employeeBankAccountMap.put(employeeBankAccount.getBankAccount().getId().toString(),employeeBankAccount);
        }

        for (BankAccountSubCDM bankAccountSubCDM:bankAccountSubCDMs) {
                verifyEmployeeBankAccount(bankAccountSubCDM, employeeBankAccountMap.get(bankAccountSubCDM.getId()));
        }

    }

    public void verifyEmployeeBankAccount(BankAccountSubCDM bankAccountSubCDM, EmployeeBankAccount employeeBankAccount){
        if(Objects.isNull(bankAccountSubCDM) && Objects.isNull(employeeBankAccount))
            return;

        if(Objects.isNull(bankAccountSubCDM) || Objects.isNull(employeeBankAccount)){
            assertTrue("bank account did not match", false);
        }

        verifyBankAccount(bankAccountSubCDM, employeeBankAccount.getBankAccount());

        verifyBankPrincipal(bankAccountSubCDM.getPrincipal(), employeeBankAccount.getEmployee());

        //assertEquals("owner type do not match", "employee",bankAccountSubCDM.getOwnerType());

    }

    public void verifyBankAccount(BankAccountSubCDM bankAccountSubCDM, BankAccount bankAccount){
        if(Objects.isNull(bankAccountSubCDM) && Objects.isNull(bankAccount))
            return;

        if(Objects.isNull(bankAccountSubCDM) || Objects.isNull(bankAccount)){
            assertTrue("bank account did not match", false);
        }

        assertEquals("account number did not match", bankAccount.getAccountNumber(), bankAccountSubCDM.getAccountNumber());
        assertEquals("routing number did not match", bankAccount.getRoutingNumber(), bankAccountSubCDM.getRoutingNumber());
        assertEquals("bank Id did not match", bankAccount.getId().toString(), bankAccountSubCDM.getId());
    }

    public void verifyBankPrincipal(BankPrincipalSubCDM bankPrincipalCDM, Employee employee){

        assertNotNull("bankPrincipal is null", bankPrincipalCDM);

        assertEquals("first name did not match", bankPrincipalCDM.getFirstName(), employee.getFirstName());
        assertEquals("last name did not match", bankPrincipalCDM.getLastName(), employee.getLastName());
        assertEquals("birth date did not match", bankPrincipalCDM.getBirthDate(), employee.getBirthDate());

        verifyAddress(bankPrincipalCDM.getAddress(), employee.getMailingAddress());
    }
}
