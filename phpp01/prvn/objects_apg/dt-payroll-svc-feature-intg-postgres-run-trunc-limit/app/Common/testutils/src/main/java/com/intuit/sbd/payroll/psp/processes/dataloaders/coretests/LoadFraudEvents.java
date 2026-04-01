package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB2DataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 27, 2008
 * Time: 2:21:22 PM
 */
public class LoadFraudEvents {

    private static Company1Dataloader c1dl;
    private static Company2Dataloader c2dl;

    public static void before() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadFraudEventData() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();
        Company company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DataLoadServices.addCompanyBankAccount(company);


        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PSPDate.setPSPTime(company.getSignUpDate());
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Collection<PaycheckDTO> payChecks = existingPayrollRun.getPaychecks();
        BigDecimal amount = new BigDecimal("10001.00");
        for (PaycheckDTO currPaycheck : payChecks) {
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(amount);
            }
        }
        Company1Dataloader.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        FraudValue fraudValue = FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudPRNumberOfPayrollsInXDays);

        String previousFraudPRNumberOfPayrollsInXDaysValue = fraudValue.getValue();
        fraudValue.setValue("1");
        Application.save(fraudValue);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1DL.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        CompanyBankAccount companyBankAccount = null;
        DomainEntitySet< CompanyBankAccount> cba = CompanyBankAccount.findCompanyBankAccounts(company);
        companyBankAccount = cba.get(0);
        companyBankAccount.addVerificationTransaction();
        companyBankAccount.addVerificationTransaction();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        TransactionState completedTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Completed);
        for(FinancialTransaction fTransaction :verificationTransactions ) {
            fTransaction.setCurrentTransactionState(completedTransactionState);
        }
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        payrollRun.payrollProcessedTooSoon();
        PayrollServices.commitUnitOfWork();

        assertEquals("Company Fraud Flag ", true, company.getIsFlaggedForFraud());

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals("Messages size", 0, result.getMessages().size());
        Assert.assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = c1DL.getCompany1();
        company2.setCompanyId("48484848488");
        company2.setFein("847656466");
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company2);
        assertTrue(result2.isSuccess());
        Company domainCompany2 = result2.getResult();
        assertEquals(0, result2.getMessages().size());
        PayrollServices.commitUnitOfWork();



        // SAP needs a service
        PayrollServices.beginUnitOfWork();
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal("150.00"));

        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal("250.00"));
        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getCompanyId(), ddCompanyService);
        assertSuccess("addService", ddServiceAddProcessResult);

        company = Application.refresh(company);

        CompanyBankAccountDTO cbaDTO = PayrollServices.dtoFactory.create(company.getCompanyBankAccountCollection().getFirst());
        ProcessResult addBankAccountResult = PayrollServices.companyManager.addCompanyBankAccount(domainCompany2.getSourceSystemCd(), domainCompany2.getSourceCompanyId(), cbaDTO, false, true);
        Assert.assertTrue(addBankAccountResult.isSuccess());

        assertEquals(1, addBankAccountResult.getMessages().size());
        Message errorMessage = addBankAccountResult.getMessages().get(0);

        assertEquals("Message code", "1040", errorMessage.getMessageCode());
        assertEquals("Message text",
                "Company QBOE:48484848488 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());

        PayrollServices.commitUnitOfWork();

        // Restore source payroll parameter
        PayrollServices.beginUnitOfWork();

        Application.refresh(fraudValue);

        fraudValue.setValue(previousFraudPRNumberOfPayrollsInXDaysValue);
        Application.save(fraudValue);
        PayrollServices.commitUnitOfWork();

    }

    public static void loadFraudCompanyAndPayroll() {
        before();
        loadFraudEventData();
    }

    public static void loadCompanyAddEmployeeWithSameBankAccount() {
        before();
        addEmployeeWithSameBankAccountRunPayroll();
    }

    public static void addEmployeeWithSameBankAccountRunPayroll() {
        //Load Data for Company2
        PayrollServices.beginUnitOfWork();
        c2dl = new Company2Dataloader();
        c2dl.persistCompany2();
        c2dl.persistEmployee3BankAccount2();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PSPDate.setPSPTime(company2.getSignUpDate().toLocal());
        PayrollRunDTO payrollRunDTO1 = c2dl.getCompany2PR_MatchingEmployeeBankAccounts(new DateDTO("2007-10-02"));
        PayrollRunDTO payrollRunDTO2 = c2dl.getCompany2PR2_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO1);
        c2dl.persistPayrollRun(payrollRunDTO2);
        PayrollServices.commitUnitOfWork();

        // offload QBOE EE CR for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        // offload all txns for Company1
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE ER DB for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();
    }


    public static void testFraudulentPayrollsProcess_MultipleBatches() {
        before();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        // offload all txns for Company1
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071123000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071119000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch1 = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertNotNull("Fraud Batch", fraudBatch1);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NumberOfPayrollsPerDayExceeded, null, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Company1 Fraud Flag ", true, company.getIsFlaggedForFraud());
        assertEquals("Company Events", 1, companyEventsList.size());

        //Load Data for Company2
        PayrollServices.beginUnitOfWork();
        c2dl = new Company2Dataloader();
        c2dl.persistCompany2();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        PSPDate.setPSPTime(company2.getSignUpDate().toLocal());
        PayrollRunDTO payrollRunDTO = c2dl.getCompany2PR_ExceedsPaycheckLimit(new DateDTO("2007-10-02"));
        PayrollRunDTO payrollRunDTO2 = c2dl.getCompany2PR2_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
        c2dl.persistPayrollRun(payrollRunDTO2);
        PayrollServices.commitUnitOfWork();

        // offload QBOE EE CR for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE ER DB for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // call the txn process batch process for the date 11-30-2007
        processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch2 = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertNotNull("Fraud Batch", fraudBatch2);

        PayrollServices.beginUnitOfWork();
        company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBOE);
        companyEventsList = CompanyEvent.findCompanyEvents(company2,
                EventTypeCode.PayrollProcessedTooSoon, null, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Company2 Fraud Flag ", true, company2.getIsFlaggedForFraud());
        assertEquals("Company2 Events", 2, companyEventsList.size());
    }


    private static void loadCompany3Data() {
        // offload all txns for Company1
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        ProcessFraudulentPayrolls process;

        //Load Data for Company2
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c2dl = new Company3Dataloader();
        c2dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        PSPDate.setPSPTime(company2.getSignUpDate().toLocal());
        PayrollRunDTO payrollRunDTO = c2dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // offload QBOE EE CR for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE ER DB for company2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        Application.commitUnitOfWork();

        // call the txn process batch process for the date 11-30-2007
        // call the txn process batch process for the date 11-30-2007
        ProcessACHTransactions processACHTxns =  new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071130");
        PayrollServices.commitUnitOfWork();

        //call Fraudulent Payrolls Batch process
        PayrollServices.beginUnitOfWork();
        process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollFraudBatch fraudBatch2 = process.getFraudBatch();
        PayrollServices.commitUnitOfWork();

        assertNotNull("Fraud Batch", fraudBatch2);

        PayrollServices.beginUnitOfWork();
        company2 = Company.findCompany(c2dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company2,
                EventTypeCode.PayrollProcessedTooSoon, null, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Company3 Fraud Flag ", true, company2.getIsFlaggedForFraud());
        assertEquals("Company3 Events", 1, companyEventsList.size());

    }

    public static void testFraudulentPayrollsProcess_3Companies() {
        testFraudulentPayrollsProcess_MultipleBatches();
        loadCompany3Data();
    }

    private static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        //set up a company with 2 payrolls with different paycheck dates
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        SpcfCalendar testPaycheckDate = SpcfCalendar.createInstance(2007, 11, 21, SpcfTimeZone.getLocalTimeZone());
        for (int i = 0; i < 12; i++) {
            PayrollRunDTO currentPayrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            testTime.addDays(2);
            PSPDate.setPSPTime(testTime);
            testPaycheckDate.addDays(2);
            currentPayrollRunDTO.setTargetPayrollTXDate(new DateDTO(testPaycheckDate));
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    //    currDDTxn.setDDTransactionAmount(pAmount);
                }
            }

            c1dl.persistPayrollRun(currentPayrollRunDTO);
        }
    }

    public static void load6PayrolLEventsFrom4Companies(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loadMultipleCompaniesAndPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071104000000");
        PayrollServices.commitUnitOfWork();

        // run fraudulent payroll batch job
        PayrollServices.beginUnitOfWork();
        ProcessFraudulentPayrolls process = new ProcessFraudulentPayrolls();
        process.processFraudulentPayrolls();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20091029000000");
        Application.commitUnitOfWork();

        LoadFraudEvents.loadFraudEventData();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20100429000000");
        Application.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        CompanyQB2DataLoader qb2dl = new CompanyQB2DataLoader();
        qb2dl.persistQBCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO cbaDto = new CompanyBankAccountDTO();
        BankAccountDTO baDto = new BankAccountDTO();
        baDto.setAccountNumber("123098");
        baDto.setAccountType(BankAccountType.Checking);
        baDto.setBankName("Bank of America");
        baDto.setRoutingNumber("263182914");
        cbaDto.setBankAccountDTO(baDto);
        cbaDto.setCompanyBankAccountID("123");
        cbaDto.setSourceBankAccountName("Blah");
        PayrollServices.companyManager.addCompanyBankAccount(qb2dl.getCompany1().getSourceSystemCd(), qb2dl.getCompany1().getCompanyId(), cbaDto, true, true);
        PayrollServices.commitUnitOfWork();
    }

    private static void loadMultipleCompaniesAndPayrolls() {
        Collection<PayrollRunDTO> payrollDTOs = new ArrayList<PayrollRunDTO>();
        payrollDTOs.clear();

        Company2Dataloader company2DataLoader = new Company2Dataloader();
        Company company2 = company2DataLoader.persistCompany2();
        payrollDTOs.add(company2DataLoader.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company2DataLoader.getCompany2PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company2, payrollDTOs);
        payrollDTOs.clear();
        PSPDate.addDaysToPSPTime(-8);

        Company3Dataloader company3DataLoader = new Company3Dataloader();
        Company company3 = company3DataLoader.persistCompany3();
        payrollDTOs.add(company3DataLoader.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02")));
        payrollDTOs.add(company3DataLoader.getCompany3PR2_DoesNotExceedLimits(new DateDTO("2007-10-09")));
        submitPayroll(company3,payrollDTOs);
        payrollDTOs.clear();
    }

    private static void submitPayroll(Company pCompany, Collection<PayrollRunDTO> payrollRunDTOs) {
        for (PayrollRunDTO payrollRunDTO: payrollRunDTOs) {
            ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(result);
        }
    }
}
