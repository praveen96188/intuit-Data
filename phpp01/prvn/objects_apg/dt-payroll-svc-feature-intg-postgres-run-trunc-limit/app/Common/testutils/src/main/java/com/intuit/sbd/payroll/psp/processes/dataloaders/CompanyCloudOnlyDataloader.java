/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/processes/dataloaders/CompanyCloudOnlyDataloader.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class CompanyCloudOnlyDataloader {

    private Company company;
    public Employee employee1;

    private DataLoader dataloader = new DataLoader();

    public Company persistCompanyButNotEmployee() {
//        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(getCompany1(), offeringInfoDTO);
//
//        assertSuccess("addCompany", result);
//
//        company = result.getResult();
//
//        CompanyService Company401kService = dataloader.persistCompanyService(company, getCompany1Service());
//        assertEquals("Status before PIN create", ServiceSubStatusCode.PendingPinCreation, Company401kService.getStatusCd());
//
//        PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBDT, "1234567", "Abcd1234");
//        assertEquals("Status after PIN create", ServiceSubStatusCode.PendingFirstPayroll, Company401kService.getStatusCd());
//
//        return company;
        return null;

    }

    public Company persistCompany1() {

        company = persistCompanyButNotEmployee();

        employee1 = persistEmployee();

        return company;
    }

    public DDServiceInfoDTO getCompany1DDService() {
        DDServiceInfoDTO ddServiceInfo = new DDServiceInfoDTO();
        ddServiceInfo.setServiceCode(ServiceCode.DirectDeposit);

        FundingModel fundingModel2D = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);
        ddServiceInfo.setFundingModel(fundingModel2D);

        return ddServiceInfo;
    }

    public ThirdParty401kServiceInfoDTO getCompany1Service() {
        ThirdParty401kServiceInfoDTO tp401kCompanyService = new ThirdParty401kServiceInfoDTO();

        tp401kCompanyService.setCustodialId("122222222");

        return tp401kCompanyService;
    }

    public static EmployeeDTO getEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setEmployeeId("EE1");
        employeeDTO.setFirstName("FirstNameOfEE1");
        employeeDTO.setLastName("TestLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSocialSecurityNumber("111223333");

        ThirdParty401kEmployeeInfoDTO employee401kDTO = new ThirdParty401kEmployeeInfoDTO();
        DateDTO birthDate = new DateDTO();
        birthDate.set(1980,3,3);

        DateDTO hireDate = new DateDTO();
        hireDate.set(1980,3,3);

        employeeDTO.setBirthDate(birthDate);
        employee401kDTO.setEmail("MyEmployee@fourOoneK.com");
        employee401kDTO.setPhoneNumber("775-555-1212");
        employeeDTO.setTerminationDate(null);
        employeeDTO.setHireDate(hireDate);
        employeeDTO.setLiveAddress(getTestMailingAddress());

        employeeDTO.setEmployee401kInfo(employee401kDTO);

        return employeeDTO;
    }

    public static EmployeeDTO getOptionalElementsEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setEmployeeId("EEOptional");
        employeeDTO.setFirstName("Owner");
        employeeDTO.setLastName("Employer");
        employeeDTO.setMiddleName("D");
        employeeDTO.setSocialSecurityNumber("888775464");

        ThirdParty401kEmployeeInfoDTO employee401kDTO = new ThirdParty401kEmployeeInfoDTO();
        DateDTO birthDate = new DateDTO();
        birthDate.set(1981,2,2);

        DateDTO hireDate = new DateDTO();
        hireDate.set(1982,5,5);

        DateDTO terminationDate = new DateDTO();
        terminationDate.set(1999,5,6);

        employeeDTO.setBirthDate(birthDate);
        employee401kDTO.setEmail("MyEmployeeOptional@fourOoneK.com");
        employee401kDTO.setPhoneNumber("775-555-1212");
        employeeDTO.setTerminationDate(terminationDate);
        employee401kDTO.setFamilyMember(true);
        employee401kDTO.setHighlyCompensatedEmployee(true);
        employee401kDTO.setOwnershipPercent(new BigDecimal("55.5"));
        employeeDTO.setHireDate(hireDate);
        employeeDTO.setLiveAddress(getTestMailingAddress());

        employeeDTO.setEmployee401kInfo(employee401kDTO);

        return employeeDTO;
    }

    public static EmployeeDTO getWarningEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setEmployeeId("EEWarn");
        employeeDTO.setFirstName("FirstNameOfEE1");
        employeeDTO.setLastName("TestLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSocialSecurityNumber(null);

        ThirdParty401kEmployeeInfoDTO employee401kDTO = new ThirdParty401kEmployeeInfoDTO();
//        DateDTO birthDate = new DateDTO();
//        birthDate.set(1980,3,3);
//
//        DateDTO hireDate = new DateDTO();
//        hireDate.set(1980,3,3);

        employeeDTO.setBirthDate(null);
        employee401kDTO.setEmail(null);
        employee401kDTO.setPhoneNumber(null);
        employeeDTO.setTerminationDate(null);
        employeeDTO.setHireDate(null);
        employeeDTO.setLiveAddress(null);

        employeeDTO.setEmployee401kInfo(employee401kDTO);

        return employeeDTO;
    }

    public EmployeeDTO getErrorEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setValidator(new EmployeeDTO401kValidator());

        employeeDTO.setEmployeeId("EEError");
        employeeDTO.setFirstName("FirstNameOfEE1");
        employeeDTO.setLastName("TestLastName");
        employeeDTO.setMiddleName("TMI");
        employeeDTO.setSocialSecurityNumber(null);

        ThirdParty401kEmployeeInfoDTO employee401kDTO = new ThirdParty401kEmployeeInfoDTO();
        DateDTO birthDate = new DateDTO();
        birthDate.set(1980,3,3);

        DateDTO hireDate = new DateDTO();
        hireDate.set(1980,3,3);

        employeeDTO.setBirthDate(birthDate);
        employee401kDTO.setEmail(null);
        employee401kDTO.setPhoneNumber("123456780123456780123456780123456780123456780123456780123456780123456780123456780123456780");
        employeeDTO.setTerminationDate(null);
        employeeDTO.setHireDate(null);
        employeeDTO.setLiveAddress(null);

        employeeDTO.setEmployee401kInfo(employee401kDTO);

        return employeeDTO;
    }

    public Employee persistEmployee() {

        EmployeeDTO employeeDTO = getEmployeeDTO();
        return persistEmployee(company, employeeDTO);
    }

    public Employee persistWarningEmployee() {
        EmployeeDTO warningEmployeeDTO = getWarningEmployeeDTO();
        warningEmployeeDTO.setValidator(new EmployeeDTO401kValidator());
        return persistEmployee(company, warningEmployeeDTO);
    }

    public Employee persistOptionalElementEmployee() {
        EmployeeDTO optionalElementsEmployee = getOptionalElementsEmployeeDTO();
        optionalElementsEmployee.setValidator(new EmployeeDTO401kValidator());
        return persistEmployee(company, optionalElementsEmployee);
    }

    private Employee persistEmployee(Company pCompany, EmployeeDTO pEmployee) {
        return persistEmployee(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(),
                pEmployee);
    }

    private Employee persistEmployee(SourceSystemCode pSourceSystemCd, String pSourceEmployeeId, EmployeeDTO pEmployee) {
        ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(
                pSourceSystemCd, pSourceEmployeeId, pEmployee);
        PayrollServicesTest.assertSuccess("addEmployee", procResult);
        return procResult.getResult();
    }

        public CompanyDTO getCompany1() {
        CompanyDTO comp = new CompanyDTO();
        comp.setDBA("Intuit");
        comp.setFein("123456789");
        comp.setLegalAddress(getTestLegalAddress());
        comp.setLegalName("Intuit");
        comp.setMailingAddress(getTestMailingAddress());
        comp.setNotificationEmail("notifications@intuit.com");
        comp.setCompanyId("1234567");
        comp.setSignUpDate(new DateDTO("2007-09-03"));
            comp.setPriceType("Standard");

        ContactDTO contact = getTestContact();
        Collection<ContactDTO> allContactsForCompany = new ArrayList();
        allContactsForCompany.add(contact);
        comp.setContacts(allContactsForCompany);
        comp.setSourceSystemCd(SourceSystemCode.QBDT);

        return comp;
    }


    private AddressDTO getTestLegalAddress() {
        AddressDTO legalAddress = new AddressDTO();
        legalAddress.setAddressLine1("6888 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");
        return legalAddress;
    }

    public static AddressDTO getTestMailingAddress() {
        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 45");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89521");
        mailingAddress.setState("NV");
        return mailingAddress;
    }

    public ContactDTO getTestContact() {
        ContactDTO contact = new ContactDTO();

        contact.setFirstName("John");
        contact.setMiddleName("P");
        contact.setLastName("Doe");
        contact.setPhoneNumber("(775) 424-8339");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("someEmail@aol.com");

        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("123 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));
        return contact;
    }

    public ProcessResult<Employee> persistErrorEmployee() {
        ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(
            SourceSystemCode.QBDT, "1234567", getErrorEmployeeDTO());
        return procResult;    }
}