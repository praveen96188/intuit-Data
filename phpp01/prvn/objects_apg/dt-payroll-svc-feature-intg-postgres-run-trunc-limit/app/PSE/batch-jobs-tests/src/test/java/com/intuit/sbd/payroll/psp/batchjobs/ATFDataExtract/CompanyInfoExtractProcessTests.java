package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * User: ihannur
 * Date: 10/5/12
 * Time: 5:11 PM
 */
public class CompanyInfoExtractProcessTests {
    static boolean extractCompanyInfo = false;

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        extractCompanyInfo = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_COMPANY_INFO_EXTRACT);
        PayrollServices.commitUnitOfWork();
        if (!extractCompanyInfo) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_INFO_EXTRACT, "true");
        }

    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extractCompanyInfo) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_INFO_EXTRACT, "false");
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.reinitialize();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{ "116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4*i));
            lawAmounts.put("1", String.valueOf(1.5*i));
            lawAmounts.put("131", String.valueOf(13.1*i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0*i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6*i)); // NV SUI-ER


            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
            lawIds.add(companyLaw.getSourceId());
            companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_HappyPath"));
    }

    @Test
    public void testHappyPathWithDGDeletedCompany() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{ "116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4*i));
            lawAmounts.put("1", String.valueOf(1.5*i));
            lawAmounts.put("131", String.valueOf(13.1*i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0*i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6*i)); // NV SUI-ER


            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
            lawIds.add(companyLaw.getSourceId());
            companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        Application.beginUnitOfWork();
        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);
        company.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedDGData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_HappyPath"));
    }

    @Test
    public void testCompanyTerminatedLastTaxQtrNotNull() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4*i));
            lawAmounts.put("1", String.valueOf(1.5*i));
            lawAmounts.put("131", String.valueOf(13.1*i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0*i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6*i)); // NV SUI-ER


            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
            lawIds.add(companyLaw.getSourceId());
            companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        DataLoadServices.updateCompanyService(companies.get(0), ServiceCode.Tax, ServiceSubStatusCode.Terminated);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companies.get(0).getSourceCompanyId(), SourceSystemCode.QBDT);
        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
        taxCompanyServiceInfo.setLastQuarterToFile(20121);
        taxCompanyServiceInfo.setFinalAnnualReturns(true);
        taxCompanyServiceInfo.setFileAnnualReturns(true);
        taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2012, 1, 7, SpcfTimeZone.getLocalTimeZone()));
        Application.save(taxCompanyServiceInfo);
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_Terminated"));
    }

    @Test
    public void testCompanyTerminatedLastTaxQtrNull() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4*i));
            lawAmounts.put("1", String.valueOf(1.5*i));
            lawAmounts.put("131", String.valueOf(13.1*i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0*i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6*i)); // NV SUI-ER


            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
            lawIds.add(companyLaw.getSourceId());
            companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companies.get(0).getSourceCompanyId(), SourceSystemCode.QBDT);
        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
        taxCompanyServiceInfo.setFinalAnnualReturns(true);
        taxCompanyServiceInfo.setFileAnnualReturns(true);
        taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2012, 1, 7, SpcfTimeZone.getLocalTimeZone()));
        Application.save(taxCompanyServiceInfo);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateCompanyService(companies.get(0), ServiceCode.Tax, ServiceSubStatusCode.Terminated);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_TerminatedLastTaxQtrNull"));
    }

    @Test
    public void testOnHoldNonExpiredAndExpired() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MI"};
        DataLoadServices.setupCompany(158905L, 5, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);

        //Adding 5 companies above..
        //Put 3 companies on Hold, remove hold from one Company
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();
                
        DataLoadServices.addCompanyOnHoldReason(companies.get(0), ServiceSubStatusCode.Fraud);
        DataLoadServices.addCompanyOnHoldReason(companies.get(0), ServiceSubStatusCode.AchRejectR1R9);

        DataLoadServices.addCompanyOnHoldReason(companies.get(1), ServiceSubStatusCode.AS400Hold);

        DataLoadServices.addCompanyOnHoldReason(companies.get(2), ServiceSubStatusCode.FraudReview);

        DataLoadServices.cancelService(companies.get(3), ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companies.get(3).getSourceCompanyId(), SourceSystemCode.QBDT);
        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
        taxCompanyServiceInfo.setLastQuarterToFile(20121);
        taxCompanyServiceInfo.setFinalAnnualReturns(true);
        taxCompanyServiceInfo.setFileAnnualReturns(true);
        taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
        Application.save(taxCompanyServiceInfo);
        PayrollServices.commitUnitOfWork();
        //Remove Hold from second company..
        DataLoadServices.removeCompanyOnHoldReasons(companies.get(1));

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_updated_happypath"));
    }

    @Test
    public void testCompanyPendingSetup() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));

        CompanyDTO companyDTO  = DataLoadServices.createCompany(SourceSystemCode.QBDT, "1212");
        Company company = DataLoadServices.newCompany(companyDTO, "123456");

        DataLoadServices.addTaxService(company);
        DataLoadServices.addDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_PendingSetup"));
    }

    @Test
    public void testUpdateCompany() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"NV", "WA", "VT"};
        String[] stateLawIds = new String[]{"116", "130", "131"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4*i));
            lawAmounts.put("1", String.valueOf(1.5*i));
            lawAmounts.put("131", String.valueOf(13.1*i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0*i)); // VT SUI-ER
            lawAmounts.put("116", String.valueOf(11.6*i)); // NV SUI-ER


            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
            lawIds.add(companyLaw.getSourceId());
            companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            company = Application.refresh(company);
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            companyDTO.getLegalAddress().setAddressLine1("123 Main Street");
            companyDTO.getLegalAddress().setAddressLine2("Suite A");
            companyDTO.getLegalAddress().setCity("San Bernardino");
            companyDTO.getLegalAddress().setState("CA");
            companyDTO.getLegalAddress().setZipCode("92407");
            companyDTO.getLegalAddress().setZipCodeExtension("");
            ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
            assertTrue(processResult.isSuccess());

            PayrollServices.commitUnitOfWork();
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_UpdateCompany"));
    }

    @Test
    public void testMultipleRuns() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MI"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
            taxCompanyServiceInfo.setLastQuarterToFile(20121);
            taxCompanyServiceInfo.setFinalAnnualReturns(true);
            taxCompanyServiceInfo.setFileAnnualReturns(true);
            taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
            Application.save(taxCompanyServiceInfo);
            PayrollServices.commitUnitOfWork();
        }
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_MultipleRun1"));

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_MultipleRun2"));
    }

    @Test
    public void testAllThenUpdate() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MI"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
            taxCompanyServiceInfo.setLastQuarterToFile(20121);
            taxCompanyServiceInfo.setFinalAnnualReturns(true);
            taxCompanyServiceInfo.setFileAnnualReturns(true);
            taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
            Application.save(taxCompanyServiceInfo);
            PayrollServices.commitUnitOfWork();
        }
        ATFDataExtractTestsUtil.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData, 2011, 1, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_MultipleRun1"));

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_MultipleRun2"));
    }

    @Test
    public void testUpdateCompanyMultipleRuns() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MI"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            company = Application.refresh(company);
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
            taxCompanyServiceInfo.setLastQuarterToFile(20121);
            taxCompanyServiceInfo.setFinalAnnualReturns(true);
            taxCompanyServiceInfo.setFileAnnualReturns(true);
            taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
            Application.save(taxCompanyServiceInfo);
            PayrollServices.commitUnitOfWork();
        }
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_MultipleRun1"));

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("158906", SourceSystemCode.QBDT);
        company.setLegalName("Updated Data");
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyInfo, "atfextract/expected/test_CompanyInfo_UpdateCompanyRun2"));
    }

}
