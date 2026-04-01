package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyFilingAmountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.FormTemplateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * User: mamin
 * Date: Mar 19, 2009
 * Time: 10:04:14 AM
 */
public class UpdateCompanyAgencyCoreTests {


    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;

    //
    @Before
    public void runBeforeEachTest() {
        AddCompanyDataLoader.beforeEachTest();

        //DataLoadServices.setupCompany("123456789");
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testUpdateCompanyAgency_newFilerTypeAdded(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        mSourceSystemCd =  company.getSourceSystemCd();
        mSourceCompanyId =  company.getSourceCompanyId();

        PayrollServices.beginUnitOfWork();
        CompanyAgency ca = CompanyAgency.findCompanyAgency(company,Agency.IRS);
        assertNotNull(ca);
        assertTrue(ca.getAgency().getPaymentTemplateCollection().size()>0);
        assertEquals("CompanyAgencyFormTemplate List Size", 2, CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());

        CompanyAgencyDTO companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);
       // assertEquals("Taxpayer Id not equal.",companyAgencyDTO.getAgencyTaxpayerId(),ca.getAgencyTaxpayerId());
        assertEquals("Institute responsible start date not equal.",companyAgencyDTO.getIntuitResponsibilityStartDate(),ca.getIntuitResponsibilityStartDate());
        assertEquals("Institute responsible end date not equal.",companyAgencyDTO.getIntuitResponsibilityEndDate(),ca.getIntuitResponsibilityEndDate());

        List<FormTemplateDTO> formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        FormTemplateDTO ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-944-FILING");
        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
        formTemplateDtoList.add(ftDTO);
        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        ProcessResult result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyAgency updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

       // assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());
        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        DomainEntitySet<CompanyAgencyFormTemplate> validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("Valid Agency form Templates.",1,validFT.size());
        assertEquals("Form Template Name.","IRS-944-FILING",validFT.get(0).getFormTemplate().getFormTemplateCd());
    }
    @Test
    public void testUpdateCompanyAgency_sameFilerTypeEffectiveDate(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        mSourceSystemCd =  company.getSourceSystemCd();
        mSourceCompanyId =  company.getSourceCompanyId();

        PayrollServices.beginUnitOfWork();
        CompanyAgency ca = CompanyAgency.findCompanyAgency(company,Agency.IRS);
        assertNotNull(ca);
        assertTrue(ca.getAgency().getPaymentTemplateCollection().size()>0);
        assertEquals("CompanyAgencyFormTemplate List Size", 2, CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());

        CompanyAgencyDTO companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);
        //assertEquals("Taxpayer Id not equal.",companyAgencyDTO.getAgencyTaxpayerId(),ca.getAgencyTaxpayerId());
        assertEquals("Institute responsible start date not equal.",companyAgencyDTO.getIntuitResponsibilityStartDate(),ca.getIntuitResponsibilityStartDate());
        assertEquals("Institute responsible end date not equal.",companyAgencyDTO.getIntuitResponsibilityEndDate(),ca.getIntuitResponsibilityEndDate());

        SpcfCalendar sameEffectiveDate = PSPDate.getPSPTime();

        List<FormTemplateDTO> formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        FormTemplateDTO ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-944-FILING");
        ftDTO.setEffectiveDate(sameEffectiveDate);
        formTemplateDtoList.add(ftDTO);
        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        ProcessResult result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyAgency updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

      //  assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        DomainEntitySet<CompanyAgencyFormTemplate> validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("Valid Agency form Templates.",1,validFT.size());
        assertEquals("Form Template Name.","IRS-944-FILING",validFT.get(0).getFormTemplate().getFormTemplateCd());

        PayrollServices.beginUnitOfWork();
        companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);

        formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-944-FILING");
        ftDTO.setEffectiveDate(sameEffectiveDate);
        formTemplateDtoList.add(ftDTO);
        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

       // assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("Valid Agency form Templates.",1,validFT.size());
        assertEquals("Form Template Name.","IRS-944-FILING",validFT.get(0).getFormTemplate().getFormTemplateCd());
    }

    @Test
    public void testUpdateCompanyAgency_modifyEffectiveDate(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        mSourceSystemCd =  company.getSourceSystemCd();
        mSourceCompanyId =  company.getSourceCompanyId();

        PayrollServices.beginUnitOfWork();
        CompanyAgency ca = CompanyAgency.findCompanyAgency(company,Agency.IRS);
        assertNotNull(ca);
        assertTrue(ca.getAgency().getPaymentTemplateCollection().size()>0);
        assertEquals("CompanyAgencyFormTemplate List Size", 2, CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());

        CompanyAgencyDTO companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);
     //   assertEquals("Taxpayer Id not equal.",companyAgencyDTO.getAgencyTaxpayerId(),ca.getAgencyTaxpayerId());
        assertEquals("Institute responsible start date not equal.",companyAgencyDTO.getIntuitResponsibilityStartDate(),ca.getIntuitResponsibilityStartDate());
        assertEquals("Institute responsible end date not equal.",companyAgencyDTO.getIntuitResponsibilityEndDate(),ca.getIntuitResponsibilityEndDate());

        List<FormTemplateDTO> formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        FormTemplateDTO ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-944-FILING");
        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
        formTemplateDtoList.add(ftDTO);
        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        ProcessResult result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyAgency updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

       // assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        DomainEntitySet<CompanyAgencyFormTemplate> validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("Valid Agency form Templates.",1,validFT.size());
        assertEquals("Filer Type.","IRS-944-FILING",validFT.get(0).getFormTemplate().getFormTemplateCd());

        PayrollServices.beginUnitOfWork();
        companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);

        formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-944-FILING");
        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
        formTemplateDtoList.add(ftDTO);
        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

      //  assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("In Valid Agency form Templates.",1,validFT.size());
        assertEquals("Invalid Filer Type.","IRS-944-FILING",validFT.get(0).getFormTemplate().getFormTemplateCd());
    }

    @Test
    public void testUpdateCompanyAgency_modifyEffectiveDateFilerType(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        mSourceSystemCd =  company.getSourceSystemCd();
        mSourceCompanyId =  company.getSourceCompanyId();

        PayrollServices.beginUnitOfWork();
        CompanyAgency ca = CompanyAgency.findCompanyAgency(company,Agency.IRS);
        assertNotNull(ca);
        assertTrue(ca.getAgency().getPaymentTemplateCollection().size()>0);
        assertEquals("CompanyAgencyFormTemplate List Size", 2, CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());

        CompanyAgencyDTO companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);
     //   assertEquals("Taxpayer Id not equal.",companyAgencyDTO.getAgencyTaxpayerId(),ca.getAgencyTaxpayerId());
        assertEquals("Institute responsible start date not equal.",companyAgencyDTO.getIntuitResponsibilityStartDate(),ca.getIntuitResponsibilityStartDate());
        assertEquals("Institute responsible end date not equal.",companyAgencyDTO.getIntuitResponsibilityEndDate(),ca.getIntuitResponsibilityEndDate());

        List<FormTemplateDTO> formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        FormTemplateDTO ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-941-FILING");
        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
        formTemplateDtoList.add(ftDTO);
        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        ProcessResult result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyAgency updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

     //   assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",2,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        DomainEntitySet<CompanyAgencyFormTemplate> validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("Valid Agency form Templates.",1,validFT.size());
        assertEquals("Filer Type.","IRS-941-FILING",validFT.get(0).getFormTemplate().getFormTemplateCd());

        PayrollServices.beginUnitOfWork();
        companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);

        formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-944-FILING");
        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
        formTemplateDtoList.add(ftDTO);
        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

       // assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("In Valid Agency form Templates.",1,validFT.size());
        assertEquals("Invalid Filer Type.","IRS-944-FILING",validFT.get(0).getFormTemplate().getFormTemplateCd());
    }


    @Test
    public void testUpdateCompanyAgency_portionOfFilerTypeChange(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        mSourceSystemCd =  company.getSourceSystemCd();
        mSourceCompanyId =  company.getSourceCompanyId();

        PayrollServices.beginUnitOfWork();
        CompanyAgency ca = CompanyAgency.findCompanyAgency(company,Agency.IRS);
        assertNotNull(ca);
        assertTrue(ca.getAgency().getPaymentTemplateCollection().size()>0);
        assertEquals("CompanyAgencyFormTemplate List Size", 2, CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());

        CompanyAgencyDTO companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);
       // assertEquals("Taxpayer Id not equal.",companyAgencyDTO.getAgencyTaxpayerId(),ca.getAgencyTaxpayerId());
        assertEquals("Institute responsible start date not equal.",companyAgencyDTO.getIntuitResponsibilityStartDate(),ca.getIntuitResponsibilityStartDate());
        assertEquals("Institute responsible end date not equal.",companyAgencyDTO.getIntuitResponsibilityEndDate(),ca.getIntuitResponsibilityEndDate());

        List<FormTemplateDTO> formTemplateDtoList = new ArrayList<FormTemplateDTO>();

        FormTemplateDTO ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-941-FILING");
        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
        formTemplateDtoList.add(ftDTO);

        SpcfCalendar sameEffectiveDate = SpcfCalendar.createInstance(2010, 5, 6);

        FormTemplateDTO ftDTO1 = new FormTemplateDTO();
        ftDTO1.setFilerType("IRS-944-FILING");
        ftDTO1.setEffectiveDate(sameEffectiveDate);
        formTemplateDtoList.add(ftDTO1);

        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        ProcessResult result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyAgency updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

     //   assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        DomainEntitySet<CompanyAgencyFormTemplate> validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("Valid Agency form Templates.",2,validFT.size());

        PayrollServices.beginUnitOfWork();
        companyAgencyDTO  = PayrollServices.dtoFactory.create(ca);

        formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-941-FILING");
        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
        formTemplateDtoList.add(ftDTO);

        ftDTO1 = new FormTemplateDTO();
        ftDTO1.setFilerType("IRS-944-FILING");
        ftDTO1.setEffectiveDate(sameEffectiveDate);
        formTemplateDtoList.add(ftDTO1);

        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        result =
                PayrollServices.companyManager.updateCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS, companyAgencyDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        updatedCompanyAgency =
                CompanyAgency.findCompanyAgency(mSourceSystemCd, mSourceCompanyId, Agency.IRS);
        PayrollServices.commitUnitOfWork();

      //  assertEquals("Agency Tax Payer Id", companyAgencyDTO.getAgencyTaxpayerId(), updatedCompanyAgency.getAgencyTaxpayerId());
        assertEquals("Responsibility Start Date", companyAgencyDTO.getIntuitResponsibilityStartDate(), updatedCompanyAgency.getIntuitResponsibilityStartDate());
        assertEquals("Responsibility end Date", companyAgencyDTO.getIntuitResponsibilityEndDate(), updatedCompanyAgency.getIntuitResponsibilityEndDate());

        assertEquals("Incorrect number of Agency Form Templates.",3,CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().size());
        validFT =  updatedCompanyAgency.findValidFormTemplatesForCompanyAgency();
        assertEquals("In Valid Agency form Templates.",2,validFT.size());
    }

    @Test
    public void updateCompanyAgency_modifyPortionOfFilerTypes() {
        SpcfCalendar effectiveDate1 = PSPDate.getPSPTime();
        SpcfCalendar effectiveDate2 = PSPDate.getPSPTime();

        //The AS/400 will only ever send yyyyMMdd formatted dates, so clear time here to match
        CalendarUtils.clearTime(effectiveDate1);
        CalendarUtils.clearTime(effectiveDate2);

        effectiveDate2.addDays(4);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        CompanyAgency ca = CompanyAgency.findCompanyAgency(company, Agency.IRS);
        CompanyAgencyDTO pCompanyAgencyDTO = PayrollServices.dtoFactory.create(ca);

        List<FormTemplateDTO> formTemplateDtoList = new ArrayList<FormTemplateDTO>();
        FormTemplateDTO ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-941-FILING");
        ftDTO.setEffectiveDate(effectiveDate1);

        FormTemplateDTO ftDTO1 = new FormTemplateDTO();
        ftDTO1.setFilerType("IRS-944-FILING");
        ftDTO1.setEffectiveDate(effectiveDate2);

        formTemplateDtoList.add(ftDTO);
        formTemplateDtoList.add(ftDTO1);
        pCompanyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(),
                company.getSourceCompanyId().toString(), Agency.IRS, pCompanyAgencyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<CompanyAgencyFormTemplate> companyAgencyFT = Application.find(CompanyAgencyFormTemplate.class);
        assertEquals("CompanyAgencyFormTemplate List Size", 3, companyAgencyFT.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        ca = CompanyAgency.findCompanyAgency(company, Agency.IRS);

        pCompanyAgencyDTO = PayrollServices.dtoFactory.create(ca);
        formTemplateDtoList = new ArrayList<FormTemplateDTO>();

        /*ftDTO = new FormTemplateDTO();
        ftDTO.setFilerType("IRS-941-FILING");
        ftDTO.setEffectiveDate(effectiveDate);*/

        ftDTO1 = new FormTemplateDTO();
        ftDTO1.setFilerType("IRS-944-FILING");

        SpcfCalendar laterDate = SpcfCalendar.createInstance(2009, 1, 15);
        ftDTO1.setEffectiveDate(laterDate);

        /*formTemplateDtoList.add(ftDTO);*/
        formTemplateDtoList.add(ftDTO1);
        pCompanyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);

        PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(),
                company.getSourceCompanyId().toString(), Agency.IRS, pCompanyAgencyDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyAgencyFT = Application.find(CompanyAgencyFormTemplate.class);
        assertEquals("CompanyAgencyFormTemplate List Size", 4, companyAgencyFT.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUpdateFilingAmounts() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2011, 10, 15);

        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate("MA-1700HI-PAYMENT"));
        CompanyAgencyDTO dto = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        CompanyFilingAmountDTO dtoToRemove = null;
        List<CompanyFilingAmountDTO> companyFilingAmountDTOs = dto.getCompanyAgencyPaymentTemplate("MA-1700HI-PAYMENT").getCompanyFilingAmountDTOs();
        for (CompanyFilingAmountDTO companyFilingAmountDTO : companyFilingAmountDTOs) {
            if (companyFilingAmountDTO.getName().equals("MA SUI Credit")) {
                companyFilingAmountDTO.setAmount(99.99);
            } else if (companyFilingAmountDTO.getName().equals("MA UHI Credit")) {
                dtoToRemove = companyFilingAmountDTO;
            } else if (companyFilingAmountDTO.getName().equals("MA Unemployment Health Insurance Rate")) {
                companyFilingAmountDTO.setEffectiveDate(new DateDTO("2010-01-01"));
            }
        }
        companyFilingAmountDTOs.remove(dtoToRemove);
        CompanyFilingAmountDTO newDTO = new CompanyFilingAmountDTO();
        newDTO.setName("MA Unemployment Health Insurance Rate");
        newDTO.setAmount(0.00083);
        newDTO.setEffectiveDate(new DateDTO("2010-06-01"));
        companyFilingAmountDTOs.add(newDTO);

        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgencyPaymentTemplate.getCompanyAgency().getAgency().getAgencyId(), dto));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(companyAgencyPaymentTemplate);
        assertEquals(5, companyAgencyPaymentTemplate.getCompanyFilingAmountCollection().size());
        DomainEntitySet<CompanyFilingAmount> filingAmounts = companyAgencyPaymentTemplate.getCompanyFilingAmountCollection().sort(CompanyFilingAmount.Name(), CompanyFilingAmount.EffectiveDate());

        CompanyFilingAmount emacRate = filingAmounts.get(0);
        assertEquals("MA Er Medical Assistance Contribution", emacRate.getName());
        assertEquals(12.34, emacRate.getAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), emacRate.getEffectiveDate().toLocal());
        assertNull(emacRate.getInvalidDate());

        CompanyFilingAmount suiCredit = filingAmounts.get(1);
        assertEquals("MA SUI Credit", suiCredit.getName());
        assertEquals(99.99, suiCredit.getAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), suiCredit.getEffectiveDate().toLocal());
        assertNull(suiCredit.getInvalidDate());

        CompanyFilingAmount uhiCredit = filingAmounts.get(2);
        assertEquals("MA UHI Credit", uhiCredit.getName());
        assertEquals(12.34, uhiCredit.getAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), uhiCredit.getEffectiveDate().toLocal());
        SpcfCalendar invalidDate = uhiCredit.getInvalidDate().toLocal();
        CalendarUtils.clearTime(invalidDate);
        assertEquals(SpcfCalendar.createInstance(2011, 10, 15, SpcfTimeZone.getLocalTimeZone()), invalidDate);

        CompanyFilingAmount firstRate = filingAmounts.get(3);
        assertEquals("MA Unemployment Health Insurance Rate", firstRate.getName());
        assertEquals(12.34, firstRate.getAmount());
        assertEquals(SpcfCalendar.createInstance(2010, 1, 1, SpcfTimeZone.getLocalTimeZone()), firstRate.getEffectiveDate().toLocal());
        assertNull(firstRate.getInvalidDate());

        CompanyFilingAmount secondRate = filingAmounts.get(4);
        assertEquals("MA Unemployment Health Insurance Rate", secondRate.getName());
        assertEquals(0.00083, secondRate.getAmount());
        assertEquals(SpcfCalendar.createInstance(2010, 6, 1, SpcfTimeZone.getLocalTimeZone()), secondRate.getEffectiveDate().toLocal());
        assertNull(secondRate.getInvalidDate());

    }

    @Test
    public void testUpdateOneFilingAmount() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2011, 10, 15);

        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate("MA-1700HI-PAYMENT"));
        CompanyAgencyDTO dto = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        List<CompanyFilingAmountDTO> companyFilingAmountDTOs = dto.getCompanyAgencyPaymentTemplate("MA-1700HI-PAYMENT").getCompanyFilingAmountDTOs();
        for (CompanyFilingAmountDTO companyFilingAmountDTO : companyFilingAmountDTOs) {
            if (companyFilingAmountDTO.getName().equals("MA SUI Credit")) {
                companyFilingAmountDTO.setAmount(99.99);
                break;
            }
        }

        CompanyFilingAmountDTO newDTO = new CompanyFilingAmountDTO();
        newDTO.setName("MA Unemployment Health Insurance Rate");
        newDTO.setAmount(0.00083);
        newDTO.setEffectiveDate(new DateDTO("2010-06-01"));
        companyFilingAmountDTOs.add(newDTO);

        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgencyPaymentTemplate.getCompanyAgency().getAgency().getAgencyId(), dto));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(companyAgencyPaymentTemplate);
        assertEquals(5, companyAgencyPaymentTemplate.getCompanyFilingAmountCollection().size());
        DomainEntitySet<CompanyFilingAmount> filingAmounts = companyAgencyPaymentTemplate.getCompanyFilingAmountCollection().sort(CompanyFilingAmount.Name(), CompanyFilingAmount.EffectiveDate());

        CompanyFilingAmount emacRate = filingAmounts.get(0);
        assertEquals("MA Er Medical Assistance Contribution", emacRate.getName());
        assertEquals(12.34, emacRate.getAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), emacRate.getEffectiveDate().toLocal());
        assertNull(emacRate.getInvalidDate());

        CompanyFilingAmount suiCredit = filingAmounts.get(1);
        assertEquals("MA SUI Credit", suiCredit.getName());
        assertEquals(99.99, suiCredit.getAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), suiCredit.getEffectiveDate().toLocal());
        assertNull(suiCredit.getInvalidDate());

        CompanyFilingAmount uhiCredit = filingAmounts.get(2);
        assertEquals("MA UHI Credit", uhiCredit.getName());
        assertEquals(12.34, uhiCredit.getAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), uhiCredit.getEffectiveDate().toLocal());
        assertNull(uhiCredit.getInvalidDate());

        CompanyFilingAmount firstRate = filingAmounts.get(3);
        assertEquals("MA Unemployment Health Insurance Rate", firstRate.getName());
        assertEquals(0.00083, firstRate.getAmount());
        assertEquals(SpcfCalendar.createInstance(2010, 6, 1, SpcfTimeZone.getLocalTimeZone()), firstRate.getEffectiveDate().toLocal());
        assertNull(firstRate.getInvalidDate());

        CompanyFilingAmount secondRate = filingAmounts.get(4);
        assertEquals("MA Unemployment Health Insurance Rate", secondRate.getName());
        assertEquals(12.34, secondRate.getAmount());
        assertEquals(SpcfCalendar.createInstance(2011, 10, 1, SpcfTimeZone.getLocalTimeZone()), secondRate.getEffectiveDate().toLocal());
        assertNull(secondRate.getInvalidDate());

    }
}
