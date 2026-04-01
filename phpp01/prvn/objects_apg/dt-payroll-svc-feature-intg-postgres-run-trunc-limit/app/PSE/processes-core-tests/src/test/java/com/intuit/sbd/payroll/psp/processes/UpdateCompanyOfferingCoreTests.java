package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * User: ihannur
 * Date: 8/1/12
 * Time: 9:57 AM
 */
public class UpdateCompanyOfferingCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        String psid = "123456789";
        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testAddTaxServiceWithoutEntitlement() {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setPriceType("Standard");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        TaxServiceInfoDTO taxServiceInfoDTO = new TaxServiceInfoDTO();
        taxServiceInfoDTO.setServiceStartDate(PSPDate.getPSPTime());
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> pr = PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), taxServiceInfoDTO);

        assertEquals("Update company offering result", 1, pr.getErrorMessages().size());
        assertEquals("Error Code", "1068", pr.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message", "Can not find default offering for this Company " + company.getSourceCompanyId() + " and Service Code Tax.", pr.getErrorMessages().get(0).getMessage());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testUpdateCompanyOfferingWithoutEntitlement() {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setPriceType("Standard");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals("Company Offerings", 1, company.getCompanyOfferingCollection().size());
        CompanyOffering companyOffering = company.getDirectDepositCompanyOffering();
        assertEquals("Company Offering Code", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());

        Offering offering = Offering.findBySKU("APAV-135-2");
        OfferingInfoDTO offeringInfoDTO = PayrollServices.dtoFactory.create(offering);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit entitlementUnit = assertOne(company.getEntitlementUnitCollection());
        Application.delete(entitlementUnit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateCompanyOffering(SourceSystemCode.QBDT, psid, offeringInfoDTO);
        assertEquals("Update company offering result", 1, result.getErrorMessages().size());
        assertEquals("Error Code", "326", result.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message", "Company " + company.getSourceSystemCompanyId() + " does not have active Primary EntitlementUnit.", result.getErrorMessages().get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAsstDefaultOffering_Change_to_futureDated() {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setPriceType("Standard");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals("Company Offerings", 1, company.getCompanyOfferingCollection().size());
        CompanyOffering companyOffering = company.getDirectDepositCompanyOffering();
        assertEquals("Company Offering Code", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());

        Offering offering = Offering.findBySKU("AP69ME-FY13");
        OfferingInfoDTO offeringInfoDTO = PayrollServices.dtoFactory.create(offering);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 7, 14);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyOffering> result = PayrollServices.companyManager.updateCompanyOffering(SourceSystemCode.QBDT, psid, offeringInfoDTO);
        assertEquals("Update company offering result", 1, result.getErrorMessages().size());
        assertEquals("Error Code", "328", result.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message", "Offering " + offeringInfoDTO.getOfferingCode() + " effective date is in future, can not change Company Offering.", result.getErrorMessages().get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 7, 15);
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateCompanyOffering(SourceSystemCode.QBDT, psid, offeringInfoDTO);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        assertEquals("Company Offerings", 1, company.getCompanyOfferingCollection().size());
        companyOffering = company.getDirectDepositCompanyOffering();
        assertEquals("Company Offering Code", OfferingCode.AP69MEFY13, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAddCompanyOffering() {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setPriceType("Standard");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals("Company Offerings", 1, company.getCompanyOfferingCollection().size());
        CompanyOffering companyOffering = company.getDirectDepositCompanyOffering();
        assertEquals("Company Offering Code", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());

        Offering offering = Offering.findBySKU("AP69ME-FY13");
        OfferingInfoDTO offeringInfoDTO = PayrollServices.dtoFactory.create(offering);
        company.removeCompanyOffering(companyOffering);
        Application.delete(companyOffering);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals("Company offerings", 0, company.getCompanyOfferingCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateCompanyOffering(SourceSystemCode.QBDT, psid, offeringInfoDTO);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals("Company Offerings", 1, company.getCompanyOfferingCollection().size());
        companyOffering = company.getDirectDepositCompanyOffering();
        assertEquals("Company Offering Code", OfferingCode.AP69MEFY13, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

    }
}

