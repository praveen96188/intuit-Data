package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyAgencyYearInfoDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.LawRateDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.PaymentTemplateQuarterPaymentDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.PaymentTemplateYearPaymentDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryCompanyAgenciesYearInfoRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryCompanyAgenciesYearInfoResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.PayrollSubmitTaxTests;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryCompanyAgenciesYearInfoTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryCompanyAgenciesYearInfoTests {
    String psid = "123456789";

    @Before
    public void loadDataHappyPath() {
        PayrollSubmitTaxTests payrollSubmitTaxTests = new PayrollSubmitTaxTests();
        payrollSubmitTaxTests.runBeforeEachTest();
        payrollSubmitTaxTests.testHappyPath();
    }

//    @Test
//    public void emptyTest() {
//    }

    @Test
    public void testCompanyHappyPathEIN() {
        String ein = "";
        try {
            PayrollServices.beginUnitOfWork();
            Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
            ein=company1.getFedTaxId();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

//        List<SAPCompanyDISDTO> coList = PSPHelper.createSAPCompanyList(searchSAPCompanyRequestDISDTO.getEin(), searchSAPCompanyRequestDISDTO.getSourceCompanyId(), searchSAPCompanyRequestDISDTO);
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyAgenciesYearInfoRequestDISDTO queryCompanyAgenciesYearInfoRequestDISDTO = new QueryCompanyAgenciesYearInfoRequestDISDTO();
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceCompanyId(psid);
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyAgenciesYearInfoRequestDISDTO.setTaxYear(2011);

            QueryCompanyAgenciesYearInfoResponseDISDTO queryCompanyAgenciesYearInfoResponseDISDTO = disAdapter.Query_CompanyAgenciesYearInfo(queryCompanyAgenciesYearInfoRequestDISDTO);
            TestHelper.verifySuccess(queryCompanyAgenciesYearInfoResponseDISDTO.getDisResponse());
            Assert.assertEquals(1, queryCompanyAgenciesYearInfoResponseDISDTO.getCompanyAgencyYearInfoDISDTO().size());
            CompanyAgencyYearInfoDISDTO companyAgencyYearInfoDISDTO = queryCompanyAgenciesYearInfoResponseDISDTO.getCompanyAgencyYearInfoDISDTO().get(0);

            TaxAdapter taxAdapter = new TaxAdapter();
            List<SAPAgencyInfoDTO> sapAgencies = taxAdapter.getAgencyInfoArray("QBDT", psid);
            Assert.assertEquals(1, sapAgencies.size());
            SAPAgencyInfoDTO sapAgencyInfoDTO = sapAgencies.get(0);

            Assert.assertEquals(sapAgencyInfoDTO.getAgency().getAgencyId(),companyAgencyYearInfoDISDTO.getAgencyId());
            Assert.assertEquals(sapAgencyInfoDTO.getAgency().getAgencyName(), companyAgencyYearInfoDISDTO.getAgencyName());

            Assert.assertEquals(2,companyAgencyYearInfoDISDTO.getPaymentTemplateYearPayment().size());
            Map<String,PaymentTemplateYearPaymentDISDTO> paymentTemplateYearPayments = new TreeMap<String, PaymentTemplateYearPaymentDISDTO>();
            paymentTemplateYearPayments.put(companyAgencyYearInfoDISDTO.getPaymentTemplateYearPayment().get(0).getPaymentTemplateCd(),companyAgencyYearInfoDISDTO.getPaymentTemplateYearPayment().get(0));
            paymentTemplateYearPayments.put(companyAgencyYearInfoDISDTO.getPaymentTemplateYearPayment().get(1).getPaymentTemplateCd(), companyAgencyYearInfoDISDTO.getPaymentTemplateYearPayment().get(1));
            Assert.assertTrue(paymentTemplateYearPayments.containsKey("IRS-940-PAYMENT"));
            Assert.assertTrue(paymentTemplateYearPayments.containsKey("IRS-941-PAYMENT"));

            Assert.assertEquals(2,sapAgencyInfoDTO.getCompanyPaymentTemplates().size());
            Map<String,SAPCompanyPaymentTemplate> sapCompanyPaymentTemplates = new TreeMap<String, SAPCompanyPaymentTemplate>();
            sapCompanyPaymentTemplates.put(sapAgencyInfoDTO.getCompanyPaymentTemplates().get(0).getPaymentTemplate().getPaymentTemplateCd(),
                    sapAgencyInfoDTO.getCompanyPaymentTemplates().get(0));
            sapCompanyPaymentTemplates.put(sapAgencyInfoDTO.getCompanyPaymentTemplates().get(1).getPaymentTemplate().getPaymentTemplateCd(),
                    sapAgencyInfoDTO.getCompanyPaymentTemplates().get(1));
            Assert.assertTrue(sapCompanyPaymentTemplates.containsKey("IRS-940-PAYMENT"));
            Assert.assertTrue(sapCompanyPaymentTemplates.containsKey("IRS-941-PAYMENT"));

            compareCompanyPaymentTemplate(paymentTemplateYearPayments.get("IRS-940-PAYMENT"),
                    sapCompanyPaymentTemplates.get("IRS-940-PAYMENT"));
            compareCompanyPaymentTemplate(paymentTemplateYearPayments.get("IRS-941-PAYMENT"),
                    sapCompanyPaymentTemplates.get("IRS-941-PAYMENT"));

        } catch (Throwable t) {
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(t.getMessage());
        }
    }

    @Test
    public void testLegacyNoRAFEnrollment() {
        String ein = "";
        String noCoPsid = "123456788";
        try {
            PayrollServices.beginUnitOfWork();
            CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

            PSPDate.setPSPTime("20070822000000");
            companyQB1DataLoader.persistQBCompany1();

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

//        List<SAPCompanyDISDTO> coList = PSPHelper.createSAPCompanyList(searchSAPCompanyRequestDISDTO.getEin(), searchSAPCompanyRequestDISDTO.getSourceCompanyId(), searchSAPCompanyRequestDISDTO);
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyAgenciesYearInfoRequestDISDTO queryCompanyAgenciesYearInfoRequestDISDTO = new QueryCompanyAgenciesYearInfoRequestDISDTO();
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceCompanyId(CompanyQB1DataLoader.COMPANY_PSID);
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyAgenciesYearInfoRequestDISDTO.setTaxYear(2011);

            QueryCompanyAgenciesYearInfoResponseDISDTO QueryCompanyAgenciesYearInfoResponseDISDTO = disAdapter.Query_CompanyAgenciesYearInfo(queryCompanyAgenciesYearInfoRequestDISDTO);
            TestHelper.verifySuccess(QueryCompanyAgenciesYearInfoResponseDISDTO.getDisResponse());
            Assert.assertEquals(0,QueryCompanyAgenciesYearInfoResponseDISDTO.getCompanyAgencyYearInfoDISDTO().size());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testNoCompanyFound() {
        try {
            String sourceCoIdDNE = "companyDNE";

            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyAgenciesYearInfoRequestDISDTO queryCompanyAgenciesYearInfoRequestDISDTO = new QueryCompanyAgenciesYearInfoRequestDISDTO();
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceCompanyId(sourceCoIdDNE);
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyAgenciesYearInfoRequestDISDTO.setTaxYear(2011);

            QueryCompanyAgenciesYearInfoResponseDISDTO response = disAdapter.Query_CompanyAgenciesYearInfo(queryCompanyAgenciesYearInfoRequestDISDTO);
            TestHelper.verifyDISResponse(DISMessages.companyDoesNotExist(sourceCoIdDNE), response.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testMigratedCompanyWithDDAndAssistedPayrolls() {
        psid = DISCompanyDataloader.setupMigratedCompanyWithDDPayrollAndAssistedPayroll().getSourceCompanyId();
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyAgenciesYearInfoRequestDISDTO queryCompanyAgenciesYearInfoRequestDISDTO = new QueryCompanyAgenciesYearInfoRequestDISDTO();
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceCompanyId(psid);
            queryCompanyAgenciesYearInfoRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyAgenciesYearInfoRequestDISDTO.setTaxYear(2012);

            QueryCompanyAgenciesYearInfoResponseDISDTO response = disAdapter.Query_CompanyAgenciesYearInfo(queryCompanyAgenciesYearInfoRequestDISDTO);
            TestHelper.verifySuccess(response.getDisResponse());
            Assert.assertEquals(1,response.getCompanyAgencyYearInfoDISDTO().size());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    //// Helper Methods
    private void compareCompanyPaymentTemplate(PaymentTemplateYearPaymentDISDTO pPaymentTemplateYearPaymentDISDTO, SAPCompanyPaymentTemplate pSAPCompanyPaymentTemplate) throws Throwable {
        TaxAdapter taxAdapter = new TaxAdapter();
        SAPPaymentTemplateYearPayment sapPaymentTemplateYearPayment = taxAdapter.getTemplateYearPayment("QBDT", psid, "2011", pSAPCompanyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd());

        Assert.assertEquals(pPaymentTemplateYearPaymentDISDTO.getAgencyTaxPayerId(),pSAPCompanyPaymentTemplate.getAgencyTaxpayerId());
        Assert.assertEquals(pPaymentTemplateYearPaymentDISDTO.getPaymentsMadeTotal(),sapPaymentTemplateYearPayment.getPaymentsMadeTotal(),0);

        Map<String,LawRateDISDTO> lawRateDISDTOs = new TreeMap<String, LawRateDISDTO>();
        for (LawRateDISDTO lawRateDISDTO : pPaymentTemplateYearPaymentDISDTO.getLawRates()) {
            lawRateDISDTOs.put(lawRateDISDTO.getLawName(),lawRateDISDTO);
        }

        Map<String,SAPCompanyLawRateDetail> sapCompanyLawRateDetails = new TreeMap<String, SAPCompanyLawRateDetail>();
        for (SAPCompanyLawRateDetail sapCompanyLawRateDetail: pSAPCompanyPaymentTemplate.getLawRates()) {
            sapCompanyLawRateDetails.put(sapCompanyLawRateDetail.getLawName(),sapCompanyLawRateDetail);
        }
        Assert.assertEquals(lawRateDISDTOs.size(), sapCompanyLawRateDetails.size());
        for (LawRateDISDTO lawRateDISDTO : lawRateDISDTOs.values()) {
            compareLawRates(lawRateDISDTO, sapCompanyLawRateDetails.get(lawRateDISDTO.getLawName()));
        }

        Assert.assertEquals(pPaymentTemplateYearPaymentDISDTO.getPendingPaymentsTotal(),sapPaymentTemplateYearPayment.getPendingPaymentsTotal(),0);
        Assert.assertEquals(pPaymentTemplateYearPaymentDISDTO.getTaxYear(),sapPaymentTemplateYearPayment.getTaxYear());

        Map<String,PaymentTemplateQuarterPaymentDISDTO> paymentTemplateQuarterPaymentDISDTOs = new TreeMap<String, PaymentTemplateQuarterPaymentDISDTO>();
        for (PaymentTemplateQuarterPaymentDISDTO paymentTemplateQuarterPaymentDISDTO : pPaymentTemplateYearPaymentDISDTO.getTemplateQuarterPayments()) {
            paymentTemplateQuarterPaymentDISDTOs.put(paymentTemplateQuarterPaymentDISDTO.getPaymentTemplateCd(),paymentTemplateQuarterPaymentDISDTO);
        }

        Map<String,SAPPaymentTemplateQuarterPayment> sapPaymentTemplateQuarterPayments = new TreeMap<String, SAPPaymentTemplateQuarterPayment>();
        for (SAPPaymentTemplateQuarterPayment sapPaymentTemplateQuarterPayment: sapPaymentTemplateYearPayment.getTemplateQuarterPayments()) {
            sapPaymentTemplateQuarterPayments.put(sapPaymentTemplateQuarterPayment.getPaymentTemplateCd(),sapPaymentTemplateQuarterPayment);
        }

        Assert.assertEquals(paymentTemplateQuarterPaymentDISDTOs.size(), sapPaymentTemplateQuarterPayments.size());
        for (PaymentTemplateQuarterPaymentDISDTO paymentTemplateQuarterPaymentDISDTO : paymentTemplateQuarterPaymentDISDTOs.values()) {
            compareQuarterPayments(paymentTemplateQuarterPaymentDISDTO, sapPaymentTemplateQuarterPayments.get(paymentTemplateQuarterPaymentDISDTO.getPaymentTemplateCd()));
        }

        Assert.assertEquals(pPaymentTemplateYearPaymentDISDTO.getYearPaymentsTotal(), sapPaymentTemplateYearPayment.getYearPaymentsTotal(),0);
    }

    private void compareQuarterPayments(PaymentTemplateQuarterPaymentDISDTO pPaymentTemplateQuarterPaymentDISDTO, SAPPaymentTemplateQuarterPayment pSAPPaymentTemplateQuarterPayment) {
        Assert.assertEquals(pPaymentTemplateQuarterPaymentDISDTO.getPaymentTemplateCd(),pSAPPaymentTemplateQuarterPayment.getPaymentTemplateCd());
        Assert.assertEquals(pPaymentTemplateQuarterPaymentDISDTO.getPaymentsMadeTotal(),pSAPPaymentTemplateQuarterPayment.getPaymentsMadeTotal(),0);
        Assert.assertEquals(pPaymentTemplateQuarterPaymentDISDTO.getPaymentTemplateName(),pSAPPaymentTemplateQuarterPayment.getPaymentTemplateName());
        Assert.assertEquals(pPaymentTemplateQuarterPaymentDISDTO.getPendingPaymentsTotal(),pSAPPaymentTemplateQuarterPayment.getPendingPaymentsTotal(),0);
        Assert.assertEquals(pPaymentTemplateQuarterPaymentDISDTO.getQuarter(),pSAPPaymentTemplateQuarterPayment.getQuarter());
        Assert.assertEquals(pPaymentTemplateQuarterPaymentDISDTO.getQuarterPaymentsTotal(),pSAPPaymentTemplateQuarterPayment.getQuarterPaymentsTotal(),0);
        Assert.assertEquals(pPaymentTemplateQuarterPaymentDISDTO.getYear(),pSAPPaymentTemplateQuarterPayment.getYear());
    }

    private void compareLawRates(LawRateDISDTO lawRateDISDTO, SAPCompanyLawRateDetail sapCompanyLawRateDetail) {
        Assert.assertEquals(lawRateDISDTO.getLawName(),sapCompanyLawRateDetail.getLawName());
        Assert.assertEquals(lawRateDISDTO.getActive(),!sapCompanyLawRateDetail.getInactive());
        Assert.assertEquals(lawRateDISDTO.getEffectiveDate(), sapCompanyLawRateDetail.getEffectiveQuarter() == null ? null : CalendarUtils.convertToDate(sapCompanyLawRateDetail.getEffectiveQuarter().getFirstDayOfQuarter()));
        Assert.assertEquals(lawRateDISDTO.getRate(), sapCompanyLawRateDetail.getRate());
    }

}
