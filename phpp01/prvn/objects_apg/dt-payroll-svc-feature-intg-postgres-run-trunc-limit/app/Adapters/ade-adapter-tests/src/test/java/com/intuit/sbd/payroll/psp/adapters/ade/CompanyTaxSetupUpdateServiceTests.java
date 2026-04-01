package com.intuit.sbd.payroll.psp.adapters.ade;

import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.api.messages.Message;
import com.intuit.ems.cep.api.messages.MessageCode;
import com.intuit.ems.cep.company.v1.service.Expand;
import com.intuit.ems.cep.company.v1.service.ShowHistory;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxItemGetServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupGetServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupUpdateServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company.CompanyTaxItemUpdateService;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AgencyIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.DateUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.AdditionalFilingAmount;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.schema.payroll.v3.company.Agency;
import com.intuit.schema.payroll.v3.company.*;
import com.intuit.schema.payroll.v3.compliance.FilingTypeEnum;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/*
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 10/4/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */

public class CompanyTaxSetupUpdateServiceTests {
    final static String COMPANY_ID = "19670404";
    final static String COMPANY_NAME = "SHIVA TEST ADE";
    final static String COMPANY_FEIN = "223456789";

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 10, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testUpdateTaxRate() {
        createCompanyWithLaws();
        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupCA("3.6", "0.1"), createTaxSetupServiceParameters(COMPANY_ID));
        TaxSetup taxSetupResult = serviceResult.getResult();
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());

        // todo asserts?

        assertNotNull(taxSetupResult);
    }

    @Test
    public void testUpdateTaxRateWithInActiveCompany() {
        createNonActiveCompany();

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupCA("3.6", "0.1"), createTaxSetupServiceParameters(COMPANY_ID));
        TaxSetup taxSetupResult = serviceResult.getResult();
        assertFalse(serviceResult.isSuccess());

        // todo asserts?

        assertNull(taxSetupResult);
    }

    @Test
    public void testUpdateTaxRateWithDGDeletedCompany() {
        createNonActiveCompany();

        Application.beginUnitOfWork();
        Company company = Company.findCompany(COMPANY_ID, SourceSystemCode.QBDT);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        //Getting existing data and validating before updating
        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(COMPANY_ID);
        taxSetupGetServiceParams.setExpand(Expand.TAXSETUP.toString());
        TaxSetup taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP)
                .service(taxSetupGetServiceParams).getResult();

        assertNull(taxSetup);

        TaxItem taxItem = ServiceFactory.getInstance()
                .<TaxItem, TaxItemGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXITEMS)
                .service(createTaxItemServiceParameters(COMPANY_ID, "US_RI_SUI_ER_UI")).getResult();

        assertNull(taxItem);


        List<TaxItem> taxItemList = ServiceFactory.getInstance()
                .<TaxItem, TaxItemGetServiceParams>constructGetListServiceInstance(ResourceNameEnum.TAXITEMS)
                .service(createTaxItemServiceParameters(COMPANY_ID, "US_RI_SUI_ER_UI")).getResult();

        assertNull(taxItemList);

    }

    @Test
    public void testTaxSetupUpdateWithDGDeletedCompany(){
        createAssistedCompanyWithRates("AR", COMPANY_ID, "987654321", "3110765 1");

        Application.beginUnitOfWork();
        Company company = Company.findCompany(COMPANY_ID, SourceSystemCode.QBDT);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        Application.commitUnitOfWork();

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                        .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                        .service(PayloadHelper.getTaxSetupAR("11.05"), createTaxSetupServiceParameters(COMPANY_ID));
        TaxSetup taxSetupResult = serviceResult.getResult();

        assertNull(taxSetupResult);
    }

    @Test
    public void testUpdateRITaxRateWithLessThanUIThreshold() {
        String psid = "199210091";

        createNonActiveCompany();
        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(getTaxSetupRI(), createTaxSetupServiceParameters(psid));

        TaxSetup taxSetupResult = serviceResult.getResult();
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(taxSetupResult);

        TaxItem taxItem = ServiceFactory.getInstance()
                                        .<TaxItem, TaxItemGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXITEMS)
                                        .service(createTaxItemServiceParameters(psid, "US_RI_SUI_ER_UI")).getResult();
        assertEquals("RI - UI rate", new BigDecimal("9.78"), taxItem.getTaxRates().get(0).getRate());

        taxItem = ServiceFactory.getInstance()
                                .<TaxItem, TaxItemGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXITEMS)
                                .service(createTaxItemServiceParameters(psid, "US_RI_SC_ER_WBI")).getResult();
        assertEquals("RI-WBI rate", new BigDecimal("0.0"), taxItem.getTaxRates().get(0).getRate());

        taxItem = ServiceFactory.getInstance()
                                .<TaxItem, TaxItemGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXITEMS)
                                .service(createTaxItemServiceParameters(psid, "US_RI_SC_ER_JDF")).getResult();
        assertEquals("RI- JDF", new BigDecimal("0.21"), taxItem.getTaxRates().get(0).getRate());
    }

    @Test
    public void testUpdateRITaxRateWithGreaterThanUIThreshold() {
        String psid = "199210091";

        createNonActiveCompany();
        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(getTaxSetupRIWithUIThreshold(), createTaxSetupServiceParameters(psid));
        TaxSetup taxSetupResult = serviceResult.getResult();
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(taxSetupResult);

        TaxItem taxItem = ServiceFactory.getInstance()
                                        .<TaxItem, TaxItemGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXITEMS)
                                        .service(createTaxItemServiceParameters(psid, "US_RI_SUI_ER_UI")).getResult();
        assertEquals("RI - UI rate", new BigDecimal("9.8"), taxItem.getTaxRates().get(0).getRate());
        taxItem = ServiceFactory.getInstance()
                                .<TaxItem, TaxItemGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXITEMS)
                                .service(createTaxItemServiceParameters(psid, "US_RI_SC_ER_WBI")).getResult();
        assertEquals("RI-WBI rate", new BigDecimal("10.01"), taxItem.getTaxRates().get(0).getRate());
        taxItem = ServiceFactory.getInstance()
                                .<TaxItem, TaxItemGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXITEMS)
                                .service(createTaxItemServiceParameters(psid, "US_RI_SC_ER_JDF")).getResult();
        assertEquals("RI- JDF", new BigDecimal("0.21"), taxItem.getTaxRates().get(0).getRate());
    }

    public static TaxSetup getTaxSetupRI() {
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_RI_SUI_ER_UI");
        taxItem.setJurisdictionId("US_RI");
        taxItem.setAgencyId("US_RI_DOT");
        taxItem.setName("RI UI/JDF Wage Base Increase");

        TaxRate taxRate = new TaxRate();
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(taxRate);
        taxRate.setRate(new BigDecimal("9.78"));
        Date startDate = DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        taxRate.setStartDate(startDate);
        taxItem.setTaxRates(taxRateList);
        taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);
        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupRIWithUIThreshold() {
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_RI_SUI_ER_UI");
        taxItem.setJurisdictionId("US_RI");
        taxItem.setAgencyId("US_RI_DOT");
        taxItem.setName("RI UI/JDF Wage Base Increase");

        TaxRate taxRate = new TaxRate();
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(taxRate);
        taxRate.setRate(new BigDecimal("9.8"));
        Date startDate = DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        taxRate.setStartDate(startDate);
        taxItem.setTaxRates(taxRateList);
        taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);
        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    private void createCompanyWithLaws() {
        // CA Company with laws, rates, etc.
        com.intuit.sbd.payroll.psp.domain.Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, COMPANY_FEIN, true, ServiceCode.Tax);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        company.setLegalName(COMPANY_NAME);
        company.setDbaName(company.getLegalName());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addCompanyLawsWithAgencyId("311-0765-1", company, "CA");
        DataLoadServices.addCompanyLawRates(company);
    }

    private void createNonActiveCompany() {
        // CA Company with laws, rates, etc. that is on hold.
        com.intuit.sbd.payroll.psp.domain.Company onHoldCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(onHoldCompany);
        // / Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(onHoldCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        onHoldCompany.setLegalName(COMPANY_NAME);
        onHoldCompany.setDbaName(onHoldCompany.getLegalName());
        PayrollServices.commitUnitOfWork();
    }

    private void createExemptCompany() {
        // todo should this be used?
        // CA Company with laws, rates, etc. that is Exempt.
        com.intuit.sbd.payroll.psp.domain.Company exemptCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, COMPANY_ID, true, ServiceCode.Tax);
        List<CompanyLaw> claws = DataLoadServices.addCompanyLawsWithAgencyId("123-4567-9", exemptCompany, "CA");
        DataLoadServices.addCompanyLawRates(exemptCompany);
        // Fix the company name so we can always compare correctly.
        PayrollServices.beginUnitOfWork();
        Application.refresh(exemptCompany);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        exemptCompany.setLegalName(COMPANY_NAME);
        exemptCompany.setDbaName(exemptCompany.getLegalName());
        for (CompanyLaw claw : claws) {
            // Set CA SUI_ER to Exempt.
            if (claw.getLaw().getLawId().equals("87")) {
                Application.refresh(claw);
                claw.setExemptionStatus(LawStatus.Exempt);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    public TaxSetupUpdateServiceParams createTaxSetupServiceParameters(String companyId) {
        TaxSetupUpdateServiceParams taxSetupUpdateServiceParams = new TaxSetupUpdateServiceParams();
        taxSetupUpdateServiceParams.setCompanyId(companyId);
        return taxSetupUpdateServiceParams;
    }

    public TaxItemGetServiceParams createTaxItemServiceParameters(String companyId, String taxItemId) {
        TaxItemGetServiceParams taxItemGetServiceParams = new TaxItemGetServiceParams();
        taxItemGetServiceParams.setCompanyId(companyId);
        taxItemGetServiceParams.setTaxItemId(taxItemId);
        return taxItemGetServiceParams;
    }

    public static Company createAssistedCompanyWithRates(String state, String psid, String ein, String aid) {
        // Company with laws, rates, etc.
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Tax);
        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIdsWithNoCalcuations(state);
        lawIds.removeAll(Arrays.asList(RateConverterFactory.INACTIVE_LAW_IDS_FOR_SUI_RATE_EXCHANGE));  //remove all inactive laws for SUI rate exchange
        DataLoadServices.addCompanyLawsWithAgencyId(aid, company, state, lawIds);
        DataLoadServices.addCompanyLawRates(company);
        return company;
    }

    @Test
    public void testAR_Round_Trip() {
        createAssistedCompanyWithRates("AR", COMPANY_ID, "987654321", "3110765 1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupAR("11.05"), createTaxSetupServiceParameters(COMPANY_ID));
        TaxSetup taxSetupResult = serviceResult.getResult();
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertEquals("Base SUI Rate", new Double(0.1105d), getCurrentRate("AR SUI-ER", COMPANY_ID, PSPDate.getPSPTime().getYear(), CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime())));
        //This is inactive now
        assertEquals("Supplemental SUI Rate", null, getCurrentActiveRateWithoutAssert("SUP-AR ST", COMPANY_ID, PSPDate.getPSPTime().getYear(), CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime())));

        assertNotNull(taxSetupResult);
    }

    @Test
    public void testAR_Invalid_Zero_Rate() {
        createAssistedCompanyWithRates("AR", COMPANY_ID, "987654321", "3110765 1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupAR("0.0"), createTaxSetupServiceParameters(COMPANY_ID));

        assertFalse(serviceResult.isSuccess());
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("AR SUI-ER", COMPANY_ID, PSPDate.getPSPTime().getYear(), CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime())));
    }

    @Test
    public void testAZ_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("AZ", psid, "987654321", "3110765 1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupAZ("0.8", "0.1"), createTaxSetupServiceParameters(psid));

        assertNotNull(serviceResult.getResult());
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertEquals("Base SUI Rate", new Double(0.008d), getCurrentRate("AZ SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("SUP-AZ TT", psid, year, quarter));
    }

    @Test
    public void testCA_Round_Trip() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("CA", psid, "987654321", "311-0765-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupCA("3.6", "0.1"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.036d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("ETT", psid, year, quarter));
    }

    @Test
    public void testCA_Invalid_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("CA", psid, "987654321", "311-0765-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupCA("8.3", "1.5"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("ETT", psid, year, quarter));
    }

    @Test
    public void testCO_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("CO", psid, "987654321", "311076-05-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupCO("1.7", "0.00"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.017d), getCurrentRate("CO SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00d), getCurrentRate("SUP-CO ST", psid, year, quarter));
    }

    @Test
    public void testCO_Qtr3_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("CO", psid, "987654321", "311076-05-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupCO("6", "0.0"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.06d), getCurrentRate("CO SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-CO ST", psid, year, quarter));
    }

    @Test
    public void testCT_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("CT", psid, "987654321", "3110765 1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupCT("6.8"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.068d), getCurrentRate("CT SUI-ER", psid, year, quarter));
    }

    @Test
    public void testGA_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupGA("0.04", null), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0004d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void testGA_Qtr2_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupGA("3.42", "0.06"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0342d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0006d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void testGA_Qtr2_Supp_Max_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupGA("8.1", "0.0"), createTaxSetupServiceParameters(psid));

        assertNotNull(serviceResult.getResult());
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());

        assertEquals("Base SUI Rate", new Double(0.081d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void testGA_Invalid_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("GA", psid, "987654321", "121531-04");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupGA("0.021", null), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("GA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-GA AA", psid, year, quarter));
    }

    @Test
    public void testHI_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("HI", psid, "987654321", "0007381875");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupHI("1.8"), createTaxSetupServiceParameters(psid));

        assertNotNull(serviceResult.getResult());
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());

        assertEquals("Base SUI Rate", new Double(0.018d), getCurrentRate("HI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0001d), getCurrentRate("SUP HI ETA", psid, year, quarter));
    }

    @Test
    public void testHI_Invalid_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("HI", psid, "987654321", "0007381875");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupHI("7.0"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("HI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP HI ETA", psid, year, quarter));
    }

    @Test
    public void testID_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("ID", psid, "987654321", "4595533-0");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupID("4.84", "0.05803"), createTaxSetupServiceParameters(psid));

        assertNotNull(serviceResult.getResult());
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());

        assertEquals("Base SUI Rate", new Double(0.0484d), getCurrentRate("ID SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0005803d), getCurrentRate("SUP-ID WDF", psid, year, quarter));
        
    }

    @Test
    public void testID_Invalid_Zero_Rate() {
        String psid =
                COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("ID", psid, "987654321", "4595533-0");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupID("0", null), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("ID SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-ID WDF", psid, year, quarter));
    }

    @Test
    public void testKY_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("KY", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupKY("6.283", "0.23"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.06283d), getCurrentRate("KY SUI-ER", psid, year, quarter));
        assertEquals("KY Surcharge", new Double(0.0023d), getCurrentRate("SUP-KY SC", psid, year, quarter));
    }

    @Test
    public void testKY_Round_Trip_Zero_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("KY", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupKY("0", "0.36"), createTaxSetupServiceParameters(psid));


        assertFalse(serviceResult.isSuccess());

        //This means as  base law has 0%, it will be updated.So old value remains.
        assertEquals("Base SUI Rate", new Double(0.03), getCurrentRate("KY SUI-ER", psid, year, quarter));
        assertEquals("KY Surcharge", new Double(0.03d), getCurrentRate("SUP-KY SC", psid, year, quarter));
    }

    @Test
    public void testMA_Round_Trip() {
        String psid = COMPANY_ID;
        DataLoadServices.updateMAPaymentTemplateSupportDate(PSPDate.getPSPTime());
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMA("1.60", "0.33"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0160d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00056d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        assertEquals("EMAC Rate", new Double(0.0033d), getCurrentAdditionalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter));
    }

    @Test
    public void testMA_Invalid_Zero_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMA("0", "0.36"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        assertEquals("EMAC Rate", null, getCurrentAdditionalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter));
    }

    @Test
    public void testMN_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MN", psid, "987654321", "10-07818-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMN("4"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.04d), getCurrentRate("MN SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("SUP-MN DWA", psid, year, quarter));
    }

    @Test
    public void testMN_Invalid_Zero_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MN", psid, "987654321", "10-07818-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMN("0"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MN SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MN DWA", psid, year, quarter));
    }

    @Test
    public void testNY_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNY("3.625", "0.075"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.03625d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00075d), getCurrentRate("SUP-NY RSF", psid, year, quarter));

        CompanyServiceParams companyServiceParams = new CompanyServiceParams();
        companyServiceParams.setExpand(Expand.TAXSETUP.toString());
        companyServiceParams.setCompanyId(COMPANY_ID);
        com.intuit.schema.payroll.v3.company.Company company = (com.intuit.schema.payroll.v3.company.Company) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.COMPANIES).service(companyServiceParams).getResult();
        assertNotNull(company);
        assertNotNull(company.getTaxSetup());
        assertNotNull(company.getTaxSetup().getTaxItems());
        assertEquals("Number of tax Items", 6, company.getTaxSetup().getTaxItems().size());
    }

    @Test
    public void testNY_Qtr4_Round_Trip() {
        String psid = COMPANY_ID;
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 6, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNY("3.625", null), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.03625d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00075d), getCurrentRate("SUP-NY RSF", psid, year, quarter));
    }

    @Test
    public void testNY_Invalid_Max_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNY("10.6", null), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NY RSF", psid, year, quarter));
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testNY_Invalid_Min_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNY("1.19", null), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NY RSF", psid, year, quarter));
    }

    @Test
    public void testNY_Invalid_Zero_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NY", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNY("0", null), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NY SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NY RSF", psid, year, quarter));
    }

    @Test
    public void testRI_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupRI("9.79"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0979d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0021d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.1000d), getCurrentRate("SUP-RI WBI", psid, year, quarter));
    }

    @Test
    public void testRI_Round_Trip_UIRateLessThanThreshold() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupRI("9.78"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0978d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0021d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-RI WBI", psid, year, quarter));
    }

    @Test
    public void testRI_Round_Trip_UIRateEqualsToThreshold() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupRI("9.80"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0980d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0021d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.1001d), getCurrentRate("SUP-RI WBI", psid, year, quarter));
    }

    @Test
    public void testRI_Invalid_Zero_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("RI", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupRI("0"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("RI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-RI JDF", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-RI WBI", psid, year, quarter));
    }

    @Test
    public void testME_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("ME", psid, "987654321", "10-07818-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupME("5.40", "0.07"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0540d), getCurrentRate("ME SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0007d), getCurrentRate("SUP-ME CSSF", psid, year, quarter));
    }

    @Test
    public void testME_Invalid_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("ME", psid, "987654321", "10-07818-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupME("9.25", "0.40"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("ME SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-ME CSSF", psid, year, quarter));
    }

    @Test
    public void testMI_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MI", psid, "987654321", "10-07818-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMI("6.625", "0.0"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.06625d), getCurrentRate("MI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-MI OA", psid, year, quarter));
    }

    @Test
    public void testMI_Invalid_Max_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MI", psid, "987654321", "10-07818-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMI("16.5", "3.56"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MI OA", psid, year, quarter));
    }

    @Test
    public void testMI_Invalid_Min_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MI", psid, "987654321", "10-07818-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMI("0.025", "0.28"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MI SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MI OA", psid, year, quarter));
    }

    @Test
    public void testMS_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MS", psid, "987654321", "17 08366 0 00");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMS("5.4", "5.4", "0"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.054d), getCurrentRate("MS SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0d), getCurrentRate("SUP-MS WTEF", psid, year, quarter));
    }
    @Test
    public void MS_Round_TripHaveWetRate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MS", psid, "987654321", "17 08366 0 00");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMS("5.4", "5.59", "0.16"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.054d), getCurrentRate("MS SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0016d), getCurrentRate("SUP-MS WTEF", psid, year, quarter));
    }

    @Test
    public void testMS_Round_Trip_precision4() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MS", psid, "987654321", "17 08366 0 00");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMS("5.4", "5.4", "0.16"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.054d), getCurrentRate("MS SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0016d), getCurrentRate("SUP-MS WTEF", psid, year, quarter));
    }

    @Test
    public void testMS_Invalid_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MS", psid, "987654321", "17 08366 0 00");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMS("6.4", "5.4", "0.33"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MS SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MS WTEF", psid, year, quarter));
    }

    @Test
    public void testMT_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MT", psid, "987654321", "2036142");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMT("2", "2.18", "0.18"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.020d), getCurrentRate("MT SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0018d), getCurrentRate("SUP-MT AFT", psid, year, quarter));
    }

    @Test
    public void testMT_Invalid_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MT", psid, "987654321", "2036142");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMT("1", "2.18", "0.01"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("MT SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-MT AFT", psid, year, quarter));
    }

    @Test
    public void testNH_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NH", psid, "987654321", "3110765 1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNH("10.5", "0.2"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.105d), getCurrentRate("NH SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.002d), getCurrentRate("SUP-NH AC", psid, year, quarter));
    }

    @Test
    public void testNH_Invalid_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NH", psid, "987654321", "3110765 1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNH("11.625", "1.4"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NH SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.03d), getCurrentRate("SUP-NH AC", psid, year, quarter));
    }

    @Test
    public void testNJ_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NJ", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNJ("3.2825", "0.5", "0.2", "0.117995", "0.1"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        // Base rate and WDF rates from example are both getting rounded.
        assertEquals("Base SUI Rate", new Double(0.032825d), getCurrentRate("NJ SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00118d), getCurrentRate("SUP-NJ WDF", psid, year, quarter));
        assertEquals("SDI Rate", new Double(0.005d), getCurrentRate("NJ SDI-ER", psid, year, quarter));
        assertEquals("Healthcare Rate", new Double(0.002d), getCurrentRate("SUP-NJ HSF", psid, year, quarter));
        assertEquals("FLI Rate", new Double(0.001d), getCurrentRate("SUP-NJ FLI", psid, year, quarter));
        //test law 171
        assertEquals("Special Healthcare Contributions", null, getCurrentActiveRateWithoutAssert("SUP-NJ SHC", psid, year, quarter));

    }

    @Test
    public void testNJ_Round_Trip_precision5() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NJ", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNJ("3.2825", "0.5", "0.2", "0.117500", "0.1"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        // Base rate and WDF rates from example are both getting rounded.
        assertEquals("Base SUI Rate", new Double(0.032825d), getCurrentRate("NJ SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00118d), getCurrentRate("SUP-NJ WDF", psid, year, quarter));
        assertEquals("SDI Rate", new Double(0.005d), getCurrentRate("NJ SDI-ER", psid, year, quarter));
        assertEquals("Healthcare Rate", new Double(0.002d), getCurrentRate("SUP-NJ HSF", psid, year, quarter));
        assertEquals("FLI Rate", new Double(0.001d), getCurrentRate("SUP-NJ FLI", psid, year, quarter));
        //test law 171
        assertEquals("Special Healthcare Contributions", null, getCurrentActiveRateWithoutAssert("SUP-NJ SHC", psid, year, quarter));

    }

    @Test
    public void testNJ_Round_Trip_InActiveFilingStatus() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        Company company = createAssistedCompanyWithRates("NJ", psid, "987654321", "06502929");

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        // Vertical bars should be removed during Master File creation which uses pipes/bars as
        // a field separator.
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "NJDLWD");
        for (CompanyLaw claw : companyAgency.getCompanyLawCollection()) {
            // Set VA SWT to Inactive.
            if (claw.getLaw().getLawId().equals("169")) {
                Application.refresh(claw);
                claw.setStatus(PayrollItemStatus.Inactive);
                claw.setFilingStatus(PayrollItemStatus.Inactive);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();


        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNJ("3.2825", "0.5", "0.2", "0.117995", "0.1"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());

        // Base rate and WDF rates from example are both getting rounded.
        assertEquals("Base SUI Rate", new Double(0.032825d), getCurrentRate("NJ SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00118d), getCurrentRate("SUP-NJ WDF", psid, year, quarter));
        assertEquals("SDI Rate", new Double(0.005d), getCurrentRate("NJ SDI-ER", psid, year, quarter));
        assertEquals("Healthcare Rate", null, getCurrentActiveRateWithoutAssert("SUP-NJ HSF", psid, year, quarter));
        assertEquals("FLI Rate", new Double(0.001d), getCurrentRate("SUP-NJ FLI", psid, year, quarter));
        assertEquals("Special Healthcare Contributions", null, getCurrentActiveRateWithoutAssert("SUP-NJ SHC", psid, year, quarter));
    }

    @Test
    public void testNJ_Invalid_Min_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NJ", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNJ("0.1", "0.5", "0", "0.1175", "0.1"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Base rate should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NJ SUI-ER", psid, year, quarter));

    }

    @Test
    public void testNJ_Invalid_Max_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NJ", psid, "987654321", "06502929");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNJ("9.0825", "0.1", "0", "0.1175", "0.1"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Base rate should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("NJ SUI-ER", psid, year, quarter));
    }

    @Test
    public void testNV_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("NV", psid, "987654321", "4595533-0");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupNV("3.725", "0.05", "0.36"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.03725d), getCurrentRate("NV SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0005d), getCurrentRate("SUP-NV CEP", psid, year, quarter));
        assertEquals("NV Bond Contribution Rate", new Double(0.0036d), getCurrentAdditionalFilingRate("NV Bond Contribution Rate", psid, year, quarter));
    }

    @Test
    public void testSC_Round_Trip() {

        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("SC", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupSC("5.4", "0.0505"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.054d), getCurrentRate("SC SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.000505d), getCurrentRate("SUP-SC CAT", psid, year, quarter));
    }

    @Test
    public void testSD_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("SD", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupSD("5.9", "0.505", "0.54"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.059d), getCurrentRate("SD SUI-ER", psid, year, quarter));
        assertEquals("SD Surcharge", new Double(0.00505d), getCurrentRate("SUP-SD TF", psid, year, quarter));
        assertEquals("SD Investment Fee", new Double(0.0054d), getCurrentRate("SUP-SD IF", psid, year, quarter));
    }

    @Test
    public void testTN_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("TN", psid, "987654321", "0757-930 6");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupTN("2.7", "0"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.027d), getCurrentRate("TN SUI-ER", psid, year, quarter));
    }

    @Test
    public void testWA_Round_Trip() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("WA", psid, "987654321", "311076-05-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupWA("4.05", "0.03"), createTaxSetupServiceParameters(psid));

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.0405d), getCurrentRate("WA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.0003d), getCurrentRate("SUP-WA WTF", psid, year, quarter));
    }

    @Test
    public void testWA_Invalid_Min_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("WA", psid, "987654321", "311076-05-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupWA("0.10", "0.55"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());

        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("WA SUI-ER", psid, year, quarter));
    }

    @Test
    public void testWA_Invalid_Max_Rate() {
        String psid = COMPANY_ID;
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("WA", psid, "987654321", "311076-05-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupWA("8.10", "0.06"), createTaxSetupServiceParameters(psid));

        assertFalse(serviceResult.isSuccess());
        // Rates should be unchanged at 3.0%.
        assertEquals("Base SUI Rate", new Double(0.03d), getCurrentRate("WA SUI-ER", psid, year, quarter));
    }

    @Test
    public void test_AgencyId_Update_EntityNotExists() throws Throwable {
        createAssistedCompanyWithRates("KS", COMPANY_ID, "987654332", "311076-05-1");
        //Getting existing data and validating before updating
        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(COMPANY_ID);
        taxSetupGetServiceParams.setExpand(Expand.TAXSETUP.toString());
        TaxSetup taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP)
                                                     .service(taxSetupGetServiceParams).getResult();
        assertNotNull(taxSetup);
        assertEquals("Agencies - IRS, KS-DOL and KS-DOR", 3, taxSetup.getAgencies().size());
        assertEquals("Tax payment groups - IRS-940, IRS-941, KS-KW5 and KS-KCNS100", 4, taxSetup.getTaxPaymentGroups().size());

        for (Agency agency : taxSetup.getAgencies()) {
            if(agency.getJurisdictionId().equals("US_FEDERAL")) {
                assertEquals("FEIN", "987654332", agency.getEmployerAccountNumber());
            } else if(agency.getJurisdictionId().equals("US_KS")) {
                assertEquals("KS State Id", "311076-05-1", agency.getEmployerAccountNumber());
            } else {
                assertTrue("Unexpected Jurisdiction Id is found", false);
            }
        }

        TaxSetupUpdateServiceParams taxSetupUpdateServiceParams = new TaxSetupUpdateServiceParams();
        taxSetupUpdateServiceParams.setCompanyId(COMPANY_ID);
        taxSetup = new TaxSetup();
        Agency irsAgency = new Agency();
        irsAgency.setId("US_FEDERAL"); // Invalid Agency Id
        irsAgency.setEmployerAccountNumber("987654300");  // Updating FEIN
        taxSetup.getAgencies().add(irsAgency);
        Agency moAgency = new Agency();
        moAgency.setId("US_MO_DOR");        // Trying to update state Id on MO agency - MO company agency is not present on this company
        moAgency.setEmployerAccountNumber("311076-05-0");
        taxSetup.getAgencies().add(moAgency);
        ServiceResult taxSetupUpdateResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetup, taxSetupUpdateServiceParams);
        assertFalse("Expecting error messages", taxSetupUpdateResult.isSuccess());
        assertNull(taxSetupUpdateResult.getResult());
        assertEquals("2 Messages", 2, taxSetupUpdateResult.getErrorMessages().size());
        for (Message message : taxSetupUpdateResult.getErrorMessages()) {
            assertEquals("Entity doesn't exists message", MessageCode.EntityDoesNotExist.getMessageCode(), message.getMessageCode());
        }
    }

    @Test
    public void test_KS_IRS_UpdateValidAgencyId() throws Throwable {
        String FEIN = "987654332";
        String invalidStateId = "036123456789F01";
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("KS-KW5-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));
        Company company = createAssistedCompanyWithRates("KS", COMPANY_ID, FEIN, invalidStateId);

        List<EmployeeDTO> employeeDTOs = DataLoadServices.createEEs(1, true);
        Employee employee = DataLoadServices.addEE(company, employeeDTOs.get(0));

        ArrayList<String> lawIds = DataLoadServices.getAllStateLawIds("KS");
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        for (String lawId : lawIds) {
            lawAmounts.put(lawId, "5");
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = Application.refresh(employee);
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO1, company, new DateDTO(2013, 10, 15), Arrays.asList(employee), lawAmounts);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction ksPayment = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("KS-KW5-PAYMENT")));
        assertEquals(invalidStateId, ksPayment.getAgencyTaxpayerId());
        assertNull(ksPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        //Getting existing data and validating before updating
        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(COMPANY_ID);
        taxSetupGetServiceParams.setExpand(Expand.TAXSETUP.toString());
        TaxSetup taxSetup = (TaxSetup) ServiceFactory.getInstance().constructGetServiceInstance(ResourceNameEnum.TAXSETUP)
                                                                          .service(taxSetupGetServiceParams).getResult();
        assertNotNull(taxSetup);
        assertEquals("Agencies - IRS, KS-DOL and KS-DOR", 3, taxSetup.getAgencies().size());
        assertEquals("Tax payment groups - IRS-940, IRS-941, KS-KW5 and KS-KCNS100", 4, taxSetup.getTaxPaymentGroups().size());

        for (Agency agency : taxSetup.getAgencies()) {
            if(agency.getJurisdictionId().equals("US_FEDERAL")) {
                assertEquals("FEIN", FEIN, agency.getEmployerAccountNumber());
            } else if(agency.getJurisdictionId().equals("US_KS")) {
                assertEquals("KS State Id", invalidStateId, agency.getEmployerAccountNumber());
            } else {
                assertTrue("Unexpected Jurisdiction Id is found", false);
            }
        }

        String newFEIN = "987654300";
        String newStateId = "036" + FEIN + "F01";
        TaxSetupUpdateServiceParams taxSetupUpdateServiceParams = new TaxSetupUpdateServiceParams();
        taxSetupUpdateServiceParams.setCompanyId(COMPANY_ID);
        taxSetup = new TaxSetup();
        Agency irsAgency = new Agency();
        irsAgency.setId("US_FEDERAL_IRS");
        irsAgency.setEmployerAccountNumber(newFEIN);  // Updating FEIN
        taxSetup.getAgencies().add(irsAgency);
        Agency ksAgency = new Agency();
        ksAgency.setId("US_KS_DOR");
        ksAgency.setEmployerAccountNumber(newStateId); // Updating US_KS_DOR agency Id only. US_KS_DOL remains same
        taxSetup.getAgencies().add(ksAgency);
        ServiceResult taxSetupUpdateResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetup, taxSetupUpdateServiceParams);
        assertTrue(taxSetupUpdateResult.isSuccess());
        taxSetup = (TaxSetup) taxSetupUpdateResult.getResult();
        assertNotNull(taxSetup);
        assertEquals("Agencies - IRS, KS-DOL and KS-DOR", 3, taxSetup.getAgencies().size());
        assertEquals("Tax payment groups - IRS-940, IRS-941, KS-KW5 and KS-KCNS100", 4, taxSetup.getTaxPaymentGroups().size());

        for (Agency agency : taxSetup.getAgencies()) {
            if(agency.getId().equals("US_FEDERAL_IRS")) {
                assertEquals("FEIN", newFEIN, agency.getEmployerAccountNumber()); // Updated FEIN to new value
            } else if(agency.getId().equals("US_KS_DOR")) {
                assertEquals("KS State Id", newStateId, agency.getEmployerAccountNumber());  // Updated US_KS_DOR agency Id to new value
            } else if(agency.getId().equals("US_KS_DOL")) {
                assertEquals("KS State Id", invalidStateId, agency.getEmployerAccountNumber());  // Not changed agency Id on US_KS_DOL
            } else {
                assertTrue("Unexpected Jurisdiction Id is found", false);
            }
        }

        PayrollServices.beginUnitOfWork();
        ksPayment = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("KS-KW5-PAYMENT")));
        assertEquals(newStateId, ksPayment.getAgencyTaxpayerId());
        assertEquals(PaymentMethod.ACHCredit, ksPayment.getMoneyMovementPaymentMethod());
        assertNotNull(ksPayment.getEntryDetailRecordCollection().findEntity(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_UpdateFilerTypeWithTaxRateAndStateId() throws Throwable {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        String psid = COMPANY_ID;
        String FEIN = "987654332";
        Company company = createAssistedCompanyWithRates("CA", psid, FEIN, "311-0765-1");
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 3, 3, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        TaxSetup taxSetup =PayloadHelper.getTaxSetupCA("3.6", "0.1");
        TaxSetupUpdateServiceParams taxSetupUpdateServiceParams = new TaxSetupUpdateServiceParams();
        taxSetupUpdateServiceParams.setCompanyId(COMPANY_ID);
        String newFEIN = "987654300";
        String newStateId = "311-0765-2";
        Agency irsAgency = new Agency();
        irsAgency.setId("US_FEDERAL_IRS");
        irsAgency.setEmployerAccountNumber(newFEIN);  // Updating FEIN
        taxSetup.getAgencies().add(irsAgency);
        Agency ksAgency = new Agency();
        ksAgency.setId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("CAEDD"));
        ksAgency.setEmployerAccountNumber(newStateId); // Updating US_KS_DOR agency Id only. US_KS_DOL remains same
        taxSetup.getAgencies().add(ksAgency);

        List<TaxFilingType> taxFilingTypes = new ArrayList<TaxFilingType>();
        TaxFilingType filingType = new TaxFilingType();
        filingType.setFilingType(FilingTypeEnum.form944);
        filingType.setStartDate(DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())));
        filingType.setAgencyName("Internal Revenue Service");
        taxFilingTypes.add(filingType);
        taxSetup.setTaxFilingTypes(taxFilingTypes);

        ServiceResult taxSetupUpdateResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetup, taxSetupUpdateServiceParams);
        assertTrue(taxSetupUpdateResult.isSuccess());
        taxSetup = (TaxSetup) taxSetupUpdateResult.getResult();
        assertNotNull(taxSetup);
        assertTrue(taxSetupUpdateResult.toString(), taxSetupUpdateResult.isSuccess());
        assertNotNull(taxSetupUpdateResult.getResult());

        assertEquals("Base SUI Rate", new Double(0.036d), getCurrentRate("CA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.001d), getCurrentRate("ETT", psid, year, quarter));

        for (Agency agency : taxSetup.getAgencies()) {
            if(agency.getId().equals("US_FEDERAL_IRS")) {
                assertEquals("FEIN", newFEIN, agency.getEmployerAccountNumber()); // Updated FEIN to new value
            } else if(agency.getId().equals("US_CA_EDD")) {
                assertEquals("KS State Id", newStateId, agency.getEmployerAccountNumber());  // Updated US_KS_DOR agency Id to new value
            } else {
                assertTrue("Unexpected Jurisdiction Id is found", false);
            }
        }
        for(TaxFilingType taxFilingType: taxSetup.getTaxFilingTypes()){
            assertEquals("FilingType",filingType.getFilingType(),taxFilingType.getFilingType());
            assertEquals("AgencyName",filingType.getAgencyName(),taxFilingType.getAgencyName());
            assertEquals("StartDate",filingType.getStartDate(),taxFilingType.getStartDate());
        }

    }
    @Test
    public void test_UpdateFilerType() throws Throwable {
        String FEIN = "987654332";
        String invalidStateId = "036123456789F01";
        Company company = createAssistedCompanyWithRates("KS", COMPANY_ID, FEIN, invalidStateId);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 3, 3, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        TaxSetup taxSetup = new TaxSetup();
        List<TaxFilingType> taxFilingTypes = new ArrayList<TaxFilingType>();
        TaxFilingType filingType = new TaxFilingType();
        filingType.setFilingType(FilingTypeEnum.form944);
        filingType.setStartDate(DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())));
        filingType.setAgencyName("Internal Revenue Service");
        taxFilingTypes.add(filingType);
        taxSetup.setTaxFilingTypes(taxFilingTypes);
        TaxSetupUpdateServiceParams taxSetupUpdateServiceParams = new TaxSetupUpdateServiceParams();
        taxSetupUpdateServiceParams.setCompanyId(COMPANY_ID);
        ServiceResult taxSetupUpdateResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetup, taxSetupUpdateServiceParams);
        assertTrue(taxSetupUpdateResult.isSuccess());
        taxSetup = (TaxSetup) taxSetupUpdateResult.getResult();
        assertNotNull(taxSetup);

       for(TaxFilingType taxFilingType: taxSetup.getTaxFilingTypes()){
           assertEquals("FilingType",filingType.getFilingType(),taxFilingType.getFilingType());
           assertEquals("AgencyName",filingType.getAgencyName(),taxFilingType.getAgencyName());
           assertEquals("StartDate",filingType.getStartDate(),taxFilingType.getStartDate());
       }

    }
    @Test
    public void test_UpdateFilerTypeFuture() throws Throwable {
        String FEIN = "987654332";
        String invalidStateId = "036123456789F01";
        Company company = createAssistedCompanyWithRates("KS", COMPANY_ID, FEIN, invalidStateId);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2015, 3, 3, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        TaxSetup taxSetup = new TaxSetup();
        List<TaxFilingType> taxFilingTypes = new ArrayList<TaxFilingType>();
        TaxFilingType filingType = new TaxFilingType();
        filingType.setFilingType(FilingTypeEnum.form944);
        filingType.setStartDate(DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())));
        filingType.setAgencyName("Internal Revenue Service");
        taxFilingTypes.add(filingType);
        filingType = new TaxFilingType();
        filingType.setFilingType(FilingTypeEnum.form941);
        filingType.setStartDate(DateUtil.getQuarterStartDate(new Date(SpcfCalendar.createInstance(2015, 12, 3, SpcfTimeZone.getLocalTimeZone()).getTimeInMilliseconds())));
        filingType.setAgencyName("Internal Revenue Service");
        taxFilingTypes.add(filingType) ;
        taxSetup.setTaxFilingTypes(taxFilingTypes);
        TaxSetupUpdateServiceParams taxSetupUpdateServiceParams = new TaxSetupUpdateServiceParams();
        taxSetupUpdateServiceParams.setCompanyId(COMPANY_ID);
        ServiceResult taxSetupUpdateResult = ServiceFactory.getInstance().constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetup, taxSetupUpdateServiceParams);
        assertTrue(taxSetupUpdateResult.isSuccess());
        taxSetup = (TaxSetup) taxSetupUpdateResult.getResult();
        assertNotNull(taxSetup);
        for(TaxFilingType taxFilingType: taxSetup.getTaxFilingTypes()){
            assertEquals("FilingType",FilingTypeEnum.form944,taxFilingType.getFilingType());
            assertEquals("AgencyName",filingType.getAgencyName(),taxFilingType.getAgencyName());
            assertEquals("StartDate",DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())),taxFilingType.getStartDate());
        }

    }

    @Test
    public void testMA_AdditionalFilingRates() {
        String psid = COMPANY_ID;
        DataLoadServices.updateMAPaymentTemplateSupportDate(PSPDate.getPSPTime());
        int year = PSPDate.getPSPTime().getYear();
        int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
        createAssistedCompanyWithRates("MA", psid, "987654321", "311-0765-1");

        ServiceResult<TaxSetup> serviceResult =
                ServiceFactory.getInstance()
                              .<TaxSetup, TaxSetupUpdateServiceParams>constructUpdateServiceInstance(ResourceNameEnum.TAXSETUP)
                              .service(PayloadHelper.getTaxSetupMA_AdditionalFiling("1.60", "0.33"), createTaxSetupServiceParameters(psid));
        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(psid);
        taxSetupGetServiceParams.setShowAllParam(ShowHistory.TAXRATE.name());
        serviceResult = ServiceFactory.getInstance().<TaxSetup, TaxSetupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams);

        assertTrue(serviceResult.toString(), serviceResult.isSuccess());
        assertNotNull(serviceResult.getResult());
        assertEquals("Total MA Emac items", 6, serviceResult.getResult().getTaxItems().size());
        Map <String,BigInteger>idsCount= new HashMap<String, BigInteger>();
        TaxItem emactaxitem = null;
        for(TaxItem taxitem: serviceResult.getResult().getTaxItems()){
            BigInteger count = idsCount.get(taxitem.getId());
            if(count == null){
                idsCount.put(taxitem.getId(),new BigInteger("1"));
            }   else{
                idsCount.put(taxitem.getId(),count.add(new BigInteger("1")));
            }
            if("US_MA_SC_ER_EMAC".equals(taxitem.getId())){
                emactaxitem =  taxitem;
            }

        }
       for(Map.Entry<String, BigInteger> ids :idsCount.entrySet()){
           assertEquals("Tax item count", 1, ids.getValue().intValue());
       }
        assertEquals("Base SUI Rate", new Double(0.0160d), getCurrentRate("MA SUI-ER", psid, year, quarter));
        assertEquals("Supplemental SUI Rate", new Double(0.00056d), getCurrentRate("SUP-MA WTF", psid, year, quarter));
        assertEquals("EMAC Rate", 3, emactaxitem.getTaxRates().size());
        assertEquals("EMAC Rate", new Double(0.0032d), getCurrentAdditionalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter));
        if(++quarter > 4){
            quarter =1;
            year++;
        }
        assertEquals("EMAC Rate", new Double(0.0034d), getCurrentAdditionalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter+1));
        if(++quarter > 4){
            quarter = 1;
            year++;
        }
        assertEquals("EMAC Rate", new Double(0.0035d), getCurrentAdditionalFilingRate("MA Er Medical Assistance Contribution", psid, year, quarter+1));
        DataLoadServices.setPSPDate(2016,2,16);
        taxSetupGetServiceParams = new TaxSetupGetServiceParams();
        taxSetupGetServiceParams.setCompanyId(psid);
        serviceResult = ServiceFactory.getInstance().<TaxSetup, TaxSetupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams);
        idsCount= new HashMap<String, BigInteger>();
        for(TaxItem taxitem: serviceResult.getResult().getTaxItems()){
            BigInteger count = idsCount.get(taxitem.getId());
            if(count == null){
                idsCount.put(taxitem.getId(),new BigInteger("1"));
            }   else{
                idsCount.put(taxitem.getId(),count.add(new BigInteger("1")));
            }
            if("US_MA_SC_ER_EMAC".equals(taxitem.getId())){
                emactaxitem =  taxitem;
            }

        }
        for(Map.Entry<String, BigInteger> ids :idsCount.entrySet()){
            assertEquals("Tax item count", 1, ids.getValue().intValue());
        }
        assertEquals("EMAC Rate effetcive", 1, emactaxitem.getTaxRates().size());

        assertEquals("EMAC Rate", new BigDecimal("0.35"), emactaxitem.getTaxRates().get(0).getRate());

    }

    @Test
    public void testAdditionalFilingAmount(){
        Application.beginUnitOfWork();
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_UHI");
        taxItem.setAgencyId("US_MA_WUA");
        taxItem.setName("");
        List <TaxRate>taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(PayloadHelper.getTaxRateList("0.03",PSPDate.getPSPTime()));
        taxItem.setTaxRates(taxRateList);
        DomainEntitySet<AdditionalFilingAmount> additionalFilingAmounts = Application.find(AdditionalFilingAmount.class,AdditionalFilingAmount.ATFLawId().isNotNull());
        for(AdditionalFilingAmount additionalFilingAmount: additionalFilingAmounts){
                DomainEntitySet<Law> laws = Application.find(Law.class, Law.LawId().equalTo(additionalFilingAmount.getATFLawId()));
                taxItem.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(additionalFilingAmount.getPaymentTemplate().getAgency().getAgencyId()));
                if(!(taxItem.getAgencyId().equals("US_MT_UID")||taxItem.getAgencyId().equals("US_WA_ESD")))
                {
                    assertTrue("Not an additional lawid",CompanyTaxItemUpdateService.isAdditionalFilingAmount(laws.getFirst(),additionalFilingAmount.getATFLawId(),taxItem));
                }
        }
        DomainEntitySet<Law> laws = Application.find(Law.class, Law.LawId().isNotNull());
        for(Law law: laws){
            DomainEntitySet<AdditionalFilingAmount> additionalFilingAmount = Application.find(AdditionalFilingAmount.class, AdditionalFilingAmount.ATFLawId().equalTo(law.getLawId()));
            taxItem.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(law.getPaymentTemplate().getAgency().getAgencyId()));
            if(additionalFilingAmount.size() >0 ){
                if(!(law.getLawId().equals("109")||law.getLawId().equals("131"))) {
                    assertTrue("Not a lawid", CompanyTaxItemUpdateService.isAdditionalFilingAmount(law, additionalFilingAmount.getFirst().getATFLawId(), taxItem));
                }
            }
        }
        Application.rollbackUnitOfWork();
    }

    private static Double getCurrentRate(String lawTypeCd, String psid, int year, int quarter) {
        SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        DomainEntitySet<CompanyLawRate> clrs = Application.find(CompanyLawRate.class,
                                                                CompanyLawRate.CompanyLaw().Law().LawTypeCd().equalTo(lawTypeCd)
                                                                              .And(CompanyLawRate.CompanyLaw().CompanyAgency().Company().SourceCompanyId().equalTo(psid))
                                                                              .And(CompanyLawRate.InvalidDate().isNull())
                                                                              .And(CompanyLawRate.EffectiveDate().greaterOrEqualThan(firstDayOfQuarter)));
        assertEquals("Number of rates for company/law", 1, clrs.size());
        return clrs.getFirst().getRate();
    }

    private static Double getCurrentActiveRateWithoutAssert(String lawTypeCd, String psid, int year, int quarter) {
        SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        DomainEntitySet<CompanyLawRate> clrs = Application.find(CompanyLawRate.class,
                                                                CompanyLawRate.CompanyLaw().Law().LawTypeCd().equalTo(lawTypeCd)
                                                                              .And(CompanyLawRate.CompanyLaw().CompanyAgency().Company().SourceCompanyId().equalTo(psid))
                                                                              .And(CompanyLawRate.InvalidDate().isNull())
                                                                              .And(CompanyLawRate.CompanyLaw().FilingStatus().equalTo(PayrollItemStatus.Active))
                                                                              .And(CompanyLawRate.EffectiveDate().greaterOrEqualThan(firstDayOfQuarter)));
        if (clrs != null && clrs.getFirst() != null) {
            return clrs.getFirst().getRate();
        }
        return null;
    }

    private static Double getCurrentAdditionalFilingRate(String taxItem, String psid, int year, int quarter) {
        SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        DomainEntitySet<CompanyFilingAmount> clfs = Application.find(CompanyFilingAmount.class,
                                                                     CompanyFilingAmount.CompanyAgencyPaymentTemplate().CompanyAgency().Company().SourceCompanyId().equalTo(psid)
                                                                                        .And(CompanyFilingAmount.Name().equalTo(taxItem))
                                                                                        .And(CompanyFilingAmount.Name().equalTo(taxItem))
                                                                                        .And(CompanyFilingAmount.EffectiveDate().lessOrEqualThan(firstDayOfQuarter))
                                                                                        .And(CompanyFilingAmount.InvalidDate().isNull())).sort(CompanyFilingAmount.EffectiveDate().Descending());
        if (clfs == null || clfs.size() == 0) {
            return null;
        }
        return clfs.getFirst().getAmount();

    }
}
