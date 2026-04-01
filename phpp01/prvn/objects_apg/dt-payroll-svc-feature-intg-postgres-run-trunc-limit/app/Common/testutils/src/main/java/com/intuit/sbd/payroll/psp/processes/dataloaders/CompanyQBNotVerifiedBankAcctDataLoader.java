package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jun 9, 2008
 * Time: 10:17:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyQBNotVerifiedBankAcctDataLoader {
    public static final String COMPANY_PSID = "8574536";
    private Company company;
    private CompanyBankAccount bankAccount1;

    private DataLoader dataloader = new DataLoader();
    private String sourceCoId = null;

    public Company persistQBCompanyNotVerifiedCoBankAcct() {
        return persistQBCompanyNotVerifiedCoBankAcct(COMPANY_PSID);
    }

    public Company persistQBCompanyNotVerifiedCoBankAcct(String pPsid) {

        sourceCoId = pPsid;
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");

        company1.getLegalAddress().setCity("Honolulu");
        company1.getLegalAddress().setState("HI");
        company1.getLegalAddress().setZipCode("96813");


        QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();

        qbInfoDTO.setCoaFeeAccountName(QBOFX.ACCOUNTS.DEFAULT_FEE_ACCOUNT_NAME);
        qbInfoDTO.setCoaSalesTaxAccountName(QBOFX.ACCOUNTS.DEFAULT_SALES_TAX_ACCOUNT_NAME);

        company1.setQuickBooksInfo(qbInfoDTO);
        company = dataloader.persistCompany(company1);

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, getCompany1Service());

        DataLoader dl = new DataLoader();
        CompanyBankAccountDTO companyBankAccountDTO = dl.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        bankAccount1 = addCBAProcResult.getResult();

//        persistCompanyPIN();
        company = Company
                .findCompany(sourceCoId, SourceSystemCode.QBDT);
        company = Company
                .findCompany(sourceCoId, SourceSystemCode.QBDT);
        company = Company
                .findCompany(sourceCoId, SourceSystemCode.QBDT);

        return company;
    }

    public CompanyDTO getCompany1() {
        CompanyDTO comp = new CompanyDTO();
        comp.setDBA("QB Desktop Company 3");
        comp.setFein("242335465");
        comp.setLegalAddress(getTestLegalAddress());
        comp.setLegalName("QB Desktop 3");
        comp.setMailingAddress(getTestMailingAddress());
        comp.setNotificationEmail("notifications3@intuit.com");
        comp.setCompanyId(sourceCoId);
        comp.setPriceType("Standard");

        ContactDTO contact = getTestContact();
        Collection<ContactDTO> allContactsForCompany = new ArrayList();
        allContactsForCompany.add(contact);
        comp.setContacts(allContactsForCompany);
        comp.setSourceSystemCd(SourceSystemCode.QBDT);
//        comp.setPayrollFrequency(
//                (PayrollFrequency) PayrollServices.entityFinder.findById(PayrollFrequency.class, PayrollFrequencyBE.Codes.MONTHLY));

        return comp;
    }

    private AddressDTO getTestLegalAddress() {
        AddressDTO legalAddress = new AddressDTO();
        legalAddress.setAddressLine1("7789 Sierra Cnt Pkwy");
        legalAddress.setCity("Reno");
        legalAddress.setZipCode("89511");
        legalAddress.setState("NV");
        return legalAddress;
    }

    private AddressDTO getTestMailingAddress() {
        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("6887 Sierra Center Parkway");
        mailingAddress.setAddressLine2("Suite 48");
        mailingAddress.setAddressLine3("test line 3");
        mailingAddress.setCity("Reno");
        mailingAddress.setZipCode("89521");
        mailingAddress.setState("NV");
        return mailingAddress;
    }

    public ContactDTO getTestContact() {
        ContactDTO contact = new ContactDTO();

        contact.setFirstName("Johnno");
        contact.setMiddleName("P");
        contact.setLastName("Doeyy");
        contact.setPhoneNumber("(775) 424-9339");
        contact.setContactRoleCd(ContactRole.PayrollAdmin);
        contact.setAccountSignatory(Boolean.TRUE);
        contact.setEmail("someEmail1234@aol.com");

        AddressDTO contactAddr = new AddressDTO();
        contactAddr.setAddressLine1("1234 High Country Rd");
        contactAddr.setCity("Reno");
        contactAddr.setState("NV");
        contactAddr.setZipCode("89502");
        contact.setAddress(contactAddr);
        contact.setContactId(DataLoader.generateContactKey(contact));
        return contact;
    }

    public DDServiceInfoDTO getCompany1Service() {
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal("151.00"));

        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal("252.00"));

        return ddCompanyService;
    }

    public CompanyBankAccountDTO getCompany1BankAccount() {
        CompanyBankAccountDTO retBA = new CompanyBankAccountDTO();
        retBA.setCompanyBankAccountID("C1BA1");

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber("4847474747");
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName("Bank of America");
        bankAccountDTO.setRoutingNumber("111000025");

        retBA.setBankAccountDTO(bankAccountDTO);
        return retBA;
    }

    public Employee getEmployee1(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE1_1");
        incEmployee.setFirstName("ThirdCompEEFirst");
        incEmployee.setLastName("ThirdCompEELast");
        incEmployee.setMiddleName("TMI");
        incEmployee.setEmail("test3@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8015551212");
        incEmployee.setTaxId("111225333");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(PSPDate.getPSPTime());
        incEmployee.setCompany(pCompany);
        return incEmployee;
    }

    public Employee getEmployee2(Company pCompany) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE2_1");
        incEmployee.setFirstName("ThirdCompEEFirstTwo");
        incEmployee.setLastName("ThirdCompEELastTwo");
        incEmployee.setMiddleName("TMI2");
        incEmployee.setEmail("test3@test32email.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("8102551212");
        incEmployee.setTaxId("212223333");
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(SpcfCalendar.getNow());
        incEmployee.setCompany(pCompany);
        return incEmployee;
    }

    public Employee getEmployee(Company pCompany, String id) {
        Employee incEmployee = new Employee();
        incEmployee.setSourceEmployeeId("EE"+id);
        incEmployee.setFirstName("FirstNameOfEE"+id);
        incEmployee.setLastName("TestLastName"+id);
        incEmployee.setMiddleName("TMI"+id);
        incEmployee.setEmail("test"+id+"@testemail.com");
        incEmployee.setGenderCd(Gender.Male);
        incEmployee.setPhone("800255121"+id);
        incEmployee.setTaxId("22222334"+id);
        incEmployee.setStatusCd(EmployeeStatus.Active);
        incEmployee.setStatusEffectiveDate(SpcfCalendar.getNow());
        incEmployee.setCompany(pCompany);
        return incEmployee;
    }

    public CompanyBankAccountDTO createCompanyBankAccountDTO(CompanyBankAccount pCompanyBankAccount) {

        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccount.getSourceBankAccountId());
        companyBankAccountDTO.setBankAccountDTO(createBankAccountDTO(pCompanyBankAccount.getBankAccount()));
        return companyBankAccountDTO;
    }

    public EmployeeBankAccountDTO createEmployeeBankAccountDTONoBADTO(EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.getSourceBankAccountId());
        return employeeBankAccountDTO;
    }

    public EmployeeBankAccountDTO createEmployeeBankAccountDTO(EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setBankAccount(createBankAccountDTO(pEmployeeBankAccount.getBankAccount()));
        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.getSourceBankAccountId());
        return employeeBankAccountDTO;
    }

    public BankAccountDTO createBankAccountDTO(BankAccount pBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(pBankAccount.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccount.getRoutingNumber());
        bankAccountDTO.setBankName(pBankAccount.getBankName());
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pBankAccount.getAccountTypeCd().toString()));
        return bankAccountDTO;
    }

    public ServiceBankAccountDTO createServiceBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                             ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);
        return serviceBankAccountDTO;
    }

    public void setCompany1(Company pCompany) {
        company = pCompany;
    }

    public void updateTo2DayFundingModel() {
        FundingModel twoDay = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                                                        company.getSourceCompanyId(), twoDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateTo5DayFundingModel() {
        FundingModel fiveDay = Application.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);
        ProcessResult procResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBDT,
                                                        company.getSourceCompanyId(), fiveDay);
        PayrollServicesTest.assertSuccess("updateCompanyFundingModel", procResult);
    }

    public void updateLimits(SpcfMoney pNewLimit) {
        ProcessResult procResult = PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT,
                                                        company.getSourceCompanyId(), pNewLimit, pNewLimit);
        PayrollServicesTest.assertSuccess("updateDDLimits", procResult);
    }

    public Company getCompany() {
        return company;
    }
}
