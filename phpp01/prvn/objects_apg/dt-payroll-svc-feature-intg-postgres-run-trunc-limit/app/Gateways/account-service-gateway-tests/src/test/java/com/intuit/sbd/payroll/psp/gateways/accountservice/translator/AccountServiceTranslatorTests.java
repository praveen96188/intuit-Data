package com.intuit.sbd.payroll.psp.gateways.accountservice.translator;

import com.intuit.payments.cdm.v2.client.BankAccount;
import com.intuit.payments.cdm.v2.client.BusinessOwner;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PhysicalAddress;
import com.intuit.payments.cdm.v2.client.enums.BankAccountTypeEnum;
import com.intuit.payments.cdm.v2.client.enums.BankAccountUsageTypeEnum;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccountServiceTranslatorTests {
    private static final String ADDRESS_SEPARATOR_1 = "\r";
    private static final String ADDRESS_SEPARATOR_2 = "\r\n";

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testSplitPaymentsBothAddressSeparator1() {
        AddressDTO addressDTO = new AddressDTO();
        PhysicalAddress paymentsAddress = new PhysicalAddress();

        String streetAddressLine1 = "Address Line 1";
        String streetAddressLine2 = "Address Line 2";

        String completeStreetAddress = streetAddressLine1 + ADDRESS_SEPARATOR_1 + streetAddressLine2;

        paymentsAddress.setStreetAddress(completeStreetAddress);

        AccountServiceTranslator accountServiceTranslator = new AccountServiceTranslator();
        accountServiceTranslator.splitPaymentsAddress(paymentsAddress, addressDTO);

        Assert.assertEquals(addressDTO.getAddressLine1(), streetAddressLine1);
        Assert.assertEquals(addressDTO.getAddressLine2(), streetAddressLine2);
    }

    @Test
    public void testSplitPaymentsBothAddressSeparator2() {
        AddressDTO addressDTO = new AddressDTO();
        PhysicalAddress paymentsAddress = new PhysicalAddress();

        String streetAddressLine1 = "Address Line 1";
        String streetAddressLine2 = "Address Line 2";

        String completeStreetAddress = streetAddressLine1 + ADDRESS_SEPARATOR_2 + streetAddressLine2;

        paymentsAddress.setStreetAddress(completeStreetAddress);

        AccountServiceTranslator accountServiceTranslator = new AccountServiceTranslator();
        accountServiceTranslator.splitPaymentsAddress(paymentsAddress, addressDTO);

        Assert.assertEquals(addressDTO.getAddressLine1(), streetAddressLine1);
        Assert.assertEquals(addressDTO.getAddressLine2(), streetAddressLine2);
    }

    @Test
    public void testSplitPaymentsSingleAddress() {
        AddressDTO addressDTO = new AddressDTO();
        PhysicalAddress paymentsAddress = new PhysicalAddress();

        String streetAddressLine1 = "Address Line 1";
        String streetAddressLine2 = null;

        String completeStreetAddress = streetAddressLine1;

        paymentsAddress.setStreetAddress(completeStreetAddress);

        AccountServiceTranslator accountServiceTranslator = new AccountServiceTranslator();
        accountServiceTranslator.splitPaymentsAddress(paymentsAddress, addressDTO);

        Assert.assertEquals(addressDTO.getAddressLine1(), streetAddressLine1);
        Assert.assertEquals(addressDTO.getAddressLine2(), streetAddressLine2);
    }

    @Test
    public void testGetUpdatedCompanyBankAccountDTO() {
        CompanyBankAccount pCompanyBankAccount = new CompanyBankAccount();
        pCompanyBankAccount.setBankAccount(new com.intuit.sbd.payroll.psp.domain.BankAccount());

        PaymentsAccount paymentsAccount = new PaymentsAccount();

        List<BankAccount> bankAccountList = new ArrayList<>();

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUsageType(BankAccountUsageTypeEnum.MONEY_OUT);
        bankAccount.setName("BankName1");
        bankAccount.setAccountNumber("0123456789");
        bankAccount.setRoutingNumber("121231234");
        bankAccount.setType(BankAccountTypeEnum.CHECKING);

        bankAccountList.add(bankAccount);

        paymentsAccount.setBankAccounts(bankAccountList);

        AccountServiceTranslator accountServiceTranslator = new AccountServiceTranslator();
        CompanyBankAccountDTO companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);

        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getBankName(), bankAccount.getName());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(), bankAccount.getRoutingNumber());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountNumber(), bankAccount.getAccountNumber());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Checking);

        bankAccount.setType(BankAccountTypeEnum.SAVINGS);
        companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Savings);

    }

    @Test
    public void testGetUpdatedCompanyBankAccountDTOWithDiffChecking() {
        CompanyBankAccount pCompanyBankAccount = new CompanyBankAccount();
        pCompanyBankAccount.setBankAccount(new com.intuit.sbd.payroll.psp.domain.BankAccount());

        PaymentsAccount paymentsAccount = new PaymentsAccount();

        List<BankAccount> bankAccountList = new ArrayList<>();

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUsageType(BankAccountUsageTypeEnum.MONEY_OUT);
        bankAccount.setName("BankName1");
        bankAccount.setAccountNumber("0123456789");
        bankAccount.setRoutingNumber("121231234");
        bankAccount.setType(BankAccountTypeEnum.SAVINGS);

        bankAccountList.add(bankAccount);

        paymentsAccount.setBankAccounts(bankAccountList);

        AccountServiceTranslator accountServiceTranslator = new AccountServiceTranslator();
        CompanyBankAccountDTO companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);

        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getBankName(), bankAccount.getName());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(), bankAccount.getRoutingNumber());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountNumber(), bankAccount.getAccountNumber());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Savings);

        bankAccount.setType(BankAccountTypeEnum.PERSONAL_CHECKING);
        companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Checking);

        bankAccount.setType(BankAccountTypeEnum.BUSINESS_CHECKING);
        companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Checking);

    }

    @Test
    public void testGetUpdatedCompanyBankAccountDTOWithDiffSavings() {
        CompanyBankAccount pCompanyBankAccount = new CompanyBankAccount();
        pCompanyBankAccount.setBankAccount(new com.intuit.sbd.payroll.psp.domain.BankAccount());

        PaymentsAccount paymentsAccount = new PaymentsAccount();

        List<BankAccount> bankAccountList = new ArrayList<>();

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUsageType(BankAccountUsageTypeEnum.MONEY_OUT);
        bankAccount.setName("BankName1");
        bankAccount.setAccountNumber("0123456789");
        bankAccount.setRoutingNumber("121231234");
        bankAccount.setType(BankAccountTypeEnum.CHECKING);

        bankAccountList.add(bankAccount);

        paymentsAccount.setBankAccounts(bankAccountList);

        AccountServiceTranslator accountServiceTranslator = new AccountServiceTranslator();
        CompanyBankAccountDTO companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);

        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getBankName(), bankAccount.getName());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(), bankAccount.getRoutingNumber());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountNumber(), bankAccount.getAccountNumber());
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Checking);

        bankAccount.setType(BankAccountTypeEnum.PERSONAL_SAVINGS);
        companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Savings);

        bankAccount.setType(BankAccountTypeEnum.BUSINESS_SAVINGS);
        companyBankAccountDTO =  accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pCompanyBankAccount);
        Assert.assertEquals(companyBankAccountDTO.getBankAccountDTO().getAccountType(), BankAccountType.Savings);

    }

    @Test
    public void testUpdateContactTO() {
        CompanyDTO companyDTO = new CompanyDTO();

        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setContactRoleCd(ContactRole.PrimaryPrincipal);
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("123");
        addressDTO.setAddressLine2("Bakers street");
        contactDTO.setAddress(addressDTO);
        companyDTO.getContacts().add(contactDTO);

        BusinessOwner owner = new BusinessOwner();
        owner.setFirstName("FirstName");
        owner.setLastName("LastName");
        owner.setPhone("9191292222");
        owner.setSsn("121231234");
        owner.setDateOfBirth(new Date());

        AccountServiceTranslator accountServiceTranslator = new AccountServiceTranslator();

        ContactDTO updatedContactDto =  accountServiceTranslator.updateContactDTO(companyDTO, owner, ContactRole.PrimaryPrincipal, "testEmail123@intuit.com");

        Assert.assertEquals(updatedContactDto.getFirstName(), owner.getFirstName());
        Assert.assertEquals(updatedContactDto.getLastName(), owner.getLastName());
        Assert.assertEquals(updatedContactDto.getPhoneNumber(), owner.getPhone());
        Assert.assertEquals(updatedContactDto.getSocialSecurityNumber(), owner.getSsn());
        Assert.assertEquals(updatedContactDto.getAddress().getAddressLine1(), contactDTO.getAddress().getAddressLine1());
        Assert.assertEquals(updatedContactDto.getAddress().getAddressLine2(), contactDTO.getAddress().getAddressLine2());
    }
}
