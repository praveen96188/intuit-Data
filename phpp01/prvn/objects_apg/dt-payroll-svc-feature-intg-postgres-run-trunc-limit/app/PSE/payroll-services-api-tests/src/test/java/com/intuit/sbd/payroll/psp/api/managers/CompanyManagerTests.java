package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dawn Martens
 * Date: Apr 10, 2008
 * Time: 10:05:32 AM
 */
public class CompanyManagerTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void createNewSourceCompanyId_Success() {
        Application.beginUnitOfWork();
        SourceSystemCode qbdt = SourceSystemCode.QBDT;
        String newId = PayrollServices.companyManager.createSourceCompanyId(qbdt);
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        String newId2 = PayrollServices.companyManager.createSourceCompanyId(qbdt);
        Application.commitUnitOfWork();

        assertNotNull("First newly created Source System ID", newId);
        assertNotNull("Second newly created Source System ID", newId2);
        assertEquals("First source system Id is of length 9", 9, newId.length());
        assertEquals("Second source system Ids is of length 9", 9, newId2.length());
        assertFalse("Ids are not the same", newId.equals(newId2));

        Long firstIdLongValue = Long.parseLong(newId);
        Long secondIdLongValue = Long.parseLong(newId2);

        Long differenceInValues = secondIdLongValue - firstIdLongValue;
        Long expectedDifferenceInValues = 1L;

        assertEquals("Ids are one apart", expectedDifferenceInValues, differenceInValues);
    }

    @Test
    public void createNewSourceCompanyId_InvalidSourceSystem() {
        Application.beginUnitOfWork();

        try {
            PayrollServices.companyManager.createSourceCompanyId(SourceSystemCode.QBOE);
            TestCase.fail("Did not catch expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ue) {
            assertEquals("Text of exception", "Create source company ID is currently only available for QuickBooks Desktop.", ue.getMessage());
        } catch (Exception e) {
            System.out.println("Msg " + e.getMessage());
            TestCase.fail("Caught unexpected exception: " + e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

     @Test
    public void updateCompanyContact_Events() {
         CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "1212");
         Company company = DataLoadServices.newCompany(companyDTO, "123456");
         DataLoadServices.addDDService(company);
         DataLoadServices.activateDDService(company);
         ContactDTO[] contacts = companyDTO.getContacts().toArray(new ContactDTO[0]);
         int otherContactIndex = 0;
         ContactDTO secPrincipalContact = new ContactDTO();



         for (int counter = 0; counter < contacts.length; counter++) {
             if(contacts[counter].getContactRoleCd() == ContactRole.Other){
                 otherContactIndex = counter;
             } else if (contacts[counter].getContactRoleCd() == ContactRole.SecondaryPrincipal){
                 secPrincipalContact = contacts[counter];
             }
         }

         companyDTO.getContacts().remove(contacts[otherContactIndex]);

         secPrincipalContact.setFaxNumber("(775) 202-9999");
         secPrincipalContact.setJobTitle("Payroll Accountant 7");
         secPrincipalContact.setFirstName("James");

         //Create and add new contact
         ContactDTO newContact = new ContactDTO();
         newContact.setFirstName("Jack");
         newContact.setMiddleName("Q");
         newContact.setLastName("Depp");
         newContact.setPhoneNumber("(775) 222-2222");
         newContact.setContactRoleCd(ContactRole.SecondaryPrincipal);
         newContact.setAccountSignatory(Boolean.TRUE);
         newContact.setEmail("SecondaryPrincipal@aol.com");
         newContact.setTitle("Mr.");
         newContact.setTitleSuffix("Jr.");
         newContact.setJobTitle("Payroll Accountant 2");
         newContact.setFaxNumber("(775) 202-2002");
         newContact.setSecondPhoneNumber("(775) 020-0220");
         newContact.setAddress(DataLoadServices.createAddress());
         newContact.setContactId(DataLoader.generateContactKey(newContact));

         companyDTO.getContacts().add(newContact);

         PayrollServices.beginUnitOfWork();
         ProcessResult<Company> result = PayrollServices.companyManager.updateCompany(
                 company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
         PayrollServices.commitUnitOfWork();

         PayrollServices.beginUnitOfWork();
         assertSuccess("updateCompany", result);

         DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyContactNameChanged,CompanyEventStatus.Active, null, null);
         Assert.assertEquals("Company contact name changed", 1, companyEventList.size());
         Assert.assertEquals("Company Contact Name New Value", "SecondaryPrincipal, James Q", companyEventList.getFirst().getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

         companyEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyContactJobTitleChanged,CompanyEventStatus.Active, null, null);
         Assert.assertEquals("Company contact Job title changed", 1, companyEventList.size());
         Assert.assertEquals("Company Contact Job title New Value", "Payroll Accountant 7", companyEventList.getFirst().getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

         companyEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyContactFaxChanged,CompanyEventStatus.Active, null, null);
         Assert.assertEquals("Company contact Fax changed", 1, companyEventList.size());
         Assert.assertEquals("Company Contact Fax New Value", "(775) 202-9999", companyEventList.getFirst().getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

         companyEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyContactAdded,CompanyEventStatus.Active, null, null);
         Assert.assertEquals("Company contact Added", 1, companyEventList.size());
         Assert.assertEquals("Company Contact Added Value", "Mr. Jack Q Depp Payroll Accountant 2 Jr. (775) 222-2222 (775) 202-2002 SecondaryPrincipal@aol.com AddressLine1 AddressLine2 AddressLine3, Ridgewood,NJ, 07450-4444, USA ",
                             companyEventList.getFirst().getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));

         companyEventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyContactDeleted,CompanyEventStatus.Active, null, null);
         Assert.assertEquals("Company contact Deleted", 1, companyEventList.size());
         Assert.assertEquals("Company Contact Deleted Value", "Mr. Johnny Q Other Payroll Accountant 4 Jr. (775) 444-4444 (775) 404-4004 Other@aol.com CONTACT_AddressLine1 CONTACT_AddressLine2 CONTACT_AddressLine3, Ridgewood,NJ, 07450-4444, USA ",
                             companyEventList.getFirst().getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue));

         PayrollServices.commitUnitOfWork();

     }
}
