package com.intuit.sbd.payroll.psp.agency.eftps;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.ops.eftpsBp.EdiEftpsRecordList;
import com.paycycle.ops.eftpsBp.GenericEdiFile;
import com.paycycle.util.PgpUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Dec 7, 2010
 * Time: 9:51:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsDataLoader {

    private DataLoader dataloader = new DataLoader();
    private static final String FIT = "1";
    private static final String FICA = "61";
    private static final String Medicare = "63";
    private static final String FUTA = "66";
    private static final String COBRA = "196";

public static void deleteFiles(String folderPath) {
        File folder = new File(folderPath);
        // Get all files
        File[] files = folder.listFiles();
        // Delete all files
        for (File file : files) {
            file.delete();
        }
    }

    public static void deleteAllTestDirFiles() {
        deleteFiles(EftpsUtil.getWorkDir());
        deleteFiles(EftpsUtil.getTfaDir());
        deleteFiles(EftpsUtil.getErrDir());
        deleteFiles(EftpsUtil.getArchiveDir());
        deleteFiles(EftpsUtil.getAS400Dir());
    }

    public static GenericEdiFile readEdiFile(File pFile) throws IOException {

        if (pFile.isDirectory()) {
            new GenericEdiFile().setFileName("InvalidFile.");
        }
        GenericEdiFile mEdiFile = new GenericEdiFile();
        try {

            mEdiFile.setFileName(pFile.getPath());
            mEdiFile.read();
        } catch (Throwable t) {
           new GenericEdiFile().setFileName("InvalidFile.");
        }
        return mEdiFile;
    }

    public static void callSimulator() {
        callSimulator(false, false,false);
    }

    public static void callReturnSimulator()
    {
        callSimulator(false, true,false); // It will set the value "R01"
    }

    public static void callReturnSimulatorWithNOC()
    {
       callSimulator(false,true,true);
    }

    public static void callRejectSimulator()
    {
        callSimulator(true,false,false);
    }

    public static void callSimulator(boolean pRejections, boolean pReturns, boolean isNOCReturn) {
        //Fetch status = completed records and call simulator. This step is not seen in the production code as we ftp files to TFA and they send back files in to TFA dir.
        DomainEntitySet<EftpsFile> list = EftpsFile.getCompletedEftpsFiles();
        TFASimulator simulator = new TFASimulator();
        for (EftpsFile eftpsFile : list) {
            try {
                File file = new File(eftpsFile.getFileName());
                file = PgpUtils.getUnencryptedFile(file);
                if (pRejections) {
                    simulator.processPaymentFileWithErrors(file, EftpsUtil.getTfaDir(), getRejectInfo(file.getAbsolutePath()), null);
                } else if(pReturns) {
                    simulator.processPaymentFileWithErrors(file, EftpsUtil.getTfaDir(), null, getReturnInfo(file.getAbsolutePath(),isNOCReturn));
                } else {
                    simulator.processFile(file, EftpsUtil.getTfaDir());
                }
            } catch (Throwable t) {
                t.printStackTrace();
                assertTrue("Error processing payment file  Simulator.", "".equals("something."));
            }
        }
    }

    public static List<ReturnSegInfo> getReturnInfo(String pFileName,boolean pIsNoCReturn) {

        List<ReturnSegInfo> returnSegInfoList = new ArrayList<ReturnSegInfo>();
        String eftpsPaymentTxId = "0";

        try {
            // Read created 813 file
            EdiEftpsRecordList inputFile = new EdiEftpsRecordList(new File(pFileName).getCanonicalPath());
            List<EDIRecordTemplate> recordList = inputFile.getRecordList();
            ReturnSegInfo returnSegInfo = null;
            // browse through each record of 813
            for (EDIRecordTemplate ediRecordTemplate : recordList) {

                if (ediRecordTemplate.getId() == RecordId.EDI_SEG_ST) {
                    returnSegInfo = new ReturnSegInfo();
                    returnSegInfo.setReturnSegId(ediRecordTemplate.getFieldValue(FieldId.EDI_SEG_ST02));

                    if(pIsNoCReturn){
                       returnSegInfo.setErrorCode("011"); 
                       returnSegInfo.setReturnSegErrorCode("C01");
                    }else{
                    returnSegInfo.setErrorCode("830");
                    returnSegInfo.setReturnSegErrorCode("R01");
                    }
                    returnSegInfoList.add(returnSegInfo);
                }
            }
            //call simulator.
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue("Error processing payment file  Simulator.", "".equals("something."));
        }
        return returnSegInfoList;
    }

    public static List<RejectionInfo> getRejectInfo(String pFileName) {
        
        DomainEntitySet<EftpsFile> list = EftpsFile.getCompletedEftpsFiles();

        TFASimulator simulator = new TFASimulator();
        List<RejectionInfo> pRejectionInfos = new ArrayList<RejectionInfo>();
        String eftpsPaymentTxId = "0";

        try {
            // Read created 813 file
            EdiEftpsRecordList inputFile = new EdiEftpsRecordList(new File(pFileName).getCanonicalPath());
            List<EDIRecordTemplate> recordList = inputFile.getRecordList();

            // browse through each record of 813
            for (EDIRecordTemplate ediRecordTemplate : recordList) {

                //Identify REF segment
                if (ediRecordTemplate.getId() == RecordId.EDI_813_SEG_INNER_REF) {
                    eftpsPaymentTxId = ediRecordTemplate.getFieldValue(FieldId.EDI_813_SEG_INNER_REF02);
                    pRejectionInfos.add(new RejectionInfo(eftpsPaymentTxId, "1101")); //Induce an error.
                    break; //
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue("Error processing payment file  Simulator.", "".equals("something."));
        }
        return pRejectionInfos;
    }

    public static void overridePendingToCompletedStatus() {
        PayrollServices.beginUnitOfWork();
        try {
             DomainEntitySet<EftpsFile> pendingFiles = EftpsFile.getPendingTransmissionEftpsFiles();

            for (EftpsFile pendingFile : pendingFiles) {
                assertNotNull(pendingFile);
                pendingFile.setStatusCd(EdiFileStatus.Completed);
                Application.save(pendingFile);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue("Problem while overridePendingToCompletedSatus.", "".equals("something."));
        }finally{
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static void create100KPayrolls()  {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-05"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"5000", "2500", "3000", "4500"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        // DataLoadServices.assertMmt(SettlementType.EFTPS, new SpcfMoney("33000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);

        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"6000", "2500", "3000", "4500"});
        payrollRunDTO1.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        // DataLoadServices.assertMmt(SettlementType.EFTPS, new SpcfMoney("60000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);


        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"7000", "2500", "4000", "5000"});
        payrollRunDTO2.setPayrollTXBatchId("Payroll_3");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void createPayrollsDataSet2(String... pPsids) {

        Hashtable<String, Company> companies = new Hashtable<String, Company>();
        Hashtable<String, List<Employee>> employees = new Hashtable<String, List<Employee>>();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        for (String psid : pPsids) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
            companies.put(psid, company);

            DataLoadServices.addCompanyPIN(company, null);
            DataLoadServices.addCompanyBankAccount(company);

            PayrollServices.beginUnitOfWork();
            PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(),company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
            PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(),company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
            PayrollServices.commitUnitOfWork();

            DataLoadServices.addFederalTaxCompanyLaws(company);
            List<Employee> emps = DataLoadServices.addEEs(company, 3);
            employees.put(psid, emps);
            DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

            DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-05"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"5000", "2500", "3000", "4500"});
            payrollDTO.setPayrollTXBatchId("Payroll_1");
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
        }

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

//        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
//        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
//        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        // DataLoadServices.assertMmt(SettlementType.EFTPS, new SpcfMoney("33000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);

        for (String psid : pPsids) {
            Company company = companies.get(psid);
            List<Employee> emps = employees.get(psid);
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
            PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"6000", "2500", "3000", "4500"});
            payrollRunDTO1.setPayrollTXBatchId("Payroll_2");
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
    //        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
    //        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
    //        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);

            PayrollServices.beginUnitOfWork();
            PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
            PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66","65"}, new String[]{"60000", "2500", "4000", "5000","6000"});
            payrollRunDTO2.setPayrollTXBatchId("Payroll_3");
            processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();

        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 07, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    private static SpcfCalendar getInitiationDate() {
        return getInitiationDate(PSPDate.getPSPTime());
    }

    private static SpcfCalendar getInitiationDate(SpcfCalendar pCalendar) {
        CalendarUtils.clearTime(pCalendar);
        return pCalendar;
    }

    public static void createPayrollsDataSet3(String pPsid) {
        createPayrollsData(pPsid, new String[]{"1", "196"}, new String[]{"5000", "-500.00"});
    }

    public static void createPayrollsData(String pPsid, String[] pLawIds, String[] pAmounts) {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = pPsid;

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);

        for (String lawId : pLawIds) {
            PayrollServices.beginUnitOfWork();
            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, lawId);
            PayrollServices.rollbackUnitOfWork();
            if (companyLaw == null) {
                DataLoadServices.addCompanyLaws(company, lawId);
            }
        }

        List<Employee> emps = DataLoadServices.addEEs(company, 1);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addCOBRACompanyLaw(company);
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, pLawIds, pAmounts);
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created));

        DomainEntitySet<MoneyMovementTransaction> pendingPayments = Application.find(MoneyMovementTransaction.class, mmtCriteria);

        for (MoneyMovementTransaction pendingPayment : pendingPayments) {
            pendingPayment.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
            pendingPayment.setInitiationDate(getInitiationDate());
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void createPayrollsDataSetFUTA940Filing(String pPsid) {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = pPsid;

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"65"}, new String[]{"500.00"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();        

        PayrollServices.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created));

        DomainEntitySet<MoneyMovementTransaction> pendingPayments = Application.find(MoneyMovementTransaction.class, mmtCriteria);

        for (MoneyMovementTransaction pendingPayment : pendingPayments) {
            pendingPayment.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
            pendingPayment.setInitiationDate(getInitiationDate());
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void createPayrollsDataSetFUTA944Filing(String pPsid) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = pPsid;

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyAgencyFormTemplate> listft = CompanyAgencyFormTemplate.getCompanyAgencyFTCollection().find(CompanyAgencyFormTemplate.FormTemplate().FormTemplateCd().equalTo("IRS-941-FILING"));
        DomainEntitySet<FormTemplate>  templates = Application.find(FormTemplate.class,new Query().Where(FormTemplate.FormTemplateCd().equalTo("IRS-944-FILING")));
        listft.get(0).setFormTemplate(templates.get(0));
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "62", "63", "64"}, new String[]{"500", "400", "300", "200", "100"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created));

        DomainEntitySet<MoneyMovementTransaction> pendingPayments = Application.find(MoneyMovementTransaction.class, mmtCriteria);

        for (MoneyMovementTransaction pendingPayment : pendingPayments) {
            pendingPayment.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
            pendingPayment.setInitiationDate(getInitiationDate());
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void createPayrollsDataSetZeroAmount(String pPsid) {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = pPsid;

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addCOBRACompanyLaw(company);
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "196"}, new String[]{"5000", "-5000.00"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)
                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created));

        DomainEntitySet<MoneyMovementTransaction> pendingPayments = Application.find(MoneyMovementTransaction.class, mmtCriteria);

        for (MoneyMovementTransaction pendingPayment : pendingPayments) {
            pendingPayment.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
            pendingPayment.setInitiationDate(getInitiationDate());
        }
        PayrollServices.commitUnitOfWork();
    }

    public static DomainEntitySet<MoneyMovementTransaction> findMoneyMovementTransactions(PaymentMethod pPaymethod) {
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>().Where(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(pPaymethod));
        return Application.find(MoneyMovementTransaction.class, query);
    }

    public static DomainEntitySet<EftpsFile> findAllEftpsFiles() {

        return Application.find(EftpsFile.class);
    }

//    public static void updateFormTemplate(String pPsid,String pFilingForm)
//    {
//        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
//        DomainEntitySet<CompanyAgency> companyAgencies = company.getCompanyAgencyCollection();
//        CompanyAgencyDTO companyAgencyDTO  = PayrollServices.dtoFactory.create(companyAgencies.get(0));
//
//        List formTemplateDtoList = new ArrayList<FormTemplateDTO>();
//        FormTemplateDTO ftDTO = new FormTemplateDTO();
//        ftDTO.setFilerType(pFilingForm);
//        ftDTO.setEffectiveDate(PSPDate.getPSPTime());
//        formTemplateDtoList.add(ftDTO);
//
//        companyAgencyDTO.setFormTemplateDtoList(formTemplateDtoList);
//
//        ProcessResult result =
//                PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), com.intuit.sbd.payroll.psp.domain.Agency.IRS, companyAgencyDTO);
//        PayrollServices.commitUnitOfWork();
//        assertTrue(result.isSuccess());
//
////        PayrollServices.beginUnitOfWork();
////        updatedCompanyAgency =
////                CompanyAgency.findCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), com.intuit.sbd.payroll.psp.domain.Agency.IRS);
//
//    }

    public static List<RejectionInfo> induceEnrollmentRejectInfo(String pFileName) {
        return induceEnrollmentRejectInfo(pFileName, "1101");
    }

    public static List<RejectionInfo> induceEnrollmentRejectInfo(String pFileName, String pRejectionCode)
    {
        DomainEntitySet<EftpsFile> list = EftpsFile.getCompletedEftpsFiles();

        TFASimulator simulator = new TFASimulator();
        List<RejectionInfo> pRejectionInfos = new ArrayList<RejectionInfo>();
        String txid = "0";

        try {
            // Read created 838 file
            EdiEftpsRecordList inputFile = new EdiEftpsRecordList(new File(pFileName).getCanonicalPath());
            List<EDIRecordTemplate> recordList = inputFile.getRecordList();

            // browse through each record of 813
            for (EDIRecordTemplate ediRecordTemplate : recordList) {

                //Identify LX segment
                if (ediRecordTemplate.getId() == RecordId.EDI_838_SEG_LX) {
                    txid = ediRecordTemplate.getFieldValue(FieldId.EDI_838_SEG_LX01);
                    pRejectionInfos.add(new RejectionInfo(txid, pRejectionCode)); //Induce an error.
                    break; //
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue("Error processing payment file  Simulator.", "".equals("something."));
        }
        return pRejectionInfos;
    }

    public static void deleteAllFsetTestDirFiles() {
        deleteFiles(BatchUtils.getTaxAgencyConfigString("psp_fset_send_dir"));
    }
}
