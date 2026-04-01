package com.intuit.sbd.payroll.psp.batchjobs.fset;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.eftps.EftpsDataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.FsetFilingProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.FsetResponseProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpFsetConnection;
import com.intuit.sbd.payroll.psp.common.utils.FileUtils;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PaymentStatus;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * User: sshetty
 * Date: 10/29/13
 * Time: 10:36 AM
 */
public class FsetFilingTests {

    private static final String TEST_FILE_PATH = "PSE/batch-jobs-tests/target/test-classes/fset/mockserver/";
    public static final String TRANSMISSION_ID = "8467320111110000003436";
    private static String DIR_ROOT = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_fset_recv_dir");

    @BeforeClass
    public static void beforeClass() {
        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        EftpsDataLoader.deleteAllFsetTestDirFiles();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        DataLoadServices.reinitialize();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    public Company setupCompanyForMS(String pPsid) {

        SpcfCalendar supportStartDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(supportStartDate);
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPsid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "65", "143", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        //Update Agency Id
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", true);

        //Enable EDI payment method
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "MS-M89-PAYMENT", PaymentMethod.ACHCredit, true));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(pPsid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(pPsid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        return company;
    }

    public void createPayrollWithMSStatePayments(Company pCompany, SpcfCalendar pPayrollRunDate, DateDTO pPaycheckDate) {

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pPayrollRunDate);
        PayrollServices.commitUnitOfWork();

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("143", "14.3");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        DomainEntitySet<Employee> employees = Employee.findEmployees(pCompany);
        List<Employee> emps = new ArrayList<Employee>(employees);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(pCompany, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(payrollRunDTO, pCompany, pPaycheckDate, emps, lawAmounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(pCompany, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();
    }

    //TODO Ignoring test for build migration
    @Ignore
    @Test
    public void testFsetFileParseError() throws Exception {
        Company company = setupCompanyForMS("1234567");
        createPayrollWithMSStatePayments(company, SpcfCalendar.createInstance(2011, 10, 13, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-10-14"));

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        //Updating the legal name, addressLine1 and addressLine2  with invalid pattern.
        company.setLegalName("  Testing for {}. invalid's #company      Legal   name * , Schema & (has)  weired prn validation.  ");
        company.getLegalAddress().setAddressLine1(" -/-Invalid's -  #St.,   /     Too \\ big     st Name,\\ Invalid  dr - A line 1 ");
        company.getLegalAddress().setAddressLine2(" /-/-Invalid's - second.     St, / Too   big   street  second Name, - A line 2 ");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("MS-M89-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("MS State WH payments", 1, moneyMovementTransactions.size());
        assertEquals("MS state WH payment method", PaymentMethod.ACHCredit, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        //Disable EDI payment method
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", false);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("MS State WH payments", 1, moneyMovementTransactions.size());
        assertEquals("MS state WH payment method", PaymentMethod.CheckPayment, assertOne(moneyMovementTransactions).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        //Enable EDI payment method
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", true);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(moneyMovementTransactions);
        assertEquals("MS state WH payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 11, 10);

        DataLoadServices.offloadAgencyTaxCredits(paymentTemplate);
        FsetFilingProcessor fsetFilingProcessor = new FsetFilingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.FsetFilingProcessor, "Test-Id", null);
        fsetFilingProcessor.setInitiationDate(SpcfCalendar.createInstance(2011, 11, 10, SpcfTimeZone.getLocalTimeZone()));
        fsetFilingProcessor.executeJobStep("GenerateFsetReturnFileStep");

        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
        Application.beginUnitOfWork();

        FsetFile fsetFile = assertOne(Application.find(FsetFile.class));
        //Update the transmission id to the one int response file
        fsetFile.setTransmissionId(TRANSMISSION_ID);
        fsetFile.setFileName(fsetFile.getFileName().substring(fsetFile.getFileName().lastIndexOf(File.separator) + 1));
        Application.save(fsetFile);
        Application.commitUnitOfWork();
        fsetFilingProcessor.executeJobStep("TransmitPendingTransmissionFilesStep");


        FsetResponseProcessor fsetResponseProcessor = new FsetResponseProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.FsetResponseProcessor, "Test-Id", null);

        copyFile("testParseError.xml", "testParseError.xml");

        //fsetResponseProcessor.executeJobStep("CheckForResponseFilesStep");

        Transporter sftp = BatchUtils.getFsetSftpConnection(new SftpFsetConnection().getDownloadListener());
        sftp.downloadFile("testParseError.xml");

        Application.beginUnitOfWork();
        FsetFile fsetAckFile = assertOne(Application.find(FsetFile.class, FsetFile.FileType().equalTo(FsetFileType.FsetAck)));

        fsetAckFile.setTransmissionId(fsetFile.getTransmissionId());
        Application.save(fsetAckFile);
        Application.commitUnitOfWork();
        fsetResponseProcessor.executeJobStep("ProcessFsetResponseFileStep");


    }

    @Test
    public void testFsetFile_CityNameLegalCharacters() throws Exception {
        Company company = setupCompanyForMS("1234567");

        createPayrollWithMSStatePayments(company, SpcfCalendar.createInstance(2022, 6, 13, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2022-6-16"));

        PayrollServices.beginUnitOfWork();

        company = Application.findById(Company.class, company.getId());

        company.getLegalAddress().setCity(" 'D%  elhi  ");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("MS-M89-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("MS State WH payments", 1, moneyMovementTransactions.size());
        assertEquals("MS state WH payment method", PaymentMethod.ACHCredit, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        //Enable EDI payment method
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", true);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(moneyMovementTransactions);
        assertEquals("MS state WH payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2022, 07, 14);

        DataLoadServices.offloadAgencyTaxCredits(paymentTemplate);
        FsetFilingProcessor fsetFilingProcessor = new FsetFilingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.FsetFilingProcessor, "Test-Id", null);
        fsetFilingProcessor.setInitiationDate(SpcfCalendar.createInstance(2022, 07, 14, SpcfTimeZone.getLocalTimeZone()));
        fsetFilingProcessor.executeJobStep("GenerateFsetReturnFileStep");

        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
        Application.beginUnitOfWork();

        FsetFile fsetFile = assertOne(Application.find(FsetFile.class));
        //Update the transmission id to the one int response file
        fsetFile.setTransmissionId(TRANSMISSION_ID);
        fsetFile.setFileName(fsetFile.getFileName().substring(fsetFile.getFileName().lastIndexOf(File.separator) + 1));
        FsetFilingDetail fsetFilingDetail = assertOne(Application.find(FsetFilingDetail.class));
        assertEquals(fsetFilingDetail.getCity(),"D elhi");
        assertNotNull(fsetFilingDetail.getCompany());
        assertEquals(fsetFilingDetail.getMoneyMovementTransaction().getCompany(),fsetFilingDetail.getCompany());
        Application.save(fsetFile);
        Application.commitUnitOfWork();
    }

    private static void copyFile(String fromFile, String toFile) {
        try {
            FileInputStream is = new FileInputStream(TEST_FILE_PATH + fromFile);
            File newFile = new File(DIR_ROOT + "/" + toFile);
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileOutputStream os = new FileOutputStream(DIR_ROOT + "/" + toFile);
            FileUtils.copyInputStream(is, os);

        } catch (FileNotFoundException fnf) {
            fail("File not found - " + fromFile);
        } catch (Throwable ex) {
            fail("Error during file copy.");
        }
    }
}
