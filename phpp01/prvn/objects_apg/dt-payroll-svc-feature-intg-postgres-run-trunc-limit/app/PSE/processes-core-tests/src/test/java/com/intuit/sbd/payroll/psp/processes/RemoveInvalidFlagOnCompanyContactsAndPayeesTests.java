package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * User: ihannur
 * Date: 7/24/13
 * Time: 10:19 AM
 */
public class RemoveInvalidFlagOnCompanyContactsAndPayeesTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testValidations() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.removeInvalidFlagOnCompanyContactsAndPayees(SourceSystemCode.QBDT, null, null);
        PayrollServices.commitUnitOfWork();
        assertFalse(result.isSuccess());

        assertEquals("Messages size", 1, result.getMessages().size());
        assertEquals("Error message code", "138", result.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.", result.getMessages().get(0).getMessage());

        DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789");

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeInvalidFlagOnCompanyContactsAndPayees(SourceSystemCode.QBDT, "123456789", null);
        PayrollServices.commitUnitOfWork();
        assertFalse(result.isSuccess());

        assertEquals("Messages size", 1, result.getMessages().size());
        assertEquals("Error message code", "5002", result.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "Required 'Email Address' input is missing or blank", result.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeInvalidFlagOnCompanyContactsAndPayees(SourceSystemCode.QBDT, "123456789", "  ");
        PayrollServices.commitUnitOfWork();
        assertFalse(result.isSuccess());

        assertEquals("Messages size", 1, result.getMessages().size());
        assertEquals("Error message code", "5002", result.getMessages().get(0).getMessageCode());
        assertEquals("Error message", "Required 'Email Address' input is missing or blank", result.getMessages().get(0).getMessage());
    }

    @Test
    public void testHappyPathContactsUpdate() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.Tax);

        // Scenario - updating 3 contacts as invalid email id. Updating 2 contacts with same email Id.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        for (Contact contact : company.getContactCollection()) {
            switch(contact.getContactRoleCd()){
                case Other:
                    contact.setEmail("SecondaryPrincipal@aol.com");
                case PayrollAdmin:
                case SecondaryPrincipal:
                    contact.setHasInvalidEmail(true);
                    break;
            }
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        for (Contact contact : company.getContactCollection()) {
            switch(contact.getContactRoleCd()){
                case Other:
                    assertEquals("Same as Secondary contact", "SecondaryPrincipal@aol.com", contact.getEmail());
                case PayrollAdmin:
                case SecondaryPrincipal:
                    assertTrue("Invalid flag", contact.getHasInvalidEmail());
                    break;
                case PrimaryPrincipal:
                    Assert.assertFalse("Email id is valid", contact.getHasInvalidEmail());
            }
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.removeInvalidFlagOnCompanyContactsAndPayees(company.getSourceSystemCd(), company.getSourceCompanyId(), "PayrollAdmin@aol.com"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        for (Contact contact : company.getContactCollection()) {
            switch(contact.getContactRoleCd()){
                case Other:
                    assertEquals("Same as Secondary contact", "SecondaryPrincipal@aol.com", contact.getEmail());
                case SecondaryPrincipal:
                    assertTrue("Invalid flag", contact.getHasInvalidEmail());
                    break;
                case PayrollAdmin:
                    assertFalse("Removed Invalid flag", contact.getHasInvalidEmail());
                case PrimaryPrincipal:
                    Assert.assertFalse("Email id is valid", contact.getHasInvalidEmail());
            }
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.removeInvalidFlagOnCompanyContactsAndPayees(company.getSourceSystemCd(), company.getSourceCompanyId(), "SecondaryPrincipal@aol.com"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        for (Contact contact : company.getContactCollection()) {
            assertFalse("Removed Invalid flag", contact.getHasInvalidEmail());
        }
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testHappyPathPayeesUpdate() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", true, ServiceCode.DirectDeposit, ServiceCode.BillPayment);

        Payee payee = assertOne(DataLoadServices.addPayees(company, 1));

        PayrollServices.beginUnitOfWork();
        Application .refresh(payee);
        assertFalse("Invalid flag", payee.getHasInvalidEmail());
        payee.setHasInvalidEmail(true);
        Application.save(payee);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.removeInvalidFlagOnCompanyContactsAndPayees(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getEmail()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application .refresh(payee);
        assertFalse("Invalid flag", payee.getHasInvalidEmail());
        PayrollServices.rollbackUnitOfWork();
    }

}
