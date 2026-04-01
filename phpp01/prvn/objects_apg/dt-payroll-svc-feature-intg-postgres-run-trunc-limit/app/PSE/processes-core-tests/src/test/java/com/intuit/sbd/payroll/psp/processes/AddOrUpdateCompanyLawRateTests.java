package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawRateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPayrollItemInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * User: mwaqarbaig
 * Date: Oct 27, 2010
 * Time: 10:22:29 AM
 */

public class AddOrUpdateCompanyLawRateTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
        companyLawRateDTO.setEffectiveDate(new DateDTO("2010-10-01"));        
        companyLawRateDTO.setRate(2.5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO);
        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        DomainEntitySet<CompanyLawRate> companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(1, companyLawRates.size());
        assertEquals(2, CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathMultipleRates() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /*  Both rates starting at the same time    */
        /*  First LawRate   */
        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2011-01-01"));
        companyLawRateDTO1.setRate(0.05d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);
        /*  Second LawRate   */
        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-04-01"));
        companyLawRateDTO2.setRate(0.07d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);
        /*  Third LawRate   */
        CompanyLawRateDTO companyLawRateDTO3 = new CompanyLawRateDTO();
        companyLawRateDTO3.setEffectiveDate(new DateDTO("2011-10-01"));
        companyLawRateDTO3.setRate(-0.06d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO3);

        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        DomainEntitySet<CompanyLawRate> companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(3, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /* First LawRate   */
        companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2011-01-01"));
        companyLawRateDTO1.setRate(0.04d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);
        /* Second LawRate   */
        companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-07-01"));
        companyLawRateDTO2.setRate(0.07d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);
        /* Third LawRate   */
        companyLawRateDTO3 = new CompanyLawRateDTO();
        companyLawRateDTO3.setEffectiveDate(new DateDTO("2011-10-01"));
        companyLawRateDTO3.setRate(0.05d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO3);

        processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(6, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathMultipleRates_OneNewRate() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /*  Both rates starting at the same time    */
        /*  First LawRate   */
        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2010-10-01"));
        companyLawRateDTO1.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);
        /*  Second LawRate   */
        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-04-01"));
        companyLawRateDTO2.setRate(7d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);
        /*  Third LawRate   */
        CompanyLawRateDTO companyLawRateDTO3 = new CompanyLawRateDTO();
        companyLawRateDTO3.setEffectiveDate(new DateDTO("2011-10-01"));
        companyLawRateDTO3.setRate(6d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO3);

        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        DomainEntitySet<CompanyLawRate> companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(3, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /* First & Last LawRate   */
        companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2011-01-01"));
        companyLawRateDTO1.setRate(4d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);

        processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(4, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNullEffectiveExpirationDates() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());

        /*  The eternal LawRate   */
        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2011-01-01"));
        companyLawRateDTO1.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);

        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        DomainEntitySet<CompanyLawRate> companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(1, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());

        /* The eternal lawRate gets replaced   */
        companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setRate(4d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);

        /*  Expecting no NPEs   */
        processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(2, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHappyPathMultipleRates_OldRatesOverlappingNewRates_OneCommonRate() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());

        /* Zero-th  LawRate   */
        CompanyLawRateDTO companyLawRateDTO0 = new CompanyLawRateDTO();
        companyLawRateDTO0.setEffectiveDate(new DateDTO("2010-07-01"));
        companyLawRateDTO0.setRate(6d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO0);

        /* First LawRate   */
        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2010-10-01"));
        companyLawRateDTO1.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);
        /* Second LawRate   */
        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-04-01"));
        companyLawRateDTO2.setRate(7d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);
        /* Third LawRate   */
        CompanyLawRateDTO companyLawRateDTO3 = new CompanyLawRateDTO();
        companyLawRateDTO3.setEffectiveDate(new DateDTO("2011-07-01"));
        companyLawRateDTO3.setRate(6d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO3);


        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        DomainEntitySet<CompanyLawRate> companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(4, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /* First LawRate   */
        companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2011-01-01"));
        companyLawRateDTO1.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);
        /* Second LawRate   */
        companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-04-01"));
        companyLawRateDTO2.setRate(7d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);
        /* Third LawRate   */
        companyLawRateDTO3 = new CompanyLawRateDTO();
        companyLawRateDTO3.setEffectiveDate(new DateDTO("2011-07-01"));
        companyLawRateDTO3.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO3);

        processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(6, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPartiallyIncorrectRatesList() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /* First LawRate   */
        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2009-04-01"));
        companyLawRateDTO2.setRate(7.5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);

        /* Second Incorrect LawRate   */
        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2010-10-02")); /*  Effective date is not on a Quarter Boundary */
        companyLawRateDTO1.setRate(2.5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);

        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "1506", errorMessage.getMessageCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUpdateRates() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /* First LawRate   */
        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2009-04-01"));
        companyLawRateDTO2.setRate(7.5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);

        /* Second LawRate   */
        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2010-10-01"));
        companyLawRateDTO1.setRate(2.5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);

        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("Company Law Rate Not Updated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTOUpd = new CompanyLawDTO();
        companyLawDTOUpd.setLawId("63");
        companyLawDTOUpd.setSourceDescription("Company tax");
        companyLawDTOUpd.setSourceId(sourceId);
        companyLawDTOUpd.setStatus(PayrollItemStatus.Active);
        companyLawDTOUpd.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /* First LawRate   */
        CompanyLawRateDTO companyLawRateDTO2Upd = new CompanyLawRateDTO();
        companyLawRateDTO2Upd.setEffectiveDate(new DateDTO("2009-04-01"));
        companyLawRateDTO2Upd.setRate(17.3d);
        companyLawDTOUpd.getRateDTOs().add(companyLawRateDTO2Upd);

        /* Second LawRate   */
        CompanyLawRateDTO companyLawRateDTO1Upd = new CompanyLawRateDTO();
        companyLawRateDTO1Upd.setEffectiveDate(new DateDTO("2010-07-01"));
        companyLawRateDTO1Upd.setRate(202.9d);
        companyLawDTOUpd.getRateDTOs().add(companyLawRateDTO1Upd);

        ProcessResult processResultUpd = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTOUpd);
        assertSuccess("Company Law Rate Not Updated", processResultUpd);
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testCompanyIsInvalid() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
        companyLawRateDTO.setEffectiveDate(new DateDTO("2010-10-01"));
        companyLawRateDTO.setRate(2.5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO);
        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), "1111", companyLawDTO);
        Message errorMessage = processResult.getMessages().get(0);
        /*  Expected Error:169, CompanyDoesNotExist */
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCompanyLawEffectiveDateIsNotOnQuarterBoundary() {
        String sourceId = "1";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        CompanyLawRateDTO companyLawRateDTO = new CompanyLawRateDTO();
        companyLawRateDTO.setEffectiveDate(new DateDTO("2010-10-02"));
        companyLawRateDTO.setRate(0.0d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO);
        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        Message errorMessage = processResult.getMessages().get(0);
        /*  Expected Error:1508, Effective Date Is Not On a Quarter Boundary */
        assertEquals("Error message code", "1506", errorMessage.getMessageCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHappyPathForPercentageAndFixedRates() {
        String sourceIdPercentage = "1";
        String sourceIdFixed = "2";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTOPercentage = new CompanyLawDTO();
        companyLawDTOPercentage.setLawId("63");
        companyLawDTOPercentage.setSourceDescription("Company tax");
        companyLawDTOPercentage.setSourceId(sourceIdPercentage);
        companyLawDTOPercentage.setStatus(PayrollItemStatus.Active);
        companyLawDTOPercentage.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        //Percentage Rate
        CompanyLawRateDTO companyLawRateDTOPercentage = new CompanyLawRateDTO();
        companyLawRateDTOPercentage.setEffectiveDate(new DateDTO("2010-10-01"));
        companyLawRateDTOPercentage.setRate(2.5d);
        companyLawDTOPercentage.getRateDTOs().add(companyLawRateDTOPercentage);
        //Send data
        ProcessResult processResultPercentage = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTOPercentage);
        assertSuccess("companyLawRateUpdated", processResultPercentage);
        PayrollServices.commitUnitOfWork();

        //Fixed Rate
        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTOFixed = new CompanyLawDTO();
        companyLawDTOFixed.setLawId("143");
        companyLawDTOFixed.setSourceDescription("Other tax");
        companyLawDTOFixed.setSourceId(sourceIdFixed);
        companyLawDTOFixed.setStatus(PayrollItemStatus.Active);
        companyLawDTOFixed.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        CompanyLawRateDTO companyLawRateDTOFixed = new CompanyLawRateDTO();
        companyLawRateDTOFixed.setEffectiveDate(new DateDTO("2010-10-01"));
        companyLawRateDTOFixed.setRate(2.5d);
        companyLawRateDTOFixed.setRateType(QbdtNumericType.MoneyType);
        companyLawDTOFixed.getRateDTOs().add(companyLawRateDTOFixed);
        //Send data
        ProcessResult processResultFixed = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTOFixed);
        assertSuccess("companyLawRateUpdated", processResultFixed);
        PayrollServices.commitUnitOfWork();

        //Verify data
        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceIdPercentage);
        DomainEntitySet<CompanyLawRate> companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(1, companyLawRates.size());
        assertEquals(2.5d, companyLawRates.get(0).getRate());
        assertEquals(QbdtNumericType.Percentage, companyLawRates.get(0).getRateType());
        companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceIdFixed);
        companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(1, companyLawRates.size());
        assertEquals(2.5d, companyLawRates.get(0).getRate());
        assertEquals(QbdtNumericType.MoneyType, companyLawRates.get(0).getRateType());
        PayrollServices.rollbackUnitOfWork();
    }

}