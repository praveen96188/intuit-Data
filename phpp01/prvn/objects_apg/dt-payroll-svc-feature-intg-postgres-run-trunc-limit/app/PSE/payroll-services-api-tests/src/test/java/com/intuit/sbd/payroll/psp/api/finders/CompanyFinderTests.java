package com.intuit.sbd.payroll.psp.api.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QuickbooksInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Aug 6, 2008
 * Time: 2:22:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyFinderTests {

    private static String TEST_EIN = "123456789";
    private static String TEST_PSID = "123456";
    private static String TEST_LEGAL_NAME = "Intuit";

    @Before
    public void runBeforeEachTest() {
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        AddCompanyDataLoader.dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    private String loadData() {
        DataLoader loader = new DataLoader();
        loader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
        Company qboe = loader.persistTestIntuitCompany();
        Assert.assertEquals("QBOE company's SourceSystemCode", SourceSystemCode.QBOE, qboe.getSourceSystemCd());
        loader.persistTestCompanyService(qboe);

        loader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Company qbdt = loader.persistTestIntuitCompany();
        Assert.assertEquals("QBDT company's SourceSystemCode", SourceSystemCode.QBDT, qbdt.getSourceSystemCd());
        loader.persistTestCompanyService(qbdt);

        Assert.assertEquals("Companies have same EIN", qboe.getFedTaxId(), qbdt.getFedTaxId());

        return qboe.getFedTaxId();
    }

    @Test
    public void findActiveCompany() {
        // Finds the active company with the given SourceSystemCode and EIN, if there is one.
        PayrollServices.beginUnitOfWork();
        String ein = loadData();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company qboe = Company.findActiveCompany(SourceSystemCode.QBOE,  ein);
        Company qbdt = Company.findActiveCompany(SourceSystemCode.QBDT,  ein);
        PayrollServices.commitUnitOfWork();

        Assert.assertTrue("QBOE company found", qboe!=null);
        Assert.assertEquals("QBOE company's SourceSystemCode", SourceSystemCode.QBOE, qboe.getSourceSystemCd());
        Assert.assertTrue("QBDT company found", qbdt!=null);
        Assert.assertEquals("QBDT company's SourceSystemCode", SourceSystemCode.QBDT, qbdt.getSourceSystemCd());
    }

    @Test
    public void findCompanyByEINCompanyTerminated() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), AddCompanyDataLoader.dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.terminateService(SourceSystemCode.QBOE,
                "123456", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertSuccess(servicePR);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByEIN(TEST_EIN);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("EIN Check", TEST_EIN, resultingCompany.getFedTaxId());
        CompanyService service = resultingCompany.getCompanyServiceCollection().get(0);
        assertEquals(ServiceSubStatusCode.Terminated, service.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByEINCompanyCancelled() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), AddCompanyDataLoader.dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult cancelResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123456", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertSuccess(cancelResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByEIN(TEST_EIN);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("EIN Check", TEST_EIN, resultingCompany.getFedTaxId());
        CompanyService service = resultingCompany.getCompanyServiceCollection().get(0);
        assertEquals(ServiceSubStatusCode.Cancelled, service.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByEINCompanyActive() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), AddCompanyDataLoader.dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByEIN(TEST_EIN);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("EIN Check", TEST_EIN, resultingCompany.getFedTaxId());
        CompanyService service = resultingCompany.getCompanyServiceCollection().get(0);
        assertEquals(ServiceSubStatusCode.PendingBankVerification, service.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByEINCompanyDGDeleted() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), AddCompanyDataLoader.dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.terminateService(SourceSystemCode.QBOE,
                "123456", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertSuccess(servicePR);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByEIN(TEST_EIN);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("EIN Check", TEST_EIN, resultingCompany.getFedTaxId());
        CompanyService service = resultingCompany.getCompanyServiceCollection().get(0);
        assertEquals(ServiceSubStatusCode.Terminated, service.getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByEINForDgDeletedCompaniesAsAdmin() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByEIN(TEST_EIN);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByEINForDgDeletedCompaniesAsNonAdmin() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByEIN(TEST_EIN);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByEINForDgDeletedCompaniesForNonSAPFlow() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByEIN(TEST_EIN);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyBySourceCompanyIDForDgDeletedCompaniesAsAdmin() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesBySourceCompanyId(TEST_PSID);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyBySourceCompanyIDForDgDeletedCompaniesAsNonAdmin() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesBySourceCompanyId(TEST_PSID);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyBySourceCompanyIDForDgDeletedCompaniesForNonSAPFlow() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesBySourceCompanyId(TEST_PSID);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByServiceKeyForDgDeletedCompaniesAsAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String serviceKey = entitlementUnit.getFullServiceKey();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByServiceKey(serviceKey);



        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByServiceKeyForDgDeletedCompaniesAsNonAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String serviceKey = entitlementUnit.getFullServiceKey();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByServiceKey(serviceKey);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByServiceKeyForDgDeletedCompaniesForNonSAPFlow() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String serviceKey = entitlementUnit.getFullServiceKey();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByServiceKey(serviceKey);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByLicenseNumberForDgDeletedCompaniesAsAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByLicenseNumber(licenseNumber);



        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByLicenseNumberForDgDeletedCompaniesAsNonAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByLicenseNumber(licenseNumber);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByLicenseNumberForDgDeletedCompaniesForNonSAPFlow() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByLicenseNumber(licenseNumber);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByCANForDgDeletedCompaniesAsAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String can = entitlementUnit.getEntitlement().getCustomerId();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByCAN(can);



        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByCANForDgDeletedCompaniesAsNonAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String can = entitlementUnit.getEntitlement().getCustomerId();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByCAN(can);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByCANForDgDeletedCompaniesForNonSAPFlow() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String can = entitlementUnit.getEntitlement().getCustomerId();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByCAN(can);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByRegNumForDgDeletedCompaniesAsAdmin() {

        String licenseNumber = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        QuickbooksInfoDTO quickbooksInfo = new QuickbooksInfoDTO();
        quickbooksInfo.setLicenseNumber(licenseNumber);
        company1.setQuickBooksInfo(quickbooksInfo);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByRegistrationNumber(licenseNumber);



        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByRegNumForDgDeletedCompaniesAsNonAdmin() {

        String licenseNumber = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        QuickbooksInfoDTO quickbooksInfo = new QuickbooksInfoDTO();
        quickbooksInfo.setLicenseNumber(licenseNumber);
        company1.setQuickBooksInfo(quickbooksInfo);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByRegistrationNumber(licenseNumber);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByRegNumForDgDeletedCompaniesForNonSAPFlow() {

        String licenseNumber = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        QuickbooksInfoDTO quickbooksInfo = new QuickbooksInfoDTO();
        quickbooksInfo.setLicenseNumber(licenseNumber);
        company1.setQuickBooksInfo(quickbooksInfo);

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByRegistrationNumber(licenseNumber);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByRealmIdForDgDeletedCompaniesAsAdmin() {

        String realmId = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIAMRealmId(realmId);
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.findAllCompaniesByRealmId(realmId);



        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByRealmIdForDgDeletedCompaniesAsNonAdmin() {

        String realmId = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIAMRealmId(realmId);
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.findAllCompaniesByRealmId(realmId);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByRealmIdForDgDeletedCompaniesForNonSAPFlow() {

        String realmId = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIAMRealmId(realmId);
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.findAllCompaniesByRealmId(realmId);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingSourceCompanyIDForDgDeletedCompaniesAsAdmin() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(TEST_PSID);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingSourceCompanyIDForDgDeletedCompaniesAsNonAdmin() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(TEST_PSID);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingSourceCompanyIDForDgDeletedCompaniesForNonSAPFlow() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(TEST_PSID);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingFedTaxIdIDForDgDeletedCompaniesAsAdmin() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        String fedTaxId = result.getResult().getFedTaxId();
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(fedTaxId);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingFedTaxIdIDForDgDeletedCompaniesAsNonAdmin() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        String fedTaxId = result.getResult().getFedTaxId();
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(fedTaxId);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingFedTaxIdIDForDgDeletedCompaniesForNonSAPFlow() {

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        String fedTaxId = result.getResult().getFedTaxId();
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.disassociateCompanyByPSID(company1.getCompanyId(),company1.getSourceSystemCd(), SpcfUniqueId.generateRandomUniqueIdString(), SpcfCalendar.createInstance().toString());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(fedTaxId);

        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingRealmIdForDgDeletedCompaniesAsAdmin() {

        String realmId = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIAMRealmId(realmId);
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(realmId);



        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingRealmIdForDgDeletedCompaniesAsNonAdmin() {

        String realmId = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIAMRealmId(realmId);
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(realmId);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingRealmIdForDgDeletedCompaniesForNonSAPFlow() {

        String realmId = "12345678901234567890";

        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();
        assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        company.setIAMRealmId(realmId);
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(realmId);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingServiceKeyForDgDeletedCompaniesAsAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("Admin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String serviceKey = entitlementUnit.getFullServiceKey();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(serviceKey);



        // Verify the EIN is correct and that this company was terminated
        Assert.assertEquals("QBOE company found", 1, searchedCompanies.size());
        Company resultingCompany = searchedCompanies.get(0);
        assertEquals("DG Disassociated flag check", true, resultingCompany.getIsDgDisassociated());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingServiceKeyForDgDeletedCompaniesAsNonAdmin() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String serviceKey = entitlementUnit.getFullServiceKey();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(serviceKey);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void findCompanyByAnythingUsingServiceKeyForDgDeletedCompaniesForNonSAPFlow() {

        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mock_id", Collections.singletonList("RTBAdmin"),"mock","name");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),company.getSourceSystemCompanyId(),entitlementUnitDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode),company.getFedTaxId());
        String serviceKey = entitlementUnit.getFullServiceKey();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        company.setIsDgDisassociated(true);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PspPrincipal pspPrincipal = new PspPrincipal("mock_id","mock name");
        Application.setCurrentPrincipal(pspPrincipal);
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> searchedCompanies = Company.searchCompaniesByAnything(serviceKey);

        Assert.assertEquals("QBOE company found", 0, searchedCompanies.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testIsEINInUse() {
        assertFalse(Company.isEINInUse("123456789"));

        String psid = PayrollServices.companyManager.createSourceCompanyId(SourceSystemCode.QBDT);
        Company ein1a = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "123456789", false, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        assertTrue(Company.isEINInUse("123456789"));

        PayrollServices.beginUnitOfWork();
        Application.refresh(ein1a);
        cancel(ein1a, ServiceCode.DirectDeposit);
        cancel(ein1a, ServiceCode.Cloud);
        EntitlementUnit entitlementUnit = ein1a.getActivePrimaryEntitlementUnit();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        assertFalse(Company.isEINInUse("123456789"));
        
        DataLoadServices.addEntitlementUnit(ein1a, "lic1", "eoc1");

        assertTrue(Company.isEINInUse("123456789"));

        PayrollServices.beginUnitOfWork();
        activate(ein1a, ServiceCode.DirectDeposit);
        activate(ein1a, ServiceCode.Cloud);
        PayrollServices.commitUnitOfWork();        

        PayrollServices.beginUnitOfWork();
        Application.refresh(ein1a);
        CompanyDTO dto1a = PayrollServices.dtoFactory.create(ein1a);
        dto1a.setFein("987654321");
        assertSuccess(PayrollServices.companyManager.updateCompany(ein1a.getSourceSystemCd(), ein1a.getSourceCompanyId(), dto1a));
        PayrollServices.commitUnitOfWork();

        assertFalse(Company.isEINInUse("123456789"));
        assertTrue(Company.isEINInUse("987654321"));

        psid = PayrollServices.companyManager.createSourceCompanyId(SourceSystemCode.QBDT);
        Company ein1b = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "123456789", false, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(ein1b, "lic1", "eoc1");

        assertTrue(Company.isEINInUse("123456789"));
    }

    private static void cancel(Company company, ServiceCode service) {
        assertSuccess(PayrollServices.companyManager.updateSubStatuses(company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                service,
                Application.<ServiceSubStatus>find(ServiceSubStatus.class, ServiceSubStatus.ServiceSubStatusCd().equalTo(ServiceSubStatusCode.Cancelled))));
    }

    private static void activate(Company company, ServiceCode service) {
        assertSuccess(PayrollServices.companyManager.updateSubStatuses(company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                service,
                Application.<ServiceSubStatus>find(ServiceSubStatus.class, ServiceSubStatus.ServiceSubStatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent))));
    }
}
