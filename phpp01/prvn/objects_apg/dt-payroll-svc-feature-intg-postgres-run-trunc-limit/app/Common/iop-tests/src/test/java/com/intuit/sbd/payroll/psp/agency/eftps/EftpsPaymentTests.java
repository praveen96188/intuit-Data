package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsPaymentProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.ops.eftpsBp.EdiEftpsRecordList;
import com.paycycle.util.PgpUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Jan 10, 2011
 * Time: 10:16:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsPaymentTests {
    private static final String FIT = "1";
    private static final String FICA = "61";


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        EftpsDataLoader.deleteAllTestDirFiles();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005,1,1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005,1,1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testConfigParameterSet() {
        File file = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("Work Directory \" %s \"  does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getArchiveDir());
        assertTrue(String.format("Archive Directory \" %s \"  does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getAS400Dir());
        assertTrue(String.format("AS400 Directory \" %s \" does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getErrDir());
        assertTrue(String.format("Error Directory \" %s \"  does not exists.", file.getName()), file.exists());

        file = new File(EftpsUtil.getTfaDir());
        assertTrue(String.format("TFA Directory \" %s \"  does not exists.", file.getName()), file.exists());

    }

    @Test
    public void testNoPaymentsExistNoFileCreated() {

        File fileSendDir = new File(EftpsUtil.getWorkDir());
        assertTrue(String.format("%s Directory doesn't exists", EftpsUtil.getWorkDir()), fileSendDir.exists());

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("EftpsFile record exists.", 0, eftpsFiles.size());
        assertEquals("Eftps payment file exists.", 0, fileSendDir.list().length);
        PayrollServices.commitUnitOfWork();
    }

    public void testCreatePaymentData() {
        testCreatePaymentData("123456789");
    }

    public void testCreatePaymentData(String psid) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addCompanyLaws(company, "63");
        DataLoadServices.addCompanyLaws(company, "200");
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "200"}, new String[]{"5000", "5000", "250"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);

        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2011-02-18"), emps, new String[]{"61", "62", "200"}, new String[]{"5000", "5000", "25"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                //.And(MoneyMovementTransaction.InitiationDate().equalTo(PSPDate.getPSPTime())
                //.And(MoneyMovementTransaction.EFTPSPaymentStatus().equalTo(EftpsPaymentStatus.ReadyToSend))
                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created));

        DomainEntitySet<MoneyMovementTransaction> pendingPayments = Application.find(MoneyMovementTransaction.class, mmtCriteria);

        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        initiationDate.setValues(initiationDate.getYear(),
                                 initiationDate.getMonth(),
                                 initiationDate.getDay(),
                                 0,0,0,0);

        for (MoneyMovementTransaction pendingPayment : pendingPayments) {
            pendingPayment.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
            pendingPayment.setInitiationDate(initiationDate);
        }
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testPaymentDataTest100k() {

        EftpsDataLoader.create100KPayrolls();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);
    }

    @Test
    public void testNextDayProcessPayments() {
        testCreatePaymentData();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);

        assertEquals("EftpsFile record not found.", 1, eftpsFiles.size());
        assertEquals("EftpsFile record not in PendingTransmission state.",
                EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());
    }

    @Test
    public void testSameDayProcessPayments() {
        testCreatePaymentData();

        String bepsSettlementDate = PSPDate.getPSPTime().format(BatchUtils.DATE_FORMAT);
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.MarkPaymentsAsProcessing.class,
                                   "PFM_SAME_DAY", "EFTPS",
                                   "BepsRefNum=E00001",
                                   "BepsSettlementDate=" + bepsSettlementDate);
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.GeneratePaymentFile.class,
                                   "PFM_SAME_DAY", "EFTPS",
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + bepsSettlementDate);

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.InProcess, eftpsFiles.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        StringBuilder sb = new StringBuilder();
        for (EftpsFile eftpsFile : eftpsFiles) {
            sb.append(eftpsFile.getFileId()).append(",");
        }
        sb.setLength(sb.length() - 1);
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.MarkPaymentsAsSent.class,
                                   "PFM_SAME_DAY",
                                   "EFTPS",
                                   "FileIds=" + sb.toString(),
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + bepsSettlementDate);


        eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("EftpsFile record not found.", 1, eftpsFiles.size());
        assertEquals("EftpsFile record not in PendingTransmission state.",
                EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());
    }

    @Test
    public void testOnHoldPaymentsExistsNoFileCreated() {
        testCreatePaymentData();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<Company> company = Application.find(Company.class, Company.SourceCompanyId().equalTo("123456789"));
        DomainEntitySet<MoneyMovementTransaction> pendingMMTs = company.get(0).findPendingTaxPayments();

        for (MoneyMovementTransaction mmt : pendingMMTs) {
            PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmt, PaymentOnHoldReason.Agent);
            Application.save(mmt);
        }

        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);

        assertEquals("EftpsFile record exists.", 0, eftpsFiles.size());
        assertEquals("Eftps payment file exists.", 0, new File(EftpsUtil.getWorkDir()).list().length);
    }

    @Test
    public void testNextDayProcessPayments_HappyPath() throws S3ConnectionException,S3UploadException {

        testCreatePaymentData();

        //call next day payments.
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());
        DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Detail ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        DomainEntitySet<FinancialTransaction> financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(payDetails.get(0).getMoneyMovementTransaction().getCompany(), EventTypeCode.TaxPaymentStatusChanged);
        assertEquals("TaxPaymentStatusChanged event cnt", 1, events.size());
        assertEquals("event details", 2, events.get(0).getCompanyEventDetailCollection().size());

        PayrollServices.rollbackUnitOfWork();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        EftpsDataLoader.callSimulator();

        //Archive file. Only payment file(813) will be in completed status.
        EdiManager.archiveFiles();

        // process awaiting response file. i.e processing the files in TFA directory. this step will process responses/acknowledgements from TFA and archive
        // anything it completes
        // If the response file owner is AS400, it will log the info in PSP and send it to AS400. The file status will be INPROCESS state until it uploads to TFA.
        BatchJobManager.runJob(BatchJobType.EftpsResponse);

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals("payment files.", 1, eftpsFiles.size());

        //Check archived files.
        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Archived))
                .OrderBy(EftpsFile.CreatedDate().Descending());
        eftpsFiles = Application.find(EftpsFile.class, query);

        assertEquals("payment files.", 3, eftpsFiles.size());

        payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Details ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.AcknowledgedByAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Completed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        Company c = payDetails.get(0).getMoneyMovementTransaction().getCompany();
        assertEquals(c,payDetails.get(0).getCompany());
        events = CompanyEvent.findCompanyEvents(c, EventTypeCode.TaxPaymentStatusChanged);
        assertEquals("TaxPaymentStatusChangeEvents", 2, events.size());
        for (CompanyEvent event : events) {
            assertEquals("event details", 2, event.getCompanyEventDetailCollection().size());
        }


        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_100k_Payments_HappyPath() throws S3ConnectionException,S3UploadException{

        EftpsDataLoader.create100KPayrolls();

        //This is compulsory as to make current date as initiated date. TODO: DON'T REMOVE THIS.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //call 100k day payments.
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());
        DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Detail ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        EftpsDataLoader.callSimulator();

        //Archive file. Only payment file(813) will be in completed status.
        EdiManager.archiveFiles();

        //process awaiting response file. i.e processing the files in TFA directory. this step will process responses/acknowledgements from TFA.
        // If the response file owner is AS400, it will log the info in PSP and send it to AS400. The file status will be INPROCESS state until it uploads to TFA.
        EdiManager.processWaitingResponseFiles();

        //ARCHIVE COMPLETED RECORDS.
        EdiManager.archiveFiles();

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals("No payment files.", 1, eftpsFiles.size());

        //Check archived files.
        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Archived))
                .OrderBy(EftpsFile.CreatedDate().Descending());
        eftpsFiles = Application.find(EftpsFile.class, query);

        assertEquals("No payment files.", 3, eftpsFiles.size());

        payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Details ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.AcknowledgedByAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.commitUnitOfWork();
    }

    //@Test
    // Dead code not in use any more removing the integration test as well
    public void testPaymentFileFromAS400() throws S3ConnectionException,S3UploadException{

        File buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment110113499_AS400.813");
        EftpsUtil.copyFile(buildPath.getPath(), EftpsUtil.getAS400Dir());
        EdiManager.processAS400Files();
        EdiManager.archiveFiles();

        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No payment files.", 1, eftpsFiles.size());
        assertEquals("Invalid status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        //update as completed.
        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        EftpsDataLoader.callSimulator();

        EdiManager.processWaitingResponseFiles();
        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Archived))
                .OrderBy(EftpsFile.CreatedDate());
        eftpsFiles = Application.find(EftpsFile.class, query);
        assertEquals("No payment files.", 1, eftpsFiles.size());

        query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.SendToAS400))
                .OrderBy(EftpsFile.CreatedDate());
        eftpsFiles = Application.find(EftpsFile.class, query);
        assertEquals("No payment files.", 2, eftpsFiles.size());
        PayrollServices.commitUnitOfWork();
    }

    //@Test
    // Dead code not in use any more removing the integration test as well
    public void testEndToEndFromAS400() throws S3ConnectionException,S3UploadException{

        //Payment file from AS400
        File buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment110113499_AS400.813");
        EftpsUtil.copyFile(buildPath.getPath(), EftpsUtil.getAS400Dir());

        //Process AS400 files.
        EdiManager.processAS400Files();

        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No payment files.", 1, eftpsFiles.size());
        assertEquals("Invalid status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        //Transmit to TFA.
        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        //Archive payment file.
        EdiManager.archiveFiles();

        // Receive & process files from TFA
        buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentAck_TFA_AS400.997");
        EftpsUtil.copyFile(buildPath.getPath(), EftpsUtil.getTfaDir());
        buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EFtpsPaymentResponse_TFA_AS400.151");
        EftpsUtil.copyFile(buildPath.getPath(), EftpsUtil.getTfaDir());
        EdiManager.processWaitingResponseFiles();

        //TRANSMIT AS400 files.
        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.SendToAS400))
                .OrderBy(EftpsFile.CreatedDate());
        eftpsFiles = Application.find(EftpsFile.class, query);
        assertEquals("Incorrect files from TFA with status SENDTOAS400.", 2, eftpsFiles.size());

        for (EftpsFile eftpsFile : eftpsFiles) {
            eftpsFile.setStatusCd(EdiFileStatus.Completed);
        }
        PayrollServices.commitUnitOfWork();
        assertEquals("No payment files.", 2, eftpsFiles.size());
        assertEquals("Invalid status.", EdiFileStatus.Completed, eftpsFiles.get(0).getStatusCd());
        assertEquals("Invalid status.", EdiFileStatus.Completed, eftpsFiles.get(1).getStatusCd());

        //ARCHIVE COMPLETED FILES.
        EdiManager.archiveFiles();

        // Receive payment response acknowledgement from AS400 to be transmitted to TFA.
        buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentResponseAcK_TFA_AS400.997");
        EftpsUtil.copyFile(buildPath.getPath(), EftpsUtil.getAS400Dir());

        EdiManager.processAS400Files();

        assertEquals("Incorrect Response Ack status.", 1, EftpsFile.getPendingTransmissionEftpsFiles().size());

        query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Archived))
                .OrderBy(EftpsFile.CreatedDate());
        eftpsFiles = Application.find(EftpsFile.class, query);
        assertEquals("No payment files.", 3, eftpsFiles.size());
    }

    @Test
    public void test_NextDay_PaymentReject() throws S3ConnectionException,S3UploadException{
        testCreatePaymentData();
        //call next day payments.
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());
        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        int paymentFileId = eftpsFiles.get(0).getFileId();
        assertEquals(1, eftpsFiles.size());

        DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Detail ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        DomainEntitySet<FinancialTransaction> financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        EftpsDataLoader.overridePendingToCompletedStatus();

        EftpsDataLoader.callRejectSimulator();

        //Process simulator generated files.
        EdiManager.processWaitingResponseFiles();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertTrue(!paymentDetails.isEmpty());
        assertEquals("Incorrect status", TaxPaymentStatus.RejectedByAgency, paymentDetails.get(0).getStatusCd());
        MoneyMovementTransaction mmt = paymentDetails.get(0).getMoneyMovementTransaction();
        assertEquals("", PaymentStatus.Executed, paymentDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",paymentDetails.getFirst().getCompany());
        financialTransactions = mmt.getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Returned, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        assertEquals("In correct pending files.", 1, EftpsFile.getPendingTransmissionEftpsFiles().size());
        PayrollServices.rollbackUnitOfWork();

        //Archive.
        EdiManager.archiveFiles();

        // test repayment initiation
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.InitiateRepayment.class,"FileIds=" + paymentFileId, "RejectCodes=1101");

        // repay
        BatchJobManager.runJob(BatchJobType.EftpsPayment);


    }

    @Test
    public void test_100k_PaymentReject() throws S3ConnectionException,S3UploadException{

        EftpsDataLoader.create100KPayrolls();

        //This is compulsory as to make current date as initiated date. TODO: DON'T REMOVE THIS.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //call next day payments.
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());

        DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Detail ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        DomainEntitySet<FinancialTransaction> financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        EftpsDataLoader.overridePendingToCompletedStatus();

        EftpsDataLoader.callRejectSimulator();

        //Process simulator generated files.
        EdiManager.processWaitingResponseFiles();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertTrue(!paymentDetails.isEmpty());
        assertEquals("Incorrect status", TaxPaymentStatus.RejectedByAgency, paymentDetails.get(0).getStatusCd());
        MoneyMovementTransaction mmt = paymentDetails.get(0).getMoneyMovementTransaction();
        assertEquals("", PaymentStatus.Executed, paymentDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",paymentDetails.getFirst().getCompany());
        financialTransactions = mmt.getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Returned, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        assertEquals("In correct pending files.", 1, EftpsFile.getPendingTransmissionEftpsFiles().size());
        PayrollServices.rollbackUnitOfWork();

        //Archive.
        EdiManager.archiveFiles();
    }

    /**
     * For payment return should use 100k payment file only.
     */
    @Test
    public void test_100k_PaymentReturn() throws S3ConnectionException,S3UploadException{

        EftpsDataLoader.create100KPayrolls();

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());

        EftpsDataLoader.overridePendingToCompletedStatus();

        EftpsDataLoader.callReturnSimulator();

        //Process simulator generated files.
        EdiManager.processWaitingResponseFiles();

        //check status.
        PayrollServices.beginUnitOfWork();
        assertEquals("Completed Files.", 4, EftpsFile.getCompletedEftpsFiles().size());
        assertEquals("Pending Transmission state .", 2, EftpsFile.getPendingTransmissionEftpsFiles().size());
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertTrue(!paymentDetails.isEmpty());
        assertEquals("Status", TaxPaymentStatus.ReturnedTaxNotPaid, paymentDetails.get(0).getStatusCd());
        MoneyMovementTransaction mmt = paymentDetails.get(0).getMoneyMovementTransaction();
        assertEquals("", TaxPaymentStatus.ReturnedTaxNotPaid, mmt.getTaxPaymentStatus());
        assertEquals("", PaymentStatus.Executed, paymentDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",paymentDetails.getFirst().getCompany());
        DomainEntitySet<FinancialTransaction> financialTransactions = mmt.getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Returned, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();
        //Archive.
        EdiManager.archiveFiles();

        //Asserting PSP_ATFPAYMENTS_TO_PROCESS table for amounts to test for ATF Payments Extract process.
        PayrollServices.beginUnitOfWork();
        EftpsPaymentDetail eftpsPaymentDetail = assertOne(EftpsPaymentDetail.findAllPaymentDetails());
        assertEquals("MMT Amount", new SpcfMoney("40500"), eftpsPaymentDetail.getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        //3 Agency tax credits and 1 Intuit credit (EmployerTaxDirectDebit)
        assertEquals("Financial transactions", 4, eftpsPaymentDetail.getMoneyMovementTransaction().getFinancialTransactionCollection().size());
        DomainEntitySet<ATFPaymentsToProcess> atfPaymentsToProcesses = Application.find(ATFPaymentsToProcess.class, ATFPaymentsToProcess.MoneyMovementTransaction().equalTo(eftpsPaymentDetail.getMoneyMovementTransaction()));
        //3 Agency tax credits
        assertEquals("Total ATFPaymentsToProcess records", 3, atfPaymentsToProcesses.size());
        //Assert all ATFPaymentsToProcess records amount is zero because MMT status is "ReturnedTaxNotPaid"
        assertEquals("Total Zero amount ATFPaymentsToProcess records", 3, atfPaymentsToProcesses.find(ATFPaymentsToProcess.Amount().equalTo(SpcfMoney.ZERO)).size());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     *
     */
    @Test
    public void test_100k_PaymentReturn_withNOC() throws S3ConnectionException,S3UploadException {

        EftpsDataLoader.create100KPayrolls();

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());

        EftpsDataLoader.overridePendingToCompletedStatus();

        EftpsDataLoader.callReturnSimulatorWithNOC();

        //Process simulator generated files.
        EdiManager.processWaitingResponseFiles();

        //check status.
        PayrollServices.beginUnitOfWork();
        assertEquals("Completed Files.", 4, EftpsFile.getCompletedEftpsFiles().size());
        assertEquals("Pending Transmission state .", 2, EftpsFile.getPendingTransmissionEftpsFiles().size());
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertTrue(!paymentDetails.isEmpty());
        assertEquals("Status", TaxPaymentStatus.ReturnedTaxPaid, paymentDetails.get(0).getStatusCd());
        MoneyMovementTransaction mmt = paymentDetails.get(0).getMoneyMovementTransaction();
        assertEquals("", PaymentStatus.Executed, paymentDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",paymentDetails.getFirst().getCompany());
        DomainEntitySet<FinancialTransaction> financialTransactions = mmt.getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();
        //Archive.
        EdiManager.archiveFiles();

        PayrollServices.beginUnitOfWork();
        mmt = Application.findById(MoneyMovementTransaction.class, mmt.getId());
        for (FinancialTransaction financialTransaction : mmt.getFinancialTransactionCollection()) {
            assertEquals("Financial transaction status", TransactionState.findTransactionState(TransactionStateCode.Executed), financialTransaction.getCurrentTransactionState());
        }
        PayrollServices.rollbackUnitOfWork();

        //Running ACH Transaction processor
        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        Application.refresh(mmt);
        for (FinancialTransaction financialTransaction : mmt.getFinancialTransactionCollection()) {
            assertEquals("Financial transaction status", TransactionState.findTransactionState(TransactionStateCode.Completed), financialTransaction.getCurrentTransactionState());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * For payment return should use 100k payment file only.
     */
    @Test
    public void test100KPaymentFile_813_997_151_Creation() throws Exception {

        DataLoadServices.reinitialize();

        EftpsDataLoader.create100KPayrolls();

        //This is compulsory as to make current date as initiated date. 
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>().Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.PendingTransmission));
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class, query);
        assertEquals("No file 813 generated.", 1, eftpsFiles.size());
        PayrollServices.commitUnitOfWork();

        File buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentFile_100K_PSP.813");
        EdiEftpsRecordList lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
        File file = new File(eftpsFiles.get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList rhsFile = new EdiEftpsRecordList(file);        assertTrue(lhsFile.equals(rhsFile));

        EdiManager.archiveFiles(); // archive 813  file.

        // over ride to completed status as there is no FTP.
        EftpsDataLoader.overridePendingToCompletedStatus();

        //Call EftpsSimulater.
        EftpsDataLoader.callSimulator();

        List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir());

        for (File mFile : fileList) {
            mFile = PgpUtils.getUnencryptedFile(mFile);
            switch (EftpsDataLoader.readEdiFile(mFile).getEftpsFileType()) {
                case EftpsPaymentAck: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment_100k_ack_PSP.997");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(mFile.getPath());
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
                case EftpsPaymentResponse: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment_100k_response_PSP.151");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(mFile.getPath());
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
            }
        }
    }

    /**
     * For payment return should use 100k payment file only.
     */
    @Test
    public void test100KPaymentFile_813_997_151_WithErrors_Creation() throws Exception{
        DataLoadServices.reinitialize();
        EftpsDataLoader.create100KPayrolls();

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>().Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.PendingTransmission));
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class, query);
        assertEquals("No file 813 generated.", 1, eftpsFiles.size());
        PayrollServices.commitUnitOfWork();

        File buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentFile_100K_PSP.813");
        EdiEftpsRecordList lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
        File file = new File(eftpsFiles.get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList rhsFile = new EdiEftpsRecordList(file);
        assertTrue(lhsFile.equals(rhsFile));

        EdiManager.archiveFiles(); // archive 813  file.

        // over ride to completed status as there is no FTP.
        EftpsDataLoader.overridePendingToCompletedStatus();

        //Call EftpsSimulater.
        EftpsDataLoader.callRejectSimulator();

        List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir());

        assertEquals("Response from Simulator", 2, fileList.size());

        for (File mFile : fileList) {
            mFile = PgpUtils.getUnencryptedFile(mFile);
            switch (EftpsDataLoader.readEdiFile(mFile).getEftpsFileType()) {
                case EftpsPaymentAck: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment_100k_ack_PSP.997");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(mFile.getPath());
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
                case EftpsPaymentResponse: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentFile_100K_Reject_PSP.151");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(mFile.getPath());
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
            }
        }
    }

    /**
     * For payment return should use 100k payment file only.
     */
    @Test
    public void test100KPaymentFile_813_997_151_827_Creation() throws Exception{
        DataLoadServices.reinitialize();
        EftpsDataLoader.create100KPayrolls();

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>().Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.PendingTransmission));
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class, query);
        assertEquals("No file 813 generated.", 1, eftpsFiles.size());
        PayrollServices.commitUnitOfWork();

        File buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentFile_100K_PSP.813");
        EdiEftpsRecordList lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
        File file = new File(eftpsFiles.get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList rhsFile = new EdiEftpsRecordList(file);
        assertTrue(lhsFile.equals(rhsFile));

        EdiManager.archiveFiles(); // archive 813  file.

        // over ride to completed status as there is no FTP.
        EftpsDataLoader.overridePendingToCompletedStatus();

        //Call EftpsSimulater.
        EftpsDataLoader.callReturnSimulator();

        List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir());

        assertEquals("Incorrect number of files.", 3, fileList.size());

        for (File mFile : fileList) {
            mFile = PgpUtils.getUnencryptedFile(mFile);
            switch (EftpsDataLoader.readEdiFile(mFile).getEftpsFileType()) {
                case EftpsPaymentAck: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment_100k_ack_PSP.997");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(mFile.getPath());
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
                case EftpsPaymentResponse: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment_100k_response_PSP.151");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(mFile.getPath());
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
                case EftpsPaymentReturn: {
                    buildPath = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentFile_100K_Return_PSP.827");
                    lhsFile = new EdiEftpsRecordList(buildPath.getCanonicalPath());
                    rhsFile = new EdiEftpsRecordList(mFile.getPath());
                    assertTrue(lhsFile.equals(rhsFile));
                    break;
                }
            }
        }
    }

    @Test
    public void testFilesFromTFAParseFail() {
        //Assume some invalid files files from TFA.
        File junkFile = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/JunkFile.txt");
        EftpsUtil.copyFile(junkFile, EftpsUtil.getTfaDir());
        EdiManager.processWaitingResponseFiles();
        assertEquals("", 1, EftpsUtil.getFilesFromDir(EftpsUtil.getErrDir()).size());
        assertEquals("Eftps Files", 0, Application.find(EftpsFile.class).size());
    }

    @Test
    public void testInvalidFilesFromTFA() {
        //Assume some invalid files arrive from TFA. 151 file
        File invalid151File = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment_100k_INVALID_PSP.151");
        EftpsUtil.copyFile(invalid151File, EftpsUtil.getTfaDir());
        EdiManager.processWaitingResponseFiles();
        assertEquals("Physical file.", 1, EftpsUtil.getFilesFromDir(EftpsUtil.getErrDir()).size());
        assertEquals("Physical file.", 0, EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir()).size());
        assertEquals("Eftps Files", 0, Application.find(EftpsFile.class).size());

        //Assume some invalid files files from TFA. - 997 file
        File invalid997File = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPayment_Invalid_ack_PSP.997");
        EftpsUtil.copyFile(invalid997File, EftpsUtil.getTfaDir());
        EdiManager.processWaitingResponseFiles();
        assertEquals("", 2, EftpsUtil.getFilesFromDir(EftpsUtil.getErrDir()).size());
        assertEquals("Physical file.", 0, EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir()).size());
        assertEquals("Eftps Files", 0, Application.find(EftpsFile.class).size());

        //Assume some invalid files files from TFA.- 827
        File invalid827File = new File(new java.io.File(".").getPath() + "/Common/iop-tests/src/test/resources/staticfiles/EftpsPaymentFile_Invalid_PSP.827");
        EftpsUtil.copyFile(invalid827File, EftpsUtil.getTfaDir());
        EdiManager.processWaitingResponseFiles();
        assertEquals("", 3, EftpsUtil.getFilesFromDir(EftpsUtil.getErrDir()).size());
        assertEquals("Physical file.", 0, EftpsUtil.getFilesFromDir(EftpsUtil.getTfaDir()).size());
        assertEquals("Physical file.", 0, EftpsUtil.getFilesFromDir(EftpsUtil.getWorkDir()).size());
        assertEquals("Eftps Files", 0, Application.find(EftpsFile.class).size());
    }

    @Test
    public void testPaymentDataSet2Test100k() {

        EftpsDataLoader.createPayrollsDataSet2("123456789", "987654321");
        BatchJobManager.runJob(BatchJobType.EftpsPayment);
    }

    /**
     * for 100k payment should be skipped if it reaches MAX ACH AMOUNT.
     */
    @Test
    public void test_100k_Payments_MAX_ACH_AMOUNT_Exceeds() {

        EftpsDataLoader.createPayrollsDataSet2("123456789", "987654321");

        //This is compulsory as to make current date as initiated date. 
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // Retrieve System Parameter and update to 1.
        PayrollServices.beginUnitOfWork();
        String eftps_813_MAX_ACH_AMOUNT_PER_SEGMENT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT, "199400.00");   // Each MMT will have 1999500.00  
        PayrollServices.commitUnitOfWork();

        try {
            //call next day payments
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                       EftpsPaymentProcessor.MarkNextDayPaymentsAsProcessing.class, "PFM_100K",
                                       "EFTPSDirectDebit");
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                       EftpsPaymentProcessor.GenerateNextDayPaymentFile.class, "PFM_100K",
                                       "EFTPSDirectDebit");

            PayrollServices.beginUnitOfWork();
            //eftps file should be in pending status.
            DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
            assertEquals("No eftps files.", 0, eftpsFiles.size());

            DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
            assertEquals("Payment Detail ", 0, payDetails.size());

            DomainEntitySet<MoneyMovementTransaction> mMTDirectDebits = EftpsDataLoader.findMoneyMovementTransactions(PaymentMethod.EFTPSDirectDebit);
            assertEquals("Direct debit Money Movement transactions ", 4, mMTDirectDebits.size());

            for (MoneyMovementTransaction moneyMovementTransaction : mMTDirectDebits) {
                DomainEntitySet<FinancialTransaction> financialTransactions1 = moneyMovementTransaction.getFinancialTransactionCollection();
                assertTrue(!financialTransactions1.isEmpty());
                for (FinancialTransaction financialTransaction : financialTransactions1) {
                    assertEquals("", TransactionStateCode.Created, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                }
            }
            PayrollServices.rollbackUnitOfWork();

        } finally {
            PayrollServices.rollbackUnitOfWork();
            // RESET ORIGINAL VALUE.
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT, eftps_813_MAX_ACH_AMOUNT_PER_SEGMENT);
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void test_TwoPayments_SeparateSegment() throws Exception{
        //create two payments.
        EftpsDataLoader.createPayrollsDataSet2("123456789", "987654321");

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // Retrieve System Parameter and update to 1.
        PayrollServices.beginUnitOfWork();
        String eftps_813_MAX_PAYMENTS_PER_SEGMENT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, "1");  // maximum payments per segment.
        PayrollServices.commitUnitOfWork();

        try {
            //call next day payments.
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment,EftpsPaymentProcessor.MarkNextDayPaymentsAsProcessing.class, "PFM_NEXT_DAY", "EFTPS");
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.GenerateNextDayPaymentFile.class, "PFM_NEXT_DAY", "EFTPS");

            PayrollServices.beginUnitOfWork();
            //eftps file should be in pending status.
            DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
            assertEquals("No eftps files.", 1, eftpsFiles.size());
            PayrollServices.rollbackUnitOfWork();

            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.MarkNextDayPaymentsAsSent.class, "PFM_NEXT_DAY", "EFTPS", "FileIds=" + eftpsFiles.get(0).getFileId());

            PayrollServices.beginUnitOfWork();
            eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
            assertEquals(1, eftpsFiles.size());

            DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
            assertEquals("Payment Detail ", 2, payDetails.size());
            assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());

            assertEquals("MMT status", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
            assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());

            DomainEntitySet<FinancialTransaction> financialTransactions1 = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
            assertTrue(!financialTransactions1.isEmpty());
            for (FinancialTransaction financialTransaction : financialTransactions1) {
                assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
            }
            ///get created 813 file
            List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getWorkDir());
            assertEquals("813 file ", 1, fileList.size());

            File file = fileList.get(0);
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
            assertEquals("ST SEGMENTS ",2, l813File.getSTRecordList().size());

        } finally {

            PayrollServices.rollbackUnitOfWork();

            // Reset back to original value.
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, eftps_813_MAX_PAYMENTS_PER_SEGMENT);
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void test_TwoPayments_SameSegment() throws Exception{

        //create two payments.
        EftpsDataLoader.createPayrollsDataSet2("123456789", "987654321");

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // Retrieve System Parameter and update to 1.
        PayrollServices.beginUnitOfWork();
        String eftps_813_MAX_PAYMENTS_PER_SEGMENT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, "3");  // maximum payments per segment.
        PayrollServices.commitUnitOfWork();

        try {
            //call next day payments.
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.MarkNextDayPaymentsAsProcessing.class, "PFM_NEXT_DAY", "EFTPS");
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.GenerateNextDayPaymentFile.class, "PFM_NEXT_DAY", "EFTPS");

            PayrollServices.beginUnitOfWork();
            //eftps file should be in pending status.
            DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
            assertEquals("No eftps files.", 1, eftpsFiles.size());
            PayrollServices.rollbackUnitOfWork();

            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.MarkNextDayPaymentsAsSent.class, "PFM_NEXT_DAY", "EFTPS", "FileIds=" + eftpsFiles.get(0).getFileId());

            PayrollServices.beginUnitOfWork();
            eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
            assertEquals(1, eftpsFiles.size());

            DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
            assertEquals("Payment Detail ", 2, payDetails.size());
            assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());

            assertEquals("MMT status", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
            assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());

            DomainEntitySet<FinancialTransaction> financialTransactions1 = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
            assertTrue(!financialTransactions1.isEmpty());
            for (FinancialTransaction financialTransaction : financialTransactions1) {
                assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
            }
            ///get created 813 file
            List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getWorkDir());
            assertEquals("813 file ", 1, fileList.size());

            File file = fileList.get(0);
            file = PgpUtils.getUnencryptedFile(file);
            //single ST segments must be seen in resultant file.
            EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
            assertEquals("ST SEGMENTS ",1, l813File.getSTRecordList().size());


        } finally {
            PayrollServices.rollbackUnitOfWork();

            // Reset back to original value.
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, eftps_813_MAX_PAYMENTS_PER_SEGMENT);
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void test_NextDayPayments_MAX_ACH_AMOUNT_Exceeds() throws Exception{

        //create 4 payments. Total ACH amount would be  4 * 650000
        EftpsDataLoader.createPayrollsDataSet2("123456789", "987654321", "999999999", "888888888");

        //This is compulsory as to make current date as initiated date. 
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // Retrieve System Parameter and update to 1.
        PayrollServices.beginUnitOfWork();
        String eftps_813_MAX_ACH_AMOUNT_PER_SEGMENT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT, "70000.00");
        PayrollServices.commitUnitOfWork();

        try {
            //call next day payments.
            BatchJobManager.runJob(BatchJobType.EftpsPayment);

            PayrollServices.beginUnitOfWork();

            assertEquals("Eftps files.", 1, EftpsDataLoader.findAllEftpsFiles().size());

            DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
            assertEquals("Payment Detail ", 4, payDetails.size());

            for (EftpsPaymentDetail payDetail : payDetails) {
                assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetail.getStatusCd());
                assertEquals("MMt status", PaymentStatus.Executed, payDetail.getMoneyMovementTransaction().getStatus());
                assertNotNull("company_fk should be populated",payDetail.getCompany());
                DomainEntitySet<FinancialTransaction> financialTransactions = payDetail.getMoneyMovementTransaction().getFinancialTransactionCollection();
                assertTrue(!financialTransactions.isEmpty());
                for (FinancialTransaction financialTransaction : financialTransactions) {
                    assertEquals("Financial Transaction.", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
                }
            }
            File file = new File(EftpsDataLoader.findAllEftpsFiles().get(0).getFileName());
            file = PgpUtils.getUnencryptedFile(file);
            //check file should have 2 segments.
            EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
            assertEquals("ST SEGMENTS ",2, l813File.getSTRecordList().size());
            

        } finally {

            PayrollServices.rollbackUnitOfWork();
            // Reset back to original value.
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT, eftps_813_MAX_ACH_AMOUNT_PER_SEGMENT);
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testConsolidatedPaymentCOBRA() throws Exception{
        //create a payment with COBRA(196) law
        EftpsDataLoader.createPayrollsDataSet3("123456789");
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        assertEquals("Eftps files.", 1, EftpsDataLoader.findAllEftpsFiles().size());
        File file = new File(EftpsDataLoader.findAllEftpsFiles().get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
        List<EDIRecordTemplate> list = l813File.getRecordListForId(RecordId.EDI_813_SEG_INNER_TIA);
        assertEquals("Incorrect TIA segments.",1,list.size());
        assertEquals("In correct payment method.", "94105",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
    }

    @Test
    public void testConsolidatedPaymentNPL() throws Exception {
        //create a payment with NPL(214) law
        EftpsDataLoader.createPayrollsData("123456789", new String[]{"1", "214"}, new String[]{"5000", "-500.00"});
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        assertEquals("Eftps files.", 1, EftpsDataLoader.findAllEftpsFiles().size());
        File file = new File(EftpsDataLoader.findAllEftpsFiles().get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
        List<EDIRecordTemplate> list = l813File.getRecordListForId(RecordId.EDI_813_SEG_INNER_TIA);
        assertEquals("Incorrect TIA segments.", 1, list.size());
        assertEquals("In correct payment method.", "94105", list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
    }

    @Test
    public void testConsolidatedPaymentEmployeeRetention() throws Exception {
        //create a payment with 215 law
        EftpsDataLoader.createPayrollsData("123456789", new String[]{"1", "215"}, new String[]{"5000", "-500.00"});
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        assertEquals("Eftps files.", 1, EftpsDataLoader.findAllEftpsFiles().size());
        File file = new File(EftpsDataLoader.findAllEftpsFiles().get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
        List<EDIRecordTemplate> list = l813File.getRecordListForId(RecordId.EDI_813_SEG_INNER_TIA);
        assertEquals("Incorrect TIA segments.", 1, list.size());
        assertEquals("In correct payment method.", "94105", list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
    }

    @Test
    public void testPaymentReducedByFICADeferralCredit() throws Exception {
        EftpsDataLoader.createPayrollsData("123456789",
                                           new String[]{"1", "61", "62", "63", "64", "200", "216"},
                                           // FIT: $1000, FICA EE: $100, FICA ER: $100, MED EE: $10, MED ER: $100, MED ADTL: $10, FICA ER Deferral: -100
                                           new String[]{"1000", "100", "100", "10", "10", "10", "-100"});
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        assertEquals("Eftps files.", 1, EftpsDataLoader.findAllEftpsFiles().size());
        File file = new File(EftpsDataLoader.findAllEftpsFiles().get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
        List<EDIRecordTemplate> list = l813File.getRecordListForId(RecordId.EDI_813_SEG_INNER_TIA);
        assertEquals("Incorrect TIA segments.", 3, list.size());

        // FICA
        assertEquals("In correct payment method.", "1",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "10000",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));
        // MED
        assertEquals("In correct payment method.", "2",list.get(1).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "3000",list.get(1).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));
        // FIT
        assertEquals("In correct payment method.", "3",list.get(2).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "100000",list.get(2).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));
    }

    @Test
    public void testConsolidatedPaymentZeroMMT()
    {
        EftpsDataLoader.createPayrollsDataSetZeroAmount("123456789");

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        //payment should be skipped.
        assertEquals("Eftps files.", 0, EftpsDataLoader.findAllEftpsFiles().size());
        assertEquals("Eftps Payment details.", 0, EftpsPaymentDetail.findAllPaymentDetails().size());
        assertEquals("Eftps Payment physical files.", 0, EftpsUtil.getFilesFromDir(EftpsUtil.getWorkDir()).size());
    }

    @Test
    public void testConsolidatedPaymentFutaMMT_940Filing() throws Exception{
        EftpsDataLoader.createPayrollsDataSetFUTA940Filing("123456789");
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DomainEntitySet<EftpsFile> eftpsFiles = EftpsDataLoader.findAllEftpsFiles();

        //payment should be skipped.
        assertEquals("Eftps files.", 1, eftpsFiles.size());
        File file = new File(eftpsFiles.get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
        List<EDIRecordTemplate> list = l813File.getRecordListForId(RecordId.EDI_813_SEG_INNER_TIA);
        assertEquals("Incorrect TIA segments.", 1, list.size());
        assertEquals("In correct payment method.", "09405",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "50000",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));  //i.e 500.00
    }

    @Test
    public void testConsolidatedPaymentFutaMMT_944Filing() throws Exception{
        //create a payment with COBRA(196) law
        EftpsDataLoader.createPayrollsDataSet3("123456789");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyAgencyFormTemplate> listft = CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().find(CompanyAgencyFormTemplate.FormTemplate().FormTemplateCd().equalTo("IRS-941-FILING"));
        DomainEntitySet<FormTemplate>  templates = Application.find(FormTemplate.class,new Query().Where(FormTemplate.FormTemplateCd().equalTo("IRS-944-FILING")));
        listft.get(0).setFormTemplate(templates.get(0));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        //payment should be skipped.
        assertEquals("Eftps files.", 1, EftpsDataLoader.findAllEftpsFiles().size());
        File file = new File(EftpsDataLoader.findAllEftpsFiles().get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
        List<EDIRecordTemplate> list = l813File.getRecordListForId(RecordId.EDI_813_SEG_INNER_TIA);
        assertEquals("Incorrect TIA segments.",1,list.size());
        assertEquals("In correct payment method.", "94405",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "450000",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));  //i.e 500.00
    }

    @Test
    public void testItemizedPayment_944Filing() throws Exception{
        //create a payment with COBRA(196) law
        EftpsDataLoader.createPayrollsDataSetFUTA944Filing("123456789");

        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DomainEntitySet<EftpsFile> eftpsFiles = EftpsDataLoader.findAllEftpsFiles();

        //payment should be skipped.
        assertEquals("Eftps files.", 1, eftpsFiles.size());

        File file = new File(eftpsFiles.get(0).getFileName());
        file = PgpUtils.getUnencryptedFile(file);
        EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
        List<EDIRecordTemplate> list = l813File.getRecordListForId(RecordId.EDI_813_SEG_INNER_TIA);

        assertEquals("Incorrect TIA segments.", 3, list.size());

        assertEquals("In correct payment method.", "1",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "70000",list.get(0).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));

        assertEquals("In correct payment method.", "2",list.get(1).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "30000",list.get(1).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));

        assertEquals("In correct payment method.", "3",list.get(2).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01));
        assertEquals("In correct payment.", "50000",list.get(2).getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02));
    }

    @Test
    public void testConsolidatedPaymentInvalidFiling() {
        //create a payment with COBRA(196) law
        EftpsDataLoader.createPayrollsDataSet3("123456789");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyAgencyFormTemplate> listft = CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().find(CompanyAgencyFormTemplate.FormTemplate().FormTemplateCd().equalTo("IRS-941-FILING"));
        DomainEntitySet<FormTemplate> templates = Application.find(FormTemplate.class, new Query().Where(FormTemplate.FormTemplateCd().equalTo("GA-GA7Q-FILING")));
        listft.get(0).setFormTemplate(templates.get(0));
        PayrollServices.commitUnitOfWork();
        BatchJobManager.runJob(BatchJobType.EftpsPayment);
        //payment should be skipped.
        assertEquals("Eftps files.", 0, EftpsDataLoader.findAllEftpsFiles().size());
    }
    
    @Test
    public void test_LimitSegmentsin813File() throws Exception{
        //create two payments.
        EftpsDataLoader.createPayrollsDataSet2("123456789", "987654321");

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // Retrieve System Parameter and update to 1.
        PayrollServices.beginUnitOfWork();
        String eftps_813_MAX_PAYMENTS_PER_SEGMENT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, "1");  // maximum payments per segment. it creates two segments.

        String eftps_838_MAX_SEGMENT_COUNT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_SEGMENTS_PER_FILE);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_SEGMENTS_PER_FILE, "1");  // maximum segments per file.  each segment should go to seperate file.
        PayrollServices.commitUnitOfWork();

        try {
            //call next day payments.
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.MarkNextDayPaymentsAsProcessing.class, "PFM_NEXT_DAY", "EFTPS");
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.GenerateNextDayPaymentFile.class, "PFM_NEXT_DAY", "EFTPS");

            PayrollServices.beginUnitOfWork();
            //eftps file should be in pending status.
            DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
            assertEquals("No eftps files.", 2, eftpsFiles.size());
            PayrollServices.rollbackUnitOfWork();

            StringBuilder sb = new StringBuilder();
            for (EftpsFile eftpsFile : eftpsFiles) {
                sb.append(eftpsFile.getFileId()).append(",");
            }
            sb.setLength(sb.length() - 1);
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.MarkNextDayPaymentsAsSent.class, "PFM_NEXT_DAY", "EFTPS", "FileIds=" + sb.toString());

            PayrollServices.beginUnitOfWork();
            //eftps file should be in pending status.
            eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
            assertEquals(2, eftpsFiles.size());

            DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
            assertEquals("Payment Detail ", 2, payDetails.size());
            assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());

            assertEquals("MMT status", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
            assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());

            DomainEntitySet<FinancialTransaction> financialTransactions1 = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
            assertTrue(!financialTransactions1.isEmpty());
            for (FinancialTransaction financialTransaction : financialTransactions1) {
                assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
            }
            ///get created 813 file
            List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getWorkDir());
            assertEquals("813 file ", 2, fileList.size());
            File file = fileList.get(0);
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
            assertEquals("ST SEGMENTS ", 1, l813File.getSTRecordList().size());

        } finally {

            PayrollServices.rollbackUnitOfWork();

            // Reset back to original value.
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_SEGMENTS_PER_FILE, eftps_838_MAX_SEGMENT_COUNT);
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, eftps_813_MAX_PAYMENTS_PER_SEGMENT);
            PayrollServices.commitUnitOfWork();
        }
    }

    /**
     * This test is to hit the boundary condition exception in the payment file as to flush the segments for the file.
     * In real time scenario, with EdiManager we can not hit the condition as we do process select maxpaymentsperfile and process them in each run.
     * In order to hit the Boundary condition, copied the payment file creation and hit the unreachable block.
     */
    @Test
    public void test_LimitSegments813File_BoundaryCondition() throws Exception{
        //create two payments.
        EftpsDataLoader.createPayrollsDataSet2("123456789", "987654321");

        //This is compulsory as to make current date as initiated date.
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // Retrieve System Parameter and update to 1.
        PayrollServices.beginUnitOfWork();
        String eftps_813_MAX_PAYMENTS_PER_SEGMENT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, "1");  // maximum payments per segment. it creates two segments.

        String eftps_838_MAX_SEGMENT_COUNT = SystemParameter.findStringValue(SystemParameter.Code.EFTPS_813_MAX_SEGMENTS_PER_FILE);
        SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_SEGMENTS_PER_FILE, "1");  // maximum segments per file.  each segment should go to seperate file.
        PayrollServices.commitUnitOfWork();

        try {

            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.MarkNextDayPaymentsAsProcessing.class, "PFM_NEXT_DAY", "EFTPS");
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.GenerateNextDayPaymentFile.class, "PFM_NEXT_DAY", "EFTPS");

            PayrollServices.beginUnitOfWork();
            //eftps file should be in pending status.
            DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
            assertEquals("No eftps files.", 2, eftpsFiles.size());
            PayrollServices.rollbackUnitOfWork();

            StringBuilder sb = new StringBuilder();
            for (EftpsFile eftpsFile : eftpsFiles) {
                sb.append(eftpsFile.getFileId()).append(",");
            }
            sb.setLength(sb.length() - 1);
            BatchJobManager.runJobStep(BatchJobType.EftpsPayment, EftpsPaymentProcessor.MarkNextDayPaymentsAsSent.class, "PFM_NEXT_DAY", "EFTPS", "FileIds=" + sb.toString());

            PayrollServices.beginUnitOfWork();
            eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
            assertEquals(2, eftpsFiles.size());

            DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
            assertEquals("Payment Detail ", 2, payDetails.size());
            assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());

            assertEquals("MMT status", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
            assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());

            DomainEntitySet<FinancialTransaction> financialTransactions1 = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
            assertTrue(!financialTransactions1.isEmpty());
            for (FinancialTransaction financialTransaction : financialTransactions1) {
                assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
            }
            ///get created 813 file
            List<File> fileList = EftpsUtil.getFilesFromDir(EftpsUtil.getWorkDir());
            assertEquals("813 file ", 2, fileList.size());
            File file = fileList.get(0);
            file = PgpUtils.getUnencryptedFile(file);
            EdiEftpsRecordList l813File = new EdiEftpsRecordList(file);
            assertEquals("ST SEGMENTS ", 1, l813File.getSTRecordList().size());

        } finally {

            PayrollServices.rollbackUnitOfWork();

            // Reset back to original value.
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_SEGMENTS_PER_FILE, eftps_838_MAX_SEGMENT_COUNT);
            SystemParameter.update(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, eftps_813_MAX_PAYMENTS_PER_SEGMENT);
            PayrollServices.commitUnitOfWork();
        }
    }

//    @Test
//    public void test_Ken() throws Exception {
//        String data = "ISA~00~          ~00~          ~30~000000880146711~12~3034888600     ~110214~141\n" +
//                      "9~U~00305~110214362~0~T~~\\GS~TF~880146711~3034888600~110214~1419~110214362~X~003\n" +
//                      "050\\ST~813~000036002\\BTI~T6~EFTPS~47~IRS~110214~~48~880146711\\DTM~009~110214\\TIA\n" +
//                      "~EFTPS~23276\\REF~VU~8655\\BPR~Z~23276~D~ACH~~~~~~~~01~111000614~03~826076945~1103\n" +
//                      "04\\TFS~T6~94105~~~24~999332442~110331~\\REF~F8~00000121\\FGS~1\\TIA~1~9600\\TIA~2~26\n" +
//                      "76\\TIA~3~11000\\SE~13~000036002\\GE~1~110214362\\IEA~1~110214362\\";
//        File sourceFile = new File(EftpsUtil.getWorkDir(), "sourcefile.edi");
//        FileWriter writer = new FileWriter(sourceFile);
//
//        try {
//            writer.write(data);
//        } finally {
//            writer.flush();
//            writer.close();
//        }
//
//        TFASimulator sim = new TFASimulator();
//
//        List<RejectionInfo> riList = new ArrayList<RejectionInfo>();
//        RejectionInfo ri = new RejectionInfo("00000121", "1125"); // (inner) REF02 and Error Code
//
//        riList.add(ri);
//
//        sim.processPaymentFileWithErrors(sourceFile, EftpsUtil.getTfaDir(), riList, null);
//    }

//    @Test
//    public void test_Ken() throws Exception {
//        //
//        // Create 100K payroll
//        //
//        EftpsDataLoader.create100KPayrolls();
//
//        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()));
//        PayrollServices.commitUnitOfWork();
//
//        EdiManager.process100kPayments();
//
//        //
//        // Create normal payroll
//        //
//        PayrollServices.beginUnitOfWork();
//        PSPDate.resetPSPTime();
//        PayrollServices.commitUnitOfWork();
//
//        testCreatePaymentData("123456780");
//
//        EdiManager.processNextDayPayments();
//
//        //
//        // Transmit files to TFA
//        //
//        EdiManager.processPendingTransmissions();
//    }

    @Test
    public void test_SameDay_Payment_HappyPath() throws S3ConnectionException,S3UploadException{
        testCreatePaymentData();

        //call same day payments.
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.MarkPaymentsAsProcessing.class,
                                   "PFM_SAME_DAY", "EFTPS",
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + PSPDate.getPSPTime().format(BatchUtils.DATE_FORMAT));
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.GeneratePaymentFile.class,
                                   "PFM_SAME_DAY", "EFTPS",
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + PSPDate.getPSPTime().format(BatchUtils.DATE_FORMAT));

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.InProcess, eftpsFiles.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        StringBuilder sb = new StringBuilder();
        for (EftpsFile eftpsFile : eftpsFiles) {
            sb.append(eftpsFile.getFileId()).append(",");
        }
        sb.setLength(sb.length() - 1);
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.MarkPaymentsAsSent.class,
                                   "PFM_SAME_DAY",
                                   "EFTPS",
                                   "FileIds=" + sb.toString(),
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + PSPDate.getPSPTime().format(BatchUtils.DATE_FORMAT));

        PayrollServices.beginUnitOfWork();
        eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());
        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());
        DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Detail ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        DomainEntitySet<FinancialTransaction> financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        PayrollServices.beginUnitOfWork();
        EftpsFile eftFile = EftpsFile.getPendingTransmissionEftpsFiles().get(0);
        assertNotNull(eftFile);
        eftFile.setStatusCd(EdiFileStatus.Completed);
        Application.save(eftFile);
        PayrollServices.commitUnitOfWork();

        EftpsDataLoader.callSimulator();

        //Archive file. Only payment file(813) will be in completed status.
        EdiManager.archiveFiles();

        //process awaiting response file. i.e processing the files in TFA directory. this step will process responses/acknowledgements from TFA.
        // If the response file owner is AS400, it will log the info in PSP and send it to AS400. The file status will be INPROCESS state until it uploads to TFA.
        EdiManager.processWaitingResponseFiles();

        //ARCHIVE COMPLETED RECORDS.
        EdiManager.archiveFiles();

        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals("No payment files.", 1, eftpsFiles.size());

        //Check archived files.
        PayrollServices.beginUnitOfWork();
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(EftpsFile.StatusCd().equalTo(EdiFileStatus.Archived))
                .OrderBy(EftpsFile.CreatedDate().Descending());
        eftpsFiles = Application.find(EftpsFile.class, query);

        assertEquals("No payment files.", 3, eftpsFiles.size());

        payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Details ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.AcknowledgedByAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_SameDay_Payment_Reject() throws S3ConnectionException,S3UploadException{
        testCreatePaymentData();

        //call same day payments.
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.MarkPaymentsAsProcessing.class,
                                   "PFM_SAME_DAY", "EFTPS",
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + PSPDate.getPSPTime().format(BatchUtils.DATE_FORMAT));
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.GeneratePaymentFile.class,
                                   "PFM_SAME_DAY", "EFTPS",
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + PSPDate.getPSPTime().format(BatchUtils.DATE_FORMAT));

        PayrollServices.beginUnitOfWork();
        //eftps file should be in pending status.
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.InProcess, eftpsFiles.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        StringBuilder sb = new StringBuilder();
        for (EftpsFile eftpsFile : eftpsFiles) {
            sb.append(eftpsFile.getFileId()).append(",");
        }
        sb.setLength(sb.length() - 1);
        BatchJobManager.runJobStep(BatchJobType.EftpsPayment,
                                   EftpsPaymentProcessor.MarkPaymentsAsSent.class,
                                   "PFM_SAME_DAY",
                                   "EFTPS",
                                   "FileIds=" + sb.toString(),
                                   "BepsRefNum=abcde",
                                   "BepsSettlementDate=" + PSPDate.getPSPTime().format(BatchUtils.DATE_FORMAT));

        PayrollServices.beginUnitOfWork();
        eftpsFiles = Application.find(EftpsFile.class);
        assertEquals("No eftps files.", 1, eftpsFiles.size());
        assertEquals("Invalid file status.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());
        eftpsFiles = EftpsFile.getPendingTransmissionEftpsFiles();
        assertEquals(1, eftpsFiles.size());
        DomainEntitySet<EftpsPaymentDetail> payDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertEquals("Payment Detail ", 1, payDetails.size());
        assertEquals("Payment status ", TaxPaymentStatus.SentToAgency, payDetails.get(0).getStatusCd());
        assertEquals("", PaymentStatus.Executed, payDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",payDetails.getFirst().getCompany());
        assertEquals(payDetails.getFirst().getMoneyMovementTransaction().getCompany(),
                payDetails.getFirst().getCompany());
        DomainEntitySet<FinancialTransaction> financialTransactions = payDetails.get(0).getMoneyMovementTransaction().getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Executed, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        //override status as completed (i.e by passing FTP). This step is not seen in production.
        EftpsDataLoader.overridePendingToCompletedStatus();

        EftpsDataLoader.callRejectSimulator();

        //Process simulator generated files.
        EdiManager.processWaitingResponseFiles();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = EftpsPaymentDetail.findAllPaymentDetails();
        assertTrue(!paymentDetails.isEmpty());
        assertEquals("Incorrect status", TaxPaymentStatus.RejectedByAgency, paymentDetails.get(0).getStatusCd());
        MoneyMovementTransaction mmt = paymentDetails.get(0).getMoneyMovementTransaction();
        assertEquals("", PaymentStatus.Executed, paymentDetails.get(0).getMoneyMovementTransaction().getStatus());
        assertNotNull("company_fk should be populated",paymentDetails.getFirst().getCompany());
        financialTransactions = mmt.getFinancialTransactionCollection();
        assertTrue(!financialTransactions.isEmpty());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("", TransactionStateCode.Returned, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        assertEquals("In correct pending files.", 1, EftpsFile.getPendingTransmissionEftpsFiles().size());
        PayrollServices.rollbackUnitOfWork();

        //Archive.
        EdiManager.archiveFiles();
    }
    // there are no no payment return tests for Same day as they will apply only for 100K.

    @Test
    /**
     * PSRV002266: When EFTPS File Creation skips payments AgencyTaxOverpaymentApplied txns are in a wrong state
     *
     * KP - Ignore this test until Zack fixes the AgencyTaxOverpaymentApplied functionality
     */
    public void test_PSRV002266_happyPath(){
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> procResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-17.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-15.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 11, 2, 15, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);

        assertEquals("EftpsFile record not found.", 1, eftpsFiles.size());
        assertEquals("EftpsFile record not in PendingTransmission state.", EdiFileStatus.PendingTransmission, eftpsFiles.get(0).getStatusCd());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpaymentApplied)
                .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed));
        assertEquals("Number of Executed AgencyTaxOverpaymentApplied FTs:", 2, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    /**
     * PSRV002266
     *
     * KP - Ignore this test until Zack fixes the AgencyTaxOverpaymentApplied functionality
     */
    public void test_PSRV002266_Cancelled(){
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63","143", "66"}, new String[]{"75", "60", "125","55", "40"});
        ProcessResult<PayrollRun> procResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-178.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-150.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 11, 2, 15 , 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EftpsPayment);
        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class);

        assertEquals("EftpsFile records generated", 0, eftpsFiles.size());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpaymentApplied)
                .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Cancelled));
        assertEquals("Number of Cancelled AgencyTaxOverpaymentApplied FTs:", 2, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }

    
    @Test
    /**
     * PSRV002374: If the disbursement (FT) sum does not match the MMT amount, we need to skip the payment
     */
    public void test_PSRV002374_MmtAndDisbursementsOutOfBalance() {
        testCreatePaymentData();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<Company> company = Application.find(Company.class, Company.SourceCompanyId().equalTo("123456789"));
        DomainEntitySet<MoneyMovementTransaction> pendingMMTs = company.get(0).findPendingTaxPayments();

        // ensure there is exactly one mmt
        assertEquals("MMT count > 1", pendingMMTs.size(), 1);

        MoneyMovementTransaction mmt = pendingMMTs.get(0);

        assertEquals("MMT not in a created state.", mmt.getStatus(), PaymentStatus.Created);
        assertEquals("MMT tax payment status nit equal to ReadyToSend.", mmt.getTaxPaymentStatus(), TaxPaymentStatus.ReadyToSend);

        BigDecimal mmtAmount = SpcfUtils.convertToBigDecimal(mmt.getMoneyMovementTransactionAmount());
        BigDecimal disbursementTotal = new BigDecimal(0.00);

        // sum the FTs to get the total disbursement amount
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            disbursementTotal = disbursementTotal.add(SpcfUtils.convertToBigDecimal(ft.getFinancialTransactionAmount()));
        }

        // ensure the disbursement amount equals the mmt amount
        assertTrue("Disbursement not equal to mmt amount", mmtAmount.equals(disbursementTotal));

        // grab the first FT on the mmt
        FinancialTransaction oobFT = mmt.getFirstFinancialTransaction();

        // subtract one dollar from the first FT just to throw the disbursement (FT) sum out of balance from the MMT amount
        oobFT.setFinancialTransactionAmount((SpcfMoney) oobFT.getFinancialTransactionAmount().subtract(new SpcfMoney("1.00")));

        Application.save(oobFT);

        PayrollServices.commitUnitOfWork();

        // attempt to create a payment file with the out-of-balance tax payment (should fail)
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();

        company = Application.find(Company.class, Company.SourceCompanyId().equalTo("123456789"));
        pendingMMTs = company.get(0).findPendingTaxPayments();

        // ensure there is exactly one mmt
        assertEquals("MMT count > 1", pendingMMTs.size(), 1);

        mmt = pendingMMTs.get(0);

        // ensure the MMT is still in a Created state and its tax payment status is still ReadyToSend
        assertEquals("MMT not in a created state.", mmt.getStatus(), PaymentStatus.Created);
        assertEquals("MMT tax payment status not equal to ReadyToSend.", mmt.getTaxPaymentStatus(), TaxPaymentStatus.ReadyToSend);

        DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class, EftpsFile.FileType().equalTo(EdiFileType.EftpsPayment));

        // ensure no eftps payment file was created
        assertEquals("EftpsFile record found.", 0, eftpsFiles.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPaymentInProcessIdChanged() {
        DataLoadServices.setPSPDate(2012, 10, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.enrollEFTPS(company);
        List<Employee> employees = DataLoadServices.addEEs(company, 1, true, true);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO(2012, 10, 10), employees, new String[]{"1"}, new String[]{"20.00"});
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess("Payroll submit failed", PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class,
                                                                                       MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                                                                                                               .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created))));
        assertEquals("tax payer id does not match company", company.getFedTaxId(), moneyMovementTransaction.getAgencyTaxpayerId());
        PayrollServices.rollbackUnitOfWork();

        String newTaxPayerId = "New Id 1";
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.getIRS_941());
        companyAgencyPaymentTemplate.updateAgencyTaxpayerId(newTaxPayerId);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class,
                                                                                       MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                                                                                                               .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created))));
        assertEquals("tax payer id was not updated", newTaxPayerId, moneyMovementTransaction.getAgencyTaxpayerId());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction.markPaymentsInProcessForDate(PaymentMethod.EFTPS, SpcfCalendar.createInstance(2012, 10, 16, SpcfTimeZone.getLocalTimeZone()), 10);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                                                                                           .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.InProcess))));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.getIRS_941());
        companyAgencyPaymentTemplate.updateAgencyTaxpayerId("New Id 2");
        PayrollServices.commitUnitOfWork();

        // in process payment should not get updated
        PayrollServices.beginUnitOfWork();
        moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)));
        assertEquals("tax payer id was updated while in process", newTaxPayerId, moneyMovementTransaction.getAgencyTaxpayerId());
        PayrollServices.rollbackUnitOfWork();

    }
}
