package com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS;

import com.intuit.ems.tfs.messages.v1.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedUnprocessedRequestTests;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEmployeeAnnualTotals;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEmployeeTotalsTestsHelper;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import javax.jws.WebParam;
import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * User: mvillani
 * Date: 8/29/2012
 * Time: 10:36 AM
 */
public class SendW2DataToTFSTests {

    private static final List<String> TAX_FORM_LINES = Arrays.asList("ALLOCTIPS", "DPDNTCARE", "DPDNTCARECO", "NONQUALPLAN", "SEC457", "ADOPTION",
                                                                     "GROUPTERMLIFE", "MEDSAVING", "NONTAXSICK", "QUALMVEX", "ROTH401K", "ROTH403B",
                                                                     "SIMPLE", "Q125POP", "Q401K", "Q403B", "Q408K", "Q457B",
                                                                     "TTT14", "Q501C", "TTT3", "TTT7", "TTT8", "FRNGBNFTS",
                                                                     "OTHER", "OTHMVEXP", "TTT1", "TTT2", "TTT4", "TTT5",
                                                                     "LTAX1", "LTAX2", "SECLOCAL", "TTT11", "TTT6", "TTT9", "TIPS", "TTT10", "TTT17", "TTT19");

    private static final List<String> DEDUCTION_TAX_FORM_LINES = Arrays.asList("Q125POP", "Q401K", "ROTH401K", "Q403B", "Q457B", "NONTAXSICK",
                                                                               "LTAX1", "TTT10", "GROUPTERMLIFE",
                                                                               "NONQUALPLAN", "DPDNTCARE",
                                                                               "Q408K", "TTT9", "SIMPLE",
                                                                               "DPDNTCARECO", "ROTH403B");

    private static final List<String> COMPENSATION_TAX_FORM_LINES = Arrays.asList("ALLOCTIPS", "DPDNTCARE", "DPDNTCARECO", "NONQUALPLAN", "SEC457", "ADOPTION",
                                                                                  "GROUPTERMLIFE", "MEDSAVING", "NONTAXSICK", "QUALMVEX", "ROTH401K", "ROTH403B",
                                                                                  "SIMPLE", "Q125POP", "Q401K", "Q403B", "Q408K", "Q457B",
                                                                                  "TTT14", "Q501C", "TTT3", "TTT7", "TTT8", "FRNGBNFTS",
                                                                                  "OTHER", "OTHMVEXP", "TTT1", "TTT2", "TTT4", "TTT5",
                                                                                  "LTAX1", "LTAX2", "SECLOCAL", "TTT11", "TTT6", "TTT9", "TIPS", "TTT10", "TTT17", "TTT19");

    private static final List<String> ER_CONTRIB_TAX_FORM_LINES = Arrays.asList("TTT7", "FRNGBNFTS", "TTT17",
                                                                                "ALLOCTIPS", "TTT1", "Q501C",
                                                                                "TTT8", "TTT3", "QUALMVEX",
                                                                                "OTHER", "MEDSAVING", "TTT19");

    private static final Map<String, BigDecimal> W2_CODES;

    static {
        W2_CODES = new HashMap<String, BigDecimal>();
        W2_CODES.put("ALLOCTIPS", new BigDecimal("51"));
        W2_CODES.put("DPDNTCARE", new BigDecimal("6"));
        W2_CODES.put("DPDNTCARECO", new BigDecimal("48"));
        W2_CODES.put("NONQUALPLAN", new BigDecimal("8"));
        W2_CODES.put("SEC457", new BigDecimal("7"));
        W2_CODES.put("ADOPTION", new BigDecimal("47"));
        W2_CODES.put("GROUPTERMLIFE", new BigDecimal("50"));
        W2_CODES.put("MEDSAVING", new BigDecimal("45"));
        W2_CODES.put("NONTAXSICK", new BigDecimal("2"));
        W2_CODES.put("QUALMVEX", new BigDecimal("29"));
        W2_CODES.put("ROTH401K", new BigDecimal("57"));
        W2_CODES.put("ROTH403B", new BigDecimal("58"));
        W2_CODES.put("SIMPLE", new BigDecimal("46"));
        W2_CODES.put("Q125POP", new BigDecimal("53"));
        W2_CODES.put("Q401K", new BigDecimal("11"));
        W2_CODES.put("Q403B", new BigDecimal("12"));
        W2_CODES.put("Q408K", new BigDecimal("13"));
        W2_CODES.put("Q457B", new BigDecimal("14"));
        W2_CODES.put("TTT14", new BigDecimal("67"));
        W2_CODES.put("Q501C", new BigDecimal("15"));
        W2_CODES.put("TTT3", new BigDecimal("56"));
        W2_CODES.put("TTT7", new BigDecimal("60"));
        W2_CODES.put("TTT8", new BigDecimal("61"));
        W2_CODES.put("FRNGBNFTS", new BigDecimal("9"));
        W2_CODES.put("OTHER", new BigDecimal("3"));
        W2_CODES.put("OTHMVEXP", new BigDecimal("10"));
        W2_CODES.put("TTT1", new BigDecimal("54"));
        W2_CODES.put("TTT2", new BigDecimal("55"));
        W2_CODES.put("TTT4", new BigDecimal("57"));
        W2_CODES.put("TTT5", new BigDecimal("58"));
        W2_CODES.put("LTAX1", new BigDecimal("17"));
        W2_CODES.put("LTAX2", new BigDecimal("19"));
        W2_CODES.put("SECLOCAL", new BigDecimal("55"));
        W2_CODES.put("TTT11", new BigDecimal("64"));
        W2_CODES.put("TTT6", new BigDecimal("59"));
        W2_CODES.put("TTT9", new BigDecimal("62"));
        W2_CODES.put("TIPS", new BigDecimal("4"));
        W2_CODES.put("TTT10", new BigDecimal("63"));
        W2_CODES.put("TTT17", new BigDecimal("70"));
        W2_CODES.put("TTT19", new BigDecimal("72"));
    }

    private static final String TEST_COMPANY_ID = "158906";
    private static final String TEST_EMPLOYEE_ID = "First_4 M_4 Last_4";


    @AfterClass
    public static void afterClass() {

        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        PayrollServices.beginUnitOfWork();
        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);
        companyListParameter.setSystemParameterValue(null);
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    @Ignore
    public void testHappyPath_WithCompanyList() throws Exception {

        createAnnualData();

        PayrollServices.beginUnitOfWork();

        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue("158906,111111111");
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();

        ProcessResult<HashMap<String, PayrollFormInfo>> processResult = new TFSW2PreviewSendJob().main((new String[]{"-year:2012"}));
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("158906", SourceSystemCode.QBDT);
        CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);


        assertEquals("Submission status: ", TFSSubmissionStatus.Submitted, companyTFSSubmission.getSubmissionStatus());
        PayrollFormInfo payrollFormInfo = processResult.getResult().get(company.getId().toString());
        List<PayrollFormInfo.EmployeeInfo> employeeInfos = payrollFormInfo.getEmployeeInfo();
        assertEquals("Number of Employees: ", 5, employeeInfos.size());
        PayrollFormInfo.CompanyInfo companyInfo = payrollFormInfo.getCompanyInfo();
        compareCompanyInfo(companyInfo, company);
        compareCompanyTotals(companyInfo, company);
        compareCompanyPayrollItemTotals(companyInfo, company);

        company = Company.findCompany("158905", SourceSystemCode.QBDT);
        companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
        assertEquals("Submission status: ", TFSSubmissionStatus.Pending, companyTFSSubmission.getSubmissionStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);
        companyListParameter.setSystemParameterValue(null);
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    @Ignore("Works only on local")
    public void testHappyPath_AllCompanies() throws Exception {

        createAnnualData();

        ProcessResult processResult = new TFSW2AnnualSendJob().main((new String[]{"-year:2012"}));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        for (Company company : companies) {
            CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
            if (companyTFSSubmission != null) {
                assertEquals("Submission status: ", TFSSubmissionStatus.Submitted, companyTFSSubmission.getSubmissionStatus());
            }
        }

        Application.rollbackUnitOfWork();

    }


    @Test
    @Ignore ("Works only on local")
    public void testHappyPath_IL() throws Exception {

        createAnnualData();

        BatchJobManager.runJob(BatchJobType.SendMonthlyDataToTFSProcessor,"-year:2012", "-month:1" );

        Application.rollbackUnitOfWork();

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testTFSTransmissionError() throws Exception {
        createAnnualData();
        new TFSW2AnnualSendJob().main((new String[]{"-year:2012", "-forceError:true"}));
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        for (Company company : companies) {
            CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
            if (companyTFSSubmission != null) {
                assertEquals("Submission status: ", TFSSubmissionStatus.Error, companyTFSSubmission.getSubmissionStatus());
            }
        }

        Application.rollbackUnitOfWork();

    }

    @Test
    @Ignore("works only in local if host set to reno ")
    public void testForNonNullAmountTransmissionW2DataToTFS(){

       createAnnualData();
       Application.beginUnitOfWork();

       QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();

       String select ="select cPItemInfo.CompanyPayrollItem.Id from  com.intuit.sbd.payroll.psp.domain.QbdtPayrollItemInfo cPItemInfo";
       org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select);

       List<Object[]> results = (List<Object[]>) hibernateQuery.list();
       Application.commitUnitOfWork();

       Application.beginUnitOfWork();
       String update ="update com.intuit.sbd.payroll.psp.domain.EmployeeW2Totals eeTotals set eeTotals.CompanyPayrollItem = '"+results.get(0)+"'";
       hibernateQuery = Application.createHibernateQuery(update);
       hibernateQuery.executeUpdate();

       Application.commitUnitOfWork();

       new TFSW2AnnualSendJob().main((new String[]{"-year:2012"}));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        for (Company company : companies) {
            CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
            if (companyTFSSubmission != null) {
                assertEquals("Submission status: ", TFSSubmissionStatus.Submitted, companyTFSSubmission.getSubmissionStatus());
            }
        }

        Application.rollbackUnitOfWork();
    }
    @Test
    @Ignore ("Works only on local")
    public void testForNullAmountTransmissionW2DataToTFS(){
                createAnnualData();
                Application.beginUnitOfWork();

               QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();

                  String select ="select cPItemInfo.CompanyPayrollItem.Id from  com.intuit.sbd.payroll.psp.domain.QbdtPayrollItemInfo cPItemInfo";
                org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select);

                        List<Object[]> results = (List<Object[]>) hibernateQuery.list();
                Application.commitUnitOfWork();

                                //Setting LawFK and Amount to null
                                //It gets replaced by 0.0

                                                Application.beginUnitOfWork();
                String update ="update com.intuit.sbd.payroll.psp.domain.EmployeeW2Totals eeTotals set eeTotals.CompanyPayrollItem = '"+results.get(0)+"',"+"eeTotals.Amount ='',eeTotals.Law=''";
                hibernateQuery = Application.createHibernateQuery(update);
                hibernateQuery.executeUpdate();


                               Application.commitUnitOfWork();

                new TFSW2AnnualSendJob().main((new String[]{"-year:2012"}));

                 PayrollServices.beginUnitOfWork();
                    DomainEntitySet<Company> companies = Application.find(Company.class);
                    for (Company company : companies) {
                        CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
                        if (companyTFSSubmission != null) {
                            assertEquals("Submission status: ", TFSSubmissionStatus.Submitted, companyTFSSubmission.getSubmissionStatus());
                        }
                    }
                 Application.rollbackUnitOfWork();


             }

    @Test
    @Ignore ("Works only on local")
    public void testForNullAmountForLawFkTransmissionW2DataToTFS(){
        createAnnualData();
        Application.beginUnitOfWork();

        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();

        String select ="select cPItemInfo.CompanyPayrollItem.Id from  com.intuit.sbd.payroll.psp.domain.QbdtPayrollItemInfo cPItemInfo";
        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select);

        List<Object[]> results = (List<Object[]>) hibernateQuery.list();
        Application.commitUnitOfWork();

        //Setting LawFK and Amount to null
        //It gets replaced by 0.0

        Application.beginUnitOfWork();
        String update ="update com.intuit.sbd.payroll.psp.domain.EmployeeW2Totals eeTotals set eeTotals.CompanyPayrollItem = '"+results.get(0)+"',"+"eeTotals.Amount =''";
        hibernateQuery = Application.createHibernateQuery(update);
        hibernateQuery.executeUpdate();


        Application.commitUnitOfWork();

        new TFSW2AnnualSendJob().main((new String[]{"-year:2012"}));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        for (Company company : companies) {
            CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
            if (companyTFSSubmission != null) {
                assertEquals("Submission status: ", TFSSubmissionStatus.Submitted, companyTFSSubmission.getSubmissionStatus());
            }
        }
        Application.rollbackUnitOfWork();


    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPriorEIN_EffectiveDateNotStartOfYear() {

       createAnnualData();

        PayrollServices.beginUnitOfWork();

        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue("158906,158905");
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        int i=0;
        String eins[]=new String[companies.size()];
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
            eins[i]="99123456"+i;
            CompanyDTO companyDTO = new DTOFactory().create(company);
            companyDTO.setFein(eins[i]);
            if (!company.getFedTaxId().equals(eins[i])) {
                EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
                entityChangeDTO.setHasNewDataFile(false);
                entityChangeDTO.setIsSuccessor(true);
                entityChangeDTO.setIsError(false);
                DateDTO dateDTO= new DateDTO();
                dateDTO.setYear(PSPDate.getPSPTime().getYear());
                dateDTO.setMonth(0);
                dateDTO.setDay(20);
                entityChangeDTO.setEffectiveDate(dateDTO);
                companyDTO.setEntityChange(entityChangeDTO);
            }

            ProcessResult processResult=       PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),
                                                                 company.getSourceCompanyId(),
                                                                 companyDTO) ;
            i++;
        }
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
         companies = Application.find(Company.class);
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
        PayrollFormInfoBuilder builder = new PayrollFormInfoBuilder();
        builder = builder.buildCompany(company,2012);
            assertNotNull(builder.getPayrollFormInfo().getCompanyInfo().getPreviousEmployerID()) ;
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPriorEIN_EffectiveDateAsStartOfYear() {

        createAnnualData();

        PayrollServices.beginUnitOfWork();

        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue("158906,158905");
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        int i=0;
        String eins[]=new String[companies.size()];
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
            eins[i]="99123456"+i;
            CompanyDTO companyDTO = new DTOFactory().create(company);
            companyDTO.setFein(eins[i]);
            if (!company.getFedTaxId().equals(eins[i])) {
                EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
                entityChangeDTO.setHasNewDataFile(false);
                entityChangeDTO.setIsSuccessor(true);
                entityChangeDTO.setIsError(false);
                DateDTO dateDTO= new DateDTO();
                dateDTO.setYear(PSPDate.getPSPTime().getYear());
                dateDTO.setMonth(0);
                dateDTO.setDay(1);
                entityChangeDTO.setEffectiveDate(dateDTO);
                companyDTO.setEntityChange(entityChangeDTO);
            }

            ProcessResult processResult=       PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),
                                                                                            company.getSourceCompanyId(),
                                                                                            companyDTO) ;
            i++;
        }
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        companies = Application.find(Company.class);
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
            PayrollFormInfoBuilder builder = new PayrollFormInfoBuilder();
            builder = builder.buildCompany(company,2012);
            assertNull(builder.getPayrollFormInfo().getCompanyInfo().getPreviousEmployerID()) ;
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPriorEIN_EffectiveDateAsPriorYear() {

        createAnnualData();

        PayrollServices.beginUnitOfWork();

        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue("158906,158905");
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        int i=0;
        String eins[]=new String[companies.size()];
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
            eins[i]="99123456"+i;
            CompanyDTO companyDTO = new DTOFactory().create(company);
            companyDTO.setFein(eins[i]);
            if (!company.getFedTaxId().equals(eins[i])) {
                EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
                entityChangeDTO.setHasNewDataFile(false);
                entityChangeDTO.setIsSuccessor(true);
                entityChangeDTO.setIsError(false);
                DateDTO dateDTO= new DateDTO();
                dateDTO.setYear(PSPDate.getPSPTime().getYear()-1);
                dateDTO.setMonth(11);
                dateDTO.setDay(30);
                entityChangeDTO.setEffectiveDate(dateDTO);
                companyDTO.setEntityChange(entityChangeDTO);
            }

            ProcessResult processResult=       PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),
                                                                                            company.getSourceCompanyId(),
                                                                                            companyDTO) ;
            i++;
        }
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        companies = Application.find(Company.class);
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
            PayrollFormInfoBuilder builder = new PayrollFormInfoBuilder();
            builder = builder.buildCompany(company,2012);
            assertNull(builder.getPayrollFormInfo().getCompanyInfo().getPreviousEmployerID()) ;
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPriorEIN_EffectiveDateAsNextYear() {

        createAnnualData();

        PayrollServices.beginUnitOfWork();

        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue("158906,158905");
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        int i=0;
        String eins[]=new String[companies.size()];
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
            eins[i]="99123456"+i;
            CompanyDTO companyDTO = new DTOFactory().create(company);
            companyDTO.setFein(eins[i]);
            if (!company.getFedTaxId().equals(eins[i])) {
                EntityChangeDTO entityChangeDTO = new EntityChangeDTO();
                entityChangeDTO.setHasNewDataFile(false);
                entityChangeDTO.setIsSuccessor(true);
                entityChangeDTO.setIsError(false);
                DateDTO dateDTO= new DateDTO();
                dateDTO.setYear(PSPDate.getPSPTime().getYear()+1);
                dateDTO.setMonth(11);
                dateDTO.setDay(30);
                entityChangeDTO.setEffectiveDate(dateDTO);
                companyDTO.setEntityChange(entityChangeDTO);
            }

            ProcessResult processResult=       PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(),
                                                                                            company.getSourceCompanyId(),
                                                                                            companyDTO) ;
            i++;
        }
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        companies = Application.find(Company.class);
        for (Company company : companies) {
            if(company.getSourceCompanyId().equals("111111111")) {
                continue;
            }
            PayrollFormInfoBuilder builder = new PayrollFormInfoBuilder();
            builder = builder.buildCompany(company,2012);
            assertNull(builder.getPayrollFormInfo().getCompanyInfo().getPreviousEmployerID()) ;
        }
        PayrollServices.rollbackUnitOfWork();
    }


    private void createAnnualData() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
        String[] statesList = new String[]{"NV", "WA", "VT", "IL"};
        String[] stateLawIds = new String[]{"116", "130", "131", "97"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts = initializeLawAmounts(i);
            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                Application.beginUnitOfWork();
                CompanyLawRate clr = new CompanyLawRate();
                clr.setCompanyLaw(companyLaw);
                clr.setEffectiveDate(PSPDate.getPSPTime());
                clr.setRate(new Double("0.059999"));
                Application.save(clr);
                Application.commitUnitOfWork();

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
            String hourlyReg = String.valueOf(companyPayrollItemSourceId);

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
            String hourlyVac = String.valueOf(companyPayrollItemSourceId);
            Integer startId = 10;
            startId = createPayrollItems(company, PayrollItemType.Deduction, startId);
            createPayrollItems(company, PayrollItemType.EmployerContribution, startId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2012-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                    EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                    employerContributionTransactionDTO.setContributionAmount(new BigDecimal(i));
                    employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                    employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                    employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                    deductionTransactionDTO.setDeductionAmount(new BigDecimal(i));
                    deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));

                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                    if ((stateLawId.equals("131")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                        deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                        employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                        deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                        employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder((long) k);

                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                    paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                    paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);


                    employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
                    CompanyPayrollItem cpi = findItemForSourcePayrollItemId(company, "TTT1", PayrollItemType.EmployerContribution);
                    employerContributionTransactionDTO.setSourcePayrollItemId(cpi.getSourcePayrollItemId());
                    employerContributionTransactionDTO.setContributionAmount(W2_CODES.get("TTT1"));
                    employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                    employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                    employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                    cpi = findItemForSourcePayrollItemId(company, "TTT10", PayrollItemType.Deduction);
                    deductionTransactionDTO = new DeductionTransactionDTO();
                    deductionTransactionDTO.setDeductionAmount(W2_CODES.get("TTT10"));
                    deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));
                    deductionTransactionDTO.setSourcePayrollItemId(cpi.getSourcePayrollItemId());

                    paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                    paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
                }
                k++;
            }

            CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_" + k, new DateDTO("2011-08-14"));
            Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
            companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

            LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
            liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
            liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
            liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

            for (Employee employee : employees) {

                QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                qbdtPayrollTransactionDTO.setAmount(new SpcfMoney("27.27"));
                qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 2));
                QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                qTlDTO.setAmount(new SpcfMoney("27.27"));
                qTlDTO.setPayrollItemId(String.valueOf(companyPayrollItemSourceId));
                qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                for (String stateLawId : stateLawIds) {
                    SpcfMoney amount = new SpcfMoney(stateLawId);
                    liabilityAdjustmentDTOs.add(DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, (SpcfMoney) amount.multiply(new SpcfMoney("2")), (SpcfMoney) amount.divide(new SpcfMoney("2")), false));

                }
            }

            payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().add(companyAdjustmentSubmissionDTO);
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;
        }

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        // Submit Payroll Item Adjustments without a payroll
        PayrollServices.beginUnitOfWork();
        companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        for (Company company : companies) {

            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            for (Employee employee : employees) {
                for (String taxFormLine : DEDUCTION_TAX_FORM_LINES) {
                    QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                    qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 20));
                    QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                    qTlDTO.setAmount(new SpcfMoney(W2_CODES.get(taxFormLine).negate().toString()));
                    qTlDTO.setPayrollItemId(String.valueOf(taxFormLine));
                    qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                    PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                }
                for (String taxFormLine : ER_CONTRIB_TAX_FORM_LINES) {
                    QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                    qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 20));
                    QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                    qTlDTO.setAmount(new SpcfMoney(W2_CODES.get(taxFormLine).toString()));
                    qTlDTO.setPayrollItemId(String.valueOf(taxFormLine));
                    qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                    PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                }

            }
        }
        PayrollServices.commitUnitOfWork();
        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        Company testCompany = Company.findCompany(TEST_COMPANY_ID, SourceSystemCode.QBDT);
        Employee testEmployee = Employee.findEmployee(testCompany, TEST_EMPLOYEE_ID);
        DomainEntitySet<EmployeePayrollItemQtrTotals> employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                Assert.assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
            } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
//                Assert.assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(W2_CODES.get(taxFormLine).multiply(new BigDecimal(7)).toString()), eeTotals.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();
        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[]{"-year:2012"});
    }

    private Integer createPayrollItems(Company pCompany, PayrollItemType pPayrollItemType, Integer pStartId) {
        List<String> taxFormLines = new ArrayList<String>();
        PayrollItemCode payrollItemCode = null;

        switch (pPayrollItemType) {
            case Deduction:
                taxFormLines = DEDUCTION_TAX_FORM_LINES;
                payrollItemCode = PayrollItemCode.OtherPreTaxDeduction;

                break;
            case Compensation:
                taxFormLines = COMPENSATION_TAX_FORM_LINES;
                payrollItemCode = PayrollItemCode.Compensation;

                break;
            case EmployerContribution:
                taxFormLines = ER_CONTRIB_TAX_FORM_LINES;
                payrollItemCode = PayrollItemCode.OtherNonTaxableEmployerContribution;

                break;
        }
        Integer i = pStartId;
        for (String taxFormLine : taxFormLines) {
            createCompanyPayrollItem(pCompany, taxFormLine, i.toString(), payrollItemCode);
            i++;
        }
        return i;
    }

    private void createCompanyPayrollItem(Company pCompany, String pTaxFormLine, String pSourcePayrollItemId, PayrollItemCode pPayrollItemCode) {

        List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(pPayrollItemCode);
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxFormLine(pTaxFormLine);
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(pSourcePayrollItemId);
        companyPayrollItemDTO.setSourcePayrollItemDescription(pTaxFormLine);
        DataLoadServices.persistPayrollItems(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyPayrollItemDTOs);
    }


    private HashMap<String, String> initializeLawAmounts(double pMultiplier) {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", String.valueOf(6.1 * pMultiplier));
        lawAmounts.put("62", String.valueOf(6.2 * pMultiplier));
        lawAmounts.put("63", String.valueOf(6.3 * pMultiplier));
        lawAmounts.put("64", String.valueOf(6.4 * pMultiplier));
        lawAmounts.put("65", String.valueOf(6.5 * pMultiplier));
        lawAmounts.put("1", String.valueOf(1.5 * pMultiplier));
        lawAmounts.put("131", String.valueOf(13.1 * pMultiplier)); // WA SUI-ER
        lawAmounts.put("130", String.valueOf(13.0 * pMultiplier)); // VT SUI-ER
        //lawAmounts.put("120", String.valueOf(12 * pMultiplier));   // OR SUI-ER
        lawAmounts.put("116", String.valueOf(11.6 * pMultiplier)); // NV SUI-ER
        return lawAmounts;
    }

    private List<Object[]> getCompanyLawTotals(SpcfUniqueId pCompanyId, int pYear) {

        String select =
                " select company.Id, law.LawId,  " +
                        "sum(eeTotals.Amount) as TotalTaxAmount," +
                        " sum(eeTotals.TaxableWages) as TotalTaxWages, " +
                        " sum(eeTotals.TipsTaxableWagesAmount) as TotalTipsTaxableWages, " +
                        " sum(eeTotals.TotalWages) as TotalTotalWages" +
                        " from  com.intuit.sbd.payroll.psp.domain.EmployeeW2Totals eeTotals, " +
                        "       com.intuit.sbd.payroll.psp.domain.Law as law, " +
                        "       com.intuit.sbd.payroll.psp.domain.Company as company";
        String where =
                " where eeTotals.Year = :year" +
                        "   and eeTotals.Company = company " +
                        "   and eeTotals.Law = law" +
                        "   and eeTotals.Law is not null ";


        if (pCompanyId != null) {
            where += "   and eeTotals.Company = :company";
        }


        String groupBy = " group by company.Id,law.LawId";
        org.hibernate.Query hibernateQuery;
        try {
            hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

            hibernateQuery.setParameter("year", pYear);

            if (pCompanyId != null) {
                Company company = Application.findById(Company.class, pCompanyId);
                hibernateQuery.setParameter("company", company);
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        try {
            @SuppressWarnings({"unchecked"})
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();
            return results;

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private List<Object[]> getCompanyPayrollItemTotals(SpcfUniqueId pCompanyId, int pYear) {
        try {

            String select =
                    " select company.Id,  cPItem.Id, " +
                            "sum(eeTotals.Amount) as TotalAmount," +
                            " sum(eeTotals.TaxableWages) as TotalTaxWages, " +
                            " sum(eeTotals.TipsTaxableWagesAmount) as TotalTipsTaxableWages," +
                            " sum(eeTotals.TotalWages) as TotalTotalWages" +
                            " from  com.intuit.sbd.payroll.psp.domain.EmployeeW2Totals eeTotals," +
                            "       com.intuit.sbd.payroll.psp.domain.CompanyPayrollItem as cPItem, " +
                            "       com.intuit.sbd.payroll.psp.domain.Company as company";

            String where =
                    " where eeTotals.Year = :year" +
                            "   and eeTotals.Company = company " +
                            "   and eeTotals.CompanyPayrollItem = cPItem" +
                            "   and eeTotals.CompanyPayrollItem is not null";

            if (pCompanyId != null) {
                where += "   and eeTotals.Company = :company";
            }


            String groupBy = " group by company.Id, cPItem.Id";

            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

            hibernateQuery.setParameter("year", pYear);

            if (pCompanyId != null) {
                Company company = Application.findById(Company.class, pCompanyId);
                hibernateQuery.setParameter("company", company);
            }

            @SuppressWarnings({"unchecked"})
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();
            return results;

        } catch (Throwable t) {
            throw new RuntimeException(t);

        }
    }

    private DomainEntitySet<EmployeeW2Totals> getEEW2Totals(Company pCompany, int pYear) {
        Criterion<EmployeeW2Totals> where = EmployeeW2Totals.Company().equalTo(pCompany)
                                                            .And(EmployeeW2Totals.Year().equalTo(pYear));
        DomainEntitySet<EmployeeW2Totals> eeW2Totals = Application.find(EmployeeW2Totals.class, where);
        return eeW2Totals;
    }

    private void compareCompanyInfo(PayrollFormInfo.CompanyInfo pCompanyInfo, Company pCompany) {

        assertEquals("CompanyLegal Name: ", pCompany.getLegalName(), pCompanyInfo.getCompanyName());
        assertEquals("Company DBA Name: ", pCompany.getDbaName(), pCompanyInfo.getCompanyName());
        assertEquals("Address Line 1: ", pCompany.getLegalAddress().getAddressLine1(), pCompanyInfo.getLegalAddressLine1());
        assertEquals("Address Line 2: ", pCompany.getLegalAddress().getAddressLine2(), pCompanyInfo.getLegalAddressLine2());
        assertEquals("City: ", pCompany.getLegalAddress().getCity(), pCompanyInfo.getLegalCity());
        assertEquals("State: ", pCompany.getLegalAddress().getState(), pCompanyInfo.getLegalState().toString());
        assertEquals("Zip: ", pCompany.getLegalAddress().getFullZipCode(), pCompanyInfo.getLegalZip());
        assertEquals("Country: ", pCompany.getLegalAddress().getCountry(), pCompanyInfo.getLegalCountry());
        assertEquals("Intuit Payroll Service Id: ", pCompany.getSourceCompanyId(), pCompanyInfo.getIntuitPayrollServiceID());

    }

    private void compareCompanyTotals(PayrollFormInfo.CompanyInfo pCompanyInfo, Company pCompany) {
        List<FederalTotals> federalTotalsList = pCompanyInfo.getFederalTotals();
        assertEquals("Federal Totals Size: ", 1, federalTotalsList.size());
        List<Object[]> companyLawTotalsList = getCompanyLawTotals(pCompany.getId(), 2012);
        BigDecimal socialSecurityWages = new BigDecimal(0);
        BigDecimal socialSecurityLiability = new BigDecimal(0);
        BigDecimal socialSecurityTips = new BigDecimal(0);
        BigDecimal federalWithholding = new BigDecimal(0);
        BigDecimal federalWages = new BigDecimal(0);
        BigDecimal futaLiability = new BigDecimal(0);
        BigDecimal futaTaxableWages = new BigDecimal(0);
        BigDecimal futaTotalWages = new BigDecimal(0);
        BigDecimal medicareLiability = new BigDecimal(0);
        BigDecimal medicareWages = new BigDecimal(0);

        for (Object[] companyTotals : companyLawTotalsList) {

            Law law = Application.findById(Law.class, companyTotals[1].toString());
            if (isFederalLaw(law.getLawId())) {
                // Social Security - Law = FICA
                if (law.isFICA()) {
                    socialSecurityWages = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[5]);
                    socialSecurityLiability = socialSecurityLiability.add(SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[2]));
                    socialSecurityTips = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[4]);
                }

                // Federal - Law = FIT
                if (law.isFIT()) {
                    federalWithholding = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[2]);
                    federalWages = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[5]);
                }

                // FUTA
                if (law.isFUTA()) {
                    futaLiability = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[2]);
                    futaTaxableWages = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[4]);
                    futaTotalWages = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[5]);
                }

                // MEDICARE
                if (law.isMED()) {
                    medicareLiability = medicareLiability.add(SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[2]));
                    medicareWages = SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[5]);
                }
            } else {
                TaxItemTotals taxItemTotals = getTaxItemTotals(law.getLawId(), pCompanyInfo);
                if (taxItemTotals != null) {
                    assertEquals("Tax Amount: ", SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[2]), taxItemTotals.getTaxAmount());
                    if (companyTotals[3] != null) {
                        assertEquals("Taxable Wages: ", SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[3]), taxItemTotals.getTaxableWagesAndTips());
                    }
                    if (companyTotals[4] != null) {
                        assertEquals("Tips: ", SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[4]), taxItemTotals.getTotalTips());
                    }
                    if (companyTotals[5] != null) {
                        assertEquals("Total Wages: ", SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[5]), taxItemTotals.getTotalWagesAndTips());
                    }
                }

            }

        }

        FederalTotals federalTotals = federalTotalsList.get(0);
        assertEquals("FIT Withholding: ", federalWithholding, federalTotals.getFederalWithholding());
        assertEquals("FIT Wages: ", federalWages, federalTotals.getFederalWages());
        assertEquals("Social Security Wages: ", socialSecurityWages, federalTotals.getSocialSecurityWages());
        assertEquals("Social Security Liability: ", socialSecurityLiability, federalTotals.getSocialSecurityLiability());
        assertEquals("Social Security Tips: ", socialSecurityTips, federalTotals.getSocialSecurityTips());
        assertEquals("FUTA Liability: ", futaLiability, federalTotals.getFUTALiability());
        assertEquals("FUTA Taxable Wages: ", futaTaxableWages, federalTotals.getTaxableFUTAWages());
        assertEquals("FUTA Total Wages: ", futaTotalWages, federalTotals.getTotalFUTAWages());

    }


    private void compareCompanyPayrollItemTotals(PayrollFormInfo.CompanyInfo pCompanyInfo, Company pCompany) {

        List<Object[]> companyPayrollItemsList = getCompanyPayrollItemTotals(pCompany.getId(), 2012);

        for (Object[] companyTotals : companyPayrollItemsList) {
            CompanyPayrollItem companyPayrollItem = Application.findById(CompanyPayrollItem.class, companyTotals[1]);
            if (companyPayrollItem != null) {

                W2Totals w2Totals = getW2Totals(companyPayrollItem.getTaxFormLine(), pCompanyInfo);
                if (w2Totals != null && !companyPayrollItem.getTaxFormLine().equals("OTHER")) {
                    assertEquals("Payroll Item Amount " + companyPayrollItem.getTaxFormLine(), SpcfUtils.convertToBigDecimal((SpcfMoney) companyTotals[2]), w2Totals.getAmount());

                }
            }
        }
    }

    private boolean isFederalLaw(String pLawId) {
        Law law = Application.findById(Law.class, pLawId);
        return (law.isFIT() || law.isFICA() || law.isFUTA() || law.isAEIC() || law.isMED());
    }

    private TaxItemTotals getTaxItemTotals(String pLawId, PayrollFormInfo.CompanyInfo pCompanyInfo) {
        List<TaxItemTotals> taxItemTotalsList = pCompanyInfo.getTaxItemTotals();
        for (TaxItemTotals taxItemTotals : taxItemTotalsList) {
            if (taxItemTotals.getTaxTableID().toString().equals(pLawId)) {
                return taxItemTotals;
            }
        }
        return null;
    }

    private W2Totals getW2Totals(String pTaxFormLine, PayrollFormInfo.CompanyInfo pCompanyInfo) {
        List<W2Totals> w2TotalsList = pCompanyInfo.getW2Totals();
        for (W2Totals w2Totals : w2TotalsList) {
            if (w2Totals.getCode().toString().equals(W2_CODES.get(pTaxFormLine).toString())) {
                return w2Totals;
            }
        }
        return null;
    }

    private CompanyPayrollItem findItemForSourcePayrollItemId(Company pCompany, String pTaxFormLine, PayrollItemType pPayrollItemType) {
        CompanyPayrollItem foundCompanyPayrollItem = null;

        DomainEntitySet<CompanyPayrollItem> existingCompanyPayrollItems =
                Application.find(CompanyPayrollItem.class, new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(pCompany)
                                                 .And(CompanyPayrollItem.TaxFormLine().equalTo(pTaxFormLine))
                                                 .And(CompanyPayrollItem.PayrollItem().PayrollItemType().equalTo(pPayrollItemType))));


        if (existingCompanyPayrollItems.size() > 1) {
            throw new RuntimeException("Did not find zero or one CompanyPayrollItem as expected for company " + pCompany.getId() + " and tax form line " + pTaxFormLine);
        }

        if (existingCompanyPayrollItems.size() > 0) {
            foundCompanyPayrollItem = existingCompanyPayrollItems.get(0);
        }

        return foundCompanyPayrollItem;
    }

    /**
     *  Fix for PSP-7002
     *  PSP was trying to send duplicate CompanyLawRates to TFS and TFS was throwing error. Thus the W2 Previews were not getting generated.
     *  This fix solves it.
     *  It requires the dev url of the TFS system to validate the send, since its configured as localhost on the teamcity build this test will run only on Local
     * @throws Exception
     */
    @Test
    @Ignore("Works only on local")
    public void testDuplicateCompanyLawRatesToTFS() throws Exception {

        createAnnualData();

        /**
         * Creating duplicate companylawrates
         */

        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);
        PayrollServices.beginUnitOfWork();

        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "131");
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);

        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2011-10-01"));
        companyLawRateDTO1.setRate(0.05d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);

        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-10-01"));
        companyLawRateDTO2.setRate(0.07d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);

        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        processResult = new TFSW2AnnualSendJob().main((new String[]{"-year:2012"}));
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class);
        for (Company company1 : companies) {
            CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company1, 2012);
            if (companyTFSSubmission != null) {
                assertEquals("Submission status: ", TFSSubmissionStatus.Submitted, companyTFSSubmission.getSubmissionStatus());
            }
        }
        Application.rollbackUnitOfWork();
    }

    private void createAnnualDataWithduplicatePitems() {
        createAnnualData();

        /**
         * Creating duplicate companylaws/pitems
         */

        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);
        PayrollServices.beginUnitOfWork();

        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "131");
        CompanyLawDTO duplicatePitem = PayrollServices.dtoFactory.create(companyLaw);
        duplicatePitem.setSourceId("99");


        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), duplicatePitem);
        assertSuccess("companyLawRateUpdated", processResult);

        CompanyLaw companyLaw97 = CompanyLaw.findCompanyLaw(company, "97");
        CompanyLawDTO duplicatePitem97 = PayrollServices.dtoFactory.create(companyLaw97);
        duplicatePitem97.setSourceId("777");


        ProcessResult processResultLaw97 = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), duplicatePitem97);
        assertSuccess("companyLawRateUpdated", processResultLaw97);
        PayrollServices.commitUnitOfWork();


        //submit a payroll after duplication of pitems
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());
        String[] stateLawIds = new String[]{"116", "130", "131", "97"};
        paycheckDate = new DateDTO("2012-01-29");
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawAmounts = initializeLawAmounts(2);
        lawAmounts.put("131", String.valueOf(14.2 * 2));
        lawAmounts.put("97", String.valueOf(9.7 * 2));
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        int k = 1;
        int i = 1;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            String hourlyVac = "1";
            String hourlyReg = "1";
            for (String stateLawId : stateLawIds) {
                CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                employerContributionTransactionDTO.setContributionAmount(new BigDecimal(i));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                deductionTransactionDTO.setDeductionAmount(new BigDecimal(i));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));

                compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                if ((stateLawId.equals("131")) && (k % 2 == 0)) {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                } else {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                }
                QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                compensationTransactionDTO.setPayStubOrder((long) k);

                paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);


                employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
                CompanyPayrollItem cpi = findItemForSourcePayrollItemId(company, "TTT1", PayrollItemType.EmployerContribution);
                employerContributionTransactionDTO.setSourcePayrollItemId(cpi.getSourcePayrollItemId());
                employerContributionTransactionDTO.setContributionAmount(W2_CODES.get("TTT1"));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                cpi = findItemForSourcePayrollItemId(company, "TTT10", PayrollItemType.Deduction);
                deductionTransactionDTO = new DeductionTransactionDTO();
                deductionTransactionDTO.setDeductionAmount(W2_CODES.get("TTT10"));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));
                deductionTransactionDTO.setSourcePayrollItemId(cpi.getSourcePayrollItemId());

                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
            }
            k++;
        }

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_" + k, new DateDTO("2011-08-14"));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        for (Employee employee : employees) {

            QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
            qbdtPayrollTransactionDTO.setAmount(new SpcfMoney("27.27"));
            qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
            qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 2));
            QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
            qTlDTO.setAmount(new SpcfMoney("27.27"));
            String companyPayrollItemSourceId = "1";
            qTlDTO.setPayrollItemId(String.valueOf(companyPayrollItemSourceId));
            qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
            PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
            for (String stateLawId : stateLawIds) {
                SpcfMoney amount = new SpcfMoney(stateLawId);
                liabilityAdjustmentDTOs.add(DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, (SpcfMoney) amount.multiply(new SpcfMoney("2")), (SpcfMoney) amount.divide(new SpcfMoney("2")), false));

            }
        }

        payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().add(companyAdjustmentSubmissionDTO);
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(submitPayrollResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);


        //Run Annuals total calculations job
        new CalculateEmployeeAnnualTotals().main(new String[]{"-year:2012"});
    }

    @Test
    public void testDuplicateCompanyLawsToTFS() {
        createAnnualDataWithduplicatePitems();
        PayrollServices.beginUnitOfWork();

        //Check for multiple records created for law_id 142
        Company pCompany = Company.findCompany("158905", SourceSystemCode.QBDT);
        Employee pEmployee = pCompany.getEmployees().getFirst();
        Employee pEmployee1 = pCompany.getEmployees().get(1);
        int pQuarter = 1;
        int pYear = 2012;
        Criterion<EmployeeLawQtrTotals> where = EmployeeLawQtrTotals.Company().equalTo(pCompany)
                .And(EmployeeLawQtrTotals.Employee().equalTo(pEmployee)
                        .And(EmployeeLawQtrTotals.Quarter().equalTo(pQuarter))
                        .And(EmployeeLawQtrTotals.Year().equalTo(pYear))
                        .And(EmployeeLawQtrTotals.Law().LawId().equalTo("131")));
        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotals = Application.find(EmployeeLawQtrTotals.class, where);
        Assert.assertEquals(employeeLawQtrTotals.size(), 2);

        Criterion<EmployeeW2Totals> ew2TWhere = EmployeeW2Totals.Company().equalTo(pCompany)
                .And(EmployeeW2Totals.Employee().equalTo(pEmployee)
                        .And(EmployeeW2Totals.Year().equalTo(pYear))
                        .And(EmployeeW2Totals.Law().LawId().equalTo("131")));
        DomainEntitySet<EmployeeW2Totals> employeeW2Totalses = Application.find(EmployeeW2Totals.class, ew2TWhere);
        Assert.assertEquals(employeeW2Totalses.size(), 2);

        //Checking for one more employee
        where = EmployeeLawQtrTotals.Company().equalTo(pCompany)
                .And(EmployeeLawQtrTotals.Employee().equalTo(pEmployee1)
                        .And(EmployeeLawQtrTotals.Quarter().equalTo(pQuarter))
                        .And(EmployeeLawQtrTotals.Year().equalTo(pYear))
                        .And(EmployeeLawQtrTotals.Law().LawId().equalTo("131")));
        employeeLawQtrTotals = Application.find(EmployeeLawQtrTotals.class, where);
        Assert.assertEquals(employeeLawQtrTotals.size(), 2);

        ew2TWhere = EmployeeW2Totals.Company().equalTo(pCompany)
                .And(EmployeeW2Totals.Employee().equalTo(pEmployee1)
                        .And(EmployeeW2Totals.Year().equalTo(pYear))
                        .And(EmployeeW2Totals.Law().LawId().equalTo("131")));
        employeeW2Totalses = Application.find(EmployeeW2Totals.class, ew2TWhere);
        Assert.assertEquals(employeeW2Totalses.size(), 2);

        //Create the request
        CompanyTFSSubmission companyTFSSubmission = new CompanyTFSSubmission();
        SubmitFilingRequest submitFilingRequest = new SendW2DataToTFS(new String[]{}, FilingTypeType.UnmodifiableAnnualPreviewData).createSubmitFilingRequest(pCompany, 2012, companyTFSSubmission.getId().toString());

        //Check for the merge of the duplicates in the CompanyTaxItemTotals
        List<TaxItemTotals> taxItemTotals = submitFilingRequest.getPayrollFormInfo().get(0).getCompanyInfo().getTaxItemTotals();
        ew2TWhere = EmployeeW2Totals.Company().equalTo(pCompany)
                .And(EmployeeW2Totals.Year().equalTo(pYear))
                .And(EmployeeW2Totals.Law().LawId().equalTo("131"));
        DomainEntitySet<EmployeeW2Totals> companyTaxItemTotals = Application.find(EmployeeW2Totals.class, ew2TWhere);

        SpcfMoney amount = SpcfMoney.ZERO;                              //TaxAmount
        SpcfMoney taxableWages = SpcfMoney.ZERO;                        //TaxableWagesAndTips
        SpcfMoney tipsTaxableWagesAmount = SpcfMoney.ZERO;              //TotalTips
        SpcfMoney totalWages = SpcfMoney.ZERO;                          //TotalWagesAndTips
        for (EmployeeW2Totals itotal : companyTaxItemTotals) {
            amount = (SpcfMoney) amount.add(itotal.getAmount());
            taxableWages = (SpcfMoney) taxableWages.add(itotal.getTaxableWages());
            tipsTaxableWagesAmount = (SpcfMoney) tipsTaxableWagesAmount.add(itotal.getTipsTaxableWagesAmount());
            totalWages = (SpcfMoney) totalWages.add(itotal.getTotalWages());
        }
        int count = 0;
        TaxItemTotals itemTotal = new TaxItemTotals();
        for (TaxItemTotals itemTotals : taxItemTotals) {
            String lawId = itemTotals.getTaxTableID().toString();
            if (lawId.equalsIgnoreCase("131")) {
                count++;
                itemTotal = itemTotals;
            }
        }
        Assert.assertEquals(1, count);
        Assert.assertEquals("Annual/W2PreviewData Company TaxAmount : ", amount.toString(), itemTotal.getTaxAmount().toString());
        Assert.assertEquals("Annual/W2PreviewData Company TaxableWagesAndTips : ", taxableWages.toString(), itemTotal.getTaxableWagesAndTips().toString());
        Assert.assertEquals("Annual/W2PreviewData Company TotalTips : ", tipsTaxableWagesAmount.toString(), itemTotal.getTotalTips().toString());
        Assert.assertEquals("Annual/W2PreviewData Company TotalWagesAndTips : ", totalWages.toString(), itemTotal.getTotalWagesAndTips().toString());

        //Check for the merge of the duplicates  in the EmployeeTaxItemTotals
        List<PayrollFormInfo.EmployeeInfo> employeeInfo = submitFilingRequest.getPayrollFormInfo().get(0).getEmployeeInfo();

        taxItemTotals.clear();
        for (PayrollFormInfo.EmployeeInfo info : employeeInfo) {
            if (info.getEmployeeID().equalsIgnoreCase(pEmployee.getSourceEmployeeId())) {
                taxItemTotals = info.getTaxItemTotals();
            }
        }
        ew2TWhere = EmployeeW2Totals.Company().equalTo(pCompany)
                .And(EmployeeW2Totals.Employee().equalTo(pEmployee))
                .And(EmployeeW2Totals.Year().equalTo(pYear))
                .And(EmployeeW2Totals.Law().LawId().equalTo("131"));
        DomainEntitySet<EmployeeW2Totals> employeeTaxItemTotals = Application.find(EmployeeW2Totals.class, ew2TWhere);
        amount = SpcfMoney.ZERO;
        taxableWages = SpcfMoney.ZERO;
        tipsTaxableWagesAmount = SpcfMoney.ZERO;
        totalWages = SpcfMoney.ZERO;
        for (EmployeeW2Totals w2Totals : employeeTaxItemTotals) {
            amount = (SpcfMoney) amount.add(w2Totals.getAmount());
            taxableWages = (SpcfMoney) taxableWages.add(w2Totals.getTaxableWages());
            tipsTaxableWagesAmount = (SpcfMoney) tipsTaxableWagesAmount.add(w2Totals.getTipsTaxableWagesAmount());
            totalWages = (SpcfMoney) totalWages.add(w2Totals.getTotalWages());
        }
        count = 0;
        itemTotal = new TaxItemTotals();
        for (TaxItemTotals itemTotals : taxItemTotals) {
            String lawId = itemTotals.getTaxTableID().toString();
            if (lawId.equalsIgnoreCase("131")) {
                count++;
                itemTotal = itemTotals;
            }
        }
        Assert.assertEquals(1, count);
        Assert.assertEquals("Annual/W2PreviewData Employee TaxAmount : ", amount.toString(), itemTotal.getTaxAmount().toString());
        Assert.assertEquals("Annual/W2PreviewData Employee TaxableWagesAndTips : ", taxableWages.toString(), itemTotal.getTaxableWagesAndTips().toString());
        Assert.assertEquals("Annual/W2PreviewData Employee TotalTips : ", tipsTaxableWagesAmount.toString(), itemTotal.getTotalTips().toString());
        Assert.assertEquals("Annual/W2PreviewData Employee TotalWagesAndTips : ", totalWages.toString(), itemTotal.getTotalWagesAndTips().toString());

        //Monthly job for TFS for law_id 97
        SendMonthlyDataToTFS.setYear(2012);
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2012, 1, 1);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2012, 1, 31);

        HashMap<SpcfUniqueId, List<Object[]>> companyMonthlyTotals = SendMonthlyDataToTFS.getEETotalsTaxAmounts(SendMonthlyDataToTFS.LAW_97, beginDate, endDate);
        final List<Object[]> companyTotals = companyMonthlyTotals.get(pCompany.getId());
        SubmitFilingRequest sfr = SendMonthlyDataToTFS.createSubmitFilingRequest(pCompany, companyTotals, beginDate, endDate);

        //Check for the merge of the duplicates in the CompanyTaxItemTotals
        ew2TWhere = EmployeeW2Totals.Company().equalTo(pCompany)
                .And(EmployeeW2Totals.Year().equalTo(pYear))
                .And(EmployeeW2Totals.Law().LawId().equalTo("97"));
        companyTaxItemTotals = Application.find(EmployeeW2Totals.class, ew2TWhere);
        Assert.assertEquals("Count of records for CompanyTaxItemTotal is not matching ", 10, companyTaxItemTotals.size());

        amount = SpcfMoney.ZERO;                              //TaxAmount
        taxableWages = SpcfMoney.ZERO;                        //TaxableWagesAndTips
        tipsTaxableWagesAmount = SpcfMoney.ZERO;              //TotalTips
        totalWages = SpcfMoney.ZERO;                          //TotalWagesAndTips
        for (EmployeeW2Totals itotal : companyTaxItemTotals) {
            amount = (SpcfMoney) amount.add(itotal.getAmount());
            taxableWages = (SpcfMoney) taxableWages.add(itotal.getTaxableWages());
            tipsTaxableWagesAmount = (SpcfMoney) tipsTaxableWagesAmount.add(itotal.getTipsTaxableWagesAmount());
            totalWages = (SpcfMoney) totalWages.add(itotal.getTotalWages());
        }

        count = 0;
        taxItemTotals = sfr.getPayrollFormInfo().get(0).getCompanyInfo().getTaxItemTotals();
        itemTotal = new TaxItemTotals();
        for (TaxItemTotals itemTotals : taxItemTotals) {
            String lawId = itemTotals.getTaxTableID().toString();
            if (lawId.equalsIgnoreCase("97")) {
                count++;
                itemTotal = itemTotals;
            }
        }
        Assert.assertEquals(1, count);
        Assert.assertEquals("MonthlyDataToTFS Company TaxAmount : ", amount.toString(), itemTotal.getTaxAmount().toString());
        Assert.assertEquals("MonthlyDataToTFS Company TaxableWagesAndTips : ", taxableWages.toString(), itemTotal.getTaxableWagesAndTips().toString());
        Assert.assertEquals("MonthlyDataToTFS Company TotalTips : ", tipsTaxableWagesAmount.toString(), itemTotal.getTotalTips().toString());
        Assert.assertEquals("MonthlyDataToTFS Company TotalWagesAndTips : ", totalWages.toString(), itemTotal.getTotalWagesAndTips().toString());

        //Check for the duplicates merge in the EmployeeTaxItemTotals
        employeeInfo = sfr.getPayrollFormInfo().get(0).getEmployeeInfo();

        taxItemTotals.clear();
        for (PayrollFormInfo.EmployeeInfo info : employeeInfo) {
            if (info.getEmployeeID().equalsIgnoreCase(pEmployee.getSourceEmployeeId())) {
                taxItemTotals = info.getTaxItemTotals();
            }
        }
        ew2TWhere = EmployeeW2Totals.Company().equalTo(pCompany)
                .And(EmployeeW2Totals.Employee().equalTo(pEmployee))
                .And(EmployeeW2Totals.Year().equalTo(pYear))
                .And(EmployeeW2Totals.Law().LawId().equalTo("97"));
        employeeTaxItemTotals = Application.find(EmployeeW2Totals.class, ew2TWhere);
        Assert.assertEquals("Count of records for EmployeeTaxItemTotal is not matching ", 2, employeeTaxItemTotals.size());
        amount = SpcfMoney.ZERO;
        taxableWages = SpcfMoney.ZERO;
        tipsTaxableWagesAmount = SpcfMoney.ZERO;
        totalWages = SpcfMoney.ZERO;
        for (EmployeeW2Totals w2Totals : employeeTaxItemTotals) {
            amount = (SpcfMoney) amount.add(w2Totals.getAmount());
            taxableWages = (SpcfMoney) taxableWages.add(w2Totals.getTaxableWages());
            tipsTaxableWagesAmount = (SpcfMoney) tipsTaxableWagesAmount.add(w2Totals.getTipsTaxableWagesAmount());
            totalWages = (SpcfMoney) totalWages.add(w2Totals.getTotalWages());
        }
        count = 0;
        itemTotal = new TaxItemTotals();
        for (TaxItemTotals itemTotals : taxItemTotals) {
            String lawId = itemTotals.getTaxTableID().toString();
            if (lawId.equalsIgnoreCase("97")) {
                count++;
                itemTotal = itemTotals;
            }
        }
        Assert.assertEquals(1, count);

        Assert.assertEquals("MonthlyDataToTFS Employee TaxAmount : ", amount.toString(), itemTotal.getTaxAmount().toString());
        Assert.assertEquals("MonthlyDataToTFS Employee TaxableWagesAndTips : ", taxableWages.toString(), itemTotal.getTaxableWagesAndTips().toString());
        Assert.assertEquals("MonthlyDataToTFS Employee TotalTips : ", tipsTaxableWagesAmount.toString(), itemTotal.getTotalTips().toString());
        Assert.assertEquals("MonthlyDataToTFS Employee TotalWagesAndTips : ", totalWages.toString(), itemTotal.getTotalWagesAndTips().toString());

        PayrollServices.rollbackUnitOfWork();

    }

    private void createAnnualDataWithduplicatePitemsAnd177Law() {
        createAnnualData();

        /**
         * Creating duplicate companylaws/pitems
         */

        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);
        PayrollServices.beginUnitOfWork();

        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "131");
        CompanyLawDTO duplicatePitem = PayrollServices.dtoFactory.create(companyLaw);
        duplicatePitem.setSourceId("99");


        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), duplicatePitem);
        assertSuccess("companyLawRateUpdated", processResult);

        CompanyLaw companyLaw97 = CompanyLaw.findCompanyLaw(company, "97");
        CompanyLawDTO duplicatePitem97 = PayrollServices.dtoFactory.create(companyLaw97);
        duplicatePitem97.setSourceId("777");


        ProcessResult processResultLaw97 = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), duplicatePitem97);
        assertSuccess("companyLawRateUpdated", processResultLaw97);
        PayrollServices.commitUnitOfWork();

        // Create Law 177 with 4 source ids
        DataLoadServices.addCompanyLaws_177(company, "1771", "1772", "1773", "1774");

        //submit a payroll after duplication of pitems
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());
        String[] stateLawIds = new String[]{ "116", "130", "131", "97"};
        paycheckDate = new DateDTO("2012-01-29");
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawAmounts = initializeLawAmounts(2);
        lawAmounts.put("131", String.valueOf(14.2 * 2));
        lawAmounts.put("97", String.valueOf(9.7 * 2));
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        int k = 1;
        int i = 1;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            HashMap<String, String> law177Amounts = CalculateEmployeeTotalsTestsHelper.initializeLaw177Amounts(i);
            for (String sourceLawId : law177Amounts.keySet()) {
                BigDecimal amount = new BigDecimal(law177Amounts.get(sourceLawId));
                LiabilityTransactionDTO liabilityTransactionDTO = new LiabilityTransactionDTO();
                liabilityTransactionDTO.setLiabilityTaxableWages(amount.multiply(new BigDecimal("10")));
                liabilityTransactionDTO.setLiabilityTotalWages(amount.multiply(new BigDecimal("10")));
                liabilityTransactionDTO.setLiabilityTipsTaxableWages(amount.multiply(new BigDecimal("10")));
                liabilityTransactionDTO.setLiabilityAmount(new BigDecimal(law177Amounts.get(sourceLawId)));
                liabilityTransactionDTO.setLawId("177");
                liabilityTransactionDTO.setPayrollItemId(sourceLawId);
                paycheckDTO.getLiabilityTransactions().add(liabilityTransactionDTO);
            }

            String hourlyVac = "1";
            String hourlyReg = "1";
            for (String stateLawId : stateLawIds) {
                CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                employerContributionTransactionDTO.setContributionAmount(new BigDecimal(i));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                deductionTransactionDTO.setDeductionAmount(new BigDecimal(i));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));

                compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                if ((stateLawId.equals("131")) && (k % 2 == 0)) {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                } else {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                }
                QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                compensationTransactionDTO.setPayStubOrder((long) k);

                paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);


                employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
                CompanyPayrollItem cpi = findItemForSourcePayrollItemId(company, "TTT1", PayrollItemType.EmployerContribution);
                employerContributionTransactionDTO.setSourcePayrollItemId(cpi.getSourcePayrollItemId());
                employerContributionTransactionDTO.setContributionAmount(W2_CODES.get("TTT1"));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                cpi = findItemForSourcePayrollItemId(company, "TTT10", PayrollItemType.Deduction);
                deductionTransactionDTO = new DeductionTransactionDTO();
                deductionTransactionDTO.setDeductionAmount(W2_CODES.get("TTT10"));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));
                deductionTransactionDTO.setSourcePayrollItemId(cpi.getSourcePayrollItemId());

                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
            }
            k++;
        }

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_" + k, new DateDTO("2011-08-14"));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        for (Employee employee : employees) {

            QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
            qbdtPayrollTransactionDTO.setAmount(new SpcfMoney("27.27"));
            qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
            qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 2));
            QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
            qTlDTO.setAmount(new SpcfMoney("27.27"));
            String companyPayrollItemSourceId = "1";
            qTlDTO.setPayrollItemId(String.valueOf(companyPayrollItemSourceId));
            qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
            PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
            for (String stateLawId : stateLawIds) {
                SpcfMoney amount = new SpcfMoney(stateLawId);
                liabilityAdjustmentDTOs.add(DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, (SpcfMoney) amount.multiply(new SpcfMoney("2")), (SpcfMoney) amount.divide(new SpcfMoney("2")), false));

            }
        }

        payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().add(companyAdjustmentSubmissionDTO);
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(submitPayrollResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);


        //Run Annuals total calculations job
        new CalculateEmployeeAnnualTotals().main(new String[]{"-year:2012"});
    }

    @Test
    public void testDuplicateLawAnd177Law() {
        createAnnualDataWithduplicatePitemsAnd177Law();
        PayrollServices.beginUnitOfWork();

        //Check for multiple records created for law_id 142
        Company pCompany = Company.findCompany("158905", SourceSystemCode.QBDT);
        Employee pEmployee = pCompany.getEmployees().getFirst();
        Employee pEmployee1 = pCompany.getEmployees().get(1);
        int pQuarter = 1;
        int pYear = 2012;
        Criterion<EmployeeLawQtrTotals> where ;
        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotals ;
        DomainEntitySet<EmployeeW2Totals> employeeW2Totalses ;
        //check for law id 177

        where = EmployeeLawQtrTotals.Company().equalTo(pCompany)
                .And(EmployeeLawQtrTotals.Employee().equalTo(pEmployee1)
                        .And(EmployeeLawQtrTotals.Quarter().equalTo(pQuarter))
                        .And(EmployeeLawQtrTotals.Year().equalTo(pYear))
                        .And(EmployeeLawQtrTotals.Law().LawId().equalTo("177")));
        employeeLawQtrTotals = Application.find(EmployeeLawQtrTotals.class, where);
        Assert.assertEquals(employeeLawQtrTotals.size(), 4);
        Criterion<EmployeeW2Totals> ew2TWhere;
        ew2TWhere = EmployeeW2Totals.Company().equalTo(pCompany)
                .And(EmployeeW2Totals.Employee().equalTo(pEmployee1)
                        .And(EmployeeW2Totals.Year().equalTo(pYear))
                        .And(EmployeeW2Totals.Law().LawId().equalTo("177")));
        employeeW2Totalses = Application.find(EmployeeW2Totals.class, ew2TWhere);
        Assert.assertEquals(employeeW2Totalses.size(), 4);

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testIABENNo() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"OR", "IA","VT"};

        double i = 1.0;
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.clear();
        lawAmounts = initializeLawAmountsForIA(i);
        List<Company> companies1 = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.Withholding);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Agency> agencies = Application.find(Agency.class, Agency.AgencyId().equalTo("IADOR"));
        assertEquals("Too many agencies found", 1, agencies.size());

        Agency iaAgency = agencies.get(0);

        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                "IA-44105-PAYMENT", DepositFrequencyCode.MONTHLY);

        DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, CompanyAgency.Agency().equalTo(iaAgency));

        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = null;
        for (CompanyAgency companyAgency : companyAgencies) {
            DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = Application.find(CompanyAgencyPaymentTemplate.class,
                    CompanyAgencyPaymentTemplate.CompanyAgency().equalTo(companyAgency)
                            .And(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplateFrequency.getPaymentTemplate())));

            assertEquals("Too many CompanyAgencyPaymentTemplates found", 1, companyAgencyPaymentTemplates.size());

            companyAgencyPaymentTemplate = companyAgencyPaymentTemplates.get(0);

            CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId = new CompanyPaymentTemplateAgencyId();
            companyPaymentTemplateAgencyId.setName("BEN Number");
            companyPaymentTemplateAgencyId.setAgencyTaxpayerId("12345678");
            companyPaymentTemplateAgencyId.setCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);

            Application.save(companyPaymentTemplateAgencyId);
        }
        PayrollServices.commitUnitOfWork();


        Company company = Company.findCompany("158905", SourceSystemCode.QBDT);
        DataLoadServices.addEEs(company, 1);

        DataLoadServices.runPayrollRun(company,statesList,PSPDate.getPSPTime(),new DateDTO("2012-01-01"),false,lawAmounts,PaymentTemplateCategory.Withholding);

        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[]{"-year:2012"});
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        CompanyTFSSubmission companyTFSSubmission = new CompanyTFSSubmission();
        SubmitFilingRequest submitFilingRequest = new SendW2DataToTFS(new String[]{}, FilingTypeType.UnmodifiableAnnualPreviewData).createSubmitFilingRequest(company, 2012, companyTFSSubmission.getId().toString());

        //Check for the merge of the duplicates in the CompanyTaxItemTotals
        List<PayrollFormInfo.CompanyInfo.TaxItemInfo> taxItemInfo = submitFilingRequest.getPayrollFormInfo().get(0).getCompanyInfo().getTaxItemInfo();

        ListIterator<PayrollFormInfo.CompanyInfo.TaxItemInfo> taxItemIterator = taxItemInfo.listIterator();
        assertTrue(taxItemInfo.size()>0);

        while(taxItemInfo.size()>0 && taxItemIterator.hasNext()) {
            PayrollFormInfo.CompanyInfo.TaxItemInfo tItemInfo=taxItemIterator.next();
            if (tItemInfo.getState()!=null && tItemInfo.getState().equals(State50Type.IA)) {
                String expectedValue = Application.find(CompanyPaymentTemplateAgencyId.class,
                        CompanyPaymentTemplateAgencyId.Name().equalTo("BEN Number")
                                .And(CompanyPaymentTemplateAgencyId.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(company))).get(0).getAgencyTaxpayerId();
                assertEquals("Expected: "+expectedValue+", Actual: "+tItemInfo.getAdditionalAccountNumberWithAgency(),expectedValue, tItemInfo.getAdditionalAccountNumberWithAgency());
            }
        }
        PayrollServices.commitUnitOfWork();

    }


    private HashMap<String, String> initializeLawAmountsForIA(double pMultiplier) {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", String.valueOf(6.1 * pMultiplier));
        lawAmounts.put("62", String.valueOf(6.2 * pMultiplier));
        lawAmounts.put("63", String.valueOf(6.3 * pMultiplier));
        lawAmounts.put("64", String.valueOf(6.4 * pMultiplier));
        lawAmounts.put("65", String.valueOf(6.5 * pMultiplier));
        lawAmounts.put("1", String.valueOf(1.5 * pMultiplier));
        lawAmounts.put("130", String.valueOf(13.0 * pMultiplier)); // VT SUI-ER
        lawAmounts.put("120", String.valueOf(12 * pMultiplier));   // OR SUI-ER
        lawAmounts.put("14", String.valueOf(11.5 * pMultiplier));
        return lawAmounts;
    }

    @Test
    public void testDGDeletedCompany_ArgumentsPassed() {
        createAnnualData();


        //Passing DGDeleted company from the SystemParameter
        PayrollServices.beginUnitOfWork();

        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue("158906,111111111");
        Application.save(companyListParameter);

        Company company = Company.findCompany("158906", SourceSystemCode.QBDT);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        PayrollServices.commitUnitOfWork();


        SendW2DataToTFS sendW2DataToTFS = new SendW2DataToTFS(new String[]{}, FilingTypeType.UnmodifiableAnnualPreviewData);
        ProcessResult processResult = sendW2DataToTFS.process();

        CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
        assertSuccess(processResult);
        assertNotNull(companyTFSSubmission);
        assertEquals("CompanyTFSSubmission status is not matching", TFSSubmissionStatus.Pending, companyTFSSubmission.getSubmissionStatus());


        //Passing a DGDeletedCompany as an argument

        PayrollServices.beginUnitOfWork();

        companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue(null);
        Application.save(companyListParameter);
        PayrollServices.commitUnitOfWork();


        sendW2DataToTFS = new SendW2DataToTFS(new String[]{"-companyId:158906"}, FilingTypeType.UnmodifiableAnnualPreviewData);
        processResult = sendW2DataToTFS.process();

        companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
        assertSuccess(processResult);
        assertNotNull(companyTFSSubmission);
        assertEquals("CompanyTFSSubmission status is not matching", TFSSubmissionStatus.Pending, companyTFSSubmission.getSubmissionStatus());
    }

    @Test
    public void testDGDeletedCompany_NoArgumentsPassed() {
        createAnnualData();

        //Passing DGDeleted company from the SystemParameter
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("158906", SourceSystemCode.QBDT);
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);
        PayrollServices.commitUnitOfWork();


        SendW2DataToTFS sendW2DataToTFS = new SendW2DataToTFS(new String[]{"-year:2012"}, FilingTypeType.UnmodifiableAnnualPreviewData);
        ProcessResult processResult = sendW2DataToTFS.process();

        CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, 2012);
        assertSuccess(processResult);
        assertNotNull(companyTFSSubmission);
        assertEquals("CompanyTFSSubmission status is not matching", TFSSubmissionStatus.Pending, companyTFSSubmission.getSubmissionStatus());

    }

}
