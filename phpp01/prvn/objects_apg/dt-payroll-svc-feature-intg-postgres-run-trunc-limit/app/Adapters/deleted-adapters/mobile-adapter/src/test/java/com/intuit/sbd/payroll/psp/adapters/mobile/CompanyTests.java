package com.intuit.sbd.payroll.psp.adapters.mobile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.mobile.webservices.CompanyWS;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import org.junit.*;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class CompanyTests {

    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

   @Before
    public void startUp() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
    }

    @After
    public void shutdown() {
        DataLoadServices.setEmployeeCount(1);
        DataLoadServices.setPayrollCount(1);
        DataLoadServices.setLoadAdditionalSavingsAccount(false);
    }

    @Test
    public void getCompanyDIY() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addEntitlementUnit(company, "1234567890", "123456", EditionType.Basic, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());

        CompanyWS companyWS = new CompanyWS();
        String jsonResponse = companyWS.getCompany(company.getFedTaxId(), DataLoadServices.PIN);

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        assertNotNull(rsResponse.getCompany());
        RSCompany rsCompany = rsResponse.getCompany();

        assertNotNull(rsCompany.getPsid());
        assertNotNull("000000001", rsCompany.getEin());
        assertNotNull("TEST_COMPANY_2", rsCompany.getLegalName());
        assertNotNull("TEST_COMPANY_2", rsCompany.getDba());
        assertNotNull(rsCompany.getSubscriptionNumber());
        assertNotNull(rsCompany.getServiceKey());
        assertNotNull(rsCompany.getNextChargeDate());
        assertEquals("TEST_null@COMPANY.COM", rsCompany.getCompanyEmail());
        assertEquals("John Doe", rsCompany.getBillingContact());
        assertEquals("89511", rsCompany.getBillingZip());
        assertNotNull(rsCompany.getSubscriptionEndDate());
        assertEquals("test@intuit.com", rsCompany.getBillingEmail());
        assertEquals("4263", rsCompany.getCreditCard());
        assertEquals("Visa", rsCompany.getCreditCardType());

        assertNull(rsCompany.getExtensionKey());
        assertNull(rsCompany.getBankAccount());

        assertNotNull(rsCompany.getAddresses());
        for (RSAddress rsAddress : rsCompany.getAddresses()) {
            switch (rsAddress.getAddressType()) {
                case Legal:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
                case Mailing:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
            }

        }

        assertNotNull(rsCompany.getContacts());

        for (RSContact rsContact : rsCompany.getContacts()) {
            if ("Other".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("Other", rsContact.getLastName());
                assertEquals("Other@aol.com", rsContact.getEmail());
                assertEquals("(775) 444-4444", rsContact.getPrimaryPhone());
            } else
            if ("Payroll Admin".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PayrollAdmin", rsContact.getLastName());
                assertEquals("PayrollAdmin@aol.com", rsContact.getEmail());
                assertEquals("(775) 333-3333", rsContact.getPrimaryPhone());
            } else
            if ("Primary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PrimaryPrincipal", rsContact.getLastName());
                assertEquals("PrimaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 111-1111", rsContact.getPrimaryPhone());
            } else
            if ("Secondary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("SecondaryPrincipal", rsContact.getLastName());
                assertEquals("SecondaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 222-2222", rsContact.getPrimaryPhone());
            }
        }

        assertNotNull(rsCompany.getServices());
        assertEquals(1, rsCompany.getServices().size());

        RSService rsService = rsCompany.getServices().get(0);
        assertEquals("DIY", rsService.getServiceCd());
        assertEquals("Active", rsService.getStatusCd());
    }

    @Test
    public void getCompany401k() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.ThirdParty401k);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addEntitlementUnit(company, "1234567890", "123456", EditionType.Basic, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());

        CompanyWS companyWS = new CompanyWS();
        String jsonResponse = companyWS.getCompany(company.getFedTaxId(), DataLoadServices.PIN);

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        assertNotNull(rsResponse.getCompany());
        RSCompany rsCompany = rsResponse.getCompany();

        assertNotNull(rsCompany.getPsid());
        assertNotNull("000000001", rsCompany.getEin());
        assertNotNull("TEST_COMPANY_3", rsCompany.getLegalName());
        assertNotNull("TEST_COMPANY_3", rsCompany.getDba());
        assertNotNull(rsCompany.getSubscriptionNumber());
        assertNotNull(rsCompany.getServiceKey());
        assertNotNull(rsCompany.getNextChargeDate());
        assertEquals("TEST_null@COMPANY.COM", rsCompany.getCompanyEmail());
        assertEquals("John Doe", rsCompany.getBillingContact());
        assertEquals("89511", rsCompany.getBillingZip());
        assertNotNull(rsCompany.getSubscriptionEndDate());
        assertEquals("test@intuit.com", rsCompany.getBillingEmail());
        assertEquals("4263", rsCompany.getCreditCard());
        assertEquals("Visa", rsCompany.getCreditCardType());

        assertNull(rsCompany.getExtensionKey());
        assertNull(rsCompany.getBankAccount());

        assertNotNull(rsCompany.getAddresses());
        for (RSAddress rsAddress : rsCompany.getAddresses()) {
            switch (rsAddress.getAddressType()) {
                case Legal:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
                case Mailing:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
            }

        }

        assertNotNull(rsCompany.getContacts());

        for (RSContact rsContact : rsCompany.getContacts()) {
            if ("Other".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("Other", rsContact.getLastName());
                assertEquals("Other@aol.com", rsContact.getEmail());
                assertEquals("(775) 444-4444", rsContact.getPrimaryPhone());
            } else
            if ("Payroll Admin".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PayrollAdmin", rsContact.getLastName());
                assertEquals("PayrollAdmin@aol.com", rsContact.getEmail());
                assertEquals("(775) 333-3333", rsContact.getPrimaryPhone());
            } else
            if ("Primary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PrimaryPrincipal", rsContact.getLastName());
                assertEquals("PrimaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 111-1111", rsContact.getPrimaryPhone());
            } else
            if ("Secondary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("SecondaryPrincipal", rsContact.getLastName());
                assertEquals("SecondaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 222-2222", rsContact.getPrimaryPhone());
            }
        }

        assertNotNull(rsCompany.getServices());
        assertEquals(2, rsCompany.getServices().size());

        RSService rsService = rsCompany.getServices().get(0);
        assertEquals("DIY", rsService.getServiceCd());
        assertEquals("Active", rsService.getStatusCd());

        rsService = rsCompany.getServices().get(1);
        assertEquals("401k", rsService.getServiceCd());
        assertEquals("Active", rsService.getStatusCd());
    }

    @Test
    public void getCompanyDD() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addEntitlementUnit(company, "1234567890", "123456", EditionType.Basic, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());

        CompanyWS companyWS = new CompanyWS();
        String jsonResponse = companyWS.getCompany(company.getFedTaxId(), DataLoadServices.PIN);

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        assertNotNull(rsResponse.getCompany());
        RSCompany rsCompany = rsResponse.getCompany();

        assertNotNull(rsCompany.getPsid());
        assertNotNull("000000001", rsCompany.getEin());
        assertNotNull("TEST_COMPANY_1", rsCompany.getLegalName());
        assertNotNull("TEST_COMPANY_1", rsCompany.getDba());
        assertNotNull(rsCompany.getSubscriptionNumber());
        assertNotNull(rsCompany.getServiceKey());
        assertNotNull(rsCompany.getNextChargeDate());
        assertEquals("TEST_null@COMPANY.COM", rsCompany.getCompanyEmail());
        assertEquals("John Doe", rsCompany.getBillingContact());
        assertEquals("89511", rsCompany.getBillingZip());
        assertNotNull(rsCompany.getSubscriptionEndDate());
        assertEquals("test@intuit.com", rsCompany.getBillingEmail());
        assertEquals("4263", rsCompany.getCreditCard());
        assertEquals("Visa", rsCompany.getCreditCardType());

        assertNull(rsCompany.getExtensionKey());

        assertNotNull(rsCompany.getBankAccount());
        RSBankAccount rsBankAccount = rsCompany.getBankAccount();

        assertNotNull("ACCNT_1", rsBankAccount.getAccountNumber());
        assertEquals("111000025", rsBankAccount.getRoutingNumber());
        assertNotNull("TestBank_2", rsBankAccount.getBankName());
        assertEquals(RSBankAccountStatusCode.Active, rsBankAccount.getStatus());
        assertEquals(RSBankAccountTypeCode.Checking, rsBankAccount.getType());
        assertNotNull(rsBankAccount.getId());

        assertNotNull(rsCompany.getAddresses());
        for (RSAddress rsAddress : rsCompany.getAddresses()) {
            switch (rsAddress.getAddressType()) {
                case Legal:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
                case Mailing:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
            }

        }

        assertNotNull(rsCompany.getContacts());

        for (RSContact rsContact : rsCompany.getContacts()) {
            if ("Other".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("Other", rsContact.getLastName());
                assertEquals("Other@aol.com", rsContact.getEmail());
                assertEquals("(775) 444-4444", rsContact.getPrimaryPhone());
            } else
            if ("Payroll Admin".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PayrollAdmin", rsContact.getLastName());
                assertEquals("PayrollAdmin@aol.com", rsContact.getEmail());
                assertEquals("(775) 333-3333", rsContact.getPrimaryPhone());
            } else
            if ("Primary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PrimaryPrincipal", rsContact.getLastName());
                assertEquals("PrimaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 111-1111", rsContact.getPrimaryPhone());
            } else
            if ("Secondary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("SecondaryPrincipal", rsContact.getLastName());
                assertEquals("SecondaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 222-2222", rsContact.getPrimaryPhone());
            }
        }

        assertNotNull(rsCompany.getServices());
        assertEquals(1, rsCompany.getServices().size());

        for (RSService rsService : rsCompany.getServices()) {
            assertEquals("DIY/DD", rsService.getServiceCd());
            assertEquals("Active", rsService.getStatusCd());
        }
    }

    @Test
    public void getCompanyDD4V() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addEntitlementUnit(company, "1234567890", "123456", EditionType.Basic, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());

        CompanyWS companyWS = new CompanyWS();
        String jsonResponse = companyWS.getCompany(company.getFedTaxId(), DataLoadServices.PIN);

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        assertNotNull(rsResponse.getCompany());
        RSCompany rsCompany = rsResponse.getCompany();

        assertNotNull(rsCompany.getPsid());
        assertNotNull("000000001", rsCompany.getEin());
        assertNotNull("TEST_COMPANY_1", rsCompany.getLegalName());
        assertNotNull("TEST_COMPANY_1", rsCompany.getDba());
        assertNotNull(rsCompany.getSubscriptionNumber());
        assertNotNull(rsCompany.getServiceKey());
        assertNotNull(rsCompany.getNextChargeDate());
        assertEquals("TEST_null@COMPANY.COM", rsCompany.getCompanyEmail());
        assertEquals("John Doe", rsCompany.getBillingContact());
        assertEquals("89511", rsCompany.getBillingZip());
        assertNotNull(rsCompany.getSubscriptionEndDate());
        assertEquals("test@intuit.com", rsCompany.getBillingEmail());
        assertEquals("4263", rsCompany.getCreditCard());
        assertEquals("Visa", rsCompany.getCreditCardType());

        assertNull(rsCompany.getExtensionKey());

        assertNotNull(rsCompany.getBankAccount());
        RSBankAccount rsBankAccount = rsCompany.getBankAccount();

        assertNotNull("ACCNT_1", rsBankAccount.getAccountNumber());
        assertEquals("111000025", rsBankAccount.getRoutingNumber());
        assertNotNull("TestBank_2", rsBankAccount.getBankName());
        assertEquals(RSBankAccountStatusCode.Active, rsBankAccount.getStatus());
        assertEquals(RSBankAccountTypeCode.Checking, rsBankAccount.getType());
        assertNotNull(rsBankAccount.getId());

        assertNotNull(rsCompany.getAddresses());
        for (RSAddress rsAddress : rsCompany.getAddresses()) {
            switch (rsAddress.getAddressType()) {
                case Legal:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
                case Mailing:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
            }

        }

        assertNotNull(rsCompany.getContacts());

        for (RSContact rsContact : rsCompany.getContacts()) {
            if ("Other".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("Other", rsContact.getLastName());
                assertEquals("Other@aol.com", rsContact.getEmail());
                assertEquals("(775) 444-4444", rsContact.getPrimaryPhone());
            } else
            if ("Payroll Admin".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PayrollAdmin", rsContact.getLastName());
                assertEquals("PayrollAdmin@aol.com", rsContact.getEmail());
                assertEquals("(775) 333-3333", rsContact.getPrimaryPhone());
            } else
            if ("Primary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PrimaryPrincipal", rsContact.getLastName());
                assertEquals("PrimaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 111-1111", rsContact.getPrimaryPhone());
            } else
            if ("Secondary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("SecondaryPrincipal", rsContact.getLastName());
                assertEquals("SecondaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 222-2222", rsContact.getPrimaryPhone());
            }
        }

        assertNotNull(rsCompany.getServices());
        assertEquals(2, rsCompany.getServices().size());

        RSService rsService = rsCompany.getServices().get(0);
        assertEquals("DIY/DD", rsService.getServiceCd());
        assertEquals("Active", rsService.getStatusCd());

        rsService = rsCompany.getServices().get(1);
        assertEquals("Bill Payment", rsService.getServiceCd());
        assertEquals("Active", rsService.getStatusCd());
    }

    @Test
    public void getCompanyAssisted() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit, ServiceCode.Tax, ServiceCode.CheckDistribution);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);

        DataLoadServices.addEntitlementUnit(company, "1234567890", "123456", EditionType.Basic, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());

        CompanyWS companyWS = new CompanyWS();
        String jsonResponse = companyWS.getCompany(company.getFedTaxId(), DataLoadServices.PIN);

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        assertNotNull(rsResponse.getCompany());
        RSCompany rsCompany = rsResponse.getCompany();

        assertNotNull(rsCompany.getPsid());
        assertNotNull("000000001", rsCompany.getEin());
        assertNotNull("TEST_COMPANY_1", rsCompany.getLegalName());
        assertNotNull("TEST_COMPANY_1", rsCompany.getDba());
        assertNotNull(rsCompany.getSubscriptionNumber());
        assertNotNull(rsCompany.getServiceKey());
        assertNotNull(rsCompany.getNextChargeDate());
        assertEquals("TEST_null@COMPANY.COM", rsCompany.getCompanyEmail());
        assertEquals("John Doe", rsCompany.getBillingContact());
        assertEquals("89511", rsCompany.getBillingZip());
        assertNotNull(rsCompany.getSubscriptionEndDate());
        assertEquals("test@intuit.com", rsCompany.getBillingEmail());
        assertEquals("4263", rsCompany.getCreditCard());
        assertEquals("Visa", rsCompany.getCreditCardType());

        assertNull(rsCompany.getExtensionKey());

        assertNotNull(rsCompany.getBankAccount());
        RSBankAccount rsBankAccount = rsCompany.getBankAccount();

        assertNotNull("ACCNT_1", rsBankAccount.getAccountNumber());
        assertEquals("111000025", rsBankAccount.getRoutingNumber());
        assertNotNull("TestBank_2", rsBankAccount.getBankName());
        assertEquals(RSBankAccountStatusCode.Active, rsBankAccount.getStatus());
        assertEquals(RSBankAccountTypeCode.Checking, rsBankAccount.getType());
        assertNotNull(rsBankAccount.getId());

        assertNotNull(rsCompany.getAddresses());
        for (RSAddress rsAddress : rsCompany.getAddresses()) {
            switch (rsAddress.getAddressType()) {
                case Legal:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
                case Mailing:
                    assertEquals("COMAIL_AddressLine1", rsAddress.getAddressLine1());
                    assertEquals("COMAIL_AddressLine2", rsAddress.getAddressLine2());
                    assertEquals("Ridgewood", rsAddress.getCity());
                    assertEquals("NJ", rsAddress.getState());
                    assertEquals("07450", rsAddress.getZip());
                    break;
            }
        }

        assertNotNull(rsCompany.getContacts());

        for (RSContact rsContact : rsCompany.getContacts()) {
            if ("Other".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("Other", rsContact.getLastName());
                assertEquals("Other@aol.com", rsContact.getEmail());
                assertEquals("(775) 444-4444", rsContact.getPrimaryPhone());
            } else
            if ("Payroll Admin".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PayrollAdmin", rsContact.getLastName());
                assertEquals("PayrollAdmin@aol.com", rsContact.getEmail());
                assertEquals("(775) 333-3333", rsContact.getPrimaryPhone());
            } else
            if ("Primary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("PrimaryPrincipal", rsContact.getLastName());
                assertEquals("PrimaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 111-1111", rsContact.getPrimaryPhone());
            } else
            if ("Secondary Principal".equals(rsContact.getContactType())) {
                assertEquals("Johnny", rsContact.getFirstName());
                assertEquals("Q", rsContact.getMiddleName());
                assertEquals("SecondaryPrincipal", rsContact.getLastName());
                assertEquals("SecondaryPrincipal@aol.com", rsContact.getEmail());
                assertEquals("(775) 222-2222", rsContact.getPrimaryPhone());
            }
        }

        assertNotNull(rsCompany.getServices());
        assertEquals(2, rsCompany.getServices().size());

        RSService rsService = rsCompany.getServices().get(0);
        assertEquals("Assisted", rsService.getServiceCd());
        assertEquals("Active", rsService.getStatusCd());

        rsService = rsCompany.getServices().get(1);
        assertEquals("Check Distribution", rsService.getServiceCd());
        assertEquals("Active", rsService.getStatusCd());
    }

}
