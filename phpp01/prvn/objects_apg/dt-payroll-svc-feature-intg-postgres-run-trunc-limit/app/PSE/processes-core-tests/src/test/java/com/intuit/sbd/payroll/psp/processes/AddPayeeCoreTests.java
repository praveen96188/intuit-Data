/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeTestSuiteDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeUpdateDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 * Contains the unit tests for the <CODE>Message</CODE> class.
 *
 * @author: Marcela Villani
 * @version: Dec 4, 2009
 */
public class AddPayeeCoreTests {


    private PayeeDTO payeeDTO;
    private static EmployeeTestSuiteDataLoader dataloader;
    private static Company3Dataloader c3dl = new Company3Dataloader();

    @BeforeClass
    public static void initialize() {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
            PayrollServicesTest.truncateTables();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void addPayeeCoreNew() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();
        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        // Persistence check
        verifyPayeeDTO(payeeDTO);
    }

    @Ignore
    @Test
    public void addPayeeCoreExistingPayee() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();
        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());
        payeeDTO.setName("UPDATED NAME");
        payeeDTO.setPhone("775-727-2727");
        payeeDTO.setTaxId("987654321");

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("New Address Rd");
        mailingAddress.setCity("Reno");
        mailingAddress.setState("NV");
        mailingAddress.setZipCode("89502");
        mailingAddress.setCountry("USA");
        payeeDTO.setMailingAddress(mailingAddress);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(pr);

        // Persistence check
        PayrollServices.beginUnitOfWork();
        verifyPayeeDTO(payeeDTO);
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void addCompanyNotSpecified() {
        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, null, payeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Source Company ID is not specified.", message.getMessage());
    }

    @Test
    @Ignore
    public void addCompanyInactive() {
        payeeDTO = getTestPayee();
        payeeDTO.setSourcePayeeId("TESTACTV");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456Inactive in its current state.", message.getMessage());
    }

    @Test
    @Ignore
    public void addCompanyOnHold() {
        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456OnHold in its current state.",
                message.getMessage());
    }


    private void verifyPayeeDTO(PayeeDTO dto) {
        verifyPayeeDTO(dto, SourceSystemCode.QBDT);
    }

    private void verifyPayeeDTO(PayeeDTO dto, SourceSystemCode pSrcSysCd) {
        Company company = Company.findCompany("8574536", pSrcSysCd);
        Payee payee = Payee.findPayee(company, dto.getSourcePayeeId());

        assertEquals("Name:", dto.getName(), payee.getName());
        assertEquals("Email:", dto.getEmail(), payee.getEmail());
        assertEquals("Phone:", dto.getPhone(), payee.getPhone());
        assertEquals("Payee Source Id:", dto.getSourcePayeeId(), payee.getSourcePayeeId());
        assertEquals("Tax Id:", dto.getTaxId(), payee.getTaxId());

    }

    public PayeeDTO getTestPayee() {
        PayeeDTO payee = new PayeeDTO();
        payee.setSourcePayeeId("TESTADDPAYEE");
        payee.setName("Add Payee Core Test");
        payee.setPhone("775-227-7227");
        payee.setTaxId("123456789");

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("123 High Country Rd");
        mailingAddress.setCity("Reno");
        mailingAddress.setState("NV");
        mailingAddress.setZipCode("89502");
        mailingAddress.setCountry("USA");
        payee.setMailingAddress(mailingAddress);

        return payee;
    }
    @Test
    public void addPayeeEventCheck() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();
        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());


        verifyPayeeDTO(payeeDTO);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList =
                                                CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBDT), EventTypeCode.PayeeAdded);
        //This accounts for the two other employees created in the BeforeClass
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Detail Code", 1,
                companyEventsList.get(0).getCompanyEventDetails(EventDetailTypeCode.PayeeId).size());
        assertEquals("Event Detail Value", Payee.findPayee(Company.findCompany("8574536", SourceSystemCode.QBDT), payeeDTO.getSourcePayeeId()).getId().toString(), companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayeeId));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updatePayeeEventCheck() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();
        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyPayeeDTO(payeeDTO);

        payeeDTO.setName("TestPayeeUpdated");

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBDT), EventTypeCode.PayeeUpdated);
        //This accounts for the two other employees created in the BeforeClass
        assertEquals("Company Events", 1, companyEventsList.size());

        PayrollServices.commitUnitOfWork();

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("Updated Address Rd");
        mailingAddress.setCity("SanFrancisco");
        mailingAddress.setState("CA");
        mailingAddress.setZipCode("89102");
        mailingAddress.setCountry("USA");
        payeeDTO.setMailingAddress(mailingAddress);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();

        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBDT), EventTypeCode.PayeeUpdated);
        //This accounts for the two other employees created in the BeforeClass
        assertEquals("Company Events", 2, companyEventsList.size());
        for(int i=0; i<2; i++) {
            assertEquals("Event Detail Code", 1,
                    companyEventsList.get(i).getCompanyEventDetails(EventDetailTypeCode.PayeeId).size());
            assertEquals("Event Detail Value", Payee.findPayee(Company.findCompany("8574536", SourceSystemCode.QBDT), payeeDTO.getSourcePayeeId()).getId().toString(), companyEventsList.get(i).getCompanyEventDetailValue(EventDetailTypeCode.PayeeId));
        }
        PayrollServices.commitUnitOfWork();

        payeeDTO.setEmail("updatedTest@gmail.com");

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList =
                CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBDT), EventTypeCode.PayeeUpdated);
        //This accounts for the two other employees created in the BeforeClass
        assertEquals("Company Events", 2, companyEventsList.size());
        PayrollServices.commitUnitOfWork();




    }

    @Test
    public void updatePayeeEventCheckForQBOE() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime((SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone())));
        c3dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        Company company=null;
        PayrollServices.beginUnitOfWork();
        company=Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.setSourceSystemCd(SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();

        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBOE, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyPayeeDTO(payeeDTO, SourceSystemCode.QBOE);

        payeeDTO.setName("TestPayeeUpdated");

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBOE, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("Updated Address Rd");
        mailingAddress.setCity("SanFrancisco");
        mailingAddress.setState("CA");
        mailingAddress.setZipCode("89102");
        mailingAddress.setCountry("USA");
        payeeDTO.setMailingAddress(mailingAddress);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBOE, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        payeeDTO.setEmail("updatedTest@gmail.com");

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBOE, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList =
                CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBOE), EventTypeCode.PayeeAdded);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList =
                CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBOE), EventTypeCode.PayeeUpdated);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updatePayeeEventCheckForNonDD() {
        Company company=DataLoadServices.newCompany(SourceSystemCode.QBDT, "8574536");

        ServiceInfoDTO vmpService=new ServiceInfoDTO();
        vmpService.setServiceCode(ServiceCode.ViewMyPaycheck);
        vmpService.setOfferingCode(OfferingCode.SYMFY14);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.addService(SourceSystemCode.QBDT, company.getSourceCompanyId(), vmpService);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();

        payeeDTO = getTestPayee();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Test Result:", pr.isSuccess());

        verifyPayeeDTO(payeeDTO, SourceSystemCode.QBDT);

        payeeDTO.setName("TestPayeeUpdated");

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("Updated Address Rd");
        mailingAddress.setCity("SanFrancisco");
        mailingAddress.setState("CA");
        mailingAddress.setZipCode("89102");
        mailingAddress.setCountry("USA");
        payeeDTO.setMailingAddress(mailingAddress);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        payeeDTO.setEmail("updatedTest@gmail.com");

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBDT, "8574536", payeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList =
                CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBDT), EventTypeCode.PayeeAdded);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList =
                CompanyEvent.findCompanyEvents(Company.findCompany("8574536", SourceSystemCode.QBDT), EventTypeCode.PayeeUpdated);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }

}