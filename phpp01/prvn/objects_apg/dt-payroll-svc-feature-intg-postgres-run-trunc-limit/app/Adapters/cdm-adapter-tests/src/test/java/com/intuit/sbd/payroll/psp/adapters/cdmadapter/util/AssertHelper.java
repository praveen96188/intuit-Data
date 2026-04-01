package com.intuit.sbd.payroll.psp.adapters.cdmadapter.util;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.schema.ems.v3.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.Assert;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * User: michaelp696
 */
public class AssertHelper {

    public static void assertCompaniesMatch(Company company, PayrollCompany payrollCompany) {
        Assert.assertEquals(company.getSourceCompanyId(), payrollCompany.getId());
        Assert.assertEquals(company.getDbaName(), payrollCompany.getCompanyName());
        Assert.assertEquals(company.getLegalName(), payrollCompany.getCompanyLegalName());
        //Address == legal address
        assertAddressMatches(company.getLegalAddress(), payrollCompany.getAddress());
        //Shipping address == mailing address
        assertAddressMatches(company.getMailingAddress(), payrollCompany.getShipAddr());
        if(company.getPhone() != null) {
            Assert.assertEquals(company.getPhone(), payrollCompany.getPhone().getFreeFormNumber());
        }
        if(company.getNotificationEmail() != null) {
            Assert.assertEquals(company.getNotificationEmail(), payrollCompany.getEmail().getAddress());
        }
    }

    public static void assertEmployeeMatches(Employee employee, PayrollEmployee payrollEmployee) {
        Assert.assertEquals(employee.getId().toString(), payrollEmployee.getId());
        assertIndividualMatches(employee, payrollEmployee);
        Assert.assertEquals(employee.getCompany().getDbaName(), payrollEmployee.getCompanyName());
        Assert.assertEquals("...." + employee.getTaxId().substring(5), payrollEmployee.getSSN());
        if(employee.getGenderCd() != null) {
            Assert.assertEquals(employee.getGenderCd().toString(), payrollEmployee.getGender().value());
        }
        assertAddressMatches(employee.getMailingAddress(), payrollEmployee.getPrimaryAddress());
        //Date fields
        assertDateMatches(employee.getBirthDate(), payrollEmployee.getBirthDate());
        assertDateMatches(employee.getHireDate(), payrollEmployee.getHiredDate());
        assertDateMatches(employee.getTerminationDate(), payrollEmployee.getReleasedDate());

        if(employee.getStatusCd() != null) {
            Assert.assertEquals(employee.getStatusCd().in(EmployeeStatus.Active), payrollEmployee.isActive());
        }
        if(employee.getWorkState() != null) {
            Assert.assertEquals(employee.getWorkState(), payrollEmployee.getWorkStateTaxFilingInfo().getState());
        }
        assertStateTaxFilingInformation(employee, payrollEmployee.getWorkStateTaxFilingInfo());
        assertPayPeriodHistory(employee, payrollEmployee.getPayPeriodHistory());
    }

    public static void assertContactMatches(Contact contact, PayrollContact payrollContact) {
        assertIndividualMatches(contact, payrollContact);
        Assert.assertEquals(contact.getId().toString(), payrollContact.getId());
        if(contact.getContactRoleCd() != null) {
            Assert.assertEquals(contact.getContactRoleCd().toString(), payrollContact.getContactType());
        }
        assertPhoneMatches(contact.getPhone(), payrollContact.getPrimaryPhone());
        assertEmailMatches(contact.getEmail(), payrollContact.getPrimaryEmailAddress());
    }

    public static void assertIndividualMatches(Individual individual, NameBase nameBase) {
        Assert.assertEquals(individual.getFirstName(), nameBase.getGivenName());
        Assert.assertEquals(individual.getMiddleName(), nameBase.getMiddleName());
        Assert.assertEquals(individual.getLastName(), nameBase.getFamilyName());
        Assert.assertEquals(individual.getFirstMiddleLastName(), nameBase.getFullName());
    }

    public static void assertAddressMatches(Address address, PhysicalAddress payrollCompanyAddress) {
        Assert.assertEquals(address.getAddressLine1(), payrollCompanyAddress.getLine1());
        Assert.assertEquals(address.getAddressLine2(), payrollCompanyAddress.getLine2());
        Assert.assertEquals(address.getAddressLine3(), payrollCompanyAddress.getLine3());
        Assert.assertEquals(address.getCity(), payrollCompanyAddress.getCity());
        Assert.assertEquals(address.getZipCode(), payrollCompanyAddress.getPostalCode());
        Assert.assertEquals(address.getZipCodeExtension(), payrollCompanyAddress.getPostalCodeSuffix());
        Assert.assertEquals(address.getState(), payrollCompanyAddress.getCountrySubDivisionCode());
        Assert.assertEquals(address.getCountry(), payrollCompanyAddress.getCountry());
    }

    public static void assertPhoneMatches(String phoneNumber, TelephoneNumber payrollCompanyPhoneNumber) {
        if(phoneNumber != null) {
            Assert.assertEquals(phoneNumber, payrollCompanyPhoneNumber.getFreeFormNumber());
        }
    }

    public static void assertEmailMatches(String emailAddress, EmailAddress payrollCompanyEmailAddress) {
        if(emailAddress != null) {
            Assert.assertEquals(emailAddress, payrollCompanyEmailAddress.getAddress());
        }
    }

    public static void assertDateMatches(SpcfCalendar spcfCalendar, XMLGregorianCalendar xmlGregorianCalendar) {
        if(spcfCalendar != null) {
            Assert.assertEquals(spcfCalendar.getDay(), xmlGregorianCalendar.getDay());
            Assert.assertEquals(spcfCalendar.getMonth(), xmlGregorianCalendar.getMonth());
            Assert.assertEquals(spcfCalendar.getYear(), xmlGregorianCalendar.getYear());
        }
    }

    public static void assertStateTaxFilingInformation(Employee employee, StateTaxFilingInformation stateTaxFilingInformation) {
        if(employee != null) {
            Assert.assertEquals(employee.getWorkState(), stateTaxFilingInformation.getState());
        }
        //TODO additional fields
    }

    public static void assertPayPeriodHistory(Employee employee, PayrollEmployee.PayPeriodHistory payPeriodHistory) {
        if(employee != null) {
            PayrollFrequencyCode payrollFrequencyCode = employee.getPayPeriod();
            if(payrollFrequencyCode != null) {
                Assert.assertEquals(payrollFrequencyCode.toString(), payPeriodHistory.toString());
            }
        }
        //TODO additional fields
    }
}
