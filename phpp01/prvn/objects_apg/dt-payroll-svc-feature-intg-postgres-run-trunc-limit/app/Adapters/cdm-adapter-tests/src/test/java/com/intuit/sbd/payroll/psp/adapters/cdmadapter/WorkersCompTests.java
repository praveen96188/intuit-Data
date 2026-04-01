package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.*;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.AddressDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.CompanyDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.ContactDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.EntitlementDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.manager.WorkersCompManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.resource.CompanyResource;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * Author: Sriram Nutakki
 * Date created: 8/20/13
 */
public class WorkersCompTests {

    @Before
    public void startUp() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void shutdown() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testJaxbMarshalling() throws Exception {

        CompanyDTO company = new CompanyDTO();
        company.setCompanyName("CompanyName");
        company.setCompanyLegalName("CompanyLegalName");
        company.setEin("EIN");
        company.setEmail("Email");
        company.setPhone("Phone");
        company.setPsid("Psid");

        AddressDTO address = new AddressDTO();
        address.setAddressLine1("AddressLine1");
        address.setAddressLine2("AddressLine2");
        address.setAddressLine3("AddressLine3");
        address.setCity("City");
        address.setCountry("Country");
        address.setState("State");
        address.setZipCode("ZipCode");
        address.setZipExtension("ZipExtension");

        ContactDTO contact = new ContactDTO();
        contact.setEmail("Email");
        contact.setFirstName("FirstName");
        contact.setLastName("LatName");
        contact.setName("Name");
        contact.setPhone("Phone");

        EntitlementDTO entitlement = new EntitlementDTO();
        entitlement.setActive(true);
        entitlement.setAssetItemCode("AssetItemCode");
        entitlement.setEditionType("EditionType");
        entitlement.setPrimary(true);

        company.addEntitlement(entitlement);
        company.addEntitlement(entitlement);
        company.setContact(contact);
        company.setAddress(address);

        String xml = toXML(company);
        Assert.assertNotNull(xml);

        CompanyListDTO companyList = new CompanyListDTO();
        companyList.addCompany(company);
        companyList.addCompany(company);
        xml = toXML(companyList);
        Assert.assertNotNull(xml);
    }

    @Test
    public void testGetPayrollCompany() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company,
                                                                              "1234567890",
                                                                              "123456",
                                                                              EditionType.Basic,
                                                                              NumberOfEmployeesType.UNLIMITED,
                                                                              DataLoadServices.AssetItemNumber.DIY_YEARLY,
                                                                              PSPDate.getPSPTime(),
                                                                              "4263",
                                                                              "Visa",
                                                                              "03/16",
                                                                              "89511",
                                                                              "John Doe",
                                                                              "test@intuit.com",
                                                                              PSPDate.getPSPTime());
        Entitlement entitlement = entitlementUnit.getEntitlement();
        String subscriptionNumber = entitlement.getSubscriptionNumber();
        String ein = entitlementUnit.getFedTaxId();
        WorkersCompManager manager = new WorkersCompManager();

        // Get companies by ein
        CompanyListDTO companyListDto = manager.getCompaniesByEIN(ein);
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(1, companyListDto.getCompanies().size());

        // Get companies by ein and subscription number
        companyListDto = manager.getCompaniesByEINAndSubsNum(ein, subscriptionNumber);
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(1, companyListDto.getCompanies().size());

        // Get companies by psid
        CompanyDTO companyDto = manager.getCompanyByPSID(psid);
        Assert.assertNotNull(companyDto);
        Assert.assertEquals(ein, companyDto.getEin());

        // Get company by ein and subscription number
        companyDto = manager.getCompanyByEINAndSubsNum(ein, subscriptionNumber);
        Assert.assertNotNull(companyDto);
        Assert.assertEquals(ein, companyDto.getEin());
    }


    private static String toXML(Object jaxbObject) throws Exception {
        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
        context.createMarshaller().marshal(jaxbObject, writer);
        String out = writer.toString();
        return out;
    }

    @Test
    public void testMultipleProfileWithSameEIN() {
        String psid = "123456788";
        DataLoadServices.setPSPDate(2015, 12, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", true, ServiceCode.Cloud);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company,
                                                                              "1234567890",
                                                                              "123456",
                                                                              EditionType.Basic,
                                                                              NumberOfEmployeesType.UNLIMITED,
                                                                              DataLoadServices.AssetItemNumber.DIY_YEARLY,
                                                                              PSPDate.getPSPTime(),
                                                                              "4263",
                                                                              "Visa",
                                                                              "03/16",
                                                                              "89511",
                                                                              "John Doe",
                                                                              "test@intuit.com",
                                                                              PSPDate.getPSPTime());
        Entitlement entitlement = entitlementUnit.getEntitlement();
        String subscriptionNumber = entitlement.getSubscriptionNumber();
        String ein = entitlementUnit.getFedTaxId();
        WorkersCompManager manager = new WorkersCompManager();

        // Get companies by ein
        CompanyListDTO companyListDto = manager.getCompaniesByEIN(ein);
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(1, companyListDto.getCompanies().size());

        // Get companies by ein and subscription number
        companyListDto = manager.getCompaniesByEINAndSubsNum(ein, subscriptionNumber);
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(1, companyListDto.getCompanies().size());

        // Get companies by psid
        CompanyDTO companyDto = manager.getCompanyByPSID(psid);
        Assert.assertNotNull(companyDto);
        Assert.assertEquals(ein, companyDto.getEin());

        // Get company by ein and subscription number
        companyDto = manager.getCompanyByEINAndSubsNum(ein, subscriptionNumber);
        Assert.assertNotNull(companyDto);
        Assert.assertEquals(ein, companyDto.getEin());
        DataLoadServices.setPSPDate(2015, 12, 28);
        DataLoadServices.disableEntitlement(entitlementUnit.getEntitlement());
        psid = "123456789";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, "987654321", true, ServiceCode.Cloud);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        entitlementUnit = DataLoadServices.addEntitlementUnit(company,
                                                              "1234567891",
                                                              "123457",
                                                              EditionType.Basic,
                                                              NumberOfEmployeesType.UNLIMITED,
                                                              DataLoadServices.AssetItemNumber.DIY_YEARLY,
                                                              PSPDate.getPSPTime(),
                                                              "4263",
                                                              "Visa",
                                                              "03/16",
                                                              "89511",
                                                              "John Doe",
                                                              "test@intuit.com",
                                                              PSPDate.getPSPTime());
        entitlement = entitlementUnit.getEntitlement();
        DataLoadServices.activateEntitlementUnit(entitlementUnit);
        String newSubscriptionNumber = entitlement.getSubscriptionNumber();
        ein = entitlementUnit.getFedTaxId();

        // Get companies by ein
        companyListDto = manager.getCompaniesByEIN(ein);
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(2, companyListDto.getCompanies().size());

        // Get companies by ein and subscription number
        companyListDto = manager.getCompaniesByEINAndSubsNum(ein, newSubscriptionNumber);
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(1, companyListDto.getCompanies().size());

        // Get companies by psid
        companyDto = manager.getCompanyByPSID(psid);
        Assert.assertNotNull(companyDto);
        Assert.assertEquals(ein, companyDto.getEin());
        Assert.assertTrue("New company", companyDto.getEntitlements().get(0).isActive());

        // Get company by ein and subscription number
        companyDto = manager.getCompanyByEINAndSubsNum(ein, subscriptionNumber);
        Assert.assertNotNull(companyDto);
        Assert.assertEquals(ein, companyDto.getEin());
        Assert.assertFalse("old company", companyDto.getEntitlements().get(0).isActive());
    }

    @Test
    public void testServiceDowngradeFromAssistedToDIYDD() {   //This is to test multiple entitlements with single profile
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId() + "1";
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId() + "1";

        DataLoadServices.activateDDService(company);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        Entitlement oldEntitlement = null;
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
            EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(entitlementUnit);
            dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
            ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(), entitlementUnit.getCompany().getSourceCompanyId(), dto);
            assertSuccess(processResult);
            oldEntitlement = processResult.getResult().getEntitlement();
        }
        PayrollServices.commitUnitOfWork();
        DataLoadServices.cancelService(company, ServiceCode.Tax);
        //Leaving Cloud active
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        org.junit.Assert.assertFalse("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        org.junit.Assert.assertFalse("Direct Deposit Service", company.isCompanyOnService(ServiceCode.DirectDeposit));
        PayrollServices.rollbackUnitOfWork();

        EntitlementUnit entitlementUnit = DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, null, null);

        DataLoadServices.addDDService(company);
        DataLoadServices.activateEntitlementUnit(entitlementUnit);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        org.junit.Assert.assertFalse("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        org.junit.Assert.assertTrue("Direct Deposit Service", company.isCompanyOnService(ServiceCode.DirectDeposit));
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementDTO.setNumberOfEmployeesType(NumberOfEmployeesType.UPTO3);
        entitlementDTO.setEditionType(EditionType.Basic);
        entitlementDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());

        Entitlement entitlement = DataLoadServices.updateEntitlement(entitlementDTO);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        org.junit.Assert.assertFalse("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        org.junit.Assert.assertTrue("DirectDeposit Service", company.isCompanyOnService(ServiceCode.DirectDeposit));
        PayrollServices.rollbackUnitOfWork();
        WorkersCompManager manager = new WorkersCompManager();
        // Get companies by ein
        CompanyListDTO companyListDto = manager.getCompaniesByEIN(company.getFedTaxId());
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(1, companyListDto.getCompanies().size());
        List<EntitlementDTO> listEntitlementDTO = companyListDto.getCompanies().get(0).getEntitlements();
        String oldSubNo = oldEntitlement.getSubscriptionNumber();
        for (EntitlementDTO entitlementDTO1 : listEntitlementDTO) {
            if (entitlementDTO1.getSubscriptionNumber().equals(oldSubNo)) {
                Assert.assertFalse("Old EntitlementDTO", entitlementDTO1.isActive());
            } else {
                Assert.assertTrue("new EntitlementDTO", entitlementDTO1.isActive());
            }
        }

    }

    @Test
    public void testLastPayrollDetailsWithLiabAdjAndVoidPayroll() {
        DataLoadServices.setPSPDate(2015, 12, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);
        DataLoadServices.setPSPDate(2015, 12, 12);
        DateDTO payrollRunDate = new DateDTO("2015-12-12");

        //Run payroll with active service
        DateDTO payrollDate = new DateDTO("2015-12-12");
        DataLoadServices.runPayrollRun(company, new String[]{"PA"}, SpcfCalendar.createInstance(2011, 1, 1), payrollDate, false);
        DataLoadServices.setPSPDate(2015, 12, 20);
        payrollRunDate = new DateDTO("2015-12-20");
        //Run payroll with active service
        payrollDate = new DateDTO("2015-12-20");
        DataLoadServices.runPayrollRun(company, new String[]{"PA"}, SpcfCalendar.createInstance(2011, 1, 1), payrollDate, false);
        DataLoadServices.setPSPDate(2015, 12, 25);
        //Create liab adj
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO("1", "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO("61", "61", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO("40", "40", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("100.11"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .addLiabilityAdjustments(SourceSystemCode.QBDT, company.getSourceCompanyId(), null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(2016, 1, 2);
        //void latest payroll
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findLastPayrollRunWithActivePaychecks(company);
        PayrollServices.commitUnitOfWork();
        voidCompletePayrollRun(payrollRun);
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findLastPayrollRunWithActivePaychecks(company);
        PayrollServices.commitUnitOfWork();
        WorkersCompManager manager = new WorkersCompManager();
        // Get companies by ein
        CompanyListDTO companyListDto = manager.getCompaniesByEIN(company.getFedTaxId());
        Assert.assertNotNull(companyListDto);
        Assert.assertEquals(1, companyListDto.getCompanies().size());
        CompanyDTO cto = companyListDto.getCompanies().get(0);
        Assert.assertEquals("last paycheckdate", payrollRun.getPaycheckDate().format("yyyyMMdd"), "20151212");
        Assert.assertEquals("last paycheckdate", payrollRun.getPaycheckDate(), SpcfCalendar.createInstance(cto.getLastPaycheckDate().getTime()));
        Assert.assertEquals("last payrollrundate", payrollRun.getPayrollRunDate().format("yyyyMMdd"), "20151212");
        Assert.assertEquals("last payrollrundate", payrollRun.getPayrollRunDate(), SpcfCalendar.createInstance(cto.getLastPayrollRunDate().getTime()));
    }

    private static void voidCompletePayrollRun(PayrollRun payrollRun) {
        PayrollServices.beginUnitOfWork();
        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(PayrollRun.Id().equalTo(payrollRun.getId()))
                        .EagerLoad(PayrollRun.PaycheckSet(), PayrollRun.LiabilityAdjustmentSet(), PayrollRun.BillPaymentSet(), PayrollRun.BillingDetailSet(), PayrollRun.LiabilityCheckSet(), PayrollRun.FinancialTransactionSet(), PayrollRun.CompanyServiceBankAccountSet());

        DomainEntitySet<PayrollRun> companyPayrollRuns = Application.find(PayrollRun.class, query);
        payrollRun = companyPayrollRuns.getFirst();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        for (Paycheck paycheckt : payrollRun.getPaycheckCollection()) {
            voidPaychecks.add(paycheckt.getSourcePaycheckId());
        }

        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));

        PayrollServices.commitUnitOfWork();
    }
}
