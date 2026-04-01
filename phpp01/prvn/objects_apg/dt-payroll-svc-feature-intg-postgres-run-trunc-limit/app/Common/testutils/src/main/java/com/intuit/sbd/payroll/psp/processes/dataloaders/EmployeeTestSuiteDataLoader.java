/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/EmployeeTestSuiteDataLoader.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class EmployeeTestSuiteDataLoader {


    public Company persistCompany(CompanyDTO pCompanyDTO) {
        ProcessResult<Company> result = DataLoader.addCompany(pCompanyDTO);
        Assert.assertTrue(result.isSuccess());

        ProcessResult<CompanyService> resultCompService = PayrollServices.companyManager.addService(SourceSystemCode.valueOf(pCompanyDTO.getSourceSystemCd().toString()), pCompanyDTO.getCompanyId(), new DDServiceInfoDTO());
        Assert.assertTrue(resultCompService.isSuccess());

        return result.getResult();
    }

    public ContactDTO getTestContact() {
        ContactDTO contact = new ContactDTO();

        contact.setFirstName("John");
        contact.setMiddleName("P");
        contact.setLastName("Doe");
        contact.setPhoneNumber("(775) 424-8339");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("contact1email@email.com");

        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));
        return contact;
    }

    public Company saveActiveCompany() {
        CompanyDTO company = new CompanyDTO();
        company.setSourceSystemCd(SourceSystemCode.QBOE);
        company.setCompanyId("123456");
        company.setDBA("Company");
        company.setFein("123456780");
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("Company");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("active@dmcinc.com");
        ContactDTO contact = getTestContact2();
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();
        contacts.add(contact);
        company.setContacts(contacts);

        return persistCompany(company);
    }

    public Company saveInactiveCompany() {
        CompanyDTO company = new CompanyDTO();
        company.setSourceSystemCd(SourceSystemCode.QBOE);
        company.setCompanyId("123456Inactive");
        company.setDBA("Inactive Company");
        company.setFein("123456781");
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("Inactive Company");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("inactive@dmcinc.com");
        ContactDTO contact = getTestContact2();
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();
        contacts.add(contact);
        company.setContacts(contacts);

        Company newCompany = persistCompany(company);
        CompanyService companyService = CompanyService.findCompanyService(newCompany, ServiceCode.DirectDeposit);
        companyService.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        newCompany = PayrollServicesTest.save(newCompany);
        return newCompany;
    }

    public Company saveHoldCompany() {
        CompanyDTO company = new CompanyDTO();
        company.setSourceSystemCd(SourceSystemCode.QBOE);
        company.setCompanyId("123456OnHold");
        company.setDBA("Intuit Hold");
        company.setFein("123456782");
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("Intuit Hold");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("hold@dmcinc.com");
        ContactDTO contact = getTestContact2();
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();
        contacts.add(contact);
        company.setContacts(contacts);

        Company newCompany = persistCompany(company);
        PayrollServices.companyManager.addOnHoldReason(newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
        Assert.assertTrue(newCompany.isCompanyOnHold());
        return newCompany;
    }

    public Company saveTerminatedCompany() {
        CompanyDTO company = new CompanyDTO();
        company.setSourceSystemCd(SourceSystemCode.QBOE);
        company.setCompanyId("123456Terminated");
        company.setDBA("Intuit Term");
        company.setFein("123456783");
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("Intuit Term");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("term@dmcinc.com");
        ContactDTO contact = getTestContact2();
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();
        contacts.add(contact);
        company.setContacts(contacts);

        Company newCompany = persistCompany(company);
        CompanyService companyService = CompanyService.findCompanyService(newCompany, ServiceCode.DirectDeposit);
        companyService.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        newCompany = Application.save(newCompany);
        return newCompany;
    }


    public Company savePendingTerminatedCompany() {
        CompanyDTO company = new CompanyDTO();
        company.setSourceSystemCd(SourceSystemCode.QBOE);
        company.setCompanyId("123456PendingTermination");
        company.setDBA("123456PendingTermination");
        company.setFein("123456784");
        company.setLegalAddress(getTestLegalAddress2());
        company.setLegalName("123456PendingTermination");
        company.setMailingAddress(getTestMailingAddress2());
        company.setNotificationEmail("pendterm@dmcinc.com");
        ContactDTO contact = getTestContact2();
        Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();
        contacts.add(contact);
        company.setContacts(contacts);

        Company newCompany = persistCompany(company);
        PayrollServices.companyManager.addOnHoldReason(newCompany.getSourceSystemCd(), newCompany.getSourceCompanyId(), ServiceSubStatusCode.PendingTermination);
        Assert.assertTrue(newCompany.isCompanyOnHold());
        return newCompany;
    }


    private AddressDTO getTestLegalAddress2() {
        AddressDTO legalAddress = new AddressDTO();
        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy2upd");
        legalAddress.setCity("Reno2");
        legalAddress.setZipCode("89512");
        legalAddress.setState("NE");
        return legalAddress;
    }

    private AddressDTO getTestMailingAddress2() {
        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("6887 Sierra Center Parkway2upd");
        mailingAddress.setAddressLine2("Suite 452");
        mailingAddress.setAddressLine3("test line 2");
        mailingAddress.setCity("Reno3");
        mailingAddress.setZipCode("89513");
        mailingAddress.setState("NM");
        return mailingAddress;
    }

    private ContactDTO getTestContact2() {
        ContactDTO contact = new ContactDTO();

        contact.setFirstName("John2");
        contact.setMiddleName("P2");
        contact.setLastName("Doe2");
        contact.setPhoneNumber("(775) 424-83392");
        contact.setContactRoleCd(ContactRole.Other);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("contact2email@email.com");
        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("123 High Country Rd2");
        contactAddr.setCity("Reno2");
        contactAddr.setState("NY");
        contactAddr.setZipCode("89522");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));
        return contact;
    }

    public DDServiceInfoDTO getTestCompanyService() {
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        BigDecimal avgPayrollRunAmount = new BigDecimal("150.00");
        ddCompanyService.setAveragePayrollAmount(avgPayrollRunAmount);

        BigDecimal highPayrollRunAmount = new BigDecimal("250.00");
        ddCompanyService.setHighAnnualPayrollAmount(highPayrollRunAmount);

        return ddCompanyService;
    }

    public Employee saveActiveEmployee(Company c) {
        ProcessResult<Employee> processResult = PayrollServices.employeeManager
                .addEmployee(c.getSourceSystemCd(), c.getSourceCompanyId(), getActiveEmployeeDTO());

        return processResult.getResult();
    }

    public EmployeeDTO getActiveEmployeeDTO() {
        EmployeeDTO ee = new EmployeeDTO();
        ee.setEmployeeId("TESTACTV");
        ee.setFirstName("Active");
        ee.setLastName("Employee");
        ee.setSocialSecurityNumber("123456789");
        return ee;
    }

    public EmployeeDTO getInactiveEmployeeDTO() {
        EmployeeDTO ee = new EmployeeDTO();
        ee.setEmployeeId("TESTINACTV");
        ee.setFirstName("Inactive");
        ee.setLastName("Employee");
        ee.setSocialSecurityNumber("123456789");
        return ee;
    }


    public Employee saveInactiveEmployee(Company c) {


        PayrollServices.employeeManager
                .addEmployee(c.getSourceSystemCd(), c.getSourceCompanyId(), getInactiveEmployeeDTO());

        ProcessResult<Employee> deactiveEmployeeProcessResult = PayrollServices.employeeManager
                .deactivateEmployee(c.getSourceSystemCd(), c.getSourceCompanyId(), "TESTINACTV", null);

        return deactiveEmployeeProcessResult.getResult();
    }

    public void loadFullSuite() {
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()));
        Company company = saveActiveCompany();
        saveActiveEmployee(company);
        saveInactiveEmployee(company);

        savePendingTerminatedCompany();
        saveTerminatedCompany();
        saveInactiveCompany();
        saveHoldCompany();        
    }

}
